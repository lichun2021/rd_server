package com.hawk.game.util;

import org.hawk.os.HawkOSOperator;

/**
 * 数学功能计算
 * 
 * 1,线段与线段相交计算
 * @author julia
 *
 */
public class AlgorithmUtil {

	/**
	 * 获取线段交点
	 * @param a	线段起点
	 * @param b 线段终点
	 * @param c 线段起点
	 * @param d 线段终点
	 * @return
	 */
	public static AlgorithmPoint getIntersection(AlgorithmPoint a, AlgorithmPoint b, AlgorithmPoint c, AlgorithmPoint d) {
		AlgorithmPoint intersection = new AlgorithmPoint(0d, 0d);
		double k1 = Math.abs(b.getY() - a.getY()) + Math.abs(b.getX() - a.getX());
		double k2 = Math.abs(d.getY() - c.getY()) + Math.abs(d.getX() - c.getX());
		if (k1 + k2 == 0) {
			return null;
		}

		if (Math.abs(b.getY() - a.getY()) + Math.abs(b.getX() - a.getX()) == 0) {
			return null;
		}

		if (Math.abs(d.getY() - c.getY()) + Math.abs(d.getX() - c.getX()) == 0) {
			return null;
		}

		if ((b.getY() - a.getY()) * (c.getX() - d.getX()) - (b.getX() - a.getX()) * (c.getY() - d.getY()) == 0) {
			return null;
		}

		double matrixLeft = (b.getX() - a.getX()) * (c.getX() - d.getX()) * (c.getY() - a.getY()) -
				c.getX() * (b.getX() - a.getX()) * (c.getY() - d.getY()) +
				a.getX() * (b.getY() - a.getY()) * (c.getX() - d.getX());

		double matrixRight = (b.getY() - a.getY()) * (c.getX() - d.getX()) - (b.getX() - a.getX()) * (c.getY() - d.getY());

		intersection.x = matrixLeft / matrixRight;

		double matrixLeft2 = (b.getY() - a.getY()) * (c.getY() - d.getY()) * (c.getX() - a.getX()) -
				c.getY() * (b.getY() - a.getY()) * (c.getX() - d.getX()) +
				a.getY() * (b.getX() - a.getX()) * (c.getY() - d.getY());

		double matrixRight2 = (b.getX() - a.getX()) * (c.getY() - d.getY()) - (b.getY() - a.getY()) * (c.getX() - d.getX());
		intersection.y = matrixLeft2 / matrixRight2;

		if ((intersection.getX() - a.getX()) * (intersection.getX() - b.getX()) <= 0
				&& (intersection.getX() - c.getX()) * (intersection.getX() - d.getX()) <= 0
				&& (intersection.getY() - a.getY()) * (intersection.getY() - b.getY()) <= 0
				&& (intersection.getY() - c.getY()) * (intersection.getY() - d.getY()) <= 0) {
			return intersection;//相交
		} else {
			return null;//相交，但不在线段上
		}

	}

	/**
	 * 计算一个点到一个直线的距离
	 * @param centerX
	 * @param centerY
	 * @param origionId
	 * @param terminalId
	 * @param terminalY 
	 * @param terminalX 
	 * @return
	 */
	public static double getDisPointToLine(int centerX, int centerY, int origionX, int origionY, int terminalX, int terminalY) {
		double aLen = lineDistance(origionX, origionY, terminalX, terminalY);
		double bLen = lineDistance(origionX, origionY, centerX, centerY);
		double cLen = lineDistance(terminalX, terminalY, centerX, centerY);

		// 点在线段上
		if (HawkOSOperator.isZero(cLen + bLen - aLen)) {
			return 0;
		}

		// 钝角三角形, 投影在起始点的延长线上
		if (cLen * cLen >= aLen * aLen + bLen * bLen) {
			return bLen;
		}

		// 直角三角形或钝角三角形, 投影在终止点的延长线上
		if (bLen * bLen >= aLen * aLen + cLen * cLen) {
			return cLen;
		}
		
		// 锐角三角形, 求高
		
		//半周长
		double p = (aLen + bLen + cLen) / 2;
		
		// 海伦公式求面积
		double s = Math.sqrt(p * (p - aLen) * (p - bLen) * (p - cLen)); 
		
		// 三角形面积公式求高
		return 2 * s / aLen;
	}

	/**
	 * 求两点之间的距离
	 * @param x1
	 * @param y1
	 * @param x3
	 * @param y2
	 * @return
	 */
	public static double lineDistance(int x1, int y1, int x2, int y2) {
		double lineLen = 0;
		lineLen = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		return lineLen;
	}
}
