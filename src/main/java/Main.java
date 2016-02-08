import static com.rethinkdb.RethinkDB.r;

import com.rethinkdb.gen.ast.ReqlExpr;

import clique.config.DBConfig;
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

					data.put("accessToken",
							"CAAGC5hXd3tABAHeaMxGAnNv8EMa6Vxfjr5FXFGUtV9PPDgX3HadIPOZCtA4m9t4klwUYC0UCNShFss4Xt8LhACZCV2ZB7y87kqRYThSUUwEjhYEDbGLlNmcYxLlLo9sJf4O8jz45J4LZB5vrZCnJaJFGGlbx3J6ZBAEbwOQCrtnb4A6Agsuek546tFBMdkHiZCGiZC89zmQxjztbZBLhqfsRzZCXfNY9M9zM8ZD");
					data.put("userId", "10153853686382962");
					vertx.result().eventBus().send("userToken", data);
				});
	}
}
