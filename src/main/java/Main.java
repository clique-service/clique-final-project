import clique.verticles.UserInitHandler;
import clique.verticles.UserTokenHandler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

/**
 * Created by TomQueen on 05/02/2016.
 */
public class Main {
	public static void main(String[] args) {
		Vertx.clusteredVertx(new VertxOptions().setClustered(true).setBlockedThreadCheckInterval(1000*60*60), vertx -> {
			vertx.result().deployVerticle(new UserTokenHandler());
			vertx.result().deployVerticle(new UserInitHandler());
			
			JsonObject data = new JsonObject();
			data.put("accessToken", "CAAGC5hXd3tABADW2TTDcMSVZC8fJoyhY1uhrlaKgaYVuRZAPi5NtnAOxvskebduh2uIvXrlAS5f8zR7S28xRpnlo2iRWz0YBSdxvJNsaDMt9tQgoW5rMo1gSzOZBc0gZABjQZADxItoEIj9YhL0t5LGF4vjpopOVonDGZADphmTy9uI2uH2XPHGtTau8PtN5IcOXE3Ji4kMwZDZD");
			data.put("userId", "525255530980979");
			vertx.result().eventBus().send("userToken", data);
		});
	}
}
