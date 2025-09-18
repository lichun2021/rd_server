package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 战旗等级奖励配置(战旗二期)
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/war_flag_level.xml")
public class WarFlagLevelCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	protected final String resource;
	
	protected final int award;
	
	/**
	 * 战地旗帜产出
	 */
	private List<ItemInfo> resourceInfo;
	
	public WarFlagLevelCfg() {
		this.id = 0;
		this.resource = "";
		this.award = 4600308;
	}

	public int getId() {
		return id;
	}

	public String getResource() {
		return resource;
	}

	public int getAward() {
		return award;
	}
	
	public List<ItemInfo> getResourceInfo() {
		return resourceInfo;
	}

	public void setResourceInfo(List<ItemInfo> resourceInfo) {
		this.resourceInfo = resourceInfo;
	}

	@Override
	protected boolean assemble() {
		
		List<ItemInfo> resourceInfo = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(resource)) {
			resourceInfo = ItemInfo.valueListOf(resource);
		}
		this.resourceInfo = resourceInfo;
		return true;
	}
}
