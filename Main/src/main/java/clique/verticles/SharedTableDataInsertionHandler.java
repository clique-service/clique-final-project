package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import com.rethinkdb.gen.ast.ReqlExpr;

import clique.config.DBConfig;
import io.vertx.core.AbstractVerticle;

public class SharedTableDataInsertionHandler extends AbstractVerticle {
	public String getHandlerName() {
		return "sharedTableDataInsertion";
	}

	public void start() {
		vertx.eventBus().<String>consumer(getHandlerName(), message -> {
			String userId = message.body();
			String tableName = userId + "Shared";

			vertx.executeBlocking(future -> {
				ReqlExpr dataToShare = r.table("Users").get(userId).do_(user -> {
					return r.table("Users").getAll(r.args(user.g("events"))).optArg("index", "events").union(
							r.table("Users").getAll(r.args(user.g("likes"))).optArg("index", "likes"),
							r.table("Users").getAll(r.args(user.g("places"))).optArg("index", "places"),
							r.table("Users").getAll(r.args(user.g("categories"))).optArg("index", "categories")
					).filter(otherUser -> {
						return otherUser.g("id").eq(user.g("id")).not().and(r.table(tableName).get(otherUser.g("id")).default_(false).not());
					}).map(otherUser -> {
						return r.hashMap().with("id", otherUser.g("id")).with("name", otherUser.g("name"))
								.with("events", getIntersection("events", user, otherUser))
								.with("likes", getIntersection("likes", user, otherUser))
								.with("places", getIntersection("places", user, otherUser))
								.with("categories", getIntersection("categories", user, otherUser));
					});
				});

				DBConfig.execute(r.table(tableName).insert(dataToShare).optArg("conflict", "replace"));

				ReqlExpr sortedResults = r.table(tableName).orderBy().optArg("index", r.desc("rating")).limit(5)
						.coerceTo("array");
				DBConfig.execute(r.table("CliqueResults").insert(
						r.hashMap().with("userId", userId).with("date", r.now()).with("results", sortedResults)));

				DBConfig.execute(r.tableDrop(tableName));
				future.complete();
			}, false, res -> {
				System.out.println("Finish shared result");
			});
		});
	}

	private ReqlExpr getIntersection(String field, ReqlExpr user, ReqlExpr otherUser) {
		return otherUser.g(field).setIntersection(user.g(field)).count();
	}
}
