package com.hawk.game.module.dayazhizhan.battleroom.worldpoint;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZFameHallCfg;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZBattleRoomFameHallMember;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.protocol.World.PBDYZZFameHallMember;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;

public class DYZZFameHall implements IDYZZWorldPoint{
	
	DYZZBattleRoom parent;
	
	private int x;
	private int y;
	private int redis;
	
	private List<PBDYZZFameHallMember> fameHallMembers;

	public DYZZFameHall(DYZZBattleRoom parent) {
		this.parent = parent;
	}
	
	public void setFameHallMembers(List<PBDYZZFameHallMember> fameHallMembers) {
		this.fameHallMembers = fameHallMembers;
	}
	
	@Override
	public DYZZBattleRoom getParent() {
		return this.parent;
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getRedis() {
		return this.redis;
	}

	@Override
	public int getPointId() {
		return GameUtil.combineXAndY(x, y);
	}

	@Override
	public String getGuildId() {
		return null;
	}

	@Override
	public WorldPointPB.Builder toBuilder(IDYZZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.addAllDyzzFameHallMembers(this.fameHallMembers);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.addAllDyzzFameHallMembers(this.fameHallMembers);
		return builder;
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.DYZZ_FAME_HALL;
	}

	@Override
	public boolean onTick() {
		return true;
	}

	@Override
	public boolean needJoinGuild() {
		return false;
	}

	@Override
	public void removeWorldPoint() {
		
	}
	
	
	public void setX(int x) {
		this.x = x;
	}
	
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setRedis(int redis) {
		this.redis = redis;
	}
	
	
	public static DYZZFameHallCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(DYZZFameHallCfg.class);
	}
	
	
	public static DYZZFameHall create(DYZZBattleRoom parent) {
		DYZZFameHall icd = new DYZZFameHall(parent);
		List<DYZZBattleRoomFameHallMember>  fameHall = parent.getExtParm().getFameHallMembers();
		List<PBDYZZFameHallMember> fameHallMembers = new ArrayList<>();
		fameHall.forEach(member->fameHallMembers.add(member.createPBDYZZFameHallMember()));
		icd.setFameHallMembers(fameHallMembers);
		return icd;
	}

}
