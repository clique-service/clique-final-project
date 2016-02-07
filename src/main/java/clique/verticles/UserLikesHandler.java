package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.ArrayList;
import java.util.List;

import clique.config.DBConfig;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserLikesHandler extends Handler {
	@Override
	public void save(JsonObject data, Message<JsonObject> message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();
		String accessToken = message.body().getString("accessToken");

		if (size != 0) {
			List<String> likeIds = new ArrayList<>();
			List<String> categories = new ArrayList<>();
			
			jsonArray.stream().forEach(like -> {
				JsonObject likeData = new JsonObject();
				
				String likeId = ((JsonObject) like).getString("id");
				likeIds.add(likeId);
				String category = ((JsonObject) like).getString("category");

				if (category != null && !category.isEmpty()) {
					categories.add(category.toLowerCase());
				}
				
				likeData.put("accessToken", accessToken);
				likeData.put("likeId", likeId);
				likeData.put("after", "");
				likeData.put("category", category);
				
				vertx.eventBus().send("likePosts", likeData);

			});

			DBConfig.execute(
					r.table("Users").get(message.body().getString("userId"))
					.update(user -> r.hashMap("likes", user.g("likes").add(likeIds).distinct()))
			);

			if (categories != null && !categories.isEmpty()) {
				DBConfig.execute(
						r.table("Users").get(message.body().getString("userId"))
						.update(user -> r.hashMap("categories", user.g("categories").add(categories).distinct()))
				);
			}

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
