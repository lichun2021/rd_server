package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ControlProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.data.ChatRoomMsgFilterCallbackData;
import com.hawk.game.data.CreateChatRoomFilterCallbackData;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.msg.ChatRoomCreateMsgFilterFinishMsg;
import com.hawk.game.msg.ChatRoomMsgFilterFinishMsg;
import com.hawk.game.msg.ChatRoomNameFilterFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailStatus;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Mail.BeDelFromChatRoomRes;
import com.hawk.game.protocol.Mail.ChangeChatRoomName;
import com.hawk.game.protocol.Mail.ChangeMembersReq;
import com.hawk.game.protocol.Mail.ChatRoomMail;
import com.hawk.game.protocol.Mail.ChatRoomMember;
import com.hawk.game.protocol.Mail.ChatRoomMsg;
import com.hawk.game.protocol.Mail.HPCancelSaveMailReq;
import com.hawk.game.protocol.Mail.HPCheckIntegralMail;
import com.hawk.game.protocol.Mail.HPCheckMailByTypeReq;
import com.hawk.game.protocol.Mail.HPCheckMailByTypeRes;
import com.hawk.game.protocol.Mail.HPCheckMailReq;
import com.hawk.game.protocol.Mail.HPCheckMailRes;
import com.hawk.game.protocol.Mail.HPCheckOtherPlayerMailReq;
import com.hawk.game.protocol.Mail.HPCreateChatRoomReq;
import com.hawk.game.protocol.Mail.HPDelMailByIdReq;
import com.hawk.game.protocol.Mail.HPGetMailRewardReq;
import com.hawk.game.protocol.Mail.HPListMail;
import com.hawk.game.protocol.Mail.HPListMailReq;
import com.hawk.game.protocol.Mail.HPListMailResp;
import com.hawk.game.protocol.Mail.HPMailListSync;
import com.hawk.game.protocol.Mail.HPMailShareReq;
import com.hawk.game.protocol.Mail.HPMailUnreadReq;
import com.hawk.game.protocol.Mail.HPMailUnreadResp;
import com.hawk.game.protocol.Mail.HPMarkReadMailReq;
import com.hawk.game.protocol.Mail.HPSaveMailReq;
import com.hawk.game.protocol.Mail.HPSendChatRoomMsgReq;
import com.hawk.game.protocol.Mail.HPTypeMail;
import com.hawk.game.protocol.Mail.HistoryPlayersRes;
import com.hawk.game.protocol.Mail.LeaveChatRoomReq;
import com.hawk.game.protocol.Mail.MailCreateChatRoomCD;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.Mail.MailUnread;
import com.hawk.game.protocol.Mail.MoveCityInviteMail;
import com.hawk.game.protocol.Mail.ReplyMoveCityInviteMail;
import com.hawk.game.protocol.Mail.UpdateChatRoom;
import com.hawk.game.protocol.Mail.UpdateChatRoomReq;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MailConst.SysMsgType;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.RedisMail.ChatData;
import com.hawk.game.protocol.RedisMail.ChatMessage;
import com.hawk.game.protocol.RedisMail.ChatRoomData;
import com.hawk.game.protocol.RedisMail.ChatType;
import com.hawk.game.protocol.RedisMail.MemberData;
import com.hawk.game.protocol.RedisMail.PlayerChatRoom;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GlobalMail;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.PersonalMailService;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.UicMsgResultFlag;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildAction;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.LogConst.SnsType;
import com.hawk.sdk.config.PlatformConstCfg;

import redis.clients.jedis.Jedis;

/**
 * 邮件
 *
 * @author david
 */
public class PlayerMailModule extends PlayerModule {
	static Logger logger = LoggerFactory.getLogger("Action");

	private long lastTickTime = 0;
	private long createChatRoomCD;
	private int lastClearDay;
	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerMailModule(Player player) {
		super(player);
	}

	/**
	 * 更新
	 *
	 * @return
	 */
	@Override
	public boolean onTick() {
		// 检查是否有未读取的全服邮件
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime - lastTickTime < GameConstCfg.getInstance().getReadMailInterval()) {
			return false;
		}

		lastTickTime = currentTime;

		// 获取当前最新邮件
		List<GlobalMail> mails = GlobalData.getInstance().getNewGlobalMails(player);
		if (mails == null || mails.size() == 0 || !GlobalData.getInstance().isLocalServer(player.getServerId())) {
			return false;
		}

		long createTime = 0L;
		for (GlobalMail mail : mails) {
			SystemMailService.getInstance().sendGlobalMail(player, mail);
			createTime = mail.getCreateTime() > createTime ? mail.getCreateTime() : createTime;
		}

		player.getEntity().setLastGmailCtime(createTime);

		return true;
	}

	@Override
	protected boolean onPlayerAssemble() {
		return true;
	}

	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		// 清理无效或者过期邮件
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
			task.setTypeName("MailModuleLogin");
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
		if (!player.isCsPlayer() && !player.isInDungeonMap() && lastClearDay != HawkTime.getYearDay()) {
			lastClearDay = HawkTime.getYearDay();
			MailService.getInstance().clear(player.getId());
		}
		syncMailList();
	}

	@Override
	protected boolean onPlayerLogout() {
		return super.onPlayerLogout();
	}

	/**
	 * 同步邮件列表
	 */
	public void syncMailList() {
		HPMailListSync.Builder builder = HPMailListSync.newBuilder();

		// 私人邮件
		List<PlayerChatRoom.Builder> chatRooms = LocalRedis.getInstance().getPlayerChatRooms(player.getId());
		for (PlayerChatRoom.Builder roomBuilder : chatRooms) {
			ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomBuilder.getRoomId());
			if (dataBuilder == null) {
				// 清理相关数据
				LocalRedis.getInstance().delPlayerChatRoom(player.getId(), roomBuilder.getRoomId());
				continue;
			}
			if (dataBuilder.getJoinMembersCount() == 2) {
				// 是否互相拉黑
				String p1 = dataBuilder.getJoinMembers(0);
				String p2 = dataBuilder.getJoinMembers(1);
				if (RelationService.getInstance().isBlacklist(player.getId(), p2) || RelationService.getInstance().isBlacklist(player.getId(), p1)) {
					LocalRedis.getInstance().delPlayerChatRoom(player.getId(), roomBuilder.getRoomId());
					continue;
				}
			}

			MailLiteInfo.Builder liteBuilder = BuilderUtil.genMailLiteBuilder(roomBuilder, dataBuilder);
			if (liteBuilder != null) {
				builder.addMail(liteBuilder);
			}
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.MAIL_LIST_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 按页取邮件
	 */
	@ProtocolHandler(code = HP.code.MAIL_LIST_MAIL_C_VALUE)
	private void onListMail(HawkProtocol protocol) {
		HPListMailReq req = protocol.parseProtocol(HPListMailReq.getDefaultInstance());
		HPListMailResp.Builder resp = HPListMailResp.newBuilder();

		for (HPListMail lm : req.getListList()) {
			int count = lm.getType() == MailService.SAVE_MAIL_TYPE ? 50 : 20;
			int unRead = 0;
			List<MailLiteInfo.Builder> list = MailService.getInstance().listMail(player.getId(), lm.getLastMailId(), lm.getType(), count);
			HPTypeMail.Builder bul = HPTypeMail.newBuilder().setType(lm.getType()).setHasNext(list.size() >= count);
			for (MailLiteInfo.Builder mail : list) {
				bul.addMail(mail);
				if (mail.getStatus() == MailStatus.NOT_READ_VALUE) {
					unRead++;
				}
			}

			resp.addList(bul);

			if (StringUtils.isEmpty(lm.getLastMailId()) && !bul.getHasNext() && unRead == 0) {
				int redCount = MailService.getInstance().unreadCount(player.getId(), lm.getType());
				if (redCount > 0) {
					cleanReadCount(lm.getType());
				}
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_LIST_MAIL_S, resp));
	}

	/**
	 * 邮件未读数
	 */
	@ProtocolHandler(code = HP.code.MAIL_TYPE_UNREAD_C_VALUE)
	private void onMailUnread(HawkProtocol protocol) {
		HPMailUnreadReq req = protocol.parseProtocol(HPMailUnreadReq.getDefaultInstance());
		HPMailUnreadResp.Builder resp = HPMailUnreadResp.newBuilder();
		for (int type : req.getTypeList()) {
			int count = MailService.getInstance().unreadCount(player.getId(), type);
			resp.addUnread(MailUnread.newBuilder().setCount(count).setType(type));
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_TYPE_UNREAD_S, resp));
	}

	/**
	 * 标记邮件已读
	 */
	@ProtocolHandler(code = HP.code.MAIL_MARK_READ_C_VALUE)
	private boolean onMarkRead(HawkProtocol protocol) {
		HPMarkReadMailReq request = protocol.parseProtocol(HPMarkReadMailReq.getDefaultInstance());
		List<String> mailIds = request.getIdList();

		int signNumber = 0;
		if (request.getType() == MailService.getInstance().chatMailType()) {
			// 聊天邮件数据
			List<PlayerChatRoom.Builder> chatRooms = LocalRedis.getInstance().getPlayerChatRooms(player.getId());
			for (PlayerChatRoom.Builder roomBuilder : chatRooms) {
				if (!mailIds.contains(roomBuilder.getRoomId())) {
					continue;
				}
				if (roomBuilder.getStatus() != MailStatus.READ_VALUE) {
					roomBuilder.setStatus(MailStatus.READ_VALUE);
					LocalRedis.getInstance().saveOrUpdatePlayerChatRoom(player.getId(), roomBuilder);
				}
				signNumber++;
				if (signNumber >= mailIds.size()) {
					break;
				}
			}
		} else {
			if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.MAIL_REWARD_RECV)) {
				sendError(protocol.getType(), Status.SysError.MAIL_AWARD_RECV_OFF);
				return true;
			}
			List<MailLiteInfo.Builder> mailEntities = MailService.getInstance().listMailEntity(mailIds);
			AwardItems award = AwardItems.valueOf();
			for (MailLiteInfo.Builder mailEntity : mailEntities) {
				MailService.getInstance().getMailReward(player, mailEntity, award);
			}

			boolean bfalse = MailService.getInstance().readMail(player.getId(), request.getType(), mailEntities);
			if (bfalse && award.getAwardItems().size() > 0) {
				award.rewardTakeMailAffectAndPush(player, Action.SYS_MAIL_AWARD, true, RewardOrginType.MAIL_REWARD);
			}
			if (mailIds.isEmpty()) { // 清除所有红点
				cleanReadCount(request.getType());
			}
		}

		player.responseSuccess(protocol.getType());

		return true;
	}

	private void cleanReadCount(int type) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			String keyUnread = MailService.getInstance().keyUnread(player.getId(), type);
			jedis.zremrangeByScore(keyUnread, 0, Double.MAX_VALUE);
		}
		HPMailUnreadResp.Builder resp = HPMailUnreadResp.newBuilder();
		resp.addUnread(MailUnread.newBuilder().setCount(0).setType(type));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_TYPE_UNREAD_S, resp));
	}

	/**
	 * 收藏邮件
	 */
	@ProtocolHandler(code = HP.code.MAIL_SAVE_C_VALUE)
	private boolean onSaveMail(HawkProtocol protocol) {
		HPSaveMailReq request = protocol.parseProtocol(HPSaveMailReq.getDefaultInstance());
		List<String> mailIds = request.getIdList();

		int saveNumber = 0;

		if (request.getType() == MailService.getInstance().chatMailType()) {
			List<PlayerChatRoom.Builder> chatRooms = LocalRedis.getInstance().getPlayerChatRooms(player.getId());
			for (PlayerChatRoom.Builder roomBuilder : chatRooms) {
				if (mailIds.contains(roomBuilder.getRoomId())) {
					if (roomBuilder.getLock() != 1) {
						roomBuilder.setLock(1);
						LocalRedis.getInstance().saveOrUpdatePlayerChatRoom(player.getId(), roomBuilder);
					}
					saveNumber++;
				}
				if (saveNumber >= mailIds.size()) {
					break;
				}
			}
		} else {
			List<MailLiteInfo.Builder> mailEntities = MailService.getInstance().listMailEntity(mailIds);
			for (MailLiteInfo.Builder mail : mailEntities) {
				mail.setLock(1);
				MailService.getInstance().readMail(mail);
				MailService.getInstance().addSaveMail(mail.build());
			}
		}

		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 取消收藏邮件
	 */
	@ProtocolHandler(code = HP.code.MAIL_CANCEL_SAVE_C_VALUE)
	private boolean onCancelSaveMail(HawkProtocol protocol) {
		HPCancelSaveMailReq request = protocol.parseProtocol(HPCancelSaveMailReq.getDefaultInstance());
		List<String> mailIds = request.getIdList();

		int cancelNumber = 0;

		if (request.getType() == MailService.getInstance().chatMailType()) {
			// 聊天邮件数据
			List<PlayerChatRoom.Builder> chatRooms = LocalRedis.getInstance().getPlayerChatRooms(player.getId());
			for (PlayerChatRoom.Builder roomBuilder : chatRooms) {
				if (mailIds.contains(roomBuilder.getRoomId())) {
					if (roomBuilder.getLock() == 1) {
						roomBuilder.setLock(0);
						LocalRedis.getInstance().saveOrUpdatePlayerChatRoom(player.getId(), roomBuilder);
					}
					cancelNumber++;
				}
				if (cancelNumber >= mailIds.size()) {
					break;
				}
			}
		} else {
			List<MailLiteInfo.Builder> mailEntities = MailService.getInstance().listMailEntity(mailIds);
			for (MailLiteInfo.Builder mailEntity : mailEntities) {
				mailEntity.setLock(0);
				MailService.getInstance().readMail(mailEntity);
				MailService.getInstance().delSaveMail(mailEntity.build());
			}
		}

		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 通过邮件ID删除邮件
	 */
	@ProtocolHandler(code = HP.code.MAIL_DEL_MAIL_BY_ID_C_VALUE)
	private boolean onDelMailById(HawkProtocol protocol) {
		HPDelMailByIdReq request = protocol.parseProtocol(HPDelMailByIdReq.getDefaultInstance());
		List<String> mailIds = request.getIdList();

		if (request.getType() == MailService.getInstance().chatMailType()) {
			MailService.getInstance().deleteChatRooms(player, mailIds);
		} else {
			this.delMailEntity(request.getType(), mailIds);
		}

		player.getPush().notifyMailDeleted(request.getType(), mailIds);
		return true;
	}

	// TODO 这个方法应该没用. 并且当有999的时候这个方法也不再适用
	// /**
	// * 通过邮件类型删除邮件
	// */
	// @ProtocolHandler(code = HP.code.MAIL_DEL_MAIL_BY_TYPE_C_VALUE)
	// private boolean onDelMailByType(HawkProtocol protocol) {
	// HPDelMailByTypeReq request =
	// protocol.parseProtocol(HPDelMailByTypeReq.getDefaultInstance());
	// MailType type = MailType.valueOf(request.getType());
	//
	// if (type == MailType.MailService.getInstance().chatMailType()) {
	// // 删除聊天室
	// List<PlayerChatRoom.Builder> chatRooms =
	// LocalRedis.getInstance().getPlayerChatRooms(player.getId());
	// List<String> mailIds =
	// chatRooms.stream().map(PlayerChatRoom.Builder::getRoomId).collect(Collectors.toList());
	// deleteChatRooms(mailIds);
	// } else {
	// this.delMailEntity(type);
	// }
	//
	// player.responseSuccess(protocol.getType());
	//
	// return true;
	// }

	/**
	 * 领取邮件奖励
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MAIL_REWARD_C_VALUE)
	private boolean onGetMailReward(HawkProtocol protocol) {
		HPGetMailRewardReq request = protocol.parseProtocol(HPGetMailRewardReq.getDefaultInstance());
		String mailId = request.getId();

		// 领取邮件奖励模块关闭，玩家不能领奖
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.MAIL_REWARD_RECV)) {
			logger.info("system control, mail reward receive has closed, mailId:{}", mailId);
			sendError(protocol.getType(), Status.SysError.MAIL_AWARD_RECV_OFF);
			return false;
		}

		MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(mailId);
		if (Objects.isNull(mail) || !Objects.equals(player.getId(), mail.getPlayerId())) {
			sendError(protocol.getType(), Status.Error.MAIL_NOT_EXIST);
			logger.error("onGetMailReward playerId : {}, playerName : {}, mail==null mailId : {}", player.getId(), player.getName(), mailId);
			return false;
		}

		AwardItems award = AwardItems.valueOf();
		MailService.getInstance().getMailReward(player, mail, award);
		if (award.getAwardItems().size() > 0) {
			award.rewardTakeMailAffectAndPush(player, Action.SYS_MAIL_AWARD, true, RewardOrginType.MAIL_REWARD);
		}
		MailService.getInstance().readMail(mail);
		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 创建聊天室
	 */
	@ProtocolHandler(code = HP.code.MAIL_CREATE_CHATROOM_C_VALUE)
	private boolean onCreateChatRoom(HawkProtocol protocol) {
		HPCreateChatRoomReq request = protocol.parseProtocol(HPCreateChatRoomReq.getDefaultInstance());
		// 接收者列表
		List<String> toPlayerList = request.getToPlayerList();
		if (checkHasBlack(protocol, toPlayerList)) {
			return true;
		}
		
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					createChatRoom(protocol);
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("onCreateChatRoom");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
		}else{
			createChatRoom(protocol);
		}
		return true;
	}

	private boolean checkHasBlack(HawkProtocol protocol, List<String> toPlayerList) {
		for(String memId : toPlayerList){
			boolean black = RelationService.getInstance().isBlacklist(memId, player.getId());
			if(black){ // 包含黑名单玩家
				sendError(protocol.getType(), Status.Error.CAHT_FAIL_HAS_BLACK);
				return true;
			}
		}
		return false;
	}

	private boolean createChatRoom(HawkProtocol protocol) {
		if (createChatRoomCD > HawkTime.getMillisecond()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_CREATE_CHAT_CD_S, MailCreateChatRoomCD.newBuilder().setCdTime(createChatRoomCD)));
			return false;
		}
		if (player.getCityLevel() < ControlProperty.getInstance().getPrivateChatNormalLv()) {
			return false;
		}

		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}

		HPCreateChatRoomReq request = protocol.parseProtocol(HPCreateChatRoomReq.getDefaultInstance());
		// 接收者列表
		List<String> toPlayerList = new ArrayList<String>(request.getToPlayerList());
		String message = request.getMsg();

		// 验证参数
		if (toPlayerList.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 验证接收者名字是否合法
		toPlayerList.add(player.getId());
		String[] toPlayerIds = toPlayerList.toArray(new String[0]);
		if (toPlayerIds == null || toPlayerIds.length <= 1) {
			sendError(protocol.getType(), Status.Error.NAME_ERROR_PLAYER);
			return false;
		}

		int inGameFriendCnt = 0;
		for (String playerId : toPlayerIds) {
			if (RelationService.getInstance().isInGamePlatformFriend(player, playerId)) {
				inGameFriendCnt++;
			}
		}

		// 发送对象不是本服玩家，无法发送聊天信息
		if (inGameFriendCnt == 0) {
			sendError(protocol.getType(), Status.Error.CHAT_NOT_INGAME_FRIEND_VALUE);
			return false;
		}

		if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
			// 替换敏感词
			message = GsApp.getInstance().getWordFilter().filterWord(message);
			List<Player> members = Arrays.asList(toPlayerIds).stream().map(GlobalData.getInstance()::makesurePlayer).collect(Collectors.toList());
			PersonalMailService.getInstance().createChatRoom(player,members, message);
			player.responseSuccess(protocol.getType());
			return true;
		}

		CreateChatRoomFilterCallbackData dataObject = new CreateChatRoomFilterCallbackData(ChatType.NORMAL_VALUE, Arrays.asList(toPlayerIds), protocol.getType());
		GameTssService.getInstance().wordUicChatFilter(player, message, 
				GameMsgCategory.CREATE_CHATROOM, GameMsgCategory.CREATE_CHATROOM, 
				JSONObject.toJSONString(dataObject), null, protocol.getType());
		createChatRoomCD = HawkTime.getMillisecond() + TimeUnit.MINUTES.toMillis(2);
		return true;
	}

	@MessageHandler
	private boolean onChatRoomCreateMsgFilterFinish(ChatRoomCreateMsgFilterFinishMsg msg) {
		String filterChatMsg = msg.getMsgContent();
		CreateChatRoomFilterCallbackData dataObject = JSONObject.parseObject(msg.getCallbackData(), CreateChatRoomFilterCallbackData.class);
		List<String> toPlayerIds = dataObject.getToPlayerIds();
		List<Player> members = toPlayerIds.stream().map(GlobalData.getInstance()::makesurePlayer).collect(Collectors.toList());
		PersonalMailService.getInstance().createChatRoom(player,members, filterChatMsg);
		player.responseSuccess(dataObject.getProtocol());
		return true;
	}


	/**
	 * 发送联盟全体邮件
	 * 
	 */
	@ProtocolHandler(code = HP.code.MAIL_SEND_GUILD_MAIL_C_VALUE)
	private boolean onSendGuildMail(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MAIL);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		HPCreateChatRoomReq req = protocol.parseProtocol(HPCreateChatRoomReq.getDefaultInstance());
		String message = req.getMsg();
		// 验证参数
		if (HawkOSOperator.isEmptyString(message)) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 联盟权限不足
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALL_MEMBER_MAIL)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY_VALUE);
			return false;
		}

		// 获取联盟的所有成员ID
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(player.getGuildId());
		if (memberIds == null || memberIds.size() == 0) {
			logger.error("onSendGuildMail:no guild member, playerId : {}, playerName : {}, guildId : {}", player.getId(), player.getName(), player.getGuildId());
			return false;
		}

		JSONObject json = new JSONObject();
		json.put("mail_id", 0);
		GameTssService.getInstance().wordUicChatFilter(player, message, 
				MsgCategory.SEND_GUILD_MAIL.getNumber(), GameMsgCategory.SEND_GUILD_MAIL, 
				"", json, protocol.getType());
		return true;
	}

	/**
	 * 查看单封邮件
	 */
	@ProtocolHandler(code = HP.code.MAIL_CHECK_MAIL_C_VALUE)
	private boolean onCheckMail(HawkProtocol protocol) {
		HPCheckMailReq request = protocol.parseProtocol(HPCheckMailReq.getDefaultInstance());
		String mailId = request.getId();

		if (request.getType() == MailService.getInstance().chatMailType()) {
			HPCheckMailRes.Builder builder = HPCheckMailRes.newBuilder();
			builder.setId(mailId);
			// 获取聊天室信息
			ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(mailId);
			if (dataBuilder == null) {
				sendError(protocol.getType(), Status.Error.MAIL_NOT_EXIST);
				return false;
			}

			List<PlayerChatRoom.Builder> chatRooms = LocalRedis.getInstance().getPlayerChatRooms(player.getId());
			if (chatRooms.isEmpty()) {
				sendError(protocol.getType(), Status.Error.MAIL_NOT_EXIST);
				return false;
			}

			for (PlayerChatRoom.Builder roomBuilder : chatRooms) {
				if (!roomBuilder.getRoomId().equals(mailId)) {
					continue;
				}
				if (roomBuilder.getStatus() == MailStatus.NOT_READ_VALUE) {
					roomBuilder.setStatus(MailStatus.READ_VALUE);
					LocalRedis.getInstance().saveOrUpdatePlayerChatRoom(player.getId(), roomBuilder);
				}
				ChatMessage.Builder messageBuilder = LocalRedis.getInstance().getChatMessage(mailId);
				ChatRoomMail.Builder mailBuilder = checkChatRoomMail(player.getId(), dataBuilder, roomBuilder, messageBuilder);
				builder.setChatRoomMail(mailBuilder);
				player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_CHECK_MAIL_S, builder));
				return true;
			}
		} else {
			// 获取邮件
			MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(mailId);
			if (Objects.isNull(mail)) {
				sendError(protocol.getType(), Status.Error.MAIL_NOT_EXIST);
				return false;
			}
			// 查看邮件返回
			HPCheckMailRes.Builder builder = MailService.getInstance().createHPCheckMailResBuilder(player.getId(), mail);
			MailService.getInstance().readMail(mail);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_CHECK_MAIL_S, builder));
		}

		return true;
	}

	@ProtocolHandler(code = HP.code.MAIL_CHECK_INTEGRAL_MAIL_C_VALUE)
	private void onCheckIntegralMail(HawkProtocol protocol) {
		HPCheckMailReq request = protocol.parseProtocol(HPCheckMailReq.getDefaultInstance());
		String mailId = request.getId();

		// 获取邮件
		MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(mailId);
		if (Objects.isNull(mail)) {
			sendError(protocol.getType(), Status.Error.MAIL_NOT_EXIST);
			return;
		}
		// 查看邮件返回
		HPCheckMailRes.Builder builder = MailService.getInstance().createHPCheckMailResBuilder(player.getId(), mail);
		// MailService.getInstance().readMail(mail);
		HPCheckIntegralMail.Builder respbuilder = HPCheckIntegralMail.newBuilder().setLite(mail).setContent(builder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_CHECK_INTEGRAL_MAIL_S, respbuilder));

	}

	/**
	 * 邮件分享
	 */
	@ProtocolHandler(code = HP.code.MAIL_SHARE_C_VALUE)
	private boolean onMailShare(HawkProtocol protocol) {
		HPMailShareReq request = protocol.parseProtocol(HPMailShareReq.getDefaultInstance());
		String uuid = request.getMailId();
		int type = request.getType();
		com.hawk.game.protocol.Const.ChatType chatType = Const.ChatType.CHAT_ALLIANCE;
		if(request.hasChannal()){
			chatType = request.getChannal();
		}
		// 获取邮件
		MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(uuid);
		if (Objects.isNull(mail)) {
			return false;
		}
		long now = HawkTime.getMillisecond();
		if (now - mail.getLastShare() < ConstProperty.getInstance().getShareTime() * 1000) {
			return false;
		}
		ChatParames parames = ChatParames.newBuilder()
				.setChatType(chatType)
				.setKey(Const.NoticeCfgId.MAIL_SHARE)
				.setPlayer(player)
				.addParms(player.getName(), uuid, player.getId(), mail.getMailId(), type)
				.addParms(request.getParamesList().toArray())
				.build();
		
		if (chatType == Const.ChatType.CHAT_FUBEN_TEAM) {
			if (player.getCYBORGState() == CYBORGState.GAMEING) {
				ICYBORGPlayer sender = CYBORGRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().addWorldBroadcastMsg(parames);
			} else if (player.getTBLYState() == TBLYState.GAMEING) {
				ITBLYPlayer sender = TBLYRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().addWorldBroadcastMsg(parames);
			} else if (player.getSwState() == SWState.GAMEING) {
				ISWPlayer sender = SWRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().addWorldBroadcastMsg(parames);
			} else if (player.getDYZZState() == DYZZState.GAMEING) {
				IDYZZPlayer sender = DYZZRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().addWorldBroadcastMsg(parames);
			}else if (player.getYQZZState() == YQZZState.GAMEING) {
				IYQZZPlayer sender = YQZZRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().addWorldBroadcastMsg(parames);
			} else if (player.getXhjzState() == XHJZState.GAMEING) {
				IXHJZPlayer sender = XHJZRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().addWorldBroadcastMsg(parames);
			} else if (player.getXQHXState() == XQHXState.GAMEING) {
				IXQHXPlayer sender = XQHXRoomManager.getInstance().makesurePlayer(player.getId());
				sender.getParent().addWorldBroadcastMsg(parames);
			}
		} else {
			ChatService.getInstance().addWorldBroadcastMsg(parames);
		}
		player.responseSuccess(protocol.getType());

		mail.setLastShare(now);
		MailService.getInstance().readMail(mail);

		LogUtil.logGuildAction(GuildAction.GUILD_SHARE_ATKREPORT, player.getGuildId());
		return true;
	}

	/**
	 * 查看其他玩家邮件
	 */
	@ProtocolHandler(code = HP.code.MAIL_CHECK_OTHERPLAYER_MAIL_C_VALUE)
	private boolean onCheckOtherPlayerMail(HawkProtocol protocol) {
		HPCheckOtherPlayerMailReq request = protocol.parseProtocol(HPCheckOtherPlayerMailReq.getDefaultInstance());
		String playerId = request.getPlayerId();
		String mailId = request.getMailId();

		// 获取邮件
		MailLiteInfo.Builder mailEntity = MailService.getInstance().getMailEntity(mailId);

		if (mailEntity == null) {
			sendError(protocol.getType(), Status.Error.MAIL_NOT_EXIST);
			return false;
		}

		// 查看邮件返回
		HPCheckMailRes.Builder builder = MailService.getInstance().createHPCheckMailResBuilder(playerId, mailEntity);

		// 返回邮件
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_CHECK_OTHERPLAYER_MAIL_S_VALUE, builder));

		return true;
	}

	/**
	 * 查看某一类邮件
	 */
	@ProtocolHandler(code = HP.code.MAIL_CHECK_MAIL_BY_TYPE_C_VALUE)
	private boolean onCheckMailByType(HawkProtocol protocol) {
		HPCheckMailByTypeReq request = protocol.parseProtocol(HPCheckMailByTypeReq.getDefaultInstance());

		// 获取邮件
		List<MailLiteInfo.Builder> mails = MailService.getInstance().listMailEntity(request.getMailIdList());
		if (mails == null || mails.size() == 0) {
			return false;
		}

		// 查看邮件返回
		HPCheckMailByTypeRes.Builder builder = HPCheckMailByTypeRes.newBuilder();
		for (MailLiteInfo.Builder mail : mails) {
			builder.addMails(MailService.getInstance().createHPCheckMailResBuilder(player.getId(), mail));
			MailService.getInstance().readMail(mail);
		}

		// 返回邮件
		HawkProtocol response = HawkProtocol.valueOf(HP.code.MAIL_CHECK_MAIL_BY_TYPE_S, builder);
		player.sendProtocol(response);

		return true;
	}

	/**
	 * 查看聊天室邮件
	 * 
	 * @param playerId
	 * @param globalBuilder
	 * @param joinTime
	 * @param chatMessage
	 * @return
	 */
	private ChatRoomMail.Builder checkChatRoomMail(String playerId, ChatRoomData.Builder dataBuilder, PlayerChatRoom.Builder roomBuilder, ChatMessage.Builder chatMessage) {
		ChatRoomMail.Builder builder = ChatRoomMail.newBuilder();

		// 添加聊天成员信息
		List<String> joinMembers = dataBuilder.getJoinMembersList();
		for (String memId : joinMembers) {

			// playerId把memId屏蔽了
			if (RelationService.getInstance().isBlacklist(playerId, memId)) {
				continue;
			}
			Player member = GlobalData.getInstance().makesurePlayer(memId);
			if (member == null) {
				continue;
			}
			ChatRoomMember.Builder memBuilder = ChatRoomMember.newBuilder();
			memBuilder.setPlayerId(memId);
			memBuilder.setName(member.getName());
			memBuilder.setIcon(member.getIcon());
			memBuilder.setGuildTag(member.getGuildTag());
			memBuilder.setVipLevel(member.getVipLevel());
			if (!HawkOSOperator.isEmptyString(member.getPfIcon())) {
				memBuilder.setPfIcon(member.getPfIcon());
			}

			if (member.isActiveOnline()) {
				memBuilder.setOnline(true);
				memBuilder.setOfflineTime(0);
			} else {
				memBuilder.setOnline(false);
				memBuilder.setOfflineTime(member.getLogoutTime());
			}
			memBuilder.setCommon(BuilderUtil.genPlayerCommonBuilder(member));
			DressItem titleDress = WorldPointService.getInstance().getShowDress(playerId, DressType.TITLE_VALUE);
			if (titleDress != null) {
				memBuilder.setDressTitle(titleDress.getModelType());
			} else {
				memBuilder.setDressTitle(0);
			}
			memBuilder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(playerId));
			builder.addRoomMember(memBuilder);
		}
		// 添加创建者
		builder.setCreaterId(dataBuilder.getCreaterId());

		// 添加聊天消息
		if (chatMessage == null) {
			return builder;
		}

		List<ChatData> chatDatas = new ArrayList<>();
		for (ChatData msg : chatMessage.getChatList()) {
			if (msg.getMsgTime() - roomBuilder.getJoinTime() > -1000) {
				chatDatas.add(msg);
			}
		}

		for (ChatData roomMsg : chatDatas) {
			ChatRoomMsg.Builder msgBuilder = ChatRoomMsg.newBuilder();
			if (roomMsg.hasPlayerId()) {
				msgBuilder.setPlayerId(roomMsg.getPlayerId());
			}
			if (roomMsg.hasVoiceId()) {
				msgBuilder.setVoiceId(roomMsg.getVoiceId()).setVoiceLength(roomMsg.getVoiceLength());
			}
			msgBuilder.setMsg(roomMsg.getMessage());
			msgBuilder.setMsgTime(roomMsg.getMsgTime());
			msgBuilder.setGuildTag(roomMsg.getGuildTag());
			msgBuilder.setOfficeId(roomMsg.getOfficeId());
			msgBuilder.setDressTitle(roomMsg.getDressTitle());
			msgBuilder.setDressTitleType(roomMsg.getDressTitleType());
			builder.addMsg(msgBuilder);
		}

		return builder;
	}

	/**
	 * 发送聊天室消息
	 */
	@ProtocolHandler(code = HP.code.MAIL_SEND_CHATROOM_MSG_C_VALUE)
	private boolean onSendChatRoomMsg(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MAIL);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		long startTime = HawkTime.getMillisecond();
		HPSendChatRoomMsgReq request = protocol.parseProtocol(HPSendChatRoomMsgReq.getDefaultInstance());
		// 消息不能为空
		if (!request.hasMsg() && !request.hasVoice()) {
			return false;
		}

		String roomId = request.getId();
		String message = request.getMsg();
		if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
			message = GsApp.getInstance().getWordFilter().filterWord(message);
			return PersonalMailService.getInstance().sendChatRoomMsg(player,roomId, message, protocol.getType(), startTime);
		}

		JSONObject json = new JSONObject();
		json.put("user_sign", WorldPointService.getInstance().getPlayerSignature(player.getId()));
		json.put("template_sign", 0);
		json.put("msg_type", 0);
		json.put("speech_original_voice_id", message);
		MsgCategory category = MsgCategory.PRIVATE_CHAT;
		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder != null && dataBuilder.getMembersBuilderList().size() > 2) {
			category = MsgCategory.GROUP_CHAT;
		} else if (dataBuilder != null) {
			Player tPlayer = null;
			String tPlayerId = "";
			for (MemberData.Builder memberData : dataBuilder.getMembersBuilderList()) {
				if (!memberData.getPlayerId().equals(player.getId())) {
					tPlayerId = memberData.getPlayerId();
					tPlayer = GlobalData.getInstance().makesurePlayer(memberData.getPlayerId());
					break;
				}
			}
			
			if (!HawkOSOperator.isEmptyString(tPlayerId) && GlobalData.getInstance().isResetAccount(tPlayerId)) {
				sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
				HawkLog.errPrintln("another player is removed player, playerId: {}, another playerId: {}", player.getId(), tPlayerId);
				return false;
			}
			
			if (tPlayer != null) {
				String pfIconPrimitive = tPlayer.getData().getPrimitivePfIcon();
				if (GlobalData.getInstance().isBanPortraitAccount(tPlayer.getOpenId())) {
					pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
				}
				boolean isFriend = RelationService.getInstance().isFriend(player.getId(), tPlayer.getId());
				json.put("msg_char_type", isFriend ? 1 : 0); // 0 非好友，1好友
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
		}
		
		ChatRoomMsgFilterCallbackData dataObject = new ChatRoomMsgFilterCallbackData(roomId, protocol.getType(), startTime);
		GameTssService.getInstance().wordUicChatFilter(player, message, 
				category.getNumber(), GameMsgCategory.CHATROOM_CHAT, 
				JSONObject.toJSONString(dataObject), json, protocol.getType());
		return true;
	}

	@MessageHandler
	private boolean onChatRoomMsgFilterFinish(ChatRoomMsgFilterFinishMsg msg) {
		String filterChatMsg = msg.getMsgContent();
		ChatRoomMsgFilterCallbackData callbackData = JSONObject.parseObject(msg.getCallbackData(), ChatRoomMsgFilterCallbackData.class);
		if (msg.getResultCode() != UicMsgResultFlag.ILLEGAL) {
			return PersonalMailService.getInstance().sendChatRoomMsg(player,callbackData.getRoomId(), filterChatMsg, callbackData.getProtocol(), callbackData.getStartTime());
		}
		
		// msg.getResultCode() == 1 表示恶意发言，仅自己可见
		String roomId = callbackData.getRoomId();
		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder == null) {
			return false;
		}

		{
			ChatData.Builder chatData = ChatData.newBuilder();
			chatData.setPlayerId(player.getId());
			chatData.setMessage(filterChatMsg);
			chatData.setGuildTag(player.getGuildTag());
			chatData.setOfficeId(GameUtil.getOfficerId(player.getId()));
			chatData.setMsgTime(HawkTime.getMillisecond());

			DressItem titleDress = WorldPointService.getInstance().getShowDress(player.getId(), DressType.TITLE_VALUE);
			if (titleDress != null) {
				chatData.setDressTitle(titleDress.getModelType());
			} else {
				chatData.setDressTitle(0);
			}

			chatData.setDressTitleType(WorldPointService.getInstance().getDressTitleType(player.getId()));
			PersonalMailService.getInstance().sendChat(roomId, player.getId(), player.getId(), chatData.build(), HawkTime.getMillisecond());
		}
		
		player.responseSuccess(callbackData.getProtocol());
		return true;
	}


	/**
	 * 获取历史交互玩家
	 */
	@ProtocolHandler(code = HP.code.MAIL_CHAT_HISTORY_PLAYERS_C_VALUE)
	private boolean onGetInteractivePlayers(HawkProtocol protocol) {
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					chatHistoryPlayers();
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("onGetInteractivePlayers");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
		}else{
			chatHistoryPlayers();
		}
		return true;
	}

	private void chatHistoryPlayers() {
		// 获取历史交互玩家
		Set<String> playerIds = LocalRedis.getInstance().getInteractivePlayers(player.getId());

		HistoryPlayersRes.Builder res = HistoryPlayersRes.newBuilder();
		Map<String, Player> snapshots = GlobalData.getInstance().getPlayerMap(playerIds.toArray(new String[playerIds.size()]));
		for (String playerId : playerIds) {
			ChatRoomMember.Builder builder = ChatRoomMember.newBuilder();
			Player snapshot = snapshots.get(playerId);
			if (snapshot == null) {
				continue;
			}
			builder.setPlayerId(playerId);
			builder.setName(snapshot.getName());
			builder.setIcon(snapshot.getIcon());
			builder.setVipLevel(snapshot.getVipLevel());
			builder.setCommon(BuilderUtil.genPlayerCommonBuilder(snapshot));
			builder.setGuildTag(snapshot.getGuildTag());
			if (!HawkOSOperator.isEmptyString(snapshot.getPfIcon())) {
				builder.setPfIcon(snapshot.getPfIcon());
			}
			DressItem titleDress = WorldPointService.getInstance().getShowDress(playerId, DressType.TITLE_VALUE);
			if (titleDress != null) {
				builder.setDressTitle(titleDress.getModelType());
			} else {
				builder.setDressTitle(0);
			}
			builder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(playerId));
			res.addMember(builder);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_CHAT_HISTORY_PLAYERS_S_VALUE, res));
	}

	/**
	 * 添加聊天室成员
	 */
	@ProtocolHandler(code = HP.code.MAIL_CHAT_ADD_PLAYERS_C_VALUE)
	private boolean onAddChatMembers(HawkProtocol protocol) {
		ChangeMembersReq requeat = protocol.parseProtocol(ChangeMembersReq.getDefaultInstance());
		String roomId = requeat.getMailUuid();
		List<String> playerIds = requeat.getPlayerIdList();
		if (checkHasBlack(protocol, playerIds)) {
			return true;
		}
		if (PersonalMailService.getInstance().is1V1ChatRoom(roomId)) {// 1对1聊天室加人要重新创建
			return false;
		}

		// 验证参数
		if (HawkOSOperator.isEmptyString(roomId) || playerIds.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 获取玩家ID

		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		List<MemberData.Builder> memberDatas = new ArrayList<MemberData.Builder>(dataBuilder.getMembersBuilderList());
		dataBuilder.clearMembers();

		// 给原成员推送添加成员
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(SysMsgType.ADD_MEMBER_VALUE + "_" + player.getName());
		for (String playerId : playerIds) {
			Player roomMember = GlobalData.getInstance().makesurePlayer(playerId);
			strBuilder.append("_" + roomMember.getName());
		}
		// 添加聊天室聊天信息
		ChatData chatData = MailService.getInstance().addChatMessage(player, roomId, null, strBuilder.toString());

		List<ChatRoomMember> crBuilders = new ArrayList<ChatRoomMember>();
		List<ChatRoomMember> oricrBuilders = this.getChatRoomMembers(memberDatas);
		// 给被添加者推送
		for (String playerId : playerIds) {
			if (HawkOSOperator.isEmptyString(playerId)) {
				continue;
			}
			// 发送聊天室信息
			PersonalMailService.getInstance().sendChat(roomId, playerId, "", chatData, HawkTime.getMillisecond());
			// 构建聊天室成员数据
			Player roomMember = GlobalData.getInstance().makesurePlayer(playerId);
			ChatRoomMember.Builder crBuilder = ChatRoomMember.newBuilder();
			crBuilder.setPlayerId(playerId);
			crBuilder.setName(roomMember.getName());
			crBuilder.setIcon(roomMember.getIcon());
			crBuilder.setVipLevel(roomMember.getVipLevel());
			crBuilder.setGuildTag(roomMember.getGuildTag());
			crBuilder.setCommon(BuilderUtil.genPlayerCommonBuilder(roomMember));
			if (!HawkOSOperator.isEmptyString(roomMember.getPfIcon())) {
				crBuilder.setPfIcon(roomMember.getPfIcon());
			}
			DressItem titleDress = WorldPointService.getInstance().getShowDress(playerId, DressType.TITLE_VALUE);
			if (titleDress != null) {
				crBuilder.setDressTitle(titleDress.getModelType());
			} else {
				crBuilder.setDressTitle(0);
			}
			crBuilder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(playerId));
			crBuilders.add(crBuilder.build());
			// 加入聊天室
			Player _player = GlobalData.getInstance().getActivePlayer(playerId);
			if (_player != null) {
				UpdateChatRoom.Builder builder = UpdateChatRoom.newBuilder();
				builder.setUuid(roomId);
				if (dataBuilder.hasName()) {
					builder.setName(dataBuilder.getName());
				}
				builder.setOperatorType(SysMsgType.ADD_MEMBER_VALUE);
				builder.addAllMember(oricrBuilders);
				_player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_UPDATE_CHATROOM_S_VALUE, builder));
			}
		}

		// 聊天室原有账号推送
		for (MemberData.Builder memberData : memberDatas) {
			memberData.setIsDelete(false);
			dataBuilder.addMembers(memberData);

			String playerId = memberData.getPlayerId();
			// 发送添加消息
			PersonalMailService.getInstance().sendChat(roomId, playerId, "", chatData, 0L);

			Player _player = GlobalData.getInstance().getActivePlayer(playerId);
			if (_player == null) {
				continue;
			}

			UpdateChatRoom.Builder builder = UpdateChatRoom.newBuilder();
			builder.setUuid(roomId);
			if (dataBuilder.hasName()) {
				builder.setName(dataBuilder.getName());
			}
			builder.setOperatorType(SysMsgType.ADD_MEMBER_VALUE);
			builder.addAllMember(crBuilders);
			_player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_UPDATE_CHATROOM_S_VALUE, builder));
		}

		// 更新保存聊天室信息
		for (String playerId : playerIds) {
			MemberData.Builder memberData = MemberData.newBuilder();
			memberData.setPlayerId(playerId);
			memberData.setIsDelete(false);
			dataBuilder.addMembers(memberData);
			dataBuilder.addJoinMembers(playerId);
		}
		LocalRedis.getInstance().addChatRoomData(roomId, dataBuilder);

		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 删除聊天室成员
	 */
	@ProtocolHandler(code = HP.code.MAIL_CHAT_DEL_PLAYERS_C_VALUE)
	private boolean onDelChatMembers(HawkProtocol protocol) {
		ChangeMembersReq request = protocol.parseProtocol(ChangeMembersReq.getDefaultInstance());
		String roomId = request.getMailUuid();
		List<String> playerIds = request.getPlayerIdList();

		if (HawkOSOperator.isEmptyString(roomId) || playerIds.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 不是创建者
		if (!player.getId().equals(dataBuilder.getCreaterId())) {
			return false;
		}

		// 从成员列表删除成员
		List<MemberData.Builder> memberDatas = new ArrayList<MemberData.Builder>(dataBuilder.getMembersBuilderList());
		dataBuilder.clearMembers();
		// 要删除的玩家ID
		for (String playerId : playerIds) {
			Iterator<MemberData.Builder> iterator = memberDatas.iterator();
			while (iterator.hasNext()) {
				MemberData.Builder memberData = iterator.next();
				if (memberData.getPlayerId().equals(playerId)) {
					iterator.remove();
					break;
				}
			}
		}

		// 被踢出成员名称
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(SysMsgType.DEL_MEMBER_VALUE + "_" + player.getName());
		for (String playerId : playerIds) {
			Player roomMember = GlobalData.getInstance().makesurePlayer(playerId);
			strBuilder.append("_" + roomMember.getName());
		}
		// 添加聊天室聊天信息
		ChatData chatData = MailService.getInstance().addChatMessage(player, roomId, null, strBuilder.toString());

		List<ChatRoomMember> crBuilders = new ArrayList<ChatRoomMember>();
		// 给被踢出玩家推送消息
		for (String playerId : playerIds) {
			if (HawkOSOperator.isEmptyString(playerId)) {
				continue;
			}
			Player roomMember = GlobalData.getInstance().makesurePlayer(playerId);
			// 构建聊天室成员数据
			ChatRoomMember.Builder crBuilder = ChatRoomMember.newBuilder();
			crBuilder.setPlayerId(playerId);
			crBuilder.setName(roomMember.getName());
			crBuilder.setIcon(roomMember.getIcon());
			crBuilder.setVipLevel(roomMember.getVipLevel());
			crBuilder.setCommon(BuilderUtil.genPlayerCommonBuilder(roomMember));
			crBuilder.setGuildTag(roomMember.getGuildTag());
			if (!HawkOSOperator.isEmptyString(roomMember.getPfIcon())) {
				crBuilder.setPfIcon(roomMember.getPfIcon());
			}
			DressItem titleDress = WorldPointService.getInstance().getShowDress(playerId, DressType.TITLE_VALUE);
			if (titleDress != null) {
				crBuilder.setDressTitle(titleDress.getModelType());
			} else {
				crBuilder.setDressTitle(0);
			}
			crBuilder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(playerId));
			crBuilders.add(crBuilder.build());

			BeDelFromChatRoomRes.Builder bfBuilder = BeDelFromChatRoomRes.newBuilder();
			bfBuilder.setUuid(roomId);
			// 发送添加消息
			Player _player = GlobalData.getInstance().getActivePlayer(playerId);
			if (_player != null) {
				// 推送被踢出聊天室
				bfBuilder.setOperatorName(player.getName());
				_player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_BE_DEL_S_VALUE, bfBuilder));
			}
			MailService.getInstance().deleteChatRooms(roomMember, Arrays.asList(roomId));
		}

		// 通知未被踢出的成员
		for (MemberData.Builder memberData : memberDatas) {
			memberData.setIsDelete(false);
			dataBuilder.addMembers(memberData);

			String playerId = memberData.getPlayerId();
			// 发送添加消息
			PersonalMailService.getInstance().sendChat(roomId, playerId, "", chatData, 0L);

			Player _player = GlobalData.getInstance().getActivePlayer(playerId);
			if (_player == null) {
				continue;
			}

			UpdateChatRoom.Builder builder = UpdateChatRoom.newBuilder();
			builder.setUuid(roomId);
			if (dataBuilder.hasName()) {
				builder.setName(dataBuilder.getName());
			}
			builder.setOperatorType(SysMsgType.DEL_MEMBER_VALUE);
			builder.addAllMember(crBuilders);
			_player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_UPDATE_CHATROOM_S_VALUE, builder));
		}

		// 更新聊天室信息
		LocalRedis.getInstance().addChatRoomData(roomId, dataBuilder);

		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 退出聊天室
	 */
	@ProtocolHandler(code = HP.code.MAIL_LEAVE_CHATROOM_C_VALUE)
	private boolean onLeaveChatRoom(HawkProtocol protocol) {
		LeaveChatRoomReq request = protocol.parseProtocol(LeaveChatRoomReq.getDefaultInstance());
		String roomId = request.getUuid();

		if (HawkOSOperator.isEmptyString(roomId)) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 从成员列表删除成员
		List<MemberData.Builder> memberDatas = new ArrayList<MemberData.Builder>(dataBuilder.getMembersBuilderList());
		dataBuilder.clearMembers();
		if (!PersonalMailService.getInstance().is1V1ChatRoom(roomId)) {
			Iterator<MemberData.Builder> iterator = memberDatas.iterator();
			while (iterator.hasNext()) {
				MemberData.Builder memberData = iterator.next();
				if (memberData.getPlayerId().equals(player.getId())) {
					iterator.remove();
					break;
				}
			}
		}

		boolean isClear = false;
		// 创建者退出重新分配创建者
		String newCreaterId = null;
		if (player.getId().equals(dataBuilder.getCreaterId())) {
			if (memberDatas.size() > 0) {
				MemberData.Builder memberData = memberDatas.get(0);
				dataBuilder.setCreaterId(memberData.getPlayerId());
			} else {
				isClear = true;
			}
		}

		// 成员删除邮件无法在聊天
		for (MemberData.Builder memberData : memberDatas) {
			if (!memberData.getIsDelete()) {
				isClear = false;
				break;
			}
		}
		if (isClear) {
			this.clearChatData(roomId, true);
			player.responseSuccess(protocol.getType());
			return true;
		}

		// 构建成员简介信息
		ChatRoomMember.Builder crBuilder = ChatRoomMember.newBuilder();
		crBuilder.setPlayerId(player.getId());
		crBuilder.setName(player.getName());
		crBuilder.setIcon(player.getIcon());
		crBuilder.setVipLevel(player.getVipLevel());
		crBuilder.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		crBuilder.setGuildTag(player.getGuildTag());
		if (!HawkOSOperator.isEmptyString(player.getPfIcon())) {
			crBuilder.setPfIcon(player.getPfIcon());
		}
		DressItem titleDress = WorldPointService.getInstance().getShowDress(player.getId(), DressType.TITLE_VALUE);
		if (titleDress != null) {
			crBuilder.setDressTitle(titleDress.getModelType());
		} else {
			crBuilder.setDressTitle(0);
		}
		crBuilder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(player.getId()));
		
		// 添加聊天室聊天信息
		ChatData chatData = MailService.getInstance().addChatMessage(player, roomId, null, SysMsgType.MEMBER_LEAVE_VALUE + "_" + player.getName());
		// 通知其它成员有人退出
		for (MemberData.Builder memberData : memberDatas) {
			memberData.setIsDelete(false);
			dataBuilder.addMembers(memberData);

			PersonalMailService.getInstance().sendChat(roomId, memberData.getPlayerId(), "", chatData, 0L);

			if (PersonalMailService.getInstance().is1V1ChatRoom(roomId)) {
				continue;
			}
			Player _player = GlobalData.getInstance().getActivePlayer(memberData.getPlayerId());
			if (_player == null) {
				continue;
			}

			UpdateChatRoom.Builder builder = UpdateChatRoom.newBuilder();
			builder.setUuid(roomId);
			if (!HawkOSOperator.isEmptyString(newCreaterId)) {
				builder.setCreaterId(newCreaterId);
			}
			if (dataBuilder.hasName()) {
				builder.setName(dataBuilder.getName());
			}
			builder.setOperatorType(SysMsgType.DEL_MEMBER_VALUE);
			builder.addMember(crBuilder);
			_player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_UPDATE_CHATROOM_S_VALUE, builder));
		}

		// 更新聊天室数据
		LocalRedis.getInstance().addChatRoomData(roomId, dataBuilder);

		// 清理玩家聊天室数据
		this.clearChatData(roomId, false);

		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 手动修改聊天室名称
	 */
	@ProtocolHandler(code = HP.code.MAIL_CHANGE_CHATROOM_NAME_C_VALUE)
	private void onChangeChatRoomName(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_CHAT_ROOM_NAME);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		long startTime = HawkTime.getMillisecond();
		ChangeChatRoomName request = protocol.parseProtocol(ChangeChatRoomName.getDefaultInstance());
		String roomId = request.getMailUuid();
		String name = request.getName();
		if (HawkOSOperator.isEmptyString(roomId) || HawkOSOperator.isEmptyString(name)) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return;
		}

		if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
			name = GsApp.getInstance().getWordFilter().filterWord(GameUtil.filterString(name));
			changeChatRoomName(roomId, name, protocol.getType(), startTime, false);
			return;
		}

		ChatRoomMsgFilterCallbackData dataObject = new ChatRoomMsgFilterCallbackData(roomId, protocol.getType(), startTime);
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", roomId);
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", roomId);
		GameTssService.getInstance().wordUicChatFilter(player, name, 
				MsgCategory.CHAT_ROOM_NAME.getNumber(), GameMsgCategory.CHATROOM_NAME, 
				JSONObject.toJSONString(dataObject), json, protocol.getType());
	}

	@MessageHandler
	private boolean onChatRoomNameFilterFinish(ChatRoomNameFilterFinishMsg filterFinishMsg) {
		String name = filterFinishMsg.getMsgContent();
		ChatRoomMsgFilterCallbackData callbackData = JSONObject.parseObject(filterFinishMsg.getCallbackData(), ChatRoomMsgFilterCallbackData.class);
		if (filterFinishMsg.getResultCode() != UicMsgResultFlag.ILLEGAL) {
			return changeChatRoomName(callbackData.getRoomId(), name, callbackData.getProtocol(), callbackData.getStartTime(), true);
		}
		
		int protocol = callbackData.getProtocol();
		String roomId = callbackData.getRoomId();
		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder == null) {
			sendError(protocol, Status.SysError.PARAMS_INVALID);
			return false;
		}

		UpdateChatRoom.Builder builder = UpdateChatRoom.newBuilder();
		builder.setUuid(roomId);
		builder.setName(name);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_UPDATE_CHATROOM_S_VALUE, builder));
		player.responseSuccess(protocol);
		return true;
	}
	
	/**
	 * 聊天室数据更新(个保法合规需求添加)
	 */
	@ProtocolHandler(code = HP.code.MAIL_UPDATE_CHATROOM_C_VALUE)
	private void onChangeChatRoomMemberInfo(HawkProtocol protocol) {
		UpdateChatRoomReq req = protocol.parseProtocol(UpdateChatRoomReq.getDefaultInstance());
		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(req.getRoomId());
		if (dataBuilder == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return;
		}

		UpdateChatRoom.Builder builder = UpdateChatRoom.newBuilder();
		builder.setUuid(req.getRoomId());
		builder.setOperatorType(SysMsgType.UPDATE_MEMBER_VALUE);
		if (dataBuilder.hasName() && dataBuilder.getName() != null) {
			builder.setName(dataBuilder.getName());
		}
		
		if (dataBuilder.hasCreaterId() && dataBuilder.getCreaterId() != null) {
			builder.setCreaterId(dataBuilder.getCreaterId());
		}
		
		List<MemberData> memberDatas = dataBuilder.getMembersList();
		for (MemberData memberData : memberDatas) {
			String playerId = memberData.getPlayerId();
			Player _player = GlobalData.getInstance().makesurePlayer(playerId);
			if (_player == null) {
				continue;
			}

			// 构建成员简介信息
			ChatRoomMember.Builder crBuilder = ChatRoomMember.newBuilder();
			crBuilder.setPlayerId(_player.getId());
			crBuilder.setName(_player.getName());
			crBuilder.setIcon(_player.getIcon());
			crBuilder.setVipLevel(_player.getVipLevel());
			crBuilder.setCommon(BuilderUtil.genPlayerCommonBuilder(_player));
			crBuilder.setGuildTag(_player.getGuildTag());
			if (!HawkOSOperator.isEmptyString(_player.getPfIcon())) {
				crBuilder.setPfIcon(_player.getPfIcon());
			}
			DressItem titleDress = WorldPointService.getInstance().getShowDress(_player.getId(), DressType.TITLE_VALUE);
			if (titleDress != null) {
				crBuilder.setDressTitle(titleDress.getModelType());
			} else {
				crBuilder.setDressTitle(0);
			}
			crBuilder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(_player.getId()));
			builder.addMember(crBuilder);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_UPDATE_CHATROOM_S_VALUE, builder));
	}

	/**
	 * 聊天室改名
	 * 
	 * @param roomId
	 * @param name
	 * @param protocol
	 * @param startTime
	 */
	private boolean changeChatRoomName(String roomId, String name, int protocol, long startTime, boolean open) {
		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder == null) {
			sendError(protocol, Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 修改聊天室名称
		dataBuilder.setName(name);

		// 通知聊天室成员推送改名
		List<MemberData> memberDatas = dataBuilder.getMembersList();
		for (MemberData memberData : memberDatas) {
			String playerId = memberData.getPlayerId();
			Player _player = GlobalData.getInstance().getActivePlayer(playerId);
			if (_player == null) {
				continue;
			}

			UpdateChatRoom.Builder builder = UpdateChatRoom.newBuilder();
			builder.setUuid(roomId);
			builder.setName(name);
			_player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_UPDATE_CHATROOM_S_VALUE, builder));
		}

		// 更新聊天室信息
		LocalRedis.getInstance().addChatRoomData(roomId, dataBuilder);

		// 添加聊天室聊天数据
		String msg = SysMsgType.CHANGE_CHATROOM_VALUE + "_" + player.getName() + "_" + name;
		ChatData chatData = MailService.getInstance().addChatMessage(player, roomId, null, msg);

		int recNum = 0;
		String toPlayerId = "";
		// 给玩家发送聊天消息
		for (MemberData memberData : memberDatas) {
			PersonalMailService.getInstance().sendChat(roomId, memberData.getPlayerId(), null, chatData, HawkTime.getMillisecond());
			if (!player.getId().equals(memberData.getPlayerId())) {
				recNum++;
				toPlayerId = memberData.getPlayerId();
			}
		}

		if (recNum > 0) {
			LogUtil.logChatInfo(player, toPlayerId, SnsType.CHANGE_CHARROOM_NAME, name, recNum);
			LogUtil.logSecTalkFlow(player, null, LogMsgType.CHANGE_CHARROOM_NAME, roomId, name);
		}

		player.responseSuccess(protocol);
		HawkLog.debugPrintln(" change chatroom name msg filter, opened: {}, costtime: {}", open, HawkTime.getMillisecond() - startTime);
		return true;
	}

	/**
	 * 聊天室成员
	 * 
	 * @param memberDatas
	 * @return
	 */
	private List<ChatRoomMember> getChatRoomMembers(List<MemberData.Builder> memberDatas) {

		List<ChatRoomMember> result = new ArrayList<ChatRoomMember>();
		String[] playerIds = new String[memberDatas.size()];
		int index = 0;
		for (MemberData.Builder memberData : memberDatas) {
			playerIds[index] = memberData.getPlayerId();
			index++;
		}
		Map<String, Player> snapshotMap = GlobalData.getInstance().getPlayerMap(playerIds);
		// 添加聊天成员信息
		for (Player snapshot : snapshotMap.values()) {
			ChatRoomMember.Builder memBuilder = ChatRoomMember.newBuilder();
			memBuilder.setPlayerId(snapshot.getId());
			memBuilder.setName(snapshot.getName());
			memBuilder.setIcon(snapshot.getIcon());
			memBuilder.setVipLevel(snapshot.getVipLevel());
			memBuilder.setGuildTag(snapshot.getGuildTag());
			if (!HawkOSOperator.isEmptyString(snapshot.getPfIcon())) {
				memBuilder.setPfIcon(snapshot.getPfIcon());
			}
			memBuilder.setCommon(BuilderUtil.genPlayerCommonBuilder(snapshot));
			DressItem titleDress = WorldPointService.getInstance().getShowDress(snapshot.getId(), DressType.TITLE_VALUE);
			if (titleDress != null) {
				memBuilder.setDressTitle(titleDress.getModelType());
			} else {
				memBuilder.setDressTitle(0);
			}
			memBuilder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(snapshot.getId()));
			result.add(memBuilder.build());
		}
		return result;
	}

	/**
	 * 删除某类型部分邮件
	 * 
	 * @param type
	 * @param mailIds
	 */
	private void delMailEntity(int type, List<String> mailIds) {
		List<MailLiteInfo.Builder> mailEntities = MailService.getInstance().listMailEntity(mailIds);
		AwardItems award = AwardItems.valueOf();

		for (MailLiteInfo.Builder entity : mailEntities) {
			MailService.getInstance().getMailReward(player, entity, award);
		}

		if (award.getAwardItems().size() > 0) {
			if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.MAIL_REWARD_RECV)) {
				sendError(type, Status.SysError.MAIL_AWARD_RECV_OFF);
			} else {
				award.rewardTakeMailAffectAndPush(player, Action.SYS_MAIL_AWARD, true, RewardOrginType.MAIL_REWARD);
			}
		}

		MailService.getInstance().delMail(player.getId(), type, mailEntities);

	}

	/**
	 * 清理聊天数据
	 * 
	 * @param roomId
	 * @param isClear
	 */
	private void clearChatData(String roomId, boolean isClear) {
		LocalRedis.getInstance().delPlayerChatRoom(player.getId(), roomId);

		if (isClear) {
			LocalRedis.getInstance().delChatRoom(roomId);
		}
	}

	/**
	 * 回复迁城邀请
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.REPLY_MOVE_CITY_MAIL_C_VALUE)
	private void onReplyMoveCityMail(HawkProtocol protocol) {
		ReplyMoveCityInviteMail req = protocol.parseProtocol(ReplyMoveCityInviteMail.getDefaultInstance());
		String mailId = req.getMailId();
		int state = req.getState();
		// mailState = 8; // 0 未处理 ,1 拒绝 , 2 接受
		// 获取邮件
		MailLiteInfo.Builder mail = MailService.getInstance().getMailEntity(mailId);
		if (Objects.isNull(mail) || mail.getMailId() != MailId.INVITE_MOVE_CITY_VALUE) {
			return;
		}
		HPCheckMailRes.Builder content = MailService.getInstance().createHPCheckMailResBuilder(player.getId(), mail);
		MoveCityInviteMail.Builder builder = content.getMoveCityInviteMailBuilder();
		builder.setMailState(state);
		MailService.getInstance().updateMailContent(mail.build(), MailParames.newBuilder().addContents(builder).setMailId(MailId.INVITE_MOVE_CITY).build().getContent());

		player.responseSuccess(protocol.getType());
	}

	/**
	 * 退出联盟
	 * 
	 * @return
	 */
	// @MessageHandler
	// private boolean onGuildQuitMsg(GuildQuitMsg msg) {
	// // 删掉在本盟发送的全体邮件
	// String roomId =
	// PersonalMailService.getInstance().chatRoomId1V1(player.getId(),
	// player.getId());
	// List<String> deleList = Arrays.asList(roomId);
	// MailService.getInstance().deleteChatRooms(player, deleList);
	// player.getPush().notifyMailDeleted(MailService.getInstance().chatMailType(),
	// deleList);
	// return true;
	// }

}
