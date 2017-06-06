package com.tbc.paas.mdl.impl.rowmapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.util.MdlUtil;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.parser.dialect.OracleDialect;

public class MdlUnencopRowMapper implements RowMapper<List<Object>> {

	private List<SqlResultColumn> resultColumns;
	private Configure configure;

	public MdlUnencopRowMapper(Configure configure,
			List<SqlResultColumn> resultColumns) {
		super();
		this.configure = configure;
		this.resultColumns = resultColumns;
	}

	@Override
	public List<Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
		List<Object> rowResult = new ArrayList<Object>();
		for (int index = 1; index <= resultColumns.size(); index++) {
			SqlResultColumn sqlResultColumn = resultColumns.get(index - 1);
			String tableName = sqlResultColumn.getTableName();
			Object data = null;
			if (sqlResultColumn.isAggregation()) {
				EntityMaping entityMaping = configure
						.getEntityMapingByTableName(tableName);
				data = rs.getObject(index);
				data = OracleDialect.aggregationDataTypeDialectForUnencop(
						sqlResultColumn, data, entityMaping);
			} else {
				EntityMaping entityMapping = configure
						.getEntityMappingByClassName(tableName);
				String propertyName = sqlResultColumn.getColumnName();
				Field columnField = entityMapping
						.getFieldbyFieldName(propertyName);
				data = MdlUtil.fetchColumnData(rs, index, columnField);
				data = OracleDialect.rowDataTypeDialectForUnencop(
						sqlResultColumn, data, entityMapping);
			}

			rowResult.add(data);
		}

		return rowResult;
	}
}
