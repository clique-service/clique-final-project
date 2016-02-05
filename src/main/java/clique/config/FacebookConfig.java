package clique.config;

import java.util.function.Function;

import io.vertx.core.http.HttpClient;

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
}
