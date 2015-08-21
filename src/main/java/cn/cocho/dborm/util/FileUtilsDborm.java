package cn.cocho.dborm.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class FileUtilsDborm {

    StringUtilsDborm stringUtils = new StringUtilsDborm();

    /**
     * 获取环境变量下的资源文件
     *
     * @param propertyName 资源文件名称
     * @return 资源文件信息对象
     * @time 2013-4-28上午11:56:17
     */
    public Properties getSourcePathProperties(String propertyName) {
        Properties prop = null;
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyName);
            if (in != null) {
                prop = new Properties();
                prop.load(in);
            } else {
                throw new RuntimeException("无法读取资源文件：" + propertyName);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法读取资源文件：" + propertyName, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignored) {
            }
        }
        return prop;
    }

    public Set<String> scanFiles(File file) {
        Set<String> filePathList = new HashSet<String>();
        if (file.exists()) {
            if (file.isFile()) {
                filePathList.add(file.getAbsolutePath());
            } else {
                File[] files = file.listFiles();
                for (File file1 : files) {
                    filePathList.addAll(scanFiles(file1));
                }
            }
        }
        return filePathList;
    }


    public Set<String> scanClassFileInPackages(String packageName) {
        Set<String> filePathList = new HashSet<String>();
        if (stringUtils.isNotBlank(packageName)) {
            String packageDirName = packageName.replace(".", "/");
            String packagePrefix = packageDirName;
            String packageSuffix = null;
            if (packageName.contains("*")) {
                packagePrefix = packageName.substring(0, packageName.indexOf("*"));
                packageSuffix = packageName.substring(packageName.indexOf("*"));
            }
            Set<String> allFilePathList = new HashSet<String>();
            try {
                Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packagePrefix);
                while (dirs.hasMoreElements()) {
                    URL url = dirs.nextElement();
                    File file = new File(url.getFile());
                    allFilePathList.addAll(scanFiles(file));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (stringUtils.isNotBlank(packageSuffix)) {//如果后缀不为空，则再次根据后缀过滤
                for (String filePath : allFilePathList) {
                    String path = filePath.substring(0, filePath.lastIndexOf("/"));
                    if (path.endsWith(packageSuffix)) {//最后一个包名包含指定后缀
                        filePathList.add(filePath);
                    }
                }
            } else {
                filePathList.addAll(allFilePathList);
            }
        }
        return filePathList;
    }


    public Set<Class<?>> scanClassInPackages(List<String> packageNames) {
        Set<Class<?>> results = new HashSet<Class<?>>();
        for (String packageName : packageNames) {
            Set<String> filePathList = scanClassFileInPackages(packageName);
            for (String filePath : filePathList) {
                try {
                    Class<?> classObj = Class.forName(filePath);
                    results.add(classObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return results;
    }


//    public static void main(String[] args) {
//        try {
//
//
//            String path = "/Users/shk/my-space/github-space/dborm/dborm-web/target/classes/com/keqiaokeji/dborm/domain/ColumnBean.class";
//            path = path.replace("/", ".");
//            path = path.substring(0, path.lastIndexOf("."));
////            Class<?> classObj = Class.forName(path);
//            Class<?> classObj = Class.forName("cn.cocho.dborm.test.utils.domain.LoginUser");
////            Class<?> classObj = Class.forName("cn.cocho.dborm.util.DbormContexts");
//
////            Class<?> classObj = Class.forName("/Users/shk/my-space/github-space/dborm/dborm-web/target/test-classes/com/keqiaokeji/dborm/test/utils/domain/SelectModule.class");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
