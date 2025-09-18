package com.hawk.game.lianmengstarwars;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.game.GsApp;
import com.hawk.game.config.DungeonBanProtoCfg;
import com.hawk.game.config.SWProtoCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.lianmengstarwars.msg.SWJoinRoomMsg;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.player.SWPlayer;
import com.hawk.game.lianmengstarwars.roomstate.SWPreparing;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.SW.PBSWVideoPackage;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;

public class SWRoomManager extends HawkAppObj {
	private static SWRoomManager Instance;
	private List<SWThread> battleThreads;
	private Cache<String, ISWPlayer> cache = CacheBuilder.newBuilder().recordStats()
			.expireAfterAccess(12, TimeUnit.HOURS).build();

	public SWRoomManager(HawkXID xid) {
		super(xid);
		battleThreads = new LinkedList<>();
		if (null == Instance) {
			Instance = this;
		}
	}

	public static SWRoomManager getInstance() {
		return Instance;
	}

	public ISWPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(ISWPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(ISWPlayer player) {
		cache.invalidate(player.getId());
	}

	final int VIDEOEXPIRE = 6 * 3600;
	/** 录像文件 */
	final String SWVIDEO = "sw_videow:";
	/** 录像最新文件号 */
	final String SWVIDEO_LAST = "sw_videow_last:";

	/** 最新录像包 无录像返回null. index从0开始 */
	public Integer videoLastIndex(String battleId) {
		String key = SWVIDEO_LAST + battleId;
		String str = RedisProxy.getInstance().getRedisSession().getString(key);
		if (str == null) {
			return null;
		}
		return Integer.valueOf(str);
	}

	/** 是否有录像 */
	public boolean videoExist(String battleId) {
		return videoLastIndex(battleId) != null;
	}

	/** 取得指定录像 */
	public Optional<PBSWVideoPackage> videoPackageOfIndex(String battleId, int index) {
		String key = SWVIDEO + battleId + ":" + index;
		System.out.println(key);
		byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
		if (bytes == null || bytes.length == 0) {
			return Optional.empty();
		}
		PBSWVideoPackage.Builder bul = PBSWVideoPackage.newBuilder();
		try {
			bul.mergeFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return Optional.of(bul.build());
	}

	public void saveSWVideo(PBSWVideoPackage pck) {
//		System.out.println("存录像");
//		{
//			String key = SWVIDEO + pck.getBattleId() + ":" + pck.getIndex();
//			System.out.println(key);
//			RedisProxy.getInstance().getRedisSession().setBytes(key, pck.toByteArray(), VIDEOEXPIRE);
//		}
//		{
//			String key = SWVIDEO_LAST + pck.getBattleId();
//			RedisProxy.getInstance().getRedisSession().setString(key, pck.getIndex() + "", VIDEOEXPIRE);
//		}
	}

	/** 战场内联盟成员数 */
	public int guildMemberInBattle(String battleId, String guildId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.SWAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.SWAOGUAN_ROOM)
				.queryObject(roomXid);
		SWBattleRoom room = null;
		if (roomObj != null) {
			room = (SWBattleRoom) roomObj.getImpl();
			return room.getGuildMemberCount(guildId);
		} else {
			throw new RuntimeException("Battle not fount id = " + battleId);
		}
	}

	/**
	 * 初次加入游戏要指定开始时间. 同battleId 不重复创建
	 */
	public boolean joinGame(String battleId, Player player) {
		if (!player.hasGuild()) {
			return false;
		}
		if (player.isInDungeonMap()) {
			throw new RuntimeException("player is LMJY gameing gemId = " + player.getSwRoomId());
		}
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.SWAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.SWAOGUAN_ROOM)
				.queryObject(roomXid);
		SWBattleRoom room = null;
		if (roomObj != null) {
			room = (SWBattleRoom) roomObj.getImpl();
		} else {
			throw new RuntimeException("game not fount id = " + battleId);
		}
		// player.setSWState(SWState.GAMEING);
		player.setSwRoomId(roomXid.getUUID());
		// player.getPush().syncPlayerInfo();
		ISWPlayer hp = makesurePlayer(player.getId());
		if (Objects.nonNull(hp) && hp.getParent() != room) {
			throw new RuntimeException("player has game id = " + hp.getParent().getId());
		}
		if (Objects.isNull(hp)) {
			hp = new SWPlayer(player);
			hp.setParent(room);
		}
		SWJoinRoomMsg msg = SWJoinRoomMsg.valueOf(room, hp);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		return true;
	}

	public boolean hasGame(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.SWAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.SWAOGUAN_ROOM)
				.queryObject(roomXid);
		return Objects.nonNull(roomObj);
	}

	public boolean creatNewBattle(long createTime, long overTime, String battleId, SWExtraParam extParm) {
		try {
			extParm.setBattleId(battleId);
			HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.SWAOGUAN_ROOM, battleId);
			SWBattleRoom room = (SWBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setCreateTime(createTime);
			room.setOverTime(overTime);
			room.setBattleCfgId(0);
			room.setExtParm(extParm);
			room.init();
			room.setState(new SWPreparing(room));

			SWThread thread = SWThread.create(room);
			battleThreads.add(thread);
			
			DungeonRedisLog.log("SWRoomManager", "SW create new battle threadName:{} battleId:{}",thread.getName(), battleId);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	@Override
	public boolean onTick() {
		Iterator<SWThread> it = battleThreads.iterator();
		while (it.hasNext()) {
			SWThread thread = it.next();
			if (thread.getBattleRoom().isGameOver()) {
				thread.close(false);
				it.remove();
				DungeonRedisLog.log("SWRoomManager", "SW close battle threadName:{} battleId:{}",thread.getName(), thread.getBattleRoom().getId());
			}
		}
		return super.onTick();
	}

	/** 取得所有房间 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<SWBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.SWAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<SWBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getSwState())) {
			return false;
		}

		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.Error.SW_BAN_OP_VALUE, 0);
				return true;
			}
		}

		SWProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(SWProtoCfg.class, protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.Error.SW_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		ISWPlayer gp = SWRoomManager.getInstance().makesurePlayer(player.getId());
		if (lcfg.isMulti()) {
			gp.onProtocol(protocol);
		} else {
			HawkTaskManager.getInstance().postProtocol(gp.getParent().getXid(), SWProtocol.valueOf(protocol, gp));
		}
		
		return true;
	}
}
