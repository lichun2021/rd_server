package com.hawk.game.nation.space;

import com.hawk.game.GsConfig;
import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 国家航天中心
 * @author zhenyu.shang
 * @since 2022年4月12日
 */
public class NationSpaceFlight extends NationalBuilding {

	private boolean isCheckComplete = false;
	private static final String redisKey = "YQZZ_START_MAIL";

	public NationSpaceFlight(NationConstructionEntity entity, NationbuildingType buildType) {
		super(entity, buildType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void buildingTick(long now) {
		if(!isCheckComplete){
			isCheckComplete = true;
			if(getLevel() >= 1){
				sendYQZZStartEmail();
			}
		}
	}

	@Override
	public void levelupOver() {
		if(getLevel() == 1){
			sendYQZZStartEmail();
		}
	}

	@Override
	public void levelupStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkStateCanBuild() {
		return true;
	}

	private void sendYQZZStartEmail(){
		try {
			String serverId = GsConfig.getInstance().getServerId();
			String key = redisKey  + ":" + serverId;
			String result = RedisProxy.getInstance().getRedisSession().getString(key);
			if (!HawkOSOperator.isEmptyString(result)) {
				return;
			}
			RedisProxy.getInstance().getRedisSession().setString(key, "1");
			StatisManager.getInstance().incRedisKey(redisKey);
			long currTime = HawkTime.getMillisecond();
			long experiTime = currTime + HawkTime.DAY_MILLI_SECONDS * 7;
			SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
					.setMailId(MailConst.MailId.YQZZ_ACTIVITY_START)
					.build(), currTime, currTime + experiTime);
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

}
