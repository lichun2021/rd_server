package com.hawk.activity.type.impl.mechacoreexplore.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkRand;

@HawkConfigManager.XmlResource(file = "activity/core_explore/core_explore_item.xml")
public class CoreExploreItemCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 行数范围（行数1，行数2）
	 */
	private final String row;
	
	private final String specialItem;
	
	private int startLine, endLine;
	
	private List<String> rewardStrList = new ArrayList<>();
	private List<Integer> rewardWeightList = new ArrayList<>();
	
	public CoreExploreItemCfg(){
		this.id = 0;
		this.row = "";
		this.specialItem = "";
	}
	
	public boolean assemble() {
		String[] array = row.split(",");
		startLine = Integer.parseInt(array[0]);
		endLine = Integer.parseInt(array[1]);
		String[] arr = specialItem.split(",");
		for (String str : arr) {
			String[] itemStr = str.split("_");
			rewardStrList.add(String.format("%s_%s_%s", itemStr[0], itemStr[1], itemStr[2]));
			rewardWeightList.add(Integer.parseInt(itemStr[3]));
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public String getRow() {
		return row;
	}

	public String getSpecialItem() {
		return specialItem;
	}
	
	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public static String getRewardStr(int line) {
		ConfigIterator<CoreExploreItemCfg> iter = HawkConfigManager.getInstance().getConfigIterator(CoreExploreItemCfg.class);
		while (iter.hasNext()) {
			CoreExploreItemCfg cfg = iter.next();
			if (cfg.getStartLine() <= line && cfg.getEndLine() >= line) {
				return HawkRand.randomWeightObject(cfg.rewardStrList, cfg.rewardWeightList);
			}
		}
		
		CoreExploreItemCfg cfg = HawkConfigManager.getInstance().getConfigByIndex(CoreExploreItemCfg.class, 0);
		return HawkRand.randomWeightObject(cfg.rewardStrList, cfg.rewardWeightList);
	}
}
