package com.hawk.activity.type.impl.guildDragonAttack;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBGuildDragonAttackAppointmentTimeSetReq;
import com.hawk.game.protocol.HP;

public class GuildDragonAttactHandler extends ActivityProtocolHandler {
	
	
	
	@ProtocolHandler(code = HP.code2.ACTIVITY_GUILD_DRAGON_ATTACK_INFO_REQ_VALUE)
	public void dataSync(HawkProtocol hawkProtocol, String playerId) {
		GuildDragonAttackActivity activity = this.getActivity(ActivityType.GUILD_DRAGON_ATTACK);
		if (activity == null) {
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}

	@ProtocolHandler(code = HP.code2.ACTIVITY_GUILD_DRAGON_ATTACK_OPEN_REQ_VALUE)
	public void open(HawkProtocol hawkProtocol, String playerId) {
		GuildDragonAttackActivity activity = this.getActivity(ActivityType.GUILD_DRAGON_ATTACK);
		if (activity == null) {
			return;
		}
		activity.openAttack(playerId,hawkProtocol.getType());
	}

	
	@ProtocolHandler(code = HP.code2.ACTIVITY_GUILD_DRAGON_ATTACK_APPOINT_REQ_VALUE)
	public void appoint(HawkProtocol hawkProtocol, String playerId) {
		GuildDragonAttackActivity activity = this.getActivity(ActivityType.GUILD_DRAGON_ATTACK);
		if (activity == null) {
			return;
		}
		PBGuildDragonAttackAppointmentTimeSetReq req = hawkProtocol.parseProtocol(PBGuildDragonAttackAppointmentTimeSetReq.getDefaultInstance());
		long appointmentTime = req.getAppointmentTime();
		activity.appointmentTimeSet(playerId, appointmentTime,hawkProtocol.getType());
	}
	
	
	@ProtocolHandler(code = HP.code2.ACTIVITY_GUILD_DRAGON_ATTACK_RANK_REQ_VALUE)
	public void damageRankInfo(HawkProtocol hawkProtocol, String playerId) {
		GuildDragonAttackActivity activity = this.getActivity(ActivityType.GUILD_DRAGON_ATTACK);
		if (activity == null) {
			return;
		}
		activity.getDamageRankInfo(playerId);
	}
}
