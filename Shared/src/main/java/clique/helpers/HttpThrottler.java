package clique.helpers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by schniz and tom boldan on 11/02/2016.
 * <p>
 * This class handles http throttling for our app
 * this should get us blocked by facebook after more time
 * since we batch the requests.
 */
public class HttpThrottler {
	HttpClient client;
	PublishSubject<RequestAndHandler> requestSubject;
	Observable<RequestAndHandler> requests;

	private String getTokenFromRequest(String request) {
		return Arrays.asList(request.split("&")).stream().filter(query -> query.startsWith("access_token=")).findFirst().orElse("access_token=").substring("access_token".length() + 1);
	}

	public HttpThrottler(HttpClient client, int buffer, int timeoutInMilliseconds) {
		this.client = client;
		this.requestSubject = PublishSubject.create();
		this.requests = this.requestSubject.asObservable();

		Observable<List<RequestAndHandler>> bufferedRequests = this.requests.buffer(timeoutInMilliseconds, TimeUnit.MILLISECONDS, buffer);
		bufferedRequests.filter(requestAndHandlers -> requestAndHandlers.size() >= 1).subscribe(this::createRequests);
	}

	public void createRequests(List<RequestAndHandler> requestAndHandlers) {
		System.out.println("creating requests");
		String token = getTokenFromRequest(requestAndHandlers.get(0).getRequest());
		System.out.println("token = " + token);
		JsonObject requestData = new JsonObject().put("batch", buildRequestObject(requestAndHandlers));
		client.post(443, "graph.facebook.com", "/v2.5/?include_headers=false&access_token=" + token).handler(response -> {
			System.out.println("got response");
			response.bodyHandler(body -> {
				System.out.println("got body");
				JsonArray list = body.toJsonArray();

				for (int i = 0; i < list.size(); i++) {
					JsonObject result = list.getJsonObject(i);
					int code = result == null ? 503 : result.getInteger("code");
					System.out.println("with code " + code);
					RequestAndHandler requestAndHandler = requestAndHandlers.get(i);
					requestAndHandler.getHandlerForHttpCode(code).handle(new JsonObject(result.getString("body")));
				}
			});
		}).endHandler(end -> {
			System.out.println("end");
		}).putHeader("Content-Type", "application/json").putHeader("Content-Length", String.valueOf(requestData.toString().length())).end(requestData.toString());
		System.out.println(requestData);
	}

	public JsonArray buildRequestObject(List<RequestAndHandler> requests) {
		return new JsonArray(requests.stream().map(requestAndHandler -> {
			return new JsonObject().put("method", "GET").put("relative_url", requestAndHandler.getRequest());
		}).collect(Collectors.toList()));
	}

	public void throttle(String request, Handler<JsonObject> handler, Handler<JsonObject> errorHandler) {
		RequestAndHandler requestAndHandler = new RequestAndHandler().setHandler(handler).setErrorHandler(errorHandler).setRequest(request);
		this.requestSubject.onNext(requestAndHandler);
	}
}
