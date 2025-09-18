package com.hawk.game.module;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.AnchorGiftCfg;
import com.hawk.game.config.ControlProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.data.ChatMsgFilterCallbackData;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.LMJYRoomManager;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.msg.ChatMsgFilterFinishMsg;
import com.hawk.game.msg.SelfChatMsgFilterFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Anchor.ASInfoRedisKey;
import com.hawk.game.protocol.Anchor.AddAnchorFollow;
import com.hawk.game.protocol.Anchor.AnchorPreviewInfoGS;
import com.hawk.game.protocol.Anchor.AnchorPreviewInfoRedisKey;
import com.hawk.game.protocol.Anchor.AnchorPreviewListGS;
import com.hawk.game.protocol.Anchor.AnchorServerInfoGS;
import com.hawk.game.protocol.Anchor.SendGiftForAnchor;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Chat.HPChatMsgCacheReq;
import com.hawk.game.protocol.Chat.HPChatOLQuestionListResp;
import com.hawk.game.protocol.Chat.HPChatOLQuestionReq;
import com.hawk.game.protocol.Chat.HPChatState;
import com.hawk.game.protocol.Chat.HPChatStateChangeReq;
import com.hawk.game.protocol.Chat.HPPushChat;
import com.hawk.game.protocol.Chat.HPSendChat;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Player.PlayerSnapshotPB;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TestFight;
import com.hawk.game.protocol.TestFight.TFFightUnit;
import com.hawk.game.protocol.TestFight.TFFighter;
import com.hawk.game.protocol.TestFight.TFGuildInfo;
import com.hawk.game.protocol.TestFight.TFGuildInfoReq;
import com.hawk.game.protocol.TestFight.TFGuildInfoResp;
import com.hawk.game.protocol.TestFight.TFTestFightReq;
import com.hawk.game.protocol.World.PlayerPresetMarchInfo;
import com.hawk.game.protocol.World.PresetMarchInfo;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.player.item.ItemService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.CreateFubenUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.Alliance;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.GsConst.UicMsgResultFlag;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogMsgType;

/**
 * 聊天模块
 * 
 * @author zdz
 *
 */
public class PlayerChatModule extends PlayerModule {
	private static Logger logger = LoggerFactory.getLogger("Chat");
	/**
	 * 上次聊天信息时间
	 */
	private long lastChatMsg = 0;

	public PlayerChatModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerLogin() {
		
		player.setChatState(HPChatState.NOT_CHAT);
		// 世界聊天已关闭
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.WORLD_CHAT)) {
			ChatService.getInstance().chatSystemClosedNotice(player, NoticeType.WORLD_CHAR_CONTROL);
		} else {
			this.addWorldSession(player);
		}

		// 联盟聊天已关闭
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.GUILD_CHAT)) {
			ChatService.getInstance().chatSystemClosedNotice(player, NoticeType.GUILD_CHAR_CONTROL);
		} else {
			this.addGuildSession(player);
		}

		// 检查当前主播信息
		ChatService.getInstance().directSendAnchorInfo(player);

		return true;
	}

	@Override
	protected boolean onPlayerLogout() {
		player.setChatState(HPChatState.NOT_CHAT);
		return true;
	}

	/**
	 * 添加世界活跃会话
	 * 
	 * @param session
	 */
	public void addWorldSession(Player receiver) {
		try {
			long now = HawkApp.getInstance().getCurrentTime();
			HPPushChat.Builder worldmsg = ChatService.getInstance().getMsgCache(player.getGuildId(), player.getId(), now, Const.ChatType.CHAT_WORLD);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MSG_PUSH_S, worldmsg));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 添加联盟会话
	 * 
	 * @param session
	 */
	public void addGuildSession(Player receiver) {
		try {
			long now = HawkApp.getInstance().getCurrentTime();
			HPPushChat.Builder guildmsg = ChatService.getInstance().getMsgCache(player.getGuildId(), player.getId(), now, Const.ChatType.CHAT_ALLIANCE);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.ALLIANCE_MSG_PUSH_S, guildmsg));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 改变聊天状态 */
	@ProtocolHandler(code = HP.code.WORLD_MSG_STATE_C_VALUE)
	private void onChatStateChange(HawkProtocol protocol) {
		HPChatStateChangeReq req = protocol.parseProtocol(HPChatStateChangeReq.getDefaultInstance());
		HPChatState chatState = req.getState();
		player.setChatState(chatState);
		if (chatState == HPChatState.CHAT) {
			this.addGuildSession(player);
			this.addWorldSession(player);
		}
		player.responseSuccess(protocol.getType());

	}

	/**
	 * 玩家聊天（世界聊天，联盟聊天,世界广播）
	 * 
	 * @param protocol
	 * @throws Exception
	 */
	@ProtocolHandler(code = HP.code.SEND_CHAT_C_VALUE)
	private void onPlayerChat(HawkProtocol protocol) throws Exception {
		long currTime = HawkTime.getMillisecond();
		if (currTime - lastChatMsg < GameConstCfg.getInstance().getChatTimeInterval() * 1000) {
			return;
		}
		HPSendChat req = protocol.parseProtocol(HPSendChat.getDefaultInstance());
		String chatMsg = req.getChatMsg();
		if (GsConfig.getInstance().isDebug()) {
			CreateFubenUtil.fuben(player, chatMsg);
		}
		byte[] voice = req.getVoice().toByteArray();
		int voiceLength = req.getVoiceLength();
		voiceLength = Math.max(1, voiceLength);
		voiceLength = Math.min(Alliance.VOICE_CHAT_MAX_TIME, voiceLength);

		ChatType type = ChatType.valueOf(req.getChatType());

		if (currTime - lastChatMsg < 1000 * ControlProperty.getInstance().getChatInterval()) { // 刷屏
			return;
		}
		
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		if (type != Const.ChatType.CHAT_BROADCAST &&
				type != Const.ChatType.CHAT_WORLD &&
				type != Const.ChatType.CHAT_ALLIANCE &&
				type != Const.ChatType.CHAT_FUBEN &&
				type != Const.ChatType.CHAT_FUBEN_TEAM&&
				type != Const.ChatType.CHAT_FUBEN_NATION) {
			return;
		}
		
		if (type == Const.ChatType.CHAT_BROADCAST) {
			if (player.getCityLevel() < ControlProperty.getInstance().getBroadcastCityLv()) {
				return;
			}
		} else if (player.getCityLevel() < ControlProperty.getInstance().getChatNormalCityLv()) {
			return;
		}

		String voiceId = req.getVoiceId();
		// 记录原始聊天内容
		logger.info("playerId:{} chatType:{} msg:{}", player.getId(), req.getChatType(), chatMsg);

		if (!GsApp.getInstance().getSilenceWordFilter().find(chatMsg).isEmpty()) {
			// ChatService.getInstance().pushChatMsg(player, "***", voiceId, voiceLength, type);
			sendSilentWord(type, chatMsg);
			return;
		}

//		// 联盟聊天模块关闭
//		if (type.equals(ChatType.CHAT_ALLIANCE)
//				&& SystemControler.getInstance().isSystemItemsClosed(ControlerModule.GUILD_CHAT)) {
//			sendError(protocol.getType(), Status.SysError.GUILD_CHAT_SYSTEM_CLOSED);
//			return;
//		}
		
		// TODO jason fix 联盟聊天权限检测
		if (type.equals(ChatType.CHAT_ALLIANCE)) {
			if(SystemControler.getInstance().isSystemItemsClosed(ControlerModule.GUILD_CHAT)){
				sendError(protocol.getType(), Status.SysError.GUILD_CHAT_SYSTEM_CLOSED);
				return;
			}
			if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.GUILD_CHAT_PERMISSION)) {
				sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
				return ;
			}
		}

		// 世界聊天关闭
		if (type.equals(ChatType.CHAT_WORLD)
				&& SystemControler.getInstance().isSystemItemsClosed(ControlerModule.WORLD_CHAT)) {
			sendError(protocol.getType(), Status.SysError.WORLD_CHAT_SYSTEM_CLOSED);
			return;
		}

		if (!checkPostChat(chatMsg, voice, type, protocol.getType())) {
			return;
		}

		// 防止刷屏
		if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
			String filterChatMsg = GsApp.getInstance().getWordFilter().filterWord(GameUtil.filterString(chatMsg));
			pushChatMsg(filterChatMsg, voiceId, voiceLength, type, currTime, false);
			return;
		}

		// 下面是安全sdk接入内容
		JSONObject json = new JSONObject();
		json.put("user_sign", WorldPointService.getInstance().getPlayerSignature(player.getId()));
		json.put("template_sign", 0);
		json.put("msg_type", 0);
		json.put("speech_original_voice_id", voiceId);
		
		MsgCategory category = MsgCategory.WORLD_CHAT;
		if (type == ChatType.CHAT_BROADCAST) {
			category = MsgCategory.WORLD_BROADCAST;
		} else if (type == ChatType.CHAT_ALLIANCE) {
			category = MsgCategory.CHAT_GUILD;
		}
		
		String filterMsg = GameUtil.filterString(chatMsg);
		long startTime = currTime;
		ChatMsgFilterCallbackData dataObject = new ChatMsgFilterCallbackData(voiceId, voiceLength, req.getChatType(), startTime);
		GameTssService.getInstance().wordUicChatFilter(player, filterMsg, 
				category.getNumber(), GameMsgCategory.WORLD_CHAT, 
				JSONObject.toJSONString(dataObject), json, protocol.getType());
	}

	@MessageHandler
	private void onChatMsgFilterFinish(ChatMsgFilterFinishMsg msg) {
		ChatMsgFilterCallbackData dataObject = JSONObject.parseObject(msg.getCallbackData(), ChatMsgFilterCallbackData.class);
		ChatType type = ChatType.valueOf(dataObject.getChatType());
		String filterChatMsg = msg.getMsgContent();
		// 文本恶意，只有自己可见
		if (msg.getResultCode() == UicMsgResultFlag.ILLEGAL) {
			sendSilentWord(type, filterChatMsg);
		} else {
			String voiceId = dataObject.getVoiceId();
			int voiceLength = dataObject.getVoiceLength();
			pushChatMsg(filterChatMsg, voiceId, voiceLength, type, dataObject.getStartTime(), true);
		}
	}

	private void pushChatMsg(String chatMsg, String voiceId, int voiceLength, ChatType type, long startTime, boolean open) {
		lastChatMsg = HawkTime.getMillisecond();
		if (type == Const.ChatType.CHAT_FUBEN || type == Const.ChatType.CHAT_FUBEN_TEAM
				|| type == Const.ChatType.CHAT_FUBEN_NATION) {
			if (player.getCYBORGState() == CYBORGState.GAMEING) {
				ICYBORGPlayer sender = CYBORGRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			} else if (player.getTBLYState() == TBLYState.GAMEING) {
				ITBLYPlayer sender = TBLYRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			} else if (player.getSwState() == SWState.GAMEING) {
				ISWPlayer sender = SWRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			} else if (player.getLmjyState()== PState.GAMEING) {
				ILMJYPlayer sender = LMJYRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			} else if (player.getDYZZState()== DYZZState.GAMEING) {
				IDYZZPlayer sender = DYZZRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			} else if (player.getYQZZState()== YQZZState.GAMEING) {
				IYQZZPlayer sender = YQZZRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			} else if (player.getXhjzState() == XHJZState.GAMEING) {
				IXHJZPlayer sender = XHJZRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			}else if (player.getFgylState() == FGYLState.GAMEING) {
				IFGYLPlayer sender = FGYLRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			}else if (player.getXQHXState() == XQHXState.GAMEING) {
				IXQHXPlayer sender = XQHXRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().sendChatMsg(sender, chatMsg, voiceId, voiceLength, type);
			}
			return;
		}

		ChatService.getInstance().pushChatMsg(player, chatMsg, voiceId, voiceLength, type);
	}

	/**
	 * 请求联盟聊天记录
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_MSG_CACHE_C_VALUE)
	private void onGuildMsgCache(HawkProtocol protocol) {
		HPChatMsgCacheReq cacheReq = protocol.parseProtocol(HPChatMsgCacheReq.getDefaultInstance());
		long minTime = cacheReq.getMsgMinTime();
		if (minTime == 0) {
			minTime = HawkApp.getInstance().getCurrentTime();
		}
		HPPushChat.Builder builder = ChatService.getInstance().getMsgCache(player.getGuildId(), player.getId(), minTime, Const.ChatType.CHAT_ALLIANCE);

		// 协议压缩
		HawkProtocol rspProto = HawkProtocol.valueOf(HP.code.ALLIANCE_MSG_CACHE_S_VALUE, builder);
		player.sendProtocol(rspProto);

	}

	/**
	 * 请求世界聊天记录
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WORLD_MSG_CACHE_C_VALUE)
	private void onWorldMsgCache(HawkProtocol protocol) {
		HPChatMsgCacheReq cacheReq = protocol.parseProtocol(HPChatMsgCacheReq.getDefaultInstance());
		long minTime = cacheReq.getMsgMinTime();
		if (minTime == 0) {
			minTime = HawkApp.getInstance().getCurrentTime();
		}
		HPPushChat.Builder builder = ChatService.getInstance().getMsgCache(player.getGuildId(), player.getId(), minTime, Const.ChatType.CHAT_WORLD);

		// 协议压缩
		HawkProtocol rspProto = HawkProtocol.valueOf(HP.code.WORLD_MSG_CACHE_S, builder);
		player.sendProtocol(rspProto);

	}

	/**
	 * 聊天检查
	 * 
	 * @param chatMsg
	 * @param chatType
	 * @return
	 */
	private boolean checkPostChat(String chatMsg, byte[] voice, ChatType chatType, int hpCode) {
		long currTime = HawkTime.getMillisecond();

		long silentTime = player.getEntity().getSilentTime();
		int chatTimeInterval = GameConstCfg.getInstance().getChatTimeInterval();
		// 禁言中···自己跟自己说话
		if (silentTime >= currTime || currTime - lastChatMsg < chatTimeInterval * 1000 || ChatService.getInstance().isSilent(player, chatType)) {

			if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
				chatMsg = GsApp.getInstance().getWordFilter().filterWord(GameUtil.filterString(chatMsg));
				sendSilentWord(chatType, chatMsg);
			} else {
				GameTssService.getInstance().wordUicChatFilter(player, GameUtil.filterString(chatMsg), 
						GameMsgCategory.SELF_CHAT, GameMsgCategory.SELF_CHAT, 
						String.valueOf(chatType.getNumber()), null, 0);
			}

			return false;
		}

		if (GameConstCfg.getInstance().getChatMsgMaxLen() < chatMsg.length()) {
			sendError(hpCode, Status.Error.CHAT_MSG_TOO_LONG);
			return false;
		}

		// 广播聊天
		if (chatType == Const.ChatType.CHAT_BROADCAST) {
			return broadcastConsume(hpCode);
		}

		return true;
	}

	@MessageHandler
	private boolean onSelfChatMsgFilterFinish(SelfChatMsgFilterFinishMsg msg) {
		String filterChatMsg = msg.getMsgContent();
		ChatType chatType = ChatType.valueOf(msg.getChatType());
		sendSilentWord(chatType, filterChatMsg);
		return true;
	}
	
	/**
	 * 发送仅对自己可见的发言内容
	 * @param chatType
	 * @param chatMsg
	 */
	private void sendSilentWord(ChatType chatType, String chatMsg) {
		LogUtil.logSecTalkFlow(player, null, LogMsgType.SLIENCE_WORD, "", chatMsg);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_CHAT_S_VALUE, genBuilder(chatType, chatMsg)));
	}

	/**
	 * 生成builder对象
	 * 
	 * @param chatType
	 * @param chatMsg
	 * @return
	 */
	private HPPushChat.Builder genBuilder(ChatType chatType, String chatMsg) {
		HPPushChat.Builder builder = HPPushChat.newBuilder();
		ChatMsg.Builder chatMsgInfo = ChatMsg.newBuilder();
		chatMsgInfo.setMsgTime(HawkTime.getMillisecond());
		chatMsgInfo.setType(chatType.getNumber());
		chatMsgInfo.setAllianceName(player.getGuildName() == null ? "" : player.getGuildName());
		chatMsgInfo.setAllianceId(player.getGuildId() == null ? "" : player.getGuildId());
		chatMsgInfo.setName(player.getName());
		chatMsgInfo.setPlayerId(player.getId());
		chatMsgInfo.setChatMsg(chatMsg);
		chatMsgInfo.setVip(player.getVipLevel());
		chatMsgInfo.setVipActive(player.getData().getVipActivated());
		chatMsgInfo.setIcon(player.getIcon());
		chatMsgInfo.setPfIcon(player.getPfIcon());
		chatMsgInfo.setGuildTag(GuildService.getInstance().getGuildTag(player.getGuildId()) == null ? "" : GuildService.getInstance().getGuildTag(player.getGuildId()));
		chatMsgInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		builder.addChatMsg(chatMsgInfo);
		return builder;
	}

	/**
	 * 广播聊天消耗
	 * 
	 * @param hpCode
	 * @return
	 */
	private boolean broadcastConsume(int hpCode) {
		ConsumeItems consumeItem = ConsumeItems.valueOf();
		Action action = Action.USE_CHAT_BROADCAST_ITEM;
		ShopCfg cfg = null;
		int itemCnt = player.getData().getItemNumByItemId(Const.ItemId.ITEM_CHAT_BROADCAST_VALUE);
		if (itemCnt == 0) {
			cfg = ItemService.getInstance().getShopCfgByItemId(Const.ItemId.ITEM_CHAT_BROADCAST_VALUE);
			if (cfg == null) {
				sendError(hpCode, Status.SysError.CONFIG_ERROR);
				return false;
			}

			consumeItem.addConsumeInfo(cfg.getPriceItemInfo(), true);
			if (!consumeItem.checkConsume(player, hpCode)) {
				return false;
			}

			action = Action.BUY_CHAT_BROADCAST_ITEM;
		} else {
			consumeItem.addItemConsume(Const.ItemId.ITEM_CHAT_BROADCAST_VALUE, 1);
		}

		if (consumeItem.checkConsume(player, hpCode)) {
			consumeItem.consumeAndPush(player, action);
		}
		return true;
	}

	/**
	 * 请求主播进入主播服务器
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ENTER_ANCHOR_SERVER_C_VALUE)
	private boolean onEnterAnchorServer(HawkProtocol protocol) {
		AnchorServerInfoGS.Builder builder = AnchorServerInfoGS.newBuilder();
		// 1.获取本服务器对应的主播服务器
		String serverId = GsConfig.getInstance().getServerId();
		String infoStr = RedisProxy.getInstance().getAnchorServerInfo(serverId);
		// 2.查看当前主播服务器，是否有主播正在开播
		if (infoStr == null) {
			builder.setServerip("");
			builder.setServerport(0);
			builder.setRoomID(0);
			builder.setToken("");
		} else {
			JSONObject infoObj = JSONObject.parseObject(infoStr);
			// 3-1.如果有，生成玩家的token，并将基本信息存入Redis，然后获取房间ID以及主播服务器的ip端口，返回给前端
			// 获取对应服务器的ip信息
			builder.setServerip(infoObj.getString(String.valueOf(ASInfoRedisKey.SERVERIP_VALUE)));
			builder.setServerport(infoObj.getIntValue(String.valueOf(ASInfoRedisKey.SERVERPORT_VALUE)));
			builder.setRoomID(infoObj.getIntValue(String.valueOf(ASInfoRedisKey.ROOMID_VALUE)));

			// 生成玩家的token和基础信息
			String jsonStr = player.toAnchorJsonStr();
			StringBuffer sb = new StringBuffer();
			sb.append(player.getId()).append(jsonStr).append(HawkApp.getInstance().getCurrentTime());
			String token = DigestUtils.md5Hex(sb.toString());
			RedisProxy.getInstance().setPlayerAnchorToken(token, jsonStr);

			builder.setToken(token);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ENTER_ANCHOR_SERVER_S_VALUE, builder));
		return true;
	}

	/**
	 * 请求主播预告列表
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.GET_ANCHOR_PREVIEW_LIST_C_VALUE)
	private boolean onGetAnchorPreviewList(HawkProtocol protocol) {
		// 1.获取本服务器对应的主播服务器
		String serverId = GsConfig.getInstance().getServerId();
		AnchorPreviewListGS.Builder builder = AnchorPreviewListGS.newBuilder();
		// 3-2.如果没有，获取当前主播服务器的对应的预告列表, 发送给前端
		String previewInfo = RedisProxy.getInstance().getAnchorPreview(serverId);
		if (previewInfo != null) {
			JSONArray previewInfoObj = JSONArray.parseArray(previewInfo);
			AnchorPreviewInfoGS.Builder previewBuilder = AnchorPreviewInfoGS.newBuilder();
			for (Object object : previewInfoObj) {
				JSONObject jsonObj = (JSONObject) object;
				String anchorId = jsonObj.getString(String.valueOf(AnchorPreviewInfoRedisKey.ANCHORID_VALUE));
				previewBuilder.setAnchorId(anchorId);
				previewBuilder.setStartTime(jsonObj.getIntValue(String.valueOf(AnchorPreviewInfoRedisKey.STARTTIME_VALUE)));
				previewBuilder.setEndTime(jsonObj.getIntValue(String.valueOf(AnchorPreviewInfoRedisKey.ENDTIME_VALUE)));

				Boolean sex = (Boolean) jsonObj.get(String.valueOf(AnchorPreviewInfoRedisKey.SEX_VALUE));
				if (sex != null) {
					previewBuilder.setSex(sex);
				}
				String icon = (String) jsonObj.get(String.valueOf(AnchorPreviewInfoRedisKey.HD_ICON_VALUE));
				if (icon != null) {
					previewBuilder.setIcon(icon);
				}
				String label = (String) jsonObj.get(String.valueOf(AnchorPreviewInfoRedisKey.LABEL_VALUE));
				if (label != null) {
					previewBuilder.setLabel(label);
				}
				String name = (String) jsonObj.get(String.valueOf(AnchorPreviewInfoRedisKey.NAME_VALUE));
				if (name != null) {
					previewBuilder.setName(name);
				}
				// 是否已经关注
				previewBuilder.setIsFollow(RedisProxy.getInstance().checkAnchorFollow(player.getId(), anchorId));

				builder.addPreviewInfos(previewBuilder);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_ANCHOR_PREVIEW_LIST_S, builder));
		return true;
	}

	/**
	 * 给主播刷礼物
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SEND_ANCHOR_GIFT_C_VALUE)
	private boolean onSendGiftForAnchor(HawkProtocol protocol) {
		SendGiftForAnchor cmd = protocol.parseProtocol(SendGiftForAnchor.getDefaultInstance());
		String anchorId = cmd.getAnchorId();
		int giftId = cmd.getGiftId();
		int num = cmd.getGiftNum();
		if (HawkOSOperator.isEmptyString(anchorId) || num <= 0) {
			HawkLog.errPrintln("anchor id is null or num is 0, anchorId:{}, num:{}", anchorId, num);
			return false;
		}
		// 检查主播是否真实存在
		if (!RedisProxy.getInstance().checkAnchorInfo(anchorId)) {
			HawkLog.errPrintln("can not find anchor in this server, anchorId:{}", anchorId);
			return false;
		}
		AnchorGiftCfg anchorGiftCfg = HawkConfigManager.getInstance().getConfigByKey(AnchorGiftCfg.class, giftId);
		if (anchorGiftCfg == null) {
			HawkLog.errPrintln("can`t find anchor gift config, configId : {}", giftId);
			return false;
		}
		ItemInfo info = anchorGiftCfg.getItemInfo();
		info.setCount(info.getCount() * num);
		// 先扣货币
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(info, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			HawkLog.errPrintln("SendGiftForAnchor playerId {}, cmd:{}", player.getId(), JsonFormat.printToString(cmd));
			return false;
		}
		consume.consumeAndPush(player, Action.SEND_GIFT_FOR_ANCHOR);
		// 将结果存入redis
		JSONObject giftInfo = new JSONObject();
		giftInfo.put("playerInfo", player.toAnchorJsonObj());
		giftInfo.put("giftId", giftId);
		giftInfo.put("glamour", anchorGiftCfg.getGlamour());
		giftInfo.put("score", anchorGiftCfg.getScore());
		giftInfo.put("num", num);
		RedisProxy.getInstance().addAnchorGiftInfo(anchorId, giftInfo.toJSONString());

		player.responseSuccess(HP.code.SEND_ANCHOR_GIFT_C_VALUE);
		
		LogUtil.logSendGiftForAnchorFlow(player, anchorId, giftId);
		
		return true;
	}

	/**
	 * 关注主播
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ADD_ANCHOR_FOLLOW_VALUE)
	public boolean onAddAnchorFollow(HawkProtocol protocol) {
		AddAnchorFollow cmd = protocol.parseProtocol(AddAnchorFollow.getDefaultInstance());
		String anchorId = cmd.getAnchorId();
		if (HawkOSOperator.isEmptyString(anchorId)) {
			return false;
		}
		// 检查主播是否真实存在
		if (!RedisProxy.getInstance().checkAnchorInfo(anchorId)) {
			HawkLog.errPrintln("can not find anchor in this server, anchorId:{}", anchorId);
			return false;
		}
		// 为玩家添加关注
		RedisProxy.getInstance().addUserAnchorFollow(player.getId(), anchorId);
		// 为主播增加粉丝(暂时不做)

		player.responseSuccess(HP.code.ADD_ANCHOR_FOLLOW_VALUE);
		return true;
	}

	/**
	 * 取消关注主播
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.DEL_ANCHOR_FOLLOW_VALUE)
	public boolean onDelAnchorFollow(HawkProtocol protocol) {
		AddAnchorFollow cmd = protocol.parseProtocol(AddAnchorFollow.getDefaultInstance());
		String anchorId = cmd.getAnchorId();
		if (HawkOSOperator.isEmptyString(anchorId)) {
			return false;
		}
		// 检查主播是否真实存在
		if (!RedisProxy.getInstance().checkAnchorInfo(anchorId)) {
			HawkLog.errPrintln("can not find anchor in this server, anchorId:{}", anchorId);
			return false;
		}
		// 为玩家取消关注
		RedisProxy.getInstance().delUserAnchorFollow(player.getId(), anchorId);
		// 为主播减少粉丝(暂时不做)

		player.responseSuccess(HP.code.DEL_ANCHOR_FOLLOW_VALUE);
		return true;
	}
	
	
	/*
	 * 玩家请求快速答疑
	 * */
	@ProtocolHandler(code = HP.code.PLAYER_CHAT_OLQUEST_REQ_VALUE)
	private void onPlayerReqOLQuestion(HawkProtocol protocol) {
		HPChatOLQuestionReq cmd = protocol.parseProtocol(HPChatOLQuestionReq.getDefaultInstance());
		if(cmd.getId() != 0){
			LocalRedis.getInstance().updateOnlineQuestion(cmd.getId());
		}
	}
	
	/*
	 * 玩家请求快速答疑排行榜
	 * */
	@ProtocolHandler(code = HP.code.PLAYER_CHAT_OLQUEST_LIST_REQ_VALUE)
	private void onPlayerReqOLQuestionRank(HawkProtocol protocol) {
		Set<String> retset = LocalRedis.getInstance().getOnlineQuestionRank(0, 5);
		HPChatOLQuestionListResp.Builder resp =  HPChatOLQuestionListResp.newBuilder();
		for(String s : retset){
			resp.addIds( Integer.parseInt(s));
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CHAT_OLQUEST_LIST_PUSH_VALUE, resp));
	}
	
	@ProtocolHandler(code = TestFight.codeTestFitht.GUILD_MEMER_LIST_C_VALUE)
	private void tfGuildInfoReq(HawkProtocol protocol) {
		TFGuildInfoReq req = protocol.parseProtocol(TFGuildInfoReq.getDefaultInstance());
		TFGuildInfo.Builder guildA = TFGuildInfo.newBuilder();
		TFGuildInfo.Builder guildB = TFGuildInfo.newBuilder();
		for (String guildId : GuildService.getInstance().getGuildIds()) {
			String guildName = GuildService.getInstance().getGuildName(guildId);
			if (req.getGuildNameA().equals(guildName)) {
				guildA.setGuildId(guildId).setGuiName(guildName);
				fillGuildmember(guildA, guildId);
			}
			if (req.getGuildNameB().equals(guildName)) {
				guildB.setGuildId(guildId).setGuiName(guildName);
				fillGuildmember(guildB, guildId);
			}
		}
		TFGuildInfoResp.Builder resp = TFGuildInfoResp.newBuilder();
		resp.setGuildA(guildA);
		resp.setGuildB(guildB);

		player.sendProtocol(HawkProtocol.valueOf(TestFight.codeTestFitht.GUILD_MEMER_LIST_S_VALUE, resp));

	}

	private void fillGuildmember(TFGuildInfo.Builder guildA, String guildId) {
		Collection<String> guildMembers = GuildService.getInstance().getGuildMembers(guildId);
		List<Player> guildPlayers = new ArrayList<>();
		for (String playerId : guildMembers) {
			guildPlayers.add(GlobalData.getInstance().makesurePlayer(playerId));
		}
		Collections.sort(guildPlayers, new Comparator<Player>() {
			@Override
			public int compare(Player o1, Player o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (Player guildMember : guildPlayers) {
			PlayerPresetMarchInfo.Builder infos = GameUtil.makeMarchPresetBuilder(guildMember.getId());
			PlayerSnapshotPB cominfo = BuilderUtil.buildSnapshotData(guildMember);
			TFFighter fiter = TFFighter.newBuilder().setPlayer(cominfo).setPreset(infos).build();
			guildA.addGuildMesbers(fiter);
		}
	}

	@ProtocolHandler(code = TestFight.codeTestFitht.GUILD_TEST_FIGHT_VALUE)
	private void tfTestFightReq(HawkProtocol protocol) {
		TFTestFightReq req = protocol.parseProtocol(TFTestFightReq.getDefaultInstance());
		int count = Math.min(10, req.getCount());
		for (int i = 0; i < count; i++) {
			List<Player> atkPlayers = new ArrayList<>();
			// 防守方玩家
			List<Player> defPlayers = new ArrayList<>();
			// 进攻方行军
			List<IWorldMarch> atkMarchs = new ArrayList<>();
			// 防守方行军
			List<IWorldMarch> defMarchs = new ArrayList<>();

			for (TFFightUnit player : req.getAtksList()) {
				buildMarch(player, atkPlayers, atkMarchs);
			}

			for (TFFightUnit player : req.getDefsList()) {
				buildMarch(player, defPlayers, defMarchs);
			}

			// 战斗数据输入
			PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_PRESIDENT, 0, atkPlayers, defPlayers, atkMarchs, defMarchs,
					BattleSkillType.BATTLE_SKILL_NONE);
			battleIncome.getBattle().setDuntype(DungeonMailType.TBLY);
			if (req.getJueduo()) {
				try {
					Field f1 = HawkOSOperator.getClassField(battleIncome.getDefCalcParames(), "duel");
					f1.set(battleIncome.getDefCalcParames(), true);
					Field f2 = HawkOSOperator.getClassField(battleIncome.getAtkCalcParames(), "duel");
					f2.set(battleIncome.getAtkCalcParames(), true);

					Field f3 = HawkOSOperator.getClassField(battleIncome.getDefCalcParames(), "decDieBecomeInjury");
					f3.set(battleIncome.getDefCalcParames(), 1000000);

					Field f4 = HawkOSOperator.getClassField(battleIncome.getAtkCalcParames(), "decDieBecomeInjury");
					f4.set(battleIncome.getAtkCalcParames(), 1000000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 战斗数据输出
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
			FightMailService.getInstance().sendFightMail(WorldPointType.KING_PALACE_VALUE, battleIncome, battleOutcome, null);

		}
		player.responseSuccess(protocol.getType());
	}

	private static IWorldMarch buildMarch(TFFightUnit battleUnit, List<Player> atkPlayers, List<IWorldMarch> atkMarchs) {
		Player fighter = GlobalData.getInstance().makesurePlayer(battleUnit.getPlayerId());
		PlayerPresetMarchInfo.Builder infos = GameUtil.makeMarchPresetBuilder(fighter.getId());
		Optional<PresetMarchInfo> preSetInfo = infos.getMarchInfosList().stream().filter(pre -> pre.getIdx() == battleUnit.getPresetmarch()).findAny();
		if (!preSetInfo.isPresent()) {
			return null;
		}
		PresetMarchInfo info = preSetInfo.get();
		List<ArmyInfo> armys = new ArrayList<>();
		for (ArmySoldierPB arp : info.getArmyList()) {
			armys.add(new ArmyInfo(arp.getArmyId(), arp.getCount()));
		}

		TemporaryMarch atkMarch = new TemporaryMarch();
		atkMarch.setArmys(armys);
		atkMarch.setPlayer(fighter);
		atkMarch.getMarchEntity().setArmourSuit(info.getArmourSuit().getNumber());
		atkMarch.getMarchEntity().setMechacoreSuit(info.getMechacoreSuit().getNumber());
		atkMarch.getMarchEntity().setHeroIdList(info.getHeroIdsList());
		atkMarch.getMarchEntity().setSuperSoldierId(info.getSuperSoldierId());
		atkMarch.setHeros(fighter.getHeroByCfgId(info.getHeroIdsList()));
		atkMarch.getMarchEntity().setSuperLab(info.getSuperLab());
		atkMarch.getMarchEntity().setTalentType(info.getTalentType().getNumber());
		atkMarch.getMarchEntity().setManhattanAtkSwId(info.getManhattan().getManhattanAtkSwId());
		atkMarch.getMarchEntity().setManhattanDefSwId(info.getManhattan().getManhattanDefSwId());
		atkPlayers.add(atkMarch.getPlayer());
		atkMarchs.add(atkMarch);
		return atkMarch;
	}
}
