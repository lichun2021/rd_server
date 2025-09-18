package com.hawk.game.cfgElement;

import java.util.List;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 作用号配置
 * 
 * @author shadow
 *
 */
public class EffectObject implements SplitEntity {
	
	private int effectType;
	private int effectValue;
	private int showType;
	public EffectObject() {
		
	}
	
	public EffectObject(int effectType, int effectValue) {
		this.effectType = effectType;
		this.effectValue = effectValue;
	}

	public EffectObject copy() {
		return new EffectObject(effectType, effectValue);
	}
	
	/**
	 * 
	 * @return 效果类型
	 */
	public int getEffectType() {
		return effectType;
	}
	
	/**
	 * 
	 * @return 效果值
	 */
	public int getEffectValue() {
		return effectValue;
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

	@Override
	public SplitEntity newInstance() {
		return new EffectObject();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(effectType);
		dataList.add(effectValue);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(2);
		setEffectType(dataArray.getInt());
		setEffectValue(dataArray.getInt());
	}
	
	public String toString() {
		return effectType + "_" + effectValue;
	}

	public int getShowType() {
		return showType;
	}

	public void setShowType(int showType) {
		this.showType = showType;
	}
	
}
