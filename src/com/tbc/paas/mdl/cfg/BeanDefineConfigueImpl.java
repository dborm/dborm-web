package com.tbc.paas.mdl.cfg;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.tbc.paas.mdl.util.ReflectUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.tbc.paas.mdl.cfg.domain.EntityDefine;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.cfg.domain.EntityRelation;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mdl.util.MdlUtil;
import com.tbc.paas.mql.metadata.domain.Column;

public class BeanDefineConfigueImpl extends AnnotationConfigureImpl {

    public BeanDefineConfigueImpl() {
        super();
    }

    /**
     * 当一个ApplicationContext被初始化或刷新触发
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 调用超类onApplicationEvent方法处理事件
        super.onApplicationEvent(event);

        ApplicationContext applicationContext = event.getApplicationContext();
        processBasicDefineBeans(applicationContext);
        processEntityRelationDefine(applicationContext);

    }

    private void processEntityRelationDefine(
            ApplicationContext applicationContext) {
        Map<String, EntityDefine> entityDefineBeans = applicationContext
                .getBeansOfType(EntityDefine.class);

        Collection<EntityDefine> entityDefines = entityDefineBeans.values();
        for (EntityDefine entityDefine : entityDefines) {
            String entityClassName = entityDefine.getEntityClassName();
            EntityMaping entityMapping = getEntityMappingByClassName(entityClassName);
            processEntityRelation(entityDefine, entityMapping);
        }
    }

    private void processBasicDefineBeans(ApplicationContext applicationContext) {
        Map<String, EntityDefine> entityDefineBeans = applicationContext
                .getBeansOfType(EntityDefine.class);

        Collection<EntityDefine> entityDefines = entityDefineBeans.values();
        for (EntityDefine entityDefine : entityDefines) {
            EntityMaping entityMaping = new EntityMaping();
            String tableName = entityDefine.getTableName();
            entityMaping.setTableName(tableName);

            String entityClassName = entityDefine.getEntityClassName();
            Class<?> entityClass = getEntityClassByName(entityClassName);
            entityMaping.setEntityClass(entityClass);

            Map<String, Field> classFields = ReflectUtil.getClassFields(entityClass);

            String pkEntityProperty = entityDefine.getPkPropery();
            Field primaryField = classFields.get(pkEntityProperty);
            entityMaping.setPrimaryField(primaryField);

            configShareState(entityDefine, entityMaping);

            String extProperty = entityDefine.getExtPropery();
            if (extProperty != null) {
                Field extField = classFields.get(extProperty);
                entityMaping.setDynamicField(extField);
            }

            processProperties(entityDefine, entityMaping, entityClassName,
                    classFields, pkEntityProperty);
            addEntityMapping(entityMaping);
        }
    }

    private void configShareState(EntityDefine entityDefine,
                                  EntityMaping entityMaping) {
        Boolean shared = entityDefine.getShared();
        if (shared == null) {
            shared = false;
        }

        entityMaping.setShared(shared);
    }

    private void processEntityRelation(EntityDefine entityDefine,
                                       EntityMaping entityMaping) {
        Properties complexProperties = entityDefine.getComplexProperties();
        if (complexProperties == null) {
            return;
        }

        Class<?> entityClass = entityMaping.getEntityClass();
        Map<String, Field> classFields = ReflectUtil.getClassFields(entityClass);
        String entityClassName = entityClass.getName();
        Set<Entry<Object, Object>> complexPropertySet = complexProperties
                .entrySet();
        for (Entry<Object, Object> entry : complexPropertySet) {
            String fieldName = entry.getKey().toString();
            EntityRelation entityRelation = new EntityRelation();
            Field entityField = classFields.get(fieldName);
            entityRelation.setEntityField(entityField);

            String entityRelationInfo = entry.getValue().toString();
            StringTokenizer tokenizer = new StringTokenizer(entityRelationInfo,
                    ",");
            int countTokens = tokenizer.countTokens();
            if (countTokens < 4) {
                throw new MdlException("Please make sure you can configure "
                        + entityClassName
                        + " in currect way for complex propery " + fieldName);
            }

            entityRelation.setFromTableName(entityClassName);
            String relationShip = tokenizer.nextToken();
            entityRelation.setRelationship(relationShip);
            String toEntity = tokenizer.nextToken();
            String toEntityClassName = getFullEntityClassName(toEntity);
            entityRelation.setToTableName(toEntityClassName);
            String fromPropertyName = tokenizer.nextToken();
            entityRelation.setFromColumnName(fromPropertyName);
            String toPropertyName = tokenizer.nextToken();
            entityRelation.setToColumnName(toPropertyName);
            entityMaping.addEntityRelation(entityRelation);
        }
    }

    private void processProperties(EntityDefine entityDefine,
                                   EntityMaping entityMaping, String entityClassName,
                                   Map<String, Field> fieldMap, String pkEntityPropery) {
        Properties propertyMap = entityDefine.getProperties();
        if (propertyMap == null) {
            return;
        }
        Set<Entry<Object, Object>> entrySet = propertyMap.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            String fieldName = entry.getKey().toString();
            String columnInfo = entry.getValue().toString();

            StringTokenizer tokenizer = new StringTokenizer(columnInfo, ",");
            if (tokenizer.countTokens() < 1) {
                throw new MdlException("Please make sure you  configure "
                        + entityClassName + " in correct way for propery ["
                        + fieldName + "]");
            }

            Field field = fieldMap.get(fieldName);
            Column column = new Column();
            String columnName = tokenizer.nextToken();
            column.setColumnName(columnName);

            String type = getColumnType(tokenizer, field);
            column.setColumnType(type);
            if (tokenizer.hasMoreTokens()) {
                String monitor = tokenizer.nextToken();
                if (monitor.equalsIgnoreCase("m")) {
                    entityMaping.addMonitoredFiled(fieldName);
                }
            }

            entityMaping.add(column, field);
            if (pkEntityPropery.equals(fieldName)) {
                entityMaping.setPrimaryColumn(column);
            }
        }
    }

    private String getColumnType(StringTokenizer tokenizer, Field columnField) {

        if (tokenizer.hasMoreTokens()) {
            return tokenizer.nextToken();
        }

        return MdlUtil.getSqlTypeByField(columnField);
    }

    private Class<?> getEntityClassByName(String entityClassName) {
        Class<?> entityClass;
        try {
            entityClass = Class.forName(entityClassName);
        } catch (ClassNotFoundException e) {
            throw new MdlException("Can't find class from name "
                    + entityClassName);
        }

        return entityClass;
    }
}
