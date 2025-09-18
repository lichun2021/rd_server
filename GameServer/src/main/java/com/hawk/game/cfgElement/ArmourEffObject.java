package com.hawk.game.cfgElement;

import java.util.List;

import com.hawk.game.protocol.Armour.ArmourAttrType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 铠甲作用号
 * @author golden
 *
 */
public class ArmourEffObject implements SplitEntity {
	
	private int attrId;
	
	private int effectType;
	
	private int effectValue;

	/**
	 * 属性强化进度
	 */
	private int rate;
	
	/**
	 * 待替换的属性
	 */
	private int replaceAttrId;
	
	/**
	 * 是否突破
	 */
	private int breakthrough;
	
	public ArmourEffObject() {
		
	}
	
	public ArmourEffObject(int attrId, int effectType, int effectValue) {
		this.attrId = attrId;
		this.effectType = effectType;
		this.effectValue = effectValue;
	}
	
	public int getAttrId() {
		return attrId;
	}

	public int getEffectType() {
		return effectType;
	}

	public int getEffectValue() {
		return effectValue;
	}

	public void setAttrId(int attrId) {
		this.attrId = attrId;
	}

	public void setEffectType(int effectType) {
		this.effectType = effectType;
	}

	public void setEffectValue(int effectValue) {
		this.effectValue = effectValue;
	}

	public EffType getType() {
		return EffType.valueOf(effectType);
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getReplaceAttrId() {
		return replaceAttrId;
	}

	public void setReplaceAttrId(int replaceAttrId) {
		this.replaceAttrId = replaceAttrId;
	}

	public int getBreakthrough() {
		return breakthrough;
	}

	public void setBreakthrough(int breakthrough) {
		this.breakthrough = breakthrough;
	}

	@Override
	public SplitEntity newInstance() {
		return new ArmourEffObject();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(attrId);
		dataList.add(effectType);
		dataList.add(effectValue);
		dataList.add(rate);
		dataList.add(replaceAttrId);	
		dataList.add(breakthrough);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(6);
		attrId = dataArray.getInt();
		effectType = dataArray.getInt();
		effectValue = dataArray.getInt();
		rate = dataArray.getInt();
		replaceAttrId = dataArray.getInt();
		breakthrough = dataArray.getInt();
	}
	
	@Override
	public String toString() {
		if (effectType == ArmourAttrType.STAR_EXTR_VALUE) {
			return attrId + "_" + effectType + "_" + effectValue + "_" + rate + "_" + replaceAttrId + "_" + breakthrough;
		} else {
			return attrId + "_" + effectType + "_" + effectValue;
		}
	}
}
