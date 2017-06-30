package kr.co.dunet.goodall;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class HttpService {
	private String protocol = "http";
	private String dns = "";
	private String port = "";
	private String urlString = null;
	public String json = null;
	private static HttpService instance = null;
	private List<BasicNameValuePair> nameValuePairs = null;

	public synchronized static HttpService getInstance() {
		if (instance == null) {
			instance = new HttpService();
		}
		return instance;
	}
	
	public HttpService() {
		setProtocol(NetworkConfig.Instance().getWebProtocol());
		setDns(NetworkConfig.Instance().getWebDns());
		setPort(NetworkConfig.Instance().getWebPort());
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	public String getJson() {
		return json;
	}

	public void setNameValuePairs(List<BasicNameValuePair> nameValuePairs) {
		this.nameValuePairs = nameValuePairs;
	}

	public Boolean sendFileData(String... postData) {
		try {
			json = null;
			
			String imagePath = postData[1];
			Log.d("FILE HTTP", imagePath);

			//URL url = new URL(protocol + "://" + dns + ":" + port + urlString);
			// Log.d("HTTP", url.toString());

			// MultipartEntityBuilder 생성
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			

			// 문자열 및 데이터 추가

			//("photo", path, myId, myName, roomCode, roomName);
			builder.addTextBody("id", postData[2],
					ContentType.create("Multipart/related", "UTF-8"));
			builder.addTextBody("name", postData[3],
					ContentType.create("Multipart/related", "UTF-8"));
			builder.addTextBody("code", postData[4],
					ContentType.create("Multipart/related", "UTF-8"));
			builder.addTextBody("roomname", postData[5],
					ContentType.create("Multipart/related", "UTF-8"));
			builder.addTextBody("key", postData[6],
					ContentType.create("Multipart/related", "UTF-8"));
			builder.addPart("photo", new FileBody(new File(imagePath)));

			HttpEntity entity = builder.build();

			// 전송
			InputStream inputStream = null;
			//CloseableHttpClient httpclient = HttpClients.createDefault();
			AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
			
			HttpPost httpPost = new HttpPost(protocol + "://" + dns + ":" + port + urlString);
			
			httpPost.setEntity(entity);
			
			//CloseableHttpResponse response = httpclient.execute(httpPost);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			
			inputStream = httpEntity.getContent();

			// 응답
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			inputStream.close();

			// 응답 결과
			String result = stringBuilder.toString();
			Log.i("File Response", result);
			//json = result;
			
			File file = new File(imagePath);
			file.delete();
			
			httpClient.close();

			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public Boolean sendProfilePhoto(String... postData) {
		try {
			json = null;
			
			String imagePath = postData[1];
			Log.d("FILE HTTP", imagePath);
			
			// MultipartEntityBuilder 생성
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			
			// 문자열 및 데이터 추가
			
			//("photo", path, myId);
			builder.addTextBody("id", postData[2],
					ContentType.create("Multipart/related", "UTF-8"));
			builder.addPart("photo", new FileBody(new File(imagePath)));
			
			HttpEntity entity = builder.build();
			
			// 전송
			InputStream inputStream = null;
			AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
			
			HttpPost httpPost = new HttpPost(protocol + "://" + dns + ":" + port + urlString);
			
			httpPost.setEntity(entity);
			
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			
			inputStream = httpEntity.getContent();
			
			// 응답
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			inputStream.close();
			
			// 응답 결과
			json = stringBuilder.toString();
			Log.i("File Response", json);
			
			File file = new File(imagePath);
			file.delete();
			
			httpClient.close();
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public Boolean sendData() {
		try {
			json = null;

			URL url = new URL(protocol + "://" + dns + ":" + port + urlString);
			//Log.d("HTTP", url.toString());

			// HttpURLConnection 인스턴스를 생성한다
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			Log.d("http service " ,  "접속");
			// 연결 요청 시, 대기하는 시간을 10초로 제한한다. 만약 시간이 초과된다면,
			// SocketTimeoutException이 발생한다
			con.setRequestMethod("POST");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setDefaultUseCaches(false);
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);

			// 요청 메시지를 서버에 전송한다

			try {
				con.connect();
				Log.d("http service " , "접속성공" );
			} catch (SocketTimeoutException e) {
				con.disconnect();
				Log.d("http service" , "접속실패");
				return false;
			}

			if (nameValuePairs != null) {
				// Log.d("HTTP", getURLQuery(nameValuePairs));
				OutputStream outputStream = con.getOutputStream();

				BufferedWriter bufferedWriter = new BufferedWriter(
						new OutputStreamWriter(outputStream, "UTF-8"));

				bufferedWriter.write(getURLQuery(nameValuePairs));
				bufferedWriter.flush();
				bufferedWriter.close();
				outputStream.close();
			}

			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// InputStream으로 데이터를 변환한다
				json = getTextFrom(con.getInputStream());

				// JSONObject result = new JSONObject(json);
				// Log.d("HTTP", result.getString("result"));
				Log.d("HttpService 수신 : ", json);
				con.disconnect();

				return true;
			} else {
				Integer x = con.getResponseCode();
				Log.d("HTTP", x.toString());

				con.disconnect();

				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}
	}
	
	public String sendProfileData() {
		try {
			json = null;
			
			URL url = new URL(protocol + "://" + dns + ":" + port + urlString);
			//Log.d("HTTP", url.toString());
			
			// HttpURLConnection 인스턴스를 생성한다
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			// 연결 요청 시, 대기하는 시간을 3초로 제한한다. 만약 시간이 초과된다면,
			// SocketTimeoutException이 발생한다
			con.setRequestMethod("POST");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setDefaultUseCaches(false);
			con.setConnectTimeout(3000);
			con.setReadTimeout(3000);
			
			// 요청 메시지를 서버에 전송한다
			
			try {
				con.connect();
			} catch (SocketTimeoutException e) {
				con.disconnect();
				
				return null;
			}
			
			if (nameValuePairs != null) {
				// Log.d("HTTP", getURLQuery(nameValuePairs));
				OutputStream outputStream = con.getOutputStream();
				
				BufferedWriter bufferedWriter = new BufferedWriter(
						new OutputStreamWriter(outputStream, "UTF-8"));
				
				bufferedWriter.write(getURLQuery(nameValuePairs));
				bufferedWriter.flush();
				bufferedWriter.close();
				outputStream.close();
			}
			
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// InputStream으로 데이터를 변환한다
				//json = getTextFrom(con.getInputStream());
				
				// JSONObject result = new JSONObject(json);
				// Log.d("HTTP", result.getString("result"));
				//Log.d("HttpService 수신 : ", json);
				
				String ret = getTextFrom(con.getInputStream()); 
				con.disconnect();
				
				return ret;
			} else {
				Integer x = con.getResponseCode();
				Log.d("HTTP", x.toString());
				
				con.disconnect();
				
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}

	private String getURLQuery(List<BasicNameValuePair> params) {
		StringBuilder stringBuilder = new StringBuilder();
		boolean first = true;

		for (BasicNameValuePair pair : params) {
			if (first) {
				first = false;
			} else {
				stringBuilder.append("&");
			}

			try {
				stringBuilder
						.append(URLEncoder.encode(pair.getName(), "UTF-8"));
				stringBuilder.append("=");
				stringBuilder
						.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return stringBuilder.toString();
	}

	/**
	 * 서버와 연결된 스트림으로부터 텍스트를 읽어들입니다.
	 * 
	 * @param in
	 *            서버와 연결된 입력 스트림
	 * @return 서버로부터 읽은 텍스트
	 */
	private String getTextFrom(InputStream in) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			// 스트림으로부터 라인 단위로 자료를 읽어 옵니다.
			while (true) {
				String line = br.readLine();
				if (null == line)
					break;
				sb.append(line + '\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
}