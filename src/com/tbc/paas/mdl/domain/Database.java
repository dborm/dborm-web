package com.tbc.paas.mdl.domain;

/**
 * 数据库连接对象
 * 
 * @author ZHANG Nan
 */
public class Database {
	/**
	 * 数据库驱动类
	 */
	private String driverClass;
	/**
	 * 驱动地址
	 */
	private String jdbcUrl;
	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 密码
	 */
	private String password;

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		if (jdbcUrl == null) {
			return 7;
		}

		return jdbcUrl.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Database)) {
			return false;
		}

		Database db = (Database) obj;

		return this.jdbcUrl.equalsIgnoreCase(db.getJdbcUrl());
	}

	@Override
	public String toString() {
		return "MdlDatabase [driverClass=" + driverClass + ", jdbcUrl="
				+ jdbcUrl + ", userName=" + userName + ", password=" + password
				+ "]";
	}
}
