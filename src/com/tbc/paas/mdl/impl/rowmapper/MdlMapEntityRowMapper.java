package com.tbc.paas.mdl.impl.rowmapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mdl.util.MdlUtil;
import com.tbc.paas.mdl.util.ReflectUtil;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.parser.dialect.OracleDialect;
import com.tbc.paas.mql.util.SqlAliasGenerator;

public class MdlMapEntityRowMapper implements RowMapper<Map<String, Object>> {

	private Configure configure;
	private List<SqlResultColumn> resultColumns;

	public MdlMapEntityRowMapper(Configure configure,
			List<SqlResultColumn> resultColumns) {
		super();
		this.configure = configure;
		this.resultColumns = resultColumns;
	}

	@Override
	public Map<String, Object> mapRow(ResultSet rs, int rowNum)
			throws SQLException {

		Map<String, Object> rowRustltMap = new HashMap<String, Object>();
		for (int index = 1; index <= resultColumns.size(); index++) {
			SqlResultColumn sqlResultColumn = resultColumns.get(index - 1);
			String entityName = sqlResultColumn.getTableName();
			String propertyName = sqlResultColumn.getColumnName();

			EntityMaping entityMapping = configure
					.getEntityMappingByClassName(entityName);
			if (entityMapping == null) {
				throw new MdlException("Doesn't configure for entity "
						+ entityName + "!");
			}

			Object entity = getEntityInstance(sqlResultColumn, entityMapping,
					rowRustltMap);

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
				Field columnField = entityMapping
						.getFieldbyFieldName(propertyName);
				Object data = MdlUtil.fetchColumnData(rs, index, columnField);
				if (data == null) {
					continue;
				}
				data = OracleDialect.dataTypeDialectConvert(columnField, data);
				ReflectUtil.setFieldValue(columnField, entity, data);
			}
		}

		return rowRustltMap;
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
}
