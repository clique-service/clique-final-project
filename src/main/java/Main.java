import clique.verticles.EventAttendeesHandler;
import clique.verticles.EventInterestedsHandler;
import clique.verticles.EventMaybesHandler;
import clique.verticles.FacebookAuthenticate;
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
		Vertx.clusteredVertx(new VertxOptions().setClustered(true).setBlockedThreadCheckInterval(1000 * 60 * 60),
				vertx -> {
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
					// data.put("accessToken",
					// "CAAGC5hXd3tABAHrIrG95jlrHoDC0fhxGMCNiaevKQxjJPwGJIPGJZAhINLbEtZAKdRuDY6ynRtlqyUMSIzYeeNGYdKxjDDrfbbyG71zz0pOyZBZAki9Qdeys0iCu2002cYdHZC5ZA4NiMQKUC7VksaHBwPVlGVwSdPEVh9UdQp2aMRK2CXuvyiZB4Qv0aKZBoNxUB8Sexb65UgZDZD");
					data.put("accessToken",
							"CAAGC5hXd3tABAM1hGpgPxIFP4LP9fTiJUjesAW1EdAak1ACopQsSLOYKaXIw52qOHlGnwprKgnrDDTYrILXjlKCtLzgYecT1DTrM72XJSpdLSWcFQSAkjaPsdKj6jhjukDboBSKudK9cyRMH1nx3NRD6U2TfVNziC7kTP2ZBTrMhdDcry96XmowqusSZCDTBNWZBiSIVSTZAuNUpgLy0");
					data.put("userId", "10153853686382962");
				//	data.put("userId", "525255530980979");// gal
					vertx.result().eventBus().send("userToken", data);
				});
	}
}
