package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;

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