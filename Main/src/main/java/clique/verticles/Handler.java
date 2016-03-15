package clique.verticles;

import clique.config.FacebookConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

public abstract class Handler extends AbstractVerticle {
	abstract public String getHandlerName();

	abstract public String getQuery(Message<JsonObject> message);

	public void start() {
		HttpClient client = FacebookConfig.getHttpFacebookClient(vertx);
		vertx.eventBus().<JsonObject> consumer(getHandlerName(), message -> {
			client.getNow(paging(message), response -> {
				if (response.statusCode() != 200)
				{
					return;
				}
				
				response.bodyHandler(body -> save(body.toJsonObject(), message));
			});
		});
	}

	abstract public void save(JsonObject data, Message<JsonObject> message);

	public String paging(Message<JsonObject> message) {
		String accessToken = message.body().getString("accessToken");
		String after = message.body().getString("after");

		String query = getQuery(message);

		if (after != null && !after.isEmpty()) {
			query += "&after=" + after;
		}

		return FacebookConfig.query(query, accessToken);
	}

	public void nextHandler(JsonObject data, Message<JsonObject> message) {
		EventBus eventBus = vertx.eventBus();
		JsonObject value = data.getJsonObject("paging").getJsonObject("cursors");
		message.body().put("after", value.getString("after"));
		eventBus.send(getHandlerName(), message.body());
	}
}