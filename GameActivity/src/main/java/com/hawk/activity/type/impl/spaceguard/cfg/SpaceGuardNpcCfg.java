package com.hawk.activity.type.impl.spaceguard.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
																	
@HawkConfigManager.XmlResource(file = "activity/space_machine_guard/space_machine_guard_npc.xml")
public class SpaceGuardNpcCfg extends HawkConfigBase {
	@Id
	private final int Id;

	/** 阶段 */
	private final int stage;

	public SpaceGuardNpcCfg() {
		Id = 0;
		stage = 0;
	}

	public int getId() {
		return Id;
	}

	public int getStage() {
		return stage;
	}
	
}
