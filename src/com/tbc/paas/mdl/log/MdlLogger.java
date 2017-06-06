package com.tbc.paas.mdl.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tbc.eagleeye.EagleEyeMDLTracer;
import com.tbc.eagleeye.EagleEyeProperty;
import com.tbc.eagleeye.hadoop.config.EagleConst;
import com.tbc.paas.mdl.util.MdlConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.util.MdlUtil;
import com.tbc.paas.mdl.util.UUIDGenerator;
import com.tbc.paas.mql.util.MqlOperation;
import com.tbc.paas.mql.util.SqlBuilder;
import com.tbc.paas.mql.util.SqlConstants;
import com.tbc.paas.mql.util.SqlHelper;

public final class MdlLogger {
	// 以下key是用于记录sql log日志头的。
	public static final String APP_CODE = "app";
	public static final String CORP_CODE = "corp";
	public static final String TRANSCATION = "tx";
	public static final String START_TIME = "start";
	public static final String BATCH_SQL = "batch";
	public static final String EXECUTE_COST_TIME = "cost";
	public static final String INVOKE_STACK = "invoker";
	public static final String SHARED = "shared";
	public static final String SQL_COMMENT = "--";
	public static final String SQL_BLOCK_END="E";
	public static final String LINE_SEPERATOR = "\n";

	// 日志级别
	public static final String LOG_LEVEL_ALL = "ALL";
	public static final String LOG_LEVEL_OFF = "OFF";
	public static final String LOG_LEVEL_QUERY = "QUERY";
	public static final String LOG_LEVEL_UPDATE = "UPDATE";

	public static final String MDL_LOG = "mdl.sql";
	public static final Log LOG = LogFactory.getLog(MDL_LOG);
	private static final ThreadLocal<String> txId = new ThreadLocal<String>();

	private static String logLevel = LOG_LEVEL_OFF;

	public static String beginTranscation() {
		String uuid = UUIDGenerator.getUUID();
		txId.set(uuid);
		return uuid;
	}

	private MdlLogger() {
		super();
	}

	public static void logCommit(MdlLogInfo mdlLog) {
		SqlBuilder sqlBuilder = new SqlBuilder("COMMIT");
		mdlLog.setSqlBuilder(sqlBuilder);

		if (!logLevel.equalsIgnoreCase(LOG_LEVEL_OFF)) {
		     recordWithoutCheck(mdlLog);
		}
        txId.set("null");
	}

	public static void logRollback(MdlLogInfo mdlLog) {
		SqlBuilder sqlBuilder = new SqlBuilder("ROLLBACK");
		mdlLog.setSqlBuilder(sqlBuilder);
		if (!logLevel.equalsIgnoreCase(LOG_LEVEL_OFF)) {
			recordWithoutCheck(mdlLog);
		}
		txId.set("null");
	}

	private static StringBuilder generateLogTitle(MdlLogInfo mdlLog) {

		String appCode = ExecutionContext.getAppCode();
		String corpCode = ExecutionContext.getCorpCode();
		String methodStackTrace = ExecutionContext.getMethodStackTrace();
		long startTime = mdlLog.getStartTime();
		long costTime = mdlLog.getCostTime();
		boolean batch = mdlLog.getSqlBuilder().isBatch();
		boolean shared = mdlLog.isShared();
		String transcation = txId.get();

		StringBuilder builder = new StringBuilder(SQL_COMMENT);
		builder.append(TRANSCATION).append(SqlConstants.COLON)
				.append(transcation).append(SqlConstants.COMMA)
				.append(APP_CODE).append(SqlConstants.COLON).append(appCode)
				.append(SqlConstants.COMMA).append(CORP_CODE)
				.append(SqlConstants.COLON).append(corpCode)
				.append(SqlConstants.COMMA).append(START_TIME)
				.append(SqlConstants.COLON).append(startTime)
				.append(SqlConstants.COMMA).append(EXECUTE_COST_TIME)
				.append(SqlConstants.COLON).append(costTime)
				.append(SqlConstants.COMMA).append(INVOKE_STACK)
				.append(SqlConstants.COLON).append(methodStackTrace);

		if (shared) {
			builder.append(SqlConstants.COMMA).append(SHARED)
					.append(SqlConstants.COLON).append(shared);
		}

		if (batch) {
			builder.append(SqlConstants.COMMA).append(BATCH_SQL)
					.append(SqlConstants.COLON).append(batch);
		}
		builder.append(LINE_SEPERATOR);

		return builder;
	}

	public static void record(MdlLogInfo mdlLog) {
		if (mdlLog == null) {
			return;
		}

        if (EagleEyeMDLTracer.traceable()){
            String sqlStr = getSqlStr(mdlLog);
            Map<String, String> map = new HashMap<String, String>();
            String dbUrl = ExecutionContext.get(MdlConstants.DB_URL);
            if (dbUrl != null){
                if (dbUrl.contains("://")){
                    dbUrl = dbUrl.substring(dbUrl.indexOf("://") + 3);
                }
                map.put(EagleConst.DB_URL,dbUrl);
            }
            if(EagleEyeProperty.MDL_SQL_LIMIT > 0 &&
                StringUtils.isNotBlank(sqlStr) && sqlStr.length() > EagleEyeProperty.MDL_SQL_LIMIT){
                sqlStr = sqlStr.substring(0, EagleEyeProperty.MDL_SQL_LIMIT);
            }
            //超过设定的时间，记录SQL语句
            if(mdlLog.getCostTime() >= EagleEyeProperty.MDL_SQL_RECORD_TIME){
                map.put(EagleConst.SQL, sqlStr);
            }
            EagleEyeMDLTracer.traceLog(mdlLog.getStartTime(), mdlLog.getStartTime() + mdlLog.getCostTime(), map);
        }

		SqlBuilder sqlBuilder = mdlLog.getSqlBuilder();
		if (!shouldRecord(sqlBuilder)) {
			return;
		}

		recordWithoutCheck(mdlLog);
	}

	private static void recordWithoutCheck(MdlLogInfo mdlLog) {
        SqlBuilder sqlBuilder = mdlLog.getSqlBuilder();
        StringBuilder builder = generateLogTitle(mdlLog);
        List<String> sqls = SqlHelper.generateExecutableSql(sqlBuilder);
        for (String sql : sqls) {
            builder.append(sql).append(";").append(LINE_SEPERATOR);
        }
        builder.append(SQL_COMMENT).append(SQL_BLOCK_END).append(LINE_SEPERATOR);
		LOG.warn(builder.toString());
	}

    private static String getSqlStr(MdlLogInfo mdlLog){
        SqlBuilder sqlBuilder = mdlLog.getSqlBuilder();
        StringBuilder builder = new StringBuilder();
        List<String> sqls = SqlHelper.generateExecutableSql(sqlBuilder);
        for (String sql : sqls) {
            builder.append(sql).append(";");
        }
        return builder.toString();
    }

	private static boolean shouldRecord(SqlBuilder sqlBuilder) {
		if (logLevel == null || logLevel.equalsIgnoreCase(LOG_LEVEL_OFF)) {
			return false;
		}

		String sql = sqlBuilder.getSql();
		MqlOperation sqlOperation = MdlUtil.getSqlOperation(sql);
		switch (sqlOperation) {
		case SELECT:
			return logLevel.equalsIgnoreCase(LOG_LEVEL_ALL)
					|| logLevel.equalsIgnoreCase(LOG_LEVEL_QUERY);
		default:
			return logLevel.equalsIgnoreCase(LOG_LEVEL_ALL)
					|| logLevel.equalsIgnoreCase(LOG_LEVEL_UPDATE);
		}
	}

	public static String getLogLevel() {
		return logLevel;
	}

	public static void setLogLevel(String logLevel) {
		MdlLogger.logLevel = logLevel;
	}
}
