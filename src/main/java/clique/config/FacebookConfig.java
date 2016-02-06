package clique.config;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

public class FacebookConfig {
	public static String appId() {
		return "425399697661648";
	}

	public static String appSecret() {
		return "662a60606275d946d89f675f55b5b09e";
	}

	public static String redirectURI() {
		return "http://localhost:9000/auth/facebook/callback";
	}

	public static String query(String query, String accessToken) {
		String sep = query.contains("?") ? "&" : "?";

		return "/v2.5/" + query + sep + "access_token=" + accessToken;
	}
	
	public static HttpClient getHttpFacebookClient(Vertx vertx)
	{
		HttpClientOptions opt = new HttpClientOptions();
		opt.setDefaultPort(443);
		opt.setSsl(true);
		opt.setDefaultHost("graph.facebook.com");
		return vertx.createHttpClient(opt);
	}
}
