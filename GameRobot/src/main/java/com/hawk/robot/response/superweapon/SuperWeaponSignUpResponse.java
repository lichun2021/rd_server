package com.hawk.robot.response.superweapon;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponSignUp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;
import com.hawk.robot.util.WorldUtil;

/**
 *
 * @author zhenyu.shang
 * @since 2018年5月17日
 */
@RobotResponse(code = HP.code.SUPER_WEAPON_WAR_SIGN_UP_S_VALUE)
public class SuperWeaponSignUpResponse extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		SuperWeaponSignUp info = protocol.parseProtocol(SuperWeaponSignUp.getDefaultInstance());
		String guildId = robotEntity.getGuildId();
		if(guildId != null){
			//添加报名信息
			int pointId = WorldUtil.combineXAndY(info.getPosX(), info.getPosY());
			WorldDataManager.getInstance().addSuperWeaponSignUpData(guildId, pointId);
		}
	}

}
