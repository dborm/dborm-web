package com.tbc.paas.mql.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 这个类为Sql的构建提供便利。通过这个类，可以很容易的调整Sql的格式，<BR>
 * 同时携带Sql的参数。
 * 
 * @author Ztian
 * 
 */
public class SqlBuilder implements SqlConstants {
	private static final String BLANK = " ";
	private static final String EMPTY = "";

	private boolean batch;
	private String sqlSplit;
	private StringBuilder builder;

	private List<Object> parameterList;

	public SqlBuilder() {
		super();
		init();
	}

	public SqlBuilder(String sqlSlice) {
		this();
		append(sqlSlice);
	}

	protected void init() {
		this.batch = false;
		this.sqlSplit = BLANK;
		this.builder = new StringBuilder();
		this.parameterList = new ArrayList<Object>();
	}

	public void clear() {
		init();
	}

	public boolean isBatch() {
		return batch;
	}

	public void setBatch(boolean batch) {
		this.batch = batch;
	}

	/**
	 * 往Sql Builder里面增加新的Sql对象。
	 * 
	 * @param sqlSlice
	 *            sql片段
	 * @return 当前SqlBuilder
	 */
	public SqlBuilder append(String sqlSlice) {
		if (sqlSlice == null) {
			return this;
		}

		this.builder.append(sqlSplit);
		this.builder.append(sqlSlice);

		return this;
	}

	public SqlBuilder append(String... sqlSlices) {
		if (sqlSlices == null) {
			return this;
		}

		for (String sqlSlice : sqlSlices) {
			this.builder.append(sqlSplit);
			this.builder.append(sqlSlice);
		}

		return this;
	}

	public SqlBuilder appendAndEscapeRest(String... sqlSlices) {
		if (sqlSlices == null || sqlSlices.length == 0) {
			return this;
		}

		this.builder.append(sqlSplit);
		for (String sqlSlice : sqlSlices) {
			this.builder.append(sqlSlice);
		}

		return this;
	}

	public SqlBuilder appendParameter(String param) {
		this.builder.append(SqlConstants.SINGLE_QUOTE);
		this.builder.append(param);
		this.builder.append(SqlConstants.SINGLE_QUOTE);

		return this;
	}

	public SqlBuilder appendParameters(List<String> params) {
		if (params == null || params.size() == 0) {
			return this;
		}

		this.builder.append(sqlSplit);
		for (String param : params) {
			this.builder.append(SINGLE_QUOTE);
			this.builder.append(param);
			this.builder.append(SqlConstants.SINGLE_QUOTE);
			this.builder.append(SqlConstants.COMMA);
		}
		removeLastCharacter();

		return this;
	}

	/**
	 * 往Sql Builder里面增加新的Sql片段,同时它不会添加与之前sql片段的分隔。
	 * 
	 * @param sqlSlice
	 *            sql片段
	 * @return 当前SqlBuilder
	 */
	public SqlBuilder escapeAppend(String sqlSlice) {
		if (sqlSlice == null) {
			return this;
		}

		this.builder.append(sqlSlice);

		return this;
	}

	public SqlBuilder escapeAppend(String... sqlSlices) {
		if (sqlSlices == null) {
			return this;
		}

		for (String sqlSlice : sqlSlices) {
			this.builder.append(sqlSlice);
		}

		return this;
	}

	/**
	 * 去掉当前Builder中最后的一个字符，如果当前串中没有任何字符。则返回自己。
	 * 
	 * @return 当前SqlBuilder
	 */
	public SqlBuilder removeLastCharacter() {
		int length = this.builder.length();
		if (length <= 0) {
			return this;
		}

		this.builder.deleteCharAt(length - 1);

		return this;
	}

	/**
	 * 去掉当前Builder中最后的一个字符，如果当前串中没有任何字符。<br>
	 * 则返回自己。 如果已经存在的字符的个数少于要删除的字符个数，则删除全部字符。
	 * 
	 * @return 当前SqlBuilder
	 */
	public SqlBuilder removeLastCharacters(int n) {
		int length = this.builder.length();
		if (length == 0) {
			return this;
		}

		int start = n > length ? 0 : length - n;

		this.builder.delete(start, length);

		return this;
	}

	/**
	 * 向当前的Sql起始部分插入一个串。
	 * 
	 * @param slice
	 *            要插入的Sql片段。
	 * @return 当前SqlBuilder
	 */
	public SqlBuilder insertStartSlice(String slice) {
		if (slice == null || slice.isEmpty()) {
			return this;
		}

		this.builder.insert(0, sqlSplit + slice);

		return this;
	}

	/**
	 * 首先检查当前Sql的末尾部分，如果末尾部分和oldSlice相同，<br>
	 * newSlice替换oldSlice.如果末尾部分和oldSlice不相同，<br>
	 * 这把当前的newSilice追加到当前SqlSlice的末尾。
	 * 
	 * @param newSlice
	 *            要添加的SQL Slice。
	 * @param oldSlice
	 *            可能已存在的Sql Slice
	 * @return 当前SqlBuilder
	 */
	public SqlBuilder replaceOrAddLastSlice(String newSlice, String oldSlice) {
		if (oldSlice == null || oldSlice.isEmpty()) {
			append(newSlice);
			return this;
		}

		int sqlLength = this.builder.length();
		int oldSliceLength = oldSlice.length();
		if (oldSliceLength > sqlLength) {
			append(newSlice);
			return this;
		}

		int lastIndexOf = this.builder.lastIndexOf(oldSlice);
		if (lastIndexOf == (sqlLength - oldSliceLength)) {
			this.builder.replace(lastIndexOf, sqlLength, newSlice);
		} else {
			append(newSlice);
		}

		return this;
	}

	public SqlBuilder removeLastSlice(String str) {
		if (str == null || str.isEmpty()) {
			return this;
		}

		int length = this.builder.length();
		int lastIndexOf = this.builder.lastIndexOf(str);
		if (lastIndexOf == -1) {
			return this;
		}

		String subString = this.builder.substring(lastIndexOf + str.length(),
				length);
		if (subString.trim().length() == 0) {
			this.builder.delete(lastIndexOf, length);
		}

		return this;
	}

	public List<Object> getParameterList() {
		return parameterList;
	}

	public Object[] getParametersArray() {
		if (parameterList == null) {
			return new Object[0];
		}

		return parameterList.toArray();
	}

	public SqlBuilder setParameters(List<Object> parameters) {
		this.parameterList.clear();

		for (Object param : parameters) {
			param = convertValue(param);
			this.parameterList.add(param);
		}

		return this;
	}

	public SqlBuilder addParameter(Object param) {
		param = convertValue(param);
		this.parameterList.add(param);

		return this;
	}

	/*
	 * 由于在batch模式下，可能某个参数代表一个参数列表，所以需要转换。
	 */
	private Object convertValue(Object param) {
		if (param == null) {
			return null;
		}

		if (param instanceof List) {
			// batch模式。
			@SuppressWarnings("unchecked")
			List<Object> parameters = (List<Object>) param;
			int size = parameters.size();
			List<Object> result = new ArrayList<Object>(size);
			for (Object value : parameters) {
				if (value instanceof Date) {
					Date date = (Date) value;
					value = new Timestamp(date.getTime());
				}
				result.add(value);
			}

			return result;
		} else {
			if (param instanceof Date) {
				Date date = (Date) param;
				param = new Timestamp(date.getTime());
			}

			return param;
		}
	}

	public SqlBuilder addParameters(List<Object> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return this;
		}

		for (Object param : parameters) {
			param = convertValue(param);
			this.parameterList.add(param);
		}

		return this;
	}

	public SqlBuilder enableSlipt() {
		this.sqlSplit = BLANK;
		return this;
	}

	public SqlBuilder disableSlipt() {
		this.sqlSplit = EMPTY;
		return this;
	}

	public String getSql() {
		return this.builder.toString();
	}

	public String getSqlSplit() {
		return this.sqlSplit;
	}

	public SqlBuilder append(SqlBuilder sqlBuilder) {
		if (sqlBuilder == null) {
			return this;
		}

		if (sqlBuilder == this) {
			return this;
		}

		String sqlSlice = sqlBuilder.getSql().trim();
		if (!sqlSlice.isEmpty()) {
			this.append(sqlSlice);
		}
		this.addParameters(sqlBuilder.getParameterList());

		return this;
	}

	public SqlBuilder repeatAppend(String prefix, List<String> repeatSlices,
			String suffix) {
		if (repeatSlices == null || repeatSlices.isEmpty()) {
			return this;
		}

		if (prefix == null) {
			prefix = "";
		}

		if (suffix == null) {
			suffix = "";
		}

		for (String slice : repeatSlices) {
			builder.append(sqlSplit);
			builder.append(prefix);
			builder.append(slice);
			builder.append(suffix);
		}

		return this;
	}

	/**
	 * 用当前的sql片段替换当前builder中的最后一个字符。
	 * 
	 * @param slice
	 *            sql片段。
	 * @return 当前SqlBuilder
	 */
	public SqlBuilder replaceLastCharacter(String slice) {

		if (slice == null) {
			return this;
		}

		int length = this.builder.length();
		if (length == 0) {
			return this;
		}

		this.builder.replace(length - 1, length, slice);

		return this;
	}

	public boolean isEmpty() {

		return builder.toString().trim().isEmpty();
	}

	@Override
	public String toString() {

		return this.getSql();
	}
}
