/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.TestOnlyImplFirebaseTrampolines;
import com.google.firebase.auth.MockGoogleCredentials;
import org.junit.After;
import org.junit.Test;

public class FirebaseRequestInitializerTest {

  @After
  public void tearDown() {
    TestOnlyImplFirebaseTrampolines.clearInstancesForTest();
  }

  @Test
  public void testDefaultSettings() throws Exception {
    FirebaseApp app = FirebaseApp.initializeApp(new FirebaseOptions.Builder()
        .setCredentials(new MockGoogleCredentials("token"))
        .build());
    HttpTransport transport = new MockHttpTransport();
    HttpRequestFactory factory = transport.createRequestFactory(
        new FirebaseRequestInitializer(app));

    HttpRequest request = factory.buildGetRequest(
        new GenericUrl("https://firebase.google.com"));

    assertEquals(0, request.getConnectTimeout());
    assertEquals(0, request.getReadTimeout());
    assertEquals("Bearer token", request.getHeaders().getAuthorization());
    // assertEquals(4, request.getNumberOfRetries());
    // assertTrue(request.getIOExceptionHandler() instanceof HttpRetryHandler);
    // assertTrue(request.getUnsuccessfulResponseHandler() instanceof HttpRetryHandler);
  }

  @Test
  public void testExplicitTimeouts() throws Exception {
    FirebaseApp app = FirebaseApp.initializeApp(new FirebaseOptions.Builder()
        .setCredentials(new MockGoogleCredentials("token"))
        .setConnectTimeout(30000)
        .setReadTimeout(60000)
        .build());
    HttpTransport transport = new MockHttpTransport();
    HttpRequestFactory factory = transport.createRequestFactory(
        new FirebaseRequestInitializer(app));

    HttpRequest request = factory.buildGetRequest(
        new GenericUrl("https://firebase.google.com"));

    assertEquals(30000, request.getConnectTimeout());
    assertEquals(60000, request.getReadTimeout());
    assertEquals("Bearer token", request.getHeaders().getAuthorization());
    // assertEquals(4, request.getNumberOfRetries());
    // assertTrue(request.getIOExceptionHandler() instanceof HttpRetryHandler);
    // assertTrue(request.getUnsuccessfulResponseHandler() instanceof HttpRetryHandler);
  }

  @Test
  public void testNullRetryConfig() throws Exception {
    FirebaseApp app = FirebaseApp.initializeApp(new FirebaseOptions.Builder()
        .setCredentials(new MockGoogleCredentials("token"))
        .build());
    HttpTransport transport = new MockHttpTransport();
    HttpRequestFactory factory = transport.createRequestFactory(
        new FirebaseRequestInitializer(app, null));

    HttpRequest request = factory.buildGetRequest(
        new GenericUrl("https://firebase.google.com"));

    assertEquals(0, request.getConnectTimeout());
    assertEquals(0, request.getReadTimeout());
    assertEquals("Bearer token", request.getHeaders().getAuthorization());
    assertEquals(0, request.getNumberOfRetries());
    assertNull(request.getIOExceptionHandler());
    assertTrue(request.getUnsuccessfulResponseHandler() instanceof HttpCredentialsAdapter);
  }

  @Test
  public void testExplicitRetryConfig() throws Exception {
    FirebaseApp app = FirebaseApp.initializeApp(new FirebaseOptions.Builder()
        .setCredentials(new MockGoogleCredentials("token"))
        .build());
    HttpTransport transport = new MockHttpTransport();
    RetryConfig retryConfig = RetryConfig.builder()
        .setMaxRetries(5)
        .build();
    HttpRequestFactory factory = transport.createRequestFactory(
        new FirebaseRequestInitializer(app, retryConfig));

    HttpRequest request = factory.buildGetRequest(
        new GenericUrl("https://firebase.google.com"));

    assertEquals(0, request.getConnectTimeout());
    assertEquals(0, request.getReadTimeout());
    assertEquals("Bearer token", request.getHeaders().getAuthorization());
    assertEquals(5, request.getNumberOfRetries());
    assertTrue(request.getIOExceptionHandler() instanceof HttpBackOffIOExceptionHandler);
    assertNotNull(request.getUnsuccessfulResponseHandler());
  }
}
