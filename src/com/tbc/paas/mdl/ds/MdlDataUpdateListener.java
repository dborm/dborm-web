package com.tbc.paas.mdl.ds;

import com.tbc.paas.mql.notify.MqlNotify;

import java.util.List;

public interface MdlDataUpdateListener {

	void updateNotify(List<MqlNotify> mqlTransUpdateNotify);

}
