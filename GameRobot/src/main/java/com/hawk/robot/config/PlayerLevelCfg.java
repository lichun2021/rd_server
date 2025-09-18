package com.hawk.robot.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 玩家等级信息配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/player_level.xml")
public class PlayerLevelCfg extends HawkConfigBase {
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

	public PlayerLevelCfg() {
		this.id = "";
		this.level = 0;
		this.exp = 0;
		this.skillPoint = 0;
		this.vitPoint = 0;
		this.battlePoint = 0;
		this.levelAward = "";
		this.heroMaxLevel = 0;
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

	public int getHeroMaxLevel() {
		return heroMaxLevel;
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

		return true;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}
}
