package com.hawk.game.player.manhattan;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hawk.game.protocol.World.PresetMarchManhattan;
import org.hawk.os.HawkException;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.config.ManhattanSWCfg;
import com.hawk.game.config.ManhattanSWSkillCfg;
import com.hawk.game.entity.ManhattanEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Manhattan.PBManhattanInfo;
import com.hawk.game.util.EffectParams;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 超武功能
 * @author lating
 */
public abstract class PlayerManhattan {
	/**
	 * db实体
	 */
	private ManhattanEntity entity;
	/**
	 * 部件等级
	 */
	private Map<Integer, Integer> posLevelMap = new ConcurrentHashMap<>();
	/** 
	 * 作用号 
	 */
	private ImmutableMap<Integer, Integer> effValMap;

	/**
	 * 初始化
	 * @param entity
	 */
	public void init(ManhattanEntity entity) {
		this.effValMap = ImmutableMap.of();
		this.entity = entity;
		Map<Integer, Integer> map = SerializeHelper.stringToMap(entity.getPosLevel(), Integer.class, Integer.class);
		posLevelMap.putAll(map);
		try {
			loadEffVal();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 等级提升
	 * @param posId
	 * @return
	 */
	public abstract void levelUpgrade(int posId);
	
	/**
	 * 品阶提升
	 * @return
	 */
	public abstract void stageUpgrade();
	
	/**
	 * 获取战力
	 * @return
	 */
	public abstract int getPower();
	
	/**
	 * 武器类型
	 * @return
	 */
	public abstract int getType();
	
	/**
	 * 构造超武builder
	 * @param builder
	 */
	public abstract void buildManhattanInfo(PBManhattanInfo.Builder builder,PresetMarchManhattan presetMarchManhattan);

	/**
	 * 刷新战力
	 */
	public abstract void refreshPower();
	/**
	 * 加载作用号
	 */
	protected abstract Map<Integer, Integer> loadEffValMap();
	protected Map<Integer, Integer> loadEffValMap(boolean loadSkill){
		return Collections.emptyMap();
	}
	
	/**
	 * 是否是聚能底座
	 * @return
	 */
	public abstract boolean isBase();
	
	/**
	 * 加载作用号
	 */
	public void loadEffVal() {
		Map<Integer, Integer> effVals = loadEffValMap();
		effValMap = ImmutableMap.copyOf(effVals);
	}
	
	public ImmutableMap<Integer, Integer> getEffValMap() {
		return effValMap;
	}
	
	/**
	 * 变化通知
	 */
	public void notifyChange() {
		Set<EffType> allEff = new HashSet<>();
		loadEffVal();
		effValMap.keySet().forEach(e -> allEff.add(EffType.valueOf(e)));
		getParent().getEffect().syncEffect(getParent(), allEff.toArray(new EffType[allEff.size()]));
		refreshPower();
	}
	
	public int getEffVal(int effId, EffectParams effParams) {
		boolean isAtk = WarEff.ATK.check(effParams.getTroopEffType());
		int manhattanSwId = isAtk ? effParams.getManhattanAtkSwId() : effParams.getManhattanDefSwId();
		if (manhattanSwId > 0) {
			Map<Integer, Integer> effVals = loadEffValMap(manhattanSwId == this.getSWCfgId());
			return effVals.getOrDefault(effId, 0);
		}
		return effValMap.getOrDefault(effId, 0);
	}
	
	/**
	 * 超武部署
	 * @return
	 */
	public boolean deploy() {
		return true;
	}
	
	/**
	 * 取消部署
	 * @return
	 */
	public boolean cancelDeploy(boolean sync) {
		return true;
	}
	
	/**
	 * 超武城内展示
	 * @return
	 */
	public boolean show() {
		return true;
	}
	
	/**
	 * 取消城内展示
	 * @return
	 */
	public boolean cancelShow() {
		return true;
	}
	
	/**
	 * 是否已部署
	 * @return
	 */
	public boolean isDeployed() {
		return entity.getDeployed() > 0;
	}
	
	/**
	 * 是否设置内城展示
	 * @return
	 */
	public boolean isCityShow() {
		return entity.getCityShow() > 0;
	}
	
	/**
	 * 部件等级数据序列化
	 * @return
	 */
	public String serializePosLevel() {
		return SerializeHelper.mapToString(posLevelMap);
	}
	
	public int getSWCfgId() {
		return entity.getSwId();
	}
	
	public ManhattanSWCfg getSwCfg() {
		return null;
	}
	
	public int getPosLevel(int posId) {
		return posLevelMap.getOrDefault(posId, 0);
	}
	
	protected void updatePosLevel(int posId, int level) {
		posLevelMap.put(posId, level);
		entity.notifyUpdate();
	}
	
	public Map<Integer, Integer> getPosLevelMap() {
		return posLevelMap;
	}
	
	public int getStage() {
		return entity.getStage();
	}
	
	public void updateStage(int stage) {
		entity.setStage(stage);
	}
	
	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(entity.getPlayerId());
	}
	
	public ManhattanEntity getEntity() {
		return entity;
	}
	
	public ManhattanSWSkillCfg getUnlockedSkillCfg() {
		return null;
	}
}
