package com.tbc.paas.mdl.cfg;

import com.tbc.framework.util.ExecutionContext;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * 这个工具类被设计在开发阶段使用，用于帮助程序员生成实体和配置文件。<br>
 * 该类能从数据库中导入元数据，并生成xml或者annotation配置，及相关的domain类
 *
 * @author Ztian
 */
public class MdlDeveloperConfigUtil {

    private DataSource dataSource;

    private boolean annontation = false;
    private boolean clipTableNamePrefix = true;
    private String entityPackage = "com.tbc.app."
            + ExecutionContext.getAppCode() + ".domain";
    private String tableNamePrefix = "t_" + ExecutionContext.getAppCode();

    private String baseEntityPath = "./src/main/java/";
    private String baseXmlPath = "./src/main/resources/";

    private String baseImplPath = "./src/main/java/";

    private String interfacePackage = "com.tbc.app."
            + ExecutionContext.getAppCode() + ".service";
    private String implPackage = "com.tbc.app."
            + ExecutionContext.getAppCode() + ".impl";


    public void generateEntityAndConfigure() {
        Connection connection = null;
        ResultSet tableResultSet = null;
        try {
            connection = dataSource.getConnection();
            if(connection == null){
                throw new RuntimeException("获取数据库连接失败。");
            }
            DatabaseMetaData dbMetaData = connection.getMetaData();
            tableResultSet = dbMetaData.getTables(null, null, null,
                    new String[]{"TABLE"});
            Map<String, Object> tablesMetaData = getCoinfigAndEntitiesMetadata(
                    connection, tableResultSet);
            generateConfigureAndEntities(tablesMetaData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tableResultSet != null) {
                try {
                    tableResultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, Object> getCoinfigAndEntitiesMetadata(
            Connection connection, ResultSet tableResultSet) throws Exception {
        ResultSetMetaData tablesMetaData = tableResultSet.getMetaData();
        int tableNameIndex = getTableNameIndex(tablesMetaData);
        Map<String, Object> fullModel = new HashMap<String, Object>();
        List<Map<String, Object>> tableModels = new ArrayList<Map<String, Object>>();
        fullModel.put("tables", tableModels);
        while (tableResultSet.next()) {
            String tableName = tableResultSet.getString(tableNameIndex);
            System.out.print("Table:" + tableName);
            if (tableNamePrefix != null && tableNamePrefix.trim().length() > 0
                    && !tableName.startsWith(tableNamePrefix)) {
                System.out.println(" skip");
                continue;
            }
            System.err.println(" generate");

            String sql = "SELECT * FROM " + tableName + " LIMIT 0";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData columnMetaData = resultSet.getMetaData();
            Map<String, Object> tableModel = generateTableModel(tableName,
                    columnMetaData);
            tableModels.add(tableModel);

            resultSet.close();
            statement.close();
        }

        return fullModel;
    }

    private Map<String, Object> generateTableModel(String tableName,
                                                   ResultSetMetaData columnMetaData) throws Exception {

        Map<String, Object> model = new HashMap<String, Object>();
        String entityName = null;
        if (clipTableNamePrefix) {
            entityName = tableName.substring(tableNamePrefix.length());
        } else {
            entityName = tableName;
        }
        entityName = convertDBNameToJavaName(entityName);
        model.put("package", entityPackage);
        model.put("annotation", annontation);
        model.put("interfacePackage",interfacePackage);
        model.put("implPackage",implPackage);
        model.put("entityName", entityName);
        model.put("tableName", tableName);

        Set<String> importClasses = new HashSet<String>();
        importClasses.add(Map.class.getName());
        model.put("importClasses", importClasses);
        List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();
        model.put("columns", columns);

        String pkName = "";
        String pkProperty = "";
        boolean tablePkFound = false;
        int tableColumnCount = columnMetaData.getColumnCount();
        for (int i = 1; i <= tableColumnCount; i++) {
            Map<String, Object> column = new HashMap<String, Object>();
            String columnName = columnMetaData.getColumnName(i);
            String columnClassName = columnMetaData.getColumnClassName(i);
            if ("java.sql.Timestamp".equalsIgnoreCase(columnClassName)
                    || "java.sql.Date".equalsIgnoreCase(columnClassName)) {
                columnClassName = "java.util.Date";
            }
            if (!columnClassName.startsWith("java.lang.")) {
                importClasses.add(columnClassName);
            }
            Class<?> columnType = Class.forName(columnClassName);
            String simpleName = columnType.getSimpleName();

            column.put("columnName", columnName);
            String propertyName = convertDBNameToJavaName(columnName);
            column.put("propertyName", propertyName);
            column.put("propertyType", simpleName);
            column.put("sqlType", columnMetaData.getColumnTypeName(i));
            if (tablePkFound == false) {
                tablePkFound = autoGuessPk(tableName, columnName);
                if (tablePkFound) {
                    pkName = columnName;
                    pkProperty = propertyName;
                }
                column.put("pkColumn", tablePkFound);
            } else {
                column.put("pkColumn", false);
            }
            columns.add(column);
        }

        if (tablePkFound == false) {
            columns.get(0).put("pkColumn", true);
            pkName = (String) columns.get(0).get("columnName");
            pkProperty = (String) columns.get(0).get("propertyName");
        }

        model.put("pkName", pkName);
        model.put("pkProperty", pkProperty);

        return model;
    }

    private void generateConfigureAndEntities(Map<String, Object> tableMetaData)
            throws Exception {
        Configuration configure = createConfigure();
        File parentDictory = getEntityDictory();
        File interfaceDir = getInterfaceDirectory();
        File implDir = getImplDir();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tableModels = (List<Map<String, Object>>) tableMetaData
                .get("tables");
        for (Map<String, Object> model : tableModels) {
            String entityName = upperCaseFirstChar(model.get("entityName").toString());
            File entityFile = new File(parentDictory, entityName + ".java");
            FileWriter entityWriter = new FileWriter(entityFile);
            Template entityTemplate = configure.getTemplate("EntityTemplate.ftl");
            entityTemplate.process(model, entityWriter);
            entityWriter.close();

            File interfaceFile = new File(interfaceDir, entityName + "Service.java");
            FileWriter interfaceWriter = new FileWriter(interfaceFile);
            Template interfaceTemplate = configure.getTemplate("InterfaceTemplate.ftl");
            interfaceTemplate.process(model, interfaceWriter);
            interfaceWriter.close();

            File implFile = new File(implDir, entityName + "ServiceImpl.java");
            FileWriter implWriter = new FileWriter(implFile);
            Template implTemplate = configure.getTemplate("ImplTemplate.ftl");
            implTemplate.process(model, implWriter);
            implWriter.close();

        }

        if (!annontation) {
            Template xmlTemplate = configure
                    .getTemplate("applicationContext-entity.ftl");
            File file = new File(getXmlDictory(),
                    "applicationContext-entity.xml");
            FileWriter fileWriter = new FileWriter(file);
            xmlTemplate.process(tableMetaData, fileWriter);
            fileWriter.close();
        }
    }

    private File getXmlDictory() {
        File file = new File(baseXmlPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    private File getEntityDictory() {
        String path = baseEntityPath + entityPackage.replace('.', '/')
                + "/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    private File getInterfaceDirectory() {
        String path = baseEntityPath + interfacePackage.replace('.', '/')
                + "/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private File getImplDir() {
        String path = baseImplPath + implPackage.replace('.', '/') + "/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private String convertDBNameToJavaName(String columnName) {
        String[] slices = columnName.split("_");
        String property = slices[0];
        for (int i = 1; i < slices.length; i++) {
            String slice = slices[i];
            slice = upperCaseFirstChar(slice);
            property += slice;
        }

        return property;
    }

    private String upperCaseFirstChar(String slice) {
        if (slice == null || slice.trim().length() == 0) {
            return slice;
        }

        char firstChar = Character.toUpperCase(slice.charAt(0));
        if (slice.length() > 1) {
            slice = firstChar + slice.substring(1);
        } else {
            slice = Character.toString(firstChar);
        }

        return slice;
    }

    private int getTableNameIndex(ResultSetMetaData tablesMetaData)
            throws SQLException {
        int columnCount = tablesMetaData.getColumnCount();
        int tableNameIndex = 0;
        for (int i = 1; i <= columnCount; i++) {
            String columnName = tablesMetaData.getColumnName(i);
            if ("TABLE_NAME".equalsIgnoreCase(columnName)) {
                tableNameIndex = i;
                break;
            }
        }
        return tableNameIndex;
    }

    private boolean autoGuessPk(String tableName, String columnName) {
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(columnName)) {
            return false;
        }
        String domainName = tableName.substring(tableNamePrefix.length() + 1);
        // 第一优先级判断
        while (domainName.contains("_")) {
            if ((domainName + "_id").equalsIgnoreCase(columnName)) {
                return true;
            }
            domainName = domainName.substring(domainName.indexOf("_") + 1);
        }
        if ((domainName + "_id").equalsIgnoreCase(columnName)) {
            return true;
        }
        return false;
    }

    private Configuration createConfigure() throws IOException {
        Configuration config = new Configuration();
        config.setBooleanFormat("true,false");
        config.setDefaultEncoding("utf-8");
        config.setNumberFormat("##########");
        config.setOutputEncoding("utf-8");
        TemplateLoader templateLoader = new ClassTemplateLoader(
                this.getClass(), "");
        config.setTemplateLoader(templateLoader);
        return config;
    }

    public boolean isAnnontation() {
        return annontation;
    }

    public void setAnnontation(boolean annontation) {
        this.annontation = annontation;
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(String tableNamePrefix) {
        this.tableNamePrefix = tableNamePrefix;
    }

    public boolean isClipTableNamePrefix() {
        return clipTableNamePrefix;
    }

    public void setClipTableNamePrefix(boolean clipTableNamePrefix) {
        this.clipTableNamePrefix = clipTableNamePrefix;
    }

    public String getBaseEntityPath() {
        return baseEntityPath;
    }

    public void setBaseEntityPath(String baseEntityPath) {
        this.baseEntityPath = baseEntityPath;
    }

    public String getBaseXmlPath() {
        return baseXmlPath;
    }

    public void setBaseXmlPath(String baseXmlPath) {
        this.baseXmlPath = baseXmlPath;
    }

    public String getBaseImplPath() {
        return baseImplPath;
    }

    public void setBaseImplPath(String baseImplPath) {
        this.baseImplPath = baseImplPath;
    }

    public String getInterfacePackage() {
        return interfacePackage;
    }

    public void setInterfacePackage(String interfacePackage) {
        this.interfacePackage = interfacePackage;
    }

    public String getImplPackage() {
        return implPackage;
    }

    public void setImplPackage(String implPackage) {
        this.implPackage = implPackage;
    }
}
