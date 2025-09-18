package com.hawk.activity.type.impl.dailysign;


import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.RegisterTimeController;
import com.hawk.activity.type.impl.dailysign.cfg.DailySignKVCfg;
import com.hawk.activity.type.impl.dailysign.cfg.DailySignActivityTimeCfg;


public class DailySignTimeController extends RegisterTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DailySignActivityTimeCfg.class;
	}
	
	/**
	 * 获取当前活动期数
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	public int getActivityTermId(long now, String playerId) {
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getTermId();
	}
	
	/**
	 * 获取第一期活动的开始时间
	 * @param activityCfg
	 * @return
	 */
	public static long getActivityPlayerOriginTime(String playerId){
		long registerTime = ActivityManager.getInstance().getPlayerRegistTime(playerId);
		
		DailySignKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DailySignKVCfg.class);
		if(null != cfg){
			if(registerTime < cfg.getFirstOpenAM0MilSec()){
				return cfg.getFirstOpenAM0MilSec();
			}			
		}
		return registerTime;
	}
	
	/**
	 * 获取当期活动时间配置
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	protected Optional<IActivityTimeCfg> getTimeCfg(long now, String playerId) {
		long registerTime = getActivityPlayerOriginTime(playerId);
		long timeGap = now - registerTime;
		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(getTimeCfgClass());
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			if (timeGap >= timeCfg.getShowTimeValue() && timeGap < timeCfg.getHiddenTimeValue()) {
				return Optional.of(timeCfg);
			}
		}
		return Optional.empty();
	}
	
	/**
	 * 获取当期活动的显示时间
	 * 
	 * @param playerId
	 * @param now
	 * @return
	 */
	@Override
	protected long getCurShowTime(long now, String playerId) {
		
		long registerTime = getActivityPlayerOriginTime(playerId);
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getShowTimeValue() + registerTime;
	}
	
	/**
	 * 获取当期活动的开始时间
	 * 
	 * @param playerId
	 * @param now
	 * @return
	 */
	
	@Override
	protected long getCurStartTime(long now, String playerId) {
		long registerTime = getActivityPlayerOriginTime(playerId);
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getStartTimeValue() + registerTime;
	}
	
	/**
	 * 获取当期活动的结束时间
	 * 
	 * @param playerId
	 * @param now
	 * @return
	 */
	
	@Override
	protected long getCurEndTime(long now, String playerId) {
		long registerTime = getActivityPlayerOriginTime(playerId);
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getEndTimeValue() + registerTime;
	}

	/**
	 * 获取当期活动的隐藏时间
	 * 
	 * @param playerId
	 * @param now
	 * @return
	 */
	
	@Override
	protected long getCurHiddenTime(long now, String playerId) {
		long registerTime =getActivityPlayerOriginTime(playerId);
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getHiddenTimeValue() + registerTime;
	}
	
	@Override
	public long getShowTimeByTermId(int termId, String playerId) {
		long registerTime = getActivityPlayerOriginTime(playerId);
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getShowTimeValue() + registerTime;
	}

	@Override
	public long getStartTimeByTermId(int termId, String playerId) {
		long registerTime = getActivityPlayerOriginTime(playerId);
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getStartTimeValue() + registerTime;
	}

	@Override
	public long getEndTimeByTermId(int termId, String playerId) {
		long registerTime = getActivityPlayerOriginTime(playerId);
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getEndTimeValue() + registerTime;
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		long registerTime = getActivityPlayerOriginTime(playerId);
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getHiddenTimeValue() + registerTime;
	}
}
