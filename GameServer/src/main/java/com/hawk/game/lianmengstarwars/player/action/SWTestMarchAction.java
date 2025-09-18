package com.hawk.game.lianmengstarwars.player.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.RandomUtils;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tickable.HawkTickable;

import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.SWConst;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.player.module.SWMarchModule;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.lianmengstarwars.worldmarch.submarch.ISWMassMarch;
import com.hawk.game.lianmengstarwars.worldpoint.ISWBuilding;
import com.hawk.game.lianmengstarwars.worldpoint.SWHeadQuarters;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchResp;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.world.march.IWorldMarch;

public class SWTestMarchAction extends HawkTickable {
	private ISWPlayer parent;
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
		int x = parent.getX();
		int y =parent.getY();
		int disSquar = (x-180)*(x-180) + (y -360 )*(y -360 );
//		if(disSquar>5500){
//			System.out.println(disSquar +  "  X  "+ x +"   Y "+ y);
//			int[] xy = getParent().getParent().getWorldPointService().randomFreePoint(new int[] {180,360}, 2);
//			if(xy != null){
//				// 迁城成功
//				getParent().getParent().doMoveCitySuccess(parent, xy);
//				parent.sendProtocol(HawkProtocol.valueOf(HP.code.MOVE_CITY_NOTIFY_PUSH));
//			}else{
//				System.out.println("随机空点 ");
//			}
//		}
		
		nextMarch = nextMarch();
		if(parent.getMaxCityDef() == 0){
			parent.setMaxCityDef(100000);
		}
		
		// List<ISWBuilding> buildings = parent.getParent().getSWBuildingList();
		// ISWBuilding build = HawkRand.randomObject(buildings);
		// onSW_SINGLE_MARCHStart(build);
		List<ISWWorldMarch> mlist = parent.getParent().getPlayerMarches(parent.getId());
		for (ISWWorldMarch march : mlist) {
			march.speedUp(20, 2000);
		}
		if (mlist.size() > 5 || parent.getParent().getWorldMarchCount() > 200) {
			return;
		}
//		System.out.println(parent.getName()+" march cnt "+ mlist.size() );
		
		boolean bfalse = Math.random() > 0.9;
		List<SWHeadQuarters> viewPoints = parent.getParent().getSWBuildingByClass(SWHeadQuarters.class);// parent.getParent().getSWBuildingList();
		Collections.shuffle(viewPoints);
		for (ISWWorldPoint point : viewPoints) {
			if (point instanceof ISWBuilding) {
				if (bfalse) {
					onWorldMassStart(point);
				} else {
					bfalse = Math.random() > 0.4;
					if(bfalse){
						onWorldSpyStart(point);
						return;
					}
					
					bfalse = RandomUtils.nextBoolean();
					if (bfalse) {
						onSW_SINGLE_MARCHStart(point);
						return;
					} 
					
					onWorldMassJoinStart();
				}
				return;
			}

		}

	}
	private boolean onWorldSpyStart(ISWWorldPoint point) {
		ISWPlayer player = parent;

		getParent().getParent().startMarch(player, player, point, WorldMarchType.SPY, "", 0, new EffectParams());

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SPY_S_VALUE, builder));
		return true;
	}
	
	private boolean onWorldMassJoinStart() {
		ISWPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(0).setPosY(0);
		int maxMarch = player.getMaxMarchSoldierNum(EffectParams.getDefaultVal());
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			if(army.getArmyId()> 110000){
				continue;
			}
			int count = Math.min(maxMarch/2, army.getFree() / 3);
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(count));
			maxMarch -= count;
			if(maxMarch< 100){
				break;
			}
		}
		List<ISWWorldMarch> guildMarchs = player.getParent().getGuildWarMarch(player.getGuildId());
		guildMarchs = guildMarchs.stream().filter(m -> m instanceof ISWMassMarch).collect(Collectors.toList());
		if(guildMarchs.isEmpty()){
			return true;
		}
		Collections.shuffle(guildMarchs);
		ISWMassMarch toJoin = (ISWMassMarch) guildMarchs.get(0);
		reqBul.setType(toJoin.getJoinMassType());
		reqBul.setMarchId(toJoin.getMarchId());

		WorldMarchReq req = reqBul.build();
		String massMarchId = req.getMarchId();

		SWMarchModule marchModule = getParent().getModule(SWConst.ModuleType.SWMarch);
		ISWWorldMarch worldMarch = player.getParent().getMarch(massMarchId);
		int marchType = req.getType().getNumber();
		// 不能加入自己的集结
		ISWPlayer leader = worldMarch.getParent();
		String leaderId = leader.getId();
		if (player.getId().equals(leaderId)) {
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!marchModule.checkMarchReq(player, req, HP.code.WORLD_MASS_JOIN_C_VALUE, armyList, leader, false)) {
			return false;
		}

		ISWMassMarch massMarch = (ISWMassMarch) worldMarch;
		// 集结行军类型和参与集结行军类型不一致
		int joinMarchType = massMarch.getJoinMassType().getNumber();
		if (marchType != joinMarchType) {
			player.sendError(HP.code.WORLD_MASS_JOIN_C_VALUE, Status.Error.MASS_JOIN_TYPE_ERROR);
			return false;
		}

		// 已经有加入这支部队的集结
		Set<ISWWorldMarch> massJoinMarchs = massMarch.getMassJoinMarchs(false);
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

	private boolean onWorldMassStart(ISWWorldPoint point) {
		int waitTime = 120;
		ISWPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(point.getX()).setPosY(point.getY());
		reqBul.setMassTime(180);
		int maxMarch =  player.getMaxMarchSoldierNum(EffectParams.getDefaultVal());
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			int count = Math.min(maxMarch/2, army.getFree() / 3);
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(count));
			maxMarch -= count;
			if(maxMarch< 100){
				break;
			}
		}
		WorldMarchReq req = reqBul.build();

		SWMarchModule marchModule = parent.getParent().getModule(SWConst.ModuleType.SWMarch);
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
			ISWPlayer defPlayer = (ISWPlayer) point;
			// 目标id
			targetId = defPlayer.getId();
			break;
		case SW_COMMAND_CENTER:// 指挥部
			marchType = WorldMarchType.SW_COMMAND_CENTER_MASS;
			break;
		case SW_HEADQUARTERS:// 司令部
			marchType = WorldMarchType.SW_HEADQUARTERS_MASS;
			break;
		default:
			return false;
		}

		// 扣兵
		if (!marchModule.checkArmyAndMarch(HP.code.WORLD_MASS_C_VALUE, player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		ISWWorldMarch march = getParent().getParent().startMarch(player, player, point, marchType, targetId, waitTime, new EffectParams(req, armyList));

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

	private boolean onSW_SINGLE_MARCHStart(ISWWorldPoint point) {
		ISWPlayer player = parent;
		WorldMarchReq.Builder reqBul = WorldMarchReq.newBuilder().setPosX(point.getX()).setPosY(point.getY());
		
		int maxMarch =  player.getMaxMarchSoldierNum(EffectParams.getDefaultVal());
		for (ArmyEntity army : player.getData().getMarchArmy()) {
			int count = Math.min(maxMarch/2, army.getFree() / 3);
			reqBul.addArmyInfo(ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(count));
			maxMarch -= count;
			if(maxMarch< 100){
				break;
			}
		}
		WorldMarchReq req = reqBul.build();

		// 路点为空
		WorldMarchType marchType = null;
		switch (point.getPointType()) {
		case SW_COMMAND_CENTER:// 指挥部
			marchType = WorldMarchType.SW_COMMAND_CENTER_SINGLE;
			break;
		case SW_HEADQUARTERS:// 司令部
			marchType = WorldMarchType.SW_HEADQUARTERS_SINGLE;
			break;
		default:
			return false;
		}

		SWMarchModule marchModule = parent.getParent().getModule(SWConst.ModuleType.SWMarch);
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!marchModule.checkMarchReq(player, req, HP.code.WORLD_MASS_DISSOLVE_C_VALUE, armyList, point, true)) {
			return false;
		}

		// 扣兵
		if (!marchModule.checkArmyAndMarch(HP.code.SW_SINGLE_MARCH_VALUE, player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		getParent().getParent().startMarch(player, player, point, marchType, "", 0, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_SINGLE_MARCH_S, builder));
		return true;

	}

	public ISWPlayer getParent() {
		return parent;
	}

	public void setParent(ISWPlayer parent) {
		this.parent = parent;
	}

	public long getNextMarch() {
		return nextMarch;
	}

	public void setNextMarch(long nextMarch) {
		this.nextMarch = nextMarch;
	}

}
