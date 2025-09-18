package com.hawk.game.crossactivity.season;

public class CrossActivitySeasonConst {
	
	//数据KEY
	public static class ResidKey{
		/** 赛季状态数据*/
	    public static final String CROSS_SEASON_STATE = "CROSS_SEASON_STATE";
	    /** 赛季计算数据锁*/
	    public static final String CROSS_SEASON_LOCK = "CROSS_SEASON_LOCK:%d";
	    /** 赛季积分数据*/
	    public static final String CROSS_SEASON_SCORE = "CROSS_SEASON_SCORE:%d";
	    /** 赛季积分获取记录*/
	    public static final String CROSS_SEASON_SCPRE_RECORD = "CROSS_SEASON_SCORE_RECORD:%d:%s";
	    /** 赛季最终榜*/
	    public static final String CROSS_SEASON_FINAL_RANK = "CROSS_SEASON_FINAL_RANK:%d";
	    /** 最终发奖记录*/
	    public static final String CROSS_SEASON_FINAL_REWARD = "CROSS_SEASON_FINAL_REWARD:%d";
	    /** 战斗发奖记录*/
	    public static final String CROSS_SEASON_BATTLE_REWARD = "CROSS_SEASON_BATTLE_REWARD:%d:%s";
	    /** 匹配失败奖励*/
	    public static final String CROSS_SEASON_MATCH_FAIL_REWARD = "CROSS_SEASON_MATCH_FAIL_REWARD:%d:%s";
	}
	
    //锁类型
    public static class LockType{
    	/** 最终排行榜计算*/
    	public static final String CROSS_SEASON_FINAL_RANK_SORT = "CROSS_SEASON_FINAL_RANK_SORT";
    	/** 跨服活动结束计算*/
    	public static final String CROSS_SEASON_FIGHT_RLT = "CROSS_SEASON_FIGHT_RLT";
    }
    
    
    
   
}
