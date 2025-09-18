package com.hawk.game.module.lianmengyqzz.battleroom.order;

import java.util.List;
import java.util.Objects;

import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderUseReq;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;

public class YQZZOrder2001 extends YQZZOrder {
	int tarX;
	int tarY;

	public YQZZOrder2001(YQZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public int canStartOrder(PBYQZZOrderUseReq req, IYQZZPlayer comdPlayer) {
		IYQZZWorldPoint worldPoint = getParent().getParent().getParent().getWorldPoint(req.getTarX(), req.getTarY()).orElse(null);
		if (worldPoint == null) {
			return Status.SysError.DATA_ERROR_VALUE;
		}
		if (!(worldPoint instanceof IYQZZBuilding)) {
			return Status.SysError.DATA_ERROR_VALUE;
		}

		IYQZZBuilding build = (IYQZZBuilding) worldPoint;
		if (!Objects.equals(build.getGuildId(), comdPlayer.getGuildId())) {
			return Status.SysError.DATA_ERROR_VALUE;
		}

		return super.canStartOrder(req, comdPlayer);
	}

	@Override
	public YQZZOrder startOrder(PBYQZZOrderUseReq req, IYQZZPlayer comdPlayer) {
		super.startOrder(req, comdPlayer);

		tarX = req.getTarX();
		tarY = req.getTarY();
		List<IYQZZWorldMarch> tarMarches = getParent().getParent().getParent().getPointMarches(GameUtil.combineXAndY(req.getTarX(), req.getTarY()),
				WorldMarchStatus.MARCH_STATUS_MARCH);

		for (IYQZZWorldMarch march : tarMarches) {
			if (!Objects.equals(march.getParent().getGuildId(), comdPlayer.getGuildId())) { // 只对发启者本盟行军有效
				continue;
			}
			long reamainTime = march.getMarchEntity().getEndTime() - HawkTime.getMillisecond();
			march.getMarchEntity().setEndTime(HawkTime.getMillisecond() + Math.min(reamainTime, 3000));
			march.updateMarch();
		}
		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_KUAI_JIJIE)
				.addParms(GameUtil.getPresidentOfficerId(comdPlayer.getId()))
				.addParms(comdPlayer.getName())
				.addParms(tarX)
				.addParms(tarY).build();
		getParent().getParent().getParent().addWorldBroadcastMsg(parames);
		return this;
	}

	public int getEffect(EffType effType, EffectParams effParams) {
		if (!inEffect()) {
			return 0;
		}
		if (GameUtil.combineXAndY(tarX, tarY) == effParams.getBattlePoint()) {
			return getConfig().getEffectVal(effType);
		}
		return 0;
	}
}
