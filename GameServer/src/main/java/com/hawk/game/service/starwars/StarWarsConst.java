package com.hawk.game.service.starwars;

public interface StarWarsConst {
	
	
	/**
	 * 星球大战相关redis存储有效期(s)
	 */
	public static final int SW_EXPIRE_SECONDS = 30 * 24 * 3600;
	
	/**
	 * 世界是特殊区，用0标识.
	 */
	public static final int WORLD_PART = 0;
	
	public enum SWActivityState {
		/** 关闭 */
		CLOSE(-1),
		/** 未开启 */
		NOT_OPEN(1),
		/** 报名阶段 */
		SIGN_UP(2),
		/** 匹配阶段 */
		MATCH(3),
		/** 第一场等待开战 */
		WAR_ONE_WAIT(4),
		/** 第一场开战 */
		WAR_ONE_OPEN(5),
		/** 第一场结算 */
		WAR_ONE_CALC(6),
		/** 第二场等待开战 */
		WAR_TWO_WAIT(7),
		/** 第二场开战 */
		WAR_TWO_OPEN(8),
		/** 第二场结算 */
		WAR_TWO_CALC(9),
		/** 第三场等待开战 */
		WAR_THREE_WAIT(10),
		/** 第三场开战 */
		WAR_THREE_OPEN(11),
		/** 第三场结算 */
		WAR_THREE_CALC(12),
		/** 战斗结束 */
		END(13);

		int value;

		SWActivityState(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}
	
	public enum SWFightState{
		/** 未开启*/
		NOT_OPEN(0),
		/** 第一场可管理阶段*/
		FIRST_MANAGE(1),
		/** 第一场待开战*/
		FIRST_WAIT(2),
		/** 第一场开战中*/
		FIRST_OPEN(3),
		/** 第一场结算中*/
		FIRST_CALC(4),
		/** 第二场可管理阶段*/
		SECOND_MANGE(5),
		/** 第二场待开战*/
		SECOND_WAIT(6),
		/** 第二场开战中*/
		SECOND_OPEN(7),
		/** 第二场结算中*/
		SECOND_CALC(8),
		/** 第三场可管理阶段*/
		THIRD_MANGE(9),
		/** 第三场待开战*/
		THIRD_WAIT(10),
		/** 第三场开战中*/
		THIRD_OPEN(11),
		/** 第三场结算中*/
		THIRD_CALC(12),
		/** 已结束*/
		FINISH(13);
		int value;
		
		SWFightState(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}
	
	public enum SWRoomState{
		/** 未初始化*/
		NOT_INIT,
		/** 初始化完成*/
		INITED,
		/** 已结束*/
		CLOSE,
		/** 初始化失败*/
		INITED_FAILED
		;
		
	}
	
	public enum SWWarType{
		/** 第一场*/
		FIRST_WAR(1),
		/** 第二场*/
		SECOND_WAR(2),
		/** 第三场*/
		THIRD_WAR(3),
		;
		int value;
		
		SWWarType(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}
	
	public enum SWGroupType{
		/** 第一场比赛的参与者*/
		PARTNER(0),
		/** 第二场比赛的参与者(第一场的胜利者)*/
		FIRST_WINNER(1),
		/** 第三场比赛的参与者(第二场比赛的胜利者)*/
		SECOND_WINNER(2),
		/** 第三场比赛的胜利者*/
		THIRD_WINNER(3)
		;
		int value;
		
		SWGroupType(int value) {
			this.value = value;
		}
		
		public int getNumber() {
			return value;
		}
	}
}
