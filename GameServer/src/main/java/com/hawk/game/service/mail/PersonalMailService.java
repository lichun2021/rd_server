package com.hawk.game.service.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.common.base.Joiner;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.GsConfig;
import com.hawk.game.config.PrivateSettingOptionCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.invoker.SendGuildMailMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.HPNewMailRes;
import com.hawk.game.protocol.Mail.HPPushChatRoomMsgRes;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.RedisMail.ChatData;
import com.hawk.game.protocol.RedisMail.ChatRoomData;
import com.hawk.game.protocol.RedisMail.ChatType;
import com.hawk.game.protocol.RedisMail.MemberData;
import com.hawk.game.protocol.RedisMail.PlayerChatRoom;
import com.hawk.game.service.MailService;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.LogConst.SnsType;

/**
 * 私人邮件服务类---聊天室
 * 
 * @author Nannan.Gao
 * @date 2016-11-30 18:28:06
 */
public class PersonalMailService {

	private static final PersonalMailService instance = new PersonalMailService();
	private static final String IS1V1MARK = "1C1";
	public static final String NOTICE_HEADER = "10nfUCKy@1#";

	public static PersonalMailService getInstance() {

		return instance;
	}

	/**
	 * 发送玩家聊天信息
	 * 
	 * @param chatRoomId
	 * @param toPlayerId
	 * @param fromPlayerId
	 * @param message
	 * @param joinTime
	 * @return
	 */
	public boolean sendChat(String chatRoomId, String toPlayerId, String fromPlayerId, ChatData chatData, long joinTime) {
		localChat(chatRoomId, toPlayerId, fromPlayerId, chatData, joinTime);
		return true;
	}

	/**
	 * 发送本服玩家聊天
	 * 
	 * @param chatRoomId
	 * @param toPlayerId
	 * @param fromPlayerId
	 * @param message
	 * @param joinTime
	 * @return
	 * @throws ParseException
	 */
	private boolean localChat(final String roomId, String toPlayerId, String fromPlayerId, ChatData chatData, long joinTime) {
		if (chatData.getMessage().length() == 0 && !Objects.equals(toPlayerId, fromPlayerId)) {
			return true;
		}
		Player toplayer = GlobalData.getInstance().makesurePlayer(toPlayerId);
		if(toplayer == null){ // 夸服玩家走掉了
			return true;
		}
		String message = chatData.getMessage();
		Optional<PlayerChatRoom.Builder> roomBuilderOP = LocalRedis.getInstance().getPlayerChatRoom(toPlayerId, roomId);
		if (roomBuilderOP.isPresent()) {
			PlayerChatRoom.Builder roomBuilder = roomBuilderOP.get();
			if (!Objects.equals(fromPlayerId, toPlayerId)) {
				roomBuilder.setStatus(MailStatus.NOT_READ_VALUE);
			}
			roomBuilder.setLastMsg(HawkTime.getMillisecond());
			LocalRedis.getInstance().saveOrUpdatePlayerChatRoom(toPlayerId, roomBuilder);
		} else {
			if (joinTime == 0L) {
				return false;
			}
			// 没找到聊天室那就创建
			ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
			if(Objects.equals(fromPlayerId, toplayer.getId()) && dataBuilder.getChatType() == ChatType.P2P && message.startsWith(SendGuildMailMsgInvoker.GMSG1V1)){
				// 占位
			}else{
				if (checkPrivateSetting(fromPlayerId, toplayer)) {
					createChatRoom(roomId, fromPlayerId, joinTime, toplayer, dataBuilder);
				}
			}
			return true;
		}

		// 玩家不在线，不推送消息
		if (!toplayer.isActiveOnline()) {
			return true;
		}
		
		// 好友 23 陌生人32特殊处理
		if (!checkPrivateSetting(fromPlayerId, toplayer)) {
			return true;
		}

		// 推送房间聊天消息
		HPPushChatRoomMsgRes.Builder pushBuilder = HPPushChatRoomMsgRes.newBuilder();
		pushBuilder.setId(roomId);
		if (chatData.hasVoiceId()) { // 语音
			pushBuilder.setVoiceId(chatData.getVoiceId()).setVoiceLength(chatData.getVoiceLength());
		}

		if (!HawkOSOperator.isEmptyString(fromPlayerId)) {
			pushBuilder.setMsg(fromPlayerId + ":" + message);
			String fromName = GlobalData.getInstance().getPlayerNameById(fromPlayerId);
			pushBuilder.setPlayerName(fromName);
		} else {
			pushBuilder.setMsg(message);
		}

		pushBuilder.setMsgTime(chatData.getMsgTime());
		pushBuilder.setGuildTag(chatData.getGuildTag());
		pushBuilder.setOfficeId(chatData.getOfficeId());
		
		if (chatData.hasDressTitle()) {
			pushBuilder.setDressTitle(chatData.getDressTitle());
		}
		
		if (chatData.hasDressTitleType()) {
			pushBuilder.setDressTitleType(chatData.getDressTitleType());
		}
		
		toplayer.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_PUSH_CHATROOM_MSG_S_VALUE, pushBuilder));
		return true;
	}

	/**
	 * 判断对方是否设置屏蔽开关
	 * 
	 * @param fromPlayerId
	 * @param toPlayer
	 * @return
	 */
	private boolean checkPrivateSetting(String fromPlayerId, Player toPlayer) {
		if (toPlayer.getId().equals(fromPlayerId)) {
			return true;
		}
		
		boolean isFriend = RelationService.getInstance().isFriend(fromPlayerId, toPlayer.getId());
		int settingVal = 0;
		if (isFriend) {
			settingVal = toPlayer.getData().getIndexedProtectSwitchVal(PrivateSettingOptionCfg.friend_setting_option);
		} else {
			settingVal = toPlayer.getData().getIndexedProtectSwitchVal(PrivateSettingOptionCfg.stranger_setting_option);
		}
		
		if (settingVal > 0) {
			return false;
		}
		
		return true;
	}
	
	private void createChatRoom(String roomId, String fromPlayerId, long joinTime, Player toplayer, ChatRoomData.Builder dataBuilder) {
		PlayerChatRoom.Builder roomBuilder = PlayerChatRoom.newBuilder();
		roomBuilder.setRoomId(roomId);
		roomBuilder.setLock(0);
		roomBuilder.setStatus(Objects.equals(fromPlayerId, toplayer.getId()) ? MailStatus.READ_VALUE : MailStatus.NOT_READ_VALUE);
		roomBuilder.setJoinTime(joinTime);
		roomBuilder.setLastMsg(joinTime);
		LocalRedis.getInstance().saveOrUpdatePlayerChatRoom(toplayer.getId(), roomBuilder);

		// 玩家不在线，不推送消息
		if (!toplayer.isActiveOnline()) {
			return;
		}

		MailLiteInfo.Builder liteBuilder = BuilderUtil.genMailLiteBuilder(roomBuilder, dataBuilder);
		if (liteBuilder != null) {
			HPNewMailRes.Builder builder = HPNewMailRes.newBuilder();
			if(is1V1ChatRoom(roomId)){
				liteBuilder.setCreaterId(fromPlayerId);
			}
			builder.addMail(liteBuilder);
			toplayer.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_NEW_MAIL_S_VALUE, builder));
		}
	}

	public String chatRoomId1V1(String pid, String tid) {
		if (pid.compareTo(tid) > 0) {
			return IS1V1MARK + pid + tid;
		}
		return IS1V1MARK + tid + pid;
	}

	public boolean is1V1ChatRoom(String roomId) {
		return roomId.startsWith(IS1V1MARK);
	}

	
	public void createChatRoom(Player player,List<Player> members, NoticeCfgId nid,Object... params) {
		String content = Joiner.on("_").join(params);
		content = NOTICE_HEADER + nid.getNumber() + "_" + content;
		this.createChatRoom(player, members, content);
	}
	
	
	
	public void createChatRoom(Player player,List<Player> members, String message) {
		boolean notOK = members.stream().filter(Objects::isNull).count() > 0;
		if (notOK) { // 跨服
			return;
		}
		if (members.size() == 2) {
			String roomId = PersonalMailService.getInstance().chatRoomId1V1(members.get(0).getId(), members.get(1).getId());
			if (!sendChatRoomMsg(player,roomId, message, HP.code.MAIL_SEND_GUILD_MAIL_C_VALUE, 0)) {
				MailService.getInstance().createChatRoom(player, members, message, ChatType.P2P, roomId);
			}
		} else {
			MailService.getInstance().createChatRoom(player, members, message, ChatType.NORMAL);
		}
	}
	
	
	/**
	 * 发送聊天室消息
	 * 
	 * @param roomId
	 *            聊天室ID
	 * @param message
	 *            消息内容
	 * @param protocol
	 *            协议号
	 * @return
	 */
	public boolean sendChatRoomMsg(Player player,String roomId, String message, int protocol, long startTime) {
		// 添加聊天室聊天信息
		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder == null) {
			return false;
		}

		ChatData chatData = MailService.getInstance().addChatMessage(player, roomId, player.getId(), message);
		// 不是聊天室成员
		List<MemberData.Builder> memberDatas = new ArrayList<MemberData.Builder>(dataBuilder.getMembersBuilderList());
		dataBuilder.clearMembers();
		boolean isMember = false;
		for (MemberData.Builder memberData : memberDatas) {
			if (memberData.getPlayerId().equals(player.getId())) {
				isMember = true;
				break;
			}
		}

		if (!isMember) {
			return false;
		}

		int recNum = 0;
		String toPlayerId = "";
		// 给玩家发送聊天消息
		for (MemberData.Builder memberData : memberDatas) {
			memberData.setIsDelete(false);
			dataBuilder.addMembers(memberData);
			if (!RelationService.getInstance().isBlacklist(memberData.getPlayerId(), player.getId())) {
				PersonalMailService.getInstance().sendChat(roomId, memberData.getPlayerId(), player.getId(), chatData, HawkTime.getMillisecond());
				if (!player.getId().equals(memberData.getPlayerId())) {
					toPlayerId = memberData.getPlayerId();
					recNum++;
				}
			}
		}

		if (startTime > 0) {
			boolean open = !GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable();
			HawkLog.debugPrintln("chatroom msg filter, opened: {}, costtime: {}", open, HawkTime.getMillisecond() - startTime);
		}

		if (recNum > 0) {
			LogUtil.logChatInfo(player, toPlayerId, SnsType.ROOM_CHAT, message, recNum);
			LogUtil.logSecTalkFlow(player, recNum == 1 ? toPlayerId : null, recNum == 1 ? LogMsgType.PERSONAL : LogMsgType.CHAT_ROOM, roomId, message);
		}

		// 更新保存聊天室信息
		LocalRedis.getInstance().addChatRoomData(roomId, dataBuilder);
		player.responseSuccess(protocol);
		return true;
	}




}
