package com.hawk.game.player.roleexchange;

public class XinyueConst {

	/**
	 * 心悦角色交易交易状态
	 */
	public static class XinyueRoleExchangeState {
		public static final int EXCHANGE_CANCLE_REG        = 101; //角色取消登记：玩家已经从角色交易取回角色，可自由登录
		public static final int EXCHANGE_REVIEW            = 200; //角色处于审核期：表示角色已在角色交易侧登记且正处于审核期，处于审核期的角色可允许自由登录游戏
		public static final int EXCHANGE_NOTICE            = 300; //公示期：角色审核通过，进入公示期。处于公示期状态的角色不允许登录游戏，也不允许购买，只能被查看
		public static final int EXCHANGE_LAUNCH            = 400; //上架期：角色公示通过，进入上架期。处于上架期状态的角色不允许登录游戏，可以被购买
		public static final int EXCHANGE_INSPECTION        = 500; //考察期： 角色被购买，转移到买家账号上后，进入考察期。处于考察期状态的角色可以登录游戏，但不能进行敏感操作
		public static final int EXCHANGE_INSPECTION_FAILED = 501; //考察不通过：未通过考察期，角色回滚到交易前状态。处于考察不通过的角色不允许登录
		public static final int EXCHANGE_FINISH_SUCCESS    = 600; //交易成功：角色通过考察期，该角色交易流程完成并结束，角色可以自由登录 
		public static final int EXCHANGE_DOING             = 999; //服务器正在转移角色，不能登录（这个状态值只是本地用的）
	}
	
	/**
	 * 服务器运行标识（0: 未停服 1: 已停服）
	 */
	public static class ServerState {
		public static final int SERVER_RUNNING = 0;
		public static final int SERVER_STOPPED = 1;
	}
	
	/**
	 * 心悦转移角色失败原因
	 */
	public static class XinyueRoleExchangeFailReason {
		public static final int ERROR_0_SUCCESS    = 0;
//		public static final int ERROR_1001  = 1001; //角色为内部账号 -- 腾讯限制?
//		public static final int ERROR_1002	= 1002; //角色处于封号状态 -- 腾讯限制?
//		public static final int ERROR_1003	= 1003; //角色处于禁言状态 -- 腾讯限制?
		public static final int ERROR_1001	= 1001; //摘要：角色所属账号下已有其他角色上架
		public static final int ERROR_1004	= 1004; //摘要：角色建筑工厂等级未达到15级
		public static final int ERROR_1005	= 1005; //摘要：在该服务器上未获取到角色信息
		public static final int ERROR_1006	= 1006; //"摘要：角色有未完成的交易, 详情：角色有未完成的交易，请前往游戏完成交易。"
		public static final int ERROR_1007  = 1007; //角色所拥有的金条数过多
		public static final int ERROR_1008	= 1008; //摘要：角色所处区服开服时间未达到30天
		public static final int ERROR_1009	= 1009; //角色距离上次交易未满30天
		public static final int ERROR_1010	= 1010; //角色正处于航海远征等跨服玩法或泰伯利亚之战、赛博之战、联合军演等副本、跨服玩法中。
		public static final int ERROR_1011	= 1011; //摘要：角色所属账号30天内已有其他角色交易成功
		public static final int ERROR_1012	= 1012; //"摘要：角色未处于本次角色交易测试的白名单中, 详情：角色未处于本次角色交易测试的白名单中"  TODO
		public static final int ERROR_1013	= 1013; //详情：角色有联盟，不能上架
		public static final int ERROR_1014	= 1014; //详情：角色有守护，不能上架
		public static final int ERROR_1015	= 1015; //详情: 备份异常 不能上架
		public static final int ERROR_1016	= 1016; //摘要：有二级密码，不能上架
		public static final int ERROR_1017	= 1017; //详情: 买家在该区服已有角色，无法购买
		public static final int ERROR_1018	= 1018; //摘要：买家在该区服也有就角色，且处于注销状态，无法购买
		public static final int ERROR_1019	= 1019; //详情：卖家处于注销状态，无法进行角色交易
		public static final int ERROR_1022	= 1022; //"摘要：买家处于封禁状态，不能交易, 详情：买家处于封禁状态，不能交易"
		public static final int ERROR_1023	= 1023; //该角色正处于公示期，当前阶段无法购买
	}
	
}
