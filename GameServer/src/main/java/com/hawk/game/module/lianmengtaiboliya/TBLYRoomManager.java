package com.hawk.game.module.lianmengtaiboliya;

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
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYProtoCfg;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYJoinRoomMsg;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.roomstate.TBLYPreparing;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TBLY.PBTBLYGameInfoSync;
import com.hawk.game.util.FixThreadXID;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;

public class TBLYRoomManager extends HawkAppObj {
	private static TBLYRoomManager Instance;
	private static int roomIndex = 1;

	/** 红 蓝方 */
	public enum CAMP {
		NONE(0), A(1), B(2);
		CAMP(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}
	}

	private Cache<String, ITBLYPlayer> cache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.HOURS).build();

	public TBLYRoomManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
	}

	public static TBLYRoomManager getInstance() {
		return Instance;
	}

	public ITBLYPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(ITBLYPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(ITBLYPlayer player) {
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
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.TBLYAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.TBLYAOGUAN_ROOM).queryObject(roomXid);
		TBLYBattleRoom room = null;
		if (roomObj != null) {
			room = (TBLYBattleRoom) roomObj.getImpl();
		} else {
			throw new RuntimeException("game not fount id = " + battleId);
		}
		// player.setTBLYState(TBLYState.GAMEING);
		player.setTBLYRoomId(roomXid.getUUID());
		// player.getPush().syncPlayerInfo();
		ITBLYPlayer hp = makesurePlayer(player.getId());
		if (Objects.nonNull(hp) && hp.getParent() != room) {
			throw new RuntimeException("player has game id = " + hp.getParent().getId());
		}
		if (Objects.isNull(hp)) {
			hp = new TBLYPlayer(player);
			hp.setParent(room);
		}
		TBLYJoinRoomMsg msg = TBLYJoinRoomMsg.valueOf(room, hp);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		return true;
	}

	public boolean hasGame(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.TBLYAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.TBLYAOGUAN_ROOM).queryObject(roomXid);
		return Objects.nonNull(roomObj);
	}

	public boolean creatNewBattle(long createTime, long overTime, String battleId, TBLYExtraParam extParm) {
		try {
			extParm.setBattleId(battleId);
			FixThreadXID roomXid = FixThreadXID.valueOf(GsConst.ObjType.TBLYAOGUAN_ROOM, battleId, ++roomIndex);
			TBLYBattleRoom room = (TBLYBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setCreateTime(createTime);
			room.setOverTime(overTime);
			room.setBattleCfgId(0);
			room.setExtParm(extParm);
			room.init();

			room.setState(new TBLYPreparing(room));
			// TBLYJoinRoomMsg msg = TBLYJoinRoomMsg.valueOf(room, leader);
			// HawkApp.getInstance().postMsg(leader.getXid(), msg);
			DungeonRedisLog.log("TBLYRoomManager", "TBLY create new battle battleId:{} extParm:{}", battleId, extParm);

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
	public ArrayList<TBLYBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.TBLYAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<TBLYBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getTBLYState())) {
			return false;
		}

		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.Error.TBLY_BAN_OP_VALUE, 0);
				return true;
			}
		}

		TBLYProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(TBLYProtoCfg.class, protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.Error.TBLY_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		ITBLYPlayer gp = TBLYRoomManager.getInstance().makesurePlayer(player.getId());
		if (lcfg.isMulti()) {
			gp.onProtocol(protocol);
		} else {
			HawkTaskManager.getInstance().postProtocol(gp.getParent().getXid(), TBLYProtocol.valueOf(protocol, gp));
		}

		return true;
	}

	final String TBLY_Sync_Key = "TBLY_SYNC:";
	LoadingCache<String, PBTBLYGameInfoSync> syncpbcache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<String, PBTBLYGameInfoSync>() {
		public PBTBLYGameInfoSync load(String roomId) {
			try {
				String key = TBLY_Sync_Key + roomId;
				byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
				if (bytes != null) {
					PBTBLYGameInfoSync pb = PBTBLYGameInfoSync.newBuilder().mergeFrom(bytes).build();
					return pb;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return PBTBLYGameInfoSync.getDefaultInstance();
		}
	});

	public void saveGailan(PBTBLYGameInfoSync resp, String roomId) {
		String key = TBLY_Sync_Key + roomId;
		RedisProxy.getInstance().getRedisSession().setBytes(key, resp.toByteArray(), GsConst.DAY_SECONDS);
	}

	public PBTBLYGameInfoSync getGaiLanResp(String roomId) {
		try {
			return syncpbcache.get(roomId);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return PBTBLYGameInfoSync.getDefaultInstance();
	}
}
