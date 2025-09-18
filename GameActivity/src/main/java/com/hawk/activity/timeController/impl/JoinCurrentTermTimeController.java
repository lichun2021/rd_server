package com.hawk.activity.timeController.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.ITimeController;

/**
 * 若本服可开启活动时间节点超出活动结束时间,则本期活动不参与
 * @author Jesse
 */
public abstract class JoinCurrentTermTimeController extends ITimeController{
	
	/**
	 * 获取服务器开服延时开启活动时间
	 */
	public abstract long getServerDelay();

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		long serverDelay = getServerDelay();
		boolean delay = serverDelay != 0;
		long timeLimit = serverOpenDate + serverDelay;
		if (delay && timeLimit > now) {
			return Optional.empty();
		}

		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(getTimeCfgClass());
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			if (now >= timeCfg.getShowTimeValue() && now < timeCfg.getHiddenTimeValue()) {
				// 延迟显示时间节点超出活动结束时间,则本期活动不参与
				if (delay && timeLimit > timeCfg.getEndTimeValue()) {
					return Optional.empty();
				}
				return Optional.of(timeCfg);
			}
		}
		return Optional.empty();
	}

	
	@Override
	public List<long[]> getOpenTimes(long startTime, long endTime,ActivityCfg acfg) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		long serverDelay = getServerDelay();
		boolean delay = serverDelay != 0;
		long timeLimit = serverOpenDate + serverDelay;
		
		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(getTimeCfgClass());
		List<long[]> list = new ArrayList<long[]>();
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			long showCfg = timeCfg.getShowTimeValue();
			long startCfg= timeCfg.getStartTimeValue();
			long endCfg = timeCfg.getEndTimeValue();
			long hiddenCfg = timeCfg.getHiddenTimeValue();
			boolean isOpne = true;
			// 延迟显示时间节点超出活动结束时间,则本期活动不参与
			if (delay && timeLimit > timeCfg.getEndTimeValue()) {
				isOpne = false;
			}
			//如果在合服期间
			if (inMergeDate(showCfg,hiddenCfg, acfg)) {
				isOpne = false;
			}
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				long[] arr = new long[3];
				arr[0] = startCfg;
				arr[1] = endCfg;
				arr[2] = timeCfg.getTermId();
				list.add(arr);
			}
		}
		return list;
			
	}
}
