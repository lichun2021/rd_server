package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.RandomItem;
import com.hawk.game.util.RandomUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 铠甲突破配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour_breakthrough.xml")
public class ArmourBreakthroughCfg extends HawkConfigBase {

	/**
	 * 品质
	 */
	@Id
	protected final int quality;
	
	/**
	 * 突破消耗
	 */
	protected final String consume;
	
	/**
	 * 品质等级上限
	 */
	protected final int levelLimit;
	
	/**
	 * 分解获取
	 */
	protected final String resolve;

	/**
	 * 战力
	 */
	protected final int armourCombat;

	/**
	 * 基础属性成长值
	 */
	protected final int breakGrowUp;
	
	/**
	 * 铠甲套装战力
	 */
	protected final String armourSuitCombat;

	/**
	 * 初始属性权重
	 */
	protected final String initAttrWeight;
	
	/**
	 * 星级限制
	 */
	protected final int starLimit;
	
	/**
	 * 战力属性计算加成
	 */
	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 突破消耗
	 */
	protected List<ItemInfo> consumItem;
	
	/**
	 * 分解获得
	 */
	protected List<Integer> resolveAwards;
	
	/**
	 * 铠甲套装战力列表
	 */
	protected List<Integer> armourSuitCombatList;
	
	/**
	 * 铠甲属性随机权重列表
	 */
	protected List<List<RandomItem>>initAttrWeightList;
	
	public ArmourBreakthroughCfg() {
		quality = 0;
		consume = "";
		levelLimit = 0;
		resolve = "";
		armourCombat = 0;
		breakGrowUp = 0;
		armourSuitCombat = "";
		initAttrWeight = "";
		starLimit = 0;
		atkAttr = "";
		hpAttr = "";
	}

	public int getQuality() {
		return quality;
	}

	public String getConsume() {
		return consume;
	}

	public int getLevelLimit() {
		return levelLimit;
	}

	public int getBreakGrowUp() {
		return breakGrowUp;
	}

	public List<ItemInfo> getConsumItem() {
		return consumItem;
	}

	public List<Integer> getResolveAwards() {
		return resolveAwards;
	}
	
	public int getArmourCombat() {
		return armourCombat;
	}

	/**
	 * 获取铠甲套装战力
	 */
	public int getSuitCombination(int index) {
		return armourSuitCombatList.get(index);
	}
	
	/**
	 * 随机属性品质列表
	 */
	public List<Integer> randomAttrQualityList() {
		List<Integer> qualityList = new ArrayList<>();
		for (List<RandomItem> initAttrWeight : initAttrWeightList) {
			RandomItem attr = RandomUtil.random(initAttrWeight);
			qualityList.add(attr.getType());
		}
		return qualityList;
	}
	
	/**
	 * 获取初始额外属性条目数量
	 * @return
	 */
	public int getExtraAttrCount() {
		return initAttrWeightList.size();
	}
	
	public int getStarLimit() {
		return starLimit;
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
		
		List<Integer> armourSuitCombatList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(armourSuitCombat)) {
			Arrays.asList(armourSuitCombat.split("_")).forEach(power -> {
				armourSuitCombatList.add(Integer.valueOf(power));
			});
		}
		this.armourSuitCombatList = armourSuitCombatList;

		List<List<RandomItem>>initAttrWeightList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(initAttrWeight)) {
			String[] initAttr = initAttrWeight.split(";");
			
			for (int i = 0; i < initAttr.length; i++) {
				
				List<RandomItem> randObjList = new ArrayList<>();
				
				String[] attrs = initAttr[i].split(",");
				for (int j = 0; j < attrs.length; j++) {
					String[] attr = attrs[j].split("_");
					RandomItem randObj = new RandomItem(Integer.parseInt(attr[0]), Integer.parseInt(attr[1]));
					randObjList.add(randObj);
				}
				initAttrWeightList.add(randObjList);
			}
		}
		this.initAttrWeightList = initAttrWeightList;
		return true;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
