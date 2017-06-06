package com.tbc.paas.mql.util;

import com.tbc.paas.mql.domain.MqlParseException;

/**
 * 现在他只为表提供别名，列的别名方法可以在后续补上。
 * 
 * @author Ztian
 * 
 */
public class SqlAliasGenerator {

	public static final String DEF_PREFIX = "a_";
	public static final char DEF_NAME_SPEARATOR = '_';

	private int index = 0;
	private char separator = DEF_NAME_SPEARATOR;

	public SqlAliasGenerator() {
		super();
	}

	public char getSeparator() {
		return separator;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}

	public String generate(String tableName) {
		if (tableName == null) {
			throw new MqlParseException("table name can't be null!");
		}

		StringBuilder builder = new StringBuilder();
		if (tableName.indexOf(DEF_NAME_SPEARATOR) == -1) {
			processNameSlice(builder, tableName);
		} else {
			String[] tableNameParts = tableName.split(String
					.valueOf(DEF_NAME_SPEARATOR));

			int start = 0;
			if (tableNameParts.length > 2) {
				start = 2;
			}

			for (int i = start; i < tableNameParts.length; i++) {
				processNameSlice(builder, tableNameParts[i]);
			}
		}

		String alias = builder.toString().toLowerCase();
		return DEF_PREFIX + alias + index++;
	}

	private void processNameSlice(StringBuilder builder, String slice) {
		char ch = slice.charAt(0);
		builder.append(ch);
		for (int i = 1; i < slice.length(); i++) {
			ch = slice.charAt(i);
			if (Character.isUpperCase(ch)) {
				builder.append(ch);
			}
		}
	}

	public static boolean isAutoGen(String alias) {
		if (alias == null || alias.isEmpty()) {
			return false;
		}

		if (!alias.startsWith(DEF_PREFIX)) {
			return false;
		}

		int lastIndexOf = alias.lastIndexOf(DEF_NAME_SPEARATOR);
		String substring = alias.substring(lastIndexOf + 1, alias.length());
		try {
			Integer.parseInt(substring);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
