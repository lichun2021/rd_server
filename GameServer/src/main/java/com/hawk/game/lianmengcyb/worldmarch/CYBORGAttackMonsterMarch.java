package com.hawk.game.lianmengcyb.worldmarch;

import java.util.Collections;

import com.hawk.game.config.CYBORGMonsterCfg;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.entity.CYBORGMarchEntity;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGMonster;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;

public class CYBORGAttackMonsterMarch extends ICYBORGWorldMarch {

	public CYBORGAttackMonsterMarch(ICYBORGPlayer parent) {
		super(parent);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ATTACK_MONSTER;
	}

	@Override
	public void heartBeats() {
		// 当前时间
		long currTime = getParent().getParent().getCurTimeMil();
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
		CYBORGMarchEntity march = getMarchEntity();
		ICYBORGWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof CYBORGMonster)) {
			int[] xy = GameUtil.splitXAndY(getMarchEntity().getTerminalId());
			MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(getParent().getId()).setMailId(MailId.CYBORG_ATK_MONSTER_MISS)
					.addContents(xy[0], xy[1])
					.addTips(march.getTargetId())
					.setDuntype(DungeonMailType.CYBORG);
			MailParames mparames = playerParamesBuilder.build();
			MailService.getInstance().sendMail(mparames);

			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}

		CYBORGMonster monster = (CYBORGMonster) ttt;
		CYBORGMonsterCfg monstercfg = monster.getCfg();
		boolean win = monstercfg.getWinArmyCount() <= march.getArmyCount();
		// 发送战斗结果给前台播放动画
		this.sendBattleResultInfo(win, march.getArmys(), Collections.emptyList(), win);

		// 泰伯利亚野怪击杀邮件 2021112401 四个参数，分别是1 坐标 2击杀个人积分 3击杀号令能量 4 联盟总号令能量
		// 泰伯利亚野怪失败邮件 2021112402 1个参数，坐标
		// 泰伯利亚野怪消失邮件 2021112403 1个参数，坐标
		if (win) {
			getParent().incrementPlayerHonor(monstercfg.getPlayerHonor());
			getParent().incrementGuildHonor(monstercfg.getGuildHonor());
			getParent().setKillMonster(getParent().getKillMonster() + 1);

			int campOrder = 0;
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.CYBORG_ATK_MONSTER_SUCCESS)
					.addContents(monster.getX(), monster.getY(), monstercfg.getPlayerHonor(), monstercfg.getGuildHonor(), campOrder)
					.addTips(march.getTargetId()).setDuntype(DungeonMailType.CYBORG).build());

			monster.removeWorldPoint();

			if (getParent().getParent().worldMonsterCount() == 0) {
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_MONSTER_KILLALL).build();
				getParent().getParent().addWorldBroadcastMsg(parames);
			}
			LogUtil.logCYBORGKillMonster(player, getParent().getParent().getId(), player.getGuildId(), player.getGuildName(), monster.getCfgId(), monstercfg.getGuildOrder());
		} else {
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.CYBORG_ATK_MONSTER_FAIL) 
					.addContents(monster.getX(), monster.getY()).addTips(march.getTargetId()).setDuntype(DungeonMailType.CYBORG).build());
		}

		// 行军返回
		onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
	}

	@Override
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();

	}
}
