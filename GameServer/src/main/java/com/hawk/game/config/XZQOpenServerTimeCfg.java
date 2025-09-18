package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Date;
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
import com.hawk.game.util.GameUtil;

/**
 * 开服前4期
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/xzq_openserver_time.xml")
public class XZQOpenServerTimeCfg extends HawkConfigBase implements IXZQTimeCfg {
	/** 活动期数*/
	@Id
	private final int termId;
	
	/** 可报名数量*/
	private final int signupPointNumLimit;
	
	/** 包含的建筑等级*/
	private final String includedPointLevel;
	
	/** 伤兵比例*/
	private final int hurtRate;
	/** 报名时间*/
	private final long signupTime;
	private final long signupEndTime;

	/** 开启时间*/
	private final long startTime;

	/** 结束时间*/
	private final long endTime;

	
	
	
	
	private List<Integer> includedPointLevelList = new ArrayList<>();

	public XZQOpenServerTimeCfg() {
		termId = 0;
		signupTime = 0;
		signupEndTime = 0;
		startTime = 0;
		endTime = 0;
		signupPointNumLimit = 0;
		includedPointLevel = "";
		hurtRate = 5000;
	}

	
	@Override
	protected boolean assemble() {
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
	public int getTermId() {
		return termId;
	}
	
	


	
	

	@Override
	public long getStartTimeValue() {
		long serverOpenDate = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		return startTime * 1000 + serverOpenDate;
	}

	@Override
	public long getEndTimeValue() {
		long serverOpenDate = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		return endTime * 1000 + serverOpenDate;
	}

	@Override
	public long getSignupTimeValue() {
		long serverOpenDate = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		return signupTime * 1000 + serverOpenDate;
	}

	@Override
	public long getSignupEndTimeValue() {
		long serverOpenDate = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		return signupEndTime * 1000 + serverOpenDate;
	}

	

	@Override
	protected boolean checkValid() {
		ConfigIterator<? extends XZQOpenServerTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(XZQOpenServerTimeCfg.class);
		long baseTime = 0;
		long baseTermId = 0;
		for (XZQOpenServerTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" XZQOpenServerTimeCfg check valid failed, term order error: className: {}, termId: {}",
						timeCfg.getClass().getName(), termId);
				return false;
			}
			baseTermId = termId;
			long signupTime = timeCfg.signupTime;
			long signupEndTime = timeCfg.signupEndTime;
			long startTime = timeCfg.startTime;
			long endTime = timeCfg.endTime;
			if (signupTime < baseTime || startTime < signupTime || signupTime > signupEndTime || startTime < signupTime
					|| endTime < startTime ) {
				HawkLog.errPrintln(" XZQOpenServerTimeCfg check valid failed, time error, className: {}, termId: {}",
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
