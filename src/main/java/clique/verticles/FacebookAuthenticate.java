package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;
import com.rethinkdb.gen.ast.ReqlExpr;

import clique.config.DBConfig;
import clique.config.FacebookConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Providing Facebook Login capabilities over HTTP
 */
public class FacebookAuthenticate extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		router.get("/").handler(index());
		router.get("/logo.png").handler(image());
		router.get("/privacy-policy").handler(privacyPolicy());
		router.get("/auth/facebook").handler(authenticate());
		router.get("/auth/facebook/callback").handler(startFetching());
		router.get("/show/:id").handler(show());
		router.get("/changes/:id").handler(changes());
		router.route().handler(StaticHandler.create("src/main/resources"));
		vertx.createHttpServer().requestHandler(router::accept).listen(9000);
	}

	private Handler<RoutingContext> show() {
		return rc -> {
			String userId = rc.request().getParam("id");
			try {
				String file = Files.readAllLines(Paths.get("src/main/resources/thanks.html")).stream()
						.collect(Collectors.joining("\n"));
				String newFile = file.replaceAll("\\{\\{USER_ID\\}\\}", userId);
				rc.response().putHeader("Content-Length", String.valueOf(newFile.length())).putHeader("Content-Type", "text/html").write(newFile).end();
			} catch (Exception e) {
				e.printStackTrace();
				rc.response().write(e.getMessage()).end();
			}
		};
	}

	private Handler<RoutingContext> changes() {
		return rc -> {
			JsonObject jsonObject = new JsonObject();
			String userId = rc.request().params().get("id");
			String tableName = userId + "Shared";
			boolean tableExists = Boolean.valueOf(DBConfig.execute(r.tableList().contains(tableName)).toString());

			if (tableExists) {
				ReqlExpr sortedResults = r.table(tableName).orderBy().optArg("index", r.desc("rating")).limit(5)
						.coerceTo("array");
				List results = DBConfig.execute(sortedResults);
				jsonObject = new JsonObject().put("users", results).put("action", results.size() < 1 ? "WAIT_NO_DATA" : "SHOW_USERS");
			} else {
				ReqlExpr sortedResults = r.table("CliqueResults").filter(row -> {
					return row.g("userId").eq(userId);
				}).orderBy(r.desc("date")).limit(1).coerceTo("array");
				List results = DBConfig.execute(sortedResults);
				if (results.size() < 1) {
					jsonObject = new JsonObject().put("action", "WAIT_NO_DATA");
				} else {
					Map result = (Map)results.get(0);
					jsonObject = new JsonObject().put("users", result.get("results")).put("action", "FINISHED");
				}
			}

			String jsonString = jsonObject.toString();
			rc.response().putHeader("Content-Type", "application/json").end(jsonString);
		};
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
				rc.response().setStatusCode(302).putHeader("Location", "/show/" + userId).end();
			});
		};
	}

	/**
	 * Fetches an access token for the provided Facebook Authentication code
	 *
	 * @param code provided by facebook
	 */
	private void fetchToken(String code, Handler<String> foundUserIdHandler) {
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
							if (meResponse.statusCode() != 200) {
								return;
							}
							meResponse.bodyHandler(meBody -> {
								System.out.println("got " + meBody.toJsonObject());
								String id = meBody.toJsonObject().getString("id");
								createChangesTable(id);
								foundUserIdHandler.handle(id);
								onAuthenticated(accessToken, id);
							});
						});
					});
				});
	}

	private void createChangesTable(String userId) {
		String tableName = userId + "Shared";

		if (!Boolean.valueOf(DBConfig.execute(r.tableList().contains(tableName)).toString())) {
			DBConfig.execute(r.tableCreate(tableName));

			DBConfig.execute(r.table(tableName).indexCreate("rating", user -> user.g("events").mul(5)
					.add(user.g("likes").mul(3)).add(user.g("places").mul(2)).add(user.g("categories").mul(2))));
		}
	}

	/**
	 * Happens when a user authenticates successfully with an access token and
	 * an ID Its launching the fetchers, basically.
	 *
	 * @param accessToken to grab data from facebook with
	 * @param userId      that was authenticated
	 */
	private void onAuthenticated(String accessToken, String userId) {
		vertx.eventBus().send("userToken", new JsonObject().put("userId", userId).put("accessToken", accessToken));
	}
}
