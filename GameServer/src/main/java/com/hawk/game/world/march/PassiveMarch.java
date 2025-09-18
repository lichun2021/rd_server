package com.hawk.game.world.march;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;

/**
 * 被动行军
 * @author zhenyu.shang
 * @since 2017年8月25日
 */
public abstract class PassiveMarch extends PlayerMarch {

	public PassiveMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}
	
	@Override
	public void register() {
		super.register();
		//判断被动行军是否是集结加入, 如果是集结加入, targetId是集结行军Id, 需要取出playerId
		//TODO zhenyu.shang 目前行军的targetId存的比较乱，有marchId, playerId, guildbuildId。所以此处后期需要优化，目前暂时不改动
		String targetId = getTargetId();
		if(this.isMassJoinMarch()){
			IWorldMarch massMach = WorldMarchService.getInstance().getMarch(targetId);
			if (massMach != null) {
				targetId = massMach.getPlayerId();
			} else {
				targetId = this.getMarchEntity().getLeaderPlayerId();
			}
		}
		//注册被动行军
		WorldMarchService.getInstance().registerPlayerPassiveMarch(targetId, this);
	}
	
	@Override
	public void remove() {
		super.remove();
		//判断被动行军是否是集结加入, 如果是集结加入, targetId是集结行军Id, 需要取出playerId
		String targetPlayerId = this.getTargetId();
		if (this.isMassJoinMarch()) {
			targetPlayerId = getMarchEntity().getLeaderPlayerId();
		}
		//移除被动行军
		if (!HawkOSOperator.isEmptyString(targetPlayerId)) {
			WorldMarchService.getInstance().removePlayerPassiveMarch(targetPlayerId, this);
		}
	}
	
	public String getTargetId(){
		return getMarchEntity().getTargetId();
	}

	@Override
	public boolean isPassiveMarch() {
		return true;
	}
}
