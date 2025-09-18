package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.serialize.string.SerializeHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * 星能探索配置
 * 
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/star_explore_upgrade.xml")
public class ArmourStarExploreUpgradeCfg extends HawkConfigBase {
	@Id
	private final int id;

	private final int starId;
	private final int level;
	private final String consume;
	private final int power;
	
	
	 /**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;

	private static Map<Integer, Map<Integer, ArmourStarExploreUpgradeCfg>> levelMap = new HashMap<>();

	public ArmourStarExploreUpgradeCfg() {
		id = 0;
		starId = 0;
		level = 0;
		consume = "";
		power = 0;
		
		atkAttr = "";
		hpAttr = "";
	}

	public static boolean doAssemble() {
		Map<Integer, Map<Integer, ArmourStarExploreUpgradeCfg>> tmp = new HashMap<>();
		ConfigIterator<ArmourStarExploreUpgradeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ArmourStarExploreUpgradeCfg.class);
		for(ArmourStarExploreUpgradeCfg cfg : iterator){
			if(!tmp.containsKey(cfg.starId)){
				tmp.put(cfg.starId, new HashMap<>());
			}
			tmp.get(cfg.starId).put(cfg.level, cfg);
		}
		levelMap = tmp;
		return true;
	}

	public int getId() {
		return id;
	}

	public int getStarId() {
		return starId;
	}

	public int getLevel() {
		return level;
	}

	public String getConsume() {
		return consume;
	}

	public int getPower() {
		return power;
	}
	
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }
	

	public static ArmourStarExploreUpgradeCfg getLevelCfg(int starId, int level){
		if(!levelMap.containsKey(starId)){
			return null;
		}
		return levelMap.get(starId).get(level);
	}
}
