package com.hawk.game.guild;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;

/**
 * 联盟小礼包. 保存24小时(配置)
 * 
 * @author lwt
 * @date 2018年3月20日
 */
public class GuildSmailGift implements SerializJsonStrAble {
	private int itemId;
	private long createTime;
	private long overTime;
	private String reward = ""; // 指定领取奖励.

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("a", itemId);
		obj.put("b", createTime);
		obj.put("c", overTime);
		obj.put("d", reward);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.itemId = obj.getIntValue("a");
		this.createTime = obj.getLongValue("b");
		this.overTime = obj.getLongValue("c");
		this.reward = obj.getString("d");

	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

}
