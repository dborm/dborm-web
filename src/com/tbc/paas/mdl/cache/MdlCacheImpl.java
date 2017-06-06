package com.tbc.paas.mdl.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * 实现了Mdl的缓存模型,用于本地缓存Mdl的一些通用数据.
 * 
 * @author Ztian
 * 
 */
public class MdlCacheImpl implements MdlCache {

	// 默认缓存的个数为103
	private static final int DEF_CACHE_MAP_SIZE = 103;

	private String cacheName;
	private long maxLifetime;
	private long maxCacheSize;

	private long cacheSize;
	private Map<String, String> cacheObjectMap;
	private Map<String, MdlCacheInfo> cacheInfoMap;
	private SortedSet<MdlCacheInfo> sortedCacheKeys;

	public MdlCacheImpl() {
		super();
		init();
	}

	public MdlCacheImpl(String cacheName, long maxSize, long maxLifetime) {
		this();
		this.cacheName = cacheName;
		this.maxCacheSize = maxSize;
		this.maxLifetime = maxLifetime;
	}

	private void init() {
		cacheSize = 0;
		cacheName = UUID.randomUUID().toString();
		sortedCacheKeys = new TreeSet<MdlCacheInfo>();
		cacheObjectMap = new HashMap<String, String>(DEF_CACHE_MAP_SIZE);
		cacheInfoMap = new HashMap<String, MdlCacheInfo>(DEF_CACHE_MAP_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#put(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized boolean put(String key, String value) {
		if (key == null || value == null) {
			return false;
		}

		remove(key);
		MdlCacheInfo cacheInfo = new MdlCacheInfo(key);
		int calculateSize = calculateSize(value);
		cacheInfo.setValueSize(calculateSize);

		if (maxCacheSize > 0 && calculateSize > maxCacheSize) {
			return false;
		}

		long desiredSize = maxCacheSize - calculateSize;
		if (cacheSize > desiredSize) {
			compressCacheSize(desiredSize);
		}

		cacheSize += calculateSize;
		sortedCacheKeys.add(cacheInfo);
		cacheObjectMap.put(key, value);
		cacheInfoMap.put(key, cacheInfo);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#get(java.lang.String)
	 */
	@Override
	public synchronized String get(String key) {

		String cacheValue = cacheObjectMap.get(key);
		if (cacheValue == null) {
			return null;
		}

		MdlCacheInfo cacheInfo = cacheInfoMap.get(key);
		cacheInfo.update();

		return cacheValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#remove(java.lang.String)
	 */
	@Override
	public synchronized String remove(String key) {
		if (key == null) {
			return null;
		}

		String value = cacheObjectMap.get(key);
		if (value == null) {
			return null;
		}

		MdlCacheInfo cacheInfo = cacheInfoMap.get(key);
		int size = cacheInfo.getValueSize();
		cacheSize -= size;
		sortedCacheKeys.remove(cacheInfo);
		cacheObjectMap.remove(key);
		cacheInfoMap.remove(key);

		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#deleteExpiredValue()
	 */
	@Override
	public synchronized void deleteExpiredValue() {
		if (maxLifetime <= 0) {
			return;
		}

		MdlCacheInfo cacheInfo = new MdlCacheInfo();
		cacheInfo.setCreateTimestamp(System.currentTimeMillis() - maxLifetime);
		SortedSet<MdlCacheInfo> headSet = sortedCacheKeys.headSet(cacheInfo);
		Iterator<MdlCacheInfo> iterator = headSet.iterator();
		while (iterator.hasNext()) {
			MdlCacheInfo next = iterator.next();
			String key = next.getKey();
			cacheSize -= cacheInfo.getValueSize();
			cacheInfoMap.remove(key);
			cacheObjectMap.remove(key);
			sortedCacheKeys.remove(cacheInfo);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#compressCache()
	 */
	@Override
	public synchronized void compressCache() {
		if (maxCacheSize <= 0) {
			return;
		}

		long desiredSize = (long) (maxCacheSize * 0.97);
		if (desiredSize <= 0 || cacheSize <= desiredSize) {
			return;
		}

		desiredSize = (int) (maxCacheSize * 0.90);

		compressCacheSize(desiredSize);
	}

	/**
	 * 这个方法把缓存的占用量压缩到希望的大小.
	 * 
	 * @param desiredSize
	 *            希望压缩后缓存达到的大小.
	 */
	public synchronized void compressCacheSize(long desiredSize) {
		if (sortedCacheKeys.size() <= 0) {
			return;
		}

		while (cacheSize >= desiredSize) {
			MdlCacheInfo cacheInfo = sortedCacheKeys.first();
			String key = cacheInfo.getKey();
			cacheSize -= cacheInfo.getValueSize();
			cacheInfoMap.remove(key);
			cacheObjectMap.remove(key);
			sortedCacheKeys.remove(cacheInfo);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#getCacheSize()
	 */
	@Override
	public synchronized long getCacheSize() {
		return cacheSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#clear()
	 */
	@Override
	public synchronized void clear() {
		cacheSize = 0;
		sortedCacheKeys.clear();
		cacheObjectMap.clear();
		cacheInfoMap.clear();
	}

	public synchronized int getCacheCount() {
		return cacheObjectMap.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#getMaxCacheSize()
	 */
	@Override
	public synchronized long getMaxCacheSize() {
		return maxCacheSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#setMaxCacheSize(int)
	 */
	@Override
	public synchronized void setMaxCacheSize(int maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#getMaxLifetime()
	 */
	@Override
	public synchronized long getMaxLifetime() {
		return maxLifetime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#setMaxLifetime(long)
	 */
	@Override
	public synchronized void setMaxLifetime(long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#getCacheName()
	 */
	@Override
	public synchronized String getCacheName() {
		return cacheName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tbc.paas.mdl.cache.Cache#setCacheName(java.lang.String)
	 */
	@Override
	public synchronized void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	/**
	 * 返回当前cache的快照.
	 * 
	 * @return
	 */
	public synchronized Set<String> getCacheKeys() {

		return cacheObjectMap.keySet();
	}

	/**
	 * 计算一个String所占用的字节数目.
	 * 
	 * @param value
	 *            要计算的对象.
	 * @return
	 */
	private int calculateSize(String value) {
		if (value == null) {
			return 0;
		}

		return 4 + value.getBytes().length;
	}
}
