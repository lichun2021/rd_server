package com.hawk.game.item;

import java.util.List;

import com.hawk.game.util.WeightAble;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 铠甲属性
 * @author golden
 *
 */
public class ArmourAttrTemplate implements SplitEntity,WeightAble {

	private int effect;
	
	private int randMin;
	
	private int randMax;
	
	private int weight;
	
	public ArmourAttrTemplate() {
		
	}
	
	public ArmourAttrTemplate(int effect, int randMin, int randMax, int weight) {
		this.effect = effect;
		this.randMin = randMin;
		this.randMax = randMax;
		this.weight = weight;
	}
	
	public int getEffect() {
		return effect;
	}

	public void setEffect(int effect) {
		this.effect = effect;
	}

	public int getRandMin() {
		return randMin;
	}

	public void setRandMin(int randMin) {
		this.randMin = randMin;
	}

	public int getRandMax() {
		return randMax;
	}

	public void setRandMax(int randMax) {
		this.randMax = randMax;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public SplitEntity newInstance() {
		return new ArmourAttrTemplate();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(effect);
		dataList.add(randMin);
		dataList.add(randMax);
		dataList.add(weight);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		setEffect(dataArray.getInt());
		setRandMin(dataArray.getInt());
		setRandMax(dataArray.getInt());
		setWeight(dataArray.getInt());
	}
}
