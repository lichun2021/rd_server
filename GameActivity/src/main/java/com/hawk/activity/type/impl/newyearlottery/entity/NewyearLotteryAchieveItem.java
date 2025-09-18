package com.hawk.activity.type.impl.newyearlottery.entity;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotteryAchieveCfg;
import com.hawk.game.protocol.Activity.GiftBuyAchieveData;
import com.hawk.game.protocol.Activity.GiftBuyAchieveState;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class NewyearLotteryAchieveItem implements SplitEntity {
	
	/** 成就id*/
	private int achieveId;
	
	/** 成就状态*/
	private int state;
	
	private int value;
	
	private int lotteryType;
	
	public NewyearLotteryAchieveItem() {
	}
	
	public static NewyearLotteryAchieveItem valueOf(int achieveId) {
		NewyearLotteryAchieveItem data = new NewyearLotteryAchieveItem();
		data.achieveId = achieveId;
		data.state = GiftBuyAchieveState.NEWYEAR_LOTTERY_NOT_ACHIEVE_VALUE;
		NewyearLotteryAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryAchieveCfg.class, data.achieveId);
		data.lotteryType = cfg.getLotteryType();
		return data;
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(achieveId);
		dataList.add(state);
		dataList.add(value);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		achieveId = dataArray.getInt();
		state = dataArray.getInt();
		value = dataArray.getInt();
	}
	
	public NewyearLotteryAchieveItem newInstance() {
		return new NewyearLotteryAchieveItem();
	}
	
	public GiftBuyAchieveData.Builder toBuilder() {
		GiftBuyAchieveData.Builder achieveBuilder = GiftBuyAchieveData.newBuilder();
		achieveBuilder.setId(this.achieveId);
		achieveBuilder.setVal(this.value);
		achieveBuilder.setState(GiftBuyAchieveState.valueOf(this.state));
		return achieveBuilder;
	}

	public int getAchieveId() {
		return achieveId;
	}

	public void setAchieveId(int achieveId) {
		this.achieveId = achieveId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getLotteryType() {
		if (lotteryType == 0) {
			NewyearLotteryAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryAchieveCfg.class, achieveId);
			lotteryType = cfg.getLotteryType();
		}
		return lotteryType;
	}

	public void setLotteryType(int lotteryType) {
		this.lotteryType = lotteryType;
	}
	
}
