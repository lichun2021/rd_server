package com.hawk.game.player.hero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.HeroSoulLevelCfg;
import com.hawk.game.config.HeroSoulSkillCfg;
import com.hawk.game.config.HeroSoulStageCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.protocol.Hero.PBHeroSoul;

public class SSSSoul implements SerializJsonStrAble {
	private final PlayerHero parent;

	private SSSSoul(PlayerHero parent) {
		this.parent = parent;
	}

	/**hero_soul_level.xml stage , level*/
	private Map<Integer, Integer> soulLevel = new HashMap<>();
	private Set<Integer> soulStage = new HashSet<>();
	private int soulSkill;

	private Map<EffType, Integer> effMap = new HashMap<>();

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		for (Entry<Integer, Integer> ent : soulLevel.entrySet()) {
			int cfgId = soulLevelCfgId(ent.getKey(), ent.getValue());
			array.add(cfgId);
		}
		obj.put("level", array);
		JSONArray stagearray = new JSONArray();
		for (int stage : soulStage) {
			stagearray.add(stage);
		}
		obj.put("stage", stagearray);
		obj.put("skill", soulSkill);

		return obj.toJSONString();
	}

	private void checkSkillUnlock() {
		ConfigIterator<HeroSoulSkillCfg> it = HawkConfigManager.getInstance().getConfigIterator(HeroSoulSkillCfg.class);
		for (HeroSoulSkillCfg cfg : it) {
			if (cfg.getHero() == getParent().getCfgId() && soulStage.size() == cfg.getUnlockStage()) {
				soulSkill = Math.max(0, cfg.getId());
			}
		}
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		if (StringUtils.isEmpty(serialiedStr)) {
			return;
		}
		try {

			JSONObject obj = JSONObject.parseObject(serialiedStr);
			JSONArray array = obj.getJSONArray("level");
			for (Object o : array) {
				HeroSoulLevelCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, Integer.valueOf(o.toString()));
				soulLevel.put(curCfg.getStage(), curCfg.getLevel());
			}

			JSONArray stagearray = obj.getJSONArray("stage");
			for (Object o : stagearray) {
				soulStage.add(Integer.valueOf(o.toString()));
			}
			soulSkill = obj.getIntValue("skill");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public int soulLevelMaxCfgId() {
		int maxCfgId = 0;
		for (Entry<Integer, Integer> ent : soulLevel.entrySet()) {
			int cfgId = soulLevelCfgId(ent.getKey(), ent.getValue());
			maxCfgId = Math.max(maxCfgId, cfgId);
		}
		return maxCfgId;
	}

	// public HeroSoulLevelCfg nexSoulLevel() {
	// int cfgId = soulLevelMaxCfgId();
	// if (cfgId == 0) {
	// return HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, soulLevelCfgId(1, 1));
	// }
	// HeroSoulLevelCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, cfgId);
	// HeroSoulLevelCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, soulLevelCfgId(curCfg.getStage(), curCfg.getLevel() + 1));
	// if (nextCfg == null && soulStage.contains(curCfg.getStage())) { // 要先升级
	// nextCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, soulLevelCfgId(curCfg.getStage() + 1, 1));
	// }
	// return nextCfg;
	// }
	//
	// public HeroSoulStageCfg nextSoulStage() {
	// HeroSoulLevelCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, soulLevelMaxCfgId());
	// HeroSoulLevelCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, soulLevelCfgId(curCfg.getStage(), curCfg.getLevel() + 1));
	// if (nextCfg != null) {// 没有升满
	// return null;
	// }
	//
	// return HawkConfigManager.getInstance().getConfigByKey(HeroSoulStageCfg.class, parent.getCfgId() * 100 + curCfg.getStage());
	// }

	public int soulLevelCfgId(int stage, int level) {
		int cfgId = parent.getCfgId() * 10000 + stage * 100 + level;
		return cfgId;
	}

	public int soulStageCfgId(int stage) {
		return parent.getCfgId() * 100 + stage;
	}

	public PBHeroSoul toPbObj() {
		PBHeroSoul.Builder builder = PBHeroSoul.newBuilder();
		builder.addAllStageUnlock(soulStage);
		if (soulSkill > 0) {
			builder.addSoulSkill(soulSkill);
		}
		for (Entry<Integer, Integer> ent : soulLevel.entrySet()) {
			int cfgId = soulLevelCfgId(ent.getKey(), ent.getValue());
			builder.addSoulLevel(cfgId);
		}
		return builder.build();
	}

	public PlayerHero getParent() {
		return parent;
	}

	public Map<Integer, Integer> getSoulLevel() {
		return soulLevel;
	}

	public void setSoulLevel(Map<Integer, Integer> soulLevel) {
		this.soulLevel = soulLevel;
	}

	public Set<Integer> getSoulStage() {
		return soulStage;
	}

	public void setSoulStage(Set<Integer> soulStage) {
		this.soulStage = soulStage;
	}

	public int getSoulSkill() {
		return soulSkill;
	}

	public void setSoulSkill(int soulSkill) {
		this.soulSkill = soulSkill;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Integer> ent : soulLevel.entrySet()) {
			int cfgId = soulLevelCfgId(ent.getKey(), ent.getValue());
			sb.append(cfgId).append("_");
		}
		sb.append(",");
		for (int stage : soulStage) {
			sb.append(stage).append("_");
		}
		return sb.toString();
	}

	public static SSSSoul create(PlayerHero playerHero) {
		SSSSoul soul = new SSSSoul(playerHero);
		soul.mergeFrom(playerHero.getHeroEntity().getSoulSerialized());
		return soul;
	}

	public List<PBHeroEffect> effectVal() {
		List<PBHeroEffect> result = new ArrayList<>();
		for (Entry<Integer, Integer> ent : soulLevel.entrySet()) {
			int cfgId = soulLevelCfgId(ent.getKey(), ent.getValue());
			HeroSoulLevelCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, cfgId);
			for (Entry<EffType, Integer> eff : curCfg.getEffectList().entrySet()) {
				result.add(PBHeroEffect.newBuilder().setEffectId(eff.getKey().getNumber()).setValue(eff.getValue()).build());
			}
		}
		for (int cfgId : soulStage) {
			HeroSoulStageCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulStageCfg.class, cfgId);
			for (Entry<EffType, Integer> eff : curCfg.getEffectList().entrySet()) {
				result.add(PBHeroEffect.newBuilder().setEffectId(eff.getKey().getNumber()).setValue(eff.getValue()).build());
			}
		}

		checkSkillUnlock();
		HeroSoulSkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulSkillCfg.class, soulSkill);
		if (skillCfg != null) {
			for (Entry<EffType, Integer> eff : skillCfg.getEffectList().entrySet()) {
				result.add(PBHeroEffect.newBuilder().setEffectId(eff.getKey().getNumber()).setValue(eff.getValue()).build());
			}
		}

		effMap = new HashMap<>();
		for (PBHeroEffect effVal : result) {
			EffType type = EffType.valueOf(effVal.getEffectId());
			if (type == null) {
				continue;
			}

			effMap.merge(type, effVal.getValue(), (v1, v2) -> v1 + v2);
		}
		return result;
	}

	public double power() {
		int power = 0;
		for (Entry<Integer, Integer> ent : soulLevel.entrySet()) {
			int cfgId = soulLevelCfgId(ent.getKey(), ent.getValue());
			HeroSoulLevelCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, cfgId);
			power += curCfg.getPower();
		}
		for (int cfgId : soulStage) {
			HeroSoulStageCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulStageCfg.class, cfgId);
			power += curCfg.getPower();
		}
		return power;
	}

	public int getEffVal(EffType eff) {
		return effMap.getOrDefault(eff, 0);
	}

	public void reset() {
		soulLevel.clear();
		soulStage.clear();
		soulSkill = 0;
	}

}
