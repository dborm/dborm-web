package com.tbc.paas.mdl.cache;

/**
 * 这个类用于记录缓存中对象的信息.
 * 
 * @author Ztian
 * 
 */
public class MdlCacheInfo implements Comparable<MdlCacheInfo> {

	private String key;
	private int valueSize;
	private int readCount;
	private long createTimestamp;
	private long lastReadTimestamp;

	public MdlCacheInfo() {
		super();
		init();
	}

	public MdlCacheInfo(String key) {
		this();
		this.key = key;
	}

	private void init() {
		key = "";
		valueSize = 0;
		readCount = 0;
		createTimestamp = System.currentTimeMillis();
		lastReadTimestamp = createTimestamp;
	}

	public synchronized int getValueSize() {
		return valueSize;
	}

	public synchronized void setValueSize(int valueSize) {
		this.valueSize = valueSize;
	}

	public synchronized String getKey() {
		return this.key;
	}

	public synchronized void update() {
		readCount++;
		lastReadTimestamp = System.currentTimeMillis();
	}

	public synchronized void setCreateTimestamp(long createTimestamp) {
		this.createTimestamp = createTimestamp;
	}

	public synchronized int getReadCount() {
		return readCount;
	}

	public synchronized long getCreateTimestamp() {
		return createTimestamp;
	}

	public synchronized long getLastReadTimestamp() {
		return lastReadTimestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MdlCacheInfo other = (MdlCacheInfo) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public int compareTo(MdlCacheInfo o) {
		if (o == null) {
			return 1;
		}

		if (this == o) {
			return 0;
		}

		if (createTimestamp > o.createTimestamp) {
			return 1;
		}

		if (createTimestamp < o.createTimestamp) {
			return -1;
		}

		if (readCount > o.readCount) {
			return 1;
		}

		if (readCount < o.readCount) {
			return -1;
		}

		if (lastReadTimestamp > o.lastReadTimestamp) {
			return 1;
		}

		if (lastReadTimestamp < o.lastReadTimestamp) {
			return -1;
		}

		if (valueSize < o.readCount) {
			return 1;
		}

		if (valueSize > o.readCount) {
			return -1;
		}

		return key.compareTo(o.key);
	}

	@Override
	public String toString() {
		return "MdlCacheInfo [key=" + key + ", createTimestamp="
				+ createTimestamp + "]";
	}
}