package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hawk.game.GsApp;
import com.hawk.game.config.DungeonBanProtoCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZProtoCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZExtraParam;
import com.hawk.game.module.lianmengyqzz.battleroom.invoker.YQZZAddGuildSignInvoker;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZJoinRoomMsg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.YQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.roomstate.YQZZPreparing;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZMatchRoomData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.msg.AutoSearchMonsterMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.YQZZ.PBYQZZGameInfoSync;
import com.hawk.game.protocol.YQZZ.PBYQZZSecondMapResp;
import com.hawk.game.protocol.YQZZ.YQZZGaiLanResp;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GsConst;

public class YQZZRoomManager extends HawkAppObj {
	private long lastLoadgailan;
	private static YQZZRoomManager Instance;
	private Cache<String, IYQZZPlayer> cache = CacheBuilder.newBuilder().recordStats()
			.expireAfterAccess(48, TimeUnit.HOURS).build();

	public YQZZRoomManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
		YQZZMapBlock.getInstance().init();
	}

	public static YQZZRoomManager getInstance() {
		return Instance;
	}

	public IYQZZPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(IYQZZPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(IYQZZPlayer player) {
		cache.invalidate(player.getId());
	}

	/** 战场内联盟成员数 */
	public int guildMemberInBattle(String battleId, String guildId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.YQZZAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.YQZZAOGUAN_ROOM)
				.queryObject(roomXid);
		YQZZBattleRoom room = null;
		if (roomObj != null) {
			room = (YQZZBattleRoom) roomObj.getImpl();
			return room.getGuildMemberCount(guildId);
		} else {
			throw new RuntimeException("Battle not fount id = " + battleId);
		}
	}

	/**内涵所有建筑信息, 以及giuldid所在联盟玩家信息. 不需要玩家信息guild不传*/
	public PBYQZZSecondMapResp getSecondMap(String battleId, String guildId) {
		YQZZBattleRoom room = getBattleRoom(battleId);
		return room.getSecondMap(guildId).build();
	}

	/**最近一次战场信息推送. 内包含所有玩家,联盟, 国家积分信息*/
	public PBYQZZGameInfoSync getLastSyncpb(String battleId) {
		YQZZBattleRoom room = getBattleRoom(battleId);
		return room.getLastSyncpb();
	}

	private YQZZBattleRoom getBattleRoom(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.YQZZAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.YQZZAOGUAN_ROOM)
				.queryObject(roomXid);
		YQZZBattleRoom room = null;
		if (roomObj != null) {
			room = (YQZZBattleRoom) roomObj.getImpl();
		} else {
			throw new RuntimeException("game not fount id = " + battleId);
		}
		return room;
	}

	/**
	 * 初次加入游戏要指定开始时间. 同battleId 不重复创建
	 */
	public boolean joinGame(String battleId, Player player) {
		if (!player.hasGuild()) {
			return false;
		}

		if (player.isInDungeonMap()) {
			// throw new RuntimeException("player is YQZZ gameing gemId = " + player.getYQZZRoomId());
		}
		YQZZBattleRoom room = getBattleRoom(battleId);
		// player.setYQZZState(YQZZState.GAMEING);
		player.setYQZZRoomId(battleId);
		// player.getPush().syncPlayerInfo();
		IYQZZPlayer hp = makesurePlayer(player.getId());
		if (Objects.nonNull(hp) && hp.getParent() != room) {
			throw new RuntimeException("player has game id = " + hp.getParent().getId());
		}
		if (Objects.isNull(hp)) {
			hp = new YQZZPlayer(player);
			hp.setParent(room);
		}
		YQZZJoinRoomMsg msg = YQZZJoinRoomMsg.valueOf(room, hp);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		return true;
	}

	public boolean hasGame(String battleId) {
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.YQZZAOGUAN_ROOM, battleId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.YQZZAOGUAN_ROOM)
				.queryObject(roomXid);
		return Objects.nonNull(roomObj);
	}

	public boolean creatNewBattle(long createTime, long overTime, YQZZExtraParam extParm) {
		try {
			String battleId = extParm.getBattleId();
			extParm.setBattleId(battleId);
			HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.YQZZAOGUAN_ROOM, battleId);
			YQZZBattleRoom room = (YQZZBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setCreateTime(createTime);
			room.setOverTime(overTime);
			room.setBattleCfgId(0);
			room.setExtParm(extParm);
			room.init();
			room.setState(new YQZZPreparing(room));

			YQZZThread thread = YQZZThread.create(room);

			GameTssService.getInstance().addInvoker(YQZZAddGuildSignInvoker.class);
			DungeonRedisLog.log("YQZZRoomManager", "YQZZ create new battle threadName:{} battleId:{}", thread.getName(), battleId);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	@Override
	public boolean onTick() {
		loadGailan();
		return super.onTick();
	}

	/** 取得所有房间 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<YQZZBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.YQZZAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<YQZZBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getYQZZState())) {
			return false;
		}

		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.YQZZError.YQZZ_BAN_OP_VALUE, 0);
				return true;
			}
		}

		YQZZProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(YQZZProtoCfg.class, protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		IYQZZPlayer gp = YQZZRoomManager.getInstance().makesurePlayer(player.getId());
		if (gp.getParent().maShangOver()) {
			gp.responseSuccess(protocol.getType());
			return true;
		}
		gp.onProtocol(protocol);
		// if (lcfg.isMultithread()) { // 可多线程处理
		// gp.getParent().onProtocol(YQZZProtocol.valueOf(protocol, gp));
		return true;
		// }
		// HawkProtocolTask protoTask = HawkProtocolTask.valueOf(gp.getParent().getXid(), YQZZProtocol.valueOf(protocol, gp));
		// return gp.getParent().getThread().addTask(protoTask);
	}
	
	/**
	 * @return 中断传递链 true
	 */
	public boolean onMessage(HawkMsg msg, Player player) {
		IYQZZPlayer gp = YQZZRoomManager.getInstance().makesurePlayer(player.getId());
		if (gp == null || Objects.isNull(player.getYQZZState())) {
			return false;
		}
		gp.onMessage(msg);
		
		if(msg.getClass() == AutoSearchMonsterMsg.class){
			return true;
		}
		
		return false;
	}
	

	final String YQZZGaiLanRespKey = "YQZZ_GaiLan:";
	YQZZGaiLanResp gaiLanResp;
	public void saveGailan(YQZZGaiLanResp resp, String roomId) {
		String key = YQZZGaiLanRespKey + roomId;
		RedisProxy.getInstance().getRedisSession().setBytes(key, resp.toByteArray(), GsConst.DAY_SECONDS);
	}

	public void loadGailan() {
		long now = HawkTime.getMillisecond();
		if (now - lastLoadgailan < 3000) {
			return;
		}
		lastLoadgailan = now;

		try {
			YQZZMatchRoomData roomData = YQZZMatchService.getInstance().getDataManger().getRoomData();
			if (roomData == null) {
				return;
			}
			String key = YQZZGaiLanRespKey + roomData.getRoomId();
			byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
			if (bytes != null) {
				gaiLanResp = YQZZGaiLanResp.newBuilder().mergeFrom(bytes).build();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public YQZZGaiLanResp getGaiLanResp() {
		return gaiLanResp;
	}

}
