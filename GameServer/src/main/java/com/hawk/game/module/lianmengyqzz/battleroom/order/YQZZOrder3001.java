package com.hawk.game.module.lianmengyqzz.battleroom.order;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZMonsterCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZMonster;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderUseReq;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.GameUtil;

public class YQZZOrder3001 extends YQZZOrder {
	int monsterCount;
	YQZZMonsterCfg monstercfg;

	public YQZZOrder3001(YQZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public YQZZOrder startOrder(PBYQZZOrderUseReq req, IYQZZPlayer comdPlayer) {
		super.startOrder(req, comdPlayer);
		monsterCount = getConfig().getP2();
		monstercfg = HawkConfigManager.getInstance().getConfigByKey(YQZZMonsterCfg.class, getConfig().getP1());
		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_YAN_ZHEN)
				.addParms(GameUtil.getPresidentOfficerId(comdPlayer.getId()))
				.addParms(comdPlayer.getName())
				.addParms(getParent().getParent().getX())
				.addParms(getParent().getParent().getY()).build();
		getParent().getParent().getParent().addWorldBroadcastMsg(parames);
		return this;
	}

	@Override
	public void onTick() {
		super.onTick();
		if (monsterCount > 0 && Objects.nonNull(monstercfg)) {
			monsterCount--;
			try {
				YQZZMonster monster = YQZZMonster.create(getParent().getParent().getParent(), monstercfg);
				int[] xy = popWorldPoint(monster.getGridCnt());
				if (xy != null) {
					monster.setX(xy[0]);
					monster.setY(xy[1]);
					getParent().getParent().getParent().getWorldPointService().addViewPoint(monster);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

	}

	public int[] popWorldPoint(int gridCnt) {
		IYQZZBuilding build = getParent().getParent().getParent().getWorldPointService().getBaseByCamp(getParent().getCamp());
		return getParent().getParent().getParent().getWorldPointService().randomSubareaPoint(build.getSubarea(), gridCnt);
	}

}
