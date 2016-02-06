package clique.verticles;

import clique.config.FacebookConfig;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;

/**
 * Created by schniz on 06/02/2016.
 */
public class FacebookAuthenticate extends AbstractVerticle {

	public Handler<RoutingContext> authenticate(OAuthService service) {
		return rc -> {
			Token token = service.getRequestToken();
			rc.session().put("token", token);
			String authorizationUrl = service.getAuthorizationUrl(token);
			rc.response().setStatusCode(301).putHeader("Location", authorizationUrl).end();
		};
	}

	@Override
	public void start() throws Exception {
		OAuthService service = new ServiceBuilder().provider(FacebookApi.class).scope(FacebookConfig.scope()).apiKey(FacebookConfig.appId()).apiSecret(FacebookConfig.appSecret()).callback(FacebookConfig.redirectURI()).build();
		ClusteredSessionStore sessionStore = ClusteredSessionStore.create(vertx);
		Router router = Router.router(vertx);
		router.route().handler(SessionHandler.create(sessionStore));
		router.get("/").handler(authenticate(service));
		router.get("/auth/facebook/callback").handler(getAccessToken(service));

		vertx.createHttpServer().requestHandler(router::accept).listen(9000);
	}

	private Handler<RoutingContext> getAccessToken(OAuthService service) {
		return rc -> {
			String code = rc.request().getParam("code");
			vertx.createHttpClient(new HttpClientOptions().setSsl(true)).getNow(443, "graph.facebook.com", "/v2.5/oauth/access_token?client_id=" + FacebookConfig.appId() + "&redirect_uri=" + FacebookConfig.redirectURI() + "&client_secret=" + FacebookConfig.appSecret() + "&code=" + code, response -> {
				response.bodyHandler(body -> {
					String accessToken = body.toJsonObject().getString("access_token");
					rc.response().end(accessToken);
				});
			});
		};
	}

	private void onAuthenticated(String accessToken, String userId) {

	}
}
