package clique.verticles;

import clique.config.DBConfig;
import clique.config.FacebookConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static com.rethinkdb.RethinkDB.r;

import java.util.List;
import java.util.stream.Collectors;

import com.rethinkdb.gen.ast.Add;

public class UserLikesHandler extends AbstractVerticle {
	public void start() {
		vertx.eventBus().<JsonObject> consumer("userLikes", message -> {
			HttpClient client = FacebookConfig.getHttpFacebookClient(vertx);

			System.out.println("Initializing user likes");

			client.getNow(usersLikesQuery(message), response -> {
				response.bodyHandler(body -> saveUserLikes(body.toJsonObject(), message));
			});
		});
	}

	private void saveUserLikes(JsonObject userData, Message<JsonObject> message) {
		JsonArray jsonArray = userData.getJsonArray("data");
		int size = jsonArray.size();

		if (size != 0) {
			List<String> newIds = jsonArray.stream().map(x -> ((JsonObject) x).getString("id")).collect(Collectors.toList());

			r.table("Users").get(message.body().getString("userId"))
					.update(user -> r.hashMap("likes", user.g("likes").add(newIds))).run(DBConfig.get());

			EventBus eventBus = vertx.eventBus();
			JsonObject value = userData.getJsonObject("paging").getJsonObject("cursors");
			message.body().put("after", value.getString("after"));
			eventBus.send("userLikes", message.body());
		}
	}

	private String usersLikesQuery(Message<JsonObject> message) {
		String accessToken = message.body().getString("accessToken");
		String userId = message.body().getString("userId");
		String after = message.body().getString("after");

		String query = userId + "/likes?fields=category,name,id";

		if (after != null && !after.isEmpty()) {
			query += "&after=" + after;
		}

		return FacebookConfig.query(query, accessToken);
	}

}
