package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/dyzz_order.xml")
public class DYZZOrderCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 开启消耗
	 */
	protected final String powerCost;// ="40,50,60,70,80";
	/** 可购买次数*/
	protected final int campCount;
	/**
	 * 生效时间
	 */
	protected final int effectTime;
	/**
	 * 冷却时间
	 */
	protected final int coolingTime;
	/**
	 * 号令作用号
	 */
	protected final String orderEffect;

	/**
	 * 互斥号令
	 */
	protected final String exclusions;

	protected final String p1;
	protected final String desc;
	/**
	 * 号令作用号属性
	 */
	private List<EffectObject> effectList;

	private List<Integer> exclusionList;

	private ImmutableList<Integer> powerCostList;

	public DYZZOrderCfg() {
		this.id = 0;
		this.powerCost = "40,50,60,70,80";
		this.effectTime = 0;
		this.coolingTime = 0;
		this.orderEffect = "";
		exclusions = "";
		campCount = 0;
		p1 = "";
		desc = "";
	}

	public int getId() {
		return id;
	}

	public int getPowerCost(int index) {
		index = Math.min(powerCostList.size() - 1, index);
		return powerCostList.get(index);
	}

	public int getEffectTime() {
		return effectTime;
	}

	public int getCoolingTime() {
		return coolingTime;
	}

	/**
	 * @return 效果列表
	 */
	public List<EffectObject> getEffectList() {
		return Collections.unmodifiableList(effectList);
	}

	public List<Integer> getExclusionList() {
		return exclusionList;
	}

	public int getCampCount() {
		return campCount;
	}

	@Override
	protected boolean assemble() {
		effectList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(orderEffect)) {
			String[] array = orderEffect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				EffectObject effect = new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
				effectList.add(effect);
			}
		}
		effectList = ImmutableList.copyOf(effectList);
		exclusionList = ImmutableList.copyOf(SerializeHelper.stringToList(Integer.class, exclusions, SerializeHelper.BETWEEN_ITEMS));
		powerCostList = ImmutableList.copyOf(SerializeHelper.stringToList(Integer.class, powerCost, SerializeHelper.BETWEEN_ITEMS));

		return true;
	}

	public String getP1() {
		return p1;
	}

	public String getDesc() {
		return desc;
	}

}
