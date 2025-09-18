package com.hawk.game.lianmengstarwars.worldpoint;

import java.util.List;
import java.util.Objects;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.SWCommandCenterCfg;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;

/**
 * 现在是剪塔了
 *
 */
public class SWCommandCenter extends ISWBuilding {
	private long lastTickTime;

	public SWCommandCenter(SWBattleRoom parent) {
		super(parent);
	}

	@Override
	public boolean onTick() {
		super.onTick();
		if (getParent().isHasNotBrodcast_SW_182() && getParent().getCurTimeMil() > getProtectedEndTime()) {
			getParent().setHasNotBrodcast_SW_182(false);
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.SW_182).build();
			getParent().addWorldBroadcastMsg(parames);
		}
		long currTime = getParent().getCurTimeMil();
		long tickPeriod = getParent().getCfg().getTickPeriod();
		if (getState() == SWBuildState.ZHAN_LING && currTime > (lastTickTime + tickPeriod)) {
			SWHeadQuarters headQuarters = getParent().getSWBuildingByClass(SWHeadQuarters.class).get(0);
			if (!Objects.equals(getGuildId(), headQuarters.getGuildId())) {
				List<ISWWorldMarch> stayMarches = getParent().getPointMarches(headQuarters.getPointId(),
						WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
				int towerAtk = getParent().getCfg().getTowerAtk();
				for (ISWWorldMarch headMarch : stayMarches) {
					// 可攻击则进行一次攻击,并记录攻击结果,添加数据
					List<ArmyInfo> armys = headMarch.getMarchEntity().getArmys();
					for (ArmyInfo armyInfo : armys) {
						armyInfo.killByTower(towerAtk);
					}
				}
//				System.out.println("****"+getClass().getSimpleName() + "ATTACK ");
			}
			lastTickTime = currTime;
		}
		return true;
	}

	public static SWCommandCenterCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(SWCommandCenterCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.SW_COMMAND_CENTER;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public double getGuildHonorPerSecond() {
		return getCfg().getGuildHonor();
	}

	@Override
	public double getPlayerHonorPerSecond() {
		return getCfg().getHonor();
	}

	@Override
	public double getFirstControlGuildHonor() {
		return getCfg().getFirstControlGuildHonor();
	}

	@Override
	public double getFirstControlPlayerHonor() {
		return getCfg().getFirstControlHonor();
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
	public int getWorldPointRadius() {
		return 4;
	}
}
