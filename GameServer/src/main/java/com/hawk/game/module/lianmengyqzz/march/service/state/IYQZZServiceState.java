package com.hawk.game.module.lianmengyqzz.march.service.state;

import java.util.Date;

import com.hawk.game.manager.AssembleDataManager;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkTime;

import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.GsConfig;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZWarConstCfg;
import com.hawk.game.module.lianmengyqzz.march.data.YQZZDataManager;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.util.GameUtil;

public abstract class IYQZZServiceState {
	
	
	
	public IYQZZServiceState(YQZZMatchService parent) {
		this.parent = parent;
	}

	private YQZZMatchService parent;

	public void init(){
		
	}
	
	public abstract void tick();

	public void gmOp(){

	}

	public YQZZMatchService getParent() {
		return parent;
	}
	
	public void syncServiceStateInfo(){
		
	}

	
	public YQZZTimeCfg getTimeCfg(){
		int termId = this.getDataManager().getStateData().getTermId();
		return  HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
	}
	
	
	
	protected YQZZActivityStateData calcInfo() {
		YQZZActivityStateData info = new YQZZActivityStateData();
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		ConfigIterator<YQZZTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		long now = HawkTime.getMillisecond();
		long serverOpenAm0 = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		long serverDelay = constCfg.getServerDelay() * 1000;
		String serverId = GsConfig.getInstance().getServerId();
		YQZZTimeCfg cfg = null;
		if(GsConfig.getInstance().getServerType() == ServerType.GUEST){
			return info;
		}
		if(constCfg.getOpenServerList().size() >0 &&
				!constCfg.getOpenServerList().contains(serverId)){
			return info;
		}
		for (YQZZTimeCfg timeCfg : its) {
			if (serverOpenAm0 + serverDelay > timeCfg.getShowTimeValue()) {
				continue;
			}
			if (now > timeCfg.getShowTimeValue()) {
				cfg = timeCfg;
			}
		}
		// 没有可供开启的配置
		if (cfg == null) {
			return info;
		}
		if (AssembleDataManager.getInstance().isCrossOverMergeServerCfg(cfg.getShowTimeValue(), cfg.getHiddenTimeValue(), serverId)) {
			return info;
		}
		int termId = 0;
		YQZZActivityState state = YQZZActivityState.HIDDEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long showTime = cfg.getShowTimeValue();
			long matchTime = cfg.getMatchTimeValue();
			long battleTime = cfg.getBattleTimeValue();
			long rewardTime = cfg.getRewardTimeValue();
			long endShowTime = cfg.getEndShowTimeValue();
			long hiddenTime = cfg.getHiddenTimeValue();
			if (now < showTime) {
				state = YQZZActivityState.HIDDEN;
			}
			if (now >= showTime && now < matchTime) {
				state = YQZZActivityState.START_SHOW;
			}
			if (now >= matchTime && now < battleTime) {
				state = YQZZActivityState.MATCH;
			}
			if (now >= battleTime && now < rewardTime) {
				state = YQZZActivityState.BATTLE;
			}
			if (now >= rewardTime && now < endShowTime) {
				state = YQZZActivityState.REWARD;
			}
			if (now >= endShowTime && now < hiddenTime) {
				state = YQZZActivityState.END_SHOW;
			}
			if (now >= hiddenTime) {
				state = YQZZActivityState.HIDDEN;
			}
		}
		
		info.setTermId(termId);
		info.setState(state);
		return info;
	}
	
	
	protected YQZZDataManager getDataManager(){
		return this.parent.getDataManger();
	}
	
	

	public static IYQZZServiceState getYQZZServiceState(YQZZActivityState state,YQZZMatchService serveice){
		if(state == null){
			return null;
		}
		switch (state) {
		case START_SHOW: return new YQZZState100StartShow(serveice);
		case MATCH:return new YQZZState200Match(serveice);
		case BATTLE: return new YQZZState300Battle(serveice);
		case REWARD: return new YQZZState310Reward(serveice);
		case END_SHOW:return new YQZZState400EndShow(serveice);
		case HIDDEN: return new YQZZState000Hidden(serveice);
		default:
			break;
		}
		return null;
	}

	
}
