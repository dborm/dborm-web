package com.tbc.paas.mql.util;

/**
 * 这个类主要为帮助Mql中的表和列生成别名.默认情况下,<br>
 * Mql的解析器会自动帮所有没有 别名的表和列生成别名.
 * 
 * @author Ztian
 * 
 */
public class MqlAlias {

	public static final String DEF_PREFIX = "a_";
	public static final String DEF_NAME_SPEARATOR = "_";

	private int index;
	private String separator;

	public MqlAlias() {
		super();
		index = 0;
		separator = DEF_NAME_SPEARATOR;
	}

	/**
	 * 这个方法负责产生一个别名.该别名会取name的首字母和以后驼峰的大写字母作为简写.<br>
	 * 例如 :如果传入tianZhen,则返回a_tz_{index}作为别名.<br>
	 * 其中a_为默认的别名前缀._{index}为默认的别名后缀,{index}会用具体的整数值替换.
	 * 
	 * @param name
	 *            要生成别名的名称.可以为空或者是空字符串.<br>
	 *            但是除了name的开头和结尾部分么外,其余部分不允许出现空白字符.<br>
	 *            否则返回值的结果是未知的.
	 * @return 生成的别名.
	 */
	public String generate(String name) {
		if (name == null) {
			return DEF_PREFIX + index++;
		}

		name = name.trim();
		if (name.isEmpty()) {
			return DEF_PREFIX + index++;
		}

		StringBuilder aliasBuilder = new StringBuilder(DEF_PREFIX);
		char ch = name.charAt(0);
		aliasBuilder.append(Character.toLowerCase(ch));
		for (int i = 1; i < name.length(); i++) {
			ch = name.charAt(i);
			if (Character.isUpperCase(ch)) {
				aliasBuilder.append(Character.toLowerCase(ch));
			}
		}

		aliasBuilder.append(DEF_NAME_SPEARATOR).append(index++);

		return aliasBuilder.toString();
	}

	/**
	 * 判断一个别名是否是由当前类生成的.判断的规则有两点.<br>
	 * <ul>
	 * <li>1.别名的前缀为"a_"</li>
	 * <li>2.别名的后缀为"_{index}",{index}代表整数
	 * </ul>
	 * 
	 * @param alias
	 *            需要判断的别名.
	 * @return 如果符合上述规则,则返回true,否则返回false.
	 */
	public static boolean isAutoGen(String alias) {
		if (alias == null || alias.isEmpty()) {
			return false;
		}

		if (!alias.startsWith(DEF_PREFIX)) {
			return false;
		}

		int lastIndexOf = alias.lastIndexOf(DEF_NAME_SPEARATOR);
		if (lastIndexOf == -1) {
			return false;
		}

		String substring = alias.substring(lastIndexOf + 1, alias.length());
		try {
			Integer.parseInt(substring);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}
}
