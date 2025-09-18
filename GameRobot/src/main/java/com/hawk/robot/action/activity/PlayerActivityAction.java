package com.hawk.robot.action.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.annotation.RobotAction;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Activity.AchieveItemPB;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.BuildLevelItemPB;
import com.hawk.game.protocol.Activity.TakeAchieveRewardReq;
import com.hawk.game.protocol.Activity.TakeAchieveRewardReq.Builder;
import com.hawk.game.protocol.Activity.TakeBuildActivityRewardReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.YuriRevenge.GetYuriRankInfo;
import com.hawk.game.protocol.YuriRevenge.State;
import com.hawk.game.protocol.YuriRevenge.YuriRankType;
import com.hawk.game.protocol.YuriRevenge.YuriRevengePageInfoResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;

/**
 * 活动相关操作
 * 
 * @author PhilChen
 *
 */
@RobotAction(valid = false)
public class PlayerActivityAction extends HawkRobotAction {

	/**
	 * 首领随机行为Map
	 */
	private Map<ActionYuriType, Integer> yuriActMap = new HashMap<ActionYuriType, Integer>();
	
	public PlayerActivityAction() {
		super();
		yuriActMap.put(ActionYuriType.GET_PAGE_INFO, ActionYuriType.GET_PAGE_INFO.getRand());
		yuriActMap.put(ActionYuriType.OPEN_FIGHT, ActionYuriType.OPEN_FIGHT.getRand());
		yuriActMap.put(ActionYuriType.GET_SELF_RANK, ActionYuriType.GET_SELF_RANK.getRand());
		yuriActMap.put(ActionYuriType.GET_GUILD_RANK, ActionYuriType.GET_GUILD_RANK.getRand());
		yuriActMap.put(ActionYuriType.NONE, ActionYuriType.NONE.getRand());
	}
	
	/**
	 * 活动类型
	 */
	private static enum ActionActivityType {
		BUILD_LEVEL_ACTIVITY,   // 建筑等级活动
		GROW_FUND_ACTIVITY,     // 成长基金活动
		LOGIN_DAY_ACTIVITY,     // 累计登陆活动 
		LOGIN_SIGN_ACTIVITY,    // 登陆签到活动
		ACHIEVE_ACTIVITY,       // 成就活动
		YURI_REVENGE_ACTIVITY   // 尤里复仇活动
	}
	
	/**
	 * 尤里复仇活动行为类型
	 */
	private enum ActionYuriType {
		GET_PAGE_INFO(10),  // 获取活动页面信息
		OPEN_FIGHT(30),     // 开启活动
		GET_SELF_RANK(15),  // 获取个人排行信息
		GET_GUILD_RANK(15), // 获取联盟排行信息
		NONE(30);           // 什么都不做
		
		private final int rand;
		
		private ActionYuriType(int rand){
			this.rand = rand;
		}

		public int getRand() {
			return rand;
		}
	}
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		ActionActivityType type = EnumUtil.random(ActionActivityType.class);
		switch (type) {
		case BUILD_LEVEL_ACTIVITY:
			doBuildLevelAction(robot);
			break;
		case GROW_FUND_ACTIVITY:
			doGrowFundAction(robot);
			break;
		case LOGIN_SIGN_ACTIVITY:
			doLoginSignAction(robot);
			break;
		case ACHIEVE_ACTIVITY:
			achieveReward(robot);
			break;
		case YURI_REVENGE_ACTIVITY:
			doYuriRevengeAction(robot);
			break;
		default:
			break;
			
		}
	}
	
	/**
	 * 成就活动
	 * 
	 * @param robot
	 * @param achieveInfos
	 */
	public static synchronized void achieveReward(GameRobotEntity robot) {
		if(!robot.isOnline()) {
			return;
		}
		
		List<AchieveItemPB> removeList = new ArrayList<>();
		for (AchieveItemPB itemPB : robot.getActivityData().getAchieveInfo()) {
			if (itemPB.getState() != AchieveState.NOT_REWARD_VALUE) {
				continue;
			}
			
			removeList.add(itemPB);
			Builder builder = TakeAchieveRewardReq.newBuilder();
			builder.setAchieveId(itemPB.getAchieveId());
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.TAKE_ACHIEVE_REWARD_C, builder));
			RobotLog.activityPrintln("achieve activity reward, playerId: {}, achieveId: {}", robot.getPlayerId(), itemPB.getAchieveId());
		}
		
		robot.getActivityData().getAchieveInfo().removeAll(removeList);
	}
	
	/**
	 * 建筑等级活动
	 * 
	 * @param robot
	 */
	private void doBuildLevelAction(GameRobotEntity robot) {
		Map<Integer, BuildLevelItemPB> map = robot.getActivityData().getActivityData(ActivityType.BUILD_LEVEL);
		if (map == null) {
			return;
		}
		for (BuildLevelItemPB itemPB : map.values()) {
			if (itemPB.getState() == AchieveState.NOT_REWARD.getNumber()) {
				TakeBuildActivityRewardReq.Builder builder = TakeBuildActivityRewardReq.newBuilder();
				builder.setItemId(itemPB.getItemId());
				robot.sendProtocol(HawkProtocol.valueOf(HP.code.TAKE_BUILD_LEVEL_REWARD_C, builder));
				RobotLog.activityPrintln("build level activity reward, playerId: {}, itemId: {}", robot.getPlayerId(), itemPB.getItemId());
			}
		}
	}
	
	/**
	 * 成长基金活动
	 * 
	 * @param robot
	 */
	private void doGrowFundAction(GameRobotEntity robot) {
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.GROW_FUND_BUY));
		RobotLog.activityPrintln("grow fund activity, playerId: {}", robot.getPlayerId());
	}
	
	
	/**
	 * 登陆签到活动
	 * 
	 * @param robot
	 */
	private void doLoginSignAction(GameRobotEntity robot) {
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.TAKE_LOGIN_SIGN_REWARD_C));
		RobotLog.activityPrintln("login sign activity, playerId: {}", robot.getPlayerId());
	}
	
	
	/**
	 * 尤里复仇活动
	 * 
	 * @param robot
	 */
	private void doYuriRevengeAction(GameRobotEntity robot) {
		YuriRevengePageInfoResp pageInfo = robot.getActivityData().getActivityData(ActivityType.YURI_REVENGE);
		// 玩家未请求过活动信息,则优先进行请求操作
		if(pageInfo == null){
			this.getPageInfoAcftion(robot);
			RobotLog.activityDebugPrintln("Robot {} yurirevenge page info null,first Do {} Action", robot.getPuid(), ActionYuriType.GET_PAGE_INFO);
			return;
		}
		
		State state = pageInfo.getState();
		ActionYuriType type = HawkRand.randomWeightObject(yuriActMap);
		switch (type) {
		case GET_PAGE_INFO:
			this.getPageInfoAcftion(robot);
			break;
		case OPEN_FIGHT:
			this.openFightAction(robot, state);
			break;
		case GET_SELF_RANK:
			this.getRankInfoAction(robot,YuriRankType.SELF_RANK);
			break;
		case GET_GUILD_RANK:
			this.getRankInfoAction(robot,YuriRankType.GUILD_RANK);
			break;
		case NONE:
			break;
		default:
			break;
		}
		
		RobotLog.activityDebugPrintln("Robot {} is leader, this period Do {} Action", robot.getPuid(), type);
	}
	
	/**
	 * 获取尤里复仇界面信息
	 * @param robot
	 */
	private void getPageInfoAcftion(GameRobotEntity robot){
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.GET_YURI_REVENGE_PAGE_INFO_C_VALUE));
	}
	
	/**
	 * 开启尤里复仇活动
	 * @param robot
	 * @param state 
	 */
	private void openFightAction(GameRobotEntity robot, State state){
		// 非四阶以上成员,操作转化为获取界面信息
		if(!robot.getGuildData().isLeader()){
			getPageInfoAcftion(robot);
			return;
		}
		// 非可开启阶段,操作转化为获取界面信息
		if(!state.equals(State.OPEN)){
			getPageInfoAcftion(robot);
			return;
		}
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.OPEN_YURI_REVENGE_ACTIVITY));
	}
	
	/**
	 * 获取排行页面信息
	 * @param robot
	 * @param rankType
	 */
	private void getRankInfoAction(GameRobotEntity robot, YuriRankType rankType){
		GetYuriRankInfo.Builder req = GetYuriRankInfo.newBuilder();
		req.setRankType(rankType);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.GET_YURI_REVENGE_RANK_INFO_C, req));
	}
}
