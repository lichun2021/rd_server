package com.hawk.game.module.lianmengtaiboliya.worldpoint.sub;

import java.util.Date;

import org.hawk.tuple.HawkTuple2;

import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYTechnologyLab;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;

public class TBLYTechnologyLabProgressStateColse extends ITBLYTechnologyLabProgressState {
	private long openTime;

	public TBLYTechnologyLabProgressStateColse(TBLYTechnologyLab parent) {
		super(parent);
	}

	@Override
	public void onTick() {
		long now = getParent().getParent().getCurTimeMil();
		if (openTime > now) {
			return;
		}
		
		for (HawkTuple2<Integer, Integer> tup : getParent().getCfg().getOpenTimeList()) {
			long start = tup.first * 60 * 1000 + getParent().getParent().getCreateTime();
			long end = tup.second * 60 * 1000 + getParent().getParent().getCreateTime();
			if (start < now && now < end) { // 开放中
				TBLYTechnologyLabProgressStateOpen progressStateOpen = new TBLYTechnologyLabProgressStateOpen(getParent());
				progressStateOpen.setZhendouJieShu(end);
				progressStateOpen.setKaishiJiaFen(getParent().getParent().getCurTimeMil() + getParent().getCfg().getBattleOpen() * 60000);
				getParent().openCnt++;
				getParent().setProgressState(progressStateOpen);
				
//				1. 发送时机：秘密科技实验室开放占领时，即比赛第15、30、45分钟
//				2. 发送内容：秘密科技实验室（75,171）已开放占领，本次争夺增益为：{0}
//				3. 参数说明：{0}为增益效果。如部队攻击+50%
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_TECH_LAB_OPEN)
						.addParms(getParent().nextBuff()).build();
				getParent().getParent().addWorldBroadcastMsg(parames);
				getParent().getParent().worldPointUpdate(getParent());
				return;
			}
		}

	}

	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}

}
