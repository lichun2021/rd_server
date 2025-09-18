package com.hawk.game.module.lianmengfgyl.battleroom;

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
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLProtoCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLJoinRoomMsg;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.player.FGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.roomstate.FGYLPreparing;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.FGYL.PBFGYLGameInfoSync;
import com.hawk.game.util.FixThreadXID;
import com.hawk.game.util.GsConst;

public class FGYLRoomManager extends HawkAppObj {
	private static FGYLRoomManager Instance;
	private static int roomIndex = 1;

	/** 红 蓝方 */
	public enum FGYL_CAMP {
		NONE(0), A(1), B(2),YURI(3);
		FGYL_CAMP(int value) {
			this.value = value;
		}

		private int value;

		public int intValue() {
			return value;
		}

		public static FGYL_CAMP valueOf(int v) {
			switch (v) {
			case 0:
				return NONE;
			case 1:
				return A;
			case 2:
				return B;
			case 3:
				return YURI;
			default:
				throw new RuntimeException("no such camp = " + v);
			}
		}
	}

	private Cache<String, IFGYLPlayer> cache = CacheBuilder.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.HOURS).build();

	public FGYLRoomManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
		FGYLMapBlock.getInstance().init();
	}

	public static FGYLRoomManager getInstance() {
		return Instance;
	}

	public IFGYLPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(IFGYLPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(IFGYLPlayer player) {
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
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.FGYLAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.FGYLAOGUAN_ROOM).queryObject(roomXid);
		FGYLBattleRoom room = null;
		if (roomObj != null) {
			room = (FGYLBattleRoom) roomObj.getImpl();
		} else {
			throw new RuntimeException("game not fount id = " + battleId);
		}
		// player.setFGYLState(FGYLState.GAMEING);
		player.setFgylRoomId(roomXid.getUUID());
		// player.getPush().syncPlayerInfo();
		IFGYLPlayer hp = makesurePlayer(player.getId());
		if (Objects.nonNull(hp) && hp.getParent() != room) {
			throw new RuntimeException("player has game id = " + hp.getParent().getId());
		}
		if (Objects.isNull(hp)) {
			hp = new FGYLPlayer(player);
			hp.setParent(room);
		}
		FGYLJoinRoomMsg msg = FGYLJoinRoomMsg.valueOf(room, hp);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		return true;
	}

	public boolean hasGame(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.FGYLAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.FGYLAOGUAN_ROOM).queryObject(roomXid);
		return Objects.nonNull(roomObj);
	}

	public boolean creatNewBattle(long createTime, long overTime, String battleId, FGYLExtraParam extParm) {
		try {
			extParm.setBattleId(battleId);
			FixThreadXID roomXid = FixThreadXID.valueOf(GsConst.ObjType.FGYLAOGUAN_ROOM, battleId, ++roomIndex);
			FGYLBattleRoom room = (FGYLBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setCreateTime(createTime);
			room.setOverTime(overTime);
			room.setBattleCfgId(0);
			room.setExtParm(extParm);
			room.init();

			room.setState(new FGYLPreparing(room));
			DungeonRedisLog.log("FGYLRoomManager", "FGYL create new battle battleId:{} extParm:{}", battleId, extParm);

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
	public ArrayList<FGYLBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.FGYLAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<FGYLBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getFgylState())) {
			return false;
		}

		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.FGYLError.FGYL_BAN_OP_VALUE, 0);
				return true;
			}
		}

		FGYLProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(FGYLProtoCfg.class, protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.FGYLError.FGYL_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		IFGYLPlayer gp = FGYLRoomManager.getInstance().makesurePlayer(player.getId());
		if (lcfg.isMulti()) {
			gp.onProtocol(protocol);
		} else {
			HawkTaskManager.getInstance().postProtocol(gp.getParent().getXid(), FGYLProtocol.valueOf(protocol, gp));
		}

		return true;
	}

	final String FGYL_Sync_Key = "FGYL_SYNC:";
	LoadingCache<String, PBFGYLGameInfoSync> syncpbcache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<String, PBFGYLGameInfoSync>() {
		public PBFGYLGameInfoSync load(String roomId) {
			try {
				String key = FGYL_Sync_Key + roomId;
				byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
				if (bytes != null) {
					PBFGYLGameInfoSync pb = PBFGYLGameInfoSync.newBuilder().mergeFrom(bytes).build();
					return pb;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return PBFGYLGameInfoSync.getDefaultInstance();
		}
	});

	public void saveGailan(PBFGYLGameInfoSync resp, String roomId) {
		String key = FGYL_Sync_Key + roomId;
		RedisProxy.getInstance().getRedisSession().setBytes(key, resp.toByteArray(), GsConst.DAY_SECONDS);
	}

	public PBFGYLGameInfoSync getGaiLanResp(String roomId) {
		try {
			return syncpbcache.get(roomId);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return PBFGYLGameInfoSync.getDefaultInstance();
	}
}
