package cn.cocho.dborm.test.query;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.test.utils.BaseTest;
import cn.cocho.dborm.test.utils.domain.LoginUser;
import cn.cocho.dborm.test.utils.domain.QsmOption;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SelectRelationTest extends BaseTest {

    private final static String USER_ID = "23432423asdasdq321eada";
    private final static String USER_NAME = "Tom";
    private final static int USER_AGE = 20;

    private final static String QSM_CONTENT = "测试内容";

    @BeforeClass
    public static void testA10initData() {
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
            option.setShowOrder(i + 10f);
            qsmOptionList.add(option);
        }
        user.setQsmOptionList(qsmOptionList);

        boolean result = Dborm.insert(user);
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
        String sql = "SELECT u.*, q.question_id, q.content FROM qsm_option q LEFT JOIN login_user u ON u.user_id=q.user_id WHERE u.user_id = ? ";
        String[] bindArgs = new String[]{USER_ID};
        // LoginUser对象里面一定要有questionId和content属性
        List<LoginUser> userList = Dborm.getEntities(sql, bindArgs, LoginUser.class);
        for (int i = 0; i < bindArgs.length; i++) {
            LoginUser user = userList.get(i);
            assertEquals(USER_NAME, user.getUserName());
            assertEquals(QSM_CONTENT, user.getContent());// 将添加其它对象的部分字段（这些字段不需要再xml文件中标注）
        }
    }

    @Test
    public void testB28GetJoinEntitys() {
        String sql = "SELECT * FROM qsm_option q LEFT JOIN login_user u ON u.user_id=q.user_id WHERE u.user_id = ? ";
        String[] bindArgs = new String[]{USER_ID};
        List<Map<String, Object>> entityList = Dborm.getEntities(sql, bindArgs, new Class<?>[]{LoginUser.class, QsmOption.class});
        for (int i = 0; i < bindArgs.length; i++) {
            Map<String, Object> entityTeam = entityList.get(i);
            LoginUser user = (LoginUser) entityTeam.get(LoginUser.class.getName());
            QsmOption option = (QsmOption) entityTeam.get(QsmOption.class.getName());
            assertEquals(USER_NAME, user.getUserName());
            assertEquals(QSM_CONTENT, option.getContent());
        }
    }

    @AfterClass
    public static void deleteData() {
        cleanTable();
    }

}
