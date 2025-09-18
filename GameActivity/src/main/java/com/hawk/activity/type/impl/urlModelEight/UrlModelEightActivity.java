package com.hawk.activity.type.impl.urlModelEight;

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
import com.hawk.activity.event.impl.TxUrlEightShareEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.urlModelEight.cfg.UrlModelEightActivityKVCfg;
import com.hawk.game.protocol.Activity.PBEmptyModel8Info;
import com.hawk.game.protocol.MailConst.MailId;

public class UrlModelEightActivity extends ActivityBase implements IURLReward<UrlModelEightActivityKVCfg> {

	public UrlModelEightActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.URL_MODEL_EIGHT_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new UrlModelEightActivity(config.getActivityId(), activityEntity);
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
		if (!event.isCrossDay()) {
			return;
		}
		if (event.isCrossDay()) {
			// 跨天设置未领取
			this.getDataGeter().setPlayerUrlModelEightActivityInfo(playerId, false, false);
		}
	}

	@Subscribe
	public void shareTxUrlEightSuccess(TxUrlEightShareEvent event) {
		try {
			String playerId = event.getPlayerId();
			if (HawkOSOperator.isEmptyString(playerId)) {
				return;
			}
			PBEmptyModel8Info info = this.getDataGeter().getPlayerUrlModelEightActivityInfo(playerId);
			boolean isReward = false;
			if (null != info) {
				isReward = info.getIsReward();
			}
			if (false == isReward) {
				// 先设置状态
				this.getDataGeter().setPlayerUrlModelEightActivityInfo(playerId, true, true);
				UrlModelEightActivityKVCfg cfg = HawkConfigManager.getInstance()
						.getKVInstance(UrlModelEightActivityKVCfg.class);
				if (null == cfg) {
					return;
				}
				// 发奖
				this.getDataGeter().sendMail(playerId, MailId.URL_TX_SHARE_EIGHT_MAIL, null, null, null,
						cfg.getRewardList(), false);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
