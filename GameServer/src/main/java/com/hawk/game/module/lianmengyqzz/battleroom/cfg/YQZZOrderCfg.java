package com.hawk.game.module.lianmengyqzz.battleroom.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderType;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/moon_war_order.xml")
public class YQZZOrderCfg extends HawkConfigBase {
	@Id
	protected final int id;
	protected final int techId;//
	protected final int techCost;
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

	protected final int p1;
	protected final int p2;

	private Map<EffType, Integer> effectList;
	private PBYQZZOrderType orderType;

	public YQZZOrderCfg() {
		this.id = 0;
		this.effectTime = 0;
		this.coolingTime = 0;
		this.orderEffect = "";
		this.p1 = 0;
		this.p2 = 0;
		this.techId = 0;
		this.techCost = 0;
	}

	public int getId() {
		return id;
	}

	public int getEffectTime() {
		return effectTime;
	}

	public int getCoolingTime() {
		return coolingTime;
	}

	public int getEffectVal(EffType effType) {
		return effectList.getOrDefault(effType, 0);
	}

	@Override
	protected boolean assemble() {
		effectList = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(orderEffect)) {
			String[] array = orderEffect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				effectList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}
		effectList = ImmutableMap.copyOf(effectList);
		orderType = PBYQZZOrderType.valueOf(id);

		return true;
	}

	public String getOrderEffect() {
		return orderEffect;
	}

	public int getP1() {
		return p1;
	}

	public PBYQZZOrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(PBYQZZOrderType orderType) {
		this.orderType = orderType;
	}

	public int getTechId() {
		return techId;
	}

	public int getTechCost() {
		return techCost;
	}

	public int getP2() {
		return p2;
	}

}
