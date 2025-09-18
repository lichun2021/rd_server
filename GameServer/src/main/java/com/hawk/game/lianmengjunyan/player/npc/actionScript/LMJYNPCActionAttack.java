package com.hawk.game.lianmengjunyan.player.npc.actionScript;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hawk.delay.HawkDelayAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tuple.HawkTuple5;

import com.hawk.game.GsApp;
import com.hawk.game.config.LMJYNpcCfg;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.npc.LMJYNPCPlayer;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchType;

public class LMJYNPCActionAttack extends ILMJYNPCAction {

	public LMJYNPCActionAttack(LMJYNPCPlayer parent) {
		super(parent);
	}

	@Override
	public boolean doAction() {
		LMJYNpcCfg npcCfg = getParent().getNpcCfg();
		boolean danran = npcCfg.randA_B_C_D_E(npcCfg.getAtkToConcentrate()).first;
		if (danran) {
			// startMassMarch();
			startMarch();
		} else {
			startMassMarch();
		}

		return true;
	}

	private void startMassMarch() {
		LMJYNpcCfg npcCfg = getParent().getNpcCfg();
		ILMJYPlayer tar = findEnemy(npcCfg);
		if (Objects.isNull(tar)) {
			return;
		}
		WorldMarchReq.Builder req = WorldMarchReq.newBuilder();
		req.setPosX(tar.getX());
		req.setPosY(tar.getY());
		req.setMassTime(npcCfg.getMassMarchWait());
		req.setType(WorldMarchType.MASS);
		req.addAllHeroId(getParent().goMarchHeros());
		req.setSuperSoldierId(getParent().goMarchSuperSoldier());
		req.setArmourSuit(ArmourSuitType.ONE);

		req.addAllArmyInfo(getParent().goMarchArmys(npcCfg.getAtkArmyNumber()));
		if (req.getArmyInfoCount() == 0) {
			return;
		}

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MASS_C_VALUE, req);
		getParent().sendProtocol(protocol);
	}

	@Override
	public void inCD() {
		LMJYNpcCfg cfg = getParent().getNpcCfg();
		long cd = getLastAction() + cfg.randNum(cfg.getAtkCD()) * 1000;
		setCoolDown(cd);
	}

	public boolean startMarch() {
		LMJYNpcCfg npcCfg = getParent().getNpcCfg();
		int num = npcCfg.randNum(npcCfg.getAtkTeamNumber());
		Set<ILMJYPlayer> tarSet = new HashSet<>();
		for (int i = 0; i < num; i++) {
			ILMJYPlayer tar = findEnemy(npcCfg);
			if (Objects.nonNull(tar)) {
				if (tarSet.contains(tar)) {
					final ILMJYPlayer to = tar;
					GsApp.getInstance().addDelayAction(15000, new HawkDelayAction() {
						@Override
						protected void doAction() {
							sendMarch(to);
						}
					});
				} else {
					sendMarch(tar);
					tarSet.add(tar);
				}
			}

		}

		return true;

	}

	private ILMJYPlayer findEnemy(LMJYNpcCfg npcCfg) {
		HawkTuple5<Boolean, Boolean, Boolean, Boolean, Boolean> atkMarchPrefTuple = npcCfg.randA_B_C_D_E(npcCfg.getAtkPrefer());
		ILMJYPlayer tar = null;
		if (atkMarchPrefTuple.first) { // 最强
			tar = getParent().getParent().getPlayerList(PState.GAMEING).stream()
					.filter(p -> !p.isInSameGuild(getParent()))
					.sorted(Comparator.comparingLong(ILMJYPlayer::getPower).reversed())
					.findFirst()
					.orElse(null);
		} else if (atkMarchPrefTuple.second) { // 最弱
			tar = getParent().getParent().getPlayerList(PState.GAMEING).stream()
					.filter(p -> !p.isInSameGuild(getParent()))
					.sorted(Comparator.comparingLong(ILMJYPlayer::getPower))
					.findFirst()
					.orElse(null);
		} else {
			tar = randomEnemy();
		}
		return tar;
	}

	private void sendMarch(ILMJYPlayer tar) {
		LMJYNpcCfg npcCfg = getParent().getNpcCfg();
		WorldMarchReq.Builder req = WorldMarchReq.newBuilder();
		req.setPosX(tar.getX());
		req.setPosY(tar.getY());

		req.addAllHeroId(getParent().goMarchHeros());
		req.setSuperSoldierId(getParent().goMarchSuperSoldier());
		req.setArmourSuit(ArmourSuitType.ONE);

		req.addAllArmyInfo(getParent().goMarchArmys(npcCfg.getAtkArmyNumber()));
		if (req.getArmyInfoCount() == 0) {
			return;
		}

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_ATTACK_PLAYER_C, req);
		getParent().sendProtocol(protocol);
	}

	@Override
	public long getCoolDown() {
		return Math.max(super.getCoolDown(), getParent().getNextMarch());
	}
	
}
