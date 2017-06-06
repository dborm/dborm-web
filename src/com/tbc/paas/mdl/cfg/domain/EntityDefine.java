package com.tbc.paas.mdl.cfg.domain;

import java.util.Properties;

/**
 * 这个类用于定义一个Entity问题
 * 
 * @author Ztian
 * 
 */
public class EntityDefine {

	private Boolean shared;
	private String tableName;
	private String entityClassName;
	private String pkPropery;
	private String extPropery;
	private Properties properties;
	private Properties complexProperties;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getEntityClassName() {
		return entityClassName;
	}

	public void setEntityClassName(String entityClassName) {
		this.entityClassName = entityClassName;
	}

	public String getPkPropery() {
		return pkPropery;
	}

	public void setPkPropery(String pkPropery) {
		this.pkPropery = pkPropery;
	}

	public String getExtPropery() {
		return extPropery;
	}

	public void setExtPropery(String extPropery) {
		this.extPropery = extPropery;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getComplexProperties() {
		return complexProperties;
	}

	public void setComplexProperties(Properties complexProperties) {
		this.complexProperties = complexProperties;
	}

	public Boolean getShared() {
		return shared;
	}

	public void setShared(Boolean shared) {
		this.shared = shared;
	}

}
