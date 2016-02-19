package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.ArrayList;
import java.util.List;

import clique.config.DBConfig;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserEventsHandler extends Handler {
	@Override
	public String getHandlerName() {
		return "userEvents";
	}

	@Override
	public String getQuery(Message<JsonObject> message) {
		String userId = message.body().getString("userId");
		return userId + "/events?fields=id,name,place";
	}

	@Override
	public void save(JsonObject data, Message<JsonObject> message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();

		List<String> eventsIds = new ArrayList<>();
		List<String> eventsPlaces = new ArrayList<>();

		if (size != 0) {
			jsonArray.stream().forEach(event -> {
				String eventId = ((JsonObject) event).getString("id");
				String accessToken = message.body().getString("accessToken");
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

				vertx.eventBus().send("eventAttendees", eventData);
				vertx.eventBus().send("eventInteresteds", eventData);
				vertx.eventBus().send("eventMaybes", eventData);
			});

			DBConfig.execute(r.table("Users").get(message.body().getString("userId"))
					.update(user -> r.hashMap("events", user.g("events").add(eventsIds).distinct())));

			if (eventsPlaces != null && !eventsPlaces.isEmpty()) {
				DBConfig.execute(r.table("Users").get(message.body().getString("userId"))
						.update(user -> r.hashMap("places", user.g("places").add(eventsPlaces).distinct())));
			}

			nextHandler(data, message);
		}
		else
		{
			System.out.println("finish " + getHandlerName());
			vertx.eventBus().send("finishedEvents: " + message.body().getString("userId"), "");
		}
	}
}
