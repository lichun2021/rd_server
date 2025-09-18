package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYHeadQuartersCfg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;

/**
 * 司令部
 *
 */
public class TBLYHeadQuarters extends ITBLYBuilding {
	private boolean hasNotBrodcast = true;

	public TBLYHeadQuarters(TBLYBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		if (hasNotBrodcast && getParent().getCurTimeMil() > getProtectedEndTime()) {
			hasNotBrodcast = false;

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_125).build();
			getParent().addWorldBroadcastMsg(parames);
		}
		
		if (getState() == TBLYBuildState.ZHAN_LING && StringUtils.isEmpty(getParent().firstControlHeXin)) {
			getParent().firstControlHeXin = getGuildId();
		}
		
		return super.onTick();
	}

	public static TBLYHeadQuartersCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYHeadQuartersCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.TBLY_HEADQUARTERS;
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
	
	@Override
	public int getPointTime() {
		return getCfg().getPointTime();
	}

	@Override
	public double getPointBase() {
		return getCfg().getPointBase();
	}

	@Override
	public double getPointSpeed() {
		return getCfg().getPointSpeed();
	}

	@Override
	public double getPointMax() {
		return getCfg().getPointMax();
	}
}
