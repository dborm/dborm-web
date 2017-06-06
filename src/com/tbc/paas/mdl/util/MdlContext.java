package com.tbc.paas.mdl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tbc.paas.mql.notify.MqlNotify;

/**
 * 这个类是为Mdl的线程提供上下文变量存储,<br>
 * 在这个类中可以存储各种类型的变量.
 * 
 * @author Ztian
 * 
 */
public final class MdlContext {

	private static ThreadLocal<List<MqlNotify>> mqlTransNotify = new ThreadLocal<List<MqlNotify>>();
	private static ThreadLocal<Map<String, Object>> context = new ThreadLocal<Map<String, Object>>();

	public static void addMqlNotify(MqlNotify mqlNotify) {
		List<MqlNotify> mqlNotifyList = mqlTransNotify.get();
		if (mqlNotifyList == null) {
			mqlNotifyList = new ArrayList<MqlNotify>();
			mqlTransNotify.set(mqlNotifyList);
		}

		mqlNotifyList.add(mqlNotify);
	}

	public static List<MqlNotify> getMqlNotifyList() {
		return mqlTransNotify.get();
	}

	public static void clearNotifyList() {
		mqlTransNotify.set(null);
	}

	public static <T> T get(String key) {
		Map<String, Object> contextMap = context.get();
		if (contextMap == null) {
			return null;
		}

		Object object = contextMap.get(key);
		if (object == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		T obj = (T) object;
		return obj;
	}

	public static <T> void put(String key, T value) {
		Map<String, Object> contextMap = context.get();

		if (contextMap == null) {
			contextMap = new HashMap<String, Object>();
			context.set(contextMap);
		}

		contextMap.put(key, value);
	}

	public static void remove(String key) {
		Map<String, Object> contextMap = context.get();
		if (contextMap == null) {
			return;
		}

		contextMap.remove(key);
	}

	public static void clear() {
		Map<String, Object> contextMap = context.get();
		if (contextMap == null) {
			return;
		}

		contextMap.clear();
	}

	public static List<String> getKeyList() {
		Map<String, Object> contextMap = context.get();
		if (contextMap == null) {
			return null;
		}

		Set<String> keySet = contextMap.keySet();

		return new ArrayList<String>(keySet);
	}

	public static List<Object> getValueList() {
		Map<String, Object> contextMap = context.get();
		if (contextMap == null) {
			return null;
		}
		Collection<Object> values = contextMap.values();

		return new ArrayList<Object>(values);
	}

}
