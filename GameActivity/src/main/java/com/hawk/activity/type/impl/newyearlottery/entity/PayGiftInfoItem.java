package com.hawk.activity.type.impl.newyearlottery.entity;

import java.util.Collections;
import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotteryGiftCfg;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotterySlotCfg;
import com.hawk.game.protocol.Activity.NewyearLotteryGiftInfoPB;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class PayGiftInfoItem implements SplitEntity {
	/**
	 * 礼包类型
	 */
	private int lotteryType;
	/**
	 * 联盟购买人数
	 */
	private int payCount;
	/**
	 * 联盟购买人数成就任务进度值
	 */
	private int achieveValue;
	/**
	 * 自身购买的时间
	 */
	private long selfPayTime;
	/**
	 * 自选奖励
	 */
	private int selectId;
	/**
	 * 随机奖励
	 */
	private String randomAward = "";
	/**
	 * 抽奖获得的奖励
	 */
	private int lotteryAwardId;

	
	public PayGiftInfoItem() {
	}

	public static PayGiftInfoItem valueOf(int lotteryType, int selectId, String randomAward) {
		PayGiftInfoItem item = new PayGiftInfoItem();
		item.lotteryType = lotteryType;
		item.selectId = selectId;
		item.randomAward = randomAward;
		return item;
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(lotteryType);
		dataList.add(payCount);
		dataList.add(achieveValue);
		dataList.add(selfPayTime);
		dataList.add(selectId);
		dataList.add(randomAward);
		dataList.add(lotteryAwardId);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(7);
		lotteryType = dataArray.getInt();
		payCount = dataArray.getInt();
		achieveValue = dataArray.getInt();
		selfPayTime = dataArray.getLong();
		selectId = dataArray.getInt();
		randomAward = dataArray.getString();
		lotteryAwardId = dataArray.getInt();
	}

	@Override
	public SplitEntity newInstance() {
		return new PayGiftInfoItem();
	}

	public int getLotteryType() {
		return lotteryType;
	}

	public void setLotteryType(int lotteryType) {
		this.lotteryType = lotteryType;
	}

	public int getSelectId() {
		return selectId;
	}

	public void setSelectId(int selectId) {
		this.selectId = selectId;
	}

	public String getRandomAward() {
		return randomAward;
	}

	public void setRandomAward(String randomAward) {
		this.randomAward = randomAward;
	}
	
	public int getLotteryAwardId() {
		return lotteryAwardId;
	}

	public void setLotteryAwardId(int lotteryAwardId) {
		this.lotteryAwardId = lotteryAwardId;
	}

	public List<RewardItem.Builder> getSelectAwardList() {
		NewyearLotteryGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryGiftCfg.class, selectId);
		if (cfg == null) {
			cfg = NewyearLotteryGiftCfg.getDefaultRewardGiftCfg(lotteryType);
			String randomReward = HawkRand.randomWeightObject(cfg.getRandomRewardItems(), cfg.getRandomRewardWeight());
			this.setSelectId(cfg.getId());
			this.setRandomAward(randomReward);
		}
		return RewardHelper.toRewardItemImmutableList(cfg.getSelectedReward());
	}
	
	public List<RewardItem.Builder> getRandomAwardList() {
		if (HawkOSOperator.isEmptyString(this.getRandomAward())) {
			getSelectAwardList();
		}
		return RewardHelper.toRewardItemImmutableList(this.getRandomAward());
	}
	
	public List<RewardItem.Builder> getRegularRewardList() {
		NewyearLotteryGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryGiftCfg.class, selectId);
		if (cfg == null) {
			getSelectAwardList();
		}
		return RewardHelper.toRewardItemImmutableList(cfg.getRegularRewards());
	}
	
	public List<RewardItem.Builder> getLotteryAwardList() {
		NewyearLotterySlotCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotterySlotCfg.class, lotteryAwardId);
		if (cfg == null) {
			return Collections.emptyList();
		}
		return RewardHelper.toRewardItemImmutableList(cfg.getRewards());
	}
	
	public long getSelfPayTime() {
		return selfPayTime;
	}
	
	public void setSelfPayTime(long selfPayTime) {
		this.selfPayTime = selfPayTime;
	}

	public int getPayCount() {
		return payCount;
	}

	public void setPayCount(int payCount) {
		this.payCount = payCount;
	}
	
	public int getAchieveValue() {
		return achieveValue;
	}

	public void setAchieveValue(int achieveValue) {
		this.achieveValue = achieveValue;
	}
	
	public NewyearLotteryGiftInfoPB.Builder toBuilder(List<NewyearLotteryAchieveItem> itemList, int oldCount, int newPayCount, int itemCount) {
		NewyearLotteryGiftInfoPB.Builder giftItem = NewyearLotteryGiftInfoPB.newBuilder();
		giftItem.setType(getLotteryType());
		giftItem.setSelectId(getSelectId());
		getRandomAwardList().forEach(e -> giftItem.addRandomRewards(e));
		giftItem.setSelfPay(getSelfPayTime() > 0 ? 1 : 0);
		giftItem.setOldPayCount(oldCount);
		giftItem.setLatestPayCount(newPayCount);
		itemList.forEach(e -> giftItem.addAchieveData(e.toBuilder()));
		giftItem.setItemCount(itemCount);
		getLotteryAwardList().forEach(e -> giftItem.addLotteryReward(e));
		giftItem.setLotteryRewardId(lotteryAwardId);
		return giftItem;
	}

}
