package com.hawk.game.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import com.hawk.game.invoker.guildTeam.GuildTeamQuitGuildInvoker;
import com.hawk.game.invoker.xhjz.XHJZWarQuitGuildInvoker;
import com.hawk.game.service.guildTeam.GuildTeamService;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GuildDonateEvent;
import com.hawk.activity.type.impl.guildDragonAttack.entity.GuildDragonTrapData;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.config.AllianceOfficialCfg;
import com.hawk.game.config.AllianceSignCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.GuildScienceMainCfg;
import com.hawk.game.config.GuildShopCfg;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.GuildCreateObj;
import com.hawk.game.guild.GuildWarRecord;
import com.hawk.game.guild.championship.ChampionshipService;
import com.hawk.game.guild.voice.VoiceRoomManager;
import com.hawk.game.invoker.ChangeGuildApplyPermition;
import com.hawk.game.invoker.GuildAcceptApplyInvoker;
import com.hawk.game.invoker.GuildAcceptInviteRpcInvoker;
import com.hawk.game.invoker.GuildAddShopItemInvoker;
import com.hawk.game.invoker.GuildApplyOfficerInvoker;
import com.hawk.game.invoker.GuildApplyRpcInvoker;
import com.hawk.game.invoker.GuildAppointOfficerInvoker;
import com.hawk.game.invoker.GuildChangeAuthInfoInvoker;
import com.hawk.game.invoker.GuildChangeFlagRpcInvoker;
import com.hawk.game.invoker.GuildChangeRoomModelInvoker;
import com.hawk.game.invoker.GuildCreateRpcInvoker;
import com.hawk.game.invoker.GuildDemiseLeaderInvoker;
import com.hawk.game.invoker.GuildDismissOfficerInvoker;
import com.hawk.game.invoker.GuildDismissRpcInvoker;
import com.hawk.game.invoker.GuildForbidPostMsgInvoker;
import com.hawk.game.invoker.GuildGetRankInvoker;
import com.hawk.game.invoker.GuildGetTaskRewardRpcInvoker;
import com.hawk.game.invoker.GuildImpeachmentLeaderInvoker;
import com.hawk.game.invoker.GuildKickMemberInvoker;
import com.hawk.game.invoker.GuildQuickJoinRpcInvoker;
import com.hawk.game.invoker.GuildQuitAndEnterNewGuildRpcInvoker;
import com.hawk.game.invoker.GuildQuitRpcInvoker;
import com.hawk.game.invoker.GuildRemoveSignInvoker;
import com.hawk.game.invoker.GuildScienceDonateAfterInvoker;
import com.hawk.game.invoker.GuildScienceRecommendInvoker;
import com.hawk.game.invoker.GuildScienceResearchInvoker;
import com.hawk.game.invoker.GuildShopBuyInvoker;
import com.hawk.game.invoker.NoGuildMemberApplyRecommendInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLMatchService;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.module.material.MeterialTransportService;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.msg.AcceptGuildApplyMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.protocol.Const.SoldierType;
//import com.hawk.game.protocol.Counterattack.PBGuildHospiceSync;
import com.hawk.game.protocol.GuildAssistant.HPAssistantMarchPB;
import com.hawk.game.protocol.GuildAssistant.HPGuildAssistantResp;
import com.hawk.game.protocol.GuildManager.AcceptApplyReq;
import com.hawk.game.protocol.GuildManager.AcceptInviteReq;
import com.hawk.game.protocol.GuildManager.ApplyGuildHelpReq;
import com.hawk.game.protocol.GuildManager.ApplyGuildOfficerReq;
import com.hawk.game.protocol.GuildManager.ApplyGuildReq;
import com.hawk.game.protocol.GuildManager.AppointGuildOfficerReq;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManager.CancelApplyReq;
import com.hawk.game.protocol.GuildManager.CancelForbinPlayerReq;
import com.hawk.game.protocol.GuildManager.ChangeAuthInfoReq;
import com.hawk.game.protocol.GuildManager.ChangeGuildApplyPermitonReq;
import com.hawk.game.protocol.GuildManager.ChangeGuildFlagReq;
import com.hawk.game.protocol.GuildManager.ChangeGuildLevelReq;
import com.hawk.game.protocol.GuildManager.ChangeGuildNameReq;
import com.hawk.game.protocol.GuildManager.ChangeGuildTagReq;
import com.hawk.game.protocol.GuildManager.ChangeLevelNameReq;
import com.hawk.game.protocol.GuildManager.CheckGuildHelpQueueRes;
import com.hawk.game.protocol.GuildManager.CheckGuildNameReq;
import com.hawk.game.protocol.GuildManager.CheckGuildTagReq;
import com.hawk.game.protocol.GuildManager.CreateGuildReq;
import com.hawk.game.protocol.GuildManager.DeadGuildMemberApplyRecommend;
import com.hawk.game.protocol.GuildManager.DimiseLeaderReq;
import com.hawk.game.protocol.GuildManager.DismissGuildOfficerReq;
import com.hawk.game.protocol.GuildManager.DonateRankType;
import com.hawk.game.protocol.GuildManager.EditMainForce;
import com.hawk.game.protocol.GuildManager.ForbidPlayerPostMessageReq;
import com.hawk.game.protocol.GuildManager.GetForbidPlayerListResp;
import com.hawk.game.protocol.GuildManager.GetGuildAssistenceInfoReq;
import com.hawk.game.protocol.GuildManager.GetGuildAssistenceInfoResp;
import com.hawk.game.protocol.GuildManager.GetGuildBBSMessageResp;
import com.hawk.game.protocol.GuildManager.GetGuildInfoResp;
import com.hawk.game.protocol.GuildManager.GetGuildLog;
import com.hawk.game.protocol.GuildManager.GetGuildMemeberInfoReq;
import com.hawk.game.protocol.GuildManager.GetGuildMemeberInfoResp;
import com.hawk.game.protocol.GuildManager.GetGuildMessageReq;
import com.hawk.game.protocol.GuildManager.GetGuildPlayerApplyResp;
import com.hawk.game.protocol.GuildManager.GetOfficerApplyListReq;
import com.hawk.game.protocol.GuildManager.GetOfficerApplyListResp;
import com.hawk.game.protocol.GuildManager.GetOtherGuildReq;
import com.hawk.game.protocol.GuildManager.GetOtherGuildResp;
import com.hawk.game.protocol.GuildManager.GetPlayerGuildApplyResp;
import com.hawk.game.protocol.GuildManager.GetRecommendGuildListReq;
import com.hawk.game.protocol.GuildManager.GetRecommendGuildListResp;
import com.hawk.game.protocol.GuildManager.GetRecommendInvitePlayerResp;
import com.hawk.game.protocol.GuildManager.GetSearchGuildListReq;
import com.hawk.game.protocol.GuildManager.GetSearchGuildListResp;
import com.hawk.game.protocol.GuildManager.GroupOpenIdInfoPB;
import com.hawk.game.protocol.GuildManager.GuildAddSignReq;
import com.hawk.game.protocol.GuildManager.GuildApplyInfo;
import com.hawk.game.protocol.GuildManager.GuildBBSMessage;
import com.hawk.game.protocol.GuildManager.GuildBoundInfoPB;
import com.hawk.game.protocol.GuildManager.GuildGetDonateRank;
import com.hawk.game.protocol.GuildManager.GuildGetDonateRankResp;
import com.hawk.game.protocol.GuildManager.GuildGetRank;
import com.hawk.game.protocol.GuildManager.GuildHelpQueue;
import com.hawk.game.protocol.GuildManager.GuildRankType;
import com.hawk.game.protocol.GuildManager.GuildRemoveSignReq;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.GuildManager.GuildVoiceChangeModelReq;
import com.hawk.game.protocol.GuildManager.HPGetGuildShopInfoResp;
import com.hawk.game.protocol.GuildManager.HPGetGuildShopLogResp;
import com.hawk.game.protocol.GuildManager.HPGuildInfoSync;
import com.hawk.game.protocol.GuildManager.HPGuildLog;
import com.hawk.game.protocol.GuildManager.HPGuildShopBuyReq;
import com.hawk.game.protocol.GuildManager.HPInviteAddShopItem;
import com.hawk.game.protocol.GuildManager.HelpGuildQueueReq;
import com.hawk.game.protocol.GuildManager.HelpGuildQueueRes;
import com.hawk.game.protocol.GuildManager.InviteGuildReq;
import com.hawk.game.protocol.GuildManager.InviteMoveCityReq;
import com.hawk.game.protocol.GuildManager.KickMemberReq;
import com.hawk.game.protocol.GuildManager.MainForceInfo;
import com.hawk.game.protocol.GuildManager.NoGuildMemberApplyRecommend;
import com.hawk.game.protocol.GuildManager.PostAnnouncementReq;
import com.hawk.game.protocol.GuildManager.PostMessageReq;
import com.hawk.game.protocol.GuildManager.PostNoticeReq;
import com.hawk.game.protocol.GuildManager.RefuseApplyReq;
import com.hawk.game.protocol.GuildManager.RefuseInviteReq;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.GuildScience.DonateType;
import com.hawk.game.protocol.GuildScience.GetGuildScienceInfoResp;
import com.hawk.game.protocol.GuildScience.GuildScienceDonateReq;
import com.hawk.game.protocol.GuildScience.GuildScienceRecommendReq;
import com.hawk.game.protocol.GuildScience.GuildScienceResearchReq;
import com.hawk.game.protocol.GuildTask.GuildTaskTakeRewardReq;
import com.hawk.game.protocol.GuildWar.GuildWarShowPB;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.HPGetGuildWarInfoReq;
import com.hawk.game.protocol.GuildWar.HPGetGuildWarInfoResp;
import com.hawk.game.protocol.GuildWar.HPGetGuildWarSingleInfoReq;
import com.hawk.game.protocol.GuildWar.HPGuildWarRecordPB;
import com.hawk.game.protocol.GuildWar.HPGuildWarRecordResp;
import com.hawk.game.protocol.GuildWar.HPWarPlayerInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Mail.GuildInviteMail;
import com.hawk.game.protocol.Mail.HPCheckMailRes;
import com.hawk.game.protocol.Mail.InviteState;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.GetPlayerBasicInfoReq;
import com.hawk.game.protocol.Player.GetPlayerBasicInfoResp;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.IDIPErrorCode;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.XZQ.PBXZQStatus;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.guildtask.event.GuildDonateTaskEvent;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventBuyInGuildShop;
import com.hawk.game.service.mssion.event.EventGuildContribute;
import com.hawk.game.service.simulatewar.SimulateWarService;
import com.hawk.game.service.simulatewar.msg.SimulateWarQuitGuildMsg;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.util.GsConst.GlobalControlType;
import com.hawk.game.util.GsConst.GuildConst;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.GuildUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildAction;
import com.hawk.log.LogConst.GuildOperType;
import com.hawk.log.LogConst.GuildTechOperType;
import com.hawk.log.Source;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.SDKManager;
import com.hawk.sdk.config.PlatformConstCfg;
import com.hawk.sdk.msdk.entity.PayItemInfo;

import io.netty.util.internal.StringUtil;

/**
 * 玩家联盟管理模块
 * 
 * @author shadow
 *
 */
public class PlayerGuildModule extends PlayerModule {
	/**
	 * 上次刷新时间
	 */
	private long lastRefreshTime = 0;

	private long invitationLetterPushTime = 0;

	public PlayerGuildModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		if (player.isCsPlayer()) {
			HPGuildInfoSync.Builder builder = GuildService.getInstance().buildCGuildSyncInfo(player);
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUILD_BASIC_INFO_SYNC_S, builder);
			player.sendProtocol(protocol);
			player.getPush().syncGuildWarCount();
		} else {
			player.getPush().syncGuildInfo();
			if (player.hasGuild()) {
				GuildService.getInstance().checkCrossDay(player);
				GuildService.getInstance().checkDonateTimesAdd(player);
				// 获取QQ公会绑群groupOpenid信息
				if (UserType.getByChannel(player.getChannel()) == UserType.QQ) {
					fetchQQGroupOpenid();
				}
				GuildService.getInstance().checkAndResetTask(player);
				// 同步联盟任务信息
				GuildService.getInstance().syncPlayerGuildTaskInfo(player);
				GuildService.getInstance().syncGuildRewardFlagInfo(player);
			}
			initInvitationLetterPush();
			player.getPush().syncGuildWarCount();
		}

		return true;
	}

	/**
	 * 更新
	 */
	@Override
	public boolean onTick() {
		if (!player.isCsPlayer()) {
			long currentTime = HawkApp.getInstance().getCurrentTime();
			if (currentTime >= lastRefreshTime + 1000) {
				lastRefreshTime = currentTime;
				if (player.hasGuild()) {
					// 联盟捐献跨天检测
					GuildService.getInstance().checkCrossDay(player);
					// 捐献次数恢复检测
					GuildService.getInstance().checkDonateTimesAdd(player);
				}
			}
			if (currentTime >= invitationLetterPushTime) {
				invitationLetterPushTime = Long.MAX_VALUE;
				GuildService.getInstance().pushInvitationLetter(player);
			}
		}
		return super.onTick();
	}

	/**
	 * 创建联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CREATE_C_VALUE)
	private boolean onCreateGuild(HawkProtocol protocol) {
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
				
		CreateGuildReq req = protocol.parseProtocol(CreateGuildReq.getDefaultInstance());
		
		// 跨服状态下该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.CREAT_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}

		if (player.getCityLv() > GuildConstProperty.getInstance().getCreateGuildCostGoldLevel()) {
			if (HawkTime.getMillisecond() - GuildService.getInstance().getPlayerQuitGuildTime(player.getId()) < GuildConstProperty.getInstance().getAllianceJoinCooldownTime()) {
				sendError(protocol.getType(), Status.Error.GUILD_QUITTIME_ILLEGAL);
				return false;
			}
		}
		
		String name = req.getName();
		int checkResult = GuildUtil.checkGuildName(name);
		if (checkResult != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), checkResult);
			return false;
		}

		if (req.hasAnnouncement() && !GuildUtil.checkAnnouncement(req.getAnnouncement())) {
			sendError(protocol.getType(), Status.Error.GUILD_ANNOUNCEMENT_ILLEGAL);
			return false;
		}
		String announcement = GuildUtil.filterEmoji(req.getAnnouncement());
		
		StringBuilder content = new StringBuilder();
		content.append(req.getName()).append(announcement);
		JSONObject json = new JSONObject();
		json.put("name", req.getName());
		json.put("announce", announcement);
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", "");
		
		GameTssService.getInstance().wordUicChatFilter(player, content.toString(), 
				MsgCategory.GUILD_NAME.getNumber(), GameMsgCategory.CREATE_GUILD, 
				json.toJSONString(), gameDataJson, protocol.getType());
		return true;
	}
	
	/**
	 * 根据规则随机联盟简称并创建联盟
	 * @param obj
	 */
	public void tryGuildCreate(GuildCreateObj obj) {
		int maxTryTime = 26 * 26 * 26 + 26 * 26 + 26;
		for (int i = 0; i <= maxTryTime; i++) {
			// 随机简称
			String tag = obj.randomTag();
			int checkResult = GuildUtil.checkGuildTag(tag);
			if (checkResult == Status.SysError.SUCCESS_OK_VALUE) {
				player.rpcCall(MsgId.GUILD_CREATE, GuildService.getInstance(), new GuildCreateRpcInvoker(player, obj));
				break;
			}
		}
	}

	/**
	 * 检测联盟名称
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CHECKNAME_C_VALUE)
	private boolean onCheckGuildName(HawkProtocol protocol) {
		CheckGuildNameReq req = protocol.parseProtocol(CheckGuildNameReq.getDefaultInstance());
		GameTssService.getInstance().wordUicChatFilter(player, req.getGuildName(), 
				MsgCategory.GUILD_NAME_CHANGE.getNumber(), GameMsgCategory.CHECK_GUILD_NAME, 
				"", protocol.getType(), getChangeGuildGameData());
		return true;
	}

	/**
	 * 检测联盟简称
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CHECKTAG_C_VALUE)
	private boolean onCheckGuildTag(HawkProtocol protocol) {
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", "");
				
		CheckGuildTagReq req = protocol.parseProtocol(CheckGuildTagReq.getDefaultInstance());
		GameTssService.getInstance().wordUicChatFilter(player, req.getGuildTag(), 
				MsgCategory.GUILD_TAG.getNumber(), GameMsgCategory.CHECK_GUILD_TAG, 
				"", gameDataJson, protocol.getType());
		return true;
	}

	/**
	 * 改变联盟名称
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CHANGENAME_C_VALUE)
	private boolean onChangeGuildName(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_GUILD_NAME);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.EDIT_ALLIANCE_NAME)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 禁止全服修改联盟名称
		if (GlobalData.getInstance().isGlobalBan(GlobalControlType.CHANGE_GUILD_NAME)) {
			String reason = GlobalData.getInstance().getGlobalBanReason(GlobalControlType.CHANGE_GUILD_NAME);
			if (HawkOSOperator.isEmptyString(reason)) {
				sendError(protocol.getType(), SysError.GLOBAL_BAN_CHANGE_GUILD_NAME);
			} else {
				player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, 0, reason);
			}
			return false;
		}
		
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		int checkResult = GameUtil.changeContentCDCheck(player.getGuildId(), ChangeContentType.CHANGE_GUILD_NAME);
		if (checkResult < 0) {
			sendError(protocol.getType(), IDIPErrorCode.CHANGE_GUILD_NAME_CD_ING);
			return false;
		}
		
		int changeNameGold = GuildConstProperty.getInstance().getChangeGuildNameGold();
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GOLD, changeNameGold);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		
		ChangeGuildNameReq req = protocol.parseProtocol(ChangeGuildNameReq.getDefaultInstance());
		GameTssService.getInstance().wordUicChatFilter(player, req.getGuildName(), 
			    MsgCategory.GUILD_NAME_CHANGE.getNumber(), GameMsgCategory.CHANGE_GUILD_NAME, 
				String.valueOf(checkResult), protocol.getType(), getChangeGuildGameData());
		return true;
	}

	/**
	 * 获取修改联盟名字传ugc sdk的游戏数据
	 * @return
	 */
	private String getChangeGuildGameData() {
		String pfIconPrimitive = player.getData().getPrimitivePfIcon();
		if (GlobalData.getInstance().isBanPortraitAccount(player.getOpenId())) {
			pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("client_version=").append(player.getAppVersion())
		       .append("&user_ip=").append(player.getClientIp())
		       .append("&role_battlepoint=").append(player.getPower())
		       .append("&role_head_url=").append(pfIconPrimitive)
		       .append("&role_group_id=").append(player.getGuildId())
		       .append("&role_total_cash=").append(player.getRechargeTotal())
		       .append("&role_vip_level=").append(player.getVipLevel())
		       .append("&msg_type=").append(0)
		       .append("&charac_no=").append(player.getId())
		       .append("&alliance_id=").append(player.hasGuild() ? player.getGuildId() : "")
		       .append("&param_id=").append("");
		return builder.toString();
	}
	
	/**
	 * 改变联盟简称
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CHANGETAG_C_VALUE)
	private boolean onChangeGuildTag(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_GUILD_TAG);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.EDIT_ALLIANCE_SHORTNAME)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
		
		int changeTagGold = 0;
		if (guild.isHasChangeTag()) {
			changeTagGold = GuildConstProperty.getInstance().getChangeGuildTagGold();
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GOLD, changeTagGold);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", "");
		
		ChangeGuildTagReq req = protocol.parseProtocol(ChangeGuildTagReq.getDefaultInstance());
		GameTssService.getInstance().wordUicChatFilter(player, req.getGuildTag(), 
				MsgCategory.GUILD_TAG.getNumber(), GameMsgCategory.CHANGE_GUILD_TAG, 
				"", gameDataJson, protocol.getType());
		return true;
	}

	/**
	 * 改变联盟旗帜
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CHANGEFLAG_C_VALUE)
	private boolean onChangeGuildFlag(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.EDIT_ALLIANCE_FLAG)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}	
		ChangeGuildFlagReq req = protocol.parseProtocol(ChangeGuildFlagReq.getDefaultInstance());
		int errorCode = GuildUtil.checkFlag(player.getGuildId(), req.getGuildFlag());
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), errorCode);
			return false;
		}
		
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
		if(guild == null || guild.getFlagId() == req.getGuildFlag()){
			sendError(protocol.getType(), Status.SysError.DATA_ERROR);
			return false;
		}
		int changeFlagGold = GuildConstProperty.getInstance().getChangeGuildFlagGold();
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GOLD, changeFlagGold);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}

		player.rpcCall(MsgId.CHANGE_GUILD_FLAG, GuildService.getInstance(),
				new GuildChangeFlagRpcInvoker(player, req.getGuildFlag(), consume, protocol.getType()));
		return true;
	}

	/**
	 * 改变联盟申请权限
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CHANGEAPPLYPERMITON_C_VALUE)
	private boolean onChangeGuildApplyPermition(HawkProtocol protocol) {
		ChangeGuildApplyPermitonReq req = protocol.parseProtocol(ChangeGuildApplyPermitonReq.getDefaultInstance());
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.EDIT_PUBLIC_RECRUIT)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		if (req.hasCommonderLevel() && req.getCommonderLevel() < 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		if (req.hasPower() && req.getPower() < 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		if (req.hasBuildingLevel() && req.getBuildingLevel() < 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		final boolean isOpen = req.getIsOpen();
		final int buildingLvl = req.hasBuildingLevel() ? req.getBuildingLevel() : -1;
		final int power = req.hasPower() ? req.getPower() : -1;
		final int commonderLvl = req.hasCommonderLevel() ? req.getCommonderLevel() : -1;
		final String lang = req.hasLang() ? req.getLang() : null;
		GuildService.getInstance().dealMsg(MsgId.CHANGE_GUILD_AUTH,
				new ChangeGuildApplyPermition(player, isOpen, buildingLvl, power, commonderLvl, lang, protocol.getType()));
		return true;
	}

	/**
	 * 公开招募
	 * 
	 * @param protocol
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@ProtocolHandler(code = HP.code.GUILDMANAGER_SENDRECRUITNOTICE_C_VALUE)
	private boolean onSendOpenRecurit(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.PUBLICRECRUIT)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(GuildConstProperty.getInstance().getPublicRecruitCost(), false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		consume.consumeAndPush(player, Action.GUILD_SENDOPENRECRUIT);
		String guildId = player.getGuildId();
		int flag = GuildService.getInstance().getGuildFlag(guildId);
		String guildName = GuildService.getInstance().getGuildName(guildId);
		String guildTag = GuildService.getInstance().getGuildTag(guildId);
		long battlePoint = GuildService.getInstance().getGuildBattlePoint(guildId);
		long curCount = GuildService.getInstance().getGuildMemberNum(guildId);
		long maxCount = GuildService.getInstance().getGuildMemberMaxNum(guildId);

		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.GUILD_RECURIT, player,
				guildName, guildId, guildTag, flag, battlePoint, curCount, maxCount);
		player.responseSuccess(protocol.getType());

		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_SENDOPENRECRUIT, Params.valueOf("guildId", player.getGuildId()));
		return true;
	}

	/**
	 * 获得推荐联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_RECOMMEND_C_VALUE)
	private boolean onGetRecommendGuild(HawkProtocol protocol) {
		if (player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADYJOIN);
			return false;
		}
		GetRecommendGuildListReq req = protocol.parseProtocol(GetRecommendGuildListReq.getDefaultInstance());
		GetRecommendGuildListResp.Builder builder = GuildService.getInstance().onGetRecommendGuild(req.getPageNum(), player.getLanguage());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_RECOMMEND_S, builder));
		return true;
	}
	
	/**
	 * 一键加入联盟
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_QUICK_JOIN_C_VALUE)
	private boolean onQuickEnterGuild(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.CREAT_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADYJOIN);
			return false;
		}
		
		// 跨服状态下该操作不可进行
		if (player.isCsPlayer()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		if (player.getCityLv() > GuildConstProperty.getInstance().getCreateGuildCostGoldLevel()) {
			if (HawkTime.getMillisecond() - GuildService.getInstance().getPlayerQuitGuildTime(player.getId()) < GuildConstProperty.getInstance().getAllianceJoinCooldownTime()) {
				sendError(protocol.getType(), Status.Error.GUILD_QUITTIME_ILLEGAL);
				return false;
			}
		}
		
		player.rpcCall(MsgId.QUICK_JOIN_GUILD, GuildService.getInstance(), new GuildQuickJoinRpcInvoker(player, protocol.getType()));
		return true;		
	}
	
	/***
	 * 无盟玩家响应联盟推荐（一键加入指定推荐联盟）
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.NOGUILD_PLAYER_APPLY_GUILD_RECOMMEND_VALUE)
	private boolean onNoGuildPlayerApplyRecommend(HawkProtocol protocol){
		NoGuildMemberApplyRecommend req = protocol.parseProtocol(NoGuildMemberApplyRecommend.getDefaultInstance());
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.CREAT_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADYJOIN);
			return false;
		}
		
		// 跨服状态下该操作不可进行
		if (player.isCsPlayer()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		if (player.getCityLv() > GuildConstProperty.getInstance().getCreateGuildCostGoldLevel()) {
			if (HawkTime.getMillisecond() - GuildService.getInstance().getPlayerQuitGuildTime(player.getId()) < GuildConstProperty.getInstance().getAllianceJoinCooldownTime()) {
				sendError(protocol.getType(), Status.Error.GUILD_QUITTIME_ILLEGAL);
				return false;
			}
		}
		if (GuildService.getInstance().containGuildApply(player.getId(), req.getGuildId())) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADY_APPLY_VALUE);
			return false;
		}
		GuildApplyInfo.Builder applyInfo = GuildApplyInfo.newBuilder();
		applyInfo.setPlayerId(player.getId());
		applyInfo.setPlayerName(player.getName());
		applyInfo.setPower(player.getPower());
		applyInfo.setVip(player.getVipLevel());
		applyInfo.setLanguage(player.getLanguage());
		applyInfo.setIcon(player.getIcon());
		applyInfo.setCommonderLevel(player.getLevel());
		applyInfo.setBuildingLevel(player.getCityLv());
		applyInfo.setVipStatus(player.getData().getVipActivated());
		applyInfo.setKillEnemy(player.getData().getStatisticsEntity().getArmyKillCnt());
		applyInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		String pfIcon = player.getPfIcon();
		if (!HawkOSOperator.isEmptyString(pfIcon)) {
			applyInfo.setPfIcon(pfIcon);
		}
		player.rpcCall(MsgId.APPLY_GUILD, GuildService.getInstance(), new NoGuildMemberApplyRecommendInvoker(player, req.getGuildId(), applyInfo, protocol.getType()));
		LogUtil.logGuildAction(GuildAction.GUILD_APPLAY, req.getGuildId());
		return true;
	}

	/***
	 * 死盟玩家响应联盟推荐
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.DEADGUILD_PLAYER_APPLY_GUILD_RECOMMEND_VALUE)
	private boolean onDeadGuildPlayerApplyRecommend(HawkProtocol protocol){
		DeadGuildMemberApplyRecommend req = protocol.parseProtocol(DeadGuildMemberApplyRecommend.getDefaultInstance());
		int result = req.getResult();
		if(result == 0){
			//拒绝加入新的联盟
			DailyDataEntity dailyEntity = player.getData().getDailyDataEntity();
			dailyEntity.setDeadGuildRefuseRecommendCnt(dailyEntity.getDeadGuildRefuseRecommendCnt() + 1);
			return true;
		}else{
			if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.QUIT_ALLIANCE)) {
				sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
				return false;
			}
			
			if (PresidentFightService.getInstance().isProvisionalPresident(player.getId())) {
				sendError(protocol.getType(), Status.Error.PROVISIONAL_PRESIDENT_CAN_NOT_QUIT_VALUE);
				return false;
			}

			if (!WorldMarchService.getInstance().checkCanQuitGuild(player.getId())) {
				sendError(protocol.getType(), Status.Error.GUILD_ALREADY_MARCH_QUIT);
				return false;
			}
			
			if (SuperWeaponService.getInstance().getStatus() == SuperWeaponPeriod.WARFARE_VALUE) {
				sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_CANNOT_QUIT_GUILD);
				return false;
			}
			
			if (XZQService.getInstance().getState() == PBXZQStatus.XZQ_OPEN) {
				sendError(protocol.getType(), Status.XZQError.XZQ_CANNOT_QUIT_GUILD);
				return false;
			}
			String reqGuildId = req.getGuildId();
			final String guildId = player.getGuildId();
			// 星球大战参与联盟特定阶段不能进行人员操作
			if (!StarWarsActivityService.getInstance().checkGuildMemberOps(guildId)) {
				sendError(protocol.getType(),  Status.Error.SW_GUILD_OPS_LIMIT);
				return false;
			}
			
			int cyborgCheckResult = CyborgWarService.getInstance().checkGuildMemberOps(reqGuildId, player.getId(), false);
			if(cyborgCheckResult != Status.SysError.SUCCESS_OK_VALUE){
				sendError(protocol.getType(),  cyborgCheckResult);
				return false;
			}
			// 跨服状态下该操作不可进行
			if (player.isCsPlayer()) {
				sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
				return false;
			}

			if(!XHJZWarService.getInstance().checkGuildOperation(null, player.getId())){
				sendError(protocol.getType(), Status.XHJZError.XHJZ_GUILD_OPERATION_FORBID);
				return false;
			}
			
			//小队管理检查
			int checkRlt = GuildTeamService.getInstance().checkGuildOperation(null, player.getId());
			if(checkRlt != Result.SUCCESS_VALUE){
				sendError(protocol.getType(), checkRlt);
				return false;
			}


			// 已参与本期泰伯利亚之战
			if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
				sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
				return false;
			}
			
			if(WarCollegeInstanceService.getInstance().getTeamId(player.getId()) != null){
				sendError(protocol.getType(),  Status.Error.GUILD_QUIT_IN_WAR_COLLEGE_VALUE);
				return false;
			}
			
			player.rpcCall(MsgId.GUILD_QUIT_GUILD, GuildService.getInstance(),
					new GuildQuitAndEnterNewGuildRpcInvoker(player, guildId, reqGuildId, protocol.getType()));
			return true;
		}
	}
	/**
	 * 获得其他联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_GETOTHERGUILD_C_VALUE)
	private boolean onGetOtherGuild(HawkProtocol protocol) {
		GetOtherGuildReq req = protocol.parseProtocol(GetOtherGuildReq.getDefaultInstance());
		GetOtherGuildResp.Builder builder = GuildService.getInstance().onGetOtherGuild(player, req.getPageNum());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETOTHERGUILD_S_VALUE, builder));
		return true;
	}

	/**
	 * 搜索联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_SEARCH_C_VALUE)
	private boolean onSearchLocalGuild(HawkProtocol protocol) {
//		// 禁止输入文本，不能直接调这个接口，否则前端有些场景会出现弹框提示后转菊花的情况
//		if (!GameUtil.checkBanMsg(player)) {
//			return false;
//		}
		
		// 禁言玩家推送禁言提示
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
			if (banInfo != null) {
				GetSearchGuildListResp.Builder builder = GetSearchGuildListResp.newBuilder();
				builder.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_SEARCH_S, builder));
				return false;
			}
		}
				
		GetSearchGuildListReq req = protocol.parseProtocol(GetSearchGuildListReq.getDefaultInstance());
		final String name = req.getName();
		if (HawkOSOperator.isEmptyString(name)) {
			sendError(protocol.getType(), Status.NameError.NAME_BLANK_ERROR_VALUE);
			return false;
		}
		
		GameTssService.getInstance().wordUicChatFilter(player, name, 
				MsgCategory.SEARCH_ADD_GUILD.getNumber(), GameMsgCategory.SEARCH_GUILD, 
				"", null, protocol.getType());
		return true;
	}

	/**
	 * 获得联盟信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_GETINFO_C_VALUE)
	private boolean onGetGuildInfo(HawkProtocol protocol) {
		GetGuildMemeberInfoReq req = protocol.parseProtocol(GetGuildMemeberInfoReq.getDefaultInstance());
		
		if (req.hasPlayerId() && !HawkOSOperator.isEmptyString(req.getPlayerId()) 
				&& !RelationService.getInstance().isInGamePlatformFriend(player, req.getPlayerId())) {
			sendError(protocol.getType(), Status.Error.GUILD_INFO_NOT_INGAME_FRIEND_VALUE);
			return false;
		}
		
		String guildId = null;
		boolean self = false;
		if (req.hasGuildId()) {
			guildId = req.getGuildId();
			if (player.hasGuild() && player.getGuildId().equals(req.getGuildId())) {
				self = true;
			}
		} else if (player.hasGuild()) {
			guildId = player.getGuildId();
			self = true;
		} else {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_EXIST);
			return false;
		}
		GetGuildInfoResp.Builder builder = GuildService.getInstance().getGuildInfo(guildId, self);
		if (builder == null) {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_EXIST);
			return false;
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETINFO_S, builder));
		return true;
	}

	/**
	 * 获得联盟成员信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_GETMEMBERINFO_C_VALUE)
	private boolean onGetGuildMemberInfo(HawkProtocol protocol) {
		GetGuildMemeberInfoReq req = protocol.parseProtocol(GetGuildMemeberInfoReq.getDefaultInstance());
		String guildId = null;
		boolean needName = false;
		if (req.hasGuildId()) {
			guildId = req.getGuildId();
			needName = true;
		} else if (player.hasGuild()) {
			guildId = player.getGuildId();
		} else {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_EXIST);
			return false;
		}
		GetGuildMemeberInfoResp.Builder builder = GuildService.getInstance().getGuildMemberInfo(guildId, needName);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETMEMBERINFO_S, builder));
		return true;
	}

	/**
	 * 获得个人联盟申请信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_GETPLAYERAPPLY_C_VALUE)
	private boolean onGetPlayerGuildApplyInfo(HawkProtocol protocol) {
		GetPlayerGuildApplyResp.Builder builder = GetPlayerGuildApplyResp.newBuilder();
		Set<String> guildIds = LocalRedis.getInstance().getPlayerGuildApply(player.getId());
		for (String guildId : guildIds) {
			GetGuildInfoResp.Builder guildInfo = GuildService.getInstance().getGuildInfo(guildId);
			if (guildInfo != null) {
				builder.addInfo(guildInfo);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETPLAYERAPPLY_S_VALUE, builder));
		return true;
	}

	/**
	 * 获得联盟申请信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_GETAPPLY_C_VALUE)
	private boolean onGetGuildApplyInfo(HawkProtocol protocol) {
		List<String> applys = LocalRedis.getInstance().getGuildPlayerApply(player.getGuildId());
		GetGuildPlayerApplyResp.Builder builder = GetGuildPlayerApplyResp.newBuilder();
		for (String string : applys) {
			GuildApplyInfo.Builder info = GuildApplyInfo.newBuilder();
			try {
				JsonFormat.merge(string, info);
				builder.addInfo(info);
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETAPPLY_S, builder));
		return true;
	}

	/**
	 * 申请联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_APPLY_C_VALUE)
	private boolean onApplyGuild(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.CREAT_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADYJOIN);
			return false;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		if (player.getCityLv() > GuildConstProperty.getInstance().getCreateGuildCostGoldLevel()) {
			if (HawkTime.getMillisecond() - GuildService.getInstance().getPlayerQuitGuildTime(player.getId()) < GuildConstProperty.getInstance().getAllianceJoinCooldownTime()) {
				sendError(protocol.getType(), Status.Error.GUILD_QUITTIME_ILLEGAL);
				return false;
			}
		}

		ApplyGuildReq req = protocol.parseProtocol(ApplyGuildReq.getDefaultInstance());

		if (GuildService.getInstance().containGuildApply(player.getId(), req.getGuildId())) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADY_APPLY_VALUE);
			return false;
		}

		GuildApplyInfo.Builder applyInfo = GuildApplyInfo.newBuilder();
		applyInfo.setPlayerId(player.getId());
		applyInfo.setPlayerName(player.getName());
		applyInfo.setPower(player.getPower());
		applyInfo.setVip(player.getVipLevel());
		applyInfo.setLanguage(player.getLanguage());
		applyInfo.setIcon(player.getIcon());
		applyInfo.setCommonderLevel(player.getLevel());
		applyInfo.setBuildingLevel(player.getCityLv());
		applyInfo.setVipStatus(player.getData().getVipActivated());
		applyInfo.setKillEnemy(player.getData().getStatisticsEntity().getArmyKillCnt());
		applyInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		String pfIcon = player.getPfIcon();
		if (!HawkOSOperator.isEmptyString(pfIcon)) {
			applyInfo.setPfIcon(pfIcon);
		}

		player.rpcCall(MsgId.APPLY_GUILD, GuildService.getInstance(), new GuildApplyRpcInvoker(player, req.getGuildId(), applyInfo, protocol.getType()));

		LogUtil.logGuildAction(GuildAction.GUILD_APPLAY, req.getGuildId());
		return true;
	}

	/**
	 * 取消联盟申请
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CANCELAPPLY_C_VALUE)
	private boolean onCancelApplyGuild(HawkProtocol protocol) {
		CancelApplyReq req = protocol.parseProtocol(CancelApplyReq.getDefaultInstance());
		if (!GuildService.getInstance().containGuildApply(player.getId(), req.getGuildId())) {
			sendError(protocol.getType(), Status.Error.GUILD_APPLY_NOTEXIST);
			return false;
		}
		String guildId = req.getGuildId();
		LocalRedis.getInstance().removePlayerGuildApply(player.getId(), guildId);
		LocalRedis.getInstance().removeGuildPlayerApply(req.getGuildId(), player.getId());
		player.responseSuccess(protocol.getType());

		GuildService.getInstance().pushApplayNum(guildId);

		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CANCELAPPLY, Params.valueOf("guildId", req.getGuildId()));
		return true;
	}

	/**
	 * 同意联盟申请
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_ACCEPTAPPLY_C_VALUE)
	private boolean onAcceptApplyGuild(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.INVITE_TO_JOIN_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		AcceptApplyReq req = protocol.parseProtocol(AcceptApplyReq.getDefaultInstance());

		GuildService.getInstance().dealMsg(MsgId.ACCEPT_APPLY_GUILD, new GuildAcceptApplyInvoker(player, req.getPlayerId(), protocol.getType()));

		LogUtil.logGuildAction(GuildAction.GUILD_DO_APPLAY, player.getGuildId());
		return true;
	}

	/**
	 * 同意联盟申请
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onAcceptGuildApplyMsg(AcceptGuildApplyMsg msg) {
		String guildId = msg.getGuildId();
		player.joinGuild(guildId, false);
		LogUtil.logGuildFlow(player, GuildOperType.GUILD_JOIN, guildId, null);

		if (player.isActiveOnline()) {
			player.getPush().syncGuildInfo();
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_ACCEPTAPPLY_SYNC_S_VALUE));
			// 推送联盟领地信息
			GuildManorList.Builder manorbuilder = GuildManorService.getInstance().makeManorListBuilder(player.getGuildId());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, manorbuilder));
			// 推送联盟战争条数
			player.getPush().syncGuildWarCount();
		}
		return true;
	}

	/**
	 * 拒绝联盟申请
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_REFUSEAPPLY_C_VALUE)
	private boolean onRefuseApplyGuild(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.INVITE_TO_JOIN_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		String guildId = player.getGuildId();
		RefuseApplyReq req = protocol.parseProtocol(RefuseApplyReq.getDefaultInstance());
		LocalRedis.getInstance().removeGuildPlayerApply(guildId, req.getPlayerId());
		LocalRedis.getInstance().removePlayerGuildApply(req.getPlayerId(), guildId);
		GuildService.getInstance().pushApplayNum(guildId);
		// 发送邮件---加入联盟被拒绝
		String guildName = GuildService.getInstance().getGuildName(guildId);
		int icon = GuildService.getInstance().getGuildFlag(guildId);
		GuildMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(req.getPlayerId())
				.setMailId(MailId.JOIN_GUILD_BE_REJECTED)
				.addSubTitles(guildName)
				.addContents(GameUtil.guild4MailContents(guildId))
				.setIcon(icon)
				.build());

		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_REFUSEAPPLY,
				Params.valueOf("guildId", guildId), Params.valueOf("targetPlayer", req.getPlayerId()));
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 获得推荐邀请玩家
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_GETRECOMMANDINVITE_C_VALUE)
	private boolean onGetRecommandInvitePlayer(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.INVITE_TO_JOIN_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		List<String> recommendPlayerIds = GlobalData.getInstance().getRecommendInvitePlayers();
		GetRecommendInvitePlayerResp.Builder builder = getRecommendInvitePlayers(player.getGuildId(), recommendPlayerIds);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETRECOMMANDINVITE_S_VALUE, builder));
		return true;
	}

	/**
	 * 获得推荐加入联盟玩家
	 * 
	 * @param page
	 * @return
	 */
	private GetRecommendInvitePlayerResp.Builder getRecommendInvitePlayers(String guildId, List<String> playerIds) {

		GetRecommendInvitePlayerResp.Builder builder = GetRecommendInvitePlayerResp.newBuilder();
		if (playerIds == null || playerIds.size() <= 0) {
			return builder;
		}
		int count = 0;
		int maxCount = GuildConstProperty.getInstance().getSearchPlayerNumber();
		for (String playerId : playerIds) {
			if (count >= maxCount) {
				break;
			}
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player == null || player.hasGuild()) {
				continue;
			}
			// 被邀请等级限制
			if (player.getCityLevel() < GuildConstProperty.getInstance().getBeInviteLevelLimit()) {
				continue;
			}
			// 被邀请cd
			if (HawkTime.getMillisecond() - GuildService.getInstance().getBeInviteTime(player.getId()) < GuildConstProperty.getInstance().getBeInviteVipCD()) {
				continue;
			}
			if(!player.isActiveOnline()){
				long logoutTime = player.getLogoutTime();
				int hours = HawkTime.getHoursInterval(HawkTime.getMillisecond(), logoutTime);
				if(hours >= GuildConstProperty.getInstance().getGuildRecommendLogoutTime()){
					GlobalData.getInstance().removeNoGuildPlayer(playerId);
					continue;
				}
			}
			GuildApplyInfo.Builder info = GuildApplyInfo.newBuilder();
			info.setPlayerId(player.getId());
			info.setPlayerName(player.getName());
			info.setPower(player.getPower());
			info.setIcon(player.getIcon());
			info.setLanguage(player.getLanguage());
			info.setVip(player.getVipLevel());
			info.setPfIcon(player.getPfIcon());
			info.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
			builder.addInfo(info);
			count++;
		}
		return builder;
	}

	/**
	 * 邀请加入联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_INVITE_C_VALUE)
	private boolean onGuildInvite(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_GUILD_INVITE);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.INVITE_TO_JOIN_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 跨服状态下该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		InviteGuildReq req = protocol.parseProtocol(InviteGuildReq.getDefaultInstance());
		if (!RelationService.getInstance().isInGamePlatformFriend(player, req.getPlayerId())) {
			sendError(protocol.getType(), Status.Error.GUILD_INVITE_NOT_INGAME_FRIEND_VALUE);
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(GuildService.getInstance().getPlayerGuildId(req.getPlayerId()))) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADYJOIN_VALUE);
			return false;
		}

		if (GlobalData.getInstance().isResetAccount(req.getPlayerId())) {
			sendError(protocol.getType(), Status.Error.GUILD_INVITE_NOT_INGAME_FRIEND_VALUE);
			return false;
		}
		
		int result = GuildService.getInstance().invitePlayer(player, req.getPlayerId());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
			LogUtil.logGuildAction(GuildAction.GUILD_INVIT, player.getGuildId());
			GuildService.getInstance().updateBeInviteTime(req.getPlayerId());
		} else {
			sendError(protocol.getType(), result);
		}
		return true;
	}

	/**
	 * 同意加入邀请联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_ACCEPTINVITE_C_VALUE)
	private boolean onAcceptGuildInvite(HawkProtocol protocol) {
		AcceptInviteReq req = protocol.parseProtocol(AcceptInviteReq.getDefaultInstance());
		if (player.hasGuild()) {
			return false;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		String mailId = req.getMailId();
		// 获取邮件
		MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(mailId);
		if (Objects.isNull(mail) || mail.getMailId() != MailId.GUILD_INVITE_VALUE) {
			sendError(protocol.getType(), Status.Error.MAIL_NOT_EXIST);
			return false;
		}
		long overTime = ConstProperty.getInstance().getAllianceInvitationOverTime();
		// 邀请函已过期
		if (HawkTime.getMillisecond() > mail.getCtime() + overTime) {
			sendError(protocol.getType(), Status.Error.GUILD_INVITE_OVER_TIME);
			return false;
		}
		HPCheckMailRes.Builder content = MailService.getInstance().createHPCheckMailResBuilder(player.getId(), mail);
		GuildInviteMail.Builder builder = content.getGuildInviteMailBuilder();

		if (builder.getInviteState() != InviteState.NOTDEAL) {
			sendError(protocol.getType(), Status.Error.GUILD_INVITE_DEALED);
			return false;
		}

		final String guildId = builder.getGuildId();
		if (player.getCityLv() > GuildConstProperty.getInstance().getCreateGuildCostGoldLevel()) {
			long edgeTime = HawkTime.getMillisecond() - GuildService.getInstance().getPlayerQuitGuildTime(player.getId());
			if (edgeTime < GuildConstProperty.getInstance().getAllianceJoinCooldownTime()) {
				sendError(protocol.getType(), Status.Error.GUILD_QUITTIME_ILLEGAL);
				return false;
			}
		}
		player.rpcCall(MsgId.ACCEPT_INVITE_APPLY_GUILD, GuildService.getInstance(),
				new GuildAcceptInviteRpcInvoker(player, guildId, mail, builder, protocol.getType()));
		return true;
	}

	/**
	 * 拒绝加入邀请联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_REFUSEINVITE_C_VALUE)
	private boolean onRefuseGuildInvite(HawkProtocol protocol) {
		RefuseInviteReq req = protocol.parseProtocol(RefuseInviteReq.getDefaultInstance());
		String mailId = req.getMailId();
		// 获取邮件
		MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(mailId);
		if (Objects.isNull(mail) || mail.getMailId() != MailId.GUILD_INVITE_VALUE) {
			sendError(protocol.getType(), Status.Error.MAIL_NOT_EXIST);
			return false;
		}

		HPCheckMailRes.Builder content = MailService.getInstance().createHPCheckMailResBuilder(player.getId(), mail);
		GuildInviteMail.Builder builder = content.getGuildInviteMailBuilder();
		builder.setInviteState(InviteState.REFUSED);
		MailService.getInstance().updateMailContent(mail.build(), MailParames.newBuilder().addContents(builder).setMailId(MailId.GUILD_INVITE).build().getContent());

		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_REFUSEINVITE,
				Params.valueOf("mailId", mailId),
				Params.valueOf("guildId", builder.getGuildId()),
				Params.valueOf("guildName", builder.getGuildName()));
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 发表联盟宣言
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_POSTANNOUNCEMENT_C_VALUE)
	private boolean onPostGuildAnnouncement(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_GUILD_ANNOUNCE);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.EDIT_ALLIANCE_ANNOUNCEMENT)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 禁止全服修改联盟宣言
		if (GlobalData.getInstance().isGlobalBan(GlobalControlType.CHANGE_GUILD_ANNOUNCE)) {
			String reason = GlobalData.getInstance().getGlobalBanReason(GlobalControlType.CHANGE_GUILD_ANNOUNCE);
			if (HawkOSOperator.isEmptyString(reason)) {
				sendError(protocol.getType(), SysError.GLOBAL_BAN_CHANGE_GUILD_ANNOUNCE);
			} else {
				player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, 0, reason);
			}
			return false;
		}
		
		int checkResult = GameUtil.changeContentCDCheck(player.getGuildId(), ChangeContentType.CHANGE_GUILD_ANNOUNCE);
		if (checkResult < 0) {
			sendError(protocol.getType(), IDIPErrorCode.CHANGE_GUILD_ANNOUNCE_CD_ING);
			return false;
		}
		
		PostAnnouncementReq req = protocol.parseProtocol(PostAnnouncementReq.getDefaultInstance());
		if (!GuildUtil.checkAnnouncement(req.getAnnouncement())) {
			sendError(protocol.getType(), Status.Error.GUILD_ANNOUNCEMENT_ILLEGAL);
			return false;
		}
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", "");
		GameTssService.getInstance().wordUicChatFilter(player, req.getAnnouncement(), 
				MsgCategory.GUILD_ANNOUNCE.getNumber(), GameMsgCategory.POST_GUILD_ANNOUNCE, 
				String.valueOf(checkResult), gameDataJson, protocol.getType());
		return true;
	}

	/**
	 * 发表联盟通告
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_POSTNOTICE_C_VALUE)
	private boolean onPostGuildNotice(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_GUILD_NOTICE);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.EDIT_ALLIANCE_NOTICE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		PostNoticeReq req = protocol.parseProtocol(PostNoticeReq.getDefaultInstance());
		if (!GuildUtil.checkNotice(req.getNotice())) {
			sendError(protocol.getType(), Status.Error.GUILD_NOTICE_ILLEGAL);
			return false;
		}
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", "");
		
		GameTssService.getInstance().wordUicChatFilter(player, req.getNotice(), 
				MsgCategory.GUILD_NOTICE.getNumber(), GameMsgCategory.POST_GUILD_NOTICE, 
				"", gameDataJson, protocol.getType());
		return true;
	}

	/**
	 * 获得联盟留言
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_GETMESSAGE_C_VALUE)
	private boolean onGetGuildMessage(HawkProtocol protocol) {
		GetGuildMessageReq req = protocol.parseProtocol(GetGuildMessageReq.getDefaultInstance());
		GetGuildBBSMessageResp.Builder builder = GetGuildBBSMessageResp.newBuilder();
		for (byte[] message : LocalRedis.getInstance().getGuildBBS(req.getGuildId())) {
			try {
				GuildBBSMessage.Builder messageBuilder = GuildBBSMessage.newBuilder().mergeFrom(message);
				if (!RelationService.getInstance().isBlacklist(player.getId(), messageBuilder.getPlayerId())) {
					builder.addMessage(messageBuilder);
				}
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
		}
		builder.setIsForbiden(false);
		LocalRedis.getInstance().setGuildNum(player.getId(), "MESSAGE", 0);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETMESSAGE_S, builder));
		return true;
	}

	/**
	 * 发表联盟留言
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_POSTMESSAGE_C_VALUE)
	private boolean onPostGuildMessage(HawkProtocol protocol) {
		PostMessageReq req = protocol.parseProtocol(PostMessageReq.getDefaultInstance());
		if (!GuildService.getInstance().isGuildExist(req.getGuildId())) {
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}

		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.MESSAGE_LEAVING_AUTHORITY)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY_VALUE);
			return false;
		}

		if (!GuildUtil.checkMessage(req.getMessage())) {
			sendError(protocol.getType(), Status.Error.GUILD_MESSAGE_ILLEGAL);
			return false;
		}

		boolean beForbid = LocalRedis.getInstance().isBeForbid(req.getGuildId(), player.getId());
		if (beForbid) {
			sendError(protocol.getType(), Status.Error.GUILD_MESSAGE_FORBID);
			return false;
		}
		
		JSONObject json = new JSONObject();
		json.put("post_id", 0);
		GameTssService.getInstance().wordUicChatFilter(player, req.getMessage(), 
				MsgCategory.GUILD_LIUYAN.getNumber(), GameMsgCategory.POST_GUILD_MSG, 
				req.getGuildId(), json, protocol.getType());
		return true;
	}
	
	/**
	 * 获得已屏蔽列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_GETFORBIDLIST_C_VALUE)
	private boolean onGetForbidPlayer(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.PUBLICRECRUIT)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}

		GetForbidPlayerListResp.Builder builder = GetForbidPlayerListResp.newBuilder();
		Set<String> list = LocalRedis.getInstance().GetForbidPostPlayer(player.getGuildId());
		for (String playerId : list) {
			GuildApplyInfo.Builder info = GuildApplyInfo.newBuilder();
			Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
			info.setPlayerId(playerId);
			info.setPlayerName(snapshot.getName());
			String guildTag = snapshot.getGuildTag();
			if(!HawkOSOperator.isEmptyString(guildTag)){
				info.setGuildTag(guildTag);
			}
			info.setIcon(snapshot.getIcon());
			String pfIcon = snapshot.getPfIcon();
			if (!HawkOSOperator.isEmptyString(pfIcon)) {
				info.setPfIcon(pfIcon);
			}
			info.setPower(snapshot.getPower());
			builder.addInfo(info);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_GETFORBIDLIST_S_VALUE, builder));
		return true;
	}

	/**
	 * 屏蔽玩家留言
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_FORBIDPLAYERMESSAGE_C_VALUE)
	private boolean onForbidPostGuildMessage(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.PUBLICRECRUIT)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}

		ForbidPlayerPostMessageReq req = protocol.parseProtocol(ForbidPlayerPostMessageReq.getDefaultInstance());
		String targetPlayerId = req.getPlayerId();
		String targetGuildId = GuildService.getInstance().getPlayerGuildId(targetPlayerId);
		if (!HawkOSOperator.isEmptyString(targetGuildId) && targetGuildId.equals(player.getGuildId())) {
			sendError(protocol.getType(), Status.Error.GUILD_FROBID_MEMBER);
			return false;
		}
		
		GuildService.getInstance().dealMsg(MsgId.FORBID_POST_GUILD_MSG, new GuildForbidPostMsgInvoker(targetPlayerId, player.getGuildId(), protocol.getType()));
		
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_FORBIDPOSTMESSAGE,
				Params.valueOf("guildId", player.getGuildId()),
				Params.valueOf("targetPlayer", req.getPlayerId()));
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 解除玩家屏蔽
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CANCELFORBIDMESSAGE_C_VALUE)
	private boolean onCancelForbidPostGuildMessage(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.PUBLICRECRUIT)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		CancelForbinPlayerReq req = protocol.parseProtocol(CancelForbinPlayerReq.getDefaultInstance());
		LocalRedis.getInstance().removeForbidPostPlayer(player.getGuildId(), req.getPlayerId());
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CANCELPOSTMESSAGE,
				Params.valueOf("guildId", player.getGuildId()),
				Params.valueOf("targetPlayer", req.getPlayerId()));
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 改变联盟称谓
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CHANGELEVELNAME_C_VALUE)
	private boolean onChangeLevelName(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_GUILD_LEVELNAME);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.EDIT_MEMBER_LEVEL_NAME)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		ChangeLevelNameReq req = protocol.parseProtocol(ChangeLevelNameReq.getDefaultInstance());
		StringBuilder sb = new StringBuilder();
		StringJoiner sj = new StringJoiner(",");
		//String[] names = new String[5];
		JSONObject json = new JSONObject();
		if (req.hasL1Name()) {
			if (!GuildUtil.checkGuildLevelName(req.getL1Name())) {
				sendError(protocol.getType(), Status.Error.GUILD_LEVELNAME_ILLEGAL);
				return false;
			}
			sb.append("L1:" + req.getL1Name());
			//names[0] = req.getL1Name();
			json.put("L1", req.getL1Name());
			sj.add("1");
		}
		
		if (req.hasL2Name()) {
			if (!GuildUtil.checkGuildLevelName(req.getL2Name())) {
				sendError(protocol.getType(), Status.Error.GUILD_LEVELNAME_ILLEGAL);
				return false;
			}
			sb.append(" L2:" + req.getL2Name());
			//names[1] = req.getL2Name();
			json.put("L2", req.getL2Name());
			sj.add("2");
		}
		
		if (req.hasL3Name()) {
			if (!GuildUtil.checkGuildLevelName(req.getL3Name())) {
				sendError(protocol.getType(), Status.Error.GUILD_LEVELNAME_ILLEGAL);
				return false;
			}
			sb.append(" L3:" + req.getL3Name());
			//names[2] = req.getL3Name();
			json.put("L3", req.getL3Name());
			sj.add("3");
		}
		
		if (req.hasL4Name()) {
			if (!GuildUtil.checkGuildLevelName(req.getL4Name())) {
				sendError(protocol.getType(), Status.Error.GUILD_LEVELNAME_ILLEGAL);
				return false;
			}
			sb.append(" L4:" + req.getL4Name());
			//names[3] = req.getL4Name();
			json.put("L4", req.getL4Name());
			sj.add("4");
		}
		
		if (req.hasL5Name()) {
			if (!GuildUtil.checkGuildLevelName(req.getL5Name())) {
				sendError(protocol.getType(), Status.Error.GUILD_LEVELNAME_ILLEGAL);
				return false;
			}
			sb.append(" L5:" + req.getL5Name());
			//names[4] = req.getL5Name();
			json.put("L5", req.getL5Name());
			sj.add("5");
		}
		
		if (sb.length() == 0) {
			return true;
		}
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", sj.toString());
		
		String logInfo = sb.toString();
		GameTssService.getInstance().wordUicChatFilter(player, logInfo, 
				MsgCategory.GUILD_LEVEL.getNumber(), GameMsgCategory.CHANGE_GUILD_LEVEL_NAME, 
				json.toJSONString(), gameDataJson, protocol.getType());
		return true;
	}

	/**
	 * 改变联盟成员权限
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CAHNGELEVEL_C_VALUE)
	private boolean onChangeLevelMember(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.CHANGE_MEMBER_LEVEL)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		ChangeGuildLevelReq req = protocol.parseProtocol(ChangeGuildLevelReq.getDefaultInstance());
		HawkTuple2<Integer, Integer> operationResult = GuildService.getInstance().onChangeGuildMemLevel(player.getGuildId(), player.getId(), req.getPlayerId(), req.getLevel());
		if (operationResult.first == Status.SysError.SUCCESS_OK_VALUE) {
			Player target = GlobalData.getInstance().getActivePlayer(req.getPlayerId());
			if (target != null && target.isActiveOnline()) {
				target.getPush().syncGuildInfo();
			}

			String oldAuthName = GuildService.getInstance().getLevelName(player.getGuildId(), operationResult.second);
			if (HawkOSOperator.isEmptyString(oldAuthName)) {
				oldAuthName = "0:" + operationResult.second;
			} else {
				oldAuthName = "1:" + oldAuthName;
			}
			String newAuthName = GuildService.getInstance().getLevelName(player.getGuildId(), req.getLevel());
			if (HawkOSOperator.isEmptyString(newAuthName)) {
				newAuthName = "0:" + req.getLevel();
			} else {
				newAuthName = "1:" + newAuthName;
			}
			// 发送邮件---联盟成员等级变更
			int icon = GuildService.getInstance().getGuildFlag(player.getGuildId());
			GuildMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(req.getPlayerId())
					.setMailId(MailId.GUILD_MEM_TITLE_CHANGED)
					.addSubTitles(newAuthName)
					.addContents(oldAuthName, newAuthName)
					.setIcon(icon)
					.build());

			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CHANGELEVELNAME, Params.valueOf("guildId", player.getGuildId()));
			player.responseSuccess(protocol.getType());
			return true;
		}
		sendError(protocol.getType(), operationResult.first);
		return true;
	}

	/**
	 * 踢出联盟成员
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_KICK_C_VALUE)
	private boolean onKickMember(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.EXPEL_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		// 已参与本期泰伯利亚之战
		if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}
		
		// 星球大战参与联盟特定阶段不能进行人员操作
		if (!StarWarsActivityService.getInstance().checkGuildMemberOps(player.getGuildId())) {
			sendError(protocol.getType(), Status.Error.SW_GUILD_OPS_LIMIT);
			return false;
		}
		//联盟正在进行反攻幽灵副本
		if(FGYLMatchService.getInstance().guildInFighting(player.getGuildId())){
			sendError(protocol.getType(),  Status.FGYLError.FGYL_IN_FIGHTING_LIMIT_VALUE);
			return false;
		}
		KickMemberReq req = protocol.parseProtocol(KickMemberReq.getDefaultInstance());
		final String targetId = req.getPlayerId();
		if(!XHJZWarService.getInstance().checkGuildOperation(null, targetId)){
			sendError(protocol.getType(), Status.XHJZError.XHJZ_GUILD_OPERATION_FORBID);
			return false;
		}
		
		//小队管理检查
		int checkRlt = GuildTeamService.getInstance().checkGuildOperation(null, targetId);
		if(checkRlt != Result.SUCCESS_VALUE){
			sendError(protocol.getType(), checkRlt);
			return false;
		}
		
		
		int cyCheckResult = CyborgWarService.getInstance().checkGuildMemberOps(player.getGuildId(), targetId, true);
		// 赛博之战出战人员特定阶段不能被踢出联盟
		if (cyCheckResult != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), cyCheckResult);
			return false;
		}
	
		if (!WorldMarchService.getInstance().checkCanQuitGuild(targetId)) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADY_MARCH_KICK);
			return false;
		}

		if (SuperWeaponService.getInstance().getStatus() == SuperWeaponPeriod.WARFARE_VALUE) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_CANNOT_KICK_GUILD);
			return false;
		}
		
		if (XZQService.getInstance().getState() == PBXZQStatus.XZQ_OPEN) {
			sendError(protocol.getType(), Status.XZQError.XZQ_CANNOT_KICK_GUILD);
			return false;
		}
		
		if(WarCollegeInstanceService.getInstance().getTeamId(targetId) != null){
			sendError(protocol.getType(), Status.Error.GUILD_KICKOUT_IN_WAR_COLLEGE_TARGET_VALUE);
			 return false;
		}
		if(WarCollegeInstanceService.getInstance().getTeamId(player.getId()) != null){
			sendError(protocol.getType(), Status.Error.GUILD_KICKOUT_IN_WAR_COLLEGE_SELF_VALUE);
			 return false;
		}
		
		//巨龙来袭开启中
		GuildDragonTrapData trap = ActivityManager.getInstance().getDataGeter().getGuildDragonTrapData(targetId);
		if(Objects.nonNull(trap) && trap.inFight){
			sendError(protocol.getType(),  Status.Error.GUILD_DRAGON_ACTTACK_STATE_LIMIT_VALUE);
			return false;
		}
		if (!MeterialTransportService.getInstance().canQuitGuild(player)) {
			sendError(protocol.getType(), Status.MTTError.MT_NO_QUIT_GUILD);
			return false;
		}
		
		GuildService.getInstance().dealMsg(MsgId.GUILD_KICK_MEMBER, new GuildKickMemberInvoker(player, targetId, protocol.getType()));
		return true;
	}

	/**
	 * 退出联盟
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onGuildQuitMsg(GuildQuitMsg msg) {
		boolean isKick = msg.isKick();
		String guildId = msg.getGuildId();
		player.quitGuild(guildId);
		
		TiberiumWarService.getInstance().onQuitGuild(player, guildId);
		
		CyborgWarService.getInstance().onQuitGuild(player, guildId);
		
		ChampionshipService.getInstance().onQuitGuild(player, guildId);		
		
		GuildManorService.getInstance().notifyManorBuffChange(player);

		//星海激战
		XHJZWarService.getInstance().dealMsg(MsgId.XHJZ_QUIT_GUILD, new XHJZWarQuitGuildInvoker(player.getId()));

		GuildTeamService.getInstance().dealMsg(MsgId.GUILD_TEAM_QUIT_GUILD, new GuildTeamQuitGuildInvoker(player.getId()));

		//投递一个消息到SimulateWarService.
		HawkTaskManager.getInstance().postMsg(SimulateWarService.getInstance().getXid(), 
				new SimulateWarQuitGuildMsg(player, msg.getGuildId()));
		
		if (player.isActiveOnline()) {
			player.getPush().syncGuildInfo();
			player.getPush().pushLeaveGuild();
			GuildService.getInstance().syncGuildTechEffect(guildId, player);
			if (isKick) {
				HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUILD_BEKICK_SYNC_S);
				player.sendProtocol(protocol);
			}
			// 刷超级武器作用号
			player.getEffect().resetEffectSuperWeapon(player);
		}
		
		int opType = isKick ? GuildOperType.GUILD_KICKOUT : GuildOperType.GUILD_QUIT;
		LogUtil.logGuildFlow(player, opType, guildId, null);
		if (GameUtil.isScoreBatchEnable(player) && !player.isActiveOnline()) {
			LocalRedis.getInstance().addScoreBatchFlag(player.getId(), ScoreType.GUILD_MEMBER_CHANGE.intValue(), "2");
		} else {
			GameUtil.scoreBatch(player,ScoreType.GUILD_MEMBER_CHANGE, 2);
		}
		
		return true;
	}

	/**
	 * 退出联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_QUIT_C_VALUE)
	private boolean onQuitGuild(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.QUIT_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 跨服状态下该操作不可进行
		if (player.isCsPlayer()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		if (CrossActivityService.getInstance().isOpen()) {
			String fightPresident = RedisProxy.getInstance().getCrossFightPresident();
			if (!HawkOSOperator.isEmptyString(fightPresident) && fightPresident.equals(player.getId())) {
				return false;	
			}
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}

		if(!XHJZWarService.getInstance().checkGuildOperation(null, player.getId())){
			sendError(protocol.getType(), Status.XHJZError.XHJZ_GUILD_OPERATION_FORBID);
			return false;
		}

		
		//小队管理检查
		int checkRlt = GuildTeamService.getInstance().checkGuildOperation(null, player.getId());
		if(checkRlt != Result.SUCCESS_VALUE){
			sendError(protocol.getType(), checkRlt);
			return false;
		}
		

		// 已参与本期泰伯利亚之战
		if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}
				
		if (PresidentFightService.getInstance().isProvisionalPresident(player.getId())) {
			sendError(protocol.getType(), Status.Error.PROVISIONAL_PRESIDENT_CAN_NOT_QUIT_VALUE);
			return false;
		}

		if (!WorldMarchService.getInstance().checkCanQuitGuild(player.getId())) {
			sendError(protocol.getType(), Status.Error.GUILD_ALREADY_MARCH_QUIT);
			return false;
		}
		
		if (SuperWeaponService.getInstance().getStatus() == SuperWeaponPeriod.WARFARE_VALUE) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_CANNOT_QUIT_GUILD);
			return false;
		}
		
		if (XZQService.getInstance().getState() == PBXZQStatus.XZQ_OPEN) {
			sendError(protocol.getType(), Status.XZQError.XZQ_CANNOT_QUIT_GUILD);
			return false;
		}
		
		if(WarCollegeInstanceService.getInstance().getTeamId(player.getId()) != null){
			sendError(protocol.getType(),  Status.Error.GUILD_QUIT_IN_WAR_COLLEGE_VALUE);
			return false;
		}
		
		if(FGYLMatchService.getInstance().guildInFighting(player.getGuildId())){
			sendError(protocol.getType(),  Status.FGYLError.FGYL_IN_FIGHTING_LIMIT_VALUE);
			return false;
		}
		//巨龙来袭开启中
		GuildDragonTrapData trap = ActivityManager.getInstance().getDataGeter().getGuildDragonTrapData(player.getId());
		if(Objects.nonNull(trap) && trap.inFight){
			sendError(protocol.getType(),  Status.Error.GUILD_DRAGON_ACTTACK_STATE_LIMIT_VALUE);
			return false;
		}
		
		final String guildId = player.getGuildId();
		// 星球大战参与联盟特定阶段不能进行人员操作
		if (!StarWarsActivityService.getInstance().checkGuildMemberOps(guildId)) {
			sendError(protocol.getType(),  Status.Error.SW_GUILD_OPS_LIMIT);
			return false;
		}
		
		int cyCheckResult = CyborgWarService.getInstance().checkGuildMemberOps(player.getGuildId(), player.getId(), false);
		// 赛博之战出战人员特定阶段不能被踢出联盟
		if (cyCheckResult != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), cyCheckResult);
			return false;
		}
		if (!MeterialTransportService.getInstance().canQuitGuild(player)) {
			sendError(protocol.getType(), Status.MTTError.MT_NO_QUIT_GUILD);
			return false;
		}
		
		player.rpcCall(MsgId.GUILD_QUIT_GUILD, GuildService.getInstance(),
				new GuildQuitRpcInvoker(player, guildId, protocol.getType()));
		return true;
	}

	/**
	 * 转让联盟盟主
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_DEMISELEADER_C_VALUE)
	private boolean onDemiseLeader(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_LEADERSHIP_CHANGE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		// 国王盟主不可让位
		if (PresidentFightService.getInstance().isProvisionalPresident(player.getId())) {
			sendError(protocol.getType(), Status.Error.PROVISIONAL_PRESIDENT_CAN_NOT_DEMISE);
			return false;
		}
		
		// 霸主和统帅不能转让盟主
		if(StarWarsOfficerService.getInstance().isKing(player.getId())){
			sendError(protocol.getType(), Status.Error.SW_KING_CANNOT_DEMISE_LEADER);
			return false;
		}
		
		DimiseLeaderReq req = protocol.parseProtocol(DimiseLeaderReq.getDefaultInstance());
		if (GlobalData.getInstance().isResetAccount(req.getPlayerId())) {
			HawkLog.errPrintln("removed player invalid operation, playerId: {}, targetId: {}", player.getId(), req.getPlayerId());
			return false;
		}

		GuildService.getInstance().dealMsg(MsgId.GUILD_DEMISE_LEADER, new GuildDemiseLeaderInvoker(player, req.getPlayerId(), protocol.getType()));
		return true;
	}

	/**
	 * 解散联盟
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_DISSMISEGUILD_C_VALUE)
	private boolean onDismissGuild(HawkProtocol protocol) {
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_CAN_NOT_DISMISS_GUILD);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.DISSOLVE_ALLIANCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}

		if(!XHJZWarService.getInstance().checkGuildOperation(player.getGuildId(), null)){
			sendError(protocol.getType(), Status.XHJZError.XHJZ_GUILD_OPERATION_FORBID);
			return false;
		}

		//小队管理检查
		int checkRlt = GuildTeamService.getInstance().checkGuildOperation(player.getGuildId(), null);
		if(checkRlt != Result.SUCCESS_VALUE){
			sendError(protocol.getType(), checkRlt);
			return false;
		}

		// 已参与本期泰伯利亚之战
		if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
			sendError(protocol.getType(), Status.Error.TIBERIUM_LEAGUA_CANNOT_DISMISS);
			return false;
		}
		
		// 已参与本期泰伯利亚联赛,不能解散
		if (TiberiumLeagueWarService.getInstance().getActivityInfo().getSeason() > 0 && TiberiumLeagueWarService.getInstance().getJoinGuilds().containsKey(player.getGuildId())) {
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}
		
		// 星球大战特定阶段及联盟,不能解散
		int swCheckResult = StarWarsActivityService.getInstance().canDismiss(player.getGuildId());
		if (swCheckResult != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), swCheckResult);
			return false;
		}
		
		// 赛博之战检测能否解散联盟
		int cyCheckResult = CyborgWarService.getInstance().checkGuildOperation(player.getGuildId());
		if (cyCheckResult != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), cyCheckResult);
			return false;
		}
		
		// 国王所在盟不可被解散
		final String guildId = player.getGuildId();
		if (PresidentFightService.getInstance().isProvisionalPresident(player.getId())) {
			sendError(protocol.getType(), Status.Error.PROVISIONAL_PRESIDENT_CAN_NOT_DISMISS);
			return false;
		}
		
		// 霸主和统帅不能解散联盟
		if(StarWarsOfficerService.getInstance().isKing(player.getId())){
			sendError(protocol.getType(), Status.Error.SW_KING_CANNOT_DISMISS);
			return false;
		}

		// 有总统府相关行军，不能解散联盟
		if (PresidentFightService.getInstance().hasPresidentFightAction(guildId)) {
			sendError(protocol.getType(), Status.Error.CAN_NOT_DISMISS_GUILD_HAS_MASS);
			return false;
		}

		// 有超级武器行军， 不能解散联盟
		if (SuperWeaponService.getInstance().getGuildOccupySuperWeapon(guildId).size() > 0) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_CAN_NOT_DIS_GUILD);
			return false;
		}
		// 有超级武器行军， 不能解散联盟
		if (XZQService.getInstance().getState() == PBXZQStatus.XZQ_OPEN) {
			sendError(protocol.getType(), Status.XZQError.XZQ_CAN_NOT_DIS_GUILD);
			return false;
		}
		//联盟正在进行反攻幽灵副本
		if(FGYLMatchService.getInstance().guildInFighting(player.getGuildId())){
			sendError(protocol.getType(),  Status.FGYLError.FGYL_IN_FIGHTING_LIMIT_VALUE);
			return false;
		}
		// 有集结的行军，不能解散联盟
		Collection<IWorldMarch> guildMarchs = WorldMarchService.getInstance().getGuildMarchs(guildId);
		for (IWorldMarch guildMarch : guildMarchs) {
			if (WorldUtil.isMassMarch(guildMarch.getMarchType().getNumber())) {
				sendError(protocol.getType(), Status.Error.CAN_NOT_DISMISS_GUILD_HAS_MASS);
				return false;
			}
		}
		
		//有报名攻防模拟战
		if (!SimulateWarService.getInstance().checkDissolveGuild(guildId)) {
			this.sendError(protocol.getType(), Status.Error.SIMULATE_WAR_CAN_NOT_DISSOLVE_VALUE);
			
			return false;
		}
		
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(guildId);
		if (spaceObj != null && spaceObj.getStage() != null) {
			this.sendError(protocol.getType(), Status.Error.SPACE_MECHA_CANNOT_DISSOLVE_VALUE);
			return false;
		}
		
		//巨龙来袭开启中
		GuildDragonTrapData trap = ActivityManager.getInstance().getDataGeter().getGuildDragonTrapData(player.getId());
		if(Objects.nonNull(trap) && trap.inFight){
			sendError(protocol.getType(),  Status.Error.GUILD_DRAGON_ACTTACK_STATE_LIMIT_VALUE);
			return false;
		}

		player.rpcCall(MsgId.DISMISS_GUILD, GuildService.getInstance(), new GuildDismissRpcInvoker(player, guildId, protocol.getType()));

		LogUtil.logGuildAction(GuildAction.GUILD_DISSOLVE, guildId);
		return true;
	}

	/**
	 * 取代盟主
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_IMPEACHMENTLEADER_C_VALUE)
	private boolean onImpeachmentLeader(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		ItemInfo cost = GuildConstProperty.getInstance().getLeaderReplaceConsume();
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(cost, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		
		player.rpcCall(MsgId.GUILD_IMPEACHMENT_LEADER, GuildService.getInstance(), new GuildImpeachmentLeaderInvoker(player, protocol.getType(), consume));
		
		return true;
	}

	/**
	 * 查看联盟帮助队列
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_CHECKQUEUES_C_VALUE)
	private boolean onCheckGuildHelpQueues(HawkProtocol protocol) {
		// 没有联盟
		String guildId = player.getGuildId();
		if (StringUtil.isNullOrEmpty(guildId)) {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_EXIST);
			return false;
		}

		List<GuildHelpQueue> helpQueueBuilders = GuildService.getInstance().getGuildHelpQueues(guildId, player);

		CheckGuildHelpQueueRes.Builder resp = CheckGuildHelpQueueRes.newBuilder();
		resp.addAllQueue(helpQueueBuilders);

		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_CHECKQUEUES_S, resp));

		return true;
	}

	/**
	 * 申请联盟帮助
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_APPLYHELP_C_VALUE)
	private boolean onApplyGuildHelp(HawkProtocol protocol) {
		ApplyGuildHelpReq req = protocol.parseProtocol(ApplyGuildHelpReq.getDefaultInstance());

		// 队列ID
		String queueId = req.getQueueId();

		// 获取队列
		QueueEntity queue = player.getData().getQueueEntity(queueId);

		// 没有该队列或队列已结束
		if (queue == null) {
			return false;
		}

		// 已经申请过联盟帮助
		if (queue.getHelpTimes() > 0) {
			return false;
		}

		// 队列已完成
		long now = HawkTime.getMillisecond();
		if (now >= queue.getEndTime()) {
			return false;
		}

		// 验证队列类型（只有城市建造、科技研究、伤兵恢复可以申请联盟帮助）
		if (queue.getQueueType() != QueueType.BUILDING_QUEUE_VALUE &&
				queue.getQueueType() != QueueType.BUILDING_DEFENER_VALUE &&
				queue.getQueueType() != QueueType.SCIENCE_QUEUE_VALUE &&
				queue.getQueueType() != QueueType.CURE_QUEUE_VALUE &&
				queue.getQueueType() != QueueType.PLANT_SCIENCE_QUEUE_VALUE &&
				queue.getQueueType() != QueueType.CURE_PLANT_QUEUE_VALUE) {

			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 没有联盟
		String guildId = player.getGuildId();
		if (StringUtil.isNullOrEmpty(guildId)) {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_EXIST);
			return false;
		}

		// 申请联盟帮助
		boolean succ = GuildService.getInstance().applyGuildHelp(guildId, player, queue);

		if (!succ) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_EMBASSY_VALUE);
			return false;
		}
		queue.setHelpTimes(1);
		QueuePB.Builder update = BuilderUtil.genQueueBuilder(queue);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_UPDATE_PUSH_VALUE, update));
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 帮助联盟队列
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_HELPQUEUE_C_VALUE)
	private boolean onHelpGuildQueue(HawkProtocol protocol) {
		HelpGuildQueueReq req = protocol.parseProtocol(HelpGuildQueueReq.getDefaultInstance());

		// 队列
		String queueId = req.getQueueId();

		// 没有联盟
		String guildId = player.getGuildId();
		if (StringUtil.isNullOrEmpty(guildId)) {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_EXIST);
			return false;
		}

		// 帮助盟友
		GuildService.getInstance().helpOneGuildQueue(guildId, player, queueId);

		HelpGuildQueueRes.Builder resp = HelpGuildQueueRes.newBuilder();
		resp.setQueueId(queueId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_HELPQUEUE_S, resp));

		return true;
	}

	/**
	 * 帮助所有联盟队列
	 */
	@ProtocolHandler(code = HP.code.GUILDMANAGER_HELPALLQUEUES_C_VALUE)
	private boolean onHelpAllGuildQueue(HawkProtocol protocol) {
		// 没有联盟
		String guildId = player.getGuildId();
		if (StringUtil.isNullOrEmpty(guildId)) {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_EXIST);
			return false;
		}

		// 帮助盟友
		GuildService.getInstance().helpAllGuildQueues(guildId, player);
		player.responseSuccess(HP.code.GUILDMANAGER_HELPALLQUEUES_C_VALUE);
		return true;
	}

	/**
	 * 获取联盟商店信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_GET_SHOP_INFO_C_VALUE)
	private boolean onGetGuildShopInfo(HawkProtocol protocol) {
		// 没有联盟
		String guildId = player.getGuildId();
		if (StringUtil.isNullOrEmpty(guildId)) {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_EXIST);
			return false;
		}
		HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder> result = GuildService.getInstance().onGetGuildShopInfo(player);
		if (result.first == Status.SysError.SUCCESS_OK_VALUE) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_SHOP_INFO_S, result.second));
			return true;
		}
		sendError(HP.code.GUILD_GET_SHOP_INFO_C_VALUE, result.first);
		return true;
	}

	/**
	 * 购买联盟商店道具
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_SHOP_BUY_C_VALUE)
	private boolean onGuildShopBuy(HawkProtocol protocol) {
		HPGuildShopBuyReq req = protocol.parseProtocol(HPGuildShopBuyReq.getDefaultInstance());
		// 没有联盟
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendError(protocol.getType(), Status.Error.GUILD_PLAYER_HASNOT_GUILD);
			return false;
		}
		final int itemId = req.getItemId();
		final int count = req.getCount();
		HawkAssert.checkPositive(count);
		GuildShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildShopCfg.class, itemId);
		// 商品信息不存在
		if (cfg == null) {
			sendError(protocol.getType(), Status.Error.GUILD_SHOP_ITEM_NOT_EXIST_VALUE);
			return false;
		}
		long totalPrice = 1l * count * cfg.getPrice();
		// 参数异常
		if (count <= 0 || totalPrice >= Integer.MAX_VALUE) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		// 超出商店库存
		int itemCount = RedisProxy.getInstance().getGuildShopItemCount(guildId, itemId);
		if (count > itemCount) {
			sendError(protocol.getType(), Status.Error.GUILD_SHOP_ITEM_COUNT_NOT_ENOUGH_VALUE);
			return false;
		}
		

		// 消耗检测
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GUILD_CONTRIBUTION, totalPrice);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemId), cfg.getPrice(), count));
		}
		consume.consumeAndPush(player, Action.GUILD_BUY);
		// 添加物品
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(Const.ItemType.TOOL_VALUE, itemId, count);
		awardItem.rewardTakeAffectAndPush(player, Action.GUILD_BUY);

		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_BUY,
				Params.valueOf("itemId", itemId),
				Params.valueOf("count", count),
				Params.valueOf("totalPrice", count * cfg.getPrice()));

		GuildService.getInstance().dealMsg(MsgId.GUILD_BUY_ITEM, new GuildShopBuyInvoker(player, itemId, count, cfg, consume, protocol.getType()));
		MissionManager.getInstance().postMsg(player, new EventBuyInGuildShop(itemId, count));

		return true;
	}

	/**
	 * 联盟商店补货
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_ADD_SHOP_ITEM_C_VALUE)
	private boolean onGuildShopAdd(HawkProtocol protocol) {
		HPGuildShopBuyReq req = protocol.parseProtocol(HPGuildShopBuyReq.getDefaultInstance());
		int count = req.getCount();
		HawkAssert.checkPositive(count);
		
		GuildService.getInstance().dealMsg(MsgId.GUILD_GUILDSHOP_ADD,
				new GuildAddShopItemInvoker(player, req.getItemId(), count, protocol.getType()));
		return true;
	}

	/**
	 * 通知补货
	 * 
	 * @param protocol
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@ProtocolHandler(code = HP.code.GUILD_INVITE_ADD_SHOP_ITEM_VALUE)
	private boolean onInviteAddShopItem(HawkProtocol protocol) {
		HPInviteAddShopItem req = protocol.parseProtocol(HPInviteAddShopItem.getDefaultInstance());
		// 发送联盟消息
		ChatService.getInstance().addWorldBroadcastMsg(ChatType.GUILD_HREF, Const.NoticeCfgId.GUILD_INVITE_ADD_SHOP_ITEM,
				player, req.getItemName(), player.getName());
		return true;
	}

	/**
	 * 获取联盟商店购买记录
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_GET_SHOP_BUY_LOG_C_VALUE)
	private boolean onGuildGetShopBuyLog(HawkProtocol protocol) {
		HawkTuple2<Integer, HPGetGuildShopLogResp.Builder> result = GuildService.getInstance().getGuildShopLog(player, GuildConst.SHOP_LOG_TYPE_BUY);
		if (result.first == Status.SysError.SUCCESS_OK_VALUE) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_SHOP_BUY_LOG_S_VALUE, result.second));
			return true;
		}
		sendError(HP.code.GUILD_GET_SHOP_BUY_LOG_C_VALUE, result.first);
		return true;
	}

	/**
	 * 获取联盟商店补货记录
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_GET_SHOP_ADD_LOG_C_VALUE)
	private boolean onGuildGetShopAddLog(HawkProtocol protocol) {
		HawkTuple2<Integer, HPGetGuildShopLogResp.Builder> result = GuildService.getInstance().getGuildShopLog(player, GuildConst.SHOP_LOG_TYPE_ADD);
		if (result.first == Status.SysError.SUCCESS_OK_VALUE) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_SHOP_ADD_LOG_S_VALUE, result.second));
			return true;
		}
		sendError(HP.code.GUILD_GET_SHOP_ADD_LOG_C_VALUE, result.first);
		return true;
	}

	/**
	 * 帮助联盟玩家被援助的信息
	 */
	@ProtocolHandler(code = HP.code.GUILD_MEMBER_ASSISTENCE_INFO_C_VALUE)
	private boolean onGetGuildAssistenceInfo(HawkProtocol protocol) {
		GetGuildAssistenceInfoReq req = protocol.parseProtocol(GetGuildAssistenceInfoReq.getDefaultInstance());
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (tarPlayer == null) {
			sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST);
			return false;
		}

		// 检查是否同盟
		if (!GuildService.getInstance().isPlayerInGuild(player.getGuildId(), tarPlayer.getId())) {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_MEMBER);
			return false;
		}

		int maxCnt = tarPlayer.getMaxAssistSoldier();
		int curCnt = WorldMarchService.getInstance().getAssistArmySoldierTotal(tarPlayer);

		GetGuildAssistenceInfoResp.Builder resp = GetGuildAssistenceInfoResp.newBuilder();
		resp.setCurCnt(curCnt);
		resp.setMaxCnt(maxCnt);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MEMBER_ASSISTENCE_INFO_S_VALUE, resp));

		return true;
	}

	/**
	 * 获取联盟日志
	 */
	@ProtocolHandler(code = HP.code.GUILD_GETLOG_C_VALUE)
	private boolean onGetGuildLog(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		GetGuildLog.Builder builder = GetGuildLog.newBuilder();
		Set<String> logs = LocalRedis.getInstance().getGuildLog(player.getGuildId(), GuildConstProperty.getInstance().getAllianceDiarySave());
		for (String string : logs) {
			HPGuildLog.Builder log = HPGuildLog.newBuilder();
			try {
				JsonFormat.merge(string, log);
				builder.addInfo(log);
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GETLOG_S_VALUE, builder));
		return true;
	}

	/**
	 * 获取联盟贡献排行信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_GET_DONATE_RANK_C_VALUE)
	private boolean onGetGuildContributionRank(HawkProtocol protocol) {
		GuildGetDonateRank req = protocol.parseProtocol(GuildGetDonateRank.getDefaultInstance());
		DonateRankType rankType = req.getRankType();
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		GuildGetDonateRankResp.Builder resp = GuildService.getInstance().onGetContributionRankInfo(player, rankType);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_DONATE_RANK_S, resp));
		return true;
	}

	/**
	 * 邀请联盟成员迁城
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_INVITE_TO_MOVE_CITY_C_VALUE)
	private boolean onInviteToMoveCity(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}

		// 跨服状态下该操作不可进行
		if (player.isCsPlayer()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.INVITE_TO_MOVE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		InviteMoveCityReq req = protocol.parseProtocol(InviteMoveCityReq.getDefaultInstance());
		int operationResult = GuildService.getInstance().onInviteToMoveCity(player, req.getInviteeId());
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.GUILD_INVITE_TO_MOVE_CITY_C_VALUE);
			LogUtil.logGuildAction(GuildAction.GUILD_INVIT_MOVE_CITY, player.getGuildId());
			return true;
		}
		sendError(protocol.getType(), operationResult);

		return true;
	}

	/**
	 * 获取联盟战争记录信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_WAR_RECORD_C_VALUE)
	private boolean onGetGuildWarRecordInfo(HawkProtocol protocol) {
		String guildId = player.getGuildId();
		List<String> guildWarRecords = LocalRedis.getInstance().getGuildWarRecord(guildId);
		HPGuildWarRecordResp.Builder resp = HPGuildWarRecordResp.newBuilder();
		if (guildWarRecords != null) {
			for (String warInfo : guildWarRecords) {
				GuildWarRecord guildWar = JSON.parseObject(warInfo, GuildWarRecord.class);

				HPGuildWarRecordPB.Builder builder = HPGuildWarRecordPB.newBuilder();
				builder.setWarTime(guildWar.getWarTime());
				builder.setWinTimes(guildWar.getWinTimes());
				String attPlayerId = guildWar.getAttPlayerId();

				HPWarPlayerInfo.Builder atkPlayerInfo = HPWarPlayerInfo.newBuilder();
				atkPlayerInfo.setType(1);
				atkPlayerInfo.setPlayerId(attPlayerId);
				atkPlayerInfo.setPlayerName(guildWar.getAttPlayerName());
				if (!HawkOSOperator.isEmptyString(guildWar.getAttGuildName())) {
					atkPlayerInfo.setGuildTag(guildWar.getAttGuildName());
				}
				Player atkPlayer = GlobalData.getInstance().makesurePlayer(attPlayerId);
				if (atkPlayer != null) {
					atkPlayerInfo.setIcon(atkPlayer.getIcon());
					atkPlayerInfo.setPfIcon(atkPlayer.getPfIcon());
				}
				atkPlayerInfo.setWarType(guildWar.getAtkMarchType());
				atkPlayerInfo.setGuildId(guildWar.getAttGuildId());
				builder.setAttPlayer(atkPlayerInfo);

				HPWarPlayerInfo.Builder defPlayerInfo = HPWarPlayerInfo.newBuilder();
				String defPlayerId = guildWar.getDefPlayerId();
				defPlayerInfo.setType(1);
				defPlayerInfo.setPlayerId(defPlayerId);
				defPlayerInfo.setPlayerName(guildWar.getDefPlayerName());
				if (!HawkOSOperator.isEmptyString(guildWar.getDefGuildName())) {
					defPlayerInfo.setGuildTag(guildWar.getDefGuildName());
				}
				Player defPlayer = GlobalData.getInstance().makesurePlayer(defPlayerId);
				if (defPlayer != null) {
					defPlayerInfo.setIcon(defPlayer.getIcon());
					defPlayerInfo.setPfIcon(defPlayer.getPfIcon());
				}
				defPlayerInfo.setWarType(guildWar.getDefMarchType());
				defPlayerInfo.setGuildId(guildWar.getDefGuildId());
				builder.setDefPlayer(defPlayerInfo);
				resp.addGuildWarRecord(builder);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_WAR_RECORD_S, resp));
		return true;
	}

	/**
	 * 获取联盟士兵援助信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_ASSISTANT_INFO_C_VALUE)
	private boolean onGetGuildAssistantInfo(HawkProtocol protocol) {
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		HPGuildAssistantResp.Builder resp = HPGuildAssistantResp.newBuilder();
		int totalForces = 0;
		if (marchList != null) {
			Set<String> playerIdSet = new HashSet<String>();
			for (IWorldMarch march : marchList) {
				playerIdSet.add(march.getPlayerId());
			}
			for (IWorldMarch march : marchList) {
				HPAssistantMarchPB.Builder marchBuilder = HPAssistantMarchPB.newBuilder();
				String playerId = march.getPlayerId();
				marchBuilder.setUuid(march.getMarchId());
				marchBuilder.setPlayerId(playerId);
				
				Player assistPlayer = GlobalData.getInstance().makesurePlayer(playerId);
				marchBuilder.setIcon(assistPlayer.getIcon());
				if (!HawkOSOperator.isEmptyString(assistPlayer.getPfIcon())) {
					marchBuilder.setPfIcon(assistPlayer.getPfIcon());
				}
				marchBuilder.setPlayerName(assistPlayer.getName());
				
				List<PlayerHero> heroList = assistPlayer.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
				for (PlayerHero hero : heroList) {
					marchBuilder.addHero(ArmyHeroPB.newBuilder().setHeroId(hero.getCfgId()).setLevel(hero.getLevel()).setStar(hero.getStar()));
					marchBuilder.addHeroList(hero.toPBobj());
				}
				
				SuperSoldier ssoldier = assistPlayer.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId()).orElse(null);
				if (Objects.nonNull(ssoldier)) {
					marchBuilder.setSsoldier(ssoldier.toPBobj());
				}

				for (ArmyInfo army : march.getMarchEntity().getArmys()) {
					if (army.getFreeCnt() <= 0) {
						continue;
					}
					totalForces += army.getFreeCnt();
					marchBuilder.addArmySoldier(army.toArmySoldierPB(assistPlayer));
				}

				
				marchBuilder.setGuildTag(GuildService.getInstance().getGuildTag(player.getGuildId()));
				resp.addMarchList(marchBuilder);
			}
		}
		resp.setForces(totalForces);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_ASSISTANT_INFO_S, resp));
		return true;
	}

	/**
	 * 获取联盟科技信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_SCIENCE_GET_INFO_C_VALUE)
	private boolean onGetScienceInfo(HawkProtocol protocol) {
		HawkTuple2<Integer, GetGuildScienceInfoResp.Builder> operationResult = GuildService.getInstance().onGetguildScienceInfo(player);
		if (operationResult.first == Status.SysError.SUCCESS_OK_VALUE) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_GET_INFO_S, operationResult.second));
			return true;
		}
		sendError(HP.code.GUILD_SCIENCE_GET_INFO_C_VALUE, operationResult.first);
		return true;
	}

	/**
	 * 设置联盟推荐信息科技
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_SCIENCE_SET_RECOMMEND_C_VALUE)
	private boolean onGuildScienceRecommend(HawkProtocol protocol) {
		GuildScienceRecommendReq req = protocol.parseProtocol(GuildScienceRecommendReq.getDefaultInstance());
		GuildService.getInstance().dealMsg(MsgId.GUILD_SCIENCE_SET_RECOMMEND,
				new GuildScienceRecommendInvoker(player, protocol.getType(), req.getRecommendIdsList(), req.getCancleIdsList()));
		return true;
	}

	/**
	 * 研究联盟科技
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_SCIENCE_RESEARCH_C_VALUE)
	private boolean onGuildScienceResearch(HawkProtocol protocol) {
		GuildScienceResearchReq req = protocol.parseProtocol(GuildScienceResearchReq.getDefaultInstance());
		GuildService.getInstance().dealMsg(MsgId.GUILD_SCIENCE_RESEARCH,
				new GuildScienceResearchInvoker(player, req.getScienceId(), protocol.getType()));
		return true;
	}

	/**
	 * 联盟科技捐献
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_SCIENCE_DONATE_C_VALUE)
	private boolean onGuildScienceDonate(HawkProtocol protocol) {
		GuildScienceDonateReq req = protocol.parseProtocol(GuildScienceDonateReq.getDefaultInstance());
		int scienceId = req.getScienceId();
		DonateType type = req.getDonateType();
		
		ConsumeItems consume = ConsumeItems.valueOf();
		int operationResult = GuildService.getInstance().getDonateConsume(player, type, scienceId, consume);
		if (operationResult != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), operationResult);
			return true;
		}
		if (!consume.checkConsume(player, protocol.getType())) {
			return true;
		}
		List<ItemInfo> consuemItems = consume.consumeAndPush(player, Action.GUILD_SCIENCE_DONATE).getAwardItems();
		
		GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(player.getId());
		GuildConstProperty property = GuildConstProperty.getInstance();
		int crit = 1;
		
		switch (type) {
		case NORMAL:
			crit = HawkRand.randomWeightObject(property.getResourceCritLvls(), property.getResourceCritWeights());
			int normalDonateTimes = member.getNormalDonateTimes() + 1;
			int donateLimit = GuildConstProperty.getInstance().getResourceDonateNumber();
			normalDonateTimes = Math.min(normalDonateTimes, donateLimit);
			member.setNormalDonateTimes(normalDonateTimes);
			// 设置下次普通捐献次数恢复时间
			if (member.getNextDonateAddTime() == 0) {
				int gap = GuildConstProperty.getInstance().getResourceDonateTime() * 1000;
				int disEff = player.getData().getEffVal(EffType.GUILD_DONATE_SPEED);
				gap = (int) Math.ceil(GsConst.EFF_RATE * gap / (GsConst.RANDOM_MYRIABIT_BASE + disEff));
				member.setNextDonateAddTime(HawkTime.getMillisecond() + gap);
			}
			break;
		case CRYSTAL:
			crit = HawkRand.randomWeightObject(property.getCrystalCritLvls(), property.getCrystalCritWeights());
			member.setDiamondDonateTimes(member.getCrystalDonateTimes() + 1);
			break;
		}
		GuildScienceMainCfg mainCfg = HawkConfigManager.getInstance().getConfigByKey(GuildScienceMainCfg.class, scienceId);
		// 科研值翻倍作用效果
		int multi = HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < player.getEffect().getEffVal(EffType.GUILD_TECH_CNBT_RATE) ? 2 : 1;
		int scienceDonate = mainCfg.getScienceBase() * crit * multi;
		
		// 联盟积分/贡献翻倍作用效果
		multi = HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < player.getEffect().getEffVal(EffType.GUILD_TECH_CNBT_SCORE) ? 2 : 1;

		// 联盟贡献奖励
		int contribution = mainCfg.getContributionBase() * crit * multi;
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItem(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GUILD_CONTRIBUTION_VALUE, contribution);
		awardItems.rewardTakeAffectAndPush(player, Action.GUILD_SCIENCE_DONATE);
		
		// 联盟科技捐献打点记录
		if (consuemItems.isEmpty()) {
			LogUtil.logGuildTechFlow(player, scienceId, GuildTechOperType.DONATE, 0, 0);
		} else {
			ItemInfo itemInfo = consuemItems.get(0);
			LogUtil.logGuildTechFlow(player, scienceId, GuildTechOperType.DONATE, itemInfo.getItemId(), (int)itemInfo.getCount());
		}
		
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_SCIENCE_DONATE,
				Params.valueOf("guildId", player.getGuildId()),
				Params.valueOf("scienceId", scienceId),
				Params.valueOf("donateType", type));
		
		// 联盟积分
		int guildScore = mainCfg.getScoreBase() * crit * multi;
		
		ActivityManager.getInstance().postEvent(new GuildDonateEvent(player.getId()));
		MissionManager.getInstance().postMsg(player, new EventGuildContribute(1));
		GuildService.getInstance().postGuildTaskMsg(new GuildDonateTaskEvent(player.getGuildId()));
		
		GuildService.getInstance().dealMsg(MsgId.GUILD_SCIENCE_DONATE_REWARD,
				new GuildScienceDonateAfterInvoker(player, type, scienceId, guildScore, scienceDonate, contribution, crit));
		
		return true;
	}

	/**
	 * 联盟捐献次数重置
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_SCIENCE_RESET_DONATE_TIMES_C_VALUE)
	private boolean onResetDonateTimes(HawkProtocol protocol) {
		int operationResult = GuildService.getInstance().checkResetDonate(player);
		if (operationResult != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(HP.code.GUILD_SCIENCE_RESET_DONATE_TIMES_C_VALUE, operationResult);
			return true;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		ItemInfo itemInfo = GuildConstProperty.getInstance().getDonateResetConsume();
		consume.addConsumeInfo(itemInfo, false);
		if (!consume.checkConsume(player, HP.code.GUILD_SCIENCE_RESET_DONATE_TIMES_C_VALUE)) {
			return true;
		}
		
		consume.consumeAndPush(player, Action.GUILD_RESET_DONATE_TIMES);
		GetGuildScienceInfoResp.Builder resp = GuildService.getInstance().onDonateTimesReset(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_RESET_DONATE_TIMES_S_VALUE, resp));
		
		// 记录联盟科技捐献次数重置数据打点
		LogUtil.logGuildTechFlow(player, 0, GuildTechOperType.RESET_DONATE_TIMES, itemInfo.getItemId(), (int)itemInfo.getCount());
		// 行为日志
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_RESET_DONATE_TIMES,
				Params.valueOf("guildId", player.getGuildId()));
		return true;
	}
	
	/**
	 * 修改联盟权限信息
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_CHANGE_AUTH_INFO_VALUE)
	public boolean onChangeGuildAuthInfo(HawkProtocol protocol) {
		ChangeAuthInfoReq req = protocol.parseProtocol(ChangeAuthInfoReq.getDefaultInstance());
		String guildId = player.getGuildId();
		// 玩家没有加入联盟
		if (HawkOSOperator.isEmptyString(guildId)) {
			GuildManorService.logger.error("onGetGuildWarInfo error, player have no guild. playerId:{}", player.getId());
			return true;
		}

		player.msgCall(MsgId.CHANGE_GUILD_AUTH_INFO, GuildService.getInstance(), new GuildChangeAuthInfoInvoker(player, req.getAuthInfoList()));
		return true;
	}
	
	/**
	 * 添加联盟标记
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_ADD_SIGN_C_VALUE)
	public boolean onAddGuildSign(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_GUILD_SIGN);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		GuildAddSignReq req = protocol.parseProtocol(GuildAddSignReq.getDefaultInstance());
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return true;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_SIGN)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY_VALUE);
			return true;
		}
		
		GuildSign guildSign = req.getSignInfo();
		int signId = guildSign.getId();
		// 检查id合法性 
		AllianceSignCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceSignCfg.class, signId);
		if(cfg == null){
			sendError(protocol.getType(), Status.Error.GUILD_SIGN_CFG_ERROR_VALUE);
			return true;
		}
		
		String signInfo = guildSign.getInfo();
		// 标记信息超长
		if(signInfo.length() > GuildConstProperty.getInstance().getAllianceSignExplainLen()){
			sendError(protocol.getType(), Status.Error.GUILD_SIGN_TO_LONG_VALUE);
			return true;
		}
		
		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", String.valueOf(signId));
		
		String value = JsonFormat.printToString(req.getSignInfo());
		GameTssService.getInstance().wordUicChatFilter(player, signInfo, 
				MsgCategory.GUILD_SIGN.getNumber(), GameMsgCategory.ADD_GUILD_SIGN, 
				value, gameDataJson, protocol.getType());
		return true;
	}
	
	/**
	 * 移除联盟标记
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_REMOVE_SIGN_C_VALUE)
	public boolean onRemoveGuildSign(HawkProtocol protocol) {
		GuildRemoveSignReq req = protocol.parseProtocol(GuildRemoveSignReq.getDefaultInstance());
		player.msgCall(MsgId.REMOVE_GUILD_SIGN, GuildService.getInstance(), new GuildRemoveSignInvoker(player, req.getSignId()));
		return true;
	}
	
	
	/**
	 * 联盟签到
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_SING_C_VALUE)
	public boolean onGuildSignature(HawkProtocol protocol) {
		int result = GuildService.getInstance().onGuildSignature(player);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), result);
			return true;
		}
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 *	拉取联盟任务信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_GET_TASK_INFO_C_VALUE)
	public boolean onGetGuildTaskInfo(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN_VALUE);
			return true;
		}
		// 同步联盟任务信息
		GuildService.getInstance().syncPlayerGuildTaskInfo(player);
		return true;
	}
	
	/**
	 * 联盟任务领奖
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_GET_TASK_REWARD_C_VALUE)
	public boolean onGetGuildTaskReward(HawkProtocol protocol) {
		GuildTaskTakeRewardReq req = protocol.parseProtocol(GuildTaskTakeRewardReq.getDefaultInstance());
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN_VALUE);
			return true;
		}
		List<Integer> taskIds = req.getTaskIdList();
		AwardItems awardItems = AwardItems.valueOf();
		player.rpcCall(MsgId.GUILD_TASK_REWARD, GuildService.getInstance(), new GuildGetTaskRewardRpcInvoker(player, taskIds, awardItems, protocol.getType()));
		return true;
	}
	
	/**
	 * 获取联盟战争界面信息
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_GUILD_WAR_INFO_C_VALUE)
	protected boolean onGetGuildWarInfo(HawkProtocol protocol) {
		String guildId = player.getGuildId();

		// 玩家没有加入联盟
		if (HawkOSOperator.isEmptyString(guildId)) {
			GuildManorService.logger.error("onGetGuildWarInfo error, player have no guild. playerId:{}", player.getId());
			return true;
		}

		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = HPGetGuildWarInfoResp.newBuilder();
		// 联盟行军
		Collection<IWorldMarch> guildMarchs = WorldMarchService.getInstance().getGuildMarchs(guildId);

		for (IWorldMarch guildMarch : guildMarchs) {
			// 检测联盟战争，失败则移除
			if (!guildMarch.checkGuildWarShow() || guildMarch.getMarchEntity().isInvalid()) {
				WorldMarchService.getInstance().rmGuildMarch(guildMarch.getMarchId(), guildId);
				continue;
			}

			if (WorldUtil.getRelation(guildMarch.getMarchEntity(), player).equals(WorldMarchRelation.NONE)) {
				WorldMarchService.getInstance().rmGuildMarch(guildMarch.getMarchId(), guildId);
				continue;
			}

			// 行军状态
			WorldMarchStatus marchStatus = WorldMarchStatus.valueOf(guildMarch.getMarchEntity().getMarchStatus());

			GuildWarShowPB.Builder showPb = GuildWarShowPB.newBuilder();
			// 行军id
			showPb.setMarchId(guildMarch.getMarchId());
			// 行军类型
			showPb.setMarchType(guildMarch.getMarchType());
			// 行军状态
			showPb.setMarchStatus(marchStatus);
			// 攻击方行军数据
			showPb.setInitiative(guildMarch.getGuildWarInitiativeInfo());
			// 防守方行军数据
			showPb.setPassivity(guildMarch.getGuildWarPassivityInfo());
			// 行军结束时间(集结状态的行军为集结结束时间)
			if (marchStatus.equals(WorldMarchStatus.MARCH_STATUS_WAITING)) {
				showPb.setEndTime(guildMarch.getMarchEntity().getStartTime());
			} else {
				showPb.setEndTime(guildMarch.getMarchEntity().getEndTime());
			}
			if (GuildService.getInstance().isInTheSameGuild(player.getId(), guildMarch.getPlayerId())) {
				showPb.setIsatk(true);
			}
			try {
				GuildFormationObj formationObj = GuildService.getInstance().getGuildFormation(guildId);
				GuildFormationCell formation = formationObj.getGuildFormation(guildMarch.getMarchId());
				if (formation != null) {
					showPb.setFormationIndex(formation.getIndex().getNumber());
					showPb.setFormationName(formation.getName());
					showPb.setNeedJoin(formation.fight(player.getId()));
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			resp.addGuildWar(showPb);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_INFO_S_VALUE, resp));
		return true;
	}

	
	/**
	 * 获取联盟战争界面单条信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_GUILD_WAR_CELL_INFO_C_VALUE)
	public boolean onGetGuildWarCellInfo(HawkProtocol protocol) {
		String guildId = player.getGuildId();

		// 玩家没有加入联盟
		if (HawkOSOperator.isEmptyString(guildId)) {
			GuildManorService.logger.error("onGetGuildWarInfo error, player have no guild. playerId:{}", player.getId());
			return true;
		}
		
		HPGetGuildWarInfoReq req = protocol.parseProtocol(HPGetGuildWarInfoReq.getDefaultInstance());
		if (!req.hasMarchId()) {
			return false;
		}
		
		IWorldMarch march = WorldMarchService.getInstance().getMarch(req.getMarchId());
		
		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = HPGetGuildWarInfoResp.newBuilder();
		
		IWorldMarch guildMarch = march;
		if (guildMarch == null || guildMarch.getMarchEntity().isInvalid()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_CELL_INFO_S_VALUE, resp));
			return false;
		}
		
		// 检测联盟战争，失败则移除
		if (!guildMarch.checkGuildWarShow() || guildMarch.getMarchEntity().isInvalid()) {
			WorldMarchService.getInstance().rmGuildMarch(guildMarch.getMarchId(), guildId);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_CELL_INFO_S_VALUE, resp));
			return false;
		}

		if (WorldUtil.getRelation(guildMarch.getMarchEntity(), player).equals(WorldMarchRelation.NONE)) {
			WorldMarchService.getInstance().rmGuildMarch(guildMarch.getMarchId(), guildId);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_CELL_INFO_S_VALUE, resp));
			return false;
		}

		
		// 行军状态
		WorldMarchStatus marchStatus = WorldMarchStatus.valueOf(guildMarch.getMarchEntity().getMarchStatus());

		GuildWarShowPB.Builder showPb = GuildWarShowPB.newBuilder();
		// 行军id
		showPb.setMarchId(guildMarch.getMarchId());
		// 行军类型
		showPb.setMarchType(guildMarch.getMarchType());
		// 行军状态
		showPb.setMarchStatus(marchStatus);
		// 攻击方行军数据
		showPb.setInitiative(guildMarch.getGuildWarInitiativeInfo());
		// 防守方行军数据
		showPb.setPassivity(guildMarch.getGuildWarPassivityInfo());
		// 行军结束时间(集结状态的行军为集结结束时间)
		if (marchStatus.equals(WorldMarchStatus.MARCH_STATUS_WAITING)) {
			showPb.setEndTime(guildMarch.getMarchEntity().getStartTime());
		} else {
			showPb.setEndTime(guildMarch.getMarchEntity().getEndTime());
		}
		if (GuildService.getInstance().isInTheSameGuild(player.getId(), guildMarch.getPlayerId())) {
			showPb.setIsatk(true);
		}
		resp.addGuildWar(showPb);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_CELL_INFO_S_VALUE, resp));
		return true;
	}
	
	
	/**
	 * 获取联盟战争界面单条信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_GUILD_WAR_SINGLE_INFO_C_VALUE)
	public void onGetGuildWarSingleInfo(HawkProtocol protocol) {
		HPGetGuildWarSingleInfoReq req = protocol.parseProtocol(HPGetGuildWarSingleInfoReq.getDefaultInstance());
		String marchId = req.getMarchId();
		if (HawkOSOperator.isEmptyString(marchId)) {
			return;
		}
		IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
		if (march == null || march.getMarchEntity() == null || march.getMarchEntity().isInvalid()) {
			return;
		}
		WorldMarch worldMarch = march.getMarchEntity();
		
		Player player = GlobalData.getInstance().makesurePlayer(worldMarch.getPlayerId());
		GuildWarSingleInfo.Builder builder = GuildWarSingleInfo.newBuilder();
		builder.setPlayerId(worldMarch.getPlayerId());
		builder.setPlayerName(player.getName());
		builder.setIconId(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		WorldMarchStatus marchStatus = WorldMarchStatus.valueOf(worldMarch.getMarchStatus());
		builder.setMarchStatus(marchStatus);
		builder.setMarchId(worldMarch.getMarchId());
		List<PlayerHero> heros = player.getHeroByCfgId(worldMarch.getHeroIdList());
		if (heros != null && !heros.isEmpty()) {
			for (PlayerHero hero : heros) {
				builder.addHeroInfo(hero.toPBobj());
			}
		}
		SuperSoldier ssoldier = player.getSuperSoldierByCfgId(worldMarch.getSuperSoldierId()).orElse(null);
		if(Objects.nonNull(ssoldier)){
			builder.setSsoldier(ssoldier.toPBobj());
		}
		List<ArmyInfo> armys = worldMarch.getArmys();
		for (ArmyInfo army : armys) {
			builder.addArmys(army.toArmySoldierPB(player));
		}
		// 行军结束时间(集结状态的行军为集结结束时间)
		if (marchStatus.equals(WorldMarchStatus.MARCH_STATUS_WAITING)) {
			builder.setEndTime(worldMarch.getStartTime());
		} else {
			builder.setEndTime(worldMarch.getEndTime());
		}
		builder.setStartTime(worldMarch.getStartTime());
		builder.setJourneyTime(worldMarch.getMarchJourneyTime());
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_SINGLE_INFO_S_VALUE, builder));
	}
	
	/**
	 * 搜索未加入联盟的玩家信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PLAYER_GETLOCALPLAYERINFOBYNAME_C_VALUE)
	private boolean onGetLocalPlayerBasicInfo(HawkProtocol protocol) {
//		// 禁止输入文本，不能直接调这个接口，否则前端有些场景会出现弹框提示后转菊花的情况
//		if (!GameUtil.checkBanMsg(player)) {
//			return false;
//		}
		
		// 禁言玩家推送禁言提示
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
			if (banInfo != null) {
				GetPlayerBasicInfoResp.Builder builder = GetPlayerBasicInfoResp.newBuilder();
				builder.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_GETLOCALPLAYERINFOBYNAME_S_VALUE, builder));
				return false;
			}
		}
				
		GetPlayerBasicInfoReq req = protocol.parseProtocol(GetPlayerBasicInfoReq.getDefaultInstance());
		final String name = req.getName();
		if (HawkOSOperator.isEmptyString(name)) {
			sendError(protocol.getType(), Status.Error.NAME_ERROR_PLAYER);
			return false;
		}
		
		GameTssService.getInstance().wordUicChatFilter(player, name, 
				GameMsgCategory.GET_PLAYER_INFO, GameMsgCategory.GET_PLAYER_INFO, 
				"", null, protocol.getType());
		return true;
	}

	/**
	 * 初始化联盟邀请函推送
	 */
	private void initInvitationLetterPush() {
		invitationLetterPushTime = 0;
		HawkTuple2<Integer, Integer> timeRange = GuildConstProperty.getInstance().getInvitationRange();
		int timeMin = timeRange.first;
		int timeMax = timeRange.second;
		int addTime = timeMin;
		if (timeMax > timeMin) {
			addTime += HawkRand.randInt(timeMax - timeMin);
		}
		invitationLetterPushTime = HawkTime.getMillisecond() + addTime * 1000l;
	}
	
	/**
	 * 联盟成员提醒盟主建群
	 * 
	 * @param protocol
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@ProtocolHandler(code = HP.code.REMIND_CREATE_GROUP_C_VALUE)
	private boolean onRemindCreateGroup(HawkProtocol protocol) {
		// 没有联盟还提醒什么呀
		if (!player.hasGuild()) {
			HawkLog.errPrintln("remind create group failed, player join no guild, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		
		long lastRemindTime = LocalRedis.getInstance().getRemindTime(player.getId());
		long now = HawkApp.getInstance().getCurrentTime();
		// 一个玩家同一天内只能提醒一次
		if (lastRemindTime > 0 && HawkTime.isSameDay(now, lastRemindTime)) {
			HawkLog.errPrintln("remind create group failed, remind time gap not allowed, playerId: {}, lastTime: {}", player.getId(), HawkTime.formatTime(lastRemindTime));
			sendError(protocol.getType(), Status.Error.REMIND_GAP_LIMIT);
			return false;
		}
		
		String guildId = player.getGuildId();
		String leaderPlayerId = GuildService.getInstance().getGuildLeaderId(guildId);
		// 联盟盟主不能提醒自己建群吧
		if (player.getId().equals(leaderPlayerId)) {
			HawkLog.errPrintln("remind create group failed, remind self, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.REMIAD_SELF_NOT_ALLOWED);
			return false;
		}
		
		Player leader = GlobalData.getInstance().makesurePlayer(leaderPlayerId);
		// 盟主没找到
		if (leader == null) {
			HawkLog.errPrintln("remind create group failed, guild leader not found, playerId: {}, leaderId: {}", player.getId(), leaderPlayerId);
			sendError(protocol.getType(), Status.Error.GUILD_LEADER_NOT_FOUND);
			return false;
		}
		
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.CREATE_GROUP_REMIND, player);
		// 记下时间
		LocalRedis.getInstance().updateRemindTime(player.getId(), now, (int) ((HawkTime.getNextAM0Date() - now) / 1000 + 30));
		
		return true;
	}
	
	/**
	 * 联盟盟主刚建完群时提醒联盟成员加入群
	 * 
	 * @param protocol
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@ProtocolHandler(code = HP.code.REMIND_JOIN_GROUP_C_VALUE)
	private boolean onRemindJoinGroup(HawkProtocol protocol) {
		// 没有联盟还提醒什么呀
		if (!player.hasGuild()) {
			HawkLog.errPrintln("remind join group failed, player join no guild, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		
		// 不管type是否为0，都需要发送联盟聊天频道的消息
		GuildBoundInfoPB guildIdBoundInfo = protocol.parseProtocol(GuildBoundInfoPB.getDefaultInstance());
		String guildId = player.getGuildId();
		GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(guildId);
		guildInfoObject.updateGuildBoundId(guildIdBoundInfo.getGuildBoundId());
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.JOIN_GROUP_REMIND, player);
		// type 为0表示联盟盟主刚建完群，通知联盟成员 查询群数据推送
		if (guildIdBoundInfo.getNoticeType() == 0) {
			Collection<String>  guildMemberIds = GuildService.getInstance().getGuildMembers(player.getGuildId());
			for (String memberId : guildMemberIds) {
				if (memberId.equals(player.getId())) {
					continue;
				}
				
				Player memberPlayer = GlobalData.getInstance().getActivePlayer(memberId);
				if (memberPlayer != null) {
					memberPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.REMIND_LOAD_GROUPDATA_PUSH));
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 联盟盟主解绑群通知
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GROUP_UNBIND_C_VALUE)
	private boolean onUnbindGroup(HawkProtocol protocol) {
		// 没有联盟还提醒什么呀
		if (!player.hasGuild()) {
			HawkLog.errPrintln("unbind group notify failed, player join no guild, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		
		String guildId = player.getGuildId();
		String leaderPlayerId = GuildService.getInstance().getGuildLeaderId(guildId);
		// 非联盟盟主没有解绑的权限
		if (!player.getId().equals(leaderPlayerId)) {
			HawkLog.errPrintln("unbind group notify failed, no authority, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.NO_AUTHORITY_UNBIND_GROUP);
			return false;
		}
		
		GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(guildId);
		
		// QQ群解绑，服务器请求实现
		if (UserType.getByChannel(player.getChannel()) == UserType.QQ) {
			String result = SDKManager.getInstance().unlinkQQGroup(player.getChannel(), HawkTime.getSeconds(), player.getOpenId(), 
					player.getAccessToken(), guildInfoObject.getNumTypeId(), 
					GameUtil.strUUID2ServerId(guildId), String.valueOf(player.getPlatId()), player.getId(), guildInfoObject.getName());
			if (result == null) {
				HawkLog.errPrintln("unlink group notify failed, result null, playerId: {}", player.getId());
				sendError(protocol.getType(), Status.Error.UNLINK_QQ_GROUP_FAILED);
				return false;
			}
			
			JSONObject json = JSONObject.parseObject(result);
			if (json.getIntValue("ret") != 0) {
				HawkLog.errPrintln("unlink group notify failed, playerId: {}, result: {}", player.getId(), result);
				sendError(protocol.getType(), Status.Error.UNLINK_QQ_GROUP_FAILED);
				return false;
			}
		}
		
		guildInfoObject.updateGuildBoundId(null);
		
		// 通知联盟成员联盟群已解绑
		Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
		for (String playerId : members) {
			Player member = GlobalData.getInstance().getActivePlayer(playerId);
			if (member != null) {
				member.sendProtocol(HawkProtocol.valueOf(HP.code.GROUP_UNBIND_NOTIFY_MEMBER_S));
			}
		}
		
		player.responseSuccess(protocol.getType());
		
		return true;
	}
	
	
	
	/**
	 * 获取QQ公会绑群groupOpenid信息
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_GROUP_OPENID_QQ_C_VALUE)
	private boolean fetchQQGroupOpenidInfo(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			HawkLog.errPrintln("get groupOpenidInfo failed, player join no guild, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		
		// 非手Q用户的玩家不支持
		if (UserType.getByChannel(player.getChannel()) != UserType.QQ) {
			HawkLog.errPrintln("get groupOpenidInfo failed, player channel error, playerId: {}, channel: {}", player.getId(), player.getChannel());
			sendError(protocol.getType(), Status.Error.UNSURPPORT_CHANNEL_PLAYER);
			return false;
		}
		
		fetchQQGroupOpenid();
		
		player.responseSuccess(protocol.getType());
		
		return true;
	}
	
	/**
	 * 发送QQ群消息
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SEND_QQ_GROUP_MSG_REQ_VALUE)
	private boolean sendQQGroupMsg(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			HawkLog.errPrintln("send QQ group msg failed, player join no guild, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		
		// 非手Q用户的玩家不支持
		if (UserType.getByChannel(player.getChannel()) != UserType.QQ) {
			HawkLog.errPrintln("send QQ group msg failed, player channel error, playerId: {}, channel: {}", player.getId(), player.getChannel());
			sendError(protocol.getType(), Status.Error.UNSURPPORT_CHANNEL_PLAYER);
			return false;
		}
		
		//
		String guildId = player.getGuildId();
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
		final String numTypeGuildId = guildInfo.getNumTypeId();
		
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getExtraThreadNum());
		HawkTaskManager.getInstance().asyncCallback(new HawkTask() {
			@Override
			public Object run() {
				try {
					String groupOpenIdInfo = SDKManager.getInstance().getQQGroupOpenid(player.getChannel(), HawkTime.getSeconds(), 
							player.getOpenId(), player.getAccessToken(), "0", numTypeGuildId, GameUtil.strUUID2ServerId(guildId));

					if (!HawkOSOperator.isEmptyString(groupOpenIdInfo)) {
						return groupOpenIdInfo;
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				return null;
			}

		}, threadIdx, new HawkCallback() {
			@Override
			public int invoke(Object args) { 
				if (args == null) {
					HawkLog.errPrintln("get groupOpenidInfo failed, result null, playerId: {}", player.getId());
					return 0;
				}
				
				JSONObject json = JSONObject.parseObject(String.valueOf(args));
				if (!json.containsKey("ret") || json.getInteger("ret") != 0) {
					HawkLog.errPrintln("get groupOpenidInfo succ, resultCode: {}, playerId: {}", json.getString("ret"), player.getId());
					return 0;
				}
				
				JSONObject data = json.getJSONObject("data");
				String groupOpenid = data.getString("group_openid");
				
				PlatformConstCfg cfg = PlatformConstCfg.getInstance();
				Map<String, String> map = new HashMap<>();
				map.put("channel", player.getChannel());
				map.put("group_openid", groupOpenid);
				map.put("title", cfg.getQq_groupmsg_title());
				map.put("desc", cfg.getQq_groupmsg_desc());
				map.put("image_url", cfg.getQq_groupmsg_image_url());
				map.put("redirect_url", cfg.getQq_groupmsg_redirect_url());
				map.put("param", cfg.getQq_groupmsg_param() == null ? "" : cfg.getQq_groupmsg_param());
				
				// 发送QQ群消息
				SDKManager.getInstance().sendQQGroupMsg(map, player.getPfTokenJson());		
				
				return 0;
			}
		});
		
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 获取QQ公会绑群groupOpenid信息
	 * 
	 * @return
	 */
	private void fetchQQGroupOpenid() {
		String guildId = player.getGuildId();
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
		final String numTypeGuildId = guildInfo.getNumTypeId();
		
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getExtraThreadNum());
		HawkTaskManager.getInstance().asyncCallback(new HawkTask() {
			@Override
			public Object run() {
				try {
					String groupOpenIdInfo = SDKManager.getInstance().getQQGroupOpenid(player.getChannel(), HawkTime.getSeconds(), 
							player.getOpenId(), player.getAccessToken(), "0", numTypeGuildId, GameUtil.strUUID2ServerId(guildId));

					if (!HawkOSOperator.isEmptyString(groupOpenIdInfo)) {
						return groupOpenIdInfo;
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				return null;
			}

		}, threadIdx, new HawkCallback() {
			@Override
			public int invoke(Object args) { 
				GroupOpenIdInfoPB.Builder builder = GroupOpenIdInfoPB.newBuilder();
				builder.setBinded(false);
				if (args == null) {
					HawkLog.errPrintln("get groupOpenidInfo failed, result null, playerId: {}", player.getId());
					player.sendProtocol(HawkProtocol.valueOf(HP.code.GROUP_OPENID_QQ_PUSH_VALUE, builder));
					return 0;
				}
				
				JSONObject json = JSONObject.parseObject(String.valueOf(args));
				if (!json.containsKey("ret") || json.getInteger("ret") != 0) {
					HawkLog.errPrintln("get groupOpenidInfo succ, resultCode: {}, playerId: {}", json.getString("ret"), player.getId());
					player.sendProtocol(HawkProtocol.valueOf(HP.code.GROUP_OPENID_QQ_PUSH_VALUE, builder));
					return 0;
				}
				
				JSONObject data = json.getJSONObject("data");
				String gc = data.getString("gc");
				if (HawkOSOperator.isEmptyString(gc)) {
					HawkLog.errPrintln("get groupOpenidInfo succ, gc attr empty, playerId: {}", player.getId());
					player.sendProtocol(HawkProtocol.valueOf(HP.code.GROUP_OPENID_QQ_PUSH_VALUE, builder));
					return 0;
				}
				
				builder.setBinded(true);
				builder.setGc(gc);
				builder.setGroupKey(data.getString("group_key"));
				builder.setGroupOpenid(data.getString("group_openid"));
				
				String relationInfo = SDKManager.getInstance().getQQGroupInfo(player.getChannel(), HawkTime.getSeconds(), 
						player.getOpenId(), player.getAccessToken(), gc);
				if (relationInfo == null) {
					HawkLog.errPrintln("get groupInfo failed, result null, playerId: {}, gc: {}, groupOpenid: {}", player.getId(), gc, data.getString("group_openid"));
					player.sendProtocol(HawkProtocol.valueOf(HP.code.GROUP_OPENID_QQ_PUSH_VALUE, builder));
					return 0;
				}
				
				JSONObject relationJson = JSONObject.parseObject(relationInfo);
				if (relationJson.containsKey("ret") && relationJson.getInteger("ret") == 0) {
					JSONObject jsonObject = relationJson.getJSONObject("data");
					builder.setRelation(jsonObject.getInteger("relation"));
				} else {
					HawkLog.errPrintln("get groupInfo succ, resultCode: {}, playerId: {}, gc: {}, groupOpenid: {}", relationJson.getInteger("ret"), player.getId(), gc, data.getString("group_openid"));
					builder.setErrorCode(relationJson.getInteger("ret"));
					builder.setDesc(relationJson.getString("message"));
				}
				
				player.sendProtocol(HawkProtocol.valueOf(HP.code.GROUP_OPENID_QQ_PUSH_VALUE, builder));
				return 0;
			}
		});
	}
	
	
	/**
	 * 申请联盟官员
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_APPLY_OFFICER_C_VALUE)
	private boolean onApplyOfficer(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN_VALUE);
			return false;
		}
		ApplyGuildOfficerReq req = protocol.parseProtocol(ApplyGuildOfficerReq.getDefaultInstance());
		GuildService.getInstance().dealMsg(MsgId.APPLY_GUILD_OFFICER, new GuildApplyOfficerInvoker(player, req.getOfficerId()));
		return true;
	}
	
	
	/**
	 * 获取联盟官员申请列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_GET_OFFICER_APPLY_LIST_C_VALUE)
	private boolean onGetApplyOfficerList(HawkProtocol protocol) {
		// 权限判定
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.APPOINT_OFFICER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		GetOfficerApplyListReq req = protocol.parseProtocol(GetOfficerApplyListReq.getDefaultInstance());
		AllianceOfficialCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceOfficialCfg.class, req.getOfficerId());
		// 联盟官职id错误
		if (cfg == null || !cfg.canAppoint()) {
			sendError(protocol.getType(), Status.Error.GUILD_OFFICEID_ERROR_VALUE);
			return true;
		}
		GetOfficerApplyListResp.Builder resp = GuildService.getInstance().getOfficerApplyList(player.getGuildId(), req.getOfficerId());
		sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_OFFICER_APPLY_LIST_S, resp));
		return true;
	}
	
	
	/**
	 * 任命联盟官员
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_APPOINT_OFFICER_C_VALUE)
	private boolean onAppointOfficer(HawkProtocol protocol) {
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		// 权限判定
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.APPOINT_OFFICER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		AppointGuildOfficerReq req = protocol.parseProtocol(AppointGuildOfficerReq.getDefaultInstance());
		GuildService.getInstance().dealMsg(MsgId.APPOINT_GUILD_OFFICER, new GuildAppointOfficerInvoker(player, req.getTarPlayerId(), req.getOfficerId()));
		return true;
	}
	
	
	/**
	 * 解除联盟官员
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_DISMISS_OFFICER_C_VALUE)
	private boolean onDismissOfficer(HawkProtocol protocol) {
		// 跨服活动开启期间不能进行部分联盟操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		// 权限判定
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.APPOINT_OFFICER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		DismissGuildOfficerReq req = protocol.parseProtocol(DismissGuildOfficerReq.getDefaultInstance());
		GuildService.getInstance().dealMsg(MsgId.DISMISS_GUILD_OFFICER, new GuildDismissOfficerInvoker(player, req.getTarPlayerId()));
		return true;
	}
	
	/**
	 * 获取联盟排行
	 * 
	 * @param protocol
	 * @return
	 */
	
	@ProtocolHandler(code = HP.code.GUILD_GET_RANK_C_VALUE)
	private boolean onGetRank(HawkProtocol protocol) {
		GuildGetRank req = protocol.parseProtocol( GuildGetRank.getDefaultInstance());
		GuildRankType rt =  req.getRankType();
		GuildService.getInstance().dealMsg(MsgId.GUILD_GET_RANK, new GuildGetRankInvoker(player, rt));
		return true;
	}
	
	@ProtocolHandler(code = HP.code.GUILD_VOICE_ROOM_JOIN_C_VALUE)
	private boolean onJoinVoiceRoom(HawkProtocol protocol) {
		boolean ret = VoiceRoomManager.getInstance().onPlayerJoin(player.getId());
		if(!ret){
			player.sendProtocol(HawkProtocol.valueOf( HP.code.GUILD_VOICE_ROOM_JOIN_FAILED_S ));
		}
		return true;
	}
	
	@ProtocolHandler(code = HP.code.GUILD_VOICE_ROOM_QUIT_C_VALUE)
	private boolean onQuitVoiceRoom(HawkProtocol protocol) {
		VoiceRoomManager.getInstance().onPlayerQuit(player.getId());
		return true;
	}
	
	@ProtocolHandler(code = HP.code.GUILD_VOICE_ROOM_CHANGE_MODEL_C_VALUE)
	private boolean onChangeRoomModel(HawkProtocol protocol) {
		// 权限判定
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.VOICE_ROOM_MODEL_CHANGE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		GuildVoiceChangeModelReq req = protocol.parseProtocol(GuildVoiceChangeModelReq.getDefaultInstance());
		player.msgCall(MsgId.CHANGE_VOICE_ROOM_MODEL, GuildService.getInstance(), new GuildChangeRoomModelInvoker(player, req.getModel(), protocol.getType()));
		return true;
	}
	
	/**
	 * 获取主力部队信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_GET_MAINFORCE_C_VALUE)
	private boolean onGetMainForceInfo(HawkProtocol protocol) {
		MainForceInfo.Builder builder = MainForceInfo.newBuilder();
		Set<SoldierType> types = player.getMainForce();
		if(!types.isEmpty()){
			builder.addAllType(types);
		}
		sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_MAINFORCE_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 更新主力部队信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_UPDATE_MAINFORCE_C_VALUE)
	private boolean onEditMainForceInfo(HawkProtocol protocol) {
		EditMainForce req = protocol.parseProtocol(EditMainForce.getDefaultInstance());
		SoldierType type = req.getType();
		Set<SoldierType> curTypes = player.getMainForce();
		boolean needUpdate = false;
		// 新增
		if (req.getOpType() == 1) {
			if (!curTypes.contains(type)) {
				if (curTypes.size() + 1 > TiberiumConstCfg.getInstance().getMySoldierTypeLimit()) {
					return false;
				}
				curTypes.add(type);
				needUpdate = true;
			}
		} else {
			if (curTypes.contains(type)) {
				curTypes.remove(type);
				needUpdate = true;
			}
		}
		if (needUpdate) {
			StringBuilder sb = new StringBuilder();
			if (!curTypes.isEmpty()) {
				for (SoldierType sType : curTypes) {
					sb.append(sType.getNumber()).append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			CustomDataEntity customDataEntity = player.getData().getCustomDataEntity(GsConst.PLAYER_MAIN_FORCE);
			if (customDataEntity == null) {
				player.getData().createCustomDataEntity(GsConst.PLAYER_MAIN_FORCE, 0, sb.toString());
			} else {
				customDataEntity.setArg(sb.toString());
			}
		}
		MainForceInfo.Builder builder = MainForceInfo.newBuilder();
		if (!curTypes.isEmpty()) {
			builder.addAllType(curTypes);
		}
		sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_UPDATE_MAINFORCE_S_VALUE, builder));
		return true;
	}
	
	
}
