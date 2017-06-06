package com.tbc.paas.mdl.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import com.tbc.paas.mql.parser.MqlParser;
import com.tbc.paas.mql.util.MqlOperation;
import com.tbc.paas.mql.util.SqlConstants;

public final class MdlUtil {

    private static final String MARK_GT = ">";
    private static final String MARK_GT_ENCODE = "&gt;";
    private static final String MARK_LT = "<";

    public static String encode(String content) {
        if (content == null || content.length() == 0) {
            return content;
        }

        String lastMark = null;
        String lastToken = "";
        StringBuilder result = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(content, MARK_LT
                + MARK_GT, true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (MARK_LT.equals(token)) {
                lastMark = MARK_LT;
                result.append(token);
            } else if (MARK_GT.equals(token)) {
                if (MARK_LT.equals(lastMark) && !MARK_LT.equals(lastToken)) {
                    result.append(MARK_GT_ENCODE);
                } else {
                    result.append(token);
                    lastMark = MARK_GT;
                }
            } else {
                if (MARK_LT.equals(lastToken) && token.length() > 0) {
                    char ch = token.charAt(0);
                    if (!Character.isUpperCase(ch)
                            && !Character.isLowerCase(ch)) {
                        lastMark = null;
                    }
                }
                result.append(token);
            }

            lastToken = token;
        }

        return result.toString();
    }

    public static String decode(String content) {

        content = content.replaceAll(MARK_GT_ENCODE, MARK_GT);

        return content;
    }

    public static MqlOperation getSqlOperation(String sql) {
        int index = MdlUtil.getKeywordIndex(sql, SqlConstants.SELECT, 0);
        if (index != -1) {
            return MqlOperation.SELECT;
        }

        index = MdlUtil.getKeywordIndex(sql, SqlConstants.INSERT, 0);
        if (index != -1) {
            return MqlOperation.INSERT;
        }

        index = MdlUtil.getKeywordIndex(sql, SqlConstants.UPDATE, 0);
        if (index != -1) {
            return MqlOperation.UPDATE;
        }

        index = MdlUtil.getKeywordIndex(sql, SqlConstants.DELETE, 0);
        if (index != -1) {
            return MqlOperation.DELETE;
        }

        throw new IllegalArgumentException("The sql is illegal!");
    }

    public static int getKeywordIndex(String sql, String keyword, int startIndex) {
        if (sql == null || keyword == null) {
            return -1;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        String upperSql = sql.toUpperCase();
        int fromIndex = startIndex;
        int keywordIndex = -1;

        while (fromIndex < sql.length() - keyword.length()) {
            keywordIndex = upperSql.indexOf(keyword, fromIndex);
            if (keywordIndex == -1) {
                break;
            }

            if (isKeyword(upperSql, keyword, keywordIndex)) {
                break;
            }

            fromIndex = keywordIndex + 1;
        }

        return keywordIndex;
    }

    private static boolean isKeyword(String sql, String keyword, int index) {

        if (index > 0) {
            char beforeChar = sql.charAt(index - 1);
            if (!Character.isWhitespace(beforeChar)) {
                return false;
            }
        }

        if (index + keyword.length() < sql.length()) {
            char afterChar = sql.charAt(index + keyword.length());
            if (!Character.isWhitespace(afterChar)) {
                return false;
            }
        }

        return true;
    }

    public static String getSqlTypeByField(Field columnField) {
        Class<?> filedType = columnField.getType();
        if (filedType.equals(Integer.class) || filedType.equals(int.class)) {
            return "int4";
        }

        if (filedType.equals(Long.class) || filedType.equals(long.class)) {
            return "int8";
        }

        if (filedType.equals(Float.class) || filedType.equals(float.class)) {
            return "float4";
        }

        if (filedType.equals(BigDecimal.class)) {
            return "decimal";
        }

        if (filedType.equals(Date.class)) {
            return "timestamp";
        }

        if (filedType.equals(String.class)) {
            return "varchar";
        }

        if (filedType.equals(Boolean.class) || filedType.equals(boolean.class)) {
            return "bool";
        }

        if (filedType.equals(Double.class) || filedType.equals(double.class)) {
            return "float8";
        }

        return null;
    }

    /**
     * 该方法自动为PS设置参数，如果参数parameters 是 Collection的子类则会<br>
     * 把该参数变成一个Collection对象循环赋值给PS
     *
     * @param ps         需要执行的statements
     * @param parameters 需要给ps赋予值的参数或者参数列表
     * @throws SQLException 任何异常发生。
     */
    public static void fillParameters(PreparedStatement ps, Object parameters)
            throws SQLException {
        if (parameters instanceof Collection) {
            Collection<?> params = (Collection<?>) parameters;
            Iterator<?> iterator = params.iterator();
            for (int j = 1; j <= params.size(); j++) {
                Object para = iterator.next();
                if (para == null) {
                    ps.setNull(j, Types.NULL);
                } else if (para.getClass().isEnum()) {
                    ps.setObject(j, para, Types.OTHER);
                } else {
                    para = filterSpecialCharacters(para);
                    ps.setObject(j, para);
                }
            }
        } else {
            if (parameters == null) {
                ps.setNull(1, Types.NULL);
            } else if (parameters.getClass().isEnum()) {
                ps.setObject(1, parameters, Types.OTHER);
            } else {
                parameters = filterSpecialCharacters(parameters);
                ps.setObject(1, parameters);
            }
        }
    }

    private static Object filterSpecialCharacters(Object para) {
        if (para == null) {
            return para;
        }

        if (!(para instanceof String)) {
            return para;
        }

        String value = (String) para;
        boolean doubleQuotePair = false;
        boolean singleQuotePair = false;
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            if (ch == '<') {
                builder.append('＜');
            } else if (ch == '>') {
                builder.append('＞');
            } else if (ch == '\'') {
                if (singleQuotePair) {
                    builder.append('’');
                } else {
                    builder.append('‘');
                }
                singleQuotePair = !singleQuotePair;
            } else if (ch == '\"') {
                if (doubleQuotePair) {
                    builder.append("”");
                } else {
                    builder.append("“");
                }
                doubleQuotePair = !doubleQuotePair;
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    public static Map<String, Object> getExtMap(Object entity,
                                                Field dynamicField) {
        Object extProperty = ReflectUtil.getFieldValue(dynamicField, entity);
        if (extProperty == null) {
            extProperty = new HashMap<String, Object>();
            ReflectUtil.setFieldValue(dynamicField, entity, extProperty);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> dynamicColumns = (Map<String, Object>) extProperty;
        return dynamicColumns;
    }

    public static Object fetchColumnData(
            ResultSet rs, int index, Field columnField) throws SQLException {
        Object value = rs.getObject(index);
        if (!MqlParser.isOracle()) {
            return ReflectUtil.adapterValue(columnField, value);
        }

        Class<?> columnType = columnField.getType();
        Object data;
        if (Date.class.isAssignableFrom(columnType)) {
            data = rs.getTimestamp(index);
        } else {
            data = value;
        }

        return data;
    }
}
