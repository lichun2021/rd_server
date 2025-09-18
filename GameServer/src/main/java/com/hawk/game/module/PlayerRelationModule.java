package com.hawk.game.module;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SendFriendsGiftsEvent;
import com.hawk.common.IDIPBanInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.backflow.BackFlowService;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ControlProperty;
import com.hawk.game.config.FriendInviteTaskCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.GuardianConstConfig;
import com.hawk.game.config.GuardianGiftCfg;
import com.hawk.game.config.GuardianGiveKvCfg;
import com.hawk.game.config.GuardianHelpCfg;
import com.hawk.game.config.GuardianItemDressCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.data.FriendInviteInfo;
import com.hawk.game.data.FriendInviteInfo.TaskAttrInfo;
import com.hawk.game.data.UnregFriendTaskInfo;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.PlayerRelationApplyEntity;
import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.gmproxy.GmProxyHelper;
import com.hawk.game.invoker.RelationAddLoveRpcInvoker;
import com.hawk.game.invoker.RelationBlacklistOperRpcInvoker;
import com.hawk.game.invoker.RelationDeleteRpcInvoker;
import com.hawk.game.invoker.RelationHanleApplyRpcInvoker;
import com.hawk.game.invoker.guard.GuardDeleteInvoker;
import com.hawk.game.invoker.guard.GuardDressUpdateInvoker;
import com.hawk.game.invoker.guard.GuardInviteHandleInvoker;
import com.hawk.game.invoker.guard.GuardInviteRpcInvoker;
import com.hawk.game.invoker.guard.GuardGiftSendInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.msg.CheckGuardDressMsg;
import com.hawk.game.msg.GuardHelpOpenCoverMsg;
import com.hawk.game.msg.GuardOutFireMsg;
import com.hawk.game.msg.UpdateGuardDressMsg;
import com.hawk.game.msg.cross.LocalLoginMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Friend.BlackListMsg;
import com.hawk.game.protocol.Friend.BlackListOperationReq;
import com.hawk.game.protocol.Friend.BlacklistInfoResp;
import com.hawk.game.protocol.Friend.ClientInviteFriendResult;
import com.hawk.game.protocol.Friend.DeleteFriendReq;
import com.hawk.game.protocol.Friend.FriendAddReq;
import com.hawk.game.protocol.Friend.FriendApplyInfoResp;
import com.hawk.game.protocol.Friend.FriendInfoResp;
import com.hawk.game.protocol.Friend.FriendMsg;
import com.hawk.game.protocol.Friend.FriendType;
import com.hawk.game.protocol.Friend.GuardDressItemBlagReq;
import com.hawk.game.protocol.Friend.GuardDressItemExchangeReq;
import com.hawk.game.protocol.Friend.GuardDressItemSendReq;
import com.hawk.game.protocol.Friend.GuardDressUpdateReq;
import com.hawk.game.protocol.Friend.GuardGiftInfoResp;
import com.hawk.game.protocol.Friend.GuardGiftSendReq;
import com.hawk.game.protocol.Friend.GuardInviteHandleReq;
import com.hawk.game.protocol.Friend.GuardInvitePlayerReq;
import com.hawk.game.protocol.Friend.GuardOpenCoverReq;
import com.hawk.game.protocol.Friend.GuardOptType;
import com.hawk.game.protocol.Friend.GuardSendAndBlagHistoryInfo;
import com.hawk.game.protocol.Friend.GuardSendAndBlagHistoryReq;
import com.hawk.game.protocol.Friend.GuardSendAndBlagHistoryResp;
import com.hawk.game.protocol.Friend.GuardSettingEditReq;
import com.hawk.game.protocol.Friend.HandleFriendApplyReq;
import com.hawk.game.protocol.Friend.InviteTaskAwardReq;
import com.hawk.game.protocol.Friend.InviteTaskRewardedPushPB;
import com.hawk.game.protocol.Friend.OperationType;
import com.hawk.game.protocol.Friend.PlatformFriendInfo;
import com.hawk.game.protocol.Friend.PlatformFriendResp;
import com.hawk.game.protocol.Friend.PresentGiftReq;
import com.hawk.game.protocol.Friend.PresentGiftResp;
import com.hawk.game.protocol.Friend.SearchStrangerReq;
import com.hawk.game.protocol.Friend.UnRegFriendInfoPB;
import com.hawk.game.protocol.Friend.UnRegFriendsPB;
import com.hawk.game.protocol.Friend.UpdateFriendInfoReq;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Mail.PBPlayerGuardDressMailContent;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Player.PlayerFlagPosition;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.RelationType;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MapUtil;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ClickEventType;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.config.PlatformConstCfg;
import com.hawk.sdk.SDKManager;

/**
 * 好友模块
 * @author JM-Long
 *
 */
public class PlayerRelationModule extends PlayerModule {
	public PlayerRelationModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerAssemble() {
		if (this.player.isCsPlayer()) {
			return true;
		}
		
		RelationService.getInstance().loadData(player.getId(), true);
		//加到离线玩家中去.
		RelationService.getInstance().removeOffinePlayer(player);
		
		return true;
	}
	
	@Override
	protected boolean onPlayerLogin() {
		if (this.player.isCsPlayer()) {
			return true;
		}
		// 和player logout处做双保险
		RelationService.getInstance().removeCachePlatfromFriend(player.getId());
		// 同步接收好友礼包次数
		player.getPush().syncGiftReceiveTimes();
		// 异步执行
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
			task.setTypeName("RelationModuleLogin");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
		} else {
			loginProcess();
		}
		
		// 拆服守护道具补偿
		checkSeparateGuard();
		return true;
	}
	
	/**
	 * 检测拆服守护补偿
	 */
	private void checkSeparateGuard() {
		double separateGuardValue = RedisProxy.getInstance().getSeparateGuardValue(player.getId());
		if (separateGuardValue == 0) {
			return;
		}
		RedisProxy.getInstance().delSeparateGuard(player.getId());
		
		// 补偿道具
		List<ItemInfo> items = new ArrayList<>();
		ItemInfo item = GameConstCfg.getInstance().getSeparateGuardItem();
		item.setCount((long)Math.ceil(separateGuardValue / 10 / 2));
		items.add(item);
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.SEPARATE_GUARD_RET_MAIL)
				.addRewards(items)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
		
		HawkLog.logPrintln("separate guard, playerId:{}, item:{}", player.getId(), item.toString());
	}
	
	/**
	 * 登录处理
	 */
	private void loginProcess() {
		synFriendInfo();
		synBlacklistInfo();
		synRelationApplyInfo();		
		// 同步好友建筑状态
		player.getPush().syncFriendBuildStatus();
		// 同步推荐好友
		player.getPush().syncRecommFriends();
		//同步hud
		RelationService.getInstance().synGuardHud(player);
		//同步仇恨名单
		RelationService.getInstance().syncHateRankList(player);
		
		checkUpdateDress(true);
	}
	
	/**
	 * 登出游戏
	 */
	protected boolean onPlayerLogout() {
		RelationService.getInstance().removeCachePlatfromFriend(player.getId());
		RelationService.getInstance().addOffinePlayer(player);
		return true;
	}
	
	/**
	 * 好友赠送礼物
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FRIEND_PRESENT_GIFT_REQ_VALUE)
	private boolean onPresentGift(HawkProtocol protocol) {
		PresentGiftReq req = protocol.parseProtocol(PresentGiftReq.getDefaultInstance());
		// 赠送好友类型
		FriendType type = req.getType();

		// 赠送指定好友
		if (type == FriendType.COMMON && !req.hasPlayerId()) {
			this.sendError(protocol.getType(), Status.Error.RELATION_ID_NOT_NULL_VALUE);
			return false;
		}

		// 本次赠送列表 <playerId，serverId>
		Map<String, String> playerIdMap = new HashMap<String, String>();
		// 已经赠送过的列表
		List<String> presentedList = LocalRedis.getInstance().getFriendPresentGift(player.getId());
		// 是否给游戏内好友赠送礼物
		boolean inGameFriend = true;

		// 如果是赠送指定好友
		if (type.equals(FriendType.COMMON)) {
			String playerId = req.getPlayerId();
			
			if (HawkOSOperator.isEmptyString(playerId) ) {
				this.sendError(protocol.getType(), Status.Error.RELATION_ID_NOT_NULL_VALUE);
				return false;
			}
			
			// 不可以赠送给自己
			if (player.getId().equals(playerId)) {
				this.sendError(protocol.getType(), Status.Error.RELATION_CAN_NOT_PRESENT_SELF_VALUE);
				return false;
			}
			
			if (presentedList.contains(playerId)) {
				this.sendError(protocol.getType(), Status.Error.RELATION_ALEARDY_PRESENT_VALUE);
				return false;
			}
			
			String  serverId = GsConfig.getInstance().getServerId();
			// 不是游戏内好友关系
			if (!RelationService.getInstance().isFriend(player.getId(), playerId)) {
				// 在判断是不是平台好友
				HawkTuple2<Boolean, String> tuple = RelationService.getInstance().isPlatformFriend(player, playerId);
				if (!tuple.first) {
					sendError(protocol.getType(), Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE);
					return false;
				} else {
					serverId = tuple.second;
					inGameFriend = false;
				}
			}
			
			playerIdMap.put(playerId, serverId);
			
		} else if (type.equals(FriendType.GAME)) {
			List<String> playerRelationIds = RelationService.getInstance().getPlayerRelationIdList(player.getId(), GsConst.RelationType.FRIEND);
			String  serverId = GsConfig.getInstance().getServerId();
			for (String playerId : playerRelationIds) {
				// 已经赠送过
				if (presentedList.contains(playerId)) {
					continue;
				}
				
				playerIdMap.put(playerId, serverId);
			}
		} else {
			
			inGameFriend = false;
			// 给所有平台好友赠送礼物
			List<PlatformFriendInfo.Builder> friendList = RelationService.getInstance().getPlatformFriendList(player);
			for (PlatformFriendInfo.Builder friendInfo : friendList) {
				// 已经赠送过
				if (presentedList.contains(friendInfo.getPlayerId())) {
					continue;
				}
				
				String playerServerId = GlobalData.getInstance().getMainServerId(friendInfo.getServerId());
				playerIdMap.put(friendInfo.getPlayerId(), playerServerId);
			}
		}
		
		final boolean isPlatformFriend = !inGameFriend;
		// 异步发送邮件
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				sendFriendGift(isPlatformFriend, playerIdMap);
				return null;
			}
		});
		//发送回流推送消息
		BackFlowService.getInstance().sendFriendPush(this.player,isPlatformFriend, playerIdMap);
		//发送好友赠送事件
		SendFriendsGiftsEvent event = new SendFriendsGiftsEvent(
				this.player.getId(),new HashSet<String>(playerIdMap.keySet()),inGameFriend);
		ActivityManager.getInstance().postEvent(event);
		// 发送邮件, 添加亲密度, 这里还有区分type为common的情况
		if (inGameFriend) {
			for (String playerId : playerIdMap.keySet()) {
				int addLove = ConstProperty.getInstance().getFriendIntimacy();
				player.rpcCall(GameConst.MsgId.FRIEND_ADD_LOVE, RelationService.getInstance(), new RelationAddLoveRpcInvoker(player.getId(), playerId, addLove));			
			}
		}

		// 添加赠送礼物列表
		LocalRedis.getInstance().addFriendPresentGift(player.getId(), playerIdMap.keySet());

		// 协议返回
		PresentGiftResp.Builder resp = PresentGiftResp.newBuilder();
		resp.setType(type);
		if (req.hasPlayerId()) {
			resp.setPlayerId(req.getPlayerId());
		}
		
		// 如果在是平台一键赠送，给客户端返回包含在好友列表里的playerId
		if (req.getType() == FriendType.PLATFORM) {
			resp.addAllFriendList(playerIdMap.keySet());
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FRIEND_PRESENT_GIFT_RESP, resp));
		return true;
	}
	
	/**
	 * 好友赠送礼物
	 * 
	 * @param playerIdMap
	 */
	private void sendFriendGift(boolean isPlatformFriend, Map<String, String> playerIdMap) {
		String  serverId = GsConfig.getInstance().getServerId();
		
		StringBuilder sb = new StringBuilder();
		if (player.hasGuild()) {
			sb.append('[');
			sb.append(player.getGuildTag());
			sb.append(']');
		}
		sb.append(player.getName());
		String content = sb.toString();
		
		if (!isPlatformFriend) {
			for (Entry<String, String> entry : playerIdMap.entrySet()) {
				String friendPlayerId = entry.getKey();
				
				AwardItems award = AwardItems.valueOf();
				award.addAward(ConstProperty.getInstance().getLoveGiftId(RelationService.getInstance().getLove(player.getId(), friendPlayerId)));
				
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(friendPlayerId)
						.setMailId(MailId.PRESTENT_GIFT)
						.setRewards(award.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents(content)
						.addTitles(player.getName())
						.addSubTitles(player.getName())
						.build());
			}
		} else {
			String awardStr = ConstProperty.getInstance().getGiveFriendGift();
			ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(String.valueOf(serverId));
			String serverName = "";
			if (serverInfo != null) {
				serverName = serverInfo.getName();
			} 
			
			for (Entry<String, String> entry : playerIdMap.entrySet()) {
				String friendPlayerId = entry.getKey();
				boolean sameServer = entry.getValue().equals(serverId);
				
				if (sameServer) {
					// 根据亲密度获取礼物配置
					AwardItems award = AwardItems.valueOf(awardStr);
					SystemMailService.getInstance().sendMail(MailParames.newBuilder()
							.setPlayerId(friendPlayerId)
							.setMailId(MailId.PRESENT_PLATFORM_FRIEND_GIFT)
							.setRewards(award.getAwardItems())
							.setAwardStatus(MailRewardStatus.NOT_GET)
							.addContents(player.getChannel(), content)
							.addTitles(player.getChannel(), player.getName())
							.addSubTitles(player.getChannel(), player.getName())
							.build());
				} else {
					// 跨服赠送
					String targetServerId = entry.getValue();
					try {
						String encodeContent = URLEncoder.encode(content, "utf-8");
						String encodeServerName = URLEncoder.encode(serverName, "utf-8");
						
						GmProxyHelper.proxyCall(targetServerId, "sendGift", "playerId=" + friendPlayerId + "&source=" + encodeContent + "&serverName=" + encodeServerName, 2000);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			}
		}
	}

	@ProtocolHandler(code = { HP.code.FRIEND_ADD_REQ_VALUE })
	private void friendAddReq(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_ADD_FRIEND);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		FriendAddReq cparam = protocol.parseProtocol(FriendAddReq.getDefaultInstance());
		GlobalData globalData = GlobalData.getInstance();
		List<String> targetIds = cparam.getPlayerIdsList();
		String content = cparam.getContent() == null ? "" : cparam.getContent();
		//字符长度判断。
		if (content.length() > GsConst.RELATION_MAX_REQEUST_CONTENT) {
			this.sendError(protocol.getType(),  Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		if (targetIds == null || targetIds.isEmpty()) {
			this.sendError(protocol.getType(), Status.Error.RELATION_ID_NOT_NULL_VALUE);
			return;
		}

		// 重复的请求
		Set<String> set = new HashSet<>(targetIds);
		if (set.size() != targetIds.size()) {
			this.sendError(protocol.getType(), Status.Error.RELATION_REPEATED_ID_VALUE);
			return;
		}
		
		if (!HawkOSOperator.isEmptyString(content) && !GameUtil.checkBanMsg(player)) {
			return;
		}

		String tPlayerId = null;
		for (String id : targetIds) {
			//判断已经判断是不是同一个服,所以跨服玩家也是通不过的.
			if (!RelationService.getInstance().isInGamePlatformFriend(player, id)) {
				// 不是同服玩家，无法添加好友
				sendError(protocol.getType(), Status.Error.ADD_NOT_INGAME_FRIEND_VALUE);
				return;
			}
			
			AccountInfo targetInfo = globalData.getAccountInfoByPlayerId(id);
			if (targetInfo == null || !globalData.isLocalServer(targetInfo.getServerId())) {
				sendError(protocol.getType(), Status.Error.ADD_NOT_INGAME_FRIEND_VALUE);
				return;
			}
			
			if (id.equals(player.getId())) {
				this.sendError(protocol.getType(), Status.Error.RELATION_CAN_NOT_ADD_SELF_VALUE);
				
				return;
			}
			//如果对面拉黑了自己则删除
			if (RelationService.getInstance().isBlacklist(id, player.getId())) {
				set.remove(id);
			}
			
			// 已删除的玩家
			if (GlobalData.getInstance().isResetAccount(id)) {
				HawkLog.errPrintln("friend add filter, target player is removed player, playerId: {}, targetPlayerId: {}", player.getId(), id);
				set.remove(id);
			}
			
			tPlayerId = id;
		}
		//如果刚好只有一个人的话,也必须显示成成功
		if (set.isEmpty()) {
			player.responseSuccess(protocol.getType());
			return;
		}
		
		StringJoiner sj = new StringJoiner(",");
		for (String remainId : set) {
			sj.add(remainId);
		}
		
		JSONObject json = null;
		Player tPlayer = GlobalData.getInstance().makesurePlayer(tPlayerId);
		if (tPlayer != null) {
			json = new JSONObject();
			String pfIconPrimitive = tPlayer.getData().getPrimitivePfIcon();
			if (GlobalData.getInstance().isBanPortraitAccount(tPlayer.getOpenId())) {
				pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
			}
			json.put("recv_account", tPlayer.getOpenId());
			json.put("recv_role_name", tPlayer.getName());
			json.put("recv_role_pic_url", pfIconPrimitive);
			json.put("recv_area_id", "wx".equalsIgnoreCase(tPlayer.getChannel()) ? 1 : 2);
			json.put("recv_plat_id", GameUtil.isAndroidAccount(tPlayer) ? 1 : 0);
			json.put("recv_world_id", tPlayer.getServerId());
			json.put("recv_role_id", tPlayer.getId());
			json.put("recv_role_level", tPlayer.getLevel());
			json.put("recv_role_battlepoint", tPlayer.getPower());
		}
		
		GameTssService.getInstance().wordUicChatFilter(player, content, 
				MsgCategory.ADD_FRIEND_FUYAN.getNumber(), GameMsgCategory.ADD_FRIEND, 
				sj.toString(), json, protocol.getType());
	}

	@ProtocolHandler(code = { HP.code.DELETE_FRIEND_REQ_VALUE })
	private void friendDeleteReq(HawkProtocol protocol) {
		DeleteFriendReq cparam = protocol.parseProtocol(DeleteFriendReq.getDefaultInstance());
		String targetId = cparam.getPlayerId();

		if (!GlobalData.getInstance().isExistPlayerId(targetId)) {
			this.sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);

			return;
		}

		player.rpcCall(GameConst.MsgId.FRIEND_DELETE_REQ, RelationService.getInstance(), new RelationDeleteRpcInvoker(player, targetId));
	}

	@ProtocolHandler(code = { HP.code.BLACKLIST_OPERATION_REQ_VALUE })
	private void blackListAddReq(HawkProtocol protocol) {
		BlackListOperationReq cparam = protocol.parseProtocol(BlackListOperationReq.getDefaultInstance());
		String targetId = cparam.getPlayerId();
		
		if (player.getId().equals(targetId)) {
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		if (!RelationService.getInstance().isInGamePlatformFriend(player, targetId)) {
			// 不是同服好友，无法使用拉黑功能 这里的判断可以当跨服判断用.
			this.sendError(protocol.getType(), Status.Error.BLACK_NOT_INGAME_FRIEND_VALUE);
			return;
		}
		
		if (GlobalData.getInstance().isResetAccount(targetId)) {
			sendError(protocol.getType(), Status.Error.GUILD_INVITE_NOT_INGAME_FRIEND_VALUE);
			return;
		}
		
	    AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(targetId);
	    if (accountInfo == null) {
	    	this.sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);	    	
	    	return;
	    }
	    	    
	    if (!GlobalData.getInstance().isLocalServer(accountInfo.getServerId())) {
	    	this.sendError(protocol.getType(), Status.Error.BLACK_NOT_INGAME_FRIEND_VALUE);
	    	return;
	    }	    

		player.rpcCall(GameConst.MsgId.BLACKLIST_OPERATION_REQ, RelationService.getInstance(), new RelationBlacklistOperRpcInvoker(player, targetId, cparam.getOper().getNumber()));
	}

	@ProtocolHandler(code = { HP.code.FRIEND_INFO_REQ_VALUE })
	private void friendInfoReq(HawkProtocol protocol) {
		synFriendInfo();
	}

	public void synFriendInfo() {
		RelationService relationService = RelationService.getInstance();
		List<PlayerRelationEntity> entityList = relationService.getPlayerRelationList(player.getId(), GsConst.RelationType.FRIEND);

		int dayAdd = RedisProxy.getInstance().dayFriendAddCount(player.getId());
		FriendInfoResp.Builder sbuilder = FriendInfoResp.newBuilder();
		sbuilder.setDayAddCount(dayAdd);
		List<String> gaveList = LocalRedis.getInstance().getFriendPresentGift(player.getId());
		for (PlayerRelationEntity entity : entityList) {
			FriendMsg.Builder friendMsgBuilder = relationService.buildFriendMsg(entity, gaveList);
			if (friendMsgBuilder != null) {
				sbuilder.addFriends(friendMsgBuilder);
			}			
		}
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.FRIEND_INFO_RESP_VALUE, sbuilder.build().toByteArray());
		player.sendProtocol(respProtocol);
	}
	
	/**
	 * 获取平台好友信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = { HP.code.PLATFORM_FRIEND_INFO_REQ_VALUE })
	protected void fetchPlatformFriendInfo(HawkProtocol protocol) {
		String playerId = player.getId();
		if ("7q1-hoyc8-1".equals(playerId) || "7q1-hpgls-l".equals(playerId)) {
			PlatformFriendResp.Builder builder = PlatformFriendResp.newBuilder();
			HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.PLATFORM_FRIEND_INFO_RESP_VALUE, builder);
			player.sendProtocol(respProtocol);
			return;
		}
		
		// 平台好友关系链授权已解除
		if (GlobalData.getInstance().isPfRelationCancel(player.getOpenId())) {
			sendError(protocol.getType(), Status.SysError.PLATFORM_RELATION_CANCEL_VALUE);
			return;
		}
				
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					syncPlatformFriends();
					return null;
				}
			};
			
			task.setPriority(1);
			task.setTypeName("fetchPlatformFriendInfo");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
			player.responseSuccess(protocol.getType());
			return;
		}
		
		syncPlatformFriends();
	}
	
	private void syncPlatformFriends() {
		List<PlatformFriendInfo.Builder> friendList = RelationService.getInstance().getPlatformFriendList(player);
		
		PlatformFriendResp.Builder builder = PlatformFriendResp.newBuilder();
		for (PlatformFriendInfo.Builder fb : friendList) {
			builder.addPlatformFriend(fb);
		}
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.PLATFORM_FRIEND_INFO_RESP_VALUE, builder);
		player.sendProtocol(respProtocol);
	}

	@ProtocolHandler(code = { HP.code.FRIEND_APPLY_INFO_REQ_VALUE })
	private void relationAppplyReq(HawkProtocol protocol) {
		synRelationApplyInfo();
	}

	public void synRelationApplyInfo() {
		RelationService relationService = RelationService.getInstance();
		Map<String, PlayerRelationApplyEntity> map = relationService.getPlayerRelationApplyMap(player.getId());

		FriendApplyInfoResp.Builder sbuilder = FriendApplyInfoResp.newBuilder();

		for (PlayerRelationApplyEntity entity : map.values()) {
			sbuilder.addApplyMsgs(relationService.buildFriendApplyMsg(entity));
		}

		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.FRIEND_APPLY_INFO_RESP_VALUE, sbuilder.build().toByteArray());
		player.sendProtocol(respProtocol);
	}

	@ProtocolHandler(code = { HP.code.BLACKLIST_INFO_REQ_VALUE })
	private void blacklistInfoReq(HawkProtocol protocol) {
		synBlacklistInfo();
	}

	public void synBlacklistInfo() {
		RelationService relationService = RelationService.getInstance();
		List<PlayerRelationEntity> entityList = relationService.getPlayerRelationList(player.getId(), GsConst.RelationType.BLACKLIST);

		BlacklistInfoResp.Builder sbuilder = BlacklistInfoResp.newBuilder();

		for (PlayerRelationEntity entity : entityList) {
			BlackListMsg.Builder builder = relationService.buildBlacklistMsg(entity);
			if (builder != null) {
				sbuilder.addBlackLists(builder);
			}			
		}

		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.BLACKLIST_INFO_RESP_VALUE, sbuilder.build().toByteArray());
		player.sendProtocol(respProtocol);
	}

	@ProtocolHandler(code = { HP.code.RECOMMEND_FRIENDS_REQ_VALUE })
	private void onRecommendFriendReq(HawkProtocol protocol) {
		player.getPush().syncRecommFriends();
	}

	@ProtocolHandler(code = { HP.code.HANDLE_FRIEND_APPLY_REQ_VALUE })
	private void handleApplyReq(HawkProtocol protocol) {
		HandleFriendApplyReq cparam = protocol.parseProtocol(HandleFriendApplyReq.getDefaultInstance());
		String playerId = cparam.getPlayerId();
		int left = 0;
		if (cparam.getType() == OperationType.AGREEE) {
			int dayAdd = RedisProxy.getInstance().dayFriendAddCount(player.getId());
			left = ControlProperty.getInstance().getDayFriendsNum() - dayAdd;
			if (left < 1) {// 到达上限
				return;
			}
		} 
		List<String> playerIds = new ArrayList<>();
		if (StringUtils.isNotEmpty(playerId)) {
			playerIds.add(playerId);
		} else {
			if (cparam.getType() == OperationType.REJECT) {
				RelationService.getInstance().getPlayerRelationApplyMap(player.getId()).keySet().stream()
				.forEach(playerIds::add);
			} else {
				RelationService.getInstance().getPlayerRelationApplyMap(player.getId()).keySet().stream()
				.limit(left)
				.forEach(playerIds::add);
			}			
		}

		RelationService relationService = RelationService.getInstance();
		player.rpcCall(GameConst.MsgId.FRIEND_APPLY_REQ, relationService, new RelationHanleApplyRpcInvoker(player, playerIds, cparam.getType()));
	}

	/**
	 * 搜索陌生人
	 * @param protocol
	 */
	@ProtocolHandler(code = { HP.code.SEARCH_STRANGER_REQ_VALUE })
	private void searchStrangerReq(HawkProtocol protocol) {
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
				
		SearchStrangerReq cparam = protocol.parseProtocol(SearchStrangerReq.getDefaultInstance());
		JSONObject callback = new JSONObject();
		callback.put("sex", cparam.getSex());
		callback.put("location", cparam.getSameCity());
		GameTssService.getInstance().wordUicChatFilter(player, cparam.getName(), 
				MsgCategory.SEARCH_ADD_FRIEND.getNumber(), GameMsgCategory.SEARCH_STRANGER, 
				callback.toJSONString(), null, protocol.getType());
	}
	
	@ProtocolHandler(code = {HP.code.UPDATE_FRIEND_INFO_REQ_VALUE})
	private void onUpdateFriendInfoReq(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_FRIEND_TXT);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		UpdateFriendInfoReq cparam = protocol.parseProtocol(UpdateFriendInfoReq.getDefaultInstance());		 
		//该操作对玩家没有实际的财产影响，所以就直接放在玩家线程操作，可能存在一种情况就是玩家把该玩家删除之后立马有发起备注
		PlayerRelationEntity playerRelationEntity = RelationService.getInstance().getPlayerRelationEntity(player.getId(), cparam.getFriendPlayerId());
		if (playerRelationEntity == null || playerRelationEntity.getType() != RelationType.FRIEND) {
			sendError(protocol.getType(), Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE);
			return;
		}
		
		//清空备注.
		if (HawkOSOperator.isEmptyString(cparam.getRemark())) {
			playerRelationEntity.setRemark("");
			player.responseSuccess(protocol.getType());
		} else {
			JSONObject json = new JSONObject();
			json.put("msg_type", 0);
			json.put("post_id", 0);
			json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
			json.put("param_id", cparam.getFriendPlayerId());
			GameTssService.getInstance().wordUicChatFilter(player, cparam.getRemark(), 
					MsgCategory.FRIEND_REMARK.getNumber(), GameMsgCategory.UPDATE_FRIEND_INFO, 
					cparam.getFriendPlayerId(), json, protocol.getType());
		}
	}
	
	/**
	 * 拉取未注册好友信息
	 * 
	 * @param protocol
	 */
	//@ProtocolHandler(code = {HP.code.FETCH_UNREG_FRIENDS_C_VALUE})
	protected void onFetchUnregFriendsReq(HawkProtocol protocol) {
		String playerId = player.getId();
		if ("7q1-hoyc8-1".equals(playerId) || "7q1-hpgls-l".equals(playerId)) {
			return;
		}
		
		// 非手Q、微信渠道此功能不开放
		if (!GameUtil.isFriendInviteEnable(player.getChannel())) {
			sendError(protocol.getType(), Status.Error.INVITE_CHANNEL_ERROR);
			return;
		}
		
		// 未注册好友展示数量
		int count = ConstProperty.getInstance().getUnregFriendShowCount();
		UnRegFriendsPB.Builder friendsData = RelationService.getInstance().getUnregFriendList(player.getId(), count);
		if (friendsData != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.UNREG_FRIENDS_ASYNC_PUSH, friendsData));
			HawkLog.logPrintln("fetch unreg friends from cache, playerId: {}, puid: {}", player.getId(), player.getPuid());
			return;
		}
		
		if (UserType.getByChannel(player.getChannel()) == UserType.WX) {
			HawkLog.logPrintln("fetch wx unreg friends from cache failed, playerId: {}, puid: {}", player.getId(), player.getPuid());
			return;
		}
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				try {
					JSONObject result = SDKManager.getInstance().fetchUnregFriends(player.getChannel(), player.getPfTokenJson(), count);
					// 拉取好友失败
					if (result == null || result.getIntValue("ret") != 0) {
						sendError(protocol.getType(), Status.Error.FETCH_UNREG_FRIENDS_FAILED);
						HawkLog.errPrintln("fetch unreg QQ friend failed, playerId: {}, result: {}", player.getId(), result);
						return false;
					}
					
					JSONArray array = result.getJSONArray("friends");
					JSONObject[] friendInfos = array.toArray(new JSONObject[array.size()]);
					UnRegFriendsPB.Builder friendsData = RelationService.getInstance().buildUnregFriendInfo(player, friendInfos, count, "openid");
					if (friendsData != null) {
						player.sendProtocol(HawkProtocol.valueOf(HP.code.UNREG_FRIENDS_ASYNC_PUSH, friendsData));
						HawkLog.logPrintln("fetch unreg friends from sdk, playerId: {}, puid: {}, ", player.getId(), player.getPuid());
					}
					
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				return true;
			}
		});
	}
	
	/**
	 *  客户端通知邀请结果
	 *  
	 * @param protocol
	 */
	//@ProtocolHandler(code = {HP.code.CLIENT_INVITE_RESULT_C_VALUE})
	protected void onClientInviteFriend(HawkProtocol protocol) {
		// 非手Q、微信渠道此功能不开放
		if (!GameUtil.isFriendInviteEnable(player.getChannel())) {
			sendError(protocol.getType(), Status.Error.INVITE_CHANNEL_ERROR);
			return;
		}
		
		// 判断是否在密友列表中
		ClientInviteFriendResult req = protocol.parseProtocol(ClientInviteFriendResult.getDefaultInstance());
		String sopenid = req.getFriendOpenid();
		if (!RelationService.getInstance().isUnregFriend(player.getId(), sopenid)) {
			sendError(protocol.getType(), Status.Error.NOT_UNREG_FRIEND);
			HawkLog.errPrintln("clientInviteFriend failed, sopenid not exist, playerId: {}, sopenid: {}", player.getId(), sopenid);
			return;
		}
		
		// 已经邀请过了，不再是密友了
		if (RelationService.getInstance().isFriendsInvited(player.getId(), sopenid)) {
			sendError(protocol.getType(), Status.Error.NOT_UNREG_FRIEND);
			HawkLog.errPrintln("clientInviteFriend failed, sopenid has been invited, playerId: {}, sopenid: {}", player.getId(), sopenid);
			return;
		}
		
		FriendInviteInfo friendInviteInfo = RelationService.getInstance().getFriendInviteInfo(player.getId());
		
		Map<String, Integer> inviteNotSuccFriends = friendInviteInfo.getInviteNotSuccFriends();
		int inviteTime = inviteNotSuccFriends.containsKey(sopenid) ? inviteNotSuccFriends.get(sopenid) : 0;
		int now = (int) (HawkApp.getInstance().getCurrentTime() / 1000);
		// 距离上次发出邀请的时间太近，不能重复邀请
		if (now - inviteTime < ConstProperty.getInstance().getGoodFriendInviteCD()) {
			sendError(protocol.getType(), Status.Error.FRIEND_INVITE_NOT_EXPIRE_VALUE);
			HawkLog.errPrintln("clientInviteFriend notify proccess failed, friend invite not expire, playerId: {}, sopenid: {}, lastTime: {}", 
					player.getId(), sopenid, inviteTime);
			return;
		}
		
		LogUtil.logClickEvent(player, ClickEventType.FRIEND_INVITE_CLICK);
		// 微信密友的邀请也改到客户端实现了
		inviteNotSuccFriends.put(sopenid, now);
		RedisProxy.getInstance().addInviteFriend(player.getId(), player.getEntity().getServerId(), sopenid);
		player.responseSuccess(protocol.getType());
		
		UnregFriendTaskInfo info = RelationService.getInstance().getUnregFriendInfo(player.getId());
		if (info == null) {
			return;
		}
		
		List<UnRegFriendInfoPB> unregFriendList = info.getUnregFriendList();
		for (int i = 0; i < unregFriendList.size(); i++) {
			UnRegFriendInfoPB friend = unregFriendList.get(i);
			if (friend.getOpenid().equals(sopenid)) {
				UnRegFriendInfoPB.Builder builder = friend.toBuilder();
				builder.setInviteTime(now);
				unregFriendList.set(i, builder.build());
				break;
			}
		}
	}
	
	/**
	 *  领取邀请好友任务奖励
	 *  
	 * @param protocol
	 */
	//@ProtocolHandler(code = {HP.code.INVITE_TASK_AWARD_C_VALUE})
	protected void onTakeInviteTaskAward(HawkProtocol protocol) {
		// 非手Q、微信渠道此功能不开放
		if (!GameUtil.isFriendInviteEnable(player.getChannel())) {
			sendError(protocol.getType(), Status.Error.INVITE_CHANNEL_ERROR);
			return;
		}
		
		InviteTaskAwardReq req = protocol.parseProtocol(InviteTaskAwardReq.getDefaultInstance());
		int taskId = req.getTaskId();
		
		FriendInviteInfo friendInviteInfo = RelationService.getInstance().getFriendInviteInfo(player.getId());
		// 还未邀请过密友
		if (friendInviteInfo.getInviteSuccFriends().isEmpty()) {
			sendError(protocol.getType(), Status.Error.HAS_NOT_INVITE_FRIEND_VALUE);
			HawkLog.errPrintln("takeInviteTaskAward failed, has not invite friend, playerId: {}", player.getId());
			return;
		}
		
		Set<Integer> hasRewardTasks = friendInviteInfo.getHasRewardTaskIds();
		// 该任务奖励已领取
		if (hasRewardTasks.contains(taskId)) {
			sendError(protocol.getType(), Status.Error.TASK_AWARD_HAS_TAKEN_VALUE);
			HawkLog.errPrintln("takeInviteTaskAward failed, task award has taken, playerId: {}, taskId: {}, hasRewardTasks: {}", player.getId(), taskId, hasRewardTasks);
			return;
		}
		
		FriendInviteTaskCfg taskCfg = HawkConfigManager.getInstance().getConfigByKey(FriendInviteTaskCfg.class, taskId);
		// 任务不存在
		if (taskCfg == null) {
			sendError(protocol.getType(), Status.Error.TASK_NOT_EXIST_VALUE);
			HawkLog.errPrintln("takeInviteTaskAward failed, task not exist, playerId: {}, taskId: {}", player.getId(), taskId);
			return;
		}
		
		int count = 0;
		Map<String, TaskAttrInfo> inviteSuccFriends = friendInviteInfo.getInviteSuccFriends();
		if (taskCfg.getType() == 3) {
			long startTime = ConstProperty.getInstance().getWechatFriendsStartTimeLong();
			long endTime = ConstProperty.getInstance().getWechatFriendsEndTimeLong();
			for (TaskAttrInfo taskAttr : inviteSuccFriends.values()) {
				if (taskAttr.getInviteTime() >= startTime && taskAttr.getInviteTime() <= endTime) {
					count ++;
				}
			}
		} else {
			int cityLevel = taskCfg.getCityLevel();
			if (cityLevel == 0) {
				count = inviteSuccFriends.size();
			} else {
				for (TaskAttrInfo taskAttr : inviteSuccFriends.values()) {
					if (taskAttr.getCityLevel() >= cityLevel) {
						count ++;
					}
				}
			}
		}
		
		// 任务未完成
		if (count < taskCfg.getCount()) {
			sendError(protocol.getType(), Status.Error.TASK_HAS_NOT_COMPLETE_VALUE);
			HawkLog.errPrintln("takeInviteTaskAward failed, task has not complete, playerId: {}, taskId: {}, count: {}, cfg count: {}", 
					player.getId(), taskId, count, taskCfg.getCount());
			return;
		}
		
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(taskCfg.getAwardItems());
		awardItems.rewardTakeAffectAndPush(player, Action.INVITE_QQ_FRIEND_AWARD, true);
		
		hasRewardTasks.add(taskId);
		RedisProxy.getInstance().addFriendInviteRewardTask(player.getId(), taskId);
		
		InviteTaskRewardedPushPB.Builder builder = InviteTaskRewardedPushPB.newBuilder();
		builder.addTaskId(taskId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.INVITE_REWARD_TASK_PUSH, builder));
		
		player.responseSuccess(protocol.getType());
	}
	
	@MessageHandler
	public void onLocalLogin(LocalLoginMsg localLoginMsg) {
		this.onPlayerAssemble();
		this.onPlayerLogin();
	}
	
	/**
	 * 同步守护信息.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_INFO_REQ_VALUE)
	public void onGuardInfoReq(HawkProtocol protocol) {
		RelationService.getInstance().synGuardInfo(player);
	}
	
	/**
	 * 拉取守护请求列表
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_INVITE_LIST_REQ_VALUE)
	public void onGuardInviteListReq(HawkProtocol protocol) {
		RelationService.getInstance().synGuardInviteList(player);
	}	
	
	/**
	 * 邀请守护玩家
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_INVITE_PLAYER_REQ_VALUE)
	public void onGuardInvitePlayerReq(HawkProtocol protocol) {
		GuardInvitePlayerReq req = protocol.parseProtocol(GuardInvitePlayerReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		if (!GlobalData.getInstance().isExistPlayerId(playerId)) {
			sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
			
			return;
		}
		
		//不能邀请一个跨服的玩家.
		if (CrossService.getInstance().isCrossPlayer(playerId)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE);
			
			return;
		}
		
		GuardianConstConfig constCfg = GuardianConstConfig.getInstance();
		ConsumeItems items = ConsumeItems.valueOf();
		items.addConsumeInfo(constCfg.getInviteCostList());
		int consumeResult = items.checkConsumeAndGetResult(player);
		if (consumeResult != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), consumeResult);
			
			return;
		}
		
		player.rpcCall(GameConst.MsgId.GUARD_INVITE_PLAYER, RelationService.getInstance(), new GuardInviteRpcInvoker(player, playerId, items));
	}
	
	/**
	 * 拉取可邀请列表
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_CAN_INVITE_PLAYERS_REQ_VALUE)
	public void onGuardCanInviteListReq(HawkProtocol protocol) {
		RelationService.getInstance().synGuardCanInvitePlayers(player);
	}
	
	/**
	 * 守护信息设置
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_SETTING_EDIT_REQ_VALUE)
	public void onGuardSettingEditReq(HawkProtocol protocol) {
		GuardSettingEditReq req = protocol.parseProtocol(GuardSettingEditReq.getDefaultInstance());
		if (req.getPosition().getNumber() < PlayerFlagPosition.GUARD_ONLINE_STATUS_VALUE
				|| req.getPosition().getNumber() > PlayerFlagPosition.GUARD_RECEIVE_RADER_VALUE) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE, 0);
			
			return;
		}
		
		
		int oldPositionValue = player.getData().getFlag(req.getPosition());
		GameUtil.setFlagAndPush(player, req.getPosition(), oldPositionValue ^ 1);
	}
	
	/**
	 * 获取守护礼包信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_GIFT_INFO_REQ_VALUE)
	public void onGuardGiftInfoReq(HawkProtocol protocol) {
		Map<Integer, Integer> guardGiftMap = player.getData().getDailyDataEntity().getGuardGiftMap();
		GuardGiftInfoResp.Builder sbuilder = GuardGiftInfoResp.newBuilder(); 
		for (Entry<Integer, Integer> entry : guardGiftMap.entrySet()) {
			sbuilder.addGiftList(BuilderUtil.buildGuardGift(entry.getKey(), entry.getValue()));
		}
		
		HawkProtocol sprotocol = HawkProtocol.valueOf(HP.code.GUARD_GIFT_INFO_RESP_VALUE, sbuilder);
		player.sendProtocol(sprotocol);
	}
	
	/**
	 * 赠送守护礼包
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_GIFT_SEND_REQ_VALUE)
	public void onGuardGiftSendReq(HawkProtocol protocol) {
		GuardGiftSendReq req = protocol.parseProtocol(GuardGiftSendReq.getDefaultInstance());
		int giftId = req.getGiftId();
		GuardianGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GuardianGiftCfg.class, giftId);
		if (giftCfg == null) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE, 0);
			
			return;
		}
		
		//尽量在玩家多做校验,跨线程交互.
		RelationService relationService = RelationService.getInstance(); 
		String guardPlayerId = relationService.getGuardPlayer(player.getId());
		if (HawkOSOperator.isEmptyString(guardPlayerId)) {
			player.sendError(protocol.getType(), Status.Error.GUARD_NOT_CREATED_VALUE, 0);
			
			return;
		}
		if (!relationService.isFriend(player.getId(), guardPlayerId)) {
			player.sendError(protocol.getType(), Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE, 0);
			
			return;
		}
		
		int boughtNum = player.getData().getDailyDataEntity().getGuardGiftNum(giftId);
		if (boughtNum >= giftCfg.getDailyLimit()) {
			player.sendError(protocol.getType(), Status.Error.GUARD_GIFT_LIMIMT_VALUE, 0);
			
			return;
		}
		
		if (CrossService.getInstance().isCrossPlayer(player.getId()) || CrossService.getInstance().isCrossPlayer(guardPlayerId)) {
			player.sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE, 0);
			
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(giftCfg.getPriceList());
		int itemCheck = consumeItems.checkConsumeAndGetResult(player);
		if (itemCheck != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(protocol.getType(), itemCheck, 0);
			
			return;
		}
		consumeItems.consumeAndPush(player, Action.GUARD_SEND_GIFT);
		
		//修改购买数量.
		player.getData().getDailyDataEntity().addGuardGiftNum(giftId, 1);
	
		MailParames.Builder builder = MailParames.newBuilder()
				.setPlayerId(guardPlayerId).setMailId(MailId.GUARD_RECEIVE_GIFT).setRewards(giftCfg.getItemList()).setAwardStatus(MailRewardStatus.NOT_GET);
		builder.addContents(player.getName());
		builder.addSubTitles(player.getName());
		SystemMailService.getInstance().sendMail(builder.build());
		
		relationService.dealMsg(GameConst.MsgId.GUARD_SEND_GIFT, new GuardGiftSendInvoker(player, giftId));				
	}
	
	/**
	 *删除守护关系.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_RELATION_DELETE_REQ_VALUE)
	public void onGuardDeleteReq(HawkProtocol protocol) {
		String guardPlayer = RelationService.getInstance().getGuardPlayer(player.getId());
		if (HawkOSOperator.isEmptyString(guardPlayer)) {
			player.sendError(protocol.getType(), Status.Error.GUARD_NOT_CREATED_VALUE, 0);
			
			return;
		}
		
		//目标对象跨服了.则不能解除关系.
		if (CrossService.getInstance().isCrossPlayer(guardPlayer)) {
			player.sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE, 0);
			
			return;
		}
		
		RelationService.getInstance().dealMsg(GameConst.MsgId.GUARD_DELETE, new GuardDeleteInvoker(player.getId()));
	}
	
	/**
	 * 处理守护请求
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_INVITE_HANDLE_REQ_VALUE)
	public void onGuardInviteHandleReq(HawkProtocol protocol) {
		GuardInviteHandleReq req = protocol.parseProtocol(GuardInviteHandleReq.getDefaultInstance());
		if (HawkOSOperator.isEmptyString(req.getPlayerId())) {
			if (req.getOper() != OperationType.REJECT) {
				player.sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE, 0);
				
				return;
			}
		} else {
			if (!GlobalData.getInstance().isExistPlayerId(req.getPlayerId())) {
				player.sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE, 0);
				
				return;
			}
			
			if (req.getOper() == OperationType.AGREEE) {
				if (CrossService.getInstance().isCrossPlayer(req.getPlayerId())) {
					this.sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE);
					
					return;
				}							
			}
		}
					
		RelationService.getInstance().dealMsg(GameConst.MsgId.GUARD_HANDLE, new GuardInviteHandleInvoker(player, req.getPlayerId(), req.getOper())); 
	}
	
	/**
	 * 请求特效
	 * @param hawkProtocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_DRESS_REQ_VALUE)
	public void onGuardDressReq(HawkProtocol hawkProtocol) {
		RelationService.getInstance().synGuardDressId(player.getId());
	}
	
	/**
	 * 修改特效
	 * @param hawkProtocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUARD_DRESS_UPDATE_REQ_VALUE)
	public void onGuardDressUpdateReq(HawkProtocol hawkProtocol) {
		GuardDressUpdateReq req = hawkProtocol.parseProtocol(GuardDressUpdateReq.getDefaultInstance());
		String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
		if (HawkOSOperator.isEmptyString(guardPlayerId)) {
			player.sendError(hawkProtocol.getType(), Status.Error.GUARD_NOT_CREATED_VALUE, 0);
			
			return;
		}
		
		if (CrossService.getInstance().isCrossPlayer(guardPlayerId)) {
			player.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE, 0);
			
			return;
		}
				
		RelationService.getInstance().dealMsg(GameConst.MsgId.GUARD_DRESS_UPDATE, new GuardDressUpdateInvoker(player, req.getDressId()));
	}
	
	@ProtocolHandler(code = HP.code.GUARD_OUT_FIRE_REQ_VALUE)
	public void onGuardOutFire(HawkProtocol hawkProtocol) {
		String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
		if (HawkOSOperator.isEmptyString(guardPlayerId)) {
			player.sendError(hawkProtocol.getType(), Status.Error.GUARD_NOT_CREATED_VALUE, 0);
			
			return; 
		}
		
		//自己或者对方跨服了.
		if (CrossService.getInstance().isCrossPlayer(player.getId()) || CrossService.getInstance().isCrossPlayer(guardPlayerId)) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_ON_CROSS_VALUE);
			
			return;
		}
		
		Player guardPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);
		//在副本状态里面.
		if (guardPlayer.isInDungeonMap()) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_IN_INSTACNE_VALUE);
			
			return;
		}
				
		long onFireEndTime = guardPlayer.getData().getPlayerBaseEntity().getOnFireEndTime();
		if (onFireEndTime <= HawkTime.getMillisecond()) {			
			sendError(hawkProtocol.getType(), Status.Error.GUARD_NOT_ON_FIRE_VALUE);
			RelationService.getInstance().synGuardUpdate(player.getId(), guardPlayerId);
			
			return;
		}
		
		ConsumeItems items = ConsumeItems.valueOf();
		items.addConsumeInfo(GuardianConstConfig.getInstance().getPutOutFirePriceList());
		int checkResult = items.checkConsumeAndGetResult(player);
		if (checkResult != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(hawkProtocol.getType(), checkResult, 0);
			
			return;
		}		
		items.consumeAndPush(player, Action.GUARD_OUT_FIRE);
		
		GuardOutFireMsg msg = new GuardOutFireMsg(player.getId());
		HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, guardPlayerId);
		HawkTaskManager.getInstance().postMsg(xid, msg);
	}
	
	@ProtocolHandler(code = HP.code.GUARD_OPEN_COVER_REQ_VALUE)
	public void onGuardOpenCover(HawkProtocol hawkProtocol) {
		GuardOpenCoverReq req = hawkProtocol.parseProtocol(GuardOpenCoverReq.getDefaultInstance());
		GuardianHelpCfg helpCfg = HawkConfigManager.getInstance().getConfigByKey(GuardianHelpCfg.class, req.getHelpCfgId());
		if (helpCfg == null) {
			player.sendError(hawkProtocol.getType(), Status.SysError.PARAMS_INVALID_VALUE, 0);
			
			return;
		}
		
		String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
		if (HawkOSOperator.isEmptyString(guardPlayerId)) {
			player.sendError(hawkProtocol.getType(), Status.Error.GUARD_NOT_CREATED_VALUE, 0);
			
			return;
		}
					
		if (CrossService.getInstance().isCrossPlayer(player.getId()) || CrossService.getInstance().isCrossPlayer(guardPlayerId)) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_ON_CROSS_VALUE);
			
			return;
		}
		
		Player guardPlayer = GlobalData.getInstance().makesurePlayer(guardPlayerId);
		if (guardPlayer == null) {
			sendError(hawkProtocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
			
			return;
		}
		
		if (!guardPlayer.getData().checkFlagSet(PlayerFlagPosition.GUARD_PROTECTED_TIME)) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_NOT_ALLOW_VALUE);
			
			return;
		}
		
		//战争狂热状态.
		if (guardPlayer.getData().getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond() || 
				WorldMarchService.getInstance().hasOffensiveMarch(guardPlayerId)) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_ON_WAR_TIME_VALUE);
			RelationService.getInstance().synGuardUpdate(player.getId(), guardPlayerId);
			
			return;
		}
		
		//在副本状态里面.
		if (guardPlayer.isInDungeonMap()) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_IN_INSTACNE_VALUE);
			
			return;
		}
		
		
		WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
		if (wp == null) {
			player.sendError(hawkProtocol.getType(), Status.Error.GUARD_NO_WORLD_POINT_VALUE, 0);
			
			return;
		}
		
		
		ConsumeItems items = ConsumeItems.valueOf();
		items.addConsumeInfo(helpCfg.getPriceList());
		int checkResult =  items.checkConsumeAndGetResult(player);
		if (checkResult != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(hawkProtocol.getType(), checkResult, 0);
			
			return;
		}		
		items.consumeAndPush(player, Action.GUARD_OPEN_COVER);
		
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, helpCfg.getItemId());
		Player.logger.info("send playerId:{} help targetPlayerId:{} opencover itemId:{}", player.getId(), guardPlayerId, helpCfg.getItemId());
		GuardHelpOpenCoverMsg msg = new GuardHelpOpenCoverMsg(player.getId(), itemCfg);
		HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, guardPlayerId);
		HawkTaskManager.getInstance().postMsg(xid, msg);
	}
	
	@MessageHandler
	public void onGuardHelpOpenConver(GuardHelpOpenCoverMsg msg) {
		Player.logger.info("receive playerId:{} help targetPlayerId:{} opencover ", msg.getPlayerId(), player.getId());
		
		//在副本里面
		if (player.isInDungeonMap()) {
			Player.logger.info("openCoverFail playerId:{}, targetPlayerId:{} in instance", msg.getPlayerId(), player.getId());
			
			return;
		}
		
		//狂热状态
		if (player.getData().getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()
				|| WorldMarchService.getInstance().hasOffensiveMarch(player.getId())) {
			Player.logger.info("openCoverFail sourcePlayerId:{}, targetPlayerId:{} on war ", msg.getPlayerId(), player.getId());
			
			return;
		}
		
		WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		if (wp == null) {
			Player.logger.info("openCoverFail sourcePlayerId:{}, targetPlayerId:{}, world point is null", msg.getPlayerId(), player.getId());
			
			return;
		}
		
		StatusDataEntity sde = player.addStatusBuff(msg.getItem().getBuffId());
		WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), sde.getEndTime());
		player.getPush().syncPlayerStatusInfo(false, sde);			
		Player sourcePlayer = GlobalData.getInstance().makesurePlayer(msg.getPlayerId());
		RelationService.getInstance().synGuardUpdate(msg.getPlayerId(), player.getId());
		
		MailParames.Builder builder = MailParames.newBuilder()
				.setPlayerId(player.getId()).setMailId(MailId.GUARD_OPEN_COVER);
		builder.addContents(sourcePlayer.getName(), msg.getItem().getId());
		builder.addContents(sourcePlayer.getName());
		SystemMailService.getInstance().sendMail(builder.build());
	}
	
	//仇恨名单
	@ProtocolHandler(code = { HP.code.HATE_RANK_INFO_REQ_VALUE })
	private void hateRankListInfoReq(HawkProtocol protocol) {
		RelationService.getInstance().syncHateRankList(player);
	}
	
	@ProtocolHandler(code = {HP.code.GUARD_DRESS_ITEM_EXCHANGE_REQ_VALUE})
	private void guardDressItemExchange(HawkProtocol protocol) {
		GuardDressItemExchangeReq req = protocol.parseProtocol(GuardDressItemExchangeReq.getDefaultInstance());
		int itemId = req.getItemId();
		ItemInfo itemInfo = new ItemInfo(ItemType.TOOL_VALUE, itemId, 1);
		GuardianItemDressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(GuardianItemDressCfg.class, itemId);
		if (dressCfg == null) {
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		if (!RelationService.getInstance().hasGuarder(player.getId())) {
			this.sendError(protocol.getType(), Status.Error.GUARD_NOT_CREATED_VALUE);
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(itemInfo, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		
		Map<Integer, Integer> dressInfoMap = player.getData().getPlayerOtherEntity().getDressItemInfoMap();
		int dressId = dressCfg.getSingleDressId();
		int oldEndTime = Math.max(MapUtil.getIntValue(dressInfoMap, dressId), HawkTime.getSeconds());
		//已经是永久
		if (oldEndTime < 0) {
			this.sendError(protocol.getType(), Status.Error.GUARD_DRESS_HAS_OWN_VALUE);
			return;
		} else if (dressCfg.getValidTime() < 0) {
			//当前使用的是永久的
			player.getData().getPlayerOtherEntity().addDressItemInfo(dressId, dressCfg.getValidTime());
		} else {
			//时间追加
			player.getData().getPlayerOtherEntity().addDressItemInfo(dressId, oldEndTime + dressCfg.getValidTime());
		}	
		consumeItems.consumeAndPush(player, Action.GUARD_DRESS_EXCHANGE);
		
		//使用特效.
		RelationService.getInstance().dealMsg(GameConst.MsgId.GUARD_DRESS_UPDATE, new GuardDressUpdateInvoker(player, dressId, false));
		//同步玩家的装扮id
		RelationService.getInstance().synGuardDressId(player.getId());				
	}
	
	@Override
	public boolean onTick() {
		checkUpdateDress(false);
		return true;
	}
	
	public void checkUpdateDress(boolean forcePush) {
		Map<Integer, Integer> dressMap = player.getData().getPlayerOtherEntity().getDressItemInfoMap();
		if (MapUtils.isEmpty(dressMap)) {
			if (forcePush) {
				Set<Integer> remainDressId = new HashSet<>(player.getData().getPlayerOtherEntity().getDressItemInfoMap().keySet());
				UpdateGuardDressMsg msg = new UpdateGuardDressMsg(player.getId(), remainDressId);
				HawkTaskManager.getInstance().postMsg(RelationService.getInstance().getXid(), msg);
			}
			return ;
		}
		int curTime = HawkTime.getSeconds();
		Set<Integer> removeDressIdSet = new HashSet<>();
		for (Entry<Integer, Integer> entry : dressMap.entrySet()) {
			if (entry.getValue() < 0) {
				continue;
			}
			if (curTime > entry.getValue()) {
				removeDressIdSet.add(entry.getKey());
			}			
		}
		
		boolean push = false;
		if (!removeDressIdSet.isEmpty()) {
			for (Integer removeDressId : removeDressIdSet) {
				player.getData().getPlayerOtherEntity().removeDressItemInfo(removeDressId);
			}						
			push = true;
		}
		
		if (push | forcePush) {
			Set<Integer> remainDressId = new HashSet<>(player.getData().getPlayerOtherEntity().getDressItemInfoMap().keySet());
			UpdateGuardDressMsg msg = new UpdateGuardDressMsg(player.getId(), remainDressId);
			HawkTaskManager.getInstance().postMsg(RelationService.getInstance().getXid(), msg);
		}
	}
	
	@MessageHandler
	private void onCheckGuardDressMsg(CheckGuardDressMsg msg) {
		Player.logger.info("receive check dress msg id:{}", player.getId());
		this.checkUpdateDress(false);
	}
	
	@ProtocolHandler(code = HP.code.GUARD_SEND_AND_BLAG_HISTORY_REQ_VALUE)
	private void onGuardSendAndBlagHistory(HawkProtocol hawkProtocol) {
		GuardSendAndBlagHistoryReq req = hawkProtocol.parseProtocol(GuardSendAndBlagHistoryReq.getDefaultInstance()); 
		List<GuardSendAndBlagHistoryInfo> infoList = null;
		if (req.getType() == GuardOptType.GUARD_SEND) {
			infoList = RedisProxy.getInstance().getGuardDressSendHistory(player.getId());
		} else {
			infoList = RedisProxy.getInstance().getGuardDressBlagHistory(player.getId());
		}
		
		GuardSendAndBlagHistoryResp.Builder respBuilder = GuardSendAndBlagHistoryResp.newBuilder();
		respBuilder.setType(req.getType());
		respBuilder.addAllInfos(infoList);
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.GUARD_SEND_AND_BLAG_HISTORY_RESP_VALUE, respBuilder);
		player.sendProtocol(respProtocol);
	}
	
	/**
	 * 守护特效索要
	 * @param hawkProtocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_DRESS_ITEM_BLAG_REQ_VALUE) 
	private void onBlagGuardDressReq(HawkProtocol hawkProtocol) {
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
				
		int mailId = MailId.GUARD_GUILD_BLAG_VALUE;
		GuardDressItemBlagReq req = hawkProtocol.parseProtocol(GuardDressItemBlagReq.getDefaultInstance());
		
		Player tPlayer = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (null == tPlayer) {
			sendError(hawkProtocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
			return;
		}
		
		if (player.isCsPlayer()) {
			this.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE);
			return;
		}
		
		if (CrossService.getInstance().isCrossPlayer(req.getPlayerId())) {
			this.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT);
			return;
		}

		CustomDataEntity customSetEntity = tPlayer.getData().getCustomDataEntity(GsConst.PLAYER_DRESS_ASKREFUSE);
		if (null != customSetEntity && !HawkOSOperator.isEmptyString(customSetEntity.getArg())
				&& 0 != Integer.valueOf(customSetEntity.getArg())) {
			sendError(hawkProtocol.getType(), Status.Error.ASK_DRESS_TARGET_REFUSE);
			return;
		}
		// 2盟友/1好友
		if (2 == req.getRelationType()) {
			mailId = MailId.GUARD_GUILD_BLAG_VALUE;
			if (!GuildService.getInstance().isInTheSameGuild(req.getPlayerId(), player.getId())) {
				sendError(hawkProtocol.getType(), Status.Error.GUILD_NOT_SAME_VALUE);
				return;
			}
		} else {
			mailId = MailId.GUARD_FRIEND_BLAG_VALUE;
			if (!RelationService.getInstance().isFriend(player.getId(), req.getPlayerId())) {
				sendError(hawkProtocol.getType(), Status.Error.RELATION_FRIEND_NOT_EXIST_VALUE);
				return;
			}
		}
		
		GuardianGiveKvCfg kvCfg = HawkConfigManager.getInstance().getConfigByIndex(GuardianGiveKvCfg.class, 0); 
		if (req.getSendWord().getBytes().length > kvCfg.getDressSendMsgLen()) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_DRESS_ASK_WORDS_LIMIT_VALUE);
			return;
		}
		
		if (LocalRedis.getInstance().checkGuardDressHasBlagLog(player.getId(), req.getPlayerId())) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_DRESS_BLAG_CD_VALUE);
			return;
		}
		
		JSONObject callback = new JSONObject();
		callback.put("mailId", mailId);
		callback.put("dressId", req.getDressId());
		callback.put("playerId", req.getPlayerId());
		
		JSONObject gameData = new JSONObject();
		String pfIconPrimitive = tPlayer.getData().getPrimitivePfIcon();
		if (GlobalData.getInstance().isBanPortraitAccount(tPlayer.getOpenId())) {
			pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
		}
		gameData.put("recv_account", tPlayer.getOpenId());
		gameData.put("recv_role_name", tPlayer.getName());
		gameData.put("recv_role_pic_url", pfIconPrimitive);
		gameData.put("recv_area_id", "wx".equalsIgnoreCase(tPlayer.getChannel()) ? 1 : 2);
		gameData.put("recv_plat_id", GameUtil.isAndroidAccount(tPlayer) ? 1 : 0);
		gameData.put("recv_world_id", tPlayer.getServerId());
		gameData.put("recv_role_id", tPlayer.getId());
		gameData.put("recv_role_level", tPlayer.getLevel());
		gameData.put("recv_role_battlepoint", tPlayer.getPower());
		
		GameTssService.getInstance().wordUicChatFilter(player, req.getSendWord(), 
				MsgCategory.ASK_DRESS.getNumber(), GameMsgCategory.BLAG_GUARD_DRESS, 
				callback.toJSONString(), gameData, hawkProtocol.getType());
	}
	
	/**
	 * 守护特效赠送.
	 * @param hawkProtocol
	 */
	@ProtocolHandler(code = HP.code.GUARD_DRESS_ITEM_SEND_REQ_VALUE)
	private void onSendGuardDress(HawkProtocol hawkProtocol) {
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
				
		GuardDressItemSendReq req = hawkProtocol.parseProtocol(GuardDressItemSendReq.getDefaultInstance());
		MailConst.MailId mailId = MailConst.MailId.GUARD_FRIEND_SEND;			
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class,
				 req.getItemId());
		if (null == itemCfg) {
			Player.logger.info("item not found playerId:{} itemId:{}", player.getId(),req.getItemId());
			sendError(hawkProtocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		GuardianItemDressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(GuardianItemDressCfg.class, req.getItemId());
		if (dressCfg == null) {
			Player.logger.error("dress cfg not found playerId:{}, itemId:{}", player.getId(), req.getItemId());
			sendError(hawkProtocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		GuardianGiveKvCfg kvCfg = HawkConfigManager.getInstance().getConfigByIndex(GuardianGiveKvCfg.class, 0); 
		if (1 != itemCfg.getCanGive()) {
			Player.logger.info("item can not give playerId:{} itemId:{}", player.getId(),req.getItemId());
			sendError(hawkProtocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}		
		String world = req.getSendWord();
		if (world.getBytes().length > kvCfg.getDressSendMsgLen()) {
			Player.logger.error("send word too long id:{}", player.getId());
			sendError(hawkProtocol.getType(), Status.Error.GUARD_DRESS_SEND_WORDS_LIMIT_VALUE);
			return;
		}
	   //是不是2盟友/1好友
		Player tPlayer = GlobalData.getInstance().makesurePlayer(req.getPlayerId());
		if (null == tPlayer) {
			sendError(hawkProtocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
			return;
		}
		
		if (player.isCsPlayer()) {
			this.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE);
			return;
		}
		
		if (CrossService.getInstance().isCrossPlayer(req.getPlayerId())) {
			this.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT);
			return;
		}

		// 盟友
		if (2 == req.getRelationType()) {
			mailId = MailConst.MailId.GUARD_GUILD_SEND;
			if (!GuildService.getInstance().isInTheSameGuild(req.getPlayerId(), player.getId())) {
				sendError(hawkProtocol.getType(), Status.Error.GUILD_NOT_SAME_VALUE);
				return;
			}
		} else {
			mailId = MailConst.MailId.GUARD_FRIEND_SEND;
			if (!RelationService.getInstance().isFriend(player.getId(), req.getPlayerId())) {
				sendError(hawkProtocol.getType(), Status.Error.RELATION_FRIEND_NOT_EXIST);
				return;
			}
		}			
			
		ConsumeItems messagerCost = ConsumeItems.valueOf();		
		if( dressCfg.getValidTime() < 0){
			int num = player.getData().getItemNumByItemId(kvCfg.getItemList2().get(0).getItemId());
			if(num > 0){
				messagerCost.addConsumeInfo(kvCfg.getItemList2());
			}else{
				messagerCost.addConsumeInfo(kvCfg.getItemList3());
			}			
		}else{
			int num = player.getData().getItemNumByItemId(kvCfg.getItemList().get(0).getItemId());
			if(num > 0){
				messagerCost.addConsumeInfo(kvCfg.getItemList());
			}else{
				messagerCost.addConsumeInfo(kvCfg.getItemList3());
			}
		}
		
		ItemInfo sendItem = new ItemInfo(ItemType.TOOL_VALUE, req.getItemId(), 1);
		messagerCost.addConsumeInfo(sendItem, false);											
		if (!messagerCost.checkConsume(player)) {
			sendError(hawkProtocol.getType(), Status.Error.GUARD_DRESS_ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		
		JSONObject callbak = new JSONObject();
		callbak.put("mailId", mailId.getNumber());
		callbak.put("itemId", req.getItemId());
		callbak.put("playerId", req.getPlayerId());

		String pfIconPrimitive = tPlayer.getData().getPrimitivePfIcon();
		if (GlobalData.getInstance().isBanPortraitAccount(tPlayer.getOpenId())) {
			pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
		}
		
		JSONObject gameData = new JSONObject();
		gameData.put("recv_account", tPlayer.getOpenId());
		gameData.put("recv_role_name", tPlayer.getName());
		gameData.put("recv_role_pic_url", pfIconPrimitive);
		gameData.put("recv_area_id", "wx".equalsIgnoreCase(tPlayer.getChannel()) ? 1 : 2);
		gameData.put("recv_plat_id", GameUtil.isAndroidAccount(tPlayer) ? 1 : 0);
		gameData.put("recv_world_id", tPlayer.getServerId());
		gameData.put("recv_role_id", tPlayer.getId());
		gameData.put("recv_role_level", tPlayer.getLevel());
		gameData.put("recv_role_battlepoint", tPlayer.getPower());
		
		GameTssService.getInstance().wordUicChatFilter(player, req.getSendWord(), 
				MsgCategory.SEND_DRESS.getNumber(), GameMsgCategory.SEND_GUARD_DRESS, 
				callbak.toJSONString(), gameData, hawkProtocol.getType());
	}
	
	public PBPlayerGuardDressMailContent.Builder buildPBPlayerGuardDressMailContent(Player opPlayer, ItemInfo itemInfo, int askDressId, String words) {
		PBPlayerGuardDressMailContent.Builder mailBuilder = PBPlayerGuardDressMailContent.newBuilder()
				.setPlayerId(opPlayer.getId()).setPlayerName(opPlayer.getName()).setPfIcon(opPlayer.getPfIcon())
				.setLevel(opPlayer.getLevel()).setCityLevel(opPlayer.getCityLevel()).setPower(opPlayer.getPower())
				.setGuildTag((null == opPlayer.getGuildTag()) ? "" : opPlayer.getGuildTag())
				.setGuildName((null == opPlayer.getGuildName()) ? "" : opPlayer.getGuildName())
				.setGuildId(opPlayer.getGuildId()).setLastOpTime(HawkTime.getMillisecond())
				.setContent(words).setAskDressId(askDressId);
		if (itemInfo != null) {
			mailBuilder.setSendItem(itemInfo.toString());
		}
		
		return mailBuilder;
	}
	
	public GuardSendAndBlagHistoryInfo.Builder builderHistoryInfo(Player tPlayer, ItemInfo itemInfo, int askDressId) {
		GuardSendAndBlagHistoryInfo.Builder builder = GuardSendAndBlagHistoryInfo.newBuilder(); 
		builder.setPlayerId(tPlayer.getId());
		builder.setPlayerName(tPlayer.getName());
		builder.setLevel(tPlayer.getLevel());
		builder.setPfIcon(tPlayer.getPfIcon());
		builder.setCityLevel(tPlayer.getCityLevel());
		builder.setGuildId(tPlayer.getGuildId());
		builder.setGuildName((null == tPlayer.getGuildName()) ? "" : tPlayer.getGuildName());
		builder.setGuildTag((null == tPlayer.getGuildTag()) ? "" : tPlayer.getGuildTag());
		builder.setPower(tPlayer.getPower());
		builder.setLastOpTime(HawkTime.getMillisecond());
		if (itemInfo != null) {
			builder.setSendItem(itemInfo.toString());
		}		
		builder.setAskDressId(askDressId);
		
		return builder;
	}
	
}
