package clique.helpers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by schniz on 11/02/2016.
 *
 * This class handles http throttling for our app
 * this should get us blocked by facebook after more time
 * since we batch the requests.
 */
public class HttpThrottler {
	HttpClient client;
	PublishSubject<RequestAndHandler> requestSubject;
	Observable<RequestAndHandler> requests;

	public HttpThrottler(HttpClient client, int buffer, int timeoutInMilliseconds) {
		this.client = client;
		this.requestSubject = PublishSubject.create();
		this.requests = this.requestSubject.asObservable();

		this.requests.buffer(timeoutInMilliseconds, TimeUnit.MILLISECONDS, buffer).subscribe(requestAndHandlers -> {
			if (requestAndHandlers.stream().count() < 1) {
				return;
			}

			requestAndHandlers.parallelStream().forEach(requestAndHandler -> {
				requestAndHandler.getHandler().handle(new JsonObject().put("uuid", UUID.randomUUID().toString()));
			});
		});
	}

	public void throttle(String request, Handler<JsonObject> handler){
		RequestAndHandler requestAndHandler = new RequestAndHandler().setHandler(handler).setRequest(request);
		this.requestSubject.onNext(requestAndHandler);
	}
}