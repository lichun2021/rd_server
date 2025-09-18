package com.hawk.game.module;

import java.util.concurrent.BlockingQueue;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossServerListCfg;
import com.hawk.game.crossactivity.CActivityInfo;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossCallbackOperationService;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.callback.PrepareStartCrossCallback;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.msg.cross.BackServerMsg;
import com.hawk.game.msg.cross.ExitCrossMsg;
import com.hawk.game.msg.cross.MoveBackCrossPlayerMsg;
import com.hawk.game.msg.cross.PrepareExitCrossMsg;
import com.hawk.game.msg.cross.PrepareMoveBackCrossPlayerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Cross.CrossMoveBackReq;
import com.hawk.game.protocol.Cross.CrossServerListResp;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.Cross.EnterCross;
import com.hawk.game.protocol.Cross.EnterCrossMsg;
import com.hawk.game.protocol.Cross.InnerBackServerReq;
import com.hawk.game.protocol.Cross.InnerEnterCrossReq;
import com.hawk.game.protocol.Cross.OnSpyReq;
import com.hawk.game.protocol.CrossActivity.CrossActivityState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.WorldTaskType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.game.world.thread.tasks.ExitCrossWorldTask;
import com.hawk.log.Action;
import com.hawk.log.LogConst.CrossStateType;

/**
 * onSource 代表是原服处理的消息
 * onTarget f代表是跨过去的服处理的消息.
 * @author jm
 *
 */
public class PlayerCSModule extends PlayerModule {
	
	public PlayerCSModule(Player player) {
		super(player);
	}
		
	/**
	 * A->B
	 * 
	 * A处理此协议B直接走一个login 即可.
	 * 
	 */
	@ProtocolHandler(code = CHP.code.ENTER_CROSS_VALUE)
	public void sourceOnCrossServer(HawkProtocol hawkProtocol) {
		
		Player.logger.info("playerId:{} try to cross", player.getId());
		
		EnterCross enterCross = hawkProtocol.parseProtocol(EnterCross.getDefaultInstance());
		EnterCrossMsg enterCrossMsg = enterCross.getEnterCrossMsg();
		String targetServerId = enterCrossMsg.getServerId();
		CrossService crossService = CrossService.getInstance();
		ConstProperty constProperty = ConstProperty.getInstance();
		
		//处于联盟军演的状态则不让跨服.
		if (player.isInDungeonMap()) {
			this.sendError(hawkProtocol.getType(), Status.Error.LMJY_BAN_OP_VALUE);
			
			return;
		}
		
		//在联盟军演组队中不能.
		if (WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
			this.sendError(hawkProtocol.getType(),  Status.Error.LMJY_BAN_OP_VALUE);
			
			return;
		}
		
		//需要有工会才可以跨
		if (HawkOSOperator.isEmptyString(player.getGuildId())) {
			this.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_HAVE_NO_GUILD_VALUE);
			
			return;
		}
		
		//玩家大本
		if (player.getCityLevel() < constProperty.getCrossCityLevel()) {
			player.sendError(hawkProtocol.getType(), Status.Error.CITY_LEVEL_NOT_ENOUGH_VALUE, 0);
			
			return;
		}
		
		//跨服是否开启.
		if (!CrossActivityService.getInstance().isOpen()) {
			player.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_NOT_OPEN_VALUE, 0);
			
			return;
		}
		
		//活动是否即将关闭.
		if (!CrossActivityService.getInstance().canCross()) {
			player.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_WILL_CLOSE_VALUE, 0);
			
			return;
		}
		
		//已经参与跨服的玩家
		if (crossService.isCrossPlayer(player.getId())) {
			player.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_FIGHTERING_VALUE, 0);
			
			return;
		}
		
		//判断同一个跨服组.
		if (!crossService.isCrossServer(GsConfig.getInstance().getServerId(), targetServerId)) {
			player.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_SERVER_NOT_GROUP_VALUE, 0);
			
			return;
		}
		
		if (!isOperateTime()) {
			player.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_UNOPERATE_TIME_VALUE, 0);
			
			return ;
		}
		
		//看下区服有没有开.
		if (!crossService.isServerOpen(targetServerId)) {
			player.sendError(hawkProtocol.getType(),  Status.CrossServerError.CROSS_SERVER_NOT_ACTIVE_VALUE, 0);
			
			return;
		}
		
		// 迁回多久之内不能再跨服
		long crossBackTime = player.getCrossBackTime();
		if (HawkTime.getMillisecond() - crossBackTime < CrossConstCfg.getInstance().getCrossCd()) {
			player.sendError(hawkProtocol.getType(),  Status.CrossServerError.CROSS_CD_VALUE, 0);
			
			return;
		}
		
		//调用各个处理离开本服
		int errorCode = sourceDoLeaveToCross();
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(hawkProtocol.getType(), errorCode, 0);
			
			return;
		}			
		
		//调用close操作把数据该结算的结算.
		HawkSession session = player.getSession();
		SessionClosedMsg closeMsg = SessionClosedMsg.valueOf();
		closeMsg.setTarget(player.getXid());
		player.onMessage(closeMsg);
		//删除保护罩
		CityManager.getInstance().removeCityShieldInfo(player.getId());
		
		//不入侵原来的逻辑只能再这里把Session加回来.		
		player.setSession(session);		
		//退出的时候会减,在这里加回来.
		GlobalData.getInstance().changePfOnlineCnt(player, true);
		
		//移除当前服的在线信息.
		RedisProxy.getInstance().removeOnlineInfo(player.getOpenId());
		
		//记录一下玩家跨服
		int termId = CrossActivityService.getInstance().getTermId();
		RedisProxy.getInstance().addCrossActivityCrossPlayerRecord(player, termId);
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
			
			
			try {
				//序列化工会的数据.
				GuildService.getInstance().serializeGuild4Cross(player.getGuildId());
				player.getData().serialData4Cross();
				//守护信息				
				RelationService.getInstance().onPlayerCross(targetServerId, player.getId());
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
		
		Player.logger.info("cross condtion check  playerId:{}, errorCode:{}, serverId:{}", player.getId(), tryCrossErrorCode, targetServerId);
		
		if (tryCrossErrorCode == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(hawkProtocol.getType());
			
			InnerEnterCrossReq.Builder builder = InnerEnterCrossReq.newBuilder();
			builder.setCurTime(HawkTime.getSeconds());
			//发起一个远程RPC到远程去,
			HawkProtocol protocl = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_REQ, builder);
			CrossProxy.getInstance().rpcRequest(protocl, new PrepareStartCrossCallback(player, enterCrossMsg), targetServerId, player.getId(), "");
		} else {			
			player.sendError(hawkProtocol.getType(), tryCrossErrorCode, 0);
			CrossCallbackOperationService.getInstance().onPrepareCrossFail(player);
		}
					
	}
	
	/**
	 * A->B
	 * A处理该请求.
	 * @return
	 */
	private int sourceDoLeaveToCross() {
		// 外面有行军，不能跨服
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if (!marchs.isEmpty()) {
			return Status.CrossServerError.CROSS_HAVE_MARCH_VALUE;
		}
		
		//消耗
		ConstProperty constProperty = ConstProperty.getInstance();
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(constProperty.getCrossCostItemInfos(), true);
		if (!consume.checkConsume(player)) {			
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}		
		consume.consumeAndPush(player, Action.CROSS_SERVER_COST);
		
		// 处理行军
		WorldMarchService.getInstance().mantualMoveCityProcessMarch(player);
		
		// 重推警报
		BlockingQueue<IWorldMarch> terminalPtMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
		if(terminalPtMarchs != null){
			for(IWorldMarch mar : terminalPtMarchs){
				if(mar instanceof IReportPushMarch){
					((IReportPushMarch) mar).removeAttackReport();
				}
			}
		}
		
		//移除世界城点
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		WorldPointService.getInstance().removeWorldPoint(worldPoint.getId(), true);
		CityManager.getInstance().removeCity(player);
		
		//移除跨服记录的装扮信息
		WorldPointService.getInstance().removeShowDress(player.getId());
		WorldPointService.getInstance().removePlayerSignature(player.getId());
		//假如有守护对象需要同步一下守护对象的信息 迁城应该是一个低频的操作,这里同步一下问题不大,
		//如果有优化的必要就计算一下之前的距离和现在的距离是否有达成守护特效的条件
		String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
		if (!HawkOSOperator.isEmptyString(guardPlayerId)) {
			WorldPoint wp = WorldPlayerService.getInstance().getPlayerWorldPoint(guardPlayerId);
			if (wp != null) {
				WorldPointService.getInstance().getWorldScene().update(wp.getAoiObjId());
			}
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 强制签回协议,
	 * A->B 在B服处理.
	 * force 强制的情况下, 中间流程出问题的情况会尽力让它走向下一个流程.
	 * @param hawkProtocl
	 */
	@MessageHandler
	public void targetOnMoveBack(MoveBackCrossPlayerMsg msg) {
		Player.logger.info("playerId:{} receive move back msg", player.getId());
		if (!player.isCsPlayer()) {
			Player.logger.error("player isn't csplayer can not receive this protocol playerId:{}", player.getId());
			
			return ;
		}
		
		boolean force = msg.isForce();	
		if (!force) {
			BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			if (!marchs.isEmpty()) {
				Player.logger.error("playerId:{} not move back because has march ", player.getId());
				
				return;
			}
		}
		
		//在线的话, 尝试踢下线.
		if (player.isActiveOnline()) {
			player.notifyPlayerKickout(Status.SysError.ADMIN_OPERATION_VALUE, null);
		}
		
		//加入到退出跨服
		targetDoPrepareExitCross();
	}
	
	/**
	 * 从GM指令发过来一个签回玩家的指令.
	 * @param msg
	 */
	@MessageHandler
	public void sourceOnPrepareForceMoveBack(PrepareMoveBackCrossPlayerMsg msg) {
		String toServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
		if (HawkOSOperator.isEmptyString(toServerId)) {
			Player.logger.error("playerId:{} prepare force move back toServerId is null ", player.getId());
			
			return;
		}
		
		Player.logger.info("playerId:{} prepare move back toServerId:{}", player.getId(), toServerId);
		
		CrossMoveBackReq.Builder req = CrossMoveBackReq.newBuilder();
		req.setForce(msg.isIsforce() ? 1 : 0);
		req.setPlayerId(player.getId());
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(CHP.code.CROSS_MOVE_BACK_VALUE, req);
		CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, player.getId(), "");
	}
	
	/**
	 * A->B
	 * A处理此协议 此协议由B扔出
	 * 
	 * @param hawkProtocol
	 */
	@MessageHandler 
	public void sourceOnBackServer(BackServerMsg msg) {
		Player.logger.info("corss player back server plaeyrId:{}", player.getId());
		//在发起退出的时候强行把数据序列化到redis中，返回之后再读一次.
		PlayerDataSerializer.csSyncPlayerData(player.getId(), true);
		//修改顺序 先把数据反序列化回来再移除.
		String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());		
		
		toServerId = toServerId == null ? "NULL" : toServerId;
		LogUtil.logPlayerCross(player, toServerId, CrossStateType.CROSS_EXIT, CrossType.CROSS);
		
		try {
			WorldPointService.getInstance().removeShowDress(player.getId());
			WorldPointService.getInstance().removePlayerSignature(player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
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
			//尝试更新一下activeserver
			GameUtil.updateActiveServer(player.getId(), GsConfig.getInstance().getServerId());
		}
		
		// 通知客户端跨服返回完成
		player.sendProtocol(HawkProtocol.valueOf(CHP.code.CROSS_BACK_FINISH));
		
		// 设置跨服返回时间
		player.setCrossBackTime(HawkTime.getMillisecond());
		
		RankService.getInstance().checkCityLvlRank(player);
		RankService.getInstance().checkPlayerLvlRank(player);
	}	
	
	/**
	 * A->B
	 * 客户端触发A转发B处理
	 * hawkProtocol 的Session 是唯一的火苗.
	 * 调用了SessionCloseMsg之后 player的Session会被关掉.需要注意.
	 * @param hawkProtocol
	 */
	@ProtocolHandler(code = CHP.code.EXIT_CORSS_VALUE)
	public void targetOnExitCrossProtocol(HawkProtocol hawkProtocol) {
		HawkLog.logPrintln("cross player exit cross from protocol playerId:{}", player.getId());
		
		//如果不是csPlayer收到这个协议说明是有问题的
		if (!player.isCsPlayer()) {
			this.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_NOT_IN_VALUE);
			return;
		}
		
		//如果是在活动结束的时候发送协议,则不判断行军.
		CActivityInfo activityInfo = CrossActivityService.activityInfo;
		if (activityInfo.getState() != CrossActivityState.C_END && activityInfo.getState() != CrossActivityState.C_HIDDEN) {			
			if (!this.isOperateTime()) {
				player.sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_UNOPERATE_TIME_VALUE, 0);
				return;
			}
			
			BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			if (!marchs.isEmpty()) {
				sendError(hawkProtocol.getType(), Status.CrossServerError.CROSS_HAVE_MARCH);
				return;
			}
		}
				
		targetDoPrepareExitCross();
	}
	
	/**
	 * 收到这个消息之后不管玩家的数据有没有处理完成,都直接强制遣返.
	 * 所以要尽量让玩家的遣返操作要快
	 * @param msg
	 */
	@MessageHandler
	public void targetOnExitCrossMessage(ExitCrossMsg msg) {
		HawkLog.logPrintln("cross player exitCross from msg playerId:{}", player.getId());
		targetDoExitCross();		
	}
	
	/**
	 * 留给活动的接口 发起遣返玩家.
	 * @author  jm 
	 * 2018 下午3:49:57
	 * @param msg
	 */
	@MessageHandler
	public void targetOnRepatriateMessage(PrepareExitCrossMsg msg) {
		HawkLog.logPrintln("cross player prepare exit cross playerId:{}", player.getId());
		targetDoPrepareExitCross();
	}
	
	/**
	 * 迁回的预处理.
	 * @author  jm 
	 */	
	public void targetDoPrepareExitCross() {
		// 通知客户端跨服开始
		player.sendProtocol(HawkProtocol.valueOf(CHP.code.CROSS_BACK_BEGIN));
		
		//检测到可以退出了那么先设置标志位
		player.setCrossStatus(GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		
		//添加到检测队列
		CrossService.getInstance().addExitCrossPlayer(player.getId());
		
		//世界相关操作
		WorldThreadScheduler.getInstance().postWorldTask(new ExitCrossWorldTask(player, WorldTaskType.CROSS_SERVER));
		//移除跨服玩家相关数据
		GuildService.getInstance().onCsPlayerOut(player);
		//移除跨服记录的装扮信息
		WorldPointService.getInstance().removeShowDress(player.getId());
		WorldPointService.getInstance().removePlayerSignature(player.getId());
		WorldPointService.getInstance().removeCollegeNameShow(player.getId());
		//清理守护信息.
		RelationService.getInstance().onPlayerExitCross(player.getId());
	}
	
	/**
	 * A->B
	 * 客户端触发A转发B处理
	 * 
	 * @return
	 */
	private void targetDoExitCross() {
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
		
		Player.logger.info("playerId:{}, exit cross  errorCode:{}", player.getId(), errorCode);
		
					
		//把玩家在本服的痕迹清理掉
		String fromServerId = CrossService.getInstance().removeImmigrationPlayer(player.getId());
		GlobalData.getInstance().removeAccountInfoOnExitCross(player.getId());
		HawkApp.getInstance().removeObj(player.getXid());
		GlobalData.getInstance().invalidatePlayerData(player.getId());					
		
		InnerBackServerReq.Builder req = InnerBackServerReq.newBuilder();
		req.setPlayerId(player.getId());
		//发送一个协会回原服.
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.INNER_BACK_SERVER_VALUE, req), fromServerId, "");
		String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
		//设置redis状态, 都已经到了这一步了，失败了就失败了，也不能怎么样了.
		boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(mainServerId, player.getId(), GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		if (!setCrossStatus) {
			Player.logger.error("csplayer exit cross set cross status fail playerId:{}", player.getId());
		}
						 
	}
	
	/**
	 * A->B
	 * A处理由客户端发送.
	 * @param protocol
	 */
	@ProtocolHandler(code = CHP.code.CROSS_SERVER_LIST_REQ_VALUE)
	public void onCrossServerListReq(HawkProtocol protocol) {
		CrossServerListCfg listCfg = AssembleDataManager.getInstance().getCrossServerListCfg(GsConfig.getInstance().getServerId());
		CrossServerListResp.Builder builder = CrossServerListResp.newBuilder(); 
		if (listCfg != null) {			
			builder.addAllServerList(listCfg.getServerList());
		} 
		
		HawkProtocol respProtocol = HawkProtocol.valueOf(CHP.code.CROSS_SERVER_LIST_RESP, builder);
		player.sendProtocol(respProtocol);
	}
	
	/**
	 * A->B B处理.
	 * A在监听到socket连接断开之后发一个消息给B
	 * @param protocol
	 */
	@ProtocolHandler(code = CHP.code.INNER_LOGOUT_VALUE)
	public void onTargetLogout(HawkProtocol protocol) {
		//所有的module走一遍logout流程但是
		SessionClosedMsg closeMsg = SessionClosedMsg.valueOf();
		closeMsg.setTarget(player.getXid());
		player.onMessage(closeMsg);
	}
	
	/**
	 * 跨服侦查请求，判断是否可以侦查
	 * @param protocol
	 */
	@ProtocolHandler(code = CHP.code.ON_SPY_REQ_VALUE)
	public void onSpy(HawkProtocol protocol) {
		OnSpyReq req = protocol.parseProtocol(OnSpyReq.getDefaultInstance());
		// 目标服serverId
		String targetServerId = req.getServerId();
		// 本服serverId
		String ownServerId = GsConfig.getInstance().getServerId();
		// 不是同一跨服组
		if (!CrossService.getInstance().isCrossServer(ownServerId, targetServerId)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_SERVER_NOT_GROUP);
			return;
		}
		// 目标服没有开启
		if (!CrossService.getInstance().isServerOpen(targetServerId)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_SERVER_NOT_ACTIVE);
			return;
		}
		player.responseSuccess(protocol.getType());
	}
	
	@Override
	public boolean onPlayerAssemble() {
		CsPlayer csPlayer = player.getCsPlayer();
		if (csPlayer != null && csPlayer.isFirstCrossServerLogin()) {
			RelationService.getInstance().onCsPlayerFirstLoginIn(player.getId());
		}
		
		return true;
	}
	@Override 
	public boolean onPlayerLogin() {
		if (player.isCsPlayer()) {
			CsPlayer csPlayer = (CsPlayer)player;
			if (csPlayer.isFirstCrossServerLogin()) {				
				LogUtil.logPlayerCross(player, GsConfig.getInstance().getServerId(), CrossStateType.CROSS_FINISH, CrossType.valueOf(csPlayer.getCrossType()));
			}					
		}  
		
		this.onCrossServerListReq(null);
		
		return true;
	}
	@Override
	public boolean onPlayerLogout() {
		//清理玩家的hpLogin消息,如果玩家离线要跨服这里得另外处理.
		player.getHpLogin().clearInnerEnterCrossMsg();
		return true;
	}
	
	/**
	 * 是否是可操作时间, 针对主动跨服和主动退出跨服的玩家.
	 * @return
	 */
	public boolean isOperateTime() {
		int unoperateTime = CrossConstCfg.getInstance().getUnoperatorTime() * 1000;
		long currentDayZeroTime = HawkTime.getAM0Date().getTime();
		long nextDayZeroTime = HawkTime.getNextAM0Date();
		long currentTime = HawkTime.getMillisecond();
		
		//当天0点的后面多长时间     下个0点的前面多长时间 都是不可以操作的.
		if (currentDayZeroTime + unoperateTime > currentTime   || currentTime + unoperateTime >  nextDayZeroTime) {
			return false;
		}
		
		return true;
	}
}
