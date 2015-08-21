package cn.cocho.dborm.test.query;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.test.utils.BaseTest;
import cn.cocho.dborm.test.utils.DBLogger;
import cn.cocho.dborm.test.utils.DataBaseManager;
import cn.cocho.dborm.test.utils.domain.LoginUser;
import cn.cocho.dborm.test.utils.domain.QsmOption;
import cn.cocho.dborm.test.utils.domain.SelectModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * 使用查询模版的测试用例
 *
 * @author KEQIAO KEJI
 * @2013年7月27日 @下午2:59:43
 */
public class SelectModuleTest extends BaseTest {


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
        for (int i = 0; i < 100; i++) {
            QsmOption option = new QsmOption();
            option.setOptionId("OPTION_ID_" + i);
            option.setQuestionId("789");
            option.setContent(QSM_CONTENT);
            option.setUserId(USER_ID);
            option.setShowOrder(i + 10f);
            qsmOptionList.add(option);
        }
        user.setQsmOptionList(qsmOptionList);

        boolean result = dborm.insert(user);
        assertEquals(true, result);
    }

    /**
     * 测试连接查询
     *
     * @author KEQIAO KEJI
     * @time 2013-6-6下午5:45:59
     */
    @Test
    public void testB25GetJoinEntitys() {
        String sql = "SELECT u.user_id, u.user_name, q.question_id, q.content, q.show_order FROM qsm_option q LEFT JOIN login_user u ON u.user_id=q.user_id WHERE u.user_id = ? ";
        String[] bindArgs = new String[]{USER_ID};
        List<SelectModule> moduleList = dborm.getEntities(sql, bindArgs, SelectModule.class);
        for (int i = 0; i < bindArgs.length; i++) {
            SelectModule module = moduleList.get(i);
            assertEquals(USER_NAME, module.getUserName());
            assertEquals(QSM_CONTENT, module.getContent());
        }
    }

    @Test
    public void testB28GetJoinEntitys() {
        String sql = "SELECT * FROM qsm_option q LEFT JOIN login_user u ON u.user_id=q.user_id WHERE u.user_id = ? ";
        String[] bindArgs = new String[]{USER_ID};
        List<SelectModule> moduleList = dborm.getEntities(sql, bindArgs, SelectModule.class);
        for (int i = 0; i < bindArgs.length; i++) {
            SelectModule module = moduleList.get(i);
            assertEquals(USER_NAME, module.getUserName());
            assertEquals(QSM_CONTENT, module.getContent());
        }
    }

    @AfterClass
    public static void deleteData() {
        cleanTable();
    }
}
