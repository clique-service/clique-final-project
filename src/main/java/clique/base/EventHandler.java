package clique.base;

import clique.config.DBConfig;
import com.rethinkdb.gen.ast.ReqlExpr;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.rethinkdb.RethinkDB.r;

public abstract class EventHandler extends Handler {
	@Override
	public void save(JsonObject data, JsonObject message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();
		String place = message.getString("place");
		String eventId = message.getString("eventId");
		List<String> places = new ArrayList<>();

		if (place != null && !place.isEmpty()) {
			places.add(place);
		}

		if (size != 0) {
			jsonArray.stream().forEach(attend -> {
				String name = ((JsonObject) attend).getString("name").toLowerCase();
				String id = ((JsonObject) attend).getString("id");

				ReqlExpr expr = r
						.branch(r
								.table("Users").get(
										id),
								r.table("Users").get(id)
										.update(user -> r
												.hashMap("events", user.g("events").add(r.array(eventId)).distinct())
												.with("places", user.g("places").add(places).distinct())),
						r.table("Users")
								.insert(r.hashMap().with("id", id).with("name", name.toLowerCase())
										.with("events", r.array(eventId)).with("places", places)
										.with("likes", r.array()).with("categories", r.array())));
				DBConfig.execute(expr);
			});

			nextHandler(data, message);
		}
	}

	abstract public String getField();

	@Override
	public String getQuery(JsonObject message) {
		String eventId = message.getString("eventId");
		String string = eventId + "/" + getField() + "?fields=id,name";
		return string;
	}
}
