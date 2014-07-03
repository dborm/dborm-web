package com.keqiaokeji.dborm.test.utils;

import java.util.Date;

import com.keqiaokeji.dborm.util.LoggerDborm;

/**
 * 日志处理类
 *
 * @author KEQIAO KEJI
 * @date 2013-4-17 下午3:07:28
 */
public class LoggerTools implements LoggerDborm {

    private String commonTarget = "com.keqiaokeji.dborm";

    /**
     * 调试信息
     *
     * @param target 目标类路径
     * @param msg    信息
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:01:53
     */
    public void debug(String target, String msg) {
        System.out.println("日期：" + new Date() + " DEBUG  TARGET:" + target + "      " + "MSG:" + msg);
    }

    /**
     * 调试信息
     *
     * @param msg
     * @return void
     * @author hsx
     * @time 2013-6-17下午04:12:48
     */
    public void debug(String msg) {
        System.out.println("日期：" + new Date() + " DEBUG  TARGET:" + commonTarget + "      " + "MSG:" + msg);

    }

    /**
     * 提示
     *
     * @param target 目标类路径
     * @param msg    信息
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:01:53
     */
    public void warn(String target, String msg) {
        System.out.println("日期：" + new Date() + " WARN  TARGET:" + target + "      " + "MSG:" + msg);
    }

    /**
     * 提示
     *
     * @param msg
     * @return void
     * @author hsx
     * @time 2013-6-17下午04:13:42
     */
    public void warn(String msg) {
        System.out.println("日期：" + new Date() + " WARN  TARGET:" + commonTarget + "      " + "MSG:" + msg);
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
    public void error(String target, String msg, Throwable e) {
        System.out.println("日期：" + new Date() + " ERROR  TARGET:" + target + "      " + "MSG:" + msg);
        e.printStackTrace();
    }

    /**
     * 异常
     *
     * @param target 目标类路径
     * @param e      异常对象
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:03:03
     */
    public void error(String target, Throwable e) {
        System.out.println("日期：" + new Date() + " ERROR  TARGET:" + target);
        e.printStackTrace();
    }

    /**
     * 异常
     *
     * @param e
     * @return void
     * @author hsx
     * @time 2013-6-17下午04:14:30
     */
    public void error(Throwable e) {
        System.out.println("日期：" + new Date() + " ERROR  TARGET:" + commonTarget);
        e.printStackTrace();
    }

    /**
     * 将异常对象转换为字符串
     *
     * @param e 异常类对象
     * @return
     * @author KEQIAO KEJI
     * @time 2013-4-22下午5:03:31
     */
    public String getExcetionMsg(Throwable e) {
        String msg = "";
        if (e != null) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            StringBuilder errMsg = new StringBuilder();
            for (StackTraceElement element : stackTrace) {
                errMsg.append(element.toString());
                errMsg.append("            ");// 换行 每个个异常栈之间换行
            }
            msg = errMsg.toString();
        }
        return msg;
    }

}
