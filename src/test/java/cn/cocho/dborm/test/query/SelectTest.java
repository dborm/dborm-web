package cn.cocho.dborm.test.query;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.core.SQLExecutor;
import cn.cocho.dborm.test.utils.BaseTest;
import cn.cocho.dborm.test.utils.DBLogger;
import cn.cocho.dborm.test.utils.DataBaseManager;
import cn.cocho.dborm.test.utils.domain.LoginUser;
import cn.cocho.dborm.test.utils.domain.QsmOption;
import cn.cocho.dborm.util.DbormDataBase;
import cn.cocho.dborm.util.LoggerUtilsDborm;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SelectTest extends BaseTest {


    private final static String USER_ID = "23432423asdasdq321eada";
    private final static String USER_NAME = "Tom";
    private final static int USER_AGE = 20;

    private final static String QSM_CONTENT = "测试内容";

    static Dborm dborm;

    @BeforeClass
    public static void testA10initData() {
        dborm = new Dborm(new DataBaseManager(), new DBLogger());
        LoginUser user = new LoginUser();
        user.setId("dsfdsfsdafdsfds2343sdfsdf");
        user.setUserId(USER_ID);
        user.setUserName(USER_NAME);
        user.setAge(USER_AGE);

        List<QsmOption> qsmOptionList = new ArrayList<QsmOption>();
        for (int i = 0; i < 10; i++) {
            QsmOption option = new QsmOption();
            option.setOptionId("OPTION_ID_" + i);
            option.setQuestionId("789");
            option.setContent(QSM_CONTENT);
            option.setUserId(USER_ID);
            option.setShowOrder(i + 0f);
            qsmOptionList.add(option);
        }
        user.setQsmOptionList(qsmOptionList);
        boolean result = dborm.insert(user);
        assertEquals(true, result);
    }

    @Test
    public void testB10GetEntityCount() {
        int count = dborm.getEntityCount(LoginUser.class);
        assertEquals(1, count);
    }

    @Test
    public void testB13GetCount() {
        String sql = "SELECT COUNT(*) FROM login_user where user_id = ? ";
        String[] selectionArgs = new String[]{USER_ID};
        int count = dborm.getCount(sql, selectionArgs);
        assertEquals(1, count);
    }

    @Test
    public void testB15GetEntity() {
        String sql = "SELECT * FROM login_user where user_id = ? ";
        String[] bindArgs = new String[]{USER_ID};
        LoginUser user = dborm.getEntity(sql, bindArgs, LoginUser.class);
        assertEquals(USER_NAME, user.getUserName());
    }

    @Test
    public void testB20GetEntitys() {
        String sql = "SELECT * FROM qsm_option where user_id = ? ";
        String[] bindArgs = new String[]{USER_ID};
        List<QsmOption> userList = dborm.getEntities(sql, bindArgs, QsmOption.class);
        assertEquals(10, userList.size());
        assertEquals(QSM_CONTENT, userList.get(2).getContent());
    }

    @Test
    public void testB25GetEntitiesByExample() {
        QsmOption qsmOption = new QsmOption();
        qsmOption.setUserId(USER_ID);
        List<QsmOption> userList = dborm.getEntitiesByExample(qsmOption, true);
        assertEquals(10, userList.size());
        assertEquals(QSM_CONTENT, userList.get(2).getContent());
    }


    String myMapperContantTest = "mapper_test";

    @Test
    public void testB30GetEntitys() {
        String sql = "SELECT * FROM qsm_option where user_id = ? ";
        String[] bindArgs = new String[]{USER_ID};
        List<QsmOption> userList = dborm.getEntities(sql, bindArgs, new MyMapper());
        assertEquals(10, userList.size());
        assertEquals(QSM_CONTENT+myMapperContantTest, userList.get(2).getContent());
    }

    class MyMapper implements Dborm.ResultMapper<QsmOption> {

        public QsmOption map(ResultSet rs) {
            QsmOption qsmOption = new QsmOption();
            try {
                qsmOption.setContent(rs.getString("content") + myMapperContantTest);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return qsmOption;
        }
    }

    @Test
    public void testC10GetCourse() {
        String sql = "SELECT id, user_name, age FROM login_user where user_id = ? ";
        String[] selectionArgs = new String[]{USER_ID};
        DbormDataBase dbormDataBase = dborm.getDataBase();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dbormDataBase.getConnection();
            rs = new SQLExecutor().getResultSet(sql, selectionArgs, conn);
            rs.next();
            String userName = rs.getString("user_name");
            assertEquals(USER_NAME, userName);

            int age = rs.getInt(3);
            assertEquals(USER_AGE, age);
        } catch (Exception e) {
            new LoggerUtilsDborm().error(this.getClass().getName(), e);
        } finally {
            try {
                rs.close();
                pst.close();
                dbormDataBase.closeConn(conn);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testB30GetCourse() {
        String sql = "SELECT option_id, content, show_order FROM qsm_option where user_id = ? ";
        String[] selectionArgs = new String[]{USER_ID};
        DbormDataBase dbormDataBase = dborm.getDataBase();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dbormDataBase.getConnection();
            rs = new SQLExecutor().getResultSet(sql, selectionArgs, conn);
            while (rs.next()) {
                String content = rs.getString("content");
                assertEquals(QSM_CONTENT, content);
                float showOrder = rs.getFloat(3);
                System.out.println(showOrder);//float类型的值不能直接判断是否相等
            }
        } catch (Exception e) {
            new LoggerUtilsDborm().error(this.getClass().getName(), e);
        } finally {
            try {
                rs.close();
                pst.close();
                dbormDataBase.closeConn(conn);
            } catch (Exception e) {
            }
        }
    }

    @AfterClass
    public static void deleteData() {
        cleanTable();
    }
}
