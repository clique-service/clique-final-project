package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import clique.config.DBConfig;
import clique.config.FacebookConfig;
import clique.helpers.JsonToPureJava;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserInitHandler extends AbstractVerticle {
	public void start() {
		vertx.eventBus().<JsonObject> consumer("userInit", message -> {
			System.out.println("Initializing user data");

			String accessToken = message.body().getString("accessToken");
			String userId = message.body().getString("userId");

			HttpClient client = FacebookConfig.getHttpFacebookClient(vertx);

			client.getNow(initUserQuery(userId, accessToken), response -> {
				response.bodyHandler(body -> {
					saveUser(body.toJsonObject());

					JsonObject data = new JsonObject();
					data.put("accessToken", accessToken);
					data.put("userId", userId);
					data.put("after", "");

					vertx.eventBus().send("userLikes", data);
					vertx.eventBus().send("userEvents", data);
					vertx.eventBus().send("userTaggedPlaces", data);
				});
			});
		});
	}

	private String initUserQuery(String userId, String accessToken) {
		return FacebookConfig.query(
				userId + "?fields=id,name,birthday,languages{id,name},hometown,location,education,work", accessToken);
	}

	private void saveUser(JsonObject userData) {
		System.out.println("Saving user...");

		userData.put("events", new JsonArray());
		userData.put("likes", new JsonArray());
		userData.put("places", new JsonArray());
		userData.put("categories", new JsonArray());
		
		r.table("Users").insert(JsonToPureJava.toJava(userData)).optArg("conflict", "replace").run(DBConfig.get());
	}
}
