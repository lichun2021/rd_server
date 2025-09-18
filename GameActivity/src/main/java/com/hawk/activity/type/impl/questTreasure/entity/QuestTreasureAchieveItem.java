package com.hawk.activity.type.impl.questTreasure.entity;

import java.util.List;

import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

public class QuestTreasureAchieveItem extends AchieveItem implements SplitEntity {

	
	private int turn;
	
	
	@Override
	public QuestTreasureAchieveItem newInstance() {
		return new QuestTreasureAchieveItem();
	}
	
	public int getTurn() {
		return turn;
	}
	
	public void setTurn(int turn) {
		this.turn = turn;
	}
	
	
	@Override
	public void serializeData(List<Object> dataList) {
		super.serializeData(dataList);
		dataList.add(this.turn);
	}
	
	
	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		int achieveId = dataArray.getInt();
		int state = dataArray.getInt();
		String achieveDataStr = dataArray.getString();
		List<Integer> dataList = SerializeHelper.stringToList(Integer.class, achieveDataStr, SerializeHelper.BETWEEN_ITEMS);
		int achieveTurn = dataArray.getInt();
		
		this.setAchieveId(achieveId);
		this.setState(state);
		this.setDataList(dataList);
		this.setTurn(achieveTurn);
	}
	
	
	
	public static QuestTreasureAchieveItem valueOf(int achieveId,int turn) {
		QuestTreasureAchieveItem data = new QuestTreasureAchieveItem();
		data.setAchieveId(achieveId);
		data.setState(AchieveState.NOT_ACHIEVE_VALUE);
		data.setTurn(turn);
		return data;
	}
}
