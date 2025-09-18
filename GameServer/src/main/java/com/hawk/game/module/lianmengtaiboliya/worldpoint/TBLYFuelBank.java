package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYFuelBankCfg;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYCollectFuelMarch;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

public class TBLYFuelBank implements ITBLYWorldPoint {
	private int cfgId;
	private int x;
	private int y;
	private int level;
	private int resourceId = 300502;
	private int totalResNum = 50;
	private int remainResNum = 50;
	private int minRes;
	private TBLYCollectFuelMarch march;
	private final TBLYBattleRoom parent;

	private TBLYFuelBank(TBLYBattleRoom parent, int cfgId) {
		this.parent = parent;
		this.cfgId = cfgId;
	}

	public static TBLYFuelBank create(TBLYBattleRoom parent, int cfgId) {
		TBLYFuelBank result = new TBLYFuelBank(parent, cfgId);
		TBLYFuelBankCfg cfg = result.getCfg();
		result.totalResNum = (int) (cfg.getTotalRes() * (1 + parent.getCurBuff530Val(EffType.TBLY530_659) * GsConst.EFF_PER));
		result.remainResNum = result.totalResNum;
		result.minRes = cfg.getMinRes();
		result.resourceId = cfg.getResourceId();
		result.level = cfg.getLevel();
		return result;
	}

	@Override
	public String getGuildId() {
		if (march == null) {
			return "";
		}
		return march.getParent().getGuildId();
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.TBLY_FUELBANK;
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
	public WorldPointPB.Builder toBuilder(ITBLYPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());

		if (march != null) {
			ITBLYPlayer owner = march.getParent();
			String thisPlayerId = owner.getId();
			builder.setPlayerId(thisPlayerId);
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLv());
			builder.setMarchId(march.getMarchId());

			builder.setGuildId(owner.getGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			builder.setManorState(TBLYBuildState.ZHAN_LING.intValue());
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
	public WorldPointDetailPB.Builder toDetailBuilder(ITBLYPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());

		if (march != null) {
			ITBLYPlayer owner = march.getParent();
			builder.setPlayerId(owner.getId());
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLevel());
			builder.setPlayerIcon(owner.getIcon());
			builder.setPlayerPfIcon(owner.getPfIcon());
			builder.setManorState(TBLYBuildState.ZHAN_LING.intValue());
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 2 蓝
			builder.setGuildId(owner.getGuildId());
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

	public TBLYFuelBankCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(TBLYFuelBankCfg.class, cfgId);
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

	public TBLYCollectFuelMarch getMarch() {
		return march;
	}

	public void setMarch(TBLYCollectFuelMarch march) {
		this.march = march;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public TBLYBattleRoom getParent() {
		return parent;
	}

	@Override
	public void removeWorldPoint() {
		if (march != null) {
			march.onMarchReturn(march.getMarchEntity().getTerminalId(), march.getMarchEntity().getOrigionId(), march.getArmys());
		}

		getParent().removeViewPoint(this);
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

}
