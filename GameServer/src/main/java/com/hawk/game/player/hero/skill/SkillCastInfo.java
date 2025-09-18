package com.hawk.game.player.hero.skill;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.util.GsConst;

public class SkillCastInfo implements SerializJsonStrAble {
	
	public static SkillCastInfo ProficiencyAuto;
	static{
		ProficiencyAuto = new SkillCastInfo();
		long now = System.currentTimeMillis();
		ProficiencyAuto.castTime = now;
		ProficiencyAuto.coolDown = now + GsConst.DAY_MILLI_SECONDS;
		ProficiencyAuto.effectTime = now + GsConst.DAY_MILLI_SECONDS;
	}
	
	private long coolDown; // 技能冷却时间
	private long castTime; // 释放技能时间
	private long effectTime; // 技能持续时间
	private String marchId;

	public long getCoolDown() {
		return coolDown;
	}

	public void setCoolDown(long coolDown) {
		this.coolDown = coolDown;
	}

	public long getCastTime() {
		return castTime;
	}

	public void setCastTime(long castTime) {
		this.castTime = castTime;
	}

	public long getEffectTime() {
		return effectTime;
	}

	public void setEffectTime(long effectTime) {
		this.effectTime = effectTime;
	}

	public String getMarchId() {
		return marchId;
	}

	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", coolDown);
		obj.put("2", castTime);
		obj.put("3", effectTime);
		obj.put("4", marchId);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		coolDown = obj.getLongValue("1");
		castTime = obj.getLongValue("2");
		effectTime = obj.getLongValue("3");
		marchId = obj.getString("4");
	}

}
