package com.tbc.paas.mdl.mql;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import com.tbc.paas.mdl.attach.MqlInsertAttachImpl;
import com.tbc.paas.mdl.attach.MqlSelectAttachImpl;
import com.tbc.paas.mdl.attach.MqlUpdateAttachImpl;
import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.util.MdlBuilder;
import com.tbc.paas.mql.domain.MqlParseException;
import com.tbc.paas.mql.domain.SqlMetadata;
import com.tbc.paas.mql.domain.SqlNode;
import com.tbc.paas.mql.grammar.ParseException;
import com.tbc.paas.mql.grammar.SqlGrammar;
import com.tbc.paas.mql.grammar.SqlGrammarTreeConstants;
import com.tbc.paas.mql.metadata.MqlMetadataService;
import com.tbc.paas.mql.parser.MqlDeleteParser;
import com.tbc.paas.mql.parser.MqlInsertParser;
import com.tbc.paas.mql.parser.MqlParser;
import com.tbc.paas.mql.parser.MqlSelectParser;
import com.tbc.paas.mql.parser.MqlUpdateParser;
import com.tbc.paas.mql.parser.attach.MqlInsertAttach;
import com.tbc.paas.mql.parser.attach.MqlSelectAttach;
import com.tbc.paas.mql.parser.attach.MqlUpdateAttach;
import com.tbc.paas.mql.util.MqlOperation;
import com.tbc.paas.mql.util.MqlUtil;

public class MdlParserFactory {

	public static MqlParser getParser(String appCode, String corpCode,
			MdlBuilder mdlBuilder, MqlMetadataService metadataService,
			Configure configure) {

		String sql = mdlBuilder.getSql();
		SqlGrammar sqlGrammar = getSqlGrammar(sql);
		SqlMetadata sqlMetadata = sqlGrammar.getSqlMetadata();
		MqlParser sqlParser = getMqlParser(sqlGrammar, mdlBuilder);
		MdlAnalyzer mdlAnalyzer = getMdlAnalyzer(appCode, corpCode,
				metadataService, sqlMetadata, sqlParser);

		mdlAnalyzer.setConfigure(configure);
		sqlParser.setMqlAnalyzer(mdlAnalyzer);

		return sqlParser;
	}

	private static MdlAnalyzer getMdlAnalyzer(String appCode, String corpCode,
			MqlMetadataService metadataService, SqlMetadata sqlMetadata,
			MqlParser sqlParser) {
		MdlAnalyzer mdlAnalyzer = new MdlAnalyzer(appCode, corpCode,
				sqlMetadata, metadataService);
		if (sqlParser instanceof MqlSelectParser) {
			mdlAnalyzer.setOperation(MqlOperation.SELECT);
		} else if (sqlParser instanceof MqlUpdateParser) {
			mdlAnalyzer.setOperation(MqlOperation.UPDATE);
		} else if (sqlParser instanceof MqlInsertParser) {
			mdlAnalyzer.setOperation(MqlOperation.INSERT);
		} else {
			mdlAnalyzer.setOperation(MqlOperation.DELETE);
		}

		return mdlAnalyzer;
	}

	private static MqlParser getMqlParser(SqlGrammar sqlGrammar,
			MdlBuilder mdlBuilder) {
		Map<String, Object> parameterMap = mdlBuilder.getParameterMap();
		List<Object> parameterList = mdlBuilder.getParameterList();
		SqlNode rootNode = sqlGrammar.getRootNode();
		int id = rootNode.getId();
		switch (id) {
		case SqlGrammarTreeConstants.JJTSELECT:
			MqlSelectParser mqlSelectParser = new MqlSelectParser(sqlGrammar,
					parameterList, parameterMap);
			MqlSelectAttach selectAttach = new MqlSelectAttachImpl();
			mqlSelectParser.setSelectAttach(selectAttach);
			return mqlSelectParser;
		case SqlGrammarTreeConstants.JJTUPDATE:
			MqlUpdateParser mqlUpdateParser = new MqlUpdateParser(sqlGrammar,
					parameterList, parameterMap);
			MqlUpdateAttach updateAttach = new MqlUpdateAttachImpl();
			mqlUpdateParser.setMqlUpdateAttach(updateAttach);
			return mqlUpdateParser;
		case SqlGrammarTreeConstants.JJTINSERT:
			MqlInsertParser mqlInsertParser = new MqlInsertParser(sqlGrammar,
					parameterList, parameterMap);
			MqlInsertAttach insertAttach = new MqlInsertAttachImpl();
			mqlInsertParser.setInsertAttach(insertAttach);
			return mqlInsertParser;
		case SqlGrammarTreeConstants.JJTDELETE:
			MqlDeleteParser mqlDeleteParser = new MqlDeleteParser(sqlGrammar,
					parameterList, parameterMap);
			return mqlDeleteParser;
		default:
			throw new MqlParseException("Doesn't support sql ["
					+ SqlGrammarTreeConstants.jjtNodeName[id] + "]");
		}
	}

	private static SqlGrammar getSqlGrammar(String sql) {
		StringReader reader = new StringReader(sql);
		SqlGrammar sqlGrammar = new SqlGrammar(reader);

		try {
			sqlGrammar.analyze();
		} catch (ParseException e) {
			throw MqlUtil.convert(sql, e);
		}

		return sqlGrammar;
	}
}
