package com.hawk.game.service.pushgift;

public enum PushGiftConditionEnum {
	//PVE_ATTACK_FAIL(100), //攻击野怪失败
	//PVP_DEFENCE_FAIL(200),  //PVP防守失败
	//PVP_ATTACK_FAIL(300),   //PVP攻击失败
	HERO_STAR_UP(400),       //英雄升星
	HERO_LEVEL_UP(500),      //英雄升级
	BUILDING_PAKCAGE(600),  //城建礼包
	COMMANDER_PACKAGE(700), //指挥官礼包
	ALL_ATTACK_FAIL(800),   //所有的战败礼包.
	UNLOCK_HERO(900),		  //解锁英雄
	ONE_KEY_HERO_UP(1100),   //一键升级英雄礼
	ONE_KEY_HERO_SKILL_UP(1200), //一键英雄技能升级.
	ALL_ATTACK_FAIL_EXTRA(1300), //战败礼包增加条件.
	SPECIAL(1400),
	
	TRAIN_SPEED(1500),        // 训练加速
	CURE_SPEED(1600),         // 治疗加速
	ITEM_CONSUME(1700),       // 指定道具消耗
	SUPER_LAB_LEVEL_UP(1800), // 超能实验室升级部件
	EQUIP_RESEARCH(1900),     // 装备研究升级
	EQUIP_ENHANCE(2000),      // 装备强化
	COMMANDER_EXP(2100),      // 指挥官经验
	
	EQUIP_RESEARCH_UNLOCK(2200), //装备研究解锁触发
	EQUIP_RESEARCH_LEVEL(2300), //装备研究强化到指定等级触发
	EQUIP_LEVEL(2400), //装备强化到指定等级触发
	ENGRY(2600), //单套能量源强化至xx等级触发
	
	/*
	 * 新增推送礼包 2022.09.01
	 */
	PUSH_GIFT_2700(2700), // 泰能强化第一次升级
	PUSH_GIFT_2800(2800), // 泰能战士第一次破译完成
	
	PUSH_GIFT_2900(2900), // 第X次将装备等级强化至Y级
	PUSH_GIFT_3000(3000), // 第X次将装备泰晶等级强化至Y级
	PUSH_GIFT_3100(3100), // 四兵营/大本的5个泰能模块强达到X级
	PUSH_GIFT_3200(3200), // 用户近X时间内充值金额达到Y元
	
	PUSH_GIFT_3300(3300), // 泰能战士--仪器升级礼包
	PUSH_GIFT_3400(3400), // 泰能战士--泰能破译礼包
	PUSH_GIFT_3500(3500), // 泰能战士--晶体分析礼包
	PUSH_GIFT_3600(3600), // 泰能战士--战士强化礼包
	
	PUSH_GIFT_3700(3700), // 月球 盟军医院加速礼包
	PUSH_GIFT_3800(3800), // 星能解锁礼包（解锁星能探索功能 时触发）
	PUSH_GIFT_3900(3900), // 量子扩充礼包（任一装备量子扩充等级达到XX级且VIP等级在vipMin_vipMax之间 时触发）
	;
	private int type;
	
	PushGiftConditionEnum(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
	} 

}
