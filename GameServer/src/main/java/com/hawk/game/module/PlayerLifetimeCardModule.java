package com.hawk.game.module;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.monthcard.MonthCardActivity;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.monthcard.entity.MonthCardItem;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.PlantFactoryCfg;
import com.hawk.game.entity.PlantFactoryEntity;
import com.hawk.game.module.plantfactory.PlayerPlantFactoryModule;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.GameConst;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.config.LifetimeCardCfg;
import com.hawk.game.entity.LifetimeCardEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.module.agency.PlayerAgencyModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.LifetimeCard.LifeTimeCardInfo;
import com.hawk.game.protocol.LifetimeCard.LifeTimeCardPlantFactoryDayResp;
import com.hawk.game.protocol.LifetimeCard.LifeTimeCardPlantFactoryRewardResp;
import com.hawk.game.protocol.LifetimeCard.PBPlantFactoryDayItem;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.PlantFactory.PBPlantFactoryType;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GsConst;
import com.hawk.log.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 终身卡
 * @author Golden
 *
 */
public class PlayerLifetimeCardModule extends PlayerModule {

	/**
	 * tick周期
	 */
	private static final long TICK_PERIOD = 5000L;
	
	/**
	 * 上次tick时间
	 */
	private long lastTickTime;

	/**
	 * 进阶卡过期推送
	 */
	private boolean advanceTimeoutPush;
	
	/**
	 * 可领奖次数,变化刷新
	 */
	private int canRewardTimes;
	
	/**
	 * 构造方法
	 * @param player
	 */
	public PlayerLifetimeCardModule(Player player) {
		super(player);
	}

	
	@Override
	public boolean onTick() {
		// 5s tick一次
		long currTime = HawkTime.getMillisecond();
		if(currTime - lastTickTime < TICK_PERIOD){
			return true;
		}
		lastTickTime = HawkTime.getMillisecond();

		// 终身卡未解锁,不tick
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		
		// 临时修改,免费到期30s内重新刷作用号
		long freeEndTime = entity.getFreeEndTime();
		if (currTime - freeEndTime > 0 && currTime - freeEndTime < 30000) {
			player.getEffect().resetLifeTimeCard(player);
		}
		
		if (!entity.isCommonUnlock()) {
			return true;
		}
		
		// 周宝箱检查补发
		checkWeekReissue();
		// 月宝箱检查补发
		checkMonthReissue();
		// 进阶卡过期检测
		checkAdvanceTimeout();
		// 检测领奖次数
		checkRewardTimes();
		return true;
	}

	private void checkFreeTime(){
		LifetimeCardCfg cfg = LifetimeCardCfg.getInstance();
		LifetimeCardEntity lifetimeCardEntity = player.getData().getLifetimeCardEntity();
		if (lifetimeCardEntity.getFreeEndTime() > 0) {
			return;	
		}
		
		long freeTime = 0;
		if (player.getCreateTime() > cfg.getStartTimeMill()){
			freeTime = cfg.getFreeTime();
		}else {
			freeTime = cfg.getOldFreeTime();
		}
		
		// 设置免费时间
		lifetimeCardEntity.setFreeEndTime(HawkTime.getMillisecond() + freeTime);
		
		// 刷新下作用号
		player.getEffect().resetLifeTimeCard(player);
		
		// 新加军情中心任务
		PlayerAgencyModule agencyModule = player.getModule(GsConst.ModuleType.AGENCY_MODULE);
		agencyModule.addMission();
		agencyModule.pushPageInfo();	
	}
	
	/**
	 * 同步终身卡信息
	 */
	public void syncLifetimeCardInfo() {
		long currentTime = HawkTime.getMillisecond();
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		LifeTimeCardInfo.Builder builder = LifeTimeCardInfo.newBuilder();
		boolean moduleUnlock = isModuleUnlock();
		builder.setModuleUnlock(moduleUnlock);
		if (moduleUnlock) {
			builder.setCommonUnlock(entity.isCommonUnlock());
			builder.setAdvancedUnlock(entity.getAdvancedEndTime() > currentTime);
			builder.setAdvancedEndTime(entity.getAdvancedEndTime());
			builder.setWeekAwarded(getWeekCanRewardTimes() <= entity.getWeekAwardTime());
			builder.setMonthAwarded(getMonthCanRewardTimes() <= entity.getMonthAwardTime());
			builder.setFreeEndTime(entity.getFreeEndTime());
			builder.setSevenDayAwardTake(getNextTakeTime7());
			builder.setThirtyDayAwardTake(getNextTakeTime30());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.LIFETIME_CARD_INFO_PUSH_VALUE, builder));
		
		canRewardTimes = getWeekCanRewardTimes() + getMonthCanRewardTimes();
	}

	@Override
	protected boolean onPlayerLogin() {
		if (isModuleUnlock()){
			checkFreeTime();
		}
		syncLifetimeCardInfo();
		return true;
	}

	/**
	 * 终身卡是否解锁
	 */
	private boolean isModuleUnlock() {
		return player.getCityLevel() >= LifetimeCardCfg.getInstance().getUnlockCityLevel();
	}
	
	/**
	 * 大本升级处理
	 */
	public void onCityLevelUp() {
		// 如果达到指定等级,重新推下信息
		if (isModuleUnlock()) {
			checkFreeTime();
			syncLifetimeCardInfo();
			player.getEffect().resetLifeTimeCard(player);
		}
	}
	
	/**
	 * 领取每周奖励
	 */
	@ProtocolHandler(code = HP.code2.LIFETIME_CARD_WEEK_AWARD_VALUE)
	private void weekAward(HawkProtocol protocol) {
		// 判断是否解锁
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		if (!entity.isCommonUnlock()) {
			return;
		}
		// 本周已经领取过
		if (getWeekCanRewardTimes() <= entity.getWeekAwardTime()){
			return;
		}
		entity.addWeekAwardTime();
		syncLifetimeCardInfo();
		
		// 发奖
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(LifetimeCardCfg.getInstance().getWeekAward());
		award.rewardTakeAffectAndPush(player, Action.LIFETIME_WEEK_AWARD, true);
	}
	
	/**
	 * 领取每月奖励
	 */
	@ProtocolHandler(code = HP.code2.LIFETIME_CARD_MONTH_AWARD_VALUE)
	private void monthAward(HawkProtocol protocol) {
		// 判断是否解锁
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		if (!entity.isCommonUnlock()) {
			return;
		}
		// 本月已经领取过
		if (getMonthCanRewardTimes() <= entity.getMonthAwardTime()){
			return;
		}
		entity.addMonthAwardTime();
		syncLifetimeCardInfo();
		// 发奖
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(LifetimeCardCfg.getInstance().getMonthAward());
		award.rewardTakeAffectAndPush(player, Action.LIFETIME_MONTH_AWARD, true);
		syncLifetimeCardInfo();
	}

	@ProtocolHandler(code = HP.code2.LIFETIME_CARD_PLANT_FACTORY_DAY_REQ_VALUE)
	private void plantFactoryDayReq(HawkProtocol protocol) {
		LifeTimeCardPlantFactoryDayResp.Builder resp = LifeTimeCardPlantFactoryDayResp.newBuilder();
		int plantMulti = 0;
		for (EffectObject effectObject:LifetimeCardCfg.getInstance().getAdvanceEffList()){
			if(effectObject.getEffectType() == Const.EffType.LIFE_TIME_CARD_646_VALUE){
				plantMulti += effectObject.getEffectValue();
			}
		}
		PlayerPlantFactoryModule plantFactoryModule = player.getModule(GsConst.ModuleType.PLANT_FACTORY);
		int effVal = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_646);
		for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
			PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, factory.getPlantCfgId());
			if(cfg == null){
				continue;
			}
			PBPlantFactoryType type = PBPlantFactoryType.valueOf(factory.getFactoryType());
			if(type == null){
				continue;
			}
			if(effVal > 0){
				int after = plantFactoryModule.getEffVal(factory);
				int before = after - effVal;
				PBPlantFactoryDayItem.Builder item = PBPlantFactoryDayItem.newBuilder();
				item.setType(type);
				item.setBefore((int)Math.ceil(cfg.getItemPerDay() * (GsConst.EFF_RATE + before) * GsConst.EFF_PER));
				item.setAfter((int)Math.ceil(cfg.getItemPerDay() * (GsConst.EFF_RATE + after) * GsConst.EFF_PER));
				resp.addItems(item);
			}else {
				int before = plantFactoryModule.getEffVal(factory);
				int after = before + plantMulti;
				PBPlantFactoryDayItem.Builder item = PBPlantFactoryDayItem.newBuilder();
				item.setType(type);
				item.setBefore((int)Math.ceil(cfg.getItemPerDay() * (GsConst.EFF_RATE + before) * GsConst.EFF_PER));
				item.setAfter((int)Math.ceil(cfg.getItemPerDay() * (GsConst.EFF_RATE + after) * GsConst.EFF_PER));
				resp.addItems(item);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.LIFETIME_CARD_PLANT_FACTORY_DAY_RESP, resp));
	}


	@ProtocolHandler(code = HP.code2.LIFETIME_CARD_PLANT_FACTORY_REWARD_REQ_VALUE)
	private void plantFactoryRewardReq(HawkProtocol protocol) {
		LifeTimeCardPlantFactoryRewardResp.Builder resp = LifeTimeCardPlantFactoryRewardResp.newBuilder();
		int plantMulti = 0;
		int cardMulti = 0;
		for (EffectObject effectObject:LifetimeCardCfg.getInstance().getAdvanceEffList()){
			if(effectObject.getEffectType() == Const.EffType.LIFE_TIME_CARD_646_VALUE){
				plantMulti += effectObject.getEffectValue();
			}
			if(effectObject.getEffectType() == Const.EffType.LIFE_TIME_CARD_641_VALUE){
				cardMulti += effectObject.getEffectValue();
			}
		}
		int day = LifetimeCardCfg.getInstance().getAdvanceRewardDay();
		PlayerPlantFactoryModule plantFactoryModule = player.getModule(GsConst.ModuleType.PLANT_FACTORY);
		int effVal = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_646);
		for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
			PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, factory.getPlantCfgId());
			if(cfg == null){
				continue;
			}
			if(effVal > 0){
				resp.addReward(RewardHelper.toRewardItem(Const.ItemType.TOOL_VALUE * GameConst.ITEM_TYPE_BASE, cfg.getItemId(), getAdvanceCount((int)cfg.getItemPerDay(), (GsConst.EFF_RATE + plantFactoryModule.getEffVal(factory)) * GsConst.EFF_PER, day)));
			}else {
				int before = plantFactoryModule.getEffVal(factory);
				int after = before + plantMulti;
				resp.addReward(RewardHelper.toRewardItem(Const.ItemType.TOOL_VALUE * GameConst.ITEM_TYPE_BASE, cfg.getItemId(), getAdvanceCount((int)cfg.getItemPerDay(), (GsConst.EFF_RATE + after) * GsConst.EFF_PER, day)));
			}
		}
		long now = HawkTime.getMillisecond();
		List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
		Optional<MonthCardActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MONTHCARD_ACTIVITY.intValue());
		if (opActivity.isPresent()) {
			MonthCardActivity activity = opActivity.get();
			Optional<ActivityMonthCardEntity> opDataEntity = activity.getPlayerDataEntity(player.getId());
			if(opDataEntity.isPresent()){
				ActivityMonthCardEntity entity = opDataEntity.get();
				for (MonthCardItem card : entity.getCardList()) {
					MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, card.getCardId());
					if (cfg.getValidEndTime(card.getPucharseTime()) <= now) {
						continue;
					}
					List<Reward.RewardItem.Builder> tmp = activity.getInitialDailyReward(player.getId(), entity, cfg);
					RewardHelper.multiCeilItemList(tmp, (GsConst.EFF_RATE + cardMulti) * GsConst.EFF_PER);
					rewardList.addAll(tmp);
				}
			}
		}
		rewardList = RewardHelper.mergeRewardItem(rewardList);
		RewardHelper.multiCeilItemList(rewardList, day);
		for(Reward.RewardItem.Builder builder : rewardList){
			resp.addCardReward(builder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.LIFETIME_CARD_PLANT_FACTORY_REWARD_RESP, resp));
	}

	public long getAdvanceCount(int count, double multi, int day){
		return (long)(Math.ceil(count * multi) * day);
	}

	/**
	 * 周奖励可领取次数
	 */
	private int getWeekCanRewardTimes() {
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		if (!entity.isCommonUnlock()) {
			return 0;
		}
		return (int)((HawkTime.getMillisecond() - entity.getCommonUnlockTime()) / GsConst.WEEK_MILLI_SECONDS) + 1;
	}
	
	/**
	 * 月奖励可领取次数
	 */
	private int getMonthCanRewardTimes() {
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		if (!entity.isCommonUnlock()) {
			return 0;
		}
		return (int)((HawkTime.getMillisecond() - entity.getCommonUnlockTime()) / GsConst.MONTH_MILLI_SECONDS) + 1;
	}
	
	/**
	 * 检测周补发
	 */
	public void checkWeekReissue() {
		try {
			LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
			if (getWeekCanRewardTimes() - entity.getWeekAwardTime() > 1) {
				entity.addWeekAwardTime();
				MailParames mailParames = MailParames.newBuilder()
						.setMailId(MailId.LIFE_TIME_CARD_AWARD_WEEK)
						.setPlayerId(player.getId())
						.setRewards(LifetimeCardCfg.getInstance().getWeekAward())
						.setAwardStatus(Const.MailRewardStatus.NOT_GET)
						.build();
				MailService.getInstance().sendMail(mailParames);
			}	
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检测月补发
	 */
	public void checkMonthReissue() {
		try {
			LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
			if (getMonthCanRewardTimes() - entity.getMonthAwardTime() > 1) {
				entity.addMonthAwardTime();
				MailParames mailParames = MailParames.newBuilder()
						.setMailId(MailId.LIFE_TIME_CARD_AWARD_MONTH)
						.setPlayerId(player.getId())
						.setRewards(LifetimeCardCfg.getInstance().getMonthAward())
						.setAwardStatus(Const.MailRewardStatus.NOT_GET)
						.build();
				MailService.getInstance().sendMail(mailParames);
			}	
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检测进阶卡过期
	 */
	public void checkAdvanceTimeout() {
		try {
			LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
			if (entity.getAdvancedEndTime() > HawkTime.getMillisecond()) {
				return;
			}
			//检测到有补卡
			if (checkReissueAdvanceCard(entity)){
				return;
			}
			if (advanceTimeoutPush) {
				return;
			}
			advanceTimeoutPush = true;
			player.getEffect().resetLifeTimeCard(player);
			syncLifetimeCardInfo();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检测可以领奖同步
	 */
	public void checkRewardTimes() {
		int times = getWeekCanRewardTimes() + getMonthCanRewardTimes();
		if (canRewardTimes != times) {
			canRewardTimes = times;
			syncLifetimeCardInfo();
		}
	}

	/**
	 * 下次领取时间(周奖励)
	 * @return
	 */
	public long getNextTakeTime7(){
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		int weeks = (int) ((HawkTime.getMillisecond() - entity.getCommonUnlockTime()) / GsConst.WEEK_MILLI_SECONDS) + 1;
		long nextTakeTime = entity.getCommonUnlockTime() + weeks * GsConst.WEEK_MILLI_SECONDS;
		return nextTakeTime;
	}

	/**
	 * 下次领取时间(月奖励)
	 * @return
	 */
	public long getNextTakeTime30(){
		LifetimeCardEntity entity = player.getData().getLifetimeCardEntity();
		int months = (int) ((HawkTime.getMillisecond() - entity.getCommonUnlockTime()) / GsConst.MONTH_MILLI_SECONDS) + 1;
		long nextTakeTime = entity.getCommonUnlockTime() + months * GsConst.MONTH_MILLI_SECONDS;
		return nextTakeTime;
	}

	/**
	 * 检测到补发，到期自动续费
	 * @param entity
	 */
	public boolean checkReissueAdvanceCard(LifetimeCardEntity entity){
		if (entity.getReady() == 1){
			entity.setReady(0);
			long currentTime = HawkTime.getMillisecond();
			// 进阶卡持续时间
			long advancedContinue = LifetimeCardCfg.getInstance().getAdvancedContinue();
			entity.setAdvancedEndTime(currentTime + advancedContinue);
			//重置作用号
			player.getEffect().resetLifeTimeCard(player);
			//同步终身卡信息
			syncLifetimeCardInfo();
			//推送泰能工厂信息
			PlayerPlantFactoryModule plantFactoryModule = player.getModule(GsConst.ModuleType.PLANT_FACTORY);
			plantFactoryModule.syncPlantFactoryInfo();
			return true;
		}
		return false;
	}

	@Override
	protected boolean onPlayerLogout() {
		this.advanceTimeoutPush = false;
		return super.onPlayerLogout();
	}
}
