package com.hawk.game.module.lianmengtaiboliya.worldmarch;

import java.util.Collections;

import org.hawk.app.HawkApp;

import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYMonsterCfg;
import com.hawk.game.module.lianmengtaiboliya.entity.TBLYMarchEntity;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYMonster;
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

public class TBLYAttackMonsterMarch extends ITBLYWorldMarch {

	public TBLYAttackMonsterMarch(ITBLYPlayer parent) {
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
		TBLYMarchEntity march = getMarchEntity();
		ITBLYWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof TBLYMonster)) {
			int[] xy = GameUtil.splitXAndY(getMarchEntity().getTerminalId());
			MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(getParent().getId()).setMailId(MailId.TBLY_ATK_MONSTER_MISS)
					.addContents(xy[0], xy[1])
					.addTips(march.getTargetId())
					.setDuntype(DungeonMailType.TBLY);
			MailParames mparames = playerParamesBuilder.build();
			MailService.getInstance().sendMail(mparames);

			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}

		TBLYMonster monster = (TBLYMonster) ttt;
		TBLYMonsterCfg monstercfg = monster.getCfg();
		boolean win = monstercfg.getWinArmyCount() <= march.getArmyCount();
		// 发送战斗结果给前台播放动画
		this.sendBattleResultInfo(win, march.getArmys(), Collections.emptyList(), win);

		// 泰伯利亚野怪击杀邮件 2021112401 四个参数，分别是1 坐标 2击杀个人积分 3击杀号令能量 4 联盟总号令能量
		// 泰伯利亚野怪失败邮件 2021112402 1个参数，坐标
		// 泰伯利亚野怪消失邮件 2021112403 1个参数，坐标
		if (win) {
			// TODO + 分
			getParent().incKillMonster();
			getParent().incrementPlayerHonor(monstercfg.getPlayerHonor());
			getParent().incrementGuildHonor(monstercfg.getGuildHonor());
			if (getParent().getCamp() == CAMP.A) {
				getParent().getParent().campAOrder += monstercfg.getGuildOrder();
			} else {
				getParent().getParent().campBOrder += monstercfg.getGuildOrder();
			}

			int campOrder = getParent().getParent().getCampOrder(getParent().getCamp());
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.TBLY_ATK_MONSTER_SUCCESS)
					.addContents(monster.getX(), monster.getY(), monstercfg.getPlayerHonor(), monstercfg.getGuildOrder(), campOrder)
					.addTips(march.getTargetId()).setDuntype(DungeonMailType.TBLY).build());

			monster.removeWorldPoint();

			if (getParent().getParent().worldMonsterCount() == 0) {
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_MONSTER_KILLALL).build();
				getParent().getParent().addWorldBroadcastMsg(parames);
			}
			LogUtil.logTBLYKillMonster(player, getParent().getParent().getId(), player.getGuildId(), player.getGuildName(), monster.getCfgId(), monstercfg.getGuildOrder());
		} else {
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.TBLY_ATK_MONSTER_FAIL)
					.addContents(monster.getX(), monster.getY()).addTips(march.getTargetId()).setDuntype(DungeonMailType.TBLY).build());
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
