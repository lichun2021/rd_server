package com.hawk.game.service.simulatewar.data;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.SimulateWarTimeCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.SimulateWar.SimulateWarActivityState;

/**
 * 攻防模拟战的活动数据
 * @author jm
 *
 */
public class SimulateWarActivityData {
	private int termId = 0;

	private SimulateWarActivityState state = SimulateWarActivityState.SW_HIDDEN;
	/**
	 * 阶段flag.
	 */
	private int stageFlag;
	/**
	 * 把符合的公会和玩家刷入Redis.
	 */
	public static final int  FLUSH_SIGN = 0x1;
	/**
	 * 是否已经匹配完成.
	 */
	public static final int MATCH_FINISH = 0x2;
		/**
	 * 是否已经计算战斗结束了.
	 */
	public static final int FIGHTER_FINISH = 0x4;
	
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public SimulateWarActivityState getState() {
		return state;
	}

	public void setState(SimulateWarActivityState state) {
		this.state = state;
	}
	
	/**
	 * 获取时间配置
	 * @return
	 */
	@JSONField(serialize = false)
	public SimulateWarTimeCfg getTimeCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(SimulateWarTimeCfg.class, termId);
	}

	public int getStageFlag() {
		return stageFlag;
	}

	public void setStageFlag(int stageFlag) {
		this.stageFlag = stageFlag;
	}
	
	/**
	 * 完成某个状态。
	 * @param stageFlag
	 */
	public void finishState(int stageFlag) {
		this.stageFlag = this.stageFlag | stageFlag;
	}
	
	public boolean hasFinishState(int stageFlag) {
		return (this.stageFlag & stageFlag) > 0;
	}
	
	public void saveToRedis() {
		RedisProxy.getInstance().addOrUpdateSimulateWarActivityInfo(this);
	}
}
