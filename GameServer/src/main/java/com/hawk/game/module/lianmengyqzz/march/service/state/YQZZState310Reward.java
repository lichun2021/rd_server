package com.hawk.game.module.lianmengyqzz.march.service.state;

import java.util.*;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.YQZZScoreEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZBattleCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.*;
import com.hawk.game.module.lianmengyqzz.march.data.global.*;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.YQZZWar;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZCountryGameData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZGuildGameData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData.YQZZPlayerGameData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZStatisticsData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityJoinState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;

public class YQZZState310Reward  extends IYQZZServiceState {

	private long lastTickTime = 0;
	
	public YQZZState310Reward(YQZZMatchService parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		this.getDataManager().getStateData().setState(YQZZActivityState.REWARD);
		this.getDataManager().getStateData().saveRedis();
		long curTime = HawkTime.getMillisecond();
		this.lastTickTime = curTime;
	}

	@Override
	public void tick() {
		YQZZActivityStateData data = this.calcInfo();
		//如果不在当前状态，则往下个状态推进
		YQZZActivityStateData curData = this.getDataManager().getStateData();
		if(curData.getTermId() != data.getTermId()
				|| curData.getState() != data.getState()){
			this.getParent().updateState(YQZZActivityState.END_SHOW);
			return;
		}
		//如果不参与战斗
		YQZZActivityJoinState joinState = this.getDataManager()
				.getStateData().getJoinGame();
		if(joinState == YQZZActivityJoinState.OUT){
			return;
		}
		YQZZTimeCfg timeCfg = this.getTimeCfg();
		long endShowTime = timeCfg.getEndShowTimeValue();
		long curTime = HawkTime.getMillisecond();
		if(curTime > this.lastTickTime + HawkTime.MINUTE_MILLI_SECONDS){
			this.lastTickTime = curTime;
			//重新加载玩家战场数据
			this.getDataManager().loadToCacheYQZZBattleData();
			if(curTime >= (endShowTime - HawkTime.MINUTE_MILLI_SECONDS * 10)){
				//15分钟后开始发奖，这样保证玩家都回来了
				int termId = this.getDataManager().getStateData().getTermId();
				//是否已经完成结算
				YQZZRecordData recordData = this.getDataManager().loadToCacheYQZZRecordData();
				if(recordData != null){
					return;
				}
				YQZZMatchRoomData roomData = this.getDataManager().getRoomData();
				//战斗是否已经结束
				YQZZGameData gameData = this.getDataManager().loadYQZZGameData(termId, roomData.getRoomId());
				if(gameData.getFinishTime() <= 0){
					return;
				}
				//添加记录
				this.addRecord();
				//发奖
				this.sendCountryAward();
				//this.sendGuildAward();
				this.sendPlayerAward();
			}
		}
	
	}

	@Override
	public void gmOp() {
		long now = HawkTime.getMillisecond();
		//重新加载玩家战场数据
		this.getDataManager().loadToCacheYQZZBattleData();
		//15分钟后开始发奖，这样保证玩家都回来了
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		if(timeCfg.getSeason() > 0){
			int season = timeCfg.getSeason();
			Map<String, YQZZSeasonServer> serverMap = YQZZSeasonServer.loadAll(season);
			Map<String, YQZZMatchRoomData> dataMap = YQZZMatchRoomData.loadAllData(termId);
			YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
			for(YQZZMatchRoomData roomData : dataMap.values()){
				int i = 0;
				for(String serverId : roomData.getServers()){
					i++;
					YQZZSeasonServer server = serverMap.get(serverId);
					if(server == null){
						continue;
					}
					server.setLastRank(i);
					long scoreAdd = (long)((100000-i*10000) * constCfg.getGroupWinpointAdd() / 10000) + constCfg.getGroupRankAdd(i);
					server.addScore(scoreAdd);
					server.saveRedis();
					YQZZRecordData recordData = new YQZZRecordData();
					recordData.setTermId(termId);
					recordData.setServerId(serverId);
					recordData.setRoomId(roomData.getRoomId());
					recordData.setRank(i);
					recordData.setScore(100000-i*10000);
					recordData.setSeasonScore(scoreAdd);
					recordData.setSendAward(0);
					recordData.setTime(now);
					recordData.saveRedis();
				}
			}
		}
		//是否已经完成结算
		YQZZRecordData recordData = this.getDataManager().loadToCacheYQZZRecordData();
		if(recordData == null){
			HawkLog.logPrintln("yqzz whc gm recordData is null");
			return;
		}
		YQZZMatchRoomData localRoomData = this.getDataManager().getRoomData();
		if(localRoomData==null){
			HawkLog.logPrintln("yqzz whc gm localRoomData is null");
			return;
		}
		//战斗是否已经结束
		YQZZGameData gameData = this.getDataManager().loadYQZZGameData(termId, localRoomData.getRoomId());
		if(gameData == null || gameData.getFinishTime() <= 0){
			HawkLog.logPrintln("yqzz whc gm gameData is null");
			return;
		}
		//添加记录
		this.addRecord();
		//发奖
		this.sendCountryAward();
		//this.sendGuildAward();
		this.sendPlayerAward();
	}

	private void addRecord(){
		long curTime = HawkTime.getMillisecond();
		YQZZBattleData battleData = this.getDataManager().getBattleData();
		if(battleData == null){
			return;
		}
		Map<String,YQZZCountryGameData> countrys = battleData.getCountryDatas();
		int termId = this.getDataManager().getStateData().getTermId();
		String serverId =GsConfig.getInstance().getServerId();
		int countryRank = 0;
		long countryRankScore = 0;
		int countryRankAward = 0;
		String roomId = this.getDataManager().getRoomData().getRoomId();
		for(YQZZCountryGameData data : countrys.values()){
			if(data.getServerId().equals(serverId)){
				countryRank = data.getRank();
				countryRankScore = data.getScore();
				break;
			}
		}
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		long scoreAdd = (long)(countryRankScore * constCfg.getGroupWinpointAdd() / 10000) + constCfg.getGroupRankAdd(countryRank);
		//记录一下
		YQZZRecordData recordData = new YQZZRecordData();
		recordData.setTermId(termId);
		recordData.setServerId(serverId);
		recordData.setRoomId(roomId);
		recordData.setRank(countryRank);
		recordData.setScore(countryRankScore);
		recordData.setSeasonScore(scoreAdd);
		recordData.setSendAward(countryRankAward);
		recordData.setTime(curTime);
		recordData.saveRedis();
		//更新最好成绩
		YQZZStatisticsData statisticsData = this.getDataManager().getStatisticsData();
		if(statisticsData.updateMaxRank(countryRank)){
			statisticsData.saveRedis();
		}
	}
	
	
	private void sendCountryAward(){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZBattleData battleData = this.getDataManager().getBattleData();
		if(battleData == null){
			return;
		}
		Map<String,YQZZCountryGameData> countrys = battleData.getCountryDatas();
		String serverId = GsConfig.getInstance().getServerId();
		int countryRank = 0;
		long countryRankScore = 0;
		for(YQZZCountryGameData data : countrys.values()){
			if(data.getServerId().equals(serverId)){
				countryRank = data.getRank();
				countryRankScore = data.getScore();
				break;
			}
		}
		//发全服奖励邮件
		YQZZCountryRankAwardCfg awardCfg = this.getYQZZCountryRankAwardCfg(countryRank);
		if(awardCfg != null){
			long currTime = HawkTime.getMillisecond();
			long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
			SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
					.setMailId(MailId.YQZZ_ACTIVITY_COUNTRY_RANK_REWARD)
					.addContents(serverId, countryRankScore, countryRank)
					.setRewards(awardCfg.getRewardList())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build(), currTime, currTime + experiTime);
			HawkLog.logPrintln("yqzz send rank country reward, serverId: {}, rank: {}, score: {}, cfgId: {}",
					serverId, countryRank, countryRankScore, awardCfg.getId());
			LogUtil.logYQZZCountryRankReward(termId, serverId, countryRankScore, countryRank, awardCfg.getId());
		}
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		if(timeCfg.getSeason() > 0){
			YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(timeCfg.getSeason(), serverId);
			if(seasonServer != null){
				seasonServer.setLastRank(countryRank);
				YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
				long scoreAdd = (long)(countryRankScore * constCfg.getGroupWinpointAdd() / 10000) + constCfg.getGroupRankAdd(countryRank);
				if(timeCfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_GROUP_VALUE){
					seasonServer.addScore(scoreAdd);
				}
				seasonServer.setTotalPoint(seasonServer.getTotalPoint() + countryRankScore);
				checkServerPointReward(seasonServer);
				seasonServer.saveRedis();
			}
		}
	}

	private void checkServerPointReward(YQZZSeasonServer seasonServer){
		ConfigIterator<YQZZSeasonServerAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZSeasonServerAwardCfg.class);
		for(YQZZSeasonServerAwardCfg cfg : iterator) {
			if (cfg.getScore() > seasonServer.getTotalPoint()) {
				continue;
			}
			if (seasonServer.getReward().contains(cfg.getId())) {
				continue;
			}
			seasonServer.getReward().add(cfg.getId());
			seasonServer.saveRedis();
			HawkLog.logPrintln("yqzz send point server reward, playerId:{}, score: {}, cfgId: {}",seasonServer.getServerId(),seasonServer.getTotalPoint(),cfg.getId());
			long currTime = HawkTime.getMillisecond();
			long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
			SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
					.setMailId(MailId.YQZZ_LEAGUE_SERVER_POINT_REWARD)
					.addContents(seasonServer.getTotalPoint())
					.setRewards(cfg.getRewardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build(), currTime, currTime + experiTime);
		}
	}
	
	
	private void sendGuildAward(){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		YQZZBattleData battleData = this.getDataManager().getBattleData();
		if(battleData == null){
			return;
		}
		YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(timeCfg.getSeason(), GsConfig.getInstance().getServerId());
		Map<String,YQZZGuildGameData> guilds = battleData.getGuildDatas();
		//联盟奖励邮件
		for(YQZZGuildGameData data : guilds.values()){
			String guildId = data.getGuildId();
			if(GuildService.getInstance().getGuildInfoObject(guildId) == null){
				continue;
			}
			int guildRank = data.getRank();
			long score = data.getScore();
			YQZZGuildRankAwardCfg guildAwardCfg = this.getYQZZGuildRankAwardCfg(guildRank);
			if(guildAwardCfg == null){
				continue;
			}
			AwardItems award = AwardItems.valueOf();
			award.addItemInfos(guildAwardCfg.getRewardList());
			MailParames.Builder paramesBuilder = MailParames.newBuilder()
					.setMailId(MailId.YQZZ_ACTIVITY_GUILD_RANK_REWARD)
					.addContents(score, guildRank)
					.setRewards(award.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET);
			GuildMailService.getInstance().sendGuildMail(guildId, paramesBuilder);
			HawkLog.logPrintln("yqzz send rank guild reward, guildId:{}, rank: {}, score: {}, cfgId: {}",
					guildId, guildRank, score, guildAwardCfg.getId());
			LogUtil.logYQZZGuildRankReward(termId, guildId, data.getGuildName(), score, guildRank, guildAwardCfg.getId());
			try {
				if(timeCfg.getSeason() > 0){
					YQZZSeasonGuild seasonGuild = YQZZSeasonGuild.loadByGuildId(timeCfg.getSeason(), guildId);
					if(seasonGuild ==null){
						GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
						seasonGuild = new YQZZSeasonGuild();
						seasonGuild.setGuildId(guildId);
						seasonGuild.setServerId(guild.getServerId());
						seasonGuild.setGuildName(guild.getName());
						seasonGuild.setGuildTag(guild.getTag());
						seasonGuild.setGuildFlag(guild.getFlagId());
						seasonGuild.setSeason(timeCfg.getSeason());
					}
					if(timeCfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_KICKOUT_VALUE && seasonServer !=null && seasonServer.isAdvance()){
						seasonGuild.setKickoutPoint(seasonGuild.getKickoutPoint() + score);
					}
					seasonGuild.setTotalPoint(seasonGuild.getTotalPoint() + score);
					checkGuildPointReward(seasonGuild);
					seasonGuild.saveRedis();
				}
			}catch (Exception e){
				HawkException.catchException(e);
			}
		}
//		Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
//		if (!opActivity.isPresent()) {
//			return;
//		}
//		SeasonActivity activity = opActivity.get();
//		for(YQZZGuildGameData data : guilds.values()) {
//			String guildId = data.getGuildId();
//			if (GuildService.getInstance().getGuildInfoObject(guildId) == null) {
//				continue;
//			}
//			int guildRank = data.getRank();
//			activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_YQZZ, guildId, guildRank);
//		}
	}

	private void checkGuildPointReward(YQZZSeasonGuild seasonGuild){
		Collection<String> idList = GuildService.getInstance().getGuildMembers(seasonGuild.getGuildId());
		for (String playerId : idList) {
			HawkLog.logPrintln("yqzz send point guild reward, guildId:{}, score: {}",playerId,seasonGuild.getTotalPoint());
			YQZZSeasonPlayer seasonPlayer = YQZZSeasonPlayer.loadByPlayerId(seasonGuild.getSeason(), playerId);
			if(seasonPlayer==null) {
				seasonPlayer = new YQZZSeasonPlayer();
				seasonPlayer.setPlayerId(playerId);
				seasonPlayer.setSeason(seasonGuild.getSeason());
				seasonPlayer.setServerId(GsConfig.getInstance().getServerId());
			}
			seasonPlayer.saveRedis();
			ConfigIterator<YQZZSeasonGuildAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZSeasonGuildAwardCfg.class);
			for(YQZZSeasonGuildAwardCfg cfg : iterator){
				if(cfg.getScore() > seasonGuild.getTotalPoint() || cfg.getRewardItems().isEmpty()){
					continue;
				}
				if(seasonPlayer.getGuildReward().contains(cfg.getId())){
					continue;
				}
				seasonPlayer.getGuildReward().add(cfg.getId());
				seasonPlayer.saveRedis();
				HawkLog.logPrintln("yqzz send point guild reward, guildId:{}, score: {}, cfgId: {}",playerId,seasonGuild.getTotalPoint(),cfg.getId());
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(MailId.YQZZ_LEAGUE_GUILD_POINT_REWARD)
						.addContents(seasonGuild.getTotalPoint())
						.setRewards(cfg.getRewardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
			seasonPlayer.saveRedis();
		}
	}
	
	private void sendPlayerAward(){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		YQZZBattleData battleData = this.getDataManager().getBattleData();
		if(battleData == null){
			return;
		}
		Map<String,YQZZPlayerGameData> players = battleData.getPlayerDatas();
		Map<String,YQZZCountryGameData> countrys = battleData.getCountryDatas();
		//玩家奖励邮件
		for(YQZZPlayerGameData data : players.values()){
			String playerServer = data.getServerId();
			String playerId = data.getPlayerId();
			long score = data.getScore();
			if(!GlobalData.getInstance().isLocalServer(playerServer)){
				continue;
			}
			Player player =GlobalData.getInstance().makesurePlayer(playerId);
			if(player == null){
				continue;
			}
			//发放排行奖励
			int countryRank = 0;
			long countryRankScore = 0;
			for(YQZZCountryGameData cdata : countrys.values()){
				if(cdata.getServerId().equals(playerServer)){
					countryRank = cdata.getRank();
					countryRankScore = cdata.getScore();
					break;
				}
			}
			double rate = HawkConfigManager.getInstance().getKVInstance(YQZZBattleCfg.class).playerRankRate(countryRank);
			int rank = data.getRank();
			YQZZPlayerRankAwardCfg cfg = this.getYQZZPlayerRankAwardCfg(rank);
			if(cfg != null && !cfg.getRewardList().isEmpty()){
				List<ItemInfo> rewardList = cfg.getRewardList();
				rewardList.forEach(item -> item.setCount((long) (item.getCount() * rate)));
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(MailId.YQZZ_ACTIVITY_PLAYER_RANK_REWARD)
						.addContents(score, rank,countryRank,rate)
						.setRewards(rewardList)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
				HawkLog.logPrintln("yqzz send rank player reward, playerId:{}, rank: {}, score: {}, cfgId: {}",
						playerId, rank, score, cfg.getId());
				LogUtil.logYQZZPlayerRankReward(player, termId, score, rank, cfg.getId());
			}
			//添加军功
			player.increaseNationMilitary((int)score, PlayerAttr.NATION_MILITARY_VALUE, Action.YQZZ_NATION_MILITARY, true);
			if(timeCfg.getSeason() > 0){
				ActivityManager.getInstance().postEvent(new YQZZScoreEvent(playerId, score,HawkTime.getMillisecond(),true));
				try {
					YQZZSeasonPlayer seasonPlayer = YQZZSeasonPlayer.loadByPlayerId(timeCfg.getSeason(), playerId);
					if(seasonPlayer == null){
						seasonPlayer = new YQZZSeasonPlayer();
						seasonPlayer.setPlayerId(playerId);
						seasonPlayer.setServerId(GsConfig.getInstance().getServerId());
						seasonPlayer.setSeason(timeCfg.getSeason());
					}
					seasonPlayer.setTotalPoint(seasonPlayer.getTotalPoint() + score);
					checkPlayerPointReward(seasonPlayer);
					seasonPlayer.saveRedis();
				}catch (Exception e){
					HawkException.catchException(e);
				}
			}
		}
	}

	private void checkPlayerPointReward(YQZZSeasonPlayer seasonPlayer){
		ConfigIterator<YQZZSeasonPersonAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZSeasonPersonAwardCfg.class);
		for(YQZZSeasonPersonAwardCfg cfg : iterator){
			if(cfg.getScore() > seasonPlayer.getTotalPoint()){
				continue;
			}
			if(seasonPlayer.getReward().contains(cfg.getId())){
				continue;
			}
			seasonPlayer.getReward().add(cfg.getId());
			seasonPlayer.saveRedis();
			HawkLog.logPrintln("yqzz send point player reward, playerId:{}, score: {}, cfgId: {}",seasonPlayer.getPlayerId(),seasonPlayer.getTotalPoint(),cfg.getId());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(seasonPlayer.getPlayerId())
					.setMailId(MailId.YQZZ_LEAGUE_PLAYER_POINT_REWARD)
					.addContents(seasonPlayer.getTotalPoint())
					.setRewards(cfg.getRewardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
	}
	
	
	private YQZZCountryRankAwardCfg getYQZZCountryRankAwardCfg(int rank){
		List<YQZZCountryRankAwardCfg> list = HawkConfigManager.getInstance()
				.getConfigIterator(YQZZCountryRankAwardCfg.class).toList();
		for(YQZZCountryRankAwardCfg cfg : list){
			if(cfg.getRankUpper() <= rank &&
					rank <= cfg.getRankLower()){
				return cfg;
			}
		}
		return null;
	}
	private YQZZGuildRankAwardCfg getYQZZGuildRankAwardCfg(int rank){
		List<YQZZGuildRankAwardCfg> list = HawkConfigManager.getInstance()
				.getConfigIterator(YQZZGuildRankAwardCfg.class).toList();
		for(YQZZGuildRankAwardCfg cfg : list){
			if(cfg.getRankUpper() <= rank &&
					rank <= cfg.getRankLower()){
				return cfg;
			}
		}
		return null;
	}
	
	private YQZZPlayerRankAwardCfg getYQZZPlayerRankAwardCfg(int rank){
		List<YQZZPlayerRankAwardCfg> list = HawkConfigManager.getInstance()
				.getConfigIterator(YQZZPlayerRankAwardCfg.class).toList();
		for(YQZZPlayerRankAwardCfg cfg : list){
			if(cfg.getRankUpper() <= rank &&
					rank <= cfg.getRankLower()){
				return cfg;
			}
		}
		return null;
	}
}
