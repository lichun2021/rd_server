package com.hawk.activity.type.impl.mechacoreexplore;

public class CoreExploreConst {
	
	/** 科技效果1： 获得矿石数量*x。 新矿石数量=原数量*（1+x1%+x2%+...+xn%），向下取整 */
	public static final int TECH_EFF_1 = 1;
	
	/** 科技效果2： 免费矿镐恢复时间减少 x%。新时间=原时间*（1-x1%-x2%-x3%-...-xn%），精确到秒，向上取整 */
	public static final int TECH_EFF_2 = 2;
	
	/** 科技效果3： 免费矿镐上限增加 */
	public static final int TECH_EFF_3 = 3;
	
	/** 科技效果4： 获得n个道具 （可配置道具id，数量，包括成长材料和炸弹钻机 */
	public static final int TECH_EFF_4 = 4;
	
	/** 科技效果5： 解锁兑换商店 */
	public static final int TECH_EFF_5 = 5;
	
	/** 科技效果6： 兑换商店价格降低、（读取新的一列价格配置） */
	public static final int TECH_EFF_6 = 6;
	
	/** 科技效果7： 兑换商店商品兑换次数增加n次。（读取新的一列限购次数配置） */
	public static final int TECH_EFF_7 = 7;
	
	/** 科技效果8： 解锁一批新商品（在core_explore_shop表里新增一列 techId，配了techId的商品需要解锁对应id的科技才能开放购买） */
	public static final int TECH_EFF_8 = 8;
}
