package com.hawk.game.service.chat;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.app.HawkAppObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ArrayListMultimap;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil.Switch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Anchor.ASInfoRedisKey;
import com.hawk.game.protocol.Anchor.AnchorInfoLiteGS;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Chat.HPChatState;
import com.hawk.game.protocol.Chat.HPPushChat;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.Dress.DressEditData;
import com.hawk.game.protocol.Dress.DressEditType;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.IDIPNotice;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Status.IdipMsgCode;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.Predicates;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.LogConst.SnsType;
import com.sun.istack.internal.NotNull;

/**
 * 聊天管理器
 * 
 * @author zdz
 *
 */
public class ChatService extends HawkAppObj {

	private static Logger logger = LoggerFactory.getLogger("Server");

	private final static Predicate<ChatMsg> Is_BroadcastMsg = Predicates.of((ChatMsg msgInfo) -> msgInfo.getType() == Const.ChatType.CHAT_WORLD_VALUE)
			.or(msgInfo -> msgInfo.getType() == Const.ChatType.CHAT_BROADCAST_VALUE)
			.or(msgInfo -> msgInfo.getType() == Const.ChatType.SPECIAL_BROADCAST_VALUE)
			.or(msgInfo -> msgInfo.getType() == Const.ChatType.SYS_BROADCAST_VALUE)
			.or(msgInfo -> msgInfo.getType() == ChatType.WORLD_HREF_VALUE);

	private final static Predicate<ChatMsg> Is_GuildMsg = Predicates.of((ChatMsg msgInfo) -> msgInfo.getType() == Const.ChatType.CHAT_ALLIANCE_VALUE)
			.or(msgInfo -> msgInfo.getType() == Const.ChatType.GUILD_HREF_VALUE)
			.and(msgInfo -> StringUtils.isNotEmpty(msgInfo.getAllianceId()));

	/**
	 * 聊天包队列
	 */
	private volatile ConcurrentLinkedQueue<ChatMsg> chatMsgQueue;

	/**
	 * 当前主播ID
	 */
	private JSONObject anchorJson;

	/**
	 * 全局实例对象
	 */
	private static ChatService instance = null;

	/**
	 * 全局实例对象
	 * 
	 * @return
	 */
	public static ChatService getInstance() {
		return instance;
	}

	public boolean isWorldMsg(ChatMsg msg) {
		return Is_BroadcastMsg.test(msg);
	}

	public boolean isGuildMsg(ChatMsg msg) {
		return Is_GuildMsg.test(msg);
	}

	public void setCurrentAnchorId(JSONObject anchorJson) {
		this.anchorJson = anchorJson;
	}

	/**
	 * 构造函数
	 * 
	 * @param xid
	 */
	public ChatService(HawkXID xid) {
		super(xid);
		if (instance == null) {
			instance = this;
			chatMsgQueue = new ConcurrentLinkedQueue<ChatMsg>();
		}
	}

	public int getLiveAnchorId() {
		int anchorId = 0;
		// 检查当前是否有主播, 推送通知
		String serverId = GsConfig.getInstance().getServerId();
		String infoStr = RedisProxy.getInstance().getAnchorServerInfo(serverId);
		// 2.查看当前主播服务器，是否有主播正在开播
		if (infoStr != null) {
			JSONObject infoObj = JSONObject.parseObject(infoStr);
			int roomId = infoObj.getIntValue(String.valueOf(ASInfoRedisKey.ROOMID_VALUE));
			String anchorServerId = infoObj.getString(String.valueOf(ASInfoRedisKey.ASERVERID_VALUE));
			// 根据roomId 判断当前是否有主播在直播
			String roomInfo = RedisProxy.getInstance().getAnchorRoomInfo(anchorServerId, String.valueOf(roomId));
			JSONObject json = JSONObject.parseObject(roomInfo);
			if(Objects.isNull(json)){
				return anchorId;
			}
			JSONArray arr = json.getJSONArray("anchorIds");

			if (arr != null && !arr.isEmpty()) {
				anchorId = arr.getIntValue(0);
			}
		}
		return anchorId;
	}

	public int getCurrentAnchorId() {
		if(Objects.isNull(anchorJson)){
			return 0;
		}
		return anchorJson.getIntValue("id");
	}

	/**
	 * 推送主播信息
	 * 
	 * @param player
	 * @param anchorId
	 */
	public void sendAnchorInfo(Player player, JSONObject anchorJson) {
		AnchorInfoLiteGS.Builder builder = AnchorInfoLiteGS.newBuilder();
		if (Objects.nonNull(anchorJson)) {
			builder.setHasAnchor(true);
			builder.setAnchorId(anchorJson.getString("id"));

			builder.setName(anchorJson.getString("name"));
			builder.setIcon(anchorJson.getString("icon"));
		} else {
			builder.setHasAnchor(false);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ANCHOR_INFO_LITE_S_VALUE, builder));
	}

	/**
	 * 给玩家推送主播上线信息
	 * 
	 * @param anchorId
	 */
	public void pushMsgToFans(@NotNull JSONObject anchorJson) {
		Objects.nonNull(anchorJson);
		String name = (String) anchorJson.get("name");
		Set<String> allFans = RedisProxy.getInstance().getAllFollowsPlayer(anchorJson.getString("id"));
		for (String playerId : allFans) {
			PushService.getInstance().pushMsg(playerId, PushMsgType.ANCHOR_ONLINE_VALUE, name);
			logger.info("push anchor info to player : {}, anchor : {}", playerId, name);
		}
	}

	/**
	 * 直接推送主播信息
	 * 
	 * @param player
	 */
	public void directSendAnchorInfo(Player player) {
		sendAnchorInfo(player, anchorJson);
	}

	/**
	 * 推送聊天
	 * 
	 * @param player
	 * @param chatMsg
	 * @param type
	 */
	public void pushChatMsg(Player player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		try {
			ChatMsg chatMsgInfo = createMsgObj(player)
					.setType(type.getNumber())
					.setChatMsg(chatMsg)
					.setVoiceId(voiceId)
					.setVoiceLength(voiceLength)
					.build();
			chatMsgQueue.add(chatMsgInfo);

			logChatInfo(player, type, chatMsg);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 清除玩家聊天
	 */
	public void clearPlayerChat(Player player) {
		IDIPNotice.Builder notice = IDIPNotice.newBuilder();
		notice.setType(NoticeType.CLEAR_CHAT_MSG);
		notice.setMode(NoticeMode.NONE);
		notice.setPlayerId(player.getId());
		// 广播需要清除消息的玩家id
		for (Player reciver : GlobalData.getInstance().getOnlinePlayers()) {
			reciver.sendProtocol(HawkProtocol.valueOf(HP.code.IDIP_NOTICE_SYNC_S_VALUE, notice));
		}
		GlobalData.getInstance().clearPlayerChat(player);
	}

	/**
	 * 记录聊天相关日志
	 * 
	 * @param player
	 * @param type
	 * @param chatMsg
	 */
	private void logChatInfo(Player player, ChatType type, String chatMsg) {
		if (GameUtil.isTlogPuidControlled(player.getOpenId())) {
			return;
		}

		int msgType = LogMsgType.WORLD;
		int snsType = SnsType.WORLD_CHAT;
		switch (type) {
		case CHAT_ALLIANCE:
			msgType = LogMsgType.GUILD;
			snsType = SnsType.GUILD_CHAT;
			break;

		case CHAT_BROADCAST:
			msgType = LogMsgType.WORLD_BROADCAST;
			snsType = SnsType.WORLD_BROADCAST;
			break;
		default:
			break;
		}

		LogUtil.logChatInfo(player, "", snsType, chatMsg, 0);
		LogUtil.logSecTalkFlow(player, null, msgType, "", chatMsg);
	}

	private class ChatMsgTask extends HawkTask {
		@Override
		public Object run() {
			if (chatMsgQueue.isEmpty()) {
				return null;
			}
			ConcurrentLinkedQueue<ChatMsg> sendMsgList = chatMsgQueue;
			chatMsgQueue = new ConcurrentLinkedQueue<ChatMsg>();

			sendMsgList.forEach(GlobalData.getInstance()::addChatMsg);

			// 世界聊天和广播

			List<ChatMsg> msgList = sendMsgList.stream()
					.filter(msg -> ChatService.getInstance().isWorldMsg(msg))
					.sorted(Comparator.comparingLong(ChatMsg::getMsgTime).reversed())
					.collect(Collectors.toList());

			if (!msgList.isEmpty()) {
				broadcastChatMsg(msgList);
			}

			// 公会聊天
			ArrayListMultimap<String, ChatMsg> guildMsgMap = ArrayListMultimap.create();

			sendMsgList.stream()
					.filter(msg -> ChatService.getInstance().isGuildMsg(msg))
					.sorted(Comparator.comparingLong(ChatMsg::getMsgTime).reversed())
					.forEach(msgInfo -> guildMsgMap.put(msgInfo.getAllianceId(), msgInfo));

			guildMsgMap.asMap().forEach((gid, msg) -> broadcastAllianceChatMsg((List<ChatMsg>) msg, gid));

			return null;
		}
	}

	/**
	 * 直接添加广播包
	 * 
	 * @param chatMsgInfo
	 */
	private void postBroadcast(ChatMsg chatMsgInfo) {
		try {
			chatMsgQueue.add(chatMsgInfo);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 直接添加广播
	 * use addWorldBroadcastMsg(ChatParames parames) instead
	 * @param chatType
	 *            广播类型 是否有超链接
	 * @param key
	 *            notice.xml 配置表id(GsConst.SysBroadcast.)
	 * @param parms
	 */
	@Deprecated
	public void addWorldBroadcastMsg(ChatType chatType, Const.NoticeCfgId key, Player player, Object... parms) {
		addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(chatType).setKey(key).setPlayer(player).addParms(parms).build());
	}

	/**
	 * 直接添加广播
	 */
	public void addWorldBroadcastMsg(ChatParames parames) {
		try {
			postBroadcast(parames.toPBMsg());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 上一次发送聊天消息 */
	private long lastPushMsg;

	@Override
	public boolean onTick() {
		try {
			long millisecond = HawkTime.getMillisecond();
			if (!chatMsgQueue.isEmpty() && millisecond - lastPushMsg > GsConfig.getInstance().getChatMsgTickPeriod()) {
				lastPushMsg = millisecond;
				HawkTaskManager.getInstance().postExtraTask(new ChatMsgTask());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	/**
	 * 广播聊天
	 * 
	 * @param builder
	 */
	private void broadcastChatMsg(List<ChatMsg> msgList) {
		Set<Player> onLinePlayers = GlobalData.getInstance().getOnlinePlayers();
		sendChatMsg(msgList, onLinePlayers);
	}

	/**
	 * 联盟广播聊天
	 * 
	 * @param builder
	 */
	private void broadcastAllianceChatMsg(List<ChatMsg> msgList, String guildId) {
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
		Set<Player> onLinePlayers = memberIds.stream().map(GlobalData.getInstance()::getActivePlayer).collect(Collectors.toSet());
		sendChatMsg(msgList, onLinePlayers);
	}

	public void sendChatMsg(List<ChatMsg> msgList, Set<? extends Player> onLinePlayers) {
		if (msgList.isEmpty() || onLinePlayers.isEmpty()) {
			return;
		}
		RelationService relationService = RelationService.getInstance();
		final int msgCount = msgList.size();
		HPPushChat.Builder msgbuilder = HPPushChat.newBuilder().addAllChatMsg(msgList).setMsgCount(msgCount);
		HPPushChat.Builder sampleMsgBuilder = HPPushChat.newBuilder().addChatMsg(msgList.get(0)).setMsgCount(msgCount);

		for (Player receiver : onLinePlayers) {
			if (receiver == null) {
				continue;
			}
			HPPushChat.Builder sendBuilder = receiver.getChatState() == HPChatState.NOT_CHAT ? sampleMsgBuilder : msgbuilder;

			if (relationService.hasBlacklist(receiver.getId())) {
				List<ChatMsg> allowed = sendBuilder.getChatMsgList().stream()
						.filter(o -> !relationService.isBlacklist(receiver.getId(), o.getPlayerId()))
						.collect(Collectors.toList());
				if (allowed.isEmpty()) {
					continue;
				}
				int blackCount = sendBuilder.getChatMsgCount() - allowed.size();
				if (blackCount == 0) {
					receiver.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_CHAT_S_VALUE, sendBuilder));
				} else {
					receiver.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_CHAT_S_VALUE, HPPushChat.newBuilder().addAllChatMsg(allowed).setMsgCount(msgCount - blackCount)));
				}
			} else {
				// 没有黑名单,直接发
				receiver.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_CHAT_S_VALUE, sendBuilder));
			}
		}
	}

	/**
	 * 获取聊天记录
	 * 
	 * @param guildId
	 * @param msgMinTime
	 * @param chatType
	 * @return
	 */
	public HPPushChat.Builder getMsgCache(String guildId, String playerId, long msgMinTime, Const.ChatType chatType) {
		try {
			List<ChatMsg> msgs = GlobalData.getInstance().getChatMsgCache(guildId, playerId, msgMinTime, chatType);

			HPPushChat.Builder builder = HPPushChat.newBuilder();
			builder.addAllChatMsg(msgs);
			builder.setMsgCount(msgs.size());
			return builder;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HPPushChat.newBuilder();
	}

	/**
	 * 构建聊天对象
	 * 
	 * @param player
	 * @param type
	 * @return
	 */
	public ChatMsg.Builder createMsgObj(Player player) {
		try {
			ChatMsg.Builder chatMsgInfo = ChatMsg.newBuilder();
			chatMsgInfo.setMsgId(HawkUUIDGenerator.genUUID());
			chatMsgInfo.setMsgTime(HawkTime.getMillisecond());
			chatMsgInfo.setPlayerId(player.getId());
			chatMsgInfo.setName(player.getName());
			chatMsgInfo.setOffice(GameUtil.getOfficerId(player.getId()));
			if (player.hasGuild()) {
				chatMsgInfo.setAllianceName(player.getGuildName());
				chatMsgInfo.setAllianceId(player.getGuildId());
				chatMsgInfo.setGuildTag(GuildService.getInstance().getGuildTag(player.getGuildId()) == null ? "" : GuildService.getInstance().getGuildTag(player.getGuildId()));
			}

			chatMsgInfo.setVip(player.getShowVIPLevel());
			chatMsgInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
			chatMsgInfo.setVipActive(player.getData().getVipActivated());
			chatMsgInfo.setIcon(player.getIcon());
			chatMsgInfo.setPfIcon(player.getPfIcon());
			
			DressItem titleDress = WorldPointService.getInstance().getShowDress(player.getId(), DressType.TITLE_VALUE);
			if (titleDress != null) {
				chatMsgInfo.setDressTitle(titleDress.getModelType());
			} else {
				chatMsgInfo.setDressTitle(0);
			}

			chatMsgInfo.setDressTitleType(WorldPointService.getInstance().getDressTitleType(player.getId()));
			chatMsgInfo.setServerId(player.getMainServerId());
			
			DressEditData.Builder collegDressEdit = DressEditData.newBuilder();
			collegDressEdit.setType(DressEditType.COLLEGE_SHOW);
			String collegeName = WorldPointService.getInstance().getCollegeNameShow(player.getId());
			if(!HawkOSOperator.isEmptyString(collegeName)){
				collegDressEdit.setData(collegeName);
			}
			chatMsgInfo.addDressEditData(collegDressEdit);
			
			return chatMsgInfo;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return ChatMsg.newBuilder();
	}

	/**
	 * 是否被禁言
	 * 
	 * @param playerName
	 *            : 玩家名称
	 * @param chatType
	 *            : 禁言频道（参加Const.ChatType）
	 */
	public boolean isSilent(Player player, ChatType chatType) {
		try {
			// 获取禁言数据
			JSONObject json = player.getData().getPresidentSilentInfo();
			// 没有被禁言
			if (json == null) {
				return false;
			}
			// 在禁言期内
			long curTime = HawkTime.getMillisecond();
			if (chatType.getNumber() == json.getIntValue("chatType") &&
					curTime > json.getLongValue("startTime") &&
					curTime < json.getLongValue("endTime")) {
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 发送禁言提示
	 * 
	 * @param player
	 * @param silentTime
	 */
	public void sendBanMsgNotice(Player player, long silentTime) {
		if (silentTime <= HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, 0, IdipMsgCode.IDIP_BAN_MSG_RELEASE_VALUE);
			return;
		}

		String msg = "";
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
		if (banInfo != null) {
			msg = banInfo.getBanMsg();
		}

		player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, silentTime, msg);
		
		clearPlayerChat(player);
	}

	/**
	 * 聊天系统控制通知
	 * 
	 * @param noticeType
	 */
	public void chatSystemControlNotice(NoticeType noticeType, int switchVal) {
		IDIPNotice.Builder notice = IDIPNotice.newBuilder();
		notice.setType(noticeType);
		notice.setMode(NoticeMode.NOTICE_MSG);
		notice.setClosed(switchVal == Switch.OFF);

		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				for (Player reciver : GlobalData.getInstance().getOnlinePlayers()) {
					reciver.sendProtocol(HawkProtocol.valueOf(HP.code.IDIP_NOTICE_SYNC_S_VALUE, notice));
				}
				return null;
			}
		});
	}

	/**
	 * 聊天系统控制通知
	 * 
	 * @param player
	 * @param noticeType
	 */
	public void chatSystemClosedNotice(Player player, NoticeType noticeType) {
		IDIPNotice.Builder notice = IDIPNotice.newBuilder();
		notice.setType(noticeType);
		notice.setMode(NoticeMode.NOTICE_MSG);
		notice.setClosed(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.IDIP_NOTICE_SYNC_S_VALUE, notice));
	}
}
