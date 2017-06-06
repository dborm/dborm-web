package com.tbc.paas.mdl.cache;

import java.util.Set;

/**
 * 该接口定义了一个简单的缓存接口,定义出了实现缓存所必须的一些方法.
 * 
 * @author Ztian
 * 
 */

public interface MdlCache {

	/**
	 * 加入一个需要缓存的值
	 * 
	 * @param key
	 *            需要保存的对象的Key
	 * @param value
	 *            缓存的值
	 * @return 是否缓存成功.
	 */
	boolean put(String key, String value);

	/**
	 * 获取一个Key对应的缓存值.
	 * 
	 * @param key
	 *            要查询的Value对应的Key.
	 * @return key对应的Value,或者null 如果没有对应的值.
	 */
	String get(String key);

	/**
	 * 删除一个缓存中的值.
	 * 
	 * @param key
	 *            标示缓存值的Key.
	 * @return 删除掉的值,或者null,如果Key不存在.
	 */
	String remove(String key);

	/**
	 * 
	 */
	void deleteExpiredValue();

	/**
	 * 
	 */
	void compressCache();

	/**
	 * 
	 * @return
	 */
	long getCacheSize();

	/**
	 * 
	 */
	void clear();

	/**
	 * 
	 * @return
	 */
	long getMaxCacheSize();

	void setMaxCacheSize(int maxCacheSize);

	long getMaxLifetime();

	void setMaxLifetime(long maxLifetime);

	String getCacheName();

	void setCacheName(String cacheName);

	int getCacheCount();

	Set<String> getCacheKeys();
}