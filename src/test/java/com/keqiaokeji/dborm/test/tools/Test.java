package com.keqiaokeji.dborm.test.tools;

/**
 * Created by shk on 14-7-3.
 */
public class Test {


    public static void main(String[] args) {
        try {


            String path = "/Users/shk/my-space/github-space/dborm/dborm-web/target/classes/com/keqiaokeji/dborm/domain/ColumnBean.class";
            path = path.replace("/", ".");
            path = path.substring(0, path.lastIndexOf("."));
//            Class<?> classObj = Class.forName(path);
            Class<?> classObj = Class.forName("com.keqiaokeji.dborm.test.utils.domain.LoginUser");
//            Class<?> classObj = Class.forName("com.keqiaokeji.dborm.util.DbormContexts");

//            Class<?> classObj = Class.forName("/Users/shk/my-space/github-space/dborm/dborm-web/target/test-classes/com/keqiaokeji/dborm/test/utils/domain/SelectModule.class");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
