package cn.cocho.dborm.util;

/**
 * Dborm环境变量
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6上午11:58:07
 */
public class DbormContexts {

    /**
     * 是否显示SQL语句（必须在DEBUG模式下并且异常处理类被实现了该参数才有效）
     */
    public static boolean showSql = true;

    /**
     * 字符串类型的数据库字段长度，根据字符串类型属性创建xml文件时生效
     */
    public static int columnLength = 64;

    /**
     * 数据库使用的编码
     */
    public static String encode = "utf8";

    /**
     * 日志记录工具类
     */
    public static LoggerDborm log = null;

}
