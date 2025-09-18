package com.hawk.game.module.lianmenxhjz.battleroom.worldpoint;

import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZGuildBaseInfo;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;

/**
 * 月球基地
 *
 */
public class XHJZBase extends IXHJZBuilding {
	private XHJZ_CAMP bornCamp;
	
	public XHJZBase(XHJZBattleRoom parent) {
		super(parent);
	}
	



	@Override
	public boolean onTick() {
		return true;
	}

	public void setBornCamp(XHJZ_CAMP bornCamp) {
		this.bornCamp = bornCamp;
	}

 
	@Override
	public boolean underGuildControl(String guildId) {
		XHJZGuildBaseInfo base = getParent().getCampBase(guildId);
		if (base == null) {
			return false;
		}
		return base.getCamp() == bornCamp;
	}
	

	public WorldPointPB.Builder toBuilder(IXHJZPlayer viewer) {
		// TODO Auto-generated method stub
		WorldPointPB.Builder builder = super.toBuilder(viewer);
		builder.setFlagView(bornCamp.intValue()); // 1 红 ,2 蓝
		builder.setProtectedEndTime(Long.MAX_VALUE);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IXHJZPlayer viewer) {
		// TODO Auto-generated method stub
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewer);
		builder.setFlagView(bornCamp.intValue()); // 1 红 ,2 蓝
		builder.setProtectedEndTime(Long.MAX_VALUE);
		return builder;
	}


}
