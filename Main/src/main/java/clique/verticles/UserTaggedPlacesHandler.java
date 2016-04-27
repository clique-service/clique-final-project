package clique.verticles;

import clique.base.Handler;
import clique.config.DBConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.rethinkdb.RethinkDB.r;

public class UserTaggedPlacesHandler extends Handler {

	@Override
	public String getHandlerName() {
		return "userTaggedPlaces";
	}

	@Override
	public String getQuery(JsonObject message) {
		String userId = message.getString("userId");
		return userId + "/tagged_places?fields=place{id}";
	}

	@Override
	public void save(JsonObject data, JsonObject message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();

		List<String> places = new ArrayList<>();

		if (size != 0) {
			jsonArray.stream().forEach(tag -> {
				JsonObject place = ((JsonObject) tag).getJsonObject("place");

				if (place != null && !place.isEmpty() && place.getString("id") != null) {
					places.add(place.getString("id"));
				}
			});

			if (places != null && !places.isEmpty()) {
				DBConfig.execute(r.table("Users").get(message.getString("userId"))
						.update(user -> r.hashMap("places", user.g("places").add(places).distinct())));
			}

			nextHandler(data, message);
		} else {
			System.out.println("finish " + getHandlerName());
			bus.send("finishedTaggedPlaces: " + message.getString("userId"), new JsonObject());
		}
	}
}
