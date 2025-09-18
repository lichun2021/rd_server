package com.hawk.activity.type.impl.appointget.cfg;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkRandObj;

public class AppointGetRandObj implements HawkRandObj {

	private int index;
	private int val;
	private int weight;

	@Override
	public int getWeight() {
		// TODO Auto-generated method stub
		return weight;
	}

	public int getVal() {
		return val;
	}

	public void setVal(int val) {
		this.val = val;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public static AppointGetRandObj create(String attr) {
		String[] attrs = attr.split("_");

		AppointGetRandObj obj = new AppointGetRandObj();
		obj.index = NumberUtils.toInt(attrs[0]);
		obj.val = NumberUtils.toInt(attrs[1]);
		obj.weight = NumberUtils.toInt(attrs[2]);

		return obj;
	}

}
