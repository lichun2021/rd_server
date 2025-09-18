package com.hawk.game.module;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.redEnvelopePlayer.RedEnvelopePlayerActivity;
import com.hawk.activity.type.impl.redEnvelopePlayer.cfg.RedEnvelopePlayerDetailsCfg;
import com.hawk.activity.type.impl.redEnvelopePlayer.redEnvelope.PlayerRedEnvelope;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.data.RedEnvelopeMsgData;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.SendRedPacketMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.HPRedEnvelope;
import com.hawk.game.protocol.Activity.RedEnvelopePersonState;
import com.hawk.game.protocol.Activity.RedEnvelopePersonalState;
import com.hawk.game.protocol.Activity.RedEnvelopePlayerRecieve;
import com.hawk.game.protocol.Activity.RedEnvelopePlayerRecieveDetails;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.log.Action;


public class PlayerRedPacketModule extends PlayerModule {

	private static Logger logger = LoggerFactory.getLogger("Server");
	
	private static Map<String, PlayerRedEnvelope> redMap = new ConcurrentHashMap<>();
	
	/**
	 * 上次聊天信息时间
	 */
	private long lastChatMsg = 0;
	
	public PlayerRedPacketModule(Player player) {
		super(player);
	}
	
	@ProtocolHandler(code = HP.code.RED_ENVELOPE_PLAYER_SEND_VALUE)
	public void playerSendRedEnvelope(HawkProtocol protocol){
		long currTime = HawkTime.getMillisecond();
		if (currTime - lastChatMsg < GameConstCfg.getInstance().getChatTimeInterval() * 1000) {
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		HPRedEnvelope req = protocol.parseProtocol(HPRedEnvelope.getDefaultInstance());
		String chatMsg = req.getChatMsg();
		ChatType type = ChatType.valueOf(req.getChatType());
		if(type != ChatType.CHAT_WORLD && type != ChatType.CHAT_ALLIANCE){
			logger.info("RedEnvelopePlayer send type error, player:{}, name:{}, type:{}", player.getId(), player.getName(), req.getChatType());
			return;
		}
		String redEnvelopeId = createRedEnvelope(type, req);
		if(redEnvelopeId == null){
			return;
		}		
		int id = req.getId();
		RedEnvelopePlayerDetailsCfg config = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopePlayerDetailsCfg.class, id);
		final RedEnvelopeMsgData dataObject = RedEnvelopeMsgData.valueOf(redEnvelopeId, config.getNoticeID(), id, req.getChatType());
		GameTssService.getInstance().wordUicChatFilter(player, chatMsg, 
				GameMsgCategory.RED_PACKET, GameMsgCategory.RED_PACKET, 
				JsonUtils.Object2Json(dataObject), null, protocol.getType());
	}
	
	@MessageHandler
	private void onChatMsgFilterFinish(SendRedPacketMsg msg) {
		RedEnvelopeMsgData dataObject = JsonUtils.String2Object(msg.getCallbackData(), RedEnvelopeMsgData.class);
		String filterChatMsg = msg.getMsgContent();
		//重新设置红包的主题
		PlayerRedEnvelope redEnvelope = redMap.get(dataObject.getId());
		if(redEnvelope != null){
			redEnvelope.setChatInfo(filterChatMsg);
		}
		pushChatMsg(filterChatMsg, dataObject);
	}

	/***
	 * 抢红包
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.RED_ENVELOPE_PLAYER_RECIEVE_VALUE)
	public void playerRecieveRedEnvelope(HawkProtocol protocol){
		RedEnvelopePlayerRecieve info = protocol.parseProtocol(RedEnvelopePlayerRecieve.getDefaultInstance());
		String red_envelope_id = info.getId();
		//如果活动没开，直接返回错误
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(ActivityType.RED_ENVELOPE_PLAYER_VALUE);
		if(!activityOp.isPresent()){
			sendError(protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		RedEnvelopePlayerActivity activity = (RedEnvelopePlayerActivity)activityOp.get();
		if(!activity.isOpening(player.getId())){
			sendError(protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		
		addTask(new HawkTask() {
			@Override
			public Object run() {
				//去内存获取，如果没有再从redis获取
				PlayerRedEnvelope redEnvelope = redMap.get(red_envelope_id);
				if(redEnvelope == null){
					//从redis获取红包
					String key = String.format(ActivityRedisKey.RED_ENVELIPE_PLAYER, red_envelope_id);
					String msg = ActivityLocalRedis.getInstance().get(key);
					if(HawkOSOperator.isEmptyString(msg)){
						logger.info("RedEnvelopePlayer recieve error, can't find red_envelope, playerId:{}, redId:{}", player.getId(), red_envelope_id);
						return null;
					}
					redEnvelope = JsonUtils.String2Object(msg, PlayerRedEnvelope.class);
					redMap.put(redEnvelope.getId(), redEnvelope);
				}
				//给奖励
				final PlayerRedEnvelope finalRedEnvelope = redEnvelope;
				finalRedEnvelope.recieve(player.getId(), (code, rewards) -> {
					if(code == 0){
						//奖励放背包
						List<RewardItem.Builder> itemList = new LinkedList<>();
						for (String item : rewards) {
							RewardItem.Builder builder = RewardHelper.toRewardItem(item);
							if (builder != null) {
								itemList.add(builder);
							}
						}
						logger.info("RedEnvelopePlayerActivity player recieve red envelope, playerId:{}, red_envelope_id:{}, reward:{}", player.getId(), red_envelope_id, rewards.get(0));
						ActivityManager.getInstance().getDataGeter().takeReward(player.getId(), itemList, Action.RED_ENVELIPE_PLAYER, true);
						//同步领取信息给发送者
						pushRedEnvelopeLitterWords2Sender(finalRedEnvelope);
						if(finalRedEnvelope.hasRecievedOver()){
							addTask(new HawkTask() {
								@Override
								public Object run() {
									//广播此红包被领取光
									broadCastRedEnvelopeState(finalRedEnvelope);
									return null;
								}
							});
						}
						//给界面信息
						pushRedEnvelopeInfo(finalRedEnvelope);
					}else{
						//sendError(protocol.getType(), code);
						logger.info("RedEnvelopePlayerActivity player recieve red envelope error:{}", code);
						if(code == Status.Error.RED_ENVELOPE_PLAYER_EXPIRE_VALUE
								|| code == Status.Error.RED_ENVELOPE_TOUCH_LIMIT_VALUE){
							sendError(protocol.getType(), code);
						}else{
							pushRedEnvelopeInfo(finalRedEnvelope);
						}
					}
				});
				return null;
			}
		});
	}
	
	private void pushChatMsg(String chatMsg, RedEnvelopeMsgData data) {
		ChatParames params = ChatParames.newBuilder()
				.setPlayer(player)
				.setChatType(ChatType.valueOf(data.getChatType()))
				.setKey(NoticeCfgId.valueOf(data.getNoticeCfgId()))
				.addParms(chatMsg)
				.addParms(data.getId())
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(params);
		logger.info("RedEnvelopePlayer add chatMsgQueue, player:{}, name:{}, configId:{}", player.getId(), player.getName(), data.getRedId());
		lastChatMsg = HawkTime.getMillisecond();
	}
	
	/***
	 * 同步红包领取界面信息
	 * @param redEnvelope
	 */
	private void pushRedEnvelopeInfo(PlayerRedEnvelope redEnvelope){
		RedEnvelopePlayerRecieveDetails.Builder build = RedEnvelopePlayerRecieveDetails.newBuilder();
		redEnvelope.buildRecieveDetails(build);
		build.setId(redEnvelope.getCfgId());
		redEnvelope.buildSenderInfo(build);
		sendProtocol(HawkProtocol.valueOf(HP.code.RED_ENVELOPE_PLAYER_RECIEVE_INFO_VALUE, build));
		if(redEnvelope.hasRecievedOver()){
			redMap.remove(redEnvelope.getId());
		}
	}
	
	/***
	 * 推送领取小字给发红包的人
	 * @param redEnvelope
	 * @param recieveId
	 */
	private void pushRedEnvelopeLitterWords2Sender(PlayerRedEnvelope redEnvelope){
		Player receiver = GlobalData.getInstance().makesurePlayer(redEnvelope.getPlayerId());
		if(!receiver.isActiveOnline()){
			return;
		}
		
		//普通领取，RED_ENVELOPE_PLAYER_SEVTEN
		//手气王，RED_ENVELOPE_PLAYER_EIGHT
		RedEnvelopePlayerDetailsCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopePlayerDetailsCfg.class, redEnvelope.getCfgId());
		if(cfg == null){
			logger.error("pushRedEnvelopeLitterWords2Sender send details error, id:{}", redEnvelope.getCfgId());
			return;
		}
		int noticeId = 0;
		String playerName = null;
		if(redEnvelope.hasRecievedOver()){
			if (!cfg.isShow()) {
				noticeId = NoticeCfgId.RED_ENVELOPE_PLAYER_LAST_REC_VALUE;
				playerName = player.getName();
			} else {
				noticeId = NoticeCfgId.RED_ENVELOPE_PLAYER_EIGHT_VALUE;
				playerName = redEnvelope.getMaxCntPlayer();
			}
		}else{
			noticeId = NoticeCfgId.RED_ENVELOPE_PLAYER_SEVTEN_VALUE;
			playerName = player.getName();
		}
//		JSONArray array = new JSONArray();
//		array.add(playerName); //领取红包的人
//		array.add(cfg.getItemId());
//		array.add(redEnvelope.getCfgId());
		ChatType type = ChatType.valueOf(redEnvelope.getType());
//		ChatMsg.Builder chatMsgInfo = ChatService.getInstance().createMsgObj(player)
//				.setType(type.getNumber())
//				.setChatMsg(array.toJSONString()) //红包的配置,领取的人放里面(如果领完了，放手气王)
//				.setChatId(redEnvelope.getId())
//				.setNoticeId(noticeId);
		
		ChatParames params = ChatParames.newBuilder()
				.setPlayer(player)
				.setChatType(type)
				.setKey(NoticeCfgId.valueOf(noticeId))
				.addParms(playerName)
				.addParms(cfg.getItemId()) // 其实cfg中的itemid就是三段式的，只是字段名取得具有迷惑性
				.addParms(redEnvelope.getCfgId())
				.build();
		if(receiver.isActiveOnline()){
			receiver.sendProtocol(HawkProtocol.valueOf(HP.code.RED_ENVELOPE_PLAYER_WHO_RECIEVE_VALUE, params.toPBMsg().toBuilder()));
		}
	}
	
	/***
	 * 广播红包状态改变
	 * @param redEnvelope
	 */
	private void broadCastRedEnvelopeState(PlayerRedEnvelope redEnvelope){
		if(redEnvelope.getType() == ChatType.CHAT_WORLD.getNumber()){
			Set<Player> onLinePlayers = GlobalData.getInstance().getOnlinePlayers();
			sendRedEnvelopeState(redEnvelope, onLinePlayers);
		}else if(redEnvelope.getType() == ChatType.CHAT_ALLIANCE.getNumber()){
			String guildId = redEnvelope.getGuildId();
			if(guildId == null){
				logger.info("RedEnvelopePlayerActivity can't find guildId, 太巧了，玩家红包还未发出来的时候，就退盟了, playerId:{}", redEnvelope.getPlayerId());
				return;
			}
			Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
			Set<Player> onLinePlayers = memberIds.stream().map(GlobalData.getInstance()::getActivePlayer).collect(Collectors.toSet());
			sendRedEnvelopeState(redEnvelope, onLinePlayers);
		}
	}
	
	private void sendRedEnvelopeState(PlayerRedEnvelope redEnvelope, Set<Player> onlinePlayers){
		for (Player receiver : onlinePlayers) {
			if (receiver == null) {
				continue;
			}
			RedEnvelopePersonalState.Builder build = RedEnvelopePersonalState.newBuilder();
			build.setId(redEnvelope.getId());
			if(redEnvelope.hasRecieved(receiver.getId())){
				build.setState(RedEnvelopePersonState.PERSON_RECEIVED);
			}
			//已抢光的状态优先级更高
			if(redEnvelope.hasRecievedOver()){
				build.setState(RedEnvelopePersonState.PERSON_RECEIVED_END);
			}
			receiver.sendProtocol(HawkProtocol.valueOf(HP.code.RED_ENVELOPE_PLAYER_STATE_S_VALUE, build));
		}
	}
	
	private String createRedEnvelope(ChatType type, HPRedEnvelope req){
		logger.info("RedEnvelopePlayer try check is send red envelope, player:{}, name:{}, hasId:{}, id:{}", player.getId(), player.getName(), req.hasId(), req.getId());
		if(req.hasId() && req.getId() != 0){
			//判断活动是否开启
			int id = req.getId();
			if(id == 0){
				return null;
			}
			RedEnvelopePlayerDetailsCfg config = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopePlayerDetailsCfg.class, id);
			if(config == null){
				logger.error("RedEnvelopePlayer send red envelope error, player is :{}, id is error:{}", player.getId(), id);
				return null;
			}
			//通过活动模块扣道具..
			boolean rlt = ActivityManager.getInstance().getDataGeter().cost(player.getId(), config.getItemList(), Action.RED_ENVELIPE_PLAYER);
			if (!rlt) {
				logger.info("RedEnvelopePlayer send red envelope failed, item not enough, playerId: {}", player.getId());
				return null;
			}
			//构建红包
			String redId = UUID.randomUUID().toString();
			int redCnt = config.getNum();
			if(redCnt == 0){
				logger.error("RedEnvelopePlayer config error, config:{}", config.getGrouping());
				return null;
			}
			PlayerRedEnvelope playerRed = new PlayerRedEnvelope(redId, redCnt);
			playerRed.setPlayerId(player.getId());
			if(!HawkOSOperator.isEmptyString(player.getGuildId())){
				playerRed.setGuildId(player.getGuildId());
			}
			playerRed.setTime(HawkTime.getMillisecond());
			playerRed.setCfgId(config.getId());
			playerRed.setType(type.getNumber());
			playerRed.setChatInfo("");
			playerRed.splitBag();
			logger.info("RedEnvelopePlayer player:{}, send red envelope sus, configId:{}, redEnvelopeId:{}", player.getId(), config.getId(), playerRed.getId());
			//缓存红包
			redMap.put(playerRed.getId(), playerRed);
			playerRed.save2Redis(null);
			return redId;
		}else{
			logger.info("RedEnvelopePlayer, player send error msg, playerId:{}, playerName:{}", player.getId(), player.getName());
		}
		return null;
	}
	
	private void addTask(HawkTask task) {
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName("RED_ENVELOPE_PLAYER");
			taskPool.addTask(task, 0, false);
		}
	}
}
