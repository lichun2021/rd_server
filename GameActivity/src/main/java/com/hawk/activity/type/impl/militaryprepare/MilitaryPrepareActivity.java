package com.hawk.activity.type.impl.militaryprepare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
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
import com.hawk.activity.event.impl.AddMilitaryPrepareScoreEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.extend.KeyValue;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.militaryprepare.cfg.MilitaryPrepareAchieveCfg;
import com.hawk.activity.type.impl.militaryprepare.cfg.MilitaryPrepareActivityKVCfg;
import com.hawk.activity.type.impl.militaryprepare.cfg.MilitaryPrepareScoreCfg;
import com.hawk.activity.type.impl.militaryprepare.entity.MilitaryPrepareEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.MilitaryPrepareInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 军事备战
 * @author Winder
 *
 */
public class MilitaryPrepareActivity extends ActivityBase implements AchieveProvider{

	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public MilitaryPrepareActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	@Override
	public ActivityType getActivityType() {
		return ActivityType.MILITARY_PREPARE_ACTIVITY;
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.ACTIVITY_MILITARY_PREPARE_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MilitaryPrepareActivity activity = new MilitaryPrepareActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_MILITARY_PREPARE, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MilitaryPrepareEntity> queryList = HawkDBManager.getInstance()
				.query("from MilitaryPrepareEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			MilitaryPrepareEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		MilitaryPrepareEntity entity = new MilitaryPrepareEntity(playerId, termId);
		return entity;
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
		Optional<MilitaryPrepareEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		MilitaryPrepareEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	public void initAchieveInfo(String playerId){
		Optional<MilitaryPrepareEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		MilitaryPrepareEntity entity = optional.get();
		//成就是否已经初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		//积分成就
		ConfigIterator<MilitaryPrepareScoreCfg> scoreItrator = HawkConfigManager.getInstance().getConfigIterator(MilitaryPrepareScoreCfg.class);
		while(scoreItrator.hasNext()){
			MilitaryPrepareScoreCfg nextScore = scoreItrator.next();
			AchieveItem item = AchieveItem.valueOf(nextScore.getAchieveId());
			entity.addItem(item);
		}
		//初始化成就项
		ConfigIterator<MilitaryPrepareAchieveCfg> cIterator = HawkConfigManager.getInstance().getConfigIterator(MilitaryPrepareAchieveCfg.class);
		while (cIterator.hasNext()) {
			MilitaryPrepareAchieveCfg next = cIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()), true);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(MilitaryPrepareAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(MilitaryPrepareScoreCfg.class, achieveId);
		}
		return config;
	}

	

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		MilitaryPrepareAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(MilitaryPrepareAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			MilitaryPrepareScoreCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(MilitaryPrepareScoreCfg.class, achieveId);
			if (scoreCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		}else{
			ActivityManager.getInstance().postEvent(new AddMilitaryPrepareScoreEvent(playerId, achieveCfg.getScore()));
		}
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		List<RewardItem.Builder> list = achieveConfig.getRewardList();
		MilitaryPrepareScoreCfg config = HawkConfigManager.getInstance().
				getConfigByKey(MilitaryPrepareScoreCfg.class, achieveConfig.getAchieveId());
		if (config == null) {
			return list;
		}
		Optional<MilitaryPrepareEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return list;
		}
		int aid = config.getAchieveId();
		MilitaryPrepareEntity entity = optional.get();
		if(entity.getAdvanced() > 0 && entity.addAdvancedBoxAchived(aid)){
			list.addAll(config.getAdvancedRewardList());
			int termId = this.getActivityTermId(playerId);
			//Tlog
			this.getDataGeter().logMilitaryPrepareAdvancedReward(playerId, termId, config.getAchieveId());
		}
		this.syncActivityDataInfo(entity);
		return list;
	}
	
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		int termId = getActivityTermId(playerId);
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		long now = HawkTime.getMillisecond();
		if (now >= endTime) {
			return;
		}
		Optional<MilitaryPrepareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MilitaryPrepareEntity entity = opEntity.get();
		if (event.isCrossDay() && !HawkTime.isSameDay(entity.getRefreshTime(), now)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setRefreshTime(now);
			entity.notifyUpdate();
		}
		ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDays(), this.providerActivityId()), true);
		//修复db异常引发的积分加少了的缺陷
		fixScoreLost(entity);
	}
	
	@Subscribe
	public void onGiftBuyEvent(PayGiftBuyEvent event) {
		MilitaryPrepareActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MilitaryPrepareActivityKVCfg.class);
		if (cfg == null) {
			return;
		}
		if(!event.getGiftId().equals(cfg.getIosAdvance()) &&
				!event.getGiftId().equals(cfg.getAndroidAdvance())){
			return;
		}
		
		
		Optional<MilitaryPrepareEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		MilitaryPrepareEntity entity = opEntity.get();
		entity.setAdvanced(1);
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		rewardList.addAll(cfg.getRewardList());
		String giftName = this.getDataGeter().getMilitaryPrepareGiftName(event.getGiftId());
		if(!HawkOSOperator.isEmptyString(giftName)){
			this.sendGiftBuyRewardByMail(entity.getPlayerId(), giftName, cfg.getAdvanceReward(), rewardList);
		}
		this.getDataGeter().takeReward(entity.getPlayerId(),rewardList, 1, Action.ACTIVITY_MILITARY_PREPARE_ADVANCED_AWARD, true);
		syncActivityDataInfo(entity);
	}
	
	
	/** 
	 * 发送奖励mail
	 */
	public void sendGiftBuyRewardByMail(String playerId, String giftName,String cfgReward,List<RewardItem.Builder> rewardList){
		try {
			// 邮件发送奖励
			Object[] content = new Object[]{giftName,cfgReward};
			Object[] title = new Object[]{};
			Object[] subTitle = new Object[]{giftName};
			//发邮件
			this.getDataGeter().sendMail(playerId, MailConst.MailId.PAY, title, subTitle, content, rewardList, true);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	/**玩家登录时检测,成就总积分是否有丢失情况,丢失则补积分
	 * @param entity
	 */
	public void fixScoreLost(MilitaryPrepareEntity entity){
		try {
			String playerId = entity.getPlayerId();
			List<AchieveItem> achieveItemList = entity.getItemList();
			int size = HawkConfigManager.getInstance().getConfigSize(MilitaryPrepareScoreCfg.class);
			MilitaryPrepareScoreCfg scoreCfg = HawkConfigManager.getInstance().getConfigByIndex(MilitaryPrepareScoreCfg.class, size - 1);
			
			KeyValue<AchieveItem, AchieveItems> keyValue = AchieveManager.getInstance().getActiveAchieveItem(playerId, scoreCfg.getAchieveId());
			if (keyValue == null) {
				return;
			}
			//最后一个宝箱成就已经完成直接return
			AchieveItem achieveItemMaxScore = keyValue.getKey();
			if (achieveItemMaxScore.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
				return;
			}
			
			int nowScore=0;
			int trueScore = 0;
			for (AchieveItem achieveItem : achieveItemList) {
				int achieveId = achieveItem.getAchieveId();
				MilitaryPrepareAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(MilitaryPrepareAchieveCfg.class, achieveId);
				if (achieveCfg != null && achieveItem.getState() == AchieveState.TOOK_VALUE) {
					trueScore += achieveCfg.getScore();
				}
				if (achieveId == scoreCfg.getAchieveId()) {
					nowScore = achieveItem.getValue(0);
				}
			}
			//差值>0  并且 成就存的分少于最后一挡的条件分
			int difScore = trueScore - nowScore;     
			if (difScore > 0 && nowScore < scoreCfg.getConditionValue(0)) {
				ActivityManager.getInstance().postEvent(new AddMilitaryPrepareScoreEvent(playerId, difScore));
				logger.info("MilitaryPrepareActivity fixScoreLost playerId:{},trueScore:{},nowScore:{},difScore:{}", playerId, trueScore, nowScore, difScore);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 补充领奖
	 * @param playerId
	 * @param boxId
	 */
	public void achieveBoxReward(String playerId,int boxId){
		MilitaryPrepareScoreCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(MilitaryPrepareScoreCfg.class, boxId);
		if(scoreCfg == null){
			return;
		}
		Optional<MilitaryPrepareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MilitaryPrepareEntity entity = opEntity.get();
		if(entity.getAdvanced() <= 0){
			return;
		}
		AchieveItem aItem = null;
		List<AchieveItem> achieveItemList = entity.getItemList();
		for (AchieveItem achieveItem : achieveItemList) {
			if(achieveItem.getAchieveId() == boxId){
				aItem = achieveItem;
				break;
			}
		}
		if(aItem == null){
			return;
		}
		if(aItem.getState() != AchieveState.TOOK_VALUE){
			return;
		}
		if(!entity.addAdvancedBoxAchived(boxId)){
			return;
		}
		// 发道具
		this.getDataGeter().takeReward(playerId, scoreCfg.getAdvancedRewardList(), 1,
				this.takeRewardAction(), true, RewardOrginType.ACTIVITY_REWARD);
		//同步消息
		this.syncActivityDataInfo(entity);
		int termId = getActivityTermId(playerId);
		//Tlog
		this.getDataGeter().logMilitaryPrepareAdvancedReward(playerId, termId, boxId);
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<MilitaryPrepareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MilitaryPrepareEntity entity = opEntity.get();
		this.syncActivityDataInfo(entity);
	}
	
	
	private void syncActivityDataInfo(MilitaryPrepareEntity entity){
		MilitaryPrepareInfoResp.Builder builder =MilitaryPrepareInfoResp.newBuilder();
		builder.setAdvanced(entity.getAdvanced() > 0);
		builder.addAllBox(entity.getAdvancedBoxList());
		pushToPlayer(entity.getPlayerId(), HP.code.MILITARY_PREPARE_INFO_RESP_VALUE, builder);
	}

	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}
