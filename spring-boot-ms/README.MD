安全-服务间交互
===================

> 示例项目：
*  spring-boot-ms1
* spring-boot-ms2

## 场景描述
两个服务：**spring-boot-ms1**和**spring-boot-ms2**，分别对外提供Rest API服务。当ms2服务访问ms2服务中的某一API时，应该如何做？下面给出简单示例工程。


## 准备
1. 认证服务器UAA：http://192.168.66.158:18080
2. 创建Spring boot 项目ms1和ms2，二者满足：
    * ms1 端口： 18080; ms2端口：  28080；
	* 通过`@EnableResourceServer`,表述两者都是资源服务器，对外提供安全的api服务；
	* ms1 使用的OAuth Client： wanghq；ms2使用OAuth Client:zhuqinghua；
3. ms1提供服务API: /api/user
```java
@RequestMapping("/user")
public String user() {
	return "ms1 user...";
}
```

## 目标
ms2 提供服务API：/api/ms1/user，在该接口中调用了ms1的API:/api/user。

## 实现
ms2作为OAuth Client，通过OAuth2RestTemplate达到访问ms1 API的目的。

RestTemplate代码：
```java
@Configuration
public class OAuthConfig {
	private final String GRANT_CLIENT_CREDENTIALS = "client_credentials";

	@Value("${ms1.clientId}")
	private String oAuth2ClientId;

	@Value("${ms1.clientSecret}")
	private String oAuth2ClientSecret;

	@Value("${ms1.accessTokenUri}")
	private String accessTokenUri;
	
	@Autowired
	private OAuth2ClientContext oauth2Context;
	
	
	@Bean
	OAuth2ProtectedResourceDetails resource() {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setClientAuthenticationScheme(AuthenticationScheme.header);
		resource.setAccessTokenUri(accessTokenUri);
		resource.setClientId(oAuth2ClientId);
		resource.setClientSecret(oAuth2ClientSecret);
		resource.setGrantType(GRANT_CLIENT_CREDENTIALS);
		return resource;
	}

	@Bean
	OAuth2RestTemplate restTemplate() {
		OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resource(), oauth2Context);
		//restTemplate.setAuthenticator(new ApiConnectOAuth2RequestAuthenticator())
		return restTemplate;
	}
}
```

ms2服务API定义：
```java
@RestController
public class MS1Controller {
	@Autowired
	private OAuth2RestTemplate restTemplate;

	@RequestMapping("/ms1/user")
	public String user() {
		try {
			// OAuth2AccessToken accessToken = restTemplate.getAccessToken();
			// HttpHeaders headers = new HttpHeaders();
			ResponseEntity<String> response = restTemplate.exchange("http://localhost:18080/api/user", HttpMethod.GET,
					null, String.class);
			return response.getBody();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
```
期望输出：**ms1 user...**

## 测试 
1. 依次启动UAA、启动ms1、ms2；
2. 为了能访问ms2，先获取一个token（暂使用client_credentials）
```shell
curl -X POST \
  'http://192.168.66.158:18080/oauth/token?client_id=wanghq&client_secret=wanghq&redirect_uri=http%3A%2F%2Flocalhost%3A489%2F&response_type=token' \
  -H 'cache-control: no-cache' \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -H 'postman-token: 68440efd-4c00-f5fd-5c83-7455f1650182' \
  -F grant_type=client_credentials
  
  
  {
    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ6aWQiOiJ1YWEiLCJzY29wZSI6WyJvcGVuaWQiLCJjb3JlIiwidWFhLm5vbmUiLCJhY3MucG9saWNpZXMucmVhZCIsImFjcy5wb2xpY2llcy53cml0ZSIsImFjcy5hdHRyaWJ1dGVzLnJlYWQiLCJwcmVkaXgtYWNzLnpvbmVzLmRlbW8udXNlciIsImFjcy5hdHRyaWJ1dGVzLndyaXRlIiwiYWNzLnpvbmVzLmFkbWluIiwicHJlZGl4LWFjcy56b25lcy5kZW1vLmFkbWluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTk5OS91YWEvb2F1dGgvdG9rZW4iLCJleHAiOjE1MTA2MDg4MDIsImlhdCI6MCwianRpIjoiMjUxM2ZkYmUtYjBlYS00MDZjLWFhNGItOGZjMGUxNzM5OWFhIiwiY2xpZW50X2lkIjoid2FuZ2hxIn0.BPyNVj8rOlQlUMHh41UH26V3tNznNUQNHtMfSyjWlopZC0-ZTpawhObmybL0ACYmp7dtwyEDVb3gnzQIgr74Q7jym5AeIqJqOfMLqM0U32xfN6rbz-dt5yyI61t2xT7Vwbx99PcbwyrfeqJYJ8XTO8Y460sZS_nG9fMU8dDjkN49PryhXffBza-ZLNHLR_uK_jou_nP-6N_4YhURiTSIFiaZzFlSBFjDOeCsuuZxhSc8nDyG_iRCp7GEvAo25SLffBH2G0-e4Izq7svLeka9F5Z4qv1qWiLSemwQsxIBNCYDgTyEwnIgCKi4nqqmKWeAVjFGPCSvmzIUBe8FZ7fCEg",
    "token_type": "bearer",
    "expires_in": 43199,
    "scope": "openid core uaa.none acs.policies.read acs.policies.write acs.attributes.read predix-acs.zones.demo.user acs.attributes.write acs.zones.admin predix-acs.zones.demo.admin",
    "jti": "2513fdbe-b0ea-406c-aa4b-8fc0e17399aa"
 }
```
3. 测试ms2的API接口： /api/ms1/user
```shell
curl -X GET \
  http://localhost:28080/api/ms1/user \
  -H 'authorization: bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ6aWQiOiJ1YWEiLCJzY29wZSI6WyJvcGVuaWQiLCJjb3JlIiwidWFhLm5vbmUiLCJhY3MucG9saWNpZXMucmVhZCIsImFjcy5wb2xpY2llcy53cml0ZSIsImFjcy5hdHRyaWJ1dGVzLnJlYWQiLCJwcmVkaXgtYWNzLnpvbmVzLmRlbW8udXNlciIsImFjcy5hdHRyaWJ1dGVzLndyaXRlIiwiYWNzLnpvbmVzLmFkbWluIiwicHJlZGl4LWFjcy56b25lcy5kZW1vLmFkbWluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTk5OS91YWEvb2F1dGgvdG9rZW4iLCJleHAiOjE1MTA2MDg4MDIsImlhdCI6MCwianRpIjoiMjUxM2ZkYmUtYjBlYS00MDZjLWFhNGItOGZjMGUxNzM5OWFhIiwiY2xpZW50X2lkIjoid2FuZ2hxIn0.BPyNVj8rOlQlUMHh41UH26V3tNznNUQNHtMfSyjWlopZC0-ZTpawhObmybL0ACYmp7dtwyEDVb3gnzQIgr74Q7jym5AeIqJqOfMLqM0U32xfN6rbz-dt5yyI61t2xT7Vwbx99PcbwyrfeqJYJ8XTO8Y460sZS_nG9fMU8dDjkN49PryhXffBza-ZLNHLR_uK_jou_nP-6N_4YhURiTSIFiaZzFlSBFjDOeCsuuZxhSc8nDyG_iRCp7GEvAo25SLffBH2G0-e4Izq7svLeka9F5Z4qv1qWiLSemwQsxIBNCYDgTyEwnIgCKi4nqqmKWeAVjFGPCSvmzIUBe8FZ7fCEg' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'postman-token: 208252dc-e7ba-ed90-875e-325b7f6ba69a'
  
输出：ms1 user...
```
输出和预期一致。