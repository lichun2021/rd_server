package com.hawk.game.lianmengcyb.player.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.RandomUtils;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tickable.HawkTickable;

import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.lianmengcyb.CYBORGConst;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.module.CYBORGMarchModule;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGMassMarch;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGNian;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchResp;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.world.march.IWorldMarch;

public class CYBORGTestMarchAction extends HawkTickable {
	private ICYBORGPlayer parent;
	private long nextMarch;

	@Override
	public void onTick() {
		if (nextMarch == 0) {
			nextMarch = nextMarch();
			return;
		}
		if (nextMarch > parent.getParent().getCurTimeMil()) {
			return;
		}
		nextMarch = nextMarch();

		List<ICYBORGWorldMarch> mlist = parent.getParent().getPlayerMarches(parent.getId());
		for (ICYBORGWorldMarch march : mlist) {
			march.speedUp(50, 2000);
		}
		if (mlist.size() > 3) {
			return;
		}

		boolean bfalse = Math.random() > 0.7;
		List<ICYBORGWorldPoint> viewPoints = parent.getParent().getViewPoints();
		Collections.shuffle(viewPoints);
		for (ICYBORGWorldPoint point : viewPoints) {
			if (point instanceof ICYBORGBuilding) {
				if(((ICYBORGBuilding) point).underGuildControl(parent.getGuildId())){
					continue;
				}
				if (!((ICYBORGBuilding) point).canBeAttack(parent.getCamp())) {
					continue;
				}
				if (bfalse) {
					onWorldMassStart(point);
				} else {
					bfalse = RandomUtils.nextBoolean();
					if (bfalse) {
						onCYBORG_SINGLE_MARCHStart(point);
					} else {
						onWorldMassJoinStart();
					}
				}
				return;
			}

		}

	}

	private long nextMarch() {
		return parent.getParent().getCurTimeMil() + (int) (Math.random() * 30_000);
	}

	private boolean onWorldMassJoinStart() {
		ICYBORGPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(0).setPosY(0);
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(1));
		}
		List<ICYBORGWorldMarch> guildMarchs = player.getParent().getGuildWarMarch(player.getGuildId());
		guildMarchs = guildMarchs.stream().filter(m -> m instanceof ICYBORGMassMarch).collect(Collectors.toList());
		if(guildMarchs.isEmpty()){
			return true;
		}
		Collections.shuffle(guildMarchs);
		ICYBORGMassMarch toJoin = (ICYBORGMassMarch) guildMarchs.get(0);
		reqBul.setType(toJoin.getJoinMassType());
		reqBul.setMarchId(toJoin.getMarchId());

		WorldMarchReq req = reqBul.build();
		String massMarchId = req.getMarchId();

		CYBORGMarchModule marchModule = parent.getParent().getModule(CYBORGConst.ModuleType.CYBORGMarch);
		ICYBORGWorldMarch worldMarch = marchModule.getParent().getMarch(massMarchId);
		int marchType = req.getType().getNumber();
		// 不能加入自己的集结
		ICYBORGPlayer leader = worldMarch.getParent();
		String leaderId = leader.getId();
		if (player.getId().equals(leaderId)) {
			player.sendError(HP.code.WORLD_MASS_JOIN_C_VALUE, Status.Error.MASS_ERR_CANOT_JOIN_SELF);
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!marchModule.checkMarchReq(player, req, HP.code.WORLD_MASS_JOIN_C_VALUE, armyList, leader, true)) {
			return false;
		}

		ICYBORGMassMarch massMarch = (ICYBORGMassMarch) worldMarch;
		// 集结行军类型和参与集结行军类型不一致
		int joinMarchType = massMarch.getJoinMassType().getNumber();
		if (marchType != joinMarchType) {
			player.sendError(HP.code.WORLD_MASS_JOIN_C_VALUE, Status.Error.MASS_JOIN_TYPE_ERROR);
			return false;
		}

		// 已经有加入这支部队的集结
		Set<ICYBORGWorldMarch> massJoinMarchs = massMarch.getMassJoinMarchs(false);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			if (massJoinMarch.getPlayerId().equals(player.getId())) {
				player.sendError(HP.code.WORLD_MASS_JOIN_C_VALUE, Status.Error.MASS_JOIN_REPEAT);
				return false;
			}
		}

		// 扣兵
		if (!marchModule.checkArmyAndMarch(HP.code.WORLD_MASS_JOIN_C_VALUE, player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_S, builder));
		// 出发
		getParent().getParent().startMarch(player, player, leader, req.getType(), massMarchId, 0, new EffectParams(req, armyList));

		return true;
	}

	private boolean onWorldMassStart(ICYBORGWorldPoint point) {
		ICYBORGPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(point.getX()).setPosY(point.getY());
		reqBul.setMassTime(180);
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(1));
		}
		WorldMarchReq req = reqBul.build();
		int waitTime = req.getMassTime();
		// int marchType = req.getType().getNumber();
		// // 不是集结类型的行军
		// if (!WorldUtil.isMassMarch(marchType)) {
		// player.sendError(protocol.getType(),
		// Status.Error.WORLD_MARCH_REQ_TYPE_ERROR);
		// return false;
		// }

		CYBORGMarchModule marchModule = parent.getParent().getModule(CYBORGConst.ModuleType.CYBORGMarch);
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!marchModule.checkMarchReq(player, req, HP.code.WORLD_MASS_C_VALUE, armyList, point, true)) {
			return false;
		}

		String targetId = "";
		WorldMarchType marchType = null;
		switch (point.getPointType()) {
		case PLAYER://
			marchType = WorldMarchType.MASS;
			ICYBORGPlayer defPlayer = (ICYBORGPlayer) point;
			// 目标id
			targetId = defPlayer.getId();
			break;
		case CYBORG_IRON_CRUTAIN_DIVICE:// 铁幕装置
			marchType = WorldMarchType.CYBORG_IRON_CRUTAIN_DIVICE_MASS;
			break;
		case CYBORG_NUCLEAR_MISSILE_SILO:// 核弹发射井
			marchType = WorldMarchType.CYBORG_NUCLEAR_MISSILE_SILO_MASS;
			break;
		case CYBORG_WEATHER_CONTROLLER:// 天气控制器
			marchType = WorldMarchType.CYBORG_WEATHER_CONTROLLER_MASS;
			break;
		case CYBORG_CHRONO_SPHERE:// 超时空传送器
			marchType = WorldMarchType.CYBORG_CHRONO_SPHERE_MASS;
			break;
		case CYBORG_COMMAND_CENTER:// 指挥部
			marchType = WorldMarchType.CYBORG_COMMAND_CENTER_MASS;
			break;
		case CYBORG_MILITARY_BASE:// 军事基地
			marchType = WorldMarchType.CYBORG_MILITARY_BASE_MASS;
			break;
		case CYBORG_HEADQUARTERS:// 司令部
			marchType = WorldMarchType.CYBORG_HEADQUARTERS_MASS;
			break;

		case NIAN:
			marchType = WorldMarchType.NIAN_MASS;
			targetId = "" + CYBORGNian.NIAN_ID;
			break;
		default:
			return false;
		}

		// 扣兵
		if (!marchModule.checkArmyAndMarch(HP.code.WORLD_MASS_C_VALUE, player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		ICYBORGWorldMarch march = getParent().getParent().startMarch(player, player, point, marchType, targetId, waitTime, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_S_VALUE, builder));

		marchModule.sendNotice(point, march);

		return true;

	}

	private boolean onCYBORG_SINGLE_MARCHStart(ICYBORGWorldPoint point) {
		ICYBORGPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(point.getX()).setPosY(point.getY());
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(1));
		}
		WorldMarchReq req = reqBul.build();

		// 路点为空
		WorldMarchType marchType = null;
		switch (point.getPointType()) {
		case CYBORG_IRON_CRUTAIN_DIVICE:// 铁幕装置
			marchType = WorldMarchType.CYBORG_IRON_CRUTAIN_DIVICE_SINGLE;
			break;
		case CYBORG_NUCLEAR_MISSILE_SILO:// 核弹发射井
			marchType = WorldMarchType.CYBORG_NUCLEAR_MISSILE_SILO_SINGLE;
			break;
		case CYBORG_WEATHER_CONTROLLER:// 天气控制器
			marchType = WorldMarchType.CYBORG_WEATHER_CONTROLLER_SINGLE;
			break;
		case CYBORG_CHRONO_SPHERE:// 超时空传送器
			marchType = WorldMarchType.CYBORG_CHRONO_SPHERE_SINGLE;
			break;
		case CYBORG_COMMAND_CENTER:// 指挥部
			marchType = WorldMarchType.CYBORG_COMMAND_CENTER_SINGLE;
			break;
		case CYBORG_MILITARY_BASE:// 军事基地
			marchType = WorldMarchType.CYBORG_MILITARY_BASE_SINGLE;
			break;
		case CYBORG_HEADQUARTERS:// 司令部
			marchType = WorldMarchType.CYBORG_HEADQUARTERS_SINGLE;
			break;
		case NIAN:
			marchType = WorldMarchType.NIAN_SINGLE;
			break;
		default:
			return false;
		}

		CYBORGMarchModule marchModule = parent.getParent().getModule(CYBORGConst.ModuleType.CYBORGMarch);
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!marchModule.checkMarchReq(player, req, HP.code.WORLD_MASS_DISSOLVE_C_VALUE, armyList, point, true)) {
			return false;
		}

		// 扣兵
		if (!marchModule.checkArmyAndMarch(HP.code.CYBORG_SINGLE_MARCH_VALUE, player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		getParent().getParent().startMarch(player, player, point, marchType, "", 0, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_SINGLE_MARCH_S, builder));
		return true;

	}

	public ICYBORGPlayer getParent() {
		return parent;
	}

	public void setParent(ICYBORGPlayer parent) {
		this.parent = parent;
	}

	public long getNextMarch() {
		return nextMarch;
	}

	public void setNextMarch(long nextMarch) {
		this.nextMarch = nextMarch;
	}


}
