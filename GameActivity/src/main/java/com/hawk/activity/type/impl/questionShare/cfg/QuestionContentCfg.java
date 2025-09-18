package com.hawk.activity.type.impl.questionShare.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/question_share/question_content.xml")
public class QuestionContentCfg extends HawkConfigBase {

	@Id
	private final int id;

	private final int answer;
	
	public QuestionContentCfg() {
		id = 0;
		answer = 0;
	}

	@Override
	protected boolean assemble() {
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getAnswer() {
		return answer;
	}
	
	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}
}
