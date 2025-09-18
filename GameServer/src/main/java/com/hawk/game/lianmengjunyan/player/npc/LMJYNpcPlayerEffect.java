package com.hawk.game.lianmengjunyan.player.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.os.HawkException;

import com.hawk.game.config.LMJYNpcCfg;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayerEffect;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class LMJYNpcPlayerEffect extends ILMJYPlayerEffect {
	private Map<EffType, Integer> effectmap;

	public LMJYNpcPlayerEffect(PlayerData playerData) {
		super(playerData);
		effectmap = new HashMap<>();
	}

	@Override
	public void init() {
		if (!effectmap.isEmpty()) {
			return;
		}
		LMJYNPCPlayerData playerdata = (LMJYNPCPlayerData) getPlayerData();
		LMJYNpcCfg npcCfg = playerdata.getNpcCfg();
		// effectList;//="1003_3000|1011_2000|1019_1000"
		try {
			effectmap.putAll(getPlayerData().getArmourEffect(1));
			for (Entry<EffType, Integer> ent : npcCfg.getEffectmap().entrySet()) {
				effectmap.merge(ent.getKey(), ent.getValue(), (v1, v2) -> v1 + v2);
			}
			initEffectHero();
			initEffectSuperSoldier();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public int getEffVal(EffType effType, String targetId, EffectParams effParams) {

		int effVal = effectmap.getOrDefault(effType, 0);
		try {
			// 英雄出征
			int effHero = 0;// 军演英雄属性永不生效 getHeroMarchEffVal(effType, effParams.getHeroIds());
			// 神兽出征
			int effSS = getSuperSoldierMarchEffVal(effType, effParams.getSuperSoliderId());

			effVal = effVal + effHero + effSS;

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return effVal;
	}

	@Override
	public int getEffVal(EffType effType) {

		return getEffVal(effType, EffectParams.getDefaultVal());
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {

		return getEffVal(effType, new EffectParams());
	}

	public int addEffectVal(EffType effType, int val) {
		return effectmap.merge(effType, val, (v1, v2) -> v1 + v2);
	}

}
