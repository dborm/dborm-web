package cn.cocho.dborm.test.utils;

import cn.cocho.dborm.core.Dborm;
import cn.cocho.dborm.util.DbormContexts;

import static org.junit.Assert.assertEquals;

public class BaseTest {


    static {
        DbormContexts.log = new DBLogger();
        DbormContexts.showSql = true;
        try {
			cleanTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测BaseDomain里面的属性是否为空，如果为空则用当前用户的信息填充
     *
     * @param entity   实体类
     * @param isInsert 是添加
     * @return 补充信息之后的实体类
     * @author KEQIAO KEJI
     * @time 2013-5-6下午3:09:59
     */
    public <T> T fillBaseDomain(T entity, boolean isInsert) {
        /*
        Class<?> entityClass = entity.getClass();
		Map<String, Field> fields = getEntityColumnFields(entityClass);
		Set<Entry<String, Field>> entrySet = fields.entrySet();
		for (Entry<String, Field> entry : entrySet) {
			Field field = entry.getValue();
			if (DbormConstants.CORP_CODE.equals(entry.getKey())) {
				Object value = ReflectUtils.getFieldValue(field, entity);
				if (value == null && StringUtils.isNotEmpty(CommonContexts.getCorpCode())) {
					ReflectUtils.setFieldValue(field, entity, CommonContexts.getCorpCode());
				}
			}
			if (isInsert) {
				if (DbormConstants.CREATE_BY.equals(entry.getKey())) {
					Object value = ReflectUtils.getFieldValue(field, entity);
					if (value == null && StringUtils.isNotEmpty(CommonContexts.getUserId())) {
						ReflectUtils.setFieldValue(field, entity, CommonContexts.getUserId());
					}
				}
				if (DbormConstants.CREATE_TIME.equals(entry.getKey())) {
					Object value = ReflectUtils.getFieldValue(field, entity);
					if (value == null) {
						ReflectUtils.setFieldValue(field, entity, new Date());
					}
				}
			} else {
				if (DbormConstants.LAST_MODIFY_BY.equals(entry.getKey())) {
					Object value = ReflectUtils.getFieldValue(field, entity);
					if (value == null && StringUtils.isNotEmpty(CommonContexts.getUserId())) {
						ReflectUtils.setFieldValue(field, entity, CommonContexts.getUserId());
					}
				}
				if (DbormConstants.LAST_MODIFY_TIME.equals(entry.getKey())) {
					Object value = ReflectUtils.getFieldValue(field, entity);
					if (value == null) {
						ReflectUtils.setFieldValue(field, entity, new Date());
					}
				}
			}
		}
		*/
        return entity;
    }


    public static void cleanTable() {
		Dborm dborm = new Dborm(new DataBaseManager(), new DBLogger());

		boolean delLogin = dborm.execSql("delete from login_user");
        assertEquals(true, delLogin);
        boolean delOption = dborm.execSql("delete from qsm_option");
        assertEquals(true, delOption);
        boolean delInfo = dborm.execSql("delete from qsm_info");
        assertEquals(true, delInfo);
    }


}
