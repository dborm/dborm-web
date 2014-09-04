package cn.cocho.dborm.test.excute;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.test.utils.BaseTest;
import cn.cocho.dborm.test.utils.domain.LoginUser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * 对象的批量操作
 *
 * @author KEQIAO KEJI
 * @2013年7月27日 @下午3:05:06
 */
public class ListEntityTest extends BaseTest {

    static int nums = 10;

    @BeforeClass
    public static void setUp() {
        List<LoginUser> userList = new ArrayList<LoginUser>();
        for (int i = 0; i < nums; i++) {
            LoginUser user = new LoginUser();
            user.setId("id" + i);
            user.setUserId("userId" + i);
            user.setUserName("Tom");
            user.setAge(10);
            user.setBirthday(new Date());
            userList.add(user);
        }
        boolean result = Dborm.insert(userList);
        assertEquals(true, result);
    }

    @Test
    public void testC15ReplaceList() {
        List<LoginUser> userList = new ArrayList<LoginUser>();
        for (int i = 0; i < nums; i++) {
            LoginUser user = new LoginUser();
            user.setId("id" + i);
            user.setUserId("userId" + i);
            user.setUserName("TomReplaceToLucy");
            userList.add(user);
        }
        boolean result = Dborm.replace(userList);
        assertEquals(true, result);
    }

    @Test
    public void testC20UpdateList() {
        List<LoginUser> userList = new ArrayList<LoginUser>();
        for (int i = 0; i < nums; i++) {
            LoginUser user = new LoginUser();
            user.setId("id" + i);
            user.setUserId("userId" + i);
            user.setUserName("LucyUpdateToJack");
            user.setBirthday(new Date());
            userList.add(user);
        }
        boolean result = Dborm.update(userList);
        assertEquals(true, result);
    }

    @BeforeClass
    public static void testC30DeleteList() {
        List<LoginUser> userList = new ArrayList<LoginUser>();
        for (int i = 0; i < nums; i++) {
            LoginUser user = new LoginUser();
            user.setId("id" + i);
            user.setUserId("userId" + i);
            userList.add(user);
        }
        boolean result = Dborm.delete(userList);
        assertEquals(true, result);
    }


}
