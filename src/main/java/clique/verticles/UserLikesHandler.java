package clique.verticles;

import clique.base.Handler;
import clique.config.DBConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.rethinkdb.RethinkDB.r;

public class UserLikesHandler extends Handler {
	@Override
	public void save(JsonObject data, JsonObject message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();
		String accessToken = message.getString("accessToken");

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

				bus.send("likePosts", likeData);
			});

			DBConfig.execute(r.table("Users").get(message.getString("userId"))
					.update(user -> r.hashMap("likes", user.g("likes").add(likeIds).distinct())));

			if (categories != null && !categories.isEmpty()) {
				DBConfig.execute(r.table("Users").get(message.getString("userId"))
						.update(user -> r.hashMap("categories", user.g("categories").add(categories).distinct())));
			}

			nextHandler(data, message);
		} else {
			System.out.println("finish " + getHandlerName());
			bus.send("finishedLikes: " + message.getString("userId"), new JsonObject());
		}
	}

	@Override
	public String getHandlerName() {
		return "userLikes";
	}

	@Override
	public String getQuery(JsonObject message) {
		String userId = message.getString("userId");
		return userId + "/likes?fields=category,name,id";
	}
}