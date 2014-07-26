package com.keqiaokeji.dborm.test.init;

import com.keqiaokeji.dborm.core.Dborm;
import com.keqiaokeji.dborm.test.utils.DBLogger;
import com.keqiaokeji.dborm.test.utils.DataBaseManager;
import com.keqiaokeji.dborm.test.utils.domain.LoginUser;
import com.keqiaokeji.dborm.util.DbormContexts;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class InitTest {

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
            Dborm.setDbormDataBase(new DataBaseManager());
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
        boolean ins = Dborm.insert(user);
        assertEquals(true, ins);
        LoginUser user1 = Dborm.getEntitie("select * from login_user", null, LoginUser.class);
        boolean result = true;
        if (user1 == null) {
            result = false;
        }
        assertEquals(true, result);

    }

    @AfterClass
    public static void deleteDb() {
        boolean del = Dborm.execSql("delete from login_user");
        assertEquals(true, del);
    }

}
