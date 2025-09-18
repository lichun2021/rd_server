package com.hawk.activity.type.impl.commandAcademySimplify;

/**
 * 指挥官学院活动(新版)
 * @author huangfei -> lating
 *
 */
public class CommandAcademySimplifyConst {

	/**
	 * 结束阶段ID
	 */
	public static final int STAGE_END = 10000;
	
	/** 总榜人数*/
	public static final int FINAL_RANK_SIZE = 100;
	
	/** 排行客户端显示数量*/
	public static final int RANK_SHOW_SIZE = 10;
	
	
	
	/**
	 * 排行榜类型
	 * @author che
	 *
	 */
	public enum RankType{
		/** 总榜*/
		FINAL(0,"final"),
		/** 建筑*/
		BUILDING(1,"building"),
		/** 英雄*/
		HERO(4,"hero"),
		/** 机甲*/
		ARMOR(5,"armor"),
		/** 部队战斗力*/
		ARMY(6,"army"),
		/** s装备数量*/
		SEQUIP(7,"sequip")
		;
		RankType(int type,String rankName){
			this.rankType = type;
			this.rankName = rankName;
		}
		private int rankType;
		
		private String rankName;
		
		public String getRankName(){
			return this.rankName;
		}
		
		public int intValue(){
			return this.rankType;
		}
		
		
		public static RankType getRankType(int rank){
			for(RankType type : values()){
				if(type.intValue() == rank){
					return type;
				}
			}
			return null;
		}
	}
	
	
	
}
