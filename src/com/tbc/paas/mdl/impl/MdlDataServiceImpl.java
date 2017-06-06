package com.tbc.paas.mdl.impl;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.MdlDataService;
import com.tbc.paas.mdl.ds.MdlDataSource;
import com.tbc.paas.mdl.util.MdlBuilder;

@SuppressWarnings("deprecation")
public class MdlDataServiceImpl extends MdlCorpDataServiceImpl implements
		MdlDataService {

	public MdlDataServiceImpl() {
		super();
	}

	/**
	 * 保存或更新实体记录
	 */
	@Override
	public String saveOrUpdateEntity(Object entity) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();
		return saveOrUpdateEntity(appCode, corpCode, entity);
	}

	@Override
	public void insertEntity(Object entity) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		insertEntity(appCode, corpCode, entity);
	}

	@Override
	public void updateEntity(Object entity) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		updateEntity(appCode, corpCode, entity);
	}

	@Override
	public int deleteEntityById(String id, Class<?> entityClass) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return deleteEntityById(appCode, corpCode, id, entityClass);
	}

	@Override
	public int executeUpdate(MdlBuilder mqlBuilder) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return executeUpdate(appCode, corpCode, mqlBuilder);
	}

	@Override
	public <T> T getEntityById(String id, Class<T> entityClass) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return super.<T> getEntityById(appCode, corpCode, id, entityClass);
	}

	@Override
	public <T> List<T> getEntityList(MdlBuilder mdlBuilder) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return super.<T> getEntityList(appCode, corpCode, mdlBuilder);
	}

	@Override
	public List<List<Object>> executeQuery(MdlBuilder mdlBuilder, boolean encap) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return executeQuery(appCode, corpCode, mdlBuilder, encap);
	}

	@Override
	public int deleteEntityByIds(List<String> entityIdList, Class<?> entityClass) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return deleteEntityByIds(appCode, corpCode, entityIdList, entityClass);
	}

	@Override
	public List<String> saveOrUpdateEntity(List<?> entity) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return saveOrUpdateEntity(appCode, corpCode, entity);
	}

	@Override
	public <T> T getUniqueEntity(MdlBuilder mdlBuilder) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return super.<T> getUniqueEntity(appCode, corpCode, mdlBuilder);
	}

	@Override
	public <T> T executeRowTypeUniqueQuery(MdlBuilder mdlBuilder) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return super.<T> executeRowTypeUniqueQuery(appCode, corpCode,
				mdlBuilder);
	}

	@Override
	public <T> List<T> executeRowTypeQuery(MdlBuilder mdlBuilder) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return super.<T> executeRowTypeQuery(appCode, corpCode, mdlBuilder);
	}

	@Override
	public List<Map<String, Object>> getMutilEntity(MdlBuilder mdlBuilder) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return getMutilEntity(appCode, corpCode, mdlBuilder);
	}

	@Override
	public <T> List<T> getMutilEntityWithRelation(String mainKey,
			MdlBuilder mdlBuilder) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();

		return super.<T> getMutilEntityWithRelation(appCode, corpCode, mainKey,
				mdlBuilder);
	}

	/**
	 * 这个方法用于通过共享中心获取数据
	 * 
	 * @param appCode
	 * @param corpCode
	 * @param mdlBuilder
	 * @param encap
	 * @return
	 */
	@Override
	public List<List<Object>> executeGlobalQuery(MdlBuilder mdlBuilder,
			boolean encap) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();
		return executeGlobalQuery(appCode, corpCode, mdlBuilder, encap);
	}

	/**
	 * 这个方法用于通过共享中心获取数据
	 * 
	 * @author ZHANG Nan
	 * @param appCode
	 * @param corpCode
	 * @param mdlBuilder
	 * @return
	 */
	@Override
	public List<Map<String, Object>> executeGlobalQuery(MdlBuilder mdlBuilder) {
		String appCode = getAppCode();
		String corpCode = getCorpCode();
		return executeGlobalQuery(appCode, corpCode, mdlBuilder);
	}

	/**
	 * 获取corpCode
	 * 
	 * @return corpCode
	 */
	private String getCorpCode() {
		String corpCode = ExecutionContext.getCorpCode();

		String message = "Corp code is emtpy,Please make sure "
				+ "your ExecutionContext have been setted!";
		// 如果获取的corpCode为空则抛出异常
		Assert.hasText(corpCode, message);

		return corpCode;
	}

	/**
	 * 创建指定公司应用的数据服务.
	 * 
	 * @param appCode
	 * @param corpCode
	 * @return
	 */
	@Override
	public MdlDataService createDataService(String appCode, String corpCode) {
		DataSource dataSource = getDataSource();
		if (!(dataSource instanceof MdlDataSource)) {
			return this;
		}

		MdlDataServiceImpl mServiceImpl = new MdlDataServiceImpl();
		JdbcTemplate jdbcTemplate = createJdbcTemplate(appCode, corpCode);
		mServiceImpl.setJdbcTemplate(jdbcTemplate);

		mServiceImpl.configure = configure;
		mServiceImpl.metadataService = metadataService;
		mServiceImpl.mdmJedisCommands = mdmJedisCommands;
		mServiceImpl.mqlPkCallback = mqlPkCallback;
		mServiceImpl.tableAppCode = tableAppCode;

		return mServiceImpl;
	}
}
