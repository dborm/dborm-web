package com.keqiaokeji.dborm.test.deep;

import com.keqiaokeji.dborm.core.Dborm;
import com.keqiaokeji.dborm.test.utils.BaseTest;
import com.keqiaokeji.dborm.test.utils.domain.LoginUser;
import com.keqiaokeji.dborm.test.utils.domain.QsmInfo;
import com.keqiaokeji.dborm.test.utils.domain.QsmOption;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RelationMoreSaveOrReplaceTest extends BaseTest {

    @Test
    public void testB10Insert() {
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
        List<QsmInfo> infoList = new ArrayList<QsmInfo>();
        for (int i = 0; i < 10; i++) {
            QsmInfo info = new QsmInfo();
            info.setQuestionId("questionId111");
            info.setContent("测试");
            infoList.add(info);
        }
        user.setQsmInfoList(null);
        //DbormDeep.getDborm().insert(user);
        boolean result = Dborm.getDborm().saveOrReplace(user);
        assertEquals(true, result);
        assertEquals(10, Dborm.getDborm().getEntityCount(QsmOption.class));
        assertEquals(0, Dborm.getDborm().getEntityCount(QsmInfo.class));

    }

    @AfterClass
    public static void deleteData() {
        cleanTable();
    }


}
