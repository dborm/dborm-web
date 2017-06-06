package com.tbc.paas.mdl.impl;

import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.MdlCorpDataService;
import com.tbc.paas.mdl.util.MdlBuilder;

@Deprecated
public class MdlCorpDataServiceImpl extends MdlBasicDataServiceImpl implements MdlCorpDataService{

	public MdlCorpDataServiceImpl() {
		super();
	}
	
	/**
	 * 保存或更新实体记录
	 */
	@Override
	public String saveOrUpdateEntity(String corpCode,Object entity) {
		String appCode = getAppCode();
		return saveOrUpdateEntity(appCode, corpCode, entity);
	}

	@Override
	public void insertEntity(String corpCode,Object entity) {
		String appCode = getAppCode();

		insertEntity(appCode, corpCode, entity);
	}

	@Override
	public void updateEntity(String corpCode,Object entity) {
		String appCode = getAppCode();

		updateEntity(appCode, corpCode, entity);
	}

	@Override
	public int deleteEntityById(String corpCode,String id, Class<?> entityClass) {
		String appCode = getAppCode();

		return deleteEntityById(appCode, corpCode, id, entityClass);
	}

	@Override
	public int executeUpdate(String corpCode,MdlBuilder mqlBuilder) {
		String appCode = getAppCode();

		return executeUpdate(appCode, corpCode, mqlBuilder);
	}

	@Override
	public <T> T getEntityById(String corpCode,String id, Class<T> entityClass) {
		String appCode = getAppCode();

		return super.<T>getEntityById(appCode, corpCode, id, entityClass);
	}

	@Override
	public <T> List<T> getEntityList(String corpCode,MdlBuilder mdlBuilder) {
		String appCode = getAppCode();

		return super.<T>getEntityList(appCode, corpCode, mdlBuilder);
	}

	@Override
	public List<List<Object>> executeQuery(String corpCode,MdlBuilder mdlBuilder, boolean encap) {
		String appCode = getAppCode();

		return executeQuery(appCode, corpCode, mdlBuilder, encap);
	}

	@Override
	public int deleteEntityByIds(String corpCode,List<String> entityIdList, Class<?> entityClass) {
		String appCode = getAppCode();

		return deleteEntityByIds(appCode, corpCode, entityIdList, entityClass);
	}

	@Override
	public List<String> saveOrUpdateEntity(String corpCode,List<?> entity) {
		String appCode = getAppCode();

		return saveOrUpdateEntity(appCode, corpCode, entity);
	}

	@Override
	public <T> T getUniqueEntity(String corpCode,MdlBuilder mdlBuilder) {
		String appCode = getAppCode();

		return super.<T>getUniqueEntity(appCode, corpCode, mdlBuilder);
	}

	@Override
	public <T> T executeRowTypeUniqueQuery(String corpCode,MdlBuilder mdlBuilder) {
		String appCode = getAppCode();

		return super.<T>executeRowTypeUniqueQuery(appCode, corpCode, mdlBuilder);
	}

	@Override
	public <T> List<T> executeRowTypeQuery(String corpCode,MdlBuilder mdlBuilder) {
		String appCode = getAppCode();

		return super.<T>executeRowTypeQuery(appCode, corpCode, mdlBuilder);
	}

	@Override
	public List<Map<String, Object>> getMutilEntity(String corpCode,MdlBuilder mdlBuilder) {
		String appCode = getAppCode();

		return getMutilEntity(appCode, corpCode, mdlBuilder);
	}

	@Override
	public <T> List<T> getMutilEntityWithRelation(String corpCode,String mainKey,MdlBuilder mdlBuilder) {
		String appCode = getAppCode();

		return super.<T>getMutilEntityWithRelation(appCode, corpCode, mainKey,mdlBuilder);
	}
	
	/**
	 * 获取AppCode
	 * @return AppCode
	 */
	protected String getAppCode() {
		String appCode = ExecutionContext.getAppCode();

		String message = "App code is emtpy,Please make sure "
				+ "your ExecutionContext have been setted!";
		//如果获取的AppCode为空则抛出异常
		Assert.hasText(appCode, message);

		return appCode;
	}
}
