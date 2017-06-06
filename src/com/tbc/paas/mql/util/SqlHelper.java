package com.tbc.paas.mql.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.tbc.paas.mql.domain.MqlParseException;

public class SqlHelper {
	private static final String SQL_PARAMETER_PLACEHOLDER = "?";
	public static final DateFormat SQL_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static SqlBuilder getRuntimeSql(String sql,
			Map<String, Object> parameters) {
		if (sql == null || sql.isEmpty()) {
			return null;
		}

		List<Object> realParameters = new ArrayList<Object>();

		StringBuilder builder = new StringBuilder(sql);
		int start = builder.indexOf(SqlConstants.COLON);
		while (start != -1) {
			String parameterName = getNextParameter(builder, start);
			int end = start + 1 + parameterName.length();

			if (parameters == null) {
				throw new MqlParseException("Hasn't set parameters!");
			}

			Object object = parameters.get(parameterName);
			if (object == null) {
				throw new MqlParseException("Parameter(" + parameterName
						+ ") for sql(" + sql + ") is empty!");
			}

			processParameters(builder, realParameters, start, end, object);
			start = builder.indexOf(SqlConstants.COLON, start);
		}

		SqlBuilder sqlBuilder = new SqlBuilder(builder.toString());
		sqlBuilder.setParameters(realParameters);

		return sqlBuilder;
	}

	private static void processParameters(StringBuilder builder,
			List<Object> parameters, int start, int end, Object object) {
		if (object instanceof List<?>) {
			List<?> paras = (List<?>) object;

			StringBuilder sql = new StringBuilder();
			for (int i = 0; i < paras.size(); i++) {
				sql.append(SqlConstants.QUESTION);
				sql.append(SqlConstants.COMMA);
			}

			sql.deleteCharAt(sql.length() - 1);
			parameters.addAll(paras);
			builder.replace(start, end, sql.toString());
		} else {
			builder.replace(start, end, SqlConstants.QUESTION);
			parameters.add(object);
		}
	}

	private static String getNextParameter(StringBuilder builder, int start) {
		int end = ++start;
		char ch = ' ';
		int length = builder.length();
		for (int i = start; i < length; i++) {
			end = i;
			ch = builder.charAt(i);
			if (!Character.isLetterOrDigit(ch) && !(ch == '$') && !(ch == '_')) {
				break;
			}
		}

		if (end == length - 1) {
			end = length;
		}

		String parameterName = builder.substring(start, end);
		return parameterName;
	}

	public static String printSql(SqlBuilder sqlBuilder) {
		String sql = sqlBuilder.getSql();
		List<Object> parameters = sqlBuilder.getParameterList();

		if (parameters == null) {
			return sql;
		}

		List<String> normalSqls = generateExecutableSql(sqlBuilder);

		return normalSqls.get(0);
	}

	@SuppressWarnings("unchecked")
	public static List<String> generateExecutableSql(SqlBuilder sqlBuilder) {
		String sql = sqlBuilder.getSql();
		List<String> normalSqls = new ArrayList<String>(1);
		List<Object> parameters = sqlBuilder.getParameterList();
		if (parameters == null) {
			normalSqls.add(sql);
			return normalSqls;
		}

		StringTokenizer tokenizer = new StringTokenizer(sql,
				SQL_PARAMETER_PLACEHOLDER, true);
		int countTokens = tokenizer.countTokens();
		List<String> slices = new ArrayList<String>(countTokens);
		while (tokenizer.hasMoreTokens()) {
			slices.add(tokenizer.nextToken());
		}

		if (sqlBuilder.isBatch()) {
			for (Object rowParameters : parameters) {
				List<Object> rowData = null;
				if (rowParameters instanceof List) {
					rowData = (List<Object>) rowParameters;
				} else {
					rowData = new ArrayList<Object>(1);
					rowData.add(rowParameters);
				}

				String normalSql = convertToNormalSql(slices, rowData);
				normalSqls.add(normalSql);
			}
		} else {
			String normalSql = convertToNormalSql(slices, parameters);
			normalSqls.add(normalSql);
		}

		return normalSqls;
	}

	private static String convertToNormalSql(List<String> slices,
			List<Object> parameters) {
		int index = -1;
		StringBuilder sb = new StringBuilder();
		for (String slice : slices) {
			if (SQL_PARAMETER_PLACEHOLDER.equals(slice)) {
				index++;
				Object parameter = parameters.get(index);
				String formatedParameter = formatParameter(parameter);
				sb.append(formatedParameter);
			} else {
				sb.append(slice);
			}
		}

		return sb.toString();
	}

	public static String formatParameter(Object parameter) {
		if (parameter == null) {
			return "null";
		}

		if (parameter instanceof String) {
			String param = (String) parameter;
			param = param.replace("'", "''");
			param = param.replace("\\", "\\\\");

			return "'" + param + "'";
		} else if (parameter instanceof Date) {
			return "'" + SQL_DATE_FORMAT.format((Date) parameter) + "'";
		} else {
			return parameter.toString();
		}
	}
}
