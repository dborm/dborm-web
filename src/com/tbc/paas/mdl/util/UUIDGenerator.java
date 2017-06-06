package com.tbc.paas.mdl.util;

import java.util.UUID;

/**
 * UUID工具类
 * @author ZHANG Nan
 *
 */
public class UUIDGenerator {
	/**
	 * 获取一个不带"-"的纯32位的UUID
	 * @author ZHANG Nan
	 * @return UUID
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
