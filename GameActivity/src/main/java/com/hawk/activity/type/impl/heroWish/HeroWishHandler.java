package com.hawk.activity.type.impl.heroWish;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBHeroWishChooseReq;
import com.hawk.game.protocol.HP;

/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class HeroWishHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.HERO_WISH_CHOOSE_REQ_VALUE)
	public void chooseHero(HawkProtocol hawkProtocol, String playerId){
		HeroWishActivity activity = this.getActivity(ActivityType.HERO_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		PBHeroWishChooseReq req = hawkProtocol.parseProtocol(PBHeroWishChooseReq.getDefaultInstance());
		activity.chooseHero(playerId,req.getChooseId());
	}
	
	
	@ProtocolHandler(code = HP.code2.HERO_WISH_ADD_REQ_VALUE)
	public void heroWish(HawkProtocol hawkProtocol, String playerId){
		HeroWishActivity activity = this.getActivity(ActivityType.HERO_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.wishHero(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code2.HERO_WISH_AHIEVE_REQ_VALUE)
	public void heroAchieve(HawkProtocol hawkProtocol, String playerId){
		HeroWishActivity activity = this.getActivity(ActivityType.HERO_WISH_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.achieveHero(playerId);
	}
	
	
}