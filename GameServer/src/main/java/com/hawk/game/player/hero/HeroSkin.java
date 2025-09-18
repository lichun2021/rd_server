package com.hawk.game.player.hero;

import java.util.Arrays;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.config.HeroSkinCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.protocol.Hero.PBHeroSkin;

public class HeroSkin implements SerializJsonStrAble {
	private PlayerHero parent;
	private int cfgId;
	private int step;
	private double luck;
	private boolean unlock;

	public HeroSkin(PlayerHero parent) {
		this.parent = parent;
	}

	public static int[] heroSkinAll(int heroId) {
		return HawkConfigManager.getInstance().getConfigIterator(HeroSkinCfg.class).stream()
				.filter(cfg -> cfg.getHeroId() == heroId)
				.mapToInt(HeroSkinCfg::getSkinId)
				.distinct()
				.toArray();
	}

	public static int getHeroIdBySkinId(int skinId) {
		HeroSkinCfg cfg = HawkConfigManager.getInstance().getCombineConfig(HeroSkinCfg.class, skinId, 1);
		if (cfg == null) {
			return 0;
		}
		return cfg.getHeroId();
	}

	public boolean isUnlock() {
		HeroSkinCfg cfg = getCfg();
		if (cfg.getType() == 1) { // 是活动类型, 需要buff
			StatusDataEntity entity = getSkinBuff(cfg);
			boolean bfalse = entity != null && entity.getEndTime() > HawkTime.getMillisecond();
			unlock = bfalse;
			return bfalse;
		}

		return unlock;
	}

	private StatusDataEntity getSkinBuff(HeroSkinCfg skinCfg) {
		return parent.getParent().getData().getStatusById(skinCfg.getSkinId());
	}

	public PBHeroSkin toPBobj() {
		HeroSkinCfg cfg = getCfg();
		long startTime = 0;
		long endTime = 0;
		int luckVal = 0;
		if (cfg.getType() == 1) { // 是活动类型, 需要buff
			StatusDataEntity entity = getSkinBuff(cfg);
			boolean bfalse = entity != null && entity.getEndTime() > HawkTime.getMillisecond();
			if (bfalse) {
				startTime = entity.getStartTime();
				endTime = entity.getEndTime();
			}
			unlock = bfalse;
		} else {
			luckVal = (int) (10000 * luck + 0.1);
			endTime = Long.MAX_VALUE;
		}

		PBHeroSkin skin = PBHeroSkin.newBuilder()
				.setSkinId(cfgId)
				.setStarTime(startTime)
				.setEndTime(endTime)
				.setLuckVal(luckVal)
				.setStep(step)
				.build();
		return skin;
	}

	@Override
	public final String serializ() {
		Object[] arr = new Object[5];
		arr[0] = cfgId;
		arr[1] = step;
		arr[2] = luck;
		arr[3] = unlock;
		JSONArray array = new JSONArray(Arrays.asList(arr));
		return array.toJSONString();
	}

	@Override
	public final void mergeFrom(String serialiedStr) {
		JSONArray array = JSONArray.parseArray(serialiedStr);
		cfgId = array.getIntValue(0);
		step = array.getIntValue(1);
		luck = array.getDoubleValue(2);
		if (array.size() > 3) {
			unlock = array.getBooleanValue(3);
		}
	}

	public HeroSkinCfg getCfg() {
		return HawkConfigManager.getInstance().getCombineConfig(HeroSkinCfg.class, cfgId, step);
	}

	public HeroSkinCfg getNextStepCfg() {
		return HawkConfigManager.getInstance().getCombineConfig(HeroSkinCfg.class, cfgId, step + 1);
	}

	public PlayerHero getParent() {
		return parent;
	}

	public void setParent(PlayerHero parent) {
		this.parent = parent;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public double getLuck() {
		return luck;
	}

	public void setLuck(double luck) {
		this.luck = luck;
	}

	public void setUnlock(boolean unlock) {
		this.unlock = unlock;
	}

}
