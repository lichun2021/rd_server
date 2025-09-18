package com.hawk.robot.response.protocol;

import com.hawk.robot.annotation.RobotResponse;

import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.SysProtocol.HPErrorCode;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.StatisticHelper;
import com.hawk.robot.action.building.ResourceCollectAction;
import com.hawk.robot.action.item.PlayerItemBuyAction;
import com.hawk.robot.action.item.PlayerItemUseAction;
import com.hawk.robot.response.RobotResponsor;

/**
 * 错误码响应
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.sys.ERROR_CODE_VALUE)
public class ErrorResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPErrorCode error = protocol.parseProtocol(HPErrorCode.getDefaultInstance());
		HP.code hpCode = HP.code.valueOf(error.getHpCode());
		Status.Error errorStatus = Status.Error.valueOf(error.getErrCode());
		String hpCodeName = hpCode == null ? String.valueOf(error.getHpCode()) : hpCode.name().toLowerCase();
		
		StatisticHelper.incErrorProtocolCnt(error.getHpCode());
		
		RobotLog.errPrintln("error code from server, playerId: {}, hpCode: {}, errorCode: {}, errorFlag: {}", 
				robotEntity.getPlayerId(), hpCodeName, 
				errorStatus == null ? error.getErrCode() : errorStatus.name().toLowerCase(), error.getErrFlag());
		switch(error.getErrCode()) {
			// 水晶不足
			case Status.Error.GOLD_NOT_ENOUGH_VALUE: {
				GameRobotApp.getInstance().executeTask(new Runnable() {
					@Override
					public void run() {
						PlayerItemUseAction.useItem(robotEntity, PlayerAttr.GOLD_VALUE);
					}
				});
				break;
			}
			// 黄金不足
			case Status.Error.GOLDORE_NOT_ENOUGH_VALUE: {
				GameRobotApp.getInstance().executeTask(new Runnable() {
					@Override
					public void run() {
						ResourceCollectAction.collectResource(robotEntity, BuildingType.ORE_REFINING_PLANT_VALUE);
						if(!PlayerItemUseAction.useItem(robotEntity, PlayerAttr.GOLDORE_VALUE) 
								&& !PlayerItemUseAction.useItem(robotEntity, PlayerAttr.GOLDORE_UNSAFE_VALUE)) {
							PlayerItemBuyAction.buyResItem(robotEntity, PlayerAttr.GOLDORE_VALUE);
							PlayerItemBuyAction.buyResItem(robotEntity, PlayerAttr.GOLDORE_UNSAFE_VALUE);
						}
					}
				});
				break;
			}
			// 石油不足
			case Status.Error.OIL_NOT_ENOUGH_VALUE: {
				GameRobotApp.getInstance().executeTask(new Runnable() {
					@Override
					public void run() {
						ResourceCollectAction.collectResource(robotEntity, BuildingType.OIL_WELL_VALUE);
						if(!PlayerItemUseAction.useItem(robotEntity, PlayerAttr.OIL_VALUE) 
								&& !PlayerItemUseAction.useItem(robotEntity, PlayerAttr.OIL_UNSAFE_VALUE)) {
							PlayerItemBuyAction.buyResItem(robotEntity, PlayerAttr.OIL_VALUE);
							PlayerItemBuyAction.buyResItem(robotEntity, PlayerAttr.OIL_UNSAFE_VALUE);
						}
					}
				});
				break;
			}
			// 铀矿不足
			case Status.Error.STEEL_NOT_ENOUGH_VALUE: {
				GameRobotApp.getInstance().executeTask(new Runnable() {
					@Override
					public void run() {
						ResourceCollectAction.collectResource(robotEntity, BuildingType.STEEL_PLANT_VALUE);
						if(!PlayerItemUseAction.useItem(robotEntity, PlayerAttr.STEEL_VALUE) 
								&& !PlayerItemUseAction.useItem(robotEntity, PlayerAttr.STEEL_UNSAFE_VALUE)) {
							PlayerItemBuyAction.buyResItem(robotEntity, PlayerAttr.STEEL_VALUE);
							PlayerItemBuyAction.buyResItem(robotEntity, PlayerAttr.STEEL_UNSAFE_VALUE);
						}
					}
				});
				break;
			}
			// 合金不足
			case Status.Error.TOMBARTHITE_NOT_ENOUGH_VALUE: {
				GameRobotApp.getInstance().executeTask(new Runnable() {
					@Override
					public void run() {
						ResourceCollectAction.collectResource(robotEntity, BuildingType.RARE_EARTH_SMELTER_VALUE);
						if(!PlayerItemUseAction.useItem(robotEntity, PlayerAttr.TOMBARTHITE_VALUE) 
								&& !PlayerItemUseAction.useItem(robotEntity, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE)) {
							PlayerItemBuyAction.buyResItem(robotEntity, PlayerAttr.TOMBARTHITE_VALUE);
							PlayerItemBuyAction.buyResItem(robotEntity, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE);
						}
					}
				});
				break;
			}
			// 体力不足
			case Status.Error.VIT_NOT_ENOUGH_VALUE: {
				GameRobotApp.getInstance().executeTask(new Runnable() {
					@Override
					public void run() {
						PlayerItemBuyAction.buyVit(robotEntity);
					}
				});
				break;
			}
			case Status.Error.CREATE_MANOR_FAILED_VALUE: {
				robotEntity.getGuildData().setMoveSee(true);
				break;
			}
			
			default:
				break;
		}
	}

}
