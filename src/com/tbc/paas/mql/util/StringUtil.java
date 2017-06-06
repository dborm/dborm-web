package com.tbc.paas.mql.util;

/**
 * 这个类是String的Util类,用于截取String中指定位置的一段String片段.
 * 
 * @author Ztian
 * 
 */
public final class StringUtil {

	/**
	 * 在默认的字符串里截取从固定位置开始,向前指定字符个数的字符串.<br>
	 * 如果字符个数不够,则返回实际存在的字符. 当str为null时,还回"",<br>
	 * 否则,如果从index开始到offset指定位置的字符.字符的指定位置总是<br>
	 * 当前Index的位置.也就是说Index所在位置的字符总是会被包含在返回值中.
	 * 
	 * @param str
	 *            需要截取的Str
	 * @param index
	 *            需要截取的开始位置.
	 * @param offset
	 *            需要向前截取的位移.
	 * @return 实际截取的长度.
	 */
	public static String beforeSub(String str, int index, int offset) {
		if (isEmpty(str)) {
			return "";
		}

		if (index >= str.length() - 1) {
			return str;
		}

		int start = 0;
		if (index > offset) {
			start = index - offset + 1;
		}

		while (start > 0 && !Character.isWhitespace(str.charAt(start))) {
			start--;
		}

		return str.substring(start, index + 1);
	}

	/**
	 * 在默认的字符串里截取从固定位置开始,向前指定字符个数的字符串.<br>
	 * 如果字符个数不够,则返回实际存在的字符. 当str为null时,还回"",<br>
	 * 否则,如果从index开始到offset指定位置的字符.字符的指定位置总是<br>
	 * 当前Index的位置.也就是说Index所在位置的字符总是会被包含在返回值中.
	 * 
	 * @param str
	 *            需要截取的Str
	 * @param index
	 *            需要截取的开始位置.
	 * @param offset
	 *            需要向前截取的位移.
	 * @return 实际截取的长度.
	 */
	public static String afterSub(String str, int index, int offset) {
		if (isEmpty(str) || index >= str.length() || offset <= 0) {
			return "";
		}

		if (index < 0) {
			index = 0;
		}

		int end = index + offset;
		if (end > str.length()) {
			end = str.length();
		}

		while (end <= str.length()
				&& !Character.isWhitespace(str.charAt(end - 1))) {
			end++;
		}
		end--;

		return str.substring(index, end);
	}

	/**
	 * 在默认的字符串里截取期望长度的字符串.,向前指定字符个数的字符串.<br>
	 * 如果字符个数不够,则返回实际存在的字符. 当str为null时,还回"",<br>
	 * 否则,如果从index开始到offset指定位置的字符.字符的指定位置总是<br>
	 * 当前Index的位置.也就是说Index所在位置的字符总是会被包含在返回值中.
	 * 
	 */
	public static String nearSub(String midStr, String str, int staIndex,
			int endIndex, int expLen) {
		
		if (isEmpty(midStr)) {
			midStr = "";
		}
		
		if (isEmpty(str)) {
			return midStr;
		}

		if (expLen < 2) {
			return midStr;
		}

		int bLen = expLen / 2;
		String beforeSub = beforeSub(str, staIndex, bLen);

		int aLen = expLen - beforeSub.length();
		String afterSub = afterSub(str, endIndex, aLen);

		return beforeSub + midStr + afterSub;
	}

	/**
	 * 判断一个字符串是不是null或者"".
	 * 
	 * @param str
	 *            要判断的字符串.
	 * @return true 如果是空或者empty.
	 */
	public static boolean isEmpty(String str) {
		if (str == null || str.isEmpty()) {
			return true;
		}

		return false;
	}
}
