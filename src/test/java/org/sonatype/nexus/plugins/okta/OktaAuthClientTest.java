package org.sonatype.nexus.plugins.okta;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.sonatype.nexus.plugins.okta.client.ApiHttpClient;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClient;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClientConfig;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClientException;
import org.sonatype.nexus.plugins.okta.client.OktaAuthClientExceptionSeverity;
import org.sonatype.nexus.plugins.okta.client.dto.OktaAuthRequest;
import org.sonatype.nexus.plugins.okta.client.dto.OktaAuthResponse;
import org.sonatype.nexus.plugins.okta.client.dto.OktaErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class OktaAuthClientTest
{
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private OktaAuthClientConfig config;
	private OktaAuthClient client;
	private ApiHttpClient apiClient;
	private ObjectMapper mapper;
	
	@Before
	public void setup() throws IOException
	{
		config = Mockito.mock(OktaAuthClientConfig.class);
		when(config.getOktaOrgUrl()).thenReturn("https://localhost");
		when(config.getOktaApi()).thenReturn("/api/v1");
		when(config.getMfaPollDelay()).thenReturn(10);
		when(config.getMfaPollMaxRetries()).thenReturn(5);
		
		apiClient = Mockito.mock(ApiHttpClient.class);
		
		this.client = new OktaAuthClient(apiClient, config);
		this.mapper = new ObjectMapper();
	}

	@Test
	public void shouldNotAuthenticate() throws IOException
	{
		final OktaAuthRequest requestBody = new OktaAuthRequest("test", "wrong", null);
		final String uri = config.getOktaOrgUrl() + config.getOktaApi() + "/authn";
		
		OktaErrorResponse resp = null;
		try (InputStream inputStream = this.getClass().getResourceAsStream("/stubs/okta-authn-response-failed.json")) {
			resp = mapper.readValue(readFromInputStream(inputStream), OktaErrorResponse.class);
		}
		
		resp.setErrorSummary("Authentication failed");
		when(apiClient.sendPostRequest(uri, requestBody, OktaAuthResponse.class)).thenThrow(new OktaAuthClientException(OktaAuthClientExceptionSeverity.INFO, resp));
		
		exception.expect(OktaAuthClientException.class);
		exception.expectMessage("Okta error 'E0000004' - Authentication failed");
		client.authn("test", "wrong");
	}

	@Test
	public void shouldAuthenticate() throws IOException
	{
		String uri = config.getOktaOrgUrl() + config.getOktaApi() + "/authn";
		
		OktaAuthResponse response = null;
		try (InputStream inputStream = this.getClass().getResourceAsStream("/stubs/okta-auth-response-mfa-required.json")) {
			response = mapper.readValue(readFromInputStream(inputStream), OktaAuthResponse.class);
		}
		when(apiClient.sendPostRequest(eq(uri), any(), any())).thenReturn(response);
		
		uri = "https://okta-test-org/api/v1/authn/factors/4657/verify";
		try (InputStream inputStream = this.getClass().getResourceAsStream("/stubs/okta-auth-response-mfa-waiting.json")) {
			response = mapper.readValue(readFromInputStream(inputStream), OktaAuthResponse.class);
		}
		when(apiClient.sendPostRequest(eq(uri), any(), any())).thenReturn(response);
		
		
		uri = "https://okta-test-org/api/v1/authn/factors/1234/verify";
		try (InputStream inputStream = this.getClass().getResourceAsStream("/stubs/okta-auth-response-success.json")) {
			response = mapper.readValue(readFromInputStream(inputStream), OktaAuthResponse.class);
		}
		when(apiClient.sendPostRequest(eq(uri), any(), any())).thenReturn(response);

		
		OktaAuthResponse actualResponse = client.authn("testUser", "testPassword");
		assertThat(actualResponse.getSessionToken(), equalTo("123456789"));
	}
	
	private String readFromInputStream(InputStream inputStream) throws IOException
	{
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				resultStringBuilder.append(line).append("\n");
			}
		}
		return resultStringBuilder.toString();
	}
}
