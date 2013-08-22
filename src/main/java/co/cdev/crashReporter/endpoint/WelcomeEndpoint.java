/*

 Copyright (c) 2013, CDev LLC
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the <organization> nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package co.cdev.crashReporter.endpoint;

import co.cdev.agave.Route;
import co.cdev.agave.configuration.RoutingContext;
import co.cdev.agave.web.HTTPResponse;
import co.cdev.agave.web.StatusCode;
import co.cdev.gson.JSONResponse;

import java.util.HashMap;
import java.util.Map;

public class WelcomeEndpoint {

    /**
     * Serves the same purpose as that of a 'welcome-file' entry in the web.xml file.
     * Note, that binding a handler to the root of the application like we are doing here will
     * always override the 'welcome-file' entry.
     *
     * @param routingContext the context that this handler method executes under
     * @throws Exception if anything goes wrong
     * @return a destination object that wraps the index.jsp page
     */
    @Route("/")
    public HTTPResponse welcome(RoutingContext routingContext) throws Exception {
        Map<String, String> message = new HashMap<String, String>();

        message.put("running", "true");

        return new JSONResponse(StatusCode._200_Ok, message);
    }

}
