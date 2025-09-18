package com.hawk.game.module.dayazhizhan.playerteam.module;

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

import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.dyzz.DYZZCallbackOperationService;
import com.hawk.game.crossproxy.dyzz.DYZZPrepareEnterCallback;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZQuitReason;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZQuitRoomMsg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZHeroCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZShopCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZShopRefreshTimeCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSoldierCfg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZBackServerMsg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZExitCrossInstanceMsg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZMoveBackCrossPlayerMsg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZPrepareExitCrossInstanceMsg;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZPrepareMoveBackMsg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonPlayerData;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZGameRoomData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZPlayerData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZPlayerScore;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZRedisData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZTeamRoom;
import com.hawk.game.module.dayazhizhan.playerteam.task.CreateTeamInvoker;
import com.hawk.game.module.dayazhizhan.playerteam.task.ExitTeamInvoker;
import com.hawk.game.module.dayazhizhan.playerteam.task.InviteTeamInvoker;
import com.hawk.game.module.dayazhizhan.playerteam.task.JoinTeamInvoker;
import com.hawk.game.module.dayazhizhan.playerteam.task.KickoutTeamInvoker;
import com.hawk.game.module.dayazhizhan.playerteam.task.TeamCancelMatchnvoker;
import com.hawk.game.module.dayazhizhan.playerteam.task.TeamMatchGameInvoker;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.player.hero.NPCHero;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.protocol.Army.ArmyInfoPB;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.Cross.DYZZCrossMsg;
import com.hawk.game.protocol.Cross.InnerEnterCrossReq;
import com.hawk.game.protocol.DYZZ.PBDYZZGameInfoSync;
import com.hawk.game.protocol.DYZZWar.PBDYZZBattleHistoryInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZBuyItemReq;
import com.hawk.game.protocol.DYZZWar.PBDYZZGameRoomState;
import com.hawk.game.protocol.DYZZWar.PBDYZZHero;
import com.hawk.game.protocol.DYZZWar.PBDYZZHeroManageInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZHeroState;
import com.hawk.game.protocol.DYZZWar.PBDYZZJoinTeamReq;
import com.hawk.game.protocol.DYZZWar.PBDYZZKickOutTeamReq;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonShareReq;
import com.hawk.game.protocol.DYZZWar.PBDYZZShopInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZShopItem;
import com.hawk.game.protocol.DYZZWar.PBDYZZSoldierManageInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamState;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarInnerBackServerReq;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarMoveBackReq;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.PlayerCrossStatus;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 泰伯利亚之战
 * @author Jesse
 */
public class PlayerDYZZModule extends PlayerModule {

	public PlayerDYZZModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		DYZZService.getInstance().syncOpenInfo(player);
		DYZZService.getInstance().syncStateInfo(player);
		return true;
	}
	
	
	/**
	 * 活动信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_INFO_REQ_VALUE)
	private void onWarInfo(HawkProtocol protocol) {
		DYZZService.getInstance().syncOpenInfo(player);
		DYZZService.getInstance().syncStateInfo(player);
	}
	
	/**
	 * 创建队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_CREATE_TEAM_REQ_VALUE)
	private void onCreateTeam(HawkProtocol protocol) {
		DYZZService.getInstance().dealMsg(MsgId.DYZZ_CREATE_TEAM, new CreateTeamInvoker(player));
	}
	
	/**
	 * 联盟邀请
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_GUILD_INVITE_REQ_VALUE)
	private void onInviteTeam(HawkProtocol protocol) {
		DYZZService.getInstance().dealMsg(MsgId.DYZZ_TEAM_INVITE, new InviteTeamInvoker(player));
	}
	
	
	/**
	 * 加入队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_GUILD_JOIN_TEAM_REQ_VALUE)
	private void onJoinTeam(HawkProtocol protocol) {
		PBDYZZJoinTeamReq req = protocol.parseProtocol(PBDYZZJoinTeamReq.getDefaultInstance());
		String teamId = req.getTeamId();
		DYZZService.getInstance().dealMsg(MsgId.DYZZ_JOIN_TEAM, new JoinTeamInvoker(player,teamId));
	}
	
	/**
	 * 踢出队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_KICK_OUT_MEMBER_REQ_VALUE)
	private void onKickoutTeam(HawkProtocol protocol) {
		PBDYZZKickOutTeamReq req = protocol.parseProtocol(PBDYZZKickOutTeamReq.getDefaultInstance());
		String member = req.getMemberId();
		DYZZService.getInstance().dealMsg(MsgId.DYZZ_KICK_OUT, new KickoutTeamInvoker(player,member));
	}
	
	@ProtocolHandler(code = HP.code2.DYZZ_EXIT_TEAM_REQ_VALUE)
	private void onQuitTeam(HawkProtocol protocol) {
		DYZZService.getInstance().dealMsg(MsgId.DYZZ_EXIT_TEAM, new ExitTeamInvoker(player));
	}
	
	
	
	/**
	 * 开始匹配
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_MATCH_REQ_VALUE)
	private void onMatchGame(HawkProtocol protocol) {
		DYZZService.getInstance().dealMsg(MsgId.DYZZ_MATCH_GAME, new TeamMatchGameInvoker(player));
	}
	
	
	
	/**
	 * 取消匹配
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_CANCEL_MATCH_REQ_VALUE)
	private void onCancelMatch(HawkProtocol protocol) {
		DYZZService.getInstance().dealMsg(MsgId.DYZZ_CANCEL_MATCH, new TeamCancelMatchnvoker(player));
	}
	
	
	
	
	/**
	 * 影响管理信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_HERO_MANAGE_INFO_REQ_VALUE)
	private void onHeroManageInfo(HawkProtocol protocol) {
		List<DYZZHeroCfg> heros = HawkConfigManager.getInstance().getConfigIterator(DYZZHeroCfg.class).toList();
		PBDYZZHeroManageInfo.Builder builder = PBDYZZHeroManageInfo.newBuilder();
		for(DYZZHeroCfg cfg : heros){
			int heroData = cfg.getHeroData();
			NPCHero hero = NPCHeroFactory.getInstance().get(heroData);
			if(Objects.isNull(hero)){
				continue;
			}
			PBDYZZHero.Builder hBuilder = PBDYZZHero.newBuilder();
			hBuilder.setHero(hero.toPBobj());
			hBuilder.setState(PBDYZZHeroState.DYZZ_HERO_NORMAL);
			builder.addHeros(hBuilder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_HERO_MANAGE_INFO_RESP_VALUE, builder));
	}
	
	
	/**
	 * 取消匹配
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_SOLIDER_MANAGE_INFO_REQ_VALUE)
	private void onSoldierManageInfo(HawkProtocol protocol) {
		List<DYZZSoldierCfg> soldiers = HawkConfigManager.getInstance().getConfigIterator(DYZZSoldierCfg.class).toList();
		PBDYZZSoldierManageInfo.Builder builder = PBDYZZSoldierManageInfo.newBuilder();
		for(DYZZSoldierCfg cfg : soldiers){
			ArmyInfoPB.Builder sBuilder = ArmyInfoPB.newBuilder();
			sBuilder.setId(String.valueOf(cfg.getId()));
			sBuilder.setArmyId(cfg.getSoldierId());
			sBuilder.setFreeCount(cfg.getCount());
			builder.addSoldiers(sBuilder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_SOLIDER_MANAGE_INFO_RESP_VALUE, builder));
	}
	
	
	/**
	 * 取消匹配
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_HISTORY_INFO_REQ_VALUE)
	private void onBattleHistoryInfo(HawkProtocol protocol) {
		List<PBDYZZGameInfoSync> list = DYZZRedisData.getInstance().getPlayerBattleHistory(this.player.getId());
		DYZZPlayerScore score = DYZZRedisData.getInstance().getDYZZPlayerScore(this.player.getId());
		PBDYZZBattleHistoryInfo.Builder builder = PBDYZZBattleHistoryInfo.newBuilder();
		int total = score.getTotalCount();
		int winPer = score.getWinPer();
		int mvp = score.getMvpCount();
		builder.setTotal(total);
		builder.setWinPer(winPer);
		builder.setMvp(mvp);
		if(list.size() > 0){
			builder.addAllRecords(list);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_HISTORY_INFO_RESP_VALUE, builder));
	}
	
	@ProtocolHandler(code = HP.code2.DYZZ_SEASON_SHARE_REQ_VALUE)
	private void onBattleHistoryShare(HawkProtocol protocol) {
		PBDYZZSeasonShareReq req = protocol.parseProtocol(PBDYZZSeasonShareReq.getDefaultInstance());
		int type = req.getShareType();
		String guildId = player.getGuildId();
		ChatType chatType = null;
		if(type == 1){
			chatType = ChatType.CHAT_WORLD;
		}else if(type == 2){
			chatType = ChatType.CHAT_ALLIANCE;
			if(HawkOSOperator.isEmptyString(guildId)){
				player.sendError(HP.code2.DYZZ_SEASON_SHARE_REQ_VALUE,
						Status.DYZZError.DYZZ_SHARE_NO_GUILD_VALUE, 0);
				return;
			}
		}
		if(chatType == null){
			return;
		}
		long count = DYZZRedisData.getInstance().getDYZZHistoryDayShare(this.player.getId());
		if(count >= 1){
			player.sendError(HP.code2.DYZZ_SEASON_SHARE_REQ_VALUE,
					Status.DYZZError.DYZZ_SHARE_COUNT_LIMIT_VALUE, 0);
			return;
		}
		DYZZSeasonPlayerData seasonData = DYZZSeasonService.getInstance()
				.getDYZZSeasonPlayerData(this.player.getId());
		DYZZPlayerScore score = DYZZRedisData.getInstance().getDYZZPlayerScore(this.player.getId());
		if(seasonData == null || score == null){
			return;
		}
		DYZZRedisData.getInstance().incDYZZHistoryDayShare(this.player.getId());
		int seasonScore = seasonData.getScore();
		int total = score.getTotalCount();
		int winPer = score.getWinPer();
		int mvp = score.getMvpCount();
		ChatParames chatParames = ChatParames.newBuilder()
				.setChatType(chatType)
				.setKey(Const.NoticeCfgId.DYZZ_SEASON_SHARE)
				.setPlayer(this.player)
				.addParms(total,winPer,mvp,seasonScore)
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(chatParames);
		player.responseSuccess(HP.code2.DYZZ_SEASON_SHARE_REQ_VALUE);
	}
	
	/**
	 * 请求塞伯利亚商店信息
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_SHOP_INFO_REQ_VALUE)
	private boolean onGetCyDYZZShopInfo(HawkProtocol protocol) {
		syncDYZZShopItems();
		return true;
	}
	
	/**
	 * 购买赛博商店物品
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_SHOP_BUY_REQ_VALUE)
	private boolean buyDYZZShopItem(HawkProtocol protocol) {
		PBDYZZBuyItemReq req = protocol.parseProtocol(PBDYZZBuyItemReq.getDefaultInstance());
		int shopId = req.getId();
		int count = req.getCount();
		DYZZShopCfg config = HawkConfigManager.getInstance().getConfigByKey(DYZZShopCfg.class, shopId);
		if (config == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			HawkLog.errPrintln("buyDYZZShopItem failed, config error, playerId: {}, shopId: {}", player.getId(), shopId);
			return false;
		}
		int shopTerm = DYZZService.getInstance().getDYZZShopTerm();
		if (config.getNumLimit() != 0) {
			int alreadyBuyCount = DYZZRedisData.getInstance().getDYZZShopItemBuyCount(player.getId(), shopId, shopTerm);
			if (alreadyBuyCount + count > config.getNumLimit()) {
				sendError(protocol.getType(), Status.DYZZError.DYZZ_WAR_SHOP_BUY_COUNT_LIMIT_VALUE);
				HawkLog.errPrintln("buyDYZZShopItem failed, buy count error, playerId: {}, shopId: {}, count: {}, alreadyBuyCount: {}", player.getId(), shopId, count,
						alreadyBuyCount);
				return false;
			}
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		List<ItemInfo> consumeItems = config.getConsumeItems();
		consumeItems.stream().forEach(e -> e.setCount(e.getCount() * count));
		consume.addConsumeInfo(consumeItems);
		if (!consume.checkConsume(player, protocol.getType())) {
			HawkLog.errPrintln("buyDYZZShopItem failed, consume error, playerId: {}, shopId: {}, count: {}", player.getId(), shopId, count);
			return false;
		}

		consume.consumeAndPush(player, Action.DYZZ_SHOP_BUY);
		AwardItems awardItems = AwardItems.valueOf();
		List<ItemInfo> shopItems = config.getShopItems();
		shopItems.stream().forEach(e -> e.setCount(e.getCount() * count));
		awardItems.addItemInfos(shopItems);
		awardItems.rewardTakeAffectAndPush(player, Action.DYZZ_SHOP_BUY, true);
		// 更新购买次数
		DYZZRedisData.getInstance().incDYZZShopItemBuyCount(player.getId(), shopId, count, shopTerm);
		DungeonRedisLog.log(player.getId(), "buyDYZZShopItem:{},count:{},term:{}", shopId,count,shopTerm);
		syncDYZZShopItems();
		return true;
	}
	
	/**
	 * 同步赛博商店信息
	 * 
	 */
	private void syncDYZZShopItems() {
		try {
			PBDYZZShopInfo.Builder builder = PBDYZZShopInfo.newBuilder();
			Map<Integer, Integer> buyCountMap = DYZZRedisData.getInstance().getDYZZShopItemBuyCount(player.getId(),
					DYZZService.getInstance().getDYZZShopTerm());
			for (Entry<Integer, Integer> entry : buyCountMap.entrySet()) {
				PBDYZZShopItem.Builder item = PBDYZZShopItem.newBuilder();
				item.setId(entry.getKey());
				item.setBuyCount(entry.getValue());
				builder.addBuyItems(item);
			}
			// 同步购买次数数据
			builder.setScore(player.getPlayerBaseEntity().getDyzzScore());
			DYZZShopRefreshTimeCfg timeCfg = DYZZService.getInstance().getNextCfg();
			if(timeCfg !=null){
				builder.setNextRefreshTime(timeCfg.getRefreshTimeValue());
			}else{
				builder.setNextRefreshTime(Long.MAX_VALUE);
			}
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_SHOP_INFO_RESP_VALUE, builder));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
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
	private boolean onQuitRoomMsg(DYZZQuitRoomMsg msg) {
		try {
			boolean isMidwayQuit = msg.getQuitReason() == DYZZQuitReason.LEAVE;
			DYZZService.getInstance().quitRoom(player, isMidwayQuit);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		try {
			CrossService.getInstance().addForceMoveBackDyzzPlayer(player.getId());
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
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_WAR_CROSS_BACK_BEGIN));
		
		//检测到可以退出了那么先设置标志位
		player.setCrossStatus(GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		
		//添加到检测队列
		CrossService.getInstance().addExitDyzzPlayer(player.getId());
		
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
		
		Player.logger.info("DYZZ playerId:{}, exit cross  errorCode:{}", player.getId(), errorCode);
							
		//把玩家在本服的痕迹清理掉
		String fromServerId = CrossService.getInstance().removeImmigrationPlayer(player.getId());
		GlobalData.getInstance().removeAccountInfoOnExitCross(player.getId());
		HawkApp.getInstance().removeObj(player.getXid());
		GlobalData.getInstance().invalidatePlayerData(player.getId());					
		
		PBDYZZWarInnerBackServerReq.Builder req = PBDYZZWarInnerBackServerReq.newBuilder();
		req.setPlayerId(player.getId());
		//发送一个协会回原服.
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(HP.code2.DYZZ_WAR_INNER_BACK_SERVER_REQ_VALUE, req), fromServerId, "");
		String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
		//设置redis状态, 都已经到了这一步了，失败了就失败了，也不能怎么样了.
		boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(mainServerId, player.getId(), GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		if (!setCrossStatus) {
			Player.logger.error("dyzz player exit cross set cross status fail playerId:{}", player.getId());
		}
		DungeonRedisLog.log(player.getId(), "mainServerId{}",mainServerId);
	}
		
	/**
	 * 预退出跨服
	 * @param msg
	 */
	@MessageHandler
	public void targetOnPrepareExitCrossMsg(DYZZPrepareExitCrossInstanceMsg msg) {
		HawkLog.logPrintln("DYZZ prepare exit cross Instance from msg playerId:{}", player.getId());
		if(!player.isInDungeonMap()){
			targetDoPrepareExitCrossInstance();		
		}
	}
	
	/**
	 * 发出退出跨服信息.
	 * @param msg
	 */
	@MessageHandler
	public void targetOnExitInstanceMessage(DYZZExitCrossInstanceMsg msg) {
		HawkLog.logPrintln("dyzz exitInstance from msg playerId:{}", player.getId());
		targetDoExitInstance();		
	}
	
	@ProtocolHandler(code = HP.code2.DYZZ_WAR_EXIT_INSTANCE_REQ_VALUE)
	public void onExitInstance(HawkProtocol hawkProtocol) {
		Player.logger.info("playerId:{} exit dyzz war from protocol", player.getId());
		if (player.isCsPlayer()) {
			//远程操作.
			CsPlayer csPlayer = player.getCsPlayer();
			if (!csPlayer.isCrossType(CrossType.DYZZ_VALUE)) {
				csPlayer.sendError(hawkProtocol.getType(), Status.DYZZError.DYZZ_WAR_NOT_IN_INSTANCE_VALUE, 0);
				return;
			}
			targetDoPrepareExitCrossInstance();
			DungeonRedisLog.log(player.getId(), "cross");
		} else {
			//本地操作.
			simulateExitCross();
			DungeonRedisLog.log(player.getId(), "simulateExitCross");
		}
	}
	
	private void simulateCross() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_WAR_SIMULATE_CROSS_BEGIN_VALUE));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_WAR_SIMULATE_CROSS_FINISH_VALUE));
				boolean rlt = DYZZService.getInstance().joinRoom(player);
				Player.logger.info("playerId:{} enter local dyzz instance result:{}", player.getId(), rlt);
			}
		});
	}
	
	private void simulateExitCross() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_WAR_SIMULATE_CROSS_BACK_BEGIN));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_WAR_SIMULATE_CROSS_BACK_FINISH_VALUE));	
				Player.logger.info("playerId:{} exit local dyzz instance", player.getId());
			}
		});
	}

	@ProtocolHandler(code = HP.code2.DYZZ_WAR_ENTER_INSTANCE_REQ_VALUE)
	public void onEnterInstance(HawkProtocol hawkProtocol) {	
		if(player.isCsPlayer()){
			player.sendError(HP.code2.DYZZ_WAR_ENTER_INSTANCE_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_IN_CROSS_VALUE, 0);
			return;
		}
		PBDYZZWarState state = DYZZService.getInstance().getDYZZWarState();
		if(state != PBDYZZWarState.DYZZ_OPEN && 
				state != PBDYZZWarState.DYZZ_CLOSE){
			player.sendError(HP.code2.DYZZ_WAR_ENTER_INSTANCE_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NOT_IN_MATCH_TIME_VALUE, 0);
			return;
		}
		HawkTuple2<String, Integer> tuple = getCrossToServerId();
		int status = tuple.second;
		if (status != Status.SysError.SUCCESS_OK_VALUE) {
			Player.logger.info("playerId:{} try to enter dyzz war failed errorCode:{}", player.getId(), status);
			this.sendError(hawkProtocol.getType(), status);
			return;
		}	
		String serverId = tuple.first;
		Player.logger.info("playerId:{} try to enter dyzz war serverId:{}", player.getId(), serverId == null ? "null" : serverId);
		if (HawkOSOperator.isEmptyString(serverId)) {
			this.sendError(hawkProtocol.getType(), Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
			return;
		}
		
		int errorCode = sourceCheckEnterInstance(serverId);
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(hawkProtocol.getType(), errorCode);
			return;
		}			
		
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		long shieldEndTime = HawkTime.getMillisecond()  + HawkTime.HOUR_MILLI_SECONDS * 2;  
		if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() < shieldEndTime) {
			StatusDataEntity entity = player.addStatusBuff(GameConst.CITY_SHIELD_BUFF_ID, shieldEndTime);			
			if (entity != null) {
				player.getPush().syncPlayerStatusInfo(false, entity);
			}
		}
		
		if (isCrossToSelf(serverId)) {
			//如果是本服的则处理.
			simulateCross();
			DungeonRedisLog.log(player.getId(), "{}",serverId);
		} else {			
			boolean rlt = sourceDoLeaveForCross(serverId);
			if (!rlt) {
				player.sendError(hawkProtocol.getType(), Status.SysError.EXCEPTION_VALUE, 0);
			} else {
				player.responseSuccess(hawkProtocol.getType());
				DungeonRedisLog.log(player.getId(), "cross {}",serverId);
			}
		}
	}
	
	/**
	 * 检测是否可以进入副本
	 * @param serverId
	 * @return
	 */
	private int sourceCheckEnterInstance(String serverId) {
		int termId = DYZZService.getInstance().getDYZZWarTerm();
		// 有行军
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if (!marchs.isEmpty()) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,player has march,playerId:{}", player.getId());
			return Status.DYZZError.DYZZ_HAS_PLYAER_MARCH_VALUE;
		}
		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,player war fever,playerId:{}", player.getId());
			return Status.DYZZError.DYZZ_WAR_IN_WAR_FEVER_VALUE;
		}
		// 城内有援助行军，不能进入泰伯利亚
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,player has assist march ,playerId:{}", player.getId());
			return Status.DYZZError.DYZZ_WAR_HAS_ASSISTANCE_MARCH_VALUE;
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
				HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,player has passive march ,playerId:{}", player.getId());
				return Status.DYZZError.DYZZ_WAR_HAS_PASSIVE_MARCH_VALUE;
			}
		}

		// 着火也不行
		if (player.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,player on fire ,playerId:{}", player.getId());
			return Status.DYZZError.DYZZ_WAR_ON_FIRE_VALUE;
		}
		if (player.isInDungeonMap()) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,player in dungeon ,playerId:{},dungeon:{}", player.getId(),player.getDungeonMap());
			return Status.Error.PLAYER_IN_INSTANCE_VALUE;
		}
		// 在联盟军演组队中不能.
		if (WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,player in lmjy team ,playerId:{}", player.getId());
			return Status.Error.LMJY_BAN_OP_VALUE;
		}

		// 已经在跨服中了.
		if (CrossService.getInstance().isCrossPlayer(player.getId())) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,player isCrossPlayer ,playerId:{}", player.getId());
			return Status.CrossServerError.CROSS_FIGHTERING_VALUE;
		}

		// 看下区服有没有开.
		if (!CrossService.getInstance().isServerOpen(serverId)) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,server not open ,playerId:{},serverId:{}", player.getId(),serverId);
			return Status.CrossServerError.CROSS_SERVER_NOT_ACTIVE_VALUE;
		}
		DYZZTeamRoom team = DYZZService.getInstance().getPlayerTeamRoom(player.getId());
		if (team == null) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,dyzzTeam null ,playerId:{}", player.getId());
			return Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE;
		}
		if (team.getState() != PBDYZZTeamState.DYZZ_TEAM_GAMING) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,dyzzTeam state err ,playerId:{},teamState:{}", player.getId(),team.getState().getNumber());
			return Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE;
		}

		if (HawkOSOperator.isEmptyString(team.getGameRoomId())) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,dyzzTeam gameId null ,playerId:{},teamId:{}", player.getId(),team.getTeamId());
			return Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE;
		}
		DYZZGameRoomData game = DYZZRedisData.getInstance().getDYZZGameData(termId, team.getGameRoomId());
		if (game == null) {
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,dyzzGameData null ,playerId:{},teamId:{},gameId:{}", player.getId(),team.getTeamId(),team.getGameRoomId());
			return Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE;
		}
		if(game.getState() != PBDYZZGameRoomState.DYZZ_GAME_GAMING){
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,dyzzGameData state err ,playerId:{},teamId:{},gameId:{},gameState:{}", 
					player.getId(),team.getTeamId(),team.getGameRoomId(),game.getState().getNumber());
			return  Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE;
		}
		if(game.getLastActiveTime() + HawkTime.MINUTE_MILLI_SECONDS < HawkTime.getMillisecond()){
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,dyzzGameData activeTime err ,playerId:{},teamId:{},gameId:{},gameState:{}", 
					player.getId(),team.getTeamId(),team.getGameRoomId(),game.getState().getNumber());
			return  Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE;
		}
		DYZZPlayerData playerData = DYZZRedisData.getInstance().getDYZZPlayerData(termId,player.getId());
		if(playerData == null){
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,DYZZPlayerData null ,playerId:{},teamId:{},gameId:{}", player.getId(),team.getTeamId(),team.getGameRoomId());
			return  Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE;
		}
		if(!playerData.getGameId().equals(team.getGameRoomId())){
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,DYZZPlayerData gameId err ,playerId:{},teamId:{},gameId:{},playerGameId:{}",
					player.getId(),team.getTeamId(),team.getGameRoomId(),playerData.getGameId());
			return  Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE;
		}
		if(playerData.isMidwayQuit()){
			HawkLog.logPrintln("DYZZ sourceCheckEnterInstance,DYZZPlayerData MidwayQuit err ,playerId:{},teamId:{},gameId:{}",
					player.getId(),team.getTeamId(),team.getGameRoomId());
			return  Status.DYZZError.DYZZ_WAR_MID_QUIT_VALUE; 
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	@MessageHandler 
	public void sourceOnBackServer(DYZZBackServerMsg msg) {
		Player.logger.info("corss player back server plaeyrId:{}", player.getId());
		//在发起退出的时候强行把数据序列化到redis中，返回之后再读一次.
		PlayerDataSerializer.csSyncPlayerData(player.getId(), true);
		//修改顺序 先把数据反序列化回来再移除.
		String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());		
		
		toServerId = toServerId == null ? "NULL" : toServerId;
		//LogUtil.logTimberiumCross(player, toServerId, CrossStateType.CROSS_EXIT);
		
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
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_WAR_CROSS_BACK_FINISH));
		// 设置跨服返回时间
		player.setCrossBackTime(HawkTime.getMillisecond());
		
		RankService.getInstance().checkCityLvlRank(player);
		RankService.getInstance().checkPlayerLvlRank(player);
		DungeonRedisLog.log(player.getId(), "");
		
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
		Player.logger.info("dyzz condtion check  playerId:{}, errorCode:{}, serverId:{}", player.getId(), tryCrossErrorCode, targetServerId);
		if (tryCrossErrorCode == Status.SysError.SUCCESS_OK_VALUE) {
			DYZZCrossMsg.Builder msgBuilder = DYZZCrossMsg.newBuilder();
			msgBuilder.setServerId(targetServerId);
			InnerEnterCrossReq.Builder builder = InnerEnterCrossReq.newBuilder();
			builder.setCurTime(HawkTime.getSeconds());
			player.setCrossStatus(PlayerCrossStatus.PREPARE_CROSS);
			//发起一个远程RPC到远程去,
			HawkProtocol protocl = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_REQ, builder);			
			CrossProxy.getInstance().rpcRequest(protocl, new DYZZPrepareEnterCallback(player, msgBuilder.build()), targetServerId, player.getId(), "");
		} else {			
			DYZZCallbackOperationService.getInstance().onPrepareCrossFail(player);
		}
		return true;
	}
	
	/**
	 * 迁回。
	 * @param msg
	 */
	@MessageHandler
	public void targetOnMoveBack(DYZZMoveBackCrossPlayerMsg msg) {
		Player.logger.info("DYZZ playerId:{} receive move back msg", player.getId());
		if (!player.isCsPlayer()) {
			Player.logger.error("DYZZ player isn't csplayer can not receive this protocol playerId:{}", player.getId());
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
	public void sourceOnPrepareMoveBack(DYZZPrepareMoveBackMsg msg) {
		String toServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
		if (HawkOSOperator.isEmptyString(toServerId)) {
			Player.logger.error("playerId:{} DYZZPrepareMoveBackMsg prepare force move back toServerId is null ", player.getId());
			return;
		}
		Player.logger.info("playerId:{} DYZZPrepareMoveBackMsg prepare move back toServerId:{}", player.getId(), toServerId);
		PBDYZZWarMoveBackReq.Builder req = PBDYZZWarMoveBackReq.newBuilder();
		req.setPlayerId(player.getId());
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code2.DYZZ_WAR_MOVE_BACK_REQ_VALUE, req);
		CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, player.getId(), "");
	}
	
	
	private boolean isCrossToSelf(String crossToServerId) {		
		return GsConfig.getInstance().getServerId().equals(crossToServerId);
	}
	
	
	
	private HawkTuple2<String, Integer> getCrossToServerId() {
		DYZZTeamRoom team = DYZZService.getInstance().getPlayerTeamRoom(player.getId());
		if(team == null){
			HawkLog.logPrintln("DYZZ getCrossToServerId,player team null ,playerId:{}", player.getId());
			return new HawkTuple2<String, Integer>("", Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
		}
		if(team.getState() != PBDYZZTeamState.DYZZ_TEAM_GAMING){
			HawkLog.logPrintln("DYZZ getCrossToServerId,player team state err ,playerId:{},teamId:{},teamState:{}", 
					player.getId(),team.getTeamId(),team.getState().getNumber());
			return new HawkTuple2<String, Integer>("", Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
		}
		int termId = DYZZService.getInstance().getDYZZWarTerm();
		String gameId = team.getGameRoomId();
		if(HawkOSOperator.isEmptyString(gameId)){
			HawkLog.logPrintln("DYZZ getCrossToServerId,player team gameId err ,playerId:{},teamId:{},teamState:{},teamGameId:{}", 
					player.getId(),team.getTeamId(),team.getState().getNumber(),team.getGameRoomId());
			return new HawkTuple2<String, Integer>("", Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
		}
		DYZZGameRoomData game = DYZZRedisData.getInstance().getDYZZGameData(termId, gameId);
		if(game == null){
			HawkLog.logPrintln("DYZZ getCrossToServerId,gameData null ,playerId:{},teamId:{},teamState:{},teamGameId:{}", 
					player.getId(),team.getTeamId(),team.getState().getNumber(),team.getGameRoomId());
			return new HawkTuple2<String, Integer>("", Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
		}
		if(game.getState() != PBDYZZGameRoomState.DYZZ_GAME_GAMING){
			HawkLog.logPrintln("DYZZ getCrossToServerId,game state err ,playerId:{},teamId:{},teamState:{},teamGameId:{},gameState:{}", 
					player.getId(),team.getTeamId(),team.getState().getNumber(),team.getGameRoomId(),game.getState().getNumber());
			return new HawkTuple2<String, Integer>("", Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE); 
		}
		return new HawkTuple2<String, Integer>(game.getServerId(), Status.SysError.SUCCESS_OK_VALUE);
		
	}
	


	
}
 