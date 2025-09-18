package com.hawk.robot.action.friend;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Friend.BlacklistInfoReq;
import com.hawk.game.protocol.Friend.FriendAddReq;
import com.hawk.game.protocol.Friend.FriendApplyInfoReq;
import com.hawk.game.protocol.Friend.FriendApplyMsg;
import com.hawk.game.protocol.Friend.FriendInfoReq;
import com.hawk.game.protocol.Friend.FriendMsg;
import com.hawk.game.protocol.Friend.FriendType;
import com.hawk.game.protocol.Friend.HandleFriendApplyReq;
import com.hawk.game.protocol.Friend.OperationType;
import com.hawk.game.protocol.Friend.PresentGiftReq;
import com.hawk.game.protocol.Friend.RecommendFriendsReq;
import com.hawk.game.protocol.Friend.SearchStrangerReq;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.RobotLog;

/**
 * 好友
 * @author golden
 *
 */
@RobotAction(valid = true)
public class FriendAction extends HawkRobotAction {
	/**
	 * 通用size
	 */
	private static final int SIZE = 5;
	/**
	 * 字符串最大长度
	 */
	private static final int STRING_LENGTH_LIMIT = 30;
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
//		if (SIZE > 1) {
//			doPresentGiftReq(robotEntity);
//			return;
//		}
		
		FriendActionType actionType = EnumUtil.random(FriendActionType.class);
		switch(actionType) {
		case DOFRIENDINFOREQ:
			doFriendInfoReq(robotEntity);
			break;
		case DOFRIENDAPPLYINFOREQ:
			doFriendApplyInfoReq(robotEntity);
			break;
		case DORECOMMENDFRIENDSREQ:
			doRecommendFriendsReq(robotEntity);
			break;
		case DOBLACKLISTINFOREQ:
			doBlacklistInfoReq(robotEntity);
			break;
		case DOSEARCHSTRANGERREQ:
			doSearchStrangerReq(robotEntity);
			break;
		case DOFRIENDADDREQ:
			doFriendAddReq(robotEntity);
			break;
		case DOHANDLEFRIENDAPPLYREQ:
			doHandleFriendApplyReq(robotEntity);
			break;
 		case DOPRESENTGIFTREQ:
			doPresentGiftReq(robotEntity);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 好友信息请求
	 * @param robotEntity
	 */
	private void doFriendInfoReq(HawkRobotEntity robotEntity) {
		FriendInfoReq.Builder req = FriendInfoReq.newBuilder();
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.FRIEND_INFO_REQ, req));
		RobotLog.cityPrintln("doFriendInfoReq, playerId: {}", robotEntity.getPlayerId());
	}
	
	/**
	 * 申请的信息
	 * @param robotEntity
	 */
	private void doFriendApplyInfoReq(HawkRobotEntity robotEntity) {
		FriendApplyInfoReq.Builder req = FriendApplyInfoReq.newBuilder();
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.FRIEND_APPLY_INFO_REQ, req));
		RobotLog.cityPrintln("doFriendApplyInfoReq, playerId: {}", robotEntity.getPlayerId());
	}
	
	/**
	 * 推荐好友
	 * @param robotEntity
	 */
	private void doRecommendFriendsReq(HawkRobotEntity robotEntity) {
		RecommendFriendsReq.Builder req = RecommendFriendsReq.newBuilder();
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.RECOMMEND_FRIENDS_REQ, req));
		RobotLog.cityPrintln("doRecommendFriendsReq, playerId: {}", robotEntity.getPlayerId());
	}
	
	/**
	 * 黑名单
	 * @param robotEntity
	 */
	private void doBlacklistInfoReq(HawkRobotEntity robotEntity) {
		BlacklistInfoReq.Builder req = BlacklistInfoReq.newBuilder();
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.BLACKLIST_INFO_REQ, req));
		RobotLog.cityPrintln("doBlacklistInfoReq, playerId: {}", robotEntity.getPlayerId());
	}
	
	/**
	 * 查找好友
	 * @param robotEntity
	 */
	private static void doSearchStrangerReq(HawkRobotEntity robotEntity){
		SearchStrangerReq.Builder req = SearchStrangerReq.newBuilder();
		req.setSex(0);
		req.setSameCity(0);
		req.setName(randomString((int) (Math.random() * SIZE) + 1));
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.SEARCH_STRANGER_REQ, req));
		RobotLog.cityPrintln("doSearchStrangerReq, playerId: {}", robotEntity.getPlayerId());
	}

	/**
	 * 添加好友
	 * @param robotEntity
	 */
	private static void doFriendAddReq(HawkRobotEntity robotEntity) {
		List<String> robotIds = WorldDataManager.getInstance().getRobotOnline();
		List<String> reqList = new ArrayList<String>();
		for (int i = 0; i < HawkRand.randInt(SIZE + 1); i++) {
			reqList.add(robotIds.get(HawkRand.randInt(robotIds.size() - 1)));
		}
		
		FriendAddReq.Builder req = FriendAddReq.newBuilder();
		req.addAllPlayerIds(reqList);
		req.setContent(randomString(HawkRand.randInt(STRING_LENGTH_LIMIT - 1) + 1));
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.FRIEND_ADD_REQ, req));
		RobotLog.cityPrintln("doFriendAddReq, playerId: {}", robotEntity.getPlayerId());
	}
	
	/**
	 * 处理好友申请
	 * @param robotEntity
	 * @param applyMsgs
	 */
	public static void doHandleFriendApplyReq(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		List<FriendApplyMsg> friendApplyMsg = robot.getBasicData().getFriendApplyMsg();
		if (friendApplyMsg.isEmpty()) {
			return;
		}
		
		HandleFriendApplyReq.Builder req = HandleFriendApplyReq.newBuilder();
		req.setType(EnumUtil.random(OperationType.class));
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.HANDLE_FRIEND_APPLY_REQ, req));
		RobotLog.cityPrintln("doHandleFriendApplyReq, playerId: {}", robotEntity.getPlayerId());
	}
	
	/**
	 * 好友赠送礼物
	 * @param robotEntity
	 * @param applyMsg
	 */
	public static void doPresentGiftReq(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		List<FriendMsg> friendMsg = robot.getBasicData().getFriendMsg();
		if (friendMsg.isEmpty()) {
			return;
		}
		
		FriendType type = EnumUtil.random(FriendType.class);
		String reqPlayerId = null;
		if (type.equals(FriendType.PLATFORM)) {
			type = FriendType.GAME;
		} else if (type.equals(FriendType.COMMON)) {
			reqPlayerId = friendMsg.get(0).getPlayerId();
		}
		
		PresentGiftReq.Builder req = PresentGiftReq.newBuilder();
		req.setType(type);
		if (!HawkOSOperator.isEmptyString(reqPlayerId)) {
			req.setPlayerId(reqPlayerId);
		}
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.FRIEND_PRESENT_GIFT_REQ, req));
		RobotLog.cityPrintln("doPresentGiftReq, playerId: {}", robotEntity.getPlayerId());
	}
	
	/**
	 * 随机指定长度字符串
	 * @param length
	 * @return
	 */
	public static String randomString(int length) {
		if (length <= 0 || length > STRING_LENGTH_LIMIT) {
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			String randomString = randomString();
			if (HawkOSOperator.isEmptyString(randomString)) {
				continue;
			}
			sb.append(randomString());
		}
		
		return sb.toString();
	}
	
	/**
	 * 随机字符
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String randomString() {
		switch(EnumUtil.random(StringType.class)) {
		case ENGLISH:
			int rand = (int)(HawkRand.randInt(26) + 97);
			return String.valueOf((char)rand);
		case CHINESE:
			return getRandomChinese();
		case NUM:
			return String.valueOf(HawkRand.randInt(26));
		default:
			return "";
		}
	}
	
	/**
	 * 随机中文字
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String getRandomChinese() {
		String ret = "";
		try {
			int highPos = 176 + Math.abs(HawkRand.randInt(39));
			int lowPos = 161 + Math.abs(HawkRand.randInt(93));
			byte[] b = { (Integer.valueOf(highPos)).byteValue(), (Integer.valueOf(lowPos)).byteValue() };
			ret = new String(new String(b, "GBK").getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			HawkException.catchException(e);
		}
		return ret;
	}
	
	/**
	 * 字符类型
	 * @author golden
	 *
	 */
	private enum StringType {
		/**
		 * 英文字母
		 */
		ENGLISH,
		/**
		 * 汉字
		 */
		CHINESE,
		/**
		 * 数字
		 */
		NUM,
		;
	}
	
	/**
	 * 好友机器人操作类型
	 * @author golden
	 *
	 */
	private enum FriendActionType {
		
		DOFRIENDINFOREQ,
		
		DOFRIENDAPPLYINFOREQ,
		
		DORECOMMENDFRIENDSREQ,
		
		DOBLACKLISTINFOREQ,
		
		DOSEARCHSTRANGERREQ,
		
		DOFRIENDADDREQ,
		
		DOHANDLEFRIENDAPPLYREQ,
		
		DOPRESENTGIFTREQ,
		;
	}
}