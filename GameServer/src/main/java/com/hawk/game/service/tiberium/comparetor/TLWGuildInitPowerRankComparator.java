package com.hawk.game.service.tiberium.comparetor;

import java.util.Comparator;

import com.hawk.game.service.tiberium.TLWGuildJoinInfo;

public class TLWGuildInitPowerRankComparator implements Comparator<TLWGuildJoinInfo>{

	@Override
	public int compare(TLWGuildJoinInfo arg0, TLWGuildJoinInfo arg1) {
		int gap = 0;
		if (arg0.initPower != arg1.initPower) {
			// 按入围时出战战力由高到低
			gap = arg0.initPower > arg1.initPower ? 1 : -1;
		} else if (arg0.createTime != arg1.createTime) {
			// 按入联盟创建时间
			gap = arg0.createTime > arg1.createTime ? 1 : -1;
		} else {
			return 0;
		}
		return  -gap;
	}

}
