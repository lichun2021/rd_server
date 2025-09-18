package com.hawk.game.lianmengcyb.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CYBORGBuildBuffCfg;
import com.hawk.game.config.CYBORGBuildBuffLevelExtraCfg;
import com.hawk.game.lianmengcyb.CYBORGGuildBaseInfo;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGIronCurtainDevice;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGNian;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;

public class CYBORGPlayerEffect extends ICYBORGPlayerEffect {
	private PlayerEffect source;
	/** 副本内额外buff */
	private Map<EffType, Integer> effectmap = new HashMap<>();

	public CYBORGPlayerEffect(CYBORGPlayerData playerData) {
		super(playerData);
		source = playerData.getSource().getPlayerEffect();
	}

	@Override
	public void resetEffectDress(Player player) {
		source.resetEffectDress(player);
	}

	@Override
	public int getEffVal(EffType effType, String targetId, EffectParams effParams) {
		if (effType == EffType.CITY_ENEMY_MARCH_SPD) {
			return 0;
		}
		try {
			int result = source.getEffVal(effType, targetId, effParams);// + effectmap.getOrDefault(effType, 0);// + buildingAdd(effType);
			// 加小龙buff
			CYBORGGuildBaseInfo campBase = getParent().getParent().getCampBase(getParent().getCamp());
			int nianKillCount = campBase.campNianKillCount;
			int nianbuff = CYBORGNian.getCfg().getKillBuff(effType, nianKillCount);
			// haoling
			int order = campBase.orderCollection.getEffectVal(effType);
			// 防守本方建筑
			CYBORGPlayer player = getParent();
			
			int ble = 0;
			CYBORGGuildBaseInfo binfo = getParent().getParent().getCampBase(player.getCamp());
			CYBORGBuildBuffLevelExtraCfg blecfg = HawkConfigManager.getInstance().getConfigByKey(CYBORGBuildBuffLevelExtraCfg.class, binfo.buildBuffLevelExtra);
			if(Objects.nonNull(blecfg)){
				ble = blecfg.getControleBuffMap().getOrDefault(effType, 0);
			}
			
			int defbuild = 0;
			ICYBORGWorldPoint point = getParent().getParent().getWorldPoint(effParams.getBattlePoint()).orElse(null);
			if (point instanceof ICYBORGBuilding) {
				ICYBORGBuilding build = (ICYBORGBuilding) point;
				if (build.getTreeCfg().getCamp() == player.getCamp().intValue()) {
					CYBORGBuildBuffCfg bbfg = HawkConfigManager.getInstance().getCombineConfig(CYBORGBuildBuffCfg.class, build.getTreeCfg().getBuffId(),
							binfo.buildBuffCfgLevel);
					if (Objects.nonNull(bbfg)) {
						defbuild = bbfg.getControleBuffMap().getOrDefault(effType, 0);
					}
				}
			}
			
			if (point == player) {
				Object buffId = getParent().getParent().getCfg().getPlayerBuildBuff();
				CYBORGBuildBuffCfg bbfg = HawkConfigManager.getInstance().getCombineConfig(CYBORGBuildBuffCfg.class, buffId,
						binfo.buildBuffCfgLevel);
				if (Objects.nonNull(bbfg)) {
					defbuild = bbfg.getControleBuffMap().getOrDefault(effType, 0);
				}
			}
			
			return result + nianbuff +order + ble + defbuild;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public int getEffectTech(int effId) {
		try {
			return source.getEffectTech(effId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int buildingAdd(EffType effType) {
		int result = 0;
		Integer iroVal = CYBORGIronCurtainDevice.getCfg().getControleBuffMap().getOrDefault(effType, 0);
		if (iroVal > 0) {
			for (CYBORGIronCurtainDevice w : getParent().getParent().getCYBORGBuildingByClass(CYBORGIronCurtainDevice.class)) {
				if (w.underGuildControl(getParent().getGuildId())) {
					result += iroVal;
				}
			}
		}
		return result;
	}

	@Override
	public int getEffVal(EffType effType) {
		return this.getEffVal(effType, EffectParams.getDefaultVal());
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {
		return this.getEffVal(effType, EffectParams.getDefaultVal());
	}

}
