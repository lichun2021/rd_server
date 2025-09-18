package com.hawk.game.config;

import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;

/**
 * 至尊vip手动激活档位配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/vip_temporary_super.xml")
public class VipTemprorySuperCfg extends HawkConfigBase {
	/**
	 * 至尊vip等级
	 */
	@Id
	protected final int id;
	/**
	 * 消耗
	 */
	protected final String activationCost;
	/**
	 *  时长，单位：天
	 */
	protected final int activationTimes;
	
	private List<ItemInfo> activationCostItems;

	public VipTemprorySuperCfg() {
		id = 0;
		activationCost = "";
		activationTimes = 0;
	}
	
	public int getId() {
		return id;
	}

	public String getActivationCost() {
		return activationCost;
	}

	public int getActivationTimes() {
		return activationTimes;
	}

	public List<ItemInfo> getActivationCostItems() {
		return activationCostItems;
	}
	
	@Override
	protected boolean assemble() {
		activationCostItems = ImmutableList.copyOf(ItemInfo.valueListOf(activationCost));
		return true;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}

}
