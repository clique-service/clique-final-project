package clique.verticles;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import static com.rethinkdb.RethinkDB.r;

import clique.config.DBConfig;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;

import clique.config.FacebookConfig;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Get;
import com.rethinkdb.gen.ast.ReqlExpr;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.sockjs.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;

/**
 * Providing Facebook Login capabilities over HTTP
 */
public class FacebookAuthenticate extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		TemplateEngine engine = HandlebarsTemplateEngine.create();
		Router router = Router.router(vertx);
		SessionStore sessionStore = LocalSessionStore.create(vertx);
		SessionHandler sessionHandler = SessionHandler.create(sessionStore);
		router.route().handler(sessionHandler);
		router.get("/").handler(index());
		router.get("/logo.png").handler(image());
		router.get("/privacy-policy").handler(privacyPolicy());
		router.get("/auth/facebook").handler(authenticate());
		router.get("/auth/facebook/callback").handler(startFetching());
		router.get("/changes/:userId").handler(req -> {
			try {
				Template template = new Handlebars().compile("changes");
				String page = template.apply(req.request().getParam("userId"));
				req.response().end(page);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		vertx.createHttpServer().requestHandler(router::accept).listen(9000);

		SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);

		SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

		sockJSHandler.socketHandler(sockJSSocket -> {
			// Just echo the data back
			sockJSSocket.handler(buffer -> {
				if (buffer.toString().equals("close")) {
					sockJSSocket.close();
					return;
				}

				JsonObject entries = buffer.toJsonObject();

				String address = entries.getString("address");
				JsonObject body = entries.getJsonObject("body");
				if (address != null && address.equals("auth")) {
					ReqlExpr get = r.table("ChangesUsers").get(body.getString("userId")).default_(r.hashMap("userId", null)).getField("userId");
					String userId = DBConfig.execute(get);
					if (userId == null) {
						sockJSSocket.close();
					} else {
						sockJSSocket.write(Buffer.buffer(new JsonObject().put("realId", userId).toString()));

						TopMatchesChanges topMatchesChanges = new TopMatchesChanges(userId);

						topMatchesChanges.getObservable().subscribe(data -> {
							sockJSSocket.write(Buffer.buffer(Json.encode(data)));
						});



						sockJSSocket.close();
					}
					return;
				}
			});
		});

		router.route("/eventbus/*").handler(sockJSHandler);
	}

	private Handler<RoutingContext> image() {
		return rc -> {
			rc.response().putHeader("Content-Type", "image/png").sendFile("src/main/resources/logo.png");
		};
	}

	private Handler<RoutingContext> privacyPolicy() {
		return rc -> {
			rc.response().putHeader("Content-Type", "text/html").sendFile("src/main/resources/terms.html");
		};
	}

	public Handler<RoutingContext> index() {
		return rc -> {
			rc.response().putHeader("Content-Type", "text/html").sendFile("src/main/resources/index.html");
		};
	}

	public Handler<RoutingContext> authenticate() {
		OAuthService service = new ServiceBuilder().provider(FacebookApi.class).scope(FacebookConfig.scope())
				.apiKey(FacebookConfig.appId()).apiSecret(FacebookConfig.appSecret())
				.callback(FacebookConfig.redirectURI()).build();
		return rc -> {
			Token token = new Token(UUID.randomUUID().toString(), FacebookConfig.appId());
			String authorizationUrl = service.getAuthorizationUrl(token);
			rc.response().setStatusCode(302).putHeader("Location", authorizationUrl).end();
		};
	}

	/**
	 * Get the code from the user
	 * 
	 * @return Handler that fetches
	 */
	private Handler<RoutingContext> startFetching() {
		return rc -> {
			String code = rc.request().getParam("code");
			fetchToken(code, userId -> {
				Object newUserId = null;
				try {
					newUserId = DBConfig.execute(r.table("ChangesUsers").insert(r.hashMap("userId", userId)).g("generated_keys"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				String x = ((List<Object>)newUserId).get(0).toString();
				rc.response().setStatusCode(302).putHeader("Location", "/changes/" + x).end();
			});
		};
	}

	/**
	 * Fetches an access token for the provided Facebook Authentication code
	 *
	 * @param code
	 *            provided by facebook
	 *
	 * @param handler
	 * 		      to handle the ID
	 */
	private void fetchToken(String code, Handler<String> handler) {
		HttpClient facebookClient = FacebookConfig.getHttpFacebookClient(vertx);
		facebookClient.getNow("/v2.5/oauth/access_token?client_id=" + FacebookConfig.appId() + "&redirect_uri="
				+ FacebookConfig.redirectURI() + "&client_secret=" + FacebookConfig.appSecret() + "&code=" + code,
				response -> {
					if (response.statusCode() != 200) {
						return;
					}
					response.bodyHandler(body -> {

						String accessToken = body.toJsonObject().getString("access_token");
						System.out.println(accessToken);

						facebookClient.getNow(FacebookConfig.query("me", accessToken), meResponse -> {
							if (meResponse.statusCode() != 200)
							{
								return;
							}
							meResponse.bodyHandler(meBody -> {
								System.out.println("got " + meBody.toJsonObject());
								String id = meBody.toJsonObject().getString("id");
								handler.handle(id);
								onAuthenticated(accessToken, id);
							});
						});
					});
				});
	}

	/**
	 * Happens when a user authenticates successfully with an access token and
	 * an ID Its launching the fetchers, basically.
	 * 
	 * @param accessToken
	 *            to grab data from facebook with
	 * @param userId
	 *            that was authenticated
	 */
	private void onAuthenticated(String accessToken, String userId) {
		vertx.eventBus().send("userToken", new JsonObject().put("userId", userId).put("accessToken", accessToken));
	}
}
