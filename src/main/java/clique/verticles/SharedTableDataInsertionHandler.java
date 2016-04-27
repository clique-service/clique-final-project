package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.Map;
import java.util.TreeSet;

import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import clique.config.DBConfig;
import clique.helpers.MessageBus;
import clique.helpers.UserResult;
import io.vertx.core.AbstractVerticle;

public class SharedTableDataInsertionHandler extends AbstractVerticle {
	private MessageBus bus;

	public static TreeSet<UserResult> result = new TreeSet<>();

	public String getHandlerName() {
		return "sharedTableDataInsertion";
	}

	public void start() {
		bus = new MessageBus();

		bus.consume(getHandlerName(), message -> {
			String userId = message.getString("userId");
			String tableName = userId + "Shared";

			vertx.executeBlocking(future -> {
				ReqlExpr dataToShare = r.table("Users").get(userId).do_(user -> {
					return r.table("Users").getAll(r.args(user.g("events"))).optArg("index", "events")
							.union(r.table("Users").getAll(r.args(user.g("likes"))).optArg("index", "likes"),
									r.table("Users").getAll(r.args(user.g("places"))).optArg("index", "places"),
									r.table("Users").getAll(r.args(user.g("categories"))).optArg("index", "categories"))
							.filter(otherUser -> {
						return otherUser.g("id").eq(user.g("id")).not()
								.and(r.table(tableName).get(otherUser.g("id")).default_(false).not());
					}).map(otherUser -> {
						return r.hashMap().with("id", otherUser.g("id")).with("name", otherUser.g("name"))
								.with("events", getIntersection("events", user, otherUser))
								.with("likes", getIntersection("likes", user, otherUser))
								.with("places", getIntersection("places", user, otherUser))
								.with("categories", getIntersection("categories", user, otherUser));
					}).map(resultUser -> resultUser
							.merge(r.hashMap("rating", resultUser.g("events").mul(5).add(resultUser.g("likes").mul(3))
									.add(resultUser.g("places").mul(2)).add(resultUser.g("categories").mul(2)))));
				});

				Connection connection = DBConfig.get();
				Cursor<Map<String, Object>> cursor = dataToShare.run(connection);

				cursor.forEach(x -> {
					result.add(UserResult.parse(x));
				//	DBConfig.execute(r.table(tableName).insert(x).optArg("conflict", "replace"));
					
					if (x.get("name").equals("gal schlezinger"))
					{
						System.out.println(x.get("rating"));
					}
					
					if (result.size() > 5) {
						result.pollFirst();
					}
				});
				
				connection.close();

				// DBConfig.execute(r.table(tableName).insert(dataToShare).optArg("conflict",
				// "replace"));

				// ReqlExpr sortedResults =
				// r.table(tableName).orderBy().optArg("index",
				// r.desc("rating")).limit(5)
				// .coerceTo("array");
				DBConfig.execute(r.table("CliqueResults")
						.insert(r.hashMap().with("userId", userId).with("date", r.now()).with("results", result.toArray())));

				DBConfig.execute(r.tableDrop(tableName));
				future.complete();
			} , false, res -> {
				System.out.println("Finish shared result");
			});
		});
	}

	private ReqlExpr getIntersection(String field, ReqlExpr user, ReqlExpr otherUser) {
		return otherUser.g(field).setIntersection(user.g(field)).count();
	}
}
