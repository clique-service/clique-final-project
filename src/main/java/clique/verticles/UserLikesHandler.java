package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.List;
import java.util.stream.Collectors;

import clique.config.DBConfig;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserLikesHandler extends Handler {
	@Override
	public void save(JsonObject data, Message<JsonObject> message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();

		if (size != 0) {
			List<String> likeIds = jsonArray.stream().map(x -> ((JsonObject) x).getString("id")).collect(Collectors.toList());

			r.table("Users").get(message.body().getString("userId"))
					.update(user -> r.hashMap("likes", user.g("likes").add(likeIds).distinct())).run(DBConfig.get());

			nextHandler(data, message);
		}
	}

	@Override
	public String getHandlerName() {
		return "userLikes";
	}

	@Override
	public String getQuery(Message<JsonObject> message) {
		String userId = message.body().getString("userId");
		return userId + "/likes?fields=category,name,id";
	}
}
