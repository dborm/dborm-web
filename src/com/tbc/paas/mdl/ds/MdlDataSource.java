package com.tbc.paas.mdl.ds;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.JedisCommands;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.domain.Database;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mdl.util.MdlConstants;

public class MdlDataSource implements DataSource {
	public Log LOG = LogFactory.getLog(MdlDataSource.class);
	private String connPoolConfigName;
	private JedisCommands mdmJedisCommands;
	private MdlDataSourceService dataSourceService;
	private MdlDataUpdateListener dataUpdateListener;

	public MdlDataSource() {
		super();
	}

	public void init() {
		MdlDataSourceServiceImpl mdlDataSourceServiceImpl = new MdlDataSourceServiceImpl();
		mdlDataSourceServiceImpl.setC0p3ConfigName(connPoolConfigName);
		mdlDataSourceServiceImpl.setMdmJedisCommands(mdmJedisCommands);
		dataSourceService = mdlDataSourceServiceImpl;
	}

	public void close() {
		if (dataSourceService != null) {
			dataSourceService.destroyAllDataSource();
		}
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		DataSource dataSource = getDataSource();
		dataSource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		DataSource dataSource = getDataSource();
		dataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.getLoginTimeout();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		DataSource dataSource = getDataSource();
		return dataSource.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		MdlConnection mdlConnection = new MdlConnection();
		DataSource dataSource = getDataSource(mdlConnection);
		try {
			Connection connection = dataSource.getConnection();
			mdlConnection.setConnection(connection);
		} catch (SQLException e) {
			logDataSourceStatus(dataSource, false);
			throw e;
		}
        if( mdlConnection.getDatabase() != null){
            // 把数据库连接放入线程变量中
            ExecutionContext.put(MdlConstants.DB_URL, mdlConnection.getDatabase().getJdbcUrl());
        }
		setSharedConnection(mdlConnection);

		mdlConnection.setDataUpdateListener(dataUpdateListener);

		mdlConnection.openLog();

		return mdlConnection;
	}

	private void logDataSourceStatus(DataSource dataSource, boolean shared) {
		if (!(dataSource instanceof ComboPooledDataSource)) {
			return;
		}

		ComboPooledDataSource ds = (ComboPooledDataSource) dataSource;
		int totalConnCount;
		try {
			String jdbcUrl = ds.getJdbcUrl();
			totalConnCount = ds.getNumConnectionsAllUsers();
			int busyConnCount = ds.getNumBusyConnectionsAllUsers();
			int idleConnCount = ds.getNumIdleConnectionsAllUsers();
			int unclosedConnCount = ds
					.getNumUnclosedOrphanedConnectionsAllUsers();

			String message = null;
			if (shared) {
				message = "Share Center DataSource Status Info:";
			} else {
				message = "Normal DataSource Status Info:";
			}

			message += "\n" + jdbcUrl;

			message += "\nTotal Connections Count:" + totalConnCount
					+ ";\nBusy Connections Count:" + busyConnCount
					+ ";\nIdel Connections Count:" + idleConnCount
					+ ";\nUnclosed Or Phaned Connections:" + unclosedConnCount;
			LOG.warn(message);
		} catch (SQLException e) {
			LOG.warn("Record DataSource status failed", e);
		}
	}

	public void openSharedConnection() {
		String corpCode = ExecutionContext.get(ExecutionContext.CORP_CODE);
		if (corpCode == null || corpCode.isEmpty()) {
			throw new MdlException("Corp code is empty. can't find data source");
		}

		Database sharedDatabase = dataSourceService.getDatabase(
				MdlConstants.SHARE_CENTER_APP_CODE, corpCode);
		DataSource sharedDataSource = dataSourceService
				.getDataSource(sharedDatabase);

		try {
			Connection sharedConn = sharedDataSource.getConnection();
			MdlConnection.setShareDatabase(sharedDatabase);
			MdlConnection.setSharedConnection(sharedConn);
		} catch (SQLException e) {
			logDataSourceStatus(sharedDataSource, true);
			throw new MdlException("Open share center connection failed!", e);
		}
	}

	public void setSharedConnection(MdlConnection mdlConnection)
			throws SQLException {
		String corpCode = ExecutionContext.get(ExecutionContext.CORP_CODE);
		if (corpCode == null || corpCode.isEmpty()) {
			throw new SQLException("Corp code is empty. can't find data source");
		}

		Database sharedDatabase = null;
		try {
			sharedDatabase = dataSourceService.getDatabase(
					MdlConstants.SHARE_CENTER_APP_CODE, corpCode);
		} catch (Exception ex) {
			return;
		}

		Database database = mdlConnection.getDatabase();
		if (database.equals(sharedDatabase)) {
			MdlConnection.closeSharedConnection();
			MdlConnection.setNeedSharedCenter(false);
			return;
		}

		boolean hasSharedConnection = MdlConnection.hasSharedConnection();
		if (hasSharedConnection) {
			Database oldShareDatabase = MdlConnection.getShareDatabase();
			if (sharedDatabase.equals(oldShareDatabase)) {
				return;
			} else {
				throw new SQLException(
						"Hasn't close old share center before close the old one!");
			}
		}

		DataSource dataSource = this.dataSourceService
				.getDataSource(sharedDatabase);

		Connection sharedConn = dataSource.getConnection();
		MdlConnection.setShareDatabase(sharedDatabase);
		MdlConnection.setSharedConnection(sharedConn);
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		MdlConnection mdlConnection = new MdlConnection();
		DataSource dataSource = getDataSource(mdlConnection);
		try {
			Connection connection = dataSource
					.getConnection(username, password);
			mdlConnection.setConnection(connection);
		} catch (SQLException e) {
			logDataSourceStatus(dataSource, false);
			throw e;
		}
        if( mdlConnection.getDatabase() != null){
            // 把数据库连接放入线程变量中
            ExecutionContext.put(MdlConstants.DB_URL, mdlConnection.getDatabase().getJdbcUrl());
        }

		setSharedConnection(mdlConnection);

		mdlConnection.setDataUpdateListener(dataUpdateListener);

		mdlConnection.openLog();

		return mdlConnection;
	}

	private DataSource getDataSource() throws SQLException {
		String appCode = ExecutionContext.getAppCode();
		String corpCode = ExecutionContext.get(ExecutionContext.CORP_CODE);

		if (appCode == null || appCode.isEmpty()) {
			throw new SQLException("App code is empty. can't find data source");
		}

		if (corpCode == null || corpCode.isEmpty()) {
			throw new SQLException("Corp code is empty. can't find data source");
		}

		DataSource dataSource = this.dataSourceService.getDataSource(appCode,
				corpCode);

		if (dataSource == null) {
			throw new SQLException("Can't find data source for ( appCode:"
					+ appCode + ",corpCode:" + corpCode + ")");
		}

		return dataSource;
	}

	private DataSource getDataSource(MdlConnection mdlConnection)
			throws SQLException {
		String appCode = ExecutionContext.getAppCode();
		String corpCode = ExecutionContext.get(ExecutionContext.CORP_CODE);

		if (appCode == null || appCode.isEmpty()) {
			throw new SQLException("App code is empty. can't find data source");
		}

		if (corpCode == null || corpCode.isEmpty()) {
			throw new SQLException("Corp code is empty. can't find data source");
		}

		Database database = dataSourceService.getDatabase(appCode, corpCode);
		mdlConnection.setDatabase(database);
		DataSource dataSource = dataSourceService.getDataSource(database);

		if (dataSource == null) {
			throw new SQLException("Can't find data source for ( appCode:"
					+ appCode + ",corpCode:" + corpCode + ")");
		}

		return dataSource;
	}

	public boolean needSharedCenter() {
		String appCode = ExecutionContext.getAppCode();
		String corpCode = ExecutionContext.get(ExecutionContext.CORP_CODE);

		if (appCode == null || appCode.isEmpty()) {
			throw new MdlException("App code is empty. can't find data source");
		}

		if (corpCode == null || corpCode.isEmpty()) {
			throw new MdlException("Corp code is empty. can't find data source");
		}
		Database database = dataSourceService.getDatabase(appCode, corpCode);

		Database sharedDatabase = dataSourceService.getDatabase(
				MdlConstants.SHARE_CENTER_APP_CODE, corpCode);

		return !database.equals(sharedDatabase);
	}

	public String getConnPoolConfigName() {
		return connPoolConfigName;
	}

	public void setConnPoolConfigName(String connPoolConfigName) {
		this.connPoolConfigName = connPoolConfigName;
	}

	public JedisCommands getMdmJedisCommands() {
		return mdmJedisCommands;
	}

	public void setMdmJedisCommands(JedisCommands mdmJedisCommands) {
		this.mdmJedisCommands = mdmJedisCommands;
	}

	public MdlDataUpdateListener getDataUpdateListener() {
		return dataUpdateListener;
	}

	public void setDataUpdateListener(MdlDataUpdateListener dataUpdateListener) {
		this.dataUpdateListener = dataUpdateListener;
	}

	public MdlDataSourceService getDataSourceService() {
		return dataSourceService;
	}

	public void setDataSourceService(MdlDataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

}
