package com.tbc.framework.util;

import com.tbc.paas.mql.parser.MqlParser;

/**
 * SQL 实用类
 * 
 * @author LIU Fangran
 * 
 */
public class SqlUtil {
	/**
	 * escape like条件的保留字符(_,%，\）
	 * 
	 * @param value
	 *            ike条件
	 * @return escape后的结果
	 */
	public static String escapeLike(String value) {

		if (!MqlParser.isOracle()) {
			// Must put the \\ replacement as the first.
			value = value.replace("\\", "\\\\");
			value = value.replace("%", "\\%");
			value = value.replace("_", "\\_");
		}

		return value;
	}
}
