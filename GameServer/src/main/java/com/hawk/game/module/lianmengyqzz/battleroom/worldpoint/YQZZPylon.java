package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZGuildBaseInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZPylonCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZPylonMarch;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldPoint;

public class YQZZPylon implements IYQZZWorldPoint {
	private int cfgId;
	private int pylonId;
	private int x;
	private int y;
	private final YQZZBattleRoom parent;
	private int aoiObjId = 0;

	private YQZZPylonMarch collectMarch;

	private YQZZPylon(YQZZBattleRoom parent) {
		this.parent = parent;
	}

	public static YQZZPylon create(YQZZBattleRoom parent, YQZZPylonCfg monstercfg) {
		YQZZPylon result = new YQZZPylon(parent);
		result.cfgId = monstercfg.getId();
		result.pylonId = monstercfg.getPylonId();
		return result;
	}

	@Override
	public boolean onTick() {
		if (collectMarch != null && collectMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			collectMarch = null;
			this.worldPointUpdate();
		}
		return true;
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.PYLON;
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
		builder.setResourceId(pylonId);
		builder.setStrongpointId(pylonId);
		if (collectMarch != null) {
			IYQZZPlayer owner = collectMarch.getParent();
			String thisPlayerId = owner.getId();
			builder.setPlayerId(thisPlayerId);
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLv());
			builder.setMarchId(collectMarch.getMarchId());
			builder.setServerId(collectMarch.getParent().getMainServerId());
			builder.setGuildId(owner.getGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			builder.setManorState(YQZZBuildState.ZHAN_LING.intValue());
			builder.setFlagView(collectMarch.getParent().getCamp().intValue()); // 1 红 2 蓝
			BuilderUtil.buildMarchEmotion(builder, collectMarch.getMarchEntity());

			builder.setLastActiveTime(collectMarch.getMarchEntity().getResEndTime() - getParent().getCurTimeMil());
		}
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IYQZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setResourceId(pylonId);
		builder.setStrongpointId(pylonId);
		if (collectMarch != null) {
			IYQZZPlayer owner = collectMarch.getParent();
			String thisPlayerId = owner.getId();
			builder.setPlayerId(thisPlayerId);
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLv());
			builder.setMarchId(collectMarch.getMarchId());
			builder.setServerId(collectMarch.getParent().getMainServerId());
			builder.setGuildId(owner.getGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			builder.setManorState(YQZZBuildState.ZHAN_LING.intValue());
			builder.setFlagView(collectMarch.getParent().getCamp().intValue()); // 1 红 2 蓝
			BuilderUtil.buildMarchEmotion(builder, collectMarch.getMarchEntity());

			builder.setLastActiveTime(collectMarch.getMarchEntity().getResEndTime() - getParent().getCurTimeMil());
		}
		
		return builder;
	}

	public YQZZPylonCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(YQZZPylonCfg.class, cfgId);
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
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
		return 2;
	}

	public WorldPoint getEntity() {
		WorldPoint worldPoint = new WorldPoint();
		worldPoint.setId(getPointId());
		worldPoint.setX(x);
		worldPoint.setY(y);
		worldPoint.setResourceId(cfgId);
		return worldPoint;
	}
	
	public boolean underGuildControl(String guildId) {
		return Objects.equals(getGuildId(), guildId);
	}

	public boolean underNationControl(String guildId) {
		return Objects.equals(getServerId(), getParent().getCampBase(guildId).campServerId);
	}

	public int getResourceId() {
		return pylonId;
	}

	public YQZZPylonMarch getCollectMarch() {
		return collectMarch;
	}

	public void setCollectMarch(YQZZPylonMarch collectMarch) {
		this.collectMarch = collectMarch;
	}

	public String getServerId() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.campServerId;
	}

	@Override
	public String getGuildId() {
		if (collectMarch == null) {
			return "";
		}
		return collectMarch.getParent().getGuildId();
	}

	public String getGuildName() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.campGuildName;
	}

	public String getGuildTag() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return "";
		}
		return binfo.campGuildTag;
	}

	public int getGuildFlag() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());
		if (binfo == null) {
			return 0;
		}
		return binfo.campguildFlag;
	}

	public String getGuildServerId() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());

		if (binfo == null) {
			return "";
		}
		return binfo.campServerId;
	}

	public YQZZ_CAMP getGuildCamp() {
		YQZZGuildBaseInfo binfo = getParent().getCampBase(getGuildId());

		if (binfo == null) {
			return YQZZ_CAMP.FOGGY;
		}
		return binfo.camp;
	}

	public String getPlayerId() {
		if (collectMarch == null) {
			return "";
		}
		return collectMarch.getParent().getId();
	}

	public String getPlayerName() {
		if (collectMarch == null) {
			return "";
		}
		return collectMarch.getParent().getName();
	}

}
