package com.hawk.activity.type.impl.equipBlackMarket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.EquipBlackMarketBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.equipBlackMarket.cfg.EquipBlackMarketGiftCfg;
import com.hawk.activity.type.impl.equipBlackMarket.cfg.EquipBlackMarketRefineCfg;
import com.hawk.activity.type.impl.equipBlackMarket.entity.EquipBlackMarketEntity;
import com.hawk.game.protocol.Activity.EquipBlackMarketInfoResp;
import com.hawk.game.protocol.Activity.EquipBlackMarketRefinningResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 装备黑市活动
 * @author che
 *
 */
public class EquipBlackMarketActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public EquipBlackMarketActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.EQUIP_BLACK_MARKET_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<EquipBlackMarketEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				logger.error("on playerLogin init EquipBlackMarketEntity error, no entity created");
			}	
			callBack(playerId, MsgId.EQUIP_BLACK_MARKET_INIT, ()-> {
				//同步界面信息给客户端
				this.syncActivityInfo(playerId, opEntity.get());
			});
			
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.EQUIP_BLACK_MARKET_INIT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<EquipBlackMarketEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		EquipBlackMarketActivity activity = new EquipBlackMarketActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<EquipBlackMarketEntity> queryList = HawkDBManager.getInstance()
				.query("from EquipBlackMarketEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			EquipBlackMarketEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		EquipBlackMarketEntity entity = new EquipBlackMarketEntity(playerId, termId);
		return entity;
	}

	
	
	@Subscribe
	public void onEquipBlackMarketBuyEvent(EquipBlackMarketBuyEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		String playerId = event.getPlayerId();
		Optional<EquipBlackMarketEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		EquipBlackMarketEntity entity = opPlayerDataEntity.get();
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		entity.addGoodsId(event.getGiftId());
		this.syncActivityInfo(playerId,entity);
		logger.info("onEquipBlackMarketBuyEvent  playerId:{}, packageId:{}", playerId, event.getGiftId());
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<EquipBlackMarketEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		EquipBlackMarketEntity entity = opPlayerDataEntity.get();
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		//重置分享记录
		entity.resetBuyPackages();
		//推送新数据
		syncActivityInfo(playerId, entity);
		logger.info("EquipBlackMarketActivity onContinueLogin resetBuyIds playerId:{}",playerId);
	}
	
	public boolean canBuyGift(String playerId,String gift){
		Optional<EquipBlackMarketEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		EquipBlackMarketEntity entity = opPlayerDataEntity.get();
		if (!opPlayerDataEntity.isPresent()) {
			return false;
		}
		return !entity.isBuyGift(gift);
	}
	
	
	
	// 同步数据消息给玩家
	public void syncActivityInfo(String playerId, EquipBlackMarketEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}
		EquipBlackMarketInfoResp.Builder builder =  EquipBlackMarketInfoResp.newBuilder();
		List<String> leftList = this.getLastPackageId(entity.getBuyPackageSet(), playerId);
		if(leftList.size() > 0){
			for(String gid : leftList){
				builder.addBuyList(gid);
			}
		}
		Set<Entry<Integer, Integer>> refines= entity.getRefineMap().entrySet();
		for(Entry<Integer, Integer> entry : refines){
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(entry.getKey());
			kbuilder.setVal(entry.getValue());
			builder.addRefines(kbuilder);
		}
		String lastBuyPackage = entity.getLastBuyPackage();
		
		builder.setLastGiftId(lastBuyPackage);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.EQUIP_BLACK_MARKET_INFO_RESP_VALUE, builder));
	}
	
	
	public List<String> getLastPackageId(Set<String> buyList,String playerId){
		List<EquipBlackMarketGiftCfg> list = HawkConfigManager.getInstance().
				getConfigIterator(EquipBlackMarketGiftCfg.class).toList();
		List<String> left = new ArrayList<String>();
		String platform = getDataGeter().getPlatform(playerId);
		if(HawkOSOperator.isEmptyString(platform)){
			logger.error("playerId:" + playerId +" platform msg null.");
			return left;
		}
		for(EquipBlackMarketGiftCfg cfg : list){
			if(!platform.equalsIgnoreCase(cfg.getStrPlatform())){
				continue;
			}
			if(buyList.contains(cfg.getPayGiftId())){
				continue;
			}
			left.add(cfg.getPayGiftId());
		}
		return left;
	}
	
	
	public void onMarketRefine(String playerId,int refineId,int refineCount,int protoType){
		EquipBlackMarketRefineCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(EquipBlackMarketRefineCfg.class,refineId);
		if(cfg == null){
			return;
		}
		
		int term = this.getActivityTermId();
		if(term == 0){
			return;
		}
		Optional<EquipBlackMarketEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		EquipBlackMarketEntity entity = opPlayerDataEntity.get();
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		int rCount = entity.getRefineCount(refineId);
		if(rCount +refineCount > cfg.getLimit()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protoType, Status.Error.ITEM_EXCHANGE_TIME_LIMIT_VALUE);
			return;
		}
		List<Reward.RewardItem.Builder> source1 = RewardHelper.toRewardItemList(cfg.getMaterial1());
		List<Reward.RewardItem.Builder> source2 = RewardHelper.toRewardItemList(cfg.getMaterial2());
		source1.addAll(source2);
		for(Reward.RewardItem.Builder rb : source1){
			long icounbt = rb.getItemCount();
			rb.setItemCount(icounbt * refineCount);
		}
		boolean consumeResult = this.getDataGeter().consumeItems(playerId, source1, protoType, 
				Action.EQUIP_BLACK_MARKET_REFINE_COST);
		if (consumeResult == false) {
			return;
		}
		List<Reward.RewardItem.Builder> achieve = RewardHelper.toRewardItemList(cfg.getTarget());
		for(Reward.RewardItem.Builder rb : achieve){
			long icounbt = rb.getItemCount();
			rb.setItemCount(icounbt * refineCount);
		}
		this.getDataGeter().takeReward(playerId,achieve, 1, Action.EQUIP_BLACK_MARKET_REFINE_ACHIEVE,
				true,RewardOrginType.ACTIVITY_REWARD);
		entity.addRefineCount(refineId, refineCount);
		EquipBlackMarketRefinningResp.Builder builder =  EquipBlackMarketRefinningResp.newBuilder();
		Set<Entry<Integer, Integer>> refines= entity.getRefineMap().entrySet();
		for(Entry<Integer, Integer> entry : refines){
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(entry.getKey());
			kbuilder.setVal(entry.getValue());
			builder.addRefines(kbuilder);
		}
		builder.setRefineId(refineId);
		builder.setCount(refineCount);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.EQUIP_BLACK_MARKET_REFINNING_RESP_VALUE, builder));
		this.getDataGeter().logEquipBlackMarketRefine(playerId, term, refineId, refineCount);
		logger.info("onMarketRefine  playerId:{}, refineId:{}, refineCount:{}", playerId, refineId,refineCount);
	}
	
	
	

}
