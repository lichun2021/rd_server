package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZMonsterCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldPoint;

public class YQZZMonster implements IYQZZWorldPoint {
	private int cfgId;
	private int x;
	private int y;
	private int monsterId;
	private final YQZZBattleRoom parent;
	private int aoiObjId = 0;

	private YQZZMonster(YQZZBattleRoom parent) {
		this.parent = parent;
	}

	public static YQZZMonster create(YQZZBattleRoom parent, YQZZMonsterCfg monstercfg) {
		YQZZMonster result = new YQZZMonster(parent);
		result.cfgId = monstercfg.getId();
		result.monsterId = monstercfg.getMonsterId();
		return result;
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.MONSTER;
	}

	@Override
	public boolean onTick() {
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
		builder.setMonsterId(monsterId);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IYQZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(monsterId);
		return builder;
	}

	public YQZZMonsterCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(YQZZMonsterCfg.class, cfgId);
	}

	public WorldEnemyCfg getWorldEnemyCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
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
	public String getGuildId() {
		return null;
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

	public WorldPoint getEntity() {
		WorldPoint worldPoint = new WorldPoint();
		worldPoint.setId(getPointId());
		worldPoint.setX(x);
		worldPoint.setY(y);
		worldPoint.setMonsterId(monsterId);
		return worldPoint;
	}

}
