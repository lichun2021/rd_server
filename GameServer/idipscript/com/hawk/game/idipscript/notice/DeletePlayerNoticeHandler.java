package com.hawk.game.idipscript.notice;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.service.BillboardService;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 删除（个人）公告【删除个人可跳转游戏内拍脸图请求】 -- 10282169
 *
 * localhost:8080/script/idip/4487?NoticeId=
 *
 * @param NoticeId   公告ID
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4487")
public class DeletePlayerNoticeHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String noticeId = request.getJSONObject("body").getString("NoticeId");
		try {
			BillboardService.getInstance().deletePlayerBillboard(noticeId, player.getId());
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
