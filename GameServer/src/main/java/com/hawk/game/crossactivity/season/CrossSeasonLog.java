package com.hawk.game.crossactivity.season;

import org.hawk.os.HawkException;

import com.hawk.game.util.LogUtil;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.LogConst.LogInfoType;

public class CrossSeasonLog {
		
	/**
	 * 航海赛季-积分计算 
	 * @param serverId       服务器ID
	 * @param targetServer   对手服务器ID
	 * @param battleRlt      对战结果  1胜利 2失败
	 * @param scoreAdd       积分变化量
	 * @param scoreBef       积分变化前
	 * @param scoreAft       积分变化后
	 * @param seasonId       赛季ID
	 * @param crossTerm      航海期数
	 * @param crossId        分组ID
	 * @param actionType     记录类型  1 初始化  2对战  3合服继承
	 */
	public static void logCrossSeasonScore(String serverId,String targetServer,int battleRlt,int scoreAdd,int scoreBef,
			int scoreAft,int seasonId,int crossTerm,int crossId,int actionType) {
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.cross_season_score);
		    logParam.put("serverId", serverId)
		    		.put("targetServer", targetServer)
		    		.put("battleRlt", battleRlt)
		    		.put("scoreAdd", scoreAdd)
		    		.put("scoreBef", scoreBef)
		    		.put("scoreAft", scoreAft)
		    		.put("seasonId", scoreAft)
		    		.put("crossTerm", scoreAft)
		    		.put("crossId", scoreAft)
		    		.put("actionType", actionType);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 航海赛季-每期航海奖励 
	 * @param serverId  服务器ID
	 * @param scoreCnt  积分数量
	 * @param seasonId  赛季ID
	 * @param crossTerm 航海期数
	 * @param crossId   分组ID
	 */
	public static void logCrossSeasonBattleReward(String serverId, int scoreCnt,int seasonId,int crossTerm,int crossId) {
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.cross_season_battle_reward);
		    logParam.put("serverId", serverId)
		    		.put("scoreCnt", scoreCnt)
		    		.put("seasonId", seasonId)
		    		.put("crossTerm", crossTerm)
		    		.put("crossId", crossId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 航海赛季-最终排名
	 * @param serverId  服务器ID
	 * @param scoreCnt  积分数量
	 * @param rankIndex 排名
	 * @param seasonId  赛季ID
	 */
	public static void logCrossSeasonFinalRank(String serverId, int scoreCnt,int rankIndex, int seasonId) {
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.cross_season_final_rank);
		    logParam.put("serverId", serverId)
		    		.put("scoreCnt", scoreCnt)
		    		.put("rankIndex", rankIndex)
		    		.put("seasonId", seasonId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 航海赛季-最终发奖
	 * @param serverId  服务器ID
	 * @param scoreCnt  积分数量
	 * @param rankIndex 排名
	 * @param rewardId  奖励ID
	 * @param seasonId  赛季ID
	 */
	public static void logCrossSeasonFinalReward(String serverId, int scoreCnt,int rankIndex, int rewardId, int seasonId) {
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.cross_season_final_reward);
		    logParam.put("serverId", serverId)
		    		.put("scoreCnt", scoreCnt)
		    		.put("rankIndex", rankIndex)
		    		.put("rewardId", rewardId)
		    		.put("seasonId", seasonId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
}
