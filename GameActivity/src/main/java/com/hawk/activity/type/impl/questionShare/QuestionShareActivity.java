package com.hawk.activity.type.impl.questionShare;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
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
import com.hawk.activity.event.impl.QuestionShareEvent;
import com.hawk.activity.event.impl.QuestionShareOkEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.questionShare.cfg.QuestionContentCfg;
import com.hawk.activity.type.impl.questionShare.cfg.QuestionShareKVCfg;
import com.hawk.activity.type.impl.questionShare.cfg.QuestionShareRewardsCfg;
import com.hawk.activity.type.impl.questionShare.entity.QuestionShareEntity;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.game.protocol.Activity.QuestionShareInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;

public class QuestionShareActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public QuestionShareActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.QUESTION_SHARE;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<QuestionShareEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			// 第一次登录发现没有题目，给玩家刷新个题目
			if (opDataEntity.get().getDayQuestionList().isEmpty()) {
				refreshQuestion(opDataEntity.get());
			}
			// 刷成就
			if (opDataEntity.get().getItemList().isEmpty()) {
				initAchieveInfo(playerId);
			}
			// this.syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, GameConst.MsgId.QUESTION_SHARE_INIT, () -> {
				Optional<QuestionShareEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error(
							"questionshare_log on QuestionShareActivity open init QuestionShareEntity error, no entity created:"
									+ playerId);
				}
				refreshQuestion(opEntity.get());
				callBack(playerId, MsgId.ACHIEVE_INIT_ACCUMULATE_RECHARGE, () -> {
					initAchieveInfo(playerId);
					this.syncActivityInfo(playerId, opEntity.get());
				});
			});
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<QuestionShareEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	// 同步答题消息给玩家
	public void syncActivityInfo(String playerId, QuestionShareEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}
		QuestionShareInfoResp.Builder builder = QuestionShareInfoResp.newBuilder();
		builder.setSharedAmount(entity.getShareAmount());
		builder.addAllQuestion(entity.getDayQuestionList());
		builder.addAllAnswer(entity.getDayAnswerList());
		builder.addAllShare(entity.getDayShareList());
		builder.setDailyAwarded(entity.getDailyRewarded());
		int questionSize = entity.getDayQuestionList().size();
		if (questionSize == entity.getDayAnswerList().size()) {
			for (int i = 0; i < questionSize; i++) {
				int questionId = entity.getDayQuestionList().get(i);
				int answerId = entity.getDayAnswerList().get(i);
				if (answerId == 0) {
					builder.addResult(0);
					continue;
				}

				QuestionContentCfg questionAnswerCfg = HawkConfigManager.getInstance()
						.getConfigByKey(QuestionContentCfg.class, questionId);
				if (null == questionAnswerCfg || answerId != questionAnswerCfg.getAnswer()) {
					builder.addResult(2);
				} else {
					builder.addResult(1);
				}
			}
		} else {
			logger.error("questionshare_log QuestionShare entity answer size can't match question size!");
			return;
		}

		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.QUESTION_SHARE_INFO_RESP_S, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		QuestionShareActivity activity = new QuestionShareActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<QuestionShareEntity> queryList = HawkDBManager.getInstance()
				.query("from QuestionShareEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			QuestionShareEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		QuestionShareEntity entity = new QuestionShareEntity(playerId, termId);
		// 刷新question
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<QuestionShareEntity> opEntity = getPlayerDataEntity(playerId);
		QuestionShareEntity entity = opEntity.get();
		if (event.isCrossDay()) {
			refreshQuestion(entity);
			entity.setDailyRewarded(0);
			this.syncActivityDataInfo(playerId);
		}
	}

	private void refreshQuestion(QuestionShareEntity entity) {
		entity.clearDay();

		int questionTime = QuestionShareKVCfg.getInstance().getQuestionTime();

		int size = HawkConfigManager.getInstance().getConfigSize(QuestionContentCfg.class);

		for (int i = 0; i < questionTime; i++) {
			int questionId = HawkRand.randInt(1, size);
			QuestionContentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(QuestionContentCfg.class,
					questionId);
			if (null != cfg) {
				entity.addQuestion(questionId);
			} else {
				logger.error("questionshare_log config and randomId not match");
			}
		}
	}

	public Result<?> onProtocolDailyReward(String playerId) {
		try {
			Optional<QuestionShareEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				logger.info("questionshare_log player:{} no data!", playerId);
				return Result.fail(Status.Error.QUESTION_SHARED_NOT_OPEN_VALUE);
			}
			QuestionShareEntity entity = opEntity.get();

			// 奖励配置
			List<RewardItem.Builder> rewardItems = HawkConfigManager.getInstance()
					.getKVInstance(QuestionShareKVCfg.class).getDailyAwardItems();

			if (null == rewardItems || rewardItems.isEmpty()) {
				return Result.fail(Status.Error.QUESTION_SHARED_DAILY_REWARD_CFG_VALUE);
			}

			if (entity.getDailyRewarded() != 0) {
				return Result.fail(Status.Error.QUESTION_SHARED_DAILY_AWARDED_VALUE);
			}
			//改数据
			entity.setDailyRewarded(1);
			//发奖
			this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.QUESTION_SHARE_DAILY_AWARD, true);
			//更新数据库
			entity.notifyUpdate();
			//同步数据
			this.syncActivityInfo(playerId, entity);
		} catch (Exception e) {
			return Result.fail(Status.Error.QUESTION_SHARED_UNKONW_ERR_VALUE);
		}
		return Result.success();
	}

	public void answerQuestion(String playerId, int answer) {
		Optional<QuestionShareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.info("questionshare_log player:{} no data!", playerId);
			return;
		}
		QuestionShareEntity entity = opEntity.get();
		int size = entity.getDayQuestionList().size();
		if (size <= 0) {
			logger.info("questionshare_log player:{} no question!", playerId);
			return;
		}
		if (size != entity.getDayAnswerList().size()) {
			logger.info("questionshare_log player:{} question size not match answer size!", playerId);
			return;
		}
		int questionId = entity.getDayQuestionList().get(size - 1);
		int answerId = entity.getDayAnswerList().get(size - 1);
		if (answerId != 0) {
			logger.info("questionshare_log player:{} answered:{}", playerId, questionId);
			return;
		}

		QuestionContentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(QuestionContentCfg.class, questionId);
		if (null == cfg) {
			logger.info("questionshare_log question:{} cfg not found", questionId);
			return;
		}

		// 回答正确和错误都有奖励
		entity.addAnswer(answer);

		try {
			if (cfg.getAnswer() == answer) {
				Object[] subTitle = new Object[] {};
				Object[] content = new Object[] { getActivityCfg().getActivityName() };
				// VERSION_UPDATE_REWARD
				MailId mailId = MailId.valueOf(
						HawkConfigManager.getInstance().getKVInstance(QuestionShareKVCfg.class).getRightMail());
				sendMailToPlayer(playerId, mailId, subTitle, content,
						QuestionShareKVCfg.getInstance().getRightAwardItems());
				logger.info("questionshare_log send right reward! playerId: {}, achieveItem: {}", playerId,
						QuestionShareKVCfg.getInstance().getRightAwardItems().toString());
			} else {
				Object[] subTitle = new Object[] {};
				Object[] content = new Object[] { getActivityCfg().getActivityName() };

				MailId mailId = MailId.valueOf(
						HawkConfigManager.getInstance().getKVInstance(QuestionShareKVCfg.class).getWrongMail());
				sendMailToPlayer(playerId, mailId, subTitle, content,
						QuestionShareKVCfg.getInstance().getWrongAwardItems());
				logger.info("questionshare_log send wrong reward! playerId: {}, achieveItem: {}", playerId,
						QuestionShareKVCfg.getInstance().getWrongAwardItems().toString());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		this.syncActivityInfo(playerId, entity);
	}

	@Subscribe
	public void shareQuestionSuccess(QuestionShareEvent event) {
		String playerId = event.getPlayerId();
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}
		Optional<QuestionShareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.info("questionshare_log QuestionShare player:{} no data!", playerId);
			return;
		}
		QuestionShareEntity entity = opEntity.get();
		int size = entity.getDayShareList().size();
		if (0 == size || 0 != entity.getDayShareList().get(size - 1)) {
			logger.info("questionshare_log QuestionShare player:{} shared!", playerId);
			return;
		}
		entity.setShare();
		this.syncActivityInfo(playerId, entity);

		// 成就逻辑
		ActivityManager.getInstance().postEvent(QuestionShareOkEvent.valueOf(playerId));
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
		Optional<QuestionShareEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		QuestionShareEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public Action takeRewardAction() {
		return Action.QUESTION_SHARE_AWARD;
	}

	@Override
	public QuestionShareRewardsCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(QuestionShareRewardsCfg.class, achieveId);
	}

	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<QuestionShareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		QuestionShareEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<QuestionShareRewardsCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(QuestionShareRewardsCfg.class);
		while (configIterator.hasNext()) {
			QuestionShareRewardsCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);

		logger.debug("questionshare_log refresh achieveitems :");
	}

}
