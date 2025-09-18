package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.state;

import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.DYZZEnergyWell;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.service.chat.ChatParames;

public class DYZZEnergyWellStateZhongLi extends IDYZZEnergyWellState {

	public DYZZEnergyWellStateZhongLi(DYZZEnergyWell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	
	
	@Override
	public void init() {
		super.init();
		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.DYZZ_337)
				.addParms(getParent().getX())
				.addParms(getParent().getY())
				.build();
		getParent().getParent().addWorldBroadcastMsg(parames);
	}


	@Override
	public boolean onTick() {
		IDYZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if (leaderMarch == null) {
			return true;
		}

		getParent().setStateObj(new DYZZEnergyWellStateZhanLingZhong(getParent()));

		return true;
	}

	@Override
	public DYZZBuildState getState() {
		return DYZZBuildState.ZHONG_LI;
	}

}
