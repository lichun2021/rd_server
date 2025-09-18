package com.hawk.game.lianmengxzq.timecontroller;

import java.util.List;
import java.util.Set;

import com.hawk.game.protocol.XZQ.PBXZQTimeInfo;

public interface IXZQTimeCfg {

	/**
	 * 获取活动期数
	 * 
	 * @return
	 */
	public int getTermId();

	
	/**
	 * 获取可以报名数量
	 * @return
	 */
	public int getSignupPointNumLimit();

	/**
	 * 获取开放的建筑
	 * 
	 * @return
	 */
	public Set<Integer> getBuilds();

	/**
	 * 
	 * @return
	 */
	public List<Integer> getOpenBuildLevels();

	/**
	 * 获取伤兵比例
	 * @return
	 */
	public int getHurtRate();
	


	/**
	 * 获取报名开始时间
	 * 
	 * @return
	 */
	public long getSignupTimeValue();

	/**
	 * 获取报名结束时间
	 * 
	 * @return
	 */
	public long getSignupEndTimeValue();

	/**
	 * 获取战斗开始时间
	 * 
	 * @return
	 */
	public long getStartTimeValue();

	/**
	 * 获取战斗结束时间
	 * 
	 * @return
	 */
	public long getEndTimeValue();



	
	

	default PBXZQTimeInfo.Builder genPBXZQTimeInfoBuilder() {
		PBXZQTimeInfo.Builder tbuilder = PBXZQTimeInfo.newBuilder();
		tbuilder.setTermId(this.getTermId());
		tbuilder.setSignupType(0);
		tbuilder.setSignupStartTime(this.getSignupTimeValue());
		tbuilder.setSignupEndTime(this.getSignupEndTimeValue());
		tbuilder.setOpenTime(this.getStartTimeValue());
		tbuilder.setEndTime(this.getEndTimeValue());
		tbuilder.addAllOpenBuildLevel(this.getOpenBuildLevels());
		tbuilder.setHurtRate(this.getHurtRate());
		return tbuilder;
	}

}
