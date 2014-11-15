package cn.cocho.dborm.util;


/**
 * 日志处理类
 *
 * @author KEQIAO KEJI
 * @time 2013-4-17 下午3:07:28
 */
public class LoggerUtilsDborm {

    /**
     * 调试信息
     *
     * @param msg 信息
     * @author KEQIAO KEJI
     * @time 2013-6-17下午04:12:48
     */
    public static void debug(String msg) {
        if (DbormContexts.log != null) {
            DbormContexts.log.debug(msg);
        }
    }


    /**
     * 异常
     *
     * @param msg 异常信息
     * @param e   异常对象
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:03:03
     */
    public static void error(String msg, Throwable e) {
        if (DbormContexts.log != null) {
            DbormContexts.log.error(msg, e);
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
            DbormContexts.log.error("",e);
        }
    }


}
