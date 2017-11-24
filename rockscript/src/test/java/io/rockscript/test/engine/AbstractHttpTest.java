/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.test.engine;

import io.rockscript.http.server.HttpServer;
import io.rockscript.http.servlet.RouterServlet;
import org.junit.After;
import org.junit.Before;

/** tests use engine directly and a separate web server is
 * active so that the scripts can reach out to it.
 * Configure the external web server by implementing {@link #configure(RouterServlet)} */
public abstract class AbstractHttpTest extends AbstractEngineTest {

  protected static final int PORT = 4000;
  protected static HttpServer server;
  protected RouterServlet routerServlet;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    server = new HttpServer(PORT);
    routerServlet = new RouterServlet();
    routerServlet.setGson(engine.getGson());
    configure(routerServlet);
    server.servlet(routerServlet);
    server.startup();
  }

  @After
  public void tearDown() {
    server.shutdown();
    super.tearDown();
  }

  protected abstract void configure(RouterServlet routerServlet);

}