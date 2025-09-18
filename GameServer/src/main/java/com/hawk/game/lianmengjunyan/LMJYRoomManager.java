package com.hawk.game.lianmengjunyan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hawk.game.GsApp;
import com.hawk.game.config.DungeonBanProtoCfg;
import com.hawk.game.config.LmjyProtoCfg;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.LMJYPlayer;
import com.hawk.game.lianmengjunyan.player.npc.LMJYNPCPlayer;
import com.hawk.game.lianmengjunyan.roomstate.LMJYPreparing;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;
import com.hawk.game.warcollege.LMJYExtraParam;

public class LMJYRoomManager extends HawkAppObj {
	private static LMJYRoomManager Instance;
	private Cache<String, ILMJYPlayer> cache = CacheBuilder.newBuilder().recordStats()
			.expireAfterAccess(1, TimeUnit.HOURS).build();

	public LMJYRoomManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
	}

	public static LMJYRoomManager getInstance() {
		return Instance;
	}

	public ILMJYPlayer makesurePlayer(String playerId) {
		return cache.getIfPresent(playerId);
	}

	public void cache(ILMJYPlayer player) {
		cache.put(player.getId(), player);
	}

	public void invalidate(ILMJYPlayer player) {
		cache.invalidate(player.getId());
	}

	public static LMJYBattleRoom creatNewBattle(List<Player> players, int lmjyBattleCfgId, LMJYExtraParam extParm) {
		long gameingP = players.stream().filter(p -> Objects.nonNull(p.getLmjyState())).count();
		if (gameingP > 0) {
			for (Player p : players) {
				System.out.println(p.getName() + "    = " + p.getLmjyState());
			}
			throw new RuntimeException("有不符合进入军演条件的玩家 ");
		}
		try {
			HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.LMJYAOGUAN_ROOM, HawkUUIDGenerator.genUUID());
			LMJYBattleRoom room = (LMJYBattleRoom) GsApp.getInstance().createObj(roomXid).getImpl();
			room.setBattleCfgId(lmjyBattleCfgId);
			room.setExtParm(extParm);
			room.setGuildId(players.get(0).getGuildId());
			room.init();

			// ILMJYPlayer leader = null;
			for (Player player : players) {
				player.setLmjyState(PState.PREJOIN);
				player.setLmjyRoomId(roomXid.getUUID());
				player.getPush().syncPlayerInfo();
				ILMJYPlayer hp = new LMJYPlayer(player);
				hp.setPos(room.popBornPointA());
				hp.setParent(room);
				room.addGamer(hp);
				LMJYRoomManager.getInstance().cache(hp);
				// if(Objects.isNull(leader)){
				// leader = hp;
				// }
			}

			// 机器人
			for (int npcId : room.getCfg().copyOfNpcList()) {
				LMJYNPCPlayer npc = new LMJYNPCPlayer(npcId);
				npc.setPos(room.popBornPointB());
				npc.setLmjyRoomId(roomXid.getUUID());
				npc.setParent(room);
				room.addGamer(npc);
				LMJYRoomManager.getInstance().cache(npc);
			}

			room.setState(new LMJYPreparing(room));
			for (ILMJYPlayer player : room.getPlayerList(PState.GAMEING, PState.PREJOIN)) {
				if (player instanceof LMJYNPCPlayer) {// 正常玩家去自己线程
					room.joinRoom(player);
				}
			}
			// LMJYJoinRoomMsg msg = LMJYJoinRoomMsg.valueOf(room, leader);
			// HawkApp.getInstance().postMsg(leader.getXid(), msg);
			return room;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	@Override
	public boolean onTick() {
		return super.onTick();
	}

	/** 取得所有房间 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<LMJYBattleRoom> findAllRoom() {
		ArrayList vals = new ArrayList();
		GsApp.getInstance().getObjMan(GsConst.ObjType.LMJYAOGUAN_ROOM).collectObjValue(vals, null);
		return (ArrayList<LMJYBattleRoom>) vals;
	}

	public boolean onProtocol(HawkProtocol protocol, Player player) {
		if (Objects.isNull(player.getLmjyState())) {
			return false;
		}
		if (player.getLmjyState() == PState.PREJOIN && protocol.getType() != HP.code.LMJY_JOIN_ROOM_REQ_VALUE) {
			return false;
		}

		DungeonBanProtoCfg banProtoCfg = HawkConfigManager.getInstance().getConfigByKey(DungeonBanProtoCfg.class,
				protocol.getType());
		if (Objects.nonNull(banProtoCfg)) {
			if (banProtoCfg.getIgnore() == 1) { // 忽略
				return true;
			}

			if (banProtoCfg.getBan() == 1) { // 禁止
				player.sendError(protocol.getType(), Status.Error.LMJY_BAN_OP_VALUE, 0);
				return true;
			}
		}

		LmjyProtoCfg lcfg = HawkConfigManager.getInstance().getConfigByKey(LmjyProtoCfg.class,
				protocol.getType());
		if (Objects.isNull(lcfg)) {
			return false;
		}

		if (lcfg.getIgnore() == 1) { // 忽略
			return true;
		}

		if (lcfg.getBan() == 1) { // 禁止
			player.sendError(protocol.getType(), Status.Error.LMJY_BAN_OP_VALUE, 0);
			return true;
		}

		// 正常处理的
		ILMJYPlayer gp = LMJYRoomManager.getInstance().makesurePlayer(player.getId());
		return gp.onProtocol(protocol);
	}
}
