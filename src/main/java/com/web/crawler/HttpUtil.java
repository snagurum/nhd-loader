package com.web.crawler;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@Data
public class HttpUtil {


    private static Map<String, String> DEFAULT_HEADERS = Map.of(
		"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
		"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
		"Accept-Language", "en-US,en;q=0.9",
		"Accept-Encoding", "identity"
	);
	private Page page;
	private Map<String, String> globalParams;

	private String responseData;
	Integer responseStatusCode;


	public static HttpClient getHttpClient(){
			CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

			HttpClient client = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
					return client;
	}
	
	
	public HttpUtil(Page page, Map<String,String> gParams){
		this.page = page;
		this.globalParams = gParams;
	}
	
	
	public String process(HttpClient client) throws IOException, PageLoadException, InterruptedException{

		String url = addParam();
		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
		addHeaders(builder);
		addMethod(builder);
		HttpRequest httpRequest = builder.build();

		HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if(this.page.getCollate())
			this.responseData = this.responseData + httpResponse.body();
		else this.responseData = httpResponse.body();
		this.responseStatusCode = httpResponse.statusCode();

		doValidation();
		return this.responseData;
	}

	private String addParam(){
		String url = this.page.getUrl();
		if(this.globalParams !=null){
			for (String key  : this.globalParams.keySet()) {
				url = url.replace(key,this.globalParams.get(key));
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

	private void doValidation(){
		System.out.println("\n\n\n\n");
		System.out.println(this.page.getPageName() + " Response Code: " + this.responseStatusCode);
		System.out.println(this.page.getPageName() + " Response Data: " + this.responseData);

				if(null!=this.page.getValidationString() && this.responseData.indexOf(this.page.getValidationString())>-1){
			throw new PageLoadException("PageLoad String validation Failure");
		}

		if(null != this.page.getValidationStatusCode() && !this.page.getValidationStatusCode().equals(this.responseStatusCode) ){
			throw new PageLoadException("PageLoad statusCode validation Failure");
		}
	}
 
}
