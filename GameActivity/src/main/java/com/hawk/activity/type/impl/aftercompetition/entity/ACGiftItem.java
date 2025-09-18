package com.hawk.activity.type.impl.aftercompetition.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.aftercompetition.AfterCompetitionConst;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionShopCfg;
import com.hawk.game.protocol.Activity.ACGiftInfoPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 礼物数据
 * 
 * @author lating
 */
public class ACGiftItem implements SplitEntity {
	
	private int giftId;
	private int selfSendCount;
	private int selfRecCount;
	private String defaultSendPlayer;
	private Set<String> bigRewardQQRec = new HashSet<>();
	private Set<String> bigRewardWXRec = new HashSet<>();

	public ACGiftItem() {
	}
	
	public static ACGiftItem valueOf(int giftId) {
		ACGiftItem data = new ACGiftItem();
		data.giftId = giftId;
		return data;
	}
	
	@Override
	public ACGiftItem newInstance() {
		return new ACGiftItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(giftId);
		dataList.add(selfSendCount);
		dataList.add(selfRecCount);
		dataList.add(defaultSendPlayer);
		dataList.add(SerializeHelper.collectionToString(this.bigRewardQQRec, SerializeHelper.BETWEEN_ITEMS));
		dataList.add(SerializeHelper.collectionToString(this.bigRewardWXRec, SerializeHelper.BETWEEN_ITEMS));
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(6);
		giftId = dataArray.getInt();
		selfSendCount = dataArray.getInt();
		selfRecCount = dataArray.getInt();
		defaultSendPlayer = dataArray.getString();
		String bigRewardQQRecStr = dataArray.getString();
		this.bigRewardQQRec = SerializeHelper.stringToSet(String.class, bigRewardQQRecStr, SerializeHelper.BETWEEN_ITEMS);
		String bigRewardWXRecStr = dataArray.getString();
		this.bigRewardWXRec = SerializeHelper.stringToSet(String.class, bigRewardWXRecStr, SerializeHelper.BETWEEN_ITEMS);
	}

	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int giftId) {
		this.giftId = giftId;
	}

	public int getSelfSendCount() {
		return selfSendCount;
	}

	public void setSelfSendCount(int selfSendCount) {
		this.selfSendCount = selfSendCount;
	}
	
	public void addSelfSendCount(int add) {
		this.selfSendCount += add;
	}
	

	public int getSelfRecCount() {
		return selfRecCount;
	}

	public void setSelfRecCount(int selfRecCount) {
		this.selfRecCount = selfRecCount;
	}
	
	public void addSelfRecCount(int add) {
		this.selfRecCount += add;
	}

	public Set<String> getBigRewardQQRec() {
		return bigRewardQQRec;
	}

	public Set<String> getBigRewardWXRec() {
		return bigRewardWXRec;
	}
	
	public void addRecieveAward(int channel, String awardUuid) {
		if (channel == AfterCompetitionConst.CHANNEL_QQ) {
			this.getBigRewardQQRec().add(awardUuid);
		}else {
			this.getBigRewardWXRec().add(awardUuid);
		}
	}
	
	public Set<String> getBigRewardRecList(int channel) {
		if (channel == AfterCompetitionConst.CHANNEL_QQ) {
			return this.getBigRewardQQRec();
		}
		return this.getBigRewardWXRec();
	}
	
	public String getDefaultSendPlayer() {
		return defaultSendPlayer;
	}

	public void setDefaultSendPlayer(String defaultSendPlayer) {
		this.defaultSendPlayer = defaultSendPlayer;
	}

	public ACGiftInfoPB.Builder toBuilder(int globalBuyCount) {
		ACGiftInfoPB.Builder builder = ACGiftInfoPB.newBuilder();
		builder.setGiftId(giftId);
		builder.setSendCount(selfSendCount);
		builder.setRecCount(selfRecCount);
		builder.setGlobalBuyCount(globalBuyCount);
		builder.addAllRecBigRewardQQ(bigRewardQQRec);
		builder.addAllRecBigRewardWX(bigRewardWXRec);
		if (!HawkOSOperator.isEmptyString(defaultSendPlayer)) {
			builder.setSendToPlayer(defaultSendPlayer);
			if (ActivityManager.getInstance().getDataGeter().checkPlayerExist(defaultSendPlayer)) {
				String playerName = ActivityManager.getInstance().getDataGeter().getPlayerName(defaultSendPlayer);
				builder.setSendToPlayerName(playerName);
			}
		}
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftId);
		builder.setTotalRewardToSend(globalBuyCount / giftCfg.getBuyGoodsNeedCount());
		return builder;
	}

}
