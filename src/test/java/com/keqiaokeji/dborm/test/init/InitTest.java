package com.keqiaokeji.dborm.test.init;

import com.keqiaokeji.dborm.annotation.DbormAnnotationScan;
import com.keqiaokeji.dborm.core.Dborm;
import com.keqiaokeji.dborm.test.utils.ConnectionManager;
import com.keqiaokeji.dborm.test.utils.LoggerTools;
import com.keqiaokeji.dborm.test.utils.domain.LoginUser;
import com.keqiaokeji.dborm.test.utils.domain.QsmInfo;
import com.keqiaokeji.dborm.test.utils.domain.QsmOption;
import com.keqiaokeji.dborm.util.DbormContexts;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        DbormContexts.log = new LoggerTools();
    }

    private void initDborm() {
        try {
            DbormAnnotationScan annotationUtils = new DbormAnnotationScan();
            List<String> scanPackageList = new ArrayList<String>();
            scanPackageList.add("com.keqiaokeji.dborm.test.utils");
            annotationUtils.setScanPackageList(scanPackageList);
            annotationUtils.entityClasses.add(LoginUser.class);
            annotationUtils.entityClasses.add(QsmInfo.class);
            annotationUtils.entityClasses.add(QsmOption.class);

            annotationUtils.initSchema();
            Dborm.setDbormDataBase(new ConnectionManager());
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
        boolean ins = Dborm.getDborm().insert(user);
        assertEquals(true, ins);
        LoginUser user1 = Dborm.getDborm().getEntitie("select * from login_user", null, LoginUser.class);
        boolean result = true;
        if (user1 == null) {
            result = false;
        }
        assertEquals(true, result);

    }

    @AfterClass
    public static void deleteDb() {
        boolean del = Dborm.getDborm().execSql("delete from login_user");
        assertEquals(true, del);
    }

}
