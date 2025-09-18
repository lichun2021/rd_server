package com.hawk.game.module.lianmengfgyl.battleroom.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.KVResource(file = "xml/fgyl_battle.xml")
public class FGYLBattleCfg extends HawkConfigBase {

	protected final int prepairTime;
	protected final double playerMarchSpeedUp;// ="1.5"
	protected final int mapX;// = 140;
	protected final int mapY;// = 140;
	protected final int teleportCooldown;
	protected final int useHonor;
	protected final int battleWounded;
	protected final int attckTime;
	
	// # 副本建筑内击杀敌方战斗力获取1个人积分
	protected final int fireSpeed;
	protected final int scoreForKill;// = 50000
	protected final double cureSpeedUp;
	// # 副本建筑内防御坦克损失战斗力获取1个人积分
	protected final int scoreForDefense;// = 10000
	protected final String specialEffect; //
	private Map<EffType, Integer> specialEffectList;
	
	protected final String specialEffect0; //
	private Map<EffType, Integer> specialEffectList0;
	protected final String specialEffect1; //
	private Map<EffType, Integer> specialEffectList1;
	protected final String specialEffect2; //
	private Map<EffType, Integer> specialEffectList2;
	protected final String specialEffect3; //
	private Map<EffType, Integer> specialEffectList3;
	protected final String specialEffect4; //
	private Map<EffType, Integer> specialEffectList4;
	public FGYLBattleCfg() {
		prepairTime = 30;
		fireSpeed = 10;
		playerMarchSpeedUp = 1.5;
		mapX = 140;
		mapY = 140;
		teleportCooldown = 30;
		scoreForKill = 5000;
		scoreForDefense = 10000;
		cureSpeedUp = 1;
		useHonor = 0;
		battleWounded= 0;
		attckTime = 0;
		specialEffect="";
		specialEffect0="";
		specialEffect1="";
		specialEffect2="";
		specialEffect3="";
		specialEffect4="";
	}

	@Override
	protected boolean assemble() {
		{
			specialEffectList = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(specialEffect)) {
				String[] array = specialEffect.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					specialEffectList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			specialEffectList = ImmutableMap.copyOf(specialEffectList);

		}
		
		{
			specialEffectList0 = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(specialEffect0)) {
				String[] array = specialEffect0.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					specialEffectList0.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			specialEffectList0 = ImmutableMap.copyOf(specialEffectList0);

		}
		{
			specialEffectList1 = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(specialEffect1)) {
				String[] array = specialEffect1.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					specialEffectList1.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			specialEffectList1 = ImmutableMap.copyOf(specialEffectList1);

		}
		{
			specialEffectList2 = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(specialEffect2)) {
				String[] array = specialEffect2.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					specialEffectList2.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			specialEffectList2 = ImmutableMap.copyOf(specialEffectList2);

		}
		{
			specialEffectList3 = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(specialEffect3)) {
				String[] array = specialEffect3.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					specialEffectList3.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			specialEffectList3 = ImmutableMap.copyOf(specialEffectList3);

		}
		{
			specialEffectList4 = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(specialEffect4)) {
				String[] array = specialEffect4.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					specialEffectList4.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			specialEffectList4 = ImmutableMap.copyOf(specialEffectList4);

		}
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

	public int getTeleportCooldown() {
		return teleportCooldown;
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

	public int getUseHonor() {
		return useHonor;
	}

	public int getBattleWounded() {
		return battleWounded;
	}

	public int getAttckTime() {
		return attckTime;
	}

	public Map<EffType, Integer> getSpecialEffectList(int cnt) {
		switch (cnt) {
		case 0:
			return specialEffectList0;
		case 1:
			return specialEffectList1;
		case 2:
			return specialEffectList2;
		case 3:
			return specialEffectList3;
		case 4:
			return specialEffectList4;
		default:
			break;
		}
		
		return new HashMap<>();
	}

	public void setSpecialEffectList(Map<EffType, Integer> specialEffectList) {
		this.specialEffectList = specialEffectList;
	}

	public String getSpecialEffect() {
		return specialEffect;
	}

}
