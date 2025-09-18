package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 世界资源刷新配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_resource_refresh.xml")
public class WorldResourceRefreshCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int openServiceTimeLowerLimit;
	
	protected final int openServiceTimeUpLimit;
	
	protected final String resourceCommonNum;
	
	protected final String resourceSpecialNum;
	
	protected final String resourceCapitalNum;
	
	// 普通区块刷新
	private Map<Integer, Integer> refreshCommon;
	// 特殊区块刷新
	private Map<Integer, Integer> refreshSpecial;
	// 首都(黑土地)区块刷新
	private Map<Integer, Integer> refreshCapital;
	
	public WorldResourceRefreshCfg() {
		id = 0;
		openServiceTimeLowerLimit = 0;
		openServiceTimeUpLimit = 0;
		resourceCommonNum = "";
		resourceSpecialNum = "";
		resourceCapitalNum = "";
	}

	public int getId() {
		return id;
	}

	public int getOpenServiceTimeLowerLimit() {
		return openServiceTimeLowerLimit;
	}

	public int getOpenServiceTimeUpLimit() {
		return openServiceTimeUpLimit;
	}

	public String getResourceCommonNum() {
		return resourceCommonNum;
	}

	public String getResourceSpecialNum() {
		return resourceSpecialNum;
	}

	public String getResourceCaptialNum() {
		return resourceCapitalNum;
	}

	public Map<Integer, Integer> getRefreshCommon() {
		return refreshCommon;
	}

	public Map<Integer, Integer> getRefreshSpecial() {
		return refreshSpecial;
	}

	public Map<Integer, Integer> getRefreshCapital() {
		return refreshCapital;
	}
	
	@Override
	protected boolean assemble() {
		Map<Integer, Integer> refreshCommon = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(resourceCommonNum)) {
			String[] array = resourceCommonNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshCommon.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshCommon = refreshCommon;

		Map<Integer, Integer> refreshSpecial = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(resourceSpecialNum)) {
			String[] array = resourceSpecialNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshSpecial.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshSpecial = refreshSpecial;
		
		Map<Integer, Integer> refreshCapital = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(resourceCapitalNum)) {
			String[] array = resourceCapitalNum.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] kv = array[i].split("_");
				refreshCapital.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
			}
		}
		this.refreshCapital = refreshCapital;
		
		return true;
	}
}
