package com.keqiaokeji.dborm.test.excute;

import com.keqiaokeji.dborm.core.Dborm;
import com.keqiaokeji.dborm.test.utils.BaseTest;
import com.keqiaokeji.dborm.test.utils.domain.LoginUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class SigleEntityTest extends BaseTest {


    @BeforeClass
    public static void testB11Insert() {
        LoginUser user = new LoginUser();
        user.setId("ID1");
        user.setUserId("USID1");
        user.setUserName("Tom");
        user.setAge(10);
        user.setBirthday(new Date());
        boolean result = Dborm.getDborm().insert(user);
        assertEquals(true, result);
    }

    @Test
    public void testB15Replace() {
        LoginUser user = new LoginUser();
        user.setId("ID1");
        user.setUserId("USID1");
        user.setUserName("TomReplace");
        boolean result = Dborm.getDborm().replace(user);
        assertEquals(true, result);
    }

    @Test
    public void testB20Update() {
        LoginUser user = new LoginUser();
        user.setId("ID1");
        user.setUserId("USID1");
        user.setUserName("TomUpdate");
        user.setAge(10);
        user.setBirthday(new Date());
        boolean result = Dborm.getDborm().update(user);
        assertEquals(true, result);
    }

    @Test
    public void testD10SaveOrUpdate() {
        LoginUser user = new LoginUser();
        user.setId("ID2");
        user.setUserId("USID2");
        user.setUserName("Tom");
        boolean result = Dborm.getDborm().saveOrUpdate(user);
        assertEquals(true, result);
    }

    @Test
    public void testD15SaveOrUpdate() {
        LoginUser user = new LoginUser();
        user.setId("ID2");
        user.setUserId("USID2");
        user.setUserName("TomSaveOrUpdate");
        boolean result = Dborm.getDborm().saveOrUpdate(user);
        assertEquals(true, result);
    }

    @Test
    public void testD20SaveOrReplace() {
        LoginUser user = new LoginUser();
        user.setId("ID2");
        user.setUserId("USID2");
        user.setAge(10);
        boolean result = Dborm.getDborm().saveOrReplace(user);
        assertEquals(true, result);
    }

    @AfterClass
    public static void testB35Delete() {
        LoginUser user = new LoginUser();
        user.setId("ID1");
        user.setUserId("USID1");
        boolean result = Dborm.getDborm().delete(user);
        assertEquals(true, result);
    }


}
