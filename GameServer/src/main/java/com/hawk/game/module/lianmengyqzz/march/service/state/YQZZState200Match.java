package com.hawk.game.module.lianmengyqzz.march.service.state;

import java.util.*;
import java.util.stream.Collectors;

import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.game.module.lianmengyqzz.march.data.global.*;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.protocol.YQZZ;
import com.hawk.game.protocol.YQZZWar;
import com.hawk.game.util.LogUtil;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.GsConfig;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityJoinState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;

public class YQZZState200Match extends IYQZZServiceState {
	
	private long lastTickTime =0;
	
	public YQZZState200Match(YQZZMatchService parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		this.getDataManager().getStateData().setState(YQZZActivityState.MATCH);
		this.getDataManager().getStateData().saveRedis();
	}

	@Override
	public void tick() {
		YQZZActivityStateData data = this.calcInfo();
		//如果不在当前状态，则进入战斗状态
		YQZZActivityStateData curData = this.getDataManager().getStateData();
		if(curData.getTermId() != data.getTermId()
				|| curData.getState() != data.getState()){
			this.getParent().updateState(YQZZActivityState.BATTLE);
			return;
		}
		//如果不参与战斗
		YQZZActivityJoinState joinState = this.getDataManager()
				.getStateData().getJoinGame();
		if(joinState == YQZZActivityJoinState.OUT){
			return;
		}
		//10S 检查一次匹配
		long curTime= HawkTime.getMillisecond();
		if(curTime > this.lastTickTime + HawkTime.MINUTE_MILLI_SECONDS){
			this.lastTickTime = curTime;
			int curTerm = this.getDataManager().getStateData().getTermId();
			//加载匹配数据
			YQZZMatchData matchData = this.getDataManager().loadYQZZMatchData();
			boolean matchFinish = matchData!= null && matchData.matchFinish();
			if(!matchFinish){
				YQZZMatchLock matchLock = this.getDataManager().createYQZZMatchLock(60 * 5);
				boolean achieve = matchLock.achieveMatchLockWithExpireTime();
				if(achieve){
					this.doMatch();
				}
			}else{
				YQZZMatchRoomData roomData =this.getDataManager().getRoomData();
				if(roomData == null || roomData.getTermId() != curTerm){
					//加载房间数据
					this.getDataManager().loadToCacheRoomDdata();
					//加载参赛服数据
					this.getDataManager().loadToCacheYQZZJoinServerDataForRoom();
					//联盟参与数据
					this.getDataManager().loadToCacheYQZZJoinGuildDataForRoom();
					//发送邮件
					this.sendMatchMail();
				}
			}
		}
	}

	@Override
	public void gmOp() {
		for(int i=0; i<10; i++){
			int curTerm = this.getDataManager().getStateData().getTermId();
			//加载匹配数据
			YQZZMatchData matchData = this.getDataManager().loadYQZZMatchData();
			boolean matchFinish = matchData!= null && matchData.matchFinish();
			if(!matchFinish){
				YQZZMatchLock matchLock = this.getDataManager().createYQZZMatchLock(60 * 5);
				boolean achieve = matchLock.achieveMatchLockWithExpireTime();
				if(achieve){
					this.doMatch();
				}
			}else{
				YQZZMatchRoomData roomData =this.getDataManager().getRoomData();
				if(roomData == null || roomData.getTermId() != curTerm){
					//加载房间数据
					this.getDataManager().loadToCacheRoomDdata();
					//加载参赛服数据
					this.getDataManager().loadToCacheYQZZJoinServerDataForRoom();
					//联盟参与数据
					this.getDataManager().loadToCacheYQZZJoinGuildDataForRoom();
					//发送邮件
					this.sendMatchMail();
				}
			}
		}
	}

	/**
	 * 发送邮件
	 */
	private void sendMatchMail(){
		YQZZMatchRoomData room = this.getDataManager().getRoomData();
		if(room == null){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		List<String> joinServers = room.getServers();
		Map<String, YQZZJoinGuild> JoinGuilds = this.getDataManager()
				.getRoomGuildsByServer(serverId);
		long currTime = HawkTime.getMillisecond();
		long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
		MailParames.Builder mailParames = MailParames.newBuilder();
		mailParames.setMailId(MailId.YQZZ_ACTIVITY_MATCH_RLT);
		mailParames.addContents(joinServers.size()-1);
		for(String sId:joinServers){
			if(sId.equals(serverId)){
				continue;
			}
			mailParames.addContents(sId);
		}
		mailParames.addContents(JoinGuilds.size());
		for(YQZZJoinGuild guild : JoinGuilds.values()){
			mailParames.addContents(guild.getGuildTag());
			mailParames.addContents(guild.getGuildName());
		}
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZJoinServer joinServer = YQZZJoinServer.load(termId, serverId);
		mailParames.addContents(joinServer.getLeaderName() == null ? "" : joinServer.getLeaderName());
		SystemMailService.getInstance().addGlobalMail(mailParames.build(), currTime, currTime + experiTime);
	}


	private void doMatch(){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		if(cfg.getSeason() > 0){
			doSeasonMatch();
		}else {
			doNormalMatch();
		}
	}

	//匹配
	private void doNormalMatch(){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		
		String matchServer = GsConfig.getInstance().getServerId();
		long curTime = HawkTime.getMillisecond();
		Map<String, YQZZJoinServer> serverMap = this.getDataManager().loadAllYQZZJoinServerData();
		List<YQZZJoinServer> serverList = new ArrayList<>();
		serverList.addAll(serverMap.values());
		//排序
		Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
			@Override
			public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
				if(o1.getOpenDayW() != o2.getOpenDayW()){
					return o1.getOpenDayW() > o2.getOpenDayW()?-1 :1;
				}
				if(o1.getPower() != o2.getPower()){
					return o1.getPower() > o2.getPower()?-1 :1;
				}
				return 0;
			}
		});
		Map<String,YQZZMatchRoomData> rooms = new HashMap<String,YQZZMatchRoomData>();
		int serverCount = serverList.size();
		int perCount = timeCfg.getMatchNeedCount();
		int poolSize = timeCfg.getMatchListCount();
		int p1 = serverCount / perCount;
		int p2 = serverCount % perCount;
		int roomCount = p1;
		int lessIndex = serverCount + 1;
		if(p2 > 0){
			//最后一个房间元素不够，最后一个房间的元素个数是  perCount -1
			roomCount += 1;
			int p3 = perCount -1;
			//共有lessCount个房间少一个元素
			int lessCount = p3 - p2 + 1;
			//从lessIndex 开始每个房间少一个元素
			lessIndex = roomCount - lessCount + 1;
		}
		
		for(int i = 1;i<=roomCount;i++){
			int getCount = perCount;
			if(i >= lessIndex){
				getCount = perCount -1;
			}
			//没有了 ,结束循环
			if(serverList.size() <= 0){
				break;
			}
			//开始拿去
			List<YQZZJoinServer> roomServers = this.getMatchRoomServers(serverList, poolSize, getCount);
			if(roomServers.isEmpty()){
				continue;
			}
			//排序
			Collections.sort(roomServers,new Comparator<YQZZJoinServer>() {
				@Override
				public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
					if(o1.getPower() != o2.getPower()){
						return o1.getPower() > o2.getPower()?-1 :1;
					}
					return 0;
				}
			});
			String roomServerId = this.getRoomServer(roomServers);
			YQZZMatchRoomData room = new YQZZMatchRoomData();
			room.setTermId(termId);
			room.setRoomId(HawkUUIDGenerator.genUUID());
			room.setRoomServerId(roomServerId);
			Set<String> joinServerSet = new HashSet<>();
			for(YQZZJoinServer roomServer : roomServers){
				room.addServer(roomServer.getServerId());
				joinServerSet.add(roomServer.getServerId());
			}
			rooms.put(room.getRoomId(), room);
			try {
				String joinServerStr = SerializeHelper.collectionToString(joinServerSet,SerializeHelper.ATTRIBUTE_SPLIT);
				for(YQZZJoinServer roomServer : roomServers){
					LogUtil.logYQZZMatch(termId, roomServer.getServerId(),roomServer.getPower(), joinServerStr, roomServerId);
				}
			}catch (Exception e){
				HawkException.catchException(e);
			}
		}
		//保存房间数据
		YQZZMatchRoomData.saveAll(termId, rooms);
		//保存匹配数据
		YQZZMatchData data = new YQZZMatchData(termId, matchServer, curTime);
		data.saveRedis();
	}


	private void doSeasonMatch(){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int type = timeCfg.getType();
		YQZZWar.PBYQZZWarType warType = YQZZWar.PBYQZZWarType.valueOf(type);
		switch (warType){
			case YQZZ_NOT_SEASON:{
			}
			break;
			case YQZZ_GROUP:{
				doSeasonGroupMatch();
			}
			break;
			case YQZZ_KICKOUT:{
				doSeasonKickoutMatch();
			}
			break;
			default:{
			}
		}
	}


	private void doSeasonGroupMatch(){
		int termId = this.getDataManager().getStateData().getTermId();
		String matchServer = GsConfig.getInstance().getServerId();
		long curTime = HawkTime.getMillisecond();
		Map<String, YQZZJoinServer> serverMap = this.getDataManager().loadAllYQZZJoinServerData();
		List<YQZZJoinServer> serverList = new ArrayList<>();
		serverList.addAll(serverMap.values());
		Map<String,YQZZMatchRoomData> rooms = new HashMap<>();
		rooms.putAll(creatSeasonRoom(serverList, YQZZWar.PBYQZZWarType.YQZZ_GROUP));
		//保存房间数据
		YQZZMatchRoomData.saveAll(termId, rooms);
		//保存匹配数据
		YQZZMatchData data = new YQZZMatchData(termId, matchServer, curTime);
		data.saveRedis();
	}

	private void doSeasonKickoutMatch(){
		String matchServer = GsConfig.getInstance().getServerId();
		long curTime = HawkTime.getMillisecond();
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		int season = timeCfg.getSeason();
		Map<String, YQZZSeasonServer> serverMap = YQZZSeasonServer.loadAll(season);
		Map<String, YQZZJoinServer> joinServerMap = this.getDataManager().loadAllYQZZJoinServerData();
		List<YQZZJoinServer> advanceServerList = new ArrayList<>();
		List<YQZZJoinServer> kickoutServerList = new ArrayList<>();
		for(YQZZJoinServer joinServer : joinServerMap.values()){
			YQZZSeasonServer seasonServer = serverMap.get(joinServer.getServerId());
			if(seasonServer != null && seasonServer.isAdvance()){
				advanceServerList.add(joinServer);
			}else {
				kickoutServerList.add(joinServer);
			}
		}
		Map<String,YQZZMatchRoomData> rooms = new HashMap<>();
		rooms.putAll(creatSeasonRoom(kickoutServerList, YQZZWar.PBYQZZWarType.YQZZ_NOT_SEASON));
		rooms.putAll(creatSeasonRoom(advanceServerList, YQZZWar.PBYQZZWarType.YQZZ_KICKOUT));
		//保存房间数据
		YQZZMatchRoomData.saveAll(termId, rooms);
		//保存匹配数据
		YQZZMatchData data = new YQZZMatchData(termId, matchServer, curTime);
		data.saveRedis();
	}

	private Map<String,YQZZMatchRoomData> creatSeasonRoom(List<YQZZJoinServer> serverList, YQZZWar.PBYQZZWarType warType){
		int termId = this.getDataManager().getStateData().getTermId();
		YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
		Map<String,YQZZMatchRoomData> rooms = new HashMap<String,YQZZMatchRoomData>();
		int serverCount = serverList.size();
		int perCount = timeCfg.getMatchNeedCount();
		int poolSize = timeCfg.getMatchListCount();
		int p1 = serverCount / perCount;
		int p2 = serverCount % perCount;
		int roomCount = p1;
		int lessIndex = serverCount + 1;
		if(p2 > 0){
			//最后一个房间元素不够，最后一个房间的元素个数是  perCount -1
			roomCount += 1;
			int p3 = perCount -1;
			//共有lessCount个房间少一个元素
			int lessCount = p3 - p2 + 1;
			//从lessIndex 开始每个房间少一个元素
			lessIndex = roomCount - lessCount + 1;
		}
		List<YQZZJoinServer> firstServerList = new ArrayList<>();
		switch (warType){
			case YQZZ_NOT_SEASON:{
				Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
					@Override
					public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
						if(o1.getPower() != o2.getPower()){
							return o1.getPower() > o2.getPower()?-1 :1;
						}
						return 0;
					}
				});
			}
			break;
			case YQZZ_GROUP:{
				Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
					@Override
					public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
						if(o1.getPower() != o2.getPower()){
							return o1.getPower() > o2.getPower()?-1 :1;
						}
						return 0;
					}
				});
				for(int i = 0; i < roomCount; i++ ){
					if(serverList.isEmpty()){
						break;
					}
					YQZZJoinServer server = serverList.remove(0);
					firstServerList.add(server);
				}
				Collections.shuffle(serverList);
			}
			break;
			case YQZZ_KICKOUT:{
				if(timeCfg.getTurn() == 1){
					HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
					Map<String,String> groupRankMap = redisSession.hGetAll(YQZZSeasonServer.getGroupRankKey(timeCfg.getSeason()));
					Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
						@Override
						public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
							int rank1 = Integer.parseInt(groupRankMap.getOrDefault(o1.getServerId(), "100"));
							int rank2 = Integer.parseInt(groupRankMap.getOrDefault(o2.getServerId(), "100"));
							if(rank1 != rank2){
								return rank1 < rank2 ? -1 : 1;
							}
							return 0;
						}
					});
				}else {
					Collections.sort(serverList,new Comparator<YQZZJoinServer>() {
						@Override
						public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
							YQZZRecordData data1= YQZZRecordData.loadData(o1.getServerId(), termId - 1);
							YQZZRecordData data2= YQZZRecordData.loadData(o2.getServerId(), termId - 1);
							int rank1 = 10;
							int rank2 = 10;
							long seasonScore1 = 0;
							long seasonScore2 = 0;
							long score1 = 0;
							long score2 = 0;
							if(data1 != null){
								rank1 = data1.getRank();
								seasonScore1 = data1.getSeasonScore();
								score1 = data1.getSeasonScore();
							}
							if(data2 != null){
								rank2 = data2.getRank();
								seasonScore2 = data2.getSeasonScore();
								score2 = data2.getSeasonScore();
							}
							if(rank1 != rank2){
								return rank1 < rank2 ? -1 : 1;
							}
							if(seasonScore1 != seasonScore2){
								return seasonScore1 > seasonScore2 ? -1 : 1;
							}
							if(score1 != score2){
								return score1 > score2 ? -1 : 1;
							}
							return 0;
						}
					});
				}
				for(int i = 0; i < roomCount; i++ ){
					if(serverList.isEmpty()){
						break;
					}
					YQZZJoinServer server = serverList.remove(0);
					firstServerList.add(server);
				}
				Collections.shuffle(serverList);
			}
			break;
		}
		for(int i = 1;i<=roomCount;i++) {
			int getCount = perCount;
			if (i >= lessIndex) {
				getCount = perCount - 1;
			}
			//没有了 ,结束循环
			if (serverList.size() <= 0) {
				break;
			}
			//开始拿去
			List<YQZZJoinServer> roomServers = new ArrayList<>();

			switch (warType) {
				case YQZZ_NOT_SEASON: {
					roomServers = this.getMatchRoomServers(serverList, poolSize, getCount);
				}
				break;
				case YQZZ_GROUP:{
					roomServers = this.getMatchRoomServers(firstServerList, serverList, getCount);
				}
				break;
				case YQZZ_KICKOUT:{
					roomServers = this.getMatchRoomServers(firstServerList, serverList, getCount);
				}
				break;
			}
			if (roomServers.isEmpty()) {
				continue;
			}
			//排序
			Collections.sort(roomServers, new Comparator<YQZZJoinServer>() {
				@Override
				public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
					if (o1.getPower() != o2.getPower()) {
						return o1.getPower() > o2.getPower() ? -1 : 1;
					}
					return 0;
				}
			});
			String roomServerId = this.getRoomServer(roomServers);
			YQZZMatchRoomData room = new YQZZMatchRoomData();
			room.setTermId(termId);
			room.setRoomId(HawkUUIDGenerator.genUUID());
			room.setRoomServerId(roomServerId);
			Set<String> joinServerSet = new HashSet<>();
			for (YQZZJoinServer roomServer : roomServers) {
				room.addServer(roomServer.getServerId());
				joinServerSet.add(roomServer.getServerId());
			}
			if(warType == YQZZWar.PBYQZZWarType.YQZZ_KICKOUT){
				room.setAdvance(true);
			}
			rooms.put(room.getRoomId(), room);
			try {
				String joinServerStr = SerializeHelper.collectionToString(joinServerSet, SerializeHelper.ATTRIBUTE_SPLIT);
				for (YQZZJoinServer roomServer : roomServers) {
					LogUtil.logYQZZMatch(termId, roomServer.getServerId(), roomServer.getPower(), joinServerStr, roomServerId);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return rooms;
	}


	private List<YQZZJoinServer> getMatchRoomServers(List<YQZZJoinServer> serverList,int poolSize,int needSize){
		List<YQZZJoinServer> rlt = new ArrayList<>();
		if(serverList.isEmpty()){
			return rlt;
		}
		YQZZJoinServer first = serverList.remove(0);
		rlt.add(first);
		poolSize = poolSize - 1;
		needSize = needSize - 1;
		if(poolSize >= serverList.size()){
			poolSize = serverList.size();
		}
		List<YQZZJoinServer> tmp = new ArrayList<>();
		for(int i = 0; i < poolSize; i++){
			YQZZJoinServer server = serverList.remove(0);
			tmp.add(server);
		}
		Collections.shuffle(tmp);
		for(int i=0; i<needSize; i++){
			if(tmp.size() <= 0){
				continue;
			}
			YQZZJoinServer server = tmp.remove(0);
			rlt.add(server);
		}
		Collections.sort(tmp, new Comparator<YQZZJoinServer>() {
			@Override
			public int compare(YQZZJoinServer o1, YQZZJoinServer o2) {
				if(o1.getPower() != o2.getPower()){
					return o1.getPower() < o2.getPower()?-1 :1;
				}
				return 0;
			}
		});
		while (tmp.size() > 0){
			YQZZJoinServer server = tmp.remove(0);
			serverList.add(0, server);
		}
		return rlt;
	}

	private List<YQZZJoinServer> getMatchRoomServers(List<YQZZJoinServer> serverList, int needSize){
		List<YQZZJoinServer> rlt = new ArrayList<>();
		for(int i=0;i<needSize;i++){
			if(serverList.isEmpty()){
				break;
			}
			YQZZJoinServer server = serverList.remove(0);
			rlt.add(server);
		}
		return rlt;
	}

	private List<YQZZJoinServer> getMatchRoomServers(List<YQZZJoinServer> firstServerList, List<YQZZJoinServer> serverList, int needSize){
		List<YQZZJoinServer> rlt = new ArrayList<>();
		YQZZJoinServer firstSeaver = firstServerList.remove(0);
		rlt.add(firstSeaver);
		for(int i=0;i<needSize-1;i++){
			if(serverList.isEmpty()){
				break;
			}
			YQZZJoinServer server = serverList.remove(0);
			rlt.add(server);
		}
		return rlt;
	}
	
	/**
	 * @param joinList 参赛的6只队伍
	 * @param serverListAll 所有可用于战场的服务器
	 * @return
	 */
	private String getRoomServer(List<YQZZJoinServer> joinList) {
		try {
			try {
				// 尝试使用cpu占用较低的服务器做为战斗服
				Optional<YQZZJoinServer> roomOp = joinList.stream().filter(s -> s.getCpuUsage() > 0).sorted(Comparator.comparingDouble(YQZZJoinServer::getCpuUsage)).findFirst();
				if (roomOp.isPresent()) {
					String serverId = roomOp.get().getServerId();
					List<String> jl = joinList.stream().map(s -> s.getServerId() + ":" + (int)s.getCpuUsage()).collect(Collectors.toList());
					DungeonRedisLog.log("YQZZgetRoomServer", "{} {}", jl, serverId);
					return serverId;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
			
			int serverChoose = 0;
			for(YQZZJoinServer join : joinList){
				String sId = join.getServerId();
				int serverId = Integer.parseInt(sId);
				if(serverChoose == 0){
					serverChoose = serverId;
				}
				if(serverId > serverChoose){
					serverChoose = serverId;
				}
//				if(GsConfig.getInstance().getGoodServerList().contains(serverId)){
//					serverChoose = serverId;
//					break;
//				}
			}
			return String.valueOf(serverChoose);
		} catch (Exception e) {
			HawkException.catchException(e);
			return joinList.get(0).getServerId();
		}
	}
}
