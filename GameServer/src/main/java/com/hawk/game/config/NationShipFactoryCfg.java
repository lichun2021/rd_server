package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 国家飞船制造厂
 * @author zhenyu.shang
 * @since 2022年4月19日
 */
@HawkConfigManager.XmlResource(file = "xml/nation_spacecraft.xml")
public class NationShipFactoryCfg extends HawkConfigBase {
	
	@Id
	protected final int partId;
	
	protected final int partType;
	
	protected final int partLevel;
	
	protected final int factoryLevel;
	
	protected final String cond	;
	
	protected final String consumeRes;
	
	protected final int continueTime;
	
	protected final String	effectParam;
	
	private List<Integer> prevCondition;
	
	private List<ItemInfo> consumeResList;
	
	private List<EffectObject> effectList;
	
	public NationShipFactoryCfg() {
		this.partId = 0;
		this.partType = 0;
		this.partLevel = 0;
		this.factoryLevel = 0;
		this.cond = "";
		this.consumeRes = "";
		this.continueTime = 0;
		this.effectParam = "";
	}

	public int getPartId() {
		return partId;
	}

	public int getPartType() {
		return partType;
	}

	public int getPartLevel() {
		return partLevel;
	}

	public int getFactoryLevel() {
		return factoryLevel;
	}

	public String getCond() {
		return cond;
	}

	public String getConsumeRes() {
		return consumeRes;
	}

	public int getContinueTime() {
		return continueTime;
	}

	public String getEffectParam() {
		return effectParam;
	}
	
	public List<Integer> getPrevCondition() {
		return prevCondition;
	}

	public List<ItemInfo> getConsumeResList() {
		return consumeResList;
	}

	public List<EffectObject> getEffectList() {
		return effectList;
	}

	@Override
	protected boolean assemble() {
		prevCondition = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(cond)) {
			String[] split1 = cond.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				prevCondition.add(Integer.parseInt(split1[i]));
			}
		}
		
		List<ItemInfo> clist = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(consumeRes)) {
			String[] split1 = consumeRes.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				ItemInfo itemInfo = new ItemInfo(split1[i]);
				clist.add(itemInfo);
			}
		}
		this.consumeResList = ImmutableList.copyOf(clist);
		
		this.effectList = new ArrayList<EffectObject>();
		if (!HawkOSOperator.isEmptyString(this.effectParam)) {
			String[] array = this.effectParam.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				EffType type = EffType.valueOf(Integer.parseInt(info[0]));
				if(type == null){
					return false;
				}
				this.effectList.add(new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1])));
			}
		}
		return true;
	}
}
