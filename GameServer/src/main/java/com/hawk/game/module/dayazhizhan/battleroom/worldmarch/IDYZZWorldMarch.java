package com.hawk.game.module.dayazhizhan.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.MarchPart;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.entity.DYZZMarchEntity;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZReportPushMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.GuildAssistant.AssistanceCallbackNotifyPB;
import com.hawk.game.protocol.GuildWar.GuildWarShowPB;
import com.hawk.game.protocol.GuildWar.GuildWarSingleInfo;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World;
import com.hawk.game.protocol.World.HPBattleResultInfoSync;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.WorldMarchDeletePush;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

public abstract class IDYZZWorldMarch implements IWorldMarch {
	private long nextPushMarchEvent;
	private boolean changed;
	private int delayCnt;
	private final int MAX_DELAY = 100;// 2;
	private final int PUSH_DELAY = 500;

	private final IDYZZPlayer parent;
	private DYZZMarchEntity marchEntity;

	public IDYZZWorldMarch(IDYZZPlayer parent) {
		this.parent = parent;
	}

	public final IDYZZPlayer getParent() {
		return parent;
	}

	@Override
	public DYZZMarchEntity getMarchEntity() {
		return marchEntity;
	}

	public void setMarchEntity(DYZZMarchEntity marchEntity) {
		this.marchEntity = marchEntity;
	}
	
	@Override
	public  int getLeaderMaxMassJoinSoldierNum(Player leader) {
		int leaderSoldierCnt = leader.getMaxMarchSoldierNum(this.getMarchEntity().getEffectParams());
		int joinMax = leader.getData().getPlayerEffect().getEffVal(EffType.GUILD_TEAM_ARMY_NUM, getMarchEntity().getEffectParams());
		return leaderSoldierCnt + joinMax;
	}
	
	public GuildWarShowPB getGuildWarShoPb(String viwerGuildId) {
		// 行军状态
		WorldMarchStatus marchStatus = WorldMarchStatus.valueOf(this.getMarchEntity().getMarchStatus());

		GuildWarShowPB.Builder showPb = GuildWarShowPB.newBuilder();
		// 行军id
		showPb.setMarchId(this.getMarchId());
		// 行军类型
		showPb.setMarchType(this.getMarchType());
		// 行军状态
		showPb.setMarchStatus(marchStatus);
		// 攻击方行军数据
		showPb.setInitiative(this.getGuildWarInitiativeInfo());
		// 防守方行军数据
		showPb.setPassivity(this.getGuildWarPassivityInfo());
		// 行军结束时间(集结状态的行军为集结结束时间)
		if (marchStatus.equals(WorldMarchStatus.MARCH_STATUS_WAITING)) {
			showPb.setEndTime(this.getMarchEntity().getStartTime());
		} else {
			showPb.setEndTime(this.getMarchEntity().getEndTime());
		}
		if (Objects.equals(viwerGuildId, getParent().getDYZZGuildId())) {
			showPb.setIsatk(true);
		}
		return showPb.build();
	}

	/** 行军战力 */
	public double armyBattlePoint() {
		double armyBattlePoint = 0;
		for (ArmyInfo army : getArmys()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if (cfg == null) {
				continue;
			}
			armyBattlePoint += cfg.getPower() * army.getFreeCnt();
		}
		return armyBattlePoint;
	}

	public static boolean onArmyBack(final Player player, List<ArmyInfo> armyList, List<Integer> heroId, int supersoldierId, IWorldMarch worldMarch) {
		for (ArmyInfo army : armyList) {
			if (army.getDeadCount() > 0) {
				army.setWoundedCount(army.getWoundedCount() + army.getDeadCount());
				army.setDeadCount(0);
			}
		}
		boolean result = ArmyService.getInstance().onArmyBack(player, armyList, heroId, supersoldierId, worldMarch);
		return result;
	}

	public int getDeadArmyCnt(List<ArmyInfo> armyList) {
		int cnt = 0;
		if (armyList != null) {
			for (ArmyInfo info : armyList) {
				cnt += info.getDeadCount();
			}
		}
		return cnt;
	}

	public List<ArmyInfo> calcDeadArmy(List<ArmyInfo> selfArmys) {
		final List<ArmyInfo> armyDeadList = new ArrayList<>();
		List<ArmyInfo> armyLeftList = new ArrayList<>();
		for (ArmyInfo armyInfo : selfArmys) {
			ArmyInfo leftArmy = armyInfo.getCopy();
			if (armyInfo.getDeadCount() > 0) {
				armyDeadList.add(armyInfo.getCopy());
				leftArmy.setTotalCount(leftArmy.getTotalCount() - leftArmy.getDeadCount());
				leftArmy.setDeadCount(0);
			}
			armyLeftList.add(leftArmy);
		}
		return armyLeftList;
	}

	/**
	 * 构建出征信息builder
	 * 
	 * @param relation
	 * @return
	 */
	public WorldMarchPB.Builder toBuilder(WorldMarchRelation relation) {
		WorldMarchPB.Builder builder = WorldMarchPB.newBuilder();
		DYZZMarchEntity entity = getMarchEntity();
		builder.setPlayerId(entity.getPlayerId());

		builder.setPlayerName(entity.getPlayerName());
		builder.setExtraSpyMarch(isExtraSpyMarch());
		builder.setEndTime(entity.getEndTime());
		builder.setMarchJourneyTime(entity.getMarchJourneyTime());

		String guildTag = getParent().getGuildTag();
		if (guildTag != null && !"".equals(guildTag)) {
			builder.setGuildTag(guildTag);
		}

		// 像素坐标
		if (entity.getItemUseX() != 0 && entity.getItemUseY() != 0) {
			builder.setItemUseTime(entity.getItemUseTime());
			builder.setItemUseX(entity.getItemUseX());
			builder.setItemUseY(entity.getItemUseY());
		}

		builder.setMarchId(entity.getMarchId());
		builder.setMarchStatus(WorldMarchStatus.valueOf(entity.getMarchStatus()));
		builder.setMarchType(WorldMarchType.valueOf(entity.getMarchType()));

		builder.setOrigionX(entity.getOrigionX());
		builder.setOrigionY(entity.getOrigionY());
		builder.setRelation(relation);

		if (entity.getSpeedUpTimes() > 0) {
			builder.setSpeedTimes(entity.getSpeedUpTimes());
		}

		builder.setStartTime(entity.getStartTime());
		builder.setTargetId(entity.getTargetId());

		builder.setTerminalX(entity.getTerminalX());
		builder.setTerminalY(entity.getTerminalY());

		if (entity.getCallbackX() > 0 || entity.getCallbackY() > 0) {
			builder.setCallBackX(entity.getCallbackX());
			builder.setCallBackY(entity.getCallbackY());
			builder.setCallBackTime(entity.getCallBackTime());
		}

		if (entity.getResEndTime() > 0) {
			builder.setResEndTime(entity.getResEndTime());
		}

		if (entity.getResStartTime() > 0) {
			builder.setResStartTime(entity.getResStartTime());
		}

		if (entity.getMassReadyTime() > 0) {
			builder.setMassReadyTime(entity.getMassReadyTime());
		}

		List<Integer> heroIdList = entity.getHeroIdList();
		if (!heroIdList.isEmpty()) {
			List<PlayerHero> heros = getParent().getHeroByCfgId(heroIdList);
			for (PlayerHero hero : heros) {
				// builder.addHeroId(hero.getCfgId());
				// builder.addHeroLvl(hero.getLevel());
				builder.addHeroList(hero.toPBobj());
				int showProficiencyEffect = hero.getShowProficiencyEffect();
				if(showProficiencyEffect>0){
					builder.addProficiencyEffect(showProficiencyEffect);
				}
			}
		}
		if (entity.getSuperSoldierId() > 0) {
			Optional<SuperSoldier> ssoldierOp = getParent().getSuperSoldierByCfgId(entity.getSuperSoldierId());
			if (ssoldierOp.isPresent()) {
				builder.setSsoldier(ssoldierOp.get().toPBobj());
			}
		}
		
		//已部署的超武信息
		builder.setDeployedSwInfo(getParent().getDeployedSwInfo());
		builder.setMechacoreShowInfo(getParent().getMechacoreShowInfo());

		for (int i = 0; i < entity.getArmys().size(); i++) {
			ArmyInfo info = entity.getArmys().get(i);
			if (info == null) {
				continue;
			}
			if (info.getFreeCnt() <= 0) {
				continue;
			}
			builder.addArmy(info.toArmySoldierPB(getParent()));
		}
		if (entity.getEffectList() != null && !entity.getEffectList().isEmpty()) {
			for (Integer eff : entity.getEffectList()) {
				builder.addShowEff(eff);
			}
		}

		if (entity.getMarchType() == WorldMarchType.COLLECT_RESOURCE_VALUE) {
			builder.setCollectBaseSpeed(entity.getCollectBaseSpeed());
			builder.setCollectSpeed(entity.getCollectSpeed());
		}
		DressItem marchDress = WorldPointService.getInstance().getShowDress(entity.getPlayerId(), DressType.MARCH_DRESS_VALUE);
		if (marchDress != null) {
			long now = getParent().getParent().getCurTimeMil();
			if (marchDress.getStartTime() + marchDress.getContinueTime() > now) {
				builder.setMarchShowDress(marchDress.getModelType());
				if (marchDress.getShowEndTime() > now) {
					builder.setMarchDressShowType(marchDress.getShowType());
				}
			}
		}

		ArmourSuitType armourSuit = ArmourSuitType.valueOf(entity.getArmourSuit());
		if (armourSuit != null) {
			builder.setArmourSuit(armourSuit);
		}
		// 使用了行军表情
		marchEntity.buildEmoticon(builder);
		builder.setFormation(getFormationInfo());
		return builder;
	}

	/**
	 * 联盟战争界面里单人信息
	 * 
	 * @param worldMarch
	 * @return
	 */
	public GuildWarSingleInfo.Builder getGuildWarSingleInfo() {
		WorldMarch worldMarch = getMarchEntity();
		IDYZZPlayer player = getParent();
		GuildWarSingleInfo.Builder builder = GuildWarSingleInfo.newBuilder();
		builder.setPlayerId(worldMarch.getPlayerId());
		builder.setPlayerName(player.getName());
		builder.setIconId(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		WorldMarchStatus marchStatus = WorldMarchStatus.valueOf(worldMarch.getMarchStatus());
		builder.setMarchStatus(marchStatus);
		builder.setMarchId(worldMarch.getMarchId());
		List<PlayerHero> heros = player.getHeroByCfgId(worldMarch.getHeroIdList());
		if (heros != null && !heros.isEmpty()) {
			for (PlayerHero hero : heros) {
				builder.addHeroInfo(hero.toPBobj());
			}
		}
		SuperSoldier ssoldier = player.getSuperSoldierByCfgId(worldMarch.getSuperSoldierId()).orElse(null);
		if (Objects.nonNull(ssoldier)) {
			builder.setSsoldier(ssoldier.toPBobj());
		}

		List<ArmyInfo> armys = worldMarch.getArmys();
		for (ArmyInfo army : armys) {
			builder.addArmys(army.toArmySoldierPB(player));
		}
		// 行军结束时间(集结状态的行军为集结结束时间)
		if (marchStatus.equals(WorldMarchStatus.MARCH_STATUS_WAITING)) {
			builder.setEndTime(worldMarch.getStartTime());
		} else {
			builder.setEndTime(worldMarch.getEndTime());
		}
		builder.setStartTime(worldMarch.getStartTime());
		builder.setJourneyTime(worldMarch.getMarchJourneyTime());
		return builder;
	}

	/**
	 * 构建出征信息builder （不包含PB optional 字段）
	 * 
	 * @param relation
	 * @return
	 */
	public WorldMarchPB.Builder toBuilder() {
		WorldMarchPB.Builder builder = WorldMarchPB.newBuilder();
		builder.setPlayerId(marchEntity.getPlayerId());
		builder.setEndTime(marchEntity.getEndTime());
		builder.setMarchJourneyTime(marchEntity.getMarchJourneyTime());

		builder.setPlayerName(marchEntity.getPlayerName());

		String guildTag = getParent().getGuildTag();
		if (guildTag != null && !"".equals(guildTag)) {
			builder.setGuildTag(guildTag);
		}

		builder.setMarchId(marchEntity.getMarchId());
		builder.setMarchStatus(WorldMarchStatus.valueOf(marchEntity.getMarchStatus()));
		builder.setMarchType(WorldMarchType.valueOf(marchEntity.getMarchType()));

		builder.setOrigionX(marchEntity.getOrigionX());
		builder.setOrigionY(marchEntity.getOrigionY());

		builder.setStartTime(marchEntity.getStartTime());
		builder.setTargetId(marchEntity.getTargetId());

		builder.setTerminalX(marchEntity.getTerminalX());
		builder.setTerminalY(marchEntity.getTerminalY());
		DressItem marchDress = WorldPointService.getInstance().getShowDress(marchEntity.getPlayerId(), DressType.MARCH_DRESS_VALUE);
		if (marchDress != null) {
			long now = getParent().getParent().getCurTimeMil();
			if (marchDress.getStartTime() + marchDress.getContinueTime() > now) {
				builder.setMarchShowDress(marchDress.getModelType());
				if (marchDress.getShowEndTime() > now) {
					builder.setMarchDressShowType(marchDress.getShowType());
				}
			}
		}
		ArmourSuitType armourSuit = ArmourSuitType.valueOf(marchEntity.getArmourSuit());
		if (armourSuit != null) {
			builder.setArmourSuit(armourSuit);
		}
		// 使用了行军表情
		marchEntity.buildEmoticon(builder);
		builder.setFormation(getFormationInfo());
		return builder;
	}

	/** 行军加速 */
	public boolean speedUp(int timeReducePercent, int speedUpTimes) {

		DYZZMarchEntity march = getMarchEntity();
		if (march.isInvalid()) {
			return false;
		}

		if (!isMarchState() && !isReturnBackMarch()) {
			return false;
		}
		// 首先总时间缩短
		long current = getParent().getParent().getCurTimeMil();
		long resaveTime = (march.getEndTime() - current) * timeReducePercent / 100;
		march.setEndTime(march.getEndTime() - resaveTime);

		// 计算当前坐标点和时间并记录
		AlgorithmPoint currPoint = WorldUtil.getMarchCurrentPosition(march);
		if (currPoint != null) {
			if (!Double.isNaN(currPoint.getX()) && !Double.isNaN(currPoint.getY())) {
				march.setItemUseX(currPoint.getX());
				march.setItemUseY(currPoint.getY());
			} else {
				march.setItemUseX(0.0d);
				march.setItemUseY(0.0d);
			}
			march.setSpeedUpTimes(march.getSpeedUpTimes() + speedUpTimes);
			march.setItemUseTime(getParent().getParent().getCurTimeMil());
			int px = (int) Math.floor(march.getItemUseX());
			int py = (int) Math.floor(march.getItemUseY());
			if (px == march.getTerminalX() && py == march.getTerminalY()) {
				return false;
			}
			// 更新行军状态
			updateMarch();
		}

		if (this.isMassMarch()) {
			Set<IDYZZWorldMarch> joinMarchs = getMassJoinMarchs(false);
			for (IDYZZWorldMarch joinMarch : joinMarchs) {
				joinMarch.getMarchEntity().setEndTime(march.getEndTime());
				// 发给自己
				HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_UPDATE_PUSH_VALUE, joinMarch.toBuilder(WorldMarchRelation.SELF));
				joinMarch.getParent().sendProtocol(protocol);
			}
		}

		if (this instanceof IDYZZReportPushMarch) {
			((IDYZZReportPushMarch) this).reachTimeChange();
		}
		return true;
	}

	@Override
	public Set<IDYZZWorldMarch> getQuarterMarch() {
		if (getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return Collections.emptySet();
		}
		List<IDYZZWorldMarch> defMarchList = getParent().getParent().getPointMarches(getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		return new HashSet<>(defMarchList);
	}

	@Override
	public Set<IDYZZWorldMarch> getMassJoinMarchs(boolean b) {
		return Collections.emptySet();
	}

	/** 通知行军事件 */
	public void notifyMarchEvent(MarchEvent eventType) {
		
		DYZZMarchEntity march = getMarchEntity();
		int marchStatus = march.getMarchStatus();
		if (eventType == MarchEvent.MARCH_UPDATE &&
				!(marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
						|| marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE
						|| marchStatus == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE)) {
			return;
		}
		changed = true;
		if (delayCnt < MAX_DELAY && eventType == MarchEvent.MARCH_UPDATE && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE) {
			delayCnt++;
			if (getParent().getParent().getCurTimeMil() - nextPushMarchEvent > 0) {
				nextPushMarchEvent = getParent().getParent().getCurTimeMil() + PUSH_DELAY;
			}
		} else {
			pushMarchEvent(eventType);
		}
	}

	public void checkPushMarchEvent() {
		if (readyToPush()) {
			pushMarchEvent(MarchEvent.MARCH_UPDATE);
		}
	}
	
	public boolean readyToPush(){
		return changed && getParent().getParent().getCurTimeMil() > nextPushMarchEvent;
	}

	private void pushMarchEvent(MarchEvent eventType) {
		if (!changed) {
			return;
		}

		markUnchanged();
		// 计算可见起点和终点的玩家集合
		// 通知所有影响到的玩家本条行军事件
		for (IDYZZPlayer player : getParent().getParent().getPlayerList(DYZZState.GAMEING)) {
			WorldMarchRelation relation = getRelation(player);
			// 自己的行军不走这种同步模式
			if (relation == WorldMarchRelation.SELF) {
				continue;
			}
			MarchEventSync.Builder builder = createSyncPB(eventType, relation);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, builder));
		}

	}

	public void markUnchanged() {
		delayCnt = 0;
		changed = false;
		nextPushMarchEvent = getParent().getParent().getCurTimeMil() + PUSH_DELAY; // 0.5秒内不再推送
	}

	private MarchEventSync.Builder createSyncPB(MarchEvent eventType, WorldMarchRelation relation) {
		MarchEventSync.Builder builder = MarchEventSync.newBuilder();
		builder.setEventType(eventType.getNumber());

		MarchData.Builder dataBuilder = MarchData.newBuilder();
		dataBuilder.setMarchId(getMarchEntity().getMarchId());
		if (eventType != MarchEvent.MARCH_DELETE) {
			dataBuilder.setMarchPB(this.toBuilder(relation));
		}
		builder.addMarchData(dataBuilder);
		return builder;
	}

	/**
	 * 获得行军和玩家的关系
	 * 
	 * @param march
	 * @param tarplayer
	 *            行军线的观察者
	 * @return
	 */
	public WorldMarchRelation getRelation(IDYZZPlayer tarplayer) {

		// 自己的行军
		if (parent == tarplayer) {
			return WorldMarchRelation.SELF;
		}

		// 同盟玩家行军
		if (tarplayer.isInSameGuild(parent)) {
			return WorldMarchRelation.GUILD_FRIEND;
		}

		// 目标点
		Optional<IDYZZWorldPoint> targetPointOp = getParent().getParent().getWorldPoint(marchEntity.getTerminalId());
		if (getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			targetPointOp = getParent().getParent().getWorldPoint(marchEntity.getOrigionId());
		}

		// 目标点为null
		if (!targetPointOp.isPresent()) {
			return WorldMarchRelation.NONE;
		}

		// 判断 自己联盟id 目标点联盟id 和 行军发起者联盟id关系
		IDYZZWorldPoint targetPoint = targetPointOp.get();
		if (tarplayer.getDYZZGuildId().equals(targetPoint.getGuildId()) && !tarplayer.isInSameGuild(parent)) {
			return WorldMarchRelation.ENEMY;
		}

		return WorldMarchRelation.NONE;
	}

	public void sendBattleResultInfo(boolean isWin, List<ArmyInfo> atkArmyList, List<ArmyInfo> defArmyList, boolean isMonsterDead) {
		HPBattleResultInfoSync.Builder builder = HPBattleResultInfoSync.newBuilder();
		builder.setMarchId(this.getMarchId());
		for (ArmyInfo army : atkArmyList) {
			builder.addMyArmyId(army.getArmyId());
		}
		for (ArmyInfo army : defArmyList) {
			builder.addOppArmyId(army.getArmyId());
		}
		
		builder.setIsMonsterDead(isMonsterDead);

		if (isWin) {
			builder.setIsWin(Result.SUCCESS_VALUE);
		} else {
			builder.setIsWin(Result.FAIL_VALUE);
		}

		List<IDYZZPlayer> players = getParent().getParent().getPlayerList(DYZZState.GAMEING);
		for (IDYZZPlayer pla : players) {
			pla.sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
		}
		for (IDYZZPlayer anchor : getParent().getParent().getAnchors()) {
			anchor.sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
		}

	}
	
	public void sendBattleResultInfo(boolean isWin, List<ArmyInfo> atkArmyList, List<ArmyInfo> defArmyList) {
		sendBattleResultInfo(isWin, atkArmyList, defArmyList, false);
	}

	@Override
	public void remove() {
		if (this instanceof IDYZZReportPushMarch) {
			((IDYZZReportPushMarch) this).removeAttackReport();
		}
		getMarchEntity().setMarchStatus(0);
		WorldMarchDeletePush.Builder builder = WorldMarchDeletePush.newBuilder();
		builder.setMarchId(getMarchId());
		builder.setRelation(WorldMarchRelation.SELF);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_DELETE_PUSH_VALUE, builder);
		getParent().sendProtocol(protocol);

		getHeros().forEach(hero -> hero.backFromMarch(this));
		getParent().getSuperSoldierByCfgId(getSuperSoldierId()).ifPresent(hero -> hero.backFromMarch(this));
		getParent().getParent().removeMarch(this);
		notifyMarchEvent(MarchEvent.MARCH_DELETE);
	}

	public void updateDefMarchAfterWar(List<IDYZZWorldMarch> helpMarchList, Map<String, List<ArmyInfo>> defArmyMap) {
		// 更新援助防御玩家行军的部队
		for (IDYZZWorldMarch tmpMarch : helpMarchList) {
			List<ArmyInfo> leftList = defArmyMap.get(tmpMarch.getPlayerId());
			if (WorldUtil.calcSoldierCnt(leftList) > 0) {
				tmpMarch.getMarchEntity().setArmys(leftList);
				continue;
			}

			if (tmpMarch.getMarchType() == WorldMarchType.ASSISTANCE) {
				AssistanceCallbackNotifyPB.Builder callbackNotifyPB = AssistanceCallbackNotifyPB.newBuilder();
				callbackNotifyPB.setMarchId(tmpMarch.getMarchId());
				IDYZZPlayer assistPlayer = tmpMarch.getParent();
				assistPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.ASSISTANCE_MARCH_CALLBACK, callbackNotifyPB));
			}

			// 死光了，行军立即送死兵回家
			tmpMarch.onMarchReturn(tmpMarch.getMarchEntity().getTerminalId(), tmpMarch.getMarchEntity().getOrigionId(), leftList);
		}
	}

	public void onMarchCallback() {
		AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(marchEntity);
		if (null == point) {
			point = new AlgorithmPoint(marchEntity.getTerminalX(), marchEntity.getTerminalY());
		}
		marchEntity.setCallBackTime(getParent().getParent().getCurTimeMil());
		marchEntity.setCallBackX(point.getX());
		marchEntity.setCallBackY(point.getY());
		onMarchReturn(marchEntity.getTerminalId(), marchEntity.getOrigionId(), marchEntity.getArmys());

	}

	public boolean onMarchReturn(int origionId, int terminalId, List<ArmyInfo> selfArmys) {
		// 修改状态
		marchEntity.setMarchStatus(WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE);
		this.onMarchReturn();

		// 所有死亡按伤兵

		int freeCount = 0;
		List<ArmyInfo> leftArmys = new ArrayList<>();
		for (ArmyInfo army : selfArmys) {
			ArmyInfo leftArmy = army.getCopy();
			leftArmy.setWoundedCount(leftArmy.getWoundedCount() + leftArmy.getDeadCount());
			leftArmy.setDeadCount(0);
			leftArmys.add(leftArmy);
			freeCount += leftArmy.getFreeCnt();
		}

		// // 死亡部队立即结算
		// if (getDeadArmyCnt(selfArmys) > 0) {
		// selfArmys = calcDeadArmy(selfArmys);
		// }
		marchEntity.setArmys(leftArmys);

		// 状态改变时修改加速召回信息
		marchEntity.setItemUseTime(0);
		marchEntity.setItemUseX(0.0d);
		marchEntity.setItemUseY(0.0d);
		marchEntity.setSpeedUpTimes(0);

		marchEntity.setOrigionId(origionId);
		marchEntity.setTerminalId(terminalId);
		long marchTime = getMarchNeedTime();
		// if (marchEntity.getCallBackTime() > 0) {
		// marchTime = Math.min(marchEntity.getCallBackTime() - marchEntity.getStartTime(), marchTime);
		// }
		if (freeCount == 0 && getMarchType() != WorldMarchType.SPY) {
			marchTime = 0;
		}

		marchEntity.setStartTime(getParent().getParent().getCurTimeMil());
		marchEntity.setEndTime(marchEntity.getStartTime() + marchTime);
		marchEntity.setMarchJourneyTime((int) marchTime);

		// 行军上需要显示的作用号
		List<Integer> marchShowEffList = new ArrayList<>();
		int[] marchShowEffs = WorldMarchConstProperty.getInstance().getMarchShowEffArray();
		if (marchShowEffs != null) {
			for (int i = 0; i < marchShowEffs.length; i++) {
				int effVal = getParent().getEffect().getEffVal(EffType.valueOf(marchShowEffs[i]));
				if (effVal > 0) {
					marchShowEffList.add(effVal);
				}
			}
		}

		if (!marchShowEffList.isEmpty()) {
			this.getMarchEntity().resetEffect(marchShowEffList);
		}

		// 刷新出征
		this.updateMarch();
		return true;
	}

	@Override
	public int compareTo(IWorldMarch o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void register() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getMarchId() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getMarchId();
	}

	@Override
	public abstract void heartBeats();

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.valueOf(marchEntity.getMarchType());
	}

	@Override
	public boolean isPassiveMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPlayerId() {
		return marchEntity.getPlayerId();
	}

	@Override
	public boolean isReturnBackMarch() {
		return getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE;
	}

	@Override
	public boolean isMarchState() {
		return getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE;
	}

	@Override
	public boolean isMassMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMassJoinMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAssistanceMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPresidentMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPresidentTowerMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWarFlagMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSuperWeaponMarch() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isGuildSpaceMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isManorMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReachAndStopMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isManorMarchReachStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNeedCalcTickMarch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getMarchNeedTime() {
		// 行军速度
		double speed = getMarchBaseSpeed() * getParent().getMarchSpeedUp();
		// 出发点
		AlgorithmPoint origion = new AlgorithmPoint(getMarchEntity().getOrigionX(), getMarchEntity().getOrigionY());
		if (getMarchEntity().getCallBackTime() > 0) {
			origion = new AlgorithmPoint(getMarchEntity().getCallbackX(), getMarchEntity().getCallbackY());
		}
		// 目标点
		AlgorithmPoint terminal = new AlgorithmPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY());
		// 根据黑土地进行分段
		List<MarchPart> parts = WorldUtil.getMarchParts(origion, terminal);

		double time = 0;
		for (MarchPart part : parts) {
			// 正常行军时间
			double partNormalTime = getPartMarchTime(part.getDistance(), speed, false);
			// // 机器人模式下 不进行黑土地减速
			// if (part.isSlowDown() && !(GsConfig.getInstance().isRobotMode()
			// && GsConfig.getInstance().isDebug())) {
			// // 黑土地行军时间
			// double partSlowDownTime = getPartMarchTime(part.getDistance(),
			// speed, true);
			// // 计算黑土地行军作用号效果
			// partSlowDownTime = effectToMarchTime(partNormalTime, true,
			// partSlowDownTime);
			//
			// time += partSlowDownTime;
			// continue;
			// }

			time += partNormalTime;
		}

		// 计算作用号效果(向上取整)
		time = Math.ceil(effectToMarchTime(time, false, 0));
		time = time > 1.0 ? time : 1.0;

		return (long) (time * 1000);
	}

	/**
	 * 获取一段距离行军时间
	 * 
	 * @param distance
	 * @param speed
	 * @param isSlowDownPart
	 * 
	 * @return
	 */
	private double getPartMarchTime(double distance, double speed, boolean isSlowDownPart) {
		// 行军速度倍数因子
		double factor = 1;
		if (isSlowDownPart) {
			factor = WorldMarchConstProperty.getInstance().getWorldMarchCoreRangeTime();
		}

		// 行军距离修正参数
		double param1 = WorldMarchConstProperty.getInstance().getDistanceAdjustParam();
		// 部队行军类型行军时间调整参数
		double param2 = 0.0d;

		WorldMarchType marchType = this.getMarchType();
		switch (marchType) {
		case SPY:
			param2 = WorldMarchConstProperty.getInstance().getReconnoitreTypeAdjustParam();
			speed = 1.0d;
			break;
		case ASSISTANCE_RES:
			param2 = WorldMarchConstProperty.getInstance().getResAidTypeAdjustParam();
			speed = 1.0d;
			break;
		case RANDOM_BOX:
			param2 = WorldMarchConstProperty.getInstance().getBoxTypeAdjustParam();
			speed = 1.0d;
			break;
		case WAREHOUSE_STORE:
		case WAREHOUSE_GET:
			param2 = WorldMarchConstProperty.getInstance().getAllianceStoreAdjustParam();
			speed = 1.0d;
			break;
		case ATTACK_MONSTER:
		case MONSTER_MASS:
		case MONSTER_MASS_JOIN:
			param2 = WorldMarchConstProperty.getInstance().getMonsterTypeAdjustParam();
			break;
		case NEW_MONSTER:
			param2 = WorldMarchConstProperty.getInstance().getNewMonsterAdjustParam();
			break;
		case NIAN_SINGLE:
		case NIAN_MASS:
		case NIAN_MASS_JOIN:
			param2 = WorldMarchConstProperty.getInstance().getNianTypeAdjustParam();
			break;
		default:
			param2 = WorldMarchConstProperty.getInstance().getArmyTypeAdjustParam();
			break;
		}

		return Math.pow((distance), param1) * param2 * factor / speed;
	}

	/**
	 * 添加行军速度作用号
	 * 
	 * @param march
	 * @param speed
	 * @return
	 */
	private double effectToMarchTime(double time, boolean slowDown, double slowDownTime) {
		Player player = getParent();
		if (slowDown) {
			// 作用号207： 黑土地行军加速 -> 行军时间 = 非雪地时间 + 雪地时间/（1 + 作用值/10000）
			slowDownTime /= 1 + player.getEffect().getEffVal(EffType.MARCH_SPD_SNOW_LAND, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
			return time + slowDownTime;
		}

		int marchType = getMarchEntity().getMarchType();

		int speedUpEffectVal = 0;

		// 作用号203： 行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD, getMarchEntity().getEffectParams());

		// 作用号204： 攻击野怪行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isAtkMonsterMarch(this)) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_MONSTER, getMarchEntity().getEffectParams());
		}

		// 作用号206/4020： 侦察时行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isSpyMarch(marchType)) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_SPY, getMarchEntity().getEffectParams());
			speedUpEffectVal += player.getEffect().getEffVal(EffType.SPY_MARCH_SPEED_ADD, getMarchEntity().getEffectParams());
		}

		// 作用号211： 新版野怪行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (marchType == WorldMarchType.NEW_MONSTER_VALUE) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_NEW_MONSTER, getMarchEntity().getEffectParams());
		}

		// 作用号212： 野怪行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isAtkMonsterMarch(this) || marchType == WorldMarchType.NEW_MONSTER_VALUE || WorldUtil.isAtkBossMarch(this)) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_ALL_MONSTER, getMarchEntity().getEffectParams());
		}

		// 作用号222： 野怪行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isAtkMonsterMarch(this) || marchType == WorldMarchType.NEW_MONSTER_VALUE || WorldUtil.isAtkBossMarch(this)) {
			speedUpEffectVal += player.getEffect().getEffVal(EffType.MARCH_SPD_ALL_MONSTER_CARD, getMarchEntity().getEffectParams());
		}

		// 作用号610： 联盟交易时行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (marchType == WorldMarchType.ASSISTANCE_RES_VALUE) {
			speedUpEffectVal += player.getEffect().getEffVal(Const.EffType.GUILD_TRADE_MARCH_SPD, getMarchEntity().getEffectParams());
		}

		// 作用号614： 联盟援助时，援军部队行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (this.isAssistanceMarch()) {
			speedUpEffectVal += player.getEffect().getEffVal(Const.EffType.GUILD_HELP_MARCH_SPD, getMarchEntity().getEffectParams());
		}

		// 作用号221：高达行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (WorldUtil.isAtkBossMarch(this)) {
			speedUpEffectVal += player.getEffect().getEffVal(Const.EffType.MARCH_SPD_GUNDAM, getMarchEntity().getEffectParams());
		}

		// 作用号208： 我方开启集结后，其他指挥官加入集结的队伍的行军速度增加 -> 行军时间 = 基础时间/(1 + 作用值/10000)
		if (this.isMassJoinMarch()) {
			Optional<IDYZZWorldPoint> targetPoint = getParent().getParent().getWorldPoint(marchEntity.getTerminalId());
			if (targetPoint.isPresent()) {
				IDYZZPlayer leader = (IDYZZPlayer) targetPoint.get();
				speedUpEffectVal += leader.getEffect().getEffVal(Const.EffType.MARCH_SPD_MASS, marchEntity.getEffectParams());
			}
		}

		time /= (1 + speedUpEffectVal * GsConst.EFF_PER);

		int slowDownEffectVal = 0;

		// // 作用号445：玩家基地被侦察或攻击时，敌人行军时间提升XX倍 -> 实际行军时间 = 其他作用号加速后行军时间 *（1 + 作用值/10000）
		// WorldPoint terminalPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getTerminalId());
		// if (this.isReturnBackMarch()) {
		// terminalPoint = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getOrigionId());
		// }
		//
		// if ((marchType == WorldMarchType.ATTACK_PLAYER_VALUE
		// || marchType == WorldMarchType.SPY_VALUE
		// || marchType == WorldMarchType.MASS_VALUE)
		// && terminalPoint != null
		// && WorldUtil.isPlayerPoint(terminalPoint)) {
		// Player defPlayer = GlobalData.getInstance().makesurePlayer(getMarchEntity().getTargetId());
		// if (defPlayer != null && defPlayer.getEffect().getEffVal(EffType.CITY_ENEMY_MARCH_SPD) > 0) {
		// slowDownEffectVal += defPlayer.getEffect().getEffVal(EffType.CITY_ENEMY_MARCH_SPD);
		// }
		// }

		time *= (1 + slowDownEffectVal * GsConst.EFF_PER);

		return time;
	}

	@Override
	public double getMarchBaseSpeed() {
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		armyList.addAll(marchEntity.getArmys());
		if (isMassMarch()) {
			Set<IDYZZWorldMarch> joinMarchs = getMassJoinMarchs(true);
			for (IDYZZWorldMarch tempMarch : joinMarchs) {
				armyList.addAll(tempMarch.marchEntity.getArmys());
			}
		}
		return WorldUtil.minSpeedInArmy(getParent(), armyList);
	}

	@Override
	public void onMarchStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarchReach(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarchReturn() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarchStop(int status, List<ArmyInfo> armys, WorldPoint targetPoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> calcPointViewers(int viewRadiusX, int viewRadiusY) {
		// TODO Auto-generated method stub
		return IWorldMarch.super.calcPointViewers(viewRadiusX, viewRadiusY);
	}

	@Override
	public void updateMarch() {
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_UPDATE_PUSH_VALUE, this.toBuilder(WorldMarchRelation.SELF));
		parent.sendProtocol(protocol);
		notifyMarchEvent(MarchEvent.MARCH_UPDATE);
	}

	@Override
	public boolean needShowInGuildWar() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 获取主动方联盟战争界面信息
	 * 
	 * @return
	 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarInitiativeInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();
		// 队长id
		IDYZZPlayer leader = getParent();
		// 队长位置
		int[] leaderPos = leader.getPosXY();
		// 队长

		builder.setPointType(WorldPointType.PLAYER);
		builder.setX(leaderPos[0]);
		builder.setY(leaderPos[1]);
		builder.setLeaderArmyLimit(this.getMaxMassJoinSoldierNum(leader));
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		builder.setGuildTag(leader.getGuildTag());
		builder.setGuildId(leader.getDYZZGuildId());
		builder.setLeaderMarch(getGuildWarSingleInfo());

		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(this.getMarchEntity().getArmys());

		// 加入集结的行军
		Set<IDYZZWorldMarch> joinMarchs = this.getMassJoinMarchs(false);
		for (IDYZZWorldMarch joinMarch : joinMarchs) {
			if (joinMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			builder.addJoinMarchs(joinMarch.getGuildWarSingleInfo());
			reachArmyCount += WorldUtil.calcSoldierCnt(joinMarch.getMarchEntity().getArmys());
		}
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}

	/** 获取被动方联盟战争界面信息 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		// 协议
		GuildWarTeamInfo.Builder builder = GuildWarTeamInfo.newBuilder();

		// 队长id
		String leaderId = this.getMarchEntity().getTargetId();
		IDYZZPlayer leader = DYZZRoomManager.getInstance().makesurePlayer(leaderId);
		// 队长位置
		int[] pos = leader.getPos();
		// 队长
		builder.setPointType(WorldPointType.PLAYER);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setLeaderArmyLimit(leader.getMaxAssistSoldier());
		builder.setGridCount(leader.getMaxMassJoinMarchNum());
		builder.setGuildTag(leader.getGuildTag());
		builder.setGuildId(leader.getDYZZGuildId());
		// 队长信息
		GuildWarSingleInfo.Builder leaderInfo = GuildWarSingleInfo.newBuilder();
		leaderInfo.setPlayerId(leader.getId());
		leaderInfo.setPlayerName(leader.getName());
		leaderInfo.setIconId(leader.getIcon());
		leaderInfo.setPfIcon(leader.getPfIcon());
		leaderInfo.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING);
		List<ArmyInfo> armys = ArmyService.getInstance().getFreeArmyList(leader);
		for (ArmyInfo army : armys) {
			leaderInfo.addArmys(army.toArmySoldierPB(leader));
		}
		builder.setLeaderMarch(leaderInfo);
		// 已经到达的士兵数量
		int reachArmyCount = WorldUtil.calcSoldierCnt(armys);

		List<IDYZZWorldMarch> assistandMarchs = getParent().getParent().getPointMarches(leader.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE);
		for (IDYZZWorldMarch assistandMarch : assistandMarchs) {
			if (assistandMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			builder.addJoinMarchs(assistandMarch.getGuildWarSingleInfo());
			reachArmyCount += WorldUtil.calcSoldierCnt(assistandMarch.getMarchEntity().getArmys());
		}
		builder.setCityLevel(leader.getCityLv());
		builder.setReachArmyCount(reachArmyCount);
		return builder;
	}

	@Override
	public boolean checkGuildWarShow() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doCollectRes(boolean changeSpeed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doQuitGuild(String guildId) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ArmyInfo> getArmys() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getArmys();
	}

	@Override
	public Player getPlayer() {
		// TODO Auto-generated method stub
		return getParent();
	}

	@Override
	public List<PlayerHero> getHeros() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getHeros();
	}

	@Override
	public boolean isGrabResMarch() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.isGrabResMarch();
	}

	@Override
	public void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveCityProcess(long currentTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getTerminalId() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getTerminalId();
	}

	@Override
	public int getMarchStatus() {
		return getMarchEntity().getMarchStatus();
	}

	@Override
	public int getAlarmPointId() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getAlarmPointId();
	}

	@Override
	public int getTerminalY() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getTerminalY();
	}

	@Override
	public int getTerminalX() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getTerminalX();
	}

	@Override
	public long getStartTime() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getStartTime();
	}

	@Override
	public long getEndTime() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getEndTime();
	}

	@Override
	public long getMassReadyTime() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getMassReadyTime();
	}

	@Override
	public String getAssistantStr() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getAssistantStr();
	}

	@Override
	public int getOrigionX() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getOrigionX();
	}

	@Override
	public int getOrigionY() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getOrigionY();
	}

	@Override
	public int getOrigionId() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getOrigionId();
	}

	@Override
	public int getSuperSoldierId() {
		// TODO Auto-generated method stub
		return IWorldMarch.super.getSuperSoldierId();
	}

	/*** 返回家中 */
	public void onMarchBack() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFortressMarch() {
		return false;
	}

	@Override
	public boolean needShowInNationWar() {
		return false;
	}
	
	/**
	 * 填充个人编队信息
	 * @param viewerId
	 */
	public World.WorldFormation.Builder getFormationInfo() {
		int formation = this.getMarchEntity().getFormation();
		World.WorldFormation.Builder formationBuilder = World.WorldFormation.newBuilder();
		formationBuilder.setIndex(formation);
		return formationBuilder;
	}
}
