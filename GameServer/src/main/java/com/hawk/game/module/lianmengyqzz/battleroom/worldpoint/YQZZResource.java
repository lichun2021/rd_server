package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZResourceCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZCollectResMarch;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldPoint;

/**资源点*/
public class YQZZResource implements IYQZZWorldPoint {
	private int cfgId;
	private int x;
	private int y;
	private int level;
	private int resourceId = 300502;
	private int totalResNum = 50;
	private int remainResNum = 50;
	private int minRes;
	private YQZZCollectResMarch march;
	private final YQZZBattleRoom parent;
	private int aoiObjId = 0;
	private WorldPoint worldPoint;

	private YQZZResource(YQZZBattleRoom parent) {
		this.parent = parent;
	}

	public static YQZZResource create(YQZZBattleRoom parent, YQZZResourceCfg resourcecfg) {
		YQZZResource result = new YQZZResource(parent);
		result.cfgId = resourcecfg.getId();
		result.resourceId = resourcecfg.getResourceId();
		WorldResourceCfg cfg = result.getResourceCfg();
		result.totalResNum = cfg.getResNum();
		result.remainResNum = cfg.getResNum();
		result.minRes = 100;
		result.level = cfg.getLevel();

		WorldPoint worldPoint = new WorldPoint();
		worldPoint.setResourceId(result.getResourceId());
		result.worldPoint = worldPoint;
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
		return WorldPointType.RESOURCE;
	}

	@Override
	public boolean onTick() {
		// System.out.println("x "+x +" , "+y);
		if (remainResNum <= 0) {
			this.removeWorldPoint();
		}
		if (march != null && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			march = null;
			this.worldPointUpdate();
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
	public WorldPointPB.Builder toBuilder(IYQZZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());

		if (march != null) {
			IYQZZPlayer owner = march.getParent();
			String thisPlayerId = owner.getId();
			builder.setPlayerId(thisPlayerId);
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLv());
			builder.setMarchId(march.getMarchId());

			builder.setGuildId(owner.getGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			builder.setManorState(YQZZBuildState.ZHAN_LING.intValue());
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 2 蓝
			BuilderUtil.buildMarchEmotion(builder, march.getMarchEntity());
		}

		builder.setResourceId(resourceId);
		builder.setTerriLevel(level);

		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IYQZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());

		if (march != null) {
			IYQZZPlayer owner = march.getParent();
			builder.setPlayerId(owner.getId());
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLevel());
			builder.setPlayerIcon(owner.getIcon());
			builder.setPlayerPfIcon(owner.getPfIcon());
			builder.setManorState(YQZZBuildState.ZHAN_LING.intValue());
			builder.setFlagView(march.getParent().getCamp().intValue()); // 1 红 2 蓝
			builder.setGuildId(owner.getGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			BuilderUtil.buildMarchEmotion(builder, march.getMarchEntity());
		}

		builder.setResourceId(resourceId);
		builder.setTerriLevel(level);
		builder.setTotalRemainResNum((int) remainResNum);

		if (march != null) {
			builder.setMarchId(march.getMarchId());
			// 采集速度
			double speed = march.getCollectSpeed();
			builder.setCollectSpeed(speed);
			builder.setTotalRemainResNum(remainResNum + march.getCollected());
			builder.setCurrentRemainResNum(remainResNum);
		}

		return builder;
	}

	public WorldResourceCfg getResourceCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, resourceId);
	}

	public YQZZResourceCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(YQZZResourceCfg.class, cfgId);
	}

	@Override
	public boolean needJoinGuild() {
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

	public YQZZCollectResMarch getMarch() {
		return march;
	}

	public void setMarch(YQZZCollectResMarch march) {
		this.march = march;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public YQZZBattleRoom getParent() {
		return parent;
	}

	@Override
	public void removeWorldPoint() {
		if (march != null) {
			march.onMarchReturn(march.getMarchEntity().getTerminalId(), march.getMarchEntity().getOrigionId(), march.getArmys());
		}

		getParent().getWorldPointService().removeViewPoint(this);
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	@Override
	public int getAoiObjId() {
		return aoiObjId;
	}

	@Override
	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	@Override
	public int getGridCnt() {
		return 1;
	}

	public WorldPoint getWorldPoint() {
		return worldPoint;
	}

	public void setWorldPoint(WorldPoint worldPoint) {
		this.worldPoint = worldPoint;
	}

}
