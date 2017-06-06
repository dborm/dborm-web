package com.tbc.paas.mdl.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tbc.paas.mql.parser.MqlPkCallback;
import com.tbc.paas.mql.util.SqlBuilder;
import com.tbc.paas.mql.util.SqlHelper;

public class MqlPkCallbackImpl implements MqlPkCallback {

	public static Log LOG = LogFactory.getLog(MqlPkCallbackImpl.class);

	private JdbcTemplate jdbcTemplate;

	public MqlPkCallbackImpl(JdbcTemplate jdbcTemplate) {
		super();
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Object> queryPrimaryKey(SqlBuilder sqlBuilder) {
		String sqlTemp = sqlBuilder.getSql();
		Object[] parameters = sqlBuilder.getParametersArray();
		List<Object> pkList = jdbcTemplate.queryForList(sqlTemp, parameters,
				Object.class);

		LOG.info(SqlHelper.printSql(sqlBuilder));

		return pkList;
	}
}
