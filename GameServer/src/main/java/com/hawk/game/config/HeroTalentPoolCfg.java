package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkRandObj;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.player.hero.PlayerHero;

@HawkConfigManager.XmlResource(file = "xml/hero_talent_pool.xml")
public class HeroTalentPoolCfg extends HawkConfigBase {
	@Id
	protected final int id;
	protected final String talentList;// ="100101_50,100201_100,100301_300"
	protected final int type;
	protected final int hero;
	protected final String name;
	protected final int parentPoolId;
	private List<TalentWeightObj> talentTWList;

	public HeroTalentPoolCfg() {
		this.id = 0;
		this.talentList = "";
		type = 0;
		hero = 0;
		name = "";
		parentPoolId = 0;
	}

	static class TalentWeightObj implements HawkRandObj {
		int talentId;
		int talentType;
		int weight;

		@Override
		public int getWeight() {
			return weight;
		}

	}

	/** 随机池的天赋全部包含在内 */
	private boolean containsAll(HeroTalentPoolCfg cfg) {
		Set<Integer> talentSet = talentTWList.stream().map(obj -> obj.talentId).collect(Collectors.toSet());
		Set<Integer> cfgSet = cfg.talentTWList.stream().map(obj -> obj.talentId).collect(Collectors.toSet());
		return talentSet.containsAll(cfgSet);
	}

	@Override
	protected boolean assemble() {
		HawkConfigManager.getInstance().makesureCfg(HeroTalentCfg.class);
		List<TalentWeightObj> list = new ArrayList<>();
		Iterable<String> tlist = Splitter.on(",").omitEmptyStrings().trimResults().split(talentList);
		for (String str : tlist) {
			String[] arr = str.split("_");
			int talentId = NumberUtils.toInt(arr[0]);
			int weight = NumberUtils.toInt(arr[1]);
			HeroTalentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroTalentCfg.class, talentId);
			// talentId_type_weight
			TalentWeightObj slot = new TalentWeightObj();
			slot.talentId = talentId;
			slot.talentType = cfg.getTalentType();
			slot.weight = weight;
			list.add(slot);
		}
		this.talentTWList = ImmutableList.copyOf(list);
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		
		if (hero > 0) {// 专属英雄的小池子
			HeroCfg heroCfg = HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, hero);
			if (heroCfg.getTalentList() != parentPoolId) {
				throw new RuntimeException("HeroTalentPoolCfg parent pool error poolId = "+id);
			}
			HeroTalentPoolCfg poolDefaultCfg = HawkConfigManager.getInstance().getConfigByKey(HeroTalentPoolCfg.class, heroCfg.getTalentList());
			if (!poolDefaultCfg.containsAll(this)) {
				throw new RuntimeException("HeroTalentPoolCfg error poolId = " + id);
			}
		}
		
		return super.checkValid();
	}

	public int randomTalent(PlayerHero hero, int index) {
		Set<Integer> set = hero.getTalentSlots().stream()
				.filter(slot -> slot.getTalent() != null)
				.filter(slot -> slot.getIndex() != index)
				.map(slot -> slot.getTalent().getCfg().getTalentType())
				.collect(Collectors.toSet());

		List<TalentWeightObj> rlist = talentTWList.stream()
				.filter(wb -> !set.contains(wb.talentType)).collect(Collectors.toList());

		TalentWeightObj randomWeightObject = HawkRand.randomWeightObject(rlist);
		return randomWeightObject.talentId;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getHero() {
		return hero;
	}

	public String getName() {
		return name;
	}

}
