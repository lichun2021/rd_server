package com.hawk.activity.type.impl.airdrop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
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
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.AirdropSupplyGiftBuyEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayAirdropEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.airdrop.cfg.AirdropSupplyAchieveCfg;
import com.hawk.activity.type.impl.airdrop.cfg.AirdropSupplyActivityKVCfg;
import com.hawk.activity.type.impl.airdrop.cfg.AirdropSupplyGiftCfg;
import com.hawk.activity.type.impl.airdrop.entity.AirdropSupplyEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.Activity.AirdropPageInfo;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 空投补给活动
 * hf
 */
public class AirdropSupplyActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public AirdropSupplyActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	public static final int NO_BUY = 0;  	//未购买
	public static final int IS_BUY = 1;		//已购买

	@Override
	public ActivityType getActivityType() {
		return ActivityType.AIRDROP_SUPPLY_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<AirdropSupplyEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
		}
	}
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.AIRDROP_SUPPLY_INIT, () -> {
				initAchieve(playerId);
				this.syncActivityDataInfo(playerId);
			});
		}
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AirdropSupplyActivity activity = new AirdropSupplyActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AirdropSupplyEntity> queryList = HawkDBManager.getInstance()
				.query("from AirdropSupplyEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			AirdropSupplyEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AirdropSupplyEntity entity = new AirdropSupplyEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (this.isOpening(playerId)) {
			Optional<AirdropSupplyEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			AirdropSupplyEntity entity = opEntity.get();
			this.syncActivityInfo(playerId, entity);
		}
	}

	
	/**同步界面信息
	 * @param playerId
	 * @param entity
	 */
	private void syncActivityInfo(String playerId, AirdropSupplyEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}
		AirdropPageInfo.Builder builder = AirdropPageInfo.newBuilder();
		builder.setBoxNum(entity.getBoxNum());
		builder.setIsBuy(entity.isBuy());
		//push
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.AIRDROP_SUPPLY_PAGE_SYNC, builder));
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
		Optional<AirdropSupplyEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		AirdropSupplyEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().getConfigByKey(AirdropSupplyAchieveCfg.class, achieveId);
		return cfg;
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.AIRDROP_SUPPLY_REWARD;
	}
	//初始化成就
	private void initAchieve(String playerId){
		Optional<AirdropSupplyEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		AirdropSupplyEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<AirdropSupplyAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(AirdropSupplyAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			AirdropSupplyAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
		ActivityManager.getInstance().postEvent(new LoginDayAirdropEvent(playerId, 1), true);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		AirdropSupplyAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(AirdropSupplyAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<AirdropSupplyEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		AirdropSupplyEntity entity = optional.get();
		//掉落箱子个数
		int addBox = achieveCfg.getBoxNum();
		//是否购买
		int isBuy = entity.isBuy() ? IS_BUY: NO_BUY;
		String redisKey = getAirdropSupplyKey(playerId);
		//更新玩家数据 time_boxNum_isBuy
		int dayth = HawkTime.getYearDay();
		String boxInfo = ActivityGlobalRedis.getInstance().get(redisKey);
		int[] boxInfoArr = new int[3];
		if (!StringUtils.isEmpty(boxInfo)) {
			boxInfoArr = SerializeHelper.string2IntArray(boxInfo);
			if (dayth != boxInfoArr[0]) {
				logger.info("AirdropSupplyActivity onTakeReward is error dayth != boxInfoArr[0] dayth:{}, boxInfo:{}", dayth, boxInfo);
			}
			boxInfoArr[1] = boxInfoArr[1] + addBox;
		}else{
			boxInfoArr[0] = dayth;
			boxInfoArr[1] = addBox;
			boxInfoArr[2] = isBuy;
		}
		List<Integer> list = new ArrayList<>(boxInfoArr.length);
		for (Integer value : boxInfoArr) {
			list.add(value);
		}
		String newBoxInfo = SerializeHelper.collectionToString(list, SerializeHelper.ATTRIBUTE_SPLIT);
		//update redis
		ActivityGlobalRedis.getInstance().set(redisKey, newBoxInfo, (int)TimeUnit.DAYS.toSeconds(30));
		//更新resdis数据
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		
		entity.setBoxNum(entity.getBoxNum() + addBox);
		//sync
		syncActivityDataInfo(playerId);
		return Result.success();
	}
	
	
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		//检查奖励并发放
		checkRewardAndSendMail(playerId);
		
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<AirdropSupplyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		AirdropSupplyEntity entity  = opEntity.get();
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<AirdropSupplyAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(AirdropSupplyAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			AirdropSupplyAchieveCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		entity.resetItemList(items);
		entity.setBoxNum(0);
		entity.setBuy(false);
		long now = HawkTime.getMillisecond();
		if (event.isCrossDay() && !HawkTime.isSameDay(entity.getRefreshTime(), now)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setRefreshTime(now);
		}
		ActivityManager.getInstance().postEvent(new LoginDayAirdropEvent(playerId, entity.getLoginDays()), true);
		
		entity.notifyUpdate();
		//push
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		//sync
		syncActivityDataInfo(playerId);
		
		
	}

	//直购
	@Subscribe
	public void onAirdropSupplyGiftBuy(AirdropSupplyGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		String payforId = event.getGiftId();
		int giftId = AirdropSupplyGiftCfg.getGiftId(payforId);
		AirdropSupplyGiftCfg airdropSupplyGiftCfg = HawkConfigManager.getInstance().getConfigByKey(AirdropSupplyGiftCfg.class, giftId);
		if(airdropSupplyGiftCfg == null){
			logger.error("onAirdropSupplyGiftBuy giftCfg is null playerId:{},payforId:{},giftId:{}", playerId, payforId, giftId);
			return;
		}
		Optional<AirdropSupplyEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		AirdropSupplyEntity entity = optional.get();
		if (entity.isBuy()) {
			return;
		}
		String redisKey = getAirdropSupplyKey(playerId);
		int dayth = HawkTime.getYearDay();
		//充值相关数据处理    dayth_boxNum_isBuy
		String boxInfo = ActivityGlobalRedis.getInstance().get(redisKey);
		int[] boxInfoArr = new int[3];
		if (!StringUtils.isEmpty(boxInfo)) {
			boxInfoArr = SerializeHelper.string2IntArray(boxInfo);
			if (dayth != boxInfoArr[0]) {
				logger.info("AirdropSupplyActivity PayGiftBuyEvent is error dayth != boxInfoArr[0] dayth:{}, boxInfo:{}", dayth, boxInfo);
				return;
			}
			boxInfoArr[2] = IS_BUY;
		}else{
			boxInfoArr[0] = dayth;
			boxInfoArr[1] = 0;
			boxInfoArr[2] = IS_BUY;
			
		}
		List<Integer> list = new ArrayList<>(boxInfoArr.length);
		for (Integer value : boxInfoArr) {
			list.add(value);
		}
		String newBoxInfo = SerializeHelper.collectionToString(list, SerializeHelper.ATTRIBUTE_SPLIT);
		logger.info("AirdropSupplyActivity PayGiftBuyEvent is success dayth:{}, newBoxInfo:{}", dayth, newBoxInfo);
		//update redis
		ActivityGlobalRedis.getInstance().set(redisKey, newBoxInfo, (int)TimeUnit.DAYS.toSeconds(30));
		
		entity.setBuy(true);
		entity.notifyUpdate();
		//sync
		syncActivityDataInfo(playerId);
	}
	
	
	/**检查并发送奖励邮件
	 * @param playerId
	 */
	public void checkRewardAndSendMail(String playerId){
		String redisKey = getAirdropSupplyKey(playerId);
		String boxInfo = ActivityGlobalRedis.getInstance().get(redisKey);
		if (!StringUtils.isEmpty(boxInfo)) {
			logger.info("AirdropSupplyActivity checkRewardAndSendMail playerId:{}, boxInfo:{}",playerId, boxInfo);
			int dayth = HawkTime.getYearDay();
			int[] boxInfoArr = SerializeHelper.string2IntArray(boxInfo);
			if (dayth != boxInfoArr[0]) {
				if (boxInfoArr[1] > 0) {
					AirdropSupplyActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AirdropSupplyActivityKVCfg.class);
					List<RewardItem.Builder> rewardList = cfg.getCommonRewardList();
					//如果购买,高级宝箱
					if (boxInfoArr[2] == 1 ) {
						rewardList = cfg.getHighRewardList();
					}
					List<RewardItem.Builder> lastRewardList = new ArrayList<>();
					int num = boxInfoArr[1];
					//宝箱个数
					for (RewardItem.Builder rewardItem : rewardList) {
						RewardItem.Builder builder = RewardItem.newBuilder().setItemId(rewardItem.getItemId()).setItemType(rewardItem.getItemType()).setItemCount(rewardItem.getItemCount() * num);
						lastRewardList.add(builder);
					}
					//邮件发送奖励
					Object[] title = new Object[0];
					Object[] subTitle = new Object[0];
					Object[] content = new Object[0];
					//发邮件
					sendMailToPlayer(playerId, MailConst.MailId.AIRDROP_SUPPLY_REWARD, title, subTitle, content, lastRewardList);
					//del redis
					ActivityGlobalRedis.getInstance().del(redisKey);
					logger.info("AirdropSupplyActivity checkRewardAndSendMail success playerId:{}, boxNum:{}, isBuy:{}", playerId, num, boxInfoArr[2] == 1);
				}
			}
		}
	}
	
	/**检查是否可以购买礼包
	 * @param playerId
	 * @param payforId
	 * @return
	 */
	public boolean canPayforGift(String playerId, String payforId) {
		Optional<AirdropSupplyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		int giftId = AirdropSupplyGiftCfg.getGiftId(payforId);
		AirdropSupplyGiftCfg airdropSupplyGiftCfg = HawkConfigManager.getInstance().getConfigByKey(AirdropSupplyGiftCfg.class, giftId);
		if (airdropSupplyGiftCfg == null) {
			return false;
		}
		AirdropSupplyEntity entity = opEntity.get();
		//已经购买
		if (entity.isBuy()) {
			return false;
		}
		return true;
	}
	
	
	/**redis 的 key
	 * @param playerId
	 * @return
	 */
	public String getAirdropSupplyKey(String playerId){
		String key = "airdrop_supply_" + playerId;
		return key;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
}
