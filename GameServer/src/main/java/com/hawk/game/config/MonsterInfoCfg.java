package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.march.ArmyInfo;

/**
 * 怪物兵种信息表
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/battle_army.xml")
public class MonsterInfoCfg extends HawkConfigBase {
	@Id
	protected final int id;

	protected final String soldier;
	
	protected final String buff;

	protected List<ArmyInfo> armyList;
	
	protected Map<Integer, Integer> buffMap;

	public MonsterInfoCfg() {
		this.id = 0;
		this.soldier = "";
		this.buff = "";
	}

	public int getId() {
		return id;
	}

	public List<ArmyInfo> getArmyList() {
		return armyList;
	}
	
	public Map<Integer, Integer> getBuffMap() {
		return buffMap;
	}

	@Override
	protected boolean assemble() {
		armyList = new ArrayList<ArmyInfo>();
		if (!HawkOSOperator.isEmptyString(soldier)) {
			String[] array = soldier.split(",");
			for (String value : array) {
				String[] info = value.split("_");
				if (info != null && info.length == 2) {
					ArmyInfo army = new ArmyInfo(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
					armyList.add(army);
				}
			}
		}
		
		buffMap = new HashMap<Integer, Integer>();
		if (!HawkOSOperator.isEmptyString(buff)) {
			String[] array = buff.split(",");
			for (String value : array) {
				String[] info = value.split("_");
				if (info != null && info.length == 2) {
					buffMap.put(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
				}
			}
		}
		
		return true;
	}
}
