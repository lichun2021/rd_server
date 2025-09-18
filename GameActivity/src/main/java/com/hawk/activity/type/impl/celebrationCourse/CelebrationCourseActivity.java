package com.hawk.activity.type.impl.celebrationCourse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.CelebrationCourseSignEvent;
import com.hawk.activity.event.impl.ShareEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.celebrationCourse.cfg.CelebrationCourseAchieveCfg;
import com.hawk.activity.type.impl.celebrationCourse.cfg.CelebrationCourseActivityKVCfg;
import com.hawk.activity.type.impl.celebrationCourse.cfg.CelebrationCoursePictureCfg;
import com.hawk.activity.type.impl.celebrationCourse.entity.CelebrationCourseEntity;
import com.hawk.game.protocol.Activity.CelebrationCourseMainRes;
import com.hawk.game.protocol.Activity.CelebrationCourseX8Res;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.DailyShareType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

public class CelebrationCourseActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public CelebrationCourseActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.CELEBRATION_COURSE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.CELEBRATION_COURSE_ACHIEVE_REWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CelebrationCourseActivity activity = new CelebrationCourseActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CelebrationCourseEntity> queryList = HawkDBManager.getInstance()
				.query("from CelebrationCourseEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CelebrationCourseEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CelebrationCourseEntity entity = new CelebrationCourseEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpenForPlayer(String playerId) {
		Optional<CelebrationCourseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		initAchieveInfo(playerId);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<CelebrationCourseEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		CelebrationCourseEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getAchieveList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<CelebrationCourseAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(CelebrationCourseAchieveCfg.class);
		while (configIterator.hasNext()) {
			CelebrationCourseAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.getAchieveList().add(item);
		}
		//无数据初始化
		if(entity.getShareIdsList().isEmpty()){
			initEntity(entity);
		}
		
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getAchieveList()), true);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void clearExtraAchieveInfo(String playerId) {
		Optional<CelebrationCourseEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		CelebrationCourseEntity entity = opEntity.get();
		List<AchieveItem> items = entity.getAchieveList();
		Map<Integer, AchieveItem> itemMap = new HashMap<>();
		for (AchieveItem item : items) {
			int achieveId = item.getAchieveId();
			if (itemMap.containsKey(achieveId)) {
				AchieveItem oldItem = itemMap.get(achieveId);
				if (item.getState() > oldItem.getState()) {
					itemMap.put(achieveId, item);
				}
			} else {
				itemMap.put(achieveId, item);
			}
		}
		List<AchieveItem> newList = new ArrayList<>(itemMap.values());
		entity.setAchieveList(newList);
		entity.notifyUpdate();
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<CelebrationCourseEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(opPlayerDataEntity.isPresent()){
			CelebrationCourseEntity entity = opPlayerDataEntity.get();	
			checkOpenShare(entity);
			initAchieveInfo(playerId);
			
			CelebrationCourseMainRes.Builder builder = CelebrationCourseMainRes.newBuilder();
			builder.addAllIds(entity.getShareIdsList());
			builder.addAllIdsReward(entity.getShareRewardList());
			
			builder.setSignNumber(entity.getSignNumber());
			if(HawkTime.getMillisecond() > entity.getSignTime() ){
				builder.setSignState(1);
			}else{
				builder.setSignState(2);
			}
			PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.CELEBRATION_COURSE_MAIN_RES, builder));
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
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<CelebrationCourseEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		CelebrationCourseEntity playerDataEntity = opPlayerDataEntity.get();
		ConfigIterator<CelebrationCourseAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(CelebrationCourseAchieveCfg.class);
		int achieveSize = configIterator.size();
		if (playerDataEntity.getAchieveList().isEmpty()) {
			initAchieveInfo(playerId);
		} else if (playerDataEntity.getAchieveList().size() > achieveSize) {
			clearExtraAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getAchieveList(), playerDataEntity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(CelebrationCourseAchieveCfg.class, achieveId);
		return config;
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
		if (isOpening(playerId)) {
			Optional<CelebrationCourseEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			//初始化成就数据
			if (opDataEntity.get().getAchieveList().isEmpty()) {
				initAchieveInfo(playerId);
			}
		}
	}
	
	public void initEntity(CelebrationCourseEntity entity){
		ConfigIterator<CelebrationCoursePictureCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(CelebrationCoursePictureCfg.class);
		List<Integer> shareList = new CopyOnWriteArrayList<>();
		while(configIterator.hasNext()){
			CelebrationCoursePictureCfg cfg = configIterator.next();
			if(cfg.getSharePicUnlockTime() == 0){
				shareList.add(cfg.getId());
			}
		}
		
		entity.setShareIdsList(shareList);
		entity.setShareRewardList(new CopyOnWriteArrayList<>());
		entity.notifyUpdate();
	}
	
	public void checkOpenShare(CelebrationCourseEntity entity){
		//活动开启时间
		long activityStartTime = getTimeControl().getStartTimeByTermId(getActivityTermId());
		long curTime = HawkTime.getMillisecond();
		List<Integer> shareIds = entity.getShareIdsList();
		boolean changeTag = false;
		ConfigIterator<CelebrationCoursePictureCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(CelebrationCoursePictureCfg.class);
		while(configIterator.hasNext()){
			CelebrationCoursePictureCfg cfg = configIterator.next();
			if(cfg.getSharePicUnlockTime() == 0){
				continue;
			}
			
			//当前时间，超出活动开始时间+间隔时间 开启
			if(curTime >= activityStartTime +( cfg.getSharePicUnlockTime()*1000 ) ){
				if(shareIds.contains(cfg.getId())){
					logger.error("checkOpenShare failed, id exist, playerId: "+ "{},id:{}", entity.getPlayerId(),cfg.getId());
					continue;
				}else{
					shareIds.add(cfg.getId());
					changeTag = true;
				}
			}
		}
		
		if(changeTag){
			entity.notifyUpdate();
		}
	}
	
	public void sign(String playerId){
		Optional<CelebrationCourseEntity> opEntity = getPlayerDataEntity(playerId);
		CelebrationCourseEntity entity = opEntity.get();
		
		if(entity.getSignTime()!=0){
			if(HawkTime.getMillisecond() < entity.getSignTime()){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.CELEBRATION_COURSE_SIGN_REQ_VALUE,Status.Error.CELEBRATION_COURSE_SIGN_YET_ERROR_VALUE);
				return;
			}
		}
		
		entity.addSignNumber();
		entity.setSignTime(HawkTime.getNextAM0Date());
		entity.notifyUpdate();
		
		syncActivityDataInfo(playerId);
		
		ActivityManager.getInstance().postEvent(new CelebrationCourseSignEvent(playerId, 1));
		responseSuccess(playerId, HP.code.CELEBRATION_COURSE_SGIN_RES_VALUE);
	}
	
	public void shareReward(String playerId){
//		ConfigIterator<CelebrationCoursePictureCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(CelebrationCoursePictureCfg.class);
//		//验证id是否存在
//		CelebrationCoursePictureCfg curCfg = null;
//		while(configIterator.hasNext()){
//			CelebrationCoursePictureCfg cfg = configIterator.next();
//			if(cfg.getId() == id){
//				curCfg = cfg;
//				break;
//			}
//		}
//		if(curCfg == null){
//			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.CELEBRATION_COURSE_SHARE_REQ_VALUE,Status.Error.CELEBRATION_COURSE_NOT_SHARE_ID_VALUE);
//			return;
//		}
		
		Optional<CelebrationCourseEntity> opEntity = getPlayerDataEntity(playerId);
		CelebrationCourseEntity entity = opEntity.get();
		
//		if(!entity.getShareIdsList().contains(id)){
//			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.CELEBRATION_COURSE_SHARE_REQ_VALUE,Status.Error.CELEBRATION_COURSE_ID_NOT_OPEN_VALUE);
//			return;
//		}
		boolean saveTag = false;
		//每日可分享多次。只能获取一次奖励
		if(HawkTime.getMillisecond() > entity.getShareTime()){
			CelebrationCourseActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(CelebrationCourseActivityKVCfg.class);
			List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemList(kvConfig.getShareReward());
			this.getDataGeter().takeReward(playerId,rewardItems, 1, Action.CELEBRATION_COURSE_ACHIEVE_SHARE_REWARD, true);
			entity.setShareTime(HawkTime.getNextAM0Date());
			saveTag=true;
		}
		
//		if(!entity.getShareRewardList().contains(id)){
//			entity.getShareRewardList().add(id);
//			saveTag=true;
//		}
		
		if(saveTag)
			entity.notifyUpdate();

		
		responseSuccess(playerId, HP.code.CELEBRATION_COURSE_SHARE_RES_VALUE);
	}
	private final String X8_URL = "http://apps.datamore.tencent-cloud.net/api/pkg_rules/result?rbk_id=5265&secret=cb1f4a1881a7cdf1cacb7f9ac239a214&platid=%d&izoneareaid=%s&vopenid=%s";
	public void shareX8Data(String playerId){
//		String jsonData = "{\"error_code\":0,\"error_message\":\"\",\"result\":{\"data\":{\"dregdate\":\"20200720\",\"dtstatdate\":\"20200927\",\"i5starheronum\":\"2\",\"iarmyname\":\"9级主站坦克\",\"iguildscore\":\"85487\",\"iheroname\":\"1017\",\"iheronum\":\"20\",\"ijijia\":\"967414\",\"ijijiafinaltimes\":\"49\",\"ijijiarate\":\"65.1%\",\"ijijiatimes\":\"91\",\"imechaidinfo\":\"[鬼武者,泰坦风暴,金刚,毁灭者]\",\"imechaname\":\"毁灭者\",\"imechapower\":\"19600\",\"imoney\":\"62\",\"inumhj\":\"351462137\",\"inumks\":\"3473780506\",\"inumsy\":\"2295768818\",\"inumxt\":\"83918750\",\"ipersonscore\":\"72\",\"ipersontalktimes\":\"4\",\"ipve\":\"15296506\",\"ipverate\":\"97.7%\",\"ipvetims\":\"3183\",\"ipvp\":\"13263255\",\"ipvpdefwoundedcnt\":\"7128\",\"ipvpdteventtime\":\"2020-07-31 21:45:00\",\"ipvpkilltimes\":\"1474342\",\"ipvprate\":\"67.4%\",\"ipvptimes\":\"26\",\"ipvpwinorlose\":\"0\",\"ipvpwinrate\":\"11.5%\",\"irank1\":\"\",\"irank2\":\"\",\"irank3\":\"\",\"irank4\":\"\",\"irank5\":\"\",\"irank6\":\"\",\"irank7\":\"\",\"irank8\":\"\",\"iregdaynum\":\"70\",\"italktimes\":\"84\",\"itermnum\":\"2\",\"iterwintimes\":\"1\",\"itotalchangecount\":\"904220\",\"itypename\":\"坦克\",\"izhanqudaynum\":\"70\",\"izhanqutimes\":\"0\",\"playerfriendsnum\":\"5\",\"receiverolename\":\"%E7%81%AC%E9%A2%9C%E7%8E%8B\",\"receiverroleid\":\"oRRojxFgSW7xUscS3LXVyusmsvJ4\",\"rk_jijia\":\"336734\",\"rk_pve\":\"349631\",\"rk_pvp\":\"4311699\",\"vroleid\":\"83g-1eqb83-1u\"},\"fixed\":{\"izoneareaid\":\"10492\",\"platid\":\"0\",\"rbk_id\":\"5265\",\"secret\":\"cb1f4a1881a7cdf1cacb7f9ac239a214\",\"vopenid\":\"oRRojxF5yGbLSA7Nt_SFOt5g-XjI\"},\"realtime\":{},\"rules\":{}}}";
//		JSONObject jsonObject = JSONObject.parseObject(jsonData);
//		if(jsonObject.containsKey("result")){
//			JSONObject resultObject = jsonObject.getJSONObject("result");
//					
//			if(resultObject.containsKey("data")){
//				JSONObject dataObject = resultObject.getJSONObject("data");
//				if(dataObject.containsKey("receiverolename")){
//					String receiverolename = dataObject.getString("receiverolename");
//					try {
//						receiverolename = URLDecoder.decode(receiverolename, "utf-8");
//					} catch (Exception e) {
//						HawkException.catchException(e);
//					}
//					dataObject.put("receiverolename", receiverolename);
//				}
//			}
//		}
//		jsonData = jsonObject.toJSONString();
//logger.info(">>>>>>>>>>>>>>>>>>>>>>  JSON="+jsonData);		
//		CelebrationCourseX8Res.Builder builder = CelebrationCourseX8Res.newBuilder();
//		builder.setJsonData(jsonData);
//		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.CELEBRATION_COURSE_X8_RES, builder));
		
		String openId = getDataGeter().getOpenId(playerId);
		String zoineId = getDataGeter().getServerId();
		int platId = getDataGeter().getPlatId(playerId);
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				HawkLog.logPrintln("player shareX8Data back platId:{}, zoineId:{}, openId{}",platId, zoineId, openId);
				String url = String.format(X8_URL, platId,zoineId,openId);				
				String content = CelebrationCourseActivity.this.getDataGeter().doHttpRequest(url, 3000);
   logger.info(url+"  ============== x8 content="+content);
				if(content!=null){
					CelebrationCourseX8Res.Builder builder = CelebrationCourseX8Res.newBuilder();
					builder.setJsonData(content);
					PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.CELEBRATION_COURSE_X8_RES, builder));
				}else{
					HawkLog.logPrintln("player shareX8Data error url:{}",url);
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.CELEBRATION_COURSE_X8_REQ_VALUE,Status.Error.CELEBRATION_COURSE_SHARE_DATA_ERROR_VALUE);
				}
				return null;
			}
		});
		
	}
	
	@Subscribe
	public void onEvent(ShareEvent event){
		if (event.getShareType() != DailyShareType.SHARE_CELEBRATION_COURSE) {
			return;
		}
		shareReward(event.getPlayerId());
		
	}
	
}
