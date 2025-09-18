package com.hawk.game.service.tiberium.comparetor;

import java.util.Comparator;

import com.hawk.game.service.tiberium.TLWGuildJoinInfo;

public class TLWGuildJoinInfoSeanRankComparator implements Comparator<TLWGuildJoinInfo>{

	@Override
	public int compare(TLWGuildJoinInfo arg0, TLWGuildJoinInfo arg1) {
		int gap = 0;
		if (arg0.initGroup.getRankOrder() != arg1.initGroup.getRankOrder()) {
			// 按淘汰赛组别由高到低
			gap = arg0.initGroup.getRankOrder() - arg1.initGroup.getRankOrder();
		} else if (arg0.kickOutTerm != arg1.getKickOutTerm()) {
			// 按淘汰轮次由高到低
			gap = arg0.kickOutTerm - arg1.getKickOutTerm();
		} else if (arg0.score != arg1.score) {
			// 按总积分由高到低
			gap = arg0.score > arg1.score ? 1 : -1;
		} else if (arg0.lastestPower != arg1.lastestPower) {
			// 按被淘汰时的出战战力由低到高
			gap = arg0.lastestPower > arg1.lastestPower ? -1 : 1;
		} else if (arg0.initPower != arg1.initPower) {
			// 按入围时出战战力由高到低
			gap = arg0.initPower > arg1.initPower ? 1 : -1;
		} else if (arg0.createTime != arg1.createTime) {
			// 按入联盟创建时间
			gap = arg0.createTime > arg1.createTime ? 1 : -1;
		} else {
			return 0;
		}
		return -gap;
	}

	

}
