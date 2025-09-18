package com.hawk.activity.type.impl.celebrationCourse;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 周年历程
 * @author luke
 */
public class CelebrationCourseActivityHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.CELEBRATION_COURSE_MAIN_REQ_VALUE)
	public void main(HawkProtocol hawkProtocol, String playerId){
		CelebrationCourseActivity activity = this.getActivity(ActivityType.CELEBRATION_COURSE_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.CELEBRATION_COURSE_SIGN_REQ_VALUE)
	public void sign(HawkProtocol hawkProtocol, String playerId){
		CelebrationCourseActivity activity = this.getActivity(ActivityType.CELEBRATION_COURSE_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.sign(playerId);
	}
	
	@ProtocolHandler(code = HP.code.CELEBRATION_COURSE_SHARE_REQ_VALUE)
	public void shareReward(HawkProtocol hawkProtocol, String playerId){
		CelebrationCourseActivity activity = this.getActivity(ActivityType.CELEBRATION_COURSE_ACTIVITY);
		if(activity == null){
			return;
		}
//		CelebrationCourseShareReq req = hawkProtocol.parseProtocol(CelebrationCourseShareReq.getDefaultInstance());
		activity.shareReward(playerId);
	}
	
	@ProtocolHandler(code = HP.code.CELEBRATION_COURSE_X8_REQ_VALUE)
	public void shareX8Data(HawkProtocol hawkProtocol, String playerId){
		CelebrationCourseActivity activity = this.getActivity(ActivityType.CELEBRATION_COURSE_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.shareX8Data(playerId);
	}
	
}