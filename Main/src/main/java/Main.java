import clique.verticles.EventAttendeesHandler;
import clique.verticles.EventInterestedsHandler;
import clique.verticles.EventMaybesHandler;
import clique.verticles.FacebookAuthenticate;
import clique.verticles.LikePostsHandler;
import clique.verticles.PostLikesHandler;
import clique.verticles.SharedTableDataInsertionHandler;
import clique.verticles.UserEventsHandler;
import clique.verticles.UserInitHandler;
import clique.verticles.UserLikesHandler;
import clique.verticles.UserTaggedPlacesHandler;
import clique.verticles.UserTokenHandler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

/**
 * Created by schniz and tom boldan on 05/02/2016.
 */
public class Main {
	public static void main(String[] args) throws IOException, TimeoutException {
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
					vertx.result().deployVerticle(new SharedTableDataInsertionHandler());
				});
	}
}
