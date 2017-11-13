/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
