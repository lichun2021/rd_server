package com.hawk.game.service.tiberium.comparetor;

import java.util.Comparator;

import com.hawk.game.service.tiberium.TLWGuildJoinInfo;

public class TLWGuildJoinInfoTeamGroupComparator implements Comparator<TLWGuildJoinInfo>{

	@Override
	public int compare(TLWGuildJoinInfo arg0, TLWGuildJoinInfo arg1) {
		if (arg0.getWinCnt() != arg1.getWinCnt()) {
			return arg1.getWinCnt() - arg0.getWinCnt();
		} else if (arg0.getScore() != arg1.getScore()) {
			return arg0.getScore() > arg1.getScore() ? -1 : 1;
		} else if (arg0.getInitPower() != arg1.getInitPower()) {
			return arg0.getInitPower() > arg1.getInitPower() ? -1 : 1;
		}  else if (arg0.getCreateTime() != arg1.getCreateTime()) {
			return arg0.getCreateTime() > arg1.getCreateTime() ? -1 : 1;
		} else {
			return 0;
		}
	}

	

}
