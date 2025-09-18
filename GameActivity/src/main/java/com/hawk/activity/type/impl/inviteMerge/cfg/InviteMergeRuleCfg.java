package com.hawk.activity.type.impl.inviteMerge.cfg;

import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.collect.Table;

@HawkConfigManager.XmlResource(file = "activity/merge_invite/merge_invite_rule.xml")
public class InviteMergeRuleCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 国家类型A
	 */
	private final int typeA;
	/**
	 * 国家类型B
	 */
	private final int typeB;
	/**
	 * 1表示允许要求合服, 0不允许
	 */
	private final int isRule;
	
	private static Table<Integer, Integer, Integer> table = ConcurrentHashTable.create();
	
	public InviteMergeRuleCfg() {
		id = 0;
		typeA = 1;
		typeB = 1;
		isRule = 0;
	}
	
	public boolean assemble() {
		table.put(typeA, typeB, isRule);
		return true;
	}

	public int getId() {
		return id;
	}

	public int getTypeA() {
		return typeA;
	}

	public int getTypeB() {
		return typeB;
	}

	public int getIsRule() {
		return isRule;
	}
	
	public static boolean canMerge(int typeA, int typeB) {
		return table.get(typeA, typeB) > 0;
	}

}