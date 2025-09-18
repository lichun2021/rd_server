package com.hawk.activity.type.impl.playerComeBack.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/****
 * 老玩家回归活动常量表
 * @author yang.rao
 *
 */

@HawkConfigManager.KVResource(file = "activity/return_activity/return_activity_const_cfg.xml")
public class PlayerComeBackParamsConfig extends HawkConfigBase {
	
	/** 流失时间（单位 s） **/
	private final int lostTime;
	
	/** vip等级 **/
	private final int vip;
	
	/** 指挥官等级 **/
	private final int level;
	
	/** 战斗力 **/
	private final int battlePoint;
	
	/** 基地等级 **/
	private final int buildlevel;
	
	public PlayerComeBackParamsConfig(){
		lostTime = 0;
		vip = 0;
		level = 0;
		battlePoint = 0;
		buildlevel = 0;
	}

	public int getLostTime() {
		return lostTime;
	}

	public int getVip() {
		return vip;
	}

	public int getLevel() {
		return level;
	}

	public int getBattlePoint() {
		return battlePoint;
	}

	public int getBuildLevel() {
		return buildlevel;
	}
}
