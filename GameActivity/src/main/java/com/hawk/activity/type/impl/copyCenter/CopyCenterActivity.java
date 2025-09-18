package com.hawk.activity.type.impl.copyCenter;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.db.HawkDBEntity;
import org.hawk.redis.HawkRedisSession;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.PBCopyCenterSync;

public class CopyCenterActivity extends ActivityBase {

	final String FREQUENCYS_KEY = "copy_center_s:";
	final String FREQUENCYSS_KEY = "copy_center_ss:";

	public CopyCenterActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.COPY_CENTER;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new CopyCenterActivity(config.getActivityId(), activityEntity);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	@Override
	public void onPlayerLogin(String playerId) {
		sync(playerId);
	}
	
	public void sync(String playerId){
		PBCopyCenterSync.Builder resp = PBCopyCenterSync.newBuilder();
		resp.setFrequencyS(getFrequencySCount(playerId));
		resp.setFrequencySS(getFrequencySSCount(playerId));
		pushToPlayer(playerId, HP.code.COPY_CENTER_SYNC_VALUE, resp);
		
	}

	/** 当期S级兑换次数 */
	public int getFrequencySCount(String playerId) {
		HawkRedisSession session = ActivityLocalRedis.getInstance().getRedisSession();
		int term = getActivityTermId();
		final String key = FREQUENCYS_KEY + playerId + term;
		return NumberUtils.toInt(session.getString(key));
	}
	/** 当期S级兑换次数 */
	public void incFrequencySCount(String playerId, int add) {
		HawkRedisSession session = ActivityLocalRedis.getInstance().getRedisSession();
		int term = getActivityTermId();
		final String key = FREQUENCYS_KEY + playerId + term;
		session.increaseBy(key, add, (int)TimeUnit.DAYS.toSeconds(30));
	}

	/** 当期SS兑换次数 */
	public int getFrequencySSCount(String playerId) {
		HawkRedisSession session = ActivityLocalRedis.getInstance().getRedisSession();
		int term = getActivityTermId();
		final String key = FREQUENCYSS_KEY + playerId + term;
		return NumberUtils.toInt(session.getString(key));
	}


	/** 当期SS兑换次数 */
	public void incFrequencySSCount(String playerId, int add) {
		HawkRedisSession session = ActivityLocalRedis.getInstance().getRedisSession();
		int term = getActivityTermId();
		final String key = FREQUENCYSS_KEY + playerId + term;
		session.increaseBy(key, add, (int)TimeUnit.DAYS.toSeconds(30));
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

}
