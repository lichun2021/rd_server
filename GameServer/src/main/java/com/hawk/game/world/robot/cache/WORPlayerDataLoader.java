package com.hawk.game.world.robot.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.entifytype.EntityType;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple3;
import org.hawk.uuid.HawkUUIDGenerator;

import com.google.common.cache.CacheLoader;
import com.hawk.game.config.PlayerShowCfg;
import com.hawk.game.config.WorldRobotNameCfg;
import com.hawk.game.config.WorldRobotResCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.lianmengtaiboliya.entity.TBLYArmyEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.world.service.WorldRobotService;

public class WORPlayerDataLoader extends CacheLoader<PlayerDataKey, Object> {
	/**
	 * 玩家id
	 */
	private String playerId;
	private String sourcePlayerId;
	private boolean realArmy;

	public WORPlayerDataLoader(String playerId, String sourcePlayerId) {
		this.playerId = playerId;
		this.sourcePlayerId = sourcePlayerId;
	}

	@Override
	public Object load(PlayerDataKey key) throws Exception {
		Player sourcePlayer = GlobalData.getInstance().makesurePlayer(sourcePlayerId);
		// 数据有效性判断
		if (sourcePlayer == null) {
			throw new RuntimeException("Source Player NULL!!!!");
		}

		if (key == PlayerDataKey.BanRankInfos) {
			return new ConcurrentHashMap<>();
		}

		Object sourceData = sourcePlayer.getData().getDataCache().makesureDate(key);

		if (key == PlayerDataKey.ArmyEntities && !realArmy) {
			List<ArmyEntity> armyList = new ArrayList<>();
			WorldRobotResCfg resCfg = HawkConfigManager.getInstance().getConfigByKey(WorldRobotResCfg.class, sourcePlayer.getCityLevel());
			for (HawkTuple3<Integer, Integer, Integer> sh : resCfg.getSoldierList()) {
				TBLYArmyEntity result = new TBLYArmyEntity();
				result.setPrimaryKey(HawkUUIDGenerator.genUUID());
				result.setEntityType(EntityType.TEMPORARY);
				result.setId(HawkUUIDGenerator.genUUID());
				result.setPlayerId(playerId);
				result.setArmyId(sh.first);
				int max = Math.max(sh.third, sh.second);
				int min = Math.min(sh.third, sh.second);
				result.addFree(HawkRand.randInt(min, max));
				armyList.add(result);
			}
			return armyList;
		}

		byte[] serBytes = PlayerDataSerializer.serializeData(key, sourceData);

		Object result = PlayerDataSerializer.unserializeData(key, serBytes, false);

		if (result instanceof HawkDBEntity) {
			afterCopyData((HawkDBEntity) result);

		} else if (result instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> it = (List<Object>) result;
			if (!it.isEmpty()) {
				boolean isDbentityList = it.get(0) instanceof HawkDBEntity;
				if (isDbentityList) {
					for (Object entity : it) {
						afterCopyData((HawkDBEntity) entity);
					}
				}
			}
		}

		return result;
	}

	private void afterCopyData(HawkDBEntity result) {
		result.setPersistable(false);
		result.setEntityType(EntityType.TEMPORARY);
		try {
			result.setPrimaryKey(HawkUUIDGenerator.genUUID());
		} catch (Exception e) {
		}
		try {
			Method mod = result.getClass().getDeclaredMethod("setPlayerId", String.class);
			if (Objects.nonNull(mod)) {
				mod.invoke(result, playerId);
			}
		} catch (Exception e) {
		}
		if (result instanceof PlayerEntity) {
			PlayerEntity playerEntity = (PlayerEntity) (result);
			playerEntity.setId(playerId);
			playerEntity.setIcon(PlayerShowCfg.randmShow());
			playerEntity.setName(WorldRobotNameCfg.randmName());
		}
		if (result instanceof StatusDataEntity) {
			StatusDataEntity buff = (StatusDataEntity) result;
			if (buff.getStatusId() == EffType.CITY_SHIELD_VALUE) {
				buff.setEndTime(0);
			}
		}

		if (result instanceof PlayerBaseEntity) {
			Player sourcePlayer = GlobalData.getInstance().makesurePlayer(sourcePlayerId);
			WorldRobotResCfg resCfg = HawkConfigManager.getInstance().getConfigByKey(WorldRobotResCfg.class, sourcePlayer.getCityLevel());
			PlayerBaseEntity baseEntity = (PlayerBaseEntity) result;
			baseEntity.setGoldoreUnsafe(resNumber(resCfg.getDown(), resCfg.getUp(), resCfg.getRes1007()));
			baseEntity.setOilUnsafe(resNumber(resCfg.getDown(), resCfg.getUp(), resCfg.getRes1008()));
			baseEntity.setSteelUnsafe(resNumber(resCfg.getDown(), resCfg.getUp(), resCfg.getRes1009()));
			baseEntity.setTombarthiteUnsafe(resNumber(resCfg.getDown(), resCfg.getUp(), resCfg.getRes1010()));
		}

		result.afterRead();
	}

	private int resNumber(int down, int up, int num) {
		int pct = HawkRand.randInt(down, up);
		return (int) (pct * 0.01 * num);
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getSourcePlayerId() {
		return sourcePlayerId;
	}

	public void setSourcePlayerId(String sourcePlayerId) {
		this.sourcePlayerId = sourcePlayerId;
	}

	public boolean isRealArmy() {
		return realArmy;
	}

	public void setRealArmy(boolean realArmy) {
		this.realArmy = realArmy;
	}

}
