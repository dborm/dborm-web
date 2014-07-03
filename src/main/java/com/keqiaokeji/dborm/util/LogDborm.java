package com.keqiaokeji.dborm.util;


/**
 * 日志处理类
 *
 * @author KEQIAO KEJI
 * @time  2013-4-17 下午3:07:28
 */
public class LogDborm {

    private static String commonTarget = "com.keqiaokeji.dborm";

    /**
     * 调试信息
     *
     * @param target 目标类路径
     * @param msg    信息
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:01:53
     */
    public static void debug(String target, String msg) {
        if (DbormContexts.log != null) {
            DbormContexts.log.debug(target, msg);
        }
    }

    /**
     * 调试信息
     *
     * @param msg 信息
     * @author KEQIAO KEJI
     * @time 2013-6-17下午04:12:48
     */
    public static void debug(String msg) {
        if (DbormContexts.log != null) {
            DbormContexts.log.debug(commonTarget, msg);
        }
    }

    /**
     * 提示
     *
     * @param target 目标类路径
     * @param msg    信息
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:01:53
     */
    public static void warn(String target, String msg) {
        if (DbormContexts.log != null) {
            DbormContexts.log.warn(target, msg);
        }
    }

    /**
     * 提示
     *
     * @param msg 信息
     * @author KEQIAO KEJI
     * @time 2013-6-17下午04:13:42
     */
    public static void warn(String msg) {
        if (DbormContexts.log != null) {
            DbormContexts.log.warn(commonTarget, msg);
        }
    }

    /**
     * 异常
     *
     * @param target 目标类路径
     * @param msg    信息
     * @param e      异常对象
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:02:21
     */
    public static void error(String target, String msg, Throwable e) {
        if (DbormContexts.log != null) {
            DbormContexts.log.error(target, msg, e);
        }
    }

    /**
     * 异常
     *
     * @param target 目标类路径
     * @param e      异常对象
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:03:03
     */
    public static void error(String target, Throwable e) {
        if (DbormContexts.log != null) {
            DbormContexts.log.error(target, "", e);
        }
    }

    /**
     * 异常
     *
     * @param e 异常对象
     * @author KEQIAO KEJI
     * @time 2013-6-17下午04:14:30
     */
    public static void error(Throwable e) {
        if (DbormContexts.log != null) {
            DbormContexts.log.error(commonTarget, "", e);
        }
    }

    /**
     * 将异常对象转换为字符串
     *
     * @param e 异常类对象
     * @return 异常信息字符串
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:03:31
     */
    public static String getExcetionMsg(Throwable e) {
        if (DbormContexts.log != null) {
            return DbormContexts.log.getExcetionMsg(e);
        } else {
            return "";
        }
    }

}
