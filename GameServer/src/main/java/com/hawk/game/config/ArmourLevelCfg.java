package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 铠甲等级配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour_level.xml")
public class ArmourLevelCfg extends HawkConfigBase {

	/**
	 * 等级
	 */
	@Id
	protected final int level;

	/**
	 * 强化消耗
	 */
	protected final String consume;

	/**
	 * 分解获得
	 */
	protected final String resolve;
	
	/**
	 * 战力
	 */
	protected final int armourCombat;
	
	/**
	 * 基础属性成长值
	 */
	protected final int levelGrowUp;
	
	/**
	 * 战力属性计算加成
	 */
	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 强化消耗
	 */
	protected List<ItemInfo> consumItem;
	
	/**
	 * 分解获得
	 */
	protected List<Integer> resolveAwards;
	
	public ArmourLevelCfg() {
		level = 0;
		consume = "";
		resolve = "";
		armourCombat = 0;
		levelGrowUp = 0;
		atkAttr = "";
		hpAttr = "";
	}

	public int getLevel() {
		return level;
	}

	public String getConsume() {
		return consume;
	}

	public int getLevelGrowUp() {
		return levelGrowUp;
	}

	public int getArmourCombat() {
		return armourCombat;
	}
	
	public List<ItemInfo> getConsumItem() {
		return consumItem;
	}
	
	public List<Integer> getResolveAwards() {
		return resolveAwards;
	}

	@Override
	protected boolean assemble() {
		consumItem = ItemInfo.valueListOf(consume);
		
		List<Integer> resolveAwards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(resolve)) {
			Arrays.asList(resolve.split(",")).forEach(award -> {
				resolveAwards.add(Integer.parseInt(award));
			});
		}
		this.resolveAwards = resolveAwards;
		
		return true;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
