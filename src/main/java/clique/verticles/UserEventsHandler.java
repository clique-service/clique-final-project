package clique.verticles;

import clique.base.Handler;
import clique.config.DBConfig;
import com.rethinkdb.gen.ast.ReqlExpr;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.rethinkdb.RethinkDB.r;

public class UserEventsHandler extends Handler {
	@Override
	public String getHandlerName() {
		return "userEvents";
	}

	@Override
	public String getQuery(JsonObject message) {
		String userId = message.getString("userId");
		return userId + "/events?fields=id,name,place";
	}

	/**
	 * @param eventId
	 * @return whether a eventId is already saved somewhere in our DB or not.
	 */
	private boolean isEventExists(String eventId) {
		ReqlExpr expr = r.table("Users").getAll(eventId).optArg("index", "events").nth(0).do_((x) -> true).default_(false);
		return DBConfig.execute(expr);
	}

	@Override
	public void save(JsonObject data, JsonObject message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();

		List<String> eventsIds = new ArrayList<>();
		List<String> eventsPlaces = new ArrayList<>();

		if (size != 0) {
			jsonArray.stream().forEach(event -> {
				String eventId = ((JsonObject) event).getString("id");
				String accessToken = message.getString("accessToken");
				eventsIds.add(eventId);

				JsonObject eventData = new JsonObject();

				JsonObject place = ((JsonObject) event).getJsonObject("place");

				if (place != null && !place.isEmpty() && place.getString("id") != null) {
					eventsPlaces.add(place.getString("id"));
					eventData.put("place", place.getString("id"));
				} else {
					eventData.put("place", "");
				}

				eventData.put("accessToken", accessToken);
				eventData.put("eventId", eventId);
				eventData.put("after", "");

				if (!isEventExists(eventId)) {
					System.out.println("events: cache miss!");
					bus.send("eventAttendees", eventData);
					bus.send("eventInteresteds", eventData);
					bus.send("eventMaybes", eventData);
				} else {
					System.out.println("events: cache hit!");
				}
			});

			DBConfig.execute(r.table("Users").get(message.getString("userId"))
					.update(user -> r.hashMap("events", user.g("events").add(eventsIds).distinct())));

			if (eventsPlaces != null && !eventsPlaces.isEmpty()) {
				DBConfig.execute(r.table("Users").get(message.getString("userId"))
						.update(user -> r.hashMap("places", user.g("places").add(eventsPlaces).distinct())));
			}

			nextHandler(data, message);
		}
		else
		{
			System.out.println("finish " + getHandlerName());
			bus.send("finishedEvents: " + message.getString("userId"), new JsonObject());
		}
	}
}
