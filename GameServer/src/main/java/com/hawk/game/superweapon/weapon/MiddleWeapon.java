package com.hawk.game.superweapon.weapon;

import com.hawk.game.util.GsConst.SuperWeaponLevel;

/**
 * 中级武器
 * @author zhenyu.shang
 * @since 2018年4月23日
 */
public abstract class MiddleWeapon extends AbstractSuperWeapon{
	
	public MiddleWeapon(int pointId) {
		super(pointId);
	}
	
	@Override
	public int getWeaponLevel() {
		return SuperWeaponLevel.MIDDLE;
	}

}
