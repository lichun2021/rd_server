package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.util.GameUtil;

/**
 * 超级武器配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/super_barrack.xml")
public class SuperWeaponCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	/**
	 * 坐标x
	 */
	protected final int x;
	/**
	 * 坐标y
	 */
	protected final int y;
	/**
	 * 超级武器类型
	 */
	protected final int type;
	/**
	 * 超级武器等级
	 */
	protected final int classify;
	/**
	 * 超级武器buff
	 */
	protected final String buff;
	/**
	 * 英雄
	 */
	protected final String heroList;
	/**
	 * 士兵
	 */
	protected final String soldierList;
	
	/**
	 * 超级武器buff
	 */
	private List<EffectObject> buffList;
	
	/**
	 * 英雄id列表
	 */
	private List<Integer> heroIdList;
	
	/**
	 * npc部队信息
	 */
	private List<ArmyInfo> armyList;
	
	public SuperWeaponCfg() {
		this.id = 0;
		this.x = 0;
		this.y = 0;
		this.type = 0;
		this.classify = 0;
		this.buff = "";
		this.heroList = "";
		this.soldierList = "";
	}

	public int getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getType() {
		return type;
	}

	public int getClassify() {
		return classify;
	}

	public String getBuff() {
		return buff;
	}
	
	public List<EffectObject> getBuffList() {
		return buffList;
	}

	public void setBuffList(List<EffectObject> buffList) {
		this.buffList = buffList;
	}
	
	public List<Integer> getHeroIdList() {
		return heroIdList;
	}

	public List<ArmyInfo> getArmyList() {
		List<ArmyInfo> list = new ArrayList<>();
		armyList.forEach(e -> list.add(e.getCopy()));
		return list;
	}

	@Override
	protected boolean assemble() {
		buffList = GameUtil.assambleSuperBarrackEffect(buff);
		
		heroIdList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(heroList)) {
			String[] heros = heroList.split("\\|");
			for (int i = 0; i < heros.length; i++) {
				heroIdList.add(Integer.valueOf(heros[i]));
			}
		}
		
		armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(soldierList)) {
			String[] soldiers = soldierList.split("\\|");
			for (int i = 0; i < soldiers.length; i++) {
				String[] soldier = soldiers[i].split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(soldier[0]), Integer.parseInt(soldier[1])));
			}
		}
		return true;
	}
}
