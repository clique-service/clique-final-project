package clique.verticles;

import clique.base.Handler;
import clique.config.DBConfig;
import com.hazelcast.spi.UrgentSystemOperation;
import com.rethinkdb.gen.ast.ReqlExpr;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.rethinkdb.RethinkDB.r;

public class UserLikesHandler extends Handler {
	/**
	 * @param likeId
	 * @return whether a likeId is already saved somewhere in our DB or not.
	 */
	private boolean isLikeExist(String likeId) {
		ReqlExpr expr = r.table("Users").getAll(likeId).optArg("index", "likes").nth(0).do_((x) -> true).default_(false);
		return DBConfig.execute(expr);
	}

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

				// Only take likes that are not in DB!
				if (!isLikeExist(likeId)) {
					System.out.println("likes: cache miss!");
					bus.send("likePosts", likeData);
				} else {
					System.out.println("likes: cache hit!");
				}
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
