package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 联盟官员配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_officials_buff.xml")
public class AllianceOfficialCfg extends HawkConfigBase {
	@Id
	protected final int officeId;
	
	protected final int canAppoint;
	
	protected final String buff;
	
	protected final String atkAttr;
	protected final String hpAttr;
	
	/** 作用号集合 */
	private List<int[]> effects;

	public AllianceOfficialCfg() {
		this.officeId = 0;
		this.canAppoint = 0;
		this.buff = "";
		this.atkAttr = "";
		this.hpAttr = "";
	}

	public int getId() {
		return officeId;
	}
	
	public int getOfficeId() {
		return officeId;
	}

	public boolean canAppoint() {
		return canAppoint == 1;
	}

	public String getBuff() {
		return buff;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	public List<int[]> getEffects() {
		List<int[]> cloneList = new ArrayList<>();
		for(int[] effect : effects){
			cloneList.add(effect.clone());
		}
		return cloneList;
	}

	@Override
	protected boolean assemble() {
		effects = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(buff)) {
			String[] array = buff.split(",");
			for (String val : array) {
				String[] eff = val.split("_");
				if (eff == null || eff.length != 2) {
					logger.error("alliance_Officials_Buff.xml error, officeId : {}, buff : {} error", officeId, buff);
					return false;
				}
				effects.add(new int[] { Integer.parseInt(eff[0]), Integer.parseInt(eff[1]) });
			}
		}
		return super.assemble();
	}
	
	

}
