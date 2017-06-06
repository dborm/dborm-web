package com.tbc.paas.mdl.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 这个类帮助用户更方便的构建用于Mdl语句.
 * 
 * @author Ztian
 * 
 */
public class MdlBuilder {

	private static final String BLANK = " ";
	private static final String EMPTY = "";

	private String sqlSplit;
	private StringBuilder builder;
	private List<Object> parameterList;
	private Map<String, Object> parameterMap;

	public MdlBuilder() {
		super();
		init();
	}

	public MdlBuilder(String sqlSlice) {
		this();
		append(sqlSlice);
	}

	/**
	 * 初始化当前的Mdl Builder.
	 */
	protected void init() {
		this.sqlSplit = BLANK;
		this.builder = new StringBuilder();
		this.parameterList = new ArrayList<Object>();
		this.parameterMap = new HashMap<String, Object>();
	}

	/**
	 * 清理当前的MqlBuilder,清空所有已经存在的数据.
	 */
	public void clear() {
		init();
	}

	/**
	 * 往Mql Builder里面增加新的Sql对象。
	 * 
	 * @param sqlSlice
	 *            sql片段
	 * @return 当前SqlBuilder
	 */
	public MdlBuilder append(String sqlSlice) {
		if (sqlSlice == null) {
			return this;
		}

		this.builder.append(sqlSplit);
		this.builder.append(sqlSlice);

		return this;
	}

	/**
	 * 往Mql Builder里面增加Sql Slice,同时添加对应的参数.<br>
	 * 注意这个参数是直接往参数List里面添加的.
	 * 
	 * @param sqlSlice
	 *            新的Mql片段.
	 * @param parameters
	 *            需要添加的参数数组.
	 * @return 当前对象.
	 */
	public MdlBuilder append(String sqlSlice, Object... parameters) {
		this.append(sqlSlice);
		this.addParameterList(Arrays.asList(parameters));

		return this;
	}

	/**
	 * 往当前的Mql Builder里面增加Sql Sqlice,同时增加一个带占位符形式的参数.
	 * 
	 * @param sqlSlice
	 *            新的Mql片段.
	 * @param key
	 *            参数的名称.
	 * @param value
	 *            参数的值
	 * @return 当前片段.
	 */
	public MdlBuilder append(String sqlSlice, String placeholder, Object value) {
		this.append(sqlSlice);
		this.addParameter(placeholder, value);

		return this;
	}

	/**
	 * 往Mql Builder里面增加新的Sql片段,同时它不会添加与之前sql片段的分隔。
	 * 
	 * @param sqlSlice
	 *            sql片段
	 * @return 当前SqlBuilder
	 */
	public MdlBuilder escapeAppend(String sqlSlice) {
		if (sqlSlice == null) {
			return this;
		}

		this.builder.append(sqlSlice);

		return this;
	}

	/**
	 * 去掉当前Builder中最后的一个字符，如果当前串中没有任何字符。则返回自己。
	 * 
	 * @return 当前MdlBuilder
	 */
	public MdlBuilder removeLastCharacter() {
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
	 * @return 当前MdlBuilder
	 */
	public MdlBuilder removeLastCharacters(int n) {
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
	 * @return 当前MdlBuilder
	 */
	public MdlBuilder insertStartSlice(String slice) {
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
	 * @return 当前MdlBuilder
	 */
	public MdlBuilder replaceOrAddLastSlice(String newSlice, String oldSlice) {
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

	/**
	 * 
	 * @param str
	 * @return
	 */
	public MdlBuilder removeLastSlice(String str) {
		if (str == null || str.isEmpty()) {
			return this;
		}

		int length = this.builder.length();
		int lastIndexOf = this.builder.lastIndexOf(str);
		if (lastIndexOf != -1 && lastIndexOf == (length - str.length())) {
			this.builder.delete(lastIndexOf, length);
		}

		return this;
	}

	public MdlBuilder enableSlipt() {
		this.sqlSplit = BLANK;
		return this;
	}

	public MdlBuilder disableSlipt() {
		this.sqlSplit = EMPTY;
		return this;
	}

	public String getSql() {
		return this.builder.toString();
	}

	public String getSqlSplit() {
		return this.sqlSplit;
	}

	public MdlBuilder append(MdlBuilder mdlBuilder) {
		if (mdlBuilder == null) {
			return this;
		}

		if (mdlBuilder == this) {
			return this;
		}

		this.append(mdlBuilder.getSql());
		this.addParameterMap(mdlBuilder.getParameterMap());
		this.addParameterList(mdlBuilder.getParameterList());

		return this;
	}

	/**
	 * 用当前的sql片段替换当前builder中的最后一个字符。
	 * 
	 * @param slice
	 *            sql片段。
	 * @return 当前MdlBuilder
	 */
	public MdlBuilder replaceLastCharacter(String slice) {

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

	/**
	 * 拼写SQL语句 添加参数
	 * 
	 * @param placeholder
	 *            参数名
	 * @param value
	 *            参数值
	 * @return 当前SQL
	 */
	public MdlBuilder addParameter(String placeholder, Object value) {
		if (value instanceof java.util.Date) {
			java.util.Date date = (java.util.Date) value;
			value = new Timestamp(date.getTime());
		}
		parameterMap.put(placeholder, value);
		return this;
	}

	public MdlBuilder addParameterMap(Map<String, Object> parameters) {
		Set<Entry<String, Object>> entrySet = parameters.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof java.util.Date) {
				java.util.Date date = (java.util.Date) value;
				value = new Timestamp(date.getTime());
			}
			parameterMap.put(key, value);
		}

		return this;
	}

	public MdlBuilder setParameterMap(Map<String, Object> parameters) {
		if (parameters == null) {
			parameterMap = new HashMap<String, Object>();
		} else {

			Set<Entry<String, Object>> entrySet = parameters.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof java.util.Date) {
					java.util.Date date = (java.util.Date) value;
					value = new Timestamp(date.getTime());
				}
				parameterMap.put(key, value);
			}
		}

		return this;
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public List<Object> getParameterList() {
		return parameterList;
	}

	public MdlBuilder setParameters(List<Object> parameters) {
		if (parameters == null) {
			this.parameterList = new ArrayList<Object>();
		} else {
			for (Object parameter : parameters) {
				if (parameter instanceof java.util.Date) {
					java.util.Date date = (java.util.Date) parameter;
					parameter = new Timestamp(date.getTime());
				}
				this.parameterList.add(parameter);
			}
		}
		return this;
	}

	public MdlBuilder addParameter(Object parameter) {
		if (parameter instanceof java.util.Date) {
			java.util.Date date = (java.util.Date) parameter;
			parameter = new Timestamp(date.getTime());
		}
		this.parameterList.add(parameter);
		return this;
	}

	public MdlBuilder addParameters(Object... parameters) {
		List<Object> parameterList = Arrays.asList(parameters);
		for (Object parameter : parameterList) {
			if (parameter instanceof java.util.Date) {
				java.util.Date date = (java.util.Date) parameter;
				parameter = new Timestamp(date.getTime());
			}
			this.parameterList.add(parameter);
		}

		return this;
	}

	public MdlBuilder addParameterList(List<Object> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return this;
		}

		for (Object parameter : parameters) {
			if (parameter instanceof java.util.Date) {
				java.util.Date date = (java.util.Date) parameter;
				parameter = new Timestamp(date.getTime());
			}
			this.parameterList.add(parameter);
		}
		
		return this;
	}

	@Override
	public String toString() {

		return this.getSql();
	}
}
