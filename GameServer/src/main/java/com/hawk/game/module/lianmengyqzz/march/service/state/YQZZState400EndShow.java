package com.hawk.game.module.lianmengyqzz.march.service.state;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZSeasonGuildRankAwardCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZSeasonServerRankAwardCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZWarConstCfg;
import com.hawk.game.module.lianmengyqzz.march.data.global.*;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.model.President;
import com.hawk.game.protocol.*;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import java.util.*;

public class YQZZState400EndShow  extends IYQZZServiceState {
	private long lastTickTime =0;
	
	public YQZZState400EndShow(YQZZMatchService parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		this.getDataManager().getStateData().setState(YQZZActivityState.END_SHOW);
		this.getDataManager().getStateData().saveRedis();
		if(GsConfig.getInstance().isDebug()){
			gmAddScore();
		}
	}

	@Override
	public void tick() {
		YQZZActivityStateData data = this.calcInfo();
		//如果不在当前状态，则往下个状态推进
		YQZZActivityStateData curData = this.getDataManager().getStateData();
		if(curData.getTermId() != data.getTermId()
				|| curData.getState() != data.getState()){
			this.getParent().updateState(YQZZActivityState.HIDDEN);
		}
		long curTime= HawkTime.getMillisecond();
		if(curTime > this.lastTickTime + HawkTime.MINUTE_MILLI_SECONDS) {
			this.lastTickTime = curTime;
			int termId = this.getDataManager().getStateData().getTermId();
			YQZZKickoutData kickoutData = YQZZKickoutData.loadData(termId);
			boolean kickoutFinish = kickoutData != null && kickoutData.kickoutFinish();
			if(!kickoutFinish){
				YQZZKickoutLock kickoutLock = this.getDataManager().createYQZZKickoutLock(60*60*24*365);
				boolean achieve = kickoutLock.achieveKickLockWithExpireTime();
				if(achieve){
					this.seasonKickout();
				}
			}else {
				sendSeasonAward();
				sendSeasonMail();
			}

		}
	}

	private void gmAddScore(){
		long now = HawkTime.getMillisecond();
		//15分钟后开始发奖，这样保证玩家都回来了
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		if(timeCfg.getSeason() > 0){
			Map<String, Integer> pointMap = new HashMap<>();
			// 读文件
			List<String> infos = new ArrayList<>();
			try {
				HawkOSOperator.readTextFileLines("tmp/yqzz_gm_server_info.txt", infos);
				if(infos.size() > 0){
					for (String info : infos) {
						String serverId = info.split(",")[0];
						int point = Integer.parseInt(info.split(",")[3]);
						pointMap.put(serverId, point);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
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
					int point = pointMap.getOrDefault(serverId, 0);
					server.setLastRank(i);
					long scoreAdd = (long)(point * constCfg.getGroupWinpointAdd() / 10000) + constCfg.getGroupRankAdd(i);
					if(timeCfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_GROUP_VALUE){
						server.addScore(scoreAdd);
					}
					server.setTotalPoint(server.getTotalPoint() + point);
					server.saveRedis();
					YQZZRecordData recordData = new YQZZRecordData();
					recordData.setTermId(termId);
					recordData.setServerId(serverId);
					recordData.setRoomId(roomData.getRoomId());
					recordData.setRank(i);
					recordData.setScore(point);
					if(timeCfg.getType() == YQZZWar.PBYQZZWarType.YQZZ_GROUP_VALUE){
						recordData.setSeasonScore(scoreAdd);
					}
					recordData.setSendAward(0);
					recordData.setTime(now);
					recordData.saveRedis();
				}
			}
		}
	}

	@Override
	public void gmOp() {
		YQZZKickoutLock kickoutLock = this.getDataManager().createYQZZKickoutLock(60*60*24*365);
		boolean achieve = kickoutLock.achieveKickLockWithExpireTime();
		if(achieve){
			this.seasonKickout();
		}
	}

	private void seasonKickout(){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		if(timeCfg.getSeason() <= 0){
			return;
		}
		int season = timeCfg.getSeason();
		int type = timeCfg.getType();
		Map<String, YQZZSeasonServer> serverMap = YQZZSeasonServer.loadAll(season);
		Map<String, YQZZMatchRoomData> dataMap = YQZZMatchRoomData.loadAllData(termId);
		HawkTuple2<Integer, Integer> turnTuple2 = getSeasonMaxTurn(season);
		YQZZWar.PBYQZZWarType warType = YQZZWar.PBYQZZWarType.valueOf(type);
		switch (warType){
			case YQZZ_NOT_SEASON:{
			}
			break;
			case YQZZ_GROUP:{
				List<YQZZSeasonServer> serverList = new ArrayList<>();
				serverList.addAll(serverMap.values());
				Collections.sort(serverList,new Comparator<YQZZSeasonServer>(){
					@Override
					public int compare(YQZZSeasonServer o1, YQZZSeasonServer o2) {
						if(o1.getScore() != o2.getScore()){
							return o1.getScore() > o2.getScore() ? -1 : 1;
						}
						if(o1.getTotalPoint() != o2.getTotalPoint()){
							return o1.getTotalPoint() > o2.getTotalPoint() ? -1 : 1;
						}
						if(o1.getPower() != o2.getPower()){
							return o1.getPower() > o2.getPower() ? -1 : 1;
						}
						return 0;
					}
				});
				int rank = 1;
				for(YQZZSeasonServer server : serverList){
					YQZZSeasonServer.updateGroupRank(season, server.getServerId(), rank);
					rank++;
				}
				if(timeCfg.getTurn() >= turnTuple2.first){
					YQZZWarConstCfg constConfig = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
					if(GsConfig.getInstance().isDebug() && constConfig.isGm()){
						// 读文件
						List<String> infos = new ArrayList<>();
						try {
							HawkOSOperator.readTextFileLines("tmp/yqzz_gm_server_info.txt", infos);
						} catch (Exception e) {
							HawkException.catchException(e);
						}
						if(infos.size() > 0){
							Map<String,Integer> rankMap = new HashMap<>();
							for (String info : infos) {
								String serverId = info.split(",")[0];
								int debugRank = Integer.parseInt(info.split(",")[2]);
								rankMap.put(serverId, debugRank);
							}
							int needCount = 6 * turnTuple2.second;
							for(YQZZSeasonServer server : serverList){
								int debugRank = rankMap.getOrDefault(server.getServerId(), 0);
								if(debugRank != 0 && debugRank <= needCount){
									server.setAdvance(true);
									server.saveRedis();
								}
								if(debugRank != 0){
									server.setGroupRank(debugRank);
									server.saveRedis();
									if(debugRank > needCount){
										YQZZSeasonServer.updateFinalRank(season, server.getServerId(), debugRank);
									}
								}
								YQZZSeasonServer.updateGroupRank(season, server.getServerId(), debugRank);
							}
						}
					}else {
						int needCount = 6 * turnTuple2.second;
						needCount = Math.min(needCount, serverList.size());
						for(int i = 0; i < needCount; i++){
							YQZZSeasonServer server = serverList.get(i);
							server.setAdvance(true);
							server.setGroupRank(i+1);
							server.saveRedis();
						}
						int finalRank = needCount + 1;
						for(int i = needCount; i < serverList.size(); i++){
							YQZZSeasonServer server = serverList.get(i);
							server.setGroupRank(finalRank);
							server.saveRedis();
							YQZZSeasonServer.updateFinalRank(season, server.getServerId(), finalRank);
							finalRank++;
						}
					}
				}
			}
			break;
			case YQZZ_KICKOUT:{
				if(timeCfg.getTurn() < turnTuple2.second){
					int needCount = 6 * (turnTuple2.second - timeCfg.getTurn());
					int group = turnTuple2.second - timeCfg.getTurn() + 1;
					int remainder = needCount % group;
					int advanceRank = needCount / group;
					List<YQZZSeasonServer> serverList = new ArrayList<>();
					serverList.addAll(serverMap.values());
					List<YQZZSeasonServer> advanceServerList = new ArrayList<>();
					List<YQZZSeasonServer> kickoutServerList = new ArrayList<>();
					for(YQZZSeasonServer server : serverList){
						if(!server.isAdvance()){
							continue;
						}
						if(server.getLastRank() > advanceRank){
							kickoutServerList.add(server);
						}else {
							advanceServerList.add(server);
						}
					}
					Collections.sort(kickoutServerList,new Comparator<YQZZSeasonServer>(){
						@Override
						public int compare(YQZZSeasonServer o1, YQZZSeasonServer o2) {
							if(o1.getLastRank() != o2.getLastRank()){
								return o1.getLastRank() < o2.getLastRank() ? -1 : 1;
							}
							YQZZRecordData data1= YQZZRecordData.loadData(o1.getServerId(), termId);
							YQZZRecordData data2= YQZZRecordData.loadData(o2.getServerId(), termId);
							long score1 = 0;
							long score2 = 0;
							if(data1 != null){
								score1 = data1.getScore();
							}
							if(data2 != null){
								score2 = data2.getScore();
							}
							if(score1 != score2){
								return score1 > score2 ? -1 : 1;
							}
							if(o1.getPower() != o2.getPower()){
								return o1.getPower() > o2.getPower() ? -1 : 1;
							}
							return 0;
						}
					});
					int i = 0;
					int rank = needCount + 1;
					for(YQZZSeasonServer server : kickoutServerList){
						if(i >= remainder){
							server.setAdvance(false);
							server.setKickoutRank(rank);
							server.saveRedis();
							YQZZSeasonServer.updateFinalRank(season, server.getServerId(), rank);
							rank++;
						}else {
							YQZZRecordData data = YQZZRecordData.loadData(server.getServerId(), termId);
							if(data != null){
								data.setAdvance(true);
								data.saveRedis();
							}
						}
						i++;
					}
					for(YQZZSeasonServer server : advanceServerList){
						YQZZRecordData data = YQZZRecordData.loadData(server.getServerId(), termId);
						if(data != null){
							data.setAdvance(true);
							data.saveRedis();
						}
					}
				}else if(timeCfg.getTurn() == turnTuple2.second){
					List<YQZZSeasonServer> serverList = new ArrayList<>();
					serverList.addAll(serverMap.values());
					for(YQZZSeasonServer server : serverList){
						if(!server.isAdvance()){
							continue;
						}
						server.setAdvance(false);
						server.setKickoutRank(server.getLastRank());
						server.saveRedis();
						YQZZSeasonServer.updateFinalRank(season, server.getServerId(), server.getLastRank());
					}
					YQZZSeasonTitle.delAll();
				}
			}
			break;
			default:{
			}
		}
		String matchServer = GsConfig.getInstance().getServerId();
		YQZZKickoutData data = new YQZZKickoutData(termId, matchServer, HawkTime.getMillisecond());
		data.saveRedis();
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


	private void sendSeasonMail(){
		try {
			int termId = this.getDataManager().getStateData().getTermId();
			YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
			if(timeCfg.getSeason() <= 0){
				return;
			}
			int season = timeCfg.getSeason();
			int type = timeCfg.getType();
			HawkTuple2<Integer, Integer> turnTuple2 = getSeasonMaxTurn(season);
			YQZZWar.PBYQZZWarType warType = YQZZWar.PBYQZZWarType.valueOf(type);
			switch (warType) {
				case YQZZ_NOT_SEASON: {
				}
				break;
				case YQZZ_GROUP: {
					if(timeCfg.getTurn() == turnTuple2.first){
						String spKey = "YQZZ:SEASON_GROUP_END:" + season + ":" + GsConfig.getInstance().getServerId();
						String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(spKey);
						if (StringUtils.isNotEmpty(val)){
							return;
						}
						ActivityGlobalRedis.getInstance().getRedisSession().setString(spKey, spKey);
						YQZZSeasonServer server = YQZZSeasonServer.loadByServerId(season, GsConfig.getInstance().getServerId());
						if(server != null && server.getScore() != 0){
							if(server.isAdvance()){
								long currTime = HawkTime.getMillisecond();
								long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
								SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
										.setMailId(MailConst.MailId.YQZZ_LEAGUE_GROUP_ADVANCE)
										.addContents(server.getScore(), server.getGroupRank())
										.build(), currTime, currTime + experiTime);
							}else {
								long currTime = HawkTime.getMillisecond();
								long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
								SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
										.setMailId(MailConst.MailId.YQZZ_LEAGUE_GROUP_KICKOUT)
										.addContents(server.getScore(), server.getGroupRank())
										.build(), currTime, currTime + experiTime);
							}
						}
					}
				}
				break;
				case YQZZ_KICKOUT:{
					if(timeCfg.getTurn() == turnTuple2.second){
						String spKey = "YQZZ:SEASON_KICKOUT_END:" + season + ":" + GsConfig.getInstance().getServerId();
						String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(spKey);
						if (StringUtils.isNotEmpty(val)){
							return;
						}
						ActivityGlobalRedis.getInstance().getRedisSession().setString(spKey, spKey);
						YQZZSeasonServer [] topServer = new YQZZSeasonServer[3];
						Map<String, YQZZSeasonServer> serverMap = YQZZSeasonServer.loadAll(season);
						for(YQZZSeasonServer seasonServer : serverMap.values()){

							if(seasonServer.getKickoutRank() <=0 || seasonServer.getKickoutRank()>topServer.length){
								continue;
							}
							topServer[seasonServer.getKickoutRank() - 1] = seasonServer;
						}
						List<Object> serverNames = new ArrayList<>();
						for(YQZZSeasonServer seasonServer : topServer){
							if(seasonServer == null){
								continue;
							}
							ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(seasonServer.getServerId());
							serverNames.add(seasonServer.getServerId());
							serverNames.add(serverInfo== null ? "" : serverInfo.getName());
						}
						long currTime = HawkTime.getMillisecond();
						long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
						MailParames.Builder mailParames = MailParames.newBuilder();
						mailParames.setMailId(MailConst.MailId.YQZZ_LEAGUE_TOP_SHOW);
						for(YQZZSeasonServer seasonServer : topServer){
							if(seasonServer == null){
								continue;
							}
							mailParames.addContents(seasonServer.getServerId());
						}
						SystemMailService.getInstance().addGlobalMail(mailParames.build(), currTime, currTime + experiTime);
					}
				}
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	private void sendSeasonAward(){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		int type = timeCfg.getType();
		int turn = timeCfg.getTurn();
		if(season <= 0){
			return;
		}
		if(type != YQZZWar.PBYQZZWarType.YQZZ_KICKOUT_VALUE){
			return;
		}
		HawkTuple2<Integer, Integer> turnTuple2 = getSeasonMaxTurn(season);
		if(turn != turnTuple2.second){
			return;
		}
		String spKey = "YQZZ:SEASON_AWARD:" + season + ":" + GsConfig.getInstance().getServerId();
		String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(spKey);
		if (StringUtils.isNotEmpty(val)){
			return;
		}
		ActivityGlobalRedis.getInstance().getRedisSession().setString(spKey, spKey);
		confirmSeasonGiftSender(season);
		sendSeasonServerAward(season);
		//sendSeasonGuildAward(season);
		addSeasonGuildGradeExp(season);

	}

	private void sendSeasonServerAward(int season){
		YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(season, GsConfig.getInstance().getServerId());
		if(seasonServer == null){
			return;
		}
		if(seasonServer.isSeasonAward()){
			return;
		}
		int rank = 0;
		if(seasonServer.getKickoutRank() > 0){
			rank = seasonServer.getKickoutRank();
		}else{
			rank = seasonServer.getGroupRank();
		}
		if(rank <= 0){
			return;
		}
		YQZZSeasonServerRankAwardCfg cfg = getSeasonServerRankAwardCfg(rank);
		if(cfg == null){
			return;
		}
		long currTime = HawkTime.getMillisecond();
		long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
		String serverId = seasonServer.getServerId();
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				.setMailId(MailConst.MailId.YQZZ_LEAGUE_SERVER_RANK)
				.addContents(rank, seasonServer.getLeaderName())
				.setRewards(ItemInfo.valueListOf(cfg.getAward()))
				.setAwardStatus(Const.MailRewardStatus.NOT_GET)
				.build(), currTime, currTime + experiTime);
		HawkLog.logPrintln("yqzz send season rank country reward, serverId: {}, rank: {}, cfgId: {}",
				seasonServer, rank, cfg.getId());
		seasonServer.setSeasonAward(true);
		seasonServer.saveRedis();
		YQZZSeasonTitle title = new YQZZSeasonTitle();
		title.setServerId(GsConfig.getInstance().getServerId());
		title.setRank(rank);
		title.setNationHonor(cfg.getNationHonor());
		title.saveRedis();
	}

	private void sendSeasonGuildAward(int season){
		List<YQZZSeasonGuild> guilds = YQZZSeasonGuild.getRankList(season);
		int rank = 0;
		for(YQZZSeasonGuild guild : guilds){
			rank++;
			String guildId = guild.getGuildId();
			if(GuildService.getInstance().getGuildInfoObject(guildId) == null){
				continue;
			}
			YQZZSeasonGuildRankAwardCfg cfg = getSeasonGuildRankAwardCfg(rank);
			if(cfg != null && !guild.isSeasonAward()){
				AwardItems award = AwardItems.valueOf();
				award.addItemInfos(ItemInfo.valueListOf(cfg.getAward()));
				MailParames.Builder paramesBuilder = MailParames.newBuilder()
						.setMailId(MailConst.MailId.YQZZ_LEAGUE_GUILD_RANK)
						.addContents(guild.getKickoutPoint(), rank)
						.setRewards(award.getAwardItems())
						.setAwardStatus(Const.MailRewardStatus.NOT_GET);
				GuildMailService.getInstance().sendGuildMail(guildId, paramesBuilder);
				HawkLog.logPrintln("yqzz send season rank guild reward, guildId:{}, rank: {}, cfgId: {}",
						guildId, rank, cfg.getId());
				guild.setSeasonAward(true);
				guild.saveRedis();
			}
		}
	}

	private void addSeasonGuildGradeExp(int season){
		YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(season, GsConfig.getInstance().getServerId());
		if(seasonServer == null){
			return;
		}
		int rank = 0;
		if(seasonServer.getKickoutRank() > 0){
			rank = seasonServer.getKickoutRank();
		}else{
			rank = seasonServer.getGroupRank();
		}
		if(rank <= 0){
			return;
		}
		Set<String> guildIds = new HashSet<>();
		ConfigIterator<YQZZTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZTimeCfg.class);
		for (YQZZTimeCfg cfg : iterator){
			if(cfg.getSeason() != season){
				continue;
			}
			if(cfg.getType() != YQZZWar.PBYQZZWarType.YQZZ_KICKOUT_VALUE){
				continue;
			}
			String serverId = GsConfig.getInstance().getServerId();
			List<String> serverList = GlobalData.getInstance().getMergeServerList(serverId);
			if(serverList == null || serverList.isEmpty()){
				serverList = new ArrayList<>();
				serverList.add(serverId);
			}
			Map<String,YQZZJoinServer> map = YQZZJoinServer.loadAll(cfg.getTermId(), serverList);
			for(YQZZJoinServer joinServer : map.values()){
				if(joinServer == null){
					continue;
				}
				guildIds.addAll(joinServer.getJoinGuilds());
			}
		}
		for(String guildId : guildIds){
			if(GuildService.getInstance().getGuildInfoObject(guildId) == null){
				continue;
			}
			try {
				Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
				if (opActivity.isPresent()) {
					SeasonActivity activity = opActivity.get();
					activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_YQZZ, guildId, rank);
				}
			}catch (Exception e){
				HawkException.catchException(e);
			}
		}
	}

	private YQZZSeasonServerRankAwardCfg getSeasonServerRankAwardCfg(int rank){
		ConfigIterator<YQZZSeasonServerRankAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZSeasonServerRankAwardCfg.class);
		for (YQZZSeasonServerRankAwardCfg cfg : iterator){
			if(rank >= cfg.getMin() && rank <= cfg.getMax()){
				return cfg;
			}
		}
		return null;
	}

	private YQZZSeasonGuildRankAwardCfg getSeasonGuildRankAwardCfg(int rank){
		ConfigIterator<YQZZSeasonGuildRankAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(YQZZSeasonGuildRankAwardCfg.class);
		for (YQZZSeasonGuildRankAwardCfg cfg : iterator){
			if(rank >= cfg.getMin() && rank <= cfg.getMax()){
				return cfg;
			}
		}
		return null;
	}

	private void confirmSeasonGiftSender(int season){
		YQZZSeasonServer seasonServer = YQZZSeasonServer.loadByServerId(season, GsConfig.getInstance().getServerId());
		if(seasonServer == null){
			return;
		}
		fillSendGiftPlayerInfo(seasonServer);
		seasonServer.saveRedis();
	}

	private String getSendGiftPlayerId(){
		// 司令
		President president = PresidentFightService.getInstance().getPresidentCity().getPresident();
		if(president == null || HawkOSOperator.isEmptyString(president.getPlayerId())){
			List<Rank.RankInfo> rankList = RankService.getInstance().getRankCache(Rank.RankType.ALLIANCE_FIGHT_KEY);
			if(rankList.isEmpty()){
				return "";
			}
			Rank.RankInfo rankInfo = rankList.get(0);
			String guildId = rankInfo.getId();
			GuildInfoObject obj = GuildService.getInstance().getGuildInfoObject(guildId);
			if(obj == null){
				return "";
			}
			return obj.getLeaderId() == null ? "" : obj.getLeaderId();
		}
		return president.getPlayerId();
	}

	private void fillSendGiftPlayerInfo(YQZZSeasonServer server){
		// 司令
		String leaderId = "";
		String leaderName = "";
		President president = PresidentFightService.getInstance().getPresidentCity().getPresident();
		if(president == null || HawkOSOperator.isEmptyString(president.getPlayerId())){
			List<Rank.RankInfo> rankList = RankService.getInstance().getRankCache(Rank.RankType.ALLIANCE_FIGHT_KEY);
			if(!rankList.isEmpty()){
				Rank.RankInfo rankInfo = rankList.get(0);
				String guildId = rankInfo.getId();
				GuildInfoObject obj = GuildService.getInstance().getGuildInfoObject(guildId);
				if(obj != null){
					leaderId = obj.getLeaderId() == null ? "" : obj.getLeaderId();
					leaderName = obj.getLeaderName() == null ? "" : obj.getLeaderName();
				}
			}
		}else {
			leaderId = president.getPlayerId() == null ? "" : president.getPlayerId();
			leaderName = president.getPlayerName() == null ? "" : president.getPlayerName();
		}
		server.setSenderId(leaderId);
		server.setLeaderName(leaderName);
	}
}
