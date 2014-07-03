package com.keqiaokeji.dborm.test;

import com.keqiaokeji.dborm.test.deep.DeepSaveOrReplaceTest;
import com.keqiaokeji.dborm.test.deep.RelationFieldTest;
import com.keqiaokeji.dborm.test.deep.RelationMoreSaveOrReplaceTest;
import com.keqiaokeji.dborm.test.excute.DefaultValuesTest;
import com.keqiaokeji.dborm.test.excute.ListEntityTest;
import com.keqiaokeji.dborm.test.excute.SigleEntityTest;
import com.keqiaokeji.dborm.test.init.InitTest;
import com.keqiaokeji.dborm.test.query.SelectModuleTest;
import com.keqiaokeji.dborm.test.query.SelectRelationTest;
import com.keqiaokeji.dborm.test.query.SelectTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({InitTest.class,
        DeepSaveOrReplaceTest.class, RelationFieldTest.class, RelationMoreSaveOrReplaceTest.class,
        DefaultValuesTest.class, ListEntityTest.class, SigleEntityTest.class,
        SelectModuleTest.class, SelectRelationTest.class, SelectTest.class})
public class AllTests {

    @Test
    public void testAll() {
        System.out.println("运行所有的测试用例");
    }

}
