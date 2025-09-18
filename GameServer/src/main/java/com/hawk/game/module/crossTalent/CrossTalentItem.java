package com.hawk.game.module.crossTalent;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;

/**
 * 天赋实体对象
 *
 * @author
 */
public class CrossTalentItem implements SerializJsonStrAble {
	private int talentId = 0;

	private int level = 0;

	private int type = 0;

	public int getTalentId() {
		return talentId;
	}

	public void setTalentId(int talentId) {
		this.talentId = talentId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/** 序列化保存 */
	@Override
	public final String serializ() {
		JSONObject result = new JSONObject();
		result.put("talentId", talentId);
		result.put("level", level);
		result.put("type", type);
		return result.toJSONString();
	}

	/** 反序列化
	 * 
	 * @param serialiedStr */
	@Override
	public final void mergeFrom(String serialiedStr) {
		JSONObject result = JSONObject.parseObject(serialiedStr);
		this.talentId = result.getIntValue("talentId");
		this.level = result.getIntValue("level");
		this.type = result.getIntValue("type");
	}

}
