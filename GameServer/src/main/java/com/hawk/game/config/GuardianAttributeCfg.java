package com.hawk.game.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/guardian_attribute.xml")
public class GuardianAttributeCfg extends HawkConfigBase {
	/**
	 * 主键
	 */
	@Id
	private final int id;
	/**
	 * 需要的守护值.
	 */
	private final int needValue; 
	/**
	 * 开启的Effeect
	 */
	private final String effectSever;
	/**
	 * 解锁的外观.
	 */
	private final String unlockDressIdSever;
	/**
	 * 解锁的作用号 
	 */
	private Map<Integer, Integer> effectSeverMap;
	/**
	 * 解锁的外观.
	 */
	private List<Integer> unlockDressIdSeverList; 
	
	public GuardianAttributeCfg() {
		this.id = 0;
		this.needValue = 0;
		this.effectSever = "";
		this.unlockDressIdSever = "";
	}
	
	public int getId() {
		return id;
	}
	public int getNeedValue() {
		return needValue;
	}
	
	@Override
	public boolean assemble() {
		effectSeverMap = Collections.synchronizedMap(SerializeHelper.stringToMap(effectSever, Integer.class, Integer.class, 
				SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.SEMICOLON_ITEMS));
		unlockDressIdSeverList = Collections.synchronizedList(SerializeHelper.stringToList(Integer.class, unlockDressIdSever, SerializeHelper.SEMICOLON_ITEMS));
		
		return true;
	}

	public Map<Integer, Integer> getEffectSeverMap() {
		return effectSeverMap;
	}

	public List<Integer> getUnlockDressIdSeverList() {
		return unlockDressIdSeverList;
	}
	
	public EffType[] getEffTypeArray() {
		EffType[] effType = new EffType[effectSeverMap.size()];
		int index = 0;
		for (Entry<Integer, Integer> entry : effectSeverMap.entrySet()) {
			effType[index++] = EffType.valueOf(entry.getKey());
		}  
		
		return effType;
	}
}
