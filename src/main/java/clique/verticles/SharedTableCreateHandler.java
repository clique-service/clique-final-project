package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.Date;

import com.rethinkdb.gen.ast.ReqlExpr;

import clique.config.DBConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class SharedTableCreateHandler extends AbstractVerticle {
	public String getHandlerName() {
		return "sharedTableCreate";
	}

	public void start() {
		vertx.eventBus().<String> consumer(getHandlerName(), message -> {
			String userId = message.body();
			String tableName = userId + "Shared";

			if (!Boolean.valueOf(DBConfig.execute(r.tableList().contains(tableName)).toString())) {
				DBConfig.execute(r.tableCreate(tableName));

				DBConfig.execute(r.table(tableName).indexCreate("rating", user -> user.g("events").mul(5)
						.add(user.g("likes").mul(3)).add(user.g("places").mul(2)).add(user.g("categories").mul(2))));
			}

			// TODO: Start gets changes
			TopMatchesChanges topFinder = new TopMatchesChanges(userId);
			// TODO:
			//vertx.eventBus().send("", true);
			
			vertx.eventBus().send("sharedTableDataInsertion", message.body());
		});
	}
}
