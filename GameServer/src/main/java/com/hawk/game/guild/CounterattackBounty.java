package com.hawk.game.guild;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.hero.SerializJsonStrAble;

/**
 * 玩家抽入联盟反击赏金
 * 
 * @author lwt
 * @date 2018年2月9日
 */
public class CounterattackBounty implements SerializJsonStrAble {
	private AwardItems bounty;
	private int upTimes;
	private String playerId;

	public CounterattackBounty(String playerId) {
		this.playerId = playerId;
		this.bounty = AwardItems.valueOf();
	}

	public void addBounty(List<ItemInfo> arg0) {
		this.bounty.addItemInfos(arg0);

	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("bounty", ItemInfo.toString(bounty.getAwardItems()));
		obj.put("upTimes", upTimes);
		obj.put("playerId", playerId);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.bounty = AwardItems.valueOf(obj.getString("bounty"));
		this.upTimes = obj.getIntValue("upTimes");
		this.playerId = obj.getString("playerId");
	}

	public AwardItems getBounty() {
		return bounty;
	}

	public void setBounty(AwardItems bounty) {
		this.bounty = bounty;
	}

	public int getUpTimes() {
		return upTimes;
	}

	public void setUpTimes(int upTimes) {
		this.upTimes = upTimes;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

}
