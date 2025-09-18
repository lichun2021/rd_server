package com.hawk.activity.type.impl.commandAcademySimplify.rank;

import java.util.Comparator;

public class FinalRankSimplifyComparator implements Comparator<FinalRankSimplifyMember>{

	@Override
	public int compare(FinalRankSimplifyMember arg0, FinalRankSimplifyMember arg1) {
		
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
