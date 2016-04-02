package clique.helpers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by schniz and tom boldan on 11/02/2016.
 */
public class RequestAndHandler {
	String request;
	Handler<JsonObject> handler;
	Handler<JsonObject> errorHandler;

	public Handler<JsonObject> getHandlerForHttpCode(int code) {
		if (code == 200) {
			return this.getHandler();
		} else {
			return this.getErrorHandler();
		}
	}

	public String getRequest() {
		return request;
	}

	public RequestAndHandler setRequest(String request) {
		this.request = request;
		return this;
	}

	public Handler<JsonObject> getHandler() {
		return handler;
	}

	public RequestAndHandler setHandler(Handler<JsonObject> handler) {
		this.handler = handler;
		return this;
	}

	public RequestAndHandler setErrorHandler(Handler<JsonObject> errorHandler) {
		this.errorHandler = errorHandler;
		return this;
	}

	public Handler<JsonObject> getErrorHandler() {
		return errorHandler;
	}
}
