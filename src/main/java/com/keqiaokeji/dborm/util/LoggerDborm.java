package com.keqiaokeji.dborm.util;

/**
 * Dborm日志记录处理接口
 *
 * @author KEQIAO KEJI
 * @time 2013年10月29日 @上午10:50:37
 */
public interface LoggerDborm {

    /**
     * 调试信息
     *
     * @param target 目标类路径
     * @param msg    信息
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:01:53
     */
    public void debug(String target, String msg);

    /**
     * 调试信息
     *
     * @param msg 信息
     * @author KEQIAO KEJI
     * @time 2013-6-17下午04:12:48
     */
    public void debug(String msg);

    /**
     * 提示
     *
     * @param target 目标类路径
     * @param msg    信息
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:01:53
     */
    public void warn(String target, String msg);

    /**
     * 提示
     *
     * @param msg 信息
     * @author KEQIAO KEJI
     * @time 2013-6-17下午04:13:42
     */
    public void warn(String msg);

    /**
     * 异常
     *
     * @param target 目标类路径
     * @param msg    信息
     * @param e      异常对象
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:02:21
     */
    public void error(String target, String msg, Throwable e);

    /**
     * 异常
     *
     * @param target 目标类路径
     * @param e      异常对象
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:03:03
     */
    public void error(String target, Throwable e);

    /**
     * 异常
     *
     * @param e 异常对象
     * @author KEQIAO KEJI
     * @time 2013-6-17下午04:14:30
     */
    public void error(Throwable e);

    /**
     * 将异常对象转换为字符串
     *
     * @param e 异常类对象
     * @return 异常信息字符串或空字符串（""）
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:03:31
     */
    public String getExcetionMsg(Throwable e);

}
