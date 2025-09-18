package com.hawk.game.crossproxy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossServerListCfg;
import com.hawk.game.crossproxy.skill.CrossSkillCollect;
import com.hawk.game.global.GlobalData;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.crossTalent.CrossTalentCollect;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;

public class CrossSkillService extends HawkAppObj {
	/**
	 * 单例对象
	 */
	private static CrossSkillService instance = null;

	private static Map<String, CrossSkillCollect> collectMap;
	private static Map<String, CrossTalentCollect> crossTalentMap;
	private long lastTick;
	private boolean inited;

	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static CrossSkillService getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public CrossSkillService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
	}

	public boolean init() {
		collectMap = new ConcurrentHashMap<>();
		crossTalentMap = new ConcurrentHashMap<>();
		onTick();
		inited = true;
		return true;
	}

	public int getEffectValIfContinue(String serverId, EffType effType) {
		if(!inited){
			return 0;
		}
		try {
			final String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
			CrossSkillCollect collect = collectMap.get(mainServerId);
			CrossTalentCollect tlcollect = getCrossTalentCollect(mainServerId);
			int result = 0;
			if (Objects.nonNull(collect)) {
				result += collect.getEffectValIfContinue(effType);
			}
			if (Objects.nonNull(tlcollect)) {
				result += tlcollect.getEffectTalent(0, effType);
			}

			return result;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	@Override
	public boolean onTick() {
		if (HawkTime.getMillisecond() - lastTick < 5000) {
			return true;
		}

		lastTick = HawkTime.getMillisecond();

		String theServerId = GsConfig.getInstance().getServerId();
		CrossServerListCfg crossServerListCfg = AssembleDataManager.getInstance().getCrossServerListCfg(theServerId);
		Set<String> serverSet = new HashSet<>();
		if (Objects.nonNull(crossServerListCfg)) {
			List<String> crossServerIds = crossServerListCfg.getServerList();
			serverSet.addAll(crossServerIds);
		}

		serverSet.add(theServerId);

		Map<String, CrossSkillCollect> map = new ConcurrentHashMap<>();
		for (String serverId : serverSet) {
			CrossSkillCollect colectOld = collectMap.getOrDefault(serverId, null);
			CrossSkillCollect colect = CrossSkillCollect.create(serverId, colectOld);
			map.put(colect.getServerId(), colect);
			
			getCrossTalentCollect(serverId);
		}

		collectMap = map;
		collectMap.values().forEach(CrossSkillCollect::checkRepush);
		crossTalentMap.values().forEach(CrossTalentCollect::onTick);
		
		return super.onTick();
	}

	/**
	 * 同步 
	 */
	public void syncCrossSkillInfo(Player player) {
		CrossSkillCollect collect = collectMap.get(player.getMainServerId());
		if (collect == null) {
			return;
		}
		collect.syncCrossSkillInfo(player);
	}

	public boolean castSkill(Player player, String skillId, List<String> params) {
		CrossSkillCollect collect = collectMap.get(player.getMainServerId());
		if (collect == null) {
			HawkLog.errPrintln("CrossSkillService castSkill failed playerId:{} MainServerId:{} skillId:{}", player.getId(), player.getMainServerId(), skillId);
			return false;
		}
		return collect.castSkill(player, skillId, params);
	}

	
	/**取得跨服天赋*/
	public CrossTalentCollect getCrossTalentCollect(String serverId) {
		serverId = GlobalData.getInstance().getMainServerId(serverId);
		if(crossTalentMap.containsKey(serverId)){
			return crossTalentMap.get(serverId);
		}
		CrossTalentCollect result = new CrossTalentCollect();
		result.setServerId(serverId);
		crossTalentMap.put(serverId, result);
		return result;
	}
	
}
