package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 兵种信息配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_function.xml")
public class AllianceFunctionCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	/**
	 *  buff类型 1增益 2减益
	 */
	protected final int type;
	
	/**
	 *  buff效果
	 */
	protected final String effect;
	
	/**
	 * 作用号
	 */
	private List<int[]> effList;

	public AllianceFunctionCfg() {
		id = 0;
		type = 0;
		effect = "";
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getEffect() {
		return effect;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}
	
	public List<int[]> getEffList() {
		return effList;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(effect)) {
			effList = new ArrayList<>();
			for (String eff : effect.split(";")) {
				String[] val = eff.split("_");
				if (val.length != 2) {
					return false;
				}
				effList.add(new int[] { Integer.parseInt(val[0]), Integer.parseInt(val[1]) });
			}
		}
		return true;
	}

}
