package com.hawk.activity.type.impl.dressup.drawingsearch;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.dressup.drawingsearch.cfg.DrawingSearchCollectCfg;
import com.hawk.activity.type.impl.dressup.drawingsearch.cfg.DrawingSearchKVCfg;
import com.hawk.activity.type.impl.dressup.drawingsearch.entity.DrawingSearchActivityEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 装扮投放系列活动一:搜寻图纸
 * @author hf
 */
public class DrawingSearchActivity extends ActivityBase {

	public DrawingSearchActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRAWING_SEARCH_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new DrawingSearchActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DrawingSearchActivityEntity> queryList = HawkDBManager.getInstance()
				.query("from DrawingSearchActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DrawingSearchActivityEntity drawingSearchActivityEntity = new DrawingSearchActivityEntity(playerId, termId);
		return drawingSearchActivityEntity;
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if(event.isCrossDay()){
			logger.info("DrawingSearchActivity receive clear dataEvent");
			this.cleanData(event.getPlayerId());
		}
	}
	
	@Override
	public void onOpen() {
		Set<String> idSet = this.getDataGeter().getOnlinePlayers();
		for (String id : idSet) {
			this.callBack(id, GameConst.MsgId.ACHIEVE_INIT_DRAWING_SEARCH, ()->{
				this.cleanData(id);
			});
		}
	}
	/**
	 * 数据重置
	 */
	private void cleanData(String playerId) {
		Optional<DrawingSearchActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		DrawingSearchActivityEntity entity = opEntity.get();
		logger.info("DrawingSearchActivity clean playerId:{}", entity.getPlayerId());
		entity.setCollectRemainTime(0);
		entity.setBeatYuriTimes(0);
		entity.setWishTimes(0);
		entity.setWolrdCollectRemainTime(0);
		entity.setWolrdCollectTimes(0);
		entity.setLastOperTime(HawkTime.getMillisecond());
		entity.setTotalDropNum(0);  //之前是活动期间总上限,,策划后期又改成每日上限
	}

	/**
	 * 获取最大可增加的个数
	 * @param entity
	 * @return
	 */
	public int getCanAddTotalDropNum(DrawingSearchActivityEntity entity){
		DrawingSearchKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DrawingSearchKVCfg.class);
		int limit = cfg.getTotalDropLimit();
		int leftLimit =  limit - entity.getTotalDropNum();
		return Math.max(leftLimit, 0);
	}

	/**
	 * 掉落总数上限
	 * @return
	 */
	public int getTotalDropLimitNum(){
		DrawingSearchKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DrawingSearchKVCfg.class);
		return cfg.getTotalDropLimit();
	}

	@Subscribe
	public void worldCollectEvent(ResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (event.getCollectTime() <= 0) {
			return;
		}

		Optional<DrawingSearchActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		DrawingSearchActivityEntity entity = opEntity.get();
		/**掉落超过总上限*/
		if(entity.getTotalDropNum() >= getTotalDropLimitNum()){
			return;
		}
		int collectTime = event.getCollectTime() + entity.getWolrdCollectRemainTime();
		DrawingSearchCollectCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DrawingSearchCollectCfg.class,
				Activity.BrokenExchangeOper.WORLD_COLLECT_VALUE);
		//配置不存在说明策划不想触发
		if (cfg == null) {
			return;
		}
		
		if (collectTime >= cfg.getDropParam()) {
			int num = collectTime / cfg.getDropParam();
			int remain = collectTime % cfg.getDropParam();
			if (cfg.getDropLimit() > 0) {
				num = num > cfg.getDropLimit()  - entity.getWolrdCollectTimes() ? cfg.getDropLimit()  - entity.getWolrdCollectTimes() : num;
			}
			if (num > 0) {
				//还能掉落几个
				int limitNum = getCanAddTotalDropNum(entity);
				num = Math.min(limitNum, num);
				this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), num,
						Action.ACTIVITY_DRAWING_SEARCH_WORLD_COLLECT, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
				entity.setWolrdCollectTimes(num + entity.getWolrdCollectTimes());
				entity.setWolrdCollectRemainTime(remain);
				entity.addTotalDropNum(num);
			}			
		} else {
			entity.setWolrdCollectRemainTime(collectTime);
		}
		logger.info("DrawingSearchActivity worldCollect playerId:{}, beforeCollectTime:{}, afterCollectTime:{}, addCollectTime:{}", event.getPlayerId(), (collectTime - event.getCollectTime()), entity.getWolrdCollectRemainTime(), event.getCollectTime());
	}

	@Subscribe
	public void wishingEvent(WishingEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<DrawingSearchActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		DrawingSearchActivityEntity entity = opEntity.get();
		/**掉落超过总上限*/
		if(entity.getTotalDropNum() >= getTotalDropLimitNum()){
			return;
		}
		entity.setWishTimes(entity.getWishTimes() + 1);

		DrawingSearchCollectCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DrawingSearchCollectCfg.class,
				Activity.BrokenExchangeOper.WISH_VALUE);
		if (cfg == null) {
			return;
		}
		
		if (entity.getWishTimes() >= cfg.getDropParam()) {
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), 1,
					Action.ACTIVITY_DRAWING_SEARCH_WISH, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setWishTimes(0);
			entity.addTotalDropNum(1);
		}
		
		logger.info("DrawingSearchActivity wishing playerId:{}, wishTimes:{}", event.getPlayerId(), entity.getWishTimes());
	}



	@Subscribe
	public void resourceCollectEvent(CityResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		// 处理使用技能的情况
		if (event.getCollectTime().isEmpty()) {
			return;
		}

		Optional<DrawingSearchActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		DrawingSearchActivityEntity entity = opEntity.get();
		/**掉落超过总上限*/
		if(entity.getTotalDropNum() >= getTotalDropLimitNum()){
			return;
		}
		int remainTime = entity.getCollectRemainTime();

		DrawingSearchCollectCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DrawingSearchCollectCfg.class,
				Activity.BrokenExchangeOper.RESOURCE_COLLECT_VALUE);
		if (cfg == null) {
			return;
		}
		int num = 0;
		int totalTime = 0;
		for (Integer timeLong : event.getCollectTime()) {
			totalTime = timeLong + remainTime;
			totalTime = totalTime > cfg.getDropLimit() ? cfg.getDropLimit() : totalTime;
			if (totalTime > cfg.getDropParam()) {
				num = totalTime / cfg.getDropParam() + num;
				remainTime = totalTime % cfg.getDropParam();
			}
		}

		if (num > 0) {
			//还能掉落几个
			int limitNum = getCanAddTotalDropNum(entity);
			num = Math.min(limitNum, num);
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), num,
					Action.ACTIVITY_DRAWING_SEARCH_RES_COLLECT, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.addTotalDropNum(num);
		}

		entity.setCollectRemainTime(remainTime);				
	}
	
	@Subscribe
	public void beatYuriEvent(MonsterAttackEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		int monsterType = event.getMosterType();
		switch(monsterType) {
		case MonsterType.TYPE_1_VALUE:
		case MonsterType.TYPE_2_VALUE:
			if (!event.isKill()) {
				return;
			}
			break;
		default:
			return;
		}
		
		int atkTimes = event.getAtkTimes();
		Optional<DrawingSearchActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		DrawingSearchActivityEntity entity = opEntity.get();
		/**掉落超过总上限*/
		if(entity.getTotalDropNum() >= getTotalDropLimitNum()){
			return;
		}

		entity.setBeatYuriTimes(entity.getBeatYuriTimes() + atkTimes);
		DrawingSearchCollectCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DrawingSearchCollectCfg.class,
				Activity.BrokenExchangeOper.BEAT_YURI_VALUE);
		if (cfg == null) {
			return;
		}
		int num = atkTimes;
		if (entity.getBeatYuriTimes() >= cfg.getDropParam()) {
			//还能掉落几个
			int limitNum = getCanAddTotalDropNum(entity);
			num = Math.min(limitNum, num);
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), atkTimes,
					Action.ACTIVITY_DRAWING_SEARCH_BEAT_YURI, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setBeatYuriTimes(0);
			entity.addTotalDropNum(num);
		}
		
		logger.info("DrawingSearchActivity beatYuri playerId:{}, beatTimes:{}, totalBeatTimes:{}, addNum:{}", event.getPlayerId(),
				atkTimes, entity.getBeatYuriTimes(), num);
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
