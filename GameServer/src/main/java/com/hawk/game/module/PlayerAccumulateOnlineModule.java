package com.hawk.game.module;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.game.config.AccumulateOnlineCfg;
import com.hawk.game.entity.AccumulateOnlineEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.AccumulateOnline.AccumulateOnlineInfoResp;
import com.hawk.game.protocol.AccumulateOnline.AccumulateOnlineRedPointSync;
import com.hawk.game.protocol.HP;
import com.hawk.log.Action;

/**
 * 累积在线
 * @author golden
 *
 */
public class PlayerAccumulateOnlineModule extends PlayerModule {

	/**
	 * 日志
	 */
	private static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * tick周期
	 */
	private static final long TICK_PERIOD = 3000L;
	
	/**
	 * 上次tick时间
	 */
	private long LAST_TICK_TIME;
	
	/**
	 * 是否有小红点
	 */
	private boolean HAS_RED_POINT;
	
	/**
	 * 构造
	 * @param player
	 */
	public PlayerAccumulateOnlineModule(Player player) {
		super(player);
	}

	/**
	 * 登录
	 */
	@Override
	protected boolean onPlayerLogin() {
		if (!hasActivity()) {
			return true;
		}
		
		// 跨天检测
		AccumulateOnlineEntity entity = player.getData().getAccumulateOnlineEntity();
		if (player.getPlayerRegisterDays() != entity.getDayCount()) {
			resetData();
		}
		
		// 初始化红点状态
		HAS_RED_POINT = canReceiveReward();
		syncRedPoint();
		return true;
	}
	
	/**
	 * 登出
	 */
	@Override
	protected boolean onPlayerLogout() {
		if (!hasActivity()) {
			return true;
		}
		AccumulateOnlineEntity entity = player.getData().getAccumulateOnlineEntity();
		entity.setOnlineTime(getOnlineTime());
		return true;
	}
	
	/**
	 * tick 检测小红点
	 */
	@Override
	public boolean onTick() {
		long currentTime = HawkTime.getMillisecond();
		if (currentTime < LAST_TICK_TIME + TICK_PERIOD) {
			return true;
		}
		LAST_TICK_TIME = currentTime;
		
		if (!hasActivity()) {
			return true;
		}
		if (checkRedPointUpdate()) {
			syncRedPoint();
		}
		return true;
	}
	
	/**
	 * 跨天
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onPlayerAcreossDayLogin(PlayerAcrossDayLoginMsg msg) {
		resetData();
		syncRedPoint();
		return true;
	}
	
	/**
	 * 重置数据
	 */
	private void resetData() {
		AccumulateOnlineEntity entity = player.getData().getAccumulateOnlineEntity();
		entity.setDayCount(player.getPlayerRegisterDays());
		entity.setReceivedId(0);
		entity.setOnlineTime(0L);
		logger.info("accmulate online data reset, playerId:{}", player.getId());
	}
	
	/**
	 * 检测小红点
	 * @return
	 */
	private boolean checkRedPointUpdate() {
		boolean needUpdate = false;
		if (canReceiveReward() != HAS_RED_POINT) {
			needUpdate = true;
			HAS_RED_POINT = !HAS_RED_POINT;
		}
		return needUpdate;
	}
	
	/**
	 * 推送数据
	 */
	private void syncData() {
		AccumulateOnlineInfoResp.Builder builder = AccumulateOnlineInfoResp.newBuilder();
		AccumulateOnlineEntity entity = player.getData().getAccumulateOnlineEntity();
		builder.setNextCanReceived(entity.getReceivedId() + 1);
		
		long nextRewardTime = 0;
		AccumulateOnlineCfg cfg = AssembleDataManager.getInstance().getAccumulateOnlineCfg(entity.getDayCount(), entity.getReceivedId() + 1);
		if (cfg != null) {
			long loginTime = player.getData().getPlayerEntity().getLoginTime();
			long lastReceiveTime = entity.getReceivedTime();
			if (lastReceiveTime > loginTime) {
				nextRewardTime = lastReceiveTime + cfg.getOnlineTime();
			} else {
				nextRewardTime = player.getData().getPlayerEntity().getLoginTime() + cfg.getOnlineTime() - entity.getOnlineTime();
			}
		}
		builder.setNextRewardTime(nextRewardTime);
		builder.setTonightTime(HawkTime.getNextAM0Date());
		builder.setServerOpenDays(entity.getDayCount());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ACCUMULATE_ONLINE_INFO_RESP, builder));
	}
	
	/**
	 * 推送小红点
	 */
	private void syncRedPoint() {
		AccumulateOnlineRedPointSync.Builder builder = AccumulateOnlineRedPointSync.newBuilder();
		builder.setHasActivity(hasActivity());
		builder.setHasRedPoint(HAS_RED_POINT);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ACCUMULATE_ONLINE_RED_POINT_SYNC, builder));
	}
	
	/**
	 * 是否已经领取了全部奖励
	 * @return
	 */
	private boolean hasReceivedAll() {
		AccumulateOnlineEntity entity = player.getData().getAccumulateOnlineEntity();
		int maxDayCount = AssembleDataManager.getInstance().getMaxAccmulateOnlineDayCount();
		AccumulateOnlineCfg cfg = AssembleDataManager.getInstance().getAccumulateOnlineCfg(entity.getDayCount(), entity.getReceivedId() + 1);
		if (cfg == null && entity.getDayCount() >= maxDayCount) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否有本天的活动
	 * @return
	 */
	private boolean hasActivity() {
		int serverOpenDays = player.getPlayerRegisterDays();
		return AssembleDataManager.getInstance().getAccumulateOnlineCfgSize(serverOpenDays) != 0 && !hasReceivedAll();
	}
	
	/**
	 * 获取在线时长
	 * @return
	 */
	private long getOnlineTime() {
		AccumulateOnlineEntity entity = player.getData().getAccumulateOnlineEntity();
		long onlineTime = 0L;
		long lastReceivedTime = entity.getReceivedTime();
		long loginTime = player.getData().getPlayerEntity().getLoginTime();
		long currentTime = HawkTime.getMillisecond();
		if (lastReceivedTime > loginTime) {
			onlineTime = currentTime - lastReceivedTime;
		} else {
			onlineTime = entity.getOnlineTime() + (currentTime - loginTime);
		}
		return onlineTime;
	}
	
	/**
	 * 是否可以领取奖励
	 * @return
	 */
	private boolean canReceiveReward() {
		AccumulateOnlineEntity entity = player.getData().getAccumulateOnlineEntity();
		AccumulateOnlineCfg cfg = AssembleDataManager.getInstance().getAccumulateOnlineCfg(entity.getDayCount(), entity.getReceivedId() + 1);
		if (cfg == null) {
			return false;
		}
		return getOnlineTime() >= cfg.getOnlineTime();
	}
	
	@ProtocolHandler(code = HP.code.ACCUMULATE_ONLINE_INFO_REQ_VALUE)
	private boolean onActitityInfoReq(HawkProtocol protocol) {
		if (!hasActivity()) {
			return false;
		}
		syncData();
		return true;
	}
	
	@ProtocolHandler(code = HP.code.ACCUMULATE_ONLINE_REWARD_REQ_VALUE)
	private boolean onTakeReward(HawkProtocol protocol) {
		if (!hasActivity()) {
			return false;
		}
		if (!canReceiveReward()) {
			syncData();
			return false;
		}
		
		AccumulateOnlineEntity entity = player.getData().getAccumulateOnlineEntity();
		
		// 发奖
		AccumulateOnlineCfg cfg = AssembleDataManager.getInstance().getAccumulateOnlineCfg(entity.getDayCount(), entity.getReceivedId() + 1);
		AwardItems reward = AwardItems.valueOf();
		reward.addItemInfos(cfg.getRewardList());
		reward.rewardTakeAffectAndPush(player, Action.ACCUMULATE_ONLINE, true, null);
		
		// 状态重置
		entity.setReceivedId(entity.getReceivedId() + 1);
		entity.setOnlineTime(0);
		entity.setReceivedTime(HawkTime.getMillisecond());
		
		// 数据推送
		syncData();
		if (checkRedPointUpdate()) {
			syncRedPoint();
		}
		
		logger.info("accumulate online get reward, playerId:{}, receivedId:{}", player.getId(), entity.getReceivedId());
		return true;
	}
}
