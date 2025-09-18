package com.hawk.activity.type.impl.globalSign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.GlobalSignCountEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.globalSign.cfg.GlobalSignAchieveCfg;
import com.hawk.activity.type.impl.globalSign.cfg.GlobalSignActivityKVCfg;
import com.hawk.activity.type.impl.globalSign.entity.GlobalSignEntity;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.protocol.Activity.GlobalSignInPlayerJoinResp;
import com.hawk.game.protocol.Activity.GlobalSignInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 全服签到
 * 
 */
public class GlobalSignActivity extends ActivityBase implements AchieveProvider {
	
	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	/** 全服签到人数*/
	private int globalSignCount;
	/** 全服签到注水人数*/
	private int globalSignAssistCount;
	/** tick时间*/
	private long lastTickTime;
	/** 注水tick*/
	private long assistTickTime;

	private long debugTickTime;
	
	public GlobalSignActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GLOBAL_SIGN_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.GLOBAL_SIGN_INIT, () -> {
				initAchieve(playerId);
			});
		}
	}

	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GlobalSignActivity activity = new GlobalSignActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GlobalSignEntity> queryList = HawkDBManager.getInstance()
				.query("from GlobalSignEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GlobalSignEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GlobalSignEntity entity = new GlobalSignEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}


	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<GlobalSignEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		GlobalSignEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(GlobalSignAchieveCfg.class, achieveId);
		return cfg;
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.GLOBAL_SIGN_TASK_REWARD;
	}
	
	//初始化成就
	private void initAchieve(String playerId){
		Optional<GlobalSignEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		GlobalSignEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<GlobalSignAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GlobalSignAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			GlobalSignAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		return Result.success();
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<GlobalSignEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GlobalSignEntity entity = opEntity.get();
		pushToPlayer(playerId, HP.code.GLOBAL_SIGN_INFO_RESP_VALUE, genGlobalSignInfo(entity));
	}

	
	/**礼包详情信息
	 * @param entity
	 * @return
	 */
	public GlobalSignInfoResp.Builder genGlobalSignInfo(GlobalSignEntity entity){
		GlobalSignInfoResp.Builder builder = GlobalSignInfoResp.newBuilder();
		long curTime = HawkTime.getMillisecond();
		boolean sign = HawkTime.isSameDay(entity.getSignTime(), curTime);
		builder.setSign(sign);
		builder.setSignCount(this.globalSignCount + this.globalSignAssistCount);
		builder.setChatControl(entity.getBullectChatControl()<= 0);
		return builder;
	}
	
	
	
	
	
	@Override
	public void onTick() {
		//注水
		this.signCountAssistCheck();
		//签到检查
		this.signCountCheck();
		
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<GlobalSignEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			//推动任务
			GlobalSignCountEvent event = new GlobalSignCountEvent(playerId, this.globalSignCount + this.globalSignAssistCount);
			ActivityManager.getInstance().postEvent(event);
		}
	}
	/**
	 * 签到
	 * @param playerId
	 */
	public void playerSign(String playerId,int chatId){
		Optional<GlobalSignEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		GlobalSignEntity entity = opDataEntity.get();
		long curTime = HawkTime.getMillisecond();
		if(HawkTime.isSameDay(entity.getSignTime(), curTime)){
			return;
		}
		
		GlobalSignActivityKVCfg cfg = HawkConfigManager.getInstance()
				.getKVInstance(GlobalSignActivityKVCfg.class);
		if(chatId >= cfg.getBlessingWordList().size()){
			return;
		}
		entity.setSignTime(curTime);
		int add = 1;
		int addAssist = 0;
		int areaId = Integer.valueOf(this.getDataGeter().getAreaId());
		//手Q系数  1微信 2手Q
		if (areaId == 2) {
			addAssist = cfg.getSigninfactorQQ() - add;
		}else{
			addAssist = cfg.getSigninfactorWX() - add;
		}
		//累加签到
		this.addGlobalSignCount(add);
		this.addGlobalSignCountAssist(addAssist);
		//奖励
		List<RewardItem.Builder> signRewards = this.getSignRewardItems();
		this.getDataGeter().takeReward(playerId, signRewards, 1,
				Action.GLOBAL_SIGN,true, RewardOrginType.ACTIVITY_REWARD);
		//同步界面
		this.syncActivityDataInfo(playerId);
		//签到返回
		GlobalSignInPlayerJoinResp.Builder builder = GlobalSignInPlayerJoinResp.newBuilder();
		builder.setChatId(chatId);
		pushToPlayer(playerId, HP.code.GLOBAL_SIGN_PLAYER_SIGN_RESP_VALUE, builder);
		//日志
		int termId = this.getActivityTermId();
		this.getDataGeter().logPlayerGlobalSign(playerId, termId);
		HawkLog.logPrintln("GlobalSignActivity player sign,playerId:{},assist{}, notfityPlayerIds:{}", playerId, addAssist);
	}

	
	public List<RewardItem.Builder> getSignRewardItems(){
		GlobalSignActivityKVCfg cfg = HawkConfigManager.getInstance()
				.getKVInstance(GlobalSignActivityKVCfg.class);
		List<String> rewardList = this.getDataGeter().getAwardFromAwardCfg(cfg.getRandomRewards());
		List<RewardItem.Builder> rewardItemList = new ArrayList<>();
		for (String rewardStr : rewardList) {
			List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemList(rewardStr);
			rewardItemList.addAll(rewardBuilders);
		}
		if(cfg.getSignRewardList()!= null){
			rewardItemList.addAll(cfg.getSignRewardList());
		}
		return rewardItemList;
	}
	

	
	/**
	 * 设置弹幕开关
	 * @param playerId
	 * @param control
	 */
	public void settingBullectChatControl(String playerId,boolean control){
		Optional<GlobalSignEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GlobalSignEntity entity = opEntity.get();
		int setting = 0;
		if(!control){
			setting = 1;
		}
		if(entity.getBullectChatControl() == setting){
			return;
		}
		entity.setBullectChatControl(setting);
		this.syncActivityDataInfo(playerId);
	}
	
	
	/**
	 * 签到数量检查
	 */
	private void signCountCheck(){
		GlobalSignActivityKVCfg cfg = HawkConfigManager.getInstance()
				.getKVInstance(GlobalSignActivityKVCfg.class);
		int termId = this.getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		long refreshTime = cfg.getRefreshTime();
		
		long count = (HawkTime.getMillisecond() - startTime)/(refreshTime * 1000);
		if(count <= this.lastTickTime){
			return;
		}
		this.lastTickTime = count;
		this.globalSignAssistCount =  this.getGlobalSignCountAssist();
		this.globalSignCount = this.getGlobalSignCount();
		//广播一下全服签到数量
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			GlobalSignCountEvent event = new GlobalSignCountEvent(playerId, 
					this.globalSignCount + this.globalSignAssistCount);
			ActivityManager.getInstance().postEvent(event);
		}
	}
	
	
	
	/**
	 * 定时注水
	 */
	private void signCountAssistCheck() {
		String serverId = this.getDataGeter().getServerId();
		int serverType = this.getDataGeter().getServerType();
		//如果不是正常服务器，不参与抢夺计算锁
		if(serverType != ServerType.NORMAL){
			return;
		}
		String lock = getGlobalSignCountAssistLockKey();
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().hGet(lock,lock);
		if(HawkOSOperator.isEmptyString(rlt) ){
			//抢锁
			ActivityGlobalRedis.getInstance().getRedisSession().hSetNx(lock,lock, serverId);
			return;
		}
		if(rlt.equals(serverId)){
			long curTime = HawkTime.getMillisecond();
			//设置锁过期时间
			ActivityGlobalRedis.getInstance().getRedisSession().expire(lock, 15);
			//注水
			this.globalSignAssistIn(curTime);
			//方便测试
			if(this.getDataGeter().isServerDebug()){
				if(this.debugTickTime != 0){
					if((curTime - this.debugTickTime) > 2 * HawkTime.HOUR_MILLI_SECONDS){
						int count = (int) ((curTime - this.debugTickTime)/10000);
						long starTime = this.debugTickTime;
						for(int i =0;i<count-1;i++){
							this.globalSignAssistIn(starTime);
							starTime += 10000;
						}
					}
				}
				this.debugTickTime = curTime;
			}
		}
	}
	
	/**
	 * 注水
	 */
	private void globalSignAssistIn(long curTime){
		int areaId = Integer.valueOf(this.getDataGeter().getAreaId());
		GlobalSignActivityKVCfg cfg = HawkConfigManager.getInstance()
				.getKVInstance(GlobalSignActivityKVCfg.class);
		List<int[]> assistList = null;
		int baseAssist = 0;
		//手Q系数  1微信 2手Q
		if (areaId == 2) {
			assistList = cfg.getSigninAssistListQQ();
			baseAssist = cfg.getSigninAssistBaseQQ();
		}else{
			assistList = cfg.getSigninAssistListWX();
			baseAssist = cfg.getSigninAssistBaseWX();
		}
		boolean initAssist = this.globalSignCountAssistInit();
		if(!initAssist){
			this.addGlobalSignCountAssist(baseAssist);
			this.globalSignCountAssistInitFinish();
			return;
		}
		int termId = this.getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		for(int[] times : assistList){
			long sH = times[0];
			long eH = times[1];
			long interval = times[2];
			int add = times[3];
			boolean r1 = curTime > (startTime+sH * 1000);
			boolean r2 = curTime < (startTime + eH * 1000);
			boolean r3 = curTime > (this.assistTickTime + interval * 1000);
			if(r1 && r2 && r3){
				this.addGlobalSignCountAssist(add);
				this.assistTickTime = curTime;
				logger.info("GlobalSignActivity assist, addCount:{}",add);
				break;
			}
		}
	}
	

	/**礼包真实数据的key
	 * @param giftId
	 * @return
	 */
	public String getGlobalSignCountKey(){
		int termId = this.getActivityTermId();
		String key = ActivityRedisKey.GLOBAL_SIGN_COUNT + termId;
		return key;
	}
	
	
	/**
	 * 添加报名次数
	 * @param add
	 */
	public void addGlobalSignCount(int add){
		String key = this.getGlobalSignCountKey();
		 ActivityGlobalRedis.getInstance().getRedisSession()
				.increaseBy(key, add, (int)TimeUnit.DAYS.toSeconds(30));
	
	}
	
	/**
	 * 获取报名次数
	 * @return
	 */
	public int getGlobalSignCount(){
		String key = this.getGlobalSignCountKey();
		String countStr = ActivityGlobalRedis.getInstance().getRedisSession().getString(key);
		return NumberUtils.toInt(countStr);
	}
	
	
	
	/**礼包真实数据的key
	 * @param giftId
	 * @return
	 */
	public String getGlobalSignCountAssistKey(){
		int termId = this.getActivityTermId();
		String key = ActivityRedisKey.GLOBAL_SIGN_COUNT_ASSIST + termId;
		return key;
	}
	
	
	/**
	 * 添加报名次数
	 * @param add
	 */
	public void addGlobalSignCountAssist(int add){
		String key = this.getGlobalSignCountAssistKey();
		 ActivityGlobalRedis.getInstance().getRedisSession()
				.increaseBy(key, add, (int)TimeUnit.DAYS.toSeconds(30));
	
	}
	
	/**
	 * 获取报名次数
	 * @return
	 */
	public int getGlobalSignCountAssist(){
		String key = this.getGlobalSignCountAssistKey();
		String countStr = ActivityGlobalRedis.getInstance().getRedisSession().getString(key);
		return NumberUtils.toInt(countStr);
	}
	/**
	 * 获取枪锁key
	 * @return
	 */
	public String getGlobalSignCountAssistLockKey(){
		int termId = this.getActivityTermId();
		String key = ActivityRedisKey.GLOBAL_SIGN_COUNT_ASSIST_LOCK + termId;
		return key;
	}
	
	/**
	 * 注水初始化key
	 * @return
	 */
	public String getGlobalSignCountAssistInitKey(){
		int termId = this.getActivityTermId();
		String key = ActivityRedisKey.GLOBAL_SIGN_COUNT_ASSIST_INIT + termId;
		return key;
	}
	
	
	/**
	 * 注水是否初始化
	 * @return
	 */
	public boolean globalSignCountAssistInit(){
		String key = getGlobalSignCountAssistInitKey();
		String rlt = ActivityGlobalRedis.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(rlt)){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 注水初始化完成
	 */
	public void globalSignCountAssistInitFinish(){
		String serverId = this.getDataGeter().getServerId();
		String key = getGlobalSignCountAssistInitKey();
		ActivityGlobalRedis.getInstance().getRedisSession().setString(key,serverId, (int)TimeUnit.DAYS.toSeconds(30));
	}
}
