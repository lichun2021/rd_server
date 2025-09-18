package com.hawk.activity.type.impl.battlefield.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 战地寻宝活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/battlefield_treasure/battlefield_circle_award.xml")
public class BattleFieldCircleAwardCfg extends HawkConfigBase {
	@Id
	private final int id;
	/** 奖励列表*/
	private final String award;
	
	private List<Integer> awardIdList = new ArrayList<Integer>();

	public BattleFieldCircleAwardCfg() {
		id = 0;
		award = "";
	}
	
	public int getId() {
		return id;
	}

	@Override
	protected boolean assemble() {
		String[] ids = award.split(",");
		for (String id : ids) {
			awardIdList.add(Integer.valueOf(id));
		}
		return true;
	}
	
	public List<Integer> getAwardIdList() {
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(awardIdList);
		return list;
	}

}
