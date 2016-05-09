package clique.helpers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.Closeable;
import java.io.IOException;

public class MessageBus implements Closeable {

	private Channel channel;
	private Connection connection;

	public MessageBus() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void send(String queueName, JsonObject messageJson) {
		try {
			channel.queueDeclare(queueName, false, false, false, null);
			String message = messageJson.toString();
			channel.basicPublish("", queueName, null, message.getBytes());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void consume(String queueName, Handler<JsonObject> handler) {
		try {
			channel.queueDeclare(queueName, false, false, false, null);
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					JsonObject json = new JsonObject(message);
					new Thread() {
						@Override
						public void run() {
							try {
								handler.handle(json);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
			};
			channel.basicConsume(queueName, true, consumer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			channel.close();
			connection.close();
		} catch (Exception e) {
		}
	}
}
