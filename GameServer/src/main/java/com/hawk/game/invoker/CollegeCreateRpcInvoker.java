package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 创建军事学院
 * 
 * @author Jesse
 *
 */
public class CollegeCreateRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;
	private String collegeName;
	/** 消耗信息 */
	private ConsumeItems consume;

	/** 协议Id */
	private int hpCode;

	public CollegeCreateRpcInvoker(Player player, String collegeName,ConsumeItems consume, int hpCode) {
		this.player = player;
		this.collegeName = collegeName;
		this.consume = consume;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		//再次检查一下名字
		int checkRlt = CollegeService.getInstance().checkCollegeName(collegeName);
		if(checkRlt != 0){
			result.put("res", checkRlt);
			HawkLog.logPrintln("CollegeCreateRpcInvoker onMessage checkRlt, playerId: {}, name:{},operationResult:{}", player.getId(), collegeName, checkRlt);
			return false;
		}
		int operationResult = CollegeService.getInstance().createCollege(player,collegeName);
		result.put("res", operationResult);
		HawkLog.logPrintln("CollegeCreateRpcInvoker onMessage, playerId: {}, name:{},operationResult:{}", player.getId(), collegeName, operationResult);
		return true;
	}
	
	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			consume.consumeAndPush(player, Action.CREATE_COLLEGE);
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.CREATE_COLLEGE);
			player.responseSuccess(hpCode);
			HawkLog.logPrintln("CollegeCreateRpcInvoker onComplete sucess, playerId: {}, name:{},operationResult:{}", player.getId(), collegeName, operationResult);
		} else {
			player.sendError(hpCode, operationResult, 0);
			HawkLog.logPrintln("CollegeCreateRpcInvoker onComplete failure, playerId: {}, name:{},operationResult:{}", player.getId(), collegeName, operationResult);

		}
		return true;
	}


	public Player getPlayer() {
		return player;
	}

	public ConsumeItems getConsume() {
		return consume;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
