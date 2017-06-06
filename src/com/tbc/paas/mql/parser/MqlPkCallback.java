package com.tbc.paas.mql.parser;

import java.util.List;

import com.tbc.paas.mql.util.SqlBuilder;

public interface MqlPkCallback {

	List<Object> queryPrimaryKey(SqlBuilder sqlBuilder);

}
