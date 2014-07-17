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
     * @param msg 信息
     * @author KEQIAO KEJI
     * @time 2013-6-17下午04:12:48
     */
    public void debug(String msg);


    /**
     * 异常
     *
     * @param msg 异常信息
     * @param e      异常对象
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:03:03
     */
    public void error(String msg, Throwable e);

}
