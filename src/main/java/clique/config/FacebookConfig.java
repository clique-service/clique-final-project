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

	public static String scope() {
		return "user_about_me," +
				"user_actions.books," +
				"user_actions.fitness," +
				"user_actions.music," +
				"user_actions.news," +
				"user_actions.video," +
				"user_birthday," +
				"user_education_history," +
				"user_events," +
				"user_friends," +
				"user_games_activity," +
				"user_hometown," +
				"user_likes," +
				"user_location," +
				"user_managed_groups," +
				"user_photos," +
				"user_posts," +
				"user_relationship_details," +
				"user_relationships," +
				"user_religion_politics," +
				"user_status," +
				"user_tagged_places," +
				"user_videos," +
				"user_website," +
				"user_work_history," +
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
