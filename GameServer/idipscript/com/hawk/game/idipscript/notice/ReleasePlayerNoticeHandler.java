package com.hawk.game.idipscript.notice;

import org.hawk.cryption.HawkMd5;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.BillboardInfo;
import com.hawk.common.BillboardInfo.BillboardScene;
import com.hawk.common.BillboardInfo.BillboardType;
import com.hawk.common.service.BillboardService;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发布（个人）公告【可跳转游戏内拍脸图（个人）请求】 -- 10282168
 *
 * localhost:8080/script/idip/4485?NoticeType=&NoticeTitle=&Content=&UrlLink=&UrlLinkJump=&ButtonType=&StartTime=&EndTime=
 *
 * @param NoticeType     公告类型（登录前0、登录后1）
 * @param NoticeTitle    公告标题
 * @param Content        公告内容
 * @param UrlLink        拍脸图的url
 * @param UrlLinkJump    跳转url链接
 * @param ButtonType   按键类型，1：红色  2：蓝色 3：其它待扩展
 * @param StartTime    公告开始生效时间
 * @param EndTime      公告结束生效时间
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4485")
public class ReleasePlayerNoticeHandler extends IdipScriptHandler {
	
	protected static final int AFTER_LOGIN = 1;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int noticeType = request.getJSONObject("body").getIntValue("NoticeType");
		String title = request.getJSONObject("body").getString("NoticeTitle");
		String content = request.getJSONObject("body").getString("Content");
		String urlLink = request.getJSONObject("body").getString("UrlLink");
		String urlLinkJump = request.getJSONObject("body").getString("UrlLinkJump");
		int buttonType = request.getJSONObject("body").getIntValue("ButtonType");
		long startTime = request.getJSONObject("body").getLongValue("StartTime");
		long endTime = request.getJSONObject("body").getLongValue("EndTime");
		// 只能是登录后公告
		if (noticeType != AFTER_LOGIN) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "NoticeType param error");
			return result;
		}
		// UrlLink参数不能为空
		if (HawkOSOperator.isEmptyString(urlLink)) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "UrlLink param empty error");
			return result;
		}
		
		int billboardScene = BillboardScene.AFTER_LOGIN;
		int billboardType = BillboardType.IMAGE; // 默认拍脸图
		content = urlLink;
		
		BillboardInfo billboardInfo = new BillboardInfo(billboardScene, billboardType);
		billboardInfo.setId(String.format("%d-%s", billboardScene, HawkOSOperator.randomUUID()));
		
		billboardInfo.setTitle(IdipUtil.decode(title));
		billboardInfo.setContent(IdipUtil.decode(content));
		billboardInfo.setStartTime((int)startTime);
		billboardInfo.setEndTime((int)endTime);
		billboardInfo.setStatus(BillboardInfo.BillboardStatus.PUBLISHED);
		billboardInfo.setCondPlatform("*");
		billboardInfo.setCondChannelId("*");
		billboardInfo.setName(IdipUtil.decode(title));
		billboardInfo.setRedirect(IdipUtil.decode(urlLinkJump));
		billboardInfo.setButtonType(buttonType);
		
		{
			String calcMd5 = HawkMd5.makeMD5(String.format("%d-%s-%s", billboardScene, billboardInfo.getContent(), billboardInfo.getCondChannelId()));
			calcMd5 = String.format("%s:%s", player.getId(), calcMd5);
			String billboardId = BillboardService.getInstance().getBillboardIdByMD5(calcMd5);
			if (!HawkOSOperator.isEmptyString(billboardId)) {
				billboardInfo.setId(billboardId);
			} else {
				BillboardService.getInstance().setBillboardMD5(billboardInfo.getId(), calcMd5);
			}
		}
		
		BillboardService.getInstance().updatePlayerBillboard(billboardInfo, player.getId());
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		// 添加敏感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		
		return result;
	}
}
