package com.hawk.log;

/**
 * 日志源
 * 
 * @author hawk,lating
 *
 */
public enum Source {
	/**
	 * 系统操作
	 */
	SYSTEM_OPERATION,
	/**
	 * 用户操作
	 */
	USER_OPERATION,
	/**
	 * GM操作
	 */
	GM_OPERATION,
	/**
	 * 属性改变
	 */
	ATTR_CHANGE,
	/**
	 * 钻石消耗
	 */
	GOLD_RESUME,
	/**
	 * 游戏奖励
	 */
	SYS_REWARD,
	/**
	 * 道具移除
	 */
	TOOLS_REMOVE,
	/**
	 * 道具增加
	 */
	TOOLS_ADD,
	/**
	 * 装备增加
	 */
	EQUIP_ADD,
	/**
	 * 装备移除
	 */
	EQUIP_REMOVE,
	/**
	 * 邮件添加
	 */
	EMAIL_ADD,
	/**
	 * 邮件读取
	 */
	EMAIL_REMOVE,
	/**
	 * 新建建筑
	 */
	BUILDING_ADD,
	/**
	 * 建筑移动
	 */
	BUILDING_MOVE,
	/**
	 * 建筑升级
	 */
	BUILDING_UPGRADE,
	/**
	 * 世界坐标变换
	 */
	WORLD_POINT,
	/**
	 * 联盟操作
	 */
	GUILD_OPRATION,
	/**
	 * 聊天
	 */
	USER_CHAT,
	/**
	 * 天赋升级
	 */
	TALENT_UPGRADE,
	/**
	 * 天赋切换
	 */
	TALENT_CHANGE,
	/**
	 * 天赋洗点
	 */
	TALENT_CLEAR,
	/**
	 * 行军变化
	 */
	MARCH,
	/**
	 * 运维监控上报
	 */
	OPS_SOURCE,
	/**
	 * 科技操作
	 */
	TECHNOLOGY_OPERATION,
	/**
	 * 世界操作
	 */
	WORLD_ACTION,
	/**
	 * 未知源
	 */
	UNKNOWN_SOURCE,
	/**
	 * 邮件
	 */
	MAIL,
	/**
	 * 领地
	 */
	GUILD_MANOR, 
	/**
	 * 队列
	 */
	QUEUE,
	/**
	 * 装备/材料操作
	 */
	EQUIP,
	/**
	 * 军事学院
	 */
	COLLEGE,
	/**
	 * 远征科技操作
	 */
	CROSS_TECH_OPERATION,
	
	/**
	 * 泰能科技
	 */
	PLANT_SCIENCE_UPGRADE,
}
