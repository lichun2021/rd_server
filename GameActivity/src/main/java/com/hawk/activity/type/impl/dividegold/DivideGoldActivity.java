package com.hawk.activity.type.impl.dividegold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.uuid.HawkUUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dividegold.cfg.DivideGoldAchieveCfg;
import com.hawk.activity.type.impl.dividegold.cfg.DivideGoldActivityKVCfg;
import com.hawk.activity.type.impl.dividegold.cfg.DivideGoldActivityTimeCfg;
import com.hawk.activity.type.impl.dividegold.cfg.DivideGoldChestWeightCfg;
import com.hawk.activity.type.impl.dividegold.cfg.DivideGoldGoldWeightCfg;
import com.hawk.activity.type.impl.dividegold.entity.DivideGoldEntity;
import com.hawk.game.protocol.Activity.DivideGoldInfo;
import com.hawk.game.protocol.Activity.OpenBoxResponse;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

/**瓜分金币(集福)活动
 * @author Winder
 */
public class DivideGoldActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");
	//当前阶段
	private int stage = 0;
	
	public DivideGoldActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	@Override
	public ActivityType getActivityType() {
		return ActivityType.DIVIDE_GOLD_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DivideGoldActivity activity  = new DivideGoldActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DivideGoldEntity> queryList = HawkDBManager.getInstance()
				.query("from DivideGoldEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DivideGoldEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DivideGoldEntity entity = new DivideGoldEntity(playerId, termId);
		return entity;
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
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		DivideGoldEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	//初始化成就
	private void initAchieve(String playerId){
		Optional<DivideGoldEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		DivideGoldEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		//当前阶段
		int stage = getStageByTime();
		ConfigIterator<DivideGoldAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(DivideGoldAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			DivideGoldAchieveCfg cfg = configIterator.next();
			if (cfg.getRound() == stage) {
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				itemList.add(item);
			}
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
	
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		DivideGoldAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(DivideGoldAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		DivideGoldAchieveCfg cfg =  HawkConfigManager.getInstance().getConfigByKey(DivideGoldAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.DIVIDE_GOLD_TASK_REWARD;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		syncActiviytInfo(playerId);
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		syncAchievePageInfo(playerId);
	}
	
	/**活动信息
	 * @param playerId
	 */
	public void syncActiviytInfo(String playerId){
		DivideGoldInfo.Builder builder = DivideGoldInfo.newBuilder();
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return ;
		}
		DivideGoldEntity entity = optional.get();
		String winRecord = SerializeHelper.collectionToString(entity.getWinRecordList(), SerializeHelper.ELEMENT_DELIMITER);
		builder.setRedEnvelopeRecord(winRecord);
		long allServerNum = getCalculaRedEnvelopeNum();
		builder.setAllSeverRedNum(allServerNum);
		String numStr = ActivityLocalRedis.getInstance().hget(ActivityRedisKey.DIVIDE_GOLD_RECEIVE_FUZI_KEY, playerId);
		int recNum = StringUtils.isEmpty(numStr)? 0 : Integer.valueOf(numStr);
		builder.setGiveNum(entity.getGiveNum());
		builder.setRecNum(recNum);
		//push
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.DIVIDE_GOLD_INFO_SYN_VALUE, builder));
	}
	
	
	@Override
	public void onOpen() {
		this.stage = getStageByTime();
	}
	
	@Override
	public void onTick() {
		//检测跑马灯
		globalServerNoticeCheck();
		//刷新Achieve
		if (stage == 0) {
			this.stage = getStageByTime();
		}
		//刷新Achieve
		if (stage != getStageByTime()) {
			//刷新
			Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
			for (String playerId : playerIds) {
				this.callBack(playerId, MsgId.ACTIVITY_DIVIDE_GOLD_REFRESH_ACHIEVE, () -> {
					syncAchievePageInfo(playerId);
				});
			}
			this.stage = getStageByTime();
		}
		
	/*	long lock = ActivityGlobalRedis.getInstance().getRedisSession().hSetNx("dgrzsl", "lock", getDataGeter().getServerId());
		if (lock > 0) {
			//复制一份数据，注水用
			String key = "divide_gold_red_zhu_shui_num_1";
			long zhuShuiRedNum = NumberUtils.toLong(ActivityGlobalRedis.getInstance().getRedisSession().getString(key));
			if (zhuShuiRedNum == 0) {
				//取之前的数据
				zhuShuiRedNum = getCalculaRedEnvelopeNum() * 100;
				//存新注水数据
				ActivityGlobalRedis.getInstance().getRedisSession().setString(key, String.valueOf(zhuShuiRedNum), getKeyExpireTime());
				logger.info("DivideGoldActivity onTick copy zhuShuiNum success zhuShuiRedNum:{} , ServerId:{}", zhuShuiRedNum, getDataGeter().getServerId());
			}
		}*/
	}
	
	/**
	 * 全服的跑马灯检测
	 */
	public void globalServerNoticeCheck(){
		//全服redis 跑马灯数据
		Map<String, String> noticeMap = ActivityGlobalRedis.getInstance().hgetAll(globalDivideNoticeKey());
		if (noticeMap != null) {
			//本服redis 跑马灯数据
			Map<String, String> noticedMap = ActivityLocalRedis.getInstance().hgetAll(ActivityRedisKey.DIVIDE_GOLD_LOCAL_NOTICE_KEY);
			for (String uuid : noticeMap.keySet()) {
				if (!noticedMap.keySet().contains(uuid)) {
					//跑马灯
					String playerName = noticeMap.get(uuid);
					sendBroadcast(Const.NoticeCfgId.ACTIVITY_DIVIDE_GOLD_BEST, null, playerName);
					//存数据
					ActivityLocalRedis.getInstance().hsetWithExpire(ActivityRedisKey.DIVIDE_GOLD_LOCAL_NOTICE_KEY, uuid, "notice", getKeyExpireTime());
					//过期
					ActivityLocalRedis.getInstance().expire(ActivityRedisKey.DIVIDE_GOLD_LOCAL_NOTICE_KEY, getKeyExpireTime());
					logger.info("DivideGoldActivity onTick sendBroadcast playerName:{}, uuid:{}", playerName, uuid);
				}
			}
		}
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		DivideGoldEntity entity = optional.get();
		//重置数据
		entity.setGiveNum(0);
		syncActivityDataInfo(playerId);
	}
	
	/**索要福字
	 * @param playerId
	 * @param itemStr 福字的物品数据
	 * @param chatType 在那个频道请求
	 */
	public Result<Void> askForFuZi(String playerId, String itemStr, ChatType chatType){
		if (getDataGeter().isCrossPlayer(playerId)) {
			return Result.fail(Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE);
		}
		if (chatType == ChatType.CHAT_ALLIANCE) {
			String guildId = getDataGeter().getGuildId(playerId);
			if (HawkOSOperator.isEmptyString(guildId)) {
				return Result.fail(Status.Error.GUILD_NO_JOIN_VALUE);
			}
		}
		// 道具合不合法
		List<RewardItem.Builder> sendItemList = RewardHelper.toRewardItemList(itemStr);
		// 判断合法？
		if (sendItemList.size() != 1) {
			return Result.fail(Status.Error.ASK_FUZI_NOT_ITEM_CFG_VALUE);
		}		
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.DIVIDE_GOLD_ACTIVITY_NOT_OPEN_VALUE);
		}
		// 判断活动是否在集福阶段，阶段是否结束
		long nowTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		DivideGoldActivityTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(DivideGoldActivityTimeCfg.class, termId);
		//未在集福字阶段
		if (nowTime < timeCfg.getChestStartTimeValue() || nowTime > timeCfg.getChestEndTimeValue()) {
			return Result.fail(Status.Error.DIVIDE_GOLD_FUZI_NOT_OPEN_VALUE);
		}
		DivideGoldEntity entity = optional.get();
		//没到cd时间
		if (entity.getAskForTime() > nowTime) {
			return Result.fail(Status.Error.ASK_FUZI_ASK_CD_VALUE);
		}
		DivideGoldActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
		
		String numStr = ActivityLocalRedis.getInstance().hget(ActivityRedisKey.DIVIDE_GOLD_RECEIVE_FUZI_KEY, playerId);
		int recNum = StringUtils.isEmpty(numStr)? 0 : Integer.valueOf(numStr);
		if (recNum >= kvCfg.getDailyAskFor()) {
			return Result.fail(Status.Error.DIVIDE_ASK_FUZI_LIMIT_VALUE);
		}
		entity.setAskForTime(nowTime + kvCfg.getGetCD() * 1000);
		//发聊天请求福字 信息
		String uuId = HawkUUIDGenerator.genUUID();
		addWorldBroadcastMsg(chatType, NoticeCfgId.ASK_FOR_FU_ZI, playerId, playerId, uuId, itemStr);
		logger.info("askForFuZi playerId:{},  chatType:{}, uuId:{},itemStr:{}", playerId, chatType.getNumber(), uuId, itemStr);
		return Result.success();
	}
	/**赠送福字
	 * @param playerId
	 * @param toPlayerId 
	 * @param itemStr 
	 * @param uuid 请求消息的标识，每条请求，只能赠送一次
	 */
	public Result<Void> giveFuZi(String playerId, String toPlayerId, String itemStr, String uuid){
		try {
			if (getDataGeter().isCrossPlayer(playerId)) {
				return Result.fail(Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE);
			}
			if (playerId.equals(toPlayerId)) {
				return Result.fail(Status.Error.DIVIDE_GIVE_FUZI_NO_SELF_VALUE); 
			}
			List<RewardItem.Builder> giveItemList = RewardHelper.toRewardItemList(itemStr);
			//道具不合法
			if (giveItemList.size() != 1) {
				return Result.fail(Status.Error.ASK_FUZI_NOT_ITEM_CFG_VALUE); 
			}
			Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
			if (!optional.isPresent()) {
				return Result.fail(Status.Error.DIVIDE_GOLD_ACTIVITY_NOT_OPEN_VALUE);
			}
			// 判断活动是否在集福阶段，阶段是否结束
			long nowTime = HawkTime.getMillisecond();
			int termId = getActivityTermId();
			DivideGoldActivityTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(DivideGoldActivityTimeCfg.class, termId);
			//未在集福字阶段
			if (nowTime < timeCfg.getChestStartTimeValue() || nowTime > timeCfg.getChestEndTimeValue()) {
				return Result.fail(Status.Error.DIVIDE_GOLD_FUZI_NOT_OPEN_VALUE);
			}
			DivideGoldActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
			DivideGoldEntity entity = optional.get();
			//赠送福字已达上限
			if (entity.getGiveNum() >= kvCfg.getDailyGive()) {
				return Result.fail(Status.Error.DIVIDE_GIVE_FUZI_LIMIT_VALUE);
			}
			//toPlayerId,通过索要被赠送获得道具已达上限(赠送时)
			String numStr = ActivityLocalRedis.getInstance().hget(ActivityRedisKey.DIVIDE_GOLD_RECEIVE_FUZI_KEY, toPlayerId);
			int recNum = StringUtils.isEmpty(numStr)? 0 : Integer.valueOf(numStr);
			if (recNum >= kvCfg.getDailyAskFor()) {
				return Result.fail(Status.Error.DIVIDE_GIVE_FUZI_TARGET_LIMIT_VALUE);
			}
			//uuid为null 则是通过主动界面赠送的,不用校验
			if (!HawkOSOperator.isEmptyString(uuid)) {
				String key = String.format(ActivityRedisKey.DIVIDE_ASK_FOR_FUZI, uuid);
				//查看是否有人赠送过这个信息的福字
				long lock = ActivityLocalRedis.getInstance().hSetNx(key, "fuzi", toPlayerId);
				logger.info("DivideGoldActivity giveFuZi uuidInfo ,key:{}, uuid:{}, lock:{}", key, uuid, lock);
				//lock = 1存储成功,如果之前有数据则 = 0
				if (lock == 0) {
					return Result.fail(Status.Error.ASK_FUZI_GIVE_ALREADY_VALUE);
				}
				//设置过期
				ActivityLocalRedis.getInstance().expire(key, getKeyExpireTime());
			}
			boolean flag = this.getDataGeter().cost(playerId, giveItemList, Action.DIVIDE_GOLD_GIVE_FUZI);
			if (!flag) {
				return Result.fail(Status.Error.ASK_FUZI_NOT_ITEM_VALUE);
			}
			// 邮件发送奖励
			Object[] content;
			content = new Object[1];
			content[0] = getDataGeter().getPlayerName(playerId);
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			//发邮件
			sendMailToPlayer(toPlayerId, MailConst.MailId.DIVIDE_GOLD_GIVE_FU_ZI, title, subTitle, content, giveItemList);
			//修改数据
			entity.setGiveNum(entity.getGiveNum() + 1);
			//存redis
			ActivityLocalRedis.getInstance().hsetWithExpire(ActivityRedisKey.DIVIDE_GOLD_RECEIVE_FUZI_KEY, toPlayerId, String.valueOf(recNum + 1), getKeyDayExpireTime());
			logger.info("DivideGoldActivity giveFuZi ,uuid:{}, fromPlayerId:{}, toPlayerId:{}, itemId:{}, num:{}", uuid,  playerId, toPlayerId, giveItemList.get(0).getItemId(), giveItemList.get(0).getItemCount());
			syncActivityDataInfo(playerId);
			callBack(toPlayerId, MsgId.ACTIVITY_DIVIDE_GOLD_SYNC, () -> {
				syncActiviytInfo(toPlayerId);
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Result.success();
	}
	
	/** 开宝箱 获得福字
	 * @param playerId
	 */
	public Result<?> openTreasureBox(String playerId){
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		// 判断活动是否在开奖阶段，阶段是否结束
		long nowTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		DivideGoldActivityTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(DivideGoldActivityTimeCfg.class, termId);
		//未在集福阶段
		if (nowTime < timeCfg.getChestStartTimeValue() || nowTime > timeCfg.getChestEndTimeValue()) {
			return Result.fail(Status.Error.DIVIDE_GOLD_FUZI_NOT_OPEN_VALUE);
		}
		DivideGoldActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
		//开宝箱的消耗物品,即宝箱本身item
		List<RewardItem.Builder> consumList = kvCfg.getChestConsume();
		//消耗
		boolean flag = this.getDataGeter().cost(playerId, consumList, Action.DIVIDE_GOLD_OPEN_BOX_COST);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		List<RewardItem.Builder> rewardList = getRandomFuZiRewards();
		//发福字
		this.getDataGeter().takeReward(playerId, rewardList, Action.DIVIDE_GOLD_OPEN_BOX_REWARD, true);
		logger.info("openTreasureBox playerId:{}", playerId);
		return Result.success(getOpenBoxResp(rewardList));
	}
	
	/**
	 * 奖励resp
	 * @param rewardList
	 * @return
	 */
	public OpenBoxResponse.Builder getOpenBoxResp(List<RewardItem.Builder> rewardList){
		OpenBoxResponse.Builder result = OpenBoxResponse.newBuilder();
		//result 填充奖励数据
		for (RewardItem.Builder builder : rewardList) {
			result.addReward(builder);
		}
		return result;
	}
	
	/**五个福字合成 红包
	 * @param playerId
	 * @return
	 */
	public Result<Void> compoundRedEnvelope(String playerId){
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.DIVIDE_GOLD_ACTIVITY_NOT_OPEN_VALUE);
		}
		// 判断活动是否在集福阶段，阶段是否结束
		long nowTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		DivideGoldActivityTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(DivideGoldActivityTimeCfg.class, termId);
		//未在集福字阶段
		if (nowTime < timeCfg.getChestStartTimeValue() || nowTime > timeCfg.getChestEndTimeValue()) {
			return Result.fail(Status.Error.DIVIDE_GOLD_FUZI_NOT_OPEN_VALUE);
		}
		DivideGoldActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
		DivideGoldEntity entity = optional.get();
		//合成红包上限
		if (entity.getCompoundRedNum() >= kvCfg.getRedEnvelopeLimit()) {
			return Result.fail(Status.Error.DIVIDE_GOLD_COMPOUND_RED_LIMIT_VALUE);
		}
		//合红包的消耗物品,即5个福字
		List<RewardItem.Builder> consumList = kvCfg.getRedEnvelopeConsume();
		//消耗
		boolean flag = this.getDataGeter().cost(playerId, consumList, Action.DIVIDE_GOLD_MAKE_RED_ENVELOPE_COST);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//开宝箱的消耗物品,即宝箱本身item
		List<RewardItem.Builder> redRewardList = kvCfg.getRedEnvelopeReward();
		//3.添加红包 
		this.getDataGeter().takeReward(playerId, redRewardList, Action.DIVIDE_GOLD_MAKE_RED_ENVELOPE_REWARD, true);
		//
		entity.setCompoundRedNum(entity.getCompoundRedNum() + 1);
		//全服增加红包合成的数量
		long allServerRedNum = ActivityGlobalRedis.getInstance().increase(globalDivideGoldRedNumKey());
		//过期
		ActivityGlobalRedis.getInstance().expire(globalDivideGoldRedNumKey(), getKeyExpireTime());
		/*//满足配置次数，存时间数据,注水公式用(仅存一次) ----老版本注水公式用的注释掉
		if (allServerRedNum == kvCfg.getAct150Pram_M()) {
			ActivityGlobalRedis.getInstance().set(globalDivideGoldUpLimitTimeKey(), String.valueOf(nowTime), getKeyExpireTime());
		}*/
		////////////////////////
		//更新代码
		String key = getDivideGoldZhuShuiNumKey();
		//注水数据
		long zhuShuiRedNum = NumberUtils.toLong(ActivityGlobalRedis.getInstance().getRedisSession().getString(key));
		long realAddNum = 0;
		//if (zhuShuiRedNum > 0) {
		//初始默认是微信系数
		long zhuShuiRate = kvCfg.getExpandRate_wx(); 	//注水系数
		long maxLimit = kvCfg.getMaxLimit_wx(); 		//临界值
		long suoShuiRate = kvCfg.getReduceRate_wx();	//缩水系数
		int areaId = Integer.valueOf(this.getDataGeter().getAreaId());
		//手Q系数  1微信 2手Q
		if (areaId == 2) {
			zhuShuiRate = kvCfg.getExpandRate_qq();
			maxLimit = kvCfg.getMaxLimit_qq();
			suoShuiRate = kvCfg.getReduceRate_qq();
		}
		//没到临界值用注水系数，否则用缩水系数
		if (zhuShuiRedNum < maxLimit) {
			realAddNum = zhuShuiRate;
		}else{
			realAddNum = suoShuiRate;
		}
		//存新注水数据
		ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(key, realAddNum, getKeyExpireTime());
		//}
		////////////////////////
		//push
		syncActivityDataInfo(playerId);
		logger.info("compoundRedEnvelope playerId:{},  allServerRedNum:{}, allServerZhuShuiRedNum:{}, realAddNum:{},", playerId, allServerRedNum, zhuShuiRedNum, realAddNum);
		return Result.success();
	}
	
	/** 红包开奖
	 * @param playerId
	 * @return
	 */
	public Result<Void> openRedEnvelope(String playerId){
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		DivideGoldEntity entity = optional.get();
		// 判断活动是否在开奖阶段，阶段是否结束
		long nowTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		DivideGoldActivityTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(DivideGoldActivityTimeCfg.class, termId);
		//未在红包开奖阶段
		if (nowTime < timeCfg.getRewardTimeValue() || nowTime > timeCfg.getRewardEndTimeValue()) {
			return Result.fail(Status.Error.DIVIDE_GOLD_RED_NOT_OPEN_VALUE);
		}
		DivideGoldActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
		//消耗的红包
		List<RewardItem.Builder> consumList = kvCfg.getRedEnvelopeReward();
		//消耗
		boolean flag = this.getDataGeter().cost(playerId, consumList, Action.DIVIDE_GOLD_MAKE_RED_ENVELOPE_COST);
		if (!flag) {
			return Result.fail(Status.Error.DIVIDE_GOLD_RED_NUM_NOT_ENOUGH_VALUE);
		}
		DivideGoldGoldWeightCfg goldCfg = getRandomGoldCfg();
		//随机的金条
		int goldNum = goldCfg.getGoldNum();
		entity.addWinRecordList(goldNum);
		//发金币
		Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLD_VALUE, goldNum);
		List<RewardItem.Builder> list = new ArrayList<>();
		list.add(reward);
		this.getDataGeter().takeReward(playerId, list, 1, Action.DIVIDE_GOLD_OPEN_RED_ENVELOPE, true, RewardOrginType.DIVIDE_GOLD_OPEN_ENVELOPE_REWARD);
		logger.info("openRedEnvelope playerId:{}, goldNum:{}", playerId, goldNum);
		//打点
		this.getDataGeter().logDivideGoldOpenRedEnvelope(playerId, goldNum);
		//push
		syncActivityDataInfo(playerId);
		//跑马灯
		if (goldCfg.getGoldNum() == kvCfg.getNoticeCondition()) {
			String uuid = HawkUUIDGenerator.genUUID();
			String playerName = getDataGeter().getPlayerName(playerId);
			//跑马灯信息存redis
			ActivityGlobalRedis.getInstance().hset(globalDivideNoticeKey(), uuid, playerName, getKeyExpireTime());
		}
		return Result.success();
	}

	/**
	 * 同步活动最新任务列表
	 * @param playerId
	 */
	public void syncAchievePageInfo(String playerId){
		//判断玩家任务是否需要刷新
		boolean isRefresh = isRefreshAchieve(playerId);
		if (isRefresh) {
			refreshAchieveInfo(playerId);
		}
	}
	/**任务数据是否刷新
	 * @param playerId
	 * @return
	 */
	public boolean isRefreshAchieve(String playerId){
		Optional<DivideGoldEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return false;
		}
		DivideGoldEntity entity = optional.get();
		//上次刷新时间
		long lastRefreshTime = entity.getLastRefreshTime();
		long nowTime = HawkTime.getMillisecond();
		//异常1, 上次刷新时间等于当前时间不处理
		if (lastRefreshTime == nowTime) {
			return false;
		}
		//异常2, 上次刷新时间大于当前时间，则重置
		if (lastRefreshTime > nowTime) {
			return true;
		}
		//不是同一天,直接刷
		if (!HawkTime.isSameDay(nowTime, lastRefreshTime)) {
			return true;
		}
		DivideGoldActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
		int[] refreshTimeArray = kvCfg.getRefreshTime();
		for (int i = 0; i < refreshTimeArray.length; i++) {
			long refreshTime = HawkTime.getHourOfDayTime(nowTime, refreshTimeArray[i]);
			if (lastRefreshTime < refreshTime && nowTime >= refreshTime) {
				return true;
			}
		}
		return false;
	}

	/**刷新任务数据
	 * @param playerId
	 */
	public void refreshAchieveInfo(String playerId){
		Optional<DivideGoldEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		DivideGoldEntity entity = optional.get();
		//表里取
		ConfigIterator<DivideGoldAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(DivideGoldAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		//当前阶段(直接用stage有几秒误差，so重算下)
		int nowStage = getStageByTime();
		while(configIterator.hasNext()){
			DivideGoldAchieveCfg cfg = configIterator.next();
			if (cfg.getRound() == nowStage) {
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				itemList.add(item);
			}
		}
		entity.resetItemList(itemList);		
		entity.setLastRefreshTime(HawkTime.getMillisecond());
		logger.info("DivideGoldActivity refreshAchieveInfo playerId:{}, stage:{} ", playerId, nowStage);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
	}
	
	/**根据当前时间，判断是那个阶段，0,8,16点，那个档位的任务库
	 * @return
	 */
	public int getStageByTime(){
		int nowHours = HawkTime.getHour();
		DivideGoldActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
		int[] refreshTimeArray = kvCfg.getRefreshTime();
		for (int i = refreshTimeArray.length - 1; i >= 0; i--) {
			int cfgHours = refreshTimeArray[i];
			if (nowHours >= cfgHours ) {
				return i + 1;
			}
		}
		return 0;
	}
	
	/**
	 * 红包随机开奖金币
	 * @return
	 */
	private DivideGoldGoldWeightCfg getRandomGoldCfg(){
		ConfigIterator<DivideGoldGoldWeightCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(DivideGoldGoldWeightCfg.class);
		Map<DivideGoldGoldWeightCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			DivideGoldGoldWeightCfg cfg = configItrator.next();
			map.put(cfg, cfg.getWeight());
		}
		DivideGoldGoldWeightCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found DivideGoldGoldWeightCfg:" + map);
		}
		return chose;
	}
	
	/**
	 * 宝箱随机开福字
	 * @return
	 */
	private List<RewardItem.Builder> getRandomFuZiRewards(){
		ConfigIterator<DivideGoldChestWeightCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(DivideGoldChestWeightCfg.class);
		Map<DivideGoldChestWeightCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			DivideGoldChestWeightCfg cfg = configItrator.next();
			map.put(cfg, cfg.getWeight());
		}
		DivideGoldChestWeightCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found DivideGoldChestWeightCfg:" + map);
		}
		return chose.getRewardItem();
	}
	
	/**
	 * redis保存到活动结束 加1天.
	 * @return
	 */
	public int getKeyExpireTime() {
		long endTime = this.getTimeControl().getHiddenTimeByTermId(this.getActivityTermId());
		long curTime = HawkTime.getMillisecond();
		int remainTime = (int)((endTime - curTime) / 1000) ;
		return remainTime + 864000;
	}
	
	/**当天过期时间
	 * @return
	 */
	public int getKeyDayExpireTime() {
		long nowTime = HawkTime.getMillisecond();
		long endTime = HawkTime.getNextAM0Date();
		int remainTime = (int)((endTime - nowTime) / 1000) ;
		return remainTime;
	}
	
	
	/**获取全服合成红包数量，真实数据/注水数据
	 * @return
	 */
	public long getCalculaRedEnvelopeNum(){
		//新的红包总数，更新之后用这个新的注水数据
		String key = getDivideGoldZhuShuiNumKey();
		long allServerZhuShuiRedNum = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(key));
		if (allServerZhuShuiRedNum > 0) {
			return allServerZhuShuiRedNum/100;
		}
		
		return 0;
		//复制之后不会走到下面逻辑
	
		/*//全服增加红包合成的数量
		long allServerRedNum = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(globalDivideGoldRedNumKey()));
		DivideGoldActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
		int m = kvCfg.getAct150Pram_M();	//60000;
		//1此条件返回真实值
		if (allServerRedNum < m) {
			return allServerRedNum;
		}
		//2 查询合成总数达到M的时间点,没有即还没达到直接返回真实值
		long upLimitTime = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(globalDivideGoldUpLimitTimeKey()));
		if (upLimitTime == 0) {
			return allServerRedNum;
		}
		int termId = getActivityTermId();
		DivideGoldActivityTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(DivideGoldActivityTimeCfg.class, termId);
		//T1 真实值，达到M值的时间 间隔, 单位秒
		double T1 = (upLimitTime - timeCfg.getStartTimeValue())/1000;
		//现在时间(开奖时间不计算，所以，nowTime如果在开奖期间，则取开奖阶段开始时间)
		long nowTime = (HawkTime.getMillisecond() > timeCfg.getChestEndTimeValue()) ? timeCfg.getChestEndTimeValue() : HawkTime.getMillisecond();
		//现在时间-开启时间 单位秒
		long t = (nowTime - timeCfg.getStartTimeValue())/1000;
		
		double a = kvCfg.getAct150Pram_a();  //1.05f;
		int b = kvCfg.getAct150Pram_b();	//3141592;
		double c = kvCfg.getAct150Pram_c();	//1800;
		int d = kvCfg.getAct150Pram_d();	//5000;
		//注水公式
		double T2 = (c * (Math.log((d*m)/(b-m))/Math.log(a)));
		double T0 = T1 - T2; //169200;
		double M2 = b*(Math.pow(a, ((t-T0)/c))/(d + Math.pow(a, ((t-T0)/c))));
		return (long)M2;*/
		
	}
	
	
	/**
	 * 瓜分金币活动 全服合成红包次数key
	 */
	private String globalDivideGoldRedNumKey() {
		return "divide_gold_red_num_" + this.getActivityTermId();
	}
	/**
	 * 全服累计合成红包数量达到配置时的时间long key
	 */
	private String globalDivideGoldUpLimitTimeKey() {
		return "divide_gold_num_limit_" + this.getActivityTermId();
	}
	
	/**
	 * 全服跑马灯key
	 */
	private String globalDivideNoticeKey() {
		return "divide_gold_all_notice_" + this.getActivityTermId();
	}
	
	/**合成红包注水key
	 * @return
	 */
	private String getDivideGoldZhuShuiNumKey(){
		return "divide_gold_zhu_shui_number_" + this.getActivityTermId();
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	public int getStage() {
		return stage;
	}
	public void setStage(int stage) {
		this.stage = stage;
	}
	
}
