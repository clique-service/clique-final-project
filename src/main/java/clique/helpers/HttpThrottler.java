package clique.helpers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by schniz on 11/02/2016.
 */
public class HttpThrottler {
	HttpClient client;
	PublishSubject<RequestAndHandler> requestSubject;
	Observable<RequestAndHandler> requests;

	public HttpThrottler(HttpClient client, int buffer) {
		this.client = client;
		this.requestSubject = PublishSubject.create();
		this.requests = this.requestSubject.asObservable();

		this.requests.buffer(50, TimeUnit.MILLISECONDS, buffer).subscribe(requestAndHandlers -> {
			if (requestAndHandlers.stream().count() < 1) {
				return;
			}

			System.out.println("Calling...");
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
