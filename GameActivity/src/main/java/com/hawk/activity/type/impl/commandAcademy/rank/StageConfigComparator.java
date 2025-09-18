package com.hawk.activity.type.impl.commandAcademy.rank;

import java.util.Comparator;

import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyStageCfg;

public class StageConfigComparator implements Comparator<CommandAcademyStageCfg>{

	@Override
	public int compare(CommandAcademyStageCfg arg0, CommandAcademyStageCfg arg1) {
		//orderId正叙
		return arg0.getOrder() > arg1.getOrder()?1:-1;
	}

}
