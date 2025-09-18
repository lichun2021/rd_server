package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/player_level.xml")
public class PlayerLevelExpCfg extends HawkConfigBase {
	/**
	 * 最大等级
	 */
	private static int maxLevel = 0;
	/**
	 * 唯一id,策划用
	 */
	protected final String id;
	/**
	 * 等级
	 */
	@Id
	protected final int level;
	/**
	 * 所需经验
	 */
	protected final int exp;
	/**
	 * 技能点 当前级别最大点
	 */
	protected final int skillPoint;
	/**
	 * 体力 上限
	 */
	protected final int vitPoint;
	/**
	 * 战斗力
	 */
	protected final int battlePoint;
	/**
	 * 等级奖励
	 */
	protected final String levelAward;

	/**
	 * 英雄最大等级
	 */
	protected final int heroMaxLevel;

	/**
	 * 战力计算属性加成
	 */
	protected final String atkAttr;
	
	/**
	 * 战力计算属性加成
	 */
	protected final String hpAttr;
	
	private List<ItemInfo> bonusList;

	public PlayerLevelExpCfg() {
		this.id = "";
		this.level = 0;
		this.exp = 0;
		this.skillPoint = 0;
		this.vitPoint = 0;
		this.battlePoint = 0;
		this.levelAward = "";
		heroMaxLevel = 0;
		atkAttr = "";
		hpAttr = "";
	}

	public String getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getExp() {
		return exp;
	}

	public int getSkillPoint() {
		return skillPoint;
	}

	public int getVitPoint() {
		return vitPoint;
	}

	public int getBattlePoint() {
		return battlePoint;
	}

	public String getLevelAward() {
		return levelAward;
	}

	public List<ItemInfo> getBonusList() {
		return bonusList.stream().map(ItemInfo::clone).collect(Collectors.toList());
	}

	public int getHeroMaxLevel() {
		return heroMaxLevel;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}

	/**
	 * 获取配置的最大等级
	 *
	 * @return
	 */
	public static int getMaxLevel() {
		return maxLevel;
	}

	@Override
	protected boolean assemble() {
		if (maxLevel < level) {
			maxLevel = level;
		}

		bonusList = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(levelAward)) {
			String[] awardItemArray = levelAward.split(",");
			for (String value : awardItemArray) {
				ItemInfo itemInfo = ItemInfo.valueOf(value);
				if (itemInfo != null) {
					bonusList.add(itemInfo);
				}
			}
		}

		return true;
	}

	@Override
	protected boolean checkValid() {
		if (bonusList != null) {
			for (ItemInfo itemInfo : bonusList) {
				if (!itemInfo.checkItemInfo()) {
					return false;
				}
			}
		}
		return true;
	}
}