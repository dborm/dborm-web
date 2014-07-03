package com.keqiaokeji.dborm.util;

import java.util.UUID;

/**
 * 字符串处理
 *
 * @author KEQIAO KEJI
 * @time 2013-4-17下午7:06:53
 */
public class StringUtilsDborm {

    /**
     * 不是空白（不是null,不是空字符串，不是纯空格）
     *
     * @param content 需要判断的内容
     * @return true：不为空，false：为空
     * @author KEQIAO KEJI
     * @time 2013-4-17下午7:04:20
     */
    public static boolean isNotBlank(String content) {
        return !isBlank(content);
    }

    /**
     * 是空白（是null,是空字符串，是纯空格）
     *
     * @param content 需要判断的内容
     * @return true：为空，false：不为空
     * @author KEQIAO KEJI
     * @time 2013-4-17下午7:04:20
     */
    public static boolean isBlank(String content) {
        return content == null || content.equals("") || (content.trim().length() == 0);
    }

    /**
     * 是空（是null,是空字符串）
     *
     * @param content 需要判断的内容
     * @return true：为空，false：不为空
     * @author KEQIAO KEJI
     * @time 2013-4-17下午7:04:20
     */
    public static boolean isEmpty(String content) {
        return content == null || content.equals("");
    }

    /**
     * 不是空（不是null,不是空字符串）
     *
     * @param content 需要判断的内容
     * @return true：不为空，false：为空
     * @author KEQIAO KEJI
     * @time 2013-4-17下午7:04:20
     */
    public static boolean isNotEmpty(String content) {
        return !isEmpty(content);
    }

    /**
     * 获得UUID
     *
     * @return 去除下划线的32位字符
     * @author KEQIAO KEJI
     * @time 2013-4-24下午1:01:23
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 去掉最后一个标记
     *
     * @param content 内容
     * @param sign    标记
     * @return 切除之后的字符串
     * @author KEQIAO KEJI
     * @time 2013-5-2下午4:55:06
     */
    public static String cutLastSign(String content, String sign) {
        if (isEmpty(content)) {
            return "";
        }
        if (content.endsWith(sign)) {
            content = content.substring(0, content.lastIndexOf(sign));
        }
        return content;
    }

    /**
     * 将驼峰格式的名称转换为下划线格式的名称，如：loginName转换之后为login_name
     *
     * @param name 驼峰格式的名称
     * @return 下划线格式的名称
     * @author KEQIAO KEJI
     * @time 2013-5-2下午8:29:03
     */
    public static String generateUnderlineName(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i > 0 && Character.isUpperCase(ch)) {// 首字母是大写不需要添加下划线
                sb.append('_');
            }
            sb.append(ch);
        }

        int startIndex = 0;
        if (sb.charAt(0) == '_') {
            startIndex = 1;
        }
        return sb.substring(startIndex).toLowerCase();
    }

}
