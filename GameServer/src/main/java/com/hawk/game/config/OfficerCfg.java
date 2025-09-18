package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 官职配置
 * 
 * @author
 *
 */
@HawkConfigManager.XmlResource(file = "xml/official_position.xml")
public class OfficerCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 名称
	private final String officeName;
	// 冷却时间
	private final int cdTime;
	// 作用号值
	private final String welfare;
	
	protected final String atkAttr;
	protected final String hpAttr;
	protected final int officeType;
	// 作用号值数组
	private Map<Integer, Integer> effectOfficer;
	private EffType[] effTypes;

	public OfficerCfg() {
		id = 0;
		cdTime = 0;
		welfare = "";
		officeName = "";
		this.atkAttr = "";
		this.hpAttr = "";
		officeType =0;
	}

	public int getId() {
		return id;
	}

	public int getCdTime() {
		return cdTime;
	}

	public String getWelfare() {
		return welfare;
	}

	public int getEffVal(int effId) {
		if (effectOfficer.containsKey(effId)) {
			return effectOfficer.get(effId);
		}
		return 0;
	}

	public EffType[] getEffTypes() {
		return effTypes;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	@Override
	protected boolean assemble() {

		// 作用号数值必须成对出现
		effectOfficer = new HashMap<Integer, Integer>();
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

	@Override
	protected boolean checkValid() {
		if (effectOfficer == null || effectOfficer.size() == 0) {
			return true;
		}

		boolean effectIdCheckResult = effectOfficer.keySet().stream().filter(effectId -> {
			return !EffectCfg.isExistEffectId(effectId);
		}).findAny().isPresent();

		boolean officerCheckResult = OfficerType.valueOf(id) != null;

		return effectIdCheckResult | officerCheckResult;
	}

	public String getOfficeName() {
		return officeName;
	}

	public int getOfficeType() {
		return officeType;
	}
	
}
