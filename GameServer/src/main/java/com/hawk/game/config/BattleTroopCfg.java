package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 战斗群组配置
 * 
 * @author Jesse
 */
@HawkConfigManager.XmlResource(file = "xml/battle_troop.xml")
public class BattleTroopCfg extends HawkConfigBase {

	@Id
	protected final int id;
	// 战斗群组
	protected final String troops;
	// 战斗群组 int{id,cont,maxCount,waitTime)
	protected List<int[]> troopsList;
	// 入场延迟
	protected final int waitTime;

	public BattleTroopCfg() {
		id = 0;
		troops = "";
		troopsList = new ArrayList<>();
		waitTime = 0;
	}

	public int getId() {
		return id;
	}

	public String getTroops() {
		return troops;
	}

	public List<int[]> getTroopsList() {
		return troopsList;
	}

	public int getWaitTime() {
		return waitTime;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(this.troops)) {
			String[] troops_strs = troops.split(",");
			for (String troops_str : troops_strs) {
				if (HawkOSOperator.isEmptyString(troops_str)) {
					continue;
				}
				String[] troop_strs = troops_str.split("_");
				int[] troop = new int[4];
				for (int i = 0; i < troop_strs.length; i++) {
					troop[i] = Integer.parseInt(troop_strs[i]);
				}
				troop[3] = waitTime;
				troopsList.add(troop);
			}
		}
		return true;
	}
}
