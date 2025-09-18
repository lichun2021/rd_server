package com.hawk.game.module.lianmenxhjz.battleroom.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "xml/xhjz_battle.xml")
public class XHJZBattleCfg extends HawkConfigBase {

	protected final int prepairTime;
	protected final int fireSpeed;
	protected final double playerMarchSpeedUp;// ="1.5"
	protected final int mapX;// = 140;
	protected final int mapY;// = 140;
	protected final int moveCityCd;
	// # 副本建筑内击杀敌方战斗力获取1个人积分
	protected final int scoreForKill;// = 50000
	protected final double cureSpeedUp;
	// # 副本建筑内防御坦克损失战斗力获取1个人积分
	protected final int scoreForDefense;// = 10000

	// # 个人初始燃油值
	protected final double fuelBase;// = 500

	// # 个人燃油恢复速度，单位秒
	protected final double fuelRecoverySpeed;// = 0.1574

	// # 个人燃油上限
	protected final double fuelMax;// = 1000

	// # 移动1个岛屿消耗的燃油数(多个岛屿就是乘以这个值）
	protected final double fuelUseBase;// = 5

	// # 出征需要携带的燃油（不带这些油无法出征）
	protected final double fuelMarchNeed;// = 160
	protected final int battleHeal;

	// 2个岛屿之间移动需要的时间 前后端
	protected final int marchBaseTime;// 5
	// 小队燃油初始值 后端
	protected final int fuelBaseTeam;// 4800
	// 小队燃油恢复速度，单位秒 后端
	protected final double fuelRecoverySpeedTeam;// 1
	
	protected final int buildOpenTwo;
	protected final int buildOpenFour;

	public XHJZBattleCfg() {
		buildOpenTwo = 0;
		buildOpenFour = 0;
		fuelMarchNeed = 0;
		fuelBase = 0;
		fuelRecoverySpeed = 0;
		fuelMax = 0;
		fuelUseBase = 0;
		prepairTime = 30;
		fireSpeed = 10;
		playerMarchSpeedUp = 1.5;
		mapX = 140;
		mapY = 140;
		moveCityCd = 30;
		scoreForKill = 5000;
		scoreForDefense = 10000;
		cureSpeedUp = 1;
		battleHeal = 0;
		marchBaseTime = 0;
		fuelBaseTeam = 0;
		fuelRecoverySpeedTeam = 0;
	}

	@Override
	protected boolean assemble() {

		return super.assemble();
	}

	public int getPrepairTime() {
		return prepairTime;
	}

	public int getFireSpeed() {
		return fireSpeed;
	}

	public double getPlayerMarchSpeedUp() {
		return playerMarchSpeedUp;
	}

	public int getMapX() {
		return mapX;
	}

	public int getMapY() {
		return mapY;
	}

	public int getMoveCityCd() {
		return moveCityCd;
	}

	public int getScoreForKill() {
		return scoreForKill;
	}

	public int getScoreForDefense() {
		return scoreForDefense;
	}

	public double getCureSpeedUp() {
		return cureSpeedUp;
	}

	public double getFuelBase() {
		return fuelBase;
	}

	public double getFuelRecoverySpeed() {
		return fuelRecoverySpeed;
	}

	public double getFuelMax() {
		return fuelMax;
	}

	public double getFuelUseBase() {
		return fuelUseBase;
	}

	public double getFuelMarchNeed() {
		return fuelMarchNeed;
	}

	public int getBattleHeal() {
		return battleHeal;
	}

	public int getMarchBaseTime() {
		return marchBaseTime;
	}

	public int getFuelBaseTeam() {
		return fuelBaseTeam;
	}

	public double getFuelRecoverySpeedTeam() {
		return fuelRecoverySpeedTeam;
	}

	public int getBuildOpenTwo() {
		return buildOpenTwo;
	}

	public int getBuildOpenFour() {
		return buildOpenFour;
	}

}
