package com.hawk.game.module.dayazhizhan.playerteam.season;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.type.impl.dyzzAchieve.cfg.DYZZAchieveKVCfg;
import com.hawk.game.crossproxy.CrossService;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZBattleRoomFameHallMember;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonGradeCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonTimeCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZSeasonStateChangeInvoker;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZRedisData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.DYZZ.PBDYZZGameInfoSync;
import com.hawk.game.protocol.DYZZ.PBGuildInfo;
import com.hawk.game.protocol.DYZZ.PBPlayerInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonData;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonOrder;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonScoreRank;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonScoreRankMember;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonState;
import com.hawk.game.protocol.DYZZWar.PBYZZSeasonTime;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;

public class DYZZSeasonService  extends HawkAppObj {
	/**
	 * 活动状态
	 */
	private DYZZSeasonInfo seasonInfo;
	/**
	 * 积分排行榜
	 */
	private DYZZSeasonScoreRank scoreRank;
	
	
	private long lastTime;
	
	public DYZZSeasonService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	private static DYZZSeasonService instance = null;
	public static DYZZSeasonService getInstance() {
		return instance;
	}
	
	/**
	 * 初始话
	 * @return
	 */
	public boolean init(){
		String serverId = GsConfig.getInstance().getServerId();
		this.seasonInfo = DYZZSeasonRedisData.getInstance()
				.getDYZZSeasonInfo(serverId);
		this.scoreRank = DYZZSeasonRedisData.getInstance()
				.getDYZZSeasonScoreRankShowData(this.seasonInfo.getTermId());
		return true;
	}
	
	
	
	@Override
	public boolean onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curTime - this.lastTime < 2000){
			return true;
		}
		this.lastTime = curTime;
		//状态更新
		this.checkStateChange();
		//排行榜更新
		this.scoreRank.checkUpdate();
		return true;
	}
	

	/**
	 * 更新阶段状态
	 */
	private void checkStateChange() {
		String serverId = GsConfig.getInstance().getServerId();
		DYZZSeasonInfo newInfo = calcInfo();
		int old_term = seasonInfo.getTermId();
		int new_term = newInfo.getTermId();
		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_term != new_term && new_term == 0) {
			seasonInfo = newInfo;
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonInfo(serverId, seasonInfo);
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				player.dealMsg(MsgId.DYZZ_SEASON_STATE_CHANGE, new DYZZSeasonStateChangeInvoker(player));
			}
		}
		PBDYZZSeasonState old_state = seasonInfo.getState();
		PBDYZZSeasonState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从隐藏阶段开始轮询
		if (new_term != old_term) {
			old_state = PBDYZZSeasonState.DYZZ_SEASON_HIDDEN;
			seasonInfo.setTermId(new_term);
			needUpdate = true;
		}

		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == PBDYZZSeasonState.DYZZ_SEASON_HIDDEN) {
				old_state = PBDYZZSeasonState.DYZZ_SEASON_SHOW;
				seasonInfo.setState(old_state);
				onShow();
			} else if (old_state == PBDYZZSeasonState.DYZZ_SEASON_SHOW) {
				old_state = PBDYZZSeasonState.DYZZ_SEASON_OPEN;
				seasonInfo.setState(old_state);
				onOpen();
			} else if (old_state == PBDYZZSeasonState.DYZZ_SEASON_OPEN) {
				old_state = PBDYZZSeasonState.DYZZ_SEASON_CLOSE;
				seasonInfo.setState(old_state);
				onEnd();
			} else if (old_state == PBDYZZSeasonState.DYZZ_SEASON_CLOSE) {
				old_state = PBDYZZSeasonState.DYZZ_SEASON_HIDDEN;
				seasonInfo.setState(old_state);
				onHidden();
			}
		}

		if (needUpdate) {
			seasonInfo = newInfo;
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonInfo(serverId, seasonInfo);
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				player.dealMsg(MsgId.DYZZ_SEASON_STATE_CHANGE, new DYZZSeasonStateChangeInvoker(player));
			}
		}
	}
	
	/**
	 * 展示状态
	 */
	private void onShow(){
		this.scoreRank.resetTerm(this.getDYZZSeasonTerm());
		HawkLog.logPrintln("DYZZSeasonService state on show...");
	}
	
	/**
	 * 开启状态
	 */
	private void onOpen(){
		//发放全服通知邮件
		int termId = this.getDYZZSeasonTerm();
		DYZZSeasonTimeCfg timeCfg = this.getDYZZSeasonTimeCfg(termId);
		Object[] content = new Object[] {termId};
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
                .setMailId(MailId.DYZZ_SEASON_OPEN)
                .addContents(content)
                .build()
                ,HawkTime.getMillisecond(), timeCfg.getEndTimeValue());
		HawkLog.logPrintln("DYZZSeasonService state on open...");
	}
	
	/**
	 * 结束状态
	 */
	private void onEnd(){
		HawkLog.logPrintln("DYZZSeasonService state on end...");
	}
	
	/**
	 * 隐藏状态
	 */
	private void onHidden(){
		HawkLog.logPrintln("DYZZSeasonService state on hidden...");
	}
	
	/**
	 * 计算赛季状态段
	 * @return
	 */
	private DYZZSeasonInfo calcInfo() {
		ConfigIterator<DYZZSeasonTimeCfg> its = HawkConfigManager.getInstance()
				.getConfigIterator(DYZZSeasonTimeCfg.class);
		long now = HawkTime.getMillisecond();
		DYZZSeasonTimeCfg cfg = null;
		for (DYZZSeasonTimeCfg timeCfg : its) {
			if (now > timeCfg.getShowTimeValue()) {
				cfg = timeCfg;
			}
		}
		// 没有可供开启的配置
		if (cfg == null) {
			return new DYZZSeasonInfo();
		}
		int termId = 0;
		PBDYZZSeasonState state = PBDYZZSeasonState.DYZZ_SEASON_HIDDEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long showTime = cfg.getShowTimeValue();
			long startTime = cfg.getStartTimeValue();
			long endTime = cfg.getEndTimeValue();
			long hiddenTime = cfg.getHiddenTimeValue();
			if (now < showTime) {
				state = PBDYZZSeasonState.DYZZ_SEASON_HIDDEN;
			}
			if (now >= showTime && now < startTime) {
				state = PBDYZZSeasonState.DYZZ_SEASON_SHOW;
			}
			if (now >= startTime && now < endTime) {
				state = PBDYZZSeasonState.DYZZ_SEASON_OPEN;
			}
			if (now >= endTime && now < hiddenTime) {
				state = PBDYZZSeasonState.DYZZ_SEASON_CLOSE;
			}
			if (now >= hiddenTime) {
				state = PBDYZZSeasonState.DYZZ_SEASON_HIDDEN;
			}
		}
		DYZZSeasonInfo info = new DYZZSeasonInfo();
		info.setTermId(termId);
		info.setState(state);
		return info;
	}
	
	/**
	 * 领取战令奖励
	 * @param player
	 */
	public void achiveDYZZSeasonOrderReward(Player player,int type,int level){
		DYZZSeasonOrder orderData = this.getDYZZSeasonOrderData(player.getId());
		orderData.achiveReward(player, type, level);
		this.syncDYZZSeasonOrderInfo(player, orderData);
		HawkLog.logPrintln("DYZZSeasonService achiveDYZZSeasonOrderReward type:{},level:{}",type,level);
	}
	
	
	/**
	 * 是否可以购买礼包
	 * @param player
	 * @return
	 */
	public boolean canBuyDYZZSeasonOrderAdvacne(Player player){
		DYZZSeasonOrder orderData = this.getDYZZSeasonOrderData(player.getId());
		if(orderData == null){
			return false;
		}
		return orderData.canBuyAdvacne();
	}
	
	
	/**
	 * 购买了礼包
	 * @param player
	 * @param giftId
	 */
	public void buyDYZZSeasonOrderAdvacne(Player player,int giftId){
		DYZZSeasonOrder orderData = this.makeSureDYZZSeasonOrderData(player);
		orderData.buyAdvance(giftId);
		this.syncDYZZSeasonOrderInfo(player, orderData);
		HawkLog.logPrintln("DYZZSeasonService buyDYZZSeasonOrderAdvacne giftId:{}",giftId);
	}
	
	/**
	 * 更新战令
	 * @param seasonData
	 */
	private void updatePlayerDYZZSeasonOrder(DYZZSeasonPlayerData seasonData){
		//战令相关
		DYZZSeasonOrder orderData = getDYZZSeasonOrderData(seasonData.getPlayerId());
		if(orderData == null){
			return;
		}
		int before = orderData.getScore();
		boolean updateScore = orderData.updateScore(seasonData.getScore());
		if(updateScore){
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(orderData);
			//推送赛季战令消息
			Player player = GlobalData.getInstance().getActivePlayer(seasonData.getPlayerId());
			if(player != null){
				syncDYZZSeasonOrderInfo(player,orderData);
			}
		}
		HawkLog.logPrintln("DYZZSeasonService updatePlayerDYZZSeasonOrder playerId:{},before:{},after:{},update:{}",
				seasonData.getPlayerId(),before,orderData.getScore(),updateScore);
		//记录一下
		DungeonRedisLog.log(seasonData.getPlayerId(), "DYZZSeason order seasonScore:{},befor:{},after:{}",
				seasonData.getScore(),before,orderData.getScore());
	}
	
	
	/**
	 * 更新玩家赛季积分
	 * @param rlt
	 * @param seasonData
	 */
	private void updatePlayerDYZZSeasonData(int dyzzTermId,String gameId,int winCamp,PBPlayerInfo pinfo,DYZZSeasonPlayerData seasonData){
		//胜利
		boolean win = pinfo.getCamp() == winCamp;
		//评分
		int kda = pinfo.getKda();
		//更新积分
		int addScore = this.calScoreAdd(seasonData.getScore(), kda, win);
		int before = seasonData.getScore();
		int score = seasonData.addScore(addScore);
		seasonData.addBattleCount();
		//保存
		DYZZSeasonRedisData.getInstance().updateDYZZSeasonPlayerData(seasonData);
		//更改榜积分
		DYZZSeasonService.getInstance().updateScoreRankPlayer(score,seasonData.getServerId(),
				seasonData.getPlatform(),seasonData.getOpenId(),seasonData.getPlayerId());
		//推送赛季消息
		Player player = GlobalData.getInstance().getActivePlayer(seasonData.getPlayerId());
		if(player != null){
			syncDYZZSeasonInfo(player,seasonData);
		}
		//记录日志
		DungeonRedisLog.log(seasonData.getPlayerId(), "DYZZSeason Socre add:{},befor:{},after:{}",
				addScore,before,score);
		//Tlog
		LogUtil.dyzzSeasonScoreChange(seasonData.getPlayerId(), dyzzTermId, gameId, seasonData.getTermId(), addScore, before, score);
		HawkLog.logPrintln("DYZZSeasonService updatePlayerDYZZSeasonData playerId:{}, before:{},after:{},add:{}",
				seasonData.getPlayerId(),before,score,addScore);
	}
	
	
	/**
	 * 发放赛季战斗奖励
	 * @param rlt
	 */
	private void sendDYZZSeasonReward(int winCamp,PBPlayerInfo pinfo,DYZZSeasonPlayerData data){
		String playerId = pinfo.getPlayerId();
		//胜利
		boolean win = pinfo.getCamp() == winCamp;
		//评分
		int kda = pinfo.getKda();
		//当前段位
		int grade = data.getGrade();
		DYZZSeasonGradeCfg gradeCfg = HawkConfigManager.getInstance()
				.getConfigByKey(DYZZSeasonGradeCfg.class, grade);
		if(gradeCfg == null){
			return;
		}
		int firstWin = 0;
		//首胜奖励
		if(win){
			//添加胜利次数
			long winCount = DYZZRedisData.getInstance().incDYZZWincountToday(playerId, 1);
			if(winCount <= 1){
				firstWin = gradeCfg.getId();
				List<ItemInfo> items = gradeCfg.getFirstWinRewardItems();
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setMailId(MailId.DYZZ_SEASON_BATTLE_FIRST_WIN)
						.setPlayerId(playerId)
						.setRewards(items)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
		}
		//发放胜负奖励
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		MailId mailId = null;
		List<ItemInfo> rewardItems = new ArrayList<>();
		if(win){
			if(pinfo.getMvp() > 0){
				mailId = MailId.DYZZ_SEASON_BATTLE_MVP;
				rewardItems.addAll(gradeCfg.getwinRewardItems());
				rewardItems.addAll(gradeCfg.getMvpRewardItems());
			}else{
				mailId = MailId.DYZZ_SEASON_BATTLE_WIN;
				rewardItems.addAll(gradeCfg.getwinRewardItems());
			}
		}else{
			if(pinfo.getMvp() > 0){
				mailId = MailId.DYZZ_SEASON_BATTLE_SMVP;
				rewardItems.addAll(gradeCfg.getLossRewardItems());
				rewardItems.addAll(gradeCfg.getSmvpRewardItems());
			}else{
				mailId = MailId.DYZZ_SEASON_BATTLE_LOSS;
				rewardItems.addAll(gradeCfg.getLossRewardItems());
			}
		}
		//是否消极
		if(pinfo.getKda() < cfg.getNegativeintegral()){
			mailId = MailId.DYZZ_SEASON_BATTLE_NEGATIVE;
			rewardItems = gradeCfg.getNegativeRewardItems();
		}
		if(mailId != null && rewardItems != null && rewardItems.size() > 0){
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setMailId(mailId)
					.setPlayerId(playerId)
					.setRewards(rewardItems)
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
		
		DungeonRedisLog.log(playerId, "sendDYZZSeasonReward,grade:{},firstWin:{},kda:{},win:{},mvp:{}",
				grade,firstWin,kda,win,pinfo.getMvp());
		HawkLog.logPrintln("DYZZSeasonService sendDYZZSeasonReward,playerId:{},grade:{},firstWin:{},kda:{},win:{},mvp:{}",
				playerId,grade,firstWin,kda,win,pinfo.getMvp());
	}
	
	
	
	/**
	 * 推送赛季信息
	 */
	private void syncDYZZSeasonInfo(Player player,DYZZSeasonPlayerData seasonData){
		if(seasonData == null){
			PBDYZZSeasonData.Builder builder = PBDYZZSeasonData.newBuilder();
			PBYZZSeasonTime.Builder tBuilder = PBYZZSeasonTime.newBuilder();
			tBuilder.setTermId(0);
			tBuilder.setState(PBDYZZSeasonState.DYZZ_SEASON_HIDDEN);
			builder.setSeasonTime(tBuilder);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_SEASON_INFO_RESP_VALUE, builder));
			return;
		}
		int seasonTerm = DYZZSeasonService.getInstance().getDYZZSeasonTerm();
		PBDYZZSeasonState state = DYZZSeasonService.getInstance().getDYZZSeasonState();
		DYZZSeasonTimeCfg timecfg = DYZZSeasonService.getInstance().getDYZZSeasonTimeCfg(seasonTerm);
		PBDYZZSeasonData.Builder builder = PBDYZZSeasonData.newBuilder();
		PBYZZSeasonTime.Builder tBuilder = PBYZZSeasonTime.newBuilder();
		tBuilder.setTermId(seasonTerm);
		tBuilder.setState(state);
		tBuilder.setShowTime(timecfg.getShowTimeValue());
		tBuilder.setStartTime(timecfg.getStartTimeValue());
		tBuilder.setEndTime(timecfg.getEndTimeValue());
		tBuilder.setHiddenTime(timecfg.getHiddenTimeValue());
		builder.setSeasonTime(tBuilder);
		
		builder.setScore(seasonData.getScore());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_SEASON_INFO_RESP_VALUE, builder));
	}
	
	
	/**
	 * 推送战令信息
	 */
	private void syncDYZZSeasonOrderInfo(Player player,DYZZSeasonOrder orderData){
		PBDYZZSeasonOrder.Builder builder = PBDYZZSeasonOrder.newBuilder();
		builder.setTermId(0);
		builder.setScore(0);
		builder.setAdvanceBuy(0);
		if(orderData != null){
			builder.setTermId(orderData.getTermId());
			builder.setScore(orderData.getScore());
			builder.addAllRewardLevels(orderData.getRewardLevel());
			builder.addAllAdvanceRewardLevels(orderData.getAdvanceRewardLevel());
			builder.setAdvanceBuy(orderData.getBuyOrderId()>0?1:0);
			orderData.fillChooseData(builder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_SEASON_ORDER_INFO_RESP_VALUE, builder));
	}
	
	
	
	/**
	 * 获取玩家赛季积分
	 * @param player
	 * @return
	 */
	public int getPlayerSeasonScore(Player player){
		DYZZSeasonPlayerData seasonData = this.getDYZZSeasonPlayerData(player.getId());
		if(seasonData != null){
			return seasonData.getScore();
		}
		return 0;
	}
	
	
	/**
	 * 玩家登陆
	 * @param player
	 */
	public void checkDYZZSeasonDataSync(Player player){
		DYZZSeasonPlayerData seasonData = this.makeSureDYZZSeasonPlayerData(player);
		//检查积分数据
		this.checkDYZZSeasonScoreData(seasonData);
		//同步积分数据
		this.syncDYZZSeasonInfo(player, seasonData);
		
		DYZZSeasonOrder orderData = this.makeSureDYZZSeasonOrderData(player);
		//检查战令数据
		this.checkDYZZSeasonOrderData(orderData, seasonData);
		//推送战令信息
		this.syncDYZZSeasonOrderInfo(player, orderData);
	}
	
	/**
	 * 检查战令信息
	 * @param orderData
	 * @param seasonData
	 */
	private void checkDYZZSeasonOrderData(DYZZSeasonOrder orderData,DYZZSeasonPlayerData seasonData){
		if(orderData == null || seasonData == null){
			return;
		}
		boolean update = orderData.updateScore(seasonData.getScore());
		if(update){
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(orderData);
		}
	}
	
	/**
	 * 将此赛季玩家数据
	 * @param seasonData
	 */
	private void checkDYZZSeasonScoreData(DYZZSeasonPlayerData seasonData){
		if(seasonData == null){
			return;
		}
		PBDYZZSeasonState state = this.getDYZZSeasonState();
		//发送段位奖励
		if(state == PBDYZZSeasonState.DYZZ_SEASON_CLOSE){
			boolean send = seasonData.doSendScoreReward();
			if(send){
				DYZZSeasonRedisData.getInstance()
						.updateDYZZSeasonPlayerData(seasonData);
			}
		}
	}
	
	
	/**
	 * 获取赛季数据
	 * @return
	 */
	private DYZZSeasonPlayerData makeSureDYZZSeasonPlayerData(Player player){
		int seasonTerm = this.getDYZZSeasonTerm();
		PBDYZZSeasonState state = this.getDYZZSeasonState();
		if(seasonTerm == 0 || state == PBDYZZSeasonState.DYZZ_SEASON_HIDDEN){
			return null;
		}
		//去redis拿
		DYZZSeasonPlayerData data = DYZZSeasonRedisData.getInstance().getDYZZSeasonPlayerData(seasonTerm, player.getId());
		if(data == null){
			DYZZSeasonCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
			data = new DYZZSeasonPlayerData();
			data.setTermId(seasonTerm);
			data.setServerId(player.getServerId());
			data.setPlatform(player.getPlatform());
			data.setOpenId(player.getOpenId());
			data.setPlayerId(player.getId());
			data.setScore(cfg.getSeasonScoreInit());
			//积分承接
			int lastTerm = seasonTerm -1;
			DYZZSeasonPlayerData lastData = DYZZSeasonRedisData.getInstance()
					.getDYZZSeasonPlayerData(lastTerm, player.getId());
			if(lastData != null){
				//承接积分
				data.continueScoreFrom(lastData.getScore());
				sendSeasonGradeRestartMail(player.getId(), lastData.getTermId(), lastData.getScore(), data.getScore());
				//查看上赛季是否需要发奖
				boolean sendAward = lastData.doSendScoreReward();
				if(sendAward){
					DYZZSeasonRedisData.getInstance().updateDYZZSeasonPlayerData(lastData);
				}
				//设置上期数据过去时间
				DYZZSeasonRedisData.getInstance().expireDYZZSeasonPlayerData(lastTerm, player.getId());
			}
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonPlayerData(data);
		}
		return data;
	}
	
	
	/**
	 * 获取赛季战令数据
	 * @return
	 */
	public DYZZSeasonOrder getDYZZSeasonOrderData(String playerId){
		int seasonTerm = DYZZSeasonService.getInstance().getDYZZSeasonTerm();
		PBDYZZSeasonState state = DYZZSeasonService.getInstance().getDYZZSeasonState();
		if(seasonTerm == 0 || state == PBDYZZSeasonState.DYZZ_SEASON_HIDDEN){
			return null;
		}
		//去redis拿
		DYZZSeasonOrder order = DYZZSeasonRedisData.getInstance().getDYZZSeasonOrder(seasonTerm, playerId);
		return order;
	}
	
	/**
	 * 获取赛季数据
	 * @return
	 */
	public DYZZSeasonPlayerData getDYZZSeasonPlayerData(String playerId){
		int seasonTerm = this.getDYZZSeasonTerm();
		PBDYZZSeasonState state = this.getDYZZSeasonState();
		if(seasonTerm == 0 || state == PBDYZZSeasonState.DYZZ_SEASON_HIDDEN){
			return null;
		}
		//去redis拿
		DYZZSeasonPlayerData data = DYZZSeasonRedisData.getInstance().getDYZZSeasonPlayerData(seasonTerm, playerId);
		return data;
	}
	
	
	/**
	 * 获取赛季战令数据
	 * @return
	 */
	private DYZZSeasonOrder makeSureDYZZSeasonOrderData(Player player){
		int seasonTerm = DYZZSeasonService.getInstance().getDYZZSeasonTerm();
		PBDYZZSeasonState state = DYZZSeasonService.getInstance().getDYZZSeasonState();
		if(seasonTerm == 0 || state == PBDYZZSeasonState.DYZZ_SEASON_HIDDEN){
			return null;
		}
		//去redis拿
		DYZZSeasonOrder order = DYZZSeasonRedisData.getInstance().getDYZZSeasonOrder(seasonTerm, player.getId());
		if(order == null){
			order = new DYZZSeasonOrder();
			order.setTermId(seasonTerm);
			order.setPlayerId(player.getId());
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(order);
			int lastTerm = seasonTerm - 1;
			DYZZSeasonOrder lastOrder = DYZZSeasonRedisData.getInstance().getDYZZSeasonOrder(lastTerm, player.getId());
			if(lastOrder != null){
				lastOrder.sendAchiveRewardAll(player);
			}

		}
		return order;
	}
	
	
	
	/**
	 * 获取当前赛季期数
	 * @return
	 */
	public int getDYZZSeasonTerm(){
		return this.seasonInfo.getTermId();
	}
	
	/**
	 * 获取当前赛季状态
	 * @return
	 */
	public PBDYZZSeasonState getDYZZSeasonState(){
		return this.seasonInfo.getState();
	}
	
	/**
	 * 获取赛季时间配置
	 * @param termId
	 * @return
	 */
	public DYZZSeasonTimeCfg getDYZZSeasonTimeCfg(int termId){
		DYZZSeasonTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DYZZSeasonTimeCfg.class, termId);
		return cfg;
	}
	
	
	
	/**
	 * 战后计算
	 * @param battleInfo
	 */
	public void onBattleFinish(int dyzzTermId,String gameId,PBDYZZGameInfoSync battleInfo) {
		if(this.getDYZZSeasonTerm() == 0){
			return;
		}
		final int winCamp = battleInfo.getWinCamp();
		//玩家数据
		Map<String,PBPlayerInfo> playerMap = this.getPBPlayerInfoMap(battleInfo);
		Map<Integer,PBGuildInfo> guildMap = this.getPBGuildInfoMap(battleInfo);
		for(PBPlayerInfo pinfo : playerMap.values()){
			final String playerId = pinfo.getPlayerId();
			String playerServerId = pinfo.getServerId();
			boolean local = GlobalData.getInstance().isLocalServer(playerServerId);
			if(!local){
				continue;
			}
			int threadIndex = Math.abs(playerId.hashCode()) % HawkTaskManager.getInstance().getThreadNum();
			HawkTaskManager.getInstance().postTask(new HawkTask(){
				@Override
				public Object run() {
					DYZZSeasonPlayerData seasonData = getDYZZSeasonPlayerData(playerId);
					if(seasonData == null){
						return null;
					}
					//发放奖励
					sendDYZZSeasonReward(winCamp,pinfo,seasonData);
					//更新玩家赛季数据
					updatePlayerDYZZSeasonData(dyzzTermId,gameId,winCamp,pinfo, seasonData);
					//更新战令
					updatePlayerDYZZSeasonOrder(seasonData);
					//更新成就
					updateAchieve(winCamp, pinfo, guildMap.get(pinfo.getCamp()));
					return null;
				}		
			},threadIndex);
		}
	}
	
	
	/**
	 * 更新赛季排行榜
	 * @param score
	 * @param serverId
	 * @param platForm
	 * @param openId
	 * @param playerId
	 */
	public void updateScoreRankPlayer(int score,String serverId,String platForm,String openId,String playerId){
		if(this.getDYZZSeasonState() != PBDYZZSeasonState.DYZZ_SEASON_OPEN){
			return;
		}
		this.scoreRank.updatePlayerScore(score, serverId, platForm,openId,playerId);
	}
	
	/**
	 * 获取排行榜成员数据
	 */
	public void getScoreRankPlayers(Player player){
		int score = this.getPlayerSeasonScore(player);
		PBDYZZSeasonScoreRank.Builder builder = this.scoreRank.createPBDYZZSeasonScoreRank();
		PBDYZZSeasonScoreRankMember.Builder sbuilder = this.scoreRank.createPlayerScoreMember(player, score);
		builder.setSelf(sbuilder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_SEASON_SCORE_RANK_RESP_VALUE, builder));
	}
	
	/**
	 * 获取战场名人堂
	 * @return
	 */
	public List<DYZZBattleRoomFameHallMember> getFameHallMemeber(){
		List<DYZZBattleRoomFameHallMember> members = new ArrayList<>();
		if(this.getDYZZSeasonTerm() == 0){
			return members;
		}
		List<DYZZSeasonScoreRankMember> list =  this.scoreRank.getFameHall();
		list.forEach(m->members.add(m.toDYZZBattleRoomFameHallMember()));
		return members;
	}
	
	/**
	 * 计算赛季增长积分
	 * 玩家该场比赛得分（向上取整）=32*段位修正系数*（胜负关系）+玩家当场比赛评分/2-5 
	 * @param score
	 * @param battleKda
	 * @return
	 */
	public int calScoreAdd(long score,int battleKda,boolean win){
		if(this.getDYZZSeasonState() != PBDYZZSeasonState.DYZZ_SEASON_OPEN){
			return 0;
		}
		DYZZSeasonCfg constCfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
		DYZZSeasonGradeCfg cfg = this.getGrade(score);
		if(cfg==null){
			return 0;
		}
		double gradeParam = cfg.getScoreParam(win);
		double param1 = constCfg.getScoreParam1();
		double param2 = constCfg.getScoreParam2();
		double param3 = constCfg.getScoreParam3();
		double rlt = param1 * gradeParam + battleKda/param2 - param3;
		rlt = Math.ceil(rlt);
		return (int) rlt;
	}
	
	/**
	 * 获取首胜奖励段位ID
	 * @param playerId
	 * @param seasonScore
	 * @return
	 */
	public int recordSeasonFirstWinGrade(long seasonScore){
		if(this.getDYZZSeasonTerm() == 0){
			return 0;
		}
		DYZZSeasonGradeCfg grade =  this.getGrade(seasonScore);
		if(grade != null){
			return grade.getId();
		}
		return 0;
	}
	
	
	/**
	 * 获取段位
	 * @return
	 */
	public DYZZSeasonGradeCfg getGrade(long score){
		DYZZSeasonGradeCfg grade = null;
		List<DYZZSeasonGradeCfg> list = HawkConfigManager.getInstance()
				.getConfigIterator(DYZZSeasonGradeCfg.class).toList();
		for(DYZZSeasonGradeCfg cfg : list){
			if(score >= cfg.getScore()){
				if(grade == null){
					grade = cfg;
				}
				if(grade.getId() < cfg.getId()){
					grade = cfg;
				}
			}
		}
		return grade;
	}
	
	
	/**
	 * 组织玩家列表
	 * @param info
	 * @return
	 */
	private Map<String,PBPlayerInfo> getPBPlayerInfoMap(PBDYZZGameInfoSync info){
		Map<String,PBPlayerInfo> map = new HashMap<>();
		for(PBPlayerInfo playerInfo :info.getPlayerInfoList()){
			map.put(playerInfo.getPlayerId(), playerInfo);
		}
		return map;
	}

	private Map<Integer, PBGuildInfo> getPBGuildInfoMap(PBDYZZGameInfoSync info){
		Map<Integer, PBGuildInfo> map = new HashMap<>();
		for(PBGuildInfo playerInfo :info.getGuildInfoList()){
			map.put(playerInfo.getCamp(), playerInfo);
		}
		return map;
	}

	public void rewardChoose(Player player, int type, int level, int choose){
		DYZZSeasonOrder orderData = this.getDYZZSeasonOrderData(player.getId());
		if(orderData == null){
			return;
		}
		orderData.rewardChoose(type, level, choose);
		this.syncDYZZSeasonOrderInfo(player, orderData);
	}

	public void sendSeasonGradeRestartMail(String playerId, int lastTermId, int oldScore, int newScore){
		DYZZSeasonGradeCfg gradeCfg = DYZZSeasonService.getInstance().getGrade(oldScore);
		int grade = 0;
		if(gradeCfg != null){
			grade = gradeCfg.getId();
		}
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.DYZZ_SEASON_GRADE_RESET)
				.setPlayerId(playerId)
				.addContents(lastTermId, oldScore, grade, newScore)
				.build());
	}

	public void updateAchieve(int winCamp,PBPlayerInfo pinfo,PBGuildInfo ginfo){
		boolean isSeason = false;
		try {
			int seasonTerm = DYZZSeasonService.getInstance().getDYZZSeasonTerm();
			PBDYZZSeasonState state = DYZZSeasonService.getInstance().getDYZZSeasonState();
			if(seasonTerm!=0 && state == PBDYZZSeasonState.DYZZ_SEASON_OPEN){
				isSeason = true;
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
		if(CrossService.getInstance().isCrossPlayer(pinfo.getPlayerId())){
			DYZZSeasonBattleInfo battleInfo = new DYZZSeasonBattleInfo(winCamp, pinfo, ginfo, isSeason);
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonBattle(battleInfo);
			HawkLog.logPrintln("DYZZSeasonService updateAchieve isCrossPlayer battleInfo:{}",battleInfo.serializ());
			return;
		}
		ActivityManager.getInstance().postEvent(new DYZZScoreEvent(pinfo.getPlayerId(), pinfo.getKda(), HawkTime.getMillisecond(), isSeason));
		int lessTen = 10;
		int equalOne = 1;
		int holyShit =17;
		DYZZAchieveKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZAchieveKVCfg.class);
		if(cfg != null){
			lessTen = cfg.getAchieveLessTen();
			equalOne = cfg.getAchieveEqualOne();
			holyShit = cfg.getAchieveHolyShit();
		}
		if(winCamp == pinfo.getCamp()){
			ActivityManager.getInstance().postEvent(new DYZZWinEvent(pinfo.getPlayerId(), true));
			if(pinfo.getMvp() == 1){
				ActivityManager.getInstance().postEvent(new DYZZWinBestEvent(pinfo.getPlayerId()));
			}
			if(ginfo != null){
				if(ginfo.getBaseHP() < lessTen){
					ActivityManager.getInstance().postEvent(new DYZZWinWithBaseLessTenEvent(pinfo.getPlayerId()));
				}
				if(ginfo.getBaseHP() == equalOne){
					ActivityManager.getInstance().postEvent(new DYZZWinWithBaseEqualOneEvent(pinfo.getPlayerId()));
				}
			}
		}else {
			ActivityManager.getInstance().postEvent(new DYZZWinEvent(pinfo.getPlayerId(), false));
			if(pinfo.getMvp() == 1){
				ActivityManager.getInstance().postEvent(new DYZZLostBestEvent(pinfo.getPlayerId()));
			}
		}
		if(pinfo.getKda() >= holyShit){
			ActivityManager.getInstance().postEvent(new DYZZHolyShitEvent(pinfo.getPlayerId()));
		}
	}

}
