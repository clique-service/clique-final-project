package clique.verticles;

import clique.config.FacebookConfig;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Providing Facebook Login capabilities over HTTP
 */
public class FacebookAuthenticate extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		router.get("/").handler(authenticate());
		router.get("/auth/facebook/callback").handler(startFetching());
		vertx.createHttpServer().requestHandler(router::accept).listen(9000);
	}

	public Handler<RoutingContext> authenticate() {
		OAuthService service = new ServiceBuilder().provider(FacebookApi.class).scope(FacebookConfig.scope()).apiKey(FacebookConfig.appId()).apiSecret(FacebookConfig.appSecret()).callback(FacebookConfig.redirectURI()).build();
		return rc -> {
			Token token = service.getRequestToken();
			String authorizationUrl = service.getAuthorizationUrl(token);
			rc.response().setStatusCode(301).putHeader("Location", authorizationUrl).end();
		};
	}

	/**
	 * Get the code from the user
	 * @return Handler that fetches
	 */
	private Handler<RoutingContext> startFetching() {
		return rc -> {
			String code = rc.request().getParam("code");
			rc.response().end("starting to work.");
			fetchToken(code);
		};
	}

	/**
	 * Fetches an access token for the provided Facebook Authentication code
	 * @param code provided by facebook
	 */
	private void fetchToken(String code) {
		HttpClient facebookClient = FacebookConfig.getHttpFacebookClient(vertx);
			facebookClient.getNow("/v2.5/oauth/access_token?client_id=" + FacebookConfig.appId() + "&redirect_uri=" + FacebookConfig.redirectURI() + "&client_secret=" + FacebookConfig.appSecret() + "&code=" + code, response -> {
				response.bodyHandler(body -> {
					String accessToken = body.toJsonObject().getString("access_token");

					facebookClient.getNow(FacebookConfig.query("me", accessToken), meResponse -> meResponse.bodyHandler(meBody -> {
						String id = meBody.toJsonObject().getString("id");
						onAuthenticated(accessToken, id);
					}));
				});
			});
	}

	/**
	 * Happens when a user authenticates successfully with an access token and an ID
	 * Its launching the fetchers, basically.
	 * @param accessToken to grab data from facebook with
	 * @param userId that was authenticated
	 */
	private void onAuthenticated(String accessToken, String userId) {
		vertx.eventBus().send("userToken", new JsonObject().put("userId", userId).put("accessToken", accessToken));
	}
}
