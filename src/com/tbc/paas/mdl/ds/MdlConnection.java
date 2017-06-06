package com.tbc.paas.mdl.ds;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.domain.Database;
import com.tbc.paas.mdl.log.MdlLogInfo;
import com.tbc.paas.mdl.log.MdlLogger;
import com.tbc.paas.mdl.util.MdlConstants;
import com.tbc.paas.mdl.util.MdlContext;
import com.tbc.paas.mql.notify.MqlNotify;

public class MdlConnection implements Connection {

	private Connection conn;
	private Database database;

	private static ThreadLocal<Boolean> needSharedCenter = new ThreadLocal<Boolean>();
	private static ThreadLocal<Database> sharedDatabase = new ThreadLocal<Database>();
	private static ThreadLocal<Connection> sharedConn = new ThreadLocal<Connection>();

	// private final String key = null;
	// private static ConcurrentHashMap<Database, List<String>> logMap = new
	// ConcurrentHashMap<Database, List<String>>(
	// 100);

	private MdlDataUpdateListener dataUpdateListener;

	public MdlConnection() {
		this(null);
	}

	public MdlConnection(Connection connection) {
		super();
		this.conn = connection;
		MdlLogger.beginTranscation();
	}

	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection sqlConnection) {
		this.conn = sqlConnection;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return conn.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.conn.isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		return this.conn.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return this.conn.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return this.conn.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return this.conn.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		conn.setAutoCommit(autoCommit);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.setAutoCommit(autoCommit);
		}
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return conn.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		MdlLogInfo log = new MdlLogInfo();
		long startTime = System.currentTimeMillis();
		log.setStartTime(startTime);
		conn.commit();
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.commit();
		}

		log.setCostTime(System.currentTimeMillis() - startTime);
		MdlLogger.logCommit(log);

		List<MqlNotify> mqlNotifyList = MdlContext.getMqlNotifyList();
		if (mqlNotifyList == null) {
			return;
		}

		if (dataUpdateListener != null) {
			dataUpdateListener.updateNotify(mqlNotifyList);
		}

		MdlContext.clearNotifyList();
	}

	@Override
	public void rollback() throws SQLException {
		MdlLogInfo log = new MdlLogInfo();
		long startTime = System.currentTimeMillis();
		log.setStartTime(startTime);
		conn.rollback();
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.rollback();
		}
		MdlContext.clearNotifyList();
		log.setCostTime(System.currentTimeMillis() - startTime);
		MdlLogger.logRollback(log);
	}

	@Override
	public void close() throws SQLException {
		conn.close();
		closeSharedConnection();
		closeLog();
        ExecutionContext.put(MdlConstants.DB_URL, null);
    }

	@Override
	public boolean isClosed() throws SQLException {
		return conn.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return conn.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		conn.setReadOnly(readOnly);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.setReadOnly(readOnly);
		}
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return conn.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		conn.setCatalog(catalog);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.setCatalog(catalog);
		}
	}

	@Override
	public String getCatalog() throws SQLException {
		return conn.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		conn.setTransactionIsolation(level);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.setTransactionIsolation(level);
		}
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return conn.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return conn.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		conn.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return conn.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {

		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {

		return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return conn.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		conn.setTypeMap(map);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.setTypeMap(map);
		}
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		conn.setHoldability(holdability);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.setHoldability(holdability);
		}
	}

	@Override
	public int getHoldability() throws SQLException {
		return conn.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return this.conn.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return conn.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		conn.rollback(savepoint);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.rollback(savepoint);
		}
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		conn.releaseSavepoint(savepoint);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.releaseSavepoint(savepoint);
		}
	}

	@Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {

		return conn.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {

		return conn.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return conn.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return conn.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return conn.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		return conn.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return conn.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return conn.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return conn.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return conn.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		conn.setClientInfo(name, value);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.setClientInfo(name, value);
		}
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		conn.setClientInfo(properties);
		Connection sConn = sharedConn.get();
		if (sConn != null) {
			sConn.setClientInfo(properties);
		}
	}

	@Override
	public String getClientInfo(String name) throws SQLException {

		return conn.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return conn.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return conn.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return conn.createStruct(typeName, attributes);
	}

	public MdlDataUpdateListener getDataUpdateListener() {
		return dataUpdateListener;
	}

	public void setDataUpdateListener(MdlDataUpdateListener dataUpdateListener) {
		this.dataUpdateListener = dataUpdateListener;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public static Database getShareDatabase() {
		return sharedDatabase.get();
	}

	public static void setShareDatabase(Database database) {
		sharedDatabase.set(database);
	}

	public static void setSharedConnection(Connection conn) {
		sharedConn.set(conn);
	}

	public static Connection getSharedConnection() {
		return sharedConn.get();
	}

	public static boolean hasSharedConnection() {
		Connection connection = sharedConn.get();
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			// NP 任何异常发生，都视为连接不存在。
		}

		return false;
	}

	public static void closeSharedConnection() {
		Connection connection = sharedConn.get();
		if (connection == null) {
			return;
		}

		try {
			if (!connection.isClosed()) {
				connection.close();
			}

			sharedConn.set(null);
			sharedDatabase.set(null);
			needSharedCenter.set(null);
		} catch (Exception ex) {
			// NP
		}
	}

	/**
	 * 只有明确设定不需要共享中心的才会返回不需要。
	 * 
	 * @return true 需要，false 不需要。
	 */
	public static boolean getNeedSharedCenter() {
		Boolean need = needSharedCenter.get();
		if (need == null) {
			return true;
		}

		return need;
	}

	public static void setNeedSharedCenter(Boolean need) {
		needSharedCenter.set(need);
	}

	public void openLog() {
		/*
		 * key = UUID.randomUUID().toString().replace("-", ""); List<String>
		 * list = logMap.get(database); if (list == null) { list = new
		 * ArrayList<String>(10); logMap.put(database, list); } list.add(key);
		 * 
		 * Set<Entry<Database, List<String>>> entrySet = logMap.entrySet();
		 * System.out.println(); System.out .println(
		 * "＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝Connection Log Start＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
		 * 
		 * for (Entry<Database, List<String>> entry : entrySet) { Database db =
		 * entry.getKey(); List<String> conns = entry.getValue();
		 * System.out.println(db.getJdbcUrl() + " : " + conns.size());
		 * 
		 * for (int i = 0; i < conns.size(); i++) {
		 * System.out.print(conns.get(i) + " "); if (i % 5 == 0) {
		 * System.out.println(); } } }
		 * 
		 * System.out.println("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝"
		 * ); System.out.println();
		 */
	}

	public void closeLog() {

		/*
		 * List<String> list = logMap.get(database); if (list == null) { list =
		 * new ArrayList<String>(10); logMap.put(database, list); }
		 * list.remove(key);
		 * 
		 * Set<Entry<Database, List<String>>> entrySet = logMap.entrySet();
		 * System.out.println(); System.out
		 * .println("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝Connection Log Start＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝"
		 * );
		 * 
		 * for (Entry<Database, List<String>> entry : entrySet) { Database db =
		 * entry.getKey(); List<String> conns = entry.getValue();
		 * System.out.println("" + db.getJdbcUrl() + " : " + conns.size());
		 * 
		 * for (int i = 0; i < conns.size(); i++) {
		 * System.out.print(conns.get(i) + " "); if (i % 5 == 0) {
		 * System.out.println(); } } }
		 * 
		 * System.out.println("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝"
		 * ); System.out.println();
		 */
	}
}
