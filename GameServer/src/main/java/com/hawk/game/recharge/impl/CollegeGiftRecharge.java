package com.hawk.game.recharge.impl;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.module.college.cfg.CollegeConstCfg;
import com.hawk.game.module.college.cfg.CollegePurchaseCfg;
import com.hawk.game.module.college.entity.CollegeMemberEntity;
import com.hawk.game.module.college.entity.CollegeMemberGiftEntity;
import com.hawk.game.msg.CollegeGiftBuyMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.protocol.Status;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.service.college.CollegeService;

/**
 * 军事学院直购礼包 
 * @author lating
 *
 */
public class CollegeGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		if (!CollegeConstCfg.getInstance().getIsSysterOpen()) {
			HawkLog.errPrintln("CollegeGiftRecharge MODULE_CLOSED, playerId: {}, openId: {}", player.getId(), player.getOpenId());
			player.sendError(protocol, Status.SysError.MODULE_CLOSED_VALUE, 0);
			return false;
		}
		// 跨服期间该操作禁用
		if(player.isCsPlayer()){
			HawkLog.errPrintln("CollegeGiftRecharge cross server, playerId: {}, openId: {}", player.getId(), player.getOpenId());
			player.sendError(protocol, Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE, 0);
			return false;
		}
		
		CollegeMemberEntity entity = player.getData().getCollegeMemberEntity();
		String collegeId = entity.getCollegeId();
		//当前未加入学院，不能购买
		if(HawkOSOperator.isEmptyString(collegeId)){
			HawkLog.errPrintln("CollegeGiftRecharge check failed, member not in college, playerId: {}", player.getId());
			player.sendError(protocol, Status.Error.MEMBER_NOT_IN_COLLEGE_VALUE, 0);
			return false;
		}
		//退出又重新加入后7天内不能兑换
		if (entity.getQuitTime() > 0 && entity.getJoinTime() > 0 && HawkTime.getMillisecond() - entity.getJoinTime() < HawkTime.DAY_MILLI_SECONDS * 7) {
			HawkLog.errPrintln("CollegeGiftRecharge check failed, cannot buy in 7 days, playerId: {}, quitTime: {}, joinTime: {}", player.getId(), entity.getQuitTime(), entity.getJoinTime());
			player.sendError(protocol, Status.Error.COLLEGE_OPER_FORBID_7_DAYS_VALUE, 0);
			return false;
		}

		int giftId = CollegePurchaseCfg.getCfgIdByPayId(giftCfg.getId());
		int buyCount = 1;
		CollegeMemberGiftEntity giftDataEntity = entity.getGiftData();
		//该礼包当前未上架，不能购买 
		if(giftDataEntity.insell(giftId)){
			HawkLog.errPrintln("CollegeGiftRecharge check failed, giftData of giftId not exist, playerId: {}, giftId: {}", player.getId(), giftId);
			player.sendError(protocol, Status.Error.COLLEGE_GIFT_CANNOT_SELL_VALUE, 0);
			return false;
		}
		int collegeLevel = CollegeService.getInstance().getCollegeLevel(player.getCollegeId());
		CollegePurchaseCfg collegeGiftCfg = HawkConfigManager.getInstance().getConfigByKey(CollegePurchaseCfg.class, giftId);
		if (collegeGiftCfg.getLimitLevel() > collegeLevel) {
			player.sendError(protocol, Status.SysError.PARAMS_INVALID_VALUE,0);
			return false;
		}
        int boughtCount = giftDataEntity.getBuyCount(giftId);
        //该礼包购买数量已达上限
        if (boughtCount + buyCount > collegeGiftCfg.getTimes()) {
        	HawkLog.errPrintln("CollegeGiftRecharge check failed, buy times limit, playerId: {}, giftId: {}, oldCount: {}, addCount: {}", player.getId(), giftId, boughtCount, buyCount);
        	player.sendError(protocol, Status.Error.COLLEGE_GIFT_BOUGHT_TIMES_LIMIT_VALUE, 0);
            return false;
        }
        
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		int cfgId = CollegePurchaseCfg.getCfgIdByPayId(giftCfg.getId());
		GsApp.getInstance().postMsg(player, new CollegeGiftBuyMsg(cfgId));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.COLLEGE_GIFT;
	}

}
