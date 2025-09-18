package com.hawk.game.module.mechacore.entity;

import java.util.List;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 机甲核心模块的作用号
 * @author lating
 *
 */
public class MechaCoreModuleEffObject implements SplitEntity {
	
	private int attrId;
	
	private int effectType;
	
	private int effectValue;
	
	private int quality;
	
	public MechaCoreModuleEffObject() {
	}
	
	public MechaCoreModuleEffObject(int attrId, int effectType, int effectValue, int quality) {
		this.attrId = attrId;
		this.effectType = effectType;
		this.effectValue = effectValue;
		this.quality = quality;
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
	
	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	@Override
	public SplitEntity newInstance() {
		return new MechaCoreModuleEffObject();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(attrId);
		dataList.add(effectType);
		dataList.add(effectValue);
		dataList.add(quality);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		attrId = dataArray.getInt();
		effectType = dataArray.getInt();
		effectValue = dataArray.getInt();
		quality = dataArray.getInt();
	}
	
	@Override
	public String toString() {
		return attrId + "_" + effectType + "_" + effectValue;
	}
}
