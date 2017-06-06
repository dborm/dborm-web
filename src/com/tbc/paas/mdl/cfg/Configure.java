package com.tbc.paas.mdl.cfg;

import java.util.List;

import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.cfg.domain.EntityRelation;

/**
 * 定义配置必须的及接口类.他更像是一个Domain,或者是一个配置的缓存类.
 * 
 * @author Ztian
 * 
 */
public interface Configure {
	
	EntityRelation getEntityRelation(String fromEntityName,String toEntityName);

	String getTableNameByEntityClassName(String entityName);

	String getEntityNameByTableName(String tableName);

	void addEntityMapping(EntityMaping entityMapping);

	EntityMaping getEntityMappingByClassName(String entityClassName);

	EntityMaping getEntityMapingByTableName(String tableName);

	List<EntityMaping> getEntityMappingList();
}