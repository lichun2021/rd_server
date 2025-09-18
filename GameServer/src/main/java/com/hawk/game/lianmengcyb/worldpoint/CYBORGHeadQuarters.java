package com.hawk.game.lianmengcyb.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CYBORGHeadQuartersCfg;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 司令部
 *
 */
public class CYBORGHeadQuarters extends ICYBORGBuilding {
	private boolean hasNotBrodcast = true;

	public CYBORGHeadQuarters(CYBORGBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		if (hasNotBrodcast && getParent().getCurTimeMil() > getProtectedEndTime()) {
			hasNotBrodcast = false;

//			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_125).build();
//			getParent().addWorldBroadcastMsg(parames);
		}
		return super.onTick();
	}

	public static CYBORGHeadQuartersCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(CYBORGHeadQuartersCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.CYBORG_HEADQUARTERS;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public double getGuildHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getGuildHonor() * beiShu;
	}

	@Override
	public double getPlayerHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getHonor() * beiShu;
	}

	@Override
	public double getFirstControlGuildHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getFirstControlGuildHonor() * beiShu;
	}

	@Override
	public double getFirstControlPlayerHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getFirstControlHonor() * beiShu;
	}

	@Override
	public int getProtectTime() {
		return getCfg().getProtectTime();
	}

	@Override
	public int getCollectArmyMin() {
		return getCfg().getCollectArmyMin();
	}
}
