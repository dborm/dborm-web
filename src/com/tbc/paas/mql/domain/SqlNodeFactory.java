package com.tbc.paas.mql.domain;

import com.tbc.paas.mql.grammar.Node;

public class SqlNodeFactory {

	public static Node jjtCreate(int id) {
		return new SqlNode(id);
	}

}
