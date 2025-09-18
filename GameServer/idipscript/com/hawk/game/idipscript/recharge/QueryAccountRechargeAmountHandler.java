package com.hawk.game.idipscript.recharge;

import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.recharge.RechargeType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询单账号下充值及直购总金额请求 -- 10282193
 *
 * localhost:8081/idip/4535
 * 
 *  <entry name="AreaId" type="uint32" desc="服务器：微信（1），手Q（2）" test="1" isverify="true" isnull="true"/>
    <entry name="PlatId" type="uint8" desc="平台:ios(0)，安卓（1）" test="0" isverify="true" isnull="true"/>
    <entry name="OpenId" type="string" size="MAX_OPENID_LEN" desc="openid" test="732945400" isverify="false" isnull="true"/>
    <entry name="BeginTime" type="uint32" desc="开始时间" test="1" isverify="true" isnull="true"/>
    <entry name="EndTime" type="uint32" desc="结束时间" test="100" isverify="true" isnull="true"/>
    <entry name="Type" type="uint32" desc="类型：0（充值+直购）、1（充值）、2（直购）" test="1" isverify="true" isnull="true"/>
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4535")
public class QueryAccountRechargeAmountHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int beginTime = request.getJSONObject("body").getIntValue("BeginTime");
		int endTime = request.getJSONObject("body").getIntValue("EndTime");
		int rechargeType = request.getJSONObject("body").getIntValue("Type");
		String openid = request.getJSONObject("body").getString("OpenId");
		int diamonds = 0;
		try {
			diamonds = getAccountRechargeAmount(openid, rechargeType, beginTime, endTime);
		}catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "server exception");
			result.getBody().put("Amount", 0);
			return result;
		}
		
		result.getBody().put("Amount", diamonds);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 查询充值总金条数
	 * @param openid
	 * @param rechargeType 类型：0（充值+直购）、1（充值）、2（直购）
	 * @return
	 */
	private int getAccountRechargeAmount(String openid, int rechargeType, int beginTime, int endTime) {
		List<RechargeInfo> rechargeInfos = RedisProxy.getInstance().getAllRechargeInfoByOpenid(openid);
		int sumGold = 0;
		for(RechargeInfo info : rechargeInfos){
			if (rechargeType != 0 && rechargeType != info.getType()) {
				continue;
			}
			int rechargeTime = info.getTime();
			if (rechargeTime < beginTime || rechargeTime > endTime) {
				continue;
			}
			
			int count = info.getCount();
			if (info.getType() == RechargeType.GIFT) {
				PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, info.getGoodsId());
				count = cfg != null ? cfg.getPayRMB()/10 : count;
			}
			
			sumGold += count;
		}
		
		return sumGold;
	}
	
}
