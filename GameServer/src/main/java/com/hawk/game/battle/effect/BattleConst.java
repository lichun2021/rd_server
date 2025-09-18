package com.hawk.game.battle.effect;

public interface BattleConst {
	/** 城防官1. 属性只在守城战中生效 */
	public static final int CITY_DEF107 = 107;
	/** 城防官2. 属性只在守城战中生效 */
	public static final int CITY_DEF108 = 108;
	
	/** 超级兵驻守 属性只在守城战中生效 */
	public static final int CITY_DEF109 = 109;

	enum WarEff {
		/** 部队作用号类型-无 */
		NO_EFF,
		/** 部队作用号类型-单人打城 */
		ATK_CITY,
		/** 部队作用号类型-集结打城 */
		ATK_CITY_MASS,
		/** 部队作用号类型-打田 */
		ATK_RES,
		/** 部队作用号类型-打联盟领地 */
		ATK_MANOR,
		/** 部队作用号类型-集结打联盟领地 */
		ATK_MANOR_MASS,
		/** 部队作用号类型-单人进攻收复联盟领地 */
		ATK_RECOVER_MANOR,
		/** 部队作用号类型-进攻收复联盟领地 */
		ATK_RECOVER_MANOR_MASS,
		/** 部队作用号类型-打驻扎点 */
		ATK_QUARTERED,
		/** 部队作用号类型-打总统府 */
		ATK_PRESIDENT,
		/** 部队作用号类型-打总统府电塔 */
		ATK_PRESIDENT_TOWER,
		/** 部队作用号类型-集结打总统府 */
		ATK_PRESIDENT_MASS,
		/** 部队作用号类型-集结打总统府电塔 */
		ATK_PRESIDENT_TOWER_MASS,
		/** 部队作用号类型-打老版野怪 */
		ATK_MONSTER,
		/** 部队作用号类型-集结打老版野怪 */
		ATK_MONSTER_MASS,
		/** 部队作用号类型-打新版野怪 */
		ATK_NEW_MONSTER,
		/** 部队作用号类型-集结打新版野怪 */
		ATK_NEW_MONSTER_MASS,
		/** 部队作用号类型-据点攻击玩家 */
		ATK_STRONG_POINT_PVP,
		/** 部队作用号类型-据点攻击怪物 */
		ATK_STRONG_POINT_PVE,
		/** 部队作用号类型-尤里来袭 */
		ATK_YURI_STRIKE_PVE,
		/** 部队作用号类型-攻击迷雾要塞 */
		ATK_FOGGY,
		/** 部队作用号类型-集结攻击迷雾要塞 */
		ATK_FOGGY_MASS,
		/** 部队作用号类型-攻击超级武器 */
		ATK_SUPER_WEAPON_PVP,
		/** 部队作用号类型-集结攻击超级武器 */
		ATK_SUPER_WEAPON_MASS_PVP,
		/** 部队作用号类型-攻击超级武器NPC */
		ATK_SUPER_WEAPON_PVE,
		/** 部队作用号类型-集结攻击超级武器NPC */
		ATK_SUPER_WEAPON_MASS_PVE,
		/** 部队作用号类型-攻击超级武器 */
		ATK_XZQ_PVP,
		/** 部队作用号类型-集结攻击超级武器 */
		ATK_XZQ_MASS_PVP,
		/** 部队作用号类型-攻击超级武器NPC */
		ATK_XZQ_PVE,
		/** 部队作用号类型-集结攻击超级武器NPC */
		ATK_XZQ_MASS_PVE,
		/** 部队作用号类型-攻击机甲boss */
		ATK_GUNDAM_PVE,
		/** 部队作用号类型-集结攻击机甲boss */
		ATK_GUNDAM_MASS_PVE,
		/** 部队作用号类型-攻击星甲召唤的据点 */
		ATK_STRONG_HOLD_PVE,
		/** 部队作用号类型-集结攻击星甲召唤的据点 */
		ATK_STRONG_HOLD_MASS_PVE,
		
		/** 部队作用号类型-寻宝资源点攻击玩家 */
		ATK_TREASURE_HUNT_RES_PVP,
		/** 部队作用号类型-攻击联盟旗帜 */
		ATTACK_WAR_FLAG_PVP,
		/** 部队作用号类型-攻击航海要塞 */
		ATK_FORTRESS_PVP,
		/** 部队作用号类型-集结攻击航海要塞 */
		ATK_FORTRESS_MASS_PVP,
		/** 部队作用号类型-攻击航海要塞NPC */
		ATK_FORTRESS_PVE,
		/** 部队作用号类型-集结攻击航海要塞NPC */
		ATK_FORTRESS_MASS_PVE,
		/** 部队作用号类型-攻击泰伯利亚建筑 */
		ATK_TBLY_BUILD_PVP,
		/** 部队作用号类型-集结攻击泰伯利亚建筑 */
		ATK_TBLY_BUILD_MASS_PVP,
		/** 部队作用号类型-攻击泰伯利亚资源点*/
		ATK_TBLY_RES,
		/** 部队作用号类型-攻击能量塔*/
		ATK_PYLON,
		/** 部队作用号类型-攻击幽灵工厂怪物*/
		ATK_GHOST_TOWER,
		
		
		
		
		/** 部队作用号类型-自己守城 */
		DEF_CITY,
		/** 部队作用号类型-集结守城 */
		DEF_CITY_MASS,
		/** 部队作用号类型-守田 */
		DEF_RES,
		/** 部队作用号类型-单人防守联盟领地 */
		DEF_MANOR,
		/** 部队作用号类型-集结防守联盟领地 */
		DEF_MANOR_MASS,
		/** 部队作用号类型-防守对方收复联盟领地 */
		DEF_RECOVER_MANOR,
		/** 部队作用号类型-防守对方集结收复联盟领地 */
		DEF_RECOVER_MANOR_MASS,
		/** 部队作用号类型-防守驻扎点 */
		DEF_QUARTERED,
		/** 部队作用号类型-单人防守总统府 */
		DEF_PRESIDENT,
		/** 部队作用号类型-单人防守总统府电塔 */
		DEF_PRESIDENT_TOWER,
		/** 部队作用号类型-集结防守总统府 */
		DEF_PRESIDENT_MASS,
		/** 部队作用号类型-集结防守总统府电塔 */
		DEF_PRESIDENT_TOWER_MASS,
		/** 部队作用号类型-防守尤里复仇 */
		DEF_YURI_REVENGE,
		/** 部队作用号类型-据点防御 */
		DEF_STRONG_POINT_PVP,
		/** 部队作用号类型-集结防守尤里复仇 */
		DEF_YURI_REVENGE_MASS,
		/** 部队作用号类型-星甲召唤舱体防守 */
		DEF_SPACE_MECHA,
		/** 部队作用号类型-星甲召唤舱体集结防守 */
		DEF_SPACE_MECHA_MASS,
		/** 部队作用号类型-防守超级武器 */
		DEF_SUPER_WEAPON,
		/** 部队作用号类型-集结防守超级武器 */
		DEF_SUPER_WEAPON_MASS,
		/** 部队作用号类型-防守超级武器 */
		DEF_XZQ,
		/** 部队作用号类型-集结防守超级武器 */
		DEF_XZQ_MASS,
		/** 部队作用号类型-防守幽灵行军 */
		DEF_GHOST_MARCH,
		/** 部队作用号类型-集结防守幽灵行军  */
		DEF_GHOST_MARCH_MASS,
		/** 部队作用号类型-防守寻宝资源点 */
		DEF_TREASURE_HUNT_RES_PVP,
		/** 部队作用号类型-攻击联盟旗帜 */
		DEF_WAR_FLAG_PVP,
		/** 部队作用号类型-防守航海要塞 */
		DEF_FORTRESS_PVP,
		/** 部队作用号类型-集结防守航海要塞 */
		DEF_FORTRESS_MASS_PVP,
		/** 部队作用号类型-防守泰伯利亚建筑 */
		DEF_TBLY_BUILD_PVP,
		/** 部队作用号类型-集结防守泰伯利亚建筑 */
		DEF_TBLY_BUILD_MASS_PVP,
		/** 部队作用号类型-防守泰伯利亚资源点*/
		DEF_TBLY_RES,
		/** 部队作用号类型-防守能量塔*/
		DEF_PYLON,
		
		/** 部队作用号类型-防守时 */
		DEF {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == DEF_CITY || eff == DEF_CITY_MASS || eff == DEF_RES || eff == DEF_YURI_REVENGE || eff == DEF_YURI_REVENGE_MASS || eff == DEF_MANOR
						|| eff == DEF_MANOR_MASS || eff == DEF_PRESIDENT || eff == DEF_QUARTERED || eff == DEF_RECOVER_MANOR || eff == DEF_RECOVER_MANOR_MASS
						|| eff == DEF_PRESIDENT_MASS || eff == DEF_STRONG_POINT_PVP || eff == DEF_SUPER_WEAPON || eff == DEF_SUPER_WEAPON_MASS || eff == DEF_XZQ || eff == DEF_XZQ_MASS 
						|| eff == DEF_GHOST_MARCH || eff == DEF_GHOST_MARCH_MASS
						|| eff == DEF_TREASURE_HUNT_RES_PVP || eff == DEF_WAR_FLAG_PVP || eff == DEF_FORTRESS_PVP || eff == DEF_FORTRESS_MASS_PVP || eff == DEF_TBLY_BUILD_PVP || eff == DEF_TBLY_BUILD_MASS_PVP
						|| eff == DEF_TBLY_RES || eff == DEF_PYLON|| eff == DEF_PRESIDENT_TOWER || eff == DEF_PRESIDENT_TOWER_MASS || eff == DEF_SPACE_MECHA || eff == DEF_SPACE_MECHA_MASS;
			}
		},
		ATK {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_CITY || eff == ATK_CITY_MASS || eff == ATK_RES || eff == ATK_MONSTER || eff == ATK_MONSTER_MASS || eff == ATK_NEW_MONSTER
						|| eff == ATK_NEW_MONSTER_MASS || eff == ATK_PRESIDENT || eff == ATK_PRESIDENT_MASS || eff == ATK_MANOR || eff == ATK_MANOR_MASS || eff == ATK_QUARTERED
						|| eff == ATK_STRONG_POINT_PVP || eff == ATK_STRONG_POINT_PVE || eff == ATK_FOGGY || eff == ATK_FOGGY_MASS || eff == ATK_SUPER_WEAPON_PVP
						|| eff == ATK_SUPER_WEAPON_MASS_PVP || eff == ATK_SUPER_WEAPON_PVE || eff == ATK_SUPER_WEAPON_MASS_PVE || eff == ATK_YURI_STRIKE_PVE
						|| eff == ATK_XZQ_PVP || eff == ATK_XZQ_MASS_PVP || eff == ATK_XZQ_PVE || eff == ATK_XZQ_MASS_PVE 
						|| eff == ATK_GUNDAM_PVE || eff == ATK_GUNDAM_MASS_PVE || eff == ATK_TREASURE_HUNT_RES_PVP || eff == ATTACK_WAR_FLAG_PVP || eff == ATK_FORTRESS_PVP || eff == ATK_FORTRESS_MASS_PVP
						|| eff == ATK_FORTRESS_PVE || eff == ATK_FORTRESS_MASS_PVE || eff == ATK_TBLY_BUILD_PVP || eff == ATK_TBLY_BUILD_MASS_PVP || eff == ATK_TBLY_RES || eff == ATK_PYLON
						|| eff == ATK_PRESIDENT_TOWER || eff == ATK_PRESIDENT_TOWER_MASS|| eff == WarEff.ATK_GHOST_TOWER || eff == WarEff.ATK_STRONG_HOLD_PVE || eff == WarEff.ATK_STRONG_HOLD_MASS_PVE;
			}
		},
		/** 部队作用号类型-攻击集结时 */
		ATK_MASS {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_CITY_MASS || eff == ATK_MONSTER_MASS || eff == ATK_NEW_MONSTER_MASS || eff == ATK_PRESIDENT_MASS || eff == ATK_MANOR_MASS
						|| eff == ATK_RECOVER_MANOR_MASS || eff == ATK_FOGGY_MASS || eff == ATK_SUPER_WEAPON_MASS_PVP || eff == ATK_SUPER_WEAPON_MASS_PVE || eff == ATK_XZQ_MASS_PVP || eff == ATK_XZQ_MASS_PVE 
						|| eff == ATK_GUNDAM_MASS_PVE || eff == WarEff.ATK_STRONG_HOLD_MASS_PVE
						|| eff == ATK_FORTRESS_MASS_PVP || eff == ATK_FORTRESS_MASS_PVE || eff == ATK_TBLY_BUILD_MASS_PVP || eff == ATK_PRESIDENT_TOWER_MASS;
			}
		},
		/** 部队作用号类型-防御集结时 */
		DEF_MASS {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == DEF_CITY_MASS || eff == DEF_MANOR_MASS || eff == DEF_RECOVER_MANOR || eff == DEF_RECOVER_MANOR_MASS || eff == DEF_PRESIDENT_MASS
						|| eff == DEF_YURI_REVENGE_MASS || eff == DEF_SUPER_WEAPON_MASS || eff == DEF_XZQ_MASS || eff == DEF_GHOST_MARCH_MASS || eff == DEF_FORTRESS_MASS_PVP || eff == DEF_TBLY_BUILD_MASS_PVP
						|| eff == DEF_PRESIDENT_TOWER_MASS || eff == DEF_SPACE_MECHA_MASS;
			}
		},
		/** 部队作用号类型-集结 */
		MASS {
			@Override
			public boolean check(WarEff eff) {
				return WarEff.ATK_MASS.check(eff) || WarEff.DEF_MASS.check(eff);
			}
		},
		/**个人战. 不管是攻防, 还是模拟战*/
		SELF_FIGHT {
			@Override
			public boolean check(WarEff eff) {
				return !MASS.check(eff);
			}
		},
		/** 部队作用号类型-个人进攻 */
		SELF_ATK {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK || eff == ATK_CITY || eff == ATK_RES || eff == ATK_MONSTER || eff == ATK_NEW_MONSTER || eff == ATK_QUARTERED
						|| eff == ATK_PRESIDENT || eff == ATK_MANOR || eff == ATK_RECOVER_MANOR || eff == ATK_STRONG_POINT_PVP || eff == ATK_STRONG_POINT_PVE
						|| eff == ATK_SUPER_WEAPON_PVP || eff == ATK_SUPER_WEAPON_PVE || eff == ATK_XZQ_PVP || eff == ATK_XZQ_PVE || eff == ATK_YURI_STRIKE_PVE || eff == ATK_GUNDAM_PVE || eff == ATK_TREASURE_HUNT_RES_PVP
						|| eff == ATTACK_WAR_FLAG_PVP || eff == ATK_FOGGY || eff == ATK_FORTRESS_PVP || eff == ATK_FORTRESS_PVE || eff == ATK_TBLY_BUILD_PVP || eff == ATK_TBLY_RES
						|| eff == ATK_PYLON || eff == ATK_PRESIDENT_TOWER|| eff == WarEff.ATK_GHOST_TOWER || eff == WarEff.ATK_STRONG_HOLD_PVE;
			}
		},
		/** 部队作用号类型-攻城 */
		CITY_ATK {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_CITY || eff == ATK_CITY_MASS;
			}
		},
		/** 部队作用号类型-守城 */
		CITY_DEF {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == DEF_CITY || eff == DEF_CITY_MASS || eff == DEF_YURI_REVENGE || eff == DEF_YURI_REVENGE_MASS || eff == DEF_GHOST_MARCH || eff == DEF_GHOST_MARCH_MASS;
			}
		},

		/******************************** 战斗场景分类 **********************************/
		/** 部队作用号类型-打怪 */
		MONSTER {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_MONSTER || eff == ATK_MONSTER_MASS || eff == ATK_NEW_MONSTER || eff == ATK_NEW_MONSTER_MASS || eff == DEF_YURI_REVENGE
						|| eff == ATK_STRONG_POINT_PVE || eff == DEF_YURI_REVENGE_MASS || eff == ATK_FOGGY || eff == ATK_FOGGY_MASS || eff == ATK_SUPER_WEAPON_PVE 
						|| eff == ATK_SUPER_WEAPON_MASS_PVE || eff == ATK_YURI_STRIKE_PVE || eff == DEF_GHOST_MARCH || eff == DEF_GHOST_MARCH_MASS || eff == ATK_GUNDAM_PVE
						|| eff == ATK_GUNDAM_MASS_PVE || eff == WarEff.ATK_GHOST_TOWER || eff == WarEff.ATK_STRONG_HOLD_PVE || eff == WarEff.ATK_STRONG_HOLD_MASS_PVE
						|| eff == WarEff.DEF_SPACE_MECHA || eff == WarEff.DEF_SPACE_MECHA_MASS;
			}
		},
		/** 部队作用号类型-新版野怪 */
		NEW_MONSTER {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_NEW_MONSTER || eff == ATK_NEW_MONSTER_MASS;
			}
		},
		/** 部队作用号类型-基地类战斗 */
		CITY {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_CITY || eff == ATK_CITY_MASS || eff == DEF_CITY || eff == DEF_CITY_MASS || eff == DEF_YURI_REVENGE
						|| eff == DEF_YURI_REVENGE_MASS|| eff == DEF_GHOST_MARCH || eff == DEF_GHOST_MARCH_MASS;
			}
		},
		/** 部队作用号类型-资源田 */
		RES {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_RES || eff == DEF_RES;
			}
		},
		/** 部队作用号类型-驻扎点 */
		QUARTERED {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_QUARTERED || eff == DEF_QUARTERED;
			}
		},
		/** 部队作用号类型-联盟堡垒类战斗 */
		MANOR {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_MANOR || eff == ATK_MANOR_MASS || eff == ATK_RECOVER_MANOR || eff == ATK_RECOVER_MANOR_MASS || eff == DEF_RECOVER_MANOR
						|| eff == WarEff.DEF_RECOVER_MANOR_MASS || eff == DEF_MANOR || eff == DEF_MANOR_MASS;
			}
		},
		/** 部队作用号类型-总统府类战斗 */
		PRESIDENT {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_PRESIDENT || eff == ATK_PRESIDENT_MASS || eff == DEF_PRESIDENT || eff == DEF_PRESIDENT_MASS;
			}
		},
		/** 部队作用号类型-总统府电塔类战斗 */
		PRESIDENT_TOWER {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_PRESIDENT_TOWER || eff == ATK_PRESIDENT_TOWER_MASS || eff == DEF_PRESIDENT_TOWER || eff == DEF_PRESIDENT_TOWER_MASS;
			}
		},
		/** 部队作用号类型-据点类战斗 */
		STRONG_POINT {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_STRONG_POINT_PVP || eff == ATK_STRONG_POINT_PVE || eff == DEF_STRONG_POINT_PVP;
			}
		},
		/** 部队作用号类型-超级武器类战斗 */
		SUPER_WEAPON {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_SUPER_WEAPON_PVP || eff == ATK_SUPER_WEAPON_MASS_PVP || eff == DEF_SUPER_WEAPON || eff == DEF_SUPER_WEAPON_MASS
						|| eff == ATK_SUPER_WEAPON_PVE || eff == WarEff.ATK_SUPER_WEAPON_MASS_PVE;
			}
		},
		/** 部队作用号类型-超级武器类战斗 */
		XZQ {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_XZQ_PVP || eff == ATK_XZQ_MASS_PVP || eff == DEF_XZQ || eff == DEF_XZQ_MASS
						|| eff == ATK_XZQ_PVE || eff == WarEff.ATK_XZQ_MASS_PVE;
			}
		},
		/** 部队作用号类型-航海要塞类战斗 */
		FROTRESS{
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_FORTRESS_PVP || eff == ATK_FORTRESS_MASS_PVP || eff == DEF_FORTRESS_PVP || eff == DEF_FORTRESS_MASS_PVP
						|| eff == ATK_FORTRESS_PVE || eff == ATK_FORTRESS_MASS_PVE;
			}
		},
		/** 部队作用号类型-寻宝资源点类战斗*/
		TREASURE_HUNT_RES{
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_TREASURE_HUNT_RES_PVP || eff == DEF_TREASURE_HUNT_RES_PVP;
			}
		},
		/** 部队作用号类型-联盟旗帜类战斗 */
		WAR_FLAG {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATTACK_WAR_FLAG_PVP || eff == DEF_WAR_FLAG_PVP;
			}
		},
		/** 部队作用号类型-泰伯利亚建筑类战斗 */
		WAR_TBLY_BUILD {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_TBLY_BUILD_PVP || eff == ATK_TBLY_BUILD_MASS_PVP || eff == DEF_TBLY_BUILD_PVP || eff == DEF_TBLY_BUILD_MASS_PVP;
			}
		},
		/** 部队作用号类型-泰伯利亚资源田 */
		WAR_TBLY_RES {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_TBLY_RES || eff == DEF_TBLY_RES;
			}
		},
		/** 部队作用号类型-能量塔 */
		WAR_PYLON {
			@Override
			public boolean check(WarEff eff) {
				return super.check(eff) || eff == ATK_PYLON || eff == DEF_PYLON;
			}
		};

		public boolean check(WarEff eff) {
			return this == eff;
		}
	}

	enum Const {
		/** 战斗公式幂函数常量 */
		POW(31),
		/** 冲锋概率44 不冲锋 56 */
		ASSAULT_RATE(44),
		/** 初始占位 */
		INIT_POS(4),
		/** 箭塔初始占位 */
		BARTIZAN_POS(3);

		int value;

		Const(int value) {
			this.value = value;
		}

		public int getNumber() {
			return value;
		}
	}

	enum Troop {
		/** 攻方 */
		ATTACKER {
			@Override
			public String shortName() {
				return "攻";
			}
		},
		/** 防方 */
		DEFENDER {
			@Override
			public String shortName() {
				return "防";
			}
		};

		abstract public String shortName();
	}

	enum BattleType {
		/** 其它战斗类型 */
		OTHER(0),
		/** 玩家VS玩家 攻城 */
		ATTACK_CITY(1),
		/** 玩家VS玩家 资源田 */
		ATTACK_RES(2),
		/** 玩家VS野怪 */
		ATTACK_MONSTER(3),
		/** 尤里复仇 尤里vs玩家 */
		YURI_YURIREVENGE(4),
		/** 玩家vs玩家 联盟领地 */
		ATTACK_MANOR(5),
		/** 玩家vs玩家 总统府 */
		ATTACK_PRESIDENT(6),
		/** 玩家vs玩家 据点 */
		ATTACK_QUARTERED(7),
		/** 玩家VS玩家 收复领地 */
		RECOVER_MANOR(8),
		/** 玩家VS玩家 据点 */
		ATTACK_STRONG_POINT_PVP(9),
		/** 玩家VS怪物 据点 */
		ATTACK_STRONG_POINT_PVE(10),
		/** 玩家VS迷雾要塞怪物 */
		ATTACK_FOGGY(11),
		/** 玩家VS新版野怪 */
		ATTACK_NEW_MONSTER(12),
		/** 玩家VS尤里来袭*/
		ATTACK_YURI_STRIKE_PVE(13),
		/** 玩家vs玩家 超级武器 **/
		ATTACK_SUPER_WEAPON_PVP(14),
		/** 玩家vsNPC 超级武器 **/
		ATTACK_SUPER_WEAPON_PVE(15),
		/** 幽灵行军*/
		GHOST_MARCH(16),
		/** 玩家vs机甲boss*/
		ATTACK_GUNDAM_PVE(17),
		/** 玩家vs玩家 寻宝资源点 */
		ATTACK_TREASURE_HUNT_RES(18),
		/** 争夺联盟旗帜战斗 */
		ATTACK_WAR_FLAG(19),
		/** 玩家vsNPC 航海要塞 **/
		ATTACK_FORTRESS_PVE(20),
		/** 玩家vs玩家 航海要塞 **/
		ATTACK_FORTRESS_PVP(21),
		/** 玩家vs玩家 泰伯利亚建筑 **/
		ATTACK_TBLY_BUILD(22),
		/** 玩家vs玩家 泰伯利亚资源点 **/
		ATTACK_TBLY_RES(23),
		/**锦标赛*/
		GUILD_CHAMPIONSHIP(24),
		/** 攻防模拟战*/
		SIMULATE_WAR(25),
		/** 能量塔战斗*/
		PYLON_WAR(26),
		/** 玩家vs玩家 总部电塔 */
		ATTACK_PRESIDENT_TOWER(27),
		/** 挑战幽灵工厂*/
		ATTACK_GOHOST_TOWER(28),
		/** 小战区pve*/
		ATTACK_XZQ_PVE(29),
		/** 小战区pvp*/
		ATTACK_XZQ_PVP(30),
		YQZZ_BUILD_PVE(31),
		/** 星甲召唤舱体守卫 */
		SPACE_MECHA_PVE(32),
		/** 玩家攻击星甲召唤的据点  */
		ATTACK_STRONG_HOLD_PVE(33),
		/** 反攻幽灵pve*/
		FGYL_BUILD_PVE(34),
		;
		

		int value;
		
		BattleType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}

	/** 跟技能配置有关 */
	enum TroopType {
		/** 部队技能属性-所有 */
		ALL,
		/** 部队技能属性-攻城 */
		ATK_CITY,
		/** 部队技能属性-守城 */
		DEF_CITY,
		/** 部队技能属性-资源战 */
		RES,
		/** 部队技能属性-资源战 */
		ATK_MONSTER,
		/** 部队技能属性-尤里复仇 */
		DEF_YURI_REVENGE;

	}
}
