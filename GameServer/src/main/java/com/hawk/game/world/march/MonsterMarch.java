package com.hawk.game.world.march;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hawk.helper.HawkAssert;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;

/**
 * 怪物行军
 * @author zhenyu.shang
 * @since 2017年9月22日
 */
public abstract class MonsterMarch implements IWorldMarch {

	private WorldMarch marchEntity;

	public MonsterMarch(WorldMarch marchEntity) {
		HawkAssert.notNull(marchEntity);
		this.marchEntity = marchEntity;
	}

	@Override
	public void register() {
		// 注册总行军
		WorldMarchService.getInstance().registerMarchs(this);
		// 更新点信息
		WorldMarchService.getInstance().updatePointMarchInfo(this, false);
		// 注册被动行军
		WorldMarchService.getInstance().registerPlayerPassiveMarch(getTargetId(), this);
		// 添加到联盟战争
		if (needShowInGuildWar()) {
			WorldMarchService.getInstance().addGuildMarch(this);
		}
	}

	@Override
	public void remove() {
		// 移除行军实体
		marchEntity.delete();
		// 移除行军表
		WorldMarchService.getInstance().onlyRemoveMarch(this.getMarchId());
		// 移除起点记录
		WorldMarchService.getInstance().removeWorldPointMarch(getMarchEntity().getOrigionX(), getMarchEntity().getOrigionY(), this);
		// 移除终点记录
		WorldMarchService.getInstance().removeWorldPointMarch(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY(), this);
		// 移除被动行军
		WorldMarchService.getInstance().removePlayerPassiveMarch(this.getTargetId(), this);
	}

	public String getTargetId() {
		return getMarchEntity().getTargetId();
	}

	@Override
	public boolean isPassiveMarch() {
		return true;
	}

	@Override
	public int getMaxMassJoinSoldierNum(Player leader) {
		throw new IllegalArgumentException("can`t use this method!");
	}

	@Override
	public int getMaxMassJoinSoldierNum(Player leader, Player perReachMarchPlayer) {
		throw new IllegalArgumentException("can`t use this method!");
	}
	
	/**
	 * 获取基础行军速度
	 * 
	 * @param march
	 * @return
	 */
	public double getMarchBaseSpeed() {
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		armyList.addAll(getMarchEntity().getArmys());
		if (isMassMarch()) {
			Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, false);
			for (IWorldMarch tempMarch : joinMarchs) {
				armyList.addAll(tempMarch.getMarchEntity().getArmys());
			}
		}
		return WorldUtil.minSpeedInArmy(null, armyList);
	}

	public WorldMarch getMarchEntity() {
		return marchEntity;
	}

	public String getPlayerId() {
		return marchEntity.getPlayerId();
	}

	@Override
	public String getMarchId() {
		return marchEntity.getMarchId();
	}

	@Override
	public boolean isReturnBackMarch() {
		return false;
	}

	@Override
	public boolean isMarchState() {
		return getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE;
	}

	@Override
	public boolean isReachAndStopMarch() {
		return false;
	}

	@Override
	public boolean isManorMarchReachStatus() {
		return false;
	}

	@Override
	public boolean isNeedCalcTickMarch() {
		// 出征需要tick
		return getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE;
	}

	@Override
	public String toString() {
		return marchEntity.toString();
	}

	@Override
	public int compareTo(IWorldMarch o) {
		return marchEntity.compareTo(o.getMarchEntity());
	}
	
	@Override
	public Set<IWorldMarch> getQuarterMarch() {
		return Collections.emptySet();
	}
}
