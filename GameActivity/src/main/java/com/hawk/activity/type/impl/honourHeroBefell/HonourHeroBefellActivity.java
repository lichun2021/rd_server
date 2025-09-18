package com.hawk.activity.type.impl.honourHeroBefell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.HonourHeroBefellLuckyCountEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.honourHeroBefell.cfg.HonourHeroBefellAchieveCfg;
import com.hawk.activity.type.impl.honourHeroBefell.cfg.HonourHeroBefellExchangeCfg;
import com.hawk.activity.type.impl.honourHeroBefell.cfg.HonourHeroBefellKVCfg;
import com.hawk.activity.type.impl.honourHeroBefell.cfg.HonourHeroBefellRewardCfg;
import com.hawk.activity.type.impl.honourHeroBefell.entity.HonourHeroBefellEntity;
import com.hawk.game.protocol.Activity.PBHHeroBefellTipAction;
import com.hawk.game.protocol.Activity.PBHonourHeroBefellExchange;
import com.hawk.game.protocol.Activity.PBHonourHeroBefellLotteryResp;
import com.hawk.game.protocol.Activity.PBHonourHeroBefellResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 荣耀英雄降临-荣耀凯恩
 * @author che
 *
 */
public class HonourHeroBefellActivity extends ActivityBase  implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public HonourHeroBefellActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HONOUR_HERO_BEFELL_ACTIVITY;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HonourHeroBefellActivity activity = new HonourHeroBefellActivity(
				config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HonourHeroBefellEntity> queryList = HawkDBManager.getInstance()
				.query("from HonourHeroBefellEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HonourHeroBefellEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HonourHeroBefellEntity entity = new HonourHeroBefellEntity(playerId, termId);
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
		Optional<HonourHeroBefellEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		HonourHeroBefellEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId,entity);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity, true, getActivityId(), entity.getTermId());
		return Optional.of(items);
	}
	
	
	//初始化成就
	private void initAchieve(String playerId,HonourHeroBefellEntity entity){
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<HonourHeroBefellAchieveCfg> configIterator = HawkConfigManager.getInstance().
				getConfigIterator(HonourHeroBefellAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			HonourHeroBefellAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
		
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.HONOUR_HERO_BEFELL_INIT, () -> {
				Optional<HonourHeroBefellEntity>  optional = this.getPlayerDataEntity(playerId);
				if (!optional.isPresent()) {
					return;
				}
				HonourHeroBefellEntity entity = optional.get();
				this.initAchieve(playerId,entity);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		Optional<HonourHeroBefellEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		//抛事件
		HonourHeroBefellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroBefellKVCfg.class);
		HonourHeroBefellEntity entity = opPlayerDataEntity.get();
		int luckyCount =(entity.getTotalLotteryCount() + entity.getTotalFreeLotteryCount())* cfg.getLuckyPer();
		ActivityManager.getInstance().postEvent(new HonourHeroBefellLuckyCountEvent(playerId, luckyCount), true);
	}
	
	
	
	/**
	 * 选择
	 * @param playerId
	 */
	public void lottery(String playerId,int type){
		if(type== 1){
			this.lotteryRewardsOne(playerId);
		}else if(type == 10){
			this.lotteryRewardsTen(playerId);
		}
	}
	
	/**
	 * 跨天刷新信息到客户端
	 * @param event
	 */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<HonourHeroBefellEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		this.syncActivityInfo(playerId, opPlayerDataEntity.get());
	}
	/**
	 * 10抽
	 * @param playerId
	 */
	private void lotteryRewardsTen(String playerId){
		Optional<HonourHeroBefellEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		HonourHeroBefellEntity entity = optional.get();
		HonourHeroBefellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroBefellKVCfg.class);
		List<RewardItem.Builder> costList = cfg.getTenCostItemList();
		//次数不足
		int lCount = entity.getTotalLotteryCount();
		if(lCount + 10 > cfg.getLimitTimes()){
			logger.info("HonourHeroBefellActivity,lotteryRewardsTen,fail,countless,playerId: "
					+ "{},curCount:{},lotteryCount:{}", playerId,lCount,10);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code2.HONOUR_HERO_BEFELL_LOTTERY_REQ_VALUE, Status.Error.HONOUR_HERO_BEFELL_LOTTERY_COUNT_LESS_VALUE);
			return;
		}
		//检查消耗
		boolean cost = this.getDataGeter().cost(playerId,costList, 1, 
				Action.HONOUR_HERO_BEFELL_LOTTERY_COST,false);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code2.HONOUR_HERO_BEFELL_LOTTERY_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		List<Integer> ranList = this.getRandomReward(10);
		if(ranList.isEmpty()){
			return;
		}
		List<RewardItem.Builder> allReward = new ArrayList<>();
		for(int rid : ranList){
			HonourHeroBefellRewardCfg rcfg = HawkConfigManager.getInstance()
					.getConfigByKey(HonourHeroBefellRewardCfg.class, rid);
			if(rcfg == null){
				continue;
			}
			allReward.addAll(rcfg.getRewardItemList());
		}
		//固定奖励
		List<RewardItem.Builder> fixReward = cfg.getFixItemList(10);
		allReward.addAll(fixReward);
		this.getDataGeter().takeReward(playerId, allReward, 1, 
				Action.HONOUR_HERO_BEFELL_LOTTERY_REWARD, true,RewardOrginType.HONOUR_HERO_BEFELL_LOTTERY_REWARD);
		entity.addTenLotteryCount();
		//抛事件
		int luckyCount =(entity.getTotalLotteryCount() + entity.getTotalFreeLotteryCount())* cfg.getLuckyPer();
		ActivityManager.getInstance().postEvent(new HonourHeroBefellLuckyCountEvent(playerId, luckyCount), true);
		//返回数据
		PBHonourHeroBefellLotteryResp.Builder builder = PBHonourHeroBefellLotteryResp.newBuilder();
		builder.addAllRewards(ranList);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.HONOUR_HERO_BEFELL_LOTTERY_RESP_VALUE,builder));
		//刷新数据
		this.syncActivityInfo(playerId, entity);
		this.getDataGeter().logHonourHeroBefellLottery(playerId, entity.getTermId(), 10, StringUtils.join(ranList, ","));
		logger.info("HonourHeroBefellActivity ten lottery, playerId: {},result:{}",playerId,ranList);
	}
	
	/**
	 * 单抽
	 * @param playerId
	 */
	private void lotteryRewardsOne(String playerId){
		Optional<HonourHeroBefellEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		HonourHeroBefellEntity entity = optional.get();
		HonourHeroBefellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroBefellKVCfg.class);
		//单抽会有免费次数，用完免费次数，每天还有打折次数
		boolean free = this.oneLotteryFree(entity);
		//次数不足
		int lCount = entity.getTotalLotteryCount();
		if(!free && (lCount + 1 > cfg.getLimitTimes())){
			logger.info("HonourHeroBefellActivity,lotteryRewardsOne,fail,countless,playerId: "
					+ "{},curCount:{},lotteryCount:{}", playerId,lCount,1);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code2.HONOUR_HERO_BEFELL_LOTTERY_REQ_VALUE, Status.Error.HONOUR_HERO_BEFELL_LOTTERY_COUNT_LESS_VALUE);
			return;
		}
		if(!free){
			//检查消耗
			List<RewardItem.Builder> costList = cfg.getOneCostItemList();
			boolean cost = this.getDataGeter().cost(playerId,costList, 1, 
					Action.HONOUR_HERO_BEFELL_LOTTERY_COST,false);
			if (!cost) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
						HP.code2.HONOUR_HERO_BEFELL_LOTTERY_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
				return;
			}
		}
		List<Integer> ranList = this.getRandomReward(1);
		if(ranList.isEmpty()){
			return;
		}
		List<RewardItem.Builder> allReward = new ArrayList<>();
		for(int rid : ranList){
			HonourHeroBefellRewardCfg rcfg = HawkConfigManager.getInstance()
					.getConfigByKey(HonourHeroBefellRewardCfg.class, rid);
			if(rcfg == null){
				continue;
			}
			allReward.addAll(rcfg.getRewardItemList());
		}
		//固定奖励
		List<RewardItem.Builder> fixReward = cfg.getFixItemList(1);
		allReward.addAll(fixReward);
		this.getDataGeter().takeReward(playerId, allReward, 
				1, Action.HONOUR_HERO_BEFELL_LOTTERY_REWARD, true,RewardOrginType.HONOUR_HERO_BEFELL_LOTTERY_REWARD);
		if(free){
			entity.addUseFreeLotteryCountToday();
		}else{
			entity.addOneLotteryCount();
		}
		//抛事件
		int luckyCount =(entity.getTotalLotteryCount() + entity.getTotalFreeLotteryCount())* cfg.getLuckyPer();
		ActivityManager.getInstance().postEvent(new HonourHeroBefellLuckyCountEvent(playerId, luckyCount), true);
		//返回数据
		PBHonourHeroBefellLotteryResp.Builder builder = PBHonourHeroBefellLotteryResp.newBuilder();
		builder.addAllRewards(ranList);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.HONOUR_HERO_BEFELL_LOTTERY_RESP_VALUE,builder));
		//刷新数据
		this.syncActivityInfo(playerId, entity);
		this.getDataGeter().logHonourHeroBefellLottery(playerId, entity.getTermId(), 1, StringUtils.join(ranList, ","));
		logger.info("HonourHeroBefellActivity one lottery, playerId: {},result:{}",playerId,ranList);
	}
	
	
	/**
	 * 物品兑换
	 * @param playerId
	 * @param exchangeId
	 * @param exchangeCount
	 * @param protocolType
	 */
	public void itemExchange(String playerId,int exchangeId,int exchangeCount){
		HonourHeroBefellExchangeCfg config = HawkConfigManager.getInstance().
				getConfigByKey(HonourHeroBefellExchangeCfg.class, exchangeId);
		if (config == null) {
			return;
		}
		HonourHeroBefellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroBefellKVCfg.class);
		boolean limit = this.getDataGeter().getHeroInfo(playerId, cfg.getExchangeHeroLimit())== null;
		if(limit){
			return;
		}
		Optional<HonourHeroBefellEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HonourHeroBefellEntity entity = opDataEntity.get();
		int eCount = entity.getExchangeCount(exchangeId);
		if(eCount + exchangeCount > config.getTimes()){
			logger.info("HonourHeroBefellActivity,itemExchange,fail,countless,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
			return;
		}
		
		List<RewardItem.Builder> makeCost = config.getNeedItemList();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, exchangeCount, Action.HONOUR_HERO_BEFELL_EXCAHNGE_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code2.HONOUR_HERO_BEFELL_EXCHANGE_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		
		//增加兑换次数
		entity.addExchangeCount(exchangeId, exchangeCount);
		//发奖励
		this.getDataGeter().takeReward(playerId, config.getGainItemList(), 
				exchangeCount, Action.HONOUR_HERO_BEFELL_EXCAHNGE_GAIN, true);
		//同步
		this.syncActivityInfo(playerId,entity);
		logger.info("HonourHeroBefellActivity,itemExchange,sucess,playerId: "
				+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
	}
	
	
	/**
	 * 更新管制
	 * @param playerId
	 * @param id
	 * @param tips
	 * @return
	 */
	public void updateActivityTips(String playerId, List<PBHHeroBefellTipAction> actions){
		if(!isOpening(playerId)){
			return;
		}
		if(actions == null || actions.size() <= 0){
			return;
		}
		Optional<HonourHeroBefellEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return;
		}
		HonourHeroBefellEntity entity = opt.get();
		for(PBHHeroBefellTipAction action : actions){
			int id = action.getId();
			int tip = action.getTip();
			HonourHeroBefellExchangeCfg config = HawkConfigManager.getInstance().
					getConfigByKey(HonourHeroBefellExchangeCfg.class, id);
			if (config == null) {
				continue;
			}
			if(tip > 0){
				entity.removeTips(id);
			}else{
				entity.addTips(id);
			}
		}
		this.syncActivityInfo(playerId, entity);
	}
	
	

	
	/**
	 * 单抽是否免费
	 * @param entity
	 * @return
	 */
	private boolean oneLotteryFree(HonourHeroBefellEntity entity){
		HonourHeroBefellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroBefellKVCfg.class);
		int freeCount = cfg.getFreeTimes();
		if(entity.getUseFreeLotteryCountToday() >= freeCount){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 随机奖励
	 * @param count
	 * @return
	 */
	private List<Integer> getRandomReward(int count){
		List<Integer> list = new ArrayList<>();
		List<HonourHeroBefellRewardCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(HonourHeroBefellRewardCfg.class).toList();
		for(int i =1; i<=count;i++){
			HonourHeroBefellRewardCfg cfg = HawkRand.randomWeightObject(cfgList);
			list.add(cfg.getId());
		}
		return list;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<HonourHeroBefellEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,HonourHeroBefellEntity entity){
		HonourHeroBefellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroBefellKVCfg.class);
		boolean oneFree = this.oneLotteryFree(entity);
		int lotteryCount = entity.getTotalLotteryCount();
		int luckyCount =(entity.getTotalLotteryCount() + entity.getTotalFreeLotteryCount())* cfg.getLuckyPer();
		List<Integer>carePoints = entity.getPlayerPoints();
		//组织PB数据
		PBHonourHeroBefellResp.Builder builder =PBHonourHeroBefellResp.newBuilder();
		builder.setOneFreeTimes(oneFree?1:0);
		builder.setLotteryCount(lotteryCount);
		builder.setLuckyCount(luckyCount);
		Map<Integer,Integer> emap = entity.getExchangeNumMap();
		for(Entry<Integer, Integer> entry : emap.entrySet()){
			PBHonourHeroBefellExchange.Builder ebuilder = PBHonourHeroBefellExchange.newBuilder();
			ebuilder.setExchangeId(entry.getKey());
			ebuilder.setNum(entry.getValue());
			builder.addExchanges(ebuilder);
		}
		List<HonourHeroBefellExchangeCfg> eList = HawkConfigManager.getInstance()
				.getConfigIterator(HonourHeroBefellExchangeCfg.class).toList();
		for(HonourHeroBefellExchangeCfg ecfg : eList){
			if(!carePoints.contains(ecfg.getId())){
				builder.addTips(ecfg.getId());
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.HONOUR_HERO_BEFELL_INFO_RESP_VALUE,builder));
	}
	
	
	

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().
				getConfigByKey(HonourHeroBefellAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.HONOUR_HERO_BEFELL_ACHIVE_REWARD;
	}

	
	
	
}
