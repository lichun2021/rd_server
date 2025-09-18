package com.hawk.game.module.lianmengfgyl.march.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsConfig;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLBilingInformationMsg;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLQuitReason;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLLevelCfg;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLRankRewardCfg;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLTimeCfg;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLActivityStateData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLDataManager;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLGuildJoinData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLHonorRank;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLRoomData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLTermRank;
import com.hawk.game.module.lianmengfgyl.march.entity.FGYLGuildEntity;
import com.hawk.game.module.lianmengfgyl.march.entity.FGYLPlayerEntity;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLConst.FGYLActivityState;
import com.hawk.game.module.lianmengfgyl.march.service.state.IFGYLServiceState;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.FGYL.PBFGYLGameInfoSync;
import com.hawk.game.protocol.FGYL.PBFGYLGuildInfo;
import com.hawk.game.protocol.FGYL.PBFGYLPlayerInfo;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarGuildHonorRankResp;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarGuildRankMember;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarGuildTermRankResp;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarStateInfo;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarStateInfoResp;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.FGYLError;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;

public class FGYLMatchService extends HawkAppObj {
	private static FGYLMatchService instance;
	private FGYLDataManager dataManger;
	private IFGYLServiceState state;
	private long lastTime;
	private long rankTickTime;
	
	public static FGYLMatchService getInstance() {
		return instance;
	}

	public FGYLMatchService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	public boolean init(){
		//初始化所需数据
		this.dataManger = new FGYLDataManager();
		this.dataManger.init();
		//初始化服务状态
		this.state = this.initFGYLServiceState();
		return true;
	}
	
	public FGYLDataManager getDataManger() {
		return dataManger;
	}
	
	public IFGYLServiceState initFGYLServiceState(){
		FGYLActivityState state = this.dataManger.getStateData().getState();
		IFGYLServiceState serviceState = IFGYLServiceState.getFGYLServiceState(state);
		if(serviceState == null){
			serviceState = IFGYLServiceState
					.getFGYLServiceState(FGYLActivityState.HIDDEN);
			serviceState.init();
		}
		return serviceState;
	}

	
	
	public boolean onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curTime - this.lastTime < 2000){
			return true;
		}
		this.lastTime = curTime;
		state.tick();
		this.rankTick();
		return true;
	}
	
	
	public void rankTick(){
		long curTime = HawkTime.getMillisecond();
		if(this.rankTickTime <= 0){
			this.rankTickTime  = curTime;
			return;
		}
		if(curTime - this.rankTickTime < 60 * 5 * 1000){
			return;
		}
		this.rankTickTime  = curTime;
		int termId = this.dataManger.getStateData().getTermId();
		if(termId <= 0){
			return;
		}
		this.getDataManger().getTermRank().refreshRank(termId);
		this.getDataManger().getHonorRank().refreshRank();
	}

	
	
	
	
	public boolean activityOpening(){
		FGYLActivityStateData data = this.dataManger.getStateData();
		if(data.getState() != FGYLActivityState.OPEN){
			return false;
		}
		return true;
	}
	
	
	
	

	public void updateState(FGYLActivityState state) {
		IFGYLServiceState serviceState = IFGYLServiceState.getFGYLServiceState(state);
		try {
			serviceState.init();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		this.state = serviceState;
		this.boardAllPlayers();
//		//记录一下
		String serverId = GsConfig.getInstance().getServerId();
		String key = "FGYLZZ_STATE_"+serverId;
		int termId = this.dataManger.getStateData().getTermId();
		DungeonRedisLog.log(key, "termId:{},state update:{}", termId,state.getValue());
		HawkLog.logPrintln("FGYLMatchService updateState,termId:{},state:{}", termId,state.getValue());
	}
	
	

	/**
	 * 报名
	 * @param player
	 * @param level
	 * @param timeIndex
	 */
	public void signupWar(int hp,Player player,int level,int timeIndex){
		if(player.isCsPlayer()){
			player.sendError(hp,Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN_VALUE, 0);
			return;
		}
		String guildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			player.sendError(hp,Status.Error.GUILD_NO_JOIN, 0);
			return;
		}
		boolean guildExist = GuildService.getInstance().isGuildExist(guildId);
		if(!guildExist){
			player.sendError(hp,Status.Error.GUILD_NO_JOIN, 0);
			return;
		}
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		if (!guildAuthority) {
			player.sendError(hp,Status.FGYLError.FGYL_SIGN_AUTH_LIMIT, 0);
			return;
		}
		
		if(this.getDataManger().getStateData().getState() != FGYLActivityState.OPEN){
			player.sendError(hp,Status.FGYLError.FGYL_WAR_NOT_IN_OPEM_TIME, 0);
			return;
		}
		int termdId = this.getDataManger().getStateData().getTermId();
		FGYLTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(FGYLTimeCfg.class, termdId);
		if(Objects.isNull(timeCfg)){
			return;
		}
		FGYLLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(FGYLLevelCfg.class, level);
		if(Objects.isNull(levelCfg)){
			return;
		}
		int guildPassLevel = this.getGuildPassLevel(guildId);
		if(levelCfg.getFree() <=0 && this.canSignUpLevel(guildId) < levelCfg.getLevel()){
			player.sendError(hp,Status.FGYLError.FGYL_SIGN_LEVEL_LIMIT, 0);
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		int termId = this.getDataManger().getStateData().getTermId();
		long curtime = HawkTime.getMillisecond();
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		
		FGYLGuildJoinData joinData = this.dataManger.getFGYLGuildJoinData(guildId);
		if(Objects.nonNull(joinData)){
			//报名次数不足
			if(joinData.getSignCount() >= constCfg.getChallengeNum()){
				player.sendError(hp,FGYLError.FGYL_SIGN_COUNT_LIMIT, 0);
				return;
			}
			//已经成功通关
			if(joinData.getWinPassLevel() > 0){
				player.sendError(hp,Status.FGYLError.FGYL_WIN_PASS, 0);
				return;
			}
			//今天已经报名
			if(HawkTime.isSameDay(curtime, joinData.getSignTime())){
				player.sendError(hp,Status.FGYLError.FGYL_SIGN_TIME_LIMIT, 0);
				return;
			}
		}
		//是否可以报名此时间
		HawkTuple2<Integer, Integer> timePoint = constCfg.getWarTimePoint(timeIndex-1);
		if(Objects.isNull(timePoint)){
			player.sendError(hp,Status.FGYLError.FGYL_SIGN_LEVEL_LIMIT, 0);
			return;
		}
		//必须提前报名
		long zeroTime = HawkTime.getAM0Date().getTime();
		long fightTime = zeroTime +  timePoint.first * HawkTime.HOUR_MILLI_SECONDS  + timePoint.second * HawkTime.MINUTE_MILLI_SECONDS;
		if(fightTime - curtime < constCfg.getApplyTime() * 1000 ){
			player.sendError(hp,Status.FGYLError.FGYL_SIGN_TIME_LIMIT, 0);
			return;
		}
		if(fightTime  >=  timeCfg.getEndTimeValue()){
			player.sendError(hp,Status.FGYLError.FGYL_SIGN_TIME_LIMIT, 0);
			return;
		}
		if(Objects.isNull(joinData)){
			joinData = new FGYLGuildJoinData();
			joinData.setGuildId(guildId);
			joinData.setTermId(termId);
			joinData.setServerId(serverId);
			this.dataManger.addFGYLGuildJoinData(joinData);
		}
		int signCount = joinData.getSignCount() + 1;
		joinData.setSignTime(curtime);
		joinData.setSignBattleIndex(timeIndex);
		joinData.setSignCount(signCount);
		joinData.setCreateFightRlt(0);
		joinData.setFightLevel(level);
		joinData.calAndSetFightCreateTime();
		joinData.recordFightRoom();
		joinData.clearGameRoom();
		joinData.saveRedis();
		
		// 发全联盟邮件
		GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
				.setMailId(MailId.FGYL_WAR_SIGN)
				.addContents(fightTime,level));
		//更新状态
		this.boardGuildPlayers(guildId);
		LogUtil.logFGYLSignUp(player, termId, guildId, level, timeIndex, guildPassLevel,signCount);
		
		ScheduleInfo schedule = ScheduleInfo.createNewSchedule(ScheduleType.SCHEDULE_TYPE_4_VALUE, player.getGuildId(), fightTime, 0, 0);
		ScheduleService.getInstance().addSystemSchedule(schedule);
	}
	
	public int canSignUpLevel(String guildId){
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		int level = this.getGuildPassLevel(guildId);
		level += constCfg.getLevelUp();
		return level;
	}
	
	
	public int getGuildPassLevel(String guildId){
		int level = 0;
		FGYLGuildEntity entity = this.getDataManger().getFGYLGuildEntity(guildId);
		if(Objects.nonNull(entity)){
			level = entity.getPassLevel();
		}
		return level;
		
	}
	
	public boolean joinRoom(Player player){
		FGYLActivityStateData stateData = this.getDataManger().getStateData();
		int termId = stateData.getTermId();
		FGYLActivityState state = stateData.getState();
		if(state != FGYLActivityState.OPEN){
			return false;
		}
		FGYLGuildJoinData joinData = FGYLMatchService.getInstance().getDataManger().getFGYLGuildJoinData(player.getGuildId());
		if (joinData == null) {
			HawkLog.logPrintln("FGYLMatchService sourceCheckEnterInstance,FGYLGuildJoinData null ,playerId:{}", player.getId());
			return false;
		}
		FGYLRoomData roomData = joinData.getGameRoom();
		if (roomData == null) {
			HawkLog.logPrintln("FGYLMatchService sourceCheckEnterInstance,FGYLRoomData NULL ,playerId:{}", player.getId());
			return false;
		}
		String roomId = roomData.getRoomId();
		if(roomData.getEndTime() > 0){
			HawkLog.logPrintln("FGYLMatchService sourceCheckEnterInstance,FGYLRoomData END ,playerId:{},", player.getId());
			return false;
		}
		if (!FGYLRoomManager.getInstance().hasGame(roomId)) {
			HawkLog.logPrintln("FGYLMatchService enter room error, game not esixt, playerId: {}, termId: {}, roomId: {}, guildId: {}", player.getId(),
					termId, roomId, joinData.getGuildId());
			return false;
		}
		if (!FGYLRoomManager.getInstance().joinGame(roomId, player)) {
			HawkLog.logPrintln("FGYLMatchService enter room error, joinGame failed, playerId: {}, termId: {}, roomId: {}, guildId: {}", player.getId(),
					termId, roomId, joinData.getGuildId());
			return false;
		}
		CommanderEntity centity = player.getData().getCommanderEntity();
		FGYLPlayerEntity fentity = centity.getFgylPlayerEntity();
		if(roomId.equals(fentity.getJoinRoomId())){
			HawkLog.logPrintln("FGYLMatchService enter room err, joinGame failed dup, playerId: {}, termId: {}, roomId: {}, guildId: {}", player.getId(),
					termId, roomId, joinData.getGuildId());
			return false;
		}
		fentity.setJoinRoomId(roomId);
		centity.notifyUpdate();
		DungeonRedisLog.log(player.getId(), "join fgyl {}", roomId);
		LogUtil.logFGYLJoinRoom(player, termId, joinData.getGuildId(), roomId);
		return true;
	}
	
	
	/**
	 * 退出战斗
	 * @param player
	 * @param roomId
	 * @param reson
	 * @return
	 */
	public boolean quitRoom(Player player, FGYLQuitReason reson){
		CommanderEntity centity = player.getData().getCommanderEntity();
		FGYLPlayerEntity fgylEntity = centity.getFgylPlayerEntity();
		String roomId = fgylEntity.getJoinRoomId();
		int termId = this.dataManger.getStateData().getTermId();
		LogUtil.logFGYLExitRoom(player, termId, player.getGuildId(), roomId, reson.intValue());
		HawkLog.logPrintln("FGYLMatchService quitRoom , termId:{},playerId:{}, guildId:{},roomId:{}",
				termId,player.getId(),player.getGuildId(),roomId);
		return true;
	}
	
	/**
	 * 结束
	 * @param msg
	 * @return
	 */
	@MessageHandler
	public void onFightOver(FGYLBilingInformationMsg msg){
		String serverId = GsConfig.getInstance().getServerId();
		int termId = this.getDataManger().getStateData().getTermId();
		String roomId = msg.getRoomId();
		PBFGYLGameInfoSync data = msg.getLastSyncpb();
		int wimCamp = data.getWinCamp();
		int level = data.getLevel();
		PBFGYLGuildInfo fightGuild = data.getGuildInfo(0);
		String guildId = fightGuild.getGuildId();
		int guildCamp = fightGuild.getCamp();
		boolean win = (wimCamp == guildCamp);
		long curTime = HawkTime.getMillisecond();
		FGYLLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(FGYLLevelCfg.class, level);
		if(Objects.isNull(levelCfg)){
			HawkLog.logPrintln("FGYLMatchService onFightOver FGYLLevelCfg null, termId:{}, guildId:{},roomId:{}, cfgId: {}",
					termId,guildId, roomId, level);
			return;
		}
		boolean exist = GuildService.getInstance().isGuildExist(guildId);
		if(!exist){
			HawkLog.logPrintln("FGYLMatchService onFightOver guild null, termId:{}, guildId:{},roomId:{}, cfgId: {}",
					termId,guildId, roomId, level);
			return;
		}
		
		FGYLGuildJoinData joinData = this.getDataManger().getFGYLGuildJoinData(guildId);
		FGYLRoomData roomData = joinData.getGameRoom();
		if(Objects.isNull(roomData)){
			HawkLog.logPrintln("FGYLMatchService onFightOver FGYLGuildJoinData roomData null, termId:{}, guildId:{},roomId:{}, cfgId: {}",
					termId,guildId, roomId, level);
			return;
		}
		if(!roomId.equals(roomData.getRoomId())){
			HawkLog.logPrintln("FGYLMatchService onFightOver FGYLGuildJoinData roomData roomId err, termId:{}, guildId:{},roomId:{},joinDataRoom:{}, cfgId:{}",
					termId,guildId, roomId, roomData.getRoomId(), level);
			return;
		}
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		long gendTime = msg.getTime();
		long gstartTime = roomData.getCreateTime();
		int useTime = (int) ((gendTime - gstartTime)/1000);
		useTime = Math.min(useTime, constCfg.getBattleTime());
		
		joinData.fightOver(win,level,useTime);
		roomData.setStartTime(gstartTime);
		roomData.setEndTime(gendTime);
		roomData.setRlt(win?1:0);
		joinData.saveRedis();
		if(win){
			FGYLGuildEntity guildData = this.getDataManger().getAndCreateFGYLGuildEntity(guildId);
			//添加排行榜
			this.getDataManger().getTermRank().addRank(termId, guildId, level, useTime,curTime);
			//添加最高通关记录
			boolean max = guildData.addRecordMax(level, useTime,curTime);
			if(max){
				this.getDataManger().getHonorRank().addFGYLHonorRanks(serverId, guildId, level, useTime, curTime);
			}
			//发奖
			this.sendFigthOverReward(termId, guildId, roomId, useTime, levelCfg, data.getPlayerInfoList());
		}else{
			//发失败邮件
			this.sendFigthOverFailMail(termId, guildId, roomId, useTime, levelCfg, data.getPlayerInfoList());
		}
		//广播一下状态
		this.boardGuildPlayers(guildId);
		LogUtil.logFGYLOverRoom(termId, guildId, roomId,level, win?1:0,useTime,data.getPlayerInfoCount(),joinData.getSignCount());
		HawkLog.logPrintln("FGYLMatchService onFightOver sucess, termId:{}, guildId:{},roomId:{},cfgId:{},useTime",
				termId,guildId, roomId, level,useTime);
	}
	
	
	/**
	 * 发副本结束奖励
	 * @param termId
	 * @param guildId
	 * @param roomId
	 * @param timeUse
	 * @param levleCfg
	 * @param list
	 */
	public void sendFigthOverReward(int termId,String guildId,String roomId,int timeUse,FGYLLevelCfg levleCfg,List<PBFGYLPlayerInfo> list){
		for(PBFGYLPlayerInfo player : list){
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getPlayerId())
					.setMailId(MailId.FGYL_WAR_FIGHT_WIN)
					.addContents(levleCfg.getLevel(),timeUse)
					.setRewards(ItemInfo.valueListOf(levleCfg.getReward()))
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
			HawkLog.logPrintln("FGYLMatchService sendFigthOverReward , termId:{}, playerId: {}, guildId:{},roomId:{}, cfgId: {}",
					termId,player.getPlayerId(),guildId, roomId, levleCfg.getLevel());
			Player sendPlayer = GlobalData.getInstance().makesurePlayer(player.getPlayerId());
			if(Objects.nonNull(sendPlayer)){
				this.recordPlayerFightRewrad(sendPlayer, termId);
				LogUtil.logFGYLFightReward(sendPlayer, termId, guildId, roomId, levleCfg.getLevel());
			}
		}
	}
	
	public void recordPlayerFightRewrad(Player sendPlayer,int rewardTermId){
		CommanderEntity centity = sendPlayer.getData().getCommanderEntity();
		FGYLPlayerEntity fentity = centity.getFgylPlayerEntity();
		fentity.setRewardTerm(rewardTermId);
		centity.notifyUpdate();
	}
	
	
	
	public void sendFigthOverFailMail(int termId,String guildId,String roomId,int timeUse,FGYLLevelCfg levleCfg,List<PBFGYLPlayerInfo> list){
		for(PBFGYLPlayerInfo player : list){
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getPlayerId())
					.setMailId(MailId.FGYL_WAR_FIGHT_FAIL)
					.addContents(levleCfg.getLevel())
					.build());
			HawkLog.logPrintln("FGYLMatchService sendFigthOverFailMail , termId:{}, playerId: {}, guildId:{},roomId:{}, cfgId: {}",
					termId,player.getPlayerId(),guildId, roomId, levleCfg.getLevel());
			Player sendPlayer = GlobalData.getInstance().makesurePlayer(player.getPlayerId());
			if(Objects.nonNull(sendPlayer)){
				LogUtil.logFGYLFightReward(sendPlayer, termId, guildId, roomId, levleCfg.getLevel());
			}
		}
	}
	
	
	
	public void sendTermRankReward() {
		int termId = this.dataManger.getStateData().getTermId();
		if(termId <= 0){
			return;
		}
		FGYLTermRank termRank = this.dataManger.getTermRank();
		termRank.refreshRank(termId);
		List<PBFGYLWarGuildRankMember> list = termRank.getRankInfos();
		for (PBFGYLWarGuildRankMember member : list) {
			try {
				String serverId = member.getServerId();
				if (!GlobalData.getInstance().isLocalServer(serverId)) {
					continue;
				}
				int rank = member.getGuildRank();
				FGYLRankRewardCfg cfg = getRankRewardCfg(rank);
				if (cfg == null) {
					continue;
				}
				String guildId = member.getId();
				boolean exist = GuildService.getInstance().isGuildExist(guildId);
				if(!exist){
					continue;
				}
				HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
					@Override
					public Object run() {
						AwardItems award = AwardItems.valueOf();
						award.addItemInfos(ItemInfo.valueListOf(cfg.getReward()));
						MailParames.Builder paramesBuilder = MailParames.newBuilder()
								.setMailId(MailId.FGYL_WAR_RANK_REWARD)
								.addContents(member.getTimeUse(),member.getPassLevel(),rank)
								.setRewards(award.getAwardItems())
								.setAwardStatus(MailRewardStatus.NOT_GET);
						GuildMailService.getInstance().sendGuildMail(guildId, paramesBuilder);
						HawkLog.logPrintln("FGYLMatchService send rank reward,serverId:{}, guildId: {},rank: {}, score: {}, level:{},timeUse:{}cfgId: {}",
								serverId,guildId,rank, member.getPassLevel(),member.getTimeUse(), cfg.getId());
						LogUtil.logFGYLRankReward(termId, guildId, rank, cfg.getId(), member.getPassLevel(), member.getTimeUse());
						return null;
					}
				});
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	
	public boolean guildInFighting(String guildId){
		if(HawkOSOperator.isEmptyString(guildId)){
			return false;
		}
		FGYLGuildJoinData joinData = this.getDataManger().getFGYLGuildJoinData(guildId);
		if(Objects.isNull(joinData)){
			return false;
		}
		FGYLRoomData roomData = joinData.getGameRoom();
		if(Objects.isNull(roomData)){
			return false;
		}
		if(roomData.getEndTime() > 0){
			return false;
		}
		return true;
	}
	
	private FGYLRankRewardCfg getRankRewardCfg(int rank) {
		List<FGYLRankRewardCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(FGYLRankRewardCfg.class).toList();
		FGYLRankRewardCfg cfg = null;
		for (FGYLRankRewardCfg rankCfg : cfgList) {
			int rankUpper = rankCfg.getRankMin();
			int rankLower = rankCfg.getRankMax();
			if (rank >= rankUpper && rank <= rankLower) {
				cfg = rankCfg;
			}
		}
		return cfg;
	}
	
	
	public PBFGYLWarStateInfo.Builder genWarStateBuilder(Player player){
		PBFGYLWarStateInfo.Builder builder = PBFGYLWarStateInfo.newBuilder();
		this.state.addBuilder(builder, player);
		return builder;
	}
	
	
	public void boardAllPlayers(){
		Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : players) {
			this.syncWarInfo(player);
		}
	}
	
	public void boardGuildPlayers(String guildId){
		List<String> onlines = GuildService.getInstance().getOnlineMembers(guildId);
		for (String playerId : onlines) {
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player != null && player.isActiveOnline()) {
				this.syncWarInfo(player);
			}
		}
	}
	
	public void syncWarInfo(Player player){
		PBFGYLWarStateInfoResp.Builder builder = PBFGYLWarStateInfoResp.newBuilder();
		builder.setWarInfo(this.genWarStateBuilder(player));
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_WAR_STATE_INFO_RESP_VALUE, builder));
	}

	
	

	public void syncWarTermRank(Player player){
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		int showSize = constCfg.getTermRankSize();
		FGYLTermRank rank = this.dataManger.getTermRank();
		PBFGYLWarGuildTermRankResp.Builder builder = PBFGYLWarGuildTermRankResp.newBuilder();
		List<PBFGYLWarGuildRankMember> ranks = rank.getRankInfos();
		Map<String,PBFGYLWarGuildRankMember> rankMap = rank.getRankMapInfos();
		for(PBFGYLWarGuildRankMember member : ranks){
			if(showSize <= 0){
				break;
			}
			builder.addMembers(member);
			showSize --;
		}
		String guildId = player.getGuildId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
		FGYLGuildJoinData joinData = this.getDataManger().getFGYLGuildJoinData(guildId);
		if(Objects.nonNull(guild) && !guild.isCrossGuild()){
			PBFGYLWarGuildRankMember self = rankMap.get(guildId);
			if(Objects.nonNull(self)){
				builder.setSelf(self);
			}else{
				int passLevel = 0;
				int timeUse = 0;
				if(Objects.nonNull(joinData)){
					passLevel = joinData.getWinPassLevel();
					timeUse = joinData.getWinPassTime();
				}
				PBFGYLWarGuildRankMember.Builder mbuilder = PBFGYLWarGuildRankMember.newBuilder();
				mbuilder.setTermId(0);
				mbuilder.setServerId(guild.getServerId());
				mbuilder.setId(guild.getId());
				mbuilder.setName(guild.getName());
				mbuilder.setTag(guild.getTag());
				mbuilder.setGuildFlag(guild.getFlagId());
				mbuilder.setGuildRank(-1);
				mbuilder.setPassLevel(passLevel);
				mbuilder.setTimeUse(timeUse);
				builder.setSelf(mbuilder);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_WAR_TERM_RANK_RESP_VALUE, builder));
	}
	
	
	
	
	public void syncWarHonorRank(Player player){
		FGYLHonorRank rank = this.dataManger.getHonorRank();
		PBFGYLWarGuildHonorRankResp.Builder builder = PBFGYLWarGuildHonorRankResp.newBuilder();
		List<PBFGYLWarGuildRankMember> ranks = rank.getRankInfos();
		boolean inRank = false;
		for(PBFGYLWarGuildRankMember member : ranks){
			builder.addMembers(member);
			if(member.getId().equals(player.getGuildId())){
				builder.setSelf(member);
				inRank = true;
			}
		}
		if(!inRank){
			String guildId = player.getGuildId();
			GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
			FGYLGuildEntity fgentity = this.getDataManger().getFGYLGuildEntity(guildId);
			if(Objects.nonNull(guild) && !guild.isCrossGuild()){
				int passLevel = 0;
				int timeUse = 0;
				if(Objects.nonNull(fgentity)){
					passLevel = fgentity.getPassLevel();
					timeUse = fgentity.getUseTime();
				}
				PBFGYLWarGuildRankMember.Builder mbuilder = PBFGYLWarGuildRankMember.newBuilder();
				mbuilder.setTermId(0);
				mbuilder.setServerId(guild.getServerId());
				mbuilder.setId(guild.getId());
				mbuilder.setName(guild.getName());
				mbuilder.setTag(guild.getTag());
				mbuilder.setGuildFlag(guild.getFlagId());
				mbuilder.setGuildRank(-1);
				mbuilder.setPassLevel(passLevel);
				mbuilder.setTimeUse(timeUse);
				builder.setSelf(mbuilder);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_WAR_HONOR_RANK_RESP_VALUE, builder));
		
	}
	



}
