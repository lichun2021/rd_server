package com.hawk.game.module;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
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
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.config.CyborgShopCfg;
import com.hawk.game.config.CyborgShopRefreshTimeCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.cyborg.CyborgCallbackOperationService;
import com.hawk.game.crossproxy.cyborg.CyborgPrepareEnterCallback;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.CyborgDismissTeamInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.msg.CYBORGQuitReason;
import com.hawk.game.lianmengcyb.msg.CYBORGQuitRoomMsg;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.msg.cyborg.CyborgBackServerMsg;
import com.hawk.game.msg.cyborg.CyborgExitCrossInstanceMsg;
import com.hawk.game.msg.cyborg.CyborgMoveBackCrossPlayerMsg;
import com.hawk.game.msg.cyborg.CyborgPrepareExitCrossInstanceMsg;
import com.hawk.game.msg.cyborg.CyborgPrepareMoveBackMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Cross;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.Cross.CyborgCrossMsg;
import com.hawk.game.protocol.Cross.InnerEnterCrossReq;
import com.hawk.game.protocol.CyborgWar.CSWScoreInfo;
import com.hawk.game.protocol.CyborgWar.CWBattleLog;
import com.hawk.game.protocol.CyborgWar.CWBuyItemReq;
import com.hawk.game.protocol.CyborgWar.CWCreateTeamReq;
import com.hawk.game.protocol.CyborgWar.CWDismissTeamReq;
import com.hawk.game.protocol.CyborgWar.CWEditTeamNameReq;
import com.hawk.game.protocol.CyborgWar.CWGetHistoryReq;
import com.hawk.game.protocol.CyborgWar.CWGetHistoryResp;
import com.hawk.game.protocol.CyborgWar.CWGetPlayerListResp;
import com.hawk.game.protocol.CyborgWar.CWGeyPlayerListReq;
import com.hawk.game.protocol.CyborgWar.CWPageInfo;
import com.hawk.game.protocol.CyborgWar.CWPlayerManageReq;
import com.hawk.game.protocol.CyborgWar.CWShopInfo;
import com.hawk.game.protocol.CyborgWar.CWShopItem;
import com.hawk.game.protocol.CyborgWar.CWSignUpReq;
import com.hawk.game.protocol.CyborgWar.CWTeamInfo;
import com.hawk.game.protocol.CyborgWar.CWTeamList;
import com.hawk.game.protocol.CyborgWar.CWTimeChoose;
import com.hawk.game.protocol.CyborgWar.CyborgWarInnerBackServerReq;
import com.hawk.game.protocol.CyborgWar.CyborgWarMoveBackReq;
import com.hawk.game.protocol.CyborgWar.GetCWTeamRankResp;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.cyborgWar.CLWActivityData;
import com.hawk.game.service.cyborgWar.CWConst;
import com.hawk.game.service.cyborgWar.CWPlayerData;
import com.hawk.game.service.cyborgWar.CWRoomData;
import com.hawk.game.service.cyborgWar.CyborgLeaguaWarService;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.cyborgWar.CyborgWarService;
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
import com.hawk.log.Action;
import com.hawk.log.LogConst.CrossStateType;

/**
 * 赛博之战
 * @author Jesse
 */
public class PlayerCyborgModule extends PlayerModule {

	public PlayerCyborgModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		try {
			CyborgWarService.getInstance().syncPageInfo(player);
			syncCyborgShopItems();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}
	
	/**
	 * 请求界面信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_GET_PAGE_INFO_C_VALUE)
	private void onGetPageInfo(HawkProtocol protocol) {
		CWPageInfo.Builder stateInfo = CyborgWarService.getInstance().genPageInfo(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_GET_PAGE_INFO_S, stateInfo));
	}
	
	/**
	 * 创建队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_CREATE_TEAM_C_VALUE)
	private boolean onCreateTeam(HawkProtocol protocol) {
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		// 跨服期间该操作禁用
		if (player.isCsPlayer()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		// 联盟权限不足
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}

		List<ItemInfo> needItems = CyborgConstCfg.getInstance().getTeamCreateCostItem();
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(needItems);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}

		CWCreateTeamReq req = protocol.parseProtocol(CWCreateTeamReq.getDefaultInstance());
		int nameLength = GameUtil.getStringLength(req.getName());
		HawkTuple2<Integer, Integer> nameLimit = CyborgConstCfg.getInstance().getNameSize();
		if (nameLength < nameLimit.first || nameLength > nameLimit.second) {
			sendError(protocol.getType(), Status.Error.CYBORG_TEAM_NAME_LONG_VALUE);
			return false;
		}
		
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", req.getName());
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", "");
		GameTssService.getInstance().wordUicChatFilter(player, req.getName(), 
				MsgCategory.CYBOR_TEAM_NAME.getNumber(), GameMsgCategory.CREATE_CYBOR_TEAM, 
				"", json, protocol.getType());
		return true;
	}
	
	/**
	 * 队伍改名
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_CHANGE_TEAM_NAME_C_VALUE)
	private boolean onEditTeamName(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_CYBOR_TEAM_NAME);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
		
		// 跨服期间该操作禁用
		if (player.isCsPlayer()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		// 联盟权限不足
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
				
		CWEditTeamNameReq req = protocol.parseProtocol(CWEditTeamNameReq.getDefaultInstance());
		String name = req.getName();
		int nameLength = GameUtil.getStringLength(name);
		HawkTuple2<Integer, Integer> nameLimit = CyborgConstCfg.getInstance().getNameSize();
		if (nameLength < nameLimit.first || nameLength > nameLimit.second) {
			sendError(protocol.getType(), Status.Error.CYBORG_TEAM_NAME_LONG_VALUE);
			return false;
		}
		
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", req.getName());
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", req.getTeamId());
		GameTssService.getInstance().wordUicChatFilter(player, req.getName(), 
				MsgCategory.CYBOR_TEAM_NAME.getNumber(), GameMsgCategory.EDIT_CYBOR_TEAM, 
				req.getTeamId(), json, protocol.getType());
		return true;
	}
	
	/**
	 * 解散队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_DISMISS_TEAM_C_VALUE)
	private boolean onDismissTeam(HawkProtocol protocol) {

		// 跨服期间该操作禁用
		if (player.isCsPlayer()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		CWDismissTeamReq req = protocol.parseProtocol(CWDismissTeamReq.getDefaultInstance());
		player.msgCall(MsgId.CYBOGR_DISMISS_TEAM, CyborgWarService.getInstance(), new CyborgDismissTeamInvoker(player, req.getTeamId()));
		return true;
	}
	
	/**
	 * 报名
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_SIGN_UP_C_VALUE)
	private void onSignUp(HawkProtocol protocol) {
		CWSignUpReq req = protocol.parseProtocol(CWSignUpReq.getDefaultInstance());
		int index = req.getTimeIndex();
		String teamId = req.getTeamId();
		int result = CyborgWarService.getInstance().signUp(player, teamId, index);
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
	@ProtocolHandler(code = HP.code.CYBORG_WAR_GET_PLAYER_LIST_C_VALUE)
	private void onGetPlayerList(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return;
		}
		CWGeyPlayerListReq req = protocol.parseProtocol(CWGeyPlayerListReq.getDefaultInstance());
		CWGetPlayerListResp.Builder builder = CyborgWarService.getInstance().getMemberList(player.getGuildId(), req.getTeamId());
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_GET_PLAYER_LIST_S, builder));
		}
	}
	
	/**
	 * 管理参战成员
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_PLAYER_MANAGE_C_VALUE)
	private void onManagePlayer(HawkProtocol protocol) {
		CWPlayerManageReq req = protocol.parseProtocol(CWPlayerManageReq.getDefaultInstance());
		int result = CyborgWarService.getInstance().updateMemberList(player, req);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			CWPlayerManageReq.Builder builder = CWPlayerManageReq.newBuilder();
			builder.setType(req.getType());
			builder.setPlayerId(req.getPlayerId());
			builder.setTeamId(req.getTeamId());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_PLAYER_MANAGE_S, builder));
		} else {
			sendError(protocol.getType(), result);
		}
	}
	
	/**
	 * 拉取联盟战队列表
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_GET_TEAM_LIST_C_VALUE)
	private void onGetTeamList(HawkProtocol protocol) {
		CWTeamList.Builder builder = CWTeamList.newBuilder();
		List<CWTeamInfo> builderList = CyborgWarService.getInstance().genGuildTeamList(player.getGuildId(), GsConfig.getInstance().getServerId());
		builder.addAllTeamList(builderList);
		String teamId = CyborgWarService.getInstance().getSelfTeamId(player);
		if(!HawkOSOperator.isEmptyString(teamId)){
			builder.setSelfTeam(teamId);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_GET_TEAM_LIST_S, builder));
	}
	
	/**
	 * 获取战队历史战报
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_GET_HISTORY_C_VALUE)
	private void onGetBattleLogs(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return;
		}
		CWGetHistoryReq req = protocol.parseProtocol(CWGetHistoryReq.getDefaultInstance());
		String teamId = req.getTeamId();
		List<CWBattleLog> logList = CyborgWarRedis.getInstance().getCWBattleLog(teamId,20);
		CWGetHistoryResp.Builder builder = CWGetHistoryResp.newBuilder();
		builder.setTeamId(teamId);
		builder.addAllBattleLog(logList);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_GET_HISTORY_S, builder));
	}
	
	
	/**
	 * 获取赛区联盟战力排行
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_GET_POWERRANK_C_VALUE)
	private void onGetGuildPowerRank(HawkProtocol protocol) {
		GetCWTeamRankResp.Builder builder = CyborgWarService.getInstance().getGuildPowerRank(player);
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_GET_POWERRANK_S, builder));
		}
	}
	
	
	/**
	 * 退出战场房间 
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onQuitRoomMsg(CYBORGQuitRoomMsg msg) {
		try {
			boolean isMidwayQuit = msg.getQuitReason() == CYBORGQuitReason.LEAVE;
			CyborgWarService.getInstance().quitRoom(player, isMidwayQuit);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {
			CrossService.getInstance().addForceMoveBackCyborgPlayer(player.getId());
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
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_CROSS_BACK_BEGIN));
		
		//检测到可以退出了那么先设置标志位
		player.setCrossStatus(GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		
		CrossService.getInstance().addExitCyborgPlayer(player.getId());
		
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
		
		Player.logger.info("cyborg playerId:{}, exit cross  errorCode:{}", player.getId(), errorCode);
							
		//把玩家在本服的痕迹清理掉
		String fromServerId = CrossService.getInstance().removeImmigrationPlayer(player.getId());
		GlobalData.getInstance().removeAccountInfoOnExitCross(player.getId());
		HawkApp.getInstance().removeObj(player.getXid());
		GlobalData.getInstance().invalidatePlayerData(player.getId());					
		
		CyborgWarInnerBackServerReq.Builder req = CyborgWarInnerBackServerReq.newBuilder();
		req.setPlayerId(player.getId());
		//发送一个协会回原服.
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(HP.code.CYBORG_WAR_INNER_BACK_SERVER_REQ_VALUE, req), fromServerId, "");
		String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
		//设置redis状态, 都已经到了这一步了，失败了就失败了，也不能怎么样了.
		boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(mainServerId, player.getId(), GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		if (!setCrossStatus) {
			Player.logger.error("cyborg player exit cross set cross status fail playerId:{}", player.getId());
		}
		
	}
		
	/**
	 * 预退出跨服
	 * @param msg
	 */
	@MessageHandler
	public void targetOnPrepareExitCrossMsg(CyborgPrepareExitCrossInstanceMsg msg) {
		HawkLog.logPrintln("cyborg prepare exit cross Instance from msg playerId:{}", player.getId());
		targetDoPrepareExitCrossInstance();		
	}
	
	/**
	 * 发出退出跨服信息.
	 * @param msg
	 */
	@MessageHandler
	public void targetOnExitInstanceMessage(CyborgExitCrossInstanceMsg msg) {
		HawkLog.logPrintln("cyborg exitInstance from msg playerId:{}", player.getId());
		targetDoExitInstance();		
	}
	
	@ProtocolHandler(code = HP.code.CYBORG_WAR_EXIT_INSTANCE_REQ_VALUE)
	public void onExitInstance(HawkProtocol hawkProtocol) {
		Player.logger.info("playerId:{} exit cyborg war from protocol", player.getId());
		if (player.isCsPlayer()) {
			//远程操作.
			CsPlayer csPlayer = player.getCsPlayer();
			if (!csPlayer.isCrossType(CrossType.CYBORG_VALUE)) {
				csPlayer.sendError(hawkProtocol.getType(), Status.Error.CYBORG_NOT_IN_INISTANCE_VALUE, 0);
				
				return;
			}
			targetDoPrepareExitCrossInstance();
		} else {
			//本地操作.
			simulateExitCross();
		}
	}
	
	private void simulateCross() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_SIMULATE_CROSS_BEGIN_VALUE));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_SIMULATE_CROSS_FINISH_VALUE));
				boolean rlt = CyborgWarService.getInstance().joinRoom(player);
				Player.logger.info("playerId:{} enter local cyborg instance result:{}", player.getId(), rlt);
			}
		});
	}
	
	private void simulateExitCross() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_SIMULATE_CROSS_BACK_BEGIN));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_SIMULATE_CROSS_BACK_FINISH_VALUE));				
			}
		});
	}

	@ProtocolHandler(code = HP.code.CYBORG_WAR_ENTER_INSTANCE_REQ_VALUE)
	public void onEnterInstance(HawkProtocol hawkProtocol) {	
		HawkTuple2<String, Integer> tuple = getCrossToServerId();
		int status = tuple.second;
		if (status != Status.SysError.SUCCESS_OK_VALUE) {
			Player.logger.info("playerId:{} try to enter cyborg war failed errorCode:{}", player.getId(), status);
			this.sendError(hawkProtocol.getType(), status);
			return;
		}	
		
		String serverId = tuple.first;
		if (HawkOSOperator.isEmptyString(serverId)) {
			this.sendError(hawkProtocol.getType(), Status.Error.CYBORG_HAS_NO_MATCH_INFO_VALUE);
			return;
		}
		
		int errorCode = sourceCheckEnterInstance(serverId);
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(hawkProtocol.getType(), errorCode);
			return;
		}			
		
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		long shieldEndTime = HawkTime.getMillisecond()  + CyborgConstCfg.getInstance().getWarOpenTime() + GameConst.TBLY_PROTECTE_TIME * 1000;  
		if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() < shieldEndTime) {
			StatusDataEntity entity = player.addStatusBuff(GameConst.CITY_SHIELD_BUFF_ID, shieldEndTime);			
			if (entity != null) {
				player.getPush().syncPlayerStatusInfo(false, entity);
			}
		}
		
		if (isCrossToSelf(serverId)) {
			//如果是本服的则处理.
			simulateCross();
			DungeonRedisLog.log(player.getId(), "onEnterInstance local:{}", serverId);
		} else {			
			boolean rlt = sourceDoLeaveForCross(serverId);
			if (!rlt) {
				player.sendError(hawkProtocol.getType(), Status.SysError.EXCEPTION_VALUE, 0);
			} else {
				player.responseSuccess(hawkProtocol.getType());
			}
			DungeonRedisLog.log(player.getId(), "onEnterInstance corss:{},rlt:{}", serverId,rlt);
		}
	}
	
	/**
	 * 检测是否可以进入副本
	 * @param serverId
	 * @return
	 */
	private int sourceCheckEnterInstance(String serverId) {
		// 有行军
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if (!marchs.isEmpty()) {
			return Status.Error.CYBORG_HAS_MARCH_VALUE;
		}

		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			return Status.Error.CYBORG_IN_WAR_FEVER_VALUE;
		}

		// 城内有援助行军，不能进入泰伯利亚
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			return Status.Error.CYBORG_HAS_ASSISTANCE_MARCH_VALUE;
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
				return Status.Error.CYBORG_HAS_PASSIVE_MARCH_VALUE;
			}
		}

		// 着火也不行
		if (player.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()) {
			return Status.Error.CYBORG_ON_FIRE_VALUE;
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
		
		/***************/
		int termId =CyborgWarService.getInstance().getTermId();
		HawkTuple2<CWRoomData, Integer> tuple = CyborgWarService.getInstance().getPlayerRoomData(player);
		
		int status = tuple.second;
		
		if(status != Status.SysError.SUCCESS_OK_VALUE){
			return status;
		}
		// 房间不存在
		CWRoomData roomData = tuple.first;
		if (roomData == null) {
			return Status.Error.CYBORG_HAS_NO_MATCH_INFO_VALUE;
		}

		if (roomData.getRoomState() != CWConst.RoomState.INITED) {
			return Status.Error.CYBORG_ROOM_DATE_NOT_INIT_VALUE;
		}
		
		// 进入战场时间检测
		long starTime = 0;
		long endTime = 0;
		long latestTime = 0;
		long curTime = HawkTime.getMillisecond();
			int timeIndex = roomData.getTimeIndex();
			CWTimeChoose timeChoose = CyborgWarService.getInstance().getWarTimeChoose(timeIndex);
			starTime = timeChoose.getTime();
			endTime = starTime + CyborgConstCfg.getInstance().getWarOpenTime();
			latestTime = endTime - CyborgConstCfg.getInstance().getLimitTimeBoforeEnd() * 1000l;
		if (curTime < starTime || curTime >= endTime) {
			return Status.Error.CYBORG_ROOM_NOT_OPEN_VALUE;
		}

		if (curTime < endTime && curTime >= latestTime) {
			return Status.Error.CYBORG_ROOM_NEAR_CLOSE_VALUE;

		}

		CWPlayerData cwPlayerData = CyborgWarRedis.getInstance().getCWPlayerData(player.getId(), termId);
		if (cwPlayerData == null) {
			return Status.Error.CYBORG_NOT_IN_THIS_WAR_VALUE;
		}

		if (!cwPlayerData.getGuildId().equals(player.getGuildId())) {
			return Status.Error.CYBORG_NOT_IN_THIS_WAR_VALUE;
		}

		if (cwPlayerData.getQuitTime() > 0) {
			return Status.Error.CYBORG_HAS_QUIT_ROOM_VALUE;
		}
		/***************/
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	@MessageHandler 
	public void sourceOnBackServer(CyborgBackServerMsg msg) {
		Player.logger.info("cyborg corss player back server plaeyrId:{}", player.getId());
		//在发起退出的时候强行把数据序列化到redis中，返回之后再读一次.
		PlayerDataSerializer.csSyncPlayerData(player.getId(), true);
		//修改顺序 先把数据反序列化回来再移除.
		String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());		
		
		toServerId = toServerId == null ? "NULL" : toServerId;
		LogUtil.logPlayerCross(player, toServerId, CrossStateType.CROSS_EXIT, Cross.CrossType.CYBORG);
		
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
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_CROSS_BACK_FINISH));
		
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
				Player.logger.error("player enter cyborg flush reids error playerId:{}", player.getId());										
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
		
		Player.logger.info("cyborg condtion check  playerId:{}, errorCode:{}, serverId:{}", player.getId(), tryCrossErrorCode, targetServerId);
		
		if (tryCrossErrorCode == Status.SysError.SUCCESS_OK_VALUE) {
			CyborgCrossMsg.Builder msgBuilder = CyborgCrossMsg.newBuilder();
			msgBuilder.setServerId(targetServerId);
			InnerEnterCrossReq.Builder builder = InnerEnterCrossReq.newBuilder();
			builder.setCurTime(HawkTime.getSeconds());
			builder.setCrossType(CrossType.CYBORG);
			player.setCrossStatus(PlayerCrossStatus.PREPARE_CROSS);
			//发起一个远程RPC到远程去,
			HawkProtocol protocl = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_REQ, builder);			
			CrossProxy.getInstance().rpcRequest(protocl, new CyborgPrepareEnterCallback(player, msgBuilder.build()), targetServerId, player.getId(), "");
		} else {			
			CyborgCallbackOperationService.getInstance().onPrepareCrossFail(player);
		}
		
		return true;
	}
	
	/**
	 * 迁回。
	 * @param msg
	 */
	@MessageHandler
	public void targetOnMoveBack(CyborgMoveBackCrossPlayerMsg msg) {
		Player.logger.info("cyborg playerId:{} receive move back msg", player.getId());
		if (!player.isCsPlayer()) {
			Player.logger.error("cyborg player isn't csplayer can not receive this protocol playerId:{}", player.getId());
			
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
	public void sourceOnPrepareMoveBack(CyborgPrepareMoveBackMsg msg) {
		String toServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
		if (HawkOSOperator.isEmptyString(toServerId)) {
			Player.logger.error("playerId:{} prepare force move back toServerId is null ", player.getId());
			
			return;
		}
		
		Player.logger.info("playerId:{} prepare move back toServerId:{}", player.getId(), toServerId);
		
		CyborgWarMoveBackReq.Builder req = CyborgWarMoveBackReq.newBuilder();
		req.setPlayerId(player.getId());
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.CYBORG_WAR_MOVE_BACK_REQ_VALUE, req);
		CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, player.getId(), "");
	}
	
	private boolean isCrossToSelf(String crossToServerId) {		
		return GsConfig.getInstance().getServerId().equals(crossToServerId);
	}
	
	/**
	 * 获取目标服务器serverId
	 * @return
	 */
	private HawkTuple2<String, Integer> getCrossToServerId() {
		HawkTuple2<CWRoomData, Integer> tuple = CyborgWarService.getInstance().getPlayerRoomData(player);
		CWRoomData roomData = tuple.first;
		if (roomData != null) {
			return new HawkTuple2<String, Integer>(roomData.getRoomServerId(), tuple.second);
		} else {
			return new HawkTuple2<String, Integer>(null, tuple.second);
		}
	}
	
	
	/**
	 * 请求塞伯利亚商店信息
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_SHOP_INFO_C_VALUE)
	private boolean onGetCyborgShopInfo(HawkProtocol protocol) {
		syncCyborgShopItems();
		return true;
	}
	
	/**
	 * 购买赛博商店物品
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CYBORG_WAR_BUY_ITEM_C_VALUE)
	private boolean buyCWShopItem(HawkProtocol protocol) {

		CWBuyItemReq req = protocol.parseProtocol(CWBuyItemReq.getDefaultInstance());
		int shopId = req.getId();
		int count = req.getCount();
		CyborgShopCfg config = HawkConfigManager.getInstance().getConfigByKey(CyborgShopCfg.class, shopId);
		if (config == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			HawkLog.errPrintln("buyCyborgShopItem failed, config error, playerId: {}, shopId: {}", player.getId(), shopId);
			return false;
		}
		String seasonInfo = CyborgLeaguaWarService.getInstance().getCyborgShopKeyInfo();

		if (config.getNumLimit() != 0) {
			int alreadyBuyCount = CyborgWarRedis.getInstance().getCyborgShopItemBuyCount(player.getId(), shopId, seasonInfo);
			if (alreadyBuyCount + count > config.getNumLimit()) {
				sendError(protocol.getType(), Status.Error.CYBORG_BUY_OVER_LIMIT_VALUE);
				HawkLog.errPrintln("buyCyborgShopItem failed, buy count error, playerId: {}, shopId: {}, count: {}, alreadyBuyCount: {}", player.getId(), shopId, count,
						alreadyBuyCount);
				return false;
			}
		}
		// 赛季专属奖励仅对应赛季开启期间可兑换
		if (config.getLimitSeason() != 0) {
			CLWActivityData clwData = CyborgLeaguaWarService.getInstance().getActivityData();
			if (clwData.getSeason() != config.getLimitSeason() || !CyborgLeaguaWarService.getInstance().isInSeason()) {
				sendError(protocol.getType(), Status.Error.CYBORG_SHOP_SEASON_LIMIT_VALUE);
				HawkLog.errPrintln("buyCyborgShopItem failed, seasonLimit error, playerId: {}, shopId: {}", player.getId(), shopId);
				return false;
			}
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		List<ItemInfo> consumeItems = config.getConsumeItems();
		consumeItems.stream().forEach(e -> e.setCount(e.getCount() * count));
		consume.addConsumeInfo(consumeItems);
		if (!consume.checkConsume(player, protocol.getType())) {
			HawkLog.errPrintln("buyCyborgShopItem failed, consume error, playerId: {}, shopId: {}, count: {}", player.getId(), shopId, count);
			return false;
		}

		consume.consumeAndPush(player, Action.CYBORG_BUY_ITEM);
		AwardItems awardItems = AwardItems.valueOf();
		List<ItemInfo> shopItems = config.getShopItems();
		shopItems.stream().forEach(e -> e.setCount(e.getCount() * count));
		awardItems.addItemInfos(shopItems);
		awardItems.rewardTakeAffectAndPush(player, Action.CYBORG_BUY_ITEM, true);

		// 更新购买次数
		CyborgWarRedis.getInstance().incrCyborgShopItemBuyCount(player.getId(), shopId, count, seasonInfo);

		syncCyborgShopItems();

		return true;
	}
	
	/**
	 * 同步赛博商店信息
	 * 
	 */
	private void syncCyborgShopItems() {
		try {
			CWShopInfo.Builder builder = CWShopInfo.newBuilder();
			Map<Integer, Integer> buyCountMap = CyborgWarRedis.getInstance().getCyborgShopItemBuyCount(player.getId(),
					CyborgLeaguaWarService.getInstance().getCyborgShopKeyInfo());
			for (Entry<Integer, Integer> entry : buyCountMap.entrySet()) {
				CWShopItem.Builder item = CWShopItem.newBuilder();
				item.setId(entry.getKey());
				item.setBuyCount(entry.getValue());
				builder.addBuyItems(item);
			}
			// 同步购买次数数据
			builder.setCyborgScore(player.getPlayerBaseEntity().getCyborgScore());
			CyborgShopRefreshTimeCfg timeCfg = CyborgLeaguaWarService.getInstance().getNextCfg();
			if(timeCfg !=null){
				builder.setNextRefreshTime(timeCfg.getRefreshTimeValue());
			}else{
				builder.setNextRefreshTime(Long.MAX_VALUE);
			}
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_SHOP_INFO_PUSH, builder));

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 请求赛季段位排行信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_SEASON_WAR_GET_STAR_RANK_C_VALUE)
	private void onGetTeamStarRank(HawkProtocol protocol) {
		GetCWTeamRankResp.Builder stateInfo = CyborgLeaguaWarService.getInstance().getSeasonStarRank(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_SEASON_WAR_GET_STAR_RANK_S, stateInfo));
	}
	

	/**
	 * 请求赛季段位排行信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CYBORG_SEASON_WAR_GET_GUILD_SCORE_C_VALUE)
	private void onGetGuildSeasonScore(HawkProtocol protocol) {
		CSWScoreInfo.Builder scoreInfo = CyborgLeaguaWarService.getInstance().genGuildSeasonScoreInfo(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_SEASON_WAR_GET_GUILD_SCORE_S, scoreInfo));
	}
}
 