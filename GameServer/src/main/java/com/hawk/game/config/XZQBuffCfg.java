package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.protocol.Const.EffType;

/**
 * 超级武器奖励配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/xzq_buff.xml")
@HawkConfigBase.CombineId(fields = { "buildLevel", "count" })
public class XZQBuffCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	protected final int buildLevel;
	protected final int count;
	protected final String effects;
	
	List<EffectObject> effectList = new ArrayList<>();
	public XZQBuffCfg() {
		id = 0;
		buildLevel = 0;
		count = 0;
		effects = "";
	}

	
	@Override
	protected boolean assemble() {
		List<EffectObject> list = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.effects)) {
			String[] array = this.effects.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				EffType type = EffType.valueOf(Integer.parseInt(info[0]));
				if(type == null){
					return false;
				}
				list.add(new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1])));
			}
		}
		this.effectList = list;
		return true;
	}

	public int getId() {
		return id;
	}



	public int getBuildLevel() {
		return buildLevel;
	}



	public int getCount() {
		return count;
	}
	
	

	public List<EffectObject> getEffectList() {
		return effectList;
	}




	
}
