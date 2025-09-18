package com.hawk.game.service.mail;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.ConcurrentArrayQueue;
import org.hawk.app.HawkAppObj;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;

import com.hawk.game.service.MailService;

public class MailManager extends HawkAppObj {
	private static MailManager Instance;

	private Queue<MailParames> awardMailqueue = new ConcurrentArrayQueue<>();

	public MailManager(HawkXID xid) {
		super(xid);
		if (null == Instance) {
			Instance = this;
		}
	}

	/** 有奖励邮件周期补发 */
	class AwardMailPeriodTick extends HawkPeriodTickable {

		public AwardMailPeriodTick(long tickPeriod, long delayTime) {
			super(tickPeriod, delayTime);
		}

		@Override
		public void onPeriodTick() {
			reissueMail();
		}

	}

	public static MailManager getInstance() {
		return Instance;
	}

	/** 如果有发送失败的邮件, 补发 */
	public void reissueMail() {
		if (this.awardMailqueue.isEmpty()) {
			return;
		}
		Queue<MailParames> opQueue = awardMailqueue;
		awardMailqueue = new ConcurrentArrayQueue<>();
		for (MailParames parames : opQueue) {
			MailService.getInstance().sendMail(parames);
		}
	}

	public boolean init() {
		int upatePeriod = (int) TimeUnit.MINUTES.toMillis(1);
		addTickable(new AwardMailPeriodTick(upatePeriod, upatePeriod));
		return true;
	}

	public void inqueue(MailParames parames) {
		this.awardMailqueue.add(parames);
	}

}
