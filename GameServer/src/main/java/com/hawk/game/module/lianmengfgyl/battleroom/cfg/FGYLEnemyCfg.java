package com.hawk.game.module.lianmengfgyl.battleroom.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/fgyl_enemy.xml")
public class FGYLEnemyCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int foggyFortress;
	private final int coordinateX;
	private final int coordinateY;
	private final int gridCnt;
	private final int type;
	private final int level;
	private final int peaceTime;
	private final String effect; // 加给npc
	private final String specialDamage; // 加给玩家
	private final int hpNum;
	private final int maxHp;
	private Map<EffType, Integer> npcEffectList;

	private Map<EffType, Integer> effectList;

	public FGYLEnemyCfg() {
		id = 0;
		foggyFortress = 0;
		effect = "";
		maxHp = 1000000;
		coordinateX = 0;
		coordinateY = 0;
		gridCnt = 3;
		type = 1;
		level = 1;
		hpNum = 5;
		peaceTime = 0;
		specialDamage = "";
	}

	public int getNpcEffectVal(EffType eff) {
		return npcEffectList.getOrDefault(eff, 0);
	}

	@Override
	protected boolean assemble() {
		{
			npcEffectList = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(effect)) {
				String[] array = effect.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					npcEffectList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			npcEffectList = ImmutableMap.copyOf(npcEffectList);

		}

		{
			effectList = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(specialDamage)) {
				String[] array = specialDamage.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					effectList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			effectList = ImmutableMap.copyOf(effectList);

		}

		return true;
	}

	public int getEnemyId() {
		return id;
	}

	public int getFoggyFortress() {
		return foggyFortress;
	}

	public String getEffect() {
		return effect;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	public void setEffectList(Map<EffType, Integer> effectList) {
		this.effectList = effectList;
	}

	public String getSpecialDamage() {
		return specialDamage;
	}

	public Map<EffType, Integer> getNpcEffectList() {
		return npcEffectList;
	}

	public void setNpcEffectList(Map<EffType, Integer> effectList) {
		this.npcEffectList = effectList;
	}

	public int getCoordinateX() {
		return coordinateX;
	}

	public int getCoordinateY() {
		return coordinateY;
	}

	public int getGridCnt() {
		return gridCnt;
	}

	public int getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}

	public int getHpNum() {
		return hpNum;
	}

	public int getPeaceTime() {
		return peaceTime;
	}

}
