package com.tbc.paas.mdl.impl.rowmapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.util.MdlUtil;
import com.tbc.paas.mdl.util.ReflectUtil;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.parser.dialect.OracleDialect;

public class MdlEntityEncopRowMapper<T> implements RowMapper<T> {

	private Class<T> entityClass;
	private List<SqlResultColumn> resultColumns;
	private Configure configure;

	public MdlEntityEncopRowMapper(Class<T> entityClass,
			List<SqlResultColumn> resultColumns, Configure configure) {
		super();
		this.entityClass = entityClass;
		this.resultColumns = resultColumns;
		this.configure = configure;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {

		String entityClassName = entityClass.getName();
		EntityMaping entityMapping = configure
				.getEntityMappingByClassName(entityClassName);
		if (entityMapping == null) {
			throw new IllegalStateException("Doesn't regist entity  "
					+ entityClassName + "!");
		}

		T entity = ReflectUtil.newInstance(entityClass);
		for (int index = 1; index <= resultColumns.size(); index++) {
			SqlResultColumn sqlResultColumn = resultColumns.get(index - 1);
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

		return entity;
	}
}
