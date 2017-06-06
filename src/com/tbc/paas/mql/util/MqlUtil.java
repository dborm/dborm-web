package com.tbc.paas.mql.util;

import com.tbc.paas.mql.domain.MqlParseException;
import com.tbc.paas.mql.grammar.ParseException;
import com.tbc.paas.mql.grammar.Token;

public class MqlUtil {
	public static final int SQL_CHARATAR_COUNT = 60;

	public static MqlParseException convert(String sql,
			ParseException parseException) {
		Token nextToken = parseException.currentToken.next;
		int startColumn = nextToken.beginColumn - 2;
		int endColumn = nextToken.endColumn;

		String nearSub = StringUtil.nearSub("${" + nextToken.image + "} ",
				sql, startColumn, endColumn, SQL_CHARATAR_COUNT);

		return new MqlParseException("Please check your mql: " + nearSub,
				parseException);
	}
}
