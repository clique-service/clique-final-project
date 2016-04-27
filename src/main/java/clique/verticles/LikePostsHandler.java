package clique.verticles;

import clique.base.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class LikePostsHandler extends Handler {
	@Override
	public String getHandlerName() {
		return "likePosts";
	}

	@Override
	public String getQuery(JsonObject message) {
		String likeId = message.getString("likeId");
		return likeId + "/posts?fields=id,message,from";
	}

	@Override
	public void save(JsonObject data, JsonObject message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();
		String likeId = message.getString("likeId");
		String category = message.getString("category");
		String accessToken = message.getString("accessToken");

		if (size != 0) {
			jsonArray.stream().forEach(post -> {
				JsonObject postData = new JsonObject();
				postData.put("accessToken", accessToken);
				postData.put("likeId", likeId);
				postData.put("postId", ((JsonObject) post).getString("id"));
				postData.put("after", "");
				postData.put("category", category);

				bus.send("postLikes", postData);
			});
			
			nextHandler(data, message);
		}
	}
}
