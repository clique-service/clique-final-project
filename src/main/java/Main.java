import clique.verticles.UserEventsHandler;
import clique.verticles.UserInitHandler;
import clique.verticles.UserLikesHandler;
import clique.verticles.UserTaggedPlacesHandler;
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
			vertx.result().deployVerticle(new UserTokenHandler());
			vertx.result().deployVerticle(new UserInitHandler());
			vertx.result().deployVerticle(new UserLikesHandler());
			vertx.result().deployVerticle(new UserEventsHandler());
			vertx.result().deployVerticle(new UserTaggedPlacesHandler());
			
			JsonObject data = new JsonObject();
			data.put("accessToken", "CAAGC5hXd3tABAHpXH4t6RQZAOnD5lomWp1mG2FzD7s6TpYknhhVCdfZBC4bwRZBHE3UZBguPFWedn0svZAQzzOBQQbSpC6QCZA8N8SJn263FVQlLCJfdpNrpLQcVzho3ZBSsXSYWK5GZApeWzgHY5Un1I6I10ZBJnYBjsM7ZBSG48ZBgVGSLWDEHaiK2fjlmnNicfoVSpjXkjqSIwZDZD");
			data.put("userId", "525255530980979");
			vertx.result().eventBus().send("userToken", data);
		});
	}
}
