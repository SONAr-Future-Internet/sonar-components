package br.ufu.facom.mehar.sonar.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class Uri {

	public String URL;
	public HttpHeaders HEADERS;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public Uri(String url) {
		this.URL = url;
		log.info("Inicializando uri do cliente [" + this.URL + "]");
	}
	
	public Uri(String url, String user, String pass) {
		this.URL = url;
		this.HEADERS = createHeaders(user, pass);
		log.info("Inicializando uri do cliente [" + this.URL + "]");
	}
	
	public String getServerUrl() {
		return this.URL;
	}

	public String createUrl(String path) {
		return new StringBuffer(URL.concat(path)).toString();
	}

	public String createUrl(String path, String... args) {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(URL).path(path);

		if (args != null) {

			for (String arg : args) {

				if (arg != null) {
					try {
						builder.pathSegment(URLEncoder.encode(arg, "UTF-8"));
					} catch (IllegalArgumentException | UnsupportedEncodingException e) {
						e.printStackTrace();
					}

				}

			}
		}

		return builder.build().toUriString();

	}

	public HttpHeaders getHeader() {
		return this.HEADERS;
	}

	private HttpHeaders createHeaders(final String username, final String password) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
				setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

			}
		};
	}

	public String createUrlWithQueryParams(String path, MultiValueMap<String, String> mapParams) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(URL).path(path);
		builder.queryParams(mapParams);
		return builder.build().toUriString();
	}
}
