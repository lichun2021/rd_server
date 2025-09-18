package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 世界机器人常量配置
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "xml/world_robot_const.xml")
public class WorldRobotConstProperty extends HawkConfigBase {
	
	/**
	 * 单例
	 */
	private static WorldRobotConstProperty instance = null;

	/**
	 * 获取单例
	 */
	public static WorldRobotConstProperty getInstance() {
		return instance;
	}

	/**
	 * 是否开启
	 */
	protected final boolean isOpen;
	
	/**
	 * 生命周期
	 */
	protected final int lifeTime;

	/**
	 * tick间隔
	 */
	protected final int tickPeroid;
	
	/**
	 * 构造
	 */
	public WorldRobotConstProperty() {
		instance = this;
		isOpen = true;
		lifeTime = 3 * 3600;
		tickPeroid = 3600;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public long getLifeTime() {
		return lifeTime * 1000L;
	}

	public long getTickPeroid() {
		return tickPeroid * 1000L;
	}
}
