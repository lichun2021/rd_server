package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.item.WarFlagSignUpItem;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.MonsterMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 母旗发奖行军
 * @author golden
 *
 */
public class CenterFlagMarch extends MonsterMarch implements BasedMarch {

	public CenterFlagMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.CENTER_FLAG_REWARD_MARCH;
	}

	@Override
	public long getMarchNeedTime() {
		// TODO
		return 10 * 1000L;
	}

	/**
	 * 行军到达
	 */
	@Override
	public void onMarchReach(Player player) {
		calc();
	}

	/**
	 * 迁城
	 */
	@Override
	public void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		calc();
	}
	
	/**
	 * 结算
	 */
	private void calc() {
		
		// 删除行军
		WorldMarchService.getInstance().removeMarch(this);

		// 发奖
		List<WarFlagSignUpItem> awardInfo = new ArrayList<>();
		SerializeHelper.stringToList(WarFlagSignUpItem.class, getMarchEntity().getAwardStr(), SerializeHelper.ELEMENT_SPLIT, SerializeHelper.BETWEEN_ITEMS, awardInfo);
		if (awardInfo != null && awardInfo.size() > 0) {
			WarFlagService.getInstance().sendCenterAwardMail(awardInfo.get(0), this.getOrigionId());
		}
	}
}
