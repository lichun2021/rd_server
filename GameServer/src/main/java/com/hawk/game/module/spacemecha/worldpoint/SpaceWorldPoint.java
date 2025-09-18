package com.hawk.game.module.spacemecha.worldpoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.serializer.HawkSerializer;

import com.google.protobuf.ByteString;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaCabinCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaSubcabinCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.SpaceMecha.MechaSpacePB;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.WorldPoint.PBSerializeData;
import com.hawk.game.protocol.WorldPoint.PointData;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 星甲召唤舱体
 * 
 * @author lating
 *
 */
public class SpaceWorldPoint extends WorldPoint {
	/**
	 * 剩余血量
	 */
	private int spaceBlood;
	/**
	 * 序号：0-主舱，1-子舱1号，2-子舱2号
	 */
	private int spaceIndex;
	/**
	 * 舱体放置时间
	 */
	private long placeTime;
	/**
	 * 舱体的难度等级
	 */
	private int spaceLevel;
	/**
	 * 防守行军
	 */
	private BlockingDeque<String> defMarchs = new LinkedBlockingDeque<String>();
	/**
	 * 参与防守的成员：发生过战斗的才算
	 */
	private Set<String> defenceMembers = new HashSet<>();
	/**
	 * 向舱体点行军的野怪npc
	 */
	private Map<Integer, Player> pointNpcPlayerMap = new ConcurrentHashMap<>();
	/**
	 * 进攻舱体的野怪ID
	 */
	private int atkEnemyId;
	
	
	public SpaceWorldPoint() {
	}
	
	public SpaceWorldPoint(int x, int y, int areaId, int zoneId, int pointType) {
		super(x, y, areaId, zoneId, pointType);
	}
	
	@Override
	public WorldPointPB.Builder toBuilder(WorldPointPB.Builder builder,String viewerId) {
		super.toBuilder(builder,viewerId);
		builder.setGuildId(getGuildId());
		builder.setMechaSpace(toBuilder());
		for (String marchId : defMarchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march != null && march.isMassMarch()) {
				Set<IWorldMarch>  joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
				for (IWorldMarch joinMarch : joinMarchs) {
					if (joinMarch.getPlayerId().equals(viewerId)) {
						builder.setHasMarchStop(true);
						return builder;
					}
				}
			}
			if (march != null && march.getPlayerId().equals(viewerId)) {
				builder.setHasMarchStop(true);
				return builder;
			}
		}
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewerId);
		builder.setGuildId(getGuildId());
		builder.setMechaSpace(toBuilder());
		for (String marchId : defMarchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march != null && march.isMassMarch()) {
				Set<IWorldMarch>  joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
				for (IWorldMarch joinMarch : joinMarchs) {
					if (joinMarch.getPlayerId().equals(viewerId)) {
						builder.setHasMarchStop(true);
						return builder;
					}
				}
			}
			if (march != null && march.getPlayerId().equals(viewerId)) {
				builder.setHasMarchStop(true);
				return builder;
			}
		}
		return builder;
	}
	
	private MechaSpacePB.Builder toBuilder() {
		MechaSpacePB.Builder builder = MechaSpacePB.newBuilder();
		builder.setRemainBlood(spaceBlood);
		builder.setLevel(spaceLevel);
		builder.setStatus(defMarchs.isEmpty() ? 0 : 1);
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(this.getGuildId());
		if (spaceObj != null && spaceObj.getStage() != null) {
			spaceObj.getStage().buildSpacePointInfo(this, builder);
		}
		return builder;
	}
	
	@Override
	public PointData.Builder buildPointData() {
		HawkLog.debugPrintln("spaceMecha build pointData, guildId: {}, placeTime: {}, posX: {}, posY: {}, spaceIndex: {}, remainBlood: {}", getGuildId(), placeTime, getX(), getY(), getSpaceIndex(), getSpaceBlood());
		PointData.Builder builder = super.buildPointData();
		PBSerializeData.Builder extraBuilder = PBSerializeData.newBuilder();
		extraBuilder.setParam1(serialize(spaceBlood));
		extraBuilder.setParam2(serialize(spaceIndex));
		extraBuilder.setParam3(serialize(placeTime));
		extraBuilder.setParam4(serialize(spaceLevel));
		builder.setExtryData(extraBuilder.build());
		return builder;
	}

	@Override
	public void mergeFromPointData(PointData.Builder builder) {
		super.mergeFromPointData(builder);
		try {
			PBSerializeData data = builder.getExtryData();
			this.spaceBlood = deserialize(data.getParam1(), Integer.class);
			this.spaceIndex = deserialize(data.getParam2(), Integer.class);
			this.placeTime = deserialize(data.getParam3(), Long.class);
			this.spaceLevel = deserialize(data.getParam4(), Integer.class);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	private <T> ByteString serialize(T value) {
		return ByteString.copyFrom(HawkSerializer.serialize(value));
	}

	private <T> T deserialize(ByteString bytes, Class<T> type) {
		return HawkSerializer.deserialize(bytes.toByteArray(), type);
	}

	public int getSpaceBlood() {
		return spaceBlood;
	}

	public void setSpaceBlood(int bloodRemain) {
		this.spaceBlood = bloodRemain;
	}

	public int getSpaceIndex() {
		return spaceIndex;
	}

	public void setSpaceIndex(int spaceIndex) {
		this.spaceIndex = spaceIndex;
	}

	public long getPlaceTime() {
		return placeTime;
	}

	public void setPlaceTime(long placeTime) {
		this.placeTime = placeTime;
	}

	public int getSpaceLevel() {
		return spaceLevel;
	}

	public void setSpaceLevel(int spaceLevel) {
		this.spaceLevel = spaceLevel;
	}

	public BlockingDeque<String> getDefMarchs() {
		return defMarchs;
	}
	
	/**
	 * 添加防守行军
	 * 
	 * @param march
	 * @param isInit
	 */
	public void addDefMarch(IWorldMarch march) {
		HawkLog.logPrintln("spaceMecha add defence march, guildId: {}, posX: {}, posY: {}, spaceIndex: {}, remainBlood: {}, marchId: {}", getGuildId(), getX(), getY(), getSpaceIndex(), getSpaceBlood(), march.getMarchId());
		boolean empty = defMarchs.isEmpty();
		//如果没有行军，则此行军为队长行军
		if(defMarchs.isEmpty()){
			march.getMarchEntity().setLeaderPlayerId(march.getPlayerId());
			defMarchs.add(march.getMarchId());
		} else {
			WorldMarch leaderMarch = WorldMarchService.getInstance().getWorldMarch(defMarchs.getFirst());
			if (leaderMarch == null) {
				defMarchs.removeFirst();
				addDefMarch(march);
			} else {
				march.getMarchEntity().setLeaderPlayerId(leaderMarch.getPlayerId());
				defMarchs.add(march.getMarchId());
			}
		}
		
		if (empty && !defMarchs.isEmpty()) {
			WorldPointService.getInstance().notifyPointUpdate(this.getX(), this.getY());
		}
		
		SpaceMechaService.getInstance().logSpaceMechaDefChange(march.getPlayer(), this, true);
	}
	
	/**
	 * 更换队长
	 * 
	 * @param targetPlayerId
	 */
	public void changeMarchLeader(String targetPlayerId) {
		String changeMarchId = null;
		for (String marchId : defMarchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null) {
				continue;
			}
			if (!targetPlayerId.equals(march.getPlayerId())) {
				continue;
			}
			changeMarchId = march.getMarchId();
			break;
		}
		
		if (HawkOSOperator.isEmptyString(changeMarchId)) {
			return;
		}
		
		defMarchs.remove(changeMarchId);
		defMarchs.addFirst(changeMarchId);
		changeLeader();
	}
	
	/**
	 * 更换队长
	 * @param leaderId
	 */
	private void changeLeader() {
		if (defMarchs.isEmpty()) {
			return;
		}
		String leaderMarchId = defMarchs.getFirst();
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		if (leaderMarch == null) {
			defMarchs.removeFirst();
			changeLeader();
		} else {
			for (String marchId : defMarchs) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				if (march != null) {
					march.getMarchEntity().setLeaderPlayerId(leaderMarch.getPlayerId());
				}
			}
		}
		
	}
	
	/**
	 * 获取行军列表
	 * 
	 * @return
	 */
	public List<IWorldMarch> getDefMarchList() {
		List<IWorldMarch> stayMarchs = new ArrayList<IWorldMarch>();
		for (String marchId : defMarchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid()) {
				continue;
			}
			stayMarchs.add(march);
		}
		return stayMarchs;
	}
	
	/**
	 * 遣返行军
	 */
	public void forceMarchBack() {
		if (defMarchs.isEmpty()) {
			return;
		}
		
		try {
			int count = 0;
			Iterator<String> iter = defMarchs.iterator();
			while (iter.hasNext()) {
				String marchId = iter.next();
				iter.remove();
				count++;
				HawkLog.logPrintln("spaceMecha force army back, guildId: {}, posX: {}, posY: {}, spaceIndex: {}, remainBlood: {}, marchId: {}", getGuildId(), getX(), getY(), getSpaceIndex(), getSpaceBlood(), marchId);
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				if (march == null) {
					continue;
				}
				SpaceMechaService.getInstance().logSpaceMechaDefChange(march.getPlayer(), this, false);
				WorldMarchService.getInstance().onMarchReturn(march, HawkTime.getMillisecond(), 0);
			}
			
			if (count > 0) {
				WorldPointService.getInstance().getWorldScene().update(this.getAoiObjId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 遣返个人行军
	 * 
	 * @param playerId
	 */
	public void forceMarchBack(String playerId) {
		if (defMarchs.isEmpty()) {
			return;
		}
		
		try {
			int count = 0;
			Iterator<String> iter = defMarchs.iterator();
			while (iter.hasNext()) {
				String marchId = iter.next();
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				if (march != null && march.getPlayerId().equals(playerId)) {
					iter.remove();
					count++;
					HawkLog.logPrintln("spaceMecha force army back, guildId: {}, playerId: {}, posX: {}, posY: {}, spaceIndex: {}, remainBlood: {}, marchId: {}", getGuildId(), playerId, getX(), getY(), getSpaceIndex(), getSpaceBlood(), marchId);
					SpaceMechaService.getInstance().logSpaceMechaDefChange(march.getPlayer(), this, false);
					WorldMarchService.getInstance().onMarchReturn(march, HawkTime.getMillisecond(), 0);
				}
			}
			
			if (count > 0) {
				WorldPointService.getInstance().getWorldScene().update(this.getAoiObjId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		changeLeader();
	}
	
	/**
	 * 移除防守行军
	 * @param marchId
	 */
	public void removeDefMarch(IWorldMarch march) {
		defMarchs.remove(march.getMarchId());
		changeLeader();
		SpaceMechaService.getInstance().logSpaceMechaDefChange(march.getPlayer(), this, false);
	}
	
	/**
	 * 获取队长行军ID
	 * 
	 * @return
	 */
	public String getLeaderMarchId() {
		if (defMarchs.isEmpty()) {
			return null;
		}
		String leaderMarchId = defMarchs.getFirst();
		IWorldMarch march = WorldMarchService.getInstance().getMarch(leaderMarchId);
		if (march != null) {
			return leaderMarchId;
		}
		
		defMarchs.removeFirst();
		changeLeader();
		return getLeaderMarchId();
	}
	
	/**
	 * 获取队长玩家
	 * 
	 * @return
	 */
	public Player getLeader() {
		String leaderMarchId = getLeaderMarchId();
		if (HawkOSOperator.isEmptyString(leaderMarchId)) {
			return null;
		}
		
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		if (leaderMarch == null) {
			return null;
		}
		Player leader = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
		return leader;
	}
	
	public Set<String> getDefenceMembers() {
		return defenceMembers;
	}
	
	public void addDefenceMember(String playerId) {
		HawkLog.debugPrintln("spaceMecha add defence member, guildId: {}, posX: {}, posY: {}, spaceIndex: {}, remainBlood: {}, playerId: {}", getGuildId(), getX(), getY(), getSpaceIndex(), getSpaceBlood(), playerId);
		defenceMembers.add(playerId);
	}
	
	public void removeDefenceMember(String playerId) {
		defenceMembers.remove(playerId);
	}
	
	/**
	 * 添加npcplayer
	 * 
	 * @param pointId
	 * @param player
	 */
	public void addNpcPlayer(int pointId, NpcPlayer player) {
		player.setPlayerPos(pointId);
		int[] xy = GameUtil.splitXAndY(pointId);
		HawkLog.debugPrintln("spaceMecha add npc player, guildId: {}, posX: {}, posY: {}, npc player: {}", getGuildId(), xy[0], xy[1], player.getId());
		pointNpcPlayerMap.put(pointId, player);
	}
	
	public void removeNpcPlayer(int pointId) {
		int[] xy = GameUtil.splitXAndY(pointId);
		String playerId = pointNpcPlayerMap.containsKey(pointId) ? pointNpcPlayerMap.get(pointId).getId() : "";
		HawkLog.debugPrintln("spaceMecha remove npc player, guildId: {}, posX: {}, posY: {}, npc player: {}", getGuildId(), xy[0], xy[1], playerId);
		pointNpcPlayerMap.remove(pointId);
	}
	
	public Player getNpcPlayer(int pointId) {
		return pointNpcPlayerMap.get(pointId);
	}
	
	public int getEnemyMarchCount() {
		return pointNpcPlayerMap.size();
	}

	public int getAtkEnemyId() {
		return atkEnemyId;
	}

	public void storeAtkEnemyId(int atkEnemyId) {
		this.atkEnemyId = atkEnemyId;
	}
	
	public int getSpaceCfgId() {
		if (this.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE) {
			SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(spaceLevel);
			return cfg.getId();
		} else {
			SpaceMechaSubcabinCfg cfg = SpaceMechaSubcabinCfg.getCfg(spaceLevel);
			return cfg.getId();
		}
	}
	
}
