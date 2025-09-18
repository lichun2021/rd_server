package com.hawk.game.module.lianmenxhjz.battleroom;

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
import com.hawk.game.module.lianmenxhjz.battleroom.cfg.XHJZProtoCfg;
import com.hawk.game.module.lianmenxhjz.battleroom.msg.XHJZJoinRoomMsg;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.player.XHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.roomstate.XHJZPreparing;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.XHJZ.PBXHJZGameInfoSync;
import com.hawk.game.util.GsConst;

public class XHJZRoomManager extends HawkAppObj {
	private static XHJZRoomManager Instance;

	/** 红 蓝方 */
	public enum XHJZ_CAMP {
		NONE(0), A(1), B(2);
		XHJZ_CAMP(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}

		public static XHJZ_CAMP valueOf(int v) {
			switch (v) {
			case 0:
				return NONE;
			case 1:
				return A;
			case 2:
				return B;

			default:
				throw new RuntimeException("no such camp = " + v);
			}
		}
	}

	private Cache<String, IXHJZPlayer> cache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.HOURS).build();

	public XHJZRoomManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
	}

	public static XHJZRoomManager getInstance() {
		return Instance;
	}

	public IXHJZPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(IXHJZPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(IXHJZPlayer player) {
		cache.invalidate(player.getId());
	}

	/**
	 * 初次加入游戏要指定开始时间. 同battleId 不重复创建
	 */
	public boolean joinGame(String battleId, Player player) {
		if (!player.hasGuild()) {
			return false;
		}
		if (player.getLmjyState() != null) {
			throw new RuntimeException("player is LMJY gameing gemId = " + player.getLmjyRoomId());
		}
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.XHJZAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.XHJZAOGUAN_ROOM).queryObject(roomXid);
		XHJZBattleRoom room = null;
		if (roomObj != null) {
			room = (XHJZBattleRoom) roomObj.getImpl();
		} else {
			throw new RuntimeException("game not fount id = " + battleId);
		}
		// player.setXHJZState(XHJZState.GAMEING);
		player.setXhjzRoomId(roomXid.getUUID());
		// player.getPush().syncPlayerInfo();
		IXHJZPlayer hp = makesurePlayer(player.getId());
		if (Objects.nonNull(hp) && hp.getParent() != room) {
			throw new RuntimeException("player has game id = " + hp.getParent().getId());
		}
		if (Objects.isNull(hp)) {
			hp = new XHJZPlayer(player);
			hp.setParent(room);
		}
		XHJZJoinRoomMsg msg = XHJZJoinRoomMsg.valueOf(room, hp);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		return true;
	}

	public boolean hasGame(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.XHJZAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.XHJZAOGUAN_ROOM).queryObject(roomXid);
		return Objects.nonNull(roomObj);
	}

	public boolean creatNewBattle(long createTime, long overTime, String battleId, XHJZExtraParam extParm) {
		try {
			extParm.setBattleId(battleId);
			HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.XHJZAOGUAN_ROOM, battleId);
			XHJZBattleRoom room = (XHJZBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setCreateTime(createTime);
			room.setOverTime(overTime);
			room.setBattleCfgId(0);
			room.setExtParm(extParm);
			room.init();

			room.setState(new XHJZPreparing(room));
			// XHJZJoinRoomMsg msg = XHJZJoinRoomMsg.valueOf(room, leader);
			// HawkApp.getInstance().postMsg(leader.getXid(), msg);
			DungeonRedisLog.log("XHJZRoomManager", "XHJZ create new battle battleId:{} extParm:{}", battleId, extParm);

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
	public ArrayList<XHJZBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.XHJZAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<XHJZBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getXhjzState())) {
			return false;
		}

		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.XHJZError.XHJZ_BAN_OP_VALUE, 0);
				return true;
			}
		}

		XHJZProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(XHJZProtoCfg.class, protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.XHJZError.XHJZ_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		IXHJZPlayer gp = XHJZRoomManager.getInstance().makesurePlayer(player.getId());
		if (lcfg.isMulti()) {
			gp.onProtocol(protocol);
		} else {
			HawkTaskManager.getInstance().postProtocol(gp.getParent().getXid(), XHJZProtocol.valueOf(protocol, gp));
		}

		return true;
	}

	final String XHJZ_Sync_Key = "XHJZ_SYNC:";
	LoadingCache<String, PBXHJZGameInfoSync> syncpbcache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<String, PBXHJZGameInfoSync>() {
		public PBXHJZGameInfoSync load(String roomId) {
			try {
				String key = XHJZ_Sync_Key + roomId;
				byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
				if (bytes != null) {
					PBXHJZGameInfoSync pb = PBXHJZGameInfoSync.newBuilder().mergeFrom(bytes).build();
					return pb;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return PBXHJZGameInfoSync.getDefaultInstance();
		}
	});

	public void saveGailan(PBXHJZGameInfoSync resp, String roomId) {
		String key = XHJZ_Sync_Key + roomId;
		RedisProxy.getInstance().getRedisSession().setBytes(key, resp.toByteArray(), GsConst.DAY_SECONDS);
	}

	public PBXHJZGameInfoSync getGaiLanResp(String roomId) {
		try {
			return syncpbcache.get(roomId);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return PBXHJZGameInfoSync.getDefaultInstance();
	}
}
