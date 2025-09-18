package com.hawk.activity.type.impl.supergold;

import java.util.*;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.type.ActivityState;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.PlayerAchieve;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.supergold.cfg.SuperGoldCfg;
import com.hawk.activity.type.impl.supergold.cfg.SuperGoldConsumeCfg;
import com.hawk.activity.type.impl.supergold.cfg.SuperGoldKVCfg;
import com.hawk.activity.type.impl.supergold.entity.SuperGoldEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.SuperGoldInfo;
import com.hawk.game.protocol.Activity.SuperGoldResult;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/***
 * 超级金矿活动
 * @author yang.rao
 *
 */
public class SuperGoldActivity extends ActivityBase{
	
	static final Logger logger = LoggerFactory.getLogger("Server");

	public SuperGoldActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SUPER_GOLD_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<SuperGoldEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			initAchieveInfo(playerId);
			Result<SuperGoldInfo.Builder> result = this.reqActivityInfo(playerId);
			if(!result.isFail()){
				PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.SUPER_GOLD_INFO_VALUE, this.reqActivityInfo(playerId).getRetObj()));
			}
		}
	}
	
	
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ON_SUPER_GOLD_ACTIVITY_OPEN, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}

	/***
	 * 玩家挖矿
	 * @param playerId
	 * @return
	 */
	public Result<?> onPlayerDigGold(String playerId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if (isActivityClose(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		Optional<SuperGoldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		SuperGoldEntity entity = opEntity.get();
		int nextAchieveId = 0; //下一个挖矿的id
		List<AchieveItem> list = entity.getItemList();
		for(AchieveItem item : list){
			if(item.getState() == AchieveState.NOT_ACHIEVE_VALUE){
				nextAchieveId = item.getAchieveId();
				break;
			}
		}
		if(nextAchieveId == 0){
			return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
		}
		SuperGoldConsumeCfg consumeCfg = HawkConfigManager.getInstance().getConfigByKey(SuperGoldConsumeCfg.class, nextAchieveId);
		if(consumeCfg == null){
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		float rate = getRate();
		// 扣金条
		boolean flag = this.getDataGeter().consumeItems(playerId, consumeCfg.getNeedItemList(),protoType, Action.SUPER_GOLD_REWARD);

		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		// takeAward的最后一个参数是由前端同学（魏超）指定的
		List<RewardItem.Builder> reward = getRewardList(consumeCfg.getReward(), rate);
		if(reward == null){
			return null;
		}
		this.getDataGeter().takeReward(playerId, reward, 1, Action.SUPER_GOLD_REWARD, false, RewardOrginType.ACTIVITY_REWARD);
		
		//偷偷的把额外奖励放背包
		SuperGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SuperGoldKVCfg.class);
		if(kvCfg != null){
			String exReward = kvCfg.getExtReward();
			List<RewardItem.Builder> exRewardBuilder = getRewardList(exReward);
			this.getDataGeter().takeReward(playerId, exRewardBuilder, 1, Action.SUPER_GOLD_REWARD, false, RewardOrginType.ACTIVITY_REWARD);
		}
		//持久化
		for(AchieveItem item : list){
			if(item.getAchieveId() == nextAchieveId){
				item.setState(AchieveState.TOOK_VALUE);
			}
		}
		entity.notifyUpdate();
		//检测关闭
		checkActivityClose(playerId);
		return Result.success(buildResultProto(reward, entity));
	}
	
	private SuperGoldResult.Builder buildResultProto(List<RewardItem.Builder> reward, SuperGoldEntity entity){
		SuperGoldResult.Builder builder = SuperGoldResult.newBuilder();
		for(RewardItem.Builder build : reward){
			builder.addResult(build);
		}
		SuperGoldInfo.Builder info = SuperGoldInfo.newBuilder();
		float maxRate = getMaxRate();
		int achieveId = 0; //下一个挖矿的id
		List<AchieveItem> list = entity.getItemList();
		for(AchieveItem item : list){
			if(item.getState() == AchieveState.NOT_ACHIEVE_VALUE){
				achieveId = item.getAchieveId();
				break;
			}
		}
		info.setMaxRate(maxRate);
		info.setAchieveId(achieveId);
		builder.setInfo(info);
		return builder;
	}

	private SuperGoldResult.Builder buildResultProto(SuperGoldEntity entity){
		return buildResultProto(new ArrayList<Reward.RewardItem.Builder>(), entity);
	}


	public Result<SuperGoldInfo.Builder> reqActivityInfo(String playerId){
		if (isActivityClose(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<SuperGoldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		SuperGoldEntity entity = opEntity.get();
		int nextAchieveId = 0; //下一个挖矿的id
		List<AchieveItem> list = entity.getItemList();
		for(AchieveItem item : list){
			if(item.getState() == AchieveState.NOT_ACHIEVE_VALUE){
				nextAchieveId = item.getAchieveId();
				break;
			}
		}
		float maxRate = getMaxRate();
		SuperGoldInfo.Builder build = SuperGoldInfo.newBuilder();
		build.setAchieveId(nextAchieveId);
		build.setMaxRate(maxRate);
		return Result.success(build);
	}
	
	private float getMaxRate(){
		ConfigIterator<SuperGoldCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(SuperGoldCfg.class);
		float maxRate = 0.0f;
		while(configItrator.hasNext()){
			SuperGoldCfg cfg = configItrator.next();
			String rateRange = cfg.getRateRange();
			String range[] = rateRange.split("_");
			for(int i = 0 ; i < range.length ; i ++){
				float f = Float.parseFloat(range[i]);
				if(f > maxRate){
					maxRate = f;
				}
			}
		}
		return maxRate;
	}
	
	/***
	 * 构建一个rewardList
	 * @param reward
	 * @param rate
	 * @return
	 */
	private List<RewardItem.Builder> getRewardList(String reward, float rate){
		StringBuilder sb = new StringBuilder();
		String src[] = reward.split("_");
		for(int i = 0 ; i < src.length - 1 ; i ++){
			sb.append(src[i])
			.append("_");
		}
		int count = (int)Math.floor(rate * Integer.parseInt(src[src.length - 1]));
		sb.append(count);
		return RewardHelper.toRewardItemList(sb.toString());
	}
	
	private List<RewardItem.Builder> getRewardList(String reward){
		return RewardHelper.toRewardItemList(reward);
	}
	
	/***
	 * 获取一个倍率
	 * @param ita
	 * @return
	 */
	private float getRate(){
		ConfigIterator<SuperGoldCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(SuperGoldCfg.class);
		Map<SuperGoldCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			SuperGoldCfg cfg = configItrator.next();
			map.put(cfg, cfg.getRate());
		}
		SuperGoldCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found SuperGoldCfg:" + map);
		}
		float[] range = chose.getFloatRange();
		try {
			int index = HawkRand.randInt(0,range.length - 1);
			float result = range[index];
			return result;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0.0f;
	}
	
	
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<SuperGoldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SuperGoldEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<SuperGoldConsumeCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SuperGoldConsumeCfg.class);
		while (configIterator.hasNext()) {
			SuperGoldConsumeCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SuperGoldActivity activity =  new SuperGoldActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SuperGoldEntity> queryList = HawkDBManager.getInstance()
				.query("from SuperGoldEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SuperGoldEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SuperGoldEntity entity = new SuperGoldEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	@Override
	public boolean isActivityClose(String playerId) {
		return checkGoldExchangeEnd(playerId);
	}
	
	/**检查金币是否换完
	 * @param playerId
	 */
	public boolean checkGoldExchangeEnd(String playerId){
		Optional<SuperGoldEntity> opEntity = getPlayerDataEntity(playerId);

		if (!opEntity.isPresent()) {
			return false;
		}
		// 判断用户是否跨天
		SuperGoldEntity entity = opEntity.get();
		List<AchieveItem> itemList = entity.getItemList();
		if (itemList == null || itemList.isEmpty()) {
			return false;
		}
		for (AchieveItem item : entity.getItemList()) {
			if (item.getState() != AchieveState.TOOK_VALUE) {
				return false;
			}
		}
		return true;
	}

	public String superGoldTestScript(int count){
		Map<Integer, Integer> map = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < count ; i++){
			int id = getRateTest();
			if(map.containsKey(id)){
				map.put(id, map.get(id) + 1);
			}else{
				map.put(id, 1);
			}
		}
		for(Integer key : map.keySet()){
			sb.append("id:" + key + ",出现的次数为:" + map.get(key) + ".<br>");
		}
		
		return sb.toString();
	}
	
	private int getRateTest(){
		ConfigIterator<SuperGoldCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(SuperGoldCfg.class);
		Map<SuperGoldCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			SuperGoldCfg cfg = configItrator.next();
			map.put(cfg, cfg.getRate());
		}
		SuperGoldCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found SuperGoldCfg:" + map);
		}
		return chose.getId();
	}

	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		ActivityState state = getIActivityEntity(playerId).getActivityState();
		if (isInvalid() || state != ActivityState.OPEN){
			return;
		}
		if (!event.isCrossDay()){
			return;
		}
		Optional<SuperGoldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()){
			return;
		}
		SuperGoldEntity entity = opEntity.get();

		List<AchieveItem> list = entity.getItemList();
		for(AchieveItem item : list){
			item.setState(AchieveState.NOT_ACHIEVE_VALUE);
		}	
		entity.notifyUpdate();
		// 同步活动数据
		syncActivityStateInfo(playerId);
	}
}
