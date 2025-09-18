package com.hawk.game.module.lianmengyqzz.march.service.state;

import java.util.*;

import com.hawk.game.config.TeamStrengthWeightCfg;
import com.hawk.game.crossactivity.rank.MatchStrengthRank;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.roomstate.YQZZGameOver;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.util.LogUtil;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.google.common.collect.HashBiMap;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZExtraParam;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZNation;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZWarConstCfg;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZGameData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinGuild;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinServer;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZMatchRoomData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityJoinState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.nation.NationService;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.YQZZ.PBYQZZGameInfoSync;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;
import redis.clients.jedis.Tuple;

public class YQZZState300Battle  extends IYQZZServiceState {

	private long lastTickTime = 0;
	public YQZZState300Battle(YQZZMatchService parent) {
		super(parent);
	}

	@Override
	public void init() {
		this.getDataManager().getStateData().setState(YQZZActivityState.BATTLE);
		this.getDataManager().getStateData().saveRedis();
		//如果不参与战斗
		YQZZActivityJoinState joinState = this.getDataManager()
				.getStateData().getJoinGame();
		if(joinState == YQZZActivityJoinState.OUT){
			return;
		}
		//创建房间
		this.createBattleRoom();
		//本服随意进入人员
		this.calFreeJoinPlayers();
		//发送邮件
		this.sendPlayerJoinFreeMail();
		//记录本服战力
		this.logBattleStartPower();
	}
	
	
	@Override
	public void tick() {
		YQZZActivityStateData data = this.calcInfo();
		//如果不在当前状态，则往下个状态推进
		YQZZActivityStateData curData = this.getDataManager().getStateData();
		if(curData.getTermId() != data.getTermId()
				|| curData.getState() != data.getState()){
			this.getParent().updateState(YQZZActivityState.REWARD);
			return;
		}
		//如果不参与战斗
		YQZZActivityJoinState joinState = this.getDataManager()
				.getStateData().getJoinGame();
		if(joinState == YQZZActivityJoinState.OUT){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		if(curTime > this.lastTickTime + HawkTime.MINUTE_MILLI_SECONDS * 1){
			this.lastTickTime = curTime;
			//存数据
			this.saveBattleRoomData();
			//重新加载玩家战场数据
			this.getDataManager().loadToCacheYQZZBattleData();
		}
	}

	@Override
	public void gmOp() {
		//存数据
		this.saveBattleRoomData();
		//重新加载玩家战场数据
		this.getDataManager().loadToCacheYQZZBattleData();
		for(YQZZBattleRoom room :YQZZRoomManager.getInstance().findAllRoom()){
			room.setState(new YQZZGameOver(room));
			room.getState().onTick();
		}
	}

	@SuppressWarnings("unused")
	private void checkCreateBattleRoom(){
		YQZZMatchRoomData roomData = this.getDataManager().getRoomData();
		if(roomData == null){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		String roomServer = roomData.getRoomServerId();
		if(!serverId.equals(roomServer)){
			return;
		}
		String roomId = roomData.getRoomId();
		
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.YQZZAOGUAN_ROOM, roomId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.YQZZAOGUAN_ROOM)
				.queryObject(roomXid);
		if (roomObj != null) {
			return;
		} 
		this.createBattleRoom();
	}
	
	
	private void saveBattleRoomData(){
		YQZZMatchRoomData roomData = this.getDataManager().getRoomData();
		if(roomData == null){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		String roomServer = roomData.getRoomServerId();
		if(!serverId.equals(roomServer)){
			return;
		}
		String roomId = roomData.getRoomId();
		PBYQZZGameInfoSync gameInfo = YQZZRoomManager.getInstance().getLastSyncpb(roomId);
		YQZZBattleData.saveSourceData(gameInfo, null, roomId);
	}
	
	/**
	 * 创建房间
	 */
	public void createBattleRoom(){
		String serverId = GsConfig.getInstance().getServerId();
		YQZZMatchRoomData room = this.getDataManager().getRoomData();
		if(room == null){
			return;
		}
		if(serverId.equals(room.getRoomServerId())){
			YQZZTimeCfg timeCfg = this.getTimeCfg();
			List<String> servers = room.getServers();
			YQZZExtraParam param = new YQZZExtraParam();
			HashBiMap<String, YQZZNation> serverCamp = HashBiMap.create(6);
			param.setBattleId(room.getRoomId());
			param.setServerCamp(serverCamp);
			Map<String,YQZZJoinServer> joinServerMap = YQZZJoinServer.loadAll(timeCfg.getTermId(), servers);
			for(int i=0;i<servers.size();i++ ){
				String sId = servers.get(i);
				YQZZNation nation = new YQZZNation();
				YQZZ_CAMP camp = this.yqzzCamp(i+1);
				nation.setServerId(sId);
				nation.setCamp(camp);
				nation.setNationLevel(NationService.getInstance().getBuildLevel(sId, NationbuildingType.NATION_SPACE_FLIGHT_VALUE));
				YQZZJoinServer server = joinServerMap.get(sId);
				if(server != null){
					nation.setPresidentId(server.getLeaderId() == null ? "" : server.getLeaderId());
					nation.setPresidentName(server.getLeaderName() == null ? "" : server.getLeaderName());
				}
				serverCamp.put(sId, nation);
			}
			boolean rlt = YQZZRoomManager.getInstance().creatNewBattle(timeCfg.getBattleTimeValue(),
					timeCfg.getRewardTimeValue(), param);
			if(rlt){
				YQZZGameData gameData = new YQZZGameData();
				gameData.setTermId(room.getTermId());
				gameData.setRoomId(room.getRoomId());
				gameData.setRoomServerId(room.getRoomServerId());
				gameData.addServer(servers);
				gameData.setLastActiveTime(HawkTime.getMillisecond());
				gameData.setFinishTime(0);
				gameData.saveRedis();
			}
		}
	}
	
	
	private void calFreeJoinPlayers(){
		String serverId = GsConfig.getInstance().getServerId();
		YQZZJoinServer serverData = this.getDataManager().getRoomServerById(serverId);
		if(serverData == null){
			return;
		}
		YQZZWarConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		List<RankInfo> list = RankService.getInstance().getRankCache(RankType.PLAYER_FIGHT_RANK);
		int size = cfg.getPlayerJoinFreeCount();
		Map<String,Integer> frees = new HashMap<>();
		for(RankInfo info : list){
			if(size <=0){
				break;
			}
			String id = info.getId();
			int rank = info.getRank();
			frees.put(id,rank);
			size --;
		}
		serverData.setFreePlayers(frees);
		serverData.saveRedis();
	}

	public void logBattleStartPower(){
		try {
			int termId = this.getDataManager().getStateData().getTermId();
			long power = getMatchPower(termId);
			String serverId = GsConfig.getInstance().getServerId();
			LogUtil.logYQZZBattleStartPower(termId, serverId, power);
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	public long getMatchPower(int termId){
		try {
			YQZZWarConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
			//获取列表
			int count = cfg.getMoonMatchNumLimit() -1;
			count = Math.max(count, 0);
			Set<Tuple> rankList = MatchStrengthRank.getInstance().getStrengthList(count);
			//列表为空则不走写入逻辑.
			if (rankList == null || rankList.size() <= 0) {
				return 0;
			}
			double memberPower = 0;
			int rank = 0;
			for(Tuple info : rankList){
				rank ++;
				String playerId = info.getElement();
				long power = (long) info.getScore();
				double powerWeight = this.getPowerWeight(rank);
				double addPower =  (power * powerWeight);
				memberPower += addPower;
				//日志
				//LogUtil.logCrossActivityPlayerStrength(termId, playerId, rank, power, powerWeight, addPower);
				HawkLog.logPrintln("YQZZActivityService battle power,termId:{},playerId:{},rank:{},power:{},powerWeight:{},memberPower:{},",
						termId,playerId,rank,power,powerWeight,addPower);
			}

			double teamParam = this.getTeamMatchParam(termId);
			long matchPower = (long) (teamParam * memberPower);
			HawkLog.logPrintln("YQZZActivityService battle power,termId:{},matchPower:{}",termId, matchPower);
			return matchPower;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 队伍磨合参数
	 * @param teamId
	 * @return
	 */
	private double getTeamMatchParam(int termId){
		YQZZWarConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		int count = cfg.getMoonMatchTimesLimit() -1;
		count = Math.max(count, 0);
		List<Integer> terms = new ArrayList<>();
		for(int i=1;i<=count;i++){
			int termTemp = termId -i;
			if(termTemp >= 1){
				terms.add(termTemp);
			}
		}

		String serverId = GsConfig.getInstance().getServerId();
		Map<Integer, YQZZRecordData> logList = YQZZRecordData.loadAll(serverId, terms);
		double param = 0;
		for(YQZZRecordData record : logList.values()){
			//int historyTerm = record.getTermId();
			int rank = record.getRank();
			double rankParam = cfg.getMoonMatchBattleResultValue(rank);
			param += rankParam;
			//日志
			//LogUtil.logCrossActivityTeamParam(termId, historyTerm, rank, rankParam);
			HawkLog.logPrintln("YQZZActivityService battle power, getTeamMatchParam,termId:{},rank:{},param:{}",
					record.getTermId(),rank,rankParam);
		}
		param = Math.min(param, cfg.getMoonMatchCofMaxValue());
		param = Math.max(param, cfg.getMoonMatchCofMinValue());
		HawkLog.logPrintln("YQZZActivityService battle power, getTeamMatchParam, result,termId:{},param:{}",termId, param);
		return param + 1;
	}

	/**
	 * 战力排名权重
	 * @param rank
	 * @return
	 */
	private double getPowerWeight(int rank){
		List<TeamStrengthWeightCfg> cfgList = AssembleDataManager.getInstance().getTeamStrengthWeightCfgList(40);
		for(TeamStrengthWeightCfg cfg : cfgList){
			if(cfg.getRankUpper()<= rank && rank <= cfg.getRankLower()){
				return cfg.getWeightValue();
			}
		}
		return 0;
	}

	private void sendPlayerJoinFreeMail(){
		String serverId = GsConfig.getInstance().getServerId();
		YQZZJoinServer serverData = this.getDataManager().getRoomServerById(serverId);
		if(serverData == null){
			return;
		}
		Map<String, YQZZJoinGuild> guilds = this.getDataManager().getRoomGuildsByServer(serverId);
		YQZZWarConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		int freeCount = cfg.getPlayerJoinFreeCount();
		int extraCount = cfg.getPlayerJoinExtraCount();
		Map<String,Integer>  freeMap = serverData.getFreePlayers();
		for(YQZZJoinGuild guild : guilds.values()){
			String guildId = guild.getGuildId();
			GuildInfoObject obj = GuildService.getInstance()
					.getGuildInfoObject(guildId);
			if(obj == null){
				continue;
			}
			Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
			for(String memberId : members){
				if(freeMap.containsKey(memberId)){
					int rank = freeMap.get(memberId);
					SystemMailService.getInstance().sendMail(MailParames.newBuilder()
							.setPlayerId(memberId)
							.setMailId(MailId.YQZZ_ACTIVITY_PLAYER_JOIN_FREE)
							.addContents(rank)
							.build());
				}else{
					SystemMailService.getInstance().sendMail(MailParames.newBuilder()
							.setPlayerId(memberId)
							.setMailId(MailId.YQZZ_ACTIVITY_PLAYER_JOIN_EXTRA)
							.addContents(freeCount,extraCount)
							.build());
				}
			}
		}
	}
	
	private YQZZ_CAMP yqzzCamp(int rank){
		switch (rank) {
		case 1:return YQZZ_CAMP.A;
		case 2:return YQZZ_CAMP.D;
		case 3:return YQZZ_CAMP.F;
		case 4:return YQZZ_CAMP.C;
		case 5:return YQZZ_CAMP.B;
		case 6:return YQZZ_CAMP.E;
		default:
			return null;
		}
	}
}
