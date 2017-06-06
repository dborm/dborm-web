package com.tbc.paas.mql.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tbc.paas.mql.util.MqlOperation;

public class MqlNotify {
	public static final String COMPLEX_COLUMN_UPDATE = ";~;";

	private String tableName;
	private String primaryKeyColumn;
	private MqlOperation mqlOpertation;
	private List<Object> affectedPrimaryKeyValueList;
	// 如果是插入和更新,这key为列名称.Value为新值.如果是删除操作,则为空.
	private Map<String, Object> affectedColumnMap;

	public MqlNotify() {
		super();
		affectedPrimaryKeyValueList = new ArrayList<Object>();
		affectedColumnMap = new HashMap<String, Object>();
	}

	public void putAffectedColumn(String columnName, Object value) {
		affectedColumnMap.put(columnName, value);
	}

	public void addAffectedPrimaryKey(String primaryKey) {
		affectedPrimaryKeyValueList.add(primaryKey);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public MqlOperation getMqlOpertation() {
		return mqlOpertation;
	}

	public void setMqlOpertation(MqlOperation mqlOpertation) {
		this.mqlOpertation = mqlOpertation;
	}

	public Map<String, Object> getAffectedColumnMap() {
		return affectedColumnMap;
	}

	public void setAffectedColumnMap(Map<String, Object> affectedColumnMap) {
		this.affectedColumnMap = affectedColumnMap;
	}

	public List<Object> getAffectedPrimaryKeyValueList() {
		return affectedPrimaryKeyValueList;
	}

	public void setAffectedPrimaryKeyValueList(
			List<Object> affectedPrimaryKeyValueList) {
		this.affectedPrimaryKeyValueList = affectedPrimaryKeyValueList;
	}

	public String getPrimaryKeyColumn() {
		return primaryKeyColumn;
	}

	public void setPrimaryKeyColumn(String primaryKeyColumn) {
		this.primaryKeyColumn = primaryKeyColumn;
	}

	public List<String> getComplexColumnUpdate() {

		List<String> complexColumnName = new ArrayList<String>();
		Set<Entry<String, Object>> entrySet = affectedColumnMap.entrySet();

		for (Entry<String, Object> entry : entrySet) {
			Object value = entry.getValue();
			if (COMPLEX_COLUMN_UPDATE.equals(value)) {
				String key = entry.getKey();     
				complexColumnName.add(key);
			}
		}

		return complexColumnName;
	}
}
