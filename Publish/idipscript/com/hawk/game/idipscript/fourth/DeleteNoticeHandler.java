package com.hawk.game.idipscript.fourth;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.service.BillboardService;
import com.hawk.game.GsConfig;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 删除公告
 *
 * localhost:8080/script/idip/4181?NoticeId=
 *
 * @param NoticeId   公告ID
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4181")
public class DeleteNoticeHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		String noticeId = request.getJSONObject("body").getString("NoticeId");
		try {
			BillboardService.getInstance().deleteBillboard(noticeId, GsConfig.getInstance().getServerId());
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
		} catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "delete notice failed");
			HawkLog.errPrintln("idip delete notice failed, noticeId: {}", noticeId);
		} 
		
		return result;
	}
}
