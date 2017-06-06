package com.tbc.paas.mdl.ds;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import redis.clients.jedis.JedisCommands;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.tbc.paas.mdl.domain.Database;
import com.tbc.paas.mdl.domain.MdlException;

/**
 * 这个类用于管理所有的数据源，它为{@link MdlDataSource}提供服务。<BR>
 * 用于运行时提供相应的应用(appCode)和公司(corpCode)对应的数据源。
 * 
 * @author Ztian
 * 
 */

public class MdlDataSourceServiceImpl implements MdlDataSourceService {

	public Logger LOG = Logger.getLogger(MdlDataSourceServiceImpl.class);
	public static final long DEF_WAIT_TIME = 1000;
	public static final String CODE_SEPARATOR = ";;";
	public static final String DEF_CORPCODE = "default";

	// private final ScheduledExecutorService dsTerminator;

	private String c0p3ConfigName;
	private JedisCommands mdmJedisCommands;

	private final ObjectMapper objectMapper;
	private final Map<String, DataSource> dataSourceMap;

	public MdlDataSourceServiceImpl() {
		super();
		objectMapper = new ObjectMapper();
		dataSourceMap = new HashMap<String, DataSource>();

		/*
		 * dsTerminator = Executors.newSingleThreadScheduledExecutor();
		 * dsTerminator.scheduleAtFixedRate(new Runnable() {
		 * 
		 * @Override public void run() { synchronized (dataSourceMap) {
		 * Collection<DataSource> dsCollection = dataSourceMap .values(); for
		 * (DataSource ds : dsCollection) {
		 * 
		 * ComboPooledDataSource cpDataSource = (ComboPooledDataSource) ds;
		 * synchronized (cpDataSource) { try { int numBusyConnections =
		 * cpDataSource .getNumIdleConnections(); int numConnections =
		 * cpDataSource .getNumConnections(); if (numBusyConnections ==
		 * numConnections) { cpDataSource.close();
		 * System.out.println("DataSource GC: " + cpDataSource.getJdbcUrl()); }
		 * 
		 * } catch (Exception e) { LOG.warn("Check busy connection failed", e);
		 * } }
		 * 
		 * System.out.println("Checked : " + cpDataSource.getJdbcUrl()); } } }
		 * }, 30, 30, TimeUnit.MILLISECONDS);
		 */
	}

	public String getC0p3ConfigName() {
		return c0p3ConfigName;
	}

	public void setC0p3ConfigName(String c0p3ConfigName) {
		this.c0p3ConfigName = c0p3ConfigName;
	}

	public JedisCommands getMdmJedisCommands() {
		return mdmJedisCommands;
	}

	public void setMdmJedisCommands(JedisCommands mdmJedisCommands) {
		this.mdmJedisCommands = mdmJedisCommands;
	}

	/**
	 * 为每个公司和应用创建一个数据源。由于一个应用的多家公司可能会在一个数据库里面，<BR>
	 * 所以必须先检查是否已经与对应的数据库建立了连接。如果已经建立，则返回已经存在的连接，<BR>
	 * 否则新建一个D数据源。
	 * 
	 * @param appCode
	 *            应用的Code.
	 * @param corpCode
	 *            公司的Code.
	 * @return 一個数据源 或者 null，如果创建数据源失败。
	 */
	private DataSource createDataSource(Database database) {

		String jdbcUrl = database.getJdbcUrl();
		String userName = database.getUserName();
		String password = database.getPassword();
		String driverClass = database.getDriverClass();

		ComboPooledDataSource dataSource = new ComboPooledDataSource(
				c0p3ConfigName);
		try {
			dataSource.setDriverClass(driverClass);
		} catch (PropertyVetoException e) {
			throw new MdlException("Load drive class [" + driverClass
					+ "] failed!", e);
		}

		dataSource.setJdbcUrl(jdbcUrl);
		dataSource.setUser(userName);
		dataSource.setPassword(password);

		return dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tbc.paas.mdl.ds.DataSourceService#getDataSource(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public DataSource getDataSource(String appCode, String corpCode) {
		Database database = getDatabase(appCode, corpCode);
		if (database == null) {
			return null;
		}

		return getDataSource(database);
	}

	@Override
	public DataSource getDataSource(Database database) {
		String key = createKeyFromDatabase(database);
		DataSource dataSource = this.dataSourceMap.get(key);
		if (dataSource != null) {
			return dataSource;
		}

		synchronized (dataSourceMap) {
			dataSource = this.dataSourceMap.get(key);
			if (dataSource != null) {
				return dataSource;
			}

			dataSource = createDataSource(database);
			dataSourceMap.put(key, dataSource);
			return dataSource;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.ds.DataSourceService#destroyAllDataSource()
	 */
	@Override
	public void destroyAllDataSource() {
		synchronized (this.dataSourceMap) {

			Collection<DataSource> dataSourceCollection = this.dataSourceMap
					.values();
			for (DataSource ds : dataSourceCollection) {
				ComboPooledDataSource pds = (ComboPooledDataSource) ds;
				try {

					DataSources.destroy(pds);
				} catch (SQLException e) {
					LOG.error("Destroy datasource [" + pds.getJdbcUrl() + "]",
							e);
				}
			}

			this.dataSourceMap.clear();
		}
	}

	/**
	 * 通过Database的一些关键属性，创建DataSource对应的Key。
	 * 
	 * @param database
	 *            数据库实体
	 * @return 数据库对应的Key
	 */
	public static String createKeyFromDatabase(Database database) {

		String jdbcUrl = database.getJdbcUrl();
		String userName = database.getUserName();
		String password = database.getPassword();
		String driverClass = database.getDriverClass();

		return driverClass + CODE_SEPARATOR + jdbcUrl + CODE_SEPARATOR
				+ userName + CODE_SEPARATOR + password;

	}

	@Override
	public Database getDatabase(String appCode, String corpCode) {
		String key = appCode + CODE_SEPARATOR + corpCode;

		try {
			String json = mdmJedisCommands.get(key);
			Database databaseCache = objectMapper.readValue(json,
					Database.class);
			return databaseCache;

		} catch (Exception e) {
			throw new MdlException("Fetch datasource info from redis "
					+ "for appCode[" + appCode + "] and corpCode[" + corpCode
					+ "] failed!", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.ds.DataSourceService#getDataSourceCount()
	 */
	@Override
	public synchronized int getDataSourceCount() {
		synchronized (this.dataSourceMap) {
			return this.dataSourceMap.size();
		}
	}

    public Map<String, DataSource> getDataSourceMap() {
        return this.dataSourceMap;
    }
}