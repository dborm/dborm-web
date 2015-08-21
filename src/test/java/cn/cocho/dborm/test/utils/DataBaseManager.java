package cn.cocho.dborm.test.utils;

import cn.cocho.dborm.util.DbormDataBase;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseManager extends DbormDataBase {



    /**
     * 获得数据库连接
     *
     * @return
     * @author FIRST WIT
     */
    public Connection getConnection() {
        return createConnection();
    }


    /**
     * 获得数据库连接
     *
     * @return
     * @author FIRST WIT
     * @time 2013-5-6上午10:46:44
     */
    private Connection createConnection() {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://rds3tmsxzi96h6921824.mysql.rds.aliyuncs.com:3306/dborm_test_db?useUnicode=true&characterEncoding=utf8";
        String username = "dborm";
        String password = "dborm_test";

        Connection conn = null;
        try {
            Class.forName(driver);// 加载驱动程序
            conn = DriverManager.getConnection(url, username, password);// 连续数据库
        } catch (Exception e) {
            new DBLogger().error("创建数据库连接出错！", e);
        }

        return conn;
    }

    @Override
    public <T> T beforeInsert(T entity) {

        return super.beforeInsert(entity);
    }
}
