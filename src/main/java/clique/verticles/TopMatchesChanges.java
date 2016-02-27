package clique.verticles;

import static com.rethinkdb.RethinkDB.r;

import com.rethinkdb.gen.ast.Limit;
import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.net.Cursor;

import clique.config.DBConfig;
import rx.Observable;
import rx.Observer;

public class TopMatchesChanges {
	private String userId;
	private Observable<Object> observable;
	private Cursor<Object> cursor;

	public TopMatchesChanges(String userId) {
		this.userId = userId;
	}

	public Observable<Object> getObservable() {
		return observable;
	}

	public void close() {
		this.cursor.close();
	}

	public void run() {
		ReqlExpr five = r.table(userId + "Shared").orderBy().optArg("index", r.desc("rating")).limit(5);

		this.cursor = DBConfig.execute(five.changes().optArg("squash", true).map(x -> {
			return five.coerceTo("array");
		}));

		this.observable = Observable.from(this.cursor);
	}
}
