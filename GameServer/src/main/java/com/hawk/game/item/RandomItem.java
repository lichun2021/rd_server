package com.hawk.game.item;

import com.hawk.game.util.WeightAble;

public class RandomItem implements WeightAble {

	int type;
	
	int weight;
	
	public RandomItem(int type, int weight) {
		this.type = type;
		this.weight = weight;
	}

	public int getType() {
		return type;
	}

	@Override
	public int getWeight() {
		return weight;
	}	
}
