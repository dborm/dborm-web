package com.tbc.paas.mdl.impl.rowmapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.cfg.domain.EntityRelation;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mdl.util.MdlUtil;
import com.tbc.paas.mdl.util.ReflectUtil;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.metadata.domain.TableRelation;
import com.tbc.paas.mql.parser.dialect.OracleDialect;
import com.tbc.paas.mql.util.SqlAliasGenerator;

public class MdlEntityRelRowMapper<T> implements RowMapper<Map<String, Object>> {

	private String mainKey;
	private Configure configure;
	private List<T> entityResults;
	private List<String> entityKeyList;
	private List<Object> entityValueList;
	private List<SqlResultColumn> resultColumns;

	public MdlEntityRelRowMapper(String mainKey, Configure configure,
			List<SqlResultColumn> resultColumns) {
		super();
		this.mainKey = mainKey;
		this.configure = configure;
		this.resultColumns = resultColumns;
		this.entityResults = new ArrayList<T>();
		this.entityKeyList = new ArrayList<String>();
		this.entityValueList = new ArrayList<Object>();
	}

	@Override
	public Map<String, Object> mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		Map<String, Object> rowRustltMap = new HashMap<String, Object>();
		for (int index = 1; index <= resultColumns.size(); index++) {
			SqlResultColumn sqlResultColumn = resultColumns.get(index - 1);
			String entityName = sqlResultColumn.getTableName();
			EntityMaping entityMapping = configure
					.getEntityMappingByClassName(entityName);
			if (entityMapping == null) {
				throw new MdlException("Doesn't configure for entity "
						+ entityName + "!");
			}

			Object entity = getEntityInstance(sqlResultColumn, entityMapping,
					rowRustltMap);
			String propertyName = sqlResultColumn.getColumnName();
			if (sqlResultColumn.isExtended()) {
				Field dynamicField = entityMapping.getDynamicField();
				Map<String, Object> dynamicColumns = MdlUtil.getExtMap(entity,
						dynamicField);
				Object data = rs.getObject(index);
				if (data == null) {
					continue;
				}
				dynamicColumns.put(propertyName, data);
			} else {
				Field columnField = entityMapping.getFieldbyFieldName(propertyName);
				Object data = MdlUtil.fetchColumnData(rs, index, columnField);
				if (data == null) {
					continue;
				}
				data=OracleDialect.dataTypeDialectConvert(columnField, data);
				ReflectUtil.setFieldValue(columnField, entity, data);
			}
		}

		clearDuplicateEntity(rowRustltMap);

		return null;
	}

	@SuppressWarnings({ "unchecked" })
	private void processEntityRelation(Object fromEntity,
			EntityMaping fromEntityMapping, Object toEntity,
			EntityMaping toEntityMapping, EntityRelation entityRelation) {
		String fromColumnName = entityRelation.getFromColumnName();
		Field fromField = fromEntityMapping.getFieldbyFieldName(fromColumnName);
		Object fromFieldValue = ReflectUtil
				.getFieldValue(fromField, fromEntity);
		String toColumnName = entityRelation.getToColumnName();
		Field toField = toEntityMapping.getFieldbyFieldName(toColumnName);
		Object toFieldValue = ReflectUtil.getFieldValue(toField, toEntity);
		if (!fromFieldValue.equals(toFieldValue)) {
			return;
		}

		String relationship = entityRelation.getRelationship();

		if (relationship.equals(TableRelation.ONE_TO_ONE)) {
			Field entityField = entityRelation.getEntityField();
			ReflectUtil.setFieldValue(entityField, fromEntity, toEntity);
		} else if (relationship.equals(TableRelation.ONE_TO_MANY)) {
			Field entityField = entityRelation.getEntityField();
			Object fieldValue = ReflectUtil.getFieldValue(entityField,
					fromEntity);

			List<Object> oneToManyList = null;
			if (fieldValue == null) {
				oneToManyList = new ArrayList<Object>();
				ReflectUtil.setFieldValue(entityField, fromEntity,
						oneToManyList);
			} else {
				oneToManyList = (List<Object>) fieldValue;
			}
			oneToManyList.add(toEntity);
		} else if (relationship.equals(TableRelation.MANY_TO_ONE)) {
			Field entityField = entityRelation.getEntityField();
			ReflectUtil.setFieldValue(entityField, fromEntity, toEntity);
		} else {
			throw new MdlException("The entity relationship[" + relationship
					+ "] is unsupported");
		}
	}

	@SuppressWarnings("unchecked")
	private void clearDuplicateEntity(Map<String, Object> rowRustltMap) {
		Set<Entry<String, Object>> entrySet = rowRustltMap.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			String key = entry.getKey();
			Object entity = entry.getValue();

			Class<?> entityClass = entity.getClass();
			String entityClassName = entityClass.getName();
			EntityMaping entityMapping = configure
					.getEntityMappingByClassName(entityClassName);
			Field primaryField = entityMapping.getPrimaryField();
			Object fieldValue = ReflectUtil.getFieldValue(primaryField, entity);
            if (fieldValue == null) {
                continue;
            }
			String mainPk = fieldValue.toString();
			String enntityTypeId = key + mainPk;

			if (!entityKeyList.contains(enntityTypeId)) {
				entityKeyList.add(enntityTypeId);
				entityValueList.add(entity);
				if (key.equals(mainKey)) {
					entityResults.add((T) entity);
				}
			}
		}
	}

	private Object getEntityInstance(SqlResultColumn sqlResultColumn,
			EntityMaping entityMapping, Map<String, Object> rowRustltMap) {
		String key = sqlResultColumn.getTableAlias();
		boolean autoGen = SqlAliasGenerator.isAutoGen(key);
		Class<?> entityClass = entityMapping.getEntityClass();
		if (autoGen) {
			key = entityClass.getName();
		}

		Object entity = rowRustltMap.get(key);
		if (entity == null) {
			entity = ReflectUtil.createInstance(entityClass);
			rowRustltMap.put(key, entity);
		}

		return entity;
	}

	public String getMainKey() {
		return mainKey;
	}

	public void setMainKey(String mainKey) {
		this.mainKey = mainKey;
	}

	public List<T> getEntityResults() {

		for (Object fromEntity : entityValueList) {
			Class<?> rowEntityClass = fromEntity.getClass();
			String rowEntityClassName = rowEntityClass.getName();
			EntityMaping rowEntityMapping = configure
					.getEntityMappingByClassName(rowEntityClassName);
			for (Object toEntity : entityValueList) {
				if (fromEntity.equals(toEntity)) {
					continue;
				}

				Class<?> entityClass = toEntity.getClass();
				String entityClassName = entityClass.getName();
				EntityMaping entityMapping = configure
						.getEntityMappingByClassName(entityClassName);

				EntityRelation entityRelation = configure.getEntityRelation(
						rowEntityClassName, entityClassName);
				if (entityRelation == null) {
					continue;
				}

				processEntityRelation(fromEntity, rowEntityMapping, toEntity,
						entityMapping, entityRelation);
			}
		}

		return entityResults;
	}
}
