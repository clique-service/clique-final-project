package clique.config;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

public class FacebookConfig {
	public static String appId() {
		String appId = System.getenv("FACEBOOK_APP_ID");

		if (appId == null || appId.isEmpty()) {
			return "425399697661648";
		}
		
		return appId;
	}
	
	public static String appSecret() {
		String appSecret = System.getenv("FACEBOOK_APP_SECRET");

		if (appSecret == null || appSecret.isEmpty()) {
			return "662a60606275d946d89f675f55b5b09e";
		}
		
		return appSecret;
	}

	public static String redirectURI() {
		String redirectURI = System.getenv("FACEBOOK_REDIRECT_URI");

		if (redirectURI == null || redirectURI.isEmpty()) {
			return "http://localhost:9000/auth/facebook/callback";
		}
		
		return redirectURI;
	}

	public static String query(String query, String accessToken) {
		String sep = query.contains("?") ? "&" : "?";

		return "/v2.5/" + query + sep + "access_token=" + accessToken;
	}

	public static String scope() {
		return 
				"user_birthday," +
				"user_events," +
				"user_likes," +
				"user_location," +
				"user_status," +
				"user_tagged_places," +
				"public_profile";
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
