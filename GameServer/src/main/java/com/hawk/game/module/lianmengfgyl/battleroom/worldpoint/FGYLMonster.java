package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLMonsterCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;

public class FGYLMonster implements IFGYLWorldPoint {
	private int cfgId;
	private int x;
	private int y;
	private int monsterId;
	private final FGYLBattleRoom parent;
	private int aoiObjId = 0;

	private FGYLMonster(FGYLBattleRoom parent, int cfgId) {
		this.parent = parent;
		this.cfgId = cfgId;
		FGYLMonsterCfg monsterCfg = getCfg();
		this.monsterId = monsterCfg.getMonsterId();
	}

	public static FGYLMonster create(FGYLBattleRoom parent, int cfgId) {
		FGYLMonster result = new FGYLMonster(parent, cfgId);
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
	public WorldPointPB.Builder toBuilder(IFGYLPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(monsterId);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IFGYLPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(monsterId);
		return builder;
	}

	public FGYLMonsterCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(FGYLMonsterCfg.class, cfgId);
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
	public FGYLBattleRoom getParent() {
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

}
