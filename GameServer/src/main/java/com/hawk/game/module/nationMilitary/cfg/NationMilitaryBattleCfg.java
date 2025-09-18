package com.hawk.game.module.nationMilitary.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 国家军衔等级配置
 * @author lating
 * @since 2022年4月24日
 */
@HawkConfigManager.XmlResource(file = "xml/nation_military_battle.xml")
@HawkConfigBase.CombineId(fields = { "id", "type"})
public class NationMilitaryBattleCfg extends HawkConfigBase {

	protected final int id;// ="1"
	protected final int type;// ="1"
	protected final int kill;// ="2"
	protected final int die;// ="1"

	public NationMilitaryBattleCfg() {
		this.id = 0;
		this.kill = 0;
		this.die = 0;
		this.type = 0;
	}

	public int getId() {
		return id;
	}

	public int getKill() {
		return kill;
	}

	public int getDie() {
		return die;
	}

	public int getType() {
		return type;
	}

}
