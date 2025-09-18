

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 在线答题分享
 * 
 * @author RickMei
 *
 */
public class TxUrlTenShareEvent  extends ActivityEvent {

	public TxUrlTenShareEvent(){ super(null);}
	public TxUrlTenShareEvent (String playerId) {
		super(playerId);
	}
	public static TxUrlTenShareEvent valueOf(String playerId) {
		TxUrlTenShareEvent pbe = new TxUrlTenShareEvent(playerId);
		return pbe;
	}
}
