package com.hawk.game.module.spacemecha.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 星甲召唤玩法常量配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "xml/space_machine_const.xml")
public class SpaceMechaConstCfg extends HawkConfigBase {
	/**
	 * 子舱数量
	 */
	protected final int subcabinNum;
	/**
	 * 玩家对各个玩法建筑行军最多队列数量
	 */
	protected final int maxMarchNum;
	/**
	 * 阶段1首波敌军出发时间
	 */
	protected final int cabinFirstWaveTime;
	/**
	 * 阶段1子舱首波敌军出发时间
	 */
	protected final int subcabinFirstWaveTime;
	/**
	 * 阶段2首波敌军出发时间
	 */
	protected final int strongholdFirstWaveTime;
	/**
	 * 阶段3Boss出发时间
	 */
	protected final int bossMarchTime;
	/**
	 * 玩家行军常量
	 */
	protected final int marchTime;
	/**
	 * 敌军行军常量时间
	 */
	protected final int enemyMarchTime;
	/**
	 * 掉落宝箱同时采集人数
	 */
	protected final int boxGatherLimit;
	/**
	 * 点位刷新距离主舱最小距离
	 */
	protected final int minRefreshDistance;
	/**
	 * 进攻据点奖励的单日次数上限
	 */
	protected final int strongholdAwardLimit;
	/**
	 * 对舱体扣血比例
	 */
	protected final String damgeTransPara;
	/**
	 * 联盟放置次数上限
	 */
	protected final int setLimitNum;
	
	/**
	 * 活动期间主舱防守成功全员奖个人领取次数限制
	 */
	protected final int cabinwinAwardPersonLimit;
	/**
	 * 活动期主舱间防守成功参与奖个人领取次数限制
	 */
	protected final int cabinPartiAwardPersonLimit;
	/**
	 * 活动期间子舱防守成功参与奖个人领取次数限制
	 */
	protected final int subcabinPartiAwardPersonLimit;
	/**
	 * 活动期间宝箱采集奖励个人获取次数限制
	 */
	protected final int boxAwardLimit;
	/**
	 * 活动期间据点奖励个人领取总次数限制
	 */
	protected final int strongholdAwardPersonLimit;
	
	private int mainSpaceDamageRatio = 10000;
	private int subSpaceDamageRatio = 10000;
	
	public SpaceMechaConstCfg() {
		subcabinNum = 2;
		maxMarchNum = 1;
		cabinFirstWaveTime = 0;
		subcabinFirstWaveTime = 0;
		strongholdFirstWaveTime = 0;
		bossMarchTime = 0;
		marchTime = 0;
		enemyMarchTime = 0;
		boxGatherLimit = 0;
		minRefreshDistance = 0;
		strongholdAwardLimit = 1;
		damgeTransPara = "";
		setLimitNum = 0;
		cabinwinAwardPersonLimit = 0;
		cabinPartiAwardPersonLimit = 0;
		subcabinPartiAwardPersonLimit = 0;
		boxAwardLimit = 0;
		strongholdAwardPersonLimit = 0;
	}
	
	public int getStrongholdAwardLimit() {
		return strongholdAwardLimit;
	}

	public int getSubcabinNum() {
		return subcabinNum;
	}

	public int getMaxMarchNum() {
		return maxMarchNum;
	}

	public long getCabinFirstWaveTime() {
		return cabinFirstWaveTime * 1000L;
	}

	public long getSubcabinFirstWaveTime() {
		return subcabinFirstWaveTime * 1000L;
	}

	public long getStrongholdFirstWaveTime() {
		return strongholdFirstWaveTime * 1000L;
	}

	public long getBossMarchTime() {
		return bossMarchTime * 1000L;
	}

	public long getMarchTime() {
		return marchTime * 1000L;
	}

	public long getEnemyMarchTime() {
		return enemyMarchTime * 1000L;
	}

	public int getBoxGatherLimit() {
		return boxGatherLimit;
	}
	
	public int getMinRefreshDistance() {
		return minRefreshDistance > 0 ? minRefreshDistance : 20;
	}

	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(damgeTransPara)) {
			String[] damageArr = damgeTransPara.split("_");
			mainSpaceDamageRatio = Integer.parseInt(damageArr[0]);
			subSpaceDamageRatio = Integer.parseInt(damageArr[1]);
		}
		return super.assemble();
	}
	
	public double getMainSpaceDamageRatio() {
		return mainSpaceDamageRatio * 1D / 10000;
	}
	
	public double getSubSpaceDamageRatio() {
		return subSpaceDamageRatio * 1D / 10000;
	}
	
	public int getSetLimitNum() {
		return setLimitNum;
	}

	public int getCabinwinAwardPersonLimit() {
		return cabinwinAwardPersonLimit;
	}

	public int getCabinPartiAwardPersonLimit() {
		return cabinPartiAwardPersonLimit;
	}

	public int getSubcabinPartiAwardPersonLimit() {
		return subcabinPartiAwardPersonLimit;
	}

	public int getBoxAwardLimit() {
		return boxAwardLimit;
	}

	public int getStrongholdAwardPersonLimit() {
		return strongholdAwardPersonLimit;
	}
	
}
