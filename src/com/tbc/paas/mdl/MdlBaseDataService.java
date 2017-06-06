package com.tbc.paas.mdl;

import java.util.List;
import java.util.Map;

import com.tbc.paas.mdl.util.MdlBuilder;

public interface MdlBaseDataService {

	/**
	 * 为某个公司的单个实体提供对应地保存和更新. 以主键是否为空作为判断条件判断,<br>
	 * 如果主键为空, 则为插入操作,否则是更新. 这个方法<b>会</b>自动为插入操作生成主键.
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param entity
	 *            需要保存或者更新的实体.
	 * 
	 * @return 被操作实体的主键
	 */
	String saveOrUpdateEntity(String appCode, String corpCode, Object entity);

	/**
	 * 为某个公司的多个相同或者不同实体提供批量保存和更新. 以主键是否为空作为判断条件判断,<br>
	 * 如果主键为空, 则为插入操作,否则是更新. 这个方法<b>会</b>自动为插入操作生成主键.
	 * 
	 * @param appCode
	 *            应用的Id
	 * @param corpCode
	 *            公司的code
	 *
	 * @return 实体的Id
	 */
	List<String> saveOrUpdateEntity(String appCode, String corpCode,
			List<?> entityList);

	/**
	 * 该方法提供实体的保存操作.这个方法<b>不会</b>自动为插入操作生成主键.<br>
	 * 如果主键为空,会抛出异常!
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param entity
	 *            需要保存的实体.
	 */
	void insertEntity(String appCode, String corpCode, Object entity);

	/**
	 * 该方法提供实体的批量保存操作.这个方法<b>不会</b>自动为插入操作生成主键.<br>
	 * 如果主键为空,会抛出异常!
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param entities
	 *            需要保存的多个实体.
	 */
	void insertEntity(String appCode, String corpCode, List<?> entities);

	/**
	 * 该方法提供单个实体的更新操作.如果实体的主键为空,则会抛出异常.
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param entity
	 *            需要更新的的实体.
	 */
	void updateEntity(String appCode, String corpCode, Object entity);

	/**
	 * 该方法提供多个实体的更新操作.如果任意一个实体的主键为空,则会抛出异常.
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param entity
	 *            需要更新的的实体.
	 */
	void updateEntity(String appCode, String corpCode, List<?> entity);

	/**
	 * 根据主键删除某个公司应用对应的实体。
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param entityId
	 *            实体对应的ID
	 * @param entityClass
	 *            实体对应的类
	 * 
	 * @return 受影响的列数
	 */
	int deleteEntityById(String appCode, String corpCode, String entityId,
			Class<?> entityClass);

	/**
	 * 根据主键删除某个公司应用对应的多个实体。
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的code
	 * @param entityIdList
	 *            实体对应的主键列表
	 * @param entityClass
	 *            实体对应的类
	 * 
	 * @return 受影响的列数
	 */
	int deleteEntityByIds(String appCode, String corpCode,
			List<String> entityIdList, Class<?> entityClass);

	/**
	 * 根据实体的类型和实体的主键获取单个实体对象,如果没有获取结果,则返回空.。
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param entityId
	 *            实体对应的ID
	 * @param entityClass
	 *            实体对应的类
	 * @return 返回对应的实体或者null
	 */
	<T> T getEntityById(String appCode, String corpCode, String entityId,
			Class<T> entityClass);

	/**
	 * 更具mdlBuilder携带的查询条件,查取单一实体类型的一个或多个实体.<br>
	 * 如果没有任何实体,则返回一个不包含任何元素的空集合.
	 * 
	 * @param <T>
	 *            实际类的类型
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param mdlBuilder
	 *            查询的条件
	 * @return 满足条件的实体列表。
	 */
	<T> List<T> getEntityList(String appCode, String corpCode,
			MdlBuilder mdlBuilder);

	/**
	 * 根据查询条件获取一个实体,如果实际获取查询的实体为多个,则会返回第一个<br>
	 * 如果没有获得任何查询结果,则返回null.
	 * 
	 * @param <T>
	 *            实体类型
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @return 满足条件的实体。
	 */
	<T> T getUniqueEntity(String appCode, String corpCode, MdlBuilder mdlBuilder);

	/**
	 * 获取多实体的查询结果,每列的实体存储在一个Map中,Map的主键为实体的别名(如果设置了别名),<br>
	 * 或者是类的全名如果没有设置实体的别名.
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param mdlBuilder
	 *            查询的条件
	 * @return 满足条件的实体
	 */
	List<Map<String, Object>> getMutilEntity(String appCode, String corpCode,
			MdlBuilder mdlBuilder);

	/**
	 * 这个方法用于获取单一简单类型的数据库查询结果,例如<br>
	 * "SELECT name from User where id = :id".<br>
	 * 所返回的结果一定是数据库支持的基本字段.该方法的返回结果都是不封装的.
	 * 
	 * @param <T>
	 *            返回值的类型(该类型为数据库的基本类型)
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param mdlBuilder
	 *            查询的条件
	 * @return 查询的结果.
	 */
	<T> List<T> executeRowTypeQuery(String appCode, String corpCode,
			MdlBuilder mdlBuilder);

	/**
	 * 这个方法用于获取单一的数据库查询结果,例如 <br>
	 * "SELECT name from User where id = "id"".<br>
	 * 所返回的结果一定是数据库支持的基本字段.如果返回的结果多余一个,则返回一个,<br>
	 * 则抛出运行时异常.该方法的返回结果都是不封装的.
	 * 
	 * @param <T>
	 *            返回值的类型
	 * @param corpCode
	 *            公司的编码
	 * @param mdlBuilder
	 *            查询的条件
	 * @return 查询的结果.
	 */
	<T> T executeRowTypeUniqueQuery(String appCode, String corpCode,
			MdlBuilder mdlBuilder);

	/**
	 * 更具mdl builder携带的条件执行批量更新操作.
	 * 
	 * @param appCode
	 *            对应实体的应用编码
	 * @param corpCode
	 *            公司的code
	 * @param mdlBuilder
	 *            对应的mdl删除语句
	 * @return 受影响的列数
	 */
	int executeUpdate(String appCode, String corpCode, MdlBuilder mdlBuilder);

	/**
	 * 获取查询的结果,查询的结果按原始的二维数据的格式返回
	 * 
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param mdlBuilder
	 *            查询的条件
	 * @param encap
	 *            指示是否封装结果.(仅仅对查询时有效)
	 * @return 满足条件的实体
	 */
	List<List<Object>> executeQuery(String appCode, String corpCode,
			MdlBuilder mdlBuilder, boolean encap);

	/**
	 * 这个方法用于获取配置了对象关系时后,取得包含对象之间关联关系.
	 * 
	 * @param <T>
	 *            返回值的类型(该类型为数据库的基本类型)
	 * @param appCode
	 *            应用的编码
	 * @param corpCode
	 *            公司的编码
	 * @param mainKey
	 *            要返回的对象的别名,没有设置别名时是类的全名.
	 * @param mdlBuilder
	 *            查询语句.
	 * @return 或得的结果.
	 */
	public <T> List<T> getMutilEntityWithRelation(String appCode,
			String corpCode, String mainKey, MdlBuilder mdlBuilder);

	/**
	 * 这个方法用于通过共享中心获取数据<BR>
	 * 其他所有方法已经能智能的从共享中心获取数据，所以这个方法被废弃。
	 * 
	 * @param appCode
	 * @param corpCode
	 * @param mdlBuilder
	 * @param encap
	 * @return
	 */
	@Deprecated
	public <T> List<List<T>> executeGlobalQuery(String appCode,
			String corpCode, MdlBuilder mdlBuilder, boolean encap);

	/**
	 * 这个方法用于通过共享中心获取数据<BR>
	 * 其他所有方法已经能智能的从共享中心获取数据，所以这个方法被废弃。
	 * 
	 * @author ZHANG Nan
	 * @param appCode
	 * @param corpCode
	 * @param mdlBuilder
	 * @return
	 */
	@Deprecated
	public List<Map<String, Object>> executeGlobalQuery(String appCode,
			String corpCode, MdlBuilder mdlBuilder);

	//
	// public void batchCopyRelativeEntities(Class<?> entityClass,
	// List<String> entityIds, List<SqlTabRel> sqlTabRelList);

}
