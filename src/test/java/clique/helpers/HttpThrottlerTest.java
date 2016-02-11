package clique.helpers;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by schniz on 11/02/2016.
 */
public class HttpThrottlerTest {

	CountDownLatch countdown = new CountDownLatch(2);

	@Test
	public void simpleTest() throws Exception {
		HttpThrottler httpThrottler = new HttpThrottler(null, 2);
		httpThrottler.throttle("/something1", result -> {
			System.out.println(result);
			countdown.countDown();
		});

		httpThrottler.throttle("/something1", result -> {
			System.out.println(result);
			countdown.countDown();
		});

		httpThrottler.throttle("/something1", result -> {
			System.out.println(result);
			countdown.countDown();
		});

		httpThrottler.throttle("/something1", result -> {
			System.out.println(result);
			countdown.countDown();
		});

		countdown.await(2000, TimeUnit.MILLISECONDS);
	}
}