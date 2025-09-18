package com.hawk.game.module.lianmengyqzz.march.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hawk.common.ServerInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZPylonCfg;
import com.hawk.game.module.lianmengyqzz.march.data.global.*;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZSeasonStateData;
import com.hawk.game.module.lianmengyqzz.march.module.PlayerYQZZModule;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.space.NationSpaceFlight;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.model.President;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.National;
import com.hawk.game.protocol.YQZZWar;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZBuildCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZBilingInformationMsg;
import com.hawk.game.module.lianmengyqzz.march.achieve.YQZZAchievManager;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZBattleStageTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZWarConstCfg;
import com.hawk.game.module.lianmengyqzz.march.data.YQZZDataManager;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZCountryGameData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZGameDataBuildScore;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZGameDataMonsterKill;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZGameDataResCollect;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZGameDataYuriKill;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZGuildGameData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZPlayerGameData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityJoinState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.module.lianmengyqzz.march.service.state.IYQZZServiceState;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.YQZZ.PBYQZZGameInfoSync;
import com.hawk.game.protocol.YQZZ.PBYQZZGuildInfo;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarAchieve;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarCountry;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarCountryCommander;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarCountryRankMember;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarCountryScore;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarCountryScoreGuild;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarGuildRankMember;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarGuildScore;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarGuildScoreBuild;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarHistroyDetailResp;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarHistroyResp;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarMatchInfoResp;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarMoonBattleStageTime;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarMoonTime;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarPageInfoResp;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarPlayerRankMember;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarPlayerScore;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarPlayerScoreBuild;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarPlayerScoreCollect;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarPlayerScoreMonster;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarPlayerScoreYuri;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarRank;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarRankResp;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarRecord;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarScoreDetailResp;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarState;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarStateInfo;
import com.hawk.game.util.LogUtil;

public class YQZZMatchService extends HawkAppObj {
	private static YQZZMatchService instance;
	private YQZZDataManager dataManger;
	private IYQZZServiceState state;
	private long lastTime;
	public static YQZZMatchService getInstance() {
		return instance;
	}

	public YQZZMatchService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	public boolean init(){
		//初始化所需数据
		this.dataManger = new YQZZDataManager();
		this.dataManger.init();
		//初始化服务状态
		this.state = this.initYQZZServiceState();
		//初始化成就任务
		YQZZAchievManager.getInstance();
		return true;
	}
	
	
	public boolean onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curTime - this.lastTime < 2000){
			return true;
		}
		this.lastTime = curTime;
		state.tick();
		onSeasonTick();
		return true;
	}

	public boolean activityOpening(){
		YQZZActivityStateData data = this.dataManger.getStateData();
		if(data.getJoinGame() != YQZZActivityJoinState.JOIN){
			return false;
		}
		if(data.getState() == YQZZActivityState.HIDDEN){
			return false;
		}
		if(data.getState() == YQZZActivityState.START_SHOW &&
				data.getSaveServerInfo() <=0){
			return false;
		}
		if(data.getState() == YQZZActivityState.END_SHOW){
			return false;
		}
		return true;
	}
	
	
	public IYQZZServiceState getState() {
		return state;
	}

	public void updateState(YQZZActivityState state) {
		IYQZZServiceState serviceState = IYQZZServiceState.getYQZZServiceState(state, this);
		try {
			serviceState.init();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		this.state = serviceState;
		Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : players) {
			this.syncYQZZWarInfo(player, true, true);
			YQZZMatchService.getInstance().syncYQZZLeagueWarInfo(player);
		}
		//记录一下
		String serverId = GsConfig.getInstance().getServerId();
		String key = "YQZZ_STATE_"+serverId;
		int termId = this.dataManger.getStateData().getTermId();
		DungeonRedisLog.log(key, "termId:{},state update:{}", termId,state.getValue());
	}
	
	
	@MessageHandler
	private void onBattleFinish(YQZZBilingInformationMsg msg) {
		int termId = this.dataManger.getStateData().getTermId();
		String roomId = msg.getRoomId();
		this.dataManger.updateYQZZGameDataFinishTime(termId, roomId);
		PBYQZZGameInfoSync gameInfo = msg.getLastSyncpb();
		YQZZBattleData.saveSourceData(gameInfo, null, roomId);
		this.addGuildStageControlLog(roomId, 100, gameInfo);
	}
	
	
	/**
	 * 记录日志
	 * @param roomId
	 * @param stage
	 * @param gameInfo
	 */
	public void addGuildStageControlLog(String roomId,int stage,PBYQZZGameInfoSync gameInfo){
		if(gameInfo == null){
			return;
		}
		//统计日志
		for(PBYQZZGuildInfo guildData: gameInfo.getGuildInfoList()){
			List<Integer>  controls = guildData.getControlBuildIdList();
			Map<Integer,Integer> controlTypes = new HashMap<>();
			for(int buildId : controls){
				YQZZBuildCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YQZZBuildCfg.class, buildId);
				if(cfg == null){
					continue;
				}
				int typeId = cfg.getBuildTypeId();
				int count = controlTypes.getOrDefault(typeId, 0);
				count ++;
				controlTypes.put(typeId, count);
			}
			for(Map.Entry<Integer, Integer> entry : controlTypes.entrySet()){
				int type = entry.getKey();
				int count = entry.getValue();
				LogUtil.logYQZZBuildGuildControlCount(guildData.getGuildId(), guildData.getGuildName(), 
						type, count, stage, roomId);
			}
		}
			
	}

	public YQZZDataManager getDataManger() {
		return dataManger;
	}
	
	public IYQZZServiceState initYQZZServiceState(){
		YQZZActivityState state = this.dataManger.getStateData().getState();
		IYQZZServiceState serviceState = IYQZZServiceState.getYQZZServiceState(state, this);
		if(serviceState == null){
			serviceState = IYQZZServiceState
					.getYQZZServiceState(YQZZActivityState.HIDDEN, this);
			serviceState.init();
		}
		return serviceState;
	}

	public boolean canJoinActivity(){
		NationSpaceFlight flight = (NationSpaceFlight)NationService.getInstance().getNationBuildingByType(National.NationbuildingType.NATION_SPACE_FLIGHT);
		return flight != null && flight.getLevel() >= 1;
	}
	
	/**
	 * 国家积分
	 * @param player
	 */
	public void syncYQZZPlayerScoreDetail(Player player){
		String playerId = player.getId();
		String guildId = player.getGuildId();
		YQZZBattleData data = this.getDataManger().getBattleData();
		YQZZPlayerGameData selfData = null;
		YQZZGuildGameData guildData = null;
		if(data != null){
			Map<String, YQZZPlayerGameData> playerMap = data.getPlayerDatas();
			Map<String, YQZZGuildGameData> guildMap = data.getGuildDatas();
			selfData = playerMap.get(playerId);
			guildData = guildMap.get(guildId);
		}
		int playerRank = (selfData == null? 0 :selfData.getRank());
		long score = (selfData == null? 0 :selfData.getScore());
		PBYQZZWarScoreDetailResp.Builder builder = PBYQZZWarScoreDetailResp.newBuilder();
		builder.setType(3);
		PBYQZZWarPlayerScore.Builder pbuilder = PBYQZZWarPlayerScore.newBuilder();
		pbuilder.setPlayerId(playerId);
		pbuilder.setScore(score);
		pbuilder.setRank(playerRank);
		if(selfData != null){
			//打怪
			Map<Integer, YQZZGameDataMonsterKill> monsterScoreMap = selfData.getMonsterScore();
			for(YQZZGameDataMonsterKill mscore : monsterScoreMap.values()){
				PBYQZZWarPlayerScoreMonster.Builder mbuilder = PBYQZZWarPlayerScoreMonster.newBuilder();
				mbuilder.setMonsterLevel(mscore.getMonsterLevel());
				mbuilder.setKillCount(mscore.getCount());
				mbuilder.setScore(mscore.getScore());
				pbuilder.addMonsterScores(mbuilder);
			}
			//幽灵基地
			Map<Integer, YQZZGameDataYuriKill> yuriScoreMap = selfData.getYuriScore();
			for(YQZZGameDataYuriKill yscore : yuriScoreMap.values()){
				PBYQZZWarPlayerScoreYuri.Builder ybuilder = PBYQZZWarPlayerScoreYuri.newBuilder();
				ybuilder.setMonsterLevel(yscore.getYuriLevel());
				ybuilder.setKillCount(yscore.getCount());
				ybuilder.setScore(yscore.getScore());
				pbuilder.addYuriScores(ybuilder);
			}
			//采集
			Map<Integer, YQZZGameDataResCollect> resScoreMap = selfData.getResScore();
			for(YQZZGameDataResCollect rscore : resScoreMap.values()){
				PBYQZZWarPlayerScoreCollect.Builder rbuilder = PBYQZZWarPlayerScoreCollect.newBuilder();
				rbuilder.setResType(rscore.getResType());
				rbuilder.setCollectCount(rscore.getCount());
				rbuilder.setScore(rscore.getScore());
				pbuilder.addCollectScores(rbuilder);
			}
			int pylonCnt = selfData.getPylonCnt();
			YQZZPylonCfg  pylonCfg = HawkConfigManager.getInstance().getConfigByIndex(YQZZPylonCfg.class, 0);
			YQZZWar.PBYQZZWarPlayerScorePowerTower.Builder powerbuilder = YQZZWar.PBYQZZWarPlayerScorePowerTower.newBuilder();
			powerbuilder.setCount(pylonCnt);
			if(pylonCfg == null){
				powerbuilder.setScore(0);
			}else {
				powerbuilder.setScore(pylonCnt * pylonCfg.getPlayerScore());
			}
			pbuilder.addPowerTowerScores(powerbuilder);
			//建筑得分
			if(guildData != null){
				Map<Integer, Integer> controlBuilds = guildData.getControlBuildTypes();
				Map<Integer, YQZZGameDataBuildScore> builderScoreMap = guildData.getPlayerBuildScore();
				for(YQZZGameDataBuildScore dataParam : builderScoreMap.values()){
					PBYQZZWarPlayerScoreBuild.Builder bbuilder = PBYQZZWarPlayerScoreBuild.newBuilder();
					int btype = dataParam.getBuildType();
					int count = controlBuilds.getOrDefault(btype, 0);
					bbuilder.setBuildId(btype);
					bbuilder.setHoldCount(count);
					bbuilder.setScore(dataParam.getScore());
					pbuilder.addBuildScores(bbuilder);
				}
			}
				
		}
		builder.setPlayerScore(pbuilder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SCORE_DETAIL_RESP, builder));
	}
	
	/**
	 * 联盟积分
	 * @param player
	 */
	public void syncYQZZGuildScoreDetail(Player player){
		String guildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		YQZZBattleData data = this.getDataManger().getBattleData();
		Map<String, YQZZGuildGameData> guildMap = null;
		YQZZGuildGameData selfData = null;
		if(data != null){
			guildMap = data.getGuildDatas();
			selfData = guildMap.get(guildId);
		}
		int guildRank = (selfData == null? 0 :selfData.getRank());
		int count = (guildMap == null? 0 :guildMap.size());
		long score = (selfData == null? 0 :selfData.getScore());
		PBYQZZWarScoreDetailResp.Builder builder = PBYQZZWarScoreDetailResp.newBuilder();
		builder.setType(2);
		PBYQZZWarGuildScore.Builder gbuilder = PBYQZZWarGuildScore.newBuilder();
		gbuilder.setGuildId(guildId);
		gbuilder.setScore(score);
		gbuilder.setRank(guildRank);
		gbuilder.setGuildCount(count);
		if(selfData != null){
			Map<Integer, YQZZGameDataBuildScore>  scoreMap = selfData.getGuildBuildScore();
			Map<Integer,Integer>  holdMap = selfData.getControlBuildTypes();
			for(YQZZGameDataBuildScore bscore : scoreMap.values()){
				int controlCount = holdMap.getOrDefault(bscore.getBuildType(), 0);
				PBYQZZWarGuildScoreBuild.Builder bbuilder = PBYQZZWarGuildScoreBuild.newBuilder();
				bbuilder.setBuildId(bscore.getBuildType());
				bbuilder.setHoldCount(controlCount);
				bbuilder.setScore(bscore.getScore());
				gbuilder.addBuildScores(bbuilder);
			}
		}
		builder.setGuildScore(gbuilder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SCORE_DETAIL_RESP, builder));
	}
	
	
	/**
	 * 国家积分
	 * @param player
	 */
	public void syncYQZZContryScoreDetail(Player player){
		int termId = this.dataManger.getStateData().getTermId();
		String serverId = player.getMainServerId();
		YQZZBattleData data = this.getDataManger().getBattleData();
		Map<String, YQZZCountryGameData> countryMap = null;
		YQZZCountryGameData selfData = null;
		Map<String, YQZZGuildGameData> guildMap = null;
		if(data != null){
			countryMap = data.getCountryDatas();
			selfData = countryMap.get(serverId);
			guildMap = data.getGuildDatas();
		}
		int countryRank = (selfData == null? 0 :selfData.getRank());
		int count = (countryMap == null? 0 :countryMap.size());
		long score = (selfData == null? 0 :selfData.getScore());
		PBYQZZWarScoreDetailResp.Builder builder = PBYQZZWarScoreDetailResp.newBuilder();
		builder.setType(1);
		PBYQZZWarCountryScore.Builder cbuilder = PBYQZZWarCountryScore.newBuilder();
		cbuilder.setTermId(termId);
		cbuilder.setServerId(serverId);
		cbuilder.setScore(score);
		cbuilder.setRank(countryRank);
		cbuilder.setCountryCount(count);
		if(guildMap != null){
			for(YQZZGuildGameData gdata : guildMap.values()){
				if(gdata.getServerId().equals(serverId)){
					PBYQZZWarCountryScoreGuild.Builder gbuilder = PBYQZZWarCountryScoreGuild.newBuilder();
					gbuilder.setId(gdata.getGuildId());
					gbuilder.setName(gdata.getGuildName());
					gbuilder.setTag(gdata.getGuildTag());
					gbuilder.setGuildFlag(gdata.getGuildFlag());
					gbuilder.setLeaderId(gdata.getLeaderId());
					gbuilder.setLeaderName(gdata.getLeaderName());
					gbuilder.setScore(gdata.getScore());
					cbuilder.addGuildScores(gbuilder);
				}
			}
		}
		//幽灵基地
		Map<Integer, YQZZGameDataYuriKill> yuriScoreMap = selfData.getYuriScore();
		for(YQZZGameDataYuriKill yscore : yuriScoreMap.values()){
			PBYQZZWarPlayerScoreYuri.Builder ybuilder = PBYQZZWarPlayerScoreYuri.newBuilder();
			ybuilder.setMonsterLevel(yscore.getYuriLevel());
			ybuilder.setKillCount(yscore.getCount());
			ybuilder.setScore(yscore.getScore());
			cbuilder.addYuriScores(ybuilder);
		}
		//能量塔
		int pylonCnt = selfData.getPylonCnt();
		YQZZPylonCfg  pylonCfg = HawkConfigManager.getInstance().getConfigByIndex(YQZZPylonCfg.class, 0);
		YQZZWar.PBYQZZWarPlayerScorePowerTower.Builder powerbuilder = YQZZWar.PBYQZZWarPlayerScorePowerTower.newBuilder();
		powerbuilder.setCount(pylonCnt);
		if(pylonCfg == null){
			powerbuilder.setScore(0);
		}else {
			powerbuilder.setScore(pylonCnt * pylonCfg.getNationScore());
		}
		cbuilder.addPowerTowerScores(powerbuilder);
		if(selfData != null){
			Map<Integer, Integer> controlBuilds = selfData.getBuildControl();
			Map<Integer, YQZZGameDataBuildScore> builderScoreMap = selfData.getBuildScore();
			for(YQZZGameDataBuildScore dataParam : builderScoreMap.values()){
				PBYQZZWarPlayerScoreBuild.Builder bbuilder = PBYQZZWarPlayerScoreBuild.newBuilder();
				int btype = dataParam.getBuildType();
				int bcount = controlBuilds.getOrDefault(btype, 0);
				bbuilder.setBuildId(btype);
				bbuilder.setHoldCount(bcount);
				bbuilder.setScore(dataParam.getScore());
				cbuilder.addBuildScores(bbuilder);
			}
		}
		builder.setCountryScore(cbuilder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SCORE_DETAIL_RESP, builder));
	}
	
	
	
	/**
	 * 排行 -玩家
	 */
	public void syncYQZZRankPlayer(Player player){
		String playerId = player.getId();
		int termId = this.dataManger.getStateData().getTermId();
		YQZZBattleData data = this.dataManger.getBattleData();
		Map<String, YQZZPlayerGameData> playerMap = null;
		YQZZPlayerGameData selfData = null;
		YQZZCountryGameData selfCountryData = null;
		if(data != null){
			playerMap = data.getPlayerDatas();
			selfData = playerMap.get(playerId);
			selfCountryData = data.getCountryDatas().get(player.getMainServerId());
		}

		long playerScore = (selfData == null? 0 : selfData.getScore());
		int playerRank = (selfData == null? 0 : selfData.getRank());
		int playerCountryRank = (selfCountryData == null? 0 : selfCountryData.getRank());
		PBYQZZWarRankResp.Builder builder = PBYQZZWarRankResp.newBuilder();
		builder.setNationRank(playerCountryRank);
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		if(playerMap != null){
			List<YQZZPlayerGameData> clist = new ArrayList<>();
			clist.addAll(playerMap.values());
			Collections.sort(clist, new Comparator<YQZZPlayerGameData>() {
				@Override
				public int compare(YQZZPlayerGameData o1, YQZZPlayerGameData o2) {
					return o1.getRank() - o2.getRank();
				}
			});
			Map<String, Integer> campMap = getCampMap(this.getDataManger().getRoomData());
			for(YQZZPlayerGameData pdata : clist){
				if(pdata.getRank() <= constCfg.getPlayerScoreRankSize()){
					PBYQZZWarPlayerRankMember.Builder mbuilder = PBYQZZWarPlayerRankMember.newBuilder();
					mbuilder.setTermId(termId);
					mbuilder.setServerId(pdata.getServerId());
					mbuilder.setId(pdata.getPlayerId());
					mbuilder.setName(pdata.getPlayerName());
					mbuilder.setGuildName(pdata.getPlayerGuildName());
					mbuilder.setPlayerRank(pdata.getRank());
					mbuilder.setPlayerScore(pdata.getScore());
					if(campMap != null){
						mbuilder.setCamp(campMap.getOrDefault(pdata.getServerId(), 1));
					}
					builder.addPlayerMembers(mbuilder);
				}
			}
		}
		builder.setType(3);
		builder.setRank(playerRank);
		builder.setScore(playerScore);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SCORE_RANK_RESP, builder));
	}
	
	/**
	 * 排行-联盟
	 */
	public void syncYQZZRankGuild(Player player){
		int termId = this.dataManger.getStateData().getTermId();
		String guildId = player.getGuildId();
		YQZZBattleData data = this.dataManger.getBattleData();
		Map<String, YQZZGuildGameData> guildMap = null;
		YQZZGuildGameData selfData = null;
		if(data != null){
			guildMap = data.getGuildDatas();
			selfData = guildMap.get(guildId);
		}
		
		int playerGuildRank = (selfData == null? 0 : selfData.getRank());
		long playerGuildScore = (selfData == null? 0 : selfData.getScore());
		int count = (guildMap == null? 0 : guildMap.size());
		
		PBYQZZWarRankResp.Builder builder = PBYQZZWarRankResp.newBuilder();
		if(guildMap != null){
			List<YQZZGuildGameData> clist = new ArrayList<>();
			clist.addAll(guildMap.values());
			Collections.sort(clist, new Comparator<YQZZGuildGameData>() {
				@Override
				public int compare(YQZZGuildGameData o1, YQZZGuildGameData o2) {
					return o1.getRank() - o2.getRank();
				}
			});
			for(YQZZGuildGameData gdata : clist){
				PBYQZZWarGuildRankMember.Builder mbuilder = PBYQZZWarGuildRankMember.newBuilder();
				mbuilder.setTermId(termId);
				mbuilder.setServerId(gdata.getServerId());
				mbuilder.setId(gdata.getGuildId());
				mbuilder.setName(gdata.getGuildName());
				mbuilder.setTag(gdata.getGuildTag());
				mbuilder.setGuildFlag(gdata.getGuildFlag());
				mbuilder.setGuildRank(gdata.getRank());
				mbuilder.setGuildScore(gdata.getScore());
				builder.addGuildMembers(mbuilder);
			}
		}
		builder.setType(2);
		builder.setRank(playerGuildRank);
		builder.setTotal(count);
		builder.setScore(playerGuildScore);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SCORE_RANK_RESP, builder));
	}
	/**
	 * 排行-国家
	 */
	public void syncYQZZRankContry(Player player){
		int termId = this.dataManger.getStateData().getTermId();
		String serverId = player.getMainServerId();
		YQZZBattleData data = this.dataManger.getBattleData();
		Map<String, YQZZCountryGameData> countryMap = null;
		YQZZCountryGameData selfData = null;
		if(data != null){
			countryMap = data.getCountryDatas();
			selfData = countryMap.get(serverId);
		}
		int playerCountryRank = (selfData == null? 0 : selfData.getRank());
		long playerCountryScore = (selfData == null? 0 : selfData.getScore());
		int totalSize = 0;
		PBYQZZWarRankResp.Builder builder = PBYQZZWarRankResp.newBuilder();
		if(countryMap != null){
			totalSize = countryMap.size();
			List<YQZZCountryGameData> clist = new ArrayList<>();
			clist.addAll(countryMap.values());
			Collections.sort(clist, new Comparator<YQZZCountryGameData>() {
				@Override
				public int compare(YQZZCountryGameData o1, YQZZCountryGameData o2) {
					return o1.getRank() - o2.getRank();
				}
			});
			Map<String, Integer> campMap = getCampMap(this.getDataManger().getRoomData());
			for(YQZZCountryGameData cdata : clist){
				PBYQZZWarCountryRankMember.Builder mbuilder = PBYQZZWarCountryRankMember.newBuilder();
				mbuilder.setTermId(termId);
				mbuilder.setServerId(cdata.getServerId());
				mbuilder.setCountryRank(cdata.getRank());
				mbuilder.setCountryScore(cdata.getScore());
				if(campMap!=null){
					mbuilder.setCamp(campMap.getOrDefault(cdata.getServerId(),1));
				}
				builder.addCountryMembers(mbuilder);
			}
		}
		
		builder.setType(1);
		builder.setRank(playerCountryRank);
		builder.setTotal(totalSize);
		builder.setScore(playerCountryScore);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SCORE_RANK_RESP, builder));
	}
	
	
	/**
	 * 战绩详情-联盟
	 */
	public void syncYQZZHistoryDetailPlayer(Player player,int termId){
		List<Integer> showTerms = this.getShowHistoryTerm();
		if(!showTerms.contains(termId)){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		YQZZRecordData recordData = YQZZRecordData.loadData(serverId, termId);
		if(recordData == null){
			return;
		}
		String roomId = recordData.getRoomId();
		YQZZBattleData data = this.getDataManger().getHistoyrYQZZBattleData(termId, roomId);
		if(data ==null){
			return;
		}
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		Map<String, YQZZPlayerGameData> playerMap = data.getPlayerDatas();
		Map<String, Integer> campMap = getCampMap(this.getDataManger().getRoomData());
		PBYQZZWarHistroyDetailResp.Builder builder = PBYQZZWarHistroyDetailResp.newBuilder();
		for(YQZZPlayerGameData member : playerMap.values()){
			if(member.getRank() > constCfg.getPlayerScoreRankSize()){
				continue;
			}
			PBYQZZWarPlayerRankMember.Builder mbuilder = PBYQZZWarPlayerRankMember.newBuilder();
			mbuilder.setTermId(termId);
			mbuilder.setServerId(member.getServerId());
			mbuilder.setId(member.getPlayerId());
			mbuilder.setName(member.getPlayerName());
			mbuilder.setGuildName(member.getPlayerGuildName());
			mbuilder.setPlayerRank(member.getRank());
			mbuilder.setPlayerScore(member.getScore());
			if(campMap != null){
				mbuilder.setCamp(campMap.getOrDefault(member.getServerId(), 1));
			}
			builder.addPlayerRecords(mbuilder);
		}
		builder.setTermId(termId);
		builder.setType(3);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_HISTORY_DETAIL_INFO_RESP, builder));
	}
	
	/**
	 * 战绩详情-联盟
	 */
	public void syncYQZZHistoryDetailGuild(Player player,int termId){
		List<Integer> showTerms = this.getShowHistoryTerm();
		if(!showTerms.contains(termId)){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		YQZZRecordData recordData = YQZZRecordData.loadData(serverId, termId);
		if(recordData == null){
			return;
		}
		String roomId = recordData.getRoomId();
		YQZZBattleData data = this.getDataManger().getHistoyrYQZZBattleData(termId, roomId);
		if(data ==null){
			return;
		}
		Map<String, YQZZGuildGameData> guildMap = data.getGuildDatas();
		PBYQZZWarHistroyDetailResp.Builder builder = PBYQZZWarHistroyDetailResp.newBuilder();
		for(YQZZGuildGameData member : guildMap.values()){
			PBYQZZWarGuildRankMember.Builder mbuilder = PBYQZZWarGuildRankMember.newBuilder();
			mbuilder.setTermId(termId);
			mbuilder.setServerId(member.getServerId());
			mbuilder.setId(member.getGuildId());
			mbuilder.setName(member.getGuildName());
			mbuilder.setTag(member.getGuildTag());
			mbuilder.setGuildFlag(member.getGuildFlag());
			mbuilder.setGuildRank(member.getRank());
			mbuilder.setGuildScore(member.getScore());
			builder.addGuildRecords(mbuilder);
		}
		builder.setTermId(termId);
		builder.setType(2);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_HISTORY_DETAIL_INFO_RESP, builder));
	}
	/**
	 * 战绩详情-国家
	 */
	public void syncYQZZHistoryDetailContry(Player player,int termId){
		List<Integer> showTerms = this.getShowHistoryTerm();
		if(!showTerms.contains(termId)){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		YQZZRecordData recordData = YQZZRecordData.loadData(serverId, termId);
		if(recordData == null){
			return;
		}
		String roomId = recordData.getRoomId();
		YQZZBattleData data = this.getDataManger().getHistoyrYQZZBattleData(termId, roomId);
		if(data ==null){
			return;
		}
		PBYQZZWarHistroyDetailResp.Builder builder = PBYQZZWarHistroyDetailResp.newBuilder();
		Map<String, YQZZCountryGameData> countryMap = data.getCountryDatas();
		Map<String, Integer> campMap = getCampMap(this.getDataManger().getRoomData());
		for(YQZZCountryGameData countryData : countryMap.values()){
			PBYQZZWarCountryRankMember.Builder mbuilder = PBYQZZWarCountryRankMember.newBuilder();
			mbuilder.setTermId(termId);
			mbuilder.setServerId(countryData.getServerId());
			mbuilder.setCountryRank(countryData.getRank());
			mbuilder.setCountryScore(countryData.getScore());
			if(campMap!=null){
				mbuilder.setCamp(campMap.getOrDefault(countryData.getServerId(),1));
			}
			builder.addCountryRecords(mbuilder);
		}
		builder.setTermId(termId);
		builder.setType(1);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_HISTORY_DETAIL_INFO_RESP, builder));
	}
	
	/***
	 * 历史战绩
	 * @param player
	 */
	public void syncYQZZHistoryInfo(Player player){
		int maxRank = this.getDataManger().getStatisticsData().getMaxRank();
		String serverId = GsConfig.getInstance().getServerId();
		List<Integer> showTerms = this.getShowHistoryTerm();
		Map<Integer,YQZZRecordData>  records = YQZZRecordData.loadAll(serverId, showTerms);
		PBYQZZWarHistroyResp.Builder builder = PBYQZZWarHistroyResp.newBuilder();
		builder.setRankMax(maxRank);
		for(YQZZRecordData record:records.values()){
			PBYQZZWarRecord.Builder rbuilder = PBYQZZWarRecord.newBuilder();
			int termId = record.getTermId();
			YQZZTimeCfg cfg = HawkConfigManager.getInstance()
					.getConfigByKey(YQZZTimeCfg.class, termId);
			if(cfg == null){
				continue;
			}
			rbuilder.setTermId(termId);
			rbuilder.setRecordTime(cfg.getBattleTimeValue());
			rbuilder.setCountryRank(record.getRank());
			rbuilder.setCountryScore(record.getScore());
			builder.addRecords(rbuilder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_HISTORY_INFO_RESP, builder));
	}
	
	private List<Integer> getShowHistoryTerm(){
		int curTerm = 0;
		ConfigIterator<YQZZTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		long now = HawkTime.getMillisecond();
		for (YQZZTimeCfg timeCfg : its) {
			if (now < timeCfg.getShowTimeValue()) {
				continue;
			}
			if(timeCfg.getTermId() > curTerm){
				curTerm = timeCfg.getTermId();
			}
		}
		List<Integer> showTerm = new ArrayList<>();
		for(int i=curTerm;i>0;i--){
			showTerm.add(i);
			if(showTerm.size() > 10){
				break;
			}
		}
		return showTerm;
	}
	
	
	
	/**
	 * 同步匹配房间信息
	 * @param player
	 */
	public void syncYQZZMatchRoomInfo(Player player){
		PBYQZZWarMatchInfoResp.Builder builder = PBYQZZWarMatchInfoResp.newBuilder();
		List<PBYQZZWarCountry> list = this.genPBYQZZWarCountryBuilderList();
		if(!list.isEmpty()){
			builder.addAllCountrys(list);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_MATCH_INFO_RESP, builder));
	}
	
	
	/**
	 * 同步活动状态信息
	 * @param player
	 * @param rankInfo
	 * @param achiveInfo
	 */
	public void syncYQZZWarInfo(Player player,boolean rankInfo,boolean achiveInfo){
		PBYQZZWarPageInfoResp.Builder builder = PBYQZZWarPageInfoResp.newBuilder();
		PBYQZZWarStateInfo.Builder sbuilder = genWarStateBuilder();
		builder.setWarInfo(sbuilder);
		if(rankInfo){
			PBYQZZWarRank.Builder rbuilder = this.genYQZZWarRankBuilder(player);
			builder.setRankInfo(rbuilder);
		}
		if(achiveInfo){
			PBYQZZWarAchieve.Builder abuilder = player.getPlayerYQZZData()
					.genAchieveBuilder();
			builder.setAchieveInfo(abuilder);
		}
		List<PBYQZZWarCountry> countrys = this.genPBYQZZWarCountryBuilderList();
		if(!countrys.isEmpty()){
			builder.addAllCountrys(countrys);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_STATE_INFO_RESP, sbuilder));
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_PAGE_INFO_RESP, builder));
	}


	public void syncYQZZLeagueWarInfo(Player player){
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_LEAGUE_WAR_INFO_RESP, genYQZZLeagueWarInfo()));
	}

	private YQZZWar.PBYQZZLeagueWarInfoResp.Builder genYQZZLeagueWarInfo(){
		YQZZWar.PBYQZZLeagueWarInfoResp.Builder resp = YQZZWar.PBYQZZLeagueWarInfoResp.newBuilder();
		resp.setType(YQZZWar.PBYQZZWarType.YQZZ_NOT_SEASON);
		int termId = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
		if(termId == 0){
			resp.setTermId(1);
			resp.setType(YQZZWar.PBYQZZWarType.YQZZ_NOT_SEASON);
			resp.setSeason(0);
			resp.setTurn(0);
			resp.setState(YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_START_SHOW);
			resp.setStartTime(Long.MAX_VALUE);
			resp.setEndTime(Long.MAX_VALUE);
			return resp;
		}
		resp.setTermId(termId);
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		if(timeCfg == null
				|| timeCfg.getSeason() <= 0
				|| timeCfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_NOT_SEASON_VALUE){
			YQZZSeasonStateData curData = getDataManger().getSeasonStateData();
			if(curData.getState() == YQZZConst.YQZZSeasonState.OPEN){
				ConfigIterator<YQZZTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
				YQZZTimeCfg nextCfg = null;
				int nextSeason = curData.getSeason();
				for(YQZZTimeCfg yqzzTimeCfg : iterator){
					if(yqzzTimeCfg.getSeason() == nextSeason
							&& yqzzTimeCfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_GROUP_VALUE
							&& yqzzTimeCfg.getTurn() == 1){
						nextCfg = yqzzTimeCfg;
						break;
					}
				}
				resp.setType(YQZZWar.PBYQZZWarType.YQZZ_GROUP);
				resp.setSeason(curData.getSeason());
				resp.setTurn(1);
				resp.setState(YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_START_SHOW);
				resp.setStartTime(nextCfg.getSeasonStartTimeValue());
				resp.setEndTime(nextCfg.getEndShowTimeValue());
			}else {
				resp.setTermId(1);
				resp.setType(YQZZWar.PBYQZZWarType.YQZZ_NOT_SEASON);
				resp.setSeason(0);
				resp.setTurn(0);
				resp.setState(YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_START_SHOW);
				resp.setStartTime(Long.MAX_VALUE);
				resp.setEndTime(Long.MAX_VALUE);
			}
			return resp;
		}
		resp.setType(YQZZWar.PBYQZZWarType.valueOf(timeCfg.getType()));
		resp.setSeason(timeCfg.getSeason());
		resp.setTurn(timeCfg.getTurn());
		resp.setState(getClientLeagueState());
		resp.setStartTime(timeCfg.getShowTimeValue());
		resp.setEndTime(timeCfg.getEndShowTimeValue());
		if(timeCfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_KICKOUT_VALUE){
			YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(timeCfg.getSeason(), GsConfig.getInstance().getServerId());
			resp.setKickout(!seasonServer.isAdvance());
			resp.setGroupRank(seasonServer.getGroupRank());
			return resp;
		}else {
			resp.setGroupRank(YQZZSeasonServer.getGroupSelfRank(timeCfg.getSeason(), GsConfig.getInstance().getServerId()));
			return resp;
		}
	}

	public YQZZWar.PBYQZZLeagueWarState getClientLeagueState(){
		YQZZActivityState state = YQZZMatchService.getInstance().getDataManger().getStateData().getState();
		switch (state) {
			case START_SHOW: return YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_START_SHOW;
			case MATCH: return YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_MATCH;
			case BATTLE: return YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_BATTLE;
			case REWARD: return  YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_END_SHOW;
			case END_SHOW: return YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_END_SHOW;
			case HIDDEN: return YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_HIDDEN;
			default: return YQZZWar.PBYQZZLeagueWarState.YQZZ_LEAGUE_HIDDEN;
		}
	}
	
	
	private List<PBYQZZWarCountry> genPBYQZZWarCountryBuilderList(){
		List<PBYQZZWarCountry> list = new ArrayList<>();
		Map<String, YQZZSeasonTitle> titleMap = YQZZSeasonTitle.loadAll();
		Map<String, YQZZJoinServer> serverMap = this.getDataManger().getRoomServerDataMap();
		Map<String, Integer> campMap = getCampMap(this.getDataManger().getRoomData());
		if(serverMap != null){
			for(YQZZJoinServer serverInfo : serverMap.values()){
				Map<String, YQZZJoinGuild> guildMap = this.getDataManger().getRoomGuildsByServer(serverInfo.getServerId());
				PBYQZZWarCountry.Builder cbuilder = PBYQZZWarCountry.newBuilder();
				cbuilder.setServerId(serverInfo.getServerId());
				if(!HawkOSOperator.isEmptyString(serverInfo.getLeaderId())){
					PBYQZZWarCountryCommander.Builder mbuilder = PBYQZZWarCountryCommander.newBuilder();
					mbuilder.setServerId(serverInfo.getServerId());
					mbuilder.setPlayerId(serverInfo.getLeaderId());
					mbuilder.setPlayerName(serverInfo.getLeaderName());
					if(!HawkOSOperator.isEmptyString(serverInfo.getLeaderGuild())){
						mbuilder.setGuildId(serverInfo.getLeaderGuild());
						mbuilder.setGuildName(serverInfo.getLeaderGuildName());
						mbuilder.setGuildFlag(serverInfo.getLeaderGuildFlag());
						mbuilder.setGuildTag(serverInfo.getLeaderGuildTag());
					}
					cbuilder.setCommander(mbuilder);
				}
				if(titleMap.containsKey(serverInfo.getServerId())){
					YQZZSeasonTitle title = titleMap.get(serverInfo.getServerId());
					if(title.getNationHonor() == 0){
						cbuilder.setNationHonor(-1);
					}else {
						cbuilder.setNationHonor(title.getNationHonor());
					}
				}else {
					cbuilder.setNationHonor(-1);
				}
				for(YQZZJoinGuild guild :guildMap.values()){
					cbuilder.addGuilds(guild.genYQZZWarGuildBuilder());
				}
				if(campMap !=null){
					cbuilder.setCamp(campMap.getOrDefault(serverInfo.getServerId(), 1));
				}
				list.add(cbuilder.build());
			}
		}
		return list;
	}

	private Map<String, Integer> getCampMap(YQZZMatchRoomData roomData){
		if(roomData == null){
			return null;
		}
		Map<String, Integer> campMap = new HashMap<>();
		int i = 0;
		for(String sid : roomData.getServers()){
			i++;
			YQZZ_CAMP camp = yqzzCamp(i);
			if(camp == null){
				continue;
			}
			campMap.put(sid, camp.intValue());
		}
		return campMap;
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

	private PBYQZZWarRank.Builder genYQZZWarRankBuilder(Player player){
		String guildId = player.getGuildId();
		String serverId = player.getMainServerId();
		String playerId = player.getId();
		YQZZBattleData battleData = this.dataManger.getBattleData();
		
		int contryRank = (battleData ==null?0:battleData.getCountryRank(serverId));
		int contryCount = (battleData == null?0:battleData.getCountryCount());
		
		int guildRank = (battleData ==null?0:battleData.getGuildRank(guildId));
		int guildCount = (battleData ==null?0:battleData.getGuildCount());
		
		int playerRank = (battleData ==null?0:battleData.getPlayerRank(playerId));
		PBYQZZWarRank.Builder builder = PBYQZZWarRank.newBuilder();
		builder.setCountryRank(contryRank);
		builder.setCountryCount(contryCount);
		builder.setGuildRank(guildRank);
		builder.setGuildCount(guildCount);
		builder.setPlayerRank(playerRank);
		return builder;
	}
	
	
	private PBYQZZWarStateInfo.Builder genWarStateBuilder(){
		int termId = this.getDataManger().getStateData().getTermId();
		YQZZActivityState state = this.getDataManger().getStateData().getState();
		YQZZActivityJoinState joinState = this.getDataManger().getStateData().getJoinGame();
		PBYQZZWarStateInfo.Builder builder = PBYQZZWarStateInfo.newBuilder();
		if(termId ==0 || state == YQZZActivityState.HIDDEN || joinState == YQZZActivityJoinState.OUT){
			builder.setTermId(0);
			builder.setState(PBYQZZWarState.YQZZ_HIDDEN);
			return builder;
		}
		PBYQZZWarState warState = this.genActivityStateBuilder();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		builder.setShowTime(timeCfg.getShowTimeValue());
		builder.setMatchTime(timeCfg.getMatchTimeValue());
		builder.setFightTime(timeCfg.getBattleTimeValue());
		builder.setFightEndTime(timeCfg.getRewardTimeValue());
		builder.setRewardEndTime(timeCfg.getEndShowTimeValue());
		builder.setHiddenTime(timeCfg.getHiddenTimeValue());
		PBYQZZWarMoonTime.Builder timeBuilder = PBYQZZWarMoonTime.newBuilder();
		timeBuilder.setFightTime(timeCfg.getBattleTimeValue());
		timeBuilder.setOverTime(timeCfg.getRewardTimeValue());
		List<YQZZBattleStageTimeCfg> stageList = HawkConfigManager.getInstance()
				.getConfigIterator(YQZZBattleStageTimeCfg.class).toList();
		for(YQZZBattleStageTimeCfg stage : stageList){
			PBYQZZWarMoonBattleStageTime.Builder sbuilder = PBYQZZWarMoonBattleStageTime.newBuilder();
			sbuilder.setStage(stage.getStageId());
			sbuilder.setStartTime(timeCfg.getBattleTimeValue() + stage.getStageStartTime() * 1000);
			sbuilder.setEndTime(timeCfg.getBattleTimeValue() + stage.getStageEndTime() * 1000);
			timeBuilder.addStageTimes(sbuilder);
		}
		
		builder.setTermId(termId);
		builder.setState(warState);
		builder.setMoonTime(timeBuilder);
		return builder;
	}
	
	
	private PBYQZZWarState genActivityStateBuilder(){
		YQZZActivityState state = this.getDataManger().getStateData().getState();
		YQZZActivityJoinState joinState = this.getDataManger().getStateData().getJoinGame();
		if(state != YQZZActivityState.HIDDEN 
				&& joinState == YQZZActivityJoinState.OUT){
			return PBYQZZWarState.YQZZ_EARTH;
		}
		switch (state) {
		case START_SHOW:return PBYQZZWarState.YQZZ_MOON_SHOW;
		case MATCH:return PBYQZZWarState.YQZZ_MOON_REPARE;
		case BATTLE:return PBYQZZWarState.YQZZ_MOON_FIGHT;
		case REWARD:return  PBYQZZWarState.YQZZ_CLOSE;
		case END_SHOW:return PBYQZZWarState.YQZZ_CLOSE;
		case HIDDEN:return PBYQZZWarState.YQZZ_HIDDEN;
		default: return null;
		}
		
	}
	

	
	
	
	public boolean joinRoom(Player player){
		int termId = this.dataManger.getStateData().getTermId();
		YQZZActivityState state = this.dataManger.getStateData().getState();
		if(state != YQZZActivityState.BATTLE){
			return false;
		}
		YQZZMatchRoomData roomData = this.dataManger.getRoomData();
		if (roomData == null) {
			HawkLog.logPrintln("YQZZWarService enter room error, room not esixt, playerId: {}, termId: {}", player.getId(), termId);
			return false;
		}
		String roomId = roomData.getRoomId();
		if (!YQZZRoomManager.getInstance().hasGame(roomId)) {
			HawkLog.logPrintln("YQZZWarService enter room error, game not esixt, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getRoomId());
			return false;
		}
		String playerServerId = player.getMainServerId();
		String playerGuildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(playerGuildId)){
			HawkLog.logPrintln("YQZZWarService enter room error, guild not esixt, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getRoomId());
			return false;
		}
		if(!roomData.getServers().contains(playerServerId)){
			HawkLog.logPrintln("YQZZWarService enter room error, server not in list, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getRoomId());
			return false;
		}
		YQZZJoinServer joinServer = this.getDataManger().getRoomServerDataMap().get(playerServerId);
		if(joinServer == null ){
			HawkLog.logPrintln("YQZZWarService enter room error, joinServer not esixt playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getRoomId());
			return false;
		}
		Map<String, YQZZJoinGuild> guildMap = this.getDataManger().getRoomGuildsByServer(joinServer.getServerId());
		YQZZJoinGuild joinGuild = guildMap.get(playerGuildId);
		if(joinGuild == null){
			HawkLog.logPrintln("YQZZWarService enter room error, joinGuild not esixt, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getRoomId());
			return false;
		}
		if (!YQZZRoomManager.getInstance().joinGame(roomData.getRoomId(), player)) {
			HawkLog.logPrintln("YQZZWarService enter room error, joinGame failed, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getRoomServerId(), player.getGuildId());
			return false;
		}
		DungeonRedisLog.log(player.getId(), "{}", roomId);
		LogUtil.logYQZZEnterInfo(player.getId(), termId, roomId, roomData.getRoomServerId(), playerGuildId, playerServerId,player.getPower());
		return true;
	}

	/**
	 * 是否是可操作时间, 针对主动跨服和主动退出跨服的玩家.
	 * @return
	 */
	public boolean isOperateTime() {
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		int unoperateTime = constCfg.getUnoperatorTime() * 1000;
		long currentDayZeroTime = HawkTime.getAM0Date().getTime();
		long nextDayZeroTime = HawkTime.getNextAM0Date();
		long currentTime = HawkTime.getMillisecond();
		
		//当天0点的后面多长时间     下个0点的前面多长时间 都是不可以操作的.
		if (currentDayZeroTime + unoperateTime > currentTime   || currentTime + unoperateTime >  nextDayZeroTime) {
			return false;
		}
		return true;
	}

	public YQZZSeasonStateData calSeasonStateData(){
		YQZZSeasonStateData seasonStateData = new YQZZSeasonStateData();
		long now = HawkTime.getMillisecond();
		int season = -1;
		ConfigIterator<YQZZTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		for(YQZZTimeCfg cfg : iterator){
			if(cfg.getSeason() < 0){
				continue;
			}
			if(cfg.getSeasonStartTimeValue() != 0 && now >= cfg.getSeasonStartTimeValue()){
				season = cfg.getSeason();
			}
		}
		if(season == -1){
			return seasonStateData;
		}
		seasonStateData.setSeason(season);
		iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		for(YQZZTimeCfg cfg : iterator){
			if(cfg.getSeason() != season){
				continue;
			}
			if(cfg.getSeasonEndTimeValue() != 0 && now <= cfg.getSeasonEndTimeValue()){
				seasonStateData.setState(YQZZConst.YQZZSeasonState.OPEN);
			}
		}
		return seasonStateData;
	}

	public void onSeasonTick(){
		try {
			YQZZSeasonStateData calData = calSeasonStateData();
			YQZZSeasonStateData curData = getDataManger().getSeasonStateData();
			if(curData.getSeason() != calData.getSeason() || curData.getState() != calData.getState()){
				curData.next(calData);
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	public void onSeasonStart(){
		String serverId = GsConfig.getInstance().getServerId();
		//服务器
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(String.valueOf(serverId));
		// 司令
		President president = PresidentFightService.getInstance().getPresidentCity().getPresident();
		int season = getDataManger().getSeasonStateData().getSeason();
		YQZZSeasonServer seasonServer = new YQZZSeasonServer();
		seasonServer.setSeason(season);
		seasonServer.setServerName(serverInfo.getName());
		if(president != null &&
				HawkOSOperator.isEmptyString(president.getPlayerId())){
			seasonServer.setLeaderName(president.getPlayerName());
		}else {
			seasonServer.setLeaderName("");
		}
		seasonServer.saveRedis();
		NationSpaceFlight flight = (NationSpaceFlight)NationService.getInstance().getNationBuildingByType(National.NationbuildingType.NATION_SPACE_FLIGHT);
		if(flight != null && flight.getLevel() >= 1){
			long currTime = HawkTime.getMillisecond();
			long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
			SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
					.setMailId(MailConst.MailId.YQZZ_LEAGUE_START)
					.build(), currTime, currTime + experiTime);
		}

	}

	public void onSeasonReward(){

	}

	public void onSeasonEnd(){

	}
}
