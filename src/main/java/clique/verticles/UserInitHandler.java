package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import clique.config.DBConfig;
import clique.config.FacebookConfig;
import clique.helpers.FinishChecker;
import clique.helpers.JsonToPureJava;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserInitHandler extends AbstractVerticle {
	public void start() {
		HttpClient client = FacebookConfig.getHttpFacebookClient(vertx);

		vertx.eventBus().<JsonObject> consumer("userInit", message -> {
			System.out.println("Initializing user data");

			String accessToken = message.body().getString("accessToken");
			String userId = message.body().getString("userId");

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

					FinishChecker isFinish = new FinishChecker();
					vertx.eventBus().consumer("finishedLikes: " + userId, likesMessage -> {
						isFinish.setLikes(true);
						isAllDone(isFinish, userId);
					});
					vertx.eventBus().consumer("finishedTaggedPlaces: " + userId, taggedPlacesMessage -> {
						isFinish.setTaggedPlaces(true);
						isAllDone(isFinish, userId);
					});
					vertx.eventBus().consumer("finishedEvents: " + userId, eventsMessage -> {
						isFinish.setEvents(true);
						isAllDone(isFinish, userId);
					});
				});
			});
		});
	}

	private void isAllDone(FinishChecker checker, String userId) {
		if (checker.isAllDone()) {
		//	vertx.eventBus().send("sharedTableCreate", userId);
			vertx.eventBus().send("sharedTableDataInsertion", userId);
		}
	}

	private String initUserQuery(String userId, String accessToken) {
		return FacebookConfig.query(
				userId + "?fields=id,name,birthday,languages{id,name},relationship_status,gender,age_range,hometown,location,education,work",
				accessToken);
	}

	private void saveUser(JsonObject userData) {
		System.out.println("Saving user...");

		userData.put("events", new JsonArray());
		userData.put("likes", new JsonArray());
		userData.put("places", new JsonArray());
		userData.put("categories", new JsonArray());

		DBConfig.execute(r.table("Users").insert(JsonToPureJava.toJava(userData)).optArg("conflict", "replace"));
	}
}
