package org.dborm.web;


import org.dborm.core.api.Dborm;

import java.util.List;

/**
 * Created by shk
 * 16/1/22 下午5:45
 */
public class PageManager {

    Dborm dborm;

    public <T> Page getPage(Page page, Class<T> entityClass, String querySql, List bindArgs) {
        initPageData(page, entityClass, querySql, bindArgs);
        initRecords(page, querySql, bindArgs);
        return page;
    }


    public <T> void initPageData(Page page, Class<T> entityClass, String querySql, List bindArgs) {
        StringBuilder sql = new StringBuilder();
        sql.append(querySql);
        sql.append(" LIMIT ");
        sql.append(page.getRows());
        sql.append(" OFFSET ");
        sql.append(page.getFirstRecord());
         List<T> dataRows = dborm.getEntities(entityClass, sql.toString(), bindArgs);
        page.setDataRows(dataRows);
    }


    public void initRecords(Page page, String querySql, List bindArgs) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ( ");
        sql.append(querySql);
        sql.append(") TEMP");
        long num = dborm.getCount(sql.toString(), bindArgs);
        page.setRecords(num);
    }


}
