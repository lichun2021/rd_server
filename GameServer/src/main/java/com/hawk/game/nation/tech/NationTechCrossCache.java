package com.hawk.game.nation.tech;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.NationTechCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/**
 * 国家科技跨服缓存
 * @author Golden
 *
 */
public class NationTechCrossCache {

	/**
	 * 服务器id
	 */
	private String serverId;
	
	/**
	 * 加载时间
	 */
	private long loadTime;
	
	/**
	 * 国家科技信息
	 */
	private Map<Integer, Integer> nationTech;
	
	/**
	 * 作用号信息
	 */
	Map<EffType, Integer> effMap;
	
	/**
	 * 构造函数
	 * @param serverId
	 */
	public NationTechCrossCache(String serverId) {
		this.serverId = serverId;
		this.nationTech = new ConcurrentHashMap<>();
		this.effMap = new ConcurrentHashMap<>();
		this.checkNationTechload();
		this.loadTime = HawkTime.getMillisecond();
	}
	
	/**
	 * 获取区服id
	 * @return
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * 获取国家科技
	 */
	public Map<Integer, Integer> getNationTech() {
		checkNationTechload();
		return nationTech;
	}
	
	/**
	 * 获取国家科技
	 */
	public int getNationTech(int cfgId) {
		checkNationTechload();
		return nationTech.getOrDefault(cfgId, 0);
	}
	
	/**
	 * 获取作用号列表
	 * @return
	 */
	public Map<EffType, Integer> getEffMap() {
		checkNationTechload();
		return effMap;
	}


	/**
	 * 加载国家科技信息
	 */
	private void checkNationTechload() {
		if (HawkTime.getMillisecond() - loadTime > 5 * GsConst.MINUTE_MILLI_SECONDS) {
			nationTech = RedisProxy.getInstance().getNationTechMap(serverId);
			effMap = calcEffect();
			loadTime = HawkTime.getMillisecond();
		}
	}
	
	/**
	 * 计算作用号
	 */
	private Map<EffType, Integer> calcEffect() {
		Map<EffType, Integer> effMap = new ConcurrentHashMap<>();
		for (Entry<Integer, Integer> tech : nationTech.entrySet()) {
			try {
				NationTechCfg cfg = AssembleDataManager.getInstance().getNationTech(tech.getKey(), tech.getValue());
				if (cfg == null) {
					continue;
				}
				List<EffectObject> effectList = cfg.getEffect();
				for (EffectObject effect : effectList) {
					int effVal = 0;
					if (effMap.containsKey(effect.getType())) {
						effVal = effMap.get(effect.getType()) + effect.getEffectValue();
					} else {
						effVal = effect.getEffectValue();
					}
					effMap.put(effect.getType(), effVal);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return effMap;
	}
}
