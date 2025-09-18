package com.hawk.activity.type.impl.monthcard.entity;

import java.util.ArrayList;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple3;

import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.game.protocol.MonthCard.MonthCardPB;
import com.hawk.game.protocol.MonthCard.MonthCardState;
import com.hawk.serialize.string.SerializeHelper;

public class MonthCardItem {

	/** 月卡Id */
	private int cardId;

	/** 月卡状态 */
	private int state;
	
	/** 购买时间  */
	private long pucharseTime;
	
	/** 待发放月卡 */
	private int ready;

	public static MonthCardItem valueOf(int cardId, int state) {
		MonthCardItem cardInfo = new MonthCardItem();
		cardInfo.cardId = cardId;
		cardInfo.state = state;
		return cardInfo;
	}
	
	public static MonthCardItem valueOf(String data) {
		MonthCardItem item = new MonthCardItem();
		try {
			String[] array = SerializeHelper.split(data, SerializeHelper.ATTRIBUTE_SPLIT);
			String[] fillArray = SerializeHelper.fillStringArray(array, 4, "0");
			int index = 0;
			item.cardId = SerializeHelper.getInt(fillArray, index++);
			item.state = SerializeHelper.getInt(fillArray, index++);
			item.pucharseTime = SerializeHelper.getLong(fillArray, index++);
			item.ready = SerializeHelper.getInt(fillArray, index++);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return item;
	}
	
	@Override
	public String toString() {
		List<Object> list = new ArrayList<>();
		list.add(cardId);
		list.add(state);
		list.add(pucharseTime);
		list.add(ready);
		return SerializeHelper.collectionToString(list, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getPucharseTime() {
		return pucharseTime;
	}

	public void setPucharseTime(long pucharseTime) {
		this.pucharseTime = pucharseTime;
	}
	
	public int getReady() {
		return ready;
	}

	public void setReady(int ready) {
		this.ready = ready;
	}

	public MonthCardPB.Builder toBuilder() {
		int initialCard = MonthCardActivityCfg.getInitialCard(cardId);
		if (initialCard <= 0) {
			HawkLog.errPrintln("card item to builder failed, cardId: {}, initialCard: {}", cardId, initialCard);
			return null;
		}
		
		MonthCardPB.Builder builder = MonthCardPB.newBuilder();
		builder.setInitialCardId(initialCard);
		builder.setStatus(this.getState());
		builder.setCardId(this.getCardId());
		if (this.getState() != MonthCardState.UNPURCHASED_VALUE) {
			builder.setPurchaseTime(this.getPucharseTime());
		}
		int type = MonthCardActivityCfg.getMonthCardType(cardId);
		HawkTuple3< Long, Long, Long> timeLimit = MonthCardActivityCfg.getTimeLimit(type);
		if(timeLimit != null){
			builder.setStartTime(timeLimit.first);
			builder.setStopTime(timeLimit.second);
			builder.setHiddenTime(timeLimit.third);
		}
		return builder;
	}
	
	public static MonthCardPB.Builder getBuilder(int cardId) {
		int initialCard = MonthCardActivityCfg.getInitialCard(cardId);
		if (initialCard <= 0) {
			HawkLog.errPrintln("card item to builder failed, cardId: {}, initialCard: {}", cardId, initialCard);
			return null;
		}
		
		MonthCardPB.Builder builder = MonthCardPB.newBuilder();
		builder.setInitialCardId(initialCard);
		builder.setStatus(MonthCardState.UNPURCHASED_VALUE);
		builder.setCardId(cardId);
		int type = MonthCardActivityCfg.getMonthCardType(cardId);
		HawkTuple3< Long, Long, Long> timeLimit = MonthCardActivityCfg.getTimeLimit(type);
		if(timeLimit != null){
			builder.setStartTime(timeLimit.first);
			builder.setStopTime(timeLimit.second);
			builder.setHiddenTime(timeLimit.third);
		}
		return builder;
	}

}
