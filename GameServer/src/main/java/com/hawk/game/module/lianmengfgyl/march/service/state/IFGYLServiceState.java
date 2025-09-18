package com.hawk.game.module.lianmengfgyl.march.service.state;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.GsConfig;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLBattleCfg;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLTimeCfg;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLActivityStateData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLDataManager;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLRoomData;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLConst.FGYLActivityState;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLMatchService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarBattleRlt;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarBattleStage;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarStateInfo;

public abstract class IFGYLServiceState {
	
	

	public void init(){
		
	}
	
	public abstract void tick();


	public FGYLMatchService getService() {
		return FGYLMatchService.getInstance();
	}
	
	public void syncServiceStateInfo(){
		
	}
	
	public FGYLTimeCfg getTimeCfg(){
		int termId = this.getDataManager().getStateData().getTermId();
		return  HawkConfigManager.getInstance().getConfigByKey(FGYLTimeCfg.class, termId);
	}
	
	protected FGYLActivityStateData calcInfo() {
		FGYLActivityStateData info = new FGYLActivityStateData();
		ConfigIterator<FGYLTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(FGYLTimeCfg.class);
		long now = HawkTime.getMillisecond();
		FGYLTimeCfg cfg = null;
		if(GsConfig.getInstance().getServerType() == ServerType.GUEST){
			return info;
		}
		for (FGYLTimeCfg timeCfg : its) {
			if (now > timeCfg.getShowTimeValue()) {
				cfg = timeCfg;
			}
		}
		// 没有可供开启的配置
		if (cfg == null) {
			return info;
		}
		
		int termId = 0;
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		String serverId = GsConfig.getInstance().getServerId();
		Long mergerTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		long serverDelay = constCfg.getServerDelay() * 1000;
		long timeLimit = serverOpenDate + serverDelay;
		
		FGYLActivityState state = FGYLActivityState.HIDDEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long showTime = cfg.getShowTimeValue();
			long startTime = cfg.getStartTimeValue();
			long endTime = cfg.getEndTimeValue();
			long hiddenTime = cfg.getHiddenTimeValue();
			if (now < showTime) {
				state = FGYLActivityState.HIDDEN;
			}
			if (now >= showTime && now < startTime) {
				state = FGYLActivityState.SHOW;
			}
			if (now >= startTime && now < endTime) {
				state = FGYLActivityState.OPEN;
			}
			if (now >= endTime && now < hiddenTime) {
				state = FGYLActivityState.END;
			}
			if (now >= hiddenTime) {
				state = FGYLActivityState.HIDDEN;
			}
			//如果拆和服时间在活动时间内，这期不开
			if(mergerTime!= null && showTime <= mergerTime && mergerTime <= hiddenTime){
				state = FGYLActivityState.HIDDEN;
			}
			//如果开服限制时间内，这期不开
			if(timeLimit >= showTime){
				state = FGYLActivityState.HIDDEN;
			}
		}
		
		info.setTermId(termId);
		info.setState(state);
		return info;
	}
	
	
	public abstract void addBuilder(PBFGYLWarStateInfo.Builder builder,Player player);
	
	
	
	
	protected FGYLDataManager getDataManager(){
		return this.getService().getDataManger();
	}
	
		

	public static IFGYLServiceState getFGYLServiceState(FGYLActivityState state){
		if(state == null){
			return null;
		}
		switch (state) {
		case HIDDEN: return new FGYLState000Hidden();
		case SHOW:return new FGYLState100Show();
		case OPEN: return new FGYLState200Open();
		case END: return new FGYLState300End();
		default:
			break;
		}
		return null;
	}

	 
	
	
	public PBFGYLWarBattleRlt.Builder genFGYLWarBattleRltBuilder(FGYLRoomData room){
		PBFGYLWarBattleRlt.Builder builder = PBFGYLWarBattleRlt.newBuilder();
		int timeUse =(int) ((room.getEndTime() - room.getStartTime()) / 1000);
		builder.setStartTime(room.getStartTime());
		builder.setLevel(room.getFightLevel());
		builder.setTimeUse(timeUse);
		builder.setWin(room.getRlt());
		return builder;
	}
	
	public List<PBFGYLWarBattleStage> genFGYLWarStageBuilder(FGYLRoomData room){
		List<PBFGYLWarBattleStage> slist = new ArrayList<>();
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		FGYLBattleCfg battleConstCfg = HawkConfigManager.getInstance().getKVInstance(FGYLBattleCfg.class);
		PBFGYLWarBattleStage.Builder builder1 = PBFGYLWarBattleStage.newBuilder();
		
		long monsterStart = room.getCreateTime() + battleConstCfg.getPrepairTime() * 1000;
		long monsterEnd = room.getCreateTime() + battleConstCfg.getAttckTime() * 1000;
		long battleEnd = room.getCreateTime() + constCfg.getBattleTime() * 1000;
		
		builder1.setStage(1);
		builder1.setStartTime(room.getCreateTime());
		builder1.setEndTime(monsterStart);
		
		PBFGYLWarBattleStage.Builder builder2 = PBFGYLWarBattleStage.newBuilder();
		builder2.setStage(2);
		builder2.setStartTime(monsterStart);
		builder2.setEndTime(monsterEnd);
		
		PBFGYLWarBattleStage.Builder builder3 = PBFGYLWarBattleStage.newBuilder();
		builder3.setStage(3);
		builder3.setStartTime(monsterEnd);
		builder3.setEndTime(battleEnd);
		
		slist.add(builder1.build());
		slist.add(builder2.build());
		slist.add(builder3.build());
		return slist;
	}

	
}
