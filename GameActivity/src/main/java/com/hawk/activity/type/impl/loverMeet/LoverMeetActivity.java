package com.hawk.activity.type.impl.loverMeet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.LoverMeetEndingCountEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.loverMeet.cfg.LoverMeetAchieveCfg;
import com.hawk.activity.type.impl.loverMeet.cfg.LoverMeetAnswerCfg;
import com.hawk.activity.type.impl.loverMeet.cfg.LoverMeetEndingCfg;
import com.hawk.activity.type.impl.loverMeet.cfg.LoverMeetKVCfg;
import com.hawk.activity.type.impl.loverMeet.cfg.LoverMeetQuestionCfg;
import com.hawk.activity.type.impl.loverMeet.entity.LoverMeetEntity;
import com.hawk.activity.type.impl.loverMeet.entity.LoverMeetQuestion;
import com.hawk.game.protocol.Activity.PBLoverMeetInfoResp;
import com.hawk.game.protocol.Activity.PBLoverQuestionAnswerResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 7夕相遇
 * @author che
 *
 */
public class LoverMeetActivity extends ActivityBase  implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public LoverMeetActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.LOVER_MEET_ACTIVITY;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LoverMeetActivity activity = new LoverMeetActivity(
				config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LoverMeetEntity> queryList = HawkDBManager.getInstance()
				.query("from LoverMeetEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LoverMeetEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LoverMeetEntity entity = new LoverMeetEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<LoverMeetEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		LoverMeetEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	
	//初始化成就
	private void initAchieve(String playerId){
		Optional<LoverMeetEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		LoverMeetEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<LoverMeetAchieveCfg> configIterator = HawkConfigManager.getInstance().
				getConfigIterator(LoverMeetAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			LoverMeetAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
		
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.LOVER_MEET_INIT, () -> {
				this.initAchieve(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	/**
	 * 开始答题
	 * @param playerId
	 */
	public void questionStart(String playerId){
		Optional<LoverMeetEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		LoverMeetEntity entity = optional.get();
		if(this.gameOver(entity)){
			logger.info("LoverMeetActivity questionStart game over, playerId: {}",playerId);
			return;
		}
		LoverMeetQuestion question = entity.getQuestion();
		if(question.getQuestionId() != 0){
			logger.info("LoverMeetActivity questionStart questionId err, playerId: {},questionId:{}",playerId,question.getQuestionId());
			return;
		}
		LoverMeetKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoverMeetKVCfg.class);
		
		List<Integer> qlist = cfg.getStartQuestionList();
		int qid = qlist.get(0);
		if(qlist.size() > 1){
			int IndexMax =  qlist.size() - 1;
			int random = HawkRand.randInt(0, IndexMax);
			qid = qlist.get(random);
		}
		question.setQuestionId(qid);
		question.setScore(0);
		question.setFavor(0);
		question.setEndingId(0);
		question.resetAnswer();
		entity.notifyUpdate();
		this.syncActivityInfo(playerId, entity);
		logger.info("LoverMeetActivity questionStart sucess questionId, playerId: {},questionId:{}",playerId,qid);
		
	}
	
	
	/**
	 * 回答问题
	 * @param playerId
	 * @param questionId
	 * @param answerId
	 */
	public void questionAnswer(String playerId,int questionId,int answerId){
		Optional<LoverMeetEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		LoverMeetEntity entity = optional.get();
		if(this.gameOver(entity)){
			logger.info("LoverMeetActivity questionAnswer game over, playerId: {}",playerId);
			return;
		}
		LoverMeetQuestion question = entity.getQuestion();
		if(question.getQuestionId() == 0){
			logger.info("LoverMeetActivity questionAnswer questionId err, playerId: {},questionId:{}",playerId,0);
			return;
		}
		
		if(question.getQuestionId() != questionId){
			logger.info("LoverMeetActivity questionAnswer questionId err, playerId: {},questionId:{},reqId:{}",playerId,question.getQuestionId(),questionId);
			return;
		}
		
		LoverMeetQuestionCfg qcfg = HawkConfigManager.getInstance()
				.getConfigByKey(LoverMeetQuestionCfg.class, questionId);
		List<Integer> answers = qcfg.getAnswerList(entity.getEndings());
		if(!answers.contains(answerId)){
			logger.info("LoverMeetActivity questionAnswer answerId err, playerId: {},questionId:{},answerId:{}",playerId,questionId,answerId);
			return;
		}
		LoverMeetAnswerCfg acfg = HawkConfigManager.getInstance()
				.getConfigByKey(LoverMeetAnswerCfg.class, answerId);
		LoverMeetKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoverMeetKVCfg.class);
		boolean cost = this.getDataGeter().cost(playerId,acfg.getCostItemList(), 1, Action.LOVER_MEET_ANSWER_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code2.LOVER_MEET_ANSWER_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		//加积分
		int addScore = acfg.getScore();
		int totalScore = question.getScore() + addScore;
		//好感
		int addFavor = acfg.getFavor();
		int totalFavor = question.getFavor() + addFavor;
		if(totalScore >= cfg.getEndingScore()){
			//可以结局
			LoverMeetEndingCfg ecfg = this.getEnding(acfg, totalFavor);
			question.setEndingId(ecfg.getId());
			question.setScore(cfg.getEndingScore());
			question.setFavor(totalFavor);
			question.addAnswer(questionId, answerId, 0, 0, HawkTime.getMillisecond());
			//日志
			logger.info("LoverMeetActivity questionAnswer ending, playerId: {},question:{}",playerId,question.serializ());
			//重置问题
			long curTime = HawkTime.getMillisecond();
			entity.resetQuestion();
			entity.addEnding(curTime, ecfg.getId());
			entity.notifyUpdate();
			logger.info("LoverMeetActivity questionAnswer resetQuestion, playerId: {},question:{}",playerId,question.serializ());
			//抛事件
			int endingCount = entity.getEndings().size();
			ActivityManager.getInstance().postEvent(new LoverMeetEndingCountEvent(playerId, endingCount), true);
			//发回答奖励
			this.getDataGeter().takeReward(playerId, acfg.getRewardItemList(), 
					1, Action.LOVER_MEET_ANSWER_AWARD, true);
			//发结局奖励
			this.getDataGeter().takeReward(playerId, ecfg.getRewardItemList(), 
					1, Action.LOVER_MEET_ENDING_AWARD, true);
			this.getDataGeter().logLoverMeetEnding(playerId, entity.getTermId(), ecfg.getId());
			//同步返回信息
			PBLoverQuestionAnswerResp.Builder builder = PBLoverQuestionAnswerResp.newBuilder();
			builder.setQuestionId(questionId);
			builder.setAnswer(answerId);
			builder.setBackId(0);
			builder.setEndingId(ecfg.getId());
			PlayerPushHelper.getInstance().pushToPlayer(playerId,
					HawkProtocol.valueOf(HP.code2.LOVER_MEET_ANSWER_RESP,builder));
			//同步活动信息
			this.syncActivityInfo(playerId, entity);
		}else{
			//继续发题
			int backId = 2;
			int nextQuestionId = acfg.getNextQuestion2();
			if(totalFavor >= acfg.getFavorLimit()){
				backId = 1;
				nextQuestionId = acfg.getNextQuestion1();
			}
			question.setQuestionId(nextQuestionId);
			question.setScore(totalScore);
			question.setFavor(totalFavor);
			question.addAnswer(questionId, answerId, backId,nextQuestionId, HawkTime.getMillisecond());
			entity.notifyUpdate();
			logger.info("LoverMeetActivity questionAnswer back, playerId: {},question:{}",playerId,question.serializ());
			//发回答奖励
			this.getDataGeter().takeReward(playerId, acfg.getRewardItemList(), 
					1, Action.LOVER_MEET_ANSWER_AWARD, true);
			//同步回答
			PBLoverQuestionAnswerResp.Builder builder = PBLoverQuestionAnswerResp.newBuilder();
			builder.setQuestionId(questionId);
			builder.setAnswer(answerId);
			builder.setBackId(backId);
			builder.setEndingId(0);
			PlayerPushHelper.getInstance().pushToPlayer(playerId,
					HawkProtocol.valueOf(HP.code2.LOVER_MEET_ANSWER_RESP,builder));
			//同步消息
			this.syncActivityInfo(playerId, entity);
		}
	}
	
	
	
	/**
	 * 获取结局
	 * @param acfg
	 * @param favor
	 * @return
	 */
	private LoverMeetEndingCfg getEnding(LoverMeetAnswerCfg acfg,int favor){
		List<Integer> endings = acfg.getEndingList();
		LoverMeetEndingCfg defaultEnding = null;
		for(int eId : endings){
			LoverMeetEndingCfg ecfg = HawkConfigManager.getInstance()
					.getConfigByKey(LoverMeetEndingCfg.class, eId);
			if(defaultEnding == null){
				defaultEnding = ecfg;
			}
			if(ecfg.getFavorRange1() <= favor && favor <= ecfg.getFavorRange2()){
				return ecfg;
			}
		}
		return defaultEnding;
	}
	
	
	/**
	 * 是否结束
	 * @param entity
	 * @return
	 */
	private boolean gameOver(LoverMeetEntity entity){
		LoverMeetKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoverMeetKVCfg.class);
		if(entity.getEndingCount() >= cfg.getMaxCount()){
			return true;
		}
		return false;
	}
	
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<LoverMeetEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,LoverMeetEntity entity){
		
		Set<Integer> endingSet = entity.getEndings();
		boolean gameOver = this.gameOver(entity);
		int score = entity.getQuestion().getScore();
		int questionId = entity.getQuestion().getQuestionId();
		
		PBLoverMeetInfoResp.Builder builder = PBLoverMeetInfoResp.newBuilder();
		builder.setFinishCount(entity.getEndingCount());
		builder.addAllEndings(endingSet);
		builder.setGameOver(gameOver);
		builder.setScore(score);
		builder.setQuestionId(questionId);
		if(questionId > 0){
			LoverMeetQuestionCfg qcfg = HawkConfigManager.getInstance()
					.getConfigByKey(LoverMeetQuestionCfg.class, questionId);
			List<Integer> answers = qcfg.getAnswerList(endingSet);
			builder.addAllAnswers(answers);
		}
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.LOVER_MEET_INFO_RESP,builder));
	}
	
	
	

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().
				getConfigByKey(LoverMeetAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.LOVER_MEET_TASK_REWARD;
	}
	
	
}
