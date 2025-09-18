package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsConfig;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.PushSurveyCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.PayStateEntity;
import com.hawk.game.entity.QuestionnaireEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.PlayerQuestionnaireModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Questionnaire.GetSurveyInfoResp;
import com.hawk.game.protocol.Questionnaire.QuestionnaireInfo;
import com.hawk.game.protocol.Questionnaire.SurveyInfo;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.QuestionnaireConst;
import com.hawk.log.Action;
import com.hawk.log.Source;


/**
 * 问卷系统管理类
 * 
 * @author julia
 *
 */
public class QuestionnaireService extends HawkAppObj {
	static Logger logger = LoggerFactory.getLogger("Server");
	

	/**
	 * 单例对象
	 */
	private static QuestionnaireService instance = null;
	
	private static String questionVersion = "";
	
	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static QuestionnaireService getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	public QuestionnaireService(HawkXID xid) {
		super(xid);
		// 设置实例
		instance = this;
	}

	/**
	 * 初始化问卷信息
	 * @return
	 */
	public boolean init() {
		// 注册全服问卷版本号周期性更新检测
		int upatePeriod = GameConstCfg.getInstance().getQuestionnaireInterval();
		addTickable(new HawkPeriodTickable(upatePeriod, upatePeriod) {
			@Override
			public void onPeriodTick() {
				String version = LocalRedis.getInstance().getQuestionnaireVersion();
				if(!HawkOSOperator.isEmptyString(version) && !version.equals(questionVersion)){
					questionVersion = version;
				}
			}
		});
		return true;
	}
	
	/**
	 * 刷新检测当前玩家所有问卷的有效性,返回最新问卷id 
	 * @param player
	 */
	public int checkValidity(Player player) {
		int pushSurveyId = 0;
		QuestionnaireEntity entity = player.getData().getQuestionnaireEntity();
		if (entity == null) {
			return pushSurveyId;
		}
		String pageSurveys = entity.getPageSurveys();
		if (HawkOSOperator.isEmptyString(pageSurveys)) {
			return pushSurveyId;
		}
		int currentTime = HawkTime.getSeconds();

		// 检测移除主界面需要展示的过期问卷
		List<int[]> pageSurveyList = convertStringToPageSurveys(pageSurveys);
		List<Integer> mailSurveys = convertStringToSurveys(entity.getMailSurveys());
		List<int[]> overTimeList = new ArrayList<>();
		List<int[]> invalidList = new ArrayList<>();
		// 当前存在的全服问卷信息
		List<SurveyInfo.Builder> surveyInfos = GlobalData.getInstance().getAllGlobalQuestionnaire();
		
		// 检测界面显示的问卷有效性
		for (int[] pageSurvey : pageSurveyList) {
			int surveyId = pageSurvey[0];
			int validityTimes = pageSurvey[1];
			PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
			// 如果问卷过期,则加入过期列表
			if (currentTime >= validityTimes) {
				overTimeList.add(pageSurvey);
			}
			//配置不存在/全服问卷id不存在,则移除
			if( cfg == null || (cfg.getPushType() == QuestionnaireConst.PUSHTYPE_GM && !isGlobalSurveyExist(surveyId, surveyInfos))){
				invalidList.add(pageSurvey);
			}
		}
		pageSurveyList.removeAll(overTimeList);
		pageSurveyList.removeAll(invalidList);
		
		// 如有过期问卷,则进行刷新
		if (overTimeList.size() > 0) {
			List<Integer> overdueSurveys = convertStringToSurveys(entity.getOverdueSurveys());
			for (int[] pageSurvey : overTimeList) {
				overdueSurveys.add(pageSurvey[0]);
			}
			entity.setOverdueSurveys(convertSurveysToDbString(overdueSurveys));
		}
		// 主界面显示列表有更新
		if (overTimeList.size() > 0 || invalidList.size() > 0) {
			entity.setPageSurveys(convertPageSurveysToDbString(pageSurveyList));
		}
		
		//检测邮件发送的问卷有效性
		List<Integer> invalidIdList = new ArrayList<>();
		for(int surveyId : mailSurveys){
			PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
			//配置不存在/全服问卷id不存在,则移除
			if( cfg == null || (cfg.getPushType() == QuestionnaireConst.PUSHTYPE_GM && !isGlobalSurveyExist(surveyId, surveyInfos))){
				invalidIdList.add(surveyId);
			}
		}
		mailSurveys.removeAll(invalidIdList);
		
		// 已发送邮件问卷列表有变更
		if(invalidIdList.size()>0){
			entity.setMailSurveys(convertSurveysToDbString(mailSurveys));
		}

		// 获取最新调查问卷的id
		if (pageSurveyList.size() > 0) {
			pushSurveyId = (int) pageSurveyList.get(pageSurveyList.size() - 1)[0];
		}
		
		for (int i = pageSurveyList.size() - 1; i >= 0; i--) {
			pushSurveyId = (int) pageSurveyList.get(i)[0];
			// 配置检测
			if (HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, pushSurveyId) != null) {
				return pushSurveyId;
			}
		}
		
		return 0;
	}

	/**
	 * 检查问卷可用性检测
	 * @param player
	 * @param req
	 * @return
	 */
	public int onQuestionCheck(Player player, QuestionnaireInfo req) {
		QuestionnaireEntity entity = player.getData().getQuestionnaireEntity();
		int surveyId = req.getId();
		PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
		return questionnaireCheck(entity, surveyId, cfg);
	}
	
	/**
	 * 推送全服调查问卷
	 * @param surveyId
	 * @return
	 */
	public int pushGlobalQuestionnaire(int surveyId) {
		PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
		if (cfg == null) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		if (cfg.getPushType() != QuestionnaireConst.PUSHTYPE_GM) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		if (!cfg.IsOpen()) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}

		// 白名单服务器不做渠道检查
		if (!GameConstCfg.getInstance().getPushSurveyGrayServerList().contains(GsConfig.getInstance().getServerId())) {
			if (cfg.getChannel() != 0 && cfg.getChannel() != Integer.parseInt(GsConfig.getInstance().getAreaId())) {
				HawkLog.errPrintln("push global survey failed, surveyId: {}", surveyId);
				return Status.SysError.CONFIG_ERROR_VALUE;
			}
		}
		
		int currentSecond = HawkTime.getSeconds();
		SurveyInfo.Builder builder = SurveyInfo.newBuilder();
		builder.setSurveyId(surveyId);
		builder.setCreateTime(currentSecond);
		GlobalData.getInstance().addGlobalQuestionnaire(builder);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 移除全服问卷
	 * @param surveyId
	 * @return
	 */
	public void removeGlobalQuestionaire(int surveyId){
		GlobalData.getInstance().removeGlobalQuestionaire(surveyId);
	}
	
	/**
	 *	根据openid推送调查问卷 
	 * @param surveyId
	 * @param openid
	 * @return
	 */
	public int pushQuestionnaireByOpenid(int surveyId, String openid) {
		PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
		if (cfg == null) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		if (cfg.getPushType() != QuestionnaireConst.PUSHTYPE_GM) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		if (!cfg.IsOpen()) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		int currentSecond = HawkTime.getSeconds();
		SurveyInfo.Builder builder = SurveyInfo.newBuilder();
		builder.setSurveyId(surveyId);
		builder.setCreateTime(currentSecond);

		List<String> playerIds = GlobalData.getInstance().getPlayerIdsByOpenid(openid);
		for (String playerId : playerIds) {
			try {
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				QuestionnaireEntity entity = player.getData().getQuestionnaireEntity();
				globalQuestionnaireCheck(player, entity, builder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 推送问卷检测 
	 * @param player
	 * @return
	 */
	
	public String globalQuestionnaireCheck(Player player, String lastCheckVersion) {
		List<SurveyInfo.Builder> surveyInfos = GlobalData.getInstance().getAllGlobalQuestionnaire();
		if(HawkOSOperator.isEmptyString(questionVersion) || surveyInfos == null){
			return questionVersion;
		}
		
		// 玩家所有问卷的有效性检测和刷新
		int newlyId = checkValidity(player);
		
		PlayerQuestionnaireModule module = player.getModule(GsConst.ModuleType.QUESTIONNAIRE);
		int currSurveyId = module.getCurrSurveyId();
		
		// 当前主界面显示的问卷id失效
		if (currSurveyId != 0 && newlyId != currSurveyId) {
			QuestionnaireInfo.Builder builder = QuestionnaireInfo.newBuilder();
			builder.setId(currSurveyId);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUESTIONNAIRE_SUBMIT_S, builder));
			module.updateCurrSurveyId(0);
		}
		
		boolean needUpdate = false;
		QuestionnaireEntity entity = player.getData().getQuestionnaireEntity();
		for (SurveyInfo.Builder surveyInfo : surveyInfos) {
			long createTime = surveyInfo.getCreateTime() * 1000L;
			// 上次检测时间大于问卷创建时间,则跳过检测
			if (entity.getLastCheckTime() > createTime) {
				continue;
			}
	
			if (globalQuestionnaireCheck(player, entity, surveyInfo)) {
				needUpdate = true;
			}
		}
		// 如果玩家推送问卷/全服推送问卷size更新,则刷新检测时间
		if (needUpdate || !questionVersion.equals(lastCheckVersion)) {
			entity.setLastCheckTime(HawkTime.getMillisecond());
		}
		return questionVersion;
	}

	/**
	 * 触发式调查问卷检测
	 * @param player
	 * @param conditionType
	 * @param conditionId
	 */
	public void questionaireCheck(Player player, int conditionType, int conditionId) {
		QuestionnaireEntity entity = player.getData().getQuestionnaireEntity();

		// 没有该触发类型的调查问卷
		List<Integer> triggerCopy = getTrigger(conditionType);
		if (triggerCopy.isEmpty()) {
			return;
		}
		
		List<Integer> pushedList = new ArrayList<>();
		pushedList.addAll(getAvailableList(entity));
		pushedList.addAll(convertStringToSurveys(entity.getFinishedSurveys()));
		for (int pushed : pushedList) {
			if (triggerCopy.indexOf(pushed) != -1) {
				triggerCopy.remove(triggerCopy.indexOf(pushed));
			}
		}
	
		if (triggerCopy.size() == 0) {
			return;
		}
	
		for (int surveyId : triggerCopy) {
			checkAndUpdateQuestionaire(player, surveyId, conditionId);
		}
	}
	
	/**
	 * 根据触发类型获取触发条件
	 * @param conditionType
	 * @return
	 */
	public List<Integer> getTrigger(int conditionType){
		List<Integer> trigger = new ArrayList<>();
		ConfigIterator<PushSurveyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(PushSurveyCfg.class);
		for(PushSurveyCfg cfg : cfgs){
			// 后台推送类问卷不加入触发map
			if (cfg.getPushType() == QuestionnaireConst.PUSHTYPE_GM) {
				continue;
			}
			// 未开启问卷不加入触发map
			if (!cfg.IsOpen()) {
				continue;
			}
			int surveyId = cfg.getId();
			List<int[]> conditions = cfg.getConditionList();
			for (int[] condition : conditions) {
				int type = condition[0];
				if (type == conditionType && trigger.indexOf(surveyId) == -1) {
					trigger.add(surveyId);
				}
			}
		}
		return trigger;
	}

	/**
	 * 检测问卷是否可回答
	 * @param entity
	 * @param surveyId
	 * @param cfg
	 * @return
	 */
	private int questionnaireCheck(QuestionnaireEntity entity, int surveyId, PushSurveyCfg cfg) {
		// 问卷配置不存在
		if(cfg == null){
			return Status.Error.QUESTIONNAIRE_DATA_ERROR_VALUE;
		}
		
		if(entity == null){
			return Status.Error.QUESTIONNAIRE_NOT_MEET_VALUE;
		}
		
		List<Integer> finishedList = convertStringToSurveys(entity.getFinishedSurveys());
		// 该问卷已提交
		if(finishedList.indexOf(surveyId) != -1){
			return Status.Error.QUESTIONNAIRE_ALREADY_COMMIT_VALUE;
		}
		
		// 玩家不满足问卷条件,问卷无效
		if(!availableCheck(entity, surveyId)){
			return  Status.Error.QUESTIONNAIRE_NOT_MEET_VALUE; 
		}
		
		List<SurveyInfo.Builder> surveyInfos = GlobalData.getInstance().getAllGlobalQuestionnaire();
		// 全服调查问卷已不存在
		if (cfg.getPushType() == QuestionnaireConst.PUSHTYPE_GM && !isGlobalSurveyExist(surveyId, surveyInfos)) {
			return Status.Error.QUESTIONNAIRE_INVALID_VALUE;
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 获取主界面展示问卷id列表
	 * @param pageSurveys
	 * @return
	 */
	private List<Integer> getPageSurveyIds(String pageSurveys){
		List<int[]> pageSurveyList = convertStringToPageSurveys(pageSurveys);
		List<Integer> pageSurveyIds = new ArrayList<>();
		for(int[] array : pageSurveyList){
			pageSurveyIds.add(array[0]);
		}
		return pageSurveyIds;
	}
	
	
	/**
	 * 刷新玩家问卷信息
	 * @param entity
	 * @param surveyId
	 */
	private void updateQuestionnaireInfo(QuestionnaireEntity entity, int surveyId) {
		List<Integer> mailSurveys = convertStringToSurveys(entity.getMailSurveys());
		// 从邮件问卷列表中移除
		if(mailSurveys.indexOf(surveyId)!= -1){
			mailSurveys.remove(mailSurveys.indexOf(surveyId));
			entity.setMailSurveys(convertSurveysToDbString(mailSurveys));
		}
		
		List<Integer> overdueSurveys = convertStringToSurveys(entity.getOverdueSurveys());
		// 从过期问卷列表中移除
		if(overdueSurveys.indexOf(surveyId)!= -1){
			overdueSurveys.remove(overdueSurveys.indexOf(surveyId));
			entity.setOverdueSurveys(convertSurveysToDbString(overdueSurveys));
		}
		
		// 从主界面问卷列表中移除
		List<int[]> pageSurveyList = convertStringToPageSurveys(entity.getPageSurveys());
		List<int[]> needRemove = new ArrayList<>();
		for (int[] pageSurvey : pageSurveyList) {
			int pageSurveyId = pageSurvey[0];
			if (pageSurveyId == surveyId) {
				needRemove.add(pageSurvey);
			}
		}
		pageSurveyList.removeAll(needRemove);
		
		entity.setPageSurveys(convertPageSurveysToDbString(pageSurveyList));
		List<Integer> finishedList = convertStringToSurveys(entity.getFinishedSurveys());
		finishedList.add(surveyId);
		entity.setFinishedSurveys(convertSurveysToDbString(finishedList));
	}

	/**
	 * 判定是否收到过该问卷
	 * @param mailSurveys
	 * @param pageSurveyList
	 * @param surveyId
	 * @return
	 */
	private boolean availableCheck(QuestionnaireEntity entity, int surveyId){
		// 判定主界面,邮件及过期问卷id中是否有对应问卷id
		List<Integer> availableList = getAvailableList(entity);
		if(availableList.indexOf(surveyId) != -1){
			return true;
		}
		return availableList.indexOf(surveyId) != -1;
	}
	
	
	/**
	 * 获取已推送且未提交的问卷id列表
	 * @param entity
	 * @return
	 */
	private List<Integer> getAvailableList(QuestionnaireEntity entity) {
		List<Integer> availableList = new ArrayList<>();
		availableList.addAll(convertStringToSurveys(entity.getMailSurveys()));
		availableList.addAll(convertStringToSurveys(entity.getOverdueSurveys()));
		availableList.addAll(getPageSurveyIds(entity.getPageSurveys()));
		return availableList;
	}
	
	/**
	 * 触发型检测,问卷创建时间设置为当前时间(目前废弃)
	 * @param player
	 * @param surveyId
	 * @param conditionId
	 * @return
	 */
	private boolean checkAndUpdateQuestionaire(Player player, int surveyId, int conditionId) {
		return checkAndUpdateQuestionaire(player, surveyId, conditionId, HawkTime.getSeconds());
	}
	
	
	/**
	 * 检测并修改问卷调查信息
	 * @param player
	 * @param surveyId
	 */
	private boolean checkAndUpdateQuestionaire(Player player, int surveyId, int conditionId, int createTime) {
		PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
		if (cfg == null) {
			return false;
		}
		
		List<int[]> conditions = cfg.getConditionList();
		boolean meet = true;
		for (int[] condition : conditions) {
			if (!conditionCheck(player, condition, conditionId)) {
				meet = false;
				break;
			}
		}
		
		// 条件不符合
		if (!meet) {
			return false;
		}
		
		QuestionnaireEntity entity = player.getData().getQuestionnaireEntity();
		List<Integer> pushShowList = cfg.getPushShowList();
		for (int pushShow : pushShowList) {
			switch (pushShow) {
			case QuestionnaireConst.PUSH_SHOW_MAIL:
				List<Integer> mailSurveList = convertStringToSurveys(entity.getMailSurveys());
				mailSurveList.add(surveyId);
				entity.setMailSurveys(convertSurveysToDbString(mailSurveList));
				// 发送问卷邮件
				SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.QUESTIONNAIRE_NOTICE).addSubTitles(player.getName()).addContents(surveyId).build());
				break;
			// 推送主页显示问卷
			case QuestionnaireConst.PUSH_SHOW_PAGE:
				List<int[]> pageSurveyList = convertStringToPageSurveys(entity.getPageSurveys());
				int[] survey = new int[2];
				survey[0] = surveyId;
				survey[1] = createTime + cfg.getValidityTime();
				pageSurveyList.add(survey);
				entity.setPageSurveys(convertPageSurveysToDbString(pageSurveyList));
				
				// 如果问卷未过期,则添加到主界面列表并推送
				if (HawkTime.getSeconds() < survey[1]) {
					QuestionnaireInfo.Builder builder = QuestionnaireInfo.newBuilder();
					builder.setId(surveyId);
					player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_QUESTIONNAIRE_INFO_S, builder));
					// 刷新主界面问卷id
					PlayerQuestionnaireModule module = player.getModule(GsConst.ModuleType.QUESTIONNAIRE);
					module.updateCurrSurveyId(surveyId);
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

	/**
	 * 问卷条件检测
	 * @param player
	 * @param condition
	 * @param conditionId (仅供购买道具触发检测时使用)
	 * @return
	 */
	private boolean conditionCheck(Player player, int[] condition, int conditionId) {
		int type = condition[0];
		int id = condition[1];
		int num = condition[2];
		switch (type) {
		// 购买指定道具
		case QuestionnaireConst.CONDITION_BUILDING_BUY_ITEM:
			if (conditionId == id) {
				return true;
			}
			break;
		// 指定建筑达到指定等级
		case QuestionnaireConst.CONDITION_BUILDING_LEVEL:
			List<BuildingBaseEntity> buildings = player.getData().getBuildingListByType(BuildingType.valueOf(id));
			if (buildings == null || buildings.size() == 0) {
				return false;
			}
			for (BuildingBaseEntity building : buildings) {
				BuildingCfg conf = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
				if (conf == null) {
					continue;
				}
				if (conf.getLevel() >= num) {
					return true;
				}
			}
			break;
		// 达到指定充值额度
		case QuestionnaireConst.CONDITION_BUILDING_RECHARGE:
			PayStateEntity stateEntity = player.getData().getPayStateEntity();
			if (stateEntity != null && stateEntity.getRechargeGold() >= num) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	/**
	 * 全服推送邮件处理
	 * @param player
	 * @param entity
	 * @param questionnaire
	 * @return
	 */
	private boolean globalQuestionnaireCheck(Player player, QuestionnaireEntity entity, SurveyInfo.Builder surveyInfo) {
		// 已检测过
		int surveyId = surveyInfo.getSurveyId();
		PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
		if (cfg == null) {
			return false;
		}
		List<Integer> pushedList = new ArrayList<>();
		pushedList.addAll(getAvailableList(entity));
		pushedList.addAll(convertStringToSurveys(entity.getFinishedSurveys()));

		// 已推送过相同问卷
		if (pushedList.indexOf(surveyId) != -1) {
			return false;
		}
		return checkAndUpdateQuestionaire(player, surveyId, 0, surveyInfo.getCreateTime());
	}
	
	

	/**
	 * 解析问卷id字符串
	 * @param surveys
	 * @return
	 */
	private List<Integer> convertStringToSurveys(String surveys){
		List<Integer> surveyList = new ArrayList<>();
		if(HawkOSOperator.isEmptyString(surveys)){
			return surveyList;
		}
		String[] surveyStrs = surveys.split(",");
		for (String surveyStr : surveyStrs) {
			surveyList.add(Integer.parseInt(surveyStr));
		}
		return surveyList;
	}

	/**
	 * 生成存储字符串
	 * @param surveyList
	 * @return
	 */
	private String convertSurveysToDbString(List<Integer> surveyList){
		StringBuilder builder = new StringBuilder();
		if(surveyList != null){
			for (int i = 0; i < surveyList.size(); i++) {
				if (i > 0) {
					builder.append(",");
				}
				builder.append(surveyList.get(i));
			}
		}
		return builder.toString();
	}

	/**
	 * 解析主界面问卷信息
	 * @param pageSurveyInfo
	 * @return
	 */
	private List<int[]> convertStringToPageSurveys(String pageSurveys) {
		List<int[]> pageSurveyList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(pageSurveys)) {
			return pageSurveyList;
		}
		String[] surveyStrs = pageSurveys.split(",");
		for (String surveyStr : surveyStrs) {
			String[] strs = surveyStr.split("_");
			int[] survey = new int[2];
			survey[0] = Integer.parseInt(strs[0]);
			survey[1] = Integer.parseInt(strs[1]);
			pageSurveyList.add(survey);
		}
		return pageSurveyList;
	}

	/**
	 * 生成存储字符串
	 * @param pageSurveyList
	 * @return
	 */
	private String convertPageSurveysToDbString(List<int[]> pageSurveyList) {
		StringBuilder builder = new StringBuilder();
		if (pageSurveyList != null && pageSurveyList.size() != 0) {
			// 根据问卷到期时间排序
			pageSurveyList.sort(new Comparator<int[]>() {
				@Override
				public int compare(int[] arg0, int[] arg1) {
					int time0 = arg0[1];
					int time1 = arg1[1];
					int gap = time1 - time0;
					if (gap == 0) {
						int id0 = arg0[0];
						int id1 = arg1[0];
						gap = id1 - id0;
					}
					return -gap;
				}
			});
	
			for (int i = 0; i < pageSurveyList.size(); i++) {
				if (i > 0) {
					builder.append(",");
				}
				int[] pageSurver = pageSurveyList.get(i);
				builder.append(pageSurver[0]).append("_").append(pageSurver[1]);
			}
		}
		return builder.toString();
	}
	
	/**
	 * 构建调查问卷信息
	 * @param surveyId
	 * @return
	 */
	public int getSurveyInfo(Player player, int surveyId, GetSurveyInfoResp.Builder builder) {
		PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
		QuestionnaireEntity entity = player.getData().getQuestionnaireEntity();
		// 检测问卷是否可回答
		int result = questionnaireCheck(entity, surveyId, cfg);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			return result;
		}
		builder.setEnterTips(cfg.getEnterTips());
		builder.setSurveyUrl(getUrlTwo(player, cfg));
		
		// 如果未开启问卷回调,则打开问卷时发奖
		if (!GameConstCfg.getInstance().isSurveyNotifyOpen()) {
			onSurveyNotify(player, surveyId);
		}
		return result;
	}
	
	/**
	 * 问卷调查提交,通知记录和发奖
	 * @param player
	 * @param surveyId
	 */
	public void onSurveyNotify(Player player, int surveyId){
		PushSurveyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushSurveyCfg.class, surveyId);
		QuestionnaireEntity entity = player.getData().getQuestionnaireEntity();
		
		// 检测问卷是否可答
		int result = questionnaireCheck(entity, surveyId, cfg);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			return;
		}
		// 刷新玩家调查问卷信息
		updateQuestionnaireInfo(entity, surveyId);
		
		// 奖励发放
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, cfg.getAward());
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.appendAward(awardCfg.getRandomAward());
		
		// 发送邮件---问卷奖励发放
		SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.QUESTIONNAIRE_REWARD)
				.addSubTitles(player.getName())
				.setRewards(awardItems.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
		
		// 回调统计
		RedisProxy.getInstance().addSurveyNotifyCnt(surveyId);
		
		QuestionnaireInfo.Builder builder = QuestionnaireInfo.newBuilder();
		builder.setId(surveyId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.QUESTIONNAIRE_SUBMIT_S, builder));
		PlayerQuestionnaireModule module = player.getModule(GsConst.ModuleType.QUESTIONNAIRE);
		module.updateCurrSurveyId(0);
		BehaviorLogger.log4Service(player, Source.UNKNOWN_SOURCE, Action.QUESTION_SUBMIT, Params.valueOf("surveyId", surveyId));
	}
	
	/**
	 * 拼接问卷url(老版,截止2020/09/03)
	 * @param player
	 * @param cfg
	 * @return
	 */
	private String getUrl(Player player, PushSurveyCfg cfg){
		StringBuilder sb = new StringBuilder();
		sb.append(cfg.getUrl()).append("&openid=").append(player.getOpenId())
		.append("&rid=").append(String.format("areaid:%s@%s", GsConfig.getInstance().getAreaId(), player.getId()))
		.append("&serverid=").append(GsConfig.getInstance().getServerId());
		return sb.toString();
	}
	
	/**
	 * 拼接问卷url(新版,2020/09/03-)
	 * @param player
	 * @param cfg
	 * @return
	 */
	private String getUrlTwo(Player player, PushSurveyCfg cfg) {
		StringBuilder sb = new StringBuilder();
		sb.append(cfg.getUrl()).append("&openid=")
				.append(player.getOpenId())
				.append("&rid=")
				.append(String.format("%s@%s@%s", GsConfig.getInstance().getAreaId(), player.getMainServerId(), player.getId()))
				.append("&serverid=")
				.append(GsConfig.getInstance().getServerId());
		return sb.toString();
	}
	
	/**
	 * 判定该全服问卷id是否存在
	 * @param surveyId
	 * @param surveyInfos
	 * @return
	 */
	public boolean isGlobalSurveyExist(int surveyId, List<SurveyInfo.Builder> surveyInfos) {
		for (SurveyInfo.Builder info : surveyInfos) {
			if (info.getSurveyId() == surveyId) {
				return true;
			}
		}
		return false;
	}
	
	
}
