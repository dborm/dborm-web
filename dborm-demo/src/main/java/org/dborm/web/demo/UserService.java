package org.dborm.web.demo;

import org.dborm.core.api.Dborm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shk
 */
@Transactional
@Service
public class UserService {

    @Autowired
    Dborm dborm;

    @Autowired
    DataSource dataSourceJDBC;

    public List<UserInfo> getList(UserInfo userInfo) {
        JdbcTemplate

        StringBuffer sql = new StringBuffer("SELECT * FROM user_info WHERE deleted = 0");
        List args = new ArrayList();
        if (userInfo.getUsername() != null && userInfo.getUsername().length() > 0) {
            sql.append(" AND username LIKE ?");
            args.add("%" + userInfo.getUsername() + "%");
        }
        sql.append(" ORDER BY update_time DESC");
        return dborm.getEntities(UserInfo.class, sql.toString(), args);
    }


    @Transactional
    public void transactional() throws Exception {
        System.out.println("测试");
        Connection conn = dataSourceJDBC.getConnection();
        conn.setAutoCommit(false);
        String sql = "insert into user_info (id, username) values('user', 'transactionTest')";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.executeUpdate();
//            throw new Exception();
//        throw new RuntimeException("测试事务");
    }

    //    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {Exception.class})
    @Transactional(readOnly = false, timeout = 360, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void transactionalbak2() {
        Connection conn = null;
        try {

            System.out.println("测试");
            Object connObj = dborm.getConnection();

            conn = dataSourceJDBC.getConnection();
//        Connection conn2 = (Connection) connObj2;
//

            conn.setAutoCommit(false);

            String sql = "insert into user_info (id, username) values('user', 'transactionTest')";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.executeUpdate();


//        String sql2 = "insert into user_info (id, username) values('user', 'transactionTest')";
            String sql2 = "insert into user_info (id, username) values('user2', 'transactionTest2')";
            PreparedStatement pst2 = conn.prepareStatement(sql2);
            pst2.executeUpdate();


//            throw new Exception();

//            conn.commit();
        } catch (Exception e) {
//            try {
//                conn.rollback();
//            } catch (SQLException e1) {
//                e1.printStackTrace();
//            }
            e.printStackTrace();
        }
//        throw new RuntimeException("测试事务");
    }


    @Transactional
    public void transactionalTestBak() throws Exception {
        UserInfo user = new UserInfo();
        user.setId("user");
        user.setUsername("transaction");
        Object con = dborm.getConnection();

        String sql = "insert into user_info (id, username) values('user','transactionTest')";
        List<String> bindArgs = new ArrayList<String>();
        bindArgs.add("user");
        bindArgs.add("trans");


        execSQL(sql, bindArgs, con);
        execSQL(sql, bindArgs, con);


//        dborm.insert(user);
//
//        dborm.insert(user);

        throw new RuntimeException("测试事务");
    }


    public void execSQL(String sql, List bindArgs, Object connection) throws Exception {
        PreparedStatement pst = null;
        Connection conn = (Connection) connection;
        try {
            pst = conn.prepareStatement(sql);
            if (bindArgs != null) {
                for (int i = 0; i < bindArgs.size(); i++) {
                    pst.setObject(i + 1, bindArgs.get(i));
                }
            }
            pst.executeUpdate();
        } catch (Exception e) {
//            conn.rollback();
            throw new Exception("出异常", e);
        } finally {
            if (pst != null) {
                pst.close();
            }
        }
    }

}
