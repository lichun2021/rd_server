package com.hawk.game.module.college;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.CollegeAcceptInviteInvoker;
import com.hawk.game.invoker.CollegeAddContributeMsg;
import com.hawk.game.invoker.CollegeAgreeApplyInvoker;
import com.hawk.game.invoker.CollegeApplyInvoker;
import com.hawk.game.invoker.CollegeCoachAutoChangeCoahMsg;
import com.hawk.game.invoker.CollegeDismissInvoker;
import com.hawk.game.invoker.CollegeFastJoinInvoker;
import com.hawk.game.invoker.CollegeJoinFreeSetMsg;
import com.hawk.game.invoker.CollegeKickInvoker;
import com.hawk.game.invoker.CollegeQuitInvoker;
import com.hawk.game.invoker.CollegeSetCoahRpcInvoker;
import com.hawk.game.invoker.CollegeVitSendMsg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.college.CollegeConst.CollegeGiftType;
import com.hawk.game.module.college.cfg.CollegeAchieveCfg;
import com.hawk.game.module.college.cfg.CollegeConstCfg;
import com.hawk.game.module.college.cfg.CollegePurchaseCfg;
import com.hawk.game.module.college.cfg.CollegeShopCfg;
import com.hawk.game.module.college.entity.CollegeMemberEntity;
import com.hawk.game.module.college.entity.CollegeMemberGiftEntity;
import com.hawk.game.module.college.entity.CollegeMemberShopEntity;
import com.hawk.game.module.college.entity.CollegeMissionEntityItem;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.msg.CollegeGiftBuyMsg;
import com.hawk.game.msg.CollegeJoinMsg;
import com.hawk.game.msg.CollegeQuitMsg;
import com.hawk.game.msg.PlayerVitCostMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MilitaryCollege.ApplyCollegeReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeAlterResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeAuth;
import com.hawk.game.protocol.MilitaryCollege.CollegeCoachChangeReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeCreateReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeExchangeReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeExchangeResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeGiftBuyReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeGiftBuyResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeGiftItem;
import com.hawk.game.protocol.MilitaryCollege.CollegeJoinFreeSetReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeMissionTakeRewardReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeMissionUpdateResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeRecommendListReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeRecommendListResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeRenameReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeSearchReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeSearchResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeShopTip;
import com.hawk.game.protocol.MilitaryCollege.CollegeShopTipReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeShopTipResp;
import com.hawk.game.protocol.MilitaryCollege.CollegeTakeOnlineRewardInfoReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeVitalitySendReq;
import com.hawk.game.protocol.MilitaryCollege.CollegeVitalitySendResp;
import com.hawk.game.protocol.MilitaryCollege.DealApplyReq;
import com.hawk.game.protocol.MilitaryCollege.GetApplyListResp;
import com.hawk.game.protocol.MilitaryCollege.GetCollegeLeaderResp;
import com.hawk.game.protocol.MilitaryCollege.InviteJoinCollegeReq;
import com.hawk.game.protocol.MilitaryCollege.InviteLoginReq;
import com.hawk.game.protocol.MilitaryCollege.InviteType;
import com.hawk.game.protocol.MilitaryCollege.KickoutCollegeReq;
import com.hawk.game.protocol.MilitaryCollege.SearchCoachReq;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.college.CollegeMissionType;
import com.hawk.game.service.college.CollegeService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 军事学院系统模块
 * 
 * @author Jesse
 */
public class PlayerCollegeModule extends PlayerModule {

	private long lastRefreshTime = 0;

	/**
	 * 构造函数
	 * 
	 * @param player
	 */
	public PlayerCollegeModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		lastRefreshTime = HawkTime.getMillisecond();
		if (player.isCsPlayer()) {
			return true;
		}
		 //这里可能要修改个人数据，就不放在task线程执行了
		CollegeService.getInstance().checkData(player);
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					loginProcess();
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("CollegeModuleLogin");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
		} else {
			loginProcess();
		}
		return true;
	}
	
	/**
	 * 登录处理
	 */
	private void loginProcess() {
		CollegeService.getInstance().syncCollegeInfo(player);
		CollegeService.getInstance().syncCollegeMemberData(player);
		CollegeMemberEntity self = getPlayerData().getCollegeMemberEntity();
		if (self.getAuth() == CollegeAuth.COACH_VALUE) {
			GetApplyListResp.Builder applyInfo = CollegeService.getInstance().getApplyList(self.getCollegeId());
			sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_APPLY_SYNC, applyInfo));
		}
		//检查一下是否可以替换教官
		if(player.hasCollege()){
			player.msgCall(MsgId.COLLEGE_COACH_AUTO_SET, CollegeService.getInstance(),
					new CollegeCoachAutoChangeCoahMsg(player));
		}
	}
	
	

	@Override
	protected boolean onPlayerLogout() {
		return true;
	}

	/**
	 * 创建学院
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_CREATE_REQ_C_VALUE)
	private boolean onCreateCollege(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		CollegeCreateReq req = protocol.parseProtocol(CollegeCreateReq.getDefaultInstance());
		String collegeName = req.getName();
		
		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() != CollegeAuth.NOJOIN_VALUE) {
			sendError(protocol.getType(), Status.Error.ALREADY_IN_COLLEGE);
			return true;
		}

		if (player.getCityLevel() < CollegeConstCfg.getInstance().getCoachCityLvlLimit()) {
			sendError(protocol.getType(), Status.Error.CITY_LEVEL_NOT_ENOUGH);
			return true;
		}

		if (player.getLevel() < CollegeConstCfg.getInstance().getCoachLevelLimit()) {
			sendError(protocol.getType(), Status.Error.PLAYER_LEVEL_NOT_ENOUGH);
			return true;
		}
		int rlt = CollegeService.getInstance().checkCollegeName(collegeName);
		if(rlt != 0){
			sendError(protocol.getType(), rlt);
			return true;
		}
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(CollegeConstCfg.getInstance().getCreateCostList());
		if (!consume.checkConsume(player)) {
			sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return true;
		}
		
		JSONObject gameDataJson = new JSONObject();
		GameTssService.getInstance().wordUicChatFilter(player, collegeName,
				MsgCategory.COLLEGE_CREATE.getNumber(), GameMsgCategory.CREATE_COLLEGE,
				"", gameDataJson, protocol.getType());
		return true;
	}

	/**
	 * 解散学院
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_DISMISS_REQ_C_VALUE)
	private boolean onDismissCollege(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服活动开启期间不能进行部分操作
		if (CrossActivityService.getInstance().isOpen()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		//月球期间不能进行部分操作
		if(YQZZMatchService.getInstance().activityOpening()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_ACTION_ILLEGAL_DURNING_OPEN);
			return false;
		}
		
		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() != CollegeAuth.COACH_VALUE) {
			sendError(protocol.getType(), Status.Error.COLLEGE_NOT_COACH);
			return true;
		}
		player.msgCall(MsgId.COLLEGE_DISMISS, CollegeService.getInstance(), new CollegeDismissInvoker(player, protocol.getType()));
		return true;
	}

	/**
	 * 退出学院
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_QUIT_REQ_C_VALUE)
	private boolean onQuitCollege(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}

		// 跨服期间该操作禁用
		if (player.isCsPlayer()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() != CollegeAuth.TRAINEE_VALUE) {
			sendError(protocol.getType(), Status.Error.COLLEGE_NOT_TRAINEE);
			return true;
		}
		player.msgCall(MsgId.COLLEGE_QUIT, CollegeService.getInstance(), new CollegeQuitInvoker(player, protocol.getType()));
		return true;
	}

	/**
	 * 踢出学院
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_KICKOUT_REQ_C_VALUE)
	private boolean onKickCollege(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		KickoutCollegeReq req = protocol.parseProtocol(KickoutCollegeReq.getDefaultInstance());
		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() != CollegeAuth.COACH_VALUE) {
			sendError(protocol.getType(), Status.Error.COLLEGE_NOT_COACH);
			return true;
		}
		List<String> ids = req.getMemberIdList();
		if (ids == null || ids.size() == 0) {
			sendError(protocol.getType(), Status.SysError.DATA_ERROR_VALUE);
			return true;
		}
		for(String id : ids){
			// 不能踢出跨服玩家
			if(CrossService.getInstance().isCrossPlayer(id)){
				sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT);
				return true;
			}
		}
		player.msgCall(MsgId.COLLEGE_KICK, CollegeService.getInstance(), new CollegeKickInvoker(player, new ArrayList<>(ids), protocol.getType()));
		return true;
	}

	/**
	 * 获取学院信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_GET_INFO_REQ_C_VALUE)
	private boolean onGetCollegeInfo(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() == CollegeAuth.NOJOIN_VALUE) {
			sendError(protocol.getType(), Status.Error.NOT_IN_COLLEGE);
			return true;
		}
		CollegeService.getInstance().syncCollegeInfo(player);
		CollegeService.getInstance().syncCollegeMemberData(player);
		return true;
	}

	/**
	 * 申请加入学院
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_APPLY_REQ_C_VALUE)
	private boolean onApplyCollege(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		ApplyCollegeReq req = protocol.parseProtocol(ApplyCollegeReq.getDefaultInstance());
		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() != CollegeAuth.NOJOIN_VALUE) {
			sendError(protocol.getType(), Status.Error.ALREADY_IN_COLLEGE);
			return true;
		}
		boolean joinFree = CollegeService.getInstance().joinCollegeFree( req.getCollegeId());
		if(joinFree){
			player.msgCall(MsgId.COLLEGE_FAST_JOIN, CollegeService.getInstance(), new CollegeFastJoinInvoker(player, req.getCollegeId(), protocol.getType()));
		}else{
			player.msgCall(MsgId.COLLEGE_APPLY, CollegeService.getInstance(), new CollegeApplyInvoker(player, req.getCollegeId(), protocol.getType()));
		}
		return true;
	}

	/**
	 * 通过申请
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_AGREE_APPLY_REQ_C_VALUE)
	private boolean onAgreeApply(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		DealApplyReq req = protocol.parseProtocol(DealApplyReq.getDefaultInstance());
		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() != CollegeAuth.COACH_VALUE) {
			sendError(protocol.getType(), Status.Error.COLLEGE_NOT_COACH);
			return true;
		}
		List<String> ids = req.getMemberIdList();
		if (ids == null || ids.size() == 0) {
			sendError(protocol.getType(), Status.SysError.DATA_ERROR);
			return true;
		}
		if (CrossService.getInstance().isCrossPlayer(ids.get(0))) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT);
			return true;
		}
		player.msgCall(MsgId.COLLEGE_AGREE_APPLY, CollegeService.getInstance(), new CollegeAgreeApplyInvoker(player, ids.get(0), protocol.getType()));
		return true;
	}

	/**
	 * 拒绝申请
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_REFUSE_APPLY_REQ_C_VALUE)
	private boolean onRefuseApply(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		DealApplyReq req = protocol.parseProtocol(DealApplyReq.getDefaultInstance());

		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() != CollegeAuth.COACH_VALUE) {
			sendError(protocol.getType(), Status.Error.COLLEGE_NOT_COACH);
			return true;
		}
		
		List<String> ids = req.getMemberIdList();
		if (ids == null || ids.size() == 0) {
			sendError(protocol.getType(), Status.SysError.DATA_ERROR);
			return true;
		}
		
		if (CrossService.getInstance().isCrossPlayer(ids.get(0))) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT);
			return true;
		}
		
		CollegeService.getInstance().onRefuseApply(player, ids.get(0));
		GetApplyListResp.Builder applyInfo = CollegeService.getInstance().getApplyList(player.getCollegeId());
		sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_APPLY_SYNC, applyInfo));
		return true;
	}

	/**
	 * 邀请加入学院
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_INVITE_JOIN_REQ_C_VALUE)
	private boolean onInviteJoin(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		InviteJoinCollegeReq req = protocol.parseProtocol(InviteJoinCollegeReq.getDefaultInstance());
		
		if (CrossService.getInstance().isCrossPlayer(req.getPlayerId())) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT);
			return true;
		}
		
		CollegeMemberEntity self = getPlayerData().getCollegeMemberEntity();
		if (self.getAuth() != CollegeAuth.COACH_VALUE) {
			sendError(protocol.getType(), Status.Error.COLLEGE_NOT_COACH);
			return true;
		}
		InviteType type = req.getType();
		switch (type) {
		case GUILD_CHAT:
			String collegeId = self.getCollegeId();
			boolean sendCd = CollegeService.getInstance().sendGuildApplyInCd(collegeId);
			if(sendCd){
				sendError(protocol.getType(), Status.Error.COLLEGE_GUILD_APPLY_SEND_CD_VALUE);
				return true;
			}
			String applyId = CollegeService.getInstance().createGuildApplayId(collegeId);
			if(!HawkOSOperator.isEmptyString(applyId)){
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, Const.NoticeCfgId.COLLEGE_INVITE, player, player.getName(),
						self.getCollegeId(),applyId);
			}
			break;
		// TOTO 微信/手Q好友
		case APP_FRIEND:

			break;
		case GAME_FRIEND:
			String targetId = req.getPlayerId();
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
			if(targetPlayer==null){
				sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST);
				return true;
			}
			if (targetPlayer.hasCollege()) {
				sendError(protocol.getType(), Status.Error.ALREADY_IN_COLLEGE);
				return true;
			}
			boolean sendMailCd = CollegeService.getInstance().sendInviteMailInCd(self.getCollegeId(), targetId);
			if(sendMailCd){
				sendError(protocol.getType(), Status.Error.COLLEGE_INVITE_MAIL_SEND_CD);
				return true;
			}
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(targetId).setMailId(MailId.COLLEGE_INVITE).addTitles(player.getName())
					.addSubTitles(player.getName()).addContents(player.getName(), self.getCollegeId()).build());
			CollegeService.getInstance().restInviteMailSendTime(self.getCollegeId(), targetId);
			break;

		default:
			break;
		}
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 接受邀请加入学院
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_ACCEPT_INVITE_REQ_C_VALUE)
	private boolean onAcceptInvite(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		ApplyCollegeReq req = protocol.parseProtocol(ApplyCollegeReq.getDefaultInstance());
		CollegeMemberEntity entity = getPlayerData().getCollegeMemberEntity();
		if (entity.getAuth() != CollegeAuth.NOJOIN_VALUE) {
			sendError(protocol.getType(), Status.Error.ALREADY_IN_COLLEGE);
			return true;
		}

		player.msgCall(MsgId.COLLEGE_APPLY, CollegeService.getInstance(), 
				new CollegeAcceptInviteInvoker(player, req.getCollegeId(), req.getGuildApplyId(),protocol.getType()));
		return true;
	}

	/**
	 * 邀请学院成员上线
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_INVITE_LOGIN_REQ_C_VALUE)
	private boolean onInviteLogin(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		InviteLoginReq req = protocol.parseProtocol(InviteLoginReq.getDefaultInstance());
		
		if (CrossService.getInstance().isCrossPlayer(req.getMemberId())) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT);
			return true;
		}
		
		int result = CollegeService.getInstance().inviteLogin(player, req.getMemberId(), req.getPlatFriend());
		if (result == SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
		} else {
			sendError(protocol.getType(), result);
		}
		return true;
	}

	/**
	 * 获取成员申请列表
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_GET_APPLY_LIST_REQ_C_VALUE)
	private boolean onGetInviteList(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		CollegeMemberEntity self = getPlayerData().getCollegeMemberEntity();
		if (self.getAuth() != CollegeAuth.COACH_VALUE) {
			sendError(protocol.getType(), Status.Error.COLLEGE_NOT_COACH);
			return true;
		}
		GetApplyListResp.Builder builder = CollegeService.getInstance().getApplyList(self.getCollegeId());
		sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_GET_APPLY_LIST_RESP_S, builder));
		return true;
	}

	/**
	 * 获取可加入学院的教官列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_GET_LIST_REQ_C_VALUE)
	private boolean onGetCoachList(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		CollegeMemberEntity self = getPlayerData().getCollegeMemberEntity();
		if (self.getAuth() != CollegeAuth.NOJOIN_VALUE) {
			sendError(protocol.getType(), Status.Error.ALREADY_IN_COLLEGE);
			return true;
		}
		GetCollegeLeaderResp.Builder builder = CollegeService.getInstance().getCanApplyCoachList(player);
		sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_GET_LIST_RESP_S, builder));
		return true;
	}

	
	/**
	 * 搜索教官
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLEGE_SEARCH_COACH_REQ_C_VALUE)
	private boolean onSearchCoach(HawkProtocol protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		SearchCoachReq req = protocol.parseProtocol(SearchCoachReq.getDefaultInstance());
		GetCollegeLeaderResp.Builder builder = CollegeService.getInstance().getSearchCoach(player, req.getName(), ConstProperty.getInstance().getSearchPrecise() > 0);
		sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_SEARCH_COACH_RESP_S, builder));
		return true;
	}


	/**
	 * 获取学院列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_RECOMMEND_REQ_C_VALUE)
	private boolean getCollegeRecommendList(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		CollegeRecommendListReq req = protocol.parseProtocol(CollegeRecommendListReq.getDefaultInstance());
		int page = req.getPage();
		CollegeRecommendListResp.Builder builder = CollegeService.getInstance().getCollegeRecommendListBuilder(player, page);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_RECOMMEND_REQ_S_VALUE, builder));
		return true;
	}
	
	
	
	
	/**
	 * 设置自由进入开关
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_JOIN_FREE_SWITCH_REQ_C_VALUE)
	private boolean setCollegeJoinFree(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		CollegeJoinFreeSetReq req = protocol.parseProtocol(CollegeJoinFreeSetReq.getDefaultInstance());
		int type = req.getJoinFree();
		
		player.msgCall(MsgId.COLLEGE_JOIN_FREE_SET, CollegeService.getInstance(), 
				new CollegeJoinFreeSetMsg(player, type, protocol.getType()));
		return true;
	}
	
	
	/***
	 * 转让教官
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_COACH_CHANGE_REQ_C_VALUE)
	private boolean setCollegecoach(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		boolean selfCross = CrossService.getInstance().isCrossPlayer(player.getId());
		if(selfCross){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		CollegeCoachChangeReq req = protocol.parseProtocol(CollegeCoachChangeReq.getDefaultInstance());
		String targetId = req.getName();
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
		if(Objects.isNull(targetPlayer)){
			sendError(protocol.getType(), Status.SysError.DATA_ERROR_VALUE);
			return false;
		}
		boolean targetCross = CrossService.getInstance().isCrossPlayer(targetId);
		if(targetCross){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		boolean same = CollegeService.getInstance().isSameCollege(player.getId(),targetId);
		if(!same){
			sendError(protocol.getType(), Status.SysError.DATA_ERROR_VALUE);
			return false;
		}
		if (targetPlayer.getCityLevel() < CollegeConstCfg.getInstance().getCoachCityLvlLimit()) {
			sendError(protocol.getType(), Status.Error.COLLEGE_MEMBER_CITY_LEVEL_NOT_ENOUGH);
			return true;
		}

		if (targetPlayer.getLevel() < CollegeConstCfg.getInstance().getCoachLevelLimit()) {
			sendError(protocol.getType(), Status.Error.COLLEGE_MEMBER_LEVEL_NOT_ENOUGH);
			return true;
		}
		player.msgCall(MsgId.COLLEGE_COACH_SET, CollegeService.getInstance(),
				new CollegeSetCoahRpcInvoker(player, targetPlayer,protocol.getType()));
		return true;
	}
	
	/**
	 * 快速加入
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_JOIN_FREE_REQ_C_VALUE)
	private boolean joinCollegeFast(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		player.msgCall(MsgId.COLLEGE_FAST_JOIN, CollegeService.getInstance(), 
				new CollegeFastJoinInvoker(player, null,protocol.getType()));
		return true;
	}
	
	
	/**
	 * 根据名字搜索学院
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_SEARCH_REQ_C_VALUE)
	private boolean searchCollegeByName(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		CollegeSearchReq req = protocol.parseProtocol(CollegeSearchReq.getDefaultInstance());
		String name = req.getName();
		CollegeSearchResp.Builder builder = CollegeService.getInstance()
				.searchCollegeByName(player,name,protocol.getType());
		if(builder.getCollegesCount() <= 0){
			player.sendError(HP.code2.COLLEGE_SEARCH_REQ_S_VALUE, Status.Error.COLLEGE_NOT_EXIST_VALUE, 0);
		}else{
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_SEARCH_REQ_S_VALUE, builder));
		}
		return true;
	}
	
	/**
	 * 学院重新命名
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_RENAME_REQ_C_VALUE)
	private boolean collegeReName(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return true;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		//没有学院
		if(player.getCollegeAuth() != CollegeAuth.COACH_VALUE){
			return true;
		}
		CollegeRenameReq req = protocol.parseProtocol(CollegeRenameReq.getDefaultInstance());
		String collegeName = req.getName();
		int checkRlt = CollegeService.getInstance().checkCollegeName(collegeName);
		if(checkRlt != 0){
			sendError(protocol.getType(), checkRlt);
			return true;
		}
		String collegeId = player.getCollegeId();
		ItemInfo cost = CollegeService.getInstance().getReNameCost(collegeId);
		if(Objects.nonNull(cost)){
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(cost, false);
			if(!consume.checkConsume(player)){
				sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH_VALUE);
				return true;
			}
		}
		JSONObject gameDataJson = new JSONObject();
		GameTssService.getInstance().wordUicChatFilter(player, collegeName,
				MsgCategory.COLLEGE_NAME_CHANGE.getNumber(), GameMsgCategory.CHANGE_COLLEGE_NAME,
				"", gameDataJson, protocol.getType());
		return true;
	}
	
	/**
	 * 兑换商店兑换
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_EXCHANGE_C_VALUE)
	private boolean onCollegeExchange(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return false;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		CollegeExchangeReq req = protocol.parseProtocol(CollegeExchangeReq.getDefaultInstance());
		int shopId = req.getShopId();
		int buyCount = req.getCount();
		int collegeLevel = CollegeService.getInstance().getCollegeLevel(player.getCollegeId());
		CollegeShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(CollegeShopCfg.class, shopId);
		if (shopCfg == null || buyCount <= 0 || shopCfg.getCollegeLevel() > collegeLevel) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		String collegeId = entity.getCollegeId();
		if(HawkOSOperator.isEmptyString(collegeId)){
			HawkLog.errPrintln("onCollegeExchange failed, member not in college, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MEMBER_NOT_IN_COLLEGE_VALUE);
			return false;
		}
		
//		//退出又重新加入后7天内不能兑换
//		if (entity.getQuitTime() > 0 && entity.getJoinTime() > 0 && HawkTime.getMillisecond() - entity.getJoinTime() < HawkTime.DAY_MILLI_SECONDS * 7) {
//			HawkLog.errPrintln("onCollegeExchange failed, cannot exchange in 7 days, playerId: {}, quitTime: {}, joinTime: {}", player.getId(), entity.getQuitTime(), entity.getJoinTime());
//			sendError(protocol.getType(), Status.Error.COLLEGE_OPER_FORBID_7_DAYS_VALUE);
//			return false;
//		}

		Map<Integer, CollegeMemberShopEntity> shopDataMap = entity.getShopDataMap();
		CollegeMemberShopEntity shopData = shopDataMap.get(shopId);
		if(Objects.isNull(shopData)){
			HawkLog.errPrintln("onCollegeExchange failed, shopData of shopId not exist, playerId: {}, shopId: {}", player.getId(), shopId);
			sendError(protocol.getType(), Status.Error.COLLEGE_GOODS_CANNOT_EXCHANGE_VALUE);
			return false;
		}
        int boughtCount = shopData.getBuyCount();
        if (boughtCount + buyCount > shopCfg.getTimes()) {
        	HawkLog.errPrintln("onCollegeExchange failed, exchange times limit, playerId: {}, shopId: {}, oldCount: {}, addCount: {}", player.getId(), shopId, boughtCount, buyCount);
        	sendError(protocol.getType(), Status.Error.COLLEGE_GOODS_EXCHANGE_TIMES_LIMIT_VALUE);
            return false;
        }
        
        List<ItemInfo> cList = ItemInfo.valueListOf(shopCfg.getNeedItem());
        cList.forEach(item -> item.setCount(item.getCount() * buyCount));
        ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(cList, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		//消耗，增加兑换次数
		consumeItems.consumeAndPush(player, Action.COLLEGE_EXCHANGE);
		shopData.setBuyCount(boughtCount + buyCount);
		entity.notifyUpdate();
		//发放兑换获得的奖励
		List<ItemInfo> gList = ItemInfo.valueListOf(shopCfg.getGainItem());
		gList.forEach(item -> item.setCount(item.getCount() * buyCount));
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(gList);
		awardItem.rewardTakeAffectAndPush(player, Action.COLLEGE_EXCHANGE,true);
		
		// 给客户端返回信息
		CollegeExchangeResp.Builder builder = CollegeExchangeResp.newBuilder();
		for (CollegeMemberShopEntity data : shopDataMap.values()) {
			builder.addShopInfo(data.toBuilder());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_EXCHANGE_S, builder));
		HawkLog.logPrintln("onCollegeExchange success, playerId: {}, shopId: {}, oldCount: {}, afterCount: {}", player.getId(), shopId, buyCount, shopData.getBuyCount());
		return true;
	}
	
	/**
	 * 学院兑换商店提醒设置
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_EXCHANGE_TIP_C_VALUE)
	private boolean onCollegeExchangeTipsChange(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return false;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		String collegeId = entity.getCollegeId();
		if(HawkOSOperator.isEmptyString(collegeId)){
			HawkLog.errPrintln("onCollegeExchangeTipsChange failed, member not in college, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MEMBER_NOT_IN_COLLEGE_VALUE);
			return false;
		}

		Map<Integer, CollegeMemberShopEntity> shopDataMap = entity.getShopDataMap();
		CollegeShopTipReq req = protocol.parseProtocol(CollegeShopTipReq.getDefaultInstance());
		List<CollegeShopTip> reqList = req.getTipsList();
		if (reqList.isEmpty()) {
			HawkLog.errPrintln("onCollegeExchangeTipsChange failed, member not in college, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		for (CollegeShopTip tipInfo : reqList) {
			CollegeMemberShopEntity data = shopDataMap.get(tipInfo.getId());
			if (data == null) {
				HawkLog.errPrintln("onCollegeExchangeTipsChange error, playerId: {}, cfgId: {}", player.getId(), tipInfo.getId());
				continue;
			}
			data.setTip(tipInfo.getTip());
		}
		entity.notifyUpdate();
		// 给客户端返回信息
		CollegeShopTipResp.Builder builder = CollegeShopTipResp.newBuilder();
		for (CollegeMemberShopEntity data : shopDataMap.values()) {
			CollegeShopTip.Builder tipBuilder = CollegeShopTip.newBuilder();
			tipBuilder.setId(data.getId());
			tipBuilder.setTip(data.getTip());
			builder.addTips(tipBuilder);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_EXCHANGE_TIP_S, builder));
		HawkLog.logPrintln("onCollegeExchangeTipsChange success, playerId: {}", player.getId());
		return true;
	}
	
	/**
	 * 直购商店购买
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.COLLEGE_GIFT_BUY_C_VALUE)
	private boolean onCollegeGiftBuy(HawkProtocol protocol){
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);
			return false;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		CollegeGiftBuyReq req = protocol.parseProtocol(CollegeGiftBuyReq.getDefaultInstance());
		int giftId = req.getGiftId();
		int buyCount = req.getBuyCount();
		int collegeLevel = CollegeService.getInstance().getCollegeLevel(player.getCollegeId());
		CollegePurchaseCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(CollegePurchaseCfg.class, giftId);
		if (giftCfg == null || buyCount <= 0 || giftCfg.getLimitLevel() > collegeLevel) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		if (giftCfg.getShopItemType() == CollegeGiftType.RMB) {
			HawkLog.errPrintln("onCollegeGiftBuy failed, RMB gift comein wrong, playerId: {}, giftId: {}", player.getId(), giftId);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		String collegeId = entity.getCollegeId();
		if(HawkOSOperator.isEmptyString(collegeId)){
			HawkLog.errPrintln("onCollegeGiftBuy failed, member not in college, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.MEMBER_NOT_IN_COLLEGE_VALUE);
			return false;
		}

		//退出又重新加入后7天内不能兑换
//		if (entity.getQuitTime() > 0 && entity.getJoinTime() > 0 && HawkTime.getMillisecond() - entity.getJoinTime() < HawkTime.DAY_MILLI_SECONDS * 7) {
//			HawkLog.errPrintln("onCollegeGiftBuy failed, cannot buy in 7 days, playerId: {}, quitTime: {}, joinTime: {}", player.getId(), entity.getQuitTime(), entity.getJoinTime());
//			sendError(protocol.getType(), Status.Error.COLLEGE_OPER_FORBID_7_DAYS_VALUE);
//			return false;
//		}
				
		CollegeMemberGiftEntity giftData = entity.getGiftData();
		if(!giftData.insell(giftId)){
			HawkLog.errPrintln("onCollegeGiftBuy failed, giftData of giftId not exist, playerId: {}, giftId: {}", player.getId(), giftId);
			sendError(protocol.getType(), Status.Error.COLLEGE_GIFT_CANNOT_SELL_VALUE);
			return false;
		}
		
        int boughtCount = giftData.getBuyCount(giftId);
        if (boughtCount + buyCount > giftCfg.getTimes()) {
        	HawkLog.errPrintln("onCollegeGiftBuy failed, buy times limit, playerId: {}, giftId: {}, oldCount: {}, addCount: {}", player.getId(), giftId, boughtCount, buyCount);
        	sendError(protocol.getType(), Status.Error.COLLEGE_GIFT_BOUGHT_TIMES_LIMIT_VALUE);
            return false;
        }
        
        if (giftCfg.getShopItemType() != CollegeGiftType.FREE) {
        	List<ItemInfo> cList = ItemInfo.valueListOf(giftCfg.getPayItem());
        	cList.forEach(item -> item.setCount(item.getCount() * buyCount));
        	ConsumeItems consumeItems = ConsumeItems.valueOf();
        	consumeItems.addConsumeInfo(cList, false);
        	if (!consumeItems.checkConsume(player, protocol.getType())) {
        		return false;
        	}
        	//消耗，增加兑换次数
        	consumeItems.consumeAndPush(player, Action.COLLEGE_GIFT_BUY);
        }
		
        collegeGiftReward(entity,giftId,buyCount);
		return true;
	}
	
	@MessageHandler
	public void onCollegeGiftBuy(CollegeGiftBuyMsg msg){
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		if(!entity.getGiftData().insell(msg.getCfgId())){
			HawkLog.errPrintln("onCollegeGiftBuy RMB callback, giftData of giftId not exist, playerId: {}, giftId: {}", player.getId(), msg.getCfgId());
			return;
		}
		collegeGiftReward(entity, msg.getCfgId(), 1);
	}
	
	/**
	 * 直购商店购买发货
	 */
	private void collegeGiftReward(CollegeMemberEntity entity, int giftId, int buyCount) {
		entity.getGiftData().addGiftBuy(giftId, buyCount);
		entity.notifyUpdate();
		int aftCount = entity.getGiftData().getBuyCount(giftId);
		CollegePurchaseCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(CollegePurchaseCfg.class, giftId);
		if (giftCfg.getShopItemType()!= CollegeGiftType.RMB && giftCfg.getAwardID()>0) {
			AwardCfg acfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, giftCfg.getAwardID());
			if (acfg != null) {
				List<ItemInfo> gList = acfg.getRandomAward().getAwardItems();
				gList.forEach(item -> item.setCount(item.getCount() * buyCount));
				AwardItems awardItem = AwardItems.valueOf();
				awardItem.addItemInfos(gList);
				awardItem.rewardTakeAffectAndPush(player, Action.COLLEGE_GIFT_BUY,true);
			}
		}
		// 给客户端返回信息
		 List<CollegeGiftItem> list = CollegeService.getInstance().genGfitBuilder(player);
		CollegeGiftBuyResp.Builder builder = CollegeGiftBuyResp.newBuilder();
		builder.addAllGiftInfo(list);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_GIFT_BUY_S, builder));
		//给其他人发奖励
		CollegeService.getInstance().sendGiftDispenseReward(player.getName(),entity.getCollegeId(), giftCfg.getDispenseReward());
		HawkLog.logPrintln("onCollegeGiftBuy success, playerId: {}, giftId: {}, count: {}, after: {}", player.getId(), giftId, buyCount, aftCount);
	}
	
	

	/**
	 * 刷新任务
	 * @param member
	 * @param sync
	 * @return
	 */
	public boolean updateMission(CollegeMemberEntity member, boolean sync){
		try {
			Map<Integer,CollegeMissionEntityItem> mmap = new HashMap<>();
			boolean reload = false;
			for(CollegeMissionEntityItem mitem : member.getMissionList()){
				if(mmap.containsKey(mitem.getCfgId())){
					reload = true;
				}else{
					mmap.put(mitem.getCfgId(), mitem);
				}
			}
			if(reload){
				member.getMissionList().clear();
				member.getMissionList().addAll(mmap.values());
				member.notifyUpdate();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		boolean update = false;
		final int yearDay = HawkTime.getYearDay();
		final int yearWeek = HawkTime.getYearWeek();
		final int month = HawkTime.getCalendar(false).get(Calendar.MONTH);
		List<CollegeMissionEntityItem> delList = new ArrayList<>();
		for (CollegeMissionEntityItem mission : member.getMissionList()) {
			CollegeAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CollegeAchieveCfg.class, mission.getCfgId());
			if (cfg == null) {
				delList.add(mission);
				continue;
			}
			switch (cfg.getMissionType()) {
			case LOGIN_MEMBERS:
			case VIT_COST:
			case ONLINEGIFT_TAKE:
				if (mission.getYearDay() != yearDay) {
					delList.add(mission);
					update = true;
				}
				break;
			default:
				break;
			}
		}
		member.getMissionList().removeAll(delList);
		ConfigIterator<CollegeAchieveCfg> mcfgit = HawkConfigManager.getInstance().getConfigIterator(CollegeAchieveCfg.class);
		for(CollegeAchieveCfg cfg : mcfgit){
			CollegeMissionEntityItem mission = member.getMission(cfg.getAchieveId());
			if(mission == null){
				mission = new CollegeMissionEntityItem();
				mission.setCfgId(cfg.getAchieveId());
				mission.setYearDay(yearDay);
				mission.setYearWeek(yearWeek);
				mission.setMonth(month);
				member.getMissionList().add(mission);
				update = true;
			}
		}
		// 刷新
		for (CollegeMissionEntityItem mission : member.getMissionList()) {
			if (mission.isMissionBonus()) { // 领了就不管了
				continue;
			}
			CollegeAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CollegeAchieveCfg.class, mission.getCfgId());
			switch (cfg.getMissionType()) {
			case LOGIN_MEMBERS:
				int cnt = CollegeService.getInstance().getLoginMemberDay(member.getCollegeId());
				if (cnt != mission.getValue()) {
					mission.setValue(cnt);
					update = true;
				}
				break;
			case ONLINEGIFT_TAKE:
				try {
					int takeCount = 0;
					Map<String, List<Integer>> tmap = member.getOnlineTookMap();
					for(List<Integer> list : tmap.values()){
						takeCount += list.size();
					}
					if (takeCount != mission.getValue()) {
						mission.setValue(takeCount);
						update = true;
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				break;
			default:
				break;
			}
			if (!mission.isMissionComplete() && mission.getValue() >= cfg.getConditionValue()) {
				mission.setState(MissionState.STATE_FINISH);
				update = true;
			}
			if (mission.isMissionComplete() && mission.getValue() < cfg.getConditionValue()) {
				mission.setState(MissionState.STATE_NOT_FINISH);
				update = true;
			}
		}
		if (sync || update) {
			member.notifyUpdate();
			CollegeMissionUpdateResp.Builder builder = CollegeMissionUpdateResp.newBuilder()
					.addAllMissions(CollegeService.getInstance().genMissionBuilder(player));
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_MISSION_RESP_S, builder));
		}
		
		
		return update;
	}
	
	private void missionValueAdd(CollegeMissionType type, int value) {
		HawkLog.logPrintln("missionValueAdd,playerId: {},missonType:{}", player.getId(),type.intValue());
		boolean missionSync = false;
		CollegeMemberEntity member = getPlayerData().getCollegeMemberEntity();
		for (CollegeMissionEntityItem mission : member.getMissionList()) {
			if (mission.isMissionBonus()) { // 领了就不管了
				continue;
			}
			CollegeAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CollegeAchieveCfg.class, mission.getCfgId());
			if (type == cfg.getMissionType()) {
				mission.addValue(value);
				missionSync = true;
				HawkLog.logPrintln("missionValueAdd,playerId: {},missonType:{},missonVlaue:{}", player.getId(),type.intValue(),mission.getValue());
			}
		}
		if (missionSync) {
			updateMission(member, missionSync);
		}
	}
	
	@MessageHandler
	private boolean onPlayerVitCostMsg(PlayerVitCostMsg event) {
		// 刷新任务
		missionValueAdd(CollegeMissionType.VIT_COST, event.getCost());
		if(this.player.hasCollege()){
			if(this.player.isCsPlayer()){
				RedisProxy.getInstance().addCollegeMemberVitCost(this.player.getMainServerId(),
						this.player.getCollegeId(), this.player.getId(), event.getCost());
			}else{
				this.player.msgCall(MsgId.COLLEGE_CONTRIBUTE_ADD, CollegeService.getInstance(), 
						new CollegeAddContributeMsg(this.player.getCollegeId(), 0, 0, event.getCost()));
			}
		}
		return true;
	}
	
	@ProtocolHandler(code = HP.code2.COLLEGE_MISSION_REWARD_TAKE_REQ_C_VALUE)
	private boolean onTakeMissionReward(HawkProtocol protocol) {
		CollegeMissionTakeRewardReq req = protocol.parseProtocol(CollegeMissionTakeRewardReq.getDefaultInstance());
		int cfgId = req.getMissionId();
		CollegeMissionEntityItem theMission = null;
		CollegeMemberEntity member = getPlayerData().getCollegeMemberEntity();
		for (CollegeMissionEntityItem mission : member.getMissionList()) {
			if (mission.isMissionComplete() && mission.getCfgId() == cfgId) {
				theMission = mission;
			}
		}
		if (theMission != null) {
			CollegeAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CollegeAchieveCfg.class, theMission.getCfgId());
			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItemInfos(ItemInfo.valueListOf(cfg.getRewards()));
			awardItem.addItemInfos(ItemInfo.valueListOf(cfg.getCollegeScore()));
			awardItem.rewardTakeAffectAndPush(player, Action.COLLEGE_MISSION_REWARD_TAKE, true);

			theMission.setState(MissionState.STATE_BONUS);
			updateMission(member, true);
			//自己添加积分
			member.getScoreData().addWeekScore(cfg.getCollegeScoreCount());
			member.notifyUpdate();
			//学院添加积分和经验
			this.player.msgCall(MsgId.COLLEGE_CONTRIBUTE_ADD, CollegeService.getInstance(), 
					new CollegeAddContributeMsg(this.player.getCollegeId(), cfg.getCollegeExp(), cfg.getCollegeScoreCount(),0));
			
			HawkLog.logPrintln("onTakeMissionReward sucess,playerId: {},missonId:{}", player.getId(),cfgId);
		}
		return true;
	}
	
	/**
	 * 退出学院
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onCollegeQuitMsg(CollegeQuitMsg msg) {
		String collegeId = msg.getCollegeId();
		CollegeMemberEntity member = getPlayerData().getCollegeMemberEntity();
		member.quit();
		if (!msg.isDismiss()) {
			CollegeService.getInstance().broadcastSyncCollegeInfo(collegeId);
		}

		CollegeService.getInstance().syncCollegeInfo(player);
		CollegeAlterResp.Builder builder = CollegeAlterResp.newBuilder();
		builder.setCollegeId(collegeId);
		builder.setCoachId(msg.getCoachId());
		builder.setCoachName(msg.getCoachName());
		if (msg.isKick()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_BE_KICK_SYNC, builder));
		} else if (msg.isDismiss()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_DISMISS_SYNC, builder));
		}
		// 更新世界显示
		WorldPointService.getInstance().updateCollegeNameShow(this.player.getId(), "");
		this.player.getPush().updatePlayerDressEditData();
		if(!msg.isKick() && !msg.isDismiss()){
			//主动退出的 扣除积分道具
			int scoreItem = CollegeConstCfg.getInstance().getCollegeScoreItem();
			int has = player.getData().getItemNumByItemId(scoreItem);
			if(has > 0){
				int lossCount = (int) (has * CollegeConstCfg.getInstance().getCollegeDeductRatio() * GsConst.EFF_PER);
				if(lossCount > 0){
					ConsumeItems consumeItems = ConsumeItems.valueOf();
					consumeItems.addItemConsume(scoreItem, lossCount);
					if (consumeItems.checkConsume(player)) {
						//消耗，增加兑换次数
						consumeItems.consumeAndPush(player, Action.COLLEGE_QUIT);
					}
				}
				
			}
			
			
		}
		// 行为日志
		BehaviorLogger.log4Service(player, Source.COLLEGE, Action.QUIT_COLLEGE, 
				Params.valueOf("collegeId", collegeId),
				Params.valueOf("coachId", msg.getCoachId()),
				Params.valueOf("coachName", msg.getCoachName()),
				Params.valueOf("isKick", msg.isKick()),
				Params.valueOf("isDismiss", msg.isDismiss()));
		return true;
	}

	/**
	 * 加入学院
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onCollegeJoinMsg(CollegeJoinMsg msg) {
		CollegeMemberEntity member = getPlayerData().getCollegeMemberEntity();
		String collegeId = msg.getCollegeId();
		member.join(collegeId, msg.isCreate());
		// 全体推送学院信息
		CollegeService.getInstance().broadcastSyncCollegeInfo(collegeId);
		if (!msg.isCreate() && !msg.isInvite()) {
			CollegeAlterResp.Builder builder = CollegeAlterResp.newBuilder();
			builder.setCollegeId(collegeId);
			builder.setCoachId(msg.getCoachId());
			builder.setCoachName(msg.getCoachName());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.COLLEGE_BE_AGREE_SYNC, builder));
		}
		// 更新世界显示
		String collegeName = CollegeService.getInstance().getCollegeName(collegeId);
		WorldPointService.getInstance().updateCollegeNameShow(this.player.getId(), collegeName);
		this.player.getPush().updatePlayerDressEditData();
		// 行为日志
		BehaviorLogger.log4Service(player, Source.COLLEGE, Action.JOIN_COLLEGE, 
				Params.valueOf("collegeId", collegeId),
				Params.valueOf("coachId", msg.getCoachId()),
				Params.valueOf("coachName", msg.getCoachName()),
				Params.valueOf("isInvite", msg.isInvite()),
				Params.valueOf("isCreate", msg.isCreate()));
		LocalRedis.getInstance().removeApplyedColleges(player.getId());
		return true;
	}


	@ProtocolHandler(code = HP.code2.COLLEGE_VITALITY_INFO_REQ_C_VALUE)
	private boolean onVitalityInfo(HawkProtocol protocol) {
		CollegeVitalitySendResp.Builder resp = CollegeVitalitySendResp.newBuilder();
		resp.setVitality(CollegeService.getInstance().genVitBuilder(player));
		sendProtocol(HawkProtocol.valueOf(HP.code2.COLLEGE_VITALITY_INFO_RESP_S_VALUE, resp));
		return true;
	}
	

	@ProtocolHandler(code = HP.code2.COLLEGE_VITALITY_SEND_REQ_C_VALUE)
	private boolean onVitalitySend(HawkProtocol protocol) {
		CollegeVitalitySendReq req = protocol.parseProtocol(CollegeVitalitySendReq.getDefaultInstance());
		this.player.msgCall(MsgId.COLLEGE_VIT_SEND, CollegeService.getInstance(), new CollegeVitSendMsg(player, req));
		return true;
	}
	
	
	@ProtocolHandler(code = HP.code2.COLLEGE_ONLINE_REWARD_INFO_REQ_C_VALUE)
	private boolean onAchieveOnlineReward(HawkProtocol protocol) {
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		CollegeTakeOnlineRewardInfoReq req = protocol.parseProtocol(CollegeTakeOnlineRewardInfoReq.getDefaultInstance());
		int result = CollegeService.getInstance().achieveOnlienRewards(player, req.getId());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
			// 刷新任务
			CollegeMemberEntity data = player.getData().getCollegeMemberEntity();
			this.updateMission(data, true);
		} else {
			player.sendError(protocol.getType(), result, 0);
		}
		return true;
	}
	
	
	

	@Override
	public boolean onTick() {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime < lastRefreshTime + 10000) {
			return true;
		}
		try {
			CollegeMemberEntity self = getPlayerData().getCollegeMemberEntity();
			if (HawkTime.isSameDay(currentTime, self.getCrossResetTime())) {
				if (player.hasCollege()) {
					long befTime = self.getOnlineTimeToday();
					self.increaceOnlineTime();
					long aftTime = self.getOnlineTimeToday();
					if (!player.isCsPlayer()) {
						// 触发奖励时同步所有在线学员
						if (CollegeService.getInstance().triggerOnlineReward(befTime, aftTime)) {
							CollegeService.getInstance().broadcastSyncCollegeInfo(player.getCollegeId());
						}
					}
				}
			} else {
				self.resetOnlineTime();
				CollegeService.getInstance().syncOnlineRewardInfo(player);
			}
			//更新任务
			updateMission(self,false);
			//兑换商店刷新检测
			if(CollegeService.getInstance().updateExchangeShopData(self, currentTime)){
				CollegeService.getInstance().sysnCollegeExchangeInfo(player);
			}
			//直购商店刷新检测
			if (CollegeService.getInstance().updateGiftShopData(self, currentTime)) {
				CollegeService.getInstance().sysnCollegeGiftInfo(player);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		lastRefreshTime = currentTime;
		return super.onTick();
	}

}
