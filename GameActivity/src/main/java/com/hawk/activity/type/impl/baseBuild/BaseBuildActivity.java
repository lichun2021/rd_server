package com.hawk.activity.type.impl.baseBuild;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.BaseBuildBuyEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.baseBuild.cfg.BaseBuildKVCfg;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.gamelib.GameConst.MsgId;

public class BaseBuildActivity extends ActivityBase {

	public BaseBuildActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.BASE_BUILD_ACTIVITY;
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BaseBuildActivity activity = new BaseBuildActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}
	

	@Override
	public boolean isActivityClose(String playerId) {
		long serverOpenTime = getDataGeter().getServerOpenDate();
		BaseBuildKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BaseBuildKVCfg.class);
		if (serverOpenTime < cfg.getActivityTimeValue()) {
			return true;
		}
		int cityLvl = getDataGeter().getConstructionFactoryLevel(playerId);
		if (cityLvl > cfg.getBuild()) {
			return true;
		}
		if(cityLvl < cfg.getBuildMin()){
			return true;
		}
		return false;
	}

	@Subscribe
	public void onEvent(BuildingLevelUpEvent event) {
		// 大本升级,检测活动是否关闭
		if(event.getBuildType() == BuildingType.CONSTRUCTION_FACTORY_VALUE){
			syncActivityStateInfo(event.getPlayerId());
		}
	}
	
	@Subscribe
	public void onAuthBuyEvent(BaseBuildBuyEvent event) {
		String playerId = event.getPlayerId();
		int payId = Integer.valueOf(event.getPayGiftId());
		BaseBuildKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BaseBuildKVCfg.class);
		if (payId != cfg.getAndroidPayId() && payId != cfg.getIosPayId()) {
			// TODO 日志记录
			return;
		}
		// 建筑升级,丢玩家线程处理消息
		callBack(playerId, MsgId.BASE_BUILD_UP, () -> {
			getDataGeter().dealWithBaseBuild(playerId);
		});
		
		logger.info("BaseBuildActivity sendAuthBuyReward, playerId:{}", playerId);
		this.getDataGeter().sendMail(playerId, MailId.BASE_BUILD_REWARD,
				new Object[] { this.getActivityCfg().getActivityName() },
				new Object[] { this.getActivityCfg().getActivityName() }, new Object[] {},
				cfg.getAwardItemsList(), false);
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
}
