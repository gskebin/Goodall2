package kr.co.dunet.goodall;

public class NetworkConfig {
	private String mqttHost = "tcp://218.38.137.132:5222";
	private String webHost = "http://192.168.0.22";
	private String webProtocol = "http";
	private String webDns = "192.168.0.22";
	private String webPort = "80";
	
	private static NetworkConfig mNetworkConfig = null;
	
	public NetworkConfig() {
		mNetworkConfig = this;
	}
	
	public synchronized static NetworkConfig Instance() {
		if (mNetworkConfig == null) {
			mNetworkConfig = new NetworkConfig();
		}
		return mNetworkConfig;
	}
	
	public String getMqttHost() {
		return mqttHost;
	}
	
	public String getWebHost() {
		return webHost;
	}
	
	public String getWebProtocol() {
		return webProtocol;
	}
	
	public String getWebDns() {
		return webDns;
	}
	
	public String getWebPort() {
		return webPort;
	}
}