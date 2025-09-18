package com.hawk.game.module.spacemecha.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 星甲召唤各阶段时长配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/space_machine_stage.xml")
public class SpaceMechaStageCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 持续时长
	 */
	protected final int time;
	
	private long timeLong;
	private static long totalTime;

	public SpaceMechaStageCfg() {
		id = 0;
		time = 0;
	}

	public int getId() {
		return id;
	}
	
	public int getTime() {
		return time;
	}
	
	public long getTimeLong() {
		return timeLong;
	}
	
	public boolean assemble() {
		timeLong = time * 1000L;
		totalTime += timeLong;
		return super.assemble();
	}
	
	public static long getTotalTime() {
		return totalTime;
	}

}
