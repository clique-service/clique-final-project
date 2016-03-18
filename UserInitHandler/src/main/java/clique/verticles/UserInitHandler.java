package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import com.rethinkdb.gen.ast.ReqlExpr;

import clique.config.DBConfig;
import clique.config.FacebookConfig;
import clique.helpers.FinishChecker;
import clique.helpers.JsonToPureJava;
import clique.helpers.MessageBus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserInitHandler extends AbstractVerticle {
	private MessageBus bus;
	public void start() {
		bus = new MessageBus();
		
		HttpClient client = FacebookConfig.getHttpFacebookClient(vertx);
		bus.consume("userInit", message -> {
			System.out.println("Initializing user data");

			String accessToken = message.getString("accessToken");
			String userId = message.getString("userId");

			client.getNow(initUserQuery(userId, accessToken), response -> {
				response.bodyHandler(body -> {
					saveUser(body.toJsonObject());

					JsonObject data = new JsonObject();
					data.put("accessToken", accessToken);
					data.put("userId", userId);
					data.put("after", "");

					bus.send("userLikes", data);
					bus.send("userEvents", data);
					bus.send("userTaggedPlaces", data);

					FinishChecker isFinish = new FinishChecker();
					bus.consume("finishedLikes: " + userId, likesMessage -> {
						isFinish.setLikes(true);
						isAllDone(isFinish, userId);
					});
					bus.consume("finishedTaggedPlaces: " + userId, taggedPlacesMessage -> {
						isFinish.setTaggedPlaces(true);
						isAllDone(isFinish, userId);
					});
					bus.consume("finishedEvents: " + userId, eventsMessage -> {
						isFinish.setEvents(true);
						isAllDone(isFinish, userId);
					});
				});
			});
		});
	}

	private void isAllDone(FinishChecker checker, String userId) {
		if (checker.isAllDone()) {
			bus.send("sharedTableDataInsertion", new JsonObject().put("userId", userId));
		}
	}

	private String initUserQuery(String userId, String accessToken) {
		return FacebookConfig.query(
				userId + "?fields=id,name,birthday,languages{id,name},relationship_status,gender,age_range,hometown,location,education,work",
				accessToken);
	}

	private void saveUser(JsonObject userData) {
		System.out.println("Saving user...");

		JsonObject defaults = new JsonObject();
		defaults.put("events", new JsonArray());
		defaults.put("likes", new JsonArray());
		defaults.put("places", new JsonArray());
		defaults.put("categories", new JsonArray());
		
		
		ReqlExpr user = r.table("Users").get(userData.getString("id")).default_(false);
		ReqlExpr insert = r.table("Users").insert(JsonToPureJava.toJava(userData.copy().mergeIn(defaults))).optArg("conflict", "replace");
		ReqlExpr update = user.update(JsonToPureJava.toJava(userData));
		
		DBConfig.execute(r.branch(user, update, insert));
	}
}
