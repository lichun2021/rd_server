package com.hawk.activity.type.impl.urlModelTen;

import com.hawk.activity.type.impl.urlReward.IURLReward;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.TxUrlTenShareEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.urlModelTen.cfg.UrlModelTenActivityKVCfg;
import com.hawk.game.protocol.Activity.PBEmptyModel10Info;
import com.hawk.game.protocol.MailConst.MailId;

public class UrlModelTenActivity extends ActivityBase implements IURLReward<UrlModelTenActivityKVCfg> {

	public UrlModelTenActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.URL_MODEL_TEN_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new UrlModelTenActivity(config.getActivityId(), activityEntity);
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
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub

	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (event.isCrossDay()) {
			// 跨天设置未领取
			this.getDataGeter().setPlayerUrlModelTenActivityInfo(playerId, false, false);
		}
	}

	@Subscribe
	public void shareTxUrlTenSuccess(TxUrlTenShareEvent event) {
		try {
			String playerId = event.getPlayerId();
			if (HawkOSOperator.isEmptyString(playerId)) {
				return;
			}
			
			PBEmptyModel10Info info = this.getDataGeter().getPlayerUrlModelTenActivityInfo(playerId);
			boolean isReward = false;
			if (info != null) {
				isReward = info.getIsReward();
			}
			
			if (!isReward) {
				// 先设置状态
				this.getDataGeter().setPlayerUrlModelTenActivityInfo(playerId, true, true);
				UrlModelTenActivityKVCfg cfg = HawkConfigManager.getInstance()
						.getKVInstance(UrlModelTenActivityKVCfg.class);
				if (cfg == null) {
					return;
				}
				
				// 发奖
				this.getDataGeter().sendMail(playerId, MailId.URL_TX_SHARE_TEN_MAIL, null, null, null,
						cfg.getRewardList(), false);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
