package com.hawk.activity.type.impl.redEnvelope.callback;

import java.util.List;

/***
 * 抢红包回调
 * @author yang.rao
 *
 */
public interface RecieveCallBack {
	public void call(int code, List<String> rewards);
}
