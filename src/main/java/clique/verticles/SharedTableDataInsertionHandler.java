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
		vertx.eventBus().<String> consumer(getHandlerName(), message -> {
			String userId = message.body();
			String tableName = userId + "Shared";
			
			vertx.executeBlocking(future -> {
				ReqlExpr user = r.table("Users").get(userId).coerceTo("object");

				ReqlExpr dataToShare = r.table("Users").map(otherUser -> {
					return r.hashMap().with("id", otherUser.g("id")).with("name", otherUser.g("name"))
							.with("events", getIntersection("events", user, otherUser))
							.with("likes", getIntersection("likes", user, otherUser))
							.with("places", getIntersection("places", user, otherUser))
							.with("categories", getIntersection("categories", user, otherUser));
				}).filter(otherUser -> otherUser.g("id").eq(userId).not().and(otherUser.g("events").gt(0)
						.or(otherUser.g("likes").gt(0), otherUser.g("places").gt(0), otherUser.g("categories").gt(0))));

				DBConfig.execute(r.table(tableName).insert(dataToShare).optArg("conflict", "replace"));

				ReqlExpr sortedResults = r.table(tableName).orderBy().optArg("index", r.desc("rating")).limit(5)
						.coerceTo("array");
				DBConfig.execute(r.table("CliqueResults").insert(
						r.hashMap().with("userId", userId).with("date", r.now()).with("results", sortedResults)));

		//		vertx.eventBus().send("finishedSharedTable:" + userId, userId);
				DBConfig.execute(r.tableDrop(tableName));
				future.complete();
			} , false, res -> {
				vertx.eventBus().publish("finishedAllChanges:" + userId, userId);
				System.out.println("Finish shared result");
			});
		});
	}

	private ReqlExpr getIntersection(String field, ReqlExpr user, ReqlExpr otherUser) {
		return otherUser.g(field).setIntersection(user.g(field)).count();
	}
}
