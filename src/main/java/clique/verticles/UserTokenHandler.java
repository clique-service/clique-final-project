
package clique.verticles;

import clique.config.FacebookConfig;
import clique.helpers.MessageBus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

public class UserTokenHandler extends AbstractVerticle {
	private MessageBus bus;
	public void start() {
		bus = new MessageBus();
		HttpClient client = FacebookConfig.getHttpFacebookClient(vertx);
		
		bus.consume("userToken", message -> {
			System.out.println("Got token to refresh");

			try {
				client.getNow(getExtendAccessToken(getAccessToken(message)), response -> {
					if (response.statusCode() != 200) {
						return;
					}
					response.bodyHandler(body -> {
						System.out.println("Token updated");
						String result = body.toString().split("=")[1];
						result = result.split("&")[0];

						JsonObject data = new JsonObject();
						data.put("accessToken", result);
						data.put("userId", message.getString("userId"));
						bus.send("userInit", data);
					});
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private String getExtendAccessToken(String accessToken) throws Exception {
		if (accessToken.isEmpty()) {
			throw new Exception("Empty access token");
		}

		String extendAccessToken = "/oauth/access_token?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s";
		String format = String.format(extendAccessToken, FacebookConfig.appId(), FacebookConfig.appSecret(),
				accessToken);
		return format;
	}
	
	private String getAccessToken(JsonObject message) {
		return message.getString("accessToken", "");
	}
}
