package com.hawk.activity.type.impl.bountyHunter.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/bounty_hunter/bounty_hunter_reward_pool.xml")
public class BountyHunterRewardPoolCfg extends HawkConfigBase implements HawkRandObj {
	@Id
	private final int id;// ="1"
	private final int weight;// ="30"
	private final int pool;
	private final int round;
	private final int bossBNotRun;// ="3"
	private final int bossBNotDie;// ="5"

	public BountyHunterRewardPoolCfg() {
		id = 0;
		weight = 100;
		pool = 0;
		round = 1;
		bossBNotDie = 3;
		bossBNotRun = 5;
	}

	public int getId() {
		return id;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public int getPool() {
		return pool;
	}

	public int getRound() {
		return round;
	}

	public int getBossBNotRun() {
		return bossBNotRun;
	}

	public int getBossBNotDie() {
		return bossBNotDie;
	}

}
