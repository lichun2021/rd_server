package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.lianmengxzq.timecontroller.IXZQTimeCfg;
import com.hawk.game.manager.AssembleDataManager;

/**
 * 固定时间
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/xzq_time.xml")
public class XZQTimeCfg extends HawkConfigBase implements IXZQTimeCfg {
	/** 活动期数*/
	@Id
	private final int termId;
	
	private final int signupPointNumLimit;
	/** 包含的建筑等级*/
	private final String includedPointLevel;
	
	private final int hurtRate;

	/** 报名时间*/
	private final String signupTime;
	/** 报名结束时间*/
	private final String signupEndTime;
	/** 开启时间*/
	private final String startTime;
	/** 结束时间*/
	private final String endTime;


	private long signupTimeValue;
	private long signupEndTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private List<Integer> includedPointLevelList = new ArrayList<>();
	public XZQTimeCfg() {
		termId = 0;
		signupTime = "";
		startTime = "";
		endTime = "";
		signupEndTime = "";
		includedPointLevel= "";
		signupPointNumLimit = 0;
		hurtRate = 0;
	}
	
	public int getTermId() {
		return termId;
	}



	public long getSignupTimeValue() {
		return signupTimeValue;
	}

	public long getSignupEndTimeValue() {
		return signupEndTimeValue;
	}

	public long getStartTimeValue() {
		return startTimeValue;
	}

	public long getEndTimeValue() {
		return endTimeValue;
	}



	@Override
	protected boolean assemble() {
		signupTimeValue = HawkTime.parseTime(signupTime);
		signupEndTimeValue = HawkTime.parseTime(signupEndTime);
		startTimeValue = HawkTime.parseTime(startTime);
		endTimeValue = HawkTime.parseTime(endTime);
		
		
		if(!HawkOSOperator.isEmptyString(this.includedPointLevel)){
			List<Integer> list = new ArrayList<>();
			String[] arr = this.includedPointLevel.split(",");
			for(String str : arr){
				list.add(Integer.parseInt(str));
			}
			this.includedPointLevelList = list;
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		ConfigIterator<? extends XZQTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(XZQTimeCfg.class);
		long baseTime = 0;
		long baseTermId = 0;
		for (XZQTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" XZQTimeCfg check valid failed, term order error: className: {}, termId: {}",
						timeCfg.getClass().getName(), termId);
				return false;
			}
			baseTermId = termId;
			long signupTime = timeCfg.getSignupTimeValue();
			long signupEndTime = timeCfg.getSignupEndTimeValue();
			long startTime = timeCfg.getStartTimeValue();
			long endTime = timeCfg.getEndTimeValue();
			if (signupTime < baseTime || startTime < signupTime || signupTime > signupEndTime || startTime < signupTime
					|| endTime < startTime) {
				HawkLog.errPrintln(" IXZQTimeCfg check valid failed, time error, className: {}, termId: {}",
						timeCfg.getClass().getName(), termId);
				return false;
			}
			baseTime = endTime;
		}
		return true;
	}
	
	
	@Override
	public int getSignupPointNumLimit() {
		return signupPointNumLimit;
	}
	
	@Override
	public Set<Integer> getBuilds() {
		Set<Integer> set = new HashSet<>();
		for(int level : this.includedPointLevelList){
			Set<Integer> builds = AssembleDataManager.getInstance().getXZQBuilds(level);
			if(builds!= null){
				set.addAll(builds);
			}
		}
		return set;
	}

	@Override
	public List<Integer> getOpenBuildLevels() {
		return this.includedPointLevelList;
	}
	
	@Override
	public int getHurtRate() {
		return this.hurtRate;
	}
}
