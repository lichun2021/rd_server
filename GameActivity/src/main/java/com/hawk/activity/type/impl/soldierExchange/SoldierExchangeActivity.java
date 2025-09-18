package com.hawk.activity.type.impl.soldierExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.google.common.eventbus.Subscribe;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.soldierExchange.cfg.SoldierExchangeAchieveCfg;
import com.hawk.activity.type.impl.soldierExchange.cfg.SoldierExchangeKVCfg;
import com.hawk.activity.type.impl.soldierExchange.entity.SoldierExchangeActivityEntity;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SoldierExchange.PBSEActivitySync;
import com.hawk.game.protocol.SoldierExchange.PBSEResp;
import com.hawk.game.protocol.SoldierExchange.PBSEShopBuyCnt;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class SoldierExchangeActivity extends ActivityBase implements AchieveProvider{


	public SoldierExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SOLDIER_EXCHANGE;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Subscribe
	public void onEvent(BuildingLevelUpEvent event) {
		// 非主堡不处理
		if (event.getBuildType() != Const.BuildingType.CONSTRUCTION_FACTORY_VALUE) {
			return;
		}
		String playerId = event.getPlayerId();
		// 活动开启,记录开启初始化的大本等级, 如果首次,则同步活动消息
		if (!isOpening(playerId)) {
			return;
		}
		this.syncActivityStateInfo(playerId);
		this.syncActivityDataInfo(playerId);
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SoldierExchangeActivity activity =  new SoldierExchangeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<SoldierExchangeActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SoldierExchangeActivityEntity entity = opEntity.get();

		PBSEActivitySync.Builder builder = PBSEActivitySync.newBuilder();
		builder.setExchangeTimes(entity.getHistorList().size());
		builder.addAllHistroyId(entity.getHistorList());
		if(entity.getExchangeType()!=0){
			builder.setLastExchangeType(SoldierType.valueOf(entity.getExchangeType()));
		}
		for(Entry<Integer, Integer> ent : entity.getShopItems().entrySet()){
			builder.addShopBuycnt(PBSEShopBuyCnt.newBuilder().setShopId(ent.getKey()).setBuyCnt(ent.getValue()));
		}
		
		for (String hisId : entity.getHistorList()) {
			byte[] bytes = ActivityGlobalRedis.getInstance().getRedisSession().getBytes(hisId.getBytes());
			PBSEResp.Builder his = PBSEResp.newBuilder();
			try {
				his.mergeFrom(bytes);
				builder.addHistroyDetail(his);
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
		}
		builder.setCd(entity.getCoolTime());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.SOLDIER_EXCHANGE_INFO_SYNC, builder));
		
//		// 活动sync 
//		message PBSEActivitySync{
//			optional int32 exchangeTimes = 1; // 已转换次数 
//			optional SoldierType lastExchangeType = 2; // 上次转换, 可买对应商店内容
//			repeated PBSEShopBuyCnt shopBuycnt = 3; 
//		}
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		SoldierExchangeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SoldierExchangeKVCfg.class);
		if (cfg.getBuildMinLevel() > this.getDataGeter().getConstructionFactoryLevel(playerId)) {
			return true;
		}
		return super.isActivityClose(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<SoldierExchangeActivityEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		SoldierExchangeActivityEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}
	
	@Override
	public void onOpen() {		
		Set<String> onlinePlayers = this.getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayers) {
			this.callBack(playerId, GameConst.MsgId.SPACE_MACHA_ACTIVITY_OPEN, ()->{
				initAchieveInfo(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}

	@Override
	public void onPlayerLogin(String playerId) {
//		syncActivityDataInfo(playerId);
	}
	

	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		syncActivityDataInfo(event.getPlayerId());
	}
	
	public void initAchieveInfo(String playerId) {
		Optional<SoldierExchangeActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SoldierExchangeActivityEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		List<AchieveItem> itemList = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<SoldierExchangeAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SoldierExchangeAchieveCfg.class);
		while (configIterator.hasNext()) {
			SoldierExchangeAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
		}
		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getItemList()), true);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SoldierExchangeActivityEntity> queryList = HawkDBManager.getInstance().query("from SoldierExchangeActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0 ) {
			SoldierExchangeActivityEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SoldierExchangeActivityEntity entity = new SoldierExchangeActivityEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub

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
		AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(SoldierExchangeAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.SOLDIER_EXCHANGE_ACHIEVE;
	}

}
