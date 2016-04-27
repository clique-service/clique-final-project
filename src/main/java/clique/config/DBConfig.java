package clique.config;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.net.Connection;

import java.util.logging.Logger;

public class DBConfig {
	private final static Logger logger = Logger.getLogger(DBConfig.class.getName());
	private final static String hostname = "127.0.0.1";
	private final static int port = 28015;
	private final static String dbName = "test";

	public static final RethinkDB r = RethinkDB.r;

	private static String hostname() {
		String hostNameEn = System.getenv("RETHINKDB_HOSTNAME");

		if (hostNameEn == null || hostNameEn.isEmpty()) {
			return hostname;
		}
		
		return hostNameEn;
	}

	private static int port() {
		String portEn = System.getenv("RETHINKDB_PORT");

		if (portEn == null || portEn.isEmpty()) {
			return port;
		}
		
		return Integer.parseInt(portEn);
	}

	private static String dbName() {
		String dbNameEn = System.getenv("RETHINKDB_DBNAME");

		if (dbNameEn == null || dbNameEn.isEmpty()) {
			return dbName;
		}
		
		return dbNameEn;
	}

	public static Connection get() {
		String url = hostname() + ":" + port() + "/" + dbName();
		try {
			logger.info("Creating a connection to " + url);
			return r.connection().hostname(hostname()).port(port()).db(dbName()).connect();
		} catch (Exception e) {
			logger.severe("Could not initiate connection to " + url + "! is the db on??");
			throw new RuntimeException(e);
		}
	}

	public static <T> T execute(ReqlExpr expr) {
		Connection connection = get();
		Object x = expr.run(connection);
		connection.close();
		return (T)x;
	}
}
