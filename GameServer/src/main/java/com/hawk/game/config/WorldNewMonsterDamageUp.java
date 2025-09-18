package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 新版野怪猎杀加成配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_newMonster_damageUp.xml")
public class WorldNewMonsterDamageUp extends HawkConfigBase {
	/**
	 * 攻击次数
	 */
	@Id
	protected final int attackTimes;
	
	/**
	 * 加成伤害
	 */
	protected final int damageBonus;
	
	public WorldNewMonsterDamageUp() {
		attackTimes = 0;
		damageBonus = 0;
	}

	public int getAttackTimes() {
		return attackTimes;
	}
	
	public int getDamageBonus() {
		return damageBonus;
	}
}
