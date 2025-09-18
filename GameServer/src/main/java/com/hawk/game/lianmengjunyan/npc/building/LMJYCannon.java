package com.hawk.game.lianmengjunyan.npc.building;

import com.hawk.game.lianmengjunyan.ILMJYWorldPoint;
import com.hawk.game.protocol.World.WorldPointPB.Builder;
import com.hawk.game.protocol.World.WorldPointType;

/***
 * 巨炮
 * @author lwt
 * @date 2018年10月30日
 */
public class LMJYCannon implements ILMJYWorldPoint{

	@Override
	public Builder toBuilder(String viewerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public com.hawk.game.protocol.World.WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onTick() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public WorldPointType getPointType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getPointId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
