package com.keqiaokeji.dborm.schema;

import com.keqiaokeji.dborm.domain.ColumnBean;
import com.keqiaokeji.dborm.domain.TableBean;
import com.keqiaokeji.dborm.util.LogDborm;
import com.keqiaokeji.dborm.util.StringUtilsDborm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * 操作Schema
 *
 * @author KEQIAO KEJI
 * @time 2013-5-23下午3:25:42
 */
public class DbormSchemaInit {

    private static String schemaPath;

    public DbormSchemaInit() throws Exception {
        initSchema();
    }

    /**
     * 初始化表结构
     *
     * @return 配置信息集合
     * @throws Exception
     * @author KEQIAO KEJI
     * @time 2013-5-23下午3:26:20
     */
    public Hashtable<String, TableBean> initSchema() throws Exception {
        Hashtable<String, TableBean> tables = new Hashtable<String, TableBean>();
        List<String> schemaFiles = getSchemaFiles();
        for (String schemaFile : schemaFiles) {
            String schemaFilePath = schemaPath + File.separator + schemaFile;
            tables.putAll(getSchemaByFile(schemaFilePath));
        }
        return tables;
    }

    private List<String> getSchemaFiles() throws Exception {
        List<String> schemaFiles = new ArrayList<String>();
        URL url = Thread.currentThread().getContextClassLoader().getResource(schemaPath);
        if (url != null) {
            File file = new File(url.toURI());
            String[] files = file.list();
            for (String fileName : files) {
                if (fileName.toLowerCase().endsWith(".xml")) {// 避免不必要的文件产生干扰，如.svn文件
                    schemaFiles.add(fileName);
                }
            }
        }
        return schemaFiles;
    }

    private Map<String, TableBean> getSchemaByFile(String schemaFilePath) {
        Map<String, TableBean> tables = new HashMap<String, TableBean>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaFilePath);
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            LogDborm.error(DbormSchemaInit.class.getName(), e);
        } catch (SAXException e) {
            LogDborm.error(DbormSchemaInit.class.getName(), e);
        } catch (IOException e) {
            LogDborm.error(DbormSchemaInit.class.getName(), e);
        }
        if (document != null) {
            Element root = document.getDocumentElement();// 获得根元素
            NodeList methodList = root.getElementsByTagName(SchemaConstants.TABLE);// 获得名称为method的元素集合
            for (int i = 0; i < methodList.getLength(); i++) {// 遍历节点 DocumentBuilder builder=null;  DocumentBuilder builder=null; 
                Element table = (Element) methodList.item(i);
                String classPath = getStringMustAttributeValue(table, SchemaConstants.TABLE_CLASS_PATH);
                String name = getStringAttributeValue(table, SchemaConstants.TABLE_NAME);
                if (name == null) {
                    String className = classPath.substring(classPath.lastIndexOf("\\."));
                    StringUtilsDborm.generateUnderlineName(className);
                }
                TableBean tableDomain = new TableBean();
                tableDomain.setClassPath(classPath);
                tableDomain.setTableName(name);
                tableDomain.setColumns(getColumnDomain(tableDomain, table));
                tableDomain.setRelation(getRelation(tableDomain, table));
                tables.put(classPath, tableDomain);
            }
        }
        return tables;
    }

    /**
     * 获得列属性集合
     *
     * @param tableDomain 表结构对象
     * @param table       表标签信息
     * @return 列对象集合
     * @author KEQIAO KEJI
     * @time 2013-5-23下午3:27:02
     */
    private Map<String, ColumnBean> getColumnDomain(TableBean tableDomain, Element table) {
        Map<String, ColumnBean> fieldList = tableDomain.getColumns();
        NodeList columnList = table.getElementsByTagName(SchemaConstants.COLUMN);
        for (int j = 0; j < columnList.getLength(); j++) {
            ColumnBean columnDomain = new ColumnBean();
            Element column = (Element) columnList.item(j);
            String fieldName = getStringMustAttributeValue(column, SchemaConstants.COLUMN_FIELD_NAME);
            columnDomain.setFieldName(fieldName);
            columnDomain.setPrimaryKey(getBooleanAttributeValue(column, SchemaConstants.COLUMN_IS_PRIMARY_KEY));
            columnDomain.setDefaultValue(getDefaultValue(column));
            String columnName = StringUtilsDborm.generateUnderlineName(fieldName);
            fieldList.put(columnName, columnDomain);
        }
        return fieldList;
    }

    private Set<String> getRelation(TableBean tableDomain, Element table) {
        Set<String> relations = tableDomain.getRelation();
        NodeList relationList = table.getElementsByTagName(SchemaConstants.RELATION);
        for (int j = 0; j < relationList.getLength(); j++) {
            Element relation = (Element) relationList.item(j);
            String fieldName = getStringMustAttributeValue(relation, SchemaConstants.RELATION_FIELD_NAME);
            relations.add(fieldName);
        }
        return relations;
    }

    /**
     * 获得必填属性的值
     *
     * @param column        列标签信息
     * @param attributeName 属性名称
     * @return 属性值，如果没有值则抛出异常
     * @author KEQIAO KEJI
     * @time 2013-5-24下午4:25:23
     */
    private String getStringMustAttributeValue(Element column, String attributeName) {
        Node node = column.getAttributes().getNamedItem(attributeName);
        if (node != null) {
            return node.getNodeValue();
        } else {
            throw new IllegalArgumentException("The attribute[" + attributeName + "] in " + schemaPath + " can't be null !");
        }
    }

    /**
     * 获得字符串类型属性的值
     *
     * @param column        列标签信息
     * @param attributeName 属性名称
     * @return 对应的值或null
     * @author KEQIAO KEJI
     * @time 2013-5-24下午4:25:37
     */
    private String getStringAttributeValue(Element column, String attributeName) {
        Node node = column.getAttributes().getNamedItem(attributeName);
        if (node != null) {
            return node.getNodeValue();
        } else {
            return "";
        }
    }

    /**
     * 获得布尔类型的值
     *
     * @param column        列标签信息
     * @param attributeName 属性名称
     * @return 如果有值则取值，默认为false
     * @author KEQIAO KEJI
     * @time 2013-5-24下午4:26:13
     */
    private boolean getBooleanAttributeValue(Element column, String attributeName) {
        Node node = column.getAttributes().getNamedItem(attributeName);
        return node != null && Boolean.parseBoolean(node.getNodeValue());
    }

    /**
     * 获得设置的默认值
     *
     * @param column 列标签信息
     * @return 默认值，如果为设置则为null
     * @author KEQIAO KEJI
     */
    private Object getDefaultValue(Element column) {
        Node node = column.getAttributes().getNamedItem(SchemaConstants.COLUMN_DEFAULT_VALUE);
        if (node != null) {
            return node.getNodeValue();
        }
        return null;
    }

    public static String getSchemaPath() {
        return schemaPath;
    }

    public static void setSchemaPath(String schemaPath) {
        DbormSchemaInit.schemaPath = schemaPath;
    }
}
