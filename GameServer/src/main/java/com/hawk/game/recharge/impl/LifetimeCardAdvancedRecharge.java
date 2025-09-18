package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.monthcard.MonthCardActivity;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.monthcard.entity.MonthCardItem;
import com.hawk.game.config.PlantFactoryCfg;
import com.hawk.game.entity.PlantFactoryEntity;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.plantfactory.PlayerPlantFactoryModule;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.gamelib.GameConst;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.config.LifetimeCardCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.LifetimeCardEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.module.PlayerLifetimeCardModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.GsConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 终身卡直购
 * @author Golden
 *
 */
public class LifetimeCardAdvancedRecharge extends AbstractGiftRecharge {

	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		// 大本等级限制
		int unlockCityLevel = LifetimeCardCfg.getInstance().getUnlockCityLevel();
		if (player.getCityLevel() < unlockCityLevel) {
			return false;
		}
		// 终身卡未解锁
		LifetimeCardEntity lifetimeCardEntity = player.getData().getLifetimeCardEntity();
		if (!lifetimeCardEntity.isCommonUnlock()) {
			return false;
		}

		long currentTime = HawkTime.getMillisecond();
		long beforeEndTime = lifetimeCardEntity.getAdvancedEndTime();
		int goFace = LifetimeCardCfg.getInstance().getGoFace();
		//如果进阶卡未临近过期则不能购买
		if (beforeEndTime > 0 && (beforeEndTime - currentTime) > HawkTime.DAY_MILLI_SECONDS * goFace){
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		long currentTime = HawkTime.getMillisecond();
		// 进阶卡持续时间
		long advancedContinue = LifetimeCardCfg.getInstance().getAdvancedContinue();
		
		LifetimeCardEntity lifetimeCardEntity = player.getData().getLifetimeCardEntity();
		long beforeEndTime = lifetimeCardEntity.getAdvancedEndTime();

		if (beforeEndTime == 0L || beforeEndTime < currentTime) {
			lifetimeCardEntity.setAdvancedEndTime(currentTime + advancedContinue);
		} else {
			lifetimeCardEntity.setAdvancedEndTime(beforeEndTime + advancedContinue);
		}
		//重置作用号
		player.getEffect().resetLifeTimeCard(player);
		//同步终身卡信息
		PlayerLifetimeCardModule module = player.getModule(GsConst.ModuleType.LIFETIME_CARD);
		module.syncLifetimeCardInfo();
		//推送泰能工厂信息
		PlayerPlantFactoryModule plantFactoryModule = player.getModule(GsConst.ModuleType.PLANT_FACTORY);
		plantFactoryModule.syncPlantFactoryInfo();
		try {
			List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
			for (PlantFactoryEntity factory : player.getData().getPlantFactoryEntities()) {
				PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, factory.getPlantCfgId());
				if (cfg == null) {
					continue;
				}
				int effVal = plantFactoryModule.getEffVal(factory);
				rewardList.add(RewardHelper.toRewardItem(Const.ItemType.TOOL_VALUE * GameConst.ITEM_TYPE_BASE, cfg.getItemId(), (long) Math.ceil(cfg.getItemPerDay() * (GsConst.EFF_RATE + effVal) * GsConst.EFF_PER)));
			}
			long now = HawkTime.getMillisecond();
			Optional<MonthCardActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MONTHCARD_ACTIVITY.intValue());
			if (opActivity.isPresent()) {
				MonthCardActivity activity = opActivity.get();
				Optional<ActivityMonthCardEntity> opDataEntity = activity.getPlayerDataEntity(player.getId());
				if (opDataEntity.isPresent()) {
					ActivityMonthCardEntity entity = opDataEntity.get();
					for (MonthCardItem card : entity.getCardList()) {
						MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, card.getCardId());
						if (cfg.getValidEndTime(card.getPucharseTime()) <= now) {
							continue;
						}
						List<Reward.RewardItem.Builder> tmp = activity.getInitialDailyReward(player.getId(), entity, cfg);
						double eff641 = (GsConst.EFF_RATE + player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_641)) * GsConst.EFF_PER;
						RewardHelper.multiCeilItemList(tmp, eff641);
						rewardList.addAll(tmp);
					}
				}
			}
			rewardList = RewardHelper.mergeRewardItem(rewardList);
			RewardHelper.multiCeilItemList(rewardList, LifetimeCardCfg.getInstance().getAdvanceRewardDay());
			List<ItemInfo> reward = new ArrayList<>();
			for(Reward.RewardItem.Builder builder : rewardList){
				if(builder.getItemCount() == 0){
					continue;
				}
				reward.add(ItemInfo.valueOf(RewardHelper.toItemString(builder.build())));
			}
			if(!reward.isEmpty()){
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailConst.MailId.LIFE_TIME_CARD_ADVANCE_THREE_DAY)
						.setRewards(reward)
						.setAwardStatus(Const.MailRewardStatus.NOT_GET)
						.build());
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.LIFETIME_ADVANCED_CARD;
	}

}
