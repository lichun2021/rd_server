package com.hawk.game.module.lianmengyqzz.march.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;


/**
 * 跨服活动时间配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/moon_war_nation_rankaward.xml")
public class YQZZCountryRankAwardCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int id;

	/** 预览时间 */
	private final int rankUpper;

	/** 开启时间 */
	private final int rankLower;

	private final String awardPack;
	
	private List<ItemInfo> rewardItems;

	public YQZZCountryRankAwardCfg() {
		id = 0;
		rankUpper = 0;
		rankLower = 0;
		awardPack = "";
	}
	
	public int getId() {
		return id;
	}
	
	public int getRankLower() {
		return rankLower;
	}
	
	public int getRankUpper() {
		return rankUpper;
	}
	
	
	public List<ItemInfo> getRewardList(){
		List<ItemInfo> ret = new ArrayList<>();
		for (ItemInfo item : rewardItems) {
			ret.add(item.clone());
		}
		return ret;
	}
	
	protected boolean assemble() {
		this.rewardItems = ItemInfo.valueListOf(this.awardPack);
		return true;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}
}
