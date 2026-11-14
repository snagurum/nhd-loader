package com.web.crawler;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@Data
public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static Map<String, String> DEFAULT_HEADERS = Map.of(
		"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
		"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
		"Accept-Language", "en-US,en;q=0.9",
		"Accept-Encoding", "identity"
	);


	private Page page;
	private Map<String, String> globalParams;
	private int requestTimeoutSeconds;

	public static HttpClient getHttpClient(int connectTimeoutSeconds){
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

		HttpClient client = HttpClient.newBuilder()
			.cookieHandler(cookieManager)
			.connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
			return client;
	}
	
	
	public HttpUtil(Page page, Map<String,String> gParams, int requestTimeoutSeconds){
		this.page = page;
		this.globalParams = gParams;
		this.requestTimeoutSeconds = requestTimeoutSeconds;
	}
	
	
	public void process(HttpClient client) throws IOException, PageLoadException, InterruptedException{

		String url = addParam();
		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.timeout(Duration.ofSeconds(requestTimeoutSeconds));
		addHeaders(builder);
		addMethod(builder);
		HttpRequest httpRequest = builder.build();

		HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		this.page.setResponseData(httpResponse.body());
		this.page.setResponseStatusCode(httpResponse.statusCode());

		doValidation(httpResponse, url);
		doLoadParams();
		return ;
	}

	private String addParam(){
		String url = this.page.getUrl();
		if(this.globalParams !=null){
			for (String key  : this.globalParams.keySet()) {
				url = url.replace(key,URLEncoder.encode(this.globalParams.get(key), StandardCharsets.UTF_8));
			}
		}
		return url;
	}

	private void addMethod(HttpRequest.Builder builder){		
		if("POST".equalsIgnoreCase(this.page.getMethod()))
			builder.POST(HttpRequest.BodyPublishers.noBody());
		else builder.GET();

	}

	private void addHeaders(HttpRequest.Builder builder){
		if(this.page.getHeaders()!= null)
			this.page.getHeaders().forEach((key, value) -> builder.header(key , value));
		else DEFAULT_HEADERS.forEach((key, value) -> builder.header(key , value));
		
		if(this.page.getReferer()!=null)
			builder.header("Referer",this.page.getReferer());

	}

	private void doValidation(HttpResponse<String> httpResponse, String requestedUrl){
		log.trace("Validating NSE page={}, requestedUrl={}, finalUrl={}, expectedStatus={}, receivedStatus={}",
			this.page.getPageName(),
			requestedUrl,
			httpResponse.uri(),
			this.page.getValidationStatusCode(),
			this.page.getResponseStatusCode());

		if(null!=this.page.getValidationString() && this.page.getReferer().indexOf(this.page.getValidationString())>-1){
			log.warn("NSE request failed VALIDATION STRING page={}, validation String : {}, response String : {}",
				this.page.getPageName(),
				page.getValidationString(),
				httpResponse.body());
			throw new PageLoadException("PageLoad String validation Failure");
		}

		if(null != this.page.getValidationStatusCode() && !this.page.getValidationStatusCode().equals(this.page.getResponseStatusCode()) ){
			log.warn("NSE request failed STATUS CODE page={}, validating status code : {}, response status code : {}",
				this.page.getPageName(),
				page.getResponseStatusCode(),
				httpResponse.statusCode());
			throw new PageLoadException("PageLoad statusCode validation Failure: expected " +
				this.page.getValidationStatusCode() + ", received " + this.page.getResponseStatusCode());
		}
	}

	private void doLoadParams(){		
		if(this.page.getPageParams()!=null)
		for (PageParam pageParam : this.page.getPageParams()) {
			this.globalParams.put(pageParam.getName(),getSubstringBetween(new String(this.page.getResponseData()), pageParam.getStartString(), pageParam.getEndString()));
		}
	}

	public static String getSubstringBetween(String text, String startStr, String endStr) {
        if (text == null || startStr == null || endStr == null) 
			throw new ParamNotFoundException("Inputs (text, startStr, endStr) cannot be null.");
        
		int startIdx = text.indexOf(startStr);
        if (startIdx == -1) 
			throw new ParamNotFoundException("The start string '" + startStr + "' was not found in the text.");
        
		text = text.substring(startIdx + startStr.length());
		int endIdx = text.indexOf(endStr);
        if (endIdx == -1) 
			throw new ParamNotFoundException("The end string '" + endStr + "' was not found in the remaining text.");
        
        return text.substring(0, endIdx);
    }
 
}
