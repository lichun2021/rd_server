package com.hawk.game.module.dayazhizhan.battleroom;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.xid.HawkXID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hawk.game.GsApp;
import com.hawk.game.config.DungeonBanProtoCfg;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZProtoCfg;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZExtraParam;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZJoinRoomMsg;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.player.DYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.roomstate.DYZZPreparing;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;

public class DYZZRoomManager extends HawkAppObj {
	private static DYZZRoomManager Instance;

	/** 红 蓝方 */
	public enum DYZZCAMP {
		A(1), B(2);
		DYZZCAMP(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}

	private Cache<String, IDYZZPlayer> cache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.HOURS).build();

	public DYZZRoomManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
	}

	public static DYZZRoomManager getInstance() {
		return Instance;
	}

	public IDYZZPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(IDYZZPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(IDYZZPlayer player) {
		cache.invalidate(player.getId());
	}

	/**
	 * 初次加入游戏要指定开始时间. 同battleId 不重复创建
	 */
	public boolean joinGame(String battleId, Player player) {
		if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {// 有行军不能加入
			player.sendError(HP.code.TBLY_JOIN_ROOM_REQ_VALUE, Status.Error.TBLY_HAS_PLYAER_MARCH_VALUE, 0);
			return false;
		}
		if (player.getLmjyState() != null) {
			throw new RuntimeException("player is LMJY gameing gemId = " + player.getLmjyRoomId());
		}
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.DYZZAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.DYZZAOGUAN_ROOM).queryObject(roomXid);
		DYZZBattleRoom room = null;
		if (roomObj != null) {
			room = (DYZZBattleRoom) roomObj.getImpl();
		} else {
			throw new RuntimeException("game not fount id = " + battleId);
		}
		// player.setDYZZState(DYZZState.GAMEING);
		// player.getPush().syncPlayerInfo();
		IDYZZPlayer hp = makesurePlayer(player.getId());
		if (Objects.nonNull(hp) && hp.getParent() != room) {
			throw new RuntimeException("player has game playerId" + player.getId() + " id = " + hp.getParent().getId());
		}
		if (Objects.isNull(hp)) {
			player.setDYZZRoomId(roomXid.getUUID());
			hp = new DYZZPlayer(player);
			hp.setParent(room);
		}
		DYZZJoinRoomMsg msg = DYZZJoinRoomMsg.valueOf(room, hp);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		return true;
	}

	public boolean hasGame(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.DYZZAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.DYZZAOGUAN_ROOM).queryObject(roomXid);
		return Objects.nonNull(roomObj);
	}

	public boolean creatNewBattle(long createTime, DYZZExtraParam extParm) {
		try {

			HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.DYZZAOGUAN_ROOM, extParm.getBattleId());
			DYZZBattleRoom room = (DYZZBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setCreateTime(createTime);
			room.setOverTime(Long.MAX_VALUE);
			room.setBattleCfgId(0);
			room.setExtParm(extParm);
			room.init();

			room.setState(new DYZZPreparing(room));
			// DYZZJoinRoomMsg msg = DYZZJoinRoomMsg.valueOf(room, leader);
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
	public ArrayList<DYZZBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.DYZZAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<DYZZBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getDYZZState())) {
			return false;
		}

		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.DYZZError.DYZZ_BAN_OP_VALUE, 0);
				return true;
			}
		}

		DYZZProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(DYZZProtoCfg.class, protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.DYZZError.DYZZ_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		IDYZZPlayer gp = DYZZRoomManager.getInstance().makesurePlayer(player.getId());
		return gp.onProtocol(protocol);
	}
}
