package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 世界地图资源刷新配置
 * 
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_resource.xml")
public class WorldResourceCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 资源类型
	protected final int resType;
	// 等级
	protected final int level;
	// 资源数量
	protected final int resNum;
	// 资源低于这个数量则清理
	protected final int resRefreshNum;
	// 在地图上的生存周期
	protected final int lifeTime;
	// 是否为新手资源
	protected final boolean newly;
	
	public WorldResourceCfg() {
		id = 0;
		resType = 0;
		level = 0;
		resNum = 0;
		lifeTime = 0;
		resRefreshNum = 0;
		newly = false;
	}

	public int getId() {
		return id;
	}

	public int getResType() {
		return resType;
	}

	public int getLevel() {
		return level;
	}

	public int getResNum() {
		return resNum;
	}

	public int getLifeTime() {
		return lifeTime;
	}
	
	public boolean isNewly() {
		return newly;
	}

	public int getResRefreshNum() {
		return resRefreshNum;
	}
}