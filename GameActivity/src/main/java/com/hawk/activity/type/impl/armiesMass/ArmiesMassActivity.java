package com.hawk.activity.type.impl.armiesMass;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ArmiesMassGiftBuyEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.ShareEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.armiesMass.cfg.ArmiesMassGiftCfg;
import com.hawk.activity.type.impl.armiesMass.cfg.ArmiesMassKVCfg;
import com.hawk.activity.type.impl.armiesMass.cfg.ArmiesMassSculptureAwardCfg;
import com.hawk.activity.type.impl.armiesMass.cfg.ArmiesMassSculptureCfg;
import com.hawk.activity.type.impl.armiesMass.entity.ArmiesMassEntity;
import com.hawk.game.protocol.Activity.ArmiesMassPageInfoResp;
import com.hawk.game.protocol.Activity.PBArmiesMassGiftType;
import com.hawk.game.protocol.Activity.PBSculptureQuality;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.DailyShareType;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 时空豪礼活动
 * @author che
 *
 */
public class ArmiesMassActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
		
	private int lastStage;

	public ArmiesMassActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ARMIES_MASS_ACTIVITY;
	}

	
	@Override
	public void onTick() {
		if(this.lastStage == 0){
			this.lastStage = this.getCurStage();
		}
		if(this.lastStage == 0){
			return;
		}
		//阶段切换检测
		this.onStageChange();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			this.checkStageReset(playerId);
		}
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		logger.info("onContinueLogin,event,playerId: {}", event.getPlayerId());
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		logger.info("onContinueLogin,playerId: {}", event.getPlayerId());
		ArmiesMassKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ArmiesMassKVCfg.class);
		String playerId = event.getPlayerId();
		boolean reset = this.checkStageReset(playerId);
		Optional<ArmiesMassEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		logger.info("onContinueLogin,playerId: {},stageRest:{}", event.getPlayerId(),reset);
		ArmiesMassEntity entity = opPlayerDataEntity.get();
		if(!reset && cfg.getTimes() > 0){
			//重置登录礼包记录
			entity.getFreeAwardList().clear();
			//重置分享记录
			entity.setShare(0);
			//重置雕像次数
			entity.setSculptureOpenCount(cfg.getOpenSculptureTimes());
			//重置雕像信息
			if(this.contineRestSculpture(entity)){
				this.initSculpture(entity);
				logger.info("onContinueLogin,initSculpture,playerId: {}", event.getPlayerId());
			}
			logger.info("onContinueLogin,playerId: {},dataRest:{}", event.getPlayerId());
		}
		syncActivityInfo(playerId, entity);
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ARMIES_MASS_INIT, () -> {
				checkStageReset(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<ArmiesMassEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}


	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ArmiesMassActivity activity = new ArmiesMassActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ArmiesMassEntity> queryList = HawkDBManager.getInstance()
				.query("from ArmiesMassEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ArmiesMassEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ArmiesMassEntity entity = new ArmiesMassEntity(playerId, termId);
		return entity;
	}
	
	
	/**
	 * 跨天是否重置
	 * @param entity
	 * @return
	 */
	public boolean contineRestSculpture(ArmiesMassEntity entity){
		int stage = entity.getStage();
		ArmiesMassSculptureCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmiesMassSculptureCfg.class, stage);
		if(cfg == null){
			return false;
		}
		ArmiesMassSculpture centerSculpture = entity.getSculptureByQulity(cfg.getCenter());
		if(centerSculpture == null){
			return false;
		}
		return true;
	}

	/**
	 * 打开雕像
	 * @param playerId
	 * @param position
	 */
	public void openSculpture(String playerId,int position){
		boolean reset = this.checkStageReset(playerId);
		if(reset){
			logger.info("openSculpture,stageRest,playerId: {},position:{}", playerId,position);
			this.syncActivityDataInfo(playerId);
			return;
		}
		Optional<ArmiesMassEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ArmiesMassEntity entity = opEntity.get();
		ArmiesMassSculpture sculpture = entity.getSculptureByPosition(position);
		if(sculpture == null){
			logger.info("openSculpture,sculpture,null,playerId: {},position:{}", playerId,position);
			return;
		}
		if(sculpture.getState() == 1){
			logger.info("openSculpture,opened,playerId: {},position:{}", playerId,position);
			return;
		}
		int openCount = entity.getSculptureOpenCount();
		if(openCount <= 0){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,HP.code.ARMIES_MASS_OPEN_SCULPTURE_REQ_VALUE,
					Status.Error.ARMIES_MASS_OPEN_TIME_LIMIT_VALUE);
			logger.info("openSculpture,openCount,less,playerId: {},position:{}", playerId,position);
			return;
		}
		int stage = entity.getStage();
		ArmiesMassSculptureCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmiesMassSculptureCfg.class, stage);
		ArmiesMassSculpture centerSculpture = entity.getSculptureByQulity(cfg.getCenter());
		int quality = 0;
		//如果没有翻到品质最高的雕像
		if(centerSculpture == null){
			//如果最后一个雕像
			if(entity.getSculptureCloseSize() <= 1){
				quality = cfg.getCenter();
			}else{
				//不是则随机
				Integer randomQuality = HawkRand.randomWeightObject(cfg.getSculptureWeights());
				if(randomQuality != null){
					quality = randomQuality.intValue();
				}
			}
		}else{
			//如果已经翻到品质最高的雕像,不能再一次翻到此品质雕像
			Map<Integer,Integer> weights = new HashMap<Integer,Integer>();
			weights.putAll(cfg.getSculptureWeights());
			weights.remove(cfg.getCenter());
			Integer randomQuality = HawkRand.randomWeightObject(weights);
			if(randomQuality != null){
				quality = randomQuality.intValue();
			}
		}
		if(quality == 0){
			logger.info("openSculpture,quality,zero,playerId: {},position:{}", playerId,position);
			return;
		}
		ArmiesMassSculptureAwardCfg awardCfg = this.getSculptureReward(quality);
		List<RewardItem.Builder>  rewardList = RewardHelper.toRewardItemList(awardCfg.getReward());
		this.getDataGeter().takeReward(playerId, rewardList, Action.ARMIES_MASS_OPEN_SCULPTURE_REWARD, true);
		sculpture.setQuality(quality);
		sculpture.setState(1);
		entity.setSculptureOpenCount(openCount - 1);
		entity.notifyUpdate();
		this.syncActivityInfo(playerId, entity);
		//日志
		int termId = this.getActivityTermId();
		this.getDataGeter().logArmiesMassOpenSculpture(playerId, termId, entity.getStage(), sculpture.getQuality());
		logger.info("openSculpture,quality,sucess,playerId: {},position:{},quality:{},center:{}", playerId,position,quality,cfg.getCenter());
		
	}
	
	
	public ArmiesMassSculptureAwardCfg getSculptureReward(int qulity){
		List<ArmiesMassSculptureAwardCfg> list = HawkConfigManager.getInstance().
				getConfigIterator(ArmiesMassSculptureAwardCfg.class).toList();
		Map<ArmiesMassSculptureAwardCfg,Integer> weights = new HashMap<>();
		for(ArmiesMassSculptureAwardCfg cfg : list){
			if(cfg.getPool() == qulity){
				weights.put(cfg, cfg.getWeight());
			}
		}
		ArmiesMassSculptureAwardCfg cfg = HawkRand.randomWeightObject(weights);
		return cfg;
	}

	
	/**
	 * 场景分享，添加次数
	 */
	@Subscribe
	public void onshare(ShareEvent event) {
		DailyShareType stype = event.getShareType();
		String playerId = event.getPlayerId();
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}
		logger.info("onshare,playerId: {},event:{}", 
				playerId,stype.getNumber());
		if(stype != DailyShareType.SHARE_ARMIES_MASS){
			return;
		}
		boolean reset = this.checkStageReset(playerId);
		if(reset){
			logger.info("onshare,stageRest,playerId: {}", playerId);
			this.syncActivityDataInfo(playerId);
			return;
		}
		Optional<ArmiesMassEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ArmiesMassEntity entity = opEntity.get();
		if(entity.getShare() > 0){
			return;
		}
		ArmiesMassKVCfg kvCfg = HawkConfigManager.getInstance().
				getKVInstance(ArmiesMassKVCfg.class);
		int openCount = entity.getSculptureOpenCount();
		entity.setShare(1);
		entity.setSculptureOpenCount(openCount + kvCfg.getShareGetTimes());
		entity.notifyUpdate();
		this.syncActivityInfo(playerId, entity);
	}
	
	
	/**
	 * 购买礼包
	 * @param playerId
	 * @param pId
	 */
	public void buyGiftPackage(String playerId,int pId,int protocolType){
		ArmiesMassGiftCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(ArmiesMassGiftCfg.class, pId);
		if(cfg == null){
			return;
		}
		if(cfg.getType() == PBArmiesMassGiftType.FREE_VALUE){
			//如果是登录礼包
			this.achieveFreeGift(playerId, cfg,protocolType);
		}else if(cfg.getType() == PBArmiesMassGiftType.DIAMOND_VALUE){
			//如果是金条礼包
			this.buyDiamondGift(playerId, cfg,protocolType);
		}
	}
	
	/**
	 * 金条礼包
	 * @param playerId
	 * @param cfg
	 */
	public void buyDiamondGift(String playerId,ArmiesMassGiftCfg cfg,int protocolType){
		boolean reset = this.checkStageReset(playerId);
		if(reset){
			logger.info("buyDiamondGift,stageRest,playerId: {},id:{}", playerId,cfg.getId());
			this.syncActivityDataInfo(playerId);
			return;
		}
		Optional<ArmiesMassEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ArmiesMassEntity entity = opEntity.get();
		if(entity.getStage() != cfg.getStageId()){
			return;
		}
		int level = entity.getBuyGiftLevel(cfg.getType(),cfg.getGroupId());
		if(cfg.getLevel() < (level +1)){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.ARMIES_MASS_GIFT_BUY_LIMIT_VALUE);
			return;
		}
		if(cfg.getLevel() != (level +1)){
			return;
		}
		List<RewardItem.Builder> costItem = RewardHelper.toRewardItemList(cfg.getPrice());
		boolean cost = this.getDataGeter().cost(playerId,costItem, 1, Action.ARMIES_MASS_BUY_GIFT_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		entity.buyGiftLevelUp(cfg.getType(),cfg.getGroupId(), cfg.getLevel());
		//发奖
		List<RewardItem.Builder>  rewardList = RewardHelper.toRewardItemList(cfg.getRewards());
		this.getDataGeter().takeReward(playerId, rewardList, Action.ARMIES_MASS_BUY_GIFT_ACHIVE, true);
		//联盟礼包
		if(cfg.getAllianceGift()>0){
			this.getDataGeter().sendAllianceGift(playerId, cfg.getAllianceGift());
		}
		//邮件
		sendGiftBuyRewardByMail(playerId, cfg.getName(), cfg.getRewards(), rewardList);
		//同步
		this.syncActivityInfo(playerId, entity);
	}
	
	
	/**
	 * 免费礼包
	 * @param playerId
	 * @param cfg
	 */
	public void achieveFreeGift(String playerId,ArmiesMassGiftCfg cfg,int protocolType){
		boolean reset = this.checkStageReset(playerId);
		if(reset){
			this.syncActivityDataInfo(playerId);
			logger.info("achieveFreeGift,stageRest,playerId: {},id:{}", playerId,cfg.getId());
			return;
		}
		Optional<ArmiesMassEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ArmiesMassEntity entity = opEntity.get();
		if((entity.getFreeGiftLevel(cfg.getType(),cfg.getGroupId())+1) != cfg.getLevel()){
			return;
		}
		entity.freeGiftLevelUp(cfg.getType(),cfg.getGroupId(), cfg.getLevel());
		//发奖
		List<RewardItem.Builder>  rewardList = RewardHelper.toRewardItemList(cfg.getRewards());
		this.getDataGeter().takeReward(playerId, rewardList, Action.ARMIES_MASS_BUY_GIFT_ACHIVE, true);
		//联盟礼包
		if(cfg.getAllianceGift()>0){
			this.getDataGeter().sendAllianceGift(playerId, cfg.getAllianceGift());
		}
		//同步
		this.syncActivityInfo(playerId, entity);
	}
		
	/**
	 * 直购礼包是否可以被购买
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public boolean canPayforGift(String playerId,String payforId){
		int gid = ArmiesMassGiftCfg.getGiftId(payforId);
		ArmiesMassGiftCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(ArmiesMassGiftCfg.class, gid);
		if(cfg == null){
			return false;
		}
		if(cfg.getType() != PBArmiesMassGiftType.RBUY_VALUE){
			return false;
		}
		boolean reset = this.checkStageReset(playerId);
		if(reset){
			logger.info("canPayforGift,stageRest,playerId: {},id:{}", playerId,cfg.getId());
			this.syncActivityDataInfo(playerId);
			return false;
		}
		Optional<ArmiesMassEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		ArmiesMassEntity entity = opEntity.get();
		if(cfg.getStageId() != entity.getStage()){
			
			return false;
		}
		int level = entity.getBuyGiftLevel(cfg.getType(),cfg.getGroupId());
		if(cfg.getLevel() != (level +1)){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 直购礼包支付完成
	 * @param event
	 */
	@Subscribe
	public void onRechargeGiftBuy(ArmiesMassGiftBuyEvent event){
		String playerId = event.getPlayerId();
		String payforId = event.getGiftId();
		logger.info("onRechargeGiftBuy,buyEvent,playerId: {},payforId:{}", playerId,payforId);
		int gid = ArmiesMassGiftCfg.getGiftId(payforId);
		ArmiesMassGiftCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(ArmiesMassGiftCfg.class, gid);
		if(cfg == null){
			return;
		}
		this.checkStageReset(playerId);
		Optional<ArmiesMassEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ArmiesMassEntity entity = opEntity.get();
		if(cfg.getStageId() == entity.getStage()){
			int type = cfg.getType();
			int group = cfg.getGroupId();
			int level = cfg.getLevel();
			entity.buyGiftLevelUp(type,group, level);
			logger.info("onRechargeGiftBuy,buyGiftLevelUp,playerId:{},type:{},group:{},level:{}", 
					playerId,type,group,level);
		}
		//发奖
		List<RewardItem.Builder>  rewardList = RewardHelper.toRewardItemList(cfg.getRewards());
		this.getDataGeter().takeReward(playerId, rewardList, Action.ARMIES_MASS_BUY_GIFT_ACHIVE, true);
		//联盟礼包
		if(cfg.getAllianceGift()>0){
			this.getDataGeter().sendAllianceGift(playerId, cfg.getAllianceGift());
		}
		sendGiftBuyRewardByMail(playerId, cfg.getName(), cfg.getRewards(), rewardList);
		this.syncActivityInfo(playerId, entity);
		
	}
	
	
	/** 
	 * 发送奖励mail
	 */
	public void sendGiftBuyRewardByMail(String playerId, String giftName,String cfgReward,List<RewardItem.Builder> rewardList){
		try {
			// 邮件发送奖励
			Object[] content = new Object[]{giftName,cfgReward};
			Object[] title = new Object[]{};
			Object[] subTitle = new Object[]{giftName};
			//发邮件
			this.getDataGeter().sendMail(playerId, MailConst.MailId.PAY, title, subTitle, content, rewardList, true);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	

	
	/**
	 * 同步数据消息给玩家
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityInfo(String playerId, ArmiesMassEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}
		ArmiesMassPageInfoResp.Builder builder = ArmiesMassPageInfoResp.newBuilder();
		builder.setStage(entity.getStage());
		builder.setOpenTimes(entity.getSculptureOpenCount());
		builder.setShare(entity.getShare());
		ArmiesMassSculptureCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmiesMassSculptureCfg.class, entity.getStage());
		if(cfg == null){
			return;
		}
		for(ArmiesMassSculpture sculpture :entity.getSculptureList()){
			builder.addSculpture(sculpture.createBuilder(PBSculptureQuality.valueOf(cfg.getCenter() - 1)));
		}
		for(ArmiesMassGift gift : entity.getBuyGiftList()){
			builder.addBuyGifts(gift.createBuilder());
		}
		for(ArmiesMassGift gift : entity.getFreeAwardList()){
			builder.addBuyGifts(gift.createBuilder());
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.ARMIES_MASS_PAGE_INFO_RESP_VALUE, builder));
	}
	
	/**
	 * 阶段切换
	 */
	public void onStageChange(){
		int curStage = this.getCurStage();
		if(this.lastStage == curStage){
			return;
		}
		if(curStage == 0){
			return;
		}
		this.lastStage = curStage;
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ARMIES_MASS_RESET, () -> {
				boolean reset = this.checkStageReset(playerId);
				if(reset){
					this.syncActivityDataInfo(playerId);
				}
			});
		}
	}
	
	/**
	 * 检查阶段更换
	 * @param playerId
	 * @return
	 */
	public boolean checkStageReset(String playerId){
		Optional<ArmiesMassEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		ArmiesMassEntity entity = opEntity.get();
		int curStage = this.getCurStage();
		if(curStage == 0){
			return false;
		}
		if(entity.getStage() == curStage){
			return false;
		}
		logger.info("checkStageReset,playerId: {},curStage:{},lastStage:{}", 
				playerId,curStage,entity.getStage());
		ArmiesMassKVCfg kvCfg = HawkConfigManager.getInstance().
				getKVInstance(ArmiesMassKVCfg.class);
		//更换阶段ID
		entity.setStage(curStage);
		//重置登录礼包记录
		entity.getFreeAwardList().clear();
		//重置分享记录
		entity.setShare(0);
		//重置礼包购买记录
		entity.getBuyGiftList().clear();
		//重置次数
		entity.setSculptureOpenCount(kvCfg.getOpenSculptureTimes());
		//重置雕像信息
		this.initSculpture(entity);
		return true;
	}

	
	/**
	 * 获取当前时间下的配置
	 * @return
	 */
	public int getCurStage(){
		long curTime = HawkTime.getMillisecond();
		int termId = this.getActivityTermId();
		ArmiesMassKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ArmiesMassKVCfg.class);
		long startTime1 = this.getTimeControl().getStartTimeByTermId(termId);
		long stageEndTime1 = startTime1 + cfg.getFirstTime();
		long stageEndTime2 = stageEndTime1 + cfg.getSecondTime();
		long stageEndTime3 = stageEndTime2 + cfg.getThirdTime();
		if(curTime >startTime1 && curTime <= stageEndTime1){
			return 1;
		}
		if(curTime >stageEndTime1 && curTime <= stageEndTime2){
			return 2;
		}
		if(curTime >stageEndTime2 && curTime <= stageEndTime3){
			return 3;
		}
		return 0;
	 }
	
	
	/**
	 *  初始化雕像
	 * @param entity
	 */
	public void initSculpture(ArmiesMassEntity entity){
		int stage = entity.getStage();
		ArmiesMassSculptureCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(ArmiesMassSculptureCfg.class, stage);
		if(cfg == null){
			return;
		}
		List<ArmiesMassSculpture> rlt = new CopyOnWriteArrayList<ArmiesMassSculpture>();
		for(int i=1;i<=cfg.getBrandNumber();i++){
			ArmiesMassSculpture sculpture = ArmiesMassSculpture.valueOf(i,0, 0);
			rlt.add(sculpture);
		}
		entity.reSetSculptureList(rlt);
	}

}
