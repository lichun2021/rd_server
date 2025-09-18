package com.hawk.game.module.crossTalent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.crossTalent.cfg.CrossTalentLevelCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.CrossActivity.CrossActivityState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Talent.CrossTalentInfo;
import com.hawk.game.protocol.Talent.HPCrossTalentInfoResp;

public class CrossTalentCollect implements SerializJsonStrAble {

	private List<CrossTalentItem> talentEntities = new ArrayList<CrossTalentItem>();
	private String serverId;
	private String mergeFromStr;

	private boolean needSync = true;
	private long totalPoint;
	private long usedPoint;
	
	private final String REDISKEY = "cross_talentt:";
	private Map<Integer, Map<EffType, Integer>> effectTalent = new HashMap<Integer, Map<EffType, Integer>>();
	private EffType[] effectSync = new EffType[0];

	/** 剩余战略点*/
	public long getLeftPoint() {
		return totalPoint - usedPoint;
	}
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		talentEntities.forEach(tal -> arr.add(tal.serializ()));
		obj.put("talentEntities", arr);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		this.mergeFromStr = serialiedStr;
		if (StringUtils.isEmpty(serialiedStr)) {
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		JSONArray arr = obj.getJSONArray("talentEntities");
		List<CrossTalentItem> list = new ArrayList<>();
		for (Object str : arr) {
			CrossTalentItem item = new CrossTalentItem();
			item.mergeFrom(str.toString());
			list.add(item);
		}
		this.talentEntities = list;
	}

	private void saveRedis() {
		String key = REDISKEY + CrossActivityService.getInstance().getTermId() + ":" + serverId;
		RedisProxy.getInstance().getRedisSession().setString(key, serializ());
	}

	public void onTick() {
		try {
			this.totalPoint = RedisProxy.getInstance().getCrossTalentPoint(serverId);
			if (GlobalData.getInstance().isLocalServer(serverId) && !this.talentEntities.isEmpty()
					&& CrossActivityService.getInstance().getActivityInfo().getState() != CrossActivityState.C_OPEN) { // 如果活动未开启
				this.talentEntities.clear();
				saveRedis();
				needSync = true;
			}
			String key = REDISKEY + CrossActivityService.getInstance().getTermId() + ":" + serverId;
			String redisStr = RedisProxy.getInstance().getRedisSession().getString(key);
			if (!StringUtils.equals(redisStr, mergeFromStr)) {
				needSync = true;
			}

			this.mergeFrom(redisStr);
			
			calUsedPoint();
			
			if (needSync) {
				initEffectTalent();
				Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
				for (Player player : players) {
					if (Objects.equals(player.getMainServerId(), serverId)) {
						player.getEffect().syncEffect(player, effectSync);
					}
				}
				needSync = false;
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void calUsedPoint() {
		int pointed = 0;
		List<CrossTalentItem> talentEntities = getTalentEntities();
		for (CrossTalentItem talentEntity : talentEntities) {
			if (talentEntity.getType() != 0) {
				continue;
			}
			CrossTalentLevelCfg tlcfg = HawkConfigManager.getInstance().getCombineConfig(CrossTalentLevelCfg.class, talentEntity.getTalentId(), talentEntity.getLevel());
			pointed += tlcfg.getTotalPoint();
		}
		this.usedPoint = pointed;
	}

	public List<CrossTalentItem> getTalentEntities() {
		return talentEntities;
	}

	public CrossTalentItem getTalentByTalentId(int talentId, int talentType) {
		for (CrossTalentItem talentEntity : getTalentEntities()) {
			if (talentId == talentEntity.getTalentId() && talentType == talentEntity.getType()) {
				return talentEntity;
			}
		}
		return null;
	}

	public CrossTalentItem createCrossTalentItem(int talentId, int type, int level, CrossTalentLevelCfg talentLevelCfg) {
		CrossTalentItem talentEntity = new CrossTalentItem();
		talentEntity.setTalentId(talentId);
		talentEntity.setLevel(level);
		talentEntity.setType(type);
		talentEntities.add(talentEntity);
		return talentEntity;
	}

	public void notifyUpdate() {
		this.totalPoint = RedisProxy.getInstance().getCrossTalentPoint(serverId);
		saveRedis();
		calUsedPoint();
		needSync = true;
	}

	/**
	 * 同步天赋信息
	 * @return 
	 */
	public void talentInfoSync(Player player) {
		HPCrossTalentInfoResp.Builder builder = HPCrossTalentInfoResp.newBuilder();
		List<CrossTalentItem> talentEntities = this.getTalentEntities();
		for (CrossTalentItem talentEntity : talentEntities) {
			if (talentEntity.getLevel() > 0) {
				CrossTalentInfo.Builder talbul = CrossTalentInfo.newBuilder();
				talbul.setTalentId(talentEntity.getTalentId());
				talbul.setLevel(talentEntity.getLevel());
				builder.addTalentInfos(talbul);
			}
		}
		builder.setServerId(serverId);
		builder.setTotalPoint(totalPoint);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_TALENT_INFO_S_VALUE, builder));
	}

	/**
	 * 初始化天赋作用号
	 */
	public void initEffectTalent() {
		Set<EffType> list = new HashSet<EffType>();
		for (Map<EffType, Integer> effId : effectTalent.values()) {
			list.addAll(effId.keySet());
		}

		Map<Integer, Map<EffType, Integer>> effectTalent = new HashMap<Integer, Map<EffType, Integer>>();

		for (CrossTalentItem talentEntity : talentEntities) {
			int talentId = talentEntity.getTalentId();
			int talentLvl = talentEntity.getLevel();
			if (talentId <= 0 || talentLvl <= 0) {
				continue;
			}

			CrossTalentLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(CrossTalentLevelCfg.class, talentId, talentLvl);
			if (cfg == null || HawkOSOperator.isEmptyString(cfg.getEffect())) {
				continue;
			}

			Map<EffType, Integer> effMap = effectTalent.get(talentEntity.getType());
			if (effMap == null) {
				effMap = new HashMap<>();
				effectTalent.put(talentEntity.getType(), effMap);
			}

			for (EffectObject eff : cfg.getEffList()) {
				EffType effId = EffType.valueOf(eff.getEffectType());
				int effVal = eff.getEffectValue();

				if (effMap.containsKey(effId)) {
					effVal += effMap.get(effId);
				}

				effMap.put(effId, effVal);
				list.add(effId);
			}
		}

		this.effectTalent = effectTalent;
		this.effectSync = list.toArray(new EffType[list.size()]);
	}

	public int getEffectTalent(int talentType, EffType effType) {
		if (!effectTalent.containsKey(talentType)) {
			return 0;
		}

		Map<EffType, Integer> talentMap = effectTalent.get(talentType);
		if (talentMap == null) {
			return 0;
		}

		if (effType == null) {
			return 0;
		}

		if (!talentMap.containsKey(effType)) {
			return 0;
		}
		return talentMap.get(effType);
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getMergeFromStr() {
		return mergeFromStr;
	}

	public void setMergeFromStr(String mergeFromStr) {
		this.mergeFromStr = mergeFromStr;
	}

	public boolean isNeedSync() {
		return needSync;
	}

	public void setNeedSync(boolean needSync) {
		this.needSync = needSync;
	}

	public void setTalentEntities(List<CrossTalentItem> talentEntities) {
		this.talentEntities = talentEntities;
	}

	public long getTotalPoint() {
		return totalPoint;
	}

	public long getUsedPoint() {
		return usedPoint;
	}

}
