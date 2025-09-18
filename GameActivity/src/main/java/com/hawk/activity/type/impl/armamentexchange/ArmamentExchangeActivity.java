package com.hawk.activity.type.impl.armamentexchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.armamentexchange.cfg.ArmamentExchangeActivityKVCfg;
import com.hawk.activity.type.impl.armamentexchange.cfg.ArmamentExchangeBoxCfg;
import com.hawk.activity.type.impl.armamentexchange.cfg.ArmamentExchangeCostCfg;
import com.hawk.activity.type.impl.armamentexchange.entity.ArmamentExchangeEntity;
import com.hawk.game.protocol.Activity.ArmamentExchangeInfo;
import com.hawk.game.protocol.Activity.ArmamentExchangeItem;
import com.hawk.game.protocol.Activity.ArmamentExchangeReq;
import com.hawk.game.protocol.Activity.ArmamentMainInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

public class ArmamentExchangeActivity extends ActivityBase implements AchieveProvider {

	public ArmamentExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.ARMAMENT_EXCHANGE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ARMAMENT_EXCHANGE_COMMON;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ArmamentExchangeActivity activity = new ArmamentExchangeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ArmamentExchangeEntity> queryList = HawkDBManager.getInstance()
				.query("from ArmamentExchangeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ArmamentExchangeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ArmamentExchangeEntity entity = new ArmamentExchangeEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
	}

	@Override
	public void onOpenForPlayer(String playerId) {
		Optional<ArmamentExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<ArmamentExchangeEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(opPlayerDataEntity.isPresent()){
			ArmamentExchangeEntity entity = opPlayerDataEntity.get();
			//初始化
			initExchange(entity);
			
			ArmamentMainInfo.Builder builder = ArmamentMainInfo.newBuilder();
			for(int i=0;i<entity.getExchangeList().size();i++)
				builder.addInfos(entity.getExchangeList().get(i));
			
			PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.ARMAMENT_MAIN_INFO_RES, builder));
		}
	}
	
	/**
	 * @return 检查道具id是否合法。
	 */
	public int checkItem(List<RewardItem.Builder> itemList,List<ArmamentExchangeItem> exchangeList){
		int n = 0;
		for (ArmamentExchangeItem armamentExchangeItem : exchangeList) {
			for (RewardItem.Builder itemBuilder : itemList) {
				if(itemBuilder.getItemType() == armamentExchangeItem.getItemType() &&
					itemBuilder.getItemId() == armamentExchangeItem.getItemId() ){
//					itemBuilder.getItemCount() == armamentExchangeItem.getItemNum()){
					if(itemBuilder.getItemCount() == armamentExchangeItem.getItemNum()){
						n++;
					}else{
						if(armamentExchangeItem.getItemNum()%itemBuilder.getItemCount() == 0){
							n += armamentExchangeItem.getItemNum()/itemBuilder.getItemCount();
						}
					}
					break;
				}
			}
		}
		return n;
	}

	public void exchange(String playerId,ArmamentExchangeReq req){
		Optional<ArmamentExchangeEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(opPlayerDataEntity.isPresent()){
			//check 客户端数据是否正常
			for (ArmamentExchangeItem  builder : req.getItemsList()) {
				if(builder.getItemNum() == 0)
					continue;
				//检查该id 是否包含在 配置内
				if(ArmamentExchangeCostCfg.map!=null){
					List<Integer> list = ArmamentExchangeCostCfg.map.get(req.getIdx());
					if(!list.contains(builder.getItemId())){
						PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.ARMAMENT_EXCHANGE_REQ_VALUE,Status.Error.ARMAMENT_ERROR_NOTFINDID_VALUE);
						return;
					}
				}
			}
			
			int n = checkItem(ArmamentExchangeCostCfg.itemMap.get(req.getIdx()),req.getItemsList());
			if(n!=req.getExchangeNum()){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.ARMAMENT_EXCHANGE_REQ_VALUE,Status.Error.ARMAMENT_ERROR_NUMBER_VALUE);
				return;
			}
			
			ArmamentExchangeEntity entity = opPlayerDataEntity.get();
			ArmamentExchangeInfo.Builder curBuilder = null;
			for (ArmamentExchangeInfo.Builder builder: entity.getExchangeList()) {
				if(builder.getIdx() == req.getIdx()){
					curBuilder = builder;
					break;
				}
			}
			
			//未开启高级宝箱
			if(curBuilder == null){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.ARMAMENT_EXCHANGE_REQ_VALUE,Status.Error.ARMAMENT_ERROR_NOT_OPEN_VALUE);
				return;
			}
						
			ArmamentExchangeBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(ArmamentExchangeBoxCfg.class, req.getIdx());
			//兑换数量不足
			if(boxCfg.getTimesLimit() - (curBuilder.getNum()+req.getExchangeNum()) < 0){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.ARMAMENT_EXCHANGE_REQ_VALUE,Status.Error.ARMAMENT_ERROR_EXCHANGE_NUMBER_VALUE);
				return;
			}
			
			List<RewardItem.Builder> costItems = new ArrayList<RewardItem.Builder>();
			for (ArmamentExchangeItem  builder : req.getItemsList()) {
				RewardItem.Builder newBuilder = RewardItem.newBuilder();
				newBuilder.setItemType(builder.getItemType());
				newBuilder.setItemId(builder.getItemId());
				newBuilder.setItemCount(builder.getItemNum());
				costItems.add(newBuilder);
			}
			
			
			RewardItem.Builder costItem = RewardItem.newBuilder();
			costItem.setItemType(boxCfg.getPriceItem().getItemType());
			costItem.setItemId(boxCfg.getPriceItem().getItemId());
			costItem.setItemCount(boxCfg.getPriceItem().getItemCount()*req.getExchangeNum());
			//添加货币
			costItems.add(  costItem );
			
			boolean flag = this.getDataGeter().cost(playerId, costItems, 1, Action.ARMAMENT_EXCHANGE_COST, true);
			if (!flag) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.ARMAMENT_EXCHANGE_REQ_VALUE,Status.Error.ARMAMENT_ERROR_COST_VALUE);
				return;
			}
			
			curBuilder.setNum(curBuilder.getNum()+req.getExchangeNum());
			
			//首次兑换打点记录
			if(entity.getIsFirst() == 0){
				entity.setIsFirst(1);
				this.getDataGeter().logArmamentExchangeFirst(playerId,getActivityTermId());
			}
			
			entity.notifyUpdate();
			
			//发奖励
			List<RewardItem.Builder> rewardItems = new ArrayList<>();
			rewardItems.add(boxCfg.getAwardItem());
			this.getDataGeter().takeReward(playerId,rewardItems, req.getExchangeNum(), Action.ARMAMENT_EXCHANGE_REWARD, true);
			
			syncActivityDataInfo(playerId);
		}
	}
	
	public void initExchange(ArmamentExchangeEntity entity){
		if(entity.getExchangeList().isEmpty()){
			//添加免费宝箱
			ConfigIterator<ArmamentExchangeBoxCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ArmamentExchangeBoxCfg.class);
			while (configIterator.hasNext()) {
				ArmamentExchangeBoxCfg cfg = configIterator.next();
				if(cfg.getType() == 0){
					ArmamentExchangeInfo.Builder builder = ArmamentExchangeInfo.newBuilder();
					builder.setIdx(cfg.getId());
					builder.setNum(0);
					entity.getExchangeList().add(builder);
				}
			}
			
			if(entity.getIsOpen()>0){//开启高级宝箱验证
				addExchange(entity);
			}
			entity.notifyUpdate();
		}
	}
	
	public void addExchange(ArmamentExchangeEntity entity){
		ConfigIterator<ArmamentExchangeBoxCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ArmamentExchangeBoxCfg.class);
		while (configIterator.hasNext()) {
			ArmamentExchangeBoxCfg cfg = configIterator.next();
			if(cfg.getType() == 1){
				ArmamentExchangeInfo.Builder builder = ArmamentExchangeInfo.newBuilder();
				builder.setIdx(cfg.getId());
				builder.setNum(0);
				entity.getExchangeList().add(builder);
			}
		}
		
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!this.isAllowOprate(event.getPlayerId())) {
			return;
		}
		if (event.isCrossDay()) {
			Optional<ArmamentExchangeEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
			if (!opEntity.isPresent()) {
				return;
			}
			ArmamentExchangeEntity entity = opEntity.get();
			entity.getExchangeList().clear();
			initExchange(opEntity.get());
		}
	}
	
	@Subscribe
	public void onEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<ArmamentExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		String goodsId = event.getGiftId();
		ArmamentExchangeEntity entity = opEntity.get();
		if(entity.getIsOpen()>0){
			return; //已开启
		}
		
		ArmamentExchangeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ArmamentExchangeActivityKVCfg.class);
		
		boolean rechargeTag = false;
		if(goodsId.equals(cfg.getIosPayId())){//ios
			rechargeTag = true;
		}else if(goodsId.equals(cfg.getAndroidPayId())){//android
			rechargeTag = true;
		}
		
		if(rechargeTag){
			entity.setIsOpen(1);
			addExchange(entity);
			this.getDataGeter().takeReward(playerId,cfg.getAwardItems(), 1, Action.ARMAMENT_EXCHANGE_ALLOPEN, true);
			entity.notifyUpdate();
			syncActivityDataInfo(playerId);
		}
		
	}
	
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return null;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		return Optional.empty();
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
}
