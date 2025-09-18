package com.hawk.activity.type.impl.joybuy.entity;

import java.util.List;

import com.hawk.game.protocol.Activity.JoyBuyExchangeInfo;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * @author luke
 *兑换基础信息
 */
public class JoyBuyExchangeItem  implements SplitEntity {
	//兑换id
	private int exchangeId;
	//次数
	private int exchangeSingeCurNumber;
	
	public int getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(int exchangeId) {
		this.exchangeId = exchangeId;
	}

	public int getExchangeSingeCurNumber() {
		return exchangeSingeCurNumber;
	}

	public void setExchangeSingeCurNumber(int exchangeSingeCurNumber) {
		this.exchangeSingeCurNumber = exchangeSingeCurNumber;
	}
	public void addExchangeSingeCurNumber(int number) {
		this.exchangeSingeCurNumber=this.exchangeSingeCurNumber+number;
	}

	@Override
	public SplitEntity newInstance() {
		return new JoyBuyExchangeItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(exchangeId);
		dataList.add(exchangeSingeCurNumber);
	}

	@Override
	public void fullData(DataArray dataArray) {
		exchangeId = dataArray.getInt();
		exchangeSingeCurNumber = dataArray.getInt();
	}
	
	public JoyBuyExchangeInfo.Builder totoBuilder(){
		JoyBuyExchangeInfo.Builder builder = JoyBuyExchangeInfo.newBuilder();
		builder.setExchangeId(exchangeId);
		builder.setExchangeSingeCurNumber(exchangeSingeCurNumber);
		return builder;
	}

}
