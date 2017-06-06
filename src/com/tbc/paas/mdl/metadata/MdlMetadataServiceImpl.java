package com.tbc.paas.mdl.metadata;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.JedisCommands;

import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.cfg.domain.EntityRelation;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mql.metadata.MqlMetadataServiceImpl;
import com.tbc.paas.mql.metadata.MqlMetadataService;
import com.tbc.paas.mql.metadata.domain.Column;
import com.tbc.paas.mql.metadata.domain.CorpTable;
import com.tbc.paas.mql.metadata.domain.Table;
import com.tbc.paas.mql.metadata.domain.TableRelation;
import com.tbc.paas.mql.metadata.domain.TableView;

public class MdlMetadataServiceImpl implements MqlMetadataService {

	private Configure configure;
	private JedisCommands mdmJedisCommands;
	private MqlMetadataServiceImpl mqlMdetadataServiceImpl;

	public MdlMetadataServiceImpl() {
		super();
	}

	public MdlMetadataServiceImpl(Configure configure,
			JedisCommands mdmJedisCommands) {
		super();
		this.configure = configure;
		this.mdmJedisCommands = mdmJedisCommands;
		init();
	}

	public void init() {
		mqlMdetadataServiceImpl = new MqlMetadataServiceImpl();
		mqlMdetadataServiceImpl.setMdmJedisCommands(mdmJedisCommands);
	}

	public Configure getConfigure() {
		return configure;
	}

	public void setConfigure(Configure configure) {
		this.configure = configure;
	}

	public JedisCommands getMdmJedisCommands() {
		return mdmJedisCommands;
	}

	public void setMdmJedisCommands(JedisCommands mdmJedisCommands) {
		this.mdmJedisCommands = mdmJedisCommands;
	}

	@Override
	public TableView getTableView(String appCode, String corpCode,
			String tableName) {
		Table table = getTable(appCode, corpCode, tableName);
		if (table == null) {

		}
		Map<String, CorpTable> corpTable = getCorpTable(appCode, corpCode,
				tableName);
		TableView tableView = new TableView(table, corpTable);

		return tableView;
	}

	@Override
	public Map<String, CorpTable> getCorpTable(String appCode, String corpCode,
			String tableName) {
		EntityMaping entityMaping = configure
				.getEntityMappingByClassName(tableName);
		if (entityMaping == null) {
			throw new MdlException("Can't find entity metadata for "
					+ tableName);
		}
		String realTableName = entityMaping.getTableName();

		return mqlMdetadataServiceImpl.getCorpTable(appCode, corpCode,
				realTableName);
	}

	@Override
	public Table getTable(String appCode, String corpCode, String tableName) {

		EntityMaping entityMaping = configure
				.getEntityMappingByClassName(tableName);
		if (entityMaping == null) {
			throw new MdlException("Can't find entity metadata for "
					+ tableName);
		}

		String entityClassName = entityMaping.getEntitySimpleClassName();
		Field primaryField = entityMaping.getPrimaryField();
		String name = primaryField.getName();
		Table table = new Table(entityClassName);
		table.setPkName(name);

		Map<String, Column> columnMap = new HashMap<String, Column>();
		List<Field> fieldList = entityMaping.getFieldList();
		for (Field field : fieldList) {
			String fieldName = field.getName();
			Class<?> type = field.getType();
			String fieldTypeName = type.getName();
			Column column = new Column();
			column.setColumnName(fieldName);
			column.setColumnType(fieldTypeName);

			columnMap.put(fieldName, column);
		}

		table.setColumnMap(columnMap);
		return table;
	}

	@Override
	public TableRelation getTableRelation(String appCode, String corpCode,
			String fromTableName, String toTableName) {

		EntityRelation entityRelation = configure.getEntityRelation(
				fromTableName, toTableName);

		return entityRelation;
	}
}
