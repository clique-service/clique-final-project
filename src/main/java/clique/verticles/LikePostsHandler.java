package clique.verticles;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class LikePostsHandler extends Handler {

	@Override
	public String getHandlerName() {
		return "likePosts";
	}

	@Override
	public String getQuery(Message<JsonObject> message) {
		String likeId = message.body().getString("likeId");
		return likeId + "/posts?fields=id,message,from";
	}

	@Override
	public void save(JsonObject data, Message<JsonObject> message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();
		String likeId = message.body().getString("likeId");
		String category = message.body().getString("category");
		String accessToken = message.body().getString("accessToken");

		if (size != 0) {
			jsonArray.stream().forEach(post -> {
				JsonObject postData = new JsonObject();
				postData.put("accessToken", accessToken);
				postData.put("likeId", likeId);
				postData.put("postId", ((JsonObject) post).getString("id"));
				postData.put("after", "");
				postData.put("category", category);

				vertx.eventBus().send("postLikes", postData);

			});
			
			nextHandler(data, message);
		}
	}
}
