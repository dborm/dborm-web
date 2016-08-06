package org.dborm.web.utils;

import org.apache.log4j.Logger;
import org.dborm.core.api.DbormLogger;

import java.util.Date;

/**
 * 日志处理类
 *
 * @author sky
 */
public class DBLogger implements DbormLogger {

    Logger logger = Logger.getLogger(DBLogger.class);

    public void debug(String msg) {
//        logger.debug(msg);//此处使用info级别时log4j也需要将级别设置为包含info级别时才可以打印出信息
        System.out.println("日期：" + new Date() + " DEBUG  " + msg);
    }

    public void error(Throwable e) {
//        logger.error(e);
        e.printStackTrace();
    }

    public void error(String msg, Throwable e) {
//        logger.error(msg, e);
        System.out.println("日期：" + new Date() + " ERROR  " + msg);
        e.printStackTrace();
    }


}
