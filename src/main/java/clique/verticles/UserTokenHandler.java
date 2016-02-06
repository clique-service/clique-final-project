package clique.verticles;

import clique.config.FacebookConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

public class UserTokenHandler extends AbstractVerticle {
	public void start() {
		EventBus eventBus = vertx.eventBus();
		eventBus.<JsonObject> consumer("userToken", message -> {
			System.out.println("Got token to refresh");

			HttpClientOptions opt = new HttpClientOptions();
			opt.setSsl(true);
			opt.setDefaultHost("graph.facebook.com");
			
			HttpClient client = vertx.createHttpClient(opt);
			try {
				client.getNow(443, "graph.facebook.com", getExtendAccessToken(getAccessToken(message)), response -> {
					response.bodyHandler(body -> {
						System.out.println("Token updated");
						String result = body.toString().split("=")[1];
						result = result.split("&")[0];
						
						JsonObject data = new JsonObject();
						data.put("accessToken", result);
						data.put("userId", message.body().getString("userId"));
						eventBus.send("userInit", data);
					});
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	private String getExtendAccessToken(String accessToken) throws Exception {
		if (accessToken.isEmpty()) {
			throw new Exception("Empty access token");
		}

		String extendAccessToken = "/oauth/access_token?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s";
		String format = String.format(extendAccessToken, FacebookConfig.appId(), FacebookConfig.appSecret(), accessToken);
		return format;
	}

	private String getAccessToken(Message<JsonObject> message) {
		return message.body().getString("accessToken", "");
	}
}
