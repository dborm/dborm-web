/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tbc.paas.mdl.cfg.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tbc.paas.mql.metadata.domain.Column;

/**
 * 
 * @author tianzhen
 */
public class EntityMaping {
	private String tableName;
	private Column primaryColumn;
	private Class<?> entityClass;

	private Field primaryField;
	private Field dynamicField;

	private Map<String, Column> columnMap;
	private Map<String, Field> fieldMap;
	private Map<String, String> fieldNameColumnNameMap;
	private Map<String, String> columnNameFieldNameMap;
	private List<EntityRelation> entityRelations;

	private List<String> monitoredFieldList;

	private boolean shared = false;

	public EntityMaping() {
		fieldMap = new HashMap<String, Field>();
		columnMap = new HashMap<String, Column>();
		fieldNameColumnNameMap = new HashMap<String, String>();
		columnNameFieldNameMap = new HashMap<String, String>();
		monitoredFieldList = new ArrayList<String>();
		entityRelations = new ArrayList<EntityRelation>();
	}

	public Field getFieldByColumnName(String columnName) {
		String fieldName = columnNameFieldNameMap.get(columnName);
		return fieldMap.get(fieldName);
	}

	public Column getColumnByFieldName(String fieldName) {
		String columnName = fieldNameColumnNameMap.get(fieldName);
		return columnMap.get(columnName);
	}

	public String getColumnNameByFieldName(String filedName) {
		return fieldNameColumnNameMap.get(filedName);
	}

	public String getFieldNameByColumnName(String columnName) {
		return columnNameFieldNameMap.get(columnName);
	}

	/**
	 * 添加列和字段映射关系
	 * 
	 * @param column
	 *            列
	 * @param field
	 *            字段
	 */
	public void add(Column column, Field field) {
		// 列名
		String columnName = column.getColumnName();
		// 字段名
		String fieldName = field.getName();
		// 保存列名和字段名关系集合，以便后续查找
		fieldNameColumnNameMap.put(fieldName, columnName);
		columnNameFieldNameMap.put(columnName, fieldName);
		// 保存字段名和字段对象关系集合
		fieldMap.put(fieldName, field);
		// 保存列名和列对象关系集合
		columnMap.put(columnName, column);
	}

	public List<Column> getColumnList() {
		Collection<Column> columns = columnMap.values();
		List<Column> columnList = new ArrayList<Column>();
		columnList.addAll(columns);

		return columnList;
	}

	/**
	 * 获取映射对象中所有字段
	 * 
	 * @return 字段集合
	 */
	public List<Field> getFieldList() {
		Collection<Field> fields = fieldMap.values();
		List<Field> fieldList = new ArrayList<Field>();
		fieldList.addAll(fields);
		return fieldList;
	}

	/**
	 * 这个方法返回实体类的完全限定名. 比如有个实体为"com.tbc.User",<br>
	 * 那返回的名称为"com.tbc.User"
	 * 
	 * @return 类名(包括包名)
	 */
	public String getEntityClassName() {
		if (entityClass == null) {
			return null;
		}

		return entityClass.getName();
	}

	/**
	 * 这个方法返回实体类的非完全选定名. 比如有个实体为"com.tbc.User",<br>
	 * 那返回的名称为"User"
	 * 
	 * @return 类名(不包括包名)
	 */
	public String getEntitySimpleClassName() {
		if (entityClass == null) {
			return null;
		}

		return entityClass.getSimpleName();
	}

	public Field getFieldbyFieldName(String fieldName) {
		if (fieldName == null) {
			return null;
		}

		return fieldMap.get(fieldName);
	}

	public Field getDynamicField() {
		return dynamicField;
	}

	public void setDynamicField(Field dynamicField) {
		this.dynamicField = dynamicField;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public Field getPrimaryField() {
		return primaryField;
	}

	public String getPrimaryFieldName() {
		return primaryField.getName();
	}

	public boolean isPrimaryFieldName(String fieldName) {
		if (fieldName == null) {
			return false;
		}

		return primaryField.getName().equals(fieldName);
	}

	public void setPrimaryField(Field primaryField) {
		this.primaryField = primaryField;
	}

	public String getPrimaryColumnName() {
		if (primaryColumn == null) {
			return null;
		}

		return primaryColumn.getColumnName();
	}

	public void setPrimaryColumn(Column primaryColumn) {
		this.primaryColumn = primaryColumn;
	}

	public Map<String, Column> getColumnMap() {
		return columnMap;
	}

	public void setColumnMap(Map<String, Column> columnMap) {
		this.columnMap = columnMap;
	}

	public Map<String, Field> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, Field> fieldMap) {
		this.fieldMap = fieldMap;
	}

	public Map<String, String> getFieldNameColumnNameMap() {
		return fieldNameColumnNameMap;
	}

	public void setFieldNameColumnNameMap(
			Map<String, String> fieldNameColumnNameMap) {
		this.fieldNameColumnNameMap = fieldNameColumnNameMap;
	}

	public Map<String, String> getColumnNameFieldNameMap() {
		return columnNameFieldNameMap;
	}

	public void setColumnNameFieldNameMap(
			Map<String, String> columnNameFieldNameMap) {
		this.columnNameFieldNameMap = columnNameFieldNameMap;
	}

	public Column getPrimaryColumn() {
		return primaryColumn;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public List<String> getMonitoredFieldList() {
		return monitoredFieldList;
	}

	public void addMonitoredFiled(String fieldName) {
		this.monitoredFieldList.add(fieldName);
	}

	public void setMonitoredFieldList(List<String> monitoredFieldList) {
		if (monitoredFieldList == null) {
			this.monitoredFieldList.clear();
		} else {
			this.monitoredFieldList = monitoredFieldList;
		}
	}

	public boolean isFieldMonitored(String fieldName) {
		if (fieldNameColumnNameMap.containsKey(fieldName)
				&& monitoredFieldList.contains(fieldName)) {
			return true;
		}

		return false;
	}

	public boolean isColumnMonitored(String columnName) {
		String fieldName = columnNameFieldNameMap.get(columnName);
		if (fieldName == null) {
			return false;
		}

		if (monitoredFieldList.contains(fieldName)) {
			return true;
		}
		return false;
	}

	public boolean isMonitored() {
		if (monitoredFieldList == null || monitoredFieldList.isEmpty()) {
			return false;
		}

		for (String fieldName : monitoredFieldList) {
			if (fieldNameColumnNameMap.containsKey(fieldName)) {
				return true;
			}
		}

		return false;
	}

	public void addEntityRelation(EntityRelation entityRelation) {
		if (entityRelation != null && !entityRelations.contains(entityRelation)) {
			entityRelations.add(entityRelation);
		}
	}

	public List<EntityRelation> getEntityRelations() {
		return entityRelations;
	}

	public void setEntityRelations(List<EntityRelation> entityRelations) {
		this.entityRelations = entityRelations;
	}

	public EntityRelation getEntityRelation(String entityName) {
		for (EntityRelation entityRelation : entityRelations) {
			String toTableName = entityRelation.getToTableName();
			if (toTableName.equalsIgnoreCase(entityName)) {
				return entityRelation;
			}
		}

		return null;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}
}
