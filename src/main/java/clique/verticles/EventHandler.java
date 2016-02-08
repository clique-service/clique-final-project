package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.ArrayList;
import java.util.List;

import com.rethinkdb.gen.ast.ReqlExpr;

import clique.config.DBConfig;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class EventHandler extends Handler {
	@Override
	public void save(JsonObject data, Message<JsonObject> message) {
		JsonArray jsonArray = data.getJsonArray("data");
		int size = jsonArray.size();
		String place = message.body().getString("place");
		String eventId = message.body().getString("eventId");
		List<String> places = new ArrayList<>();
		
		if (place != null && !place.isEmpty())
		{
			places.add(place);
		}

		if (size != 0) {
			jsonArray.stream().forEach(attend -> {
				String name = ((JsonObject) attend).getString("name").toLowerCase();
				String id = ((JsonObject) attend).getString("id");

				ReqlExpr expr = r.branch(r.table("Users").get(id),
						r.table("Users").get(id)
								.update(user -> r.hashMap("events", user.g("events").add(eventId).distinct())
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
	public String getQuery(Message<JsonObject> message) {
		String eventId = message.body().getString("eventId");
		String string = eventId + "/" + getField() + "?fields=id,name";
		return string;
	}
}
