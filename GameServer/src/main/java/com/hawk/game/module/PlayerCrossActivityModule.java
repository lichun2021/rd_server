package com.hawk.game.module;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.common.IDIPBanInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossMissionCfg;
import com.hawk.game.config.CrossPackCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.rank.MatchStrengthRank;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.crossproxy.crossGift.CrossGiftOper;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.msg.MissionMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Cross.InnerEnterCrossMsg;
import com.hawk.game.protocol.CrossActivity.CrossActivityPylonRankReq;
import com.hawk.game.protocol.CrossActivity.CrossActivityState;
import com.hawk.game.protocol.CrossActivity.CrossFightGuildInfo;
import com.hawk.game.protocol.CrossActivity.CrossFightGuildInfoResp;
import com.hawk.game.protocol.CrossActivity.CrossGiftInfo;
import com.hawk.game.protocol.CrossActivity.CrossGiftInfoPush;
import com.hawk.game.protocol.CrossActivity.CrossGiftRecord;
import com.hawk.game.protocol.CrossActivity.CrossGiftRecord.Builder;
import com.hawk.game.protocol.CrossActivity.CrossGiftRecordReq;
import com.hawk.game.protocol.CrossActivity.CrossGiftRecordResp;
import com.hawk.game.protocol.CrossActivity.CrossGiftSearchReq;
import com.hawk.game.protocol.CrossActivity.CrossGiftSearchRes;
import com.hawk.game.protocol.CrossActivity.CrossGiftSendReq;
import com.hawk.game.protocol.CrossActivity.CrossStateInfo;
import com.hawk.game.protocol.CrossActivity.CrossTaxInfo;
import com.hawk.game.protocol.CrossActivity.CrossTaxPageInfo;
import com.hawk.game.protocol.CrossActivity.CrossTaxRecord;
import com.hawk.game.protocol.CrossActivity.CrossTaxSendRecord;
import com.hawk.game.protocol.CrossActivity.CrossTaxSendRecordResp;
import com.hawk.game.protocol.CrossActivity.CrossTaxSendReq;
import com.hawk.game.protocol.CrossActivity.GetCAchieveRewardReq;
import com.hawk.game.protocol.CrossActivity.GetCrossMissionAward;
import com.hawk.game.protocol.CrossActivity.GetCrossPageInfoResp;
import com.hawk.game.protocol.CrossActivity.GetCrossRankInfoReq;
import com.hawk.game.protocol.CrossActivity.GetCrossRankResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.type.IMission;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;

/**
 * 跨服活动模块
 * 
 * @author Jesse
 */
public class PlayerCrossActivityModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 * @param player
	 */
	public PlayerCrossActivityModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		// 同步跨服活动状态
		CrossActivityService.getInstance().syncStateInfo(this.player);
		
		// 同步跨服活动任务数据
		CrossActivityService.getInstance().syncTargetInfo(this.player);
		
		// 检测跨服道具移除
		CrossActivityService.getInstance().checkCrossItemRemove(player);
		
		CrossActivityService.getInstance().removePlayerMission(player.getId());
		
		CrossActivityService.getInstance().pushServerScoreRank(player);
		
		CrossActivityService.getInstance().syncGuildOccupyAccumulateData(player);
		
		if (player.isCsPlayer() && player.hasGuild()) {
			// 动态加载跨服联盟科技数据
			Map<Integer, Integer> csGuildTech = GuildService.getInstance().getCsEffectsGuildTech(player.getGuildId());
			if (csGuildTech == null) {
				GuildService.getInstance().loadGuildTech4Cross(player.getGuildId());
			}
		}
		
		if (player.isCsPlayer()) {
			player.getData().checkEffectValueInCross();
		}
		
		//赛季数据
		CrossActivitySeasonService.getInstance().syncSeasonData(player);
		
		return true;
	}

	@Override
	protected boolean onPlayerLogout() {
		
		// 活动期间设置下道具检测期数，避免登陆的时候移除
		if (!CrossActivityService.getInstance().isState(CrossActivityState.C_HIDDEN)) {
			int termId = CrossActivityService.getInstance().getTermId();
			RedisProxy.getInstance().setCrossItemCheckTerm(player.getId(), termId);
		}
		
		CrossActivityService.getInstance().removePlayerMission(player.getId());
		//刷新一下玩家战力
		MatchStrengthRank.getInstance().updateStrength(this.player);
		return true;
	}
	
	/**
	 * 获取界面信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = CHP.code.CROSS_GET_PAGE_INFO_C_VALUE)
	private boolean onGetPageInfo(HawkProtocol protocol) {
		GetCrossPageInfoResp.Builder builder = CrossActivityService.getInstance().getPageInfo(this.player);
		sendProtocol(HawkProtocol.valueOf(CHP.code.CROSS_GET_PAGE_INFO_S, builder));
		return true;
	}
	
	/**
	 * 获取排行信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = CHP.code.CROSS_GET_RANK_INFO_C_VALUE)
	private boolean onGetRankInfo(HawkProtocol protocol) {
		GetCrossRankInfoReq req = protocol.parseProtocol(GetCrossRankInfoReq.getDefaultInstance());
		GetCrossRankResp.Builder builder = CrossActivityService.getInstance().getRankInfo(this.player, req.getRankType());
		sendProtocol(HawkProtocol.valueOf(CHP.code.CROSS_GET_RANK_INFO_S, builder));
		return true;
	}
	
	/**
	 * 领取成就奖励
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = CHP.code.CROSS_GET_ACHIEVE_REWARD_C_VALUE)
	private boolean onGetReward(HawkProtocol protocol){
		GetCAchieveRewardReq req = protocol.parseProtocol(GetCAchieveRewardReq.getDefaultInstance());
		int result = CrossActivityService.getInstance().getCAchieveReward(player, req.getAchieveId());
		if(result == Status.SysError.SUCCESS_OK_VALUE){
			player.responseSuccess(protocol.getType());
		}else{
			sendError(protocol.getType(), result);
		}
		return true;
	}

	@Override
	protected boolean onPlayerAssemble() {
		boolean isFirstCrossServerLogin = false;
		if (player.isCsPlayer()) {
			CsPlayer csPlayer = (CsPlayer) player;
			isFirstCrossServerLogin = csPlayer.isFirstCrossServerLogin();
		}
		// 如果是首次跨服,则进行影子联盟的加入或创建
		if (isFirstCrossServerLogin) {
			HPLogin.Builder loginBuilder = player.getHpLogin();
			if (loginBuilder != null) {
				InnerEnterCrossMsg msg = loginBuilder.getInnerEnterCrossMsg();
				String guildId = msg.getGuildId();
				int guildAuth = Const.GuildAuthority.L3_VALUE;
				if (msg.hasGuildAuth()) {
					guildAuth = msg.getGuildAuth();
				}
				GuildService.getInstance().onCsPlayerEnter(player, guildId, guildAuth);
			}
		}
		return true;
	}
		
	
	
	/**
	 * 获取远航充能界面信息 
	 */
	@ProtocolHandler(code = HP.code.CROSS_CHARGE_PAGE_INFO_C_VALUE)
	private boolean onGetChargePageInfo(HawkProtocol protocol) {
		CrossActivityService.getInstance().pushCrossPageInfo(player);
		return true;
	}
	
	/**
	 * 税收(仓库)界面信息
	 */
	@ProtocolHandler(code = HP.code.CROSS_TAX_PAGE_INFO_C_VALUE)
	private boolean taxPageInfo(HawkProtocol protocol) {
		pushTaxPageInfo();
		return true;
	}

	public void pushTaxPageInfo() {
		CrossTaxPageInfo.Builder builder = CrossTaxPageInfo.newBuilder();
		
		String serverId = GsConfig.getInstance().getServerId();
		Map<Integer, Long> crossTax = RedisProxy.getInstance().getAllCrossTax(serverId);
		for (Entry<Integer, Long> tax : crossTax.entrySet()) {
			CrossTaxInfo.Builder taxInfo = CrossTaxInfo.newBuilder();
			taxInfo.setResType(tax.getKey());
			taxInfo.setResValue(tax.getValue());
			builder.addTax(taxInfo);
		}
		
		List<CrossTaxRecord> records = RedisProxy.getInstance().getTaxRecord(serverId);
		builder.addAllRecord(records);
		sendProtocol(HawkProtocol.valueOf(HP.code.CROSS_TAX_PAGE_INFO_S, builder));
	}
	
	/**
	 * 仓库发奖
	 */
	@ProtocolHandler(code = HP.code.CROSS_TAX_SEND_C_VALUE)
	private boolean sendTax(HawkProtocol protocol) {
		CrossTaxSendReq req = protocol.parseProtocol(CrossTaxSendReq.getDefaultInstance());
		CrossActivityService.getInstance().sendTax(player, req);
		pushTaxPageInfo();
		return true;
	}
	
	/**
	 * 获取发奖记录
	 */
	@ProtocolHandler(code = HP.code.CROSS_TAX_SEND_INFO_C_VALUE)
	private boolean getSendTaxRecord(HawkProtocol protocol) {
		CrossTaxSendRecordResp.Builder builder = CrossTaxSendRecordResp.newBuilder();
		
		List<CrossTaxSendRecord> taxSendRecord = RedisProxy.getInstance().getTaxSendRecord(GsConfig.getInstance().getServerId());
		if (!taxSendRecord.isEmpty()) {
			builder.addAllRecord(taxSendRecord);
		}
		sendProtocol(HawkProtocol.valueOf(HP.code.CROSS_TAX_SEND_INFO_S, builder));
		return true;
	}
	
	/**
	 * 跨服任务
	 */
	@MessageHandler	
	private void onRefreshMission(MissionMsg msg) {
		if (!CrossActivityService.getInstance().isOpen()) {
			return;
		}
		
		MissionEvent event = msg.getEvent();
		
		// 事件触发任务列表
		List<MissionType> touchMissions = event.touchMissions();
		if (touchMissions == null || touchMissions.isEmpty()) {
			return;
		}
		
		boolean update = false;
		// 任务配置
		Map<Integer, MissionEntityItem> missions = CrossActivityService.getInstance().getCrossMission(player.getId());
		for (MissionEntityItem entityItem : missions.values()) {
			
			if (entityItem == null) {
				continue;
			}
			
			CrossMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossMissionCfg.class, entityItem.getCfgId());
			if (cfg == null) {
				continue;
			}
			
			// 任务类型
			MissionType missionType = MissionType.valueOf(cfg.getType());
			
			// 不触发此类型任务
			if (!touchMissions.contains(missionType)) {
				continue;
			}
			
			if (entityItem.getState() != MissionState.STATE_NOT_FINISH) {
				continue;
			}
			
			// 刷新任务
			IMission iMission = MissionContext.getInstance().getMissions(missionType);
			iMission.refreshMission(player.getData(), event, entityItem, cfg.getMissionCfgItem());
			update = true;
		}
		
		if (update) {
			CrossActivityService.getInstance().updateCrossMission(player.getId());
			CrossActivityService.getInstance().pushCrossPageInfo(player);
		}
	}

	/**
	 * 获取任务奖励
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CROSS_GET_MISSION_AWARD_C_VALUE)
	private void getCrossMissionAward(HawkProtocol protocol) {
		if (!CrossActivityService.getInstance().isOpen() && !CrossActivityService.getInstance().isState(CrossActivityState.C_END)) {
			return;
		}
		
		GetCrossMissionAward req = protocol.parseProtocol(GetCrossMissionAward.getDefaultInstance());
		int cfgId = req.getMissionId();
		
		Map<Integer, MissionEntityItem> missions = CrossActivityService.getInstance().getCrossMission(player.getId());
		MissionEntityItem mission = missions.get(cfgId);
		if (mission == null) {
			return;
		}
		
		if (mission.getState() != MissionState.STATE_FINISH) {
			return;
		}
		
		CrossMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossMissionCfg.class, cfgId);
		if (cfg == null) {
			return;
		} 
		mission.setState(MissionState.STATE_BONUS);
		
		// 发奖
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(cfg.getRewardItem());
		
		int effVal = player.getEffect().getEffVal(EffType.CROSS_SELF_TASK_REWARD_DOUBLE);
		if (effVal > 0) {
			awardItems.addItemInfos(cfg.getRewardItem());
		}
		
		awardItems.rewardTakeAffectAndPush(player, Action.CROSS_MISSION_AWARD, true, RewardOrginType.CROSS_MISSION_AWARD);
		
		player.responseSuccess(protocol.getType());
		
		// 刷新任务状态
		CrossActivityService.getInstance().updateCrossMission(player.getId());
		
		// 推送充能界面信息
		CrossActivityService.getInstance().pushCrossPageInfo(player);
		
		LogUtil.logCrossActivtyMission(player, player.getGuildId(), cfgId);
	}
	
	/**
	 * 充能界面状态
	 */
	@ProtocolHandler(code = HP.code.CROSS_CHARGE_STATE_INFO_C_VALUE)
	private boolean chargeActivityState(HawkProtocol protocol) {
		CrossStateInfo.Builder stateInfo = CrossActivityService.getInstance().genStateInfo();
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CROSS_CHARGE_STATE_INFO_S, stateInfo));
		return true;
	}
	
	/**
	 * 礼包信息请求
	 */
	@ProtocolHandler(code = HP.code2.CROSS_ACTIVITY_GIFTINFO_REQ_VALUE)
	private void giftInfoReq(HawkProtocol protocol) {
		pushCrossGiftInfo();
	}

	/**
	 * 推送礼包信息
	 */
	private void pushCrossGiftInfo() {
		CrossGiftInfoPush.Builder builder = CrossGiftInfoPush.newBuilder();
		Map<Integer, Integer> crossGiftInfos = RedisProxy.getInstance().getCrossGiftInfos();
		for (Entry<Integer, Integer> giftInfo : crossGiftInfos.entrySet()) {
			CrossGiftInfo.Builder giftBuilder = CrossGiftInfo.newBuilder();
			giftBuilder.setGiftId(giftInfo.getKey());
			giftBuilder.setSendCount(giftInfo.getValue());
			builder.addGiftInfo(giftBuilder);
		}
		
		List<CrossGiftRecord.Builder> records = RedisProxy.getInstance().getAllCrossGiftRecord();
		for (Builder record : records) {
			int giftId = record.getGiftId();
			CrossPackCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossPackCfg.class, giftId);
			if (cfg == null) {
				continue;
			}
			builder.addRecord(record);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_ACTIVITY_GIFTINFO_RESP, builder));
	}
	
	/**
	 * 发礼包
	 */
	@ProtocolHandler(code = HP.code2.CROSS_ACTIVITY_GIFT_SEND_VALUE)
	private void giftSendReq(HawkProtocol protocol) {
		CrossGiftSendReq req = protocol.parseProtocol(CrossGiftSendReq.getDefaultInstance());
		int giftId = req.getGiftId();
		List<String> tarPlayerIds = req.getTargetPlayerIdList();
		
		if (!CrossActivityService.getInstance().isAwardTime()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_SEND_GIFT_PEROID_ERROR);
			return;
		}
		
		// 礼包不存在
		CrossPackCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossPackCfg.class, giftId);
		if (cfg == null) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_SEND_GIFT_ID_ERROR);
			return;
		}
		
		// 发放礼包达到上限
		int alreadySendCount = RedisProxy.getInstance().getCrossGiftSendCount(giftId);
		if (alreadySendCount + tarPlayerIds.size() > cfg.getNum()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_SEND_GIFT_COUNT_LIMIT_ERROR);
			return;
		}
		
		// 礼包类型错误
		CrossGiftOper imp = CrossGiftOper.imp(cfg.getType());
		if (imp == null) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_SEND_GIFT_TYPE_ERROR);
			return;
		}
		
		// 礼包类型权限检测
		if (!imp.doCheck(player.getId())) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_SEND_GIFT_AUTH_ERROR);
			return;
		}
		
		for (String tarPlayerId : tarPlayerIds) {
			try {
				// 目标玩家不存在
				Player tarPlayer = GlobalData.getInstance().makesurePlayer(tarPlayerId);
				if (tarPlayer == null) {
					sendError(protocol.getType(), Status.CrossServerError.CROSS_SEND_GIFT_PLYAER_NO_ERROR);
					return;
				}
				
				// 已经颁发过这个玩家同类型礼包
				if (RedisProxy.getInstance().existCrossGiftPlayerRecord(cfg.getSendType(), tarPlayerId)) {
					sendError(protocol.getType(), Status.CrossServerError.CROSS_SEND_GIFT_PLYAER_REP_ERROR);
					return;
				}
				
				// 礼包记录
				CrossGiftRecord.Builder record = CrossGiftRecord.newBuilder();
				record.setSendTime(HawkTime.getMillisecond());
				record.setGiftId(giftId);
				record.setSendPlayerName(player.getName());
				record.setPlayerId(tarPlayer.getId());
				record.setPlayerName(tarPlayer.getName());
				String guildTag = GuildService.getInstance().getGuildTag(tarPlayer.getGuildTag());
				if (!HawkOSOperator.isEmptyString(guildTag)) {
					record.setGuildTag(guildTag);
				}
				record.setServerId(tarPlayer.getMainServerId());
				
				RedisProxy.getInstance().addCrossGiftRecord(record);
				RedisProxy.getInstance().addCrossGiftPlayerRecord(cfg.getSendType(), tarPlayerId);
				RedisProxy.getInstance().updateCrossGiftInfo(giftId, 1);
				
				// 发送邮件---大总统礼包
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(tarPlayerId)
						.setMailId(MailId.CROSS_ACTIVITY_MAIL_20181223)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addTitles(cfg.getGiftName())
						.addSubTitles(cfg.getGiftName())
						.addContents(cfg.getGiftName())
						.setRewards(cfg.getRewardItems())
						.build());
				
				ChatParames.Builder builder = ChatParames.newBuilder();
				builder.setKey(Const.NoticeCfgId.CROSS_GIFT_SEND);
				builder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
				builder.addParms(cfg.getGiftName());
				builder.addParms(tarPlayer.getName());
				ChatService.getInstance().addWorldBroadcastMsg(builder.build());
				
				LogUtil.logCrossActivtySendGift(player, player.getId(), tarPlayer.getId(), cfg.getType(), giftId, 1);
				logger.info("send cross gift, tarPlayerId:{}, giftId:{}, beforeCount:{}, giftType:{}", tarPlayerId, giftId, alreadySendCount, cfg.getType());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		pushCrossGiftInfo();
	}
	


	/**
	 * 获取联盟出战信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.CROSS_FIGHT_INFO_REQ_VALUE)
	private void fightGuildInfo(HawkProtocol protocol) {
		pushFightGuildInfo(player);
	}

	private void pushFightGuildInfo(Player player) {
		// 所有出战联盟
		List<String> allCrossFightGuild = RedisProxy.getInstance().getAllCrossFightGuild();
		
		// 取前可出战联盟
		CrossFightGuildInfoResp.Builder builder = CrossFightGuildInfoResp.newBuilder();
		// 是否是盟主
		boolean isGuildLeader = GuildService.getInstance().isGuildLeader(player.getId());
		builder.setIsGuildLeader(isGuildLeader);
		// 是否是战时司令
		String crossFightPresident = RedisProxy.getInstance().getCrossFightPresident();
		boolean isFightPresident = player.getId().equals(crossFightPresident);
		builder.setIsFightPresident(isFightPresident);
		
		// 所有申请出战的盟
		String serverId = GsConfig.getInstance().getServerId();
		int termId = CrossActivityService.getInstance().getTermId();
		Set<String> allCrossApply = RedisProxy.getInstance().getAllCrossApply(serverId, termId);
		
		// 所有邀请出战的盟
		Map<String, String> allCrossInvite = RedisProxy.getInstance().getAllCrossInviteTime(serverId, termId);
		
		Set<String> crossCanFightGuilds = RedisProxy.getInstance().getCrossCanFightGuilds();
		for (String guildId : crossCanFightGuilds) {
			try {
				RankInfo rankInfo = RankService.getInstance().getRankInfo(RankType.ALLIANCE_FIGHT_KEY, guildId);
				if (rankInfo == null) {
					continue;
				}
				CrossFightGuildInfo.Builder guildInfo = CrossFightGuildInfo.newBuilder();
				guildInfo.setGuildId(guildId);
				guildInfo.setGuildName(GuildService.getInstance().getGuildName(guildId));
				guildInfo.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
				guildInfo.setLeaderId(GuildService.getInstance().getGuildLeaderId(guildId));
				guildInfo.setLeaderName(GuildService.getInstance().getGuildLeaderName(guildId));
				guildInfo.setGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
				guildInfo.setPower(rankInfo.getRankInfoValue());
				guildInfo.setCurrentCount(GuildService.getInstance().getGuildMemberNum(guildId));
				guildInfo.setMaxCount(GuildService.getInstance().getGuildMemberMaxNum(guildId));
				guildInfo.setFight(allCrossFightGuild.contains(guildId));
				
				// 如果是战时司令返回上次邀请时间
				if (isFightPresident && allCrossInvite.containsKey(guildId)) {
					long invaiteTime = Long.parseLong(allCrossInvite.get(guildId));
					guildInfo.setLastInvateTime(invaiteTime);
				}
				
				builder.addInfo(guildInfo);
				
				// 战时司令返回联盟申请信息
				if (isFightPresident && allCrossApply.contains(guildId)) {
					// 已经是出战的联盟,这里做下申请信息动态删除
					if (allCrossFightGuild.contains(guildId)) {
						RedisProxy.getInstance().removeCrossApply(serverId, termId, guildId);
					} else {
						builder.addFightApplyInfo(guildInfo);
					}
				}
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 如果是盟主,同步联盟邀请信息
		if (isGuildLeader) {
			long crossInviteTime = RedisProxy.getInstance().getCrossInvite(serverId, termId, player.getGuildId());
			if (crossInviteTime > 0L) {
				// 已经是出战的联盟,这里做下邀请信息动态删除
				if (allCrossFightGuild.contains(player.getGuildId())) {
					RedisProxy.getInstance().removeCrossInvite(serverId, termId, player.getGuildId());
				} else {
					builder.setHasInviteInfo(true);
				}
			}
			long crossApplyTime = RedisProxy.getInstance().getCrossApplyTime(serverId, termId, player.getGuildId());
			if (crossApplyTime > 0L) {
				builder.setLastApplyTime(crossApplyTime);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_FIGHT_INFO_RESP, builder));
	}
	
	/**
	 * 请求礼包记录
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.CROSS_ACTIVITY_GIFT_RECORD_REQ_VALUE)
	private void giftSendRecordReq(HawkProtocol protocol) {
		CrossGiftRecordReq req = protocol.parseProtocol(CrossGiftRecordReq.getDefaultInstance());
		int type = req.getType();
		if (CrossGiftOper.imp(type) == null) {
			return;
		}
		
		CrossGiftRecordResp.Builder builder = CrossGiftRecordResp.newBuilder();
		List<CrossGiftRecord.Builder> records = RedisProxy.getInstance().getAllCrossGiftRecord();
		for (Builder record : records) {
			int giftId = record.getGiftId();
			CrossPackCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossPackCfg.class, giftId);
			if (cfg == null) {
				continue;
			}
			if (cfg.getType() != type) {
				continue;
			}
			builder.addRecord(record);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_ACTIVITY_GIFT_RECORD_RESP_VALUE, builder));
	}
	
	/**
	 * 跨服礼包邀请玩家
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.CROSS_GIFT_SEARCH_C_VALUE)
	private boolean onSearchMember(HawkProtocol protocol) {
		CrossGiftSearchReq request = protocol.parseProtocol(CrossGiftSearchReq.getDefaultInstance());
		if (!request.hasName()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID, 0);
			return false;
		}
		
//		// 禁止输入文本，不能直接调这个接口，否则前端有些场景会出现弹框提示后转菊花的情况
//		if (!GameUtil.checkBanMsg(player)) {
//			return;
//		}
		
		// 禁言玩家推送禁言提示
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
			if (banInfo != null) {
				CrossGiftSearchRes.Builder response = CrossGiftSearchRes.newBuilder();
				response.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_GIFT_SEARCH_S, response));
				return false;
			}
		}

		if (!player.hasGuild()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE, 0);
			return false;
		}

		GameTssService.getInstance().wordUicChatFilter(player, request.getName(), 
				MsgCategory.CROSS_GIFT_SEARCH_MEMBER.getNumber(), GameMsgCategory.CROSS_GIFT_SEARCH_MEMBER, 
				String.valueOf(request.getType()), null, protocol.getType());
		return true;
	}
	

	

	
	
	@ProtocolHandler(code = HP.code2.CROSS_PYLON_OCCUPY_INFO_REQ_VALUE)
	private boolean getCrossPylonOccupyInfo(HawkProtocol protocol) {
		CrossActivityService.getInstance().syncPylonOccupyData(player);
		return true;
	}
	
	
	
	
	
	
	@ProtocolHandler(code = HP.code2.CROSS_ACTIVITY_SEASON_DATA_REQ_VALUE)
	public void getCrossActivitySeasonInfo(HawkProtocol protocol){
		CrossActivitySeasonService.getInstance().syncSeasonData(player);
	}
	
	
	@ProtocolHandler(code = HP.code2.CROSS_ACTIVITY_SEASON_RANK_REQ_VALUE)
	public void getCrossActivitySeasonRank(HawkProtocol protocol){
		CrossActivitySeasonService.getInstance().syncRankData(player);
	}
	
	@ProtocolHandler(code = HP.code2.CROSS_ACTIVITY_SEASON_SCORE_REQ_VALUE)
	public void getCrossActivitySeasonScore(HawkProtocol protocol){
		CrossActivitySeasonService.getInstance().syncScoreData(player);
	}
	
	@ProtocolHandler(code = HP.code2.CROSS_ACTIVITY_PYLON_RANK_REQ_VALUE)
	public void getCrossActivityPlyonRank(HawkProtocol protocol){
		CrossActivityPylonRankReq request = protocol.parseProtocol(CrossActivityPylonRankReq.getDefaultInstance());
		CrossActivityService.getInstance().getPylonRankData(player, request.getServerId());
	}
}
