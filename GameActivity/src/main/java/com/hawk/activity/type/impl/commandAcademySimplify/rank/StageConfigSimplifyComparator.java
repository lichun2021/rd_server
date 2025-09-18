package com.hawk.activity.type.impl.commandAcademySimplify.rank;

import java.util.Comparator;

import com.hawk.activity.type.impl.commandAcademySimplify.cfg.CommandAcademySimplifyStageCfg;


public class StageConfigSimplifyComparator implements Comparator<CommandAcademySimplifyStageCfg>{

	@Override
	public int compare(CommandAcademySimplifyStageCfg arg0, CommandAcademySimplifyStageCfg arg1) {
		//orderId正叙
		return arg0.getOrder() > arg1.getOrder()?1:-1;
	}

}
