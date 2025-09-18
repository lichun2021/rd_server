package com.hawk.game.module.spacemecha.worldpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.serializer.HawkSerializer;
import org.hawk.tuple.HawkTuple2;

import com.google.protobuf.ByteString;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.SpaceMecha.StrongHoldPB;
import com.hawk.game.protocol.SpaceMecha.StrongHoldStatus;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.WorldPoint.PBSerializeData;
import com.hawk.game.protocol.WorldPoint.PointData;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 星甲召唤据点
 * 
 * @author lating
 *
 */
public class StrongHoldWorldPoint extends WorldPoint {
	/**
	 * 据点ID
	 */
	private int strongHoldId;
	/**
	 * 当前是第几管血
	 */
	private int hpNum;
	/**
	 * 防守部队信息
	 */
	private List<ArmyInfo> defArmyList = new ArrayList<>();
	/**
	 * 是否是特殊据点：1是0否
	 */
	private int special;
	
	private HawkTuple2<Integer, Integer> effectTuple;
	
	private int enemyId;
	/**
	 * 据点发出首波敌军状态同步
	 */
	private int enmeyStatusSync;
	
	
	public StrongHoldWorldPoint() {
	}
	
	public StrongHoldWorldPoint(int x, int y, int areaId, int zoneId, int pointType) {
		super(x, y, areaId, zoneId, pointType);
	}
	
	@Override
	public WorldPointPB.Builder toBuilder(WorldPointPB.Builder builder,String viewerId) {
		super.toBuilder(builder,viewerId);
		builder.setStrongHold(toBuilder());
		builder.setGuildId(this.getGuildId());
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewerId);
		builder.setStrongHold(toBuilder());
		builder.setGuildId(this.getGuildId());
		return builder;
	}
	
	private StrongHoldPB.Builder toBuilder() {
		StrongHoldPB.Builder builder = StrongHoldPB.newBuilder();
		builder.setId(strongHoldId);
		builder.setPosX(this.getX());
		builder.setPosY(this.getY());
		builder.setHpNum(hpNum);
		builder.setSpecial(special);
		builder.setRemainBlood(this.getRemainBlood());
		if (this.getRemainBlood() <= 0) {
			builder.setStatus(StrongHoldStatus.HOLD_BROKEN);
		} else {
			int pointId = this.getId();
			Collection<IWorldMarch> marchs = WorldMarchService.getInstance().getWorldPointMarch(this.getId());
			Optional<IWorldMarch> optional = marchs.stream().filter(e -> e.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE && e.getTerminalId() == pointId).findAny();
			builder.setStatus(optional.isPresent() ? StrongHoldStatus.HOLD_BE_ATTACKING : StrongHoldStatus.HOLD_NO_ATTACK);
		}
		
		defArmyList.stream().forEach(armyInfo -> builder.addArmy(armyInfo.toArmySoldierPB(null)));
		
		SpaceMechaStrongholdCfg cfg = getStrongHoldCfg();
		List<PlayerHero> heros = NPCHeroFactory.getInstance().get(cfg.getHeroIdList());
		for (PlayerHero hero : heros) {
			builder.addHero(hero.toPBobj());
		}
		
		builder.setEnemyStatus(enmeyStatusSync);
		
		MechaSpaceInfo obj = SpaceMechaService.getInstance().getGuildSpace(this.getGuildId());
		if (obj != null && obj.getStage() != null) {
			builder.setWave(obj.getStage().getRound());
		}
		
		builder.setEnemyId(enemyId);
		SpaceMechaEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
		//组装部队信息
		NpcPlayer npcPlayer = SpaceMechaService.getInstance().getNpcPlayer(enemyCfg);
		List<ArmyInfo> armylist = enemyCfg.getArmyList();
		for (ArmyInfo armyInfo : armylist) {
			builder.addArmyInfo(armyInfo.toArmySoldierPB(npcPlayer));
		}
		
		return builder;
	}

	@Override
	public PointData.Builder buildPointData() {
		HawkLog.debugPrintln("spaceMecha build pointData stronghold, guildId: {}, strongHoldId: {}, special: {}, posX: {}, posY: {}, hpNum: {}, remainBlood: {}, defArmyList: {}", 
				getGuildId(), strongHoldId, special, getX(), getY(), hpNum, getRemainBlood(), defArmyList);
		PointData.Builder builder = super.buildPointData();
		PBSerializeData.Builder extraBuilder = PBSerializeData.newBuilder();
		extraBuilder.setParam1(serialize(strongHoldId));
		extraBuilder.setParam2(serialize(hpNum));
		List<String> list = defArmyList.stream().map(e -> e.toString()).collect(Collectors.toList());
		extraBuilder.setParam3(serialize(list));
		extraBuilder.setParam4(serialize(special));
		builder.setExtryData(extraBuilder.build());
		return builder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mergeFromPointData(PointData.Builder builder) {
		super.mergeFromPointData(builder);
		try {
			PBSerializeData data = builder.getExtryData();
			this.strongHoldId = deserialize(data.getParam1(), Integer.class);
			this.hpNum = deserialize(data.getParam2(), Integer.class);
			List<String> list = deserialize(data.getParam3(), ArrayList.class);
			for (String armyInfo : list) {
				ArmyInfo info = new ArmyInfo(armyInfo);
				defArmyList.add(info);
			}
			this.special = deserialize(data.getParam4(), Integer.class);
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
	
	public int getHpNum() {
		return hpNum;
	}

	public void setHpNum(int hpNum) {
		this.hpNum = hpNum;
	}

	public List<ArmyInfo> getDefArmyList() {
		return defArmyList;
	}

	public void setDefArmyList(List<ArmyInfo> defArmyList) {
		this.defArmyList = defArmyList;
	}

	public int getStrongHoldId() {
		return strongHoldId;
	}

	public void setStrongHoldId(int strongHoldId) {
		this.strongHoldId = strongHoldId;
	}
	
	public int getSpecial() {
		return special;
	}

	public void setSpecial(int special) {
		this.special = special;
	}

	public HawkTuple2<Integer, Integer> getEffectTuple() {
		return effectTuple;
	}

	public void storeEffectTuple(HawkTuple2<Integer, Integer> effectTuple) {
		this.effectTuple = effectTuple;
	}
	
	public int getEnemyId() {
		return enemyId;
	}

	public void storeEnemyId(int enemyId) {
		this.enemyId = enemyId;
	}
	
	public SpaceMechaStrongholdCfg getStrongHoldCfg() {
		 return HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, this.getStrongHoldId());
	}
	
	public void syncEnemyStatus() {
		if (enmeyStatusSync > 0) {
			return;
		}
		enmeyStatusSync = 1;
		WorldPointService.getInstance().notifyPointUpdate(this.getX(), this.getY());
	}
}
