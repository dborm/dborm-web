package com.tbc.paas.mdl.attach;

public class AbstractMqlAttach {

	public static final int OPT_TIME_INTERVAL = 60000;
	public static final String CREATE_TIME = "create_time";
	public static final String CREATE_BY = "create_by";
	public static final String LAST_MODIFY_TIME = "last_modify_time";
	public static final String LAST_MODIFY_BY = "last_modify_by";
	public static final String OPT_TIME = "opt_time";

	public AbstractMqlAttach() {
		super();
	}

	/**
	 * 判断列名是否为自动维护字段(自动维护字段包括：create_by、create_time、last_modify_by、
	 * last_modify_time、opt_time)
	 * 
	 * @param columnName
	 *            列名
	 * @return 是否为自动维护字段
	 */
	public static boolean isAutoMaintainColumn(String columnName) {
		// if (columnName == null) {
		// return false;
		// }
		//
		// if (columnName.equalsIgnoreCase(CREATE_TIME)
		// || columnName.equalsIgnoreCase(CREATE_BY)
		// || columnName.equalsIgnoreCase(LAST_MODIFY_TIME)
		// || columnName.equalsIgnoreCase(LAST_MODIFY_BY)
		// || columnName.equalsIgnoreCase(OPT_TIME)) {
		// return true;
		// }

		return false;
	}

	public static boolean shouldAutoMaintainColumn(String columnName) {
		if (columnName == null) {
			return false;
		}

		if (columnName.equalsIgnoreCase(CREATE_TIME)
				|| columnName.equalsIgnoreCase(CREATE_BY)
				|| columnName.equalsIgnoreCase(LAST_MODIFY_TIME)
				|| columnName.equalsIgnoreCase(LAST_MODIFY_BY)) {
			return true;
		}

		return false;
	}

}
