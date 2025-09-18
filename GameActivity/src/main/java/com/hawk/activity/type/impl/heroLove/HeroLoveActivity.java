package com.hawk.activity.type.impl.heroLove;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.AddHeroLoveScoreEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayHeroLoveEvent;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.heroLove.cfg.HeroLoveAchieveCfg;
import com.hawk.activity.type.impl.heroLove.cfg.HeroLoveActivityKVCfg;
import com.hawk.activity.type.impl.heroLove.cfg.HeroLoveItemCfg;
import com.hawk.activity.type.impl.heroLove.cfg.HeroLoveLevelRewardCfg;
import com.hawk.activity.type.impl.heroLove.entity.HeroLoveEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.HeroLoveGiftItemResp;
import com.hawk.game.protocol.Activity.HeroLovePageInfoResp;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 英雄委任(曾用名)
 * 英雄计划(现用名)
 * @author jm
 *
 */
public class HeroLoveActivity extends ActivityBase implements AchieveProvider{

	public HeroLoveActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);		
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HERO_LOVE;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HeroLoveActivity activity = new HeroLoveActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HeroLoveEntity> queryList = HawkDBManager.getInstance()
				.query("from HeroLoveEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HeroLoveEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HeroLoveEntity heroLoveEntity = new HeroLoveEntity(playerId, termId);
		return heroLoveEntity;
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return true;
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return true;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_HERO_LOVE, ()-> {
				Optional<HeroLoveEntity> opEntity = this.getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				initAchieveItems(playerId, false, opEntity.get());			
			});
		}
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<HeroLoveEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		HeroLoveEntity playerDataEntity = opPlayerDataEntity.get();
		initAchieveItems(playerId, false, playerDataEntity);
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	private void initAchieveItems(String playerId, boolean b, HeroLoveEntity playerDataEntity) {						
		// 成就已初始化
		if (!playerDataEntity.getItemList().isEmpty()) {
			return;
		}

		// 初始添加成就项
		ConfigIterator<HeroLoveAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HeroLoveAchieveCfg.class);
		for (HeroLoveAchieveCfg cfg : configIterator) {			
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			playerDataEntity.addItem(item);			
		}

		// 积分成就
		ConfigIterator<HeroLoveLevelRewardCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(HeroLoveLevelRewardCfg.class);
		for (HeroLoveLevelRewardCfg cfg : scoreAchieveIt) {
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			playerDataEntity.addItem(item);
		}		

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, playerDataEntity.getItemList()), true);
		//登录成就发一下
		ActivityManager.getInstance().postEvent(new LoginDayHeroLoveEvent(playerId, 1));
		playerDataEntity.setLastLoginTime(HawkTime.getMillisecond());
		
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig achieveConfig = HawkConfigManager.getInstance().getConfigByKey(HeroLoveAchieveCfg.class, achieveId);
		if (achieveConfig == null) {
			achieveConfig = HawkConfigManager.getInstance().getConfigByKey(HeroLoveLevelRewardCfg.class, achieveId);
		}
		
		return achieveConfig;
	}

	@Override
	public Action takeRewardAction() {
		return Action.HERO_LOVE_ACHIEVE;
	}	
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {		 			
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	private int getMaxScore() {
		return this.getKvCfg().getMaxScore();  
	}
	private HeroLoveActivityKVCfg getKvCfg() {
		return HawkConfigManager.getInstance().getConfigByIndex(HeroLoveActivityKVCfg.class, 0); 
	}
	@Override
	public void syncActivityDataInfo(String playerId) {
		HeroLovePageInfoResp.Builder builder = genPageInfo(playerId);
		if (builder != null) {
			pushToPlayer(playerId, HP.code.HERO_LOVE_PAGE_INFO_RESP_VALUE, builder);
		}
	}
	public HeroLovePageInfoResp.Builder genPageInfo(String playerId) {
		Optional<HeroLoveEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return null;
		}		
		HeroLoveEntity entity = opPlayerDataEntity.get();
		HeroLovePageInfoResp.Builder builder = HeroLovePageInfoResp.newBuilder();
		builder.setScore(entity.getScore());
		
		return builder;
	}
	

	/**
	 * 赠送道具,增加亲密度.
	 * @param id
	 * @param num
	 * @return
	 */
	public int giveItem(String playerId, int id, int num) {
		super.logger.info("playerId:{}, id:{}, num:{}", playerId, id, num);
		HeroLoveItemCfg heroLoveItemCfg = HawkConfigManager.getInstance().getConfigByKey(HeroLoveItemCfg.class, id);
		if (heroLoveItemCfg == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		//数组必须大于0且不大于
		if (num < 0 || num > 999) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Optional<HeroLoveEntity> opPlayerData = this.getPlayerDataEntity(playerId);
		if (!opPlayerData.isPresent()) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		HeroLoveEntity playerData = opPlayerData.get();
		int maxScore = this.getMaxScore();
		if (playerData.getScore() >= maxScore) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		int realNum = 0;		
		if (heroLoveItemCfg.isFree()) {
			/*int needItemNum = (maxScore - playerData.getScore() - 1) / heroLoveItemCfg.getMaxLove() + 1;
			realNum = Math.min(needItemNum, num);*/
			realNum = num;
		} else {
			realNum = 1;
		}
		
		super.logger.info("playerId:{}, id:{}, realNum:{}", playerId, id, realNum);
		List<RewardItem.Builder> costList = new ArrayList<>();
		for (RewardItem.Builder rewardItem : heroLoveItemCfg.getCostItemList()) {
			RewardItem.Builder builder = RewardItem.newBuilder()
					.setItemId(rewardItem.getItemId())
					.setItemType(rewardItem.getItemType())
					.setItemCount(rewardItem.getItemCount() * realNum);
			costList.add(builder);
		} 
		boolean costResult = false;
		if (heroLoveItemCfg.isFree()) {
			costResult = this.getDataGeter().consumeItems(playerId, costList, HP.code.HERO_LOVE_GIFT_ITEM_REQ_VALUE, Action.HERO_LOVE_GIFT_ITEM);			
		} else {
			costResult = this.getDataGeter().consumeItemsIsGold(playerId, costList, HP.code.HERO_LOVE_GIFT_ITEM_REQ_VALUE, Action.HERO_LOVE_GIFT_ITEM);
		}
		
		if (!costResult) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}
		//addSocre todo 这里需要添加一个增加积分的算法.
		int addScore = 0;
		List<Integer> rateList = new ArrayList<>();
		int rate = 0;
		for (int i = 0; i < realNum; i ++) {
			rate = heroLoveItemCfg.getRandomLoveCrit();
			rateList.add(rate);
			addScore += rate * heroLoveItemCfg.getMinLove() / GameConst.RANDOM_MYRIABIT_BASE;
		} 
		int realScore = Math.min(playerData.getScore() + addScore, this.getMaxScore());
		playerData.setScore(realScore);
		AchieveManager.getInstance().onEvent(new AddHeroLoveScoreEvent(playerId, addScore));
		
		HeroLoveGiftItemResp.Builder sbuilder = HeroLoveGiftItemResp.newBuilder();
		sbuilder.setId(id);
		sbuilder.addAllRates(rateList);
		
		this.pushToPlayer(playerId, HP.code.HERO_LOVE_GIFT_ITEM_RESP_VALUE, sbuilder);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (!event.isCrossDay()) {
			return;
		}
		
		Optional<HeroLoveEntity> opEntity = this.getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		HeroLoveEntity heroLoveEntity = opEntity.get();
		if(!HawkTime.isSameDay(HawkTime.getMillisecond(), heroLoveEntity.getLastLoginTime())){
			super.logger.info("receive reset achieve event playerId:{}", event.getPlayerId());
			heroLoveEntity.setLastLoginTime(HawkTime.getMillisecond());
			this.resetAchieve(event.getPlayerId());
			ActivityManager.getInstance().postEvent(new LoginDayHeroLoveEvent(event.getPlayerId(), 1));
		}		
	}
	
	/**
	 * 重置成就
	 */
	public void resetAchieve(String playerId) {
		Optional<HeroLoveEntity> opPlayerData = this.getPlayerDataEntity(playerId);
		if (!opPlayerData.isPresent()) {
			return;
		}
		HeroLoveEntity heroLoveEntity = opPlayerData.get();
		List<AchieveItem> achieveItemList = heroLoveEntity.getItemList();
		for (AchieveItem achieveItem : achieveItemList) {
			HeroLoveAchieveCfg heroLoveAchieveCfg = HawkConfigManager.getInstance().getConfigByKey(HeroLoveAchieveCfg.class, achieveItem.getAchieveId());
			if (heroLoveAchieveCfg != null) {
				achieveItem.reset();
			}
		}
		
		heroLoveEntity.notifyUpdate();
		AchievePushHelper.pushAchieveUpdate(heroLoveEntity.getPlayerId(), heroLoveEntity.getItemList());
	}

	public List<Integer> receiveAchieves(String playerId, List<Integer> achieveIdsList) {
		super.logger.info("playerId:{} achieveIdList:{}", playerId, achieveIdsList);
		Optional<AchieveItems> opAchieveItems = this.getAchieveItems(playerId);
		List<Integer> receivedIdList = new ArrayList<>();
		if (!opAchieveItems.isPresent()) {
			return null;
		}
		AchieveItems achieveItems = opAchieveItems.get();
		List<AchieveItem> items = new ArrayList<>();
		for (int achieveId : achieveIdsList) {
			for (AchieveItem achieveItem : achieveItems.getItems()) {
				if (achieveItem.getAchieveId() == achieveId && achieveItem.getState() == AchieveState.NOT_REWARD_VALUE) {
					receivedIdList.add(achieveItem.getAchieveId());
					achieveItem.setState(AchieveState.TOOK_VALUE);
					items.add(achieveItem);
					achieveItems.getEntity().notifyUpdate();
					
					AchieveConfig achieveConfig = this.getAchieveCfg(achieveId);
					List<RewardItem.Builder> rewardList = this.getRewardList(playerId, achieveConfig);					
					//这个消息不支持多个,只能发多次消息.
					HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);		
					PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(rewardList, this.takeRewardAction(), 
							false, RewardOrginType.ACHIEVE_REWARD, achieveId);
					HawkTaskManager.getInstance().postMsg(xid, msg);
				}
			}
		}
		super.logger.info("playerId:{} receivedIdList:{}", playerId, receivedIdList);
		if (!items.isEmpty()) {
			AchievePushHelper.pushAchieveUpdate(playerId, items);
		}		
		
		return receivedIdList;
	}	
}
