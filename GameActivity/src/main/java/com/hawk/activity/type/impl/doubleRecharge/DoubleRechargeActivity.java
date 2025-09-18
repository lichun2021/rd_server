package com.hawk.activity.type.impl.doubleRecharge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.msg.ActivityDoubleRechargeMsg;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.doubleRecharge.cfg.DoubleRechagreGoodsCfg;
import com.hawk.activity.type.impl.doubleRecharge.entity.DoubleRechargeEntity;

/**
 * 充值翻倍活动
 * @author Jesse
 *
 */
public class DoubleRechargeActivity extends ActivityBase {

	public DoubleRechargeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DOUBLE_RECHARGE_ACTIVITY;
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DoubleRechargeActivity activity = new DoubleRechargeActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DoubleRechargeEntity> queryList = HawkDBManager.getInstance()
				.query("from DoubleRechargeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DoubleRechargeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DoubleRechargeEntity entity = new DoubleRechargeEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<DoubleRechargeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
	}
	
	/**
	 * 获取玩家本期活动充值送钻信息
	 * @param playerId
	 * @return
	 */
	public Map<String, Integer> getGoodsAddMap(String playerId){
		Map<String, Integer> map = new HashMap<>();
//		if(!isOpening(playerId)){
//			return map;
//		}
		Optional<DoubleRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return map;
		}
		DoubleRechargeEntity entity = opEntity.get();
		Set<String> hasBuy = entity.getBuyGoodsSet();
		ConfigIterator<DoubleRechagreGoodsCfg> cfgs= HawkConfigManager.getInstance().getConfigIterator(DoubleRechagreGoodsCfg.class);
		for(DoubleRechagreGoodsCfg cfg : cfgs){
			String goodsId = cfg.getGoodsId();
			if(hasBuy.contains(goodsId)){
				continue;
			}
			map.put(goodsId, cfg.getAddNum());
		}
		return map;
	}
	
	@Subscribe
	public void onEvent(DiamondRechargeEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<DoubleRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		String goodsId = event.getGoodsId();
		DoubleRechargeEntity entity = opEntity.get();
		Set<String> hasBuy = entity.getBuyGoodsSet();
		if (hasBuy.contains(goodsId)) {
			return;
		}
		entity.addGoodsId(goodsId);
		HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_SERVER_ACTIVITY),
				ActivityDoubleRechargeMsg.valueOf(playerId));
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
