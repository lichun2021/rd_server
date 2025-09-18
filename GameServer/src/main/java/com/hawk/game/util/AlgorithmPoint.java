package com.hawk.game.util;

import org.hawk.os.HawkOSOperator;

/**
 * 点结构
 * @author julia
 */
public class AlgorithmPoint {
	double x;
	double y;

	public AlgorithmPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	// 反解
	public AlgorithmPoint(String value) {
		if (HawkOSOperator.isEmptyString(value)) {
			return;
		}
		String[] items = value.split("_");
		x = Double.parseDouble(items[0]);
		y = Double.parseDouble(items[1]);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public int getId() {
		return GameUtil.combineXAndY((int) x, (int) y);
	}

	public double distanceTo(AlgorithmPoint point) {
		double a = Math.pow(Math.abs(x - point.getX()), 2);
		double b = Math.pow(Math.abs(y - point.getY()), 2);
		double distance = Math.sqrt(a + b);
		return distance;
	}
	
	@Override
	public String toString() {
		return String.format("%f_%f", x, y);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AlgorithmPoint){
			AlgorithmPoint p = (AlgorithmPoint) obj;
			return Double.doubleToLongBits(p.x) == Double.doubleToLongBits(this.x) && Double.doubleToLongBits(p.y) == Double.doubleToLongBits(this.y);
		}
		return false;
	}
}
