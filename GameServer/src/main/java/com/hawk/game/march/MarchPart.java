package com.hawk.game.march;

import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.util.AlgorithmPoint;

public class MarchPart {
	// 起点
	AlgorithmPoint startPoint;
	// 终点
	AlgorithmPoint endPoint;
	// 是否减速段
	boolean isSlowDown = false;
	// 比例
	double distancePer = 0.0;
	// 长度
	double distance = 0.0;
	
	/**
	 * 构造
	 * 
	 * @param startPoint
	 * @param endPoint
	 * @param isSlowDown
	 */
	public MarchPart(AlgorithmPoint startPoint, AlgorithmPoint endPoint, boolean isSlowDown, boolean needFixDis) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.isSlowDown = isSlowDown;
		double dis = startPoint.distanceTo(this.endPoint);
		if(needFixDis){
			if (dis > 1 ) {
				dis = (dis - WorldMarchConstProperty.getInstance().getDistanceSubtractionParam()) / Math.sqrt(2);
			}
		} else {
			dis = dis / Math.sqrt(2);
		}
		double slowDownScale = WorldMarchConstProperty.getInstance().getWorldMarchCoreRangeTime();
		double speedScale = 1.0f;
		if (this.isSlowDown) {
			speedScale = slowDownScale;
		}
		this.distancePer = dis * speedScale;
		this.distance = dis;
	}

	public double getDistancePer() {
		return distancePer;
	}

	public void setDistancePer(double distancePer) {
		this.distancePer = distancePer;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public AlgorithmPoint getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(AlgorithmPoint startPoint) {
		this.startPoint = startPoint;
	}

	public AlgorithmPoint getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(AlgorithmPoint endPoint) {
		this.endPoint = endPoint;
	}

	public boolean isSlowDown() {
		return isSlowDown;
	}

	public void setSlowDown(boolean isSlowDown) {
		this.isSlowDown = isSlowDown;
	}
	
}
