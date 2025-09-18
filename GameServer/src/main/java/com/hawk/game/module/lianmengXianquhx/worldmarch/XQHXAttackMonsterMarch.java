package com.hawk.game.module.lianmengXianquhx.worldmarch;

import java.util.Collections;

import org.hawk.app.HawkApp;

import com.hawk.game.module.lianmengXianquhx.IXQHXWorldPoint;
import com.hawk.game.module.lianmengXianquhx.XQHXGuildBaseInfo;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXMonsterCfg;
import com.hawk.game.module.lianmengXianquhx.entity.XQHXMarchEntity;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXMonster;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

public class XQHXAttackMonsterMarch extends IXQHXWorldMarch {

	public XQHXAttackMonsterMarch(IXQHXPlayer parent) {
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
	public double getPartMarchTime(double distance, double speed, boolean isSlowDownPart) {
		speed = speed * (1+ getParent().getEffect().getEffVal(EffType.XQHX_10071) * GsConst.EFF_PER);
		double time = super.getPartMarchTime(distance, speed, isSlowDownPart);
		time = (long) (time * (1 - getParent().getEffect().getEffVal(EffType.XQHX_10072) * GsConst.EFF_PER));
		return time;
	}

	@Override
	public void onMarchReach(Player player) {
		XQHXMarchEntity march = getMarchEntity();
		IXQHXWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof XQHXMonster)) {
			int[] xy = GameUtil.splitXAndY(getMarchEntity().getTerminalId());
			MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(getParent().getId()).setMailId(MailId.XQHX_ATK_MONSTER_MISS)
					.addContents(xy[0], xy[1])
					.addTips(march.getTargetId())
					.setDuntype(DungeonMailType.XQHX);
			MailParames mparames = playerParamesBuilder.build();
			MailService.getInstance().sendMail(mparames);

			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}

		XQHXMonster monster = (XQHXMonster) ttt;
		XQHXMonsterCfg monstercfg = monster.getCfg();
		boolean win = monstercfg.getWinArmyCount() <= march.getArmyCount();
		// 发送战斗结果给前台播放动画
		this.sendBattleResultInfo(win, march.getArmys(), Collections.emptyList(), win);

		// 泰伯利亚野怪击杀邮件 2021112401 四个参数，分别是1 坐标 2击杀个人积分 3击杀号令能量 4 联盟总号令能量
		// 泰伯利亚野怪失败邮件 2021112402 1个参数，坐标
		// 泰伯利亚野怪消失邮件 2021112403 1个参数，坐标
		if (win) {
			// TODO + 分
			getParent().incKillMonster();
			getParent().incrementMonsterHonor(monstercfg.getPlayerScore() + getParent().getEffect().getEffVal(EffType.XQHX_10074));
			
			
			XQHXGuildBaseInfo campBase = getParent().getParent().getCampBase(getParent().getCamp());
			campBase.monsterHonor += monstercfg.getAllianceScore()+getParent().getEffect().getEffVal(EffType.XQHX_10073);
			campBase.campOrder += monstercfg.getAllianceOrder() + getParent().getEffect().getEffVal(EffType.XQHX_10075);
			
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.XQHX_ATK_MONSTER_SUCCESS)
					.addContents(monster.getX(), monster.getY(), monstercfg.getPlayerScore(), monstercfg.getAllianceOrder(),monstercfg.getAllianceScore())
					.addTips(march.getTargetId()).setDuntype(DungeonMailType.XQHX).build());

			monster.removeWorldPoint();

			if (getParent().getParent().worldMonsterCount() == 0) {
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.XQHX_MONSTER_KILLALL).build();
				getParent().getParent().addWorldBroadcastMsg(parames);
			}
//			LogUtil.logXQHXKillMonster(player, getParent().getParent().getId(), player.getGuildId(), player.getGuildName(), monster.getCfgId(), monstercfg.getGuildOrder());
		} else {
			MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.XQHX_ATK_MONSTER_FAIL)
					.addContents(monster.getX(), monster.getY()).addTips(march.getTargetId()).setDuntype(DungeonMailType.XQHX).build());
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
