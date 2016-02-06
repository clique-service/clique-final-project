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
		return "public_profile,user_friends,email,user_about_me,user_actions.books,user_actions.fitness,user_actions.music,user_actions.news,user_actions.video,user_actions:{app_namespace},user_birthday,user_education_history,user_events,user_games_activity,user_hometown,user_likes,user_location,user_managed_groups,user_photos,user_posts,user_relationships,user_relationship_details,user_religion_politics,user_tagged_places,user_videos,user_website,user_work_history,read_custom_friendlists,read_insights,read_audience_network_insights,read_page_mailboxes,manage_pages,publish_pages,publish_actions,rsvp_event,pages_show_list,pages_manage_cta,ads_read,ads_management";
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
