package clique.config;

import java.util.concurrent.TimeoutException;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

public class DBConfig {
	private final static String hostname = "192.168.99.100";
	private final static int port = 32769;
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
			return r.connection().hostname(hostname()).port(port()).db(dbName()).connect();
		} catch (TimeoutException e) {
			throw new RuntimeException();
		}
	}
}
