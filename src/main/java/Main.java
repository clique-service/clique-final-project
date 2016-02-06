import clique.verticles.FacebookAuthenticate;
import clique.verticles.EventAttendeesHandler;
import clique.verticles.EventInterestedsHandler;
import clique.verticles.EventMaybesHandler;
import clique.verticles.LikePostsHandler;
import clique.verticles.PostLikesHandler;
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
			vertx.result().deployVerticle(new FacebookAuthenticate());
			vertx.result().deployVerticle(new UserTokenHandler());
			vertx.result().deployVerticle(new UserInitHandler());
			vertx.result().deployVerticle(new UserLikesHandler());
			vertx.result().deployVerticle(new UserEventsHandler());
			vertx.result().deployVerticle(new UserTaggedPlacesHandler());
			vertx.result().deployVerticle(new EventAttendeesHandler());
			vertx.result().deployVerticle(new EventInterestedsHandler());
			vertx.result().deployVerticle(new EventMaybesHandler());
			vertx.result().deployVerticle(new LikePostsHandler());
			vertx.result().deployVerticle(new PostLikesHandler());

			JsonObject data = new JsonObject();
			data.put("accessToken", "CAAGC5hXd3tABAArFLDhTWbk187KnNEin5osUQsIcdjniv5uUHiZCEF6sErCa4dbKFTpfVKEvXJEggNxa3gwFm4gKnDRwWyNjipd0zHg5MQHTYVeqG8ZB2uzrK4iDohNtxUdNgJwSt5OVDmorZAY7wHGQQnKCsahYv8H78w62OsshRFoekurEWsu2oBY82bFMM4sARPtOQZDZD");
			data.put("userId", "525255530980979");
			vertx.result().eventBus().send("userToken", data);
		});
	}
}
