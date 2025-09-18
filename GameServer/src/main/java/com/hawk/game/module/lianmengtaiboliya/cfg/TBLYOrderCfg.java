package com.hawk.game.module.lianmengtaiboliya.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tbly_order.xml")
public class TBLYOrderCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 开启消耗
	 */
	protected final int powerCost;
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
	protected final int number;

	/**
	 * 号令作用号属性
	 */
	private List<EffectObject> effectList;

	private List<Integer> exclusionList;

	public TBLYOrderCfg() {
		this.id = 0;
		this.powerCost = 0;
		this.effectTime = 0;
		this.coolingTime = 0;
		this.orderEffect = "";
		exclusions = "";
		number = 30;
	}

	public int getId() {
		return id;
	}

	public int getPowerCost() {
		return powerCost;
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
		exclusionList = SerializeHelper.stringToList(Integer.class, exclusions, SerializeHelper.BETWEEN_ITEMS);
		return true;
	}

	public String getOrderEffect() {
		return orderEffect;
	}

	public String getExclusions() {
		return exclusions;
	}

	public int getNumber() {
		return number;
	}

	public void setEffectList(List<EffectObject> effectList) {
		this.effectList = effectList;
	}

	public void setExclusionList(List<Integer> exclusionList) {
		this.exclusionList = exclusionList;
	}

}
