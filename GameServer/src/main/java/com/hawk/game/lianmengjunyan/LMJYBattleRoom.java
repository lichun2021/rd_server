package com.hawk.game.lianmengjunyan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.hawk.game.config.LMJYBattleCfg;
import com.hawk.game.config.WarCollegeTimeControlCfg;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.module.LMJYArmyModule;
import com.hawk.game.lianmengjunyan.module.LMJYMarchModule;
import com.hawk.game.lianmengjunyan.module.LMJYWorldModule;
import com.hawk.game.lianmengjunyan.msg.LMJYQuitRoomMsg;
import com.hawk.game.lianmengjunyan.msg.LMJYQuitRoomMsg.QuitReason;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.LMJYPlayer;
import com.hawk.game.lianmengjunyan.player.npc.LMJYNPCPlayer;
import com.hawk.game.lianmengjunyan.roomstate.ILMJYBattleRoomState;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.lianmengjunyan.worldmarch.LMJYMassJoinSingleMarch;
import com.hawk.game.lianmengjunyan.worldmarch.LMJYMassSingleMarch;
import com.hawk.game.lianmengjunyan.worldmarch.submarch.ILMJYMassMarch;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Lmjy.PBLMJYGameInfoSync;
import com.hawk.game.protocol.Lmjy.PBLMJYGameOver;
import com.hawk.game.protocol.Lmjy.PBLMJYPlayerQuitRoom;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.warcollege.LMJYExtraParam;

/** 虎牢关
 * 
 * @author lwt
 * @date 2018年10月26日 */
public class LMJYBattleRoom extends HawkAppObj {
	private LMJYExtraParam extParm;
	private boolean campAwin;
	private int overState;
	private int battleCfgId;
	private String guildId;
	/** 游戏内报名的玩家. 有可能不在游戏中 */
	private List<ILMJYPlayer> playerList = new ArrayList<>();

	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	private List<ILMJYWorldPoint> viewPoints = new LinkedList<>();

	private List<ILMJYWorldMarch> worldMarchList = new LinkedList<>();

	private ILMJYBattleRoomState state;
	private final long createTime;
	private long startTime;
	private long overTime;
	private List<int[]> bornPointAList = new LinkedList<>();
	private List<int[]> bornPointBList = new LinkedList<>();
	private LMJYPlayer leader;

	public LMJYBattleRoom(HawkXID xid) {
		super(xid);
		createTime = HawkTime.getMillisecond();
	}

	/** 副本是否马上要结束了
	 * 
	 * @return */
	public boolean maShangOver() {
		return getOverTime() - HawkTime.getMillisecond() < 3000;
	}

	public void sync() {
		List<ILMJYPlayer> plist = getPlayerList(PState.GAMEING, PState.PREJOIN);
		List<String> deadNpc = new ArrayList<>(3);
		for (ILMJYPlayer player : plist) {
			if (player instanceof LMJYNPCPlayer && ((LMJYNPCPlayer) player).isDead()) {
				deadNpc.add(player.getId());
			}
		}

		for (ILMJYPlayer player : plist) {
			if (player instanceof LMJYPlayer) {
				PBLMJYGameInfoSync.Builder bul = PBLMJYGameInfoSync.newBuilder();
				bul.setGameStartTime(startTime);
				bul.setGameOverTime(overTime);
				bul.setBattleCfgId(battleCfgId);
				bul.addAllDestroyedNpc(deadNpc);

				Player source = ((LMJYPlayer) player).getSource();
				int[] quitCnt = GameUtil.splitXAndY(source.lmjyQuitCnt);
				bul.setQuitCnt(quitCnt[1]);

				player.sendProtocol(HawkProtocol.valueOf(HP.code.LMJY_GAME_SYNC, bul));
			}
		}

	}

	/** 初始化, 创建npc等 */
	public void init() {
		registerModule(LMJYConst.ModuleType.HLGWorld, new LMJYWorldModule(this));
		registerModule(LMJYConst.ModuleType.HLGMarch, new LMJYMarchModule(this));
		registerModule(LMJYConst.ModuleType.HLGArmy, new LMJYArmyModule(this));

		LMJYBattleCfg cfg = getCfg();
		bornPointAList = cfg.copyOfbornPointAList();
		bornPointBList = cfg.copyOfbornPointBList();
		startTime = createTime + cfg.getPrepairTime() * 1000;
		overTime = startTime + cfg.getBattleTime() * 1000;
	}

	public int[] popBornPointA() {
		return bornPointAList.remove(0);
	}

	public int[] popBornPointB() {
		return bornPointBList.remove(0);
	}

	public LMJYBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(LMJYBattleCfg.class, battleCfgId);
	}

	public long getPlayerMarchCount(String playerId) {
		return worldMarchList.stream()
				.filter(m -> Objects.equals(m.getPlayerId(), playerId))
				.count();
	}

	public ILMJYWorldMarch getPlayerMarch(String playerId, String marchId) {
		return worldMarchList.stream()
				.filter(m -> Objects.equals(m.getPlayerId(), playerId) && Objects.equals(m.getMarchId(), marchId))
				.findAny()
				.orElse(null);
	}

	public List<ILMJYWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<ILMJYWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<ILMJYWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}

	public List<ILMJYWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<ILMJYWorldMarch> result = new ArrayList<>();
		for (ILMJYWorldMarch ma : worldMarchList) {
			if (!Objects.equals(playerId, ma.getPlayerId())) {
				continue;
			}
			if (!typeList.isEmpty() && !typeList.contains(ma.getMarchType())) {
				continue;
			}
			boolean a = Objects.nonNull(status1) && ma.getMarchStatus() == status1.getNumber();
			boolean b = Objects.nonNull(status2) && ma.getMarchStatus() == status2.getNumber();
			boolean c = Objects.nonNull(status3) && ma.getMarchStatus() == status3.getNumber();
			if (free || a || b || c) {
				result.add(ma);
			}
		}
		return result;

	}

	public ILMJYWorldMarch getMarch(String marchId) {
		return worldMarchList.stream()
				.filter(m -> Objects.equals(m.getMarchId(), marchId))
				.findAny()
				.orElse(null);
	}

	/** 联盟战争展示行军 */
	public List<ILMJYWorldMarch> getGuildWarMarch() {
		List<ILMJYWorldMarch> result = new ArrayList<>();
		for (ILMJYWorldMarch march : getWorldMarchList()) {
			if (march.needShowInGuildWar()) {
				if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					result.add(march);
				}
			}
		}
		return result;
	}

	public List<ILMJYWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<ILMJYWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<ILMJYWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<ILMJYWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<ILMJYWorldMarch> result = new ArrayList<>();
		for (ILMJYWorldMarch ma : worldMarchList) {
			// boolean isNotO = ma.getOrigionX() != x || ma.getOrigionY() != y; // 不是起点行军
			boolean isNotT = ma.getTerminalX() != x || ma.getTerminalY() != y;
			if (isNotT) {
				continue;
			}
			if (!typeList.isEmpty() && !typeList.contains(ma.getMarchType())) {
				continue;
			}
			boolean a = Objects.nonNull(status1) && ma.getMarchStatus() == status1.getNumber();
			boolean b = Objects.nonNull(status2) && ma.getMarchStatus() == status2.getNumber();
			boolean c = Objects.nonNull(status3) && ma.getMarchStatus() == status3.getNumber();
			if (free || a || b || c) {
				result.add(ma);
			}
		}
		return result;

	}

	/** 根据坐标获得世界点信息，若是未被占用的点就返回空 */
	public Optional<ILMJYWorldPoint> getWorldPoint(int x, int y) {
		return viewPoints.stream().filter(p -> p.getX() == x && p.getY() == y).findFirst();
	}

	public Optional<ILMJYWorldPoint> getWorldPoint(int pointId) {
		int[] pos = GameUtil.splitXAndY(pointId);
		return getWorldPoint(pos[0], pos[1]);
	}

	@Override
	public boolean onTick() {
		try {
			if (Objects.nonNull(state)) {
				return state.onTick();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(ILMJYPlayer player) {
		state.enterWorld(player);
	}

	/** 副本中玩家更新点
	 * 
	 * @param point */
	public void worldPointUpdate(ILMJYWorldPoint point) {
		for (ILMJYPlayer pla : getPlayerList(PState.GAMEING)) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(pla.getId()));
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			pla.sendProtocol(protocol);
		}
	}

	public void joinRoom(ILMJYPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this || this.getViewPoints().contains(player)) {
			return;
		}

		player.init();
		player.setLmjyState(PState.GAMEING);
		player.getPush().pushJoinGame();
		this.addViewPoint(player);
		if (player instanceof LMJYPlayer) {
			ChatParames parames = ChatParames.newBuilder()
					.setPlayer(player)
					.setChatType(ChatType.CHAT_FUBEN)
					.setKey(NoticeCfgId.LMJY_PLAYER_JOIN)
					.addParms(player.getName())
					.build();
			this.addWorldBroadcastMsg(parames);

			if (leader == null) {
				leader = (LMJYPlayer) player;
				for (ILMJYPlayer p : playerList) {
					if (p instanceof LMJYNPCPlayer) {
						p.getPlayerData().setDataCache(leader.getData().getDataCache());
					}
				}
			}

		}
		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code.LMJY_ENTER_GAME_SUCCESS));

		for (ILMJYPlayer p : getPlayerList(PState.GAMEING)) {
			enterWorld(p);
		}
		sync();

		DungeonRedisLog.log(player.getId(), "roomId {} guildId {} LMJYBattleCfg:{}", getId(), player.getGuildId(), getBattleCfgId());
		DungeonRedisLog.log(getId(), "player:{} guildId:{} serverId:{} LMJYBattleCfg:{}", player.getId(), player.getGuildId(), player.getMainServerId(), getBattleCfgId());
	}

	public void quitWorld(ILMJYPlayer quitPlayer, LMJYQuitRoomMsg.QuitReason reason) {
		{ // 弹窗
			PBLMJYGameOver.Builder builder = PBLMJYGameOver.newBuilder();
			builder.setGameUseTime(HawkTime.getMillisecond() - getStartTime());
			builder.setWin(false);
			if (reason == QuitReason.LEAVE) {
				builder.setOverState(4); // 4 主动退出
				if (quitPlayer instanceof LMJYPlayer) {
					Player source = ((LMJYPlayer) quitPlayer).getSource();
					WarCollegeTimeControlCfg wcfig = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
					int[] quitCnt = GameUtil.splitXAndY(source.lmjyQuitCnt);
					if (quitCnt[0] != HawkTime.getYearDay()) {
						quitCnt[0] = HawkTime.getYearDay();
						quitCnt[1] = 0;
					}
					int cd = wcfig.getWarCollegeQuitCdArray()[Math.min(quitCnt[1]++, wcfig.getWarCollegeQuitCdArray().length - 1)];
					source.lmjyCD = HawkTime.getMillisecond() + TimeUnit.MINUTES.toMillis(cd);
					source.lmjyQuitCnt = GameUtil.combineXAndY(quitCnt[0], quitCnt[1]);
				}
			} else if (reason == QuitReason.FIREOUT) {
				builder.setOverState(2);
			}
			if (builder.hasOverState()) {
				quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.LMJY_GAME_OVER, builder));
			}
		}

		LMJYQuitRoomMsg msg = LMJYQuitRoomMsg.valueOf(this, quitPlayer, reason);
		HawkApp.getInstance().postMsg(quitPlayer.getXid(), msg);
		boolean inWorld = viewPoints.remove(quitPlayer);
		playerList.remove(quitPlayer);

		if (inWorld) {
			// 删除行军
			clearQuiterMarch(quitPlayer);

			for (ILMJYPlayer gamer : playerList) {
				PBLMJYPlayerQuitRoom.Builder bul = PBLMJYPlayerQuitRoom.newBuilder();
				bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
				gamer.sendProtocol(HawkProtocol.valueOf(HP.code.LMJY_PLAYER_QUIT, bul));

				// 删除点
				WorldPointSync.Builder builder = WorldPointSync.newBuilder();
				builder.setIsRemove(true);
				builder.addPoints(quitPlayer.toBuilder(gamer.getId()));
				gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
			}

			quitPlayer.getData().getQueueEntities().clear();
		}

		if (reason == QuitReason.LEAVE) {
			ChatParames parames = ChatParames.newBuilder()
					.setPlayer(quitPlayer)
					.setChatType(ChatType.CHAT_FUBEN)
					.setKey(NoticeCfgId.LMJY_PLAYER_QUIT)
					.addParms(quitPlayer.getName())
					.build();
			addWorldBroadcastMsg(parames);
		}

	}

	private void clearQuiterMarch(ILMJYPlayer quitPlayer) {
		try {
			List<ILMJYWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (ILMJYWorldMarch march : quiterMarches) {
				if (march instanceof LMJYMassSingleMarch) {
					march.getMassJoinMarchs(true).forEach(ILMJYWorldMarch::onMarchCallback);
				}
				if (march instanceof LMJYMassJoinSingleMarch) {
					Optional<ILMJYMassMarch> massMarch = ((LMJYMassJoinSingleMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						ILMJYMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(ILMJYWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.remove();
			}
			// 援助返回
			List<ILMJYWorldMarch> helpMarchList = quitPlayer.assisReachMarches();
			for (ILMJYWorldMarch march : helpMarchList) {
				march.onMarchReturn(march.getTerminalId(), march.getOrigionId(), march.getArmys());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public boolean isHasAssistanceMarch(String viewerId, int pointId) {
		boolean bfalse = getPointMarches(pointId,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE)
						.stream()
						.filter(march -> march.getPlayerId().equals(viewerId))
						.count() > 0;
		return bfalse;
	}

	public void addGamer(ILMJYPlayer hp) {
		playerList.add(hp);
	}

	public List<ILMJYPlayer> getPlayerList(PState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<ILMJYPlayer> getPlayerList(PState st1, PState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<ILMJYPlayer> getPlayerList(PState st1, PState st2, PState st3) {
		List<ILMJYPlayer> result = new ArrayList<>();
		for (ILMJYPlayer player : playerList) {
			PState state = player.getLmjyState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public void setPlayerList(List<ILMJYPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	public void addViewPoint(ILMJYWorldPoint vp) {
		viewPoints.add(vp);
	}

	public List<ILMJYWorldPoint> getViewPoints() {
		return new ArrayList<>(viewPoints);
	}

	public void setViewPoints(List<ILMJYWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<ILMJYWorldMarch> getWorldMarchList() {
		return new ArrayList<>(worldMarchList);
	}

	public void removeMarch(ILMJYWorldMarch march) {
		worldMarchList.remove(march);
	}

	public void addMarch(ILMJYWorldMarch march) {
		worldMarchList.add(march);
	}

	public void setWorldMarchList(List<ILMJYWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public ILMJYBattleRoomState getState() {
		return state;
	}

	public void setState(ILMJYBattleRoomState state) {
		this.state = state;
		sync();
	}

	public long getCreateTime() {
		return createTime;
	}

	public int getBattleCfgId() {
		return battleCfgId;
	}

	public void setBattleCfgId(int battleCfgId) {
		this.battleCfgId = battleCfgId;
	}

	public boolean isCampAwin() {
		return campAwin;
	}

	public void setCampAwin(boolean campAwin) {
		this.campAwin = campAwin;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getOverTime() {
		return overTime;
	}

	public LMJYExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(LMJYExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(ILMJYPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player)
				.setType(type.getNumber())
				.setChatMsg(chatMsg)
				.setVoiceId(voiceId)
				.setVoiceLength(voiceLength)
				.build();
		Set<Player> tosend = new HashSet<>(getPlayerList(PState.GAMEING, PState.PREJOIN));
		ChatService.getInstance().sendChatMsg(Arrays.asList(chatMsgInfo), tosend);

	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		Set<Player> tosend = new HashSet<>(getPlayerList(PState.GAMEING, PState.PREJOIN));
		ChatService.getInstance().sendChatMsg(Arrays.asList(parames.toPBMsg()), tosend);
	}

	public int getOverState() {
		return overState;
	}

	/** 1 胜利 2 被npc击败 3 时间结束 */
	public void setOverState(int overState) {
		this.overState = overState;
	}

	public String getId() {
		return getXid().getUUID();
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	private GuildFormationObj guildFormationObj;

	public GuildFormationObj getGuildFormation(String guildId2) {
		if (Objects.isNull(guildFormationObj)) {
			guildFormationObj = new GuildFormationObj();
			guildFormationObj.unSerializ("");
		}

		return guildFormationObj;
	}

}
