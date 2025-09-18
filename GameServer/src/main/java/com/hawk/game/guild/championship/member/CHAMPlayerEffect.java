package com.hawk.game.guild.championship.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildChampionship.PBChampionEff;
import com.hawk.game.util.EffectParams;

public class CHAMPlayerEffect extends PlayerEffect {

	private Map<EffType, Integer> effectmap;
	private List<PBChampionEff> efflist;

	public CHAMPlayerEffect(PlayerData playerData, List<PBChampionEff> efflist) {
		super(playerData);
		effectmap = new HashMap<>();
		this.efflist = efflist;
	}

	@Override
	public PlayerData getPlayerData() {

		throw new UnsupportedOperationException();
	}

	@Override
	public void setPlayerData(PlayerData playerData) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void init() {
		for (PBChampionEff eff : efflist) {
			effectmap.put(EffType.valueOf(eff.getEffectId()), eff.getValue());
		}

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
		int effVal = effectmap.getOrDefault(effType, 0);
		return effVal;
	}

	@Override
	public int getEffVal(EffType effType, EffectParams effParams) {
		int effVal = effectmap.getOrDefault(effType, 0);
		return effVal;
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
