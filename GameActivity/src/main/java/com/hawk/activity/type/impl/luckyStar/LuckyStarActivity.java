package com.hawk.activity.type.impl.luckyStar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkRand;
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
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LuckyStarBuyEvent;
import com.hawk.activity.event.impl.LuckyStarLotteryEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.luckyStar.cfg.LuckyStarAchieveCfg;
import com.hawk.activity.type.impl.luckyStar.cfg.LuckyStarFreeBagCfg;
import com.hawk.activity.type.impl.luckyStar.cfg.LuckyStarGiftCfg;
import com.hawk.activity.type.impl.luckyStar.cfg.LuckyStarKVCfg;
import com.hawk.activity.type.impl.luckyStar.cfg.LuckyStartLotterCfg;
import com.hawk.activity.type.impl.luckyStar.entity.LuckyStarEntity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.luckyStarGiftInfo;
import com.hawk.game.protocol.Activity.luckyStarInfo;
import com.hawk.game.protocol.Activity.luckyStarLotResponse;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class LuckyStarActivity extends ActivityBase implements AchieveProvider{

	private final Logger logger = LoggerFactory.getLogger("Server");
	
	public LuckyStarActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.LUCKY_STAR;
	}

    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.ON_LUCKY_STAR_ACTIVITY_OPEN, () -> {
				playerInitEntity(playerId);
			});
		}
	}
	
	/**
	 * 跨天
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		LuckyStarEntity entity = opEntity.get();
		long lastCrossDayTime = entity.getDayTime();
		if (HawkTime.isSameDay(lastCrossDayTime, now)) {
			return;
		}
		
		entity.setDayTime(now);
		if (lastCrossDayTime == 0) {
			initAchieveItems(event.getPlayerId());
		}
		
		//记录一下日志
		logger.info("luckyStarActivity CrossDay, playerId:" + event.getPlayerId() + ",entity:" + entity);
		entity.crossDay();
		entity.notifyUpdate();
		pushToPlayer(event.getPlayerId(), HP.code.LUCKY_STAR_INFO_S_VALUE, (luckyStarInfo.Builder)reqLuckyStarInfo(event.getPlayerId()).getRetObj());
	}
	
	/***
	 * 幸运星购买事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(LuckyStarBuyEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		LuckyStarEntity entity = opEntity.get();
		String giftId = event.getGiftId();
		String playerId = event.getPlayerId();
		entity.onPlayerBuyGift(giftId);
		pushToPlayer(playerId, HP.code.LUCKY_STAR_INFO_S_VALUE, (luckyStarInfo.Builder)reqLuckyStarInfo(playerId).getRetObj());
	}
	
	public Result<?> reqLuckyStarInfo(String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		LuckyStarEntity entity = opEntity.get();
		luckyStarInfo.Builder build = luckyStarInfo.newBuilder();
		ConfigIterator<LuckyStarGiftCfg> itr = HawkConfigManager.getInstance().getConfigIterator(LuckyStarGiftCfg.class);
		while(itr.hasNext()){
			LuckyStarGiftCfg cfg = itr.next();
			String giftId = cfg.getPayGiftId();
			String platform = getDataGeter().getPlatform(playerId);
			if(platform == null || platform.trim().equals("")){
				logger.error("playerId:" + playerId +" platform msg null.");
				continue;
			}
			if(!platform.equalsIgnoreCase(cfg.getStrPlatform())){
				continue;
			}
			int cueBuyCnt = getDataGeter().getGiftBuyCnt(giftId);
			luckyStarGiftInfo.Builder info = luckyStarGiftInfo.newBuilder();
			info.setGiftId(giftId);
			if(cueBuyCnt == 0){
				info.setLeftBuyCnt(-1);
			}else if(cueBuyCnt > 0){
				info.setLeftBuyCnt(cueBuyCnt - entity.getGiftBuyCnt(giftId));
			}else{
				info.setLeftBuyCnt(0);
			}
			build.addGiftLeftBuyCnt(info);
		}
		build.setSumLotCnt(entity.getLotCnt());
		if(entity.getLastBuyGiftId() != null && !entity.getLastBuyGiftId().equals("")){
			build.setLastGiftId(entity.getLastBuyGiftId());
		}
		if(entity.getTodayRecieveBag() != null && !entity.getTodayRecieveBag().trim().equals("")){
			build.setRecieveBagId(entity.getTodayRecieveBag());
		}
		return Result.success(build);
	}
	
	/***
	 * 抽奖
	 * @param playerId
	 * @param lotCnt 抽奖次数
	 */
	public Result<?> lottery(String playerId, int lotCnt){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		LuckyStarEntity entity = opEntity.get();
		int sumLotCnt = 0; //总的抽奖次数
		LuckyStarKVCfg config = LuckyStarKVCfg.getInstance();
		if(lotCnt != 1 && lotCnt != 10){
			logger.error("client send error lotCnt:" + lotCnt + ",playerId:" + playerId);
			return null;
		}
		//消耗道具
		List<RewardItem.Builder> cost = new ArrayList<RewardItem.Builder>();
		for(int i = 0 ; i < lotCnt ; i ++){
			for(RewardItem.Builder itemBuild : config.getCostList()){
				cost.add(itemBuild);
			}
		}
		boolean flag = this.getDataGeter().cost(playerId, cost, Action.LUCKY_STAR_LOT);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		luckyStarLotResponse.Builder result = luckyStarLotResponse.newBuilder();
		for (int i = 0; i < lotCnt; i++) {
			// 抽奖
			entity.lottery(1);
			sumLotCnt = entity.getLotCnt(); //当前抽奖次数
			//result.addLot(lotOnce(i + 1, sumLotCnt, playerId));
			List<RewardItem.Builder> list = lotOnce(i + 1, sumLotCnt, playerId, lotCnt == 1 ? true : false, result);
			if(list != null){
				for(RewardItem.Builder rb : list){
					result.addReward(rb);
				}
			}
		}
	
		entity.notifyUpdate();
		ActivityManager.getInstance().postEvent(new LuckyStarLotteryEvent(playerId, entity.getLotCnt())); //这里就是要传全量，不传增量，增量有风险
		getDataGeter().logLuckyStarLottery(playerId, lotCnt); 
		pushToPlayer(playerId, HP.code.LUCKY_STAR_INFO_S_VALUE, (luckyStarInfo.Builder)reqLuckyStarInfo(playerId).getRetObj());
		return Result.success(result);
	}
	
	/***
	 * 抽奖一次
	 * @param sumLotCnt
	 * @param playerId
	 * @param single 是否单次抽奖
	 * @return
	 */
	private List<RewardItem.Builder> lotOnce(int index , int sumLotCnt, String playerId, boolean single, luckyStarLotResponse.Builder result){
		//去除保底机制 -- 20240724
//		LuckyStarKVCfg config = LuckyStarKVCfg.getInstance();
//		if(sumLotCnt % config.getLotteryCnt() == 0){
//			int rewardId = config.getRewardId();
//			rewardConfig = HawkConfigManager.getInstance().getConfigByKey(LuckyStartLotterCfg.class, rewardId);			
//		}else{
//		}
		
		LuckyStartLotterCfg rewardConfig = getRate();
		if(single){
			result.setLotId(rewardConfig.getId());
		}
		//给奖励
		this.getDataGeter().takeReward(playerId, rewardConfig.getRewardList(), 1, Action.LUCKY_STAR_LOT, false, RewardOrginType.LUCKY_STAR_GIFT);
		return rewardConfig.getRewardList();
	}
	
	/***
	 * 随机抽取函数
	 * @param ita
	 * @return
	 */
	private LuckyStartLotterCfg getRate(){
		ConfigIterator<LuckyStartLotterCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(LuckyStartLotterCfg.class);
		Map<LuckyStartLotterCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			LuckyStartLotterCfg cfg = configItrator.next();
			map.put(cfg, cfg.getSingleWeight());
		}
		LuckyStartLotterCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found LuckyStartLotterCfg:" + map);
		}
		return chose;
	}
	
	public Result<?> recieveFreeBag(String playerId, String freeBagId){		
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		LuckyStarFreeBagCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LuckyStarFreeBagCfg.class, freeBagId);
		if(cfg == null){
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		LuckyStarEntity entity = opEntity.get();
		if(!entity.hasFreeBag()){
			return Result.fail(Status.Error.ACTIVITY_CAN_NOT_TAKE_REWARD_VALUE);
		}
		//给奖励
		this.getDataGeter().takeReward(playerId, cfg.getRewardList(), 1, Action.LUCKY_STAR_FERR_BAG, true, RewardOrginType.LUCKY_STAR_FREE_BAG);
		entity.setTodayRecieveBag(freeBagId);
		//刷新一次界面
		pushToPlayer(playerId, HP.code.LUCKY_STAR_INFO_S_VALUE, (luckyStarInfo.Builder)reqLuckyStarInfo(playerId).getRetObj());
		return Result.success();
	}
	
	private void playerInitEntity(String playerId){
		Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("on luckystarActivity open init luckystarEntity error, no entity created");
		}
		
		LuckyStarEntity entity = opEntity.get();
		long now = HawkTime.getMillisecond();
		long lastCrossDayTime = entity.getDayTime();
		if (!HawkTime.isSameDay(lastCrossDayTime, now)) {
			entity.setDayTime(now);
		}
		
		initAchieveItems(playerId);
		pushToPlayer(playerId, HP.code.LUCKY_STAR_INFO_S_VALUE, (luckyStarInfo.Builder)reqLuckyStarInfo(playerId).getRetObj());
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				logger.error("on playerLogin init luckystarEntity error, no entity created");
			}	
			//同步界面信息给客户端
			pushToPlayer(playerId, HP.code.LUCKY_STAR_INFO_S_VALUE, (luckyStarInfo.Builder)reqLuckyStarInfo(playerId).getRetObj());
		}
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LuckyStarActivity activity = new LuckyStarActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LuckyStarEntity> queryList = HawkDBManager.getInstance()
				.query("from LuckyStarEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LuckyStarEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LuckyStarEntity entity = new LuckyStarEntity(playerId, termId);
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
		return !isHidden(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		LuckyStarEntity entity = opEntity.get();
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(LuckyStarAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.LUCKY_STAR_ACHIEVE_REWARD;
	}

	/***
	 * 初始化成就
	 * @param playerId
	 * @return
	 */
	public void initAchieveItems(String playerId) {
		Optional<LuckyStarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		LuckyStarEntity entity = opEntity.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		ConfigIterator<LuckyStarAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(LuckyStarAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			LuckyStarAchieveCfg cfg = configIterator.next();				
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
			itemList.add(item);
		}
		entity.setItemList(itemList);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	
}
