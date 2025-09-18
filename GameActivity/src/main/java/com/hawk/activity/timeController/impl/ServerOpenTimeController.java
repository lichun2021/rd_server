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
 * 根据开服时间开启活动的时间控制器
 * 
 * @author Jesse
 */
public abstract class ServerOpenTimeController extends ITimeController {

	
	
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		long timeGap = now - serverOpenDate;
		if (timeGap < 0) {
			return Optional.empty();
		}

		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(getTimeCfgClass());
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			if (timeGap >= timeCfg.getShowTimeValue() && timeGap < timeCfg.getHiddenTimeValue()) {
				return Optional.of(timeCfg);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public List<long[]> getOpenTimes(long startTime, long endTime,ActivityCfg acfg) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		long timeGap = endTime - serverOpenDate;
		if(timeGap <0){
			return null;
		}
		
		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(getTimeCfgClass());
		List<long[]> list = new ArrayList<long[]>();
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			long showCfg = timeCfg.getShowTimeValue() + serverOpenDate;
			long startCfg= timeCfg.getStartTimeValue() + serverOpenDate;
			long endCfg = timeCfg.getEndTimeValue()+ serverOpenDate;
			long hiddenCfg = timeCfg.getHiddenTimeValue() + serverOpenDate;
			boolean isOpne = true;
			//如果在合服期间
			if (inMergeDate(showCfg, hiddenCfg, acfg)) {
				isOpne = false;
			}
			// 开始  结束  配置开启     配置结束 
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

	@Override
	protected long getCurShowTime(long now) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getShowTimeValue() + serverOpenDate;
	}

	@Override
	protected long getCurStartTime(long now) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getStartTimeValue() + serverOpenDate;
	}

	@Override
	protected long getCurEndTime(long now) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (opTimeCfg == null) {
			return 0;
		}
		return opTimeCfg.get().getEndTimeValue() + serverOpenDate;
	}

	@Override
	protected long getCurHiddenTime(long now) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getHiddenTimeValue() + serverOpenDate;
	}

	@Override
	public long getShowTimeByTermId(int termId) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getShowTimeValue() + serverOpenDate;
	}

	@Override
	public long getStartTimeByTermId(int termId) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getStartTimeValue() + serverOpenDate;
	}

	@Override
	public long getEndTimeByTermId(int termId) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getEndTimeValue() + serverOpenDate;
	}

	@Override
	public long getHiddenTimeByTermId(int termId) {
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getHiddenTimeValue() + serverOpenDate;
	}

}
