package cn.cocho.dborm.test.tools;

import cn.cocho.dborm.util.StringUtilsDborm;

import java.sql.*;

/**
 * 生成Domain相关的信息(该类为简单工具栏，生成核心代码，部分代码需要自己根据情况完善)
 */
public class CreateDomain {


    public static void main(String[] args) throws Exception {
        System.out.println(getBeanCodeByTableName("login_user"));
    }


    public static String getBeanCodeByTableName(String tableName) throws Exception {
        Connection conn = getConnection();
        String sql = "select * from " + tableName;
//            DatabaseMetaData metaData = conn.getMetaData();
//            ResultSet rs = metaData.getTables(null,null,"qsm_info",null);
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetMetaData data = rs.getMetaData();
        rs.next();
        String beanCode = getBeanCode(data, tableName);
        rs.close();
        stmt.close();
        conn.close();
        return beanCode;
    }

    private static String getBeanCode(ResultSetMetaData data, String tableName) throws Exception {
        StringBuilder builder = new StringBuilder();

        builder.append("\r\n\r\n");
        builder.append("import cn.cocho.dborm.annotation.Table;\r\n");
        builder.append("import cn.cocho.dborm.annotation.Column;");
        builder.append("\r\n\r\n");
        builder.append("@Table\r\n");
        builder.append("public class ");
        builder.append(StringUtilsDborm.underlineToHumpName(tableName, true));
        builder.append("{\r\n\r\n");
        for (int i = 1; i <= data.getColumnCount(); i++) {
//            @Column(isPrimaryKey = true, defaultValue = "0")
//            private String id;
            builder.append("\t@Column\r\n");
            builder.append("\tprivate ");
            builder.append(columnTypeToJavaType(data.getColumnTypeName(i))); //获得指定列的数据类型名
            builder.append(" ");
            builder.append(StringUtilsDborm.underlineToHumpName(data.getColumnName(i), false));//获得指定列的列名
            builder.append(";\r\n\n");
        }
        builder.append("\r\n\r\n\r\n}\r\n\r\n");
        return builder.toString();
    }

    private static String columnTypeToJavaType(String columnType) {
        String type = "String";
        if ("INT".equalsIgnoreCase(columnType)) {
            type = "Integer";
        } else if ("BIGINT".equalsIgnoreCase(columnType)) {
            type = "Long";
        } else if ("VARCHAR".equalsIgnoreCase(columnType)) {
            type = "String";
        }
        return type;
    }


    private static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://127.0.0.1:3306/mdl-test?useUnicode=true&characterEncoding=utf8";
            String user = "shk";
            String pass = "shk";
            conn = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }


}
