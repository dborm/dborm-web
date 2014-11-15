package cn.cocho.dborm.test.deep;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.test.utils.BaseTest;
import cn.cocho.dborm.test.utils.domain.LoginUser;
import cn.cocho.dborm.test.utils.domain.QsmOption;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeepSaveOrReplaceTest extends BaseTest {

    static int num = 3;

    @BeforeClass
    public static void initData() {
        LoginUser user = new LoginUser();
        user.setId("relation111");
        user.setUserId("relation1");
        user.setAge(10);
        user.setBirthday(new Date());
        List<QsmOption> optionList = new ArrayList<QsmOption>();
        for (int i = 0; i < num; i++) {
            QsmOption option = new QsmOption();
            option.setOptionId("optionId" + i);
            option.setQuestionId("questionId111");
            option.setContent("测试");
            optionList.add(option);
        }
        user.setQsmOptionList(optionList);
        List<LoginUser> users = new ArrayList<LoginUser>();
        users.add(user);
        boolean result = Dborm.insert(users);
        assertEquals(true, result);
        assertEquals(1, Dborm.getEntityCount(LoginUser.class));
        assertEquals(num, Dborm.getEntityCount(QsmOption.class));
    }

    @Test
    public void saveOrReplace() {
        LoginUser user = new LoginUser();
        user.setId("relation111");//主键相同，将会变为修改操作，只修改有值的属性，没有值的不影响
        user.setUserId("relation1");
        user.setAge(20);
        List<QsmOption> optionList = new ArrayList<QsmOption>();
        for (int i = 0; i < num; i++) {
            QsmOption option = new QsmOption();
            option.setOptionId("optionId-replace" + i);//主键不同，SaveOrReplace的时候将会变为新增操作
            option.setQuestionId("questionId111");
            option.setContent("测试");
            optionList.add(option);
        }
        user.setQsmOptionList(optionList);
        List<LoginUser> users = new ArrayList<LoginUser>();
        users.add(user);
        boolean result = Dborm.saveOrReplace(users);
        assertEquals(true, result);
        assertEquals(1, Dborm.getEntityCount(LoginUser.class));
        assertEquals(num * 2, Dborm.getEntityCount(QsmOption.class));
    }

    @AfterClass
    public static void deleteData() {
        cleanTable();
    }
}