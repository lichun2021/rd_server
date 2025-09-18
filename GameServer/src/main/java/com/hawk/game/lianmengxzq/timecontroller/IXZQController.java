package com.hawk.game.lianmengxzq.timecontroller;

import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.config.XZQOpenServerTimeCfg;
import com.hawk.game.config.XZQTimeCfg;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.protocol.XZQ.PBXZQStatus;
import com.hawk.game.util.GameUtil;

/**
 * 基础活动时间控制器(按期配置,循环)
 * 
 * @author Jesse
 *
 */
public abstract class IXZQController {



	/**
	 * 获取当期活动时间配置
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	protected abstract Optional<IXZQTimeCfg> getTimeCfg(long now);


	/**
	 * 获取当前期数
	 * @param now
	 * @return
	 */
	public int getActivityTermId(long now) {
		Optional<IXZQTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getTermId();
	}
	
	
	

	/**
	 * 更新活动状态(仅供ActivityManager更新活动状态,外部禁止调用!!!!!)
	 * @param xzqService 
	 * 
	 * @param config
	 * @param activity
	 */
	public void updateState() {
		XZQTermStatus curData = getCurrentState();
		//完成上轮剩余
		this.finishLast(curData);
		//如果已经全完成争夺直接结束
		if(curData.getStatus() == PBXZQStatus.XZQ_OPEN && 
				XZQService.getInstance().checkBattleOver()){
			curData.setStatus(PBXZQStatus.XZQ_HIDDEN);
		}
		while (curData.getStatus() != XZQService.getInstance().getState()) {
			switch (this.getNextState(XZQService.getInstance().getState())) {
			case XZQ_SIGNUP:
				XZQService.getInstance().onSignup();
				break;
			case XZQ_WAIT_OPEN:
				XZQService.getInstance().onWaiteopen();
				break;
			case XZQ_OPEN:
				XZQService.getInstance().onOpen();
				break;
			case XZQ_HIDDEN:
				XZQService.getInstance().onHiden();
			default:
				break;
			}
		}
	}
	
	
	private void finishLast(XZQTermStatus curData){
		int xzqTermId = XZQService.getInstance().getXZQTermId();
		if(curData.getTermId() == 0 || xzqTermId < curData.getTermId()){
			// 因为停服，使得上一期没有走完，并且新的一期已经开启
			while (PBXZQStatus.XZQ_HIDDEN != XZQService.getInstance().getState()) {
				switch (this.getNextState(XZQService.getInstance().getState())) {
				case XZQ_SIGNUP:
					XZQService.getInstance().onSignup();
					break;
				case XZQ_WAIT_OPEN:
					XZQService.getInstance().onWaiteopen();
					break;
				case XZQ_OPEN:
					XZQService.getInstance().onOpen();
					break;
				case XZQ_HIDDEN:
					XZQService.getInstance().onHiden();
				default:
					break;
				}
			}
		}
	}
	
	/**
	 * 获取下一个阶段
	 * @param state
	 * @return
	 */
	protected  PBXZQStatus getNextState(PBXZQStatus state){
		switch (state) {
			case XZQ_SIGNUP:return PBXZQStatus.XZQ_WAIT_OPEN;
			case XZQ_WAIT_OPEN:return PBXZQStatus.XZQ_OPEN;
			case XZQ_OPEN:return PBXZQStatus.XZQ_HIDDEN;
			case XZQ_HIDDEN:return PBXZQStatus.XZQ_SIGNUP;
			default: return null; 
		}
	}

	/**
	 * 获取当前活动状态
	 * 
	 * @param activity
	 * @param timeCfg
	 * @return
	 */
	protected XZQTermStatus getCurrentState() {
		XZQTermStatus statues = new XZQTermStatus();
		long now = HawkTime.getMillisecond();
		Optional<IXZQTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			statues.setStatus(PBXZQStatus.XZQ_HIDDEN);
			return statues;
		}
		IXZQTimeCfg cfg = opTimeCfg.get();
		statues.setTermId(cfg.getTermId());
		long signupTime = cfg.getSignupTimeValue();
		long signupEndTime = cfg.getSignupEndTimeValue();
		long startTime = cfg.getStartTimeValue();
		long endTime = cfg.getEndTimeValue();
		if (now >= signupTime && now < signupEndTime) {
			statues.setStatus(PBXZQStatus.XZQ_SIGNUP);
		} else if (now >= signupEndTime && now < startTime) {
			statues.setStatus(PBXZQStatus.XZQ_WAIT_OPEN);
		} else if (now >= startTime && now < endTime) {
			statues.setStatus(PBXZQStatus.XZQ_OPEN);
		} else {
			statues.setStatus(PBXZQStatus.XZQ_HIDDEN);
		}
		return statues;
	}

	


	/**
	 * 获取即将开放的最近一期的时间配置
	 * @return
	 */
	public IXZQTimeCfg getBuildNearlyTimeCfg(int buildLevel){
		long xzqOpenTime = XZQConstCfg.getInstance().getXzqOpenTimeValue();
		long serverOpenTime = GameUtil.getServerOpenTime();
		boolean newServer = (xzqOpenTime <= serverOpenTime);
		
		int serverOpenDays = GameUtil.getServerOpenDays(); // 开服几天
		int chooseDays =  XZQConstCfg.getInstance().getOpenTimeDays();
		long curTime = HawkTime.getMillisecond();
		
		List<XZQOpenServerTimeCfg> list = HawkConfigManager.getInstance().
				getConfigIterator(XZQOpenServerTimeCfg.class).toList();
		for(XZQOpenServerTimeCfg cfg : list){
			if(newServer && curTime < cfg.getSignupTimeValue() &&
					serverOpenDays <= chooseDays &&
						cfg.getOpenBuildLevels().contains(buildLevel)){
				return cfg;
			}
		}
		
		long starTime = 0;
		if(newServer){
			starTime =  GsApp.getInstance().getServerOpenAM0Time() + 
					chooseDays * HawkTime.DAY_MILLI_SECONDS;
		}
		
		String serverId = GsConfig.getInstance().getServerId();
		Long serverMergeTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		List<XZQTimeCfg> list2 = HawkConfigManager.getInstance().
				getConfigIterator(XZQTimeCfg.class).toList();
		for(XZQTimeCfg cfg : list2){
			if(serverMergeTime!= null && serverMergeTime > cfg.getSignupTimeValue()
					&&serverMergeTime < cfg.getEndTimeValue()){
				continue;
			}
			if(curTime < cfg.getSignupTimeValue() &&
					starTime < cfg.getSignupTimeValue() && 
							cfg.getOpenBuildLevels().contains(buildLevel)){
				return cfg;
			}
		}
		return null;
	}

	/**
	 * 获取即将开放的最近一期的时间配置
	 * @return
	 */
	public IXZQTimeCfg getNearlyTimeCfg(){
		long xzqOpenTime = XZQConstCfg.getInstance().getXzqOpenTimeValue();
		long serverOpenTime = GameUtil.getServerOpenTime();
		boolean limit = (xzqOpenTime <= serverOpenTime);
		
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		int serverOpenDays = GameUtil.getServerOpenDays(); // 开服几天
		int chooseDays =  XZQConstCfg.getInstance().getOpenTimeDays();
		long curTime = HawkTime.getMillisecond();
		if(limit && serverOpenDays <= chooseDays){
			List<XZQOpenServerTimeCfg> list = HawkConfigManager.getInstance().
					getConfigIterator(XZQOpenServerTimeCfg.class).toList();
			for(XZQOpenServerTimeCfg cfg : list){
				if(curTime < cfg.getSignupEndTimeValue()){
					return cfg;
				}
			}
		}
		long starTime = 0;
		if(limit){
			starTime = serverOpenDate + chooseDays * HawkTime.DAY_MILLI_SECONDS;
		}
		String serverId = GsConfig.getInstance().getServerId();
		Long serverMergeTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		List<XZQTimeCfg> list2 = HawkConfigManager.getInstance().
				getConfigIterator(XZQTimeCfg.class).toList();
		for(XZQTimeCfg cfg : list2){
			if(serverMergeTime!= null && serverMergeTime > cfg.getSignupTimeValue()
					&&serverMergeTime < cfg.getEndTimeValue()){
				continue;
			}
			if(curTime < cfg.getSignupTimeValue() &&
					starTime < cfg.getSignupTimeValue() &&
						xzqOpenTime < cfg.getSignupTimeValue()){
				return cfg;
			}
		}
		return null;
	}
	
	
	/**
	 * 获取即将开放的最近一期的时间配置
	 * @return
	 */
	public IXZQTimeCfg getTimeCfg(int termId){
		IXZQTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XZQOpenServerTimeCfg.class, termId);
		if(cfg != null){
			return cfg;
		}
		cfg = HawkConfigManager.getInstance().
				getConfigByKey(XZQTimeCfg.class,termId);
		return cfg;
	}
	
	
	
}
