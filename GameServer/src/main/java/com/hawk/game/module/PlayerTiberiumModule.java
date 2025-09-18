package com.hawk.game.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.hawk.game.protocol.*;
import com.hawk.game.service.guildTeam.model.GuildTeamRoomData;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tiberium.*;
import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.crossproxy.tiberiumwar.TiberiumCallbackOperationService;
import com.hawk.game.crossproxy.tiberiumwar.TiberiumPrepareEnterCallback;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.tiberium.TWEditMemberTargetInvoker;
import com.hawk.game.invoker.tiberium.TWEditTeamTargetInvoker;
import com.hawk.game.invoker.tiberium.TWTeamMemberManageInvoker;
import com.hawk.game.invoker.tiberium.TWTeamMemberResetInvoker;
import com.hawk.game.module.lianmengtaiboliya.msg.QuitReason;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYQuitRoomMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.msg.tiberium.TiberiumBackServerMsg;
import com.hawk.game.msg.tiberium.TiberiumExitCrossInstanceMsg;
import com.hawk.game.msg.tiberium.TiberiumMoveBackCrossPlayerMsg;
import com.hawk.game.msg.tiberium.TiberiumPrepareExitCrossInstanceMsg;
import com.hawk.game.msg.tiberium.TiberiumPrepareMoveBackMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.Cross.InnerEnterCrossReq;
import com.hawk.game.protocol.Cross.TiberiumCrossMsg;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.TiberiumWar.GetTLWGuildRankResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetFinalMatchInfoReq;
import com.hawk.game.protocol.TiberiumWar.TLWGetFinalMatchInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoReq;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetOBRoomInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGetScoreInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetTeamGuildInfoReq;
import com.hawk.game.protocol.TiberiumWar.TLWGetTeamGuildInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWSelfMatchList;
import com.hawk.game.protocol.TiberiumWar.TWBattleLog;
import com.hawk.game.protocol.TiberiumWar.TWEditMemberTarget;
import com.hawk.game.protocol.TiberiumWar.TWEditTeamName;
import com.hawk.game.protocol.TiberiumWar.TWEditTeamTarget;
import com.hawk.game.protocol.TiberiumWar.TWGetHistoryResp;
import com.hawk.game.protocol.TiberiumWar.TWGetTeamInfo;
import com.hawk.game.protocol.TiberiumWar.TWGetTeamSummaryResp;
import com.hawk.game.protocol.TiberiumWar.TWPlayerList;
import com.hawk.game.protocol.TiberiumWar.TWPlayerManage;
import com.hawk.game.protocol.TiberiumWar.TWSignUpReq;
import com.hawk.game.protocol.TiberiumWar.TWTeamInfo;
import com.hawk.game.protocol.TiberiumWar.TWTeamManageInfoReq;
import com.hawk.game.protocol.TiberiumWar.TWTeamManageInfoResp;
import com.hawk.game.protocol.TiberiumWar.TWTeamMemberManage;
import com.hawk.game.protocol.TiberiumWar.TWTeamMemberReset;
import com.hawk.game.protocol.TiberiumWar.TiberiumWarInnerBackServerReq;
import com.hawk.game.protocol.TiberiumWar.TiberiumWarMoveBackReq;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.tiberium.TiberiumConst.TLWActivityState;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.PlayerCrossStatus;
import com.hawk.game.util.LogUtil;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.LogConst.CrossStateType;

/**
 * 泰伯利亚之战
 * @author Jesse
 */
public class PlayerTiberiumModule extends PlayerModule {

	public PlayerTiberiumModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		TBLYWarService.getInstance().syncPageInfo(player);
		TBLYSeasonService.getInstance().syncPageInfo(player);
		return true;
	}
	
	/**
	 * 请求界面信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_GET_PAGE_INFO_C_VALUE)
	private void onGetPageInfo(HawkProtocol protocol) {
		TBLYWarService.getInstance().syncPageInfo(player);
//		TWPageInfo.Builder stateInfo = TiberiumWarService.getInstance().genPageInfo(player);
//		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_GET_PAGE_INFO_S, stateInfo));
	}
	
	/**
	 * 报名
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_SIGN_UP_C_VALUE)
	private void onSignUp(HawkProtocol protocol) {
		TWSignUpReq req = protocol.parseProtocol(TWSignUpReq.getDefaultInstance());
		int index = req.getTimeIndex();
		int result = TiberiumWarService.getInstance().signUp(player, index);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(protocol.getType());
		} else {
			sendError(protocol.getType(), result);
		}
	}
	
	/**
	 * 获取成员列表
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_GET_PLAYER_LIST_C_VALUE)
	private void onGetPlayerList(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return;
		}
		TWPlayerList.Builder builder = TiberiumWarService.getInstance().getMemberList(player.getGuildId());
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_GET_PLAYER_LIST_S, builder));
		}
	}
	
	/**
	 * 管理参战成员
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_PLAYER_MANAGE_C_VALUE)
	private void onManagePlayer(HawkProtocol protocol) {
		TWPlayerManage req = protocol.parseProtocol(TWPlayerManage.getDefaultInstance());
		int result = TiberiumWarService.getInstance().updateMemberList(player, req);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			TWPlayerManage.Builder builder = TWPlayerManage.newBuilder();
			builder.setType(req.getType());
			builder.setPlayerId(req.getPlayerId());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_PLAYER_MANAGE_S, builder));
		} else {
			sendError(protocol.getType(), result);
		}
	}
	
	/**
	 * 获取本盟历史战报
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_GET_HISTORY_C_VALUE)
	private void onGetBattleLogs(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return;
		}
		List<TWBattleLog> logList = new ArrayList<>();
		List<TWBattleLog> logList1 = RedisProxy.getInstance().getTWBattleLog(player.getGuildId()+":1",20);
		List<TWBattleLog> logList2 = RedisProxy.getInstance().getTWBattleLog(player.getGuildId()+":2",20);
		for(int i = 0; i < 20; i++){
			if(logList1.size() > i){
				logList.add(logList1.get(i));
			}
			if(logList2.size() > i){
				logList.add(logList2.get(i));
			}
		}
		//TWGuildEloData eloData = RedisProxy.getInstance().getTWGuildElo(player.getGuildId());
		TWGetHistoryResp.Builder builder = TWGetHistoryResp.newBuilder();
		builder.addAllBattleLog(logList);
//		if (eloData != null) {
//			builder.setEloScore(eloData.getScore());
//		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_GET_HISTORY_S, builder));
	}
	
	
	/**
	 * 退出联盟
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onGuildQuitMsg(GuildQuitMsg msg) {
		return true;
	}
	
	/**
	 * 退出战场房间
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onQuitRoomMsg(TBLYQuitRoomMsg msg) {
		try {
			boolean isMidwayQuit = msg.getQuitReason() == QuitReason.LEAVE;
			if(TBLYSeasonService.getInstance().isInSeason(player)){
				TBLYSeasonService.getInstance().quitRoom(player, isMidwayQuit);
			}else {
				TBLYWarService.getInstance().quitRoom(player, isMidwayQuit);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {
			CrossService.getInstance().addForceMoveBackTiberiumPlayer(player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		
		return true;
	}
	
	
	/**
	 * 迁回的预处理.
	 * @author  jm 
	 */	
	public void targetDoPrepareExitCrossInstance() {
		// 通知客户端跨服开始
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_CROSS_BACK_BEGIN));
		
		//检测到可以退出了那么先设置标志位
		player.setCrossStatus(GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		
		//添加到检测队列
		CrossService.getInstance().addExitTiberiumPlayer(player.getId());
		
		//移除跨服玩家相关数据
		GuildService.getInstance().onCsPlayerOut(player);
		//移除跨服记录的装扮信息
		WorldPointService.getInstance().removeShowDress(player.getId());
		WorldPointService.getInstance().removePlayerSignature(player.getId());
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
		Player.logger.info("target do exit instance playerId:{}", player.getId());
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
				Player.logger.error("csplayer exit cross flush to redis fail ", player.getId());
				
				break operationCollection;
			}
		}
		
		Player.logger.info("tiberium playerId:{}, exit cross  errorCode:{}", player.getId(), errorCode);
							
		//把玩家在本服的痕迹清理掉
		String fromServerId = CrossService.getInstance().removeImmigrationPlayer(player.getId());
		GlobalData.getInstance().removeAccountInfoOnExitCross(player.getId());
		HawkApp.getInstance().removeObj(player.getXid());
		GlobalData.getInstance().invalidatePlayerData(player.getId());					
		
		TiberiumWarInnerBackServerReq.Builder req = TiberiumWarInnerBackServerReq.newBuilder();
		req.setPlayerId(player.getId());
		//发送一个协会回原服.
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_INNER_BACK_SERVER_REQ_VALUE, req), fromServerId, "");
		String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
		//设置redis状态, 都已经到了这一步了，失败了就失败了，也不能怎么样了.
		boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(mainServerId, player.getId(), GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		if (!setCrossStatus) {
			Player.logger.error("tiberium player exit cross set cross status fail playerId:{}", player.getId());
		}
		
	}
		
	/**
	 * 预退出跨服
	 * @param msg
	 */
	@MessageHandler
	public void targetOnPrepareExitCrossMsg(TiberiumPrepareExitCrossInstanceMsg msg) {
		HawkLog.logPrintln("tiberium prepare exit cross Instance from msg playerId:{}", player.getId());
		targetDoPrepareExitCrossInstance();		
	}
	
	/**
	 * 发出退出跨服信息.
	 * @param msg
	 */
	@MessageHandler
	public void targetOnExitInstanceMessage(TiberiumExitCrossInstanceMsg msg) {
		HawkLog.logPrintln("tiberium exitInstance from msg playerId:{}", player.getId());
		targetDoExitInstance();		
	}
	
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_EXIT_INSTANCE_REQ_VALUE)
	public void onExitInstance(HawkProtocol hawkProtocol) {
		Player.logger.info("playerId:{} exit tiberium war from protocol", player.getId());
		if (player.isCsPlayer()) {
			//远程操作.
			CsPlayer csPlayer = player.getCsPlayer();
			if (!csPlayer.isCrossType(CrossType.TIBERIUM_VALUE)) {
				csPlayer.sendError(hawkProtocol.getType(), Status.Error.TIBERIUM_NOT_IN_INSTANCE_VALUE, 0);
				
				return;
			}
			targetDoPrepareExitCrossInstance();
		} else {
			//本地操作.
			simulateExitCross();
		}
	}
	
	private void simulateCross() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_SIMULATE_CROSS_BEGIN_VALUE));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_SIMULATE_CROSS_FINISH_VALUE));
				if(TBLYSeasonService.getInstance().isInSeason(player)){
					boolean rlt = TBLYSeasonService.getInstance().joinRoom(player);
					Player.logger.info("playerId:{} enter local tiberium instance result:{}", player.getId(), rlt);
				}else {
					boolean rlt = TBLYWarService.getInstance().joinRoom(player);
					Player.logger.info("playerId:{} enter local tiberium instance result:{}", player.getId(), rlt);
				}

			}
		});
	}
	
	private void simulateExitCross() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_SIMULATE_CROSS_BACK_BEGIN));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_SIMULATE_CROSS_BACK_FINISH_VALUE));				
			}
		});
	}

	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE)
	public void onEnterInstance(HawkProtocol hawkProtocol) {	
//		HawkTuple2<String, Integer> tuple = getCrossToServerId();
//		int status = tuple.second;
//		if (status != Status.SysError.SUCCESS_OK_VALUE) {
//			Player.logger.info("playerId:{} try to enter tiberium war failed errorCode:{}", player.getId(), status);
//			this.sendError(hawkProtocol.getType(), status);
//			return;
//		}

		if(TBLYSeasonService.getInstance().isInSeason(player)){
			if(!TBLYSeasonService.getInstance().checkEnter(player)){
				Player.logger.info("playerId:{} checkEnter tiberium season fail ", player.getId());
				return;
			}
		}else {
			if(!TBLYWarService.getInstance().checkEnter(player)){
				Player.logger.info("playerId:{} checkEnter tiberium fail ", player.getId());
				return;
			}
		}
		String serverId =  getCrossToServerId();
		Player.logger.info("playerId:{} try to enter tiberium war serverId:{}", player.getId(), serverId == null ? "null" : serverId);
		if (HawkOSOperator.isEmptyString(serverId)) {
			this.sendError(hawkProtocol.getType(), Status.Error.TIBERIUM_HAS_NO_MATCH_INFO_VALUE);
			return;
		}
		int errorCode = sourceCheckEnterInstance(serverId);
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(hawkProtocol.getType(), errorCode);

			return;
		}

		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		long shieldEndTime = HawkTime.getMillisecond()  + TiberiumConstCfg.getInstance().getWarOpenTime() + GameConst.TBLY_PROTECTE_TIME * 1000;
		if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() < shieldEndTime) {
			StatusDataEntity entity = player.addStatusBuff(GameConst.CITY_SHIELD_BUFF_ID, shieldEndTime);
			if (entity != null) {
				player.getPush().syncPlayerStatusInfo(false, entity);
			}
		}
		
		if (isCrossToSelf(serverId)) {
			//如果是本服的则处理.
			simulateCross();
		} else {			
			boolean rlt = sourceDoLeaveForCross(serverId);
			if (!rlt) {
				player.sendError(hawkProtocol.getType(), Status.SysError.EXCEPTION_VALUE, 0);
			} else {
				player.responseSuccess(hawkProtocol.getType());
			}
		}
	}

	/**
	 * 获得目标服Id
	 * @return 目标服Id
	 */
	private String getCrossToServerId() {
		if(TBLYSeasonService.getInstance().isInSeason(player)){
			GuildTeamRoomData roomData = TBLYSeasonService.getInstance().getRoomData(player);
			if(roomData == null){
				return null;
			}
			return roomData.roomServerId;
		}else {
			GuildTeamRoomData roomData = TBLYWarService.getInstance().getRoomData(player);
			if(roomData == null){
				return null;
			}
			return roomData.roomServerId;
		}
	}


	/**
	 * 跨服前原服检查
	 * @param serverId 联盟id
	 * @return 检查结果
	 */
	private int sourceCheckEnterInstance(String serverId) {
		// 有行军
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if (!marchs.isEmpty()) {
			return Status.XHJZError.XHJZ_HAS_MARCH_VALUE;
		}

		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			return Status.XHJZError.XHJZ_IN_WAR_FEVER_VALUE;
		}

		// 城内有援助行军，不能进入泰伯利亚
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), World.WorldMarchType.ASSISTANCE_VALUE,
				World.WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			return Status.XHJZError.XHJZ_HAS_ASSISTANCE_MARCH_VALUE;
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
				return Status.XHJZError.XHJZ_HAS_PASSIVE_MARCH_VALUE;
			}
		}

		// 着火也不行
		if (player.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()) {
			return Status.XHJZError.XHJZ_ON_FIRE_VALUE;
		}

		// 在联盟军演组队中不能.
		if (WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
			return Status.Error.LMJY_BAN_OP_VALUE;
		}

		// 已经在跨服中了.
		if (CrossService.getInstance().isCrossPlayer(player.getId())) {
			return Status.CrossServerError.CROSS_FIGHTERING_VALUE;
		}

		//已经在副本中了
		if (player.isInDungeonMap()) {
			Player.logger.error("playerId:{} has in instance:{}", player.getId(), player.getDungeonMap());
			return Status.Error.PLAYER_IN_INSTANCE_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

//	/**
//	 * 检测是否可以进入副本
//	 * @param serverId
//	 * @return
//	 */
//	private int sourceCheckEnterInstance(String serverId) {
//		boolean isLeaguaWar = TiberiumLeagueWarService.getInstance().isJointLeaguaWar(player);
//		if (!player.hasGuild()) {
//			return Status.Error.GUILD_NO_JOIN_VALUE;
//		}
//
//		int termId = TiberiumWarService.getInstance().getTermId();
//		if (isLeaguaWar) {
//			termId = TiberiumLeagueWarService.getInstance().getActivityInfo().getMark();
//		}
//		// 有行军
//		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
//		if (!marchs.isEmpty()) {
//			return Status.Error.TIBERIUM_HAS_MARCH_VALUE;
//		}
//
//		// 战争狂热
//		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
//			return Status.Error.TIBERIUM_IN_WAR_FEVER_VALUE;
//		}
//
//		// 城内有援助行军，不能进入泰伯利亚
//		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
//				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
//		if (!marchList.isEmpty()) {
//			return Status.Error.TIBERIUM_HAS_ASSISTANCE_MARCH_VALUE;
//		}
//
//		// 有被动行军
//		BlockingQueue<IWorldMarch> passiveMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
//		if (!CollectionUtils.isEmpty(passiveMarchs)) {
//			int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
//			for (IWorldMarch march : passiveMarchs) {
//				if (march == null || march.getMarchEntity() == null || march.getMarchEntity().isInvalid()) {
//					continue;
//				}
//				if (march.getTerminalId() != playerPos) {
//					continue;
//				}
//				return Status.Error.TIBERIUM_HAS_PASSIVE_MARCH_VALUE;
//			}
//		}
//
//		// 着火也不行
//		if (player.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()) {
//			return Status.Error.TIBERIUM_ON_FIRE_VALUE;
//		}
//
//		if (player.getLmjyState() != null) {
//			return Status.Error.LMJY_BAN_OP_VALUE;
//		}
//
//		// 在联盟军演组队中不能.
//		if (WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
//			return Status.Error.LMJY_BAN_OP_VALUE;
//		}
//
//		// 已经在跨服中了.
//		if (CrossService.getInstance().isCrossPlayer(player.getId())) {
//			return Status.CrossServerError.CROSS_FIGHTERING_VALUE;
//		}
//
//		// 看下区服有没有开.
//		if (!CrossService.getInstance().isServerOpen(serverId)) {
//			return Status.CrossServerError.CROSS_SERVER_NOT_ACTIVE_VALUE;
//		}
//
//		HawkTuple2<TWRoomData, Integer> tuple = TiberiumWarService.getInstance().getPlayerRoomData(player);
//
//		int status = tuple.second;
//
//		if(status != Status.SysError.SUCCESS_OK_VALUE){
//			return status;
//		}
//		// 房间不存在
//		TWRoomData roomData = tuple.first;
//		if (roomData == null) {
//			return Status.Error.TIBERIUM_HAS_NO_MATCH_INFO_VALUE;
//		}
//
//		if (HawkOSOperator.isEmptyString(roomData.getOppGuildId(player.getGuildId()))) {
//			return Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE;
//		}
//
//		if (roomData.getRoomState() != RoomState.INITED) {
//			return Status.Error.TIBERIUM_ROOM_DATE_NOT_INIT_VALUE;
//		}
//
//		// 进入战场时间检测
//		long starTime = 0;
//		long endTime = 0;
//		long latestTime = 0;
//		long curTime = HawkTime.getMillisecond();
//		if (isLeaguaWar) {
//			TiberiumSeasonTimeCfg timeCfg = TiberiumLeagueWarService.getInstance().getCurrTimeCfg();
//			starTime = timeCfg.getWarStartTimeValue();
//			endTime = starTime + TiberiumConstCfg.getInstance().getWarOpenTime();
//			latestTime = endTime - TiberiumConstCfg.getInstance().getLimitTimeBoforeEnd() * 1000l;
//		} else {
//			int timeIndex = roomData.getTimeIndex();
//			WarTimeChoose timeChoose = TiberiumWarService.getInstance().getWarTimeChoose(timeIndex);
//			starTime = timeChoose.getTime();
//			endTime = starTime + TiberiumConstCfg.getInstance().getWarOpenTime();
//			latestTime = endTime - TiberiumConstCfg.getInstance().getLimitTimeBoforeEnd() * 1000l;
//		}
//		if (curTime < starTime || curTime >= endTime) {
//			return Status.Error.TIBERIUM_ROOM_NOT_OPEN_VALUE;
//		}
//
//		if (curTime < endTime && curTime >= latestTime) {
//			return Status.Error.TIBERIUM_ROOM_NEAR_CLOSE_VALUE;
//
//		}
//
//		TWPlayerData twPlayerData = RedisProxy.getInstance().getTWPlayerData(player.getId(), termId);
//		if (twPlayerData == null) {
//			return Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE;
//		}
//
//		if (!twPlayerData.getGuildId().equals(player.getGuildId())) {
//			return Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE;
//		}
//
//		if (twPlayerData.getQuitTime() > 0) {
//			return Status.Error.TIBERIUM_HAS_JOINED_WAR_VALUE;
//		}
//
//		return Status.SysError.SUCCESS_OK_VALUE;
//	}

	@MessageHandler 
	public void sourceOnBackServer(TiberiumBackServerMsg msg) {
		Player.logger.info("corss player back server plaeyrId:{}", player.getId());
		//在发起退出的时候强行把数据序列化到redis中，返回之后再读一次.
		PlayerDataSerializer.csSyncPlayerData(player.getId(), true);
		//修改顺序 先把数据反序列化回来再移除.
		String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());		
		
		toServerId = toServerId == null ? "NULL" : toServerId;
		LogUtil.logTimberiumCross(player, toServerId, CrossStateType.CROSS_EXIT);
		
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
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_CROSS_BACK_FINISH));
		
		// 设置跨服返回时间
		player.setCrossBackTime(HawkTime.getMillisecond());
		
		RankService.getInstance().checkCityLvlRank(player);
		RankService.getInstance().checkPlayerLvlRank(player);
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
				Player.logger.error("player set cross status fail playerId:{}", player.getId());											
				break tryCross;
			}
			
			tryCrossErrorCode = Status.SysError.SUCCESS_OK_VALUE;
		}			
		
		Player.logger.info("tiberium condtion check  playerId:{}, errorCode:{}, serverId:{}", player.getId(), tryCrossErrorCode, targetServerId);
		
		if (tryCrossErrorCode == Status.SysError.SUCCESS_OK_VALUE) {
			TiberiumCrossMsg.Builder msgBuilder = TiberiumCrossMsg.newBuilder();
			msgBuilder.setServerId(targetServerId);
			InnerEnterCrossReq.Builder builder = InnerEnterCrossReq.newBuilder();
			builder.setCurTime(HawkTime.getSeconds());
			player.setCrossStatus(PlayerCrossStatus.PREPARE_CROSS);
			builder.setCrossType(CrossType.TIBERIUM);
			//发起一个远程RPC到远程去,
			HawkProtocol protocl = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_REQ, builder);			
			CrossProxy.getInstance().rpcRequest(protocl, new TiberiumPrepareEnterCallback(player, msgBuilder.build()), targetServerId, player.getId(), "");
		} else {			
			TiberiumCallbackOperationService.getInstance().onPrepareCrossFail(player);
		}
		
		return true;
	}
	
	/**
	 * 迁回。
	 * @param msg
	 */
	@MessageHandler
	public void targetOnMoveBack(TiberiumMoveBackCrossPlayerMsg msg) {
		Player.logger.info("tiberium playerId:{} receive move back msg", player.getId());
		if (!player.isCsPlayer()) {
			Player.logger.error("timberium player isn't csplayer can not receive this protocol playerId:{}", player.getId());
			
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
	 * 从GM指令发过来一个签回玩家的指令.
	 * @param msg
	 */
	@MessageHandler
	public void sourceOnPrepareMoveBack(TiberiumPrepareMoveBackMsg msg) {
		String toServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
		if (HawkOSOperator.isEmptyString(toServerId)) {
			Player.logger.error("playerId:{} prepare force move back toServerId is null ", player.getId());
			
			return;
		}
		
		Player.logger.info("playerId:{} prepare move back toServerId:{}", player.getId(), toServerId);
		
		TiberiumWarMoveBackReq.Builder req = TiberiumWarMoveBackReq.newBuilder();
		req.setPlayerId(player.getId());
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_MOVE_BACK_REQ_VALUE, req);
		CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, player.getId(), "");
	}
	
	private boolean isCrossToSelf(String crossToServerId) {		
		return GsConfig.getInstance().getServerId().equals(crossToServerId);
	}
	
//	private HawkTuple2<String, Integer> getCrossToServerId() {
//		HawkTuple2<TWRoomData, Integer> tuple2 = TiberiumWarService.getInstance().getPlayerRoomData(player);
//		TWRoomData roomData = tuple2.first;
//		int status = tuple2.second;
//		if(status != Status.SysError.SUCCESS_OK_VALUE){
//			return new HawkTuple2<String, Integer>("", status);
//		}else{
//
//			if (roomData == null) {
//				return new HawkTuple2<String, Integer>("", Status.Error.TIBERIUM_HAS_NO_MATCH_INFO_VALUE);
//			} else {
//				return new HawkTuple2<String, Integer>(roomData.getRoomServerId(), Status.SysError.SUCCESS_OK_VALUE);
//			}
//		}
//	}
	
	/**
	 * 获取小组信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_GET_TEAMINFO_C_VALUE)
	private boolean onGetTeamInfo(HawkProtocol protocol) {
		TWGetTeamInfo req = protocol.parseProtocol(TWGetTeamInfo.getDefaultInstance());
		HawkTuple2<Integer, TWTeamInfo.Builder> tuple = TiberiumWarService.getInstance().getTeamInfo(player, req.getTeamIndex());
		int result = tuple.first;
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), result);
			return true;
		}
		sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_GET_TEAMINFO_S, tuple.second));
		return true;
	}
	
	/**
	 * 获取个人任务信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_GET_TEAM_SUMMARY_REQ_C_VALUE)
	private boolean onGetTeamSummary(HawkProtocol protocol) {
		HawkTuple2<Integer, TWGetTeamSummaryResp.Builder> tuple = TiberiumWarService.getInstance().getTeamSummaryInfo(player);
		int result = tuple.first;
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), result);
			return true;
		}
		sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_GET_TEAM_SUMMARY_RESP_S, tuple.second));
		return true;
	}
	
	/**
	 * 获取小组成员管理界面信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_GET_TEAM_MANAGE_INFO_C_VALUE)
	private boolean onGetTeamManageInfo(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return true;
		}
		
		// 已参与本期泰伯利亚之战
		if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}
		
		TWTeamManageInfoReq req = protocol.parseProtocol(TWTeamManageInfoReq.getDefaultInstance());
		HawkTuple2<Integer, TWTeamManageInfoResp.Builder> tuple = TiberiumWarService.getInstance().getTeamMangeInfo(player, req.getTeamIndex());
		int result = tuple.first;
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), result);
			return true;
		}
		sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_GET_TEAM_MANAGE_INFO_S_VALUE, tuple.second));
		return true;
	}
	
	/**
	 * 
	 * 小组成员管理
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_TEAM_MEMBER_MANAGE_C_VALUE)
	private boolean onTeamMemberManage(HawkProtocol protocol) {
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return true;
		}
		
		// 已参与泰伯利亚联赛,当前阶段不能修改
		TLWActivityData tlwActivity = TiberiumLeagueWarService.getInstance().getActivityInfo();
		if(TiberiumLeagueWarService.getInstance().isJointLeaguaWar(player) && tlwActivity.getState().getNumber()> TLWActivityState.TLW_WAR_MANGE.getNumber()){
			sendError(protocol.getType(), Status.Error.TIBERIUM_LEAGUA_CANNOT_MANAGE_TEAM);
			return false;
		}
		
		// 已参与本期泰伯利亚之战
		if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}
		TWTeamMemberManage req = protocol.parseProtocol(TWTeamMemberManage.getDefaultInstance());
		
		player.msgCall(MsgId.TIBERIUM_MEMBER_MANAGE, TiberiumWarService.getInstance(),
				new TWTeamMemberManageInvoker(player, req.getTeamIndex(), req.getType(), req.getPlayerId(), protocol.getType()));
		return true;
	}
	
	/**
	 * 
	 * 小组成员重置
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_TEAM_MEMBER_RESET_C_VALUE)
	private boolean onTeamMemberReset(HawkProtocol protocol) {
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return true;
		}
		
		// 已参与泰伯利亚联赛,当前阶段不能修改
		TLWActivityData tlwActivity = TiberiumLeagueWarService.getInstance().getActivityInfo();
		if(TiberiumLeagueWarService.getInstance().isJointLeaguaWar(player) && tlwActivity.getState().getNumber()> TLWActivityState.TLW_WAR_MANGE.getNumber()){
			sendError(protocol.getType(), Status.Error.TIBERIUM_LEAGUA_CANNOT_MANAGE_TEAM);
			return false;
		}
		
		// 已参与本期泰伯利亚之战
		if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}
		
		TWTeamMemberReset req = protocol.parseProtocol(TWTeamMemberReset.getDefaultInstance());
		player.msgCall(MsgId.TIBERIUM_MEMBER_RESET, TiberiumWarService.getInstance(),
				new TWTeamMemberResetInvoker(player, req.getTeamIndex(), protocol.getType()));
		return true;
	}
	
	/**
	 * 改变小组名称
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_EDIT_TEAM_NAME_C_VALUE)
	private boolean onEditTeamName(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_TIBERIUM_TEAM_NAME);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return true;
		}
		
		// 已参与泰伯利亚联赛,当前阶段不能修改
		TLWActivityData tlwActivity = TiberiumLeagueWarService.getInstance().getActivityInfo();
		if(TiberiumLeagueWarService.getInstance().isJointLeaguaWar(player) && tlwActivity.getState().getNumber()> TLWActivityState.TLW_WAR_MANGE.getNumber()){
			sendError(protocol.getType(), Status.Error.TIBERIUM_LEAGUA_CANNOT_MANAGE_TEAM);
			return false;
		}
		
		// 已参与本期泰伯利亚之战
		if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}
		
		TWEditTeamName req = protocol.parseProtocol(TWEditTeamName.getDefaultInstance());
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", req.getTeamIndex());
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", String.valueOf(req.getTeamIndex()));
		GameTssService.getInstance().wordUicChatFilter(player, req.getName(), 
				MsgCategory.TIBER_TEAM_NAME.getNumber(), GameMsgCategory.EDIT_TIBERIUM_TEAM, 
				String.valueOf(req.getTeamIndex()), json, protocol.getType());
		return true;
	}
	
	/**
	 * 改变小组目标
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_EDIT_TEAM_TARGET_C_VALUE)
	private boolean onEditTeamTarget(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return true;
		}
		
		// 已参与泰伯利亚联赛,当前阶段不能修改
		TLWActivityData tlwActivity = TiberiumLeagueWarService.getInstance().getActivityInfo();
		if(TiberiumLeagueWarService.getInstance().isJointLeaguaWar(player) && tlwActivity.getState().getNumber()> TLWActivityState.TLW_WAR_MANGE.getNumber()){
			sendError(protocol.getType(), Status.Error.TIBERIUM_LEAGUA_CANNOT_MANAGE_TEAM);
			return false;
		}
		
		// 已参与本期泰伯利亚之战
		if(!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())){
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}
		
		TWEditTeamTarget req = protocol.parseProtocol(TWEditTeamTarget.getDefaultInstance());
		player.msgCall(MsgId.TIBERIUM_EDIT_TEAM_TARGET, TiberiumWarService.getInstance(),
				new TWEditTeamTargetInvoker(player, req.getTeamIndex(), req.getTeamTargetList(), protocol.getType()));
		return true;
	}
	
	/**
	 * 改变小组成员策略
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_WAR_EDIT_MEMBER_TARGET_C_VALUE)
	private boolean onEditMemberTarget(HawkProtocol protocol) {
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return true;
		}

		// 已参与泰伯利亚联赛,当前阶段不能修改
		TLWActivityData tlwActivity = TiberiumLeagueWarService.getInstance().getActivityInfo();
		if (TiberiumLeagueWarService.getInstance().isJointLeaguaWar(player) && tlwActivity.getState().getNumber() > TLWActivityState.TLW_WAR_MANGE.getNumber()) {
			sendError(protocol.getType(), Status.Error.TIBERIUM_LEAGUA_CANNOT_MANAGE_TEAM);
			return false;
		}

		// 已参与本期泰伯利亚之战
		if (!TiberiumWarService.getInstance().checkGuildOperation(player.getGuildId())) {
			sendError(protocol.getType(), Status.Error.TIBERIUM_GUILD_OPERATION_FORBID);
			return false;
		}

		TWEditMemberTarget req = protocol.parseProtocol(TWEditMemberTarget.getDefaultInstance());
		player.msgCall(MsgId.TIBERIUM_EDIT_MEMBER_TARGET, TiberiumWarService.getInstance(),
				new TWEditMemberTargetInvoker(player, req.getTeamIndex(), req.getMemberId(), req.getMemberTargetList(), protocol.getType()));
		return true;
	}
	
	/**
	 * 获取赛区联盟战力排行
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.TIBERIUM_LEAGUA_WAR_GET_CHOOSE_RANK_REQ_VALUE)
	private void onGetGuildChooseRank(HawkProtocol protocol) {
		if(TiberiumConstCfg.getInstance().IsNewOpen()){
			GetTLWGuildRankResp.Builder builder = TBLYSeasonService.getInstance().getGuildChooseRank(player);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.TIBERIUM_LEAGUA_WAR_GET_CHOOSE_RANK_RESP, builder));
		}
	}
	
	/**
	 * 获取赛区联盟战力排行
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_LEAGUA_GET_POWERRANK_C_VALUE)
	private void onGetGuildPowerRank(HawkProtocol protocol) {
		TiberiumWar.TLWPageInfoReq req = protocol.parseProtocol(TiberiumWar.TLWPageInfoReq.getDefaultInstance());
		GetTLWGuildRankResp.Builder builder;
		if(TiberiumConstCfg.getInstance().IsNewOpen()){
			builder = TBLYSeasonService.getInstance().getGuildBattleRank(player, req.getServer());
		}else {
			builder = TiberiumLeagueWarService.getInstance().getGuildPowerRank(player, req.getServer());
		}
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_GET_POWERRANK_S, builder));
		}
	}
	
	/**
	 * 获取泰伯利亚联赛主界面信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_LEAGUA_WAR_GET_PAGE_INFO_C_VALUE)
	private void onGetTLWPageInfo(HawkProtocol protocol) {
//		TiberiumWar.TLWPageInfoReq req = protocol.parseProtocol(TiberiumWar.TLWPageInfoReq.getDefaultInstance());
//		TLWPageInfo.Builder builder = TiberiumLeagueWarService.getInstance().genPageInfo(player, TiberiumConst.TLWServerType.getType(req.getServer().getNumber()));
//		if (builder != null) {
//			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_GET_PAGE_INFO_S, builder));
//		}
		TBLYSeasonService.getInstance().syncPageInfo(player);

	}
	
	/**
	 * 获取小组联盟列表
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_LEAGUA_WAR_GET_ZONE_GUILD_INFO_C_VALUE)
	private void onGetTLWZoneGuildInfo(HawkProtocol protocol) {
		TLWGetTeamGuildInfoReq req = protocol.parseProtocol(TLWGetTeamGuildInfoReq.getDefaultInstance());
		TLWGetTeamGuildInfoResp.Builder builder;
		if(TiberiumConstCfg.getInstance().IsNewOpen()){
			builder = TBLYSeasonService.getInstance().getTeamGuildInfo(player, req.getTeamId());
		}else {
			builder = TiberiumLeagueWarService.getInstance().getTeamGuildInfo(req.getTeamId(), player, req.getServer());
		}
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_GET_ZONE_GUILD_INFO_S, builder));
		}
	}
	
	/**
	* 获取赛区赛程信息 
	* @param protocol
	*/
	@ProtocolHandler(code = HP.code.TIBERIUM_LEAGUA_WAR_GET_MATCH_INFO_C_VALUE)
	private void onGetTLWMatchInfo(HawkProtocol protocol) {
		TLWGetMatchInfoReq req = protocol.parseProtocol(TLWGetMatchInfoReq.getDefaultInstance());
		TLWGetMatchInfoResp.Builder builder;
		if(TiberiumConstCfg.getInstance().IsNewOpen()){
			builder = TBLYSeasonService.getInstance().getTLWMatchInfo(player, req);
		}else {
			builder = TiberiumLeagueWarService.getInstance().getTLWMatchInfo(req, player);
		}
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_GET_MATCH_INFO_S, builder));
		}
	}
	
	/**
	 * 获取本盟赛程信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_LEAGUA_WAR_GET_SELF_MATCH_LIST_REQ_VALUE)
	private void onGetSelfMatchInfo(HawkProtocol protocol){
		TLWSelfMatchList.Builder builder;
		if(TiberiumConstCfg.getInstance().IsNewOpen()){
			builder = TBLYSeasonService.getInstance().getSelfMatchInfo(player);
		}else {
			builder = TiberiumLeagueWarService.getInstance().getSelfMatchInfo(player);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_GET_SELF_MATCH_LIST_RESP, builder));
	}
	
	/**
	 * 获取决赛对阵信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_LEAGUA_WAR_GET_FINAL_MATCH_INFO_C_VALUE)
	private void onGetTLWFinalMatchInfo(HawkProtocol protocol) {
		TLWGetFinalMatchInfoReq req = protocol.parseProtocol(TLWGetFinalMatchInfoReq.getDefaultInstance());
		TLWGetFinalMatchInfoResp.Builder builder = TiberiumLeagueWarService.getInstance().getTLWFinalInfo(player, req.getGroup(), req.getServer());
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_GET_FINAL_MATCH_INFO_S, builder));
		}
	}
	
	/**
	 * 获取赛季积分奖励信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_LEAGUA_WAR_GET_SCORE_INFO_REQ_VALUE)
	private void onGetTLWScoreRewardInfo(HawkProtocol protocol) {
		TLWGetScoreInfoResp.Builder builder = TBLYSeasonService.getInstance().getTLWScoreRewardInfo(player);
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_GET_SCORE_INFO_RESP, builder));
		}
	}
	
	/**
	 * 获取联赛对战列表
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TIBERIUM_OB_GET_ROOM_LIST_C_VALUE)
	private void onGetTLWRoomInfo(HawkProtocol protocol) {
		boolean isPerform = TiberiumConstCfg.getInstance().isPerform();
		TLWGetOBRoomInfo.Builder builder;
		if (isPerform) {
			// 表演赛模式,展示当前开启的普通泰伯利亚列表
			builder = TBLYWarService.getInstance().getTWRoomInfo();
		} else {
			// 非表演赛模式,展示联赛决赛开启的对战列表
			builder = TBLYSeasonService.getInstance().getTLWRoomInfo();
		}
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_OB_GET_ROOM_LIST_S_VALUE, builder));
		}
	}

	/**
	 * 报名老服比赛
	 * @param protocol
	 */
//	@ProtocolHandler(code = HP.code.TIBERIUM_LEAGUA_WAR_SIGN_UP_REQ_VALUE)
//	private void onTLWSignUp(HawkProtocol protocol) {
//		TBLYSeasonService.getInstance().newSignupOld(player);
//	}
	
}
 