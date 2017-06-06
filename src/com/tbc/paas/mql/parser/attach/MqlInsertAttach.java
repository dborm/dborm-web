package com.tbc.paas.mql.parser.attach;

import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.util.SqlBuilder;

/**
 * 使用这个接口可以为全局添加一个字段提供帮助.
 * 
 * @author Ztian
 * 
 */
public interface MqlInsertAttach {
	SqlBuilder getColumnAttach(MqlAnalyzer analyzer);

	SqlBuilder getValueAttach(MqlAnalyzer mqlAnalyzer);
}
