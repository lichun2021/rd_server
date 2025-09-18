package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.GhostTower.GhostTowerChanllageResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.world.service.WorldResTreasurePointService;

/**
 * 创建幽灵工厂怪
 * 
 * @author che
 *
 */
public class GhostTowerMonsterCreateInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	
	private int pointId;
	
	private int posX;
	
	private int posY;

	

	public GhostTowerMonsterCreateInvoker(Player player,int pointId,int posX,int posY) {
		this.player = player;
		this.pointId = pointId;
		this.posX = posX;
		this.posY = posY;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		WorldResTreasurePointService.getInstance().recordGhostPoint(player.getId(), pointId);
		GhostTowerChanllageResp.Builder builder = GhostTowerChanllageResp.newBuilder(); 
		builder.setPosX(this.posX);
		builder.setPoxY(this.posY);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GHOST_TOWER_CHALLANGE_RESP,builder));
		return false;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	


	
}
