package com.tbc.paas.mdl.cache;

public class MdlExpireCacheImpl extends MdlCacheImpl {

	// 默认检查缓存过期的的时间间隔
	private static final long DEF_CHECH_INTERVAL = 500;
	// 默认为最大保存时间为一分钟.
	private static final int DEF_MAX_LITE_TIME = 1000 * 60;
	// 默认为10M
	private static final int DEF_MAX_CACHE_SIZE = 1024 * 1024 * 10;

	private Thread expireMonitor;
	private volatile long checkInterval;

	public MdlExpireCacheImpl() {
		super();
		init();
	}

	private void init() {
		checkInterval = DEF_CHECH_INTERVAL;
		setMaxLifetime(DEF_MAX_LITE_TIME);
		setMaxCacheSize(DEF_MAX_CACHE_SIZE);

		initMonitorThread();
	}

	private void initMonitorThread() {
		expireMonitor = new Thread() {

			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						Thread.sleep(checkInterval);
					} catch (InterruptedException e) {
						break;
					}

					deleteExpiredValue();
				}
			}
		};

		expireMonitor.setDaemon(true);
	}

	public void start() {
		expireMonitor.start();
	}

	public void stop() {
		expireMonitor.interrupt();
	}

	public long getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}
}
