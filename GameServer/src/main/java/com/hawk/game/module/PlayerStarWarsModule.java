package com.hawk.game.module;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.healexchange.HealExchangeActivity;
import com.hawk.activity.type.impl.healexchange.entity.HealExchangeEntity;
import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.StarWarsConstCfg;
import com.hawk.game.config.StarWarsPartCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.crossproxy.starwars.StarWarsCallbackOperationService;
import com.hawk.game.crossproxy.starwars.StarWarsPrepareEnterCallback;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.lianmengstarwars.msg.SWJoinRoomMsg;
import com.hawk.game.lianmengstarwars.msg.SWQuitReason;
import com.hawk.game.lianmengstarwars.msg.SWQuitRoomMsg;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.msg.starwars.StarWarsBackServerMsg;
import com.hawk.game.msg.starwars.StarWarsEnterCrossInstanceMsg;
import com.hawk.game.msg.starwars.StarWarsExitCrossInstanceMsg;
import com.hawk.game.msg.starwars.StarWarsMoveBackCrossPlayerMsg;
import com.hawk.game.msg.starwars.StarWarsPrepareExitCrossInstanceMsg;
import com.hawk.game.msg.starwars.StarWarsPrepareMoveBackMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.Cross.InnerEnterCrossReq;
import com.hawk.game.protocol.Cross.StarWarsCrossMsg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.SW.SWPageInfo;
import com.hawk.game.protocol.SW.StarWarsInnerBackServerReq;
import com.hawk.game.protocol.SW.StarWarsMoveBackReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsGiftAwardReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsGiftRecordReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsGiftReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsKingRecordReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOffericerChangeKingReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsSearchPlayerReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsSearchPlayerResp;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsServerIdListReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsServerIdListResp;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsServerPresidentReq;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsServerPresidentResp;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.starwars.SWRoomData;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.service.starwars.callback.SearchPlayerCallBack;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.PlayerCrossStatus;
import com.hawk.game.util.LogUtil;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst;
import com.hawk.log.LogConst.CrossStateType;

/**
 * 星球大战
 * @author jm
 *
 */
public class PlayerStarWarsModule extends PlayerModule{

	public PlayerStarWarsModule(Player player) {
		super(player);
	} 
	
	
	@Override
	protected boolean onPlayerLogin() {
		StarWarsActivityService.getInstance().syncStateInfo(player);
		StarWarsActivityService.getInstance().syncPageInfo(player);
		return super.onPlayerLogin();
	}

	/**
	 * 获取星球大战主界面信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.STAR_WAR_GET_PAGE_INFO_C_VALUE)
	private void onGetTLWPageInfo(HawkProtocol protocol) {
		SWPageInfo.Builder builder = StarWarsActivityService.getInstance().genPageInfo(player);
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.STAR_WAR_GET_PAGE_INFO_S, builder));
		}
	}
	
	/**
	 * 请求国王记录.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.STAR_WARS_KING_RECORD_REQ_VALUE)
	private void onStarWarsKingRecordReq(HawkProtocol protocol) {
		StarWarsKingRecordReq cparam = protocol.parseProtocol(StarWarsKingRecordReq.getDefaultInstance());
		StarWarsOfficerService service = StarWarsOfficerService.getInstance();		
		service.synStarWarsKingRecord(player, cparam.getPart(), cparam.getTeam());
	} 
	
	/**
	 * 请求礼包记录
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.STAR_WARS_GIFT_RECORD_REQ_VALUE)
	private void onStarWarsGiftRecordReq(HawkProtocol protocol) {
		StarWarsGiftRecordReq cparam = protocol.parseProtocol(StarWarsGiftRecordReq.getDefaultInstance());
		StarWarsOfficerService service = StarWarsOfficerService.getInstance(); 
		if (!service.isValidPart(cparam.getPart())) {
			this.sendError(protocol.getType(), Status.Error.STAR_WARS_INCORRECT_PART_VALUE);
			
			return;
		}
		service.synStarWarsGiftRecord(player, cparam.getPart());
	}
	
	@ProtocolHandler(code = HP.code.STAR_WARS_GIFT_REQ_VALUE)
	private void onStarWarsGiftSendInfoReq(HawkProtocol protocol) {
		StarWarsGiftReq req = protocol.parseProtocol(StarWarsGiftReq.getDefaultInstance());
		StarWarsOfficerService service = StarWarsOfficerService.getInstance(); 
		if (!service.isValidPart(req.getPart())) {
			this.sendError(protocol.getType(), Status.Error.STAR_WARS_INCORRECT_PART_VALUE);
			
			return;
		}
		service.synStarWarsGiftInfo(player, req.getPart());
	}
	
	/**
	 * 请求官职信息.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.STAR_WARS_OFFICER_REQ_VALUE)
	private void onStarWarsOfficerReq(HawkProtocol protocol) {
		StarWarsOfficerReq req = protocol.parseProtocol(StarWarsOfficerReq.getDefaultInstance());
		StarWarsOfficerService service = StarWarsOfficerService.getInstance();
		if (req.getReal() == 1) {
			service.tryReloadOfficer();
		}		 
		service.synStarWarsOfficer(player);
	}			
	/**
	 * 这个功能应该可能会被重用吧。时间赶 ----------
	 * @param hawkProtocol
	 */
	@ProtocolHandler(code = HP.code.STAR_WARS_SEARCH_PLAYER_REQ_VALUE)
	private void onStarWarsSearchPlayerReq(HawkProtocol hawkProtocol) {
		StarWarsSearchPlayerReq req = hawkProtocol.parseProtocol(StarWarsSearchPlayerReq.getDefaultInstance());
		if (HawkOSOperator.isEmptyString(req.getPlayerName())) {
			sendError(hawkProtocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			
			return;
		}
		
		String playerId = RedisProxy.getInstance().getPlayerIdByName(req.getPlayerName());
		
		if (HawkOSOperator.isEmptyString(playerId)) {
			this.sendError(hawkProtocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
			
			return;			
		}
		String targetServerId = GameUtil.getServerIdFromPlayerId(playerId);
		String realTargetServerId = GlobalData.getInstance().getMainServerId(targetServerId);	
		Integer part = AssembleDataManager.getInstance().getServerPart(realTargetServerId);
		if (req.getPart() != GsConst.StarWarsConst.WORLD_PART && part != req.getPart()) {
			this.sendError(hawkProtocol.getType(), Status.Error.STAR_WARS_NOT_IN_POPEDOM_VALUE);
			
			return;
		}
		
		StarWarsSearchPlayerResp.Builder sbuilder = StarWarsSearchPlayerResp.newBuilder();
		
		//如果是本服.
		if (GlobalData.getInstance().isLocalServer(realTargetServerId)) {
			Player searchPlayer = GlobalData.getInstance().makesurePlayer(playerId);
			if (searchPlayer == null) {
				this.sendError(hawkProtocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST_VALUE);
				
				return;
			}
			sbuilder.setPart(req.getPart());
			sbuilder.setPlayer(BuilderUtil.buildCrossPlayer(searchPlayer));
			HawkProtocol respProtocl = HawkProtocol.valueOf(HP.code.STAR_WARS_SEARCH_PLAYER_RESP_VALUE, sbuilder);
			player.sendProtocol(respProtocl);
		} else {
			HawkProtocol rpcProtocol = HawkProtocol.valueOf(hawkProtocol.getType(), req.toBuilder().setPlayerId(playerId));
			CrossProxy.getInstance().rpcRequest(rpcProtocol, new SearchPlayerCallBack(player), realTargetServerId, player.getId(), "");
		}
	} 
	
	@ProtocolHandler(code = HP.code.STAR_WARS_SERVERID_LIST_REQ_VALUE)
	private void onStarWarsServerIdListReq(HawkProtocol protocol) {
		StarWarsServerIdListReq req = protocol.parseProtocol(StarWarsServerIdListReq.getDefaultInstance()); 
		int part = req.getPart();
		if (!StarWarsOfficerService.getInstance().isValidPart(part)) {
			sendError(protocol.getType(), Status.Error.STAR_WARS_INCORRECT_PART_VALUE);
			
			return;
		}
		
		StarWarsPartCfg starWarsCfg = AssembleDataManager.getInstance().getServerPartCfg(GsConfig.getInstance().getServerId());
		if (starWarsCfg == null) {
			sendError(protocol.getType(), Status.Error.STAR_WARS_INCORRECT_PART_VALUE);
			
			return;
		}
		
		StarWarsServerIdListResp.Builder sbuilder = StarWarsServerIdListResp.newBuilder();
		sbuilder.setPart(part);
		for (String serverId : starWarsCfg.getServerList()) {
			if (!GlobalData.getInstance().isMainServer(serverId)) {
				continue;
			}
			
			sbuilder.addServerIdList(serverId);
		}
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.STAR_WARS_SERVERID_LIST_RESP_VALUE, sbuilder);
		player.sendProtocol(respProtocol);
	}
		
	@ProtocolHandler(code = HP.code.STAR_WARS_GIFT_AWARD_REQ_VALUE)
	private void onStarWarsGiftSendReq(HawkProtocol protocol) {
		StarWarsGiftAwardReq req = protocol.parseProtocol(StarWarsGiftAwardReq.getDefaultInstance());
		int errorCode = StarWarsOfficerService.getInstance().onStarWarsSendGift(player, req.getPart(), req.getGiftId(), req.getPlayerIdsList());
		if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
		} else {
			this.sendError(protocol.getType(), errorCode);
		}
	}
	
	@ProtocolHandler(code = HP.code.STAR_WARS_ENTER_REQ_VALUE)
	private void onEnterInstance(HawkProtocol hawkProtocol) {
		String serverId = this.getCrossToServerId();
		DungeonRedisLog.log(player.getId(), " try to enter  starWars serverId:{}", serverId);
		if (HawkOSOperator.isEmptyString(serverId)) {
			this.sendError(hawkProtocol.getType(), Status.Error.STAR_WARS_HAS_NO_MATCH_INFO_VALUE);
			
			return;
		}
		
		StarWarsConstCfg starWarsConstCfg = StarWarsConstCfg.getInstance();
		boolean addLoingPlayer = false;
		try {
			int errorCode = sourceCheckEnterInstance(serverId);
			if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
				this.sendError(hawkProtocol.getType(), errorCode);			
				return;
			}
			
			//这里看下战斗服的数量.						
			if (starWarsConstCfg.getMaxWaitLoginingPlayer() != 0) {
				long waitPlayer = RedisProxy.getInstance().increaseStarWarsLoginingPlayer(StarWarsActivityService.getInstance().getRedisStateName());
				if (waitPlayer >= starWarsConstCfg.getMaxWaitLoginingPlayer()) {
					StarWarsActivityService.getInstance().tryDeincreseLoginingPlayer();
					StarWarsActivityService.getInstance().removeJoinPlayer(player.getGuildId(), player.getId());
					this.sendError(hawkProtocol.getType(), Status.Error.STAR_WARS_BATTLE_SERVER_BUSY_VALUE);
					return;
				}							
			}
			addLoingPlayer = true;
			WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
			//这里默认都当这个时间是有效的因为前面已经判断了是不是在战斗状态.
			long shieldEndTime = StarWarsActivityService.getInstance().getWarLeftTime() + HawkTime.getMillisecond();  
			if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() < shieldEndTime) {
				player.addStatusBuff(GameConst.CITY_SHIELD_BUFF_ID, shieldEndTime);
			}
			// 激活大帝战兑换活动
			healActivityActive();
			
			if (isCrossToSelf(serverId)) {
				//如果是本服的则处理.
				simulateCross();
			} else {			
				boolean leaveResult = sourceDoLeaveForCross(serverId);
				if (!leaveResult) {
					//失败之后需要减一,成功则说明已经发起了一个rpc的调用,我们可以在rpc调用那里去加减一.
					StarWarsActivityService.getInstance().tryDeincreseLoginingPlayer();
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			//如果已经设置了
			if (addLoingPlayer) {
				StarWarsActivityService.getInstance().tryDeincreseLoginingPlayer();
			}
			//防止异常情况下这个值
			StarWarsActivityService.getInstance().removeJoinPlayer(player.getGuildId(), player.getId());
		}	
			
	}
	
	@MessageHandler
	private void onJoinRoomMsg(SWJoinRoomMsg msg) {
		healActivityActive(); // 方便测试活动
	}

	/** 激活兑换活动*/
	private void healActivityActive() {
		try {
			Optional<ActivityBase> optionalActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.HEAL_EXCHANGE_VALUE);
			if (!optionalActivity.isPresent()) {
				return;
			}
			HealExchangeActivity hactivity = (HealExchangeActivity) optionalActivity.get();
			Optional<HealExchangeEntity> opDataEntity = hactivity.getPlayerDataEntity(player.getId());
			if (!opDataEntity.isPresent()) {
				return;
			}
			HealExchangeEntity entity = opDataEntity.get();
			if (entity.getActive() > 0) {
				return;
			}
			entity.setActive(1);
			hactivity.syncActivityDataInfo(player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	private boolean isCrossToSelf(String crossToServerId) {		
		return GsConfig.getInstance().getServerId().equals(crossToServerId);
	}
	
	private void simulateCross() {
		player.sendProtocol(HawkProtocol.valueOf(CHP.code.COMMON_CROSS_SIMULATE_BEGIN_VALUE));
		boolean rlt = StarWarsActivityService.getInstance().enterRoom(player);
		Player.logger.info("playerId:{} enter local starwar instance result:{}", player.getId(), rlt);
		
	}
	
	private void simulateExitCross() {
		StarWarsActivityService.getInstance().removeJoinPlayer(player.getGuildId(), player.getId());
		player.sendProtocol(HawkProtocol.valueOf(CHP.code.COMMON_CROSS_SIMULATE_BACK_FINISH));
	}

	private boolean sourceDoLeaveForCross(String targetServerId) {
		HawkSession session = player.getSession();
		SessionClosedMsg closeMsg = SessionClosedMsg.valueOf();
		closeMsg.setTarget(player.getXid());
		player.onMessage(closeMsg);
		
		//不入侵原来的逻辑只能再这里把Session加回来.		
		player.setSession(session);
		//减掉在线.
		GlobalData.getInstance().changePfOnlineCnt(player, true);
		
		//移除当前服的在线信息.
		RedisProxy.getInstance().removeOnlineInfo(player.getOpenId());
		
		//做一些处理,然后发起请求.
		int tryCrossErrorCode = Status.SysError.EXCEPTION_VALUE;
		tryCross:
		{
			//把数据刷到redis里面.
			boolean flushToDb = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, false);
			if (!flushToDb) {
				Player.logger.error("player enter cross flush reids error playerId:{}", player.getId());	
				DungeonRedisLog.log(player.getId() , " enter cross flush reids error ");
				break tryCross;
			}
			
			//序列化工会的数据.
			try {
				GuildService.getInstance().serializeGuild4Cross(player.getGuildId());
				player.getData().serialData4Cross();
			} catch (Exception e) {
				HawkException.catchException(e);
				break tryCross;
			}	 		
			
			boolean  setStatus = RedisProxy.getInstance().setPlayerCrossStatus(GsConfig.getInstance().getServerId(), player.getId(), GsConst.PlayerCrossStatus.PREPARE_CROSS);
			if (!setStatus) {
				DungeonRedisLog.log(player.getId() , "player set cross status fail  ");
				break tryCross;
			}
			
			tryCrossErrorCode = Status.SysError.SUCCESS_OK_VALUE;
		}			
		
		DungeonRedisLog.log(player.getId() , "starWars condtion check  playerId:{}, errorCode:{}, serverId:{}", player.getId(), tryCrossErrorCode, targetServerId);
		
		if (tryCrossErrorCode == Status.SysError.SUCCESS_OK_VALUE) {
			StarWarsCrossMsg.Builder msgBuilder = StarWarsCrossMsg.newBuilder();
			msgBuilder.setServerId(targetServerId);
			InnerEnterCrossReq.Builder builder = InnerEnterCrossReq.newBuilder();
			builder.setCurTime(HawkTime.getSeconds());
			builder.setCrossType(CrossType.STAR_WARS);
			player.setCrossStatus(PlayerCrossStatus.PREPARE_CROSS);
			//发起一个远程RPC到远程去,
			HawkProtocol protocl = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_REQ, builder);			
			CrossProxy.getInstance().rpcRequest(protocl, new StarWarsPrepareEnterCallback(player, msgBuilder.build()), targetServerId, player.getId(), "");
			return true;
		} else {			
			StarWarsCallbackOperationService.getInstance().onPrepareCrossFail(player);
			return false;			
		}			
	}

	private int sourceCheckEnterInstance(String serverId) {		
		if (!player.hasGuild()) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		
		// 有行军
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if (!marchs.isEmpty()) {
			return Status.Error.STAR_WARS_HAS_MARCH_VALUE;
		}

		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			return Status.Error.STAR_WARS_IN_WAR_FEVER_VALUE;
		}

		// 城内有援助行军，不能进入
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			return Status.Error.STAR_WARS_HAS_ASSISTANCE_MARCH_VALUE;
		}

		// 有被动行军
		BlockingQueue<IWorldMarch> passiveMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
		if (!CollectionUtils.isEmpty(passiveMarchs)) {
			int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
			for (IWorldMarch march : passiveMarchs) {
				if (march == null || march.getMarchEntity() == null || march.getMarchEntity().isInvalid()) {
					continue;
				}
				if (march.getTerminalId() != playerPos) {
					continue;
				}
				return Status.Error.STAR_WARS_HAS_PASSIVE_MARCH_VALUE;
			}
		}

		//着火也不行
		if (player.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()) {
			return Status.Error.STAR_WARS_ON_FIRE_VALUE;
		}

		if (player.getLmjyState() != null) {
			return Status.Error.LMJY_BAN_OP_VALUE;
		}

		// 在联盟军演组队中不能.
		if (WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
			return Status.Error.LMJY_BAN_OP_VALUE;
		}

		// 已经在跨服中了.
		if (CrossService.getInstance().isCrossPlayer(player.getId())) {
			return Status.CrossServerError.CROSS_FIGHTERING_VALUE;
		}

		// 看下区服有没有开.
		if (!CrossService.getInstance().isServerOpen(serverId)) {
			return Status.CrossServerError.CROSS_SERVER_NOT_ACTIVE_VALUE;
		}
		
		//如果是已经在副本里面了
		if (player.isInDungeonMap()) {
			return Status.Error.PLAYER_IN_INSTANCE_VALUE;
		}
		
		//判断一下玩家所在的part 然后看下是否可以进入.
		int part = AssembleDataManager.getInstance().getServerPart(GsConfig.getInstance().getServerId());
		if (part < 0) {
			return Status.Error.STAR_WARS_INCORRECT_PART_VALUE;
		}
		
		int checkResult = StarWarsActivityService.getInstance().checkEnterCondition(player);
		if (checkResult != Status.SysError.SUCCESS_OK_VALUE) {
			return checkResult;
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	private String getCrossToServerId() {
		SWRoomData roomData = StarWarsActivityService.getInstance().getCurrRoomData(player);
		if (roomData == null) {
			return null;
		}
		return roomData.getRoomServerId();
	}
	
	/**
	 * 从GM指令发过来一个签回玩家的指令.
	 * @param msg
	 */
	@MessageHandler
	public void sourceOnPrepareMoveBack(StarWarsPrepareMoveBackMsg msg) {
		String toServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
		if (HawkOSOperator.isEmptyString(toServerId)) {
			DungeonRedisLog.log(player.getId() , "playerId:{} prepare force move back toServerId is null ", player.getId());
			return;
		}
		
		DungeonRedisLog.log(player.getId() , "playerId:{} prepare move back toServerId:{}", player.getId(), toServerId);
		StarWarsMoveBackReq.Builder req = StarWarsMoveBackReq.newBuilder(); 
		req.setPlayerId(player.getId());
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.STAR_WARS_MOVE_BACK_REQ_VALUE, req);
		CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, player.getId(), "");
	}
	
	@MessageHandler 
	public void sourceOnBackServer(StarWarsBackServerMsg msg) {
		DungeonRedisLog.log(player.getId(), "starwars corss player back server ");
		//在发起退出的时候强行把数据序列化到redis中，返回之后再读一次.
		PlayerDataSerializer.csSyncPlayerData(player.getId(), true);
		//修改顺序 先把数据反序列化回来再移除.
		String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());		
		
		//清理玩家的加入信息
		StarWarsActivityService.getInstance().removeJoinPlayer(player.getGuildId(), player.getId());
		
		toServerId = toServerId == null ? "NULL" : toServerId;
		LogUtil.logPlayerCross(player, toServerId, CrossStateType.CROSS_EXIT, CrossType.STAR_WARS);
		
		//只有玩家在线的时候才走登录流程.
		if (player.getSession() != null && player.getSession().isActive()) {
			//模拟login协议需要的策数据.
			AccountInfo accoutnInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
			accoutnInfo.setLoginTime(HawkTime.getMillisecond());
			
			HPLogin.Builder cloneHpLoginBuilder = player.getHpLogin().clone();
			cloneHpLoginBuilder.setFlag(1);
			HawkProtocol loginProtocol = HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, cloneHpLoginBuilder);
			player.getSession().setUserObject("account", accoutnInfo);
			loginProtocol.bindSession(player.getSession());		
			player.onProtocol(loginProtocol);
			//在login的时候会加，所以这减掉
			GlobalData.getInstance().changePfOnlineCnt(player, false);
		} else {
			//回原服的时候更新一下activeServer;
			GameUtil.updateActiveServer(player.getId(), GsConfig.getInstance().getServerId());
		}
		
		
		// 通知客户端跨服返回完成
		player.sendProtocol(HawkProtocol.valueOf(HP.code.STAR_WARS_CROSS_BACK_FINISH_VALUE));
		
		// 设置跨服返回时间
		player.setCrossBackTime(HawkTime.getMillisecond());
		
		RankService.getInstance().checkCityLvlRank(player);
		RankService.getInstance().checkPlayerLvlRank(player);
	}
	
	/**
	 * 迁回的预处理.
	 * @author  jm 
	 */	
	public void targetDoPrepareExitCrossInstance() {
		// 通知客户端跨服开始
		player.sendProtocol(HawkProtocol.valueOf(HP.code.STAR_WARS_CROSS_BACK_BEGIN));
		
		//检测到可以退出了那么先设置标志位
		player.setCrossStatus(GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		
		//添加到检测队列
		CrossService.getInstance().addExitStarWarsPlayer(player.getId());
		
		//移除跨服玩家相关数据
		GuildService.getInstance().onCsPlayerOut(player);
		//移除跨服记录的装扮信息
		WorldPointService.getInstance().removeShowDress(player.getId());
		WorldPointService.getInstance().removePlayerSignature(player.getId());
		WorldPointService.getInstance().removeShowEquipTech(player.getId());
		WorldPointService.getInstance().removeCollegeNameShow(player.getId());
		//cs player里面有个check exist 需要设置这个状态.假装行军都已经完成了.
		player.setCrossStatus(GsConst.PlayerCrossStatus.EXIT_CROSS_MARCH_FINAL);
		//清理守护信息.
		RelationService.getInstance().onPlayerExitCross(player.getId());
	}
	
	/**
	 * A->B
	 * 客户端触发A转发B处理
	 * 
	 * @return
	 */
	private void targetDoExitInstance() {
		DungeonRedisLog.log(player.getId(), "target do exit instance  ");
		//设置状态,
		player.setCrossStatus(GsConst.PlayerCrossStatus.EXIT_CROSS);		
		
		//调用一个close结算玩家的状态.
		try {
			SessionClosedMsg closeMsg = SessionClosedMsg.valueOf();
			closeMsg.setTarget(player.getXid());
			player.onMessage(closeMsg);
			
			//删除保护罩
			CityManager.getInstance().removeCityShieldInfo(player.getId());
		} catch (Exception e) {
			//报错也要执行完.
			HawkException.catchException(e);
		}
		
		int errorCode = Status.SysError.SUCCESS_OK_VALUE;		
		//这种用异常不好控制.
		operationCollection:
		{
			//刷新玩家的数据到redis, 失败退出.
			boolean flushToRedis = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, true);
			if (!flushToRedis) {
				Player.logger.error("starwars csplayer exit cross flush to redis fail ", player.getId());
				
				break operationCollection;
			}
		}
		
		Player.logger.info("starwars playerId:{}, exit cross  errorCode:{}", player.getId(), errorCode);
							
		//把玩家在本服的痕迹清理掉
		String fromServerId = CrossService.getInstance().removeImmigrationPlayer(player.getId());
		GlobalData.getInstance().removeAccountInfoOnExitCross(player.getId());
		HawkApp.getInstance().removeObj(player.getXid());
		GlobalData.getInstance().invalidatePlayerData(player.getId());					
		
		StarWarsInnerBackServerReq.Builder req = StarWarsInnerBackServerReq.newBuilder();
		req.setPlayerId(player.getId());
		//发送一个协会回原服.
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(HP.code.STAR_WARS_INNER_BACK_SERVER_REQ_VALUE, req), fromServerId, "");
		String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
		//设置redis状态, 都已经到了这一步了，失败了就失败了，也不能怎么样了.
		boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(mainServerId, player.getId(), GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		if (!setCrossStatus) {
			Player.logger.error("starwars player exit cross set cross status fail playerId:{}", player.getId());
		}
		
	}
		
	/**
	 * 预退出跨服
	 * @param msg
	 */
	@MessageHandler
	public void targetOnPrepareExitCrossMsg(StarWarsPrepareExitCrossInstanceMsg msg) {
		DungeonRedisLog.log(player.getId(), "starwars prepare exit cross Instance from msg");
		targetDoPrepareExitCrossInstance();		
	}
	
	/**
	 * 发出退出跨服信息.
	 * @param msg
	 */
	@MessageHandler
	public void targetOnExitInstanceMessage(StarWarsExitCrossInstanceMsg msg) {
		DungeonRedisLog.log(player.getId(), "starwars exitInstance from msg");
		targetDoExitInstance();		
	}
	
	/**
	 * 发出进入跨服信息.
	 * @param msg
	 */
	@MessageHandler
	public void targetOnEnterInstanceMessage(StarWarsEnterCrossInstanceMsg msg) {
		//写死跨服ID
		String serverId = this.getCrossToServerId();
				
		DungeonRedisLog.log(player.getId(), "try to enter  starWars serverId:{}", player.getId(), serverId == null ? "null" : serverId);
		if (HawkOSOperator.isEmptyString(serverId)) {
			//this.sendError(hawkProtocol.getType(), Status.Error.STAR_WARS_HAS_NO_MATCH_INFO_VALUE);
			return;
		}
		
		boolean addLoingPlayer = false;
		try {
			//不要所有判断条件，直接进跨服
			boolean leaveResult = sourceDoLeaveForCross(serverId);
			if (!leaveResult) {
				//失败之后需要减一,成功则说明已经发起了一个rpc的调用,我们可以在rpc调用那里去加减一.
				StarWarsActivityService.getInstance().tryDeincreseLoginingPlayer();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			//如果已经设置了
			if (addLoingPlayer) {
				StarWarsActivityService.getInstance().tryDeincreseLoginingPlayer();
			}
			//防止异常情况下这个值
			StarWarsActivityService.getInstance().removeJoinPlayer(player.getGuildId(), player.getId());
		}		
	}
	
	@ProtocolHandler(code = HP.code.STAR_WARS_EXIT_REQ_VALUE)
	public void onExitInstance(HawkProtocol hawkProtocol) {
		DungeonRedisLog.log(player.getId(), "exit star wars from protocol");
		if (player.isCsPlayer()) {
			//远程操作.
			CsPlayer csPlayer = player.getCsPlayer();
			if (!csPlayer.isCrossType(CrossType.STAR_WARS_VALUE)) {
				csPlayer.sendError(hawkProtocol.getType(), Status.Error.STAR_WARS_NOT_IN_INSTANCE_VALUE, 0);
				return;
			}
			targetDoPrepareExitCrossInstance();
		}else{
			//本地操作.
			simulateExitCross();
		}
	}
	
	/**
	 * 迁回。
	 * @param msg
	 */
	@MessageHandler
	public void targetOnMoveBack(StarWarsMoveBackCrossPlayerMsg msg) {
		DungeonRedisLog.log(player.getId(), "receive move back");
		if (!player.isCsPlayer()) {
			Player.logger.error("starwars player isn't csplayer can not receive this protocol playerId:{}", player.getId());
			
			return ;
		}
	 		
		//在线的话, 尝试踢下线.
		if (player.isActiveOnline()) {
			player.notifyPlayerKickout(Status.SysError.ADMIN_OPERATION_VALUE, null);
		}
		
		//加入到退出跨服
		targetDoPrepareExitCrossInstance();
	}
	
	/**
	 * 退出战场房间
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onQuitRoomMsg(SWQuitRoomMsg msg) {
		try {
			boolean isMidwayQuit = msg.getQuitReason() == SWQuitReason.LEAVE;
			StarWarsActivityService.getInstance().quitRoom(player, isMidwayQuit);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {
			CrossService.getInstance().addForceMoveBackStarWarsPlayer(player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		
		return true;
	}
	
	@ProtocolHandler(code = HP.code.STAR_WARS_OFFERICER_CHANGE_KING_REQ_VALUE)
	private void onStarWarsChangeKing(HawkProtocol hawkProtocol) {
		StarWarsOffericerChangeKingReq req = hawkProtocol.parseProtocol(StarWarsOffericerChangeKingReq.getDefaultInstance());
		StarWarsOfficerService service = StarWarsOfficerService.getInstance();
		int result = service.onSetWorldKing(player, req.getTargetPlayerId());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(hawkProtocol.getType());
		} else {
			this.sendError(hawkProtocol.getType(), result);
		}
	}
	
	@ProtocolHandler(code = HP.code.STAR_WARS_SERVER_PRESIDENT_REQ_VALUE)
	private void onStarWarsJoinGuildLeaderReq(HawkProtocol hawkProtocol) {
		StarWarsServerPresidentReq cparam = hawkProtocol.parseProtocol(StarWarsServerPresidentReq.getDefaultInstance());
		List<CrossPlayerStruct> playerList = RedisProxy.getInstance().getStarWarsJoinGuildLeader(cparam.getServerIdsList());
		StarWarsServerPresidentResp.Builder sbuilder = StarWarsServerPresidentResp.newBuilder();
		sbuilder.addAllJoinGuildLeaderList(playerList);
		HawkProtocol respProtocol = HawkProtocol.valueOf(HP.code.STAR_WARS_SERVER_PRESIDENT_RESP_VALUE, sbuilder);
		player.sendProtocol(respProtocol);
	}
}
