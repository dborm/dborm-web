package com.keqiaokeji.dborm.test.excute;

import com.keqiaokeji.dborm.core.Dborm;
import com.keqiaokeji.dborm.test.utils.BaseTest;
import com.keqiaokeji.dborm.test.utils.domain.LoginUser;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * 测试数据库字段中使用默认值
 *
 * @author KEQIAO KEJI
 * @2013年8月6日 @下午6:06:30
 */
public class DefaultValuesTest extends BaseTest {


    @Test
    public void testB11Insert() {
        LoginUser user = new LoginUser();
        user.setId("ID1");
        user.setUserId("USID1");
        user.setUserName("Tom");
        user.setAge(10);
        user.setBirthday(new Date());
        boolean result = Dborm.insert(user);
        assertEquals(true, result);

        List<LoginUser> userList = Dborm.getEntities("select * from login_user", null, LoginUser.class);
        LoginUser queryUser = userList.get(0);
        Integer loginNum = queryUser.getLoginNum();
        if (loginNum != null) {
            int num = loginNum;
            assertEquals(0, num);//因为schema（表的描述文件）中设置了默认值为0
        } else {
            assertEquals(true, false);
        }
    }

    @AfterClass
    public static void testZ10DeleteDb() {
        boolean delLogin = Dborm.execSql("delete from login_user");
        assertEquals(true, delLogin);
    }


}
