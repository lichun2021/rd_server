package com.hawk.activity.type.impl.mechacoreexplore.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/core_explore/core_explore_tech.xml")
public class CoreExploreTechCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 科技类型，分别对应：
	 *   - 1.获得矿石数量*x。 新矿石数量=原数量*（1+x1%+x2%+...+xn%），向下取整
		 - 2.免费矿镐恢复时间减少 x%。新时间=原时间*（1-x1%-x2%-x3%-...-xn%），精确到秒，向上取整
		 - 3.免费矿镐上限增加
		 - 4.获得n个道具 （可配置道具id，数量，包括成长材料和炸弹钻机）
		 - 5.解锁兑换商店
		 - 6.兑换商店价格降低、（读取新的一列价格配置）
		 - 7.兑换商店商品兑换次数增加n次。（读取新的一列限购次数配置）
	 */
	private final int techType;
	
	private final String techEffect;
	
	private final String conditions;
	
	private final String techCost;
	
	private List<Integer> conditionList = new ArrayList<>();
	
	public CoreExploreTechCfg(){
		this.id = 0;
		this.techType = 0;
		this.techEffect = "";
		this.conditions = "";
		this.techCost = "";
	}
	
	public boolean assemble() {
		conditionList = SerializeHelper.stringToList(Integer.class, conditions, ",");
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getTechType() {
		return techType;
	}

	public String getTechEffect() {
		return techEffect;
	}

	public String getConditions() {
		return conditions;
	}

	public String getTechCost() {
		return techCost;
	}
	
	public List<Integer> getConditionList() {
		return conditionList;
	}

}
