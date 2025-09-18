package com.hawk.game.module.lianmengXianquhx.player.module.guildformation;

import java.util.HashSet;
import java.util.Set;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MassFormation.FormationJoinNotice;
import com.hawk.game.protocol.MassFormation.FormationJoinNoticePush;
import com.hawk.game.protocol.MassFormation.RemoveFormationNotice;
import com.hawk.game.protocol.World.WorldMarchStatus;

/**
 * 联盟编队
 * 
 * @author Golden
 *
 */
public class XQHXGuildFormationModule extends PlayerModule {
	private IXQHXPlayer player;
	/**
	 * 上次推送时间
	 */
	private long lastPushTime = 0;
	/**
	 * 取消通知的行军ID
	 */
	private Set<String> delNoticeSet = new HashSet<>(); 
	
	public XQHXGuildFormationModule(IXQHXPlayer player) {
		super(player);
		this.player = player;
	}

	@Override
	public boolean onTick() {
		checkJoinPush();
		
		return true;
	}
	
	@Override
	protected boolean onPlayerLogin() {
		return true;
	}
	
	/**
	 * 检测集结加入推送
	 */
	private void checkJoinPush() {
		long currentTime = player.getParent().getCurTimeMil();
		if (currentTime - lastPushTime < 2000L) {
			return;
		}
		lastPushTime = currentTime;
		
		
		// 需要自己加入的集结&自己还没有派遣队列加入
		FormationJoinNoticePush.Builder push = FormationJoinNoticePush.newBuilder();
		if (player.hasGuild()) {
			GuildFormationObj formationObj = getGuildFormation();
			for (GuildFormationCell formation : formationObj.getFormations()) {
				// 不需要出征
				if (!formation.fight(player.getId())) {
					continue;
				}
				Set<String> noticeMarchIds = formation.getNoticeJoinMarchIds(player.getId());
				for (String marchId : noticeMarchIds) {
					if (delNoticeSet.contains(marchId)) {
						continue;
					}
					FormationJoinNotice.Builder notice = FormationJoinNotice.newBuilder();
					notice.setIndex(formation.getIndex());
					notice.setMarchId(marchId);
					notice.setIsLeader(formation.isLeader(player.getId()));
					notice.setName(formation.getName());
					long marchStartTime = player.getParent().getMarch(marchId).getMarchEntity().getStartTime();
					notice.setMassStartTime(marchStartTime);
					WorldMarchStatus status = player.getParent().getMarch(marchId).getMassJoinMarchStatus(player.getId());
					if (status != null) {
						notice.setStatus(status);
					}
					push.addInfo(notice);
				}
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MASS_FORMATION_JOIN_NOTICE, push));
	}

	/**
	 * 跨服处理,makeSure一下
	 * @return
	 */
	private GuildFormationObj getGuildFormation() {
		return player.getParent().getGuildFormation(player.getGuildId());
	}
	

	/**
	 * 取消待集结提醒
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MASS_FORMATION_REMOVE_NOTICE_VALUE)
	private boolean delNotice(HawkProtocol protocol) {
		RemoveFormationNotice oper = protocol.parseProtocol(RemoveFormationNotice.getDefaultInstance());
		delNoticeSet.add(oper.getMarchId());
		return true;
	}
	
	/**
	 * 是否忽略了提醒
	 * @param marchId
	 * @return
	 */
	public boolean delNotice(String marchId) {
		return delNoticeSet.contains(marchId);
	}
}
