package clique.verticles;

import clique.config.DBConfig;
import clique.config.FacebookConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

import static com.rethinkdb.RethinkDB.r;


public class UserInitHandler extends AbstractVerticle {
	public void start() {
		vertx.eventBus().<JsonObject> consumer("userInit", message -> {
		
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
		r.table("Users").insert(r.hashMap("id", userData.getString("id")).with("name", userData.getString("name"))
				.with("birthday", userData.getString("birthday")).with("location", userData.getValue("location"))
				.with("education", userData.getValue("education")).with("work", userData.getValue("work"))
				.with("languages", userData.getValue("languages"))).run(DBConfig.get());
	}
}
