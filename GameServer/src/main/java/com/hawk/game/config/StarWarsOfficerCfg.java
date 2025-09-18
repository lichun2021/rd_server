package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.Const.EffType;

import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/star_wars_officer.xml")
public class StarWarsOfficerCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 名称
	private final String officeName;
	// 冷却时间
	private final int cdTime;
	// 作用号值
	private final String welfare;		
	/**
	 *1 统帅, 2全军统帅， 3 霸主.
	 */
	private final int level;	

	/**
	 * 作用号。
	 */
	private Map<Integer, Integer> effectOfficer;
	/**
	 * effType
	 */
	private EffType[] effTypes;
	
	public StarWarsOfficerCfg() {
		this.id = 0;
		this.officeName = "";
		this.cdTime = 0;
		this.welfare = "";		
		this.level = 0;
	}
	
	public int getId() {
		return id;
	}
	public String getOfficeName() {
		return officeName;
	}
	public int getCdTime() {
		return cdTime;
	}
	public String getWelfare() {
		return welfare;
	}
	
	public boolean assemble() {
		// 作用号数值必须成对出现
		effectOfficer = new HashMap<Integer, Integer>();
		effTypes = new EffType[0];
		if (!HawkOSOperator.isEmptyString(welfare)) {
			String[] array = welfare.split(",");
			for (String val : array) {
				String[] eff = val.split("_");
				if (eff == null || eff.length != 2) {
					return false;
				}
				effectOfficer.put(Integer.valueOf(eff[0]), Integer.valueOf(eff[1]));
			}
			
			int i = 0;
			Set<Integer> effIdSet = effectOfficer.keySet();
			effTypes = new EffType[effectOfficer.size()];
			for (int effId : effIdSet) {
				effTypes[i++] = EffType.valueOf(effId);
			}
		}

		return true;
	}
	
	public int getEffVal(int effId) {
		if (effectOfficer.containsKey(effId)) {
			return effectOfficer.get(effId);
		}
		return 0;
	}

	public Map<Integer, Integer> getEffectOfficer() {
		return effectOfficer;
	}

	public EffType[] getEffTypes() {
		return effTypes;
	}
	
	public int getLevel() {
		return level;
	}
}
