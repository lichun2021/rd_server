package com.hawk.game.service.cyborgWar;

public interface CWConst {
	/** redis过期时间-14天 */
	public static final int EXPIRE_TIME_14 = 14 * 24 * 3600;
	
	/** redis过期时间-30天 */
	public static final int EXPIRE_TIME_30 = 30 * 24 * 3600;
	
	/** redis过期时间-90天 */
	public static final int EXPIRE_TIME_90 = 90 * 24 * 3600;
	
	/** redis过期时间-180天 */
	public static final int EXPIRE_TIME_180 = 180 * 24 * 3600;
	
	/** 星级积分偏移量*/
	public static final long STAR_SCORE_OFFSET = 100000000L;
	
	public enum CWMemverMangeType {
		/** 移除*/
		KICK, 
		/** 插入*/
		JOIN;
	}

	public enum CWActivityState {
		/** 关闭 */
		CLOSE(-1),
		/** 未开启 */
		NOT_OPEN(0),
		/** 未开启 */
		OPEN(1),
		/** 报名阶段 */
		SIGN(2),
		/** 匹配阶段 */
		MATCH(3),
		/** 战斗阶段 */
		WAR(4);

		int value;

		CWActivityState(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}

	public enum FightState {
		/** 未开启 */
		NOT_OPEN,
		/** 开启中 */
		OPEN,
		/** 已结束 */
		FINISH,
		/** 已发奖 */
		AWARDED;
	}

	public enum RoomState {
		/** 未初始化 */
		NOT_INIT,
		/** 初始化完成 */
		INITED,
		/** 已结束 */
		CLOSE,
		/** 初始化失败 */
		INITED_FAILED;
	}

	public enum CLWActivityState {
		/** 关闭 */
		CLOSE(-1),
		/** 未开启 */
		NOT_OPEN(0),
		/** 预热阶段*/
		SHOW(1),
		/** 开启 */
		OPEN(2),
		/** 结束展示阶段 */
		END(3);

		int value;

		CLWActivityState(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}
	
	/**
	 * 赛博联赛段位变化原因
	 * @author z
	 *
	 */
	public enum CLWStarReason {
		/** 赛季首期初始化 */
		BEGIN_INIT(1),
		/** 赛季中期初始化 */
		MID_INIT(2),
		/** 赛博战斗 */
		WAR(3),
		/** 消极出战扣星 */
		NO_SIGN(4),
		/** 匹配失败加星 */
		MATCH_FAILED(5);
		int value;

		CLWStarReason(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}
}
