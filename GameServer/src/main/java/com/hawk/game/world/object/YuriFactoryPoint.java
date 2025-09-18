package com.hawk.game.world.object;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkApp;
import org.hawk.helper.HawkAssert;

import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.world.WorldPoint;

/**
 * 尤里兵工厂
 * 
 * @author zhenyu.shang
 * @since 2017年9月18日
 */
public class YuriFactoryPoint {
	
	/** 对应的世界点 */
	private WorldPoint worldPoint;

	/** 公会对应的尤里兵工厂 */
	private Map<String, GuildYuriFactory> guildYuriMap;

	public YuriFactoryPoint(WorldPoint worldPoint) {
		HawkAssert.notNull(worldPoint);
		this.worldPoint = worldPoint;
		this.guildYuriMap = new ConcurrentHashMap<String, GuildYuriFactory>();
	}

	/**
	 * 心跳
	 */
	public void heartbeat() {
		// 在活动中则执行心跳
		if (isInActive()) {
			for (GuildYuriFactory factory : guildYuriMap.values()) {
				factory.heartbeat();
			}
		}
	}

	/**
	 * 开始尤里复仇活动
	 * 
	 * @param guildId
	 */
	public void startYuriRevenge(String guildId) {
		guildYuriMap.put(guildId, new GuildYuriFactory(worldPoint.getId(), guildId));
	}

	/**
	 * 结束尤里复仇活动
	 */
	public void closeYuriRevenge(String guildId, boolean clearMarch) {
		GuildYuriFactory factory = guildYuriMap.get(guildId);
		if (factory != null && clearMarch) {
			factory.clearMarch();
		}
		guildYuriMap.remove(guildId);
	}

	public WorldPoint getWorldPoint() {
		return worldPoint;
	}

	public long getLifeStartTime() {
		return worldPoint.getLifeStartTime();
	}

	public int getId() {
		return worldPoint.getId();
	}

	public GuildYuriFactory getGuildYuriFactory(String guildId) {
		return guildYuriMap.get(guildId);
	}

	/**
	 * 重置尤里工厂时间
	 */
	public void resetLifeStartTime() {
		worldPoint.setLifeStartTime(HawkApp.getInstance().getCurrentTime() + WorldMapConstProperty.getInstance().getYuriLifeTime());
	}

	/**
	 * 是否在活动中
	 * 
	 * @return
	 */
	public boolean isInActive() {
		return !guildYuriMap.isEmpty();
	}

	@Override
	public String toString() {
		return worldPoint.toString();
	}
}
