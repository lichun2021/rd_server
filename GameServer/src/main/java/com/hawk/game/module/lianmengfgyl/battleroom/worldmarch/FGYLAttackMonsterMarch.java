package com.hawk.game.module.lianmengfgyl.battleroom.worldmarch;

import java.util.Collections;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLMonsterCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.entity.FGYLMarchEntity;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLMonster;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;

public class FGYLAttackMonsterMarch extends IFGYLWorldMarch {

	public FGYLAttackMonsterMarch(IFGYLPlayer parent) {
		super(parent);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ATTACK_MONSTER;
	}

	@Override
	public void heartBeats() {
		// 当前时间
		long currTime = HawkApp.getInstance().getCurrentTime();
		// 行军或者回程时间未结束
		if (getMarchEntity().getEndTime() > currTime) {
			return;
		}
		// 行军返回到达
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			onMarchBack();
			return;
		}

		// 行军到达
		onMarchReach(getParent());

	}

	@Override
	public void onMarchReach(Player player) {
		FGYLMarchEntity march = getMarchEntity();
		IFGYLWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof FGYLMonster)) {
			int[] xy = GameUtil.splitXAndY(getMarchEntity().getTerminalId());
			MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(getParent().getId()).setMailId(MailId.FGYL_ATK_MONSTER_MISS)
					.addContents(xy[0], xy[1])
					.addTips(march.getTargetId())
					.setDuntype(DungeonMailType.FGYL);
			MailParames mparames = playerParamesBuilder.build();
			MailService.getInstance().sendMail(mparames);

			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}
		FGYLConstCfg ccfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		if (getParent().getKillMonster() >= ccfg.getMonsterAttackMax()) {
			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}

		FGYLMonster monster = (FGYLMonster) ttt;
		FGYLMonsterCfg monstercfg = monster.getCfg();
		boolean win = monstercfg.getWinArmyCount() <= march.getArmyCount();
		// 发送战斗结果给前台播放动画
		this.sendBattleResultInfo(win, march.getArmys(), Collections.emptyList(), win);

		if (win) {
			// TODO + 分
			getParent().incKillMonster();
			getParent().incrementGuildHonor(monstercfg.getGuildHonor());
			getParent().setSkillOrder(getParent().getSkillOrder() + monstercfg.getPlayerHonor());
			getParent().setMonstKill(getParent().getMonstKill() + 1);
			
			int campOrder = getParent().getParent().getCampOrder(getParent().getCamp());
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.FGYL_ATK_MONSTER_SUCCESS)
					.addContents(monster.getX(), monster.getY(), monstercfg.getPlayerHonor(), monstercfg.getGuildOrder(), campOrder)
					.addTips(march.getTargetId()).setDuntype(DungeonMailType.FGYL).build());

			monster.removeWorldPoint();
			getParent().getParent().addMonsterKill(getParent(), 1);
			getParent().getPush().syncFGYLPlayerInfo();
		} else {
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.FGYL_ATK_MONSTER_FAIL)
					.addContents(monster.getX(), monster.getY()).addTips(march.getTargetId()).setDuntype(DungeonMailType.FGYL).build());
		}

		// 行军返回
		onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
	}

	@Override
	public void onMarchBack() {
		// 部队回城
		onArmyBack();

		this.remove();

	}
}
