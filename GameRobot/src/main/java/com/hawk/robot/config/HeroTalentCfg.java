package com.hawk.robot.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * 天赋功能配置
 * 
 * @author
 *
 */
@HawkConfigManager.XmlResource(file = "xml/talent.xml")
public class HeroTalentCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final String frontTalent; //解锁前置条件
	
	protected final int heroLevel;
	
	private List<String> frontTalents;
	
	protected final int owner;
	
	public HeroTalentCfg() {
		id = 0;
		frontTalent = "";
		owner = 0;
		heroLevel = 0;
	}
	
	public List<String> getFrontTalents() {
		return frontTalents;
	}

	public int getId() {
		return id;
	}

	public String getFrontTalent() {
		return frontTalent;
	}

	public int getOwner() {
		return owner;
	}

	public int getHeroLevel() {
		return heroLevel;
	}

	@Override
	protected boolean assemble() {
		/**请保证集合非null,且不可变*/
		frontTalents = Splitter.on("|").omitEmptyStrings().trimResults().splitToList(frontTalent);
		frontTalents = ImmutableList.copyOf(frontTalents);
		return true;
	}
}
