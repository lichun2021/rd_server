package com.hawk.activity.type.impl.submarineWar.rank;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.alibaba.fastjson.JSON;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.submarineWar.SubmarineWarActivity;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarKVCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarRankCfg;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.PBSubmarineWarRank;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Tuple;

public class SubmarineWarRank{

	public static final String RANK_LOCK_TYPE_SERVER_GROUP = "GROUP";
	public static final String RANK_LOCK_TYPE_RANK_SORT = "SORT";
	
	public static final String RANK_CACHE_TYPE_SHOW = "SHOW";
	public static final String RANK_CACHE_TYPE_REWARD = "REWARD";
	
	public static final String RANK_REWARD_RECORD_END = "END";
	
	
	private static final int timeParm = 9999999;
	
	//服务器分组 服务器ID-组信息
	private Map<String,SubmarineWarRankGroup> serverGroupData = new HashMap<>();
	//排行榜缓存
	private List<PBSubmarineWarRank> showRank = new ArrayList<>();
	//玩家数据缓存
	private Map<String,SubmarineWarRankPlayerData> playerDatas = new HashMap<>();
	
	
	private long groupTickTime;
	private long sortTickTime;
	private long rewardTickTime;

	
	public double calRankScore(int score,int rankTime){
		if(rankTime >= timeParm){
			return 0;
		}
		if(score <= 0){
			return 0;
		}
		long timeParam = timeParm - rankTime;
		String scoreParm =score + "." + timeParam;
		return Double.valueOf(scoreParm);
	}
	
	
	
	
	
	
	public void ontick(long curTime){
		//初始化
		if(this.groupTickTime <= 0){
			this.groupTickTime = curTime;
			this.sortTickTime = curTime;
			this.rewardTickTime = curTime;
			this.reloadGroup();
			this.reloadRankShowCache();
			return;
		}
		//写入参加信息
		this.serverJoin();
		//抢夺数据操作锁
		this.takeLock();
		//分组tick 1分钟
		if(curTime - this.groupTickTime >= 5 * 60 * 1000){
			this.groupTickTime = curTime;			
			//分组
			this.doGroup();
			//加载分组信息
			this.reloadGroup();
		}
		//展示排行榜tick 5分钟
		if(curTime - this.sortTickTime >= 5 * 60 * 1000){
			this.sortTickTime = curTime;			
			//排序
			this.doRankShowSort();
			//加载排行榜信息
			this.reloadRankShowCache();
		}
		//排行榜发奖tick 2分钟
		if(curTime - this.rewardTickTime >= 2 * 60 * 1000){
			this.rewardTickTime = curTime;			
			//排行榜奖励奖励排序
			this.doRewardSort(curTime);
			//排行榜发奖
			this.sendRankReward(curTime);
		}
	}

	
	public void serverJoin(){
		//如果不是正常服务器，不参与抢夺计算锁
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		if(serverId.startsWith("1999") || serverId.startsWith("2999") ){
		    return;
		}
		final String joinKey= this.getJoinKey();
		String str = ActivityGlobalRedis.getInstance().getRedisSession().hGet(joinKey, serverId);
		if(!HawkOSOperator.isEmptyString(str)){
			return;
		}
		//写入参与信息
		long updateTime = HawkTime.getMillisecond();
		ActivityGlobalRedis.getInstance().getRedisSession().hSet(joinKey, serverId, String.valueOf(updateTime));
	}
	
	
	/**
	 * 争夺数据操作的锁
	 */
	public void takeLock(){
		this.takeDoGroupLock();
		this.takeRankSortLock();
	}
	
	/**
	 * 争抢分组数据锁
	 */
	public void takeDoGroupLock(){
		//如果不是正常服务器，不参与抢夺计算锁
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		if(serverId.startsWith("1999") || serverId.startsWith("2999") ){
		    return;
		}
		String lock= this.getLockKey(RANK_LOCK_TYPE_SERVER_GROUP, 0);
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().getString(lock);
		if(HawkOSOperator.isEmptyString(rlt)){
			//抢锁
			ActivityGlobalRedis.getInstance().getRedisSession().setNx(lock, serverId);
			return;
		}
		HawkLog.logPrintln("SubmarineWarRankGoupLock serverId:"+rlt);
		if(!rlt.equals(serverId)){
			return;
		}
		//设置锁过期时间5分钟
		ActivityGlobalRedis.getInstance().getRedisSession().expire(lock, 300);
	}
	
	/**
	 * 争抢排行榜排序锁
	 */
	public void takeRankSortLock(){
		//如果不是正常服务器，不参与抢夺计算锁
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		if(serverId.startsWith("1999") || serverId.startsWith("2999") ){
		    return;
		}
		Integer groupId = this.getServerGroupId(serverId);
		if(Objects.isNull(groupId)){
			return;
		}
		String lock= this.getLockKey(RANK_LOCK_TYPE_RANK_SORT, groupId);
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().getString(lock);
		if(HawkOSOperator.isEmptyString(rlt)){
			//抢锁
			ActivityGlobalRedis.getInstance().getRedisSession().setNx(lock, serverId);
			return;
		}
		HawkLog.logPrintln("SubmarineWarRankSortLock serverId:{},groupId:{}",rlt,groupId);
		if(!rlt.equals(serverId)){
			return;
		}
		ActivityGlobalRedis.getInstance().getRedisSession().expire(lock, 300);
	}
	
	public void reloadGroup(){
		Map<String,SubmarineWarRankGroup> serverMap = new HashMap<>();
		Map<Integer,SubmarineWarRankGroup> groupMap = this.loadGroup();
		for(SubmarineWarRankGroup group : groupMap.values()){
			for(String sid : group.getServers()){
				serverMap.put(sid, group);
			}
		}
		this.serverGroupData = serverMap;
	}
	
	public void doGroup(){
		//查看是否有分组锁
		String lock= this.getLockKey(RANK_LOCK_TYPE_SERVER_GROUP, 0);
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().getString(lock);
		if(!serverId.equals(rlt)){
			return;
		}
		//获取数据
		Map<Integer,SubmarineWarRankGroup> groupMap = this.loadGroup();
		Map<String,Long> joinMap = this.loadJoinServer();
		long curTime = HawkTime.getMillisecond();
		//检查分组的服务器是否有不是主服的，不是主服的则干掉
		boolean checkMain = this.checkServerMain(groupMap, curTime);
		int maxGourpId = this.getGroupIdMax(groupMap);
		boolean checkGroup = this.checkGroup(joinMap, groupMap,maxGourpId);
		if(checkMain || checkGroup){
			this.saveGroup(groupMap);
			HawkLog.logPrintln("SubmarineWarRankdoGroup serverId:"+JSON.toJSONString(groupMap));
		}
	}
	
	public int getGroupIdMax(Map<Integer,SubmarineWarRankGroup> groupMap){
		int maxId = 0;
		for(Map.Entry<Integer, SubmarineWarRankGroup> entry : groupMap.entrySet()){
			SubmarineWarRankGroup group = entry.getValue();
			if(group.getGroupId() > maxId){
				maxId = group.getGroupId();
			}
		}
		return maxId;
	}
	
	public boolean checkGroup(Map<String,Long> joinMap,Map<Integer,SubmarineWarRankGroup> groupMap,int maxGroupId){
		//分组
		boolean update = false;
		Map<String,Integer> serverGroupIdMap = new HashMap<>();
		for(Map.Entry<Integer, SubmarineWarRankGroup> entry : groupMap.entrySet()){
			SubmarineWarRankGroup group = entry.getValue();
			for(String sid : group.getServers()){
				serverGroupIdMap.put(sid, group.getGroupId());
			}
		}
		List<Integer> slist = new ArrayList<>();
		for(Map.Entry<String, Long> entry : joinMap.entrySet()){
			String sid = entry.getKey();
			//已经有组了
			if(serverGroupIdMap.containsKey(sid)){
				continue;
			}
			//不是主服
			String mainServer = ActivityManager.getInstance().getDataGeter().getMainServer(sid);
			if(!sid.equals(mainServer)){
				continue;
			}
			slist.add(Integer.parseInt(sid));
		}
		if(slist.size() > 0){
			update = true;
			//排序
			Collections.sort(slist);
			//组ID
			SubmarineWarKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
			int groupMemberCnt = kvCfg.getServerNumber();
			int groupCount = slist.size() / groupMemberCnt + 1;
			for(int i=1;i<=groupCount;i++){
				if(slist.size() <= 0){
					continue;
				}
				int groupId = maxGroupId + i;
				SubmarineWarRankGroup addGroup = new SubmarineWarRankGroup();
				addGroup.setGroupId(groupId);
				for(int m =0;m<groupMemberCnt;m++){
					if(slist.size() <= 0){
						continue;
					}
					String str = String.valueOf(slist.remove(0));
					addGroup.getServers().add(str);
				}
				groupMap.put(addGroup.getGroupId(), addGroup);
				HawkLog.logPrintln("SubmarineWarRank,checkGroup,addGroup,groupId:{}",addGroup.serializ());
			}
		}
		return update;
	}
	
	
	/**
	 * 检查是否已经有和服的组了
	 * @param groupMap
	 * @param curTime
	 * @return
	 */
	public boolean checkServerMain(Map<Integer,SubmarineWarRankGroup> groupMap,long curTime){
		boolean update = false;
		for(Map.Entry<Integer, SubmarineWarRankGroup> entry : groupMap.entrySet()){
			SubmarineWarRankGroup group = entry.getValue();
			List<String> dels = new ArrayList<>();
			for(String serId :group.getServers()){
				String mainServer = ActivityManager.getInstance().getDataGeter().getMainServer(serId);
				if(!serId.equals(mainServer)){
					dels.add(serId);
				}
			}
			//删除不是主服的
			for(String del :dels){
				group.getServers().remove(del);
				group.setUpdateTime(curTime);
				update = true;
			}
		}
		return update;
	}
	
	
	
	/**
	 * 重载分组信息
	 */
	public void checkGroupData(){
		 Map<String,SubmarineWarRankGroup>  rlt = new HashMap<>();
		 Map<Integer,SubmarineWarRankGroup>  groupMap = this.loadGroup();
		 for(Map.Entry<Integer, SubmarineWarRankGroup> entry : groupMap.entrySet()){
			SubmarineWarRankGroup group = entry.getValue();
			for(String serverId : group.getServers()){
				rlt.put(serverId, group);
			}
		}
		this.serverGroupData = rlt;
	}
	
	
	
	public List<PBSubmarineWarRank.Builder> sortRank(int rankSize,int group,boolean withPlayerData){
		String rankKey = this.getRankKey(group);
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(rankKey, 0, Math.max((rankSize - 1), 0));
		List<PBSubmarineWarRank.Builder> rlist = new ArrayList<>();
		int rank = 1;
		for(Tuple t : set){
			try {
				String ele = t.getElement();
				String[] parr = ele.split(SerializeHelper.ATTRIBUTE_SPLIT);
				String playerServerId = parr[0];
				String playerId = parr[1];
				String playerMainServerId = ActivityManager.getInstance().getDataGeter().getMainServer(playerServerId);
				//如果已经不是主服了删除
				if(!playerServerId.equals(playerMainServerId)){
					this.delSubmarineWarRankMember(group, ele);
					continue;
				}
				//还没有加载到这个服的组，先不管
				Integer playerServerGroup = this.getServerGroupId(playerServerId);
				if(Objects.isNull(playerServerGroup)){
					continue;
				}
				//如果不是当前组的，删掉
				if(group != playerServerGroup.intValue()){
					this.delSubmarineWarRankMember(group, ele);
					continue;
				}
				long score  = (long)t.getScore();
				PBSubmarineWarRank.Builder rBuilder = PBSubmarineWarRank.newBuilder();
				//如果需要携带玩家数据
				if(withPlayerData){
					SubmarineWarRankPlayerData pdata = this.loadPlayerData(playerId);
					if(Objects.isNull(pdata)){
						continue;
					}
					rBuilder.setPlayerName(pdata.getPlayerName());
					if(!HawkOSOperator.isEmptyString(pdata.getGuildTag())){
						rBuilder.setGuildTag(pdata.getGuildTag());
					}
					if(!HawkOSOperator.isEmptyString(pdata.getGuildName())){
						rBuilder.setGuildTag(pdata.getGuildName());
					}
					if(!HawkOSOperator.isEmptyString(pdata.getGuildTag())){
						rBuilder.setGuildTag(pdata.getGuildTag());
					}
				}
				rBuilder.setServerId(playerServerId);
				rBuilder.setPlayerId(playerId);
				rBuilder.setLevel(0);
				rBuilder.setScore(score);
				rBuilder.setRank(rank);
				rlist.add(rBuilder);
				rank++;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
		}
		return rlist;
	}
	

	public void doRankShowSort() {
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Integer groupId = this.getServerGroupId(serverId);
		if(Objects.isNull(groupId)){
			return;
		}
		String lock= this.getLockKey(RANK_LOCK_TYPE_RANK_SORT, groupId);
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().getString(lock);
		HawkLog.logPrintln("SubmarineWarRank,doRankSort serverId:{},groupId:{}",rlt,groupId);
		if(!serverId.equals(rlt)){
			return;
		}
		int rankSize = this.getRankSize();
		List<PBSubmarineWarRank.Builder> rlist = this.sortRank(rankSize,groupId,true);
		this.saveSubmarineWarRankShowCache(rlist, groupId);
	}
	
	
	public void reloadRankShowCache(){
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Integer groupId = this.getServerGroupId(serverId);
		if(Objects.isNull(groupId)){
			return;
		}
		this.showRank = this.loadSubmarineWarRankShowCache(groupId);
	}
	
	
	
	
	public void doRewardSort(long curTime){
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Integer groupId = this.getServerGroupId(serverId);
		if(Objects.isNull(groupId)){
			return;
		}
		String lock= this.getLockKey(RANK_LOCK_TYPE_RANK_SORT, groupId);
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().getString(lock);
		HawkLog.logPrintln("SubmarineWarRank,doRewardSort serverId:{},groupId:{}",rlt,groupId);
		if(!serverId.equals(rlt)){
			return;
		}
		Long mergeTime = ActivityManager.getInstance().getDataGeter().getServerMergeTime();
		List<HawkTuple3<String, Long, Long>>  times = this.getSendRewardTimes();
		for(HawkTuple3<String, Long, Long> timeEntry : times){
			String key = timeEntry.first;
			long sortTime = timeEntry.second;
			if(curTime < sortTime){
				continue;
			}
			if(Objects.nonNull(mergeTime) && mergeTime.longValue() > sortTime){
				continue;
			}

			String cacheDatakey = this.getRankCacheKey(RANK_CACHE_TYPE_REWARD, groupId, key);
			boolean has = ActivityGlobalRedis.getInstance().getRedisSession().exists(cacheDatakey);
			if(has){
				continue;
			}
			int rankSize = this.getRankSize();
			List<PBSubmarineWarRank.Builder> rlist = this.sortRank(rankSize,groupId,false);
			this.saveSubmarineWarRankDailyRewardCache(rlist, groupId, key);
		}
	}
	

	public void sendRankReward(long curTime){
		int termId = this.getTermId();
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Integer groupId = this.getServerGroupId(serverId);
		if(Objects.isNull(groupId)){
			return;
		}
		Long mergeTime = ActivityManager.getInstance().getDataGeter().getServerMergeTime();
		List<HawkTuple3<String, Long, Long>>  times = this.getSendRewardTimes();
		Map<String,String> sendRecord = this.loadSubmarineWarRankRewardRecod();
		for(HawkTuple3<String, Long, Long> timeEntry : times){
			String key = timeEntry.first;
			long sortTime = timeEntry.second;
			long sendTime = timeEntry.third;
			if(curTime < sendTime){
				continue;
			}
			if(Objects.nonNull(mergeTime) && mergeTime.longValue() > sortTime && curTime >mergeTime.longValue()){
				continue;
			}
			if(sendRecord.containsKey(key)){
				continue;
			}
			List<PBSubmarineWarRank>  list = this.loadSubmarineWarRankRewardCache(groupId, key);
			if(Objects.isNull(list)){
				continue;
			}
			if(list.size() <= 0){
				continue;
			}
			this.addSubmarineWarRankRewardRecod(key, String.valueOf(curTime));
			for(PBSubmarineWarRank rankMember : list){
				String rankServerId = rankMember.getServerId();
				String playerId = rankMember.getPlayerId();
				if(!serverId.equals(rankServerId)){
					continue;
				}
				boolean playerExist = ActivityManager.getInstance().getDataGeter().isPlayerExist(playerId);
				if(!playerExist){
					continue;
				}
				int rank = rankMember.getRank();
				SubmarineWarRankCfg cfg = getRankRewardCfg(rank);
				if (cfg == null) {
					continue;
				}
				this.sendRewardEmail(key, playerId, rank, cfg);
				//TLOG
				this.logSubmarineWarRankReward(playerId, termId, groupId, key, rank);
			}
		}
		
	}
	
	public void sendRewardEmail(String rewardType,String playerId,int rank,SubmarineWarRankCfg cfg){
		MailId mId = MailId.SUBMARINE_WAR_DAILY_RANK_REWARD;
		List<RewardItem.Builder> rlist = RewardHelper.toRewardItemImmutableList(cfg.getReward());
		if(rewardType.equals(RANK_REWARD_RECORD_END)){
			mId = MailId.SUBMARINE_WAR_FINAL_RANK_REWARD;
			rlist = RewardHelper.toRewardItemImmutableList(cfg.getFinalReward());
		}
		// 邮件发送奖励
		Object[] content = new Object[]{rank};
//		Object[] title = new Object[0];
//		Object[] subTitle = new Object[0];
		//发邮件
		ActivityManager.getInstance().getDataGeter().sendMail(playerId, mId, null, null, content, rlist, false);
	}
	

		
	private SubmarineWarRankCfg getRankRewardCfg(int rank) {
		List<SubmarineWarRankCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(SubmarineWarRankCfg.class).toList();
		SubmarineWarRankCfg cfg = null;
		for (SubmarineWarRankCfg rankCfg : cfgList) {
			int rankUpper = rankCfg.getRankUpper();
			int rankLower = rankCfg.getRankLower();
			if (rank >= rankUpper && rank <= rankLower) {
				cfg = rankCfg;
			}
		}
		return cfg;
	}
	

	
	
	
	
	public boolean addPlayerScore(String playerId,int score,int rankTime) {
		if(playerId == null || score <= 0){
			return false;
		}
		int termId = getTermId();
		if(termId == 0){
			return false;
		}
		this.addSubmarineWarRank(playerId, score, rankTime);
		this.addPlayerData(playerId);
		return true;
	}
	

	public void delPlayerScore(String playerId){
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Integer groupId = this.getServerGroupId(serverId);
		if(Objects.isNull(groupId)){
			return;
		}
		String filedKey = serverId + SerializeHelper.ATTRIBUTE_SPLIT + playerId;
		String redisKey = this.getRankKey(groupId);
		ActivityLocalRedis.getInstance().zrem(redisKey, filedKey);
	}
	
	

	public List<PBSubmarineWarRank> getShowRankList() {
		return this.showRank;
	}

	
	
	public int getRankSize() {
		SubmarineWarKVCfg config = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
		int size =  config.getRankSize();
		int rankRewardSize = SubmarineWarRankCfg.getMaxRank();
		return Math.max(size, rankRewardSize); 
	}
	
	

	public Integer getServerGroupId(String serverId){
		SubmarineWarRankGroup group = this.serverGroupData.get(serverId);
		if(Objects.isNull(group)){
			return null;
		}
		return Integer.valueOf(group.getGroupId());
	}

	
	public int getTermId(){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.SUBMARINE_FIGHT_VALUE);
		if(opActivity.isPresent()){
			SubmarineWarActivity activity = (SubmarineWarActivity)opActivity.get();
			return activity.getActivityTermId();
		}
		return 0;
	}
	
	
	
	public List<HawkTuple3<String, Long, Long>>  getSendRewardTimes(){
		List<HawkTuple3<String, Long, Long>> list= new ArrayList<>();
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.SUBMARINE_FIGHT_VALUE);
		if(!opActivity.isPresent()){
			return list;
		}
		SubmarineWarActivity activity = (SubmarineWarActivity)opActivity.get();
		int termId = activity.getActivityTermId();
		if(termId <= 0){
			return list;
		}
		SubmarineWarKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
		long startTime = activity.getTimeControl().getStartTimeByTermId(termId);
		long endTime = activity.getTimeControl().getEndTimeByTermId(termId);
		int days = HawkTime.calcBetweenDays(new Date(startTime), new Date(endTime)) + 2;
		long startZero = HawkTime.getAM0Date(
				new Date(startTime)).getTime();
		for(int day = 0;day <days;day++ ){
			HawkTuple3<Integer, Integer, Integer> srotTime = kvCfg.getDailyRankRewarSortHour();
			HawkTuple3<Integer, Integer, Integer> sendTime = kvCfg.getDailyRankRewarSendHour();
			long sortTimePoint = startZero + day*HawkTime.DAY_MILLI_SECONDS 
					+ srotTime.first* HawkTime.HOUR_MILLI_SECONDS 
					+ srotTime.second* HawkTime.MINUTE_MILLI_SECONDS 
					+ srotTime.third * 1000;
			long sendTimePoint = startZero + day*HawkTime.DAY_MILLI_SECONDS 
					+ sendTime.first* HawkTime.HOUR_MILLI_SECONDS 
					+ sendTime.second* HawkTime.MINUTE_MILLI_SECONDS 
					+ sendTime.third * 1000;
			if(sortTimePoint <=startTime || sortTimePoint >= endTime){
				continue;
			}
			String key = HawkTime.formatTime(new Date(sortTimePoint),HawkTime.FORMAT_YMD);
			list.add(HawkTuples.tuple(key,sortTimePoint,sendTimePoint));
		}
		list.add(HawkTuples.tuple(RANK_REWARD_RECORD_END,endTime,endTime));
		return list;
	}
	
	
	

	
	
	
	
	
	public SubmarineWarRankPlayerData getPlayerRankShowData(String playerKey){
		SubmarineWarRankPlayerData data = this.playerDatas.get(playerKey);
		if(Objects.nonNull(data)){
			return data;
		}
		SubmarineWarRankPlayerData redisData = this.loadPlayerData(playerKey);
		if(Objects.nonNull(redisData)){
			this.playerDatas.put(playerKey, redisData);
			return redisData;
		}
		return null;
		
	}
	
	
	

    /**
     * <!-- 潜艇大战排行榜奖励 -->
     * @param playerId
     * @param termId
     * @param groupId  分组ID
     * @param rankType 排行榜类型
     * @param rankIndex 排行
     */
    private void logSubmarineWarRankReward(String playerId, int termId, int groupId, String rankType,int rankIndex){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("groupId", groupId);
        param.put("rankType", rankType);
        param.put("rankIndex", rankIndex);
        ActivityManager.getInstance().getDataGeter().logActivityCommon(playerId, LogInfoType.submarine_war_rank_reward, param);
    }
    
	
	
	public String getLockKey(String type,int param){
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.SUBMARINE_WAR_RANK_GROUP_LOCK, termId,type,param);
		return key;
	}
	
	public String getJoinKey(){
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.SUBMARINE_WAR_JOIN_SERVER, termId);
		return key;
	}
	
	public String getGroupKey(){
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.SUBMARINE_WAR_RANK_GROUP, termId);
		return key;
	}
	
	
	public String getRankKey(int group) {
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.SUBMARINE_WAR_SCORE_RANK, termId,group);
		return key;
	}

	public String getPlayerDataKey(String playerId){
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.SUBMARINE_WAR_SCORE_RANK_PLAYER_DATA, termId,playerId);
		return key;
	}
	
	public String getRankCacheKey(String type,int group,String param){
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.SUBMARINE_WAR_SCORE_RANK_CACHE, termId,type,group,param);
		return key;
	}
	
	
	public String getRankRewardSendKey(){
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.SUBMARINE_WAR_SCORE_RANK_REWARD_RECORD, termId,serverId);
		return key;
	}
	
	public Map<String,Long> loadJoinServer(){
		String key = this.getJoinKey();
		Map<String,String> map = ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(key);
		Map<String,Long> rlt = new HashMap<>();
		for(Map.Entry<String, String> entry : map.entrySet()){
			rlt.put(entry.getKey(), Long.parseLong(entry.getValue()));
		}
		return rlt;
	}
	
	public Map<Integer,SubmarineWarRankGroup> loadGroup(){
		String key = this.getGroupKey();
		Map<String,String> map = ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(key);
		Map<Integer,SubmarineWarRankGroup> rlt = new HashMap<>();
		for(Map.Entry<String, String> entry : map.entrySet()){
			String val = entry.getValue();
			SubmarineWarRankGroup group = new SubmarineWarRankGroup();
			group.mergeFrom(val);
			rlt.put(group.getGroupId(), group);
		}
		return rlt;
	}
	
	public void saveGroup(Map<Integer,SubmarineWarRankGroup> group){
		Map<String,String> rlt = new HashMap<>();
		for(Map.Entry<Integer, SubmarineWarRankGroup> entry : group.entrySet()){
			String key = String.valueOf(entry.getKey());
			String val = entry.getValue().serializ();
			rlt.put(key, val);
		}
		String redisKey = this.getGroupKey();
		ActivityGlobalRedis.getInstance().getRedisSession().hmSet(redisKey, rlt,  (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	public void addPlayerData(String playerId){
		String key = this.getPlayerDataKey(playerId);
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		String playerName = ActivityManager.getInstance().getDataGeter().getPlayerName(playerId);
		String guildName = ActivityManager.getInstance().getDataGeter().getGuildNameByByPlayerId(playerId);
		String guildTag = ActivityManager.getInstance().getDataGeter().getGuildTagByPlayerId(playerId);
		
		SubmarineWarRankPlayerData data = new SubmarineWarRankPlayerData();
		data.setServerId(serverId);
		data.setPlayerId(playerId);
		data.setPlayerName(playerName);
		data.setGuildName(guildName);
		data.setGuildTag(guildTag);
		ActivityGlobalRedis.getInstance().getRedisSession().setString(key, data.serializ(),(int)TimeUnit.DAYS.toSeconds(30));
		
	}
	
	
	public SubmarineWarRankPlayerData loadPlayerData(String playerKey){
		String key = this.getPlayerDataKey(playerKey);
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().getString(key, (int)TimeUnit.DAYS.toSeconds(30));
		if(HawkOSOperator.isEmptyString(rlt)){
			return null;
		}
		SubmarineWarRankPlayerData data = new SubmarineWarRankPlayerData();
		data.mergeFrom(rlt);
		return data;
	}
	
	
	
	public void addSubmarineWarRank(String playerId,int score,int rankTime){
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Integer groupId = this.getServerGroupId(serverId);
		if(Objects.isNull(groupId)){
			return;
		}
		String filedKey = serverId + SerializeHelper.ATTRIBUTE_SPLIT + playerId;
		double rankScore = this.calRankScore(score, rankTime);
		String redisKey = this.getRankKey(groupId);
		ActivityLocalRedis.getInstance().zaddWithExpire(redisKey, rankScore, filedKey, (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	
	public PBSubmarineWarRank.Builder getCurSubmarineWarRank(String playerId){
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		PBSubmarineWarRank.Builder builder = PBSubmarineWarRank.newBuilder();
		String playerName = ActivityManager.getInstance().getDataGeter().getPlayerName(playerId);
		String guildName = ActivityManager.getInstance().getDataGeter().getGuildNameByByPlayerId(playerId);
		String guildTag = ActivityManager.getInstance().getDataGeter().getGuildTagByPlayerId(playerId);
		if(!HawkOSOperator.isEmptyString(playerName)){
			builder.setPlayerName(playerName);
			builder.setPlayerId(playerId);
			builder.setServerId(serverId);
		}
		if(!HawkOSOperator.isEmptyString(guildName)){
			builder.setGuildName(guildName);
		}
		if(!HawkOSOperator.isEmptyString(guildTag)){
			builder.setGuildTag(guildTag);
		}
		Integer groupId = this.getServerGroupId(serverId);
		if(Objects.isNull(groupId)){
			builder.setRank(0);
			builder.setScore(0);
			builder.setLevel(0);
			return builder;
		}
		String filedKey = serverId + SerializeHelper.ATTRIBUTE_SPLIT + playerId;
		String redisKey = this.getRankKey(groupId);
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(redisKey, filedKey);
		int rank = 0;
		int level = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore().longValue();
		}
		builder.setRank(rank);
		builder.setScore(score);
		builder.setLevel(level);
		return builder;
	}
	
	
	public SubmarineWarRankGroup getServerGroup(){
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		return this.serverGroupData.get(serverId);
	}
	
	
	
	public void delSubmarineWarRankMember(int group,String playerKey){
		String key = this.getRankKey(group);
		ActivityGlobalRedis.getInstance().getRedisSession().hDel(key, playerKey);
	}
	
	
	public void saveSubmarineWarRankShowCache(List<PBSubmarineWarRank.Builder> list,int group){
		if(list.size() <= 0){
			return;
		}
		List<String> cahche = new ArrayList<>();
		for(PBSubmarineWarRank.Builder rank : list){
			String str = JsonFormat.printToString(rank.build());
			cahche.add(str);
		}
		String key = this.getRankCacheKey(RANK_CACHE_TYPE_SHOW, group,"0");
		ActivityGlobalRedis.getInstance().getRedisSession().del(key);
    	ActivityGlobalRedis.getInstance().getRedisSession().lPush(key,(int)TimeUnit.DAYS.toSeconds(30), cahche.toArray(new String[cahche.size()]));
	}
	
	
	
	public List<PBSubmarineWarRank> loadSubmarineWarRankShowCache(int group){
		List<PBSubmarineWarRank> cahche = new ArrayList<>();
		String key = this.getRankCacheKey(RANK_CACHE_TYPE_SHOW, group,"0");
		List<String> datas = ActivityGlobalRedis.getInstance().getRedisSession()
    			.lRange(key, 0, -1, (int)TimeUnit.DAYS.toSeconds(30));
    	for(String data : datas){
    		try {
    			PBSubmarineWarRank.Builder mbuilder = PBSubmarineWarRank.newBuilder();
    			JsonFormat.merge(new String(data.getBytes(), "UTF-8"), mbuilder);
    		    cahche.add(mbuilder.build());
    		} catch (ParseException | UnsupportedEncodingException e) {
    			HawkException.catchException(e);
    		}
    	}
    	Collections.reverse(cahche);
    	return cahche;
	}
	
	
	public void saveSubmarineWarRankDailyRewardCache(List<PBSubmarineWarRank.Builder> list,int group,String day){
		List<String> cahche = new ArrayList<>();
		if(Objects.isNull(list) || list.isEmpty()){
			PBSubmarineWarRank.Builder defaut = PBSubmarineWarRank.newBuilder();
			String str = JsonFormat.printToString(defaut.build());
			cahche.add(str);
		}else{
			for(PBSubmarineWarRank.Builder rank : list){
				String str = JsonFormat.printToString(rank.build());
				cahche.add(str);
			}
		}
		String key = this.getRankCacheKey(RANK_CACHE_TYPE_REWARD, group,day);
    	ActivityGlobalRedis.getInstance().getRedisSession().lPush(key,(int)TimeUnit.DAYS.toSeconds(30), cahche.toArray(new String[cahche.size()]));
	}
	
	
	public List<PBSubmarineWarRank> loadSubmarineWarRankRewardCache(int group,String param){ 
		List<PBSubmarineWarRank> cahche = new ArrayList<>();
		String key = this.getRankCacheKey(RANK_CACHE_TYPE_REWARD, group,param);
		List<String> datas = ActivityGlobalRedis.getInstance().getRedisSession()
    			.lRange(key, 0, -1, (int)TimeUnit.DAYS.toSeconds(30));
    	for(String data : datas){
    		try {
    			PBSubmarineWarRank.Builder mbuilder = PBSubmarineWarRank.newBuilder();
    			JsonFormat.merge(new String(data.getBytes(), "UTF-8"), mbuilder);
    		    cahche.add(mbuilder.build());
    		} catch (Exception e) {
    			HawkException.catchException(e);
    		}
    	}
    	return cahche;
	}
	
	
	
	public Map<String,String> loadSubmarineWarRankRewardRecod(){
		String key = this.getRankRewardSendKey();
		Map<String,String> map = ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(key, (int)TimeUnit.DAYS.toSeconds(30));
		return map;
	}
	
	
	public void addSubmarineWarRankRewardRecod(String key,String val){
		String rediskey = this.getRankRewardSendKey();
		ActivityGlobalRedis.getInstance().getRedisSession().hSet(rediskey, key, val,  (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	
}
