package com.tbc.paas.mdl;

import java.util.List;
import java.util.Map;

import com.tbc.paas.mdl.util.MdlBuilder;

@SuppressWarnings("deprecation")
public interface MdlDataService extends MdlCorpDataService {

	/**
	 * 
	 * @param entity
	 */
	void insertEntity(Object entity);

	/**
	 * 
	 * @param entity
	 */
	void updateEntity(Object entity);

	/**
	 * 使用默认的AppCode为某个公司的实体提供对应得保存和更新服务。<br>
	 * 以主键判断,如果主键为空,则为插入 否则是更新.
	 * 
	 * @param entity
	 *            需要保存的实体
	 * 
	 * @return 实体的Id
	 */
	String saveOrUpdateEntity(Object entity);

	/**
	 * 使用默认的AppCode为某个公司的一些列实体提供对应得保存和更新服务。<br>
	 * 以主键判断,如果主键为空,则为插入 否则是更新.
	 * 
	 * @param entity
	 *            需要保存的实体
	 * 
	 * @return 实体的Id
	 */
	List<String> saveOrUpdateEntity(List<?> entity);

	/**
	 * 使用默认的AppCode删除某个公司应用的实体。
	 * 
	 * @param entityId
	 *            实体对应的ID
	 * @param entityClass
	 *            实体对应的类
	 * 
	 * @return 受影响的列数
	 */
	int deleteEntityById(String entityId, Class<?> entityClass);

	/**
	 * 使用默认的AppCode删除某个公司应用的实体。
	 * 
	 * @param entityClass
	 *            实体对应的类
	 * 
	 * @return 受影响的列数
	 */
	int deleteEntityByIds(List<String> entityIdList, Class<?> entityClass);

	/**
	 * 使用默认的AppCode删除某个公司应用的实体。
	 * 
	 * @return 受影响的列数
	 */
	int executeUpdate(MdlBuilder mdlBuilder);

	/**
	 * 使用默认的appCode获得某个ID对应的实体。
     *
	 * @param entityId
	 *            实体对应的ID
	 * @param entityClass
	 *            实体对应的类
	 * @return 返回对应的实体或者null
	 */
	<T> T getEntityById(String entityId, Class<T> entityClass);

	/**
	 * 通过根据实体属性定义的条件来获取一系列满足条件的对象
	 * 
	 * @param <T>
	 *            实际类的类型
	 * @param mdlBuilder
	 *            查询的条件
	 * @return 满足条件的实体。
	 */
	<T> List<T> getEntityList(MdlBuilder mdlBuilder);

	/**
	 * 不带参数的实体查询,返回的结果有且仅有一个符合条件的实体..
	 * 
	 * @param <T>
	 *            实体类型
	 * @return 满足条件的实体。
	 */
	<T> T getUniqueEntity(MdlBuilder mdlBuilder);

	/**
	 * 获取多实体的查询结果,每列的实体存储在一个Map中,Map的主键为Entity.class
	 * 
	 * @param mdlBuilder
	 *            查询的条件
	 * @return 满足条件的实体
	 */
	List<Map<String, Object>> getMutilEntity(MdlBuilder mdlBuilder);

	/**
	 * 使用默认的appCode获取查询的结果,查询的结果按原始的二维数据的格式返回
	 * 
	 * @param mdlBuilder
	 *            查询的条件
	 * @param encap
	 *            指示是否封装结果.(仅仅对查询时有效)
	 * @return 满足条件的实体
	 */
	List<List<Object>> executeQuery(MdlBuilder mdlBuilder, boolean encap);

	/**
	 * 这个方法用于获取单一的数据库查询结果,例如 <br>
	 * "SELECT name from User where id = "id"".<br>
	 *
	 * @param <T>
	 *            返回值的类型
	 * @param mdlBuilder
	 *            查询的条件
	 * @return 查询的结果.
	 */
	<T> T executeRowTypeUniqueQuery(MdlBuilder mdlBuilder);

	/**
	 * 这个方法用于获取单一的数据库查询结果,例如<br>
	 * "SELECT name from User where id = :id".<br>
	 * 所返回的结果一定是数据库支持的基本字段.该方法的返回结果都是不封装的.
	 * 
	 * @param <T>
	 *            返回值的类型(该类型为数据库的基本类型)
	 * @param mdlBuilder
	 *            查询的条件
	 * @return 查询的结果.
	 */
	<T> List<T> executeRowTypeQuery(MdlBuilder mdlBuilder);

	/**
	 * 这个方法用于获取配置了对象关系时后,取得包含对象之间关联关系.
	 * 
	 * @param <T>
	 *            返回值的类型(该类型为数据库的基本类型)
	 * @param mainKey
	 *            要返回的对象的别名,没有设置别名时是类的全名.
	 * @param mdlBuilder
	 *            查询语句.
	 * @return 或得的结果.
	 */
	public <T> List<T> getMutilEntityWithRelation(String mainKey,
			MdlBuilder mdlBuilder);

	/**
	 * 这个方法用于通过共享中心获取数据
	 * 
	 * @param mdlBuilder
	 * @param encap
	 * @return
	 */
	public <T> List<List<T>> executeGlobalQuery(MdlBuilder mdlBuilder,
			boolean encap);

	/**
	 * 这个方法用于通过共享中心获取数据
	 * 
	 * @author ZHANG Nan
	 * @param mdlBuilder
	 * @return
	 */
	@Deprecated
	public List<Map<String, Object>> executeGlobalQuery(MdlBuilder mdlBuilder);

	/**
	 * 
	 * @param appCode
	 * @param corpCode
	 * @return
	 */
	@Deprecated
	MdlDataService createDataService(String appCode, String corpCode);
}
