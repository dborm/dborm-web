package com.tbc.paas.mql.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.util.Assert;

import redis.clients.jedis.JedisCommands;

import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mql.metadata.domain.CorpTable;
import com.tbc.paas.mql.metadata.domain.Table;
import com.tbc.paas.mql.metadata.domain.TableRelation;
import com.tbc.paas.mql.metadata.domain.TableView;

public class MqlMetadataServiceImpl implements MqlMetadataService {

	public static final String KEY_SEPARATOR = ";;";
	public static final String DEF_CORPCODE = "default";

	private final String BASIC_TYPE = "basic info";
	private final String EXT_TYPE = "ext info";
	private final String REL_TYPE = "relation info";
	private final String All_TYPE = "basic and ext info";

	private JedisCommands mdmJedisCommands;

	public MqlMetadataServiceImpl() {
		super();
	}

	public JedisCommands getMdmJedisCommands() {
		return mdmJedisCommands;
	}

	public void setMdmJedisCommands(JedisCommands mdmJedisCommands) {
		this.mdmJedisCommands = mdmJedisCommands;
	}

	@Override
	public Map<String, CorpTable> getCorpTable(String appCode, String corpCode,
			String tableName) {
		Assert.hasLength(appCode, "appCode is empty!");
		Assert.hasLength(corpCode, "corpCode is empty!");
		Assert.notNull(mdmJedisCommands,
				"Mdm redis hasn't been setted,please make sure it is started!!");

		try {
			Map<String, CorpTable> corpTableCaches = new HashMap<String, CorpTable>();
			TypeReference<Map<String, CorpTable>> typeRef = new TypeReference<Map<String, CorpTable>>() {
			};

			if (DEF_CORPCODE.equals(corpCode)) {
				return corpTableCaches;
			}

			String key = appCode + KEY_SEPARATOR + tableName;
			String json = mdmJedisCommands.hget(key, corpCode);
			if (json == null) {
				return corpTableCaches;
			}

			ObjectMapper objectMapper = new ObjectMapper();
			corpTableCaches = objectMapper.readValue(json, typeRef);

			return corpTableCaches;
		} catch (Exception e) {
			String errMsg = getErrorMessage(EXT_TYPE, appCode, corpCode,
					tableName);
			throw new MdlException(errMsg, e);
		}
	}

	@Override
	public Table getTable(String appCode, String corpCode, String tableName) {

		Assert.hasLength(appCode, "appCode is empty!");
		Assert.hasLength(corpCode, "corpCode is empty!");
		Assert.hasLength(tableName, "Table name is empty!");
		Assert.notNull(mdmJedisCommands, "Mdm redis hasn't been setted!");

		String key = appCode + KEY_SEPARATOR + tableName;
		try {
			String json = mdmJedisCommands.hget(key, DEF_CORPCODE);
			if (json == null) {
				return null;
			}

			ObjectMapper objectMapper = new ObjectMapper();
			Table tableCache = objectMapper.readValue(json, Table.class);
			return tableCache;
		} catch (Exception e) {
			String errMsg = getErrorMessage(BASIC_TYPE, appCode, corpCode,
					tableName);
			throw new MdlException(errMsg, e);
		}
	}

	@Override
	public TableView getTableView(String appCode, String corpCode,
			String tableName) {

		String errMsg = getErrorMessage(All_TYPE, appCode, corpCode, tableName);

		String key = appCode + KEY_SEPARATOR + tableName;
		List<String> tableAndExtJson = mdmJedisCommands.hmget(key,
				DEF_CORPCODE, corpCode);
		if (tableAndExtJson == null || tableAndExtJson.isEmpty()) {
			throw new MdlException(errMsg);
		}

		TableView tableView = new TableView();
		try {
			String mainTableJson = tableAndExtJson.get(0);
			ObjectMapper objectMapper = new ObjectMapper();
			Table mainTable = objectMapper
					.readValue(mainTableJson, Table.class);
			tableView.setMainTable(mainTable);

			if (tableAndExtJson.size() <= 1 || DEF_CORPCODE.equals(corpCode)) {
				tableView.setExtTables(new HashMap<String, CorpTable>());
				return tableView;
			}

			String extTablesJson = tableAndExtJson.get(1);
			if (extTablesJson == null) {
				return tableView;
			}
			TypeReference<Map<String, CorpTable>> typeRef = new TypeReference<Map<String, CorpTable>>() {
			};
			Map<String, CorpTable> extTables = objectMapper.readValue(
					extTablesJson, typeRef);
			tableView.setExtTables(extTables);
		} catch (Exception e) {
			throw new MdlException(errMsg);
		}

		return tableView;
	}

	@Override
	public TableRelation getTableRelation(String appCode, String corpCode,
			String fromTableName, String toTableName) {

		Assert.hasLength(appCode, "appCode is empty!");
		Assert.hasText(fromTableName, "Table  name is empty!");
		Assert.hasText(toTableName, "Table  name is empty!");
		Assert.notNull(mdmJedisCommands, "Mdm redis hasn't been setted!");

		String key = appCode + KEY_SEPARATOR + fromTableName + KEY_SEPARATOR
				+ toTableName;
		try {
			String json = mdmJedisCommands.hget(key, DEF_CORPCODE);
			Assert.hasText(json, "Fetch table relation from redis for "
					+ fromTableName + " and " + toTableName + " failed!");
			ObjectMapper objectMapper = new ObjectMapper();
			TableRelation tableRelation = objectMapper.readValue(json,
					TableRelation.class);
			return tableRelation;
		} catch (Exception e) {
			String errMsg = getErrorMessage(REL_TYPE, appCode, corpCode,
					fromTableName + " and " + toTableName);
			throw new MdlException(errMsg, e);
		}
	}

	private String getErrorMessage(String type, String appCode,
			String corpCode, String tableName) {
		String msg = "Mql mdetadata service fetch table[" + tableName + "] "
				+ type + " from redis for app(" + appCode + ") and corp("
				+ corpCode + ") failed! ";

		return msg;
	}
}
