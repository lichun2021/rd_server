package com.hawk.game.entity;

import java.util.List;

import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.config.AgencyConstCfg;
import com.hawk.game.protocol.Agency.AgencyEventState;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;
 
/**
 * 情报中心事件
 * 
 * @author Golden
 *
 */
public class AgencyEventEntity implements SplitEntity {

	/**
	 * 事件唯一标识
	 */
	private String uuid;
	
	/**
	 * 事件id
	 */
	private int eventId;
	
	/**
	 * 开始时间
	 */
	private long startTime;
	
	/**
	 * 事件状态 未完成/已完成
	 */
	private int eventState;
	
	/**
	 * 野怪坐标X
	 */
	private int posX;
	
	/**
	 * 野怪坐标Y
	 */
	private int posY;
	
	/**
	 * 是否是特殊事件
	 */
	private int isSpecialEvent;
	
	/**
	 * 是否是道具事件
	 */
	private int isItemEvent;
	
	public AgencyEventEntity() {
		
	}
	
	@Override
	public SplitEntity newInstance() {
		return new AgencyEventEntity();
	}
	
	/**
	 * 构造
	 * @param eventId
	 * @param posX
	 * @param posY
	 */
	public AgencyEventEntity(int eventId, int posX, int posY) {
		this.uuid = HawkUUIDGenerator.genUUID();
		this.eventId = eventId;
		this.startTime = HawkTime.getMillisecond();
		this.eventState = AgencyEventState.AGENCY_NOT_FINISH_VALUE;
		this.posX = posX;
		this.posY = posY;
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(uuid);
		dataList.add(eventId);
		dataList.add(startTime);
		dataList.add(eventState);
		dataList.add(posX);
		dataList.add(posY);
		dataList.add(isSpecialEvent);
		dataList.add(isItemEvent);
	}
	
	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(8);
		uuid = dataArray.getString();
		eventId = dataArray.getInt();
		startTime = dataArray.getLong();
		eventState = dataArray.getInt();
		posX = dataArray.getInt();
		posY = dataArray.getInt();
		isSpecialEvent = dataArray.getInt();
		isItemEvent = dataArray.getInt();
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getEventState() {
		return eventState;
	}

	public void setEventState(int eventState) {
		this.eventState = eventState;
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}
	
	public int getPointId() {
		return GameUtil.combineXAndY(posX, posY);
	}

	public int getIsSpecialEvent() {
		return isSpecialEvent;
	}

	public void setIsSpecialEvent(int isSpecialEvent) {
		this.isSpecialEvent = isSpecialEvent;
	}

	public int getIsItemEvent() {
		return isItemEvent;
	}

	public void setIsItemEvent(int isItemEvent) {
		this.isItemEvent = isItemEvent;
	}
	
	
	
	/**
	 * 获取事件结束时间
	 * @param event
	 * @return
	 */
	public long getEventEndTime() {
		long agencyEndTime = this.getStartTime() + AgencyConstCfg.getInstance().getEventDisappearTime(); 
		if (this.getIsSpecialEvent() == 1 || this.getIsItemEvent() == 1) {
			agencyEndTime = Long.MAX_VALUE;
		}
		return agencyEndTime;
	}
	
}
