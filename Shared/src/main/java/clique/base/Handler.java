package clique.base;

import clique.config.FacebookConfig;
import clique.helpers.MessageBus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

public abstract class Handler extends AbstractVerticle {
	protected MessageBus bus;
	
	abstract public String getHandlerName();

	abstract public String getQuery(JsonObject message);

	public void start() {
		bus = new MessageBus();
		
		HttpClient client = FacebookConfig.getHttpFacebookClient(vertx);
		bus.consume(getHandlerName(), message -> {
			client.getNow(paging(message), response -> {
				if (response.statusCode() != 200)
				{
					return;
				}
				
				response.bodyHandler(body -> save(body.toJsonObject(), message));
			});
		});
	}

	abstract public void save(JsonObject data, JsonObject message);

	public String paging(JsonObject message) {
		String accessToken = message.getString("accessToken");
		String after = message.getString("after");

		String query = getQuery(message);

		if (after != null && !after.isEmpty()) {
			query += "&after=" + after;
		}

		return FacebookConfig.query(query, accessToken);
	}

	public void nextHandler(JsonObject data, JsonObject message) {
		JsonObject value = data.getJsonObject("paging").getJsonObject("cursors");
		message.put("after", value.getString("after"));
		bus.send(getHandlerName(), message);
	}
}