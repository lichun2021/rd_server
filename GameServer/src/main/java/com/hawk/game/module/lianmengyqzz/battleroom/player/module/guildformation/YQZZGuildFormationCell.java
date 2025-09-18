package com.hawk.game.module.lianmengyqzz.battleroom.player.module.guildformation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.world.march.IWorldMarch;

public class YQZZGuildFormationCell extends GuildFormationCell {
	private YQZZGuildFormationObj parent;

	public void copyFrom(GuildFormationCell cell){
		if(Objects.nonNull(cell)){
			this.marchIds = cell.getMarchIds();
		}
	}
	
	@Override
	public Set<String> getNoticeJoinMarchIds(String playerId) {
		Set<String> ret = new HashSet<>();
		for (String marchId : marchIds) {
			IWorldMarch march = parent.getParent().getMarch(marchId);
			if (march == null) {
				continue;
			}
			if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				continue;
			}
			ret.add(marchId);
		}
		return ret;
	}
	
	/**
	 * 检测行军id移除
	 */
	@Override
	public void checkMarchIdRemove() {
		List<String> rmList = new ArrayList<>();
		for (String marchId : marchIds) {
			IWorldMarch march = getParent().getParent().getMarch(marchId);
			if (march == null) {
				rmList.add(marchId);
				continue;
			}
		}
		marchIds.removeAll(rmList);
	}

	public YQZZGuildFormationObj getParent() {
		return parent;
	}

	public void setParent(YQZZGuildFormationObj parent) {
		this.parent = parent;
	}

}
