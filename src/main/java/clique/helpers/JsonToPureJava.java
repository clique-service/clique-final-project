package clique.helpers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by schniz on 05/02/2016.
 * Converts Vert.x 3 JsonArray to pure java
 * for using it with RethinkDB
 */
public class JsonToPureJava {
	public static Object toJava(Object json) {
		if (json instanceof JsonObject) {
			Map<String, Object> map = ((JsonObject) json).getMap();
			Map<String, Object> newMap = new HashMap<>();

			for (String key : map.keySet()) {
				newMap.put(key, toJava(map.get(key)));
			}

			return newMap;
		} else if (json instanceof JsonArray) {
			return ((JsonArray) json).stream().map(JsonToPureJava::toJava).toArray();
		}

		return json;
	}
}
