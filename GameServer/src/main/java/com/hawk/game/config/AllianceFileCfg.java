package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.XmlResource(file = "xml/alliance_file.xml")
public class AllianceFileCfg extends HawkConfigBase implements Comparable<AllianceFileCfg> {
	
	@Id
	private final int id;
	/** 
	 * 类型 {@link com.hakw.const.GsConst.GuildFavourite}
	 */
	private final int type;
	/**
	 * 不同的类型 含义不一样
	 */
	private final int typeCoefficient;
	/**
	 * 排序规则
	 */
	private final int order;
	
	public AllianceFileCfg() {
		id = 0;
		type = 0;
		typeCoefficient = 0;
		order = 0;
	}
	@Override
	public int compareTo(AllianceFileCfg o) {
		return order - o.order;  
	}
	public int getId() {
		return id;
	}
	public int getType() {
		return type;
	}
	public int getOrder() {
		return order;
	}
	public int getTypeCoefficient() {
		return typeCoefficient;
	}
	
}
