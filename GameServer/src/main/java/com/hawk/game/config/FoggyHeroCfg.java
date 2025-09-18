package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.player.hero.NPCHeroFactory;

/**
 *
 * @author zhenyu.shang
 * @since 2018年2月22日
 */
@HawkConfigManager.XmlResource(file = "xml/foggy_hero.xml")
public class FoggyHeroCfg extends HawkConfigBase {

	@Id
	protected final int id;

	protected final int heroId;

	protected final int level;

	protected final int starLevel;

	protected final String heroSkill;// ="2101001_5|2101002_5|2101003_5";

	private ImmutableList<HawkTuple2<Integer, Integer>> skillList;

	public FoggyHeroCfg() {
		this.id = 0;
		this.heroId = 0;
		this.level = 0;
		this.starLevel = 0;
		this.heroSkill = "";
	}

	public int getId() {
		return id;
	}

	public int getHeroId() {
		return heroId;
	}

	public int getLevel() {
		return level;
	}

	public int getStarLevel() {
		return starLevel;
	}

	/** first:skillId,second:level */
	public ImmutableList<HawkTuple2<Integer, Integer>> getSkillList() {
		return skillList;
	}

	public void setSkillList(ImmutableList<HawkTuple2<Integer, Integer>> skillList) {
		this.skillList = skillList;
	}

	public String getHeroSkill() {
		return heroSkill;
	}

	@Override
	protected boolean assemble() {
		{
			List<HawkTuple2<Integer, Integer>> list = new ArrayList<>();
			Splitter.on("|").omitEmptyStrings().trimResults().split(heroSkill).forEach(str -> {
				String[] arr = str.split("_");
				HawkTuple2<Integer, Integer> slot = HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toInt(arr[1]));
				list.add(slot);
			});
			this.skillList = ImmutableList.copyOf(list);
		}
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		{
			HeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, heroId);
			HawkAssert.notNull(cfg, "cfg error unknow heroId " + heroId);
		}

		for (HawkTuple2<Integer, Integer> skillId_lv : skillList) {
			HeroSkillCfg skill = HawkConfigManager.getInstance().getConfigByKey(HeroSkillCfg.class, skillId_lv.first);
			HawkAssert.notNull(skill, "cfg error unknow heroId " + heroId);
		}
		
		NPCHeroFactory.getInstance().get(id);
		return super.checkValid();
	}
}
