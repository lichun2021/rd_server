

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 在线答题分享
 * 
 * @author RickMei
 *
 */
public class TxUrlEightShareEvent  extends ActivityEvent {

	public TxUrlEightShareEvent(){ super(null);}
	public TxUrlEightShareEvent (String playerId) {
		super(playerId);
	}
	public static TxUrlEightShareEvent valueOf(String playerId) {
		TxUrlEightShareEvent pbe = new TxUrlEightShareEvent(playerId);
		return pbe;
	}
}
