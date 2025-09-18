package com.hawk.game.module.lianmengfgyl.march.service.state;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLTimeCfg;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLActivityStateData;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLConst.FGYLActivityState;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarState;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarStateInfo;


public class FGYLState100Show  extends IFGYLServiceState {
	
	
	
	@Override
	public void init() {
		FGYLActivityStateData data = this.calcInfo();
		//重置一下状态
		this.getDataManager().getStateData().setTermId(data.getTermId());
		this.getDataManager().getStateData().setState(FGYLActivityState.SHOW);
		this.getDataManager().getStateData().saveRedis();
		//清除一下数据
		this.getDataManager().clearGuildJoinData();
	}
	
	
	@Override
	public void tick() {
		FGYLActivityStateData data = this.calcInfo();
		//如果不在当前状态，则往下个状态推进
		FGYLActivityStateData curData = this.getDataManager().getStateData();
		if(curData.getTermId() != data.getTermId()
				|| curData.getState() != data.getState()){
			this.getService().updateState(FGYLActivityState.OPEN);
			return;
		}
		
	}


	@Override
	public void addBuilder(PBFGYLWarStateInfo.Builder builder, Player player) {
		FGYLActivityStateData stateData = this.getDataManager().getStateData();
		int termdId = stateData.getTermId();
		builder.setTermId(termdId);
		builder.setState(PBFGYLWarState.FGYL_SHOW);
		FGYLTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(FGYLTimeCfg.class, termdId);
		builder.setShowTime(timeCfg.getShowTimeValue());
		builder.setStartTime(timeCfg.getStartTimeValue());
		builder.setEndTime(timeCfg.getEndTimeValue());
		builder.setHiddenTime(timeCfg.getHiddenTimeValue());
		
	}
	
	


	


	
	
}
