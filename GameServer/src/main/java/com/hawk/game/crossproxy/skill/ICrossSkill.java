package com.hawk.game.crossproxy.skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.CrossSkillCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.player.skill.ISkill;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Cross.PBCrossSkill;

/** @author lwt
 * @date 2017年7月26日 */
public abstract class ICrossSkill implements ISkill, SerializJsonStrAble {
	private CrossSkillCollect parent;
	private long coolDown; //
	private long buffendTime; // buff结束时间 期间内为持续生效中

	public ICrossSkill() {
	}

	/** 配置ID */
	public String skillID() {
		return getClass().getAnnotation(CrossSkill.class).skillID();
	}

	public CrossSkillCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(CrossSkillCfg.class, skillID());
	}

	public int getEffectVal(EffType effType) {
		return getCfg().getEffectVal(effType);
	}

	/** 生效中的作用号 */
	public Map<EffType, Integer> getEffectValContinue() {
		Map<EffType, Integer> result = new HashMap<>();
		Set<EffType> set = getCfg().getEffectMap().keySet();
		for (EffType ef : set) {
			int val = getEffectValIfContinue(ef);
			if (val > 0) {
				result.put(ef, val);
			}
		}
		return result;
	}

	/** 持续生效时间做用号值*/
	public int getEffectValIfContinue(EffType effType) {
		if (isInForce()) {
			return getEffectVal(effType);
		}
		return 0;
	}

	/**
	 * 持续生效中
	 * @return
	 */
	public boolean isInForce() {
		return HawkTime.getMillisecond() < buffendTime;
	}

	public long getCoolDown() {
		return coolDown;
	}

	public void setCoolDown(long coolDown) {
		this.coolDown = coolDown;
	}

	public long getBuffendTime() {
		return buffendTime;
	}

	public void setBuffendTime(long buffendTime) {
		this.buffendTime = buffendTime;
	}

	public CrossSkillCollect getParent() {
		return parent;
	}

	public void setParent(CrossSkillCollect parent) {
		this.parent = parent;
	}

	public PBCrossSkill toPBObj() {
		PBCrossSkill.Builder builder = PBCrossSkill.newBuilder();
		builder.setSkillId(skillID());
		builder.setBuffendTime(buffendTime);
		builder.setCoolDown(coolDown);
		return builder.build();
	}

	/** 序列化保存 */
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("id", skillID());
		obj.put("a", getCoolDown());
		obj.put("b", getBuffendTime());

		return obj.toJSONString();
	}

	/** 反序列化
	 * 
	 * @param serialiedStr */
	@Override
	public void mergeFrom(String serialiedStr) {
		if (StringUtils.isNotEmpty(serialiedStr)) {
			JSONObject obj = JSONObject.parseObject(serialiedStr);
			setCoolDown(obj.getLongValue("a"));
			setBuffendTime(obj.getLongValue("b"));
		}
	}

	public boolean cast(Player player, List<String> params) {
		long nowTime = HawkTime.getMillisecond();
		if (nowTime < getCoolDown()) {
			return false;
		}

		CrossSkillCfg cfg = getCfg();
		long coolDown = nowTime + cfg.getCrossCd() * 1000;
		long buffendTime = nowTime + cfg.getContinueTime() * 1000;

		setCoolDown(coolDown);
		setBuffendTime(buffendTime);

		saveRedis();

		return true;
	}

	public void saveRedis() {
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		redisSession.hSet(getParent().redisKey(), skillID(), serializ());
	}

}
