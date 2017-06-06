package com.tbc.paas.mql.parser.attach;

import com.tbc.paas.mql.parser.MqlSelectParser;
import com.tbc.paas.mql.util.SqlBuilder;

/**
 * 运行时为Sql的Column添加一些字段.
 * 
 * @author Ztian
 * 
 */
public interface MqlSelectAttach {
	/**
	 * 获取要在运行时为Sql添加的额外操作.
	 * 
	 * @param analyzer
	 *            sql的分析器
	 * @return 要添加的Sql
	 */
	SqlBuilder getColumnAttach(MqlSelectParser analyzer);
}
