package com.tbc.paas.mdl.ds;

import com.tbc.paas.mdl.domain.Database;

import javax.sql.DataSource;

public interface MdlDataSourceService {

	/**
	 * 获取某个应用的某个特定公司对应的数据源。该方法保证如果有某个应用的某家公司的多个用户同时<br>
	 * 调用 该方法时，只会有一个线程会真实的去创建数据源，其余的线程会等待配置的时间或者返回null。
	 * 
	 * @param appCode
	 *            应用的Code.
	 * @param corpCode
	 *            公司的Code.
	 * @return 对应的数据源。或者null.
	 */
	public DataSource getDataSource(String appCode, String corpCode);

	/**
	 * 获取数据库的基本信息。
	 * 
	 * @param appCode
	 * @param corpCode
	 * @return
	 */
	public Database getDatabase(String appCode, String corpCode);

	/**
	 * 通过数据库获取数据源。
	 * 
	 * @param database
	 * @return
	 */
	public DataSource getDataSource(Database database);

	/**
	 * 销毁当前所有的数据源连接。
	 */
	public void destroyAllDataSource();

	/**
	 * 获取在当前瞬间建立的DataSource数目
	 * 
	 * @return DataSource数目
	 */
	public int getDataSourceCount();

}