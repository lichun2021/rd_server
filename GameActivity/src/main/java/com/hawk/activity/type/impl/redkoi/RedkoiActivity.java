package com.hawk.activity.type.impl.redkoi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.redkoi.cfg.RedkoiActivitWishAwardCfg;
import com.hawk.activity.type.impl.redkoi.cfg.RedkoiActivityKVCfg;
import com.hawk.activity.type.impl.redkoi.cfg.RedkoiActivityKoiAwardCfg;
import com.hawk.activity.type.impl.redkoi.entity.RedkoiEntity;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.protocol.Activity.AwardWishPoint;
import com.hawk.game.protocol.Activity.KoiAwardChangeReq;
import com.hawk.game.protocol.Activity.KoiAwardRecord;
import com.hawk.game.protocol.Activity.KoiAwardRecordsResp;
import com.hawk.game.protocol.Activity.KoiWishReq;
import com.hawk.game.protocol.Activity.KoiWishResp;
import com.hawk.game.protocol.Activity.PlayerRoleInfo;
import com.hawk.game.protocol.Activity.RedKoiInfo;
import com.hawk.game.protocol.Activity.RedKoiInfoSyncResp;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class RedkoiActivity  extends ActivityBase{
	
	
	private long lastRefreshTime;
	
	private long lastRankRefreshTime;
	
	/** 跑马灯广播列表*/
	private List<KoiAwardRecord.Builder> broadcast = 
			new ArrayList<KoiAwardRecord.Builder>();
	
	
	public RedkoiActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	//获取数据计算锁
	public String getServerCalculateKey(){
		int termId = getActivityTermId();
		String key = "redkoi_activity:serverCalculateKey:"+termId;
		return key;
	}
	
	//排行数据
	public String getWishDataKey(int awardId,String turnId){
		int termId = getActivityTermId();
		return "redkoi_activity:wish_rank_" + termId+":"+awardId+"_"+turnId;
	}
	
	//许愿点总数
	public String getWishCountKey(String turnId){
		int termId = getActivityTermId();
		return "redkoi_activity:wish_point_count:" + termId+"_"+turnId;
	}
	
	//锦鲤计算完成记录
	public String getChooseRedkoiFinishKey(){
		int termId = getActivityTermId();
		return "redkoi_activity:choose_koi_finish_" + termId;
	}
	
	//锦鲤记录
	public String getKoiRecordKey(){
		int termId = getActivityTermId();
		return "redkoi_activity:koi_record_" + termId;
	}
	
	//发奖纪录
	public String getSendAwardKey(){
		int termId = getActivityTermId();
		String serverId = this.getDataGeter().getServerId();
		return "redkoi_activity:koi_record:"+serverId+"_" + termId;
	}
	
	
	
	@Override
	public void onTick() {
		long nowTime = HawkTime.getMillisecond();
		if(this.lastRefreshTime == 0){
			this.lastRefreshTime = nowTime;
			this.lastRankRefreshTime = nowTime;
			return;
		}
		final String lock= this.getServerCalculateKey();
		String serverId = this.getDataGeter().getServerId();
		int serverType = this.getDataGeter().getServerType();
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().hGet(lock,lock);
		
		if(HawkOSOperator.isEmptyString(rlt) ){
			//如果不是正常服务器，不参与抢夺计算锁
			if(serverType != ServerType.NORMAL){
				return;
			}
			//抢锁
			ActivityGlobalRedis.getInstance().getRedisSession().hSetNx(lock,lock, serverId);
			return;
		}
		logger.info("redKoiCalculateLock serverId:"+rlt);
		if(rlt.equals(serverId)){
			//设置锁过期时间
			ActivityGlobalRedis.getInstance().getRedisSession().expire(lock, 15);
			//如果是自己检查结算
			this.checkKoiCalculateTime();
			//清理排行榜
			this.clearWishRank(nowTime);
		}
		//所有服务器都发奖
		this.sendAward();
		//广播奖励
		this.awardBroadcast();
		//进入下轮
		this.checkKoiNextTurn(lastRefreshTime, nowTime);
		this.lastRefreshTime = nowTime;
	}
	
	
	

	@Override
	public ActivityType getActivityType() {
		return ActivityType.REDKOI_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RedkoiActivity activity = new RedkoiActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RedkoiEntity> queryList = HawkDBManager.getInstance()
				.query("from RedkoiEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RedkoiEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RedkoiEntity entity = new RedkoiEntity(playerId, termId);
		RedkoiActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		entity.setFreeTimes(config.getFreeTimes());
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	
	
	
	
	

	
	@Override
	public void onPlayerLogin(String playerId) {
		this.syncToPlayer(playerId,this.getServerWishPointCount());
	}
	
	@Override
	public void onOpen() {
		Map<Integer,AwardWishPoint.Builder> map = new HashMap<Integer,AwardWishPoint.Builder>();
		ConfigIterator<RedkoiActivityKoiAwardCfg> config = HawkConfigManager.getInstance().
				getConfigIterator(RedkoiActivityKoiAwardCfg.class);
		for(RedkoiActivityKoiAwardCfg cfg : config){
			AwardWishPoint.Builder abuilder = AwardWishPoint.newBuilder();
			abuilder.setAwardId(cfg.getRewardId());
			abuilder.setPoint(0);
			map.put(cfg.getRewardId(),abuilder);
		}
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.ACTIVITY_REDKOI_INIT, () -> {
				this.syncToPlayer(playerId,map);
			});
		}
	}
	
	
	@Override
	public void onEnd() {
		
	}
	
	
	
	public void awardBroadcast(){
		if(this.broadcast.size() <= 0){
			return;
		}
		KoiAwardRecord.Builder record = this.broadcast.remove(0);
		if(record == null){
			return;
		}
		int awardId = record.getAwardId();
		PlayerRoleInfo infoBuilder = record.getInfo();
		String koiServerId = infoBuilder.getServerId();
		String playerName = infoBuilder.getPlayerName();
		
		RedkoiActivityKoiAwardCfg cfg =  HawkConfigManager.getInstance().
				getConfigByKey(RedkoiActivityKoiAwardCfg.class, awardId);
		//跑马灯
		this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.ACTIVITY_REDKOI_AWARD,null, 
				koiServerId,playerName,cfg.getBigReward());
	}
	
	
	public void sendAward(){
		String finishKey = this.getChooseRedkoiFinishKey();
		String sendRecord = getSendAwardKey();
		Set<String> finishSet = ActivityGlobalRedis.getInstance().getRedisSession().sMembers(finishKey);
		Set<String> sendSet = ActivityGlobalRedis.getInstance().getRedisSession().sMembers(sendRecord);
		for(String key : finishSet){
			if(!sendSet.contains(key)){
				ActivityGlobalRedis.getInstance().getRedisSession().sAdd(sendRecord, (int)TimeUnit.DAYS.toSeconds(30), key);
				this.sendRedKoiAward(key);
			}
			
		}
	}
	
	
	/**
	 * 检查结算
	 */
	public void checkKoiCalculateTime(){
		long lastTurn = this.getLastTurn();
		String finishKey = this.getChooseRedkoiFinishKey();
		Set<String> finishSet = ActivityGlobalRedis.getInstance().getRedisSession().sMembers(finishKey);
		if(finishSet.contains(String.valueOf(lastTurn))){
			return;
		}
		this.calculateRedkoiAward(String.valueOf(lastTurn));
		//记录此轮完成
		ActivityGlobalRedis.getInstance().getRedisSession().sAdd(finishKey,(int)TimeUnit.DAYS.toSeconds(30), String.valueOf(lastTurn));
	}
	
	
	
	public void sendRedKoiAward(String turnId){
		//获取结果，发跑马灯，如果在本服，则发奖励
		int termId = getActivityTermId();
		Map<String,List<KoiAwardRecord.Builder>> recordmap = this.getKoiRecords();
		//本轮锦鲤列表
		List<KoiAwardRecord.Builder> list = recordmap.get(turnId);
		String serverId = this.getDataGeter().getServerId();
		if(list == null || list.size() == 0){
			logger.info("sendRedKoiAward serverId:"+serverId +",turnId:" + turnId+", no player award");
			return;
		}
		for(KoiAwardRecord.Builder award : list){
			PlayerRoleInfo infoBuilder = award.getInfo();
			String koiServerId = infoBuilder.getServerId();
			String playerId = infoBuilder.getPlayerId();
			String playerName = infoBuilder.getPlayerName();
			int koiAwardId = award.getAwardId();
			logger.info("redKoiAwardRecord koiServerId:"+koiServerId +
					",playerId:" + playerId+",playerName:" + playerName+",turnId:"+turnId);
			//添加进广播列表
			this.broadcast.add(award);
			//邮件奖励
			boolean isLocalServer =this.getDataGeter().isLocalServer(koiServerId);
			if(isLocalServer){
				//发奖励
				RedkoiActivityKoiAwardCfg cfg =  HawkConfigManager.getInstance().
						getConfigByKey(RedkoiActivityKoiAwardCfg.class, koiAwardId);
				List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getBigReward());
				Object[] content =  new Object[]{koiAwardId};
				this.getDataGeter().sendMail(infoBuilder.getPlayerId(), MailId.REDKOI_WISH_AWARD, null, null, content,
						rewardList, false);
				this.getDataGeter().redkoiAward(playerId, koiAwardId, String.valueOf(termId),turnId);
				logger.info("sendRedKoiAward serverId:"+serverId +",playerId" + playerId + ",playerName:" + playerName+
						",awardId"+award.getAwardId()+",time:"+award.getAwardTime());
			}
		}
		
	}
	
	
	
	
	/**
	 * 计算锦鲤得主
	 */
	public void calculateRedkoiAward(String turnId){
		ConfigIterator<RedkoiActivityKoiAwardCfg> config = HawkConfigManager.getInstance().
				getConfigIterator(RedkoiActivityKoiAwardCfg.class);
		List<String> kois = new ArrayList<String>();
		try {
			for(RedkoiActivityKoiAwardCfg awardcfg : config){
				String koi = this.makeRedkoi(awardcfg.getRewardId(),turnId);
				if(HawkOSOperator.isEmptyString(koi)){
					continue;
				}
				PlayerRoleInfo.Builder infoBuilder = PlayerRoleInfo.newBuilder();
				JsonFormat.merge(koi, infoBuilder);
				KoiAwardRecord.Builder builder = KoiAwardRecord.newBuilder();
				builder.setAwardId(awardcfg.getRewardId());
				builder.setAwardTime(Long.parseLong(turnId));
				builder.setInfo(infoBuilder);
				String record = JsonFormat.printToString(builder.build());
				kois.add(record);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		
		
		String recordKey = this.getKoiRecordKey();
		//添加锦鲤到redis
		if(kois.size() > 0){
			String[] koiStr = new String[kois.size()];
			kois.toArray(koiStr);
			ActivityGlobalRedis.getInstance().getRedisSession().sAdd(recordKey, (int)TimeUnit.DAYS.toSeconds(30),koiStr);
		}
		
	}
	
	
	/**
	 * 确定红警锦鲤
	 */
	public String makeRedkoi(int awardId,String turnId){
		//选出锦鲤
		int termId = getActivityTermId();
		String wishDataKey = getWishDataKey(awardId,turnId);
		Set<Tuple> set = ActivityGlobalRedis.getInstance().getRedisSession().
				zRevrangeWithScores(wishDataKey,0, 300,(int)TimeUnit.DAYS.toSeconds(30));
		if(set.size() <=0){
			return null;
		}
		String koiStr = this.randomKoi(set);
		logger.info("makeRedkoi koiStr:termId:"+termId+",turnId:" + turnId+",awardId:"+awardId+",koistr:"+koiStr);
		return koiStr;
	}
	
	
	/**
	 * 随机锦鲤
	 * @param set
	 * @return
	 */
	public String randomKoi(Set<Tuple> set){
		Map<String,Integer> wishPoints = new HashMap<String,Integer>();
		int pointCount = 0;
		for(Tuple tuple : set){
			String id = tuple.getElement();
			int score = (int) tuple.getScore();
			wishPoints.put(id, score);
			pointCount += score;
		}
		Random ran = new Random();
		int randomNum =ran.nextInt(pointCount);
		for (String id : wishPoints.keySet()) {
			int value = wishPoints.get(id);
			randomNum = randomNum - value;
			if (randomNum <= 0) {
				return id;
			}
		}
		return null;
	}
	
	

	
	public long getCurTurn(){
		RedkoiActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		HawkTuple3<Long,Long,Long> turns = config.getTurnIds();
		long turn = turns.second;
		return turn;
	}
	
	
	public long getLastTurn(){
		RedkoiActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		HawkTuple3<Long,Long,Long> turns = config.getTurnIds();
		long turn = turns.first;
		return turn;
	}
	
	public long getNextTurn(){
		RedkoiActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		HawkTuple3<Long,Long,Long> turns = config.getTurnIds();
		long turn = turns.third;
		return turn;
	}
	
	
	public void clearWishRank(long nowTime){
		long gap = nowTime - this.lastRankRefreshTime;
		if(gap < HawkTime.MINUTE_MILLI_SECONDS * 15){
			return;
		}
		this.lastRankRefreshTime = nowTime;
		long turn = this.getCurTurn();
		ConfigIterator<RedkoiActivityKoiAwardCfg> config = HawkConfigManager.getInstance().
				getConfigIterator(RedkoiActivityKoiAwardCfg.class);
		for(RedkoiActivityKoiAwardCfg cfg : config){
			String wishDataKey = this.getWishDataKey(cfg.getRewardId(),String.valueOf(turn));
			//获得锁就刷掉10000名以下的人
			ActivityGlobalRedis.getInstance().getRedisSession().zRemrangeByRank(wishDataKey, 0, -10000);
		}
			
		
	}
	
	
	public boolean getActivityInfo(String playerId){
		this.syncToPlayer(playerId,this.getServerWishPointCount());
		return true;
	}
	
	/**
	 * 许愿
	 * @param playerId
	 */
	public boolean wishMake(String playerId,HawkProtocol protocol){

		KoiWishReq req = protocol.parseProtocol(KoiWishReq.getDefaultInstance());
		if(!this.inWishTime()){
			sendErrorAndBreak(playerId, protocol.getType(),
					Status.Error.REDKOI_NOT_IN_WISH_TIME_VALUE);
			return false;
		}
		Optional<RedkoiEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return false;
		}
		RedkoiEntity entity = opPlayerDataEntity.get();
		RedkoiActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		int termId = getActivityTermId();
		long turn = this.getCurTurn();
		String turnId = String.valueOf(turn);
		if(!turnId.equals(entity.getTurnId())){
			Map<Integer,AwardWishPoint.Builder> list = this.getServerWishPointCount();
			callBack(playerId, GameConst.MsgId.ACTIVITY_REDKOI_INIT, () -> {
				this.syncToPlayer(playerId, list);
			});
			return false;
		}
		if(entity.getCurChoseAward() == 0){
			return false;
		}
		int wishTimes = req.getTimes();
		List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
		
		long moneyCost = 0;
		Reward.RewardItem.Builder itemPrice = RewardHelper.toRewardItem(cfg.getItemPrice());
		
		if(wishTimes == 1){
			Reward.RewardItem.Builder costSingle = RewardHelper.toRewardItem(cfg.getSinglePrice());
			int hasNum = this.getDataGeter().getItemNum(playerId, costSingle.getItemId());
			//单次许愿
			int freeTimes = entity.getFreeTimes();
			if(freeTimes >= wishTimes){
				freeTimes -= wishTimes;
				entity.setFreeTimes(freeTimes);
			}else{
				//是否有足够的道具提供消耗
				if(hasNum >= 1){
					rewardList.add(costSingle);
				}else{
					//消耗相应数量的金币
					moneyCost = costSingle.getItemCount() * itemPrice.getItemCount();
				}
			}
		}else if(wishTimes == 10){
			//10连续
			Reward.RewardItem.Builder costTen = RewardHelper.toRewardItem(cfg.getTenPrice());
			int hasNum = this.getDataGeter().getItemNum(playerId, costTen.getItemId());
			if(hasNum >= costTen.getItemCount()){
				rewardList.add(costTen);
			}else{
				moneyCost = (costTen.getItemCount() - hasNum) * itemPrice.getItemCount();
				costTen.setItemCount(hasNum);
				rewardList.add(costTen);
			}
		}
		
		if(moneyCost > 0){
			RewardItem.Builder moneyBuilder = RewardItem.newBuilder();
			moneyBuilder.setItemType(ItemType.PLAYER_ATTR_VALUE);
			moneyBuilder.setItemId(itemPrice.getItemId());
			moneyBuilder.setItemCount(moneyCost);		
			rewardList.add(moneyBuilder);
		}
		if(rewardList != null && rewardList.size() > 0){
			boolean consumeResult = this.getDataGeter().consumeItems
					(playerId, rewardList, protocol.getType(), Action.ACTIVITY_REDKOI_WISH_COST);
			if(!consumeResult){
				return false;
			}
		}
		
		this.addWishPoint(entity,wishTimes);
		entity.notifyUpdate();
		this.getWishAward(playerId, wishTimes);
		this.syncToPlayer(playerId, this.getServerWishPointCount());
		//玩家许愿消耗记录
		this.getDataGeter().redkoiPlayerCost(playerId, entity.getCurChoseAward(), itemPrice.getItemId(),
				moneyCost, wishTimes, String.valueOf(termId), turnId);
		return true;
	}
	
	
	/**
	 * 获取许愿奖励
	 * @param playerId
	 * @param times
	 */
	public void getWishAward(String playerId,int times){
		List<Integer> rlts = new ArrayList<Integer>();
		List<RewardItem.Builder> rewardItems = new ArrayList<RewardItem.Builder>();
		RedkoiActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		for(int i=0;i<times;i++){
			RedkoiActivitWishAwardCfg cfg = this.getRandomWishItem(times);
			rlts.add(cfg.getId());
			List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getRewards());
			rewardItems.addAll(rewardList);
		}
		//给10连赠送奖励
		if(times == 10){
			List<RewardItem.Builder> tenGiftList = RewardHelper.toRewardItemList(kvcfg.getTenGiftItem());
			rewardItems.addAll(tenGiftList);
		}
		//给10连赠送奖励
		if(times == 1){
			List<RewardItem.Builder> singleGiftList = RewardHelper.toRewardItemList(kvcfg.getSingleGiftItem());
			rewardItems.addAll(singleGiftList);
		}
		//给奖励
		this.getDataGeter().takeReward(playerId,rewardItems, 1, Action.ACTIVITY_REDKOI_WISH_REWARD, false, RewardOrginType.REDKOI_WISH_REWARD);
		KoiWishResp.Builder buider = KoiWishResp.newBuilder();
		buider.addAllAwardIds(rlts);
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.REDKOI_MAKE_WISH_RESP_VALUE, buider);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, protocol);
	}
	
	
	
	public RedkoiActivitWishAwardCfg getRandomWishItem(int times){
		ConfigIterator<RedkoiActivitWishAwardCfg> config = HawkConfigManager.getInstance().
				getConfigIterator(RedkoiActivitWishAwardCfg.class);
		List<RedkoiActivitWishAwardCfg> list = config.toList();
		int weight = 0;
		for(RedkoiActivitWishAwardCfg cfg : list){
			if(times == 1){
				weight += cfg.getSingleWeight();
			}else if(times == 10){
				weight += cfg.getTenWeight();
			}
		}
		Random ran = new Random();
		int molecular = ran.nextInt(weight);
		RedkoiActivitWishAwardCfg result = null;
		for (RedkoiActivitWishAwardCfg cfg : list) {
			if(times == 1){
				molecular = molecular - cfg.getSingleWeight();
			}else if(times == 10){
				molecular = molecular - cfg.getTenWeight();
			}
			
			if (molecular < 0) {
				result = cfg;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 检查进入下一轮
	 */
	public void checkKoiNextTurn(long lastTime,long nowTime){
		if(lastTime == 0){
			return;
		}
		RedkoiActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		int[][] awardTimeArr = config.getGetRewardTimeArr();
		boolean nextTurn = false;
		for(int i=0;i<awardTimeArr.length;i++){
			int[] time = awardTimeArr[i];
			int hour = time[0];
			int minute = time[1];
			int second = time[2];
			long calTime = HawkTime.getAM0Date().getTime();
			calTime +=  hour * HawkTime.HOUR_MILLI_SECONDS;
			calTime += minute * HawkTime.MINUTE_MILLI_SECONDS;
			calTime += second * 1000;
			if(lastTime < calTime && calTime <= nowTime){
				nextTurn = true;
				break;
			}
		}
		
		if(nextTurn){
			Map<Integer,AwardWishPoint.Builder> map = this.getServerWishPointCount();
			Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
			for(String playerId : onlinePlayerIds){
				callBack(playerId, GameConst.MsgId.ACTIVITY_REDKOI_INIT, () -> {
					this.syncToPlayer(playerId,map);
				});
			}
		}
	}
	/**
	 * 是否在许愿时间内
	 * @return
	 */
	public boolean inWishTime(){
		long nowTime = HawkTime.getMillisecond();
		long zeroTime = HawkTime.getAM0Date().getTime();
		RedkoiActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		int[][] timeArr = config.getBetTimeArr();
		for(int[] time : timeArr){
			int shour = time[0];
			int sminute = time[1];
			int sseconde = time[2];
			int ehour = time[3];
			int eminute = time[4];
			int eseconde = time[5];
			long startTime = zeroTime;
			startTime +=  shour * HawkTime.HOUR_MILLI_SECONDS;
			startTime += sminute * HawkTime.MINUTE_MILLI_SECONDS;
			startTime += sseconde * 1000;
			long endTime = zeroTime;
			endTime +=  ehour * HawkTime.HOUR_MILLI_SECONDS;
			endTime += eminute * HawkTime.MINUTE_MILLI_SECONDS;
			endTime += eseconde * 1000;
			if(startTime <nowTime && nowTime <endTime){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 添加许愿点
	 * @param entity
	 */
	public void addWishPoint(RedkoiEntity entity,int points){
		long turn = this.getCurTurn();
		String turnId = String.valueOf(turn);
		int wishAwardId = entity.getCurChoseAward();
		if(wishAwardId == 0){
			return;
		}
		int playerWishPoint = points;
		if(entity.getWishPointMap().containsKey(wishAwardId)){
			playerWishPoint += entity.getWishPointMap().get(wishAwardId);
		}
		entity.getWishPointMap().put(wishAwardId, playerWishPoint);
		String playerId = entity.getPlayerId();
		String wishDataKey = this.getWishDataKey(wishAwardId,turnId);
		String pointCountKey = this.getWishCountKey(turnId);
		
		PlayerRoleInfo.Builder infoBuilder = PlayerRoleInfo.newBuilder();
		infoBuilder.setPlayerId(playerId);
		infoBuilder.setOpenid(this.getDataGeter().getOpenId(playerId));
		infoBuilder.setPlayerName(this.getDataGeter().getPlayerName(playerId));
		infoBuilder.setIcon(this.getDataGeter().getIcon(playerId));
		String pfIcon = this.getDataGeter().getPfIcon(playerId);
		if (!HawkOSOperator.isEmptyString(pfIcon)) {
			infoBuilder.setPfIcon(pfIcon);
		}
		infoBuilder.setServerId(this.getDataGeter().getPlayerServerId(playerId));
		String member = JsonFormat.printToString(infoBuilder.build());
		ActivityGlobalRedis.getInstance().getRedisSession().zIncrby(
				wishDataKey, member, points, (int)TimeUnit.DAYS.toSeconds(30));
		//纪录全服许愿点
		ActivityGlobalRedis.getInstance().getRedisSession().zIncrby(
				pointCountKey, String.valueOf(wishAwardId), points, (int)TimeUnit.DAYS.toSeconds(30));
		
		
	}

	
	
	
	
	/**
	 * 获取默认锦鲤大奖选择
	 * @return
	 */
	public int getChooseAwardDefaut(){
		ConfigIterator<RedkoiActivityKoiAwardCfg> config = HawkConfigManager.getInstance().
				getConfigIterator(RedkoiActivityKoiAwardCfg.class);
		if(config.hasNext()){
			return config.next().getRewardId();
		}
		return 0;
		
	}
	
	
	
	
	/**
	 * 选择大奖
	 * @param player
	 * @param awardId
	 */
	public boolean chooseAward(String playerId,HawkProtocol protocol){
		KoiAwardChangeReq req = protocol.parseProtocol(KoiAwardChangeReq.getDefaultInstance());
		int awardId = req.getKoiAwardId();
		Optional<RedkoiEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return false;
		}
		if(!this.inWishTime()){
			sendErrorAndBreak(playerId, protocol.getType(), 
					Status.Error.REDKOI_NOT_IN_WISH_TIME_VALUE);
			return false;
		}
		RedkoiEntity entity = opPlayerDataEntity.get();
		ConfigIterator<RedkoiActivityKoiAwardCfg> config = HawkConfigManager.getInstance().
				getConfigIterator(RedkoiActivityKoiAwardCfg.class);
		if(!config.toMap().containsKey(awardId)){
			return false;
		}
		entity.setCurChoseAward(awardId);
		entity.notifyUpdate();
		return true;
	}
	
	
	
	/**
	 * 锦鲤中奖纪录
	 */
	public  boolean getKoiAwardRecord(String playerId){
		KoiAwardRecordsResp.Builder builder = KoiAwardRecordsResp.newBuilder();
		Map<String,List<KoiAwardRecord.Builder>> map = this.getKoiRecords();
		List<KoiAwardRecord> resultList = new ArrayList<>(100);
		for(List<KoiAwardRecord.Builder> list :map.values()){
			if(list!= null && list.size() > 0){
				for(KoiAwardRecord.Builder kb : list){
					resultList.add(kb.build());
				}
			}
		}
		Collections.sort(resultList, Comparator.comparing(KoiAwardRecord::getAwardTime).reversed());
		builder.addAllRecords(resultList);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.REDKOI_AWARD_RECORD_RESP_VALUE, builder);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, protocol);
		return true;
	} 
	
	
	/**
	 * 去redis拿锦鲤记录
	 */
	public Map<String,List<KoiAwardRecord.Builder>> getKoiRecords(){
		String recordKey = this.getKoiRecordKey();
		Set<String> koiRecordSet = ActivityGlobalRedis.getInstance().getRedisSession().sMembers(recordKey);
		Map<String,List<KoiAwardRecord.Builder>> awardMap = new HashMap<String,List<KoiAwardRecord.Builder>>();
		for(String rStr : koiRecordSet){
			KoiAwardRecord.Builder aBuilder = KoiAwardRecord.newBuilder();
			try {
				JsonFormat.merge(rStr, aBuilder);
			} catch (ParseException e) {
				e.printStackTrace();
				continue;
			}
			String key = String.valueOf(aBuilder.getAwardTime());
			List<KoiAwardRecord.Builder> alist = awardMap.get(key);
			if(alist == null){
				alist = new ArrayList<KoiAwardRecord.Builder>();
				awardMap.put(key, alist);
			}
			alist.add(aBuilder);
		}
		return awardMap;
	}
	
	private Map<Integer,AwardWishPoint.Builder> getServerWishPointCount(){
		Map<Integer,AwardWishPoint.Builder> map  =  new HashMap<Integer,AwardWishPoint.Builder>();
		ConfigIterator<RedkoiActivityKoiAwardCfg> config = HawkConfigManager.getInstance().
				getConfigIterator(RedkoiActivityKoiAwardCfg.class);
		long turnId = this.getCurTurn();
		String pointCountKey = this.getWishCountKey(String.valueOf(turnId));
		Set<Tuple> set = ActivityGlobalRedis.getInstance().getRedisSession().
				zRevrangeWithScores(pointCountKey,0, config.size(),(int)TimeUnit.DAYS.toSeconds(30));
		Map<String,Integer> points = new HashMap<String,Integer>();
		for(Tuple tuple : set){
			String id = tuple.getElement();
			int score = (int) tuple.getScore();
			points.put(id, score);
		}
		for(RedkoiActivityKoiAwardCfg cfg : config){
			int pointAll = 0;
			String rewardId = String.valueOf(cfg.getRewardId());
			if(points.containsKey(rewardId)){
				pointAll = points.get(rewardId);
			}
			AwardWishPoint.Builder abuilder = AwardWishPoint.newBuilder();
			abuilder.setAwardId(cfg.getRewardId());
			abuilder.setPoint(pointAll);
			map.put(cfg.getRewardId(),abuilder);
		}
		return map;
	}
	
	
	
	
	public void syncToPlayer(String playerId,Map<Integer,AwardWishPoint.Builder> map){
		
		RedKoiInfoSyncResp.Builder builder = RedKoiInfoSyncResp.newBuilder();
		RedKoiInfo.Builder rbuider = RedKoiInfo.newBuilder();
		Optional<RedkoiEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		int termId = this.getActivityTermId();
		long turn = this.getCurTurn();
		long nextTurn = this.getNextTurn();
		String turnId = String.valueOf(turn);
		RedkoiActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		RedkoiEntity entity = opPlayerDataEntity.get();
		boolean update = false;
		if(!turnId.equals(entity.getTurnId())){
			entity.setTurnId(turnId);
			entity.setFreeTimes(kvcfg.getFreeTimes());
			entity.getWishPointMap().clear();
			entity.setCurChoseAward(0);
			update = true;
		}
		
		rbuider.setKoiAwardId(entity.getCurChoseAward());
		rbuider.setFreeTimes(entity.getFreeTimes());
		Map<Integer, Integer> pointMap = entity.getWishPointMap();
		for(int awardId : pointMap.keySet()){
			int point = pointMap.get(awardId);
			AwardWishPoint.Builder abuilder = AwardWishPoint.newBuilder();
			abuilder.setAwardId(awardId);
			abuilder.setPoint(point);
			rbuider.addPoints(abuilder);
		}
		for(AwardWishPoint.Builder abuilder : map.values()){
			rbuider.addServerPoints(abuilder);
		}
		rbuider.setLotteryTime(Long.parseLong(turnId));
		long endTime = this.getTimeControl().getEndTimeByTermId(termId);
		int isEndTurn = nextTurn > endTime? 1: 0;
		rbuider.setIsEndTurn(isEndTurn);
		builder.setInfo(rbuider);
		if(update){
			entity.notifyUpdate();
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.REDKOI_INFO_SYNC_RESP_VALUE, builder);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, protocol);
	}
	
	
	
}



