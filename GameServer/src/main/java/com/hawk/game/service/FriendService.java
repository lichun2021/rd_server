package com.hawk.game.service;

public class FriendService {
	
	private static FriendService service;
	
	private FriendService() {}
	
	public static FriendService getInstance() {
		if (service == null) {
			service = new FriendService();
		}
		return service;
	}

}
