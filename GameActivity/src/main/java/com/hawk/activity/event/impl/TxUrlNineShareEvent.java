

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 在线答题分享
 * 
 * @author RickMei
 *
 */
public class TxUrlNineShareEvent  extends ActivityEvent {

	public TxUrlNineShareEvent(){ super(null);}
	public TxUrlNineShareEvent (String playerId) {
		super(playerId);
	}
	public static TxUrlNineShareEvent valueOf(String playerId) {
		TxUrlNineShareEvent pbe = new TxUrlNineShareEvent(playerId);
		return pbe;
	}
}
