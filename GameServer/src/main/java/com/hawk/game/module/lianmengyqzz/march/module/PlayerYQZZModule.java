package com.hawk.game.module.lianmengyqzz.march.module;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.hawk.common.IDIPBanInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.lianmengyqzz.march.cfg.*;
import com.hawk.game.module.lianmengyqzz.march.data.global.*;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZSeasonGiftRecordData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.model.President;
import com.hawk.game.protocol.*;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
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
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.crossproxy.yqzz.YQZZCallbackOperationService;
import com.hawk.game.crossproxy.yqzz.YQZZPrepareEnterCallback;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitReason;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitRoomMsg;
import com.hawk.game.module.lianmengyqzz.march.achieve.YQZZAchievManager;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.entitiy.PlayerYQZZData;
import com.hawk.game.module.lianmengyqzz.march.entitiy.PlayerYQZZEntity;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchieve;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZBackServerMsg;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZExitCrossInstanceMsg;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZMoveBackCrossPlayerMsg;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZPrepareExitCrossInstanceMsg;
import com.hawk.game.module.lianmengyqzz.march.msg.YQZZPrepareMoveBackMsg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.Cross.InnerEnterCrossReq;
import com.hawk.game.protocol.Cross.YQZZCrossMsg;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarAchieveAwardReq;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarAchieveResp;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarHistroyDetailReq;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarInnerBackServerReq;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarMoveBackReq;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarRankReq;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarScoreDetailReq;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
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
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;

/**
 * 月球之战
 * @author che
 */
public class PlayerYQZZModule extends PlayerModule {

	private long updateTick;

	public static int serverGroupRank;
	
	public PlayerYQZZModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		PlayerYQZZEntity ydata = player.getData().getPlayerYqzzEntity();
		if(ydata.getPlayerYQZZData() == null){
			ydata.setTermId(0);
			ydata.setAchieveSerialized("");
			ydata.afterRead();
		}
		//登陆同步数据
		YQZZMatchService.getInstance().syncYQZZWarInfo(player, true, true);
		YQZZMatchService.getInstance().syncYQZZLeagueWarInfo(player);
		return true;
	}
	
	
	@Override
	public boolean onTick() {
		//2S更新
		long curTime = HawkTime.getMillisecond();
		if((this.updateTick + 2 * 1000) > curTime){
			return true;
		}
		this.updateTick = curTime;
		//检测初始化数据
		this.checkInit();
		//检查数据更新设置
		this.checkUpdate();
		//更新成就数据
		this.updateAchieve();
		return true;
	}
	
		
	/**
	 * 更新成就
	 */
	private void updateAchieve(){
		YQZZActivityStateData stateData = YQZZMatchService.getInstance().getDataManger().getStateData();
		int term = stateData.getTermId();
		YQZZActivityState state = stateData.getState();
		if(term == 0 || state == YQZZActivityState.HIDDEN){
			return;
		}
		PlayerYQZZData data = this.player.getPlayerYQZZData();
		List<YQZZAchieve> update = YQZZAchievManager.getInstance()
				.parserAchieve(data.getAchieves());
		if(update!= null && !update.isEmpty()){
			data.notifyChange();
			//同步数据
			PBYQZZWarAchieveResp.Builder builder = PBYQZZWarAchieveResp.newBuilder();
			builder.setAchieveInfo(data.genAchieveBuilder());
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_ACHIEVE_INFO_RESP, builder));
		}
	}
	
	
	/***
	 * 检测数据初始化
	 */
	private void checkInit(){
		YQZZActivityStateData stateData = YQZZMatchService.getInstance().getDataManger().getStateData();
		int term = stateData.getTermId();
		YQZZActivityState state = stateData.getState();
		if(term == 0 || state == YQZZActivityState.HIDDEN){
			return;
		}
		PlayerYQZZData data = this.player.getPlayerYQZZData();
		if(data.getTermId() == term){
			return;
		}
		HawkLog.logPrintln("PlayerYQZZModule checkInit reset, playerId:{},from:{},to:{}", player.getId(),data.getTermId(),term);
		String playerGuild = this.player.getGuildId();
		data.setTermId(term);
		data.setPlayerGuild(playerGuild);
		this.initAchieve(data);
		
		PBYQZZWarAchieveResp.Builder builder = PBYQZZWarAchieveResp.newBuilder();
		builder.setAchieveInfo(data.genAchieveBuilder());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_ACHIEVE_INFO_RESP, builder));
	}
	
	
	/**
	 * 检查数据变化并且更新
	 */
	private void checkUpdate(){
		YQZZActivityStateData stateData = YQZZMatchService.getInstance().getDataManger().getStateData();
		int term = stateData.getTermId();
		YQZZActivityState state = stateData.getState();
		if(term == 0){
			return;
		}
		if(state == YQZZActivityState.HIDDEN 
				|| state == YQZZActivityState.END_SHOW 
						|| state == YQZZActivityState.REWARD){
			return;
		}
		PlayerYQZZData data = this.player.getPlayerYQZZData();
		String playerGuild = this.player.getGuildId();
		if(!Objects.equals(playerGuild, data.getPlayerGuild())){
			data.setPlayerGuild(playerGuild);
		}
	}
	
	
	
	/**
	 * 初始化成就
	 * @param data
	 */
	private void initAchieve(PlayerYQZZData data){
		Map<Integer,YQZZAchieve> map = new ConcurrentHashMap<Integer, YQZZAchieve>();
		List<YQZZPlayerAchieveCfg> plist = HawkConfigManager.getInstance()
				.getConfigIterator(YQZZPlayerAchieveCfg.class).toList();
		List<YQZZAllianceAchieveCfg> alist = HawkConfigManager.getInstance()
				.getConfigIterator(YQZZAllianceAchieveCfg.class).toList();
		List<YQZZCountryAchieveCfg> clist = HawkConfigManager.getInstance()
				.getConfigIterator(YQZZCountryAchieveCfg.class).toList();
		List<YQZZAchieveCfg> cfgs = new ArrayList<>();
		cfgs.addAll(plist);
		cfgs.addAll(alist);
		cfgs.addAll(clist);
		//加载个人任务
		for(YQZZAchieveCfg cfg : cfgs){
			YQZZAchieve achive = new YQZZAchieve();
			boolean initRlt = achive.init(cfg, data);
			if(initRlt){
				map.put(achive.getAchieveId(), achive);
			}
		}
		data.restAchieves(map);
		HawkLog.logPrintln("PlayerYQZZModule initAchieve, playerId:{},termId:{},achieves{}", 
				player.getId(),data.getTermId(),data.serializAchieve());
	}
	
	/**
	 * 活动信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.YQZZ_PAGE_INFO_REQ_VALUE)
	private void onWarInfo(HawkProtocol protocol) {
		YQZZMatchService.getInstance().syncYQZZWarInfo(player, true, true);
	}
	
	
	/**
	 * 匹配房间信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.YQZZ_MATCH_INFO_REQ_VALUE)
	private void onMatchRoomInfo(HawkProtocol protocol) {
		YQZZMatchService.getInstance().syncYQZZMatchRoomInfo(player);
	}
	
	/**
	 * 历史页面信息
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.YQZZ_HISTORY_INFO_REQ_VALUE)
	private void onHistoryInfo(HawkProtocol protocol) {
		YQZZMatchService.getInstance().syncYQZZHistoryInfo(player);
	}
	
	
	@ProtocolHandler(code = HP.code2.YQZZ_HISTORY_DETAIL_INFO_REQ_VALUE)
	private void onHistoryDetail(HawkProtocol protocol) {
		PBYQZZWarHistroyDetailReq req = protocol.parseProtocol(PBYQZZWarHistroyDetailReq.getDefaultInstance());
		int type = req.getType();
		int termId = req.getTermId();
		if(type == 1){
			YQZZMatchService.getInstance()
				.syncYQZZHistoryDetailContry(player, termId);
		}else if(type == 2){
			YQZZMatchService.getInstance()
				.syncYQZZHistoryDetailGuild(player, termId);			
		}else if(type == 3){
			YQZZMatchService.getInstance()
				.syncYQZZHistoryDetailPlayer(player, termId);
		}
	}
	
	
	@ProtocolHandler(code = HP.code2.YQZZ_ACHIEVE_AWARD_REQ_VALUE)
	public  void onAchiveReward(HawkProtocol protocol){
		PBYQZZWarAchieveAwardReq req = protocol.parseProtocol(PBYQZZWarAchieveAwardReq.getDefaultInstance());
		int ahchiveId= req.getAchieveId();
		YQZZActivityStateData stateData = YQZZMatchService.getInstance().getDataManger().getStateData();
		int term = stateData.getTermId();
		YQZZActivityState state = stateData.getState();
		if(term == 0 || state == YQZZActivityState.HIDDEN){
			player.sendError(HP.code2.YQZZ_ACHIEVE_AWARD_REQ_VALUE,
					Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE, 0);
			return;
		}
		boolean achive = player.getPlayerYQZZData().achieveReward(ahchiveId);
		if(achive){
			PBYQZZWarAchieveResp.Builder builder = PBYQZZWarAchieveResp.newBuilder();
			builder.setAchieveInfo( player.getPlayerYQZZData().genAchieveBuilder());
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_ACHIEVE_INFO_RESP, builder));
			HawkLog.logPrintln("PlayerYQZZModule onAchiveReward, playerId:{},termId:{},achieveId{}", 
					player.getId(),player.getPlayerYQZZData().getTermId(),ahchiveId);
			LogUtil.logYQZZAchieveReward(player, player.getPlayerYQZZData().getTermId(), ahchiveId);
		}
	}
	
	
	@ProtocolHandler(code = HP.code2.YQZZ_SCORE_DETAIL_REQ_VALUE)
	public  void onScoreDetail(HawkProtocol protocol){
		PBYQZZWarScoreDetailReq req = protocol.parseProtocol(PBYQZZWarScoreDetailReq.getDefaultInstance());
		int type= req.getType();
		if(type == 1){
			YQZZMatchService.getInstance().syncYQZZContryScoreDetail(player);
		}else if(type == 2){
			YQZZMatchService.getInstance().syncYQZZGuildScoreDetail(player);
		}else if(type == 3){
			YQZZMatchService.getInstance().syncYQZZPlayerScoreDetail(player);
		}
	}
	
	
	@ProtocolHandler(code = HP.code2.YQZZ_SCORE_RANK_REQ_VALUE)
	public  void onRankDetail(HawkProtocol protocol){
		PBYQZZWarRankReq req = protocol.parseProtocol(PBYQZZWarRankReq.getDefaultInstance());
		int type= req.getType();
		if(type == 1){
			YQZZMatchService.getInstance().syncYQZZRankContry(player);
		}else if(type == 2){
			YQZZMatchService.getInstance().syncYQZZRankGuild(player);
		}else if(type == 3){
			YQZZMatchService.getInstance().syncYQZZRankPlayer(player);
		}
	}
	
	
	/**
	 * 退出战场房间
	 * @return
	 */
	@MessageHandler
	private boolean onQuitRoomMsg(YQZZQuitRoomMsg msg) {
		try {
			CrossService.getInstance().addForceMoveBackYqzzPlayer(player.getId());
			if(msg.getQuitReason() == YQZZQuitReason.LEAVE){
				player.getPlayerYQZZData().setLeaveBattleTime(HawkTime.getMillisecond());
			}
			boolean isMidwayQuit = msg.getQuitReason() == YQZZQuitReason.LEAVE;
			int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
			LogUtil.logYQZZQuitInfo(player.getId(), termId, msg.getRoomId(),player.getGuildId(), isMidwayQuit);
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
		armysCheckAndFix(player);
		// 通知客户端跨服开始
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_CROSS_BACK_BEGIN));
		
		//检测到可以退出了那么先设置标志位
		player.setCrossStatus(GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		
		//添加到检测队列
		CrossService.getInstance().addExitYqzzPlayer(player.getId());
		
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
	
	private void armysCheckAndFix(Player player) {
		// 玩家出征部队数量
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		if (marchCount > 0) {
			DungeonRedisLog.log(player.getId(), "armysCheckAndFix, playerId: {}, marchCount: {}", player.getId(), marchCount);
			return;
		}

		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
			// 出征中的army数量
			int marchArmyCount = armyEntity.getMarch();
			if (marchArmyCount <= 0) {
				continue;
			}

			int armyId = armyEntity.getArmyId();
			armyIds.add(armyId);

			armyEntity.clearMarch();
			armyEntity.addFree(marchArmyCount);
			LogUtil.logArmyChange(player, armyEntity, marchArmyCount, ArmySection.FREE, ArmyChangeReason.MARCH_FIX);
			DungeonRedisLog.log(player.getId(), "armysCheckAndFix, playerId:{}, armyId:{}, marchArmyCount:{}, armyFree:{}", armyEntity.getPlayerId(), armyEntity.getId(),
					marchArmyCount, armyEntity.getFree());
		}

		if (!armyIds.isEmpty()) {
			player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, armyIds.toArray(new Integer[armyIds.size()]));
		}
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
		
		Player.logger.info("YQZZ playerId:{}, exit cross  errorCode:{}", player.getId(), errorCode);
							
		//把玩家在本服的痕迹清理掉
		String fromServerId = CrossService.getInstance().removeImmigrationPlayer(player.getId());
		GlobalData.getInstance().removeAccountInfoOnExitCross(player.getId());
		HawkApp.getInstance().removeObj(player.getXid());
		GlobalData.getInstance().invalidatePlayerData(player.getId());					
		
		PBYQZZWarInnerBackServerReq.Builder req = PBYQZZWarInnerBackServerReq.newBuilder();
		req.setPlayerId(player.getId());
		//发送一个协会回原服.
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_INNER_BACK_SERVER_REQ_VALUE, req), fromServerId, "");
		String mainServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
		//设置redis状态, 都已经到了这一步了，失败了就失败了，也不能怎么样了.
		boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(mainServerId, player.getId(), GsConst.PlayerCrossStatus.PREPARE_EXIT_CROSS);
		if (!setCrossStatus) {
			Player.logger.error("yqzz player exit cross set cross status fail playerId:{}", player.getId());
		}
		DungeonRedisLog.log(player.getId(), "mainServerId{}",mainServerId);
	}
		
	/**
	 * 预退出跨服
	 * @param msg
	 */
	@MessageHandler
	public void targetOnPrepareExitCrossMsg(YQZZPrepareExitCrossInstanceMsg msg) {
		HawkLog.logPrintln("PlayerYQZZModule targetOnPrepareExitCrossMsg YQZZ "
				+ "prepare exit cross Instance from msg playerId:{}", player.getId());
		if(!player.isInDungeonMap()){
			targetDoPrepareExitCrossInstance();		
		}
	}
	
	/**
	 * 发出退出跨服信息.
	 * @param msg
	 */
	@MessageHandler
	public void targetOnExitInstanceMessage(YQZZExitCrossInstanceMsg msg) {
		HawkLog.logPrintln("PlayerYQZZModule targetOnExitInstanceMessage "
				+ "yqzz exitInstance from msg playerId:{}", player.getId());
		targetDoExitInstance();		
	}
	
	@ProtocolHandler(code = HP.code2.YQZZ_WAR_EXIT_INSTANCE_REQ_VALUE)
	public void onExitInstance(HawkProtocol hawkProtocol) {
		Player.logger.info("playerId:{} exit yqzz war from protocol", player.getId());
		if (player.isCsPlayer()) {
			if (!YQZZMatchService.getInstance().isOperateTime()) {
				player.sendError(hawkProtocol.getType(), Status.YQZZError.YQZZ_UNOPERATE_TIME_VALUE, 0);
				return ;
			}
			//远程操作.
			CsPlayer csPlayer = player.getCsPlayer();
			if (!csPlayer.isCrossType(CrossType.YQZZ_VALUE)) {
				csPlayer.sendError(hawkProtocol.getType(), Status.YQZZError.YQZZ_WAR_NOT_IN_INSTANCE_VALUE, 0);
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
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_SIMULATE_CROSS_BEGIN_VALUE));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_SIMULATE_CROSS_FINISH_VALUE));
				boolean rlt = YQZZMatchService.getInstance().joinRoom(player);
				Player.logger.info("playerId:{} enter local yqzz instance result:{}", player.getId(), rlt);
			}
		});
	}
	
	private void simulateExitCross() {
		YQZZMatchService.getInstance().getDataManger().removeJoinExtraPlayer(player.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_SIMULATE_CROSS_BACK_BEGIN));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_SIMULATE_CROSS_BACK_FINISH_VALUE));	
				Player.logger.info("playerId:{} exit local yqzz instance", player.getId());
			}
		});
	}

	@ProtocolHandler(code = HP.code2.YQZZ_WAR_ENTER_INSTANCE_REQ_VALUE)
	public void onEnterInstance(HawkProtocol hawkProtocol) {	
		if(player.isCsPlayer()){
			player.sendError(HP.code2.YQZZ_WAR_ENTER_INSTANCE_REQ_VALUE,
					Status.YQZZError.YQZZ_WAR_IN_CROSS_VALUE, 0);
			return;
		}
		String playerServerId = GsConfig.getInstance().getServerId();
		YQZZActivityState state = YQZZMatchService.getInstance()
				.getDataManger().getStateData().getState();
		int termId =YQZZMatchService.getInstance()
				.getDataManger().getStateData().getTermId();
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		long curTime = HawkTime.getMillisecond();
		if(state != YQZZActivityState.BATTLE){
			player.sendError(HP.code2.YQZZ_WAR_ENTER_INSTANCE_REQ_VALUE,
					Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE, 0);
			return;
		}
		if (!YQZZMatchService.getInstance().isOperateTime()) {
			player.sendError(hawkProtocol.getType(), Status.YQZZError.YQZZ_UNOPERATE_TIME_VALUE, 0);
			return ;
		}
		YQZZMatchRoomData roomData = YQZZMatchService.getInstance().getDataManger().getRoomData();
		if(roomData == null){
			player.sendError(HP.code2.YQZZ_WAR_ENTER_INSTANCE_REQ_VALUE,
					Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE, 0);
			return;
		}
		YQZZJoinServer roomServer = YQZZMatchService.getInstance()
				.getDataManger().getRoomServerById(playerServerId);
		if(roomServer == null){
			player.sendError(HP.code2.YQZZ_WAR_ENTER_INSTANCE_REQ_VALUE,
					Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE, 0);
			return;
		}
		String playerGuild = player.getGuildId();
		if(HawkOSOperator.isEmptyString(playerGuild)){
			player.sendError(HP.code2.YQZZ_WAR_ENTER_INSTANCE_REQ_VALUE,
					Status.YQZZError.YQZZ_WAR_ENTER_GUILD_LIMIT_VALUE, 0);
			return;
		}
		long leaveTime = player.getPlayerYQZZData().getLeaveBattleTime();
		if(curTime - leaveTime <= constCfg.getJoinBattleCdTime()){
			player.sendError(HP.code2.YQZZ_WAR_ENTER_INSTANCE_REQ_VALUE,
					Status.YQZZError.YQZZ_WAR_ENTER_CD_LIMIT_VALUE, 0);
			return;
		}
		HawkTuple2<String, Integer> tuple = getCrossToServerId();
		int status = tuple.second;
		if (status != Status.SysError.SUCCESS_OK_VALUE) {
			Player.logger.info("playerId:{} try to enter yqzz war failed errorCode:{}", player.getId(), status);
			this.sendError(hawkProtocol.getType(), status);
			return;
		}	
		String serverId = tuple.first;
		Player.logger.info("playerId:{} try to enter yqzz war serverId:{}", player.getId(), serverId == null ? "null" : serverId);
		if (HawkOSOperator.isEmptyString(serverId)) {
			this.sendError(hawkProtocol.getType(), Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE);
			return;
		}
		
		int errorCode = sourceCheckEnterInstance(serverId);
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(hawkProtocol.getType(), errorCode);
			return;
		}			
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		long shieldEndTime = timeCfg.getEndShowTimeValue() + HawkTime.HOUR_MILLI_SECONDS;  
		if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() < shieldEndTime) {
			StatusDataEntity entity = player.addStatusBuff(GameConst.CITY_SHIELD_BUFF_ID, shieldEndTime);			
			if (entity != null) {
				player.getPush().syncPlayerStatusInfo(false, entity);
			}
		}
		if(!roomServer.getFreePlayers().containsKey(player.getId())){
			long battleTime = YQZZMatchService.getInstance().getState().getTimeCfg().getBattleTimeValue();
			int joinExtraCount = (int) ((curTime - battleTime) / 60000 * constCfg.getPlayerJoinExtraCountMin() + constCfg.getPlayerJoinExtraCount());
			boolean result = YQZZMatchService.getInstance().getDataManger().addJoinExtraPlayer(player.getId(), joinExtraCount);
			if (!result) {
				this.sendError(hawkProtocol.getType(), Status.YQZZError.YQZZ_WAR_ENTER_CNT_OVER_LIMIT_VALUE);
				return;
			}
		}
		//添加入记录
		YQZZJoinPlayerData joinData = new YQZZJoinPlayerData(roomData.getTermId(),roomData.getRoomId(),player);
		YQZZRoomPlayerData playerRoomData = new YQZZRoomPlayerData(roomData.getTermId(),roomData.getRoomId(),player);
		joinData.saveRedis();
		playerRoomData.saveRedis();
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
		// 有行军
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if (!marchs.isEmpty()) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,player has march,playerId:{}", player.getId());
			return Status.YQZZError.YQZZ_HAS_PLYAER_MARCH_VALUE;
		}
		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,player war fever,playerId:{}", player.getId());
			return Status.YQZZError.YQZZ_WAR_IN_WAR_FEVER_VALUE;
		}
		// 城内有援助行军，不能进入泰伯利亚
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,player has assist march ,playerId:{}", player.getId());
			return Status.YQZZError.YQZZ_WAR_HAS_ASSISTANCE_MARCH_VALUE;
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
				HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,player has passive march ,playerId:{}", player.getId());
				return Status.YQZZError.YQZZ_WAR_HAS_PASSIVE_MARCH_VALUE;
			}
		}

		// 着火也不行
		if (player.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,player on fire ,playerId:{}", player.getId());
			return Status.YQZZError.YQZZ_WAR_ON_FIRE_VALUE;
		}
		if (player.isInDungeonMap()) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,player in dungeon ,playerId:{},dungeon:{}", player.getId(),player.getDungeonMap());
			return Status.Error.PLAYER_IN_INSTANCE_VALUE;
		}
		// 在联盟军演组队中不能.
		if (WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,player in lmjy team ,playerId:{}", player.getId());
			return Status.Error.LMJY_BAN_OP_VALUE;
		}

		// 已经在跨服中了.
		if (CrossService.getInstance().isCrossPlayer(player.getId())) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,player isCrossPlayer ,playerId:{}", player.getId());
			return Status.CrossServerError.CROSS_FIGHTERING_VALUE;
		}

		// 看下区服有没有开.
		if (!CrossService.getInstance().isServerOpen(serverId)) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,server not open ,playerId:{},serverId:{}", player.getId(),serverId);
			return Status.CrossServerError.CROSS_SERVER_NOT_ACTIVE_VALUE;
		}
		YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
		if(state!= YQZZActivityState.BATTLE){
			HawkLog.logPrintln("YQZZ getCrossToServerId, state err ,playerId:{},state:{}", 
					player.getId(),state.getValue());
			return Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE;
		}
		YQZZMatchRoomData roomData = YQZZMatchService.getInstance().getDataManger().getRoomData();
		if(roomData == null){
			HawkLog.logPrintln("YQZZ getCrossToServerId,roomData null ,playerId:{}", player.getId());
			return Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE;
		}
		if (HawkOSOperator.isEmptyString(roomData.getRoomServerId())) {
			HawkLog.logPrintln("YQZZ sourceCheckEnterInstance,roomData gameId null ,playerId:{},teamId:{}", player.getId(),roomData.getRoomId());
			return Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	@MessageHandler 
	public void sourceOnBackServer(YQZZBackServerMsg msg) {
		Player.logger.info("corss player back server plaeyrId:{}", player.getId());
		//在发起退出的时候强行把数据序列化到redis中，返回之后再读一次.
		PlayerDataSerializer.csSyncPlayerData(player.getId(), true);
		//修改顺序 先把数据反序列化回来再移除.
		String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());		
		toServerId = toServerId == null ? "NULL" : toServerId;
		//LogUtil.logTimberiumCross(player, toServerId, CrossStateType.CROSS_EXIT);
		
		YQZZMatchService.getInstance().getDataManger().removeJoinExtraPlayer(player.getId());
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
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_WAR_CROSS_BACK_FINISH));
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
			boolean setStatus = RedisProxy.getInstance().setPlayerCrossStatus(GsConfig.getInstance().getServerId(), player.getId(), GsConst.PlayerCrossStatus.PREPARE_CROSS);
			if (!setStatus) {
				Player.logger.error("player set cross status fail playerId:{}", player.getId());											
				break tryCross;
			}
			tryCrossErrorCode = Status.SysError.SUCCESS_OK_VALUE;
		}			
		Player.logger.info("yqzz condtion check  playerId:{}, errorCode:{}, serverId:{}", player.getId(), tryCrossErrorCode, targetServerId);
		if (tryCrossErrorCode == Status.SysError.SUCCESS_OK_VALUE) {
			YQZZCrossMsg.Builder msgBuilder = YQZZCrossMsg.newBuilder();
			msgBuilder.setServerId(targetServerId);
			InnerEnterCrossReq.Builder builder = InnerEnterCrossReq.newBuilder();
			builder.setCurTime(HawkTime.getSeconds());
			builder.setCrossType(CrossType.YQZZ);
			player.setCrossStatus(PlayerCrossStatus.PREPARE_CROSS);
			//发起一个远程RPC到远程去,
			HawkProtocol protocl = HawkProtocol.valueOf(CHP.code.INNER_ENTER_CROSS_REQ, builder);			
			CrossProxy.getInstance().rpcRequest(protocl, new YQZZPrepareEnterCallback(player, msgBuilder.build()), targetServerId, player.getId(), "");
		} else {			
			YQZZCallbackOperationService.getInstance().onPrepareCrossFail(player);
		}
		return true;
	}
	
	/**
	 * 迁回。
	 * @param msg
	 */
	@MessageHandler
	public void targetOnMoveBack(YQZZMoveBackCrossPlayerMsg msg) {
		Player.logger.info("YQZZ playerId:{} receive move back msg", player.getId());
		if (!player.isCsPlayer()) {
			Player.logger.error("YQZZ player isn't csplayer can not receive this protocol playerId:{}", player.getId());
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
	public void sourceOnPrepareMoveBack(YQZZPrepareMoveBackMsg msg) {
		String toServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
		if (HawkOSOperator.isEmptyString(toServerId)) {
			Player.logger.error("playerId:{} YQZZPrepareMoveBackMsg prepare force move back toServerId is null ", player.getId());
			return;
		}
		Player.logger.info("playerId:{} YQZZPrepareMoveBackMsg prepare move back toServerId:{}", player.getId(), toServerId);
		PBYQZZWarMoveBackReq.Builder req = PBYQZZWarMoveBackReq.newBuilder();
		req.setPlayerId(player.getId());
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code2.YQZZ_WAR_MOVE_BACK_REQ_VALUE, req);
		CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, player.getId(), "");
	}
	
	
	private boolean isCrossToSelf(String crossToServerId) {		
		return GsConfig.getInstance().getServerId().equals(crossToServerId);
	}
	
	
	
	private HawkTuple2<String, Integer> getCrossToServerId() {
		YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
		if(state!= YQZZActivityState.BATTLE){
			HawkLog.logPrintln("YQZZ getCrossToServerId, state err ,playerId:{},state:{}", 
					player.getId(),state.getValue());
			return new HawkTuple2<String, Integer>("", Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE);
		}
		YQZZMatchRoomData roomData = YQZZMatchService.getInstance().getDataManger().getRoomData();
		if(roomData == null){
			HawkLog.logPrintln("YQZZ getCrossToServerId,roomData null ,playerId:{}", player.getId());
			return new HawkTuple2<String, Integer>("", Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE);
		}
		String playerServer = player.getMainServerId();
		Map<String, YQZZJoinServer> serverMap = YQZZMatchService.getInstance().getDataManger().getRoomServerDataMap();
		if(serverMap == null){
			HawkLog.logPrintln("YQZZ getCrossToServerId,serverMap null ,playerId:{}", player.getId());
			return new HawkTuple2<String, Integer>("", Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE);
		}
		YQZZJoinServer joinServer = serverMap.get(playerServer);
		if(joinServer == null){
			HawkLog.logPrintln("YQZZ getCrossToServerId,joinServer null ,playerId:{}", player.getId());
			return new HawkTuple2<String, Integer>("", Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE);
		}
		Map<String, YQZZJoinGuild> guildMap = YQZZMatchService.getInstance()
				.getDataManger().getRoomGuildsByServer(playerServer);
		if(!guildMap.containsKey(player.getGuildId())){
			HawkLog.logPrintln("YQZZ getCrossToServerId,JoinGuild null ,playerId:{}", player.getId());
			return new HawkTuple2<String, Integer>("", Status.YQZZError.YQZZ_WAR_ENTER_GUILD_LIMIT_VALUE);
		}
		return new HawkTuple2<String, Integer>(roomData.getRoomServerId(), Status.SysError.SUCCESS_OK_VALUE);
	}


	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_WAR_INFO_REQ_VALUE)
	private void onLeagueWarInfo(HawkProtocol protocol) {
		YQZZMatchService.getInstance().syncYQZZLeagueWarInfo(player);
	}

	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_WAR_DETIAL_INFO_REQ_VALUE)
	private void onLeagueWarDetialInfo(HawkProtocol protocol) {
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season <= 0){
			YQZZWar.PBYQZZLeagueWarDetialInfoReq req = protocol.parseProtocol(YQZZWar.PBYQZZLeagueWarDetialInfoReq.getDefaultInstance());
			YQZZWar.PBYQZZWarType type = req.getType();
			YQZZWar.PBYQZZLeagueWarDetialInfoResp.Builder resp = YQZZWar.PBYQZZLeagueWarDetialInfoResp.newBuilder();
			resp.setType(type);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_WAR_DETIAL_INFO_RESP, resp));
			return;
		}
		YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
		YQZZWar.PBYQZZLeagueWarDetialInfoReq req = protocol.parseProtocol(YQZZWar.PBYQZZLeagueWarDetialInfoReq.getDefaultInstance());
		YQZZWar.PBYQZZWarType type = req.getType();
		YQZZWar.PBYQZZLeagueWarDetialInfoResp.Builder resp = YQZZWar.PBYQZZLeagueWarDetialInfoResp.newBuilder();
		resp.setType(type);
		ConfigIterator<YQZZTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		for (YQZZTimeCfg cfg : iterator){
			int turn = cfg.getTurn();
			if(cfg.getSeason() != season
					|| cfg.getType() != type.getNumber()
					|| turn > timeCfg.getTurn()){
				continue;
			}
			Map<String, YQZZMatchRoomData> dataMap = YQZZMatchRoomData.loadAllData(cfg.getTermId());
			if(dataMap.isEmpty()){
				continue;
			}
			boolean isEnd = true;
			if(turn == timeCfg.getTurn()
					&& (state == YQZZActivityState.MATCH || state == YQZZActivityState.BATTLE || state == YQZZActivityState.REWARD)){
				isEnd = false;
			}
			int index = 1;
			for(YQZZMatchRoomData roomData : dataMap.values()){
				if(cfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_KICKOUT_VALUE && !roomData.isAdvance()){
					continue;
				}
				YQZZWar.PBYQZZLeagueWarGroupInfo.Builder group = genGroupInfo(roomData, season, cfg.getTermId(), isEnd);
				group.setTurn(turn);
				group.setIsEnd(isEnd);
				group.setGroup(index);
				index++;
				resp.addGroupInfos(group);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_WAR_DETIAL_INFO_RESP, resp));
	}

	public YQZZWar.PBYQZZLeagueWarGroupInfo.Builder genGroupInfo(YQZZMatchRoomData room, int season, int termId, boolean isEnd){
		YQZZWar.PBYQZZLeagueWarGroupInfo.Builder group = YQZZWar.PBYQZZLeagueWarGroupInfo.newBuilder();
		if(isEnd){
			List<YQZZRecordData> recordDataList = new ArrayList<>();
			for(String serverId : room.getServers()){
				YQZZRecordData recordData = YQZZRecordData.loadData(serverId, termId);
				if(recordData != null){
					recordDataList.add(recordData);
				}
			}
			Collections.sort(recordDataList,new Comparator<YQZZRecordData>(){
				@Override
				public int compare(YQZZRecordData o1, YQZZRecordData o2) {
					if(o1.getRank() != o2.getRank()){
						return o1.getRank() > o2.getRank() ? 1 : -1;
					}
					return 0;
				}
			});
			for(YQZZRecordData recordData : recordDataList){
				group.addServerInfos(genServerInfo(recordData, recordData.getServerId(), season));
			}
		}else {
			for(String serverId : room.getServers()){
				group.addServerInfos(genServerInfo(null, serverId, season));
			}
		}
		return group;
	}

	private YQZZWar.PBYQZZLeagueWarServerInfo.Builder genServerInfo(YQZZRecordData recordData, String serverId, int season){
		YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(season, serverId);
		YQZZWar.PBYQZZLeagueWarServerInfo.Builder serverInfo = YQZZWar.PBYQZZLeagueWarServerInfo.newBuilder();
		serverInfo.setServerId(serverId);
		serverInfo.setServerName(seasonServer.getServerName());
		serverInfo.setLeaderName(seasonServer.getLeaderName());
		serverInfo.setLastRank(0);
		serverInfo.setSeason(season);
		if(recordData != null){
			serverInfo.setRank(recordData.getRank());
			serverInfo.setWinPoint(recordData.getScore());
			serverInfo.setScore(recordData.getSeasonScore());
			serverInfo.setIsKickOut(!recordData.isAdvance());
		}
		return serverInfo;
	}

	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_WAR_SELF_INFO_REQ_VALUE)
	private void onLeagueWarSelfInfo(HawkProtocol protocol) {
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season <= 0){
			YQZZWar.PBYQZZLeagueWarSelfInfoResp.Builder resp = YQZZWar.PBYQZZLeagueWarSelfInfoResp.newBuilder();
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_WAR_SELF_INFO_RESP, resp));
			return;
		}
		YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
		YQZZWar.PBYQZZLeagueWarSelfInfoResp.Builder resp = YQZZWar.PBYQZZLeagueWarSelfInfoResp.newBuilder();
		ConfigIterator<YQZZTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		for (YQZZTimeCfg cfg : iterator) {
			int turn = cfg.getTurn();
			if (cfg.getSeason() != season
					|| turn > timeCfg.getTurn()) {
				continue;
			}
			Map<String, YQZZMatchRoomData> dataMap = YQZZMatchRoomData.loadAllData(cfg.getTermId());
			if (dataMap.isEmpty()) {
				continue;
			}
			if(turn == timeCfg.getTurn()
					&& (state == YQZZActivityState.MATCH || state == YQZZActivityState.BATTLE|| state == YQZZActivityState.REWARD)){
				continue;
			}
			boolean isEnd = true;
			String serverId = player.getMainServerId();
			for(YQZZMatchRoomData roomData : dataMap.values()){
				if(!roomData.getServers().contains(serverId)){
					continue;
				}
				YQZZWar.PBYQZZLeagueWarGroupInfo.Builder group = genGroupInfo(roomData, season, cfg.getTermId(), isEnd);
				group.setTurn(turn);
				group.setIsEnd(isEnd);
				resp.addGroupInfos(group);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_WAR_SELF_INFO_RESP, resp));
	}

	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_GROUP_WAR_SERVER_RANK_REQ_VALUE)
	private void onLeagueGroupWarServerRank(HawkProtocol protocol) {
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season <= 0){
			YQZZWar.PBYQZZLeagueKickoutWarServerRankResp.Builder resp = YQZZWar.PBYQZZLeagueKickoutWarServerRankResp.newBuilder();
			resp.setMyInfo(buildSlefServerInfo());
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GROUP_WAR_SERVER_RANK_RESP, resp));
			return;
		}
		YQZZWar.PBYQZZLeagueKickoutWarServerRankResp.Builder resp = YQZZWar.PBYQZZLeagueKickoutWarServerRankResp.newBuilder();
		Map<String, YQZZSeasonServer> serverMap = YQZZSeasonServer.loadAll(season);
		YQZZWar.PBYQZZLeagueWarServerInfo.Builder selfServer = null;
		List<HawkTuple2<String, Integer>> rankList = YQZZSeasonServer.getGroupRank(season);
		for(HawkTuple2<String, Integer> tuple2 : rankList){
			String serverId = tuple2.first;
			int rank = tuple2.second;
			YQZZSeasonServer seasonServer = serverMap.get(serverId);
			if(seasonServer == null || seasonServer.getScore() == 0){
				continue;
			}
			YQZZWar.PBYQZZLeagueWarServerInfo.Builder builder = genServerInfo(seasonServer);
			builder.setRank(rank);
			resp.addRankInfos(builder);
			if(GsConfig.getInstance().getServerId().equals(serverId)){
				selfServer = builder;
			}
		}
		if(selfServer!=null){
			resp.setMyInfo(selfServer);
		}else {
			resp.setMyInfo(buildSlefServerInfo());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GROUP_WAR_SERVER_RANK_RESP, resp));
	}

	private YQZZWar.PBYQZZLeagueWarServerInfo.Builder genServerInfo(YQZZSeasonServer seasonServer){
		YQZZWar.PBYQZZLeagueWarServerInfo.Builder serverInfo = YQZZWar.PBYQZZLeagueWarServerInfo.newBuilder();
		serverInfo.setServerId(seasonServer.getServerId());
		serverInfo.setServerName(seasonServer.getServerName());
		serverInfo.setScore(seasonServer.getScore());
		serverInfo.setWinPoint(seasonServer.getTotalPoint());
		serverInfo.setLastRank(0);
		serverInfo.setLeaderName(seasonServer.getLeaderName());
		serverInfo.setSeason(seasonServer.getSeason());
		return serverInfo;

	}

	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_KICKOUT_WAR_SERVER_RANK_REQ_VALUE)
	private void onLeagueKickoutWarServerRank(HawkProtocol protocol) {
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season <= 0){
			YQZZWar.PBYQZZLeagueKickoutWarServerRankResp.Builder resp = YQZZWar.PBYQZZLeagueKickoutWarServerRankResp.newBuilder();
			resp.setMyInfo(buildSlefServerInfo());
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_KICKOUT_WAR_SERVER_RANK_RESP, resp));
			return;
		}
		Map<String, YQZZSeasonServer> serverMap = YQZZSeasonServer.loadAll(season);
		List<HawkTuple2<String, Integer>> rankList = YQZZSeasonServer.getFinalRank(season);
		int min = 24;
		if(rankList.size() > 0){
			min = rankList.get(0).second - 1;
		}
		YQZZWar.PBYQZZLeagueKickoutWarServerRankResp.Builder resp = YQZZWar.PBYQZZLeagueKickoutWarServerRankResp.newBuilder();
		for(int i = 1;i <= min; i++){
			YQZZWar.PBYQZZLeagueWarServerInfo.Builder builder = YQZZWar.PBYQZZLeagueWarServerInfo.newBuilder();
			builder.setServerId("-1");
			builder.setServerName("虚位以待");
			builder.setRank(i);
			builder.setSeason(season);
			resp.addRankInfos(builder);
		}
		YQZZWar.PBYQZZLeagueWarServerInfo.Builder selfServer = null;
		for(HawkTuple2<String, Integer> tuple2 : rankList){
			String serverId = tuple2.first;
			int rank = tuple2.second;
			YQZZSeasonServer seasonServer = serverMap.get(serverId);
			if(seasonServer == null || seasonServer.getScore() == 0){
				continue;
			}
			YQZZWar.PBYQZZLeagueWarServerInfo.Builder builder = YQZZWar.PBYQZZLeagueWarServerInfo.newBuilder();
			builder.setServerId(serverId);
			builder.setServerName(seasonServer.getServerName());
			builder.setRank(rank);
			builder.setIsEnd(true);
			builder.setSeason(season);
			resp.addRankInfos(builder);
			if(GsConfig.getInstance().getServerId().equals(serverId)){
				selfServer = builder;
			}
		}
		if(selfServer != null){
			resp.setMyInfo(selfServer);
		}else {
			resp.setMyInfo(buildSlefServerInfo());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_KICKOUT_WAR_SERVER_RANK_RESP, resp));
	}

	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_WAR_GUILD_RANK_REQ_VALUE)
	private void onLeagueWarGuildRank(HawkProtocol protocol) {
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season <= 0 || timeCfg.getType() != YQZZWar.PBYQZZWarType.YQZZ_KICKOUT_VALUE){
			YQZZWar.PBYQZZLeagueWarGuildRankResp.Builder resp = YQZZWar.PBYQZZLeagueWarGuildRankResp.newBuilder();
			resp.setMyInfo(buildSelfGuildInfo(-1, 0));
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_WAR_GUILD_RANK_RESP, resp));
			return;
		}
		YQZZWar.PBYQZZLeagueWarGuildRankResp.Builder resp = YQZZWar.PBYQZZLeagueWarGuildRankResp.newBuilder();
		resp.addAllRankInfos(YQZZSeasonGuild.getRankPBList(season));
		if(player.hasGuild()){
			HawkTuple2<Integer, Long> tuple2 = YQZZSeasonGuild.getSelfRank(season, player.getGuildId());
			resp.setMyInfo(buildSelfGuildInfo(tuple2.first, tuple2.second));
		}else{
			resp.setMyInfo(buildSelfGuildInfo(-1, 0));
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_WAR_GUILD_RANK_RESP, resp));

	}

	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUA_WAR_GET_SCORE_INFO_REQ_VALUE)
	private void onLeagueWarGetScoreInfo(HawkProtocol protocol) {
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season <= 0){
			YQZZWar.YLWGetScoreInfoResp.Builder resp = YQZZWar.YLWGetScoreInfoResp.newBuilder();
			resp.setNationalScore(buildScoreInfo(0, new ArrayList<>()));
			resp.setGuildScore(buildScoreInfo(0, new ArrayList<>()));
			resp.setSelfScore(buildScoreInfo(0, new ArrayList<>()));
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUA_WAR_GET_SCORE_INFO_RESP, resp));
			return;
		}
		YQZZWar.YLWGetScoreInfoResp.Builder resp = YQZZWar.YLWGetScoreInfoResp.newBuilder();
		YQZZSeasonServer server = YQZZSeasonServer.loadByServerId(season, player.getMainServerId());
		if(server != null){
			resp.setNationalScore(buildScoreInfo(server.getTotalPoint(), server.getReward()));
		}else {
			resp.setNationalScore(buildScoreInfo(0, new ArrayList<>()));
		}
		YQZZSeasonGuild guild = YQZZSeasonGuild.loadByGuildId(season, player.getGuildId());
		YQZZSeasonPlayer seasonPlayer = YQZZSeasonPlayer.loadByPlayerId(season, player.getId());
		if(guild != null && seasonPlayer != null){
			if(seasonPlayer != null){
				resp.setGuildScore(buildScoreInfo(guild.getTotalPoint(), seasonPlayer.getGuildReward()));
			}
		}else {
			if(seasonPlayer != null){
				resp.setGuildScore(buildScoreInfo(0, seasonPlayer.getGuildReward()));
			}else {
				resp.setGuildScore(buildScoreInfo(0, new ArrayList<>()));
			}

		}

		if(seasonPlayer != null){
			resp.setSelfScore(buildScoreInfo(seasonPlayer.getTotalPoint(), seasonPlayer.getReward()));
		}else {
			resp.setSelfScore(buildScoreInfo(0, new ArrayList<>()));
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUA_WAR_GET_SCORE_INFO_RESP, resp));
	}




	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_GIFT_INFO_REQ_VALUE)
	private void onLeagueGiftInfo(HawkProtocol protocol) {
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season <= 0
				|| timeCfg.getType() != YQZZWar.PBYQZZWarType.YQZZ_KICKOUT_VALUE){
			YQZZWar.YQZZGiftInfoResp.Builder resp = YQZZWar.YQZZGiftInfoResp.newBuilder();
			resp.setGiftId(-1);
			resp.setSendCount(0);
			resp.setRank(-1);
			resp.setIsEnd(false);
			resp.setCanSend(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GIFT_INFO_RESP, resp));
			return;
		}
		HawkTuple2<Integer, Integer> turnTuple2 = getSeasonMaxTurn(season);
		if(timeCfg.getTurn() != turnTuple2.second){
			YQZZWar.YQZZGiftInfoResp.Builder resp = YQZZWar.YQZZGiftInfoResp.newBuilder();
			resp.setGiftId(-1);
			resp.setSendCount(0);
			resp.setRank(-1);
			resp.setIsEnd(false);
			resp.setCanSend(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GIFT_INFO_RESP, resp));
			return;
		}
		YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
		if(state != YQZZActivityState.END_SHOW && state != YQZZActivityState.HIDDEN){
			YQZZWar.YQZZGiftInfoResp.Builder resp = YQZZWar.YQZZGiftInfoResp.newBuilder();
			resp.setGiftId(-1);
			resp.setSendCount(0);
			resp.setRank(-1);
			resp.setIsEnd(false);
			resp.setCanSend(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GIFT_INFO_RESP, resp));
			return;
		}
		long now = HawkTime.getMillisecond();
		if(state == YQZZActivityState.HIDDEN && now > timeCfg.getSeasonEndTimeValue()){
			YQZZWar.YQZZGiftInfoResp.Builder resp = YQZZWar.YQZZGiftInfoResp.newBuilder();
			resp.setGiftId(-1);
			resp.setSendCount(0);
			resp.setRank(-1);
			resp.setIsEnd(false);
			resp.setCanSend(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GIFT_INFO_RESP, resp));
			return;
		}
		YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(season, GsConfig.getInstance().getServerId());
		if(seasonServer == null){
			YQZZWar.YQZZGiftInfoResp.Builder resp = YQZZWar.YQZZGiftInfoResp.newBuilder();
			resp.setGiftId(-1);
			resp.setSendCount(0);
			resp.setRank(-1);
			resp.setIsEnd(false);
			resp.setCanSend(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GIFT_INFO_RESP, resp));
			return;
		}
		int rank = seasonServer.getKickoutRank() > 0? seasonServer.getKickoutRank() : seasonServer.getGroupRank();
		YQZZSeasonServerRankAwardCfg cfg = getSendAwardCfg(rank);
		if(cfg == null){
			YQZZWar.YQZZGiftInfoResp.Builder resp = YQZZWar.YQZZGiftInfoResp.newBuilder();
			resp.setGiftId(-1);
			resp.setSendCount(0);
			resp.setRank(-1);
			resp.setIsEnd(false);
			resp.setCanSend(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GIFT_INFO_RESP, resp));
			return;
		}
		YQZZWar.YQZZGiftInfoResp.Builder resp = YQZZWar.YQZZGiftInfoResp.newBuilder();
		resp.setGiftId(cfg.getId());
		resp.setSendCount(YQZZMatchService.getInstance().getDataManger().getGiftRecordDataList().size());
		resp.setRank(rank);
		resp.setIsEnd(true);
		resp.setCanSend(seasonServer.getSenderId().equals(player.getId()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GIFT_INFO_RESP, resp));
		return;

	}

	private HawkTuple2<Integer, Integer> getSeasonMaxTurn(int season){
		int groupMaxTurn = -1;
		int kickoutMaxTurn = -1;
		ConfigIterator<YQZZTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		for(YQZZTimeCfg cfg : iterator){
			if(cfg.getSeason() != season){
				continue;
			}
			if(cfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_GROUP_VALUE && cfg.getTurn() > groupMaxTurn){
				groupMaxTurn = cfg.getTurn();
			}
			if(cfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_KICKOUT_VALUE && cfg.getTurn() > kickoutMaxTurn){
				kickoutMaxTurn = cfg.getTurn();
			}
		}
		HawkTuple2<Integer, Integer> tuple2 = new HawkTuple2<>(groupMaxTurn, kickoutMaxTurn);
		return tuple2;
	}

	private YQZZSeasonServerRankAwardCfg getSendAwardCfg(int rank){
		ConfigIterator<YQZZSeasonServerRankAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZSeasonServerRankAwardCfg.class);
		for (YQZZSeasonServerRankAwardCfg cfg : iterator){
			if(rank >= cfg.getMin() && rank <= cfg.getMax()){
				return cfg;
			}
		}
		return null;
	}

	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_GIFT_SEND_INFO_REQ_VALUE)
	private void onLeagueGiftSend(HawkProtocol protocol) {
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		if(season <= 0){
			return;
		}
		YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(season, GsConfig.getInstance().getServerId());
		if(seasonServer == null){
			return;
		}
		if(!seasonServer.getSenderId().equals(player.getId())){
			return;
		}
		int rank = seasonServer.getKickoutRank() > 0? seasonServer.getKickoutRank() : seasonServer.getGroupRank();
		YQZZSeasonServerRankAwardCfg cfg = getSendAwardCfg(rank);
		if(cfg == null){
			return;
		}
		YQZZWar.YQZZGiftSendReq req = protocol.parseProtocol(YQZZWar.YQZZGiftSendReq.getDefaultInstance());
		if(req.getSendInfoList().size() + YQZZMatchService.getInstance().getDataManger().getGiftRecordDataList().size() > cfg.getSendNum()){
			sendError(protocol.getType(), Status.YQZZError.YQZZ_GIFT_SERVER_LIMIT_VALUE);
			return;
		}
		for(YQZZWar.YQZZGiftSendInfo info : req.getSendInfoList()) {
			if (YQZZMatchService.getInstance().getDataManger().getGiftRecordDataMap().containsKey(info.getTargetPlayerId())) {
				sendError(protocol.getType(), Status.YQZZError.YQZZ_GIFT_PLAYER_LIMIT_VALUE);
				return;
			}
		}
		for(YQZZWar.YQZZGiftSendInfo info : req.getSendInfoList()){
			if(YQZZMatchService.getInstance().getDataManger().getGiftRecordDataMap().containsKey(info.getTargetPlayerId())){
				continue;
			}
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(info.getTargetPlayerId());
			if(targetPlayer == null){
				continue;
			}
			YQZZSeasonGiftRecordData record = new YQZZSeasonGiftRecordData();
			Player shot = GlobalData.getInstance().makesurePlayer(info.getTargetPlayerId());
			record.setSeason(season);
			record.setServerId(player.getMainServerId());
			record.setSendTime(HawkTime.getMillisecond());
			record.setGiftId(info.getGiftId());
			record.setSendPlayerName(player.getName());
			record.setSendPlayerTag(player.getGuildTag());
			record.setPlayerName(shot.getName());
			record.setPlayerId(info.getTargetPlayerId());
			record.setGuildTag(shot.getGuildTag());
			YQZZMatchService.getInstance().getDataManger().addSeasonGiftRecord(record);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(info.getTargetPlayerId())
					.setMailId(MailConst.MailId.YQZZ_LEAGUE_SERVER_LEADER_REWARD)
					.setRewards(ItemInfo.valueListOf(cfg.getSendAward()))
					.setAwardStatus(Const.MailRewardStatus.NOT_GET)
					.addContents(player.getName())
					.build());
			HawkLog.logPrintln("yqzz send gift player reward, playerId:{}, rank: {}, sendPlayer: {}, cfgId: {}",
					info.getTargetPlayerId(), rank, player.getId(), cfg.getId());
		}
		onLeagueGiftInfo(null);
		onLeagueGiftRecord(null);
	}

	@ProtocolHandler(code = HP.code2.YQZZ_LEAGUE_GIFT_RECORD_REQ_VALUE)
	private void onLeagueGiftRecord(HawkProtocol protocol) {
		YQZZWar.YQZZGiftRecordResp.Builder resp = YQZZWar.YQZZGiftRecordResp.newBuilder();
		for(YQZZSeasonGiftRecordData record : YQZZMatchService.getInstance().getDataManger().getGiftRecordDataList()){
			resp.addRecord(record.toPB());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_GIFT_RECORD_RESP, resp));
	}

	@ProtocolHandler(code = HP.code2.YQZZ_SEARCH_REQ_VALUE)
	private void onSearch(HawkProtocol protocol) {
		YQZZWar.YQZZSearchReq req = protocol.parseProtocol(YQZZWar.YQZZSearchReq.getDefaultInstance());
		if (!req.hasName()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID, 0);
			return;
		}
		// 禁言玩家推送禁言提示
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), GsConst.IDIPBanType.BAN_SEND_MSG);
			if (banInfo != null) {
				YQZZWar.YQZZSearchResp.Builder response = YQZZWar.YQZZSearchResp.newBuilder();
				response.setMsg(banInfo.getBanMsg());
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SEARCH_RESP, response));
				return;
			}
		}
		GameTssService.getInstance().wordUicChatFilter(player, req.getName(),
				com.hawk.game.protocol.Player.MsgCategory.YQZZ_SEARCH_MEMBER.getNumber(), GameMsgCategory.YQZZ_SEARCH_MEMBER,
				String.valueOf(req.getType()), null, protocol.getType());
	}

	public YQZZWar.YLWScoreInfo.Builder buildScoreInfo(long score, List<Integer> reward){
		YQZZWar.YLWScoreInfo.Builder builder = YQZZWar.YLWScoreInfo.newBuilder();
		builder.setScore(score);
		builder.addAllRewardedId(reward);
		return builder;
	}

	public YQZZWar.PBYQZZLeagueWarServerInfo.Builder buildSlefServerInfo(){
		String serverId = GsConfig.getInstance().getServerId();
		//服务器
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(String.valueOf(serverId));
		// 司令
		President president = PresidentFightService.getInstance().getPresidentCity().getPresident();
		YQZZWar.PBYQZZLeagueWarServerInfo.Builder builder = YQZZWar.PBYQZZLeagueWarServerInfo.newBuilder();
		builder.setServerId(serverId);
		builder.setServerName(serverInfo.getName());
		builder.setScore(0);
		builder.setRank(-1);
		builder.setWinPoint(0);
		builder.setLastRank(0);
		if(president != null &&
				HawkOSOperator.isEmptyString(president.getPlayerId())){
			builder.setLeaderName(president.getPlayerName());
		}
		return builder;
	}

	public YQZZWar.PBYQZZLeagueWarGuildInfo.Builder buildSelfGuildInfo(int rank, long score){
		YQZZWar.PBYQZZLeagueWarGuildInfo.Builder guildInfo = YQZZWar.PBYQZZLeagueWarGuildInfo.newBuilder();
		guildInfo.setServerId(GsConfig.getInstance().getServerId());
		guildInfo.setRank(rank);
		guildInfo.setScore(score);
		if(player.hasGuild()){
			guildInfo.setGuildName(player.getGuildName());
			guildInfo.setGuildLeader(player.getGuildLeaderName());
			guildInfo.setGuildFlag(player.getGuildFlag());
			guildInfo.setGuildTag(player.getGuildTag());
		}else {
			guildInfo.setGuildTag("");
			guildInfo.setGuildName("");
			guildInfo.setGuildLeader("");
		}
		return guildInfo;
	}
}
 