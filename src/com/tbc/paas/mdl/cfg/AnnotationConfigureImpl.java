/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tbc.paas.mdl.cfg;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import com.tbc.paas.mdl.util.ReflectUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.tbc.paas.mdl.cfg.annotation.Column;
import com.tbc.paas.mdl.cfg.annotation.Dynamic;
import com.tbc.paas.mdl.cfg.annotation.Id;
import com.tbc.paas.mdl.cfg.annotation.Table;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mdl.util.MdlUtil;

/**
 * 这个类用于结合Spring配置所有注册的实体类
 *
 * @author Ztian
 */
public class AnnotationConfigureImpl extends AbstractConfigure implements
        ApplicationListener<ContextRefreshedEvent> {

    public AnnotationConfigureImpl() {
        super();
    }

    /**
     * 添加实体映射关系
     *
     * @param entityClass
     */
    public void addEntityClass(Class<?> entityClass) {
        String entityClassName = entityClass.getName();
        // 如果类名实体集合中存在则终止
        if (classNameEntityMap.containsKey(entityClassName)) {
            return;
        }
        // 解析实体类返回实体映射对象
        EntityMaping maping = analyzeClass(entityClass);
        addEntityMapping(maping);
    }

    /**
     * 解析实体类
     *
     * @param entityClass 实体类
     * @return 实体映射关系对象
     */
    private EntityMaping analyzeClass(Class<?> entityClass) {
        // 如果实体类中不存在Table的Annotation则返回null
        if (!entityClass.isAnnotationPresent(Table.class)) {
            return null;
        }

        EntityMaping mapping = new EntityMaping();
        // 添加实体类到mapping集合
        mapping.setEntityClass(entityClass);
        // 通过Annotation的Table标记获取table对象
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        // 添加表名到mapping集合
        mapping.setTableName(tableAnnotation.tableName());
        configShareState(tableAnnotation, mapping);

        String extMapName = null;
        // 判断是否包含Dynamic的Annotation标记
        if (entityClass.isAnnotationPresent(Dynamic.class)) {
            // 通过Annotation的Dynamic标记获取Dynamic对象
            Dynamic dynamicAnnotation = entityClass
                    .getAnnotation(Dynamic.class);
            // 获取动态集合的字段名称
            extMapName = dynamicAnnotation.value();
        }

        Map<String, Field> classFields = ReflectUtil.getClassFields(entityClass);
        // 循环处理获得的字段
        for (Field field : classFields.values()) {
            processField(mapping, extMapName, field);
        }

        return mapping;
    }

    private void configShareState(Table tableDefine, EntityMaping entityMaping) {

        String shared = tableDefine.shared();

        if (shared == null || shared.isEmpty()) {
            entityMaping.setShared(false);
        } else {
            Boolean share = Boolean.valueOf(shared);
            entityMaping.setShared(share);
        }
    }

    private static void processField(EntityMaping mapping, String extMapName,
                                     Field field) {

        // 判断是否为扩展字段
        if (extMapName != null && field.getName().equals(extMapName)) {
            // 判断扩展字段类型不为map则抛出异常
            if (!field.getType().isAssignableFrom(Map.class)) {
                throw new IllegalArgumentException(
                        "Dynamic ext field must implement java.util.Map,please change it for "
                                + mapping.getEntityClass().getCanonicalName());
            }
            // 否则添加动态字段到mapping集合
            mapping.setDynamicField(field);
            return;
        }

        // 判断字段带有Column标记
        if (field.isAnnotationPresent(Column.class)) {
            // 通过Column标记获取对应的Column对象
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.columnName();
            String columnType = columnAnnotation.columnType();

            com.tbc.paas.mql.metadata.domain.Column column = new com.tbc.paas.mql.metadata.domain.Column();
            column.setColumnName(columnName);
            if (columnType == null || columnType.isEmpty()) {
                columnType = MdlUtil.getSqlTypeByField(field);
            }
            column.setColumnType(columnType);
            // 保存列和字段到mapping集合
            mapping.add(column, field);

            // 判断字段带有Id标记（主键）
            boolean monitor = columnAnnotation.isMonitor();
            if (monitor) {
                mapping.addMonitoredFiled(field.getName());
            }

            if (field.isAnnotationPresent(Id.class)) {
                // 获取主键
                Field primaryField = mapping.getPrimaryField();
                // 如果已经有Column为主键的字段存在 则抛出 多个 带有Id标记的异常
                if (primaryField != null) {
                    throw new MdlException(
                            "Only one column can be marked with Id annottion!");
                }

                // 保存主键字段
                mapping.setPrimaryField(field);
                // 保存主键列
                mapping.setPrimaryColumn(column);
            }
        }
    }

    /**
     * 当一个ApplicationContext被初始化或刷新触发
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 通过该事件获取ApplicationContext
        ApplicationContext applicationContext = event.getApplicationContext();
        // 处理Annotation式Beans
        processAnnotationBeans(applicationContext);
    }

    /**
     * 处理Annotation式Beans
     *
     * @param applicationContext sping applicationContext
     */
    private void processAnnotationBeans(ApplicationContext applicationContext) {
        // 获取带有Table标记的Annotation Bean
        Map<String, Object> beansWithAnnotation = applicationContext
                .getBeansWithAnnotation(Table.class);
        Collection<Object> values = beansWithAnnotation.values();
        // 循环添加对象及映射关系
        for (Object object : values) {
            Class<?> entityClass = object.getClass();
            addEntityClass(entityClass);
        }
    }
}
