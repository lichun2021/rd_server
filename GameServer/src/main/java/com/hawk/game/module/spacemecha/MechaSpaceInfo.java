package com.hawk.game.module.spacemecha;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.config.SpaceMechaCabinCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.module.spacemecha.stage.ISpaceMechaStage;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.SpaceMecha.SapceMechaSummary;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaEffValPB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaInfoPB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 星甲召唤舱体类
 * 
 * @author lating
 *
 */
public class MechaSpaceInfo {
	/**
	 * 舱体ID
	 */
	private String id;
	/**
	 * 所属联盟ID
	 */
	private String guildId;
	/**
	 * 最高难度等级
	 */
	private int maxLevel;
	private int newMaxLevel;
	/**
	 * 当前阶段
	 */
	private ISpaceMechaStage stage;
	
	/**
	 * 舱体点信息，指定key为0对应主舱 
	 */
	private Map<Integer, SpaceWorldPoint> spacePointMap = new ConcurrentHashMap<>();
	
	/**
	 * 特殊据点
	 */
	private StrongHoldWorldPoint spStrongHoldPoint;
	private int spStrongHoldId;
	private int spStrongHoldBroken;
	
	/**
	 * 敌方所在位置：包括怪物点、据点、boss点的点id
	 */
	private BlockingDeque<Integer> enemyPointIds = new LinkedBlockingDeque<Integer>();
	/**
	 * 向联盟机甲舱体发起进攻的各种野怪行军
	 */
	private Set<IWorldMarch> enemyMarchs = new ConcurrentHashSet<>();

	/**
	 * 阶段4宝箱点集合
	 */
	private Set<Integer> boxPointIdSet = new ConcurrentHashSet<>();
	
	/**
	 * 加到主舱上的作用号
	 */
	private Map<Integer, Integer> mainSpaceEffectMap = new ConcurrentHashMap<>();
	/**
	 * 加到boss上的作用号
	 */
	private Map<Integer, Integer> bossEffectMap = new ConcurrentHashMap<>();
	/**
	 * 敌军行军对应的enemy配置信息
	 */
	private Map<String, Integer> marchEnemyMap = new ConcurrentHashMap<>();

	
	public MechaSpaceInfo(String guildId) {
		this.id = HawkOSOperator.randomUUID();
		this.guildId = guildId;
		this.maxLevel = 1;
	}
	
	/**
	 * 获取舱体阶段
	 * @return
	 */
	public ISpaceMechaStage getStage() {
		return stage;
	}

	/**
	 * 设置舱体阶段
	 * @param stage
	 */
	public void setStage(ISpaceMechaStage stage) {
		this.stage = stage;
	}
	
	/**
	 * 获取舱体当前所属阶段值
	 * 
	 * @return
	 */
	public SpaceMechaStage getStageVal() {
		return stage == null ? null : stage.getStageVal();
	}

	/**
	 * 获取舱体的放置时间
	 * @return
	 */
	public long getPlaceTime() {
		return spacePointMap.get(SpacePointIndex.MAIN_SPACE).getPlaceTime();
	}

	/**
	 * 获取当前所放置的舱体等级
	 * @return
	 */
	public int getLevel() {
		return spacePointMap.get(SpacePointIndex.MAIN_SPACE).getSpaceLevel();
	}

	/**
	 * 获取可挑战的舱体最高等级
	 * 
	 * @return
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * 设置可挑战的舱体最高等级
	 * 
	 * @param maxLevel
	 */
	public void setMaxLevel(int maxLevel) {
		maxLevel = Math.min(maxLevel, SpaceMechaLevelCfg.getMaxLevel());
		this.maxLevel = maxLevel;
		GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guildInfoObj != null) {
			guildInfoObj.setSpaceMaxLv(maxLevel);
		}
	}

	/**
	 * 获取舱体所属联盟ID
	 * @return
	 */
	public String getGuildId() {
		return guildId;
	}

	/**
	 * 设置舱体所属联盟ID
	 * @param guildId
	 */
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	
	/**
	 * 获取所有的舱体世界点信息
	 * 
	 * @return
	 */
	protected Map<Integer, SpaceWorldPoint> getSpacePointMap() {
		return spacePointMap;
	}
	
	/**
	 * 获取舱体的世界点信息
	 * 
	 * @param spaceIndex
	 * @return
	 */
	public SpaceWorldPoint getSpaceWorldPoint(int spaceIndex) {
		return spacePointMap.get(spaceIndex);
	}
	
	/**
	 * 获取舱体的世界点ID
	 * @param spaceIndex
	 * @return
	 */
	public int getSpacePointId(int spaceIndex) {
		return spacePointMap.get(spaceIndex).getId();
	}
	
	/**
	 * 获取舱体的剩余血量
	 * @param pointIndex
	 * @return
	 */
	public int getSpaceBlood(int pointIndex) {
		return spacePointMap.get(pointIndex).getSpaceBlood();
	}
	
	/**
	 * 获取放置舱体消耗的星币数量
	 * 
	 * @return
	 */
	public int getCost() {
		SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(getLevel());
		return cfg.getCost();
	}
	
	/**
	 * 判断是否有行军驻扎在舱体中
	 * 
	 * @param spaceIndex
	 * @return
	 */
	public boolean hasMarchInSpace(int spaceIndex) {
		BlockingDeque<String> marchs = getSpaceDefMarchs(spaceIndex);
		return !marchs.isEmpty();
	}

	/**
	 * 获取舱体的防守行军ID
	 * 
	 * @param spaceIndex
	 * @return
	 */
	public BlockingDeque<String> getSpaceDefMarchs(int spaceIndex) {
		return spacePointMap.get(spaceIndex).getDefMarchs();
	}
	
	/**
	 * 获取舱体的防守行军
	 * 
	 * @param spaceIndex
	 * @return
	 */
	public List<IWorldMarch> getSpaceMarchs(int spaceIndex) {
		return spacePointMap.get(spaceIndex).getDefMarchList();
	}
	
	/**
	 * 添加舱体防守行军
	 * 
	 * @param spaceIndex
	 * @param march
	 */
	public void addSpaceMarch(int spaceIndex, IWorldMarch march) {
		spacePointMap.get(spaceIndex).addDefMarch(march);
	}

	/**
	 * 舱体行军遣返
	 * @param index
	 */
	public void forceSpaceMarchBack(int spaceIndex) {
		spacePointMap.get(spaceIndex).forceMarchBack();
	}
	
	/**
	 * 遣返个人行军
	 * @param spaceIndex
	 * @param playerId
	 */
	public void forceSpaceMarchBack(int spaceIndex, String playerId) {
		spacePointMap.get(spaceIndex).forceMarchBack(playerId);
	}
	
	/**
	 * 遣返集体行军
	 */
	public void forceAllSpaceMarchBack() {
		this.forceSpaceMarchBack(SpacePointIndex.MAIN_SPACE);
		this.forceSpaceMarchBack(SpacePointIndex.SUB_SPACE_1);
		this.forceSpaceMarchBack(SpacePointIndex.SUB_SPACE_2);
	}
	
	/**
	 * 遣返个人行军
	 * @param playerId
	 */
	public void forceAllSpaceMarchBack(String playerId) {
		this.forceSpaceMarchBack(SpacePointIndex.MAIN_SPACE, playerId);
		this.forceSpaceMarchBack(SpacePointIndex.SUB_SPACE_1, playerId);
		this.forceSpaceMarchBack(SpacePointIndex.SUB_SPACE_2, playerId);
	}
	
	/**
	 * 移除舱体的防守参与者
	 * @param playerId
	 */
	public void removeDefenceMember(String playerId) {
		spacePointMap.get(SpacePointIndex.MAIN_SPACE).removeDefenceMember(playerId);
		spacePointMap.get(SpacePointIndex.SUB_SPACE_1).removeDefenceMember(playerId);
		spacePointMap.get(SpacePointIndex.SUB_SPACE_2).removeDefenceMember(playerId);
	}
	
	/**
	 * 获取舱体的队长行军ID
	 * @param spaceIndex
	 * @return
	 */
	public String getSpaceLeaderMarchId(int spaceIndex) {
		return spacePointMap.get(spaceIndex).getLeaderMarchId();
	}
	
	/**
	 * 获取舱体防守行军队长信息
	 * @param spaceIndex
	 * @return
	 */
	public Player getSpaceLeader(int spaceIndex) {
		return spacePointMap.get(spaceIndex).getLeader();
	}

	/**
	 * 获取所有的野怪行军信息
	 * @return
	 */
	public Set<IWorldMarch> getEnemyMarchs() {
		return enemyMarchs;
	}
	
	/**
	 * 添加野怪行军
	 * @param march
	 */
	public void addEnemyMarch(IWorldMarch march) {
		if (march == null) {
			return;
		}
		enemyMarchs.add(march);
		WorldPointService.getInstance().notifyPointUpdate(march.getTerminalX(), march.getTerminalY());
	}
	
	/**
	 * 移除野怪信息
	 * 
	 * @param marchId
	 */
	public void removeEnemyMarch(String marchId){
		Iterator<IWorldMarch> it = enemyMarchs.iterator();
		while (it.hasNext()) {
			IWorldMarch march = it.next();
			if(march.getMarchId().equals(marchId)){
				HawkLog.debugPrintln("spaceMecha remove enemy march, guildId: {}, marchId: {}, marchType: {}, posX: {}, posY: {}", guildId, marchId, march.getMarchType(), march.getOrigionX(), march.getOrigionY());
				it.remove();
				WorldMarchService.getInstance().removeMarch(march);
			}
		}
	}

	/**
	 * 获取所有的野怪点ID
	 * @return
	 */
	public BlockingDeque<Integer> getEnemyPointIds() {
		return enemyPointIds;
	}

	/**
	 * 添加野怪点ID
	 * @param pointId
	 */
	public void addEnemyPointId(int pointId) {
		this.enemyPointIds.add(pointId);
	}
	
	/**
	 * 设置特殊据点信息
	 * @param worldPoint
	 */
	public void setSpStrongHoldPoint(StrongHoldWorldPoint worldPoint) {
		this.spStrongHoldPoint = worldPoint;
		this.spStrongHoldId = worldPoint.getStrongHoldId();
	}
	
	/**
	 * 获取特殊据点
	 * @return
	 */
	public StrongHoldWorldPoint getSpStrongHoldPoint() {
		return spStrongHoldPoint;
	}

	/**
	 * 获取特殊据点ID
	 * @return
	 */
	public int getSpStrongHoldCfgId() {
		return spStrongHoldId;
	}
	
	/**
	 * 获取特殊据点是否被击破的标识
	 * @return
	 */
	public int getSpStrongHoldBroken() {
		return spStrongHoldBroken;
	}

	/**
	 * 移除据点
	 * @param pointId
	 * @param strongHoldId
	 */
	public void removeStrongHold(int pointId, int strongHoldId) {
		StrongHoldWorldPoint strongHoldPoint = (StrongHoldWorldPoint) WorldPointService.getInstance().getWorldPoint(pointId);
		HawkTuple2<Integer, Integer> tuple = strongHoldPoint.getEffectTuple();
		
		enemyPointIds.remove(pointId);
		WorldPointService.getInstance().removeWorldPoint(pointId, true);
		SpaceMechaStrongholdCfg strongHoldCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, strongHoldId);
		int[] xy = GameUtil.splitXAndY(pointId);
		HawkLog.logPrintln("spaceMecha remove stronghold point, guildId: {}, strongHoldId: {}, special: {}, posX: {}, posY: {}", guildId, strongHoldId, strongHoldCfg.getIsSpecial(), xy[0], xy[1]);
		
		// 随机buff，防守点位的随机增益减益效果
		addEffectToMainSpace(tuple.first, tuple.second);
		if (strongHoldCfg.getIsSpecial() > 0) {
			this.spStrongHoldBroken = 1;
			spStrongHoldPoint.setRemainBlood(0);
			spStrongHoldPoint.setHpNum(strongHoldCfg.getHpNumber());
			// 联盟中文本消息告知已击破特殊据点
			Object[] objects = new Object[] { strongHoldPoint.getX(), strongHoldPoint.getY(), strongHoldId };
			ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_SPHOLD_BROKEN_GUILD).setGuildId(guildId).addParms(objects).build());
			//ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.SYS_BROADCAST).setKey(NoticeCfgId.SPACE_MECHA_SPHOLD_BROKEN_PERSON).addParms(objects).build());
		}
	}

	/**
	 * 获取敌军行军-怪物配置对应信息
	 */
	public Map<String, Integer> getMarchEnemyMap() {
		return marchEnemyMap;
	}
	
	/**
	 * 给主舱添加作用号加成
	 * 
	 * @param effId
	 * @param effVal
	 */
	public void addEffectToMainSpace(int effId, int effVal) {
		HawkLog.logPrintln("spaceMecha add effect to mainspace, guildId: {}, effId: {}, effVal: {}", guildId, effId, effVal);
		int oldVal = mainSpaceEffectMap.getOrDefault(effId, 0);
		mainSpaceEffectMap.put(effId, oldVal + effVal);
	}
	
	/**
	 * 获取附加在舱体上的作用号值
	 * 
	 * @param effId
	 * @return
	 */
	public int getSpaceEffVal(int effId) {
		int val = mainSpaceEffectMap.getOrDefault(effId, 0);
		HawkLog.debugPrintln("spaceMecha get effect from mainspace, guildId: {}, effId: {}, effVal: {}", guildId, effId, val);
		return val;
	}
	
	/**
	 * 给boss加作用号
	 * 
	 * @param effId
	 * @param effVal
	 */
	public void addEffectToBoss(int effId, int effVal) {
		HawkLog.logPrintln("spaceMecha add effect to boss, guildId: {}, effId: {}, effVal: {}", guildId, effId, effVal);
		int oldVal = bossEffectMap.getOrDefault(effId, 0);
		bossEffectMap.put(effId, oldVal + effVal);
	}
	
	/**
	 * 获取boss加成作用号值
	 * 
	 * @param effId
	 * @return
	 */
	public int getBossEffVal(int effId) {
		int val = bossEffectMap.getOrDefault(effId, 0);
		HawkLog.debugPrintln("spaceMecha get effect from boss, guildId: {}, effId: {}, effVal: {}", guildId, effId, val);
		return val;
	}
	
	/**
	 * 星甲的概要信息
	 */
	public SpaceMechaInfoPB.Builder toBuilder() {
		SpaceMechaInfoPB.Builder builder = SpaceMechaInfoPB.newBuilder();
		SapceMechaSummary.Builder summary = SapceMechaSummary.newBuilder();
		if (stage == null) {
			summary.setStage(SpaceMechaStage.SPACE_END);
			builder.setSummary(summary);
			return builder;
		}
		
		summary.setStage(getStageVal());
		summary.setLevel(getLevel());
		summary.setEndTime(getStage().getEndTime());
		summary.setMainSpaceBlood(getSpaceBlood(SpacePointIndex.MAIN_SPACE));
		summary.setSpace1Blood(getSpaceBlood(SpacePointIndex.SUB_SPACE_1));
		summary.setSpace2Blood(getSpaceBlood(SpacePointIndex.SUB_SPACE_2));
		SpaceWorldPoint spacePoint = spacePointMap.get(SpacePointIndex.MAIN_SPACE);
		summary.setPosX(spacePoint.getX());
		summary.setPosY(spacePoint.getY());
		
		for (Entry<Integer, Integer> entry : mainSpaceEffectMap.entrySet()) {
			SpaceMechaEffValPB.Builder effVal = SpaceMechaEffValPB.newBuilder();
			effVal.setEffId(entry.getKey());
			effVal.setEffVal(entry.getValue());
			summary.addSpaceEffVal(effVal);
		}
		
		for (Entry<Integer, Integer> entry : bossEffectMap.entrySet()) {
			SpaceMechaEffValPB.Builder effVal = SpaceMechaEffValPB.newBuilder();
			effVal.setEffId(entry.getKey());
			effVal.setEffVal(entry.getValue());
			summary.addEnemyEffVal(effVal);
		}
		
		builder.setSummary(summary);
		return builder;
	}
	
	/**
	 * 同步星甲的概要信息
	 */
	public void syncSpaceMechaInfo() {
		List<String> playerIds = GuildService.getInstance().getOnlineMembers(guildId);
		if (playerIds.isEmpty()) {
			return;
		}
		
		SpaceMechaInfoPB.Builder builder = toBuilder();
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				for (String playerId : playerIds) {
					Player player = GlobalData.getInstance().getActivePlayer(playerId);
					if (player != null) {
						CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(player, MechaSpaceConst.PERSONAL_STRONGHOD_AWARD_TOTAL);
						builder.getSummaryBuilder().setStrongHoldRewardTimes(customData.getValue());
						builder.getSummaryBuilder().setStrongHoldAtkTimes(SpaceMechaService.getInstance().getAtkStrongHoldTimesToday(player));
						player.sendProtocol(HawkProtocol.valueOf(HP.code2.SPACE_MECHA_INFO_SYNC_VALUE, builder));
					}
				}
				return null;
			}
		});
	}
	
	/**
	 * 同步概要信息
	 * 
	 * @param playerId
	 */
	public void syncSpaceMacheInfo(String playerId) {
		SpaceMechaInfoPB.Builder builder = toBuilder();
		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player != null) {
			CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(player, MechaSpaceConst.PERSONAL_STRONGHOD_AWARD_TOTAL);
			builder.getSummaryBuilder().setStrongHoldRewardTimes(customData.getValue());
			builder.getSummaryBuilder().setStrongHoldAtkTimes(SpaceMechaService.getInstance().getAtkStrongHoldTimesToday(player));
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.SPACE_MECHA_INFO_SYNC_VALUE, builder));
		}
	}
	
	/**
	 * 清除点
	 */
	public void clearPoint() {
		HawkLog.logPrintln("spaceMecha clear point, guildId: {}, stage: {}, enemyPointIds: {}", getGuildId(), getStageVal(), enemyPointIds);
		try {
			List<Integer> pointIds = spacePointMap.values().stream().map(e -> e.getId()).collect(Collectors.toList());
			pointIds.addAll(enemyPointIds);
			WorldPointService.getInstance().removeWorldPoints(pointIds, true);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		enemyPointIds.clear();
	}
	
	/**
	 * 获取宝箱点信息
	 * 
	 * @return
	 */
	public Set<Integer> getBoxPointIdSet() {
		return boxPointIdSet;
	}
	
	/**
	 * 添加宝箱点
	 * @param pointId
	 */
	public void addBoxPoint(int pointId) {
		boxPointIdSet.add(pointId);
	}
	
	/**
	 * 清除宝箱点
	 * @param pointId
	 */
	public void removeBoxPoint(int pointId) {
		boxPointIdSet.remove(pointId);
	}
	
	public String getId() {
		return this.id;
	}
	
	public int getNewMaxLevel() {
		return newMaxLevel;
	}
	
	public void setNewMaxLevel(int newMaxLevel) {
		this.newMaxLevel = newMaxLevel;
	}
	
}
