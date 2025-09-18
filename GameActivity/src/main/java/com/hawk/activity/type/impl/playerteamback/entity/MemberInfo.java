package com.hawk.activity.type.impl.playerteamback.entity;

import java.util.List;
import org.hibernate.util.StringHelper;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 战队成员信息数据
 * 
 * @author lating
 *
 */
public class MemberInfo implements SplitEntity {
	/**
	 * 账号ID
	 */
	private String openId;
	
	/** 平台ID */
	private int platId;
	
	/** 区服ID */
	private String serverId;
	
	/** 回流标识： 0非回流， 1回流   */
	private int backFlag;
	
	/**
	 * 加入战队的时间
	 */
	private int groupTs;
	
	public MemberInfo() {
	}
	
	public static MemberInfo valueOf(String openId, int platId, String serverId, int backFlag, int groupTs) {
		MemberInfo data = new MemberInfo();
		data.openId = openId;
		data.platId = platId;
		data.serverId = serverId;
		data.backFlag = backFlag;
		data.groupTs = groupTs;
		return data;
	}
	
	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public int getPlatId() {
		return platId;
	}

	public void setPlatId(int platId) {
		this.platId = platId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getBackFlag() {
		return backFlag;
	}

	public void setBackFlag(int backFlag) {
		this.backFlag = backFlag;
	}
	
	public int getGroupTs() {
		return groupTs;
	}

	public void setGroupTs(int groupTs) {
		this.groupTs = groupTs;
	}

	@Override
	public SplitEntity newInstance() {
		return new MemberInfo();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(openId);
		dataList.add(platId);
		dataList.add(serverId);
		dataList.add(backFlag);
		dataList.add(groupTs);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		openId = dataArray.getString();
		platId = dataArray.getInt();
		serverId = dataArray.getString();
		backFlag = dataArray.getInt();
		groupTs = dataArray.getInt();
	}

	@Override
	public String toString() {
		return "[openId=" + openId + ", platId=" + platId + ", serverId=" + serverId + ", backFlag=" + backFlag + "]";
	}
	
	public static String serializeToString(MemberInfo info){
		return String.format("%s_%d_%s_%d_%d", info.getOpenId(), info.getPlatId(), info.getServerId(), info.getBackFlag(), info.getGroupTs());
	}
	
	public static void parseFromString(MemberInfo info, String d){
		String[] strAry = StringHelper.split("_", d);
		if(strAry.length == 5){
			info.setOpenId(strAry[0]);
			info.setPlatId(Integer.valueOf(strAry[1]));
			info.setServerId(strAry[2]);
			info.setBackFlag(Integer.valueOf(strAry[3]));
			info.setGroupTs(Integer.valueOf(strAry[4]));
		}
	}
	
}
