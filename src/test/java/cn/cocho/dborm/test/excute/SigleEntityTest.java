package cn.cocho.dborm.test.excute;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.test.utils.BaseTest;
import cn.cocho.dborm.test.utils.DBLogger;
import cn.cocho.dborm.test.utils.DataBaseManager;
import cn.cocho.dborm.test.utils.domain.LoginUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class SigleEntityTest extends BaseTest {


    static Dborm dborm;

    @BeforeClass
    public static void testB11Insert() {
        dborm = new Dborm(new DataBaseManager(), new DBLogger());

        LoginUser user = new LoginUser();
        user.setId("ID1");
        user.setUserId("USID1");
        user.setUserName("Tom");
        user.setAge(10);
        user.setBirthday(new Date());
        boolean result = dborm.insert(user);
        assertEquals(true, result);
    }

    @Test
    public void testB15Replace() {
        LoginUser user = new LoginUser();
        user.setId("ID1");
        user.setUserId("USID1");
        user.setUserName("TomReplace");
        boolean result = dborm.replace(user);
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
        boolean result = dborm.update(user);
        assertEquals(true, result);
    }

    @Test
    public void testD10SaveOrUpdate() {
        LoginUser user = new LoginUser();
        user.setId("ID2");
        user.setUserId("USID2");
        user.setUserName("Tom");
        boolean result = dborm.saveOrUpdate(user);
        assertEquals(true, result);
    }

    @Test
    public void testD15SaveOrUpdate() {
        LoginUser user = new LoginUser();
        user.setId("ID2");
        user.setUserId("USID2");
        user.setUserName("TomSaveOrUpdate");
        boolean result = dborm.saveOrUpdate(user);
        assertEquals(true, result);
    }

    @Test
    public void testD20SaveOrReplace() {
        LoginUser user = new LoginUser();
        user.setId("ID2");
        user.setUserId("USID2");
        user.setAge(10);
        boolean result = dborm.saveOrReplace(user);
        assertEquals(true, result);
    }

    @AfterClass
    public static void testB35Delete() {
        LoginUser user = new LoginUser();
        user.setId("ID1");
        user.setUserId("USID1");
        boolean result = dborm.delete(user);
        assertEquals(true, result);
    }


}
