package com.hawk.game.module.spacemecha;

/**
 * 星甲召唤功能常量
 * 
 * @author lating
 *
 */
public class MechaSpaceConst {
	
	/**
	 * 联盟官员号召盟友
	 */
	public static final String GUILD_OFFICER_NOTICE = "spaceMechaNotice";
	/**
	 * 攻击据点奖励次数
	 */
	public static final String ATK_STRONG_AWARD = "atkStrongAward";
	/**
	 * 攻击据点次数
	 */
	public static final String ATK_STRONG_TIMES = "atkStrongTimes";
	/**
	 * 采集奖励次数
	 */
	public static final String PERSONAL_BOX_AWARD_TOTAL = "spaceMechaBoxAwardTotal";
	/**
	 * 进攻据点奖励次数
	 */
	public static final String PERSONAL_STRONGHOD_AWARD_TOTAL = "atkStrongAwardTermTotal";
	/**
	 * 子舱防守奖励次数
	 */
	public static final String PERSONAL_SUBSPACE_AWARD_TOTAL = "subSpaceDefAwardTotal";
	/**
	 * 主舱防守成功参与奖励次数
	 */
	public static final String PERSONAL_MAINSPACE_PARTI_AWARD_TOTAL = "mainSpaceDefPartyAwardTotal";
	/**
	 * 主舱防守成功全员奖励次数
	 */
	public static final String PERSONAL_MAINSPACE_ALL_AWARD_TOTAL = "mainSpaceDefAwardTotal";
	
	
	/**
	 * 活动控制状态变量
	 */
	public static final int ACTIVITY_STATE_INIT    = -1; //起服初始状态  
	public static final int ACTIVITY_STATE_OPEN     = 0; //活动开启状态
	public static final int ACTIVITY_STATE_CLOSE    = 1; //活动关闭状态
	public static final int ACTIVITY_STATE_SHUTDOWN = 2; //停服处理状态

	/**
	 * 舱体编号
	 */
	public static final class SpacePointIndex {
		public static final int MAIN_SPACE    = 0; // 主舱体   
		public static final int SUB_SPACE_1   = 1; // 1号子舱体
		public static final int SUB_SPACE_2   = 2; // 2号子舱体
	}
	
	/**
	 * 星甲召唤相关建筑占地格数
	 */
	public static final class SpaceMechaGrid {
		public static final int SPACE_MAIN_GRID   = 2; // 主舱体占地格数
		public static final int SPACE_SLAVE_GRID  = 1; // 子舱占地格数
		public static final int MONSTER_GRID      = 1; // 普通怪物点占地格数
		public static final int STRONG_HOLD_GRID  = 2; // 据点占地格数
		public static final int MECHA_BOX         = 1; // 宝箱占地格数
	}

}
