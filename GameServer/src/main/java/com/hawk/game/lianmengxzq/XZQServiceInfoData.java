package com.hawk.game.lianmengxzq;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.XZQ.PBXZQStage;
import com.hawk.game.protocol.XZQ.PBXZQStatus;
import com.hawk.game.util.GameUtil;

public class XZQServiceInfoData implements SerializJsonStrAble{
	
	// 小战区期数
	private int termId;
	// 小战区当前所处状态
	private PBXZQStatus state = PBXZQStatus.XZQ_HIDDEN;
	
	

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", state.getNumber());
		obj.put("2", termId);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.state = PBXZQStatus.valueOf(obj.getIntValue("1"));
		this.termId = obj.getIntValue("2");
		
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public PBXZQStatus getState() {
		return state;
	}

	public void setState(PBXZQStatus state) {
		this.state = state;
	}
	
	
	
	/**
	 * 获取战区阶段
	 * @return
	 */
	public PBXZQStage getXZQStage(){
		long serverOpenTime = GameUtil.getServerOpenTime();
		if(serverOpenTime <= XZQConstCfg.getInstance().getXzqOpenTimeValue()){
			return PBXZQStage.XZQ_CIRCLE;
		}
		int serverOpenDays = GameUtil.getServerOpenDays(); // 开服几天
		if (serverOpenDays <= XZQConstCfg.getInstance().getOpenTimeDays()) {
			return PBXZQStage.XZQ_CHOOSE;
		}
		return PBXZQStage.XZQ_CIRCLE;
	}
	
	

	
	
	
}
