package com.hawk.game.item;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.game.config.WarFlagConstProperty;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 战旗报名信息
 * @author golden
 *
 */
public class WarFlagSignUpItem implements SplitEntity {

	public String playerId;
	
	public long signUpTime;
	
	public List<String> box;
	
	public int specialBoxCount;
	
	public long nextTickTime;
	
	public static WarFlagSignUpItem valueOf(String playerId) {
		WarFlagSignUpItem signUpInfo = new WarFlagSignUpItem();
		signUpInfo.setPlayerId(playerId);
		long currentTime = HawkTime.getMillisecond();
		signUpInfo.setSignUpTime(currentTime);
		signUpInfo.setBox(new ArrayList<>());
		signUpInfo.setSpecialBoxCount(0);
		signUpInfo.setNextTickTime(currentTime + WarFlagConstProperty.getInstance().getBigFlagCellsTickTime());
		return signUpInfo;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new WarFlagSignUpItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(playerId);
		dataList.add(signUpTime);
		dataList.add(SerializeHelper.collectionToString(box, SerializeHelper.COLON_ITEMS));
		dataList.add(specialBoxCount);
		dataList.add(nextTickTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		playerId = dataArray.getString();
		signUpTime = dataArray.getLong();
		String boxStr = dataArray.getString();
		box = SerializeHelper.stringToList(String.class, boxStr, SerializeHelper.COLON_ITEMS);
		specialBoxCount = dataArray.getInt();
		nextTickTime = dataArray.getLong();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getSignUpTime() {
		return signUpTime;
	}

	public void setSignUpTime(long signUpTime) {
		this.signUpTime = signUpTime;
	}

	public List<String> getBox() {
		return box;
	}

	public void setBox(List<String> box) {
		this.box = box;
	}

	public int getSpecialBoxCount() {
		return specialBoxCount;
	}

	public void setSpecialBoxCount(int specialBoxCount) {
		this.specialBoxCount = specialBoxCount;
	}

	public long getNextTickTime() {
		return nextTickTime;
	}

	public void setNextTickTime(long nextTickTime) {
		this.nextTickTime = nextTickTime;
	}
}
