package cn.cocho.dborm.test.deep;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.test.utils.BaseTest;
import cn.cocho.dborm.test.utils.DBLogger;
import cn.cocho.dborm.test.utils.DataBaseManager;
import cn.cocho.dborm.test.utils.domain.LoginUser;
import cn.cocho.dborm.test.utils.domain.QsmOption;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RelationFieldTest extends BaseTest {


    static Dborm dborm;

    @BeforeClass
    public static void testB10Insert() {
        dborm = new Dborm(new DataBaseManager(), new DBLogger());
        LoginUser user = new LoginUser();
        user.setId("relation111");
        user.setUserId("relation1");
        user.setAge(10);
        user.setBirthday(new Date());
        List<QsmOption> optionList = new ArrayList<QsmOption>();
        for (int i = 0; i < 10; i++) {
            QsmOption option = new QsmOption();
            option.setOptionId("optionId" + i);
            option.setQuestionId("questionId111");
            option.setContent("测试");
            optionList.add(option);
        }
        user.setQsmOptionList(optionList);
        boolean result = dborm.insert(user);
        assertEquals(true, result);
    }

    @Test
    public void testB15Update() {
        LoginUser user = new LoginUser();
        user.setId("relation111");
        user.setUserId("relation1");
        user.setAge(20);
        user.setUserName("Jace");
        user.setBirthday(new Date());
        List<QsmOption> optionList = new ArrayList<QsmOption>();
        for (int i = 0; i < 10; i++) {
            QsmOption option = new QsmOption();
            option.setOptionId("optionId" + i);
            option.setQuestionId("questionId111");
            option.setContent("测试修改");
            optionList.add(option);
        }
        user.setQsmOptionList(optionList);
        boolean result = dborm.update(user);
        assertEquals(true, result);

    }

    @Test
    public void testB20Replace() {
        LoginUser user = new LoginUser();
        user.setId("relation111");
        user.setUserId("relation1");
        user.setAge(30);
        user.setBirthday(new Date());
        List<QsmOption> optionList = new ArrayList<QsmOption>();
        for (int i = 0; i < 10; i++) {
            QsmOption option = new QsmOption();
            option.setOptionId("optionId" + i);
            option.setQuestionId("questionId111");
            option.setContent("测试替换");
            optionList.add(option);
        }
        user.setQsmOptionList(optionList);
        boolean result = dborm.replace(user);
        assertEquals(true, result);

    }

    @AfterClass
    public static void testB10Delete() {
        LoginUser user = new LoginUser();
        user.setId("relation111");
        user.setUserId("relation1");
        user.setAge(10);
        user.setBirthday(new Date());
        List<QsmOption> optionList = new ArrayList<QsmOption>();
        for (int i = 0; i < 10; i++) {
            QsmOption option = new QsmOption();
            option.setOptionId("optionId" + i);
            option.setQuestionId("questionId111");
            option.setContent("测试");
            optionList.add(option);
        }
        user.setQsmOptionList(optionList);
        boolean result = dborm.delete(user);
        assertEquals(true, result);
    }


}
