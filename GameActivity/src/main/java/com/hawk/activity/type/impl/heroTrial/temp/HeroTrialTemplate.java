package com.hawk.activity.type.impl.heroTrial.temp;

import java.util.List;

import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 英雄试炼
 * @author golden
 *
 */
public class HeroTrialTemplate implements SplitEntity {

	private String uuid;
	
	/**
	 * 任务id
	 */
	private int missionId;
	
	/**
	 * 接受任务时间
	 */
	private long receiveTime;
	
	/**
	 * 试炼英雄
	 */
	private String heros;
	
	/**
	 * 是否进阶
	 */
	private int hasAdvanced;
	
	public HeroTrialTemplate() {
		
	}
	
	public HeroTrialTemplate(String uuid, int missionId) {
		this.uuid = uuid;
		this.missionId = missionId;
		this.receiveTime = 0L;
		this.heros = "";
		this.hasAdvanced = 0;
	}
	
	public HeroTrialTemplate(String uuid, int missionId, long receiveTime, List<Integer> heros, boolean hasAdvanced) {
		this.uuid = uuid;
		this.missionId = missionId;
		this.receiveTime = receiveTime;
		this.heros = genHeroStr(heros);
		this.hasAdvanced = (hasAdvanced ? 1 : 0);
	}
	
	/**
	 * 生成英雄字符串
	 */
	public String genHeroStr(List<Integer> heros) {
		return SerializeHelper.collectionToString(heros, SerializeHelper.BETWEEN_ITEMS);
	}
	
	/**
	 * 获取英雄列表
	 */
	public List<Integer> getHeroList() {
		return SerializeHelper.stringToList(Integer.class, heros, SerializeHelper.BETWEEN_ITEMS);
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getMissionId() {
		return missionId;
	}

	public void setMissionId(int missionId) {
		this.missionId = missionId;
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getHeros() {
		return heros;
	}

	public void setHeros(String heros) {
		this.heros = heros;
	}

	public void setTrialHeros(List<Integer> heros) {
		this.heros = genHeroStr(heros);
	}
	
	public boolean isHasAdvanced() {
		return hasAdvanced != 0;
	}

	public void setHasAdvanced(boolean hasAdvanced) {
		this.hasAdvanced = hasAdvanced ? 1 : 0;
	}

	@Override
	public SplitEntity newInstance() {
		return new HeroTrialTemplate(HawkUUIDGenerator.genUUID(), 0);
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(uuid);
		dataList.add(missionId);
		dataList.add(receiveTime);
		dataList.add(heros);
		dataList.add(hasAdvanced);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		setUuid(dataArray.getString());
		setMissionId(dataArray.getInt());
		setReceiveTime(dataArray.getLong());
		setHeros(dataArray.getString());
		setHasAdvanced(dataArray.getInt() != 0);
	}
}
