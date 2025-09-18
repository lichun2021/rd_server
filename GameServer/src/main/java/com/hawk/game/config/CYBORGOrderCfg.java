package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrderType;
import com.hawk.game.util.GameUtil;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_order.xml")
public class CYBORGOrderCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 生效时间
	 */
	protected final int effectTime;
	/** 技能绑定的建筑*/
	protected final String position; // 81_18
	protected final int type;
	/**
	 * 冷却时间
	 */
	protected final int coolingTime;
	/**
	 * 号令作用号
	 */
	protected final String orderEffect;

	protected final int p1;
	
	/**
	 * 号令作用号属性
	 */
	private int buildPointId;
	private List<EffectObject> effectList;
	private PBCYBORGOrderType orderType;

	public CYBORGOrderCfg() {
		this.id = 0;
		this.effectTime = 0;
		this.coolingTime = 0;
		this.orderEffect = "";
		this.position = "0_0";
		this.type = 0;
		this.p1 = 3;
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

	/**
	 * @return 效果列表
	 */
	public List<EffectObject> getEffectList() {
		return Collections.unmodifiableList(effectList);
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
		orderType = PBCYBORGOrderType.valueOf(type);

		{
			String[] x_y = position.split("_");
			int[] pos = new int[2];
			pos[0] = NumberUtils.toInt(x_y[0]);
			pos[1] = NumberUtils.toInt(x_y[1]);
			buildPointId = GameUtil.combineXAndY(pos[0], pos[1]);
		}
		return true;
	}

	public int getBuildPointId() {
		return buildPointId;
	}

	public void setBuildPointId(int buildPointId) {
		this.buildPointId = buildPointId;
	}

	public PBCYBORGOrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(PBCYBORGOrderType orderType) {
		this.orderType = orderType;
	}

	public String getPosition() {
		return position;
	}

	public int getType() {
		return type;
	}

	public String getOrderEffect() {
		return orderEffect;
	}

	public void setEffectList(List<EffectObject> effectList) {
		this.effectList = effectList;
	}

	public int getP1() {
		return p1;
	}

}
