package clique.helpers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schniz and tom boldan on 05/02/2016. Converts Vert.x 3 JsonArray to pure java for
 * using it with RethinkDB
 */
public class JsonToPureJava {
	public static Object toJava(Object json) {
		if (json instanceof JsonObject) {
			return toJava(((JsonObject) json).getMap());
		} else if (json instanceof JsonArray) {
			return toJava(((JsonArray) json).getList());
		} else if (json instanceof String) {
			return ((String) json).toLowerCase();
		} else if (json instanceof List<?>) {
			return ((List<?>) json).stream().map(JsonToPureJava::toJava).toArray();
		} else if (json instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) json;
			Map<Object, Object> newMap = new HashMap<>();

			for (Object key : map.keySet()) {
				newMap.put(key, toJava(map.get(key)));
			}

			return newMap;
		}

		return json;
	}
}
