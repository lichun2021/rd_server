package com.hawk.game.data;

/***
 * 福袋响应解析类
 * @author yang.rao
 *
 */
public class LuckyBagWxResponse {
	private int ret;
	private String msg;
	private ResponseUrl data;
	public int getRet() {
		return ret;
	}
	public void setRet(int ret) {
		this.ret = ret;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public ResponseUrl getData() {
		return data;
	}
	public void setData(ResponseUrl data) {
		this.data = data;
	}
	
	public static class ResponseUrl {
		private String url;
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		@Override
		public String toString() {
			return "ResponseUrl [url=" + url + "]";
		}
	}
	
	@Override
	public String toString() {
		if(data != null){
			return "LuckyBagResponse [ret=" + ret + ", msg=" + msg + ", data=" + data + "]";
		}
		return "LuckyBagResponse [ret=" + ret + ", msg=" + msg + "]";
	}
	
	
}
