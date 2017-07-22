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
package io.rockscript.action.http;

import java.io.IOException;

public class Response {

  final int status;
  final String statusText;
  // TODO Extract a TextResponseBody class, then a ResponseBody interface and add a BinaryResponseBody with byte[] content
  final String textBody;
  final ResponseHeaders headers;

  Response(int status, String statusText, String textBody, ResponseHeaders headers) {
    this.status = status;
    this.statusText = statusText;
    this.textBody = textBody;
    this.headers = headers;
  }

  String contentType() throws IOException {
    return headers.values("Content-Type").stream().findFirst()
                  .orElseThrow(() -> new IOException("Missing Content-Type response header"));
  }
}