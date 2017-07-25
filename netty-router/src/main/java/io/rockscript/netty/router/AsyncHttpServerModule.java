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

package io.rockscript.netty.router;

import com.google.inject.AbstractModule;


public class AsyncHttpServerModule extends AbstractModule {

  AsyncHttpServerConfiguration asyncHttpServerConfiguration = null;

  public AsyncHttpServerModule() {
    this(new AsyncHttpServerConfiguration()
        .defaultNotFoundHandler());
  }

  public AsyncHttpServerModule(AsyncHttpServerConfiguration asyncHttpServerConfiguration) {
    this.asyncHttpServerConfiguration = asyncHttpServerConfiguration;
  }

  public AsyncHttpServerModule requestHandlerClass(Class<? extends RequestHandler> requestHandlerClass) {
    asyncHttpServerConfiguration.scan(requestHandlerClass);
    return this;
  }

  @Override
  protected void configure() {
    bind(AsyncHttpServerConfiguration.class).toInstance(asyncHttpServerConfiguration);
    bind(AsyncHttpServer.class).toProvider(new AsyncHttpServerProvider());
  }
}