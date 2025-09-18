package com.hawk.game.module.staffofficer;

/**
 * 4. 参谋技的生效条件有以下4个类型：
  1. 无特殊生效条件，非战斗中生效
  2. 无特殊生效条件，战斗中生效，集结战斗中自己生效自己的参谋技
  3. 战斗中，我方成员军备参谋部参谋值总和>=对方成员军备参谋部参谋值总和时生效，集结战斗中自己生效自己的参谋技
    1. 当战斗人数大于15人时，取最高的15人参谋值总和。
  4. 战斗中，我方成员军备参谋部参谋值总和>=对方成员军备参谋部参谋值总和时生效，集结战斗中仅队长的该参谋技生效
    1. 当战斗人数大于15人时，取最高的15人参谋值总和。
 * @author lwt
 * @date 2023年9月25日
 */
public enum StaffOfficerType {
	SOType1, SOType2, SOType3, SOType4;

	public static StaffOfficerType valueOf(int skillType) {

		switch (skillType) {
		case 1:
			return SOType1;
		case 2:
			return SOType2;

		case 3:
			return SOType3;
		case 4:

			return SOType4;

		default:
			break;
		}

		return null;
	}
}
