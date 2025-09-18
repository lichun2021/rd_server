package com.hawk.activity.type.impl.redEnvelope.base;

import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.util.JsonUtils;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.impl.redEnvelope.RedEnvelopeActivity;
import com.hawk.activity.type.impl.redEnvelope.callback.RecieveCallBack;
import com.hawk.activity.type.impl.redEnvelope.cfg.RedEnvelopeAchieveCfg;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.RedEnvelopeState;

/***
 * 系统红包
 * @author yang.rao
 *
 */
public class SystemRedEnvelope extends BaseRedEnvelope {
	
	public SystemRedEnvelope(){
		super();
	}
	
	public SystemRedEnvelope(String id, int count){
		super(id, count);
	}

	/***
	 * 抢红包
	 * @param playerId 抢红包的人
	 */
	@Override
	public void recieve(String playerId, RecieveCallBack callback) {
		RedEnvelopeAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopeAchieveCfg.class, Integer.valueOf(getId()));
		if(cfg == null){
			throw new RuntimeException(String.format("systemRedEnvelope error, can't find config: %s", getId()));
		}
		int rewardId = cfg.getRewardsID();
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.RED_ENVELOPE_VALUE);
		if(!opActivity.isPresent()){
			throw new RuntimeException(String.format("can't find activity, activityId:%d", Activity.ActivityType.RED_ENVELOPE_VALUE));
		}
		if(hasRecievedOver()){
			callback.call(Status.Error.RED_ENVELOPE_DELIVE_OVER_VALUE, null);
			return;
		}else if(hasRecieved(playerId)){
			callback.call(Status.Error.RED_ENVELOPE_ALREADY_RECIEVE_VALUE, null);
			return ;
		}
		 RedEnvelopeActivity activity = (RedEnvelopeActivity)opActivity.get();
		 activity.getDataGeter().sendSystemRedEnvelope(playerId, rewardId, callback);
	}
	
	/***
	 * 系统红包，不用拆分
	 */
	@Override
	public void splitBag() {
	}

	@Override
	public RedEnvelopeState getPlayerState(String playerId) {
		if(hasRecieved(playerId)){
			return RedEnvelopeState.ALREADY_RECIEVED;
		}else if(hasRecievedOver()){
			return RedEnvelopeState.DELIVE_OVER; //已经派完
		}else{
			return RedEnvelopeState.CAN_RECIEVE; //可领取
		}
	}

	@Override
	public OnceRedEnvelope getMyRecieveDetail(String playerId) {
		for(OnceRedEnvelope once : getSpiltList()){
			if(once.getRecieveId() != null &&  once.getRecieveId().equals(playerId)){
				return once;
			}
		}
		return null;
	}

	@Override
	public List<OnceRedEnvelope> getRecieveDetails() {
		return null;
	}

	@Override
	public void save2Redis(String key) {
		ActivityLocalRedis.getInstance().set(key, JsonUtils.Object2Json(this));
	}

}
