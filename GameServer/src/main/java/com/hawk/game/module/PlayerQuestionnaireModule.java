package com.hawk.game.module;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.GameConstCfg;
import com.hawk.game.msg.SurveyNotifyMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Questionnaire.GetSurveyInfoReq;
import com.hawk.game.protocol.Questionnaire.GetSurveyInfoResp;
import com.hawk.game.protocol.Questionnaire.QuestionnaireInfo;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.QuestionnaireService;

/**
 * 调查问卷模块
 */
public class PlayerQuestionnaireModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	// 上次检测时间
	private long lastTickTime = 0;

	// 上次检测时全服问卷version
	private String lastCheckVersion = "";
	
	// 当前玩家展示的问卷id
	private int currSurveyId = 0;

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerQuestionnaireModule(Player player) {
		super(player);
		
	}
	
	
	@Override
	protected boolean onPlayerLogin() {
		// 检查是否有未读取的推送问卷
		lastCheckVersion = QuestionnaireService.getInstance().globalQuestionnaireCheck(player, lastCheckVersion);
		int surveyId = QuestionnaireService.getInstance().checkValidity(player);
		QuestionnaireInfo.Builder builder = QuestionnaireInfo.newBuilder();
		builder.setId(surveyId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_QUESTIONNAIRE_INFO_S, builder));
		currSurveyId = surveyId;
		return true;
	}

	/**
	 * 获取问卷调查问题信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.QUESTIONNAIRE_GET_SURVEY_INFO_C_VALUE)
	public boolean onGetSurveyInfo(HawkProtocol protocol) {
		GetSurveyInfoReq req = protocol.parseProtocol(GetSurveyInfoReq.getDefaultInstance());
		GetSurveyInfoResp.Builder builder = GetSurveyInfoResp.newBuilder();
		int result = QuestionnaireService.getInstance().getSurveyInfo(player, req.getId(), builder);
		if(result == Status.SysError.SUCCESS_OK_VALUE){
			sendProtocol(HawkProtocol.valueOf(HP.code.QUESTIONNAIRE_GET_SURVEY_INFO_S, builder));
			return true;
		}
		sendError(protocol.getType(), result);
		return true;
	}

	/**
	 * 检查问卷可用性
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.QUESTIONNAIRE_CHECK_C_VALUE)
	public boolean onQuestionnaireCheck(HawkProtocol protocol){
		QuestionnaireInfo req = protocol.parseProtocol(QuestionnaireInfo.getDefaultInstance());
		int result = QuestionnaireService.getInstance().onQuestionCheck(player, req);
		if(result == Status.SysError.SUCCESS_OK_VALUE){
			player.responseSuccess(HP.code.QUESTIONNAIRE_CHECK_C_VALUE);
			return true;
		}
		sendError(HP.code.QUESTIONNAIRE_CHECK_C_VALUE, result);
		return false;
	}
	
	@Override
	public boolean onTick() {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime - lastTickTime < GameConstCfg.getInstance().getQuestionnaireInterval()) {
			return false;
		}
		lastTickTime = currentTime;

		// 检查是否有未读取的全服调查问卷
		lastCheckVersion = QuestionnaireService.getInstance().globalQuestionnaireCheck(player, lastCheckVersion);
		
		return true;
	}
	
	/**
	 * 问卷调查回调
	 * @param msg
	 */
	@MessageHandler
	private void onSurveyNotify(SurveyNotifyMsg msg){
		QuestionnaireService.getInstance().onSurveyNotify(player, msg.getSurveyId());
	}
	
	
	/**
	 * 刷新当前主界面显示问卷id缓存
	 * @param surveyId
	 */
	public void updateCurrSurveyId(int surveyId){
		currSurveyId = surveyId;
	}


	public int getCurrSurveyId() {
		return currSurveyId;
	}
	
}