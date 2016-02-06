package clique.config;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import java.util.concurrent.TimeoutException;

public class DBConfig {
	private final static String hostname = "127.0.0.1";
	private final static int port = 28015;
	private final static String dbName = "test";
	
	public static final RethinkDB r = RethinkDB.r;

	private static String hostname()
	{
		return hostname;
	}

	private static int port()
	{
		return port;
	}

	private static String dbName()
	{
		return dbName;
	}
	
	public static Connection get()
	{
		try {
			System.out.println(hostname() + ":" + port() + "/" + dbName());
			return r.connection().hostname(hostname()).port(port()).db(dbName()).connect();
		} catch (TimeoutException e) {
			throw new RuntimeException();
		}
	}
}
