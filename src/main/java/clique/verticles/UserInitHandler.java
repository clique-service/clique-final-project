package clique.verticles;

import clique.config.DBConfig;
import clique.config.FacebookConfig;
import clique.helpers.JsonToPureJava;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import static com.rethinkdb.RethinkDB.r;


public class UserInitHandler extends AbstractVerticle {
	public void start() {
		vertx.eventBus().<JsonObject> consumer("userInit", message -> {
			System.out.println("Initializing user data");
			HttpClientOptions opt = new HttpClientOptions();
			opt.setSsl(true);
			opt.setDefaultHost("graph.facebook.com");
			HttpClient client = vertx.createHttpClient(opt);
			
			client.getNow(443, "graph.facebook.com", initUserQuery(message), response -> {
				response.bodyHandler(body -> saveUser(body.toJsonObject()));
			});
		});
	}

	private String initUserQuery(Message<JsonObject> message) {
		String accessToken = message.body().getString("accessToken");
		String userId = message.body().getString("userId");

		return FacebookConfig.query(userId + "?fields=id,name,birthday,languages{id},hometown,location,education,work",
				accessToken);
	}

	private void saveUser(JsonObject userData) {
		System.out.println("Saving user...");

		r.table("Users").insert(JsonToPureJava.toJava(userData)).run(DBConfig.get());
	}
}
