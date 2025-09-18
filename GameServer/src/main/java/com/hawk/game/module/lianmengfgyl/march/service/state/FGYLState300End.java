package com.hawk.game.module.lianmengfgyl.march.service.state;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLTimeCfg;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLActivityStateData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLGuildJoinData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLRoomData;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLConst.FGYLActivityState;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarState;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarStateInfo;

public class FGYLState300End  extends IFGYLServiceState {

	

	/* (non-Javadoc)
	 * @see com.hawk.game.module.lianmengfgyl.march.service.state.IFGYLServiceState#init()
	 */
	@Override
	public void init() {
		//重置一下状态
		this.getDataManager().getStateData().setState(FGYLActivityState.END);
		this.getDataManager().getStateData().saveRedis();
		//发排行奖励
		this.getService().sendTermRankReward();
	}
	
	
	@Override
	public void tick() {
		FGYLActivityStateData data = this.calcInfo();
		//如果不在当前状态，则往下个状态推进
		FGYLActivityStateData curData = this.getDataManager().getStateData();
		if(curData.getTermId() != data.getTermId()
				|| curData.getState() != data.getState()){
			this.getService().updateState(FGYLActivityState.HIDDEN);
			return;
		}
		
	}


	@Override
	public void addBuilder(PBFGYLWarStateInfo.Builder builder, Player player) {
		FGYLActivityStateData stateData = this.getDataManager().getStateData();
		int termdId = stateData.getTermId();
		builder.setTermId(termdId);
		builder.setState(PBFGYLWarState.FGYL_END);
		FGYLTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(FGYLTimeCfg.class, termdId);
		builder.setShowTime(timeCfg.getShowTimeValue());
		builder.setStartTime(timeCfg.getStartTimeValue());
		builder.setEndTime(timeCfg.getEndTimeValue());
		builder.setHiddenTime(timeCfg.getHiddenTimeValue());
		
		FGYLGuildJoinData joinData = this.getDataManager().getFGYLGuildJoinData(player.getGuildId());
		if(Objects.nonNull(joinData)){
			FGYLRoomData roomData = joinData.getGameRoom();
			if(Objects.nonNull(roomData)){
				builder.setSignLevel(roomData.getFightLevel());
				builder.setBattleRlt(this.genFGYLWarBattleRltBuilder(roomData));
			}
		}
		
	}
	


}
