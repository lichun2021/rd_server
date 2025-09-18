package com.hawk.game.module.lianmengyqzz.march.service;


public class YQZZConst {


	public enum YQZZSeasonState{
		HIDDEN(0),
		OPEN(1),
		REWARD(2),
		;
		private int value;

		YQZZSeasonState(int value){
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static YQZZSeasonState valueOf(int value){
			switch (value){
				case 0:return HIDDEN;
				case 1:return OPEN;
				case 2:return REWARD;
				default:return null;
			}
		}
	}
	
	/**
	 * 活动状态
	 * @author che
	 *
	 */
	public enum YQZZActivityState{
		HIDDEN(0),
		START_SHOW(100),
		MATCH(200),
		BATTLE(300),
		REWARD(310),
		END_SHOW(400);
		private int value;
		YQZZActivityState(int val){
			this.value = val;
		}
		public int getValue() {
			return value;
		}
		
		public static YQZZActivityState valueOf(int val){
			switch (val) {
			case 0:return HIDDEN;
			case 100:return START_SHOW;
			case 200:return MATCH;
			case 300:return BATTLE;
			case 310:return REWARD;
			case 400:return END_SHOW;
			default:return null;
			}
		}
		
	}
	
	
	
	/**
	 * 活动参与状态
	 * @author che
	 *
	 */
	public enum YQZZActivityJoinState{
		JOIN(1),
		OUT(2);
		private int value;
		
		YQZZActivityJoinState(int val){
			this.value = val;
		}
		public int getValue() {
			return value;
		}
		
		public static YQZZActivityJoinState valueOf(int val){
			switch (val) {
			case 1:return JOIN;
			case 2:return OUT;
			default:return null;
			}
		}
	
	}
	
	
	/**
	 * 成就任务状态
	 * @author che
	 *
	 */
	public enum YQZZAchieveState{
		
		PROGRESS(0),
		FINISH(1),
		REWARD(2);
		private int value;
		
		YQZZAchieveState(int val){
			this.value = val;
		}
		public int getValue() {
			return value;
		}
		
		public static YQZZAchieveState valueOf(int val){
			switch (val) {
			case 0:return PROGRESS;
			case 1:return FINISH;
			case 2:return REWARD;
			default:return null;
			}
		}
	}
	
	/**
	 * 成就条件类型
	 * @author che
	 *
	 */
	public enum YQZZAchieveType{
		/**个人	副本内打野x次*/
		PLAYER_KILL_MONSTER(5900001),
		/**个人	个人本期获得军功值x*/
		PLAYER_CONTRIBUTE(5900002),
		/**个人	击杀其他国家x级及以上士兵x*/
		PLAYER_KILL_SOLDIER_LIMIT_LEVEL(5900003),
		/** 个人累计参与驻防x建筑x分钟	 */
		PLAYER_IN_BUILDING_TIME(5900004),
		/**联盟	联盟胜利点达到x*/
		GUILD_WIN_SCORE(5900005),
		/** 联盟	联盟占领过x类型建筑，占领过即可达成 */
		GUILD_OCCUPY_BUILDING(5900006),
		/** 联盟	战场结束时，联盟占领着x1,x2类型建筑X座	*/
		GUILD_HOLD_BUILDING(5900007),
		/** 联盟	战场结束时，联盟占领着x1,x2类型建筑各X座	*/
		GUILD_HOLD_BUILDING_EACH(5900009),
		/** 国家	国家胜利点达到x*/
		COUNTRY_WIN_SOCRE(5900010),
		/** 国家	战场结束时，国家占领着x1,x2类型建筑X座	*/
		COUNTRY_HOLD_BUILDING(5900011),
		/** 国家	战场结束时，国家占领着x1,x2类型建筑各X座	*/
		COUNTRY_HOLD_BUILDING_EACH(5900013),
		/**玩家战胜幽灵基地X次*/
		PLAYER_KILL_FOGGY(5900017),
		/**玩家攻击能量塔X次（派兵出战计算完成，不用完成占领）*/
		PLAYER_MARCH_PYLON(5900018),
		/**联盟成员累计占领能量塔X次（需要完成占领）*/
		GUILD_COLLECT_PYLON(5900019),
		/**联盟成员累计战胜幽灵基地X次*/
		GUILD_KILL_FOGGY(5900020),
		/**联盟成员累积打怪X次*/
		GUILD_KILL_MONSTER(5900021);
		private int value;
		
		YQZZAchieveType(int val){
			this.value = val;
		}
		public int getValue() {
			return value;
		}
		
		
		public static YQZZAchieveType valueOf(int val){
			for(YQZZAchieveType type : YQZZAchieveType.values()){
				if(type.value == val){
					return type;
				}
			}
			return null;
		}
	}
	
	//数据存一年，一年开24期，数据量并不是很大
	public static int REDIS_DATA_EXPIRE_TIME = 3600 * 24 *360;

}
