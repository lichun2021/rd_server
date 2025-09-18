package com.hawk.game.module.dayazhizhan.battleroom.worldpoint;

import com.google.common.collect.ImmutableList;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZCollectFuelMarch;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;

public abstract class IDYZZFuelBank implements IDYZZWorldPoint {
	private int x;
	private int y;
	private int redis;
	private int level;
	private int resourceId = 300502;
	private int totalResNum = 50;
	private int remainResNum = 50;
	private ImmutableList<Double> collectSpeed;
	private int minRes;
	private DYZZCollectFuelMarch march;
	private final DYZZBattleRoom parent;

	public IDYZZFuelBank(DYZZBattleRoom parent) {
		this.parent = parent;
	}

	@Override
	public String getGuildId() {
		if (march == null) {
			return "";
		}
		return march.getParent().getDYZZGuildId();
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.DYZZ_FUELBANK;
	}

	@Override
	public boolean onTick() {
		if (remainResNum <= 0) {
			this.removeWorldPoint();
		}
		if (march != null && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			march = null;
			getParent().worldPointUpdate(this);
		}
		if (march == null && remainResNum <= minRes) {
			this.removeWorldPoint();
		}
		return true;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getPointId() {
		return GameUtil.combineXAndY(x, y);
	}

	@Override
	public WorldPointPB.Builder toBuilder(IDYZZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());

		if (march != null) {
			IDYZZPlayer owner = march.getParent();
			String thisPlayerId = owner.getId();
			builder.setPlayerId(thisPlayerId);
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLv());
			builder.setMarchId(march.getMarchId());

			builder.setGuildId(owner.getDYZZGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			builder.setManorState(DYZZBuildState.ZHAN_LING.getNumber());
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 2 蓝
			BuilderUtil.buildMarchEmotion(builder, march.getMarchEntity());
			if (march.getParent() == viewer) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
			}
		}

		builder.setResourceId(resourceId);
		builder.setTerriLevel(level);

		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());

		if (march != null) {
			IDYZZPlayer owner = march.getParent();
			builder.setPlayerId(owner.getId());
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLevel());
			builder.setPlayerIcon(owner.getIcon());
			builder.setPlayerPfIcon(owner.getPfIcon());
			builder.setManorState(DYZZBuildState.ZHAN_LING.getNumber());
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 2 蓝
			builder.setGuildId(owner.getDYZZGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			BuilderUtil.buildMarchEmotion(builder, march.getMarchEntity());
			if (march.getParent() == viewer) {
				builder.setHasMarchStop(true);
				builder.setFormation(march.getFormationInfo());
			}
		}

		builder.setResourceId(resourceId);
		builder.setTerriLevel(level);
		builder.setTotalRemainResNum((int) remainResNum);

		if (march != null) {
			builder.setMarchId(march.getMarchId());
			// 采集速度
			double speed = march.getCollectSpeed();
			builder.setCollectSpeed(speed);
			builder.setCurrentRemainResNum((int) remainResNum);
		}

		return builder;
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public int getTotalResNum() {
		return totalResNum;
	}

	/**
	 * @param col
	 *            希望采集量
	 * @return 实际采集
	 */
	public int doCollect(int col) {
		col = Math.min(this.remainResNum, col);
		this.remainResNum -= col;
		return col;
	}

	public void setTotalResNum(int totalResNum) {
		this.totalResNum = totalResNum;
	}

	public double getRemainResNum() {
		return remainResNum;
	}

	public void setRemainResNum(int remainResNum) {
		this.remainResNum = remainResNum;
	}

	public DYZZCollectFuelMarch getMarch() {
		return march;
	}

	public void setMarch(DYZZCollectFuelMarch march) {
		this.march = march;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public DYZZBattleRoom getParent() {
		return parent;
	}

	@Override
	public void removeWorldPoint() {
		if (march != null) {
			march.onMarchReturn(march.getMarchEntity().getTerminalId(), march.getMarchEntity().getOrigionId(), march.getArmys());
		}

		getParent().removeViewPoint(this);
	}

	@Override
	public int getRedis() {
		return redis;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public ImmutableList<Double> getCollectSpeed() {
		return collectSpeed;
	}

	public void setCollectSpeed(ImmutableList<Double> collectSpeedList) {
		this.collectSpeed = collectSpeedList;
	}

	public int getMinRes() {
		return minRes;
	}

	public void setMinRes(int minRes) {
		this.minRes = minRes;
	}

	public void setRedis(int redis) {
		this.redis = redis;
	}

}
