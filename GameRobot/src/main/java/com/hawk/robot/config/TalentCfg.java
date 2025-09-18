package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class TalentCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final String frontTalent; //解锁前置条件
	
	protected final int heroLevel;
	
	private List<String> frontTalents;
	
	protected final int owner;
	
	private static List<Integer> defaultUnlockIds = new ArrayList<>();
	
	private static Map<Integer, Integer> frontTalentMap = new HashMap<>();
	
	public TalentCfg() {
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
		if (frontTalents.isEmpty()) {
			defaultUnlockIds.add(id);
		}
		
		for (String frontTalent : frontTalents) {
			String[] strs = frontTalent.trim().split("_"); 
			frontTalentMap.put(Integer.valueOf(strs[0]), id);
		}
		
		return true;
	}
	
	public static List<Integer> getDefaultUnlockedIds() {
		return defaultUnlockIds;
	}
	
	public static Integer getTalentIdByFront(int frontId) {
		return frontTalentMap.get(frontId);
	}
}
