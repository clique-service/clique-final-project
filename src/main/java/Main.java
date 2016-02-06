import clique.verticles.FacebookAuthenticate;
import clique.verticles.UserInitHandler;
import clique.verticles.UserTokenHandler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

/**
 * Created by TomQueen on 05/02/2016.
 */
public class Main {
	public static void main(String[] args) {
		Vertx.clusteredVertx(new VertxOptions().setClustered(true).setBlockedThreadCheckInterval(1000*60*60), vertx -> {
			vertx.result().deployVerticle(new FacebookAuthenticate());
			vertx.result().deployVerticle(new UserTokenHandler());
			vertx.result().deployVerticle(new UserInitHandler());

			JsonObject data = new JsonObject();
			data.put("accessToken", "CAAGC5hXd3tABAP7HkI2zzT9XD2ZAf6nODx9qrZBgaw22GD8X9HyjBXapNgv1ekwgRhd9NOHk2rbQpSZCPtnrmhLdwzSc2kg0fvrFufMqeOcPbQ1zRF7MPN89m4fjMyqHwzaZCsXsF9dazQU6TOv9V0Bgxp8pZCLDQg9M9Gf0iL9dCPkWZAM0dhuf7pCXr0bx7ZCszReSGLC4ItNiR0ZBZAxzlREu1MWjVaB8ZD");
			data.put("userId", "10153853686382962");
			vertx.result().eventBus().send("userToken", data);
		});
	}
}
