package com.hawk.game.module.lianmengyqzz.battleroom.player.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.RandomUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tickable.HawkTickable;

import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.module.YQZZMarchModule;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZAttackMonsterMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZMassMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildType;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZCommandCenter;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZMonster;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchResp;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;

public class YQZZTestMarchAction extends HawkTickable {
	private IYQZZPlayer parent;
	private long nextMarch;

	@Override
	public void onTick() {
		if (nextMarch == 0) {
			for (ArmyEntity armyEntity : parent.getData().getMarchArmy()) {
				if (armyEntity.getFree() < 1000000) {
					armyEntity.addFree(1000000);
				}
			}
			nextMarch = nextMarch();
			return;
		}
		if (nextMarch > parent.getParent().getCurTimeMil()) {
			return;
		}

		nextMarch = nextMarch();
		if (parent.getMaxCityDef() == 0) {
			parent.setMaxCityDef(100000);
		}

		// List<IYQZZBuilding> buildings = parent.getParent().getYQZZBuildingList();
		// IYQZZBuilding build = HawkRand.randomObject(buildings);
		// onYQZZ_SINGLE_MARCHStart(build);
		List<IYQZZWorldMarch> mlist = parent.getParent().getPlayerMarches(parent.getId());
		for (IYQZZWorldMarch march : mlist) {
			march.speedUp(20, 2000);
		}
		if (mlist.size() > 4 || parent.getParent().getWorldMarchCount() > 1000) {
//			System.out.println(parent.getX() +" " + parent.getY()+"  "+ mlist.size() +"  "+ parent.getParent().getWorldMarchCount());
			return;
		}
//		System.out.println(parent.getName() + " march cnt " + mlist.size());

		List<IYQZZWorldPoint> list = new ArrayList<>(getParent().getParent().getViewPoints());
		Collections.shuffle(list);
		for (IYQZZWorldPoint point : list) {
			if (point instanceof YQZZMonster) {
				onYQZZ_SINGLE_MARCHStart(point);
				break;
			}
		}

	}

	private boolean onWorldSpyStart(IYQZZWorldPoint point) {
		IYQZZPlayer player = parent;

		getParent().getParent().startMarch(player, player, point, WorldMarchType.SPY, "", 0, new EffectParams());

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SPY_S_VALUE, builder));
		return true;
	}

	private boolean onWorldMassJoinStart() {
		IYQZZPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(0).setPosY(0);
		int maxMarch = player.getMaxMarchSoldierNum(EffectParams.getDefaultVal());
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			int count = Math.min(maxMarch / 2, army.getFree() / 3);
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(count));
			maxMarch -= count;
			if (maxMarch < 100) {
				break;
			}
		}
		List<IYQZZWorldMarch> guildMarchs = player.getParent().getGuildWarMarch(player.getGuildId());
		guildMarchs = guildMarchs.stream().filter(m -> m instanceof IYQZZMassMarch).collect(Collectors.toList());
		if (guildMarchs.isEmpty()) {
			return true;
		}
		Collections.shuffle(guildMarchs);
		IYQZZMassMarch toJoin = (IYQZZMassMarch) guildMarchs.get(0);
		reqBul.setType(toJoin.getJoinMassType());
		reqBul.setMarchId(toJoin.getMarchId());

		WorldMarchReq req = reqBul.build();
		String massMarchId = req.getMarchId();

		YQZZMarchModule marchModule = parent.getParent().getModule(YQZZConst.ModuleType.YQZZMarch);
		IYQZZWorldMarch worldMarch = parent.getParent().getMarch(massMarchId);
		int marchType = req.getType().getNumber();
		// 不能加入自己的集结
		IYQZZPlayer leader = worldMarch.getParent();
		String leaderId = leader.getId();
		if (player.getId().equals(leaderId)) {
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!marchModule.checkMarchReq(player, req, HP.code.WORLD_MASS_JOIN_C_VALUE, armyList, leader, false)) {
			return false;
		}

		IYQZZMassMarch massMarch = (IYQZZMassMarch) worldMarch;
		// 集结行军类型和参与集结行军类型不一致
		int joinMarchType = massMarch.getJoinMassType().getNumber();
		if (marchType != joinMarchType) {
			player.sendError(HP.code.WORLD_MASS_JOIN_C_VALUE, Status.Error.MASS_JOIN_TYPE_ERROR);
			return false;
		}

		// 已经有加入这支部队的集结
		Set<IYQZZWorldMarch> massJoinMarchs = massMarch.getMassJoinMarchs(false);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			if (massJoinMarch.getPlayerId().equals(player.getId())) {
				return false;
			}
		}

		// 集结队伍是否已出发
		if (massMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			return false;
		}

		// 检查是否同盟
		if (!player.isInSameGuild(leader)) {
			return false;
		}

		// 扣兵
		if (!marchModule.checkArmyAndMarch(HP.code.WORLD_MASS_JOIN_S_VALUE, player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_S_VALUE, builder));

		// 出发
		getParent().getParent().startMarch(player, player, leader, req.getType(), massMarchId, 0, new EffectParams(req, armyList));

		return true;
	}

	private boolean onWorldMassStart(IYQZZWorldPoint point) {
		int waitTime = 120;
		IYQZZPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(point.getX()).setPosY(point.getY());
		reqBul.setMassTime(180);
		int maxMarch = player.getMaxMarchSoldierNum(EffectParams.getDefaultVal());
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			if(army.getArmyId() %10 == 7){
				continue;
			}
			int count = Math.min(maxMarch / 2, army.getFree() / 3);
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(count));
			maxMarch -= count;
			if (maxMarch < 100) {
				break;
			}
		}
		WorldMarchReq req = reqBul.build();

		YQZZMarchModule marchModule = parent.getParent().getModule(YQZZConst.ModuleType.YQZZMarch);
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!marchModule.checkMarchReq(player, req, HP.code.WORLD_MASS_C_VALUE, armyList, point, false)) {
			return false;
		}

		String targetId = "";
		WorldMarchType marchType = null;
		switch (point.getPointType()) {
		case PLAYER://
			marchType = WorldMarchType.MASS;
			IYQZZPlayer defPlayer = (IYQZZPlayer) point;
			// 目标id
			targetId = defPlayer.getId();
			break;
		case YQZZ_BUILDING:// 指挥部
			marchType = WorldMarchType.YQZZ_BUILDING_MASS;
			break;
		default:
			return false;
		}

		// 扣兵
		if (!marchModule.checkArmyAndMarch(HP.code.WORLD_MASS_C_VALUE, player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		IYQZZWorldMarch march = getParent().getParent().startMarch(player, player, point, marchType, targetId, waitTime, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_S_VALUE, builder));

		marchModule.sendNotice(point, march,0,"");

		return true;
	}

	private long nextMarch() {
		return parent.getParent().getCurTimeMil() + (int) (Math.random() * 10_000);
	}

	private boolean onYQZZ_SINGLE_MARCHStart(IYQZZWorldPoint targetPoint) {
		IYQZZPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(targetPoint.getX()).setPosY(targetPoint.getY());

		int maxMarch = 500;
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			if(army.getArmyId() > 110000){
				continue;
			}
			int count = Math.min(maxMarch / 2, army.getFree() / 3);
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(count));
			maxMarch -= count;
			if (maxMarch < 100) {
				break;
			}
		}
		YQZZMarchModule marchModule = parent.getParent().getModule(YQZZConst.ModuleType.YQZZMarch);
		WorldMarchReq req = reqBul.build();

		// 扣兵
		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!marchModule.checkMarchReq(player, req, HP.code.WORLD_FIGHTMONSTER_C_VALUE, armyList, targetPoint, false)) {
			return false;
		}
		if (!marchModule.checkArmyAndMarch(HP.code.WORLD_FIGHTMONSTER_C_VALUE, player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		// 开启行军,目标放怪物ID
		YQZZMonster monster = (YQZZMonster) targetPoint;
		String targetId = monster.getCfg().getMonsterId() + "";
		// 野怪配置
		WorldEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monster.getCfg().getMonsterId());
		int vitCost = cfg.getCostPhysicalPower();
		int buff = player.getEffect().getEffVal(EffType.ATK_MONSTER_VIT_ADD, new EffectParams(req, new ArrayList<>()));
		vitCost = (int) (vitCost * (1 + buff * GsConst.EFF_PER));
		// 体力减少
		int buffReduce = player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_VIT_REDUCE);
		vitCost = (int) (vitCost * (1 - buffReduce * GsConst.EFF_PER));
		vitCost = Math.max(vitCost, 1);
		// 体力消耗
//		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, vitCost);
		// 体力不足
//		if (!consumeItems.checkConsume(player, protocol.getType())) {
//			return false;
//		}

		IYQZZWorldMarch worldMatch = getParent().getParent().startMarch(player, player, targetPoint, WorldMarchType.ATTACK_MONSTER, targetId, 0, new EffectParams(req, armyList));
		if (worldMatch == null) {
			return false;
		}
		YQZZAttackMonsterMarch attackMonsterMarch = (YQZZAttackMonsterMarch) worldMatch;
		attackMonsterMarch.setVitBack(vitCost);
		// 扣除体力
//		consumeItems.consumeAndPush(player, Action.FIGHT_MONSTER);
		// 返回
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_FIGHTMONSTER_S_VALUE, builder));
		return true;

	}

	public IYQZZPlayer getParent() {
		return parent;
	}

	public void setParent(IYQZZPlayer parent) {
		this.parent = parent;
	}

	public long getNextMarch() {
		return nextMarch;
	}

	public void setNextMarch(long nextMarch) {
		this.nextMarch = nextMarch;
	}

}
