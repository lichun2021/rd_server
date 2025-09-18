package com.hawk.activity.type.impl.alliesWishing;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBAllianceWishTipsReq;
import com.hawk.game.protocol.Activity.PBPBAllianceWishExchangeReq;
import com.hawk.game.protocol.Activity.PBPBAllianceWishHelpReq;
import com.hawk.game.protocol.HP;

/**
 * 盟军祝福
 * 
 * @author che
 *
 */
public class AllianceWishHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_SIGN_REQ_VALUE)
	public void allianceWishSign(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.playerSign(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_SUPPLY_SIGN_REQ_VALUE)
	public void allianceWishSupplySign(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.playerSupplySign(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_DO_WISH_REQ_VALUE)
	public void allianceWishDoWish(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.playerWish(playerId);
	}
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_DO_LUXURY_WISH_REQ_VALUE)
	public void allianceWishDoLuxuryWish(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.playerWishLuxury(playerId);
	}
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_SEND_GUILD_HELP_REQ_VALUE)
	public void allianceSendGuild(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.sendGuildHelp(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_HELP_GUILD_MEMBER_REQ_VALUE)
	public void allianceGuildMemberHelp(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		PBPBAllianceWishHelpReq req = hawkProtocol.parseProtocol(PBPBAllianceWishHelpReq.getDefaultInstance());
		activity.helpWish(playerId, req.getPlayerId());
	}
	
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_ACHIEVE_REQ_VALUE)
	public void allianceWishAchieve(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.playerWishAchive(playerId);
	}
	
	
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_ITEM_EXCHANGE_REQ_VALUE)
	public void allianceGuildExchange(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		PBPBAllianceWishExchangeReq req = hawkProtocol.parseProtocol(PBPBAllianceWishExchangeReq.getDefaultInstance());
		activity.itemExchange(playerId, req.getId(), req.getNum(), hawkProtocol.getType());
	}
	
	
	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_EXCHANGE_REFRESH_REQ_VALUE)
	public void allianceGuildExchangeRefresh(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.resetItemChange(playerId, hawkProtocol.getType());
	}
	
	

	@ProtocolHandler(code = HP.code2.ALLIANCE_WISH_CARE_TIPS_REQ_VALUE)
	public void updateCare(HawkProtocol hawkProtocol, String playerId){
		AllianceWishActivity activity = this.getActivity(ActivityType.ALLIANCE_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		PBAllianceWishTipsReq req = hawkProtocol.parseProtocol(PBAllianceWishTipsReq.getDefaultInstance());
		activity.updateActivityTips(playerId, req.getTipsList());
	}
	
	
}