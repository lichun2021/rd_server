package com.hawk.robot.response.superweapon;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperWeapon.AllSuperWeaponInfo;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 *
 * @author zhenyu.shang
 * @since 2018年5月8日
 */
@RobotResponse(code = HP.code.SUPER_WEAPON_INFO_S_VALUE)
public class SuperWeaponInfoResponse extends RobotResponsor{

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		AllSuperWeaponInfo info = protocol.parseProtocol(AllSuperWeaponInfo.getDefaultInstance());
		WorldDataManager.getInstance().setSuperWeaponInfo(info);
	}

}
