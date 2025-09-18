package com.hawk.game.module.lianmengfgyl.march.data;

import com.alibaba.fastjson.JSONObject;

/**
 * @author LENOVO
 *
 */
public class FGYLRoomData {

	private String roomId;
	//联盟ID
	private String guildId;
	//报名时间
	private long signTime;
	//报名时段
	private long signBattleIndex;
	//副本难度
	private int fightLevel;
	//配置开始时间
	private long confingStartTime;
	//真实副本创建时间
	private long createTime;
	//真实开始时间
	private long startTime;
	//真实结束时间
	private long endTime;
	//战斗结果 1胜利 0失败
	private int rlt;
	//是否异常结束
	private int err;
	

	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("roomId", this.roomId);
		obj.put("guildId", this.guildId);
		obj.put("signTime", this.signTime);
		obj.put("signBattleIndex", this.signBattleIndex);
		obj.put("fightLevel", this.fightLevel);
		obj.put("confingStartTime", this.confingStartTime);
		obj.put("createTime", this.createTime);
		obj.put("startTime", this.startTime);
		obj.put("endTime", this.endTime);
		obj.put("rlt", this.rlt);
		obj.put("err", this.err);
		return obj.toJSONString();
	}

	public void mergeFrom(String serialiedStr){
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.roomId = obj.getString("roomId");
		this.guildId = obj.getString("guildId");
		this.signTime = obj.getLongValue("signTime");
		this.signBattleIndex = obj.getIntValue("signBattleIndex");
		this.fightLevel = obj.getIntValue("fightLevel");
		this.confingStartTime = obj.getLongValue("confingStartTime");
		this.createTime = obj.getLongValue("createTime");
		this.startTime = obj.getLongValue("startTime");
		this.endTime = obj.getLongValue("endTime");
		this.rlt = obj.getIntValue("rlt");
		this.err =  obj.getIntValue("err");
	}
	
	
	

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public long getSignTime() {
		return signTime;
	}

	public void setSignTime(long signTime) {
		this.signTime = signTime;
	}

	public long getSignBattleIndex() {
		return signBattleIndex;
	}

	public void setSignBattleIndex(long signBattleIndex) {
		this.signBattleIndex = signBattleIndex;
	}

	public int getFightLevel() {
		return fightLevel;
	}

	public void setFightLevel(int fightLevel) {
		this.fightLevel = fightLevel;
	}
	
	public long getConfingStartTime() {
		return confingStartTime;
	}
	
	public void setConfingStartTime(long confingStartTime) {
		this.confingStartTime = confingStartTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getRlt() {
		return rlt;
	}

	public void setRlt(int rlt) {
		this.rlt = rlt;
	}

	public int getErr() {
		return err;
	}
	
	public void setErr(int err) {
		this.err = err;
	}

}
