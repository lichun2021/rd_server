package com.hawk.activity.type.impl.commandAcademy.rank;

import java.util.Comparator;

public class FinalRankComparator implements Comparator<FinalRankMember>{

	@Override
	public int compare(FinalRankMember arg0, FinalRankMember arg1) {
		
		//分数倒叙
		if(arg0.getScore().intValue() != arg1.getScore().intValue()){
			return arg0.getScore() > arg1.getScore()?-1:1 ;
		}
//		//阶段正序
//		if(arg0.getStage() != arg1.getStage()){
//			return arg0.getStage() > arg1.getStage()?1:-1 ;
//		}
		//阶段排名正序
		return arg0.getParam() > arg1.getParam()?1:-1;
	}

}
