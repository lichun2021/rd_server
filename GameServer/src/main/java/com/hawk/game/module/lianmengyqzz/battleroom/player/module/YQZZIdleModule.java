package com.hawk.game.module.lianmengyqzz.battleroom.player.module;

import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitReason;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.MailParames;

public class YQZZIdleModule extends PlayerModule {
	private IYQZZPlayer player;
	private long idelStart;
	private long lastCheckIdel;

	public YQZZIdleModule(IYQZZPlayer player) {
		super(player);
		this.player = player;
		idelStart = GsApp.getInstance().getCurrentTime() + TimeUnit.MINUTES.toMillis(10);
	}

	@Override
	public boolean onTick() {
		checkIdel();
		return super.onTick();
	}

	private void checkIdel() {
		long curTimeMil = player.getParent().getCurTimeMil();
		if (curTimeMil - lastCheckIdel < 60000) {
			return;
		}
		lastCheckIdel = curTimeMil;

		long time1 = HawkTime.getMillisecond();
		if (player.isActiveOnline()) {
			idelStart = Math.max(idelStart, curTimeMil);
			GlobalData.getInstance().makesurePlayer(player.getId());
			return;
		}
		long time2 = HawkTime.getMillisecond();
		if (curTimeMil - idelStart < player.getParent().getCfg().getIdelKick() * 1000) {
			return;
		}
		if (!player.getParent().getPlayerMarches(player.getId()).isEmpty()) {
			return;
		}
		long time3 = HawkTime.getMillisecond();
		player.getParent().quitWorld(player, YQZZQuitReason.IDEL);

		long time4 = HawkTime.getMillisecond();
		MailParames.Builder builder = MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.YQZZ_AUTO_KICK_OUT);
		MailService.getInstance().sendMail(builder.build());
		long time5 = HawkTime.getMillisecond();
		if (time5 - time1 > 1000) {
			DungeonRedisLog.log(player.getParent().getId(), "{} YQZZIdleModule {} tick too much time, t1:{} t2:{} t3:{} t4:{} t5:{}", player.getId(), time2 -time1, time3 -time2, time4-time3, time5- time4, time5 );
		}
	}

}
