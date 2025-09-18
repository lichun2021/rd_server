package com.hawk.activity.type.impl.materialTransport;

import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportKVCfg;
import com.hawk.activity.type.impl.materialTransport.entity.MaterialTransportEntity;

public class MaterialTransportActivity extends ActivityBase {
	public final Logger logger = LoggerFactory.getLogger("Server");

	public MaterialTransportActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isOpening(String playerId) {
		return super.isOpening(playerId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.MATERIAL_TRANSPORT;
	}

	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MaterialTransportActivity activity = new MaterialTransportActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MaterialTransportEntity> queryList = HawkDBManager.getInstance()
				.query("from MaterialTransportEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			MaterialTransportEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		MaterialTransportEntity entity = new MaterialTransportEntity(playerId, termId);
		return entity;
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		MaterialTransportKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MaterialTransportKVCfg.class);
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<MaterialTransportEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		MaterialTransportEntity entity = optional.get();


	}

}
