package com.hawk.game.config;

import java.util.HashSet;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "cfg/cs/csProtocol.xml")
public class CrossProtocolCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	/**
	 * 模块名
	 */
	private final String name;
	
	/**
	 * A->B A处理的协议
	 */
	private final String localProtocols;
	
	/**
	 * A->B A直接拦截的协议.
	 */
	private final String shieldProtocols;
	private Set<Integer> localProtocolSet;
	private Set<Integer> shieldProtocolSet;

	public CrossProtocolCfg() {
		this.id = 0;
		this.name = "";
		this.localProtocols = "";
		this.shieldProtocols = "";
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<Integer> getLocalProtocolSet() {
		return localProtocolSet;
	}

	public Set<Integer> getShieldProtocolSet() {
		return shieldProtocolSet;
	}
	
	@Override
	public boolean assemble() {
		if (HawkOSOperator.isEmptyString(localProtocols)) {
			localProtocolSet = new HashSet<>();
		} else {
			localProtocolSet = SerializeHelper.stringToSet(Integer.class, localProtocols, SerializeHelper.ATTRIBUTE_SPLIT);
		}

		if (HawkOSOperator.isEmptyString(shieldProtocols)) {
			shieldProtocolSet = new HashSet<>();
		} else {
			shieldProtocolSet = SerializeHelper.stringToSet(Integer.class, shieldProtocols, SerializeHelper.ATTRIBUTE_SPLIT);
		}

		return true;
	}
}
