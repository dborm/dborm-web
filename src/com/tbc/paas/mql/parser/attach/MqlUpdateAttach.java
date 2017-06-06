package com.tbc.paas.mql.parser.attach;

import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.util.SqlBuilder;

/**
 * 使用这个几口可以动态为Insert添加一些通用字段.
 * 
 * @author Ztian
 * 
 */
public interface MqlUpdateAttach {
	SqlBuilder getColumnAttachPart(MqlAnalyzer mqlAnalyzer);
}
