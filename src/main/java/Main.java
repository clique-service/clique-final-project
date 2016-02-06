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
			data.put("accessToken", "CAAGC5hXd3tABANip7bGH3rs33FAd7Hjbhm6t6yv1nlSEDT4YRZBX5Vq0F5uxhERbBwtxKyXqlVTkT8MdC6KSykWvPWygqbjWJybeEhz8pGQ9vVkPZCpIFwktxsMFZB0HXbl7SWEpQzLvO0VCCimYV3As6pNOoVRJs96UgmosbH68Wlbk80DYONHypwP3WbxIJPp5LU9RQZDZD");
			data.put("userId", "525255530980979");
			vertx.result().eventBus().send("userToken", data);
		});
	}
}
