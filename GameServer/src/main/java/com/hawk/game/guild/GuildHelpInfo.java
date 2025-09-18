package com.hawk.game.guild;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;

/**
 * 联盟帮助 本类把过多东西强行揉在一起. 后续如有必要请根据具体<队列>拆分为不同子类. 并提供help()接口 方便处理相关<队列>,而不是if
 * (queueType =123) else...
 * 
 * @author lwt
 * @date 2018年2月12日
 */
public class GuildHelpInfo implements SerializJsonStrAble {
	private String playerId;
	private int playerIcon;
	private QueueType queueType;
	private int itemId;
	private int count;
	private long buildTime;
	private QueueStatus queueStatus;
	private long totalDisTime;
	private List<String> helpers;
	private List<String> parames;// 额外参数
	private int multiply;  //一键升X级
	
	
	public GuildHelpInfo() {
		this.helpers = new ArrayList<>();
		this.parames = new ArrayList<>();
	}

	public static GuildHelpInfo valueOf(String jsonStr) {
		GuildHelpInfo result = new GuildHelpInfo();
		result.mergeFrom(jsonStr);
		return result;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("playerId", playerId);
		obj.put("playerIcon", playerIcon);
		obj.put("queueType", queueType);
		obj.put("itemId", itemId);
		obj.put("count", count);
		obj.put("buildTime", buildTime);
		obj.put("queueStatus", queueStatus);
		obj.put("totalDisTime", totalDisTime);
		obj.put("helpers", JSON.toJSON(helpers));
		obj.put("parames", JSON.toJSON(parames));
		obj.put("multiply", multiply);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.playerId = obj.getString("playerId");
		this.playerIcon = obj.getIntValue("playerIcon");
		this.queueType = obj.getObject("queueType", QueueType.class);
		this.itemId = obj.getIntValue("itemId");
		this.count = obj.getIntValue("count");
		this.buildTime = obj.getLongValue("buildTime");
		this.queueStatus = obj.getObject("queueStatus", QueueStatus.class);
		this.totalDisTime = obj.getLongValue("totalDisTime");
		obj.getJSONArray("helpers").stream().map(Object::toString).forEach(this.helpers::add);
		obj.getJSONArray("parames").stream().map(Object::toString).forEach(this.parames::add);
		if(obj.containsKey("multiply")){
			this.multiply = obj.getIntValue("multiply");
		}
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getPlayerIcon() {
		return playerIcon;
	}

	public void setPlayerIcon(int playerIcon) {
		this.playerIcon = playerIcon;
	}

	public QueueType getQueueType() {
		return queueType;
	}

	public void setQueueType(QueueType queueType) {
		this.queueType = queueType;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getBuildTime() {
		return buildTime;
	}

	public void setBuildTime(long buildTime) {
		this.buildTime = buildTime;
	}

	public QueueStatus getQueueStatus() {
		return queueStatus;
	}

	public void setQueueStatus(QueueStatus queueStatus) {
		this.queueStatus = queueStatus;
	}

	public long getTotalDisTime() {
		return totalDisTime;
	}

	public void setTotalDisTime(long totalDisTime) {
		this.totalDisTime = totalDisTime;
	}

	public List<String> getHelpers() {
		return helpers;
	}

	public void setHelpers(List<String> helpers) {
		this.helpers = helpers;
	}

	public List<String> getParames() {
		return parames;
	}

	public void setParames(List<String> parames) {
		this.parames = parames;
	}
	
	public int getMultiply() {
		return multiply;
	}
	
	public void setMultiply(int multiply) {
		this.multiply = multiply;
	}

	public int getCurHelpCount(){
		int hcount = this.helpers.size();
		if(this.multiply > 1){
			hcount *= this.multiply;
		}
		return hcount;
	}
}
