package clique.helpers;

import clique.config.FacebookConfig;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by schniz on 11/02/2016.
 */
public class HttpThrottlerTest {

	int times = 3;
	int throttle = 2;
	CountDownLatch countdown = new CountDownLatch(times);

	/**
	 * Creates a list with the {item} parameter duplicated {count} times
	 * @param item to duplicate in list
	 * @param count times to duplicate
	 * @return List with [item, item, item, ...] {count} times.
	 * @example manyTimes("item", 3) => ["item", "item", "item"]
	 */
	private List<String> manyTimes(String item, int count) {
		List<String> list = new ArrayList<>();

		// Imperative :(
		for (int i = 0; i < count; i++) {
			list.add(item);
		}

		return list;
	}

	@Test
	public void checkThrottling() throws Exception {
		Vertx vertx = Vertx.vertx();
		HttpClient httpFacebookClient = FacebookConfig.getHttpFacebookClient(vertx);
		final TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		final HttpThrottler httpThrottler = new HttpThrottler(httpFacebookClient, throttle, 150);

		// Throttle for {times} times
		for (int i = 0; i < times; i++) {
			httpThrottler.throttle("me?&access_token=CAAGC5hXd3tABAFEOisHUfhlrLZCztM7S1ujwZBIa8rolVLbchq6QZA7RxFk5elW60ZBNzeMoZC9VnDERp9fhkmmvZCL5jB3i4dSYgGzwxjLU5foCefafjOw7YXCmZBivfCZC6PacEQhCNksiJ9IOEqvaE2INdgWcULpE5YZB7cmORkfBPTxwrJlizwsbd2e37Kjw5o5Di4R7BpLoSYLZCsUVviTQBPBhWZAqlYZD", json -> {
				System.out.println(json);
				testSubscriber.onNext("item");
				countdown.countDown();
			}, err -> {
				System.out.println("error!");
			});

			// Check if the throttler has published
			// only when it reaches a multiplication of {throttle}
//			int timesOfPublish = (i + 1 - ((i + 1) % throttle));
//			testSubscriber.assertReceivedOnNext(manyTimes("item", timesOfPublish));
		}

		// Wait for the rest of the calls
		countdown.await(20000, TimeUnit.MILLISECONDS);

		// Should be all of our calls
		testSubscriber.assertReceivedOnNext(manyTimes("item", times));
	}
}