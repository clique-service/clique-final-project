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

			vertx.executeBlocking(future -> {
				String tableName = userId + "Shared";
				DBConfig.execute(r.tableCreate(tableName));

				DBConfig.execute(r.table(tableName).indexCreate("rating", user -> user.g("events").mul(5)
						.add(user.g("likes").mul(3)).add(user.g("places").mul(2)).add(user.g("categories").mul(2))));

				// TODO: Start gets changes

				ReqlExpr user = r.table("Users").get(userId).coerceTo("object");

				ReqlExpr dataToShare = r.table("Users").map(otherUser -> {
					return r.hashMap().with("id", otherUser.g("id")).with("name", otherUser.g("name"))
							.with("events", getIntersection("events", user, otherUser))
							.with("likes", getIntersection("likes", user, otherUser))
							.with("places", getIntersection("places", user, otherUser))
							.with("categories", getIntersection("categories", user, otherUser));
				}).filter(otherUser -> otherUser.g("id").eq(userId).not().and(otherUser.g("events").gt(0)
						.or(otherUser.g("likes").gt(0), otherUser.g("places").gt(0), otherUser.g("categories").gt(0))));

				DBConfig.execute(r.table(tableName).insert(dataToShare));

				ReqlExpr sortedResults = r.table(tableName).orderBy().optArg("index", r.desc("rating")).limit(5)
						.coerceTo("array");
				DBConfig.execute(r.table("CliqueResults").insert(
						r.hashMap().with("userId", userId).with("date", r.now()).with("results", sortedResults)));

				DBConfig.execute(r.tableDrop(tableName));
				future.complete();
			} , res -> {
				System.out.println("Finish shared result");
			});
		});

	}

	private ReqlExpr getIntersection(String field, ReqlExpr user, ReqlExpr otherUser) {
		return otherUser.g(field).setIntersection(user.g(field)).count();
	}
}
