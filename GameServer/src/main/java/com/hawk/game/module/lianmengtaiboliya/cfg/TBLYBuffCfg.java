package com.hawk.game.module.lianmengtaiboliya.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRandObj;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tbly_buff.xml")
public class TBLYBuffCfg extends HawkConfigBase implements HawkRandObj {
	// id="1" weight="1000" refreshTime="900_1800_2700" orderEffect="1717_5000" effectTime="900"
	@Id
	protected final int id;
	protected final int weight;
	protected final String refreshTime;
	protected final String orderEffect;
	/**
	 * 生效时间
	 */
	protected final int effectTime;

	private Map<EffType, Integer> effectList;
	private List<Integer> refreshTimeList;

	public TBLYBuffCfg() {
		this.id = 0;
		this.effectTime = 0;
		this.weight = 0;
		this.orderEffect = "";
		this.refreshTime = "";
	}

	@Override
	protected boolean assemble() {
		effectList = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(orderEffect)) {
			String[] array = orderEffect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				effectList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}
		effectList = ImmutableMap.copyOf(effectList);

		refreshTimeList = Splitter.on("_").splitToList(refreshTime).stream().map(Integer::valueOf).collect(Collectors.toList());

		return true;
	}

	public int getId() {
		return id;
	}

	public int getEffectTime() {
		return effectTime;
	}

	public String getOrderEffect() {
		return orderEffect;
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	public void setEffectList(Map<EffType, Integer> effectList) {
		this.effectList = effectList;
	}

	public List<Integer> getRefreshTimeList() {
		return refreshTimeList;
	}

	public void setRefreshTimeList(List<Integer> refreshTimeList) {
		this.refreshTimeList = refreshTimeList;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public String getRefreshTime() {
		return refreshTime;
	}

}
