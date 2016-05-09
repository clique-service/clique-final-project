package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.rethinkdb.gen.ast.Insert;
import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import clique.config.DBConfig;
import clique.helpers.MessageBus;
import clique.helpers.UserResult;
import io.vertx.core.AbstractVerticle;
import rx.Observable;
import rx.subjects.PublishSubject;

public class SharedTableDataInsertionHandler extends AbstractVerticle {
	private MessageBus bus;

	public String getHandlerName() {
		return "sharedTableDataInsertion";
	}

	public void start() {
		bus = new MessageBus();

		bus.consume(getHandlerName(), message -> {
			TreeSet<UserResult> result = new TreeSet<>();

			String userId = message.getString("userId");
			String tableName = userId + "Shared";

			Boolean isCurrentlyRunning = DBConfig.execute(r.table(tableName).get("isRunning").g("isRunning").default_(false));

			if (isCurrentlyRunning) {
				return;
			}

			DBConfig.execute(r.table(tableName).insert(r.hashMap("id", "isRunning").with("isRunning", true)));

			PublishSubject<List<UserResult>> saver = PublishSubject.create();
			Observable<List<UserResult>> toSave$ = saver.asObservable();

			toSave$.sample(5000, TimeUnit.MILLISECONDS).subscribe(lastResult -> {
				if (lastResult == null) return;
				ReqlExpr insert = r.table(tableName).insert(r.hashMap().with("id", userId).with("results", lastResult.toArray())).optArg("conflict", "replace");
				DBConfig.execute(insert);
				System.out.println("saved");
			});

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
					UserResult parsedResult = UserResult.parse(x);
					Optional<UserResult> sameResult = result.stream().filter(e -> e.getId().equals(parsedResult.getId())).findFirst();

					if (sameResult.isPresent() && sameResult.get().getRate() < parsedResult.getRate()) {
						result.remove(sameResult.get());
						result.add(parsedResult);
					}
					else if (!sameResult.isPresent()) {
						result.add(parsedResult);
					}

					if (result.size() > 5) {
						result.pollFirst();
					}

					saver.onNext(result.stream().collect(Collectors.toList()));
				});

				connection.close();

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
