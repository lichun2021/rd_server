package com.hawk.game.lianmengjunyan.player.npc.actionScript;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.LMJYNpcCfg;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.npc.LMJYNPCPlayer;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchReq;

public class LMJYNPCActionSpy extends ILMJYNPCAction {

	public LMJYNPCActionSpy(LMJYNPCPlayer parent) {
		super(parent);
	}

	@Override
	public boolean doAction() {
		LMJYNpcCfg npcCfg = getParent().getNpcCfg();
		int num = npcCfg.randNum(npcCfg.getSpyTeamNumber());
		Set<ILMJYPlayer> spyEd = new HashSet<>();
		for (int i = 0; i < num; i++) {
			ILMJYPlayer tar = randomEnemy();
			if (Objects.isNull(tar) || spyEd.contains(tar) || getParent().isInSameGuild(tar)) {
				continue;
			}
			WorldMarchReq.Builder builder = WorldMarchReq.newBuilder();
			builder.setPosX(tar.getX());
			builder.setPosY(tar.getY());

			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_SPY_C, builder);
			getParent().sendProtocol(protocol);
			spyEd.add(tar);
		}

		return true;
	}

	@Override
	public void inCD() {
		LMJYNpcCfg cfg = getParent().getNpcCfg();
		long cd = getLastAction() + cfg.randNum(cfg.getSpyCD()) * 1000;
		setCoolDown(cd);

	}

}
