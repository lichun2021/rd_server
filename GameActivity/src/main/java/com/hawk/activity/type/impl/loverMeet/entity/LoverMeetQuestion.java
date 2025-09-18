package com.hawk.activity.type.impl.loverMeet.entity;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class LoverMeetQuestion{

	private int score;

	private int favor;
	
	private int questionId;
		
	private int endingId;
	
	private List<AnswerRecord> answers;
	
	public LoverMeetQuestion() {
		this.score = 0;
		this.favor = 0;
		this.questionId = 0;
		this.endingId = 0;
		this.answers = new ArrayList<>();
	}

	
	
	
	
	/**
	 * 序列化保存
	 */
	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("score", this.score);
		obj.put("favor", this.favor);
		obj.put("questionId", this.questionId);
		obj.put("endingId", this.endingId);
		JSONArray answerArr = new JSONArray();
		for(AnswerRecord record : answers){
			answerArr.add(record.serializ());
		}
		obj.put("answers", answerArr.toJSONString());
		return obj.toJSONString();
	}

	/**
	 * 反序列化
	 * 
	 * @param serialiedStr
	 */
	public void mergeFrom(String serialiedStr){
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.score = 0;
			this.favor = 0;
			this.questionId = 0;
			this.endingId = 0;
			this.answers = new ArrayList<>();
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.score = obj.getIntValue("score");
		this.favor = obj.getIntValue("favor");
		this.questionId = obj.getIntValue("questionId");
		this.endingId = obj.getIntValue("endingId");
		
		this.answers = new ArrayList<>();
		String answersStr = obj.getString("answers");
		if(!HawkOSOperator.isEmptyString(answersStr)){
			JSONArray answerArr = JSONArray.parseArray(answersStr);
			for(int i = 0;i<answerArr.size();i++){
				String answerSer = answerArr.getString(i);
				AnswerRecord answerRecord = new AnswerRecord();
				answerRecord.mergeFrom(answerSer);
				this.answers.add(answerRecord);
			}
		}
		
	}
	
	
	


	public int getScore() {
		return score;
	}


	public void setScore(int score) {
		this.score = score;
	}
	
	

	public int getFavor() {
		return favor;
	}


	public void setFavor(int favor) {
		this.favor = favor;
	}





	public int getQuestionId() {
		return questionId;
	}


	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}


	public int getEndingId() {
		return endingId;
	}


	public void setEndingId(int endingId) {
		this.endingId = endingId;
	}
	
	public void resetAnswer(){
		this.answers = new ArrayList<>();
	}
	
	public void addAnswer(int questionId,int answerId,int backId,int nextQuestion,long time){
		AnswerRecord record = new AnswerRecord();
		record.setAnswerId(answerId);
		record.setAnswerId(answerId);
		record.setBackId(backId);
		record.setNextQuestId(nextQuestion);
		record.setAnswerTime(time);
		this.answers.add(record);
	}
	
	public static LoverMeetQuestion valueOf(String serialiedStr){
		LoverMeetQuestion question = new LoverMeetQuestion();
		question.mergeFrom(serialiedStr);
		return question;
	}
	
	
	
	
}
