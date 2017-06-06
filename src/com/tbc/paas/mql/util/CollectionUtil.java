package com.tbc.paas.mql.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtil {

	/**
	 * 回获取一个空,或者是以固定值初始化的List.底层会初始化一个ArrayList,<br>
	 * 封装所有initializeData.
	 * 
	 * @param initializeData
	 *            初始化List的数据.
	 * @return 一个不为null的ArrayList.
	 */
	public static <T> List<T> getInitializedList(T... initializeData) {
		if (initializeData == null || initializeData.length == 0) {
			return new ArrayList<T>();
		}

		List<T> list = new ArrayList<T>(initializeData.length);
		for (T data : initializeData) {
			list.add(data);
		}
		
		return list;
	}
}
