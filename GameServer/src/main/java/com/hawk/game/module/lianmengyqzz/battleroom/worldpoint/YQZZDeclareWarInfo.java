package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import java.util.Objects;

import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.YQZZDeclareWar;
import com.hawk.game.service.chat.ChatParames;

public class YQZZDeclareWarInfo {
	private final IYQZZBuilding parent;
	private final YQZZDeclareWar decl;
	private boolean hasSend373;

	public YQZZDeclareWarInfo(IYQZZBuilding parent, YQZZDeclareWar decl) {
		this.parent = parent;
		this.decl = decl;
	}

	public void tick() {
		send373Notice();
	}

	private void send373Notice() {
		if (hasSend373 || parent.getParent().getCurTimeMil() < decl.getEndTime()) {
			return;
		}

		hasSend373 = true;
		// 广播通知
		ChatParames parames = ChatParames.newBuilder()
				.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
				.setKey(NoticeCfgId.YQZZ_GUILD_DECLEAR_BUILD_OVER)
				.setGuildId(decl.getGuildId())
				.addParms(parent.getX())
				.addParms(parent.getY())
				.addParms(parent.getBuildTypeCfg().getDeclareTime())
				.build();
		parent.getParent().addWorldBroadcastMsg(parames);
		// 广播通知(建筑内被宣战联盟)
		if (!Objects.equals(parent.getOnwerGuildId(), decl.getGuildId())) {
			ChatParames paramesDef = ChatParames.newBuilder()
					.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
					.setKey(NoticeCfgId.YQZZ_ENEMY_GUILD_DECLEAR_BUILD_OVER)
					.setGuildId(parent.getOnwerGuildId())
					.addParms(decl.getServerId())
					.addParms(decl.getGuildName())
					.addParms(parent.getX())
					.addParms(parent.getY())
					.build();
			parent.getParent().addWorldBroadcastMsg(paramesDef);
		}
	}

	public boolean isHasSend373() {
		return hasSend373;
	}

	public void setHasSend373(boolean hasSend373) {
		this.hasSend373 = hasSend373;
	}

	public IYQZZBuilding getParent() {
		return parent;
	}

	public YQZZDeclareWar getDecl() {
		return decl;
	}

}
