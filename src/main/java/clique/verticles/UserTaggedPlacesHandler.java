package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.ArrayList;
import java.util.List;

import clique.config.DBConfig;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserTaggedPlacesHandler extends Handler{

	@Override
	public String getHandlerName() {
		return "userTaggedPlaces";
	}

	@Override
	public String getQuery(Message<JsonObject> message) {
		String userId = message.body().getString("userId");
		return userId + "/tagged_places?fields=place{id}";		
	}

	@Override
	public void save(JsonObject data, Message<JsonObject> message) {
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
				r.table("Users").get(message.body().getString("userId"))
						.update(user -> r.hashMap("places", user.g("places").add(places).distinct()))
						.run(DBConfig.get());
			}
			
			nextHandler(data, message);
		}
	}
}
