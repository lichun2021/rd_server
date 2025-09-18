package com.hawk.game.lianmengstarwars;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SW.PBSWCmd;
import com.hawk.game.protocol.SW.PBSWVideoPackage;

public class SWVideoTest extends HawkPeriodTickable {

	public SWVideoTest(long tickPeriod, int startIndex) {
		super(tickPeriod);
		this.startIndex = startIndex;
		this.packindex = startIndex;
	}

	final int startIndex;
	int packindex;
	PBSWVideoPackage videopack;
	boolean over;
	int cmdIndex;
	long timeCha;
	public Player player; // 观众

	@Override
	public void onPeriodTick() {
		if (over) {
			return;
		}
		long now = HawkTime.getMillisecond();
		if (videopack == null) {
			videopack = SWRoomManager.getInstance().videoPackageOfIndex("tguyhnjko6", packindex).get();
			if (packindex == startIndex) {
				for (PBSWCmd cmd : videopack.getHeadList()) {
					player.sendProtocol(HawkProtocol.valueOf(cmd.getCmd(), cmd.getCmdBytes().toByteArray()));
					timeCha = now - cmd.getTimestemp();
				}
			}

			packindex++;
		}

		for (int i = 0; i < videopack.getCmdListCount(); i++) {
			if (i < cmdIndex) {
				continue;
			}
			PBSWCmd cmd = videopack.getCmdList(i);
			if (now - timeCha > cmd.getTimestemp()) {
				System.out.println(HP.code.valueOf(cmd.getCmd()) + "   view send");
				player.sendProtocol(HawkProtocol.valueOf(cmd.getCmd(), cmd.getCmdBytes().toByteArray()));
				cmdIndex++;
			}

		}
		if (cmdIndex == videopack.getCmdListCount()) { // 本包播完了
			if (videopack.getIslast()) {
				over = true;
			}
			videopack = null;
			cmdIndex = 0;
		}

	}

	public int getStartIndex() {
		return startIndex;
	}
}
