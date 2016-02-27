package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import clique.config.DBConfig;
import io.vertx.core.AbstractVerticle;

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
			vertx.eventBus().send("sharedTableDataInsertion", message.body());
		});
	}
}
