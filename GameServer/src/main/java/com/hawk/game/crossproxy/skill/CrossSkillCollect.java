package com.hawk.game.crossproxy.skill;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.redis.HawkRedisSession;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.CrossSkillCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Cross.PBCrossSkillSync;
import com.hawk.game.protocol.HP;

public class CrossSkillCollect {
	private Set<EffType> lastSyncEffectInUse = Collections.emptySet();
	private EffType[] effectAll; // 所有技能作用号, 不管生不生效
	private String serverId;
	private Map<String, ICrossSkill> skillMap;
	public String CROSS_SKILL_KEY = "CrossSkillx:";

	private CrossSkillCollect() {
	}

	public static CrossSkillCollect create(String servId, CrossSkillCollect colectOld) {
		CrossSkillCollect result = new CrossSkillCollect();
		result.setServerId(GlobalData.getInstance().getMainServerId(servId));
		result.init();
		if (Objects.nonNull(colectOld)) {
			result.setLastSyncEffectInUse(colectOld.getLastSyncEffectInUse());
		}
		return result;
	}

	public void init() {
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		Map<String, String> dbData = redisSession.hGetAll(redisKey());
		ConfigIterator<CrossSkillCfg> it = HawkConfigManager.getInstance().getConfigIterator(CrossSkillCfg.class);

		Set<EffType> effAll = new HashSet<>();
		Map<String, ICrossSkill> map = new HashMap<>();
		for (CrossSkillCfg cfg : it) {
			ICrossSkill skill = CrossSkillFactory.getInstance().createEmptySkill(cfg.getId());
			skill.setParent(this);
			skill.mergeFrom(dbData.getOrDefault(cfg.getId(), ""));
			map.put(skill.skillID(), skill);
			effAll.addAll(skill.getCfg().getEffectMap().keySet());
		}

		effectAll = effAll.toArray(new EffType[0]);
		skillMap = ImmutableMap.copyOf(map);
	}

	public boolean castSkill(Player player, String skillId, List<String> params) {

		ICrossSkill skillCast = skillMap.get(skillId);
		if (Objects.isNull(skillCast)) {
			return false;
		}
		boolean bfalse = skillCast.cast(player, params);
		if (!bfalse) {
			return false;
		}
		for (ICrossSkill skill : skillMap.values()) {
			if (!skill.isInForce()) {
				continue;
			}
			if (skill.getCfg().isMutexSkill(skillId)) {
				skill.setBuffendTime(0);
				skill.saveRedis();
			}
		}
		syncCrossSkillInfo();
		return true;
	}

	public String redisKey() {
		return CROSS_SKILL_KEY + serverId;
	}

	/**
	 * 检查是否要重新推送
	 */
	public void checkRepush() {
		boolean bfalse = true;
		Set<EffType> toPush = effectInUse();
		Set<EffType> hasPush = this.getLastSyncEffectInUse();
		if (toPush.size() == hasPush.size() && toPush.containsAll(hasPush)) {
			bfalse = false;
		}
		if (bfalse) {
			syncCrossSkillInfo();
		}
	}

	private Set<EffType> effectInUse() {
		Set<EffType> result = new HashSet<>();
		for (ICrossSkill skill : skillMap.values()) {
			result.addAll(skill.getEffectValContinue().keySet());
		}
		return result;
	}

	public void syncCrossSkillInfo() {
		setLastSyncEffectInUse(effectInUse());
		Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : players) {
			syncCrossSkillInfo(player);
			player.getEffect().syncEffect(player, effectAll);
		}
	}

	/**
	 * 司令技能数据. 
	 */
	public void syncCrossSkillInfo(Player player) {
		if (Objects.equals(player.getMainServerId(), serverId)) {

			PBCrossSkillSync.Builder resp = PBCrossSkillSync.newBuilder();
			for (ICrossSkill skill : skillMap.values()) {
				resp.addCrossSkills(skill.toPBObj());
			}

			player.sendProtocol(HawkProtocol.valueOf(HP.code.CROSS_SKILL_SYNC, resp));
		}
	}

	/** 持续生效时间做用号值*/
	public int getEffectValIfContinue(EffType effType) {
		int result = 0;
		try {
			result = skillMap.values().stream().mapToInt(skill -> skill.getEffectValIfContinue(effType)).sum();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return result;
	}

	public String getServerId() {
		return serverId;
	}

	private void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public Set<EffType> getLastSyncEffectInUse() {
		return lastSyncEffectInUse;
	}

	public void setLastSyncEffectInUse(Set<EffType> lastSyncEffect) {
		this.lastSyncEffectInUse = lastSyncEffect;
	}

}
