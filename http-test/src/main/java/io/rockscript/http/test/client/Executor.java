/*
 * Copyright ©2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.http.test.client;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpHost;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static javax.net.ssl.SSLContext.getInstance;
import static org.apache.http.auth.AUTH.PROXY_AUTH;
import static org.apache.http.auth.AUTH.WWW_AUTH;
import static org.apache.http.auth.AuthScope.ANY;
import static org.apache.http.client.protocol.HttpClientContext.*;
import static org.apache.http.conn.socket.PlainConnectionSocketFactory.getSocketFactory;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.TLS;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.getSystemSocketFactory;
import static org.apache.http.impl.client.HttpClientBuilder.create;

/**
 * An Executor for fluent requests.
 * <p>
 * A {@link PoolingHttpClientConnectionManager} with maximum 100 connections per route and
 * a total maximum of 200 connections is used internally.
 * </p>
 */
public class Executor {

    final static PoolingHttpClientConnectionManager CONNMGR;
    final static HttpClient CLIENT;

    static {
        LayeredConnectionSocketFactory ssl = null;
        try {
            ssl = getSystemSocketFactory();
        } catch (final SSLInitializationException ex) {
            final SSLContext sslcontext;
            try {
                sslcontext = getInstance(TLS);
                sslcontext.init(null, null, null);
                ssl = new SSLConnectionSocketFactory(sslcontext);
            } catch (final SecurityException ignore) {
            } catch (final KeyManagementException ignore) {
            } catch (final NoSuchAlgorithmException ignore) {
            }
        }

        final Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory> create().register("http", getSocketFactory())
                                                                                                        .register("https", ssl!=null ?
                                                                                                          ssl :
                                                                                                          SSLConnectionSocketFactory.getSocketFactory())
                                                                                                        .build();

        CONNMGR = new PoolingHttpClientConnectionManager(sfr);
        CONNMGR.setDefaultMaxPerRoute(100);
        CONNMGR.setMaxTotal(200);
        CONNMGR.setValidateAfterInactivity(1000);
        CLIENT = create().setConnectionManager(CONNMGR).build();
    }

    public static Executor newInstance() {
        return new Executor(CLIENT);
    }

    public static Executor newInstance(final HttpClient httpclient) {
        return new Executor(httpclient!=null ? httpclient : CLIENT);
    }

    private final HttpClient httpclient;
    private volatile AuthCache authCache;
    private volatile CredentialsProvider credentialsProvider;
    private volatile CookieStore cookieStore;

    Executor(final HttpClient httpclient) {
        super();
        this.httpclient = httpclient;
        this.authCache = new BasicAuthCache();
    }

    /**
     * @since 4.5
     */
    public Executor use(final CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public Executor auth(final AuthScope authScope, final Credentials creds) {
        if (this.credentialsProvider==null) {
            this.credentialsProvider = new BasicCredentialsProvider();
        }
        this.credentialsProvider.setCredentials(authScope, creds);
        return this;
    }

    public Executor auth(final HttpHost host, final Credentials creds) {
        final AuthScope authScope = host!=null ? new AuthScope(host.getHostName(), host.getPort()) : ANY;
        return auth(authScope, creds);
    }

    /**
     * @since 4.4
     */
    public Executor auth(final String host, final Credentials creds) {
        return auth(HttpHost.create(host), creds);
    }

    public Executor authPreemptive(final HttpHost host) {
        final BasicScheme basicScheme = new BasicScheme();
        try {
            basicScheme.processChallenge(new BasicHeader(WWW_AUTH, "BASIC "));
        } catch (final MalformedChallengeException ignore) {
        }
        this.authCache.put(host, basicScheme);
        return this;
    }

    /**
     * @since 4.4
     */
    public Executor authPreemptive(final String host) {
        return authPreemptive(HttpHost.create(host));
    }

    public Executor authPreemptiveProxy(final HttpHost proxy) {
        final BasicScheme basicScheme = new BasicScheme();
        try {
            basicScheme.processChallenge(new BasicHeader(PROXY_AUTH, "BASIC "));
        } catch (final MalformedChallengeException ignore) {
        }
        this.authCache.put(proxy, basicScheme);
        return this;
    }

    /**
     * @since 4.4
     */
    public Executor authPreemptiveProxy(final String proxy) {
        return authPreemptiveProxy(HttpHost.create(proxy));
    }

    public Executor auth(final Credentials cred) {
        return auth(ANY, cred);
    }

    public Executor auth(final String username, final String password) {
        return auth(new UsernamePasswordCredentials(username, password));
    }

    public Executor auth(final String username, final String password, final String workstation, final String domain) {
        return auth(new NTCredentials(username, password, workstation, domain));
    }

    public Executor auth(final HttpHost host, final String username, final String password) {
        return auth(host, new UsernamePasswordCredentials(username, password));
    }

    public Executor auth(final HttpHost host, final String username, final String password, final String workstation, final String domain) {
        return auth(host, new NTCredentials(username, password, workstation, domain));
    }

    public Executor clearAuth() {
        if (this.credentialsProvider!=null) {
            this.credentialsProvider.clear();
        }
        return this;
    }

    /**
     * @deprecated (4.5) Use {@link #use(CookieStore)}.
     */
    @Deprecated
    public Executor cookieStore(final CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    /**
     * @since 4.5
     */
    public Executor use(final CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    public Executor clearCookies() {
        if (this.cookieStore!=null) {
            this.cookieStore.clear();
        }
        return this;
    }

    /**
     * Executes the request. Please Note that response content must be processed
     * or discarded using {@link TestResponse#discardContent()}, otherwise the
     * connection used for the request might not be released to the pool.
     *
     * @see TestResponse#handleResponse(ResponseHandler)
     * @see TestResponse#discardContent()
     */
    public TestResponse execute(final TestRequest testRequest) throws ClientProtocolException, IOException {
        final HttpClientContext localContext = HttpClientContext.create();
        if (this.credentialsProvider!=null) {
            localContext.setAttribute(CREDS_PROVIDER, this.credentialsProvider);
        }
        if (this.authCache!=null) {
            localContext.setAttribute(AUTH_CACHE, this.authCache);
        }
        if (this.cookieStore!=null) {
            localContext.setAttribute(COOKIE_STORE, this.cookieStore);
        }
        return new TestResponse(testRequest.internalExecute(this.httpclient, localContext), testRequest);
    }

    /**
     * @deprecated (4.3) do not use.
     */
    @Deprecated
    public static void registerScheme(final Scheme scheme) {
    }

    /**
     * @deprecated (4.3) do not use.
     */
    @Deprecated
    public static void unregisterScheme(final String name) {
    }

    /**
     * Closes all idle persistent connections used by the internal pool.
     *
     * @since 4.4
     */
    public static void closeIdleConnections() {
        CONNMGR.closeIdleConnections(0, MICROSECONDS);
    }

}