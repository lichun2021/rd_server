package com.hawk.game.module;

import java.util.List;
import java.util.Map;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CrScoreEvent;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrMissionRewardCfg;
import com.hawk.game.config.PlotChapterCfg;
import com.hawk.game.config.PlotLevelCfg;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.PlotBattleEntity;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlotBattle;
import com.hawk.game.protocol.PlotBattle.CRRankResp;
import com.hawk.game.protocol.PlotBattle.CrMissionRewardReq;
import com.hawk.game.protocol.PlotBattle.LevelState;
import com.hawk.game.protocol.PlotBattle.LevelStatusMsg;
import com.hawk.game.protocol.PlotBattle.SynPlotBattleInfo;
import com.hawk.game.protocol.PlotBattle.UploadActionLogReq;
import com.hawk.game.protocol.PlotBattle.UploadBattleReq;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.PlotBattleService;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventPlotBattle;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;

public class PlayerPlotBattleModule extends PlayerModule {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	public PlayerPlotBattleModule(Player player) {
		super(player);
	}
	
	@ProtocolHandler(code={HP.code.PLOT_BATTLE_INFO_REQ_VALUE})
	private void reqPlotBattleInfo(HawkProtocol protocol) {
		this.synPlotBattleInfo();
		
	}
	
	@ProtocolHandler(code={HP.code.UPLOAD_BATTLE_REQ_VALUE})
	private void uploadBattle(HawkProtocol protocol) {		
		UploadBattleReq cparam = protocol.parseProtocol(UploadBattleReq.getDefaultInstance());
		PlotBattleEntity plotBattleEntity = player.getData().getPlotBattleEntity();
		logger.info("playerPlotBattle playerId:{}, newId:{}, oldId:{}", player.getId(), cparam.getLevelsId(), plotBattleEntity.getLevelId());
		PlotLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(PlotLevelCfg.class, cparam.getLevelsId());
		if (levelCfg == null) {
			this.sendError(Status.SysError.PARAMS_INVALID_VALUE, protocol.getType());
			return;
		}
		
		if (player.getMilitaryRankLevel() < levelCfg.getRankLevel()) {
			this.sendError(Status.SysError.PARAMS_INVALID_VALUE, protocol.getType());
			return;
		}
		
		boolean isFirstCross = false;
		if (plotBattleEntity.getLevelId() == 0) {
			plotBattleEntity.setLevelId(cparam.getLevelsId());
			plotBattleEntity.setStatus(LevelState.CROSSED_VALUE);
			isFirstCross = true;
		} else {
			PlotLevelCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(PlotLevelCfg.class, plotBattleEntity.getLevelId());
			PlotLevelCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(PlotLevelCfg.class, cparam.getLevelsId());
			//暂时只有一章，先这么判断，后面功能不全，这里的逻辑也需要补全
			if (nextCfg.getSequeceNo() > curCfg.getSequeceNo()) {
				plotBattleEntity.setLevelId(cparam.getLevelsId());
				plotBattleEntity.setStatus(PlotBattle.LevelState.CROSSED_VALUE);
				isFirstCross = true;
			}
		}
		
		if (isFirstCross) {
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItemInfos(levelCfg.getFirstRewardItem());
			awardItems.rewardTakeAffectAndPush(player, Action.PLOT_BATTLE_REWARD, false);
		}
		
		MissionManager.getInstance().postMsg(player, new EventPlotBattle(cparam.getLevelsId(),  true));
	}
	
	@ProtocolHandler(code = {HP.code.UPLOAD_ACTION_LOG_REQ_VALUE})
	private void uploadActionLog(HawkProtocol protocol) {
		UploadActionLogReq cparam = protocol.parseProtocol(UploadActionLogReq.getDefaultInstance());  
		//logger.info("playerPlotBattle playerId: {}, levelsId: {}, actionType: {}", player.getId(), cparam.getLevelsId(), cparam.getType());
		LogUtil.logRts(player, cparam.getLevelsId(), cparam.getType().getNumber());
	}
	
	protected int isCanBattle(int levelId) {
		PlotBattleEntity plotBattleEntitiy = player.getData().getPlotBattleEntity();
		
		if (plotBattleEntitiy.getLevelId() == 0) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		PlotLevelCfg battleLevelCfg = HawkConfigManager.getInstance().getConfigByKey(PlotLevelCfg.class, levelId);
		if (battleLevelCfg == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		PlotLevelCfg curLevelCfg = HawkConfigManager.getInstance().getConfigByKey(PlotLevelCfg.class, plotBattleEntitiy.getLevelId());
		PlotChapterCfg battleChapter = HawkConfigManager.getInstance().getConfigByKey(PlotChapterCfg.class, battleLevelCfg.getChapterId());
		PlotChapterCfg curChapter = HawkConfigManager.getInstance().getConfigByKey(PlotChapterCfg.class, curLevelCfg.getChapterId());
		if (curChapter.getSequenceNo() > battleChapter.getSequenceNo() ) {
			return Status.SysError.SUCCESS_OK_VALUE;
		} else if (curChapter.getSequenceNo() == battleChapter.getSequenceNo() && curLevelCfg.getSequeceNo() > curLevelCfg.getSequeceNo()) {
			return Status.SysError.SUCCESS_OK_VALUE;
		} else {
			if (StoryMissionService.getInstance().isRtsMissionUnlock(player.getData(), levelId)){
				return Status.SysError.SUCCESS_OK_VALUE;
			} else {
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			
		}
		
		
	}
	

	/**
	 * 找到第一个关卡
	 * @return
	 */
	private PlotLevelCfg findPlotLevel(int chapterId, int sequenceNo){
		Map<Integer, List<PlotLevelCfg>> map = AssembleDataManager.getInstance().getPlotLevelCfgListMap();
		
		List<PlotLevelCfg> cfgList = map.get(chapterId);
		for (PlotLevelCfg cfg : cfgList) {
			if(cfg.getSequeceNo() == sequenceNo) {
				return cfg;
			}
		}
		
		return null;
	}
	
	private PlotChapterCfg findPlotChapterBySequence(int sequenceNo) {
		ConfigIterator<PlotChapterCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PlotChapterCfg.class);
		PlotChapterCfg  cfg = null;
		while (configIterator.hasNext()) {
			cfg = configIterator.next();
			if (sequenceNo == cfg.getSequenceNo()) {
				return cfg;
			}
		}
		
		return null;
	}
	
	/**
	 * 找到下一个关卡
	 * @param levelId
	 * @return
	 */
	protected PlotLevelCfg findNextLevel(int levelId) {
		if (levelId == 0) {
			return findMinLevel();
		}
		
		PlotLevelCfg plotLevelCfg = HawkConfigManager.getInstance().getConfigByKey(PlotLevelCfg.class, levelId);		
		PlotLevelCfg nextLevelCfg = this.findPlotLevel(plotLevelCfg.getChapterId(), plotLevelCfg.getSequeceNo() + 1);
		if (nextLevelCfg == null) {
			PlotChapterCfg chapterCfg = HawkConfigManager.getInstance().getConfigByKey(PlotChapterCfg.class, plotLevelCfg.getChapterId());
			chapterCfg = this.findPlotChapterBySequence(chapterCfg.getSequenceNo() + 1);
			if(chapterCfg != null) {
				//新章节 从第一关开始
				nextLevelCfg = this.findPlotLevel(chapterCfg.getChapterId(), 1);
			}
		}
		
		return nextLevelCfg;
	}
	
	//找到最小的章节
	private PlotLevelCfg findMinLevel() {
		ConfigIterator<PlotChapterCfg> plotChapterCfgIterator = HawkConfigManager.getInstance().getConfigIterator(PlotChapterCfg.class);
		PlotChapterCfg minPlotChapterCfg = null;
		PlotChapterCfg tmpCfg = null;
		
		while (plotChapterCfgIterator.hasNext()) {
			tmpCfg = plotChapterCfgIterator.next();
			if (minPlotChapterCfg == null) {
				minPlotChapterCfg = tmpCfg;
			} else if (minPlotChapterCfg.getSequenceNo() > tmpCfg.getSequenceNo()) {
				minPlotChapterCfg = tmpCfg;
			}
		}
		
		
		Map<Integer, List<PlotLevelCfg>> map = AssembleDataManager.getInstance().getPlotLevelCfgListMap();
		List<PlotLevelCfg> cfgList = map.get(minPlotChapterCfg.getChapterId());
		PlotLevelCfg minLevelCfg = null;
		for (PlotLevelCfg cfg : cfgList) {
			if (minLevelCfg == null) {
				minLevelCfg = cfg;
			} else if (minLevelCfg.getSequeceNo() > cfg.getSequeceNo()) {
				minLevelCfg = cfg;
			}
		}
		
		return minLevelCfg;
	}
	
	private void synPlotBattleInfo() {
		PlotBattleEntity plotBattleEntity = player.getData().getPlotBattleEntity();
		LevelStatusMsg.Builder msgBuilder = LevelStatusMsg.newBuilder();
		msgBuilder.setLevelId(plotBattleEntity.getLevelId());
		if (plotBattleEntity.getStatus() == 0) {
			msgBuilder.setState(LevelState.OPEN);
		} else {
			msgBuilder.setState(LevelState.valueOf(plotBattleEntity.getStatus()));
		}
		
		
		SynPlotBattleInfo.Builder sbuilder = SynPlotBattleInfo.newBuilder();
		sbuilder.setMsg(msgBuilder);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SYN_PLOT_BATTLE_INFO_VALUE, sbuilder));
	}
	
	/**
	 * CR英雄试练领奖
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code={HP.code.CR_MISSION_GET_REWARD_C_VALUE})
	private boolean getCrMissionReward(HawkProtocol protocol) {		
		CrMissionRewardReq req = protocol.parseProtocol(CrMissionRewardReq.getDefaultInstance());
		DailyDataEntity dataEntity = player.getData().getDailyDataEntity();
		if(dataEntity.getCrRewardTimes() >= ConstProperty.getInstance().getCrRewardLimit()){
			sendError(protocol.getType(), Status.Error.CR_REWARD_OVER_LIMIT);
			return true;
		}
		int score = req.getScore();
		CrMissionRewardCfg cfg = getCrReward(score);
		if(cfg == null){
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return true;
		}
		dataEntity.setCrRewardTimes(dataEntity.getCrRewardTimes() + 1);
		if(score > dataEntity.getCrHighestScore()){
			dataEntity.setCrHighestScore(score);
		}
		if(player.hasGuild()){
			String guildId = player.getGuildId();
			double maxScore = LocalRedis.getInstance().getPlayerPlotBattleMaxScoreWithGuildId(guildId, player.getId(), PlotBattleService.getInstance().getCrRankExpireTime());
			long realMaxScore = RankScoreHelper.getRealScore((long)maxScore);
			logger.info("plotBattler player playerId:{}, guildId:{},score:{}, realMaxScore:{}", player.getId(), guildId, score, realMaxScore);
			if(score > realMaxScore){
				logger.info("plotBattler player upload new Score, playerId:{}, guildId:{}, score:{}", player.getId(), player.getGuildId(), score);
				//转换一下分数
				long exchangeScore = RankScoreHelper.calcSpecialRankScore(score); //转换之后的分数
				LocalRedis.getInstance().updatePlayerPlotBattleScore(player.getGuildId(), player.getId(), exchangeScore, PlotBattleService.getInstance().getCrRankExpireTime());
				PlotBattleService.getInstance().buildCrRankInfoFromRedis(player.getGuildId()); //插入最大分数，重新构建联盟排行榜
			}
		}
		if(req.hasSus() && req.getSus()){
			if(req.hasPlayerId()){
				String playerId = req.getPlayerId();
				PlotBattleService.getInstance().sendCrSusMail(player.getId(), playerId);
			}
		}
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, cfg.getAwardId());
		AwardItems awardItems = awardCfg.getRandomAward();
		awardItems.rewardTakeAffectAndPush(player, Action.CR_MISSION_REWARD, RewardOrginType.CR_MISSION_REWARD);
		ActivityManager.getInstance().postEvent(new CrScoreEvent(player.getId(), score));
		player.getPush().syncPlayerInfo();
		return true;
	}
	
	/** 请求试炼排行榜  **/
	@ProtocolHandler(code = HP.code.CR_MISSION_REQ_RANK_LIST_C_VALUE)
	private void reqCrMissionRankInfo(HawkProtocol protocol){
		if(player.hasGuild()){
			CRRankResp.Builder res = PlotBattleService.getInstance().getCrRankInfoFromCache(player.getGuildId());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CR_MISSION_RES_RANK_LIST_S_VALUE, res));
		}
	}
	
	/**
	 * 获取英雄试练奖励
	 * @param score
	 * @return
	 */
	private CrMissionRewardCfg getCrReward(int score){
		ConfigIterator<CrMissionRewardCfg> its = HawkConfigManager.getInstance().getConfigIterator(CrMissionRewardCfg.class);
		for (CrMissionRewardCfg cfg : its) {
			if(score >= cfg.getMinScore() && score<= cfg.getMaxScore()){
				return cfg;
			}
		}
		return null;
	}
}
