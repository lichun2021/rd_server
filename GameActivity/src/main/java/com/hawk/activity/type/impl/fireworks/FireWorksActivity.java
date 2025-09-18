package com.hawk.activity.type.impl.fireworks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.fireworks.cfg.FireWorksBuffCfg;
import com.hawk.activity.type.impl.fireworks.cfg.FireWorksKVCfg;
import com.hawk.activity.type.impl.fireworks.entity.FireBuff;
import com.hawk.activity.type.impl.fireworks.entity.FireWorksEntity;
import com.hawk.game.protocol.Activity.FireWorksInfoSync;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class FireWorksActivity extends ActivityBase {
	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public FireWorksActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FIRE_WORKS_ACTIVITY;
	}

	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<FireWorksEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	/**同步消息
	 * @param playerId
	 * @param entity
	 */
	private void syncActivityInfo(String playerId, FireWorksEntity entity) {
		FireWorksInfoSync.Builder builder = FireWorksInfoSync.newBuilder();
		builder.setDayFree(entity.isDayFree());
		for (FireBuff fireBuff : entity.getBufInfoList()) {
			builder.addFireBuffInfo(fireBuff.createBuilder());
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.FIREWORKS_INFO_SYNC, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		FireWorksActivity activity = new FireWorksActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FireWorksEntity> queryList = HawkDBManager.getInstance()
				.query("from FireWorksEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FireWorksEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FireWorksEntity entity = new FireWorksEntity(playerId, termId);
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
		Optional<FireWorksEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		FireWorksEntity entity = opEntity.get();
		entity.setDayFree(false);
		entity.notifyUpdate();
		this.syncActivityDataInfo(playerId);
	}


	/**领取免费奖励
	 * @param playerId
	 * @param proType
	 * @return
	 */
	Result<?> getFreeFireReward(String playerId,int proType) {
		try {
			if (!isOpening(playerId)) {
				return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			}
			Optional<FireWorksEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
			
			FireWorksEntity entity = opEntity.get();
			//免费领取时间未到
			if (entity.isDayFree()) {
				return Result.fail(Status.Error.FIRE_WORKS_NO_FREE_TIME_VALUE);
			}
			//判断是否在时间范围内
			FireWorksKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireWorksKVCfg.class);
			HawkTuple2<Long, Long> resetTime = cfg.getResetTimeTuple();
			boolean isBetweenFreeTime = isBetweenFreeTime(resetTime);
			if (!isBetweenFreeTime) {
				return Result.fail(Status.Error.FIRE_WORKS_NO_FREE_TIME_VALUE);
			}
			List<RewardItem.Builder> reward = RewardHelper.toRewardItemList(cfg.getFreeItem());
			//发奖
			this.getDataGeter().takeReward(playerId, reward, Action.FIRE_WORKS_FREE_REWARD, true);
			
			entity.setDayFree(true);
			entity.notifyUpdate();
			
			this.syncActivityInfo(playerId, entity);
			
			logger.info("FireWorksActivity getFreeFireReward success playerId:{}", playerId );

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Result.success();
	}
	
	/**根据配置获取下次刷新时间点
	 * @param resetTime
	 * @return
	 */
	public boolean isBetweenFreeTime(HawkTuple2<Long, Long> resetTime){
		long nowTime = HawkTime.getMillisecond();
		long zoreTime = HawkTime.getAM0Date().getTime();
		long startTime = zoreTime + resetTime.first;
		long endTime = zoreTime + resetTime.second;
		if (nowTime >= startTime && nowTime <= endTime) {
			return true;
		}
		return false;
	}

	/**点燃烟花
	 * @param playerId
	 * @param num  点燃个数
	 * @param proType
	 * @return
	 */
	public Result<?> lightFireWorks( String playerId, int num, int proType) {
		try {
			if (!isOpening(playerId)) {
				return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			}
			Optional<FireWorksEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
			FireWorksKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireWorksKVCfg.class);
			//消耗
			List<RewardItem.Builder> costItem = RewardHelper.toRewardItemList(cfg.getConsumeItem());
			
			boolean success = this.getDataGeter().cost(playerId, costItem, num, Action.FIRE_WORKS_LIGHT_COST, false);
			if (!success) {
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}
			
			
			FireWorksEntity entity = opEntity.get();
			
			FireWorksKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(FireWorksKVCfg.class);
			List<RewardItem.Builder> allRewards = new ArrayList<>();
			//点燃多次烟花
			for (int i = 0; i < num; i++) {
				//优先排除已经满级的buff
				List<Integer> buffTypeList = getCanBuffTypeList(entity);
				//活动表里的buffId
				int randomIdx = HawkRand.randomWeightObject(buffTypeList);
				int buffType = buffTypeList.get(randomIdx);
				//更新db中buff数据
				FireBuff fireBuff = updateBuffInfo(entity, buffType);			
				//生效的buffId
				int buffId = fireBuff.getBuffId();
				//有效时间
				long deadline = fireBuff.getEndTime();
				// 加buff
				this.getDataGeter().addBuff(playerId, buffId, deadline);
				List<RewardItem.Builder> fireRewards = kvCfg.getFrieworksRewardList();
				allRewards.addAll(fireRewards);
				
				// tlog打点
				int termId = this.getActivityTermId();
				this.getDataGeter().logFireWorksForBuffActive(playerId, termId, fireBuff.getBuffId());
			}
			//发奖励
			this.getDataGeter().takeReward(playerId, allRewards, Action.FIRE_WORKS_LIGHT_REWARD, true);
			//updata
			entity.notifyUpdate();
			//烟花持续时间
			long duration = cfg.getFrieworksDuration();
			this.getDataGeter().updatePlayerFireWorks(playerId, num,  duration);
			// 操作成功
			this.syncActivityInfo(playerId, entity);
			logger.info("FireWorksActivity lightFireWorks success playerId:{}, num:{}, duration:{}", playerId, num, duration);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Result.success();
	}
	
	
	
	/**获取可随机的技能类型
	 * @param entity
	 * @return
	 */
	public List<Integer> getCanBuffTypeList(FireWorksEntity entity){
		List<Integer> buffTypeList = new ArrayList<>(FireWorksBuffCfg.getBuffTypeList());
		List<FireBuff> fireBuffList = entity.getBufInfoList();
		for (FireBuff fireBuff : fireBuffList) {
			FireWorksBuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(FireWorksBuffCfg.class, fireBuff.getBuffId());
			int maxLevel = FireWorksBuffCfg.getBuffMaxLevel().get(buffCfg.getType());
			if (buffCfg.getLv() == maxLevel) {
				buffTypeList.remove(Integer.valueOf(buffCfg.getType()));
			}
		}
		//如果晚上身上都满级,则正常随机
		if (buffTypeList.isEmpty()) {
			return FireWorksBuffCfg.getBuffTypeList();
		}
		return buffTypeList;
	}
	
	/**更新db的buff信息
	 * @param entity
	 * @param buffId
	 */
	public FireBuff updateBuffInfo(FireWorksEntity entity, int buffType){
		List<FireBuff> fireBuffList = entity.getBufInfoList();
		long nowTime = HawkTime.getMillisecond();
		
		FireBuff fireBuff = getFireBuff(entity, buffType);
		if (fireBuff == null) {
			FireWorksBuffCfg cfg = getFireWorksBuffCfg(buffType, 1);
			fireBuff = FireBuff.valueOf(cfg.getBuffId(), nowTime + cfg.getTime());
			fireBuffList.add(fireBuff);
			return fireBuff;
		}else{
			//已经存在的buff无论生不生效都升等级
			//移除旧的
			fireBuffList.remove(fireBuff);
			//添加高级
			FireWorksBuffCfg cfg = getNextFireWorksBuffCfg(fireBuff.getBuffId());
			FireBuff highFireBuff = FireBuff.valueOf(cfg.getBuffId(), nowTime + cfg.getTime());
			fireBuffList.add(highFireBuff);
			return highFireBuff;
		}
	}
	
	/**获取单个buff信息
	 * @param entity
	 * @param buffId
	 * @return
	 */
	public FireBuff getFireBuff(FireWorksEntity entity, int buffType){
		List<FireBuff> fireBuffList = entity.getBufInfoList();
		for (FireBuff fireBuff : fireBuffList) {
			FireWorksBuffCfg bufCfg = HawkConfigManager.getInstance().getConfigByKey(FireWorksBuffCfg.class, fireBuff.getBuffId());
			if (bufCfg == null) {
				logger.error("FireWorksActivity get FireWorksBuffCfg  is null buffType:{}", buffType);
				return null;
			}
			if (bufCfg.getType() == buffType) {
				return fireBuff;
			}
		}
		logger.info("FireWorksActivity getFireBuff is null buffType:{}", buffType);
		return null;
	}
	
	/** 获取下一个buf数据
	 * @param buffId
	 * @return
	 */
	public FireWorksBuffCfg getNextFireWorksBuffCfg(int buffId){
		FireWorksBuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(FireWorksBuffCfg.class, buffId);
		int maxLevel = FireWorksBuffCfg.getBuffMaxLevel().get(buffCfg.getType());
		if (buffCfg.getLv() < maxLevel) {
			buffCfg = getFireWorksBuffCfg(buffCfg.getType(), buffCfg.getLv() + 1);
		}
		return buffCfg;
	}
	/**根据id 和 等级 获取对应的配置数据
	 * @param buffId
	 * @param level
	 * @return
	 */
	public FireWorksBuffCfg getFireWorksBuffCfg(int buffType, int level){
		ConfigIterator<FireWorksBuffCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(FireWorksBuffCfg.class);
		while (configIterator.hasNext()) {
			FireWorksBuffCfg cfg = configIterator.next();
			if (cfg.getType() == buffType && cfg.getLv() == level) {
				return cfg;
			}
			
		}
		logger.error("FireWorksActivity getFireWorksBuffCfg is null buffType:{}, level:{}", buffType, level);
		return null;
		
	}
}
