package com.hawk.game.config;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 攻防模拟战助威
 * 
 * @author jm
 *
 */
@HawkConfigManager.XmlResource(file = "xml/simulate_war_encourage.xml")
public class SimulateWarEncourageCfg extends HawkConfigBase {
	/**
	 * 等同于次数
	 */
	@Id
	private final int id;
	/**
	 * 消耗
	 */
	private final String cost;
	/**
	 * buff
	 */
	private final String buff;
	/**
	 * 消耗 {@link #cost}
	 */
	private  List<ItemInfo> costList = new ArrayList<>();
	/**
	 *  buff {@link #buff}
	 */
	private  Map<EffType, Integer> buffMap = new HashMap<>();
	
	public SimulateWarEncourageCfg() {
		this.id = 0;
		this.cost = "";
		this.buff = "";
	}
	public int getId() {
		return id;
	}
	public String getCost() {
		return cost;
	}
	public String getBuff() {
		return buff;
	}
	
	
	@Override
	public boolean assemble() {
		costList = Collections.synchronizedList(ItemInfo.valueListOf(cost));
		Map<Integer, Integer> intMap = SerializeHelper.stringToMap(buff, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
		Map<EffType, Integer> effMap = new HashMap<>();
		intMap.entrySet().forEach(entry->{
			effMap.put(EffType.valueOf(entry.getKey()), entry.getValue());
		});
		
		this.buffMap = Collections.synchronizedMap(effMap);
		
		return true;
	}
	public List<ItemInfo> getCostList() {
		return costList;
	}
	public Map<EffType, Integer> getBuffMap() {
		return buffMap;
	}
}
