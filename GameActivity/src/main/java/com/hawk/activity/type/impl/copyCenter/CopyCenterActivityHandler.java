package com.hawk.activity.type.impl.copyCenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.copyCenter.cfg.CopyCenterActivityCfg;
import com.hawk.activity.type.impl.copyCenter.cfg.CopyCenterChipCfg;
import com.hawk.activity.type.impl.copyCenter.cfg.CopyCenterKVCfg;
import com.hawk.activity.type.impl.copyCenter.cfg.CopyCenterRewardCfg;
import com.hawk.game.protocol.Activity.PBCopyCenterReq;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.log.Action;

/** 累计登录活动网络消息接收句柄
 * 
 * @author PhilChen */
public class CopyCenterActivityHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code.COPY_CENTER_ONE_VALUE)
	public boolean onOnece(HawkProtocol protocol, String playerId) {
		PBCopyCenterReq req = protocol.parseProtocol(PBCopyCenterReq.getDefaultInstance());
		final int heroId = req.getHeroId();
		return replication(playerId, heroId, 1, protocol.getType(), Action.ACTIVITY_REPLICATION_CENTER_ONE);

	}

	@ProtocolHandler(code = HP.code.COPY_CENTER_TEN_VALUE)
	public boolean onTenTimes(HawkProtocol protocol, String playerId) {
		PBCopyCenterReq req = protocol.parseProtocol(PBCopyCenterReq.getDefaultInstance());
		final int heroId = req.getHeroId();
		return replication(playerId, heroId, 10, protocol.getType(), Action.ACTIVITY_REPLICATION_CENTER_TEN);
	}

	private boolean replication(String playerId, final int heroId, final int REP_COUNT, int type, Action action) {
		CopyCenterActivity activity = getActivity(ActivityType.COPY_CENTER);
		CopyCenterActivityCfg actCfg = HawkConfigManager.getInstance().getConfigIterator(CopyCenterActivityCfg.class).stream()
				.filter(cfg -> cfg.getHeroId() == 0 || cfg.getHeroId() == heroId)
				.findFirst()
				.get();

		PBHeroInfo heroInfo = activity.getDataGeter().getHeroInfo(playerId, heroId);
		if (Objects.isNull(heroInfo)
				|| actCfg.getHeroId() != 0 && actCfg.getHeroId() != heroId
				|| heroInfo.getLevel() < actCfg.getLevel()
				|| heroInfo.getStar() < actCfg.getStar()
				|| heroInfo.getQualityColor() < actCfg.getColor()) {
			return false;
		}

		CopyCenterKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(CopyCenterKVCfg.class);
		final int chipId = RewardHelper.toRewardItem(heroInfo.getUnlockPieces()).getItemId();
		boolean isS = heroInfo.getQualityColor() < 5; // 是S级
		if (isS && activity.getFrequencySCount(playerId) + REP_COUNT > kvCfg.getFrequencyS()) {
			return false;
		}
		if (!isS && activity.getFrequencySSCount(playerId) + REP_COUNT > kvCfg.getFrequencySS()) {
			return false;
		}

		// 扣费
		Reward.RewardItem.Builder cost = RewardHelper.toRewardItem(isS ? kvCfg.getCostItem() : kvCfg.getSeniorCostItem());
		// 拥有道具数
		int ticketCount = activity.getDataGeter().getItemNum(playerId, cost.getItemId());
		List<RewardItem.Builder> itemList = costList(actCfg, ticketCount, REP_COUNT, isS ? kvCfg.getItemPrice() : kvCfg.getSeniorItemPrice());
		boolean consumeResult = activity.getDataGeter().consumeItems(playerId, itemList, type, action);
		if (consumeResult == false) {
			return false;
		}
		{
			List<RewardItem.Builder> rewardList = reward(chipId, REP_COUNT, isS);
			ActivityReward reward = new ActivityReward(rewardList, action);
			reward.setAlert(true);
			reward.setOrginType(RewardOrginType.COPY_CENTER_REWARD, activity.getActivityId());
			activity.postReward(playerId, reward);

		}
		if (StringUtils.isNotEmpty(kvCfg.getExReward())) {
			List<RewardItem.Builder> result = RewardHelper.toRewardItemList(kvCfg.getExReward());
			result.forEach(bul -> bul.setItemCount(bul.getItemCount() * REP_COUNT));
			ActivityReward reward = new ActivityReward(result, action);
			reward.setAlert(false);
			reward.setOrginType(RewardOrginType.COPY_CENTER_REWARD, activity.getActivityId());
			activity.postReward(playerId, reward);
		}

		if (isS) {
			activity.incFrequencySCount(playerId, REP_COUNT);
		} else {
			activity.incFrequencySSCount(playerId, REP_COUNT);
		}
		activity.sync(playerId);
		return true;
	}

	private List<Builder> costList(CopyCenterActivityCfg actCfg, int ticketCount, int repCount, String itemPrice) {
		int notHave = repCount - ticketCount;
		CopyCenterKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(CopyCenterKVCfg.class);
		Reward.RewardItem.Builder ticket = RewardHelper.toRewardItem(kvCfg.getCostItem());
		List<Builder> cost = new ArrayList<>();
		if (notHave > 0) {
			if (ticketCount > 0) {
				ticket.setItemCount(ticketCount);
				cost.add(ticket);
			}
			Reward.RewardItem.Builder priceItem = RewardHelper.toRewardItem(itemPrice);
			priceItem.setItemCount(priceItem.getItemCount() * notHave);
			cost.add(priceItem);

		} else {
			cost.add(ticket.setItemCount(repCount));
		}
		return cost;
	}

	private List<RewardItem.Builder> reward(final int chipId, int repeat, boolean isS) {
		final int type = isS ? 1 : 2;
		List<RewardItem.Builder> result = new ArrayList<>();
		for (int i = 0; i < repeat; i++) {
			CopyCenterChipCfg chipCfg = HawkRand.randomWeightObject(HawkConfigManager.getInstance().getConfigIterator(CopyCenterChipCfg.class).toList());
			List<CopyCenterRewardCfg> list = HawkConfigManager.getInstance().getConfigIterator(CopyCenterRewardCfg.class).toList().stream()
					.filter(cfg -> cfg.getType() == type)
					.collect(Collectors.toList());
			CopyCenterRewardCfg rewardCfg = HawkRand.randomWeightObject(list);
			result.addAll(RewardHelper.toRewardItemList(rewardCfg.getRewards()));
			result.add(RewardHelper.toRewardItem(ItemType.TOOL_VALUE, chipId, chipCfg.getChipNum()));
		}
		return result;
	}

}
