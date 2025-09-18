package com.hawk.game.module.lianmengXianquhx;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
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
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hawk.game.GsApp;
import com.hawk.game.config.DungeonBanProtoCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXProtoCfg;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXJoinRoomMsg;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.player.XQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.roomstate.XQHXPreparing;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.XQHX.PBXQHXGameInfoSync;
import com.hawk.game.util.FixThreadXID;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;

public class XQHXRoomManager extends HawkAppObj {
	private static XQHXRoomManager Instance;
	private static int roomIndex = 1;

	/** 红 蓝方 */
	public enum XQHX_CAMP {
		A(1), B(2);
		XQHX_CAMP(int value) {
			this.value = value;
		}

		public static XQHX_CAMP valueOf(int v) {
			switch (v) {
			case 1:
				return A;
			case 2:
				return B;

			default:
				throw new RuntimeException("no such camp = " + v);
			}
		}

		private int value;

		public int intValue() {
			return value;
		}
	}

	private Cache<String, IXQHXPlayer> cache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.HOURS).build();

	public XQHXRoomManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
	}

	public static XQHXRoomManager getInstance() {
		return Instance;
	}

	public IXQHXPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(IXQHXPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(IXQHXPlayer player) {
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
		if (!player.hasGuild()) {
			return false;
		}
		if (player.getLmjyState() != null) {
			throw new RuntimeException("player is LMJY gameing gemId = " + player.getLmjyRoomId());
		}
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.XQHXAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.XQHXAOGUAN_ROOM).queryObject(roomXid);
		XQHXBattleRoom room = null;
		if (roomObj != null) {
			room = (XQHXBattleRoom) roomObj.getImpl();
		} else {
			throw new RuntimeException("game not fount id = " + battleId);
		}
		// player.setXQHXState(XQHXState.GAMEING);
		player.setXQHXRoomId(roomXid.getUUID());
		// player.getPush().syncPlayerInfo();
		IXQHXPlayer hp = makesurePlayer(player.getId());
		if (Objects.nonNull(hp) && hp.getParent() != room) {
			throw new RuntimeException("player has game id = " + hp.getParent().getId());
		}
		if (Objects.isNull(hp)) {
			hp = new XQHXPlayer(player);
			hp.setParent(room);
		}
		XQHXJoinRoomMsg msg = XQHXJoinRoomMsg.valueOf(room, hp);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		return true;
	}

	public boolean hasGame(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.XQHXAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.XQHXAOGUAN_ROOM).queryObject(roomXid);
		return Objects.nonNull(roomObj);
	}

	public boolean creatNewBattle(long createTime, long overTime, String battleId, XQHXExtraParam extParm) {
		try {
			extParm.setBattleId(battleId);
			FixThreadXID roomXid = FixThreadXID.valueOf(GsConst.ObjType.XQHXAOGUAN_ROOM, battleId, ++roomIndex);
			XQHXBattleRoom room = (XQHXBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setCreateTime(createTime);
			room.setOverTime(overTime);
			room.setBattleCfgId(0);
			room.setExtParm(extParm);
			room.init();

			room.setState(new XQHXPreparing(room));
			// XQHXJoinRoomMsg msg = XQHXJoinRoomMsg.valueOf(room, leader);
			// HawkApp.getInstance().postMsg(leader.getXid(), msg);
			DungeonRedisLog.log("XQHXRoomManager", "XQHX create new battle battleId:{} extParm:{}", battleId, extParm);

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
	public ArrayList<XQHXBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.XQHXAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<XQHXBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getXQHXState())) {
			return false;
		}

		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.XQHXError.XQHX_BAN_OP_VALUE, 0);
				return true;
			}
		}

		XQHXProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(XQHXProtoCfg.class, protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.XQHXError.XQHX_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		IXQHXPlayer gp = XQHXRoomManager.getInstance().makesurePlayer(player.getId());
		if (lcfg.isMulti()) {
			gp.onProtocol(protocol);
		} else {
			HawkTaskManager.getInstance().postProtocol(gp.getParent().getXid(), XQHXProtocol.valueOf(protocol, gp));
		}

		return true;
	}

//	final String XQHX_Sync_Key = "XQHX_SYNC:";
//	LoadingCache<String, PBXQHXGameInfoSync> syncpbcache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<String, PBXQHXGameInfoSync>() {
//		public PBXQHXGameInfoSync load(String roomId) {
//			try {
//				String key = XQHX_Sync_Key + roomId;
//				byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
//				if (bytes != null) {
//					PBXQHXGameInfoSync pb = PBXQHXGameInfoSync.newBuilder().mergeFrom(bytes).build();
//					return pb;
//				}
//			} catch (Exception e) {
//				HawkException.catchException(e);
//			}
//			return PBXQHXGameInfoSync.getDefaultInstance();
//		}
//	});

	public void saveGailan(PBXQHXGameInfoSync resp, String roomId) {
//		String key = XQHX_Sync_Key + roomId;
//		RedisProxy.getInstance().getRedisSession().setBytes(key, resp.toByteArray(), GsConst.DAY_SECONDS);
	}

	public PBXQHXGameInfoSync getGaiLanResp(String roomId) {
//		try {
//			return syncpbcache.get(roomId);
//		} catch (ExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return PBXQHXGameInfoSync.getDefaultInstance();
	}
}
