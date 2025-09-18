package com.hawk.activity.type.impl.blackTech;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.blackTech.cfg.BlackTechBuffCfg;
import com.hawk.activity.type.impl.blackTech.cfg.BlackTechKVCfg;
import com.hawk.activity.type.impl.blackTech.cfg.BlackTechPackageCfg;
import com.hawk.activity.type.impl.blackTech.entity.BlackTechEntity;
import com.hawk.game.protocol.Activity.HPBlackTechActivityInfoSync;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.HPHeartBeat;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class BlackTechActivity extends ActivityBase {
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<BlackTechEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	public BlackTechActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BLACK_TECH_ACTIVITY;
	}

	private void syncActivityInfo(String playerId, BlackTechEntity entity) {

		BlackTechKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BlackTechKVCfg.class);
		if (null == cfg) {
			return;
		}
		HPBlackTechActivityInfoSync.Builder builder = HPBlackTechActivityInfoSync.newBuilder();
		//前端约定
		builder.setDeadline(entity.getDeadline() > HawkTime.getMillisecond() ? entity.getDeadline() : 0);
		//前端约定 这里这里的buff 在激活之后才是个有效字段
		builder.setCurPoolId( builder.getDeadline() > 0 ? entity.getBuffId() : entity.getPoolId());
		builder.setActiveTimes(
				entity.getActiveTimes() >= cfg.getActiveTimes() ? 0 : cfg.getActiveTimes() - entity.getActiveTimes());
		builder.setFreeTimes(entity.getDrawTimes() > cfg.getRefresh() ? 0 : cfg.getRefresh() - entity.getDrawTimes());
		builder.setUseGoldenTimes(
				entity.getDrawTimes() > cfg.getRefresh() ? entity.getDrawTimes() - cfg.getRefresh() : 0);
		for (Map.Entry<Integer, Integer> entry : entity.getBuyRecordMap().entrySet()) {
			KeyValuePairInt.Builder pair = KeyValuePairInt.newBuilder();
			pair.setKey(entry.getKey());
			pair.setVal(entry.getValue());
			builder.addGoods(pair);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.HP_BLACK_TECH_INFO_SYNC_S, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BlackTechActivity activity = new BlackTechActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BlackTechEntity> queryList = HawkDBManager.getInstance()
				.query("from BlackTechEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			BlackTechEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BlackTechEntity entity = new BlackTechEntity(playerId, termId);
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
		if (event.isCrossDay()) {
			Optional<BlackTechEntity> opEntity = this.getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}

			BlackTechEntity entity = opEntity.get();
			// 刷新抽奖次数 刷新激活次数
			entity.setActiveTimes(0);
			entity.setDrawTimes(0);
			entity.notifyUpdate();
			this.syncActivityDataInfo(playerId);
		}
	}

	/**
	 * 刷新buff池
	 * 
	 * @param protocolType
	 * @param playerId
	 */
	Result<?> onProtocolActivityDrawReq(int protocolType, String playerId) {
		try {
			Optional<BlackTechEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return Result.fail(Status.Error.BLACK_TECH_NOT_OPEN_VALUE);
			}
			BlackTechEntity entity = opDataEntity.get();
			// 配置表
			BlackTechKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BlackTechKVCfg.class);
			if (null == cfg) {
				return Result.fail(Status.Error.BLACK_TECH_NO_CFG_VALUE);
			}

			RewardItem.Builder costItem = cfg.getCostByDrawTimes(entity.getDrawTimes() + 1);
			List<RewardItem.Builder> cost = new ArrayList<>();
			if (null != costItem) {
				cost.add(costItem);
			}
			
			//没到时间不能抽
			if(entity.getDeadline() > HawkTime.getMillisecond()){
				return Result.fail(Status.Error.BLACK_TECH_DRAW_BUFF_VALUE);
			}
			
			List<BlackTechBuffCfg> cfgs = new ArrayList<>();
			ConfigIterator<BlackTechBuffCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(BlackTechBuffCfg.class);
			while(cfgIter.hasNext()){
				BlackTechBuffCfg c = cfgIter.next();
				// 大本等级限制
				if (getDataGeter().getConstructionFactoryLevel(playerId) < c.getBuildLimit()) {
					continue;
				}
				// 章节任务完成限制
				if (c.getTaskLimit() > 0 && !getDataGeter().hasFinishStoryMission(playerId, c.getTaskLimit())) {
					continue;
				}
				if (c.getUnlockEquipResearch() > 0 && !getDataGeter().isUnlockEquipResearch(playerId,c.getUnlockEquipResearch())) {
					continue;
				}
				cfgs.add(c);
			}
			
			// 获取随机配置
			BlackTechBuffCfg randomCfg = HawkRand.randomWeightObject(cfgs);

			// 判断扣钱
			if (!cost.isEmpty()) {
				// 判断道具足够否
				boolean flag = this.getDataGeter().cost(playerId, cost, 1, Action.BLACK_TECH_DRAW_COST, false);
				if (!flag) {
					return Result.fail(Status.Error.BLACK_TECH_DRAW_ITEM_VALUE);
				}
			}
			// 刷新奖池
			entity.getBuyRecordMap().clear();
			entity.setDrawTimes(entity.getDrawTimes() + 1);
			entity.setPoolId(randomCfg.getBuffId());
			entity.setDeadline(0);

			entity.notifyUpdate();
			// 操作成功
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);
			//同步服务器时间 修复倒计时大于配置的时间 
			HPHeartBeat.Builder builder = HPHeartBeat.newBuilder();
			builder.setTimeStamp(HawkTime.getMillisecond());
			this.getDataGeter().sendProtocol(playerId, HawkProtocol.valueOf(HP.sys.HEART_BEAT, builder));
			// 推送信息
			this.syncActivityInfo(playerId, entity);
			// 打点
			this.getDataGeter().logBlackTechDraw(playerId, null != costItem ? costItem.getItemCount() : 0,
					randomCfg.getBuffId());

		} catch (Exception e) {
			HawkException.catchException(e);
			return Result.fail(Status.Error.BLACK_TECH_NOT_OPEN_VALUE);
		}
		return Result.success();
	}

	/**
	 * 购买加持礼包
	 * 
	 * @param protocolType
	 * @param playerId
	 * @param cfgId
	 * @param count
	 */
	Result<?> onProtocolActivityBuyReq(int protocolType, String playerId, int cfgId, int count) {
		try {
			Optional<BlackTechEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return Result.fail(Status.Error.BLACK_TECH_NOT_OPEN_VALUE);
			}
			BlackTechEntity entity = opDataEntity.get();

			if (HawkTime.getMillisecond() > entity.getDeadline()) {
				return Result.fail(Status.Error.BLACK_TECH_POOL_CLOSE_VALUE);
			}

			BlackTechPackageCfg packageCfg = HawkConfigManager.getInstance().getConfigByKey(BlackTechPackageCfg.class,
					cfgId);
			if (null == packageCfg) {
				return Result.fail(Status.Error.BLACK_TECH_PACKAGE_CFG_VALUE);
			}

			// 验证购买限制
			if (entity.getBuyTimes(cfgId) + count > packageCfg.getLimitNum()) {
				return Result.fail(Status.Error.BLACK_TECH_PACKAGE_TIMES_VALUE);
			}

			// 验证道具足够
			// 判断道具足够否
			boolean flag = this.getDataGeter().cost(playerId, packageCfg.getPriceList(), count,
					Action.BLACK_TECH_BUY_COST, false);
			if (!flag) {
				return Result.fail(Status.Error.BLACK_TECH_PACKAGE_ITEM_VALUE);
			}
			// 设置entity数据
			entity.addBuyTimes(cfgId, count);
			entity.notifyUpdate();
			// 发道具
			this.getDataGeter().takeReward(playerId, packageCfg.getItemList(), count, Action.BLACK_TECH_BUY_GAIN, true,
					RewardOrginType.ACTIVITY_REWARD);
			// 操作成功
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);
			this.syncActivityInfo(playerId, entity);

			// 打点
			this.getDataGeter().logBlackTechBuy(playerId, packageCfg.getId());

		} catch (Exception e) {
			HawkException.catchException(e);
			return Result.fail(Status.Error.BLACK_TECH_NOT_OPEN_VALUE);
		}
		return Result.success();
	}

	/**
	 * 激活buff
	 * 
	 * @param protocolType
	 * @param playerId
	 */
	Result<?> onProtocolActivityActiveReq(int protocolType, String playerId) {
		try {
			Optional<BlackTechEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return Result.fail(Status.Error.BLACK_TECH_NOT_OPEN_VALUE);
			}
			BlackTechEntity entity = opDataEntity.get();
			//没到时间不能再次激活
			if(entity.getDeadline() > HawkTime.getMillisecond()){
				return Result.fail(Status.Error.BLACK_TECH_DRAW_BUFF_VALUE);
			}
			//没有随机不能激活
			if(0 == entity.getPoolId()){
				return Result.fail(Status.Error.BLACK_TECH_NO_CFG_VALUE);
			}		
			// 配置表
			BlackTechKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BlackTechKVCfg.class);
			if (null == cfg) {
				return Result.fail(Status.Error.BLACK_TECH_NO_CFG_VALUE);
			}
			// 所谓的buff配置表(其实就是奖池配置表)
			BlackTechBuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BlackTechBuffCfg.class,
					entity.getPoolId());
			if(null == buffCfg){
				return Result.fail(Status.Error.BLACK_TECH_NO_CFG_VALUE);
			}
			// 每日激活次数限制
			if (entity.getActiveTimes() >= cfg.getActiveTimes()) {
				return Result.fail(Status.Error.BLACK_TECH_ACTIVE_TIMES_VALUE);
			}
			// 增加已激活次数
			entity.setActiveTimes(entity.getActiveTimes() + 1);
			// 设置buff到期时间
			entity.setDeadline(buffCfg.getBuffTime() + HawkTime.getMillisecond());
			//刷新金币消耗是每轮的,激活buff之后就重置了,重新计算刷新金币消耗
			entity.setDrawTimes(0);
			//设置激活的buffid
			entity.setBuffId(entity.getPoolId());
			//重置当前随机到的poolid
			entity.setPoolId(0);
			// 增加激活的
			entity.notifyUpdate();
			// 加buff
			this.getDataGeter().addBuff(playerId, entity.getBuffId(), entity.getDeadline());
			// 操作成功
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);
			this.syncActivityInfo(playerId, entity);
			// 打点
			this.getDataGeter().logBlackTechActive(playerId, entity.getBuffId());

		} catch (Exception e) {
			HawkException.catchException(e);
			return Result.fail(Status.Error.BLACK_TECH_NOT_OPEN_VALUE);
		}
		return Result.success();
	}
}
