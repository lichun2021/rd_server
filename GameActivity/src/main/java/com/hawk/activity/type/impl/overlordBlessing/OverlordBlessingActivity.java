package com.hawk.activity.type.impl.overlordBlessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.OverlordBlessingEvent;
import com.hawk.activity.event.impl.OverlordBlessingMarchEvent;
import com.hawk.activity.event.impl.ShareEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.overlordBlessing.cfg.OverlordBlessingAchieveCfg;
import com.hawk.activity.type.impl.overlordBlessing.cfg.OverlordBlessingKVCfg;
import com.hawk.activity.type.impl.overlordBlessing.entity.OverlordBlessingEntity;
import com.hawk.game.protocol.Activity.BlessState;
import com.hawk.game.protocol.Activity.OverlordBlessPageInfo;
import com.hawk.game.protocol.Activity.OverlordInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.DailyShareType;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;


import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerStruct;
import com.hawk.gamelib.GameConst;

public class OverlordBlessingActivity extends ActivityBase implements AchieveProvider {
	/**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 膜拜人数
	 */
	private AtomicInteger blessingNum = new AtomicInteger(0);
	/**
	 * 膜拜人数更新时间
	 */
	private long lastTickTime = 0;
	/**
	 * 已达成的成就
	 */
	private Set<Integer> finishAchieveIdSet = new HashSet<>();

	public OverlordBlessingActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.OVERLORD_BLESS_ACTIVITY;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		OverlordBlessingActivity activity = new OverlordBlessingActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<OverlordBlessingEntity> queryList = HawkDBManager.getInstance()
				.query("from OverlordBlessingEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			OverlordBlessingEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		OverlordBlessingEntity entity = new OverlordBlessingEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<OverlordBlessingEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return ;
		}
		OverlordBlessingEntity entity = opEntity.get();
		OverlordBlessPageInfo.Builder builder = genOverlordBlessingPageInfo(entity);
		//push
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.OVERLORD_BLESS_INFO_RESP_VALUE, builder));
	}

	/**
	 * 膜拜行军发起判断
	 * 
	 * @param playerId
	 * @return
	 */
	public int overlordBlessingCheck(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<OverlordBlessingEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		OverlordBlessingEntity entity = opEntity.get();
		//今日已经膜拜过
		if (entity.isHasBless()) {
			return Status.Error.OVERLORD_BLESS_COMPLETED_VALUE;
		}
		
		return 0;
	}
	/**
	 * 行军到达后进行膜拜
	 * @param playerId
	 * @param protoType
	 * @return  
	 */
	@Subscribe
	public void overlordBlessing(OverlordBlessingMarchEvent event){
		String playerId = event.getPlayerId();
		if (overlordBlessingCheck(playerId) != 0) {
			return;
		}
		
		Optional<OverlordBlessingEntity> opEntity = getPlayerDataEntity(playerId);
		OverlordBlessingEntity entity = opEntity.get();
		
		String key = getBlessKey();
		String realKey = getBlessRealKey();
		//膜拜人数
		long blessNum = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(key));
		//注水公式
		long waterNum = getWaterRateByChannle(playerId, blessNum);

		//真实数据
		long realBlessNum = ActivityGlobalRedis.getInstance().increase(realKey);
		ActivityGlobalRedis.getInstance().expire(realKey, (int)TimeUnit.DAYS.toSeconds(30));
		
		//更新人数
		blessNum = ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(key, waterNum, (int)TimeUnit.DAYS.toSeconds(30));
		blessingNum.set((int)blessNum);
		entity.setHasBless(true);
		ActivityManager.getInstance().postEvent(new OverlordBlessingEvent(playerId, (int)blessNum));
		//sync
		syncActivityDataInfo(playerId);
		
		logger.info("goToBlessingOverlord blessNum: {}, realBlessNum: {}, addWaterNum: {}", blessNum, realBlessNum, waterNum);
		//Tlog
		this.getDataGeter().logOverlordBlessingInfo(playerId, getActivityTermId(), realBlessNum, blessNum);
	}
	
	
	/**
	 * 领取膜拜奖励
	 * @param playerId
	 * @return
	 */
	public Result<?> receiveBlessingReward(String playerId){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<OverlordBlessingEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		OverlordBlessingEntity entity = opEntity.get();
		//是否是霸主
		boolean isOverlord = isOverlordMember(playerId);
		//今日未膜拜过
		if (!isOverlord && !entity.isHasBless()) {
			return Result.fail(Status.Error.OVERLORD_BLESS_NO_COMPLETE_VALUE);
		}
		//今日已经膜拜过
		if (entity.isReceiveBless()) {
			return Result.fail(Status.Error.OVERLORD_BLESS_REWARD_HAVE_RECEIVED_VALUE);
		}
		OverlordBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OverlordBlessingKVCfg.class);
		List<RewardItem.Builder> rewardList = cfg.getCivilRewardList();
		
		if (isOverlord) {
			rewardList = cfg.getOverlordRewardList();
		}
		this.getDataGeter().takeReward(playerId, rewardList, Action.OVERLORD_BLESSING_AWARD, true);
		
		entity.setReceiveBless(true);
		entity.notifyUpdate();
		
		syncActivityDataInfo(playerId);
		
		return Result.success();
	}
	
	/**
	 * 领取分享奖励
	 * @param playerId
	 * @return
	 */
	public Result<?> receiveBlessingShareReward(String playerId){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<OverlordBlessingEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		OverlordBlessingEntity entity = opEntity.get();
		//今日未分享过
		if (!entity.isHasShare()) {
			return Result.fail(Status.Error.OVERLORD_SHARE_NO_COMPLETE_VALUE);
		}
		//今日已经领取过分享奖励
		if (entity.isReceiveShare()) {
			return Result.fail(Status.Error.OVERLORD_SHARE_REWARD_HAVE_RECEIVED_VALUE);
		}
		OverlordBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OverlordBlessingKVCfg.class);
		List<RewardItem.Builder> rewardList = cfg.getCivilShareRewardList();
		//是否是霸主成员
		boolean isOverlord = isOverlordMember(playerId); 
		if (isOverlord) {
			rewardList = cfg.getOverlordShareRewardList();
		}
		this.getDataGeter().takeReward(playerId, rewardList, Action.OVERLORD_BLESSING_SHARE_AWARD, true);
		entity.setReceiveShare(true);
		syncActivityDataInfo(playerId);
		
		return Result.success();
		
	}
	
	
	/**
	 * 获取注水参数
	 * @param playerId
	 * @param blessNum
	 * @return
	 */
	public long getWaterRateByChannle(String playerId, long blessNum){
		OverlordBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OverlordBlessingKVCfg.class);
		String areaId = this.getDataGeter().getAreaId();	
		String waterLimitStr = cfg.getWaterLimitWX();
		String waterScaleStr = cfg.getWaterScaleWX();
		if (Integer.valueOf(areaId) == 2) {
			waterLimitStr = cfg.getWaterLimitQQ();
			waterScaleStr = cfg.getWaterScaleQQ();
		}
		int waterNum = 1;
		int[] waterLimit = SerializeHelper.string2IntArray(waterLimitStr);
		int[] waterScale = SerializeHelper.string2IntArray(waterScaleStr);
		if (blessNum < waterLimit[0]) {
			waterNum = waterScale[0];
		}else if(blessNum >= waterLimit[0] && blessNum <= waterLimit[1] ){
			waterNum = waterScale[1];
		}
		return waterNum;
	}
	
	/**
	 * 生成协议
	 * @param entity
	 * @return
	 */
	public OverlordBlessPageInfo.Builder genOverlordBlessingPageInfo(OverlordBlessingEntity entity){
		OverlordBlessPageInfo.Builder builder = OverlordBlessPageInfo.newBuilder();
		String playerId = entity.getPlayerId();
		String key = getBlessKey();
		long blessNum = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(key));
		builder.setAllServerBlessCount(blessNum);
		
		boolean isOverlordMember = isOverlordMember(playerId);
		//膜拜的状态
		BlessState blessState = BlessState.NO_COMPLETE;
		if (entity.isReceiveBless()) {
			blessState = BlessState.REWARDED;
		}else if ((entity.isHasBless() && !entity.isReceiveBless()) || (!entity.isReceiveBless() && isOverlordMember)) {
			blessState = BlessState.NO_GET;
		}
		builder.setState(blessState);
		//分享的状态
		BlessState shareState = BlessState.NO_COMPLETE;
		if (entity.isReceiveShare()) {
			shareState = BlessState.REWARDED;
		}else if (entity.isHasShare() && !entity.isReceiveShare()) {
			shareState = BlessState.NO_GET;
		}
		builder.setShareState(shareState);
		
		builder.setIsOverLordMember(isOverlordMember);
		
		StarWarsOfficerStruct worldKing = this.getDataGeter().getWorldKing();
		if (worldKing != null) {
			CrossPlayerStruct king = worldKing.getPlayerInfo();
			OverlordInfo.Builder overlordBuilder = OverlordInfo.newBuilder();
			overlordBuilder.setName(king.getName());
			overlordBuilder.setIcon(king.getIcon());
			overlordBuilder.setGuildName(king.getGuildName());
			overlordBuilder.setGuildTag(king.getGuildTag());
			overlordBuilder.setGuildFlag(king.getGuildFlag());
			overlordBuilder.setServerId(king.getMainServerId());
			overlordBuilder.setPfIcon(king.getPfIcon());
			builder.setOverlordInfo(overlordBuilder);
		}
		else{
			// 测试代码
			logger.info("OverlordBlessingActivity not have overlord king info ");
			/*OverlordInfo.Builder overlordBuilder = OverlordInfo.newBuilder();
			overlordBuilder.setName("霸主");
			overlordBuilder.setIcon(1);
			overlordBuilder.setGuildName("霸主联盟");
			overlordBuilder.setGuildTag("");
			overlordBuilder.setGuildFlag(1);
			overlordBuilder.setServerId(60016+"");
			overlordBuilder.setPfIcon("");
			builder.setOverlordInfo(overlordBuilder);*/
		}
	
		return builder;
	}
	
	/**
	 * 是否是霸主成员
	 * @param king
	 * @return
	 */
	public boolean isOverlordMember(String playerId){
		//霸主信息
		StarWarsOfficerStruct worldKing = this.getDataGeter().getWorldKing();
		if (worldKing == null) {
			return false;
		}
		CrossPlayerStruct king = worldKing.getPlayerInfo();
		String localServerId = this.getDataGeter().getServerId();
		if(king.getMainServerId().equals(localServerId)){
			String playerGuildId = this.getDataGeter().getGuildId(playerId);
			String kingGuildId = king.getGuildID();
			if (playerGuildId.equals(kingGuildId)) {
				long joinTm = this.getDataGeter().getJoinGuildTime(playerId);
				OverlordBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OverlordBlessingKVCfg.class);
				if (joinTm <= cfg.getAllianceLimitTimeValue()) {
					return true;
				}
			}
		}
		return false;
	}
	
	//分享协议
	@Subscribe
	public void onEvent(ShareEvent event){
		if (event.getShareType() != DailyShareType.SHARE_OVERLORD_BLESSING) {
			return;
		}
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<OverlordBlessingEntity> optional = getPlayerDataEntity(event.getPlayerId());
		if (!optional.isPresent()) {
			return;
		}
		OverlordBlessingEntity entity = optional.get();
		
		if (entity.isHasShare()) {
			return;
		}
		entity.setHasShare(true);
		entity.notifyUpdate();
		//sync
		syncActivityDataInfo(event.getPlayerId());
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<OverlordBlessingEntity> optional = getPlayerDataEntity(event.getPlayerId());
		if (!optional.isPresent()) {
			return;
		}
		
		OverlordBlessingEntity entity = optional.get();
        if (entity.getItemList().isEmpty()) {
            this.initAchieve(event.getPlayerId());
        }
        
        loginCheckAchieve(entity);
        
		if(!event.isCrossDay()){
			return;
		}
		entity.setHasBless(false);
		entity.setReceiveBless(false);
		entity.setHasShare(false);
		entity.setReceiveShare(false);
		entity.notifyUpdate();
		
		syncActivityDataInfo(event.getPlayerId());
	}
	
	/**
	 * 登录时检测成就数据
	 * 
	 * @param entity
	 */
	private void loginCheckAchieve(OverlordBlessingEntity entity) {
		List<AchieveItem> achieveItems = entity.getItemList();
		int personalMaxValue = 0;
		for (AchieveItem item : achieveItems) {
			personalMaxValue = Math.max(personalMaxValue, item.getValue(0));
		}
		
		int memBlessNum = blessingNum.get();
		if (personalMaxValue != memBlessNum) {
			ActivityManager.getInstance().postEvent(new OverlordBlessingEvent(entity.getPlayerId(), memBlessNum));
		}
	}
	
	
	/**
	 * key
	 */
	private String getBlessKey() {
		String key = "overlord_bless_" + this.getActivityTermId();
		return key;
	}
	
	/**
	 * 真实数据的 key
	 */
	private String getBlessRealKey() {
		String key = "overlord_bless_real_" + this.getActivityTermId();
		return key;
	}
	
	 @Override
	 public void onTick() {
		 ActivityEntity activityEntity = getActivityEntity();
         if (activityEntity.getActivityState() != ActivityState.OPEN) {
        	 return;
         }
	        
		 long now = HawkTime.getMillisecond();
		 if (now - lastTickTime < TimeUnit.MINUTES.toMillis(1)) {
			 return;
		 }
		 
		 lastTickTime = now;
		 String key = getBlessKey();
		 int redisBlessNum = NumberUtils.toInt(ActivityGlobalRedis.getInstance().get(key));
		 int memBlessNum = blessingNum.get();
		 if (memBlessNum == redisBlessNum) {
			 return;
		 }
		 
		 blessingNum.set(redisBlessNum);
		 int areaId = Integer.parseInt(getDataGeter().getAreaId());
		 Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		 boolean isUpdate = false;
		 ConfigIterator<OverlordBlessingAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(OverlordBlessingAchieveCfg.class);
		 for(OverlordBlessingAchieveCfg cfg : iterator){
             if(cfg.getPf() != areaId){
                continue;
             }
             
             int configValue = cfg.getConditionValue(0);
             if(redisBlessNum >= configValue && !finishAchieveIdSet.contains(cfg.getAchieveId())){
                finishAchieveIdSet.add(cfg.getAchieveId());
                isUpdate = true;
             }
		 }
		 
		 if(isUpdate){
             for(String playerId : onlinePlayerIds){
                callBack(playerId, GameConst.MsgId.OVERLORD_BLESSING_NUM_SYNC, () -> {
                    ActivityManager.getInstance().postEvent(new OverlordBlessingEvent(playerId, redisBlessNum));
                });
             }
		 }
	 }

	@Override
	public boolean isProviderActive(String playerId) {
		 //活动开就激活
        return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		 //活动展示就同步
        return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		 //获得玩家活动数据
        Optional<OverlordBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        OverlordBlessingEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(playerId);
        }
        //返回当前成就数据
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(OverlordBlessingAchieveCfg.class, achieveId);
        return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.OVERLORD_BLESSING_ACHIEVE;
	}
	
	 /**
     * 初始化成就数据
     * @param playerId 玩家id
     */
    private void initAchieve(String playerId) {
        Optional<OverlordBlessingEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        OverlordBlessingEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        int areaId = Integer.parseInt(getDataGeter().getAreaId());
        ConfigIterator<OverlordBlessingAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(OverlordBlessingAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()) {
        	OverlordBlessingAchieveCfg cfg = iterator.next();
        	if (cfg.getPf() == areaId) {
        		AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
        		list.add(item);
        	}
        }
        entity.setItemList(list);
    }
	
}
