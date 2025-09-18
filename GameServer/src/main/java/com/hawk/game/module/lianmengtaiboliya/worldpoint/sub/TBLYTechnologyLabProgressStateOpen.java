package com.hawk.game.module.lianmengtaiboliya.worldpoint.sub;

import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYTechnologyLabCfg;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYBuildState;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYTechnologyLab;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.TBLY.PBTBLYTechonolgyLabEffect;
import com.hawk.game.service.chat.ChatParames;

public class TBLYTechnologyLabProgressStateOpen extends ITBLYTechnologyLabProgressState {
	private long kaishiJiaFen;
	private long zhendouJieShu; // 争夺结束
	private long lastTick;

	public TBLYTechnologyLabProgressStateOpen(TBLYTechnologyLab parent) {
		super(parent);
	}

	@Override
	public void onTick() {
		long curTimeMil = getParent().getParent().getCurTimeMil();
		long timePass = curTimeMil - lastTick;
		if (timePass < 1000) {
			return;
		}
		lastTick = curTimeMil;

		if (kaishiJiaFen > lastTick) {
			return;
		}

		if (getParent().getState() == TBLYBuildState.ZHAN_LING) {
			if (getParent().getLeaderMarch().getParent().getCamp() == CAMP.A) {
				getParent().campAZhanLingPct++;
			} else {
				getParent().campAZhanLingPct--;
			}
			getParent().getParent().worldPointUpdate(getParent());
		}
		CAMP win = null;
		TBLYTechnologyLabCfg labcfg = getParent().getCfg();
		if (getParent().campAZhanLingPct == labcfg.getTotalPoint()) { // A 赢了
			win = CAMP.A;
		}
		if (getParent().campAZhanLingPct == 0) { // B赢
			win = CAMP.B;
		}
		if (zhendouJieShu < curTimeMil && getParent().campAZhanLingPct == labcfg.getTotalPoint() / 2) { // 占领方赢了
			if (getParent().getLeaderMarch() != null) {
				win = getParent().getLeaderMarch().getParent().getCamp();
			}
		}
		if (zhendouJieShu < curTimeMil && getParent().campAZhanLingPct > labcfg.getTotalPoint() / 2) {// A 赢了
			win = CAMP.A;
		}
		if (zhendouJieShu < curTimeMil && getParent().campAZhanLingPct < labcfg.getTotalPoint() / 2) {// B 赢了
			win = CAMP.B;
		}

		int buffId = 0;
		if (win != null) {
			PBTBLYTechonolgyLabEffect.Builder buf = getParent().getTechnologyLabBuffList().get(getParent().openCnt - 1);
			if (buf.getCamp() == 0) {
				buf.setCamp(win.intValue());
				buffId = buf.getSkillId();
			}
		}

		if (win != null || curTimeMil > zhendouJieShu) {
			long nextOpen = getParent().getNextZhengDouKaishiTime();
			getParent().setProtectedEndTime(nextOpen);
			TBLYTechnologyLabProgressStateColse tblyTechnologyLabProgressStateColse = new TBLYTechnologyLabProgressStateColse(getParent());
			tblyTechnologyLabProgressStateColse.setOpenTime(nextOpen);
			getParent().setProgressState(tblyTechnologyLabProgressStateColse);
			getParent().cleanGuildMarch("");

			if (buffId > 0) {
				// 1. {0}为增益效果。如“部队攻击+50%”
				// 2. {1}为阵营名称。如“兄弟会”
				// 3. {2}为联盟简称及联盟名称。如“[傲天阁]龙傲天下”
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_TECH_LAB_CLOSE)
						.addParms(buffId)
						.addParms(win.intValue())
						.addParms(getParent().getParent().getCampGuildTag(win))
						.addParms(getParent().getParent().getCampGuildName(win))
						.build();
				getParent().getParent().addWorldBroadcastMsg(parames);
			}
			getParent().getParent().worldPointUpdate(getParent());
		}

	}

	public long getKaishiJiaFen() {
		return kaishiJiaFen;
	}

	public void setKaishiJiaFen(long kaishiJiaFen) {
		this.kaishiJiaFen = kaishiJiaFen;
	}

	public long getZhendouJieShu() {
		return zhendouJieShu;
	}

	public void setZhendouJieShu(long zhendouJieShu) {
		this.zhendouJieShu = zhendouJieShu;
	}

	public long getLastTick() {
		return lastTick;
	}

	public void setLastTick(long lastTick) {
		this.lastTick = lastTick;
	}

}
