package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.hawk.game.march.ArmyInfo;

/**
 * 机甲配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_gundam.xml")
public class WorldGundamCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	protected final String soldier;
	
	protected final String killAward;
	
	protected final int costPhysicalPower;
	
	private List<ArmyInfo> armyList;
	
	private List<Integer> killAwards;
	
	public WorldGundamCfg() {
		id = 0;
		soldier = "";
		killAward = "";
		costPhysicalPower = 0;
	}

	public int getId() {
		return id;
	}
	
	public int getCostPhysicalPower() {
		return costPhysicalPower;
	}

	public List<ArmyInfo> getArmyList() {
		List<ArmyInfo> list = new ArrayList<>();
		armyList.forEach(e -> list.add(e.getCopy()));
		return list;
	}

	public List<Integer> getKillAwards() {
		return new ArrayList<>(killAwards);
	}

	@Override
	protected boolean assemble() {
		
		List<ArmyInfo> armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(soldier)) {
			for (String army : Splitter.on("|").split(soldier)) {
				String[] armyStrs = army.split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(armyStrs[0]), Integer.parseInt(armyStrs[1])));
			}
		}
		this.armyList = armyList;
		
		List<Integer> killAawards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(killAward)) {
			for (String award : Splitter.on(";").split(killAward)) {
				killAawards.add(Integer.parseInt(award));
			}
		}
		this.killAwards = killAawards;
		
		return true;
	}
}
