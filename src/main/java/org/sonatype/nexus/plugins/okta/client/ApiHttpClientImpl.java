package org.sonatype.nexus.plugins.okta.client;

import static org.sonatype.nexus.plugins.okta.client.OktaAuthClientExceptionSeverity.ERROR;
import static org.sonatype.nexus.plugins.okta.client.OktaAuthClientExceptionSeverity.INFO;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.okta.client.dto.OktaErrorResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiHttpClientImpl implements ApiHttpClient
{
	private static final Logger LOG = LoggerFactory.getLogger(ApiHttpClientImpl.class);

	private final ObjectMapper mapper;
	private final CloseableHttpClient client;

	public ApiHttpClientImpl()
	{
		this(HttpClients.createSystem());
	}

	public ApiHttpClientImpl(final CloseableHttpClient client)
	{
		this(client, new ObjectMapper());
	}

	public ApiHttpClientImpl(final CloseableHttpClient client, final ObjectMapper mapper)
	{
		super();
		this.client = client;
		this.mapper = mapper;
	}

	@Override
	public <T> T sendPostRequest(final String uri, final Object requestBody, final Class<T> responseClazz)
	{
		try
		{
			final String json = mapper.writeValueAsString(requestBody);
			if (!LOG.isDebugEnabled())
			{
				LOG.debug("Sending POST request to {} with request body {}", uri, json);
			}
			else
			{
				LOG.info("Sending POST request to {}", uri);
			}

			final StringEntity entity = new StringEntity(json);
			final HttpPost request = new HttpPost(uri);
			request.setEntity(entity);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");

			try (CloseableHttpResponse response = client.execute(request))
			{
				final int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode > 399)
				{
					final OktaAuthClientExceptionSeverity severity = statusCode > 499 ? ERROR : INFO;
					throw new OktaAuthClientException(severity, readResponseBody(uri, response, OktaErrorResponse.class));
				}

				final String responseStr = EntityUtils.toString(response.getEntity());
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Retrieved {} response from {} with response body: {}", statusCode, uri, responseStr);
				}
				else
				{
					LOG.info("Retrieved {} response from {}", statusCode, uri);
				}

				final T responseObj = mapper.readValue(responseStr, responseClazz);
				return responseObj;
			}
		}
		catch (final IOException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String asStrOrEmpty(final Object obj)
	{
		try
		{
			return mapper.writeValueAsString(obj);
		}
		catch (final JsonProcessingException e)
		{
			return "";
		}
	}

	private <T> T readResponseBody(final String uri, final CloseableHttpResponse response, final Class<T> responseClazz)
	{
		if (response.getEntity() != null)
		{
			String responseStr = "";
			try
			{
				responseStr = EntityUtils.toString(response.getEntity());

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Retrieved response body: {}", responseStr);
				}

				final T responseObj = mapper.readValue(responseStr, responseClazz);
				return responseObj;
			}
			catch (ParseException | IOException e)
			{
				final StatusLine statusLine = response.getStatusLine();
				final String errMsg = "Unable to parse response from " + uri + " with code " + statusLine.getStatusCode() + " - "
						+ statusLine.getReasonPhrase() + ": " + responseStr;
				throw new OktaAuthClientException(ERROR, errMsg, e);
			}
		}
		final StatusLine statusLine = response.getStatusLine();
		final String errMsg = "No response body provided by " + uri + " with code " + statusLine.getStatusCode() + " - "
				+ statusLine.getReasonPhrase();
		throw new OktaAuthClientException(ERROR, errMsg);
	}
}
