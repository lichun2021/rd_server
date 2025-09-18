package com.hawk.game.lianmengcyb;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hawk.game.GsApp;
import com.hawk.game.config.CYBORGProtoCfg;
import com.hawk.game.config.DungeonBanProtoCfg;
import com.hawk.game.lianmengcyb.msg.CYBORGJoinRoomMsg;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.player.CYBORGPlayer;
import com.hawk.game.lianmengcyb.roomstate.CYBORGPreparing;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;

public class CYBORGRoomManager extends HawkAppObj {
	private static CYBORGRoomManager Instance;

	/** 红 蓝方 */
	public enum CYBORG_CAMP {
		A(1), B(2), C(3), D(4);
		CYBORG_CAMP(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}

		public static CYBORG_CAMP valueOf(int v) {
			switch (v) {
			case 1:
				return A;
			case 2:
				return B;

			case 3:
				return C;

			case 4:
				return D;

			default:
				throw new RuntimeException("no such camp = " + v);
			}
		}
	}

	private Cache<String, ICYBORGPlayer> cache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.HOURS).build();

	public CYBORGRoomManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
	}

	public static CYBORGRoomManager getInstance() {
		return Instance;
	}

	public ICYBORGPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(ICYBORGPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(ICYBORGPlayer player) {
		cache.invalidate(player.getId());
	}

	/**
	 * 初次加入游戏要指定开始时间. 同battleId 不重复创建
	 */
	public boolean joinGame(String battleId, Player player) {
		if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {// 有行军不能加入
			player.sendError(HP.code.CYBORG_JOIN_ROOM_REQ_VALUE, Status.Error.CYBORG_HAS_PLYAER_MARCH_VALUE, 0);
			return false;
		}
		if (!player.hasGuild()) {
			return false;
		}
		if (player.getLmjyState() != null) {
			throw new RuntimeException("player is LMJY gameing gemId = " + player.getLmjyRoomId());
		}
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.CYBORGAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.CYBORGAOGUAN_ROOM).queryObject(roomXid);
		CYBORGBattleRoom room = null;
		if (roomObj != null) {
			room = (CYBORGBattleRoom) roomObj.getImpl();
		} else {
			throw new RuntimeException("game not fount id = " + battleId);
		}
		// player.setCYBORGState(CYBORGState.GAMEING);
		player.setCYBORGRoomId(roomXid.getUUID());
		// player.getPush().syncPlayerInfo();
		ICYBORGPlayer hp = makesurePlayer(player.getId());
		if (Objects.nonNull(hp) && hp.getParent() != room) {
			throw new RuntimeException("player has game id = " + hp.getParent().getId());
		}
		if (Objects.isNull(hp)) {
			hp = new CYBORGPlayer(player);
			hp.setParent(room);
		}
		CYBORGJoinRoomMsg msg = CYBORGJoinRoomMsg.valueOf(room, hp);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		return true;
	}

	public boolean hasGame(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.CYBORGAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.CYBORGAOGUAN_ROOM).queryObject(roomXid);
		return Objects.nonNull(roomObj);
	}

	public boolean creatNewBattle(long createTime, long overTime, String battleId, CYBORGExtraParam extParm) {
		try {
			extParm.setBattleId(battleId);
			HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.CYBORGAOGUAN_ROOM, battleId);
			CYBORGBattleRoom room = (CYBORGBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setCreateTime(createTime);
			room.setOverTime(overTime);
			room.setBattleCfgId(0);
			room.setExtParm(extParm);
			room.init();

			room.setState(new CYBORGPreparing(room));
			// CYBORGJoinRoomMsg msg = CYBORGJoinRoomMsg.valueOf(room, leader);
			// HawkApp.getInstance().postMsg(leader.getXid(), msg);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	@Override
	public boolean onTick() {
		return super.onTick();
	}

	/** 取得所有房间 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<CYBORGBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.CYBORGAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<CYBORGBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getCYBORGState())) {
			return false;
		}
		
		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.Error.CYBORG_BAN_OP_VALUE, 0);
				return true;
			}
		}
		
		CYBORGProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(CYBORGProtoCfg.class, protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.Error.CYBORG_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		ICYBORGPlayer gp = CYBORGRoomManager.getInstance().makesurePlayer(player.getId());
		
		if (lcfg.isMulti()) {
			gp.getParent().onProtocol(CYBORGProtocol.valueOf(protocol, gp));
		} else {
			HawkTaskManager.getInstance().postProtocol(gp.getParent().getXid(), CYBORGProtocol.valueOf(protocol, gp));
		}
		
		return true;
	}
}
