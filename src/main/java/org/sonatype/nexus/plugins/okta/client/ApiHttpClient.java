package org.sonatype.nexus.plugins.okta.client;

public interface ApiHttpClient
{
	<T> T sendPostRequest(final String uri, final Object requestBody, final Class<T> responseClazz);
	
	String asStrOrEmpty(Object obj);
}
