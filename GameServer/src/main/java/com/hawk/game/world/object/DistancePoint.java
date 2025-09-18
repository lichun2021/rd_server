package com.hawk.game.world.object;

/**
 * 计算周围点到这个点的
 * @author jm
 *
 */
public class DistancePoint implements Comparable<DistancePoint>{
	/**
	 * 点
	 */
	Point point;	
	/**
	 * 长度
	 */
	private int distince;
	
	public DistancePoint(Point point, int[]centPos) {
		this.point = point;
		distince = (int)(Math.sqrt((point.getX() - centPos[0]) * (point.getX() - centPos[0]) + 
				(point.getY() - centPos[1]) * (point.getY() - centPos[1])) * 1000);		
	} 
	
	public int compareTo(DistancePoint o) {
		return distince - o.distince ;  
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}
	
}
