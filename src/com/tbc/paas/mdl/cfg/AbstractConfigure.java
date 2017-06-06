package com.tbc.paas.mdl.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.cfg.domain.EntityRelation;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mql.util.SqlConstants;

public abstract class AbstractConfigure implements Configure {

	protected List<EntityMaping> entityMappingList;
	protected Map<String, EntityMaping> tableNameEntityMap;
	protected Map<String, EntityMaping> classNameEntityMap;
	protected Map<String, Integer> classSimpleNameCountMap;
	protected Map<String, String> classSimpleNameFullNameMap;

	public AbstractConfigure() {
		super();
		this.entityMappingList = new ArrayList<EntityMaping>();
		this.classNameEntityMap = new HashMap<String, EntityMaping>();
		this.tableNameEntityMap = new HashMap<String, EntityMaping>();
		this.classSimpleNameCountMap = new HashMap<String, Integer>();
		this.classSimpleNameFullNameMap = new HashMap<String, String>();
	}

	@Override
	public void addEntityMapping(EntityMaping entityMapping) {
		String className = entityMapping.getEntityClassName();
		String simpleClassName = entityMapping.getEntitySimpleClassName();
		Integer simpleNameCount = classSimpleNameCountMap.get(simpleClassName);
		if (simpleNameCount == null) {
			simpleNameCount = 0;
		}
		simpleNameCount++;
		classSimpleNameCountMap.put(simpleClassName, simpleNameCount);
		classSimpleNameFullNameMap.put(simpleClassName, className);

		String tableName = entityMapping.getTableName();
		entityMappingList.add(entityMapping);
		classNameEntityMap.put(className, entityMapping);
		tableNameEntityMap.put(tableName, entityMapping);
	}

	/**
	 * 通过实体类名称获取对应的映射对象
	 */
	@Override
	public EntityMaping getEntityMappingByClassName(String entityClassName) {
		// 获取全路径实体类名
		String fullEntityClassName = getFullEntityClassName(entityClassName);
		// 通过全路径实体类名称获取对应的实体映射对象
		return classNameEntityMap.get(fullEntityClassName);
	}

	/**
	 * 通过实体类名获取全路径实体类名
	 * 
	 * @param entityClassName
	 *            实体类名
	 * @return 全路径实体类名
	 */
	protected String getFullEntityClassName(String entityClassName) {
		// 判断不包含 “.” 点
		if (entityClassName.indexOf(SqlConstants.DOT) == -1) {
			// 从实体类名集合中获取该类名总数
			Integer count = classSimpleNameCountMap.get(entityClassName);
			if (count == null || count == 1) {
				// 从全路径实体类名中获取类的全路径名
				entityClassName = classSimpleNameFullNameMap
						.get(entityClassName);
			} else {
				// 有多个实体类名称异常
				throw new MdlException(
						"More than one entity class's simple  name called "
								+ entityClassName);
			}
		}
		return entityClassName;
	}

	@Override
	public EntityMaping getEntityMapingByTableName(String tableName) {
		return tableNameEntityMap.get(tableName);
	}

	@Override
	public List<EntityMaping> getEntityMappingList() {
		return entityMappingList;
	}

	public void clear() {
		this.entityMappingList.clear();
		this.classNameEntityMap.clear();
		this.tableNameEntityMap.clear();
	}

	@Override
	public String getTableNameByEntityClassName(String entityClassName) {
		String fullEntityClassName = getFullEntityClassName(entityClassName);
		EntityMaping entityMaping = classNameEntityMap.get(fullEntityClassName);
		return entityMaping.getTableName();
	}

	@Override
	public String getEntityNameByTableName(String tableName) {
		EntityMaping entityMaping = tableNameEntityMap.get(tableName);
		String entitySimpleClassName = entityMaping.getEntitySimpleClassName();
		Integer count = classSimpleNameCountMap.get(entitySimpleClassName);
		if (count <= 1) {
			return entitySimpleClassName;
		}

		return entityMaping.getEntityClassName();
	}

	@Override
	public EntityRelation getEntityRelation(String fromEntityName,
			String toEntityName) {
		EntityMaping entityMapping = getEntityMappingByClassName(fromEntityName);
		String toEntityClassName = getFullEntityClassName(toEntityName);
		return entityMapping.getEntityRelation(toEntityClassName);
	}
}
