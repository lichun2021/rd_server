package com.hawk.game.module.lianmengfgyl.march.service.state;

import com.hawk.game.module.lianmengfgyl.march.data.FGYLActivityStateData;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLConst.FGYLActivityState;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarState;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarStateInfo;

public class FGYLState000Hidden  extends IFGYLServiceState {


	@Override
	public void init() {
		this.getDataManager().getStateData().setState(FGYLActivityState.HIDDEN);
		this.getDataManager().getStateData().saveRedis();
	}
	
	
	@Override
	public void tick() {
		FGYLActivityStateData curData = this.calcInfo();
		FGYLActivityStateData data = this.getDataManager().getStateData();
		//如果不在当前状态，则往下个状态推进
		if(curData.getTermId() != 0 &&  curData.getTermId() >data.getTermId() 
				&&FGYLActivityState.HIDDEN != curData.getState()){
			this.getService().updateState(FGYLActivityState.SHOW);
			return;
		}
	}
	
	
	@Override
	public void addBuilder(PBFGYLWarStateInfo.Builder builder, Player player) {
		FGYLActivityStateData stateData = this.getDataManager().getStateData();
		int termdId = stateData.getTermId();
		builder.setTermId(termdId);
		builder.setState(PBFGYLWarState.FGYL_HIDDEN);
	}

	
}
