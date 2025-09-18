package com.hawk.game.battle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.hawk.os.HawkException;

import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class NPCEffect extends PlayerEffect {
	private Player parent;
	private Map<EffType, Integer> effectmap;

	public NPCEffect(Player parent,PlayerData playerData) {
		super(playerData);
		effectmap = new HashMap<>();
		this.parent = parent;
	}

	@Override
	public PlayerData getPlayerData() {
		return parent.getData();
	}

	@Override
	public void setPlayerData(PlayerData playerData) {

		throw new UnsupportedOperationException();
	}

	/**如果加入了装备就需要调用*/
	@Override
	public void init() {
		effectmap.putAll(getPlayerData().getArmourEffect(1));
	}

	@Override
	public void initEffectVip() {

		throw new UnsupportedOperationException();
	}

	@Override
	public void initEffectVip(Player player) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void initEffectStatus() {

		throw new UnsupportedOperationException();
	}

	@Override
	public void clearEffectTalent(Player player) {

		throw new UnsupportedOperationException();
	}

	@Override
	public Set<EffType> initEffectTalent() {

		throw new UnsupportedOperationException();
	}

	@Override
	public void initEffectTalent(Player player) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void initEffectTech() {

		throw new UnsupportedOperationException();
	}

	@Override
	public void initEffectManor() {

		throw new UnsupportedOperationException();
	}

	@Override
	public void initEffectBuilding() {

		throw new UnsupportedOperationException();
	}

	@Override
	public void addEffectVip(Player player, int effId, int effVal) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void addEffectBuilding(Player player, int effId, int effVal) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void resetEffectEquip(Player player, EffType[] effTypes) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void addEffectTech(Player player, TechnologyEntity entity) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void resetEffectBuilding(int effId, int effVal) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void resetEffectBuilding(Player player, int effId, int effVal) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void syncEffect(Player player, EffType... types) {

		throw new UnsupportedOperationException();
	}

	@Override
	public int getEffVal(EffType effType, String targetId, EffectParams effParams) {
		try {
			// 英雄出征
			int effHero = 0;
			if (Objects.nonNull(effParams.getHeroIds()) && !effParams.getHeroIds().isEmpty()) {
				List<PlayerHero> heros = this.parent.getHeroByCfgId(effParams.getHeroIds());
				for (PlayerHero hero : heros) {
					effHero = effHero + hero.getBattleEffect(effType, EffectParams.getDefaultVal());
				}
			}
			int effSuperSoldier = 0;
			Optional<SuperSoldier> sOP = parent.getSuperSoldierByCfgId(effParams.getSuperSoliderId());
			if (sOP.isPresent()) {
				effSuperSoldier = sOP.get().battleEffect().getOrDefault(effType, 0);
			}
			int effVal = effHero + effectmap.getOrDefault(effType, 0) + effSuperSoldier;
			return effVal;

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	public int addEffectVal(EffType effType, int val) {
		return effectmap.merge(effType, val, (v1, v2) -> v1 + v2);
	}

	@Override
	public int getEffVal(EffType effType) {

		return getEffVal(effType, new EffectParams());
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {

		throw new UnsupportedOperationException();
	}

	@Override
	public long getStatusEndTime(EffType effType) {

		throw new UnsupportedOperationException();
	}

	@Override
	public int getCollectEff() {

		throw new UnsupportedOperationException();
	}

}
