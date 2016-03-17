package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.ArrayList;
import java.util.List;

import clique.config.DBConfig;
import com.rethinkdb.gen.ast.ReqlExpr;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class PostLikesHandler extends Handler {

	@Override
	public String getHandlerName() {
		return "postLikes";
	}

	@Override
	public String getQuery(Message<JsonObject> message) {
		String postId = message.body().getString("postId");
		return postId + "/likes?fields=name,id,profile_type";
	}

	@Override
	public void save(JsonObject data, Message<JsonObject> message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();
		String likeId = message.body().getString("likeId");
		String category = message.body().getString("category");

		List<String> categories = new ArrayList<>();

		if (category != null && !category.isEmpty()) {
			categories.add(category.toLowerCase());
		}

		if (size != 0) {
			jsonArray.stream().forEach(attend -> {
				if (((JsonObject) attend).getString("profile_type").equals("user")) {
					String name = ((JsonObject) attend).getString("name").toLowerCase();
					String id = ((JsonObject) attend).getString("id");

					ReqlExpr expr = r.branch(
							r.table("Users")
									.get(id),
							r.table("Users").get(id)
									.update(user -> r.hashMap("likes", user.g("likes").add(r.array(likeId)).distinct())
											.with("categories", user.g("categories").add(categories).distinct())),
							r.table("Users")
									.insert(r.hashMap().with("id", id).with("name", name.toLowerCase())
											.with("likes", r.array(likeId)).with("categories", categories)
											.with("events", r.array()).with("places", r.array())));
					DBConfig.execute(expr);
				}
			});

			nextHandler(data, message);
		}

	}
}
