package clique.helpers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import static clique.helpers.JsonToPureJava.toJava;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by schniz on 06/02/2016.
 */
public class JsonToPureJavaTest {

	@Test
	public void testToJava() throws Exception {
		JsonObject simpleObject = new JsonObject().put("Hey", "ho");
		JsonArray simpleArray = new JsonArray().add("hey");

		assertEquals("test", toJava("test"));
		assertArrayEquals(new String[] { "hey" }, (Object [])toJava(simpleArray));
	}
}