package cn.cocho.dborm.test.init;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.test.utils.DBLogger;
import cn.cocho.dborm.test.utils.DataBaseManager;
import cn.cocho.dborm.test.utils.domain.LoginUser;
import cn.cocho.dborm.util.DbormContexts;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class InitTest {

    Dborm dborm;

    public InitTest() {
    }

    @Test
    public void init() {
        try {
            initDbormContexts();
            initDborm();
            checkTableIsExists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDbormContexts() {
        DbormContexts.showSql = true;
        DbormContexts.log = new DBLogger();
    }

    private void initDborm() {
        try {
            dborm = new Dborm(new DataBaseManager(), new DBLogger());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkTableIsExists() {
        LoginUser user = new LoginUser();
        user.setId("ID1234567");
        user.setUserId("USID1");
        user.setUserName("Tom");
        user.setAge(10);
        user.setBirthday(new Date());
        boolean ins = dborm.insert(user);
        assertEquals(true, ins);
        LoginUser user1 = dborm.getEntity("select * from login_user", null, LoginUser.class);
        boolean result = true;
        if (user1 == null) {
            result = false;
        }
        assertEquals(true, result);

    }

    @AfterClass
    public static void deleteDb() {
        boolean del = new Dborm(new DataBaseManager(), new DBLogger()).execSql("delete from login_user");
        assertEquals(true, del);
    }

}
