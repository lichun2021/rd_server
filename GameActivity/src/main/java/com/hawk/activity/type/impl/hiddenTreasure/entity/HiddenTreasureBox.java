package com.hawk.activity.type.impl.hiddenTreasure.entity;

import com.alibaba.fastjson.JSONObject;

public class HiddenTreasureBox  {
	private int poolCfgId;
	private boolean open; // 是否打开

	public int getPoolCfgId() {
		return poolCfgId;
	}

	public void setPoolCfgId(int poolCfgId) {
		this.poolCfgId = poolCfgId;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}


	/** 序列化 */
	public String serializ() {
		JSONObject result = new JSONObject();
		result.put("poolCfgId", poolCfgId);
		result.put("open", open);
		return result.toJSONString();
	}

	public void mergeFrom(String jsonstr) {

		JSONObject result = JSONObject.parseObject(jsonstr);
		this.poolCfgId = result.getIntValue("poolCfgId");
		this.open = result.getBooleanValue("open");
	}

	

}
