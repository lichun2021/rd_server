package com.hawk.game.crossactivity;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 跨服礼包记录
 * @author Golden
 *
 */
public class CrossGiftRecord implements SplitEntity {

	/**
	 * 颁发者玩家名字
	 */
	private String sendPlayerName;
	
	/**
	 * 接收者联盟简称
	 */
	private String receiveGuildTag;
	
	/**
	 * 接受者玩家名字
	 */
	private String receivePlayerName;
	
	/**
	 * 礼包id
	 */
	private int giftId;

	public CrossGiftRecord() {
		
	}
	
	@Override
	public SplitEntity newInstance() {
		return new CrossGiftRecord();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(sendPlayerName);
		dataList.add(receiveGuildTag);
		dataList.add(receivePlayerName);
		
	}

	@Override
	public void fullData(DataArray dataArray) {
		// TODO Auto-generated method stub
		
	}
}
