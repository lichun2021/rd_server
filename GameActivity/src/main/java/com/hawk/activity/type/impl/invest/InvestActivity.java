package com.hawk.activity.type.impl.invest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.invest.cfg.InvestProductCfg;
import com.hawk.activity.type.impl.invest.entity.InvestEntity;
import com.hawk.activity.type.impl.invest.entity.InvestItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.InvestInfoList;
import com.hawk.game.protocol.Activity.InvestInfoPB;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;

/**
 * 投资理财活动
 * 
 * @author lating
 *
 */
public class InvestActivity extends ActivityBase {
	
	private static final String INVEST_REDIS_KEY = "invest_activity";  // + ":" + termId + ":" + serverId;

	public InvestActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.INVEST_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		InvestActivity activity = new InvestActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<InvestEntity> queryList = HawkDBManager.getInstance()
				.query("from InvestEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			InvestEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		InvestEntity entity = new InvestEntity(playerId, termId);
		return entity;
	}
	
	/**
	 * 登录
	 * 
	 */
	@Override
	public void onPlayerLogin(String playerId) {
		
	}
	
	/**
	 * 跨天事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		
	}

	@Override
	public void onOpen() {
		
	}
	
	/**
	 * 活动展示结束
	 * 
	 */
	@Override
	public void onHidden() {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				grantInvestBackReward();
				return null;
			}
		});
	}
	
	/**
	 * 补发理财投资本金和增益
	 */
	private void grantInvestBackReward() {
		String serverId = this.getDataGeter().getServerId();
		grantInvestBackReward(serverId);
		List<String> serverList = this.getDataGeter().getMergeServerList();
		if (serverList == null || serverList.isEmpty()) {
			return;
		}
		
		for (String server : serverList) {
			if (serverId.equals(server)) {
				continue;
			}
			
			grantInvestBackReward(server);
		}
	}
	
	/**
	 * 补发理财投资本金和增益
	 * 
	 * @param serverId
	 */
	private void grantInvestBackReward(String serverId) {
		ActivityEntity activityEntity = getActivityEntity();
		int termId = activityEntity.getTermId();
		String key = INVEST_REDIS_KEY + ":" + termId + ":" + serverId;		
		Map<String, String> map = ActivityGlobalRedis.getInstance().hgetAll(key);
		
		for (Entry<String, String> entry : map.entrySet()) {
			try {
				String[] keyInfo = entry.getKey().split(":");
				String playerId = keyInfo[0];
				
				if (!this.getDataGeter().isServerPlayer(playerId)) {
					continue;
				}

				int productId = Integer.valueOf(keyInfo[1]);
				HawkDBEntity dbEntity = loadFromDB(playerId, termId);
				if (dbEntity == null) {
					HawkLog.errPrintln("InvestActivity end detect consumeBack failed, dbEntity not eixst, playerId: {}, serverId: {}, productId: {}", playerId, serverId, productId);
					continue;
				}
				
				InvestProductCfg productCfg = HawkConfigManager.getInstance().getConfigByKey(InvestProductCfg.class, productId);
				if (productCfg == null) {
					HawkLog.errPrintln("InvestActivity end detect consumeBack failed, config error, playerId: {}, productId: {}", playerId, productId);
					continue;
				}
				
				InvestEntity entity = (InvestEntity)dbEntity;
				InvestItem item = entity.getInvestItem(productId);
				
				if (item != null && item.getProfitBack() == 0) {
					List<RewardItem.Builder> awardItemList = getProfileList(productCfg, item);
					// 发邮件
					this.getDataGeter().sendMail(playerId, MailId.INVEST_PROFIT,
							new Object[] { this.getActivityCfg().getActivityName() },
							new Object[] { this.getActivityCfg().getActivityName() }, 
							new Object[] {},
							awardItemList, false); 
					
					ActivityGlobalRedis.getInstance().hDel(key, entry.getKey());

					item.setProfitBack(1);
					entity.notifyUpdate();
					HawkLog.logPrintln("InvestActivity end detect consumeBack success, playerId: {}, serverId: {}, productId: {}", playerId, serverId, productId);
				} else {
					HawkLog.logPrintln("InvestActivity end detect consumeBack failed, playerId: {}, serverId: {}, productId: {}, profitBack: {}", playerId, serverId, productId, item != null ? item.getProfitBack() : -1);
				}
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

	/**
	 * 同步礼包信息
	 * 
	 * @param playerId
	 */
	public int syncInvestProductInfo(String playerId) {
		if (isHidden(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<InvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("InvestActivity syncInvestInfo failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		InvestEntity entity = opEntity.get();
		InvestInfoList.Builder listBuilder = InvestInfoList.newBuilder();
		for (InvestItem item : entity.getItemList()) {
			InvestInfoPB.Builder builder = item.toBuilder();
			listBuilder.addProduct(builder);
		}
		
		pushToPlayer(playerId, HP.code.INVEST_INFO_PUSH_VALUE, listBuilder);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 购买理财产品
	 * 
	 * @param playerId
	 * @param productId 理财产品ID
	 * @param productId 是否购买加成道具
	 */
	public int productInvest(String playerId, int productId, int investAmount, boolean addCuctomer) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<InvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("InvestActivity invest failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		InvestProductCfg productCfg = HawkConfigManager.getInstance().getConfigByKey(InvestProductCfg.class, productId);
		if (productCfg == null) {
			HawkLog.errPrintln("InvestActivity invest failed, config error, playerId: {}, productId: {}", playerId, productId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		InvestEntity entity = opEntity.get();
		InvestItem item = entity.getInvestItem(productId);
		if (item != null) {
			HawkLog.errPrintln("InvestActivity invest failed, product has already bought, playerId: {}, productId: {}", playerId, productId);
			return Status.Error.INVEST_PRODUCT_ON_STATUS_VALUE;
		}
		
		RewardItem.Builder priceItem = productCfg.getInvestConsumeItem();
		int amountLimit = productCfg.getInvestMaxAmount();
		if (addCuctomer) {
			amountLimit += productCfg.getComsumerAmount();
		}
		
		if (investAmount < priceItem.getItemCount() || investAmount > amountLimit) {
			HawkLog.errPrintln("InvestActivity invest failed, investAmount invalid, playerId: {}, productId: {}, investAmount: {}, investMax: {}", playerId, productId, investAmount, productCfg.getInvestMaxAmount());
			return Status.Error.INVEST_AMOUNT_INVALID_VALUE;
		}
		
		priceItem.setItemCount(investAmount);
		List<RewardItem.Builder> consumeItemList = new ArrayList<RewardItem.Builder>(2);
		consumeItemList.add(priceItem);
		if (addCuctomer) {
			consumeItemList.add(productCfg.getCustomerPriceItem());
		}
		
		boolean success = getDataGeter().consumeItems(playerId, consumeItemList, HP.code.INVEST_BUY_REQ_VALUE, Action.INVEST_PROFIT);
		if (!success) {
			HawkLog.errPrintln("InvestActivity invest failed, consumeItems not enought, playerId: {}, productId: {}, addCuctomer: {}", playerId, productId, addCuctomer);
			return 0; // 这里不要去假设消耗的时什么东西，不足时在掉消耗接口时已经返回错误提示了，所以这里可以不用去管到底是什么不足的问题
		}
		
		item = new InvestItem(productId);
		item.setInvestAmount(investAmount);
		item.setAddCustomer(addCuctomer ? 1: 0);
		entity.addItem(item);
		
		String key = getRedisKey(entity.getTermId());
		String innerKey = playerId + ":" + productId;
		ActivityGlobalRedis.getInstance().hset(key, innerKey, String.valueOf(item.getPurchaseTime()));
		
		syncInvestProductInfo(playerId);
		
		// tlog日志
		this.getDataGeter().logInvest(playerId, productId, investAmount, addCuctomer, false);
		
		HawkLog.logPrintln("InvestActivity invest success, playerId: {}, productId: {}, addCuctomer: {}, investAmount: {}", playerId, productId, addCuctomer, investAmount);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 领取理财投资收益和本金
	 * 
	 * @return
	 */
	public int receiveInvestProfit(String playerId, int productId) {
		if (isHidden(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<InvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("InvestActivity receiveInvestProfit failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		InvestEntity entity = opEntity.get();
		InvestItem item = entity.getInvestItem(productId);
		if (item == null || item.getProfitBack() != 0) {
			HawkLog.errPrintln("InvestActivity receiveInvestProfit failed, profit back already, playerId: {}, productId: {}, profitBack: {}", playerId, productId, item != null ? item.getProfitBack() : -1);
			return Status.Error.INVEST_PROFIT_RECEIVED_VALUE;
		}
		
		InvestProductCfg productCfg = HawkConfigManager.getInstance().getConfigByKey(InvestProductCfg.class, productId);
		if (productCfg == null) {
			HawkLog.errPrintln("InvestActivity receiveInvestProfit failed, config error, playerId: {}, productId: {}", playerId, productId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		long timeNow = HawkTime.getMillisecond();
		if (timeNow - item.getPurchaseTime() < productCfg.getProductDuration()) {
			HawkLog.errPrintln("InvestActivity receiveInvestProfit failed, rec time error, playerId: {}, productId: {}, purchaseTime: {}", playerId, productId, item.getPurchaseTime());
			return Status.Error.INVEST_DURATION_NOT_END_VALUE;
		}
		
		List<RewardItem.Builder> awardItemList = getProfileList(productCfg, item);
		
		{
			ActivityReward reward = new ActivityReward(awardItemList, Action.INVEST_PROFIT);
			postReward(playerId, reward, false);
		}
		
		item.setProfitBack(1);
		entity.notifyUpdate();
		
		String key = getRedisKey(entity.getTermId());
		String innerKey = playerId + ":" + productId;
		ActivityGlobalRedis.getInstance().hDel(key, innerKey);
		
		syncInvestProductInfo(playerId);
		
		this.getDataGeter().sendMail(playerId, MailId.INVEST_PROFIT_RECEIVE,
				new Object[] { this.getActivityCfg().getActivityName() },
				new Object[] { this.getActivityCfg().getActivityName() }, 
				new Object[] {item.getInvestAmount(), awardItemList.get(1).getItemCount()},
				awardItemList, true); 
		
		HawkLog.logPrintln("InvestActivity receiveInvestProfit success, playerId: {}, productId: {}", playerId, productId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 投资取消
	 * 
	 * @param playerId
	 * @param productId
	 * @return
	 */
	public int investCancel(String playerId, int productId) {
		if (isHidden(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<InvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("InvestActivity investCancel failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		InvestEntity entity = opEntity.get();
		InvestItem item = entity.getInvestItem(productId);
		if (item == null) {
			return Status.Error.INVEST_NOT_MATCH_VALUE;
		}
		
		if (item.getProfitBack() != 0) {
			HawkLog.errPrintln("InvestActivity investCancel failed, profit back already, playerId: {}, productId: {}, profitBack: {}", playerId, productId, item != null ? item.getProfitBack() : -1);
			return Status.Error.INVEST_DURATION_END_VALUE;
		}
		
		InvestProductCfg productCfg = HawkConfigManager.getInstance().getConfigByKey(InvestProductCfg.class, productId);
		if (productCfg == null) {
			HawkLog.errPrintln("InvestActivity investCancel failed, config error, playerId: {}, productId: {}", playerId, productId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		long timeNow = HawkTime.getMillisecond();
		if (timeNow - item.getPurchaseTime() >= productCfg.getProductDuration()) {
			return Status.Error.INVEST_DURATION_END_VALUE;
		}
		
		int amount = item.getInvestAmount();
		RewardItem.Builder priceItem = productCfg.getInvestConsumeItem();
		priceItem.setItemCount(amount);
		
		List<RewardItem.Builder> awardItemList = new ArrayList<RewardItem.Builder>(1);
		awardItemList.add(priceItem);
		{
			ActivityReward reward = new ActivityReward(awardItemList, Action.INVEST_CANCEL);
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		entity.removeInvestItem(item);
		entity.notifyUpdate();
		
		String key = getRedisKey(entity.getTermId());
		String innerKey = playerId + ":" + productId;
		ActivityGlobalRedis.getInstance().hDel(key, innerKey);
		
		syncInvestProductInfo(playerId);
		
		this.getDataGeter().sendMail(playerId, MailId.INVEST_CANCEL,
				new Object[] { this.getActivityCfg().getActivityName() },
				new Object[] { this.getActivityCfg().getActivityName() }, 
				new Object[] {item.getInvestAmount()},
				Collections.emptyList(), true); 
		
		// tlog日志
		this.getDataGeter().logInvest(playerId, productId, 0, false, true);
		
		HawkLog.logPrintln("InvestActivity investCancel success, playerId: {}, productId: {}", playerId, productId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 获取投资收益列表
	 * 
	 * @param productCfg
	 * @param item
	 * @return
	 */
	private List<RewardItem.Builder> getProfileList(InvestProductCfg productCfg, InvestItem item) {
		long amount = item.getInvestAmount();
		
		List<RewardItem.Builder> awardItemList = new ArrayList<RewardItem.Builder>(2);
		RewardItem.Builder priceItem = productCfg.getInvestConsumeItem();
		priceItem.setItemCount(amount);
		awardItemList.add(priceItem);
		
		int profileRatio = productCfg.getProductProfit();
		if (item.getAddCustomer() != 0) {
			//amount += productCfg.getComsumerAmount();
			profileRatio += productCfg.getCustomerProfit();
		}
		
		RewardItem.Builder awardItem = RewardItem.newBuilder();
		awardItem.setItemType(ItemType.PLAYER_ATTR_VALUE * 10000);
		awardItem.setItemId(PlayerAttr.GOLD_VALUE);
		awardItem.setItemCount((long) Math.ceil(amount * profileRatio * 1D / 10000));
		awardItemList.add(awardItem);
		
		return awardItemList;
	}
	
	/**
	 * 获取redisKey
	 * @param termId
	 * @return
	 */
	private String getRedisKey(int termId) {
		return INVEST_REDIS_KEY + ":" + termId + ":" + this.getDataGeter().getServerId();
	}
	
}
