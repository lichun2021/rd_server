package com.hawk.activity.type.impl.radiationWarTwo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.MonsterBossAttackEvent;
import com.hawk.activity.event.impl.RadiationWarTwoBossKillCountEvent;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.radiationWarTwo.cfg.RadiationWarTwoAchieveCfg;
import com.hawk.activity.type.impl.radiationWarTwo.cfg.RadiationWarTwoActivityKVCfg;
import com.hawk.activity.type.impl.radiationWarTwo.entity.RadiationWarTwoEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.RadiationWarPageInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.log.Action;

/**
 * @Desc:185 新版辐射战争2(按日期配置开启)
 * @author:Winder
 * @date:2020年5月11日
 */
public class RadiationWarTwoActivity extends ActivityBase implements AchieveProvider {

	//击杀数据
	private Map<String,Integer> killCount = new ConcurrentHashMap<>();
	private boolean initData;
	
	public RadiationWarTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.RADIATION_WAR_TWO_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_RADIATION_WAR_TWO_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RadiationWarTwoActivity activity = new RadiationWarTwoActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RadiationWarTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from RadiationWarTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RadiationWarTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RadiationWarTwoEntity entity = new RadiationWarTwoEntity(playerId, termId);
		return entity;
	}
	

	
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<RadiationWarTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		RadiationWarTwoEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getAchieveItems().isEmpty() &&
				!entity.getGuildItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		List<AchieveItem> updateList = new ArrayList<>();
		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		List<AchieveItem> guildItemList = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<RadiationWarTwoAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RadiationWarTwoAchieveCfg.class);
		while (configIterator.hasNext()) {
			RadiationWarTwoAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			if(next.getType() == 1){
				itemList.add(item);
			}
			if(next.getType() == 2){
				guildItemList.add(item);
			}
			
		}
		if(entity.getAchieveItems().isEmpty()){
			entity.resetItemList(itemList);
			updateList.addAll(entity.getItemList());
		}
		if(entity.getGuildItemList().isEmpty()){
			entity.reSetGuildItemList(guildItemList);
			updateList.addAll(entity.getGuildItemList());
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, updateList), true);
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
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<RadiationWarTwoEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		RadiationWarTwoEntity playerDataEntity = opPlayerDataEntity.get();
		if(playerDataEntity.getItemList().isEmpty() ||
				playerDataEntity.getGuildItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		List<AchieveItem> list = new ArrayList<>();
		list.addAll(playerDataEntity.getItemList());
		list.addAll(playerDataEntity.getGuildItemList());
		AchieveItems items = new AchieveItems(list, playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(RadiationWarTwoAchieveCfg.class, achieveId);
		return config;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		RadiationWarTwoAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(RadiationWarTwoAchieveCfg.class, achieveId);
		if(Objects.isNull(achieveCfg)){
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		if(achieveCfg.getType() == 2){
			//需要玩家有联盟才能领奖
			String guildId = this.getDataGeter().getGuildId(playerId);
			if(HawkOSOperator.isEmptyString(guildId)){
				return Result.fail(Status.Error.GUILD_PLAYER_HASNOT_GUILD_VALUE);
			}
			return Result.success();
		}
		return Result.success();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		String guildId = this.getDataGeter().getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		int index = Math.abs(guildId.hashCode()) % HawkTaskManager.getInstance().getExtraThreadNum();  
		HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
			@Override
			public Object run() {
				int killCount = getBossKillCountByPlayerId(playerId);
				ActivityManager.getInstance().postEvent(new RadiationWarTwoBossKillCountEvent(playerId,killCount));
				syncActivityDataInfo(playerId);
				return null;
			}
			
		},index);
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
		Optional<RadiationWarTwoEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		RadiationWarTwoEntity playerDataEntity = opPlayerDataEntity.get();
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<RadiationWarTwoAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(RadiationWarTwoAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			RadiationWarTwoAchieveCfg cfg = achieveIterator.next();
			if(cfg.getType() == 1){
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				items.add(item);
			}
		}
		//重置成就
		playerDataEntity.resetItemList(items);
		//重置击杀叛军次数
		playerDataEntity.setKillNum(0);
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, playerDataEntity.getItemList());
		//同步
		syncActivityDataInfo(playerId);
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<RadiationWarTwoEntity> opRadiationWarEntity = getPlayerDataEntity(playerId);
		if (!opRadiationWarEntity.isPresent()) {
			return;
		}
		RadiationWarTwoEntity radiationWarTwoEntity = opRadiationWarEntity.get();
		int killNum = radiationWarTwoEntity.getKillNum();
		int guildKillNum = this.getBossKillCountByPlayerId(playerId);
		RadiationWarPageInfo.Builder builder = RadiationWarPageInfo.newBuilder();
		builder.setKillNum(killNum);
		builder.setGuildKillNum(guildKillNum);
		pushToPlayer(playerId, HP.code.RADITION_WAR_TWO_PAGE_INFO_RESP_VALUE, builder);
	}
	
	
	@Subscribe
	public void onMonsterKillEvent(MonsterAttackEvent event) {
		String playerId = event.getPlayerId();
		Optional<RadiationWarTwoEntity> opRadiationWarEntity = getPlayerDataEntity(playerId);
		if (!opRadiationWarEntity.isPresent()) {
			return;
		}
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isKill()) {
			return;
		}
		
		//幽灵叛军类型野怪
		if (event.getMosterType() == MonsterType.TYPE_2_VALUE 
				&& this.getDataGeter().activity185Monster(event.getMonsterId())) {
			
			RadiationWarTwoEntity raEntity = opRadiationWarEntity.get();
			raEntity.setKillNum(raEntity.getKillNum() + event.getAtkTimes());
			raEntity.notifyUpdate();
			//同步信息
			syncActivityDataInfo(playerId);
		}		
	}
	
	
	/**
	 * BOSS击杀
	 * @param event
	 */
	@Subscribe
	public void onMonsterBossKillEvent(MonsterBossAttackEvent event) {
		String playerId = event.getPlayerId();
		Optional<RadiationWarTwoEntity> opRadiationWarEntity = getPlayerDataEntity(playerId);
		if (!opRadiationWarEntity.isPresent()) {
			return;
		}
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isKill()) {
			return;
		}
		HawkLog.logPrintln("RadiationWarTwoActivity onMonsterBossKillEvent leaderId:{}, monsterId:{}", playerId, event.getMonsterId());
		if(event.getMosterType() == MonsterType.TYPE_3_VALUE){
			//击杀首领
			this.onKillBossMonster(event);
		}
		
	}
	
	
	@Subscribe
	public void onPlayerJoinGuild(JoinGuildEvent event){
		String playerId = event.getPlayerId();
		Optional<RadiationWarTwoEntity> opRadiationWarEntity = getPlayerDataEntity(playerId);
		if (!opRadiationWarEntity.isPresent()) {
			return;
		}
		if (!isOpening(playerId)) {
			return;
		}
		String guildId = this.getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		int killCount = this.getBossKillCount(guildId);
		ActivityManager.getInstance().postEvent(new RadiationWarTwoBossKillCountEvent(playerId,killCount),true);
		syncActivityDataInfo(playerId);
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
	public void onShow() {
		this.killCount.clear();
		super.onShow();
	}
	
	
	
	@Override
	public void onTick() {
		if(this.initData){
			return;
		}
		this.initKillData();
	}
	
	//初始化数据
	private void initKillData(){
		this.initData = true;
		this.killCount.clear();
		if(this.getActivityTermId() <= 0){
			return;
		}
		Map<String,Integer> dataMap = this.getRedisAllKillCount();
		this.killCount.putAll(dataMap);
	}
	
	
	
	private void onKillBossMonster(MonsterBossAttackEvent event){
		if(!event.isKill()){
			return;
		}
		String playerId = event.getPlayerId();
		String guildId = this.getDataGeter().getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		boolean addCount = this.killBossAddCountVerify(event.getMonsterId());
		if(!addCount){
			return;
		}
		
		int index = Math.abs(guildId.hashCode()) % HawkTaskManager.getInstance().getExtraThreadNum();  
		HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
			@Override
			public Object run() {
				int killCount = getBossKillCount(guildId);
				int aftKillCount = killCount + 1;
				//redis 记录击杀增加
				addGuildKillCount(guildId, 1);
				//更新到缓存
				updateBossKillCount(guildId, aftKillCount);
				//推动下成就
				Collection<String> playerIds = getDataGeter().getGuildMemberIds(guildId);
				for(String pid : playerIds){
					if(getDataGeter().isOnlinePlayer(pid)){
						ActivityManager.getInstance().postEvent(new RadiationWarTwoBossKillCountEvent(pid,aftKillCount));
						syncActivityDataInfo(pid);
					}
				}
				HawkLog.logPrintln("RadiationWarTwoActivity onMonsterBossKillEvent leaderId:{}, guildId:{},monsterId:{},bef:{},aft:{}",
						playerId,guildId,event.getMonsterId(),killCount,aftKillCount);
				return null;
			}
			
		},index);
	}
	
	
	/**
	 * 验证一下 是否是这个活动需要积累击杀次数的BOSS怪
	 * @param monsterId
	 * @return
	 */
	private boolean killBossAddCountVerify(int monsterId){
		ConfigIterator<RadiationWarTwoAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(RadiationWarTwoAchieveCfg.class);
		while (configIterator.hasNext()) {
			RadiationWarTwoAchieveCfg next = configIterator.next();
			if(next.getAchieveType() == AchieveType.RADIATIO_NWAR_TWO_BOSS_KILL_COUNT
					&& next.getConditionValues().contains(monsterId)){
				return true;
			}
		}
		return false;
	}
	
	
	private int getBossKillCountByPlayerId(String playerId){
		String guildId = this.getDataGeter().getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return 0;
		}
		return this.getBossKillCount(guildId);
	}
	
	private int getBossKillCount(String guildId){
		return this.killCount.getOrDefault(guildId, 0);
	}
	
	
	private void updateBossKillCount(String guildId,int killCount){
		this.killCount.put(guildId, killCount);
	}
	

	
	private Map<String,Integer> getRedisAllKillCount(){
		Map<String,Integer> killMap = new HashMap<String,Integer>();
		String key = this.getGuildKillCountKey();
		Map<String,String> rltMap = ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(key, (int)TimeUnit.DAYS.toSeconds(30));
		for(Map.Entry<String, String> entry : rltMap.entrySet()){
			String guildId = entry.getKey();
			String countStr = entry.getValue();
			if(HawkOSOperator.isEmptyString(countStr)){
				continue;
			}
			int count = Integer.valueOf(countStr);
			killMap.put(guildId, count);
		}
		return killMap;
	}
	
	
	/**
	 * 添加击杀数量
	 * @param guildId
	 * @return
	 */
	private void addGuildKillCount(String guildId,int count){
		String key = this.getGuildKillCountKey();
		ActivityGlobalRedis.getInstance().getRedisSession().hIncrBy(key, guildId, count, (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	/**
	 * 联盟击杀key
	 * @return
	 */
	private String getGuildKillCountKey(){
		int termId = this.getActivityTermId();
		return ActivityRedisKey.RADIATION_WAR_TWO_KILL_COUNT+":"+termId;
	}
	
	
	/**
	 * 是否可以使用道具
	 * @param playerId
	 * @param itemId
	 * @return
	 */
	public static boolean useBossItemLimit(String playerId,int itemId){
		RadiationWarTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RadiationWarTwoActivityKVCfg.class);
		List<Integer> nlist = cfg.getNewBossItemList();
		List<Integer> olist = cfg.getOldBossItemList();
		//不属于此活动的道具，不限制
		if(!nlist.contains(itemId) && !olist.contains(itemId)){
			return false;
		}
		//活动不存在，限制使用
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.RADIATION_WAR_TWO_VALUE);
		if (!opActivity.isPresent()) {
			return true;
		}
		//活动不在开放时间，限制使用
		RadiationWarTwoActivity activity = (RadiationWarTwoActivity) opActivity.get();
		boolean open = activity.isOpening(playerId);
		if(!open){
			return true;
		}
		return false;
	}
	
	
}
