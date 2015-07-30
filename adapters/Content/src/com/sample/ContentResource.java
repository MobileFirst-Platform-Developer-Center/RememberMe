/**
* Copyright 2015 IBM Corp.
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

package com.sample;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.worklight.adapters.rest.api.WLServerAPI;
import com.worklight.adapters.rest.api.WLServerAPIProvider;
import com.worklight.core.auth.OAuthSecurity;
import com.worklight.core.auth.impl.AuthenticationContext;

@Path("/")
public class ContentResource {
	/*
	 * For more info on JAX-RS see https://jsr311.java.net/nonav/releases/1.1/index.html
	 */

	//Define logger (Standard java.util.Logger)
	static Logger logger = Logger.getLogger(ContentResource.class.getName());

    //Define the server api to be able to perform server operations
    WLServerAPI api = WLServerAPIProvider.getWLServerAPI();

	@GET
	@Path("/hello")
	@Produces("application/xml")
	@OAuthSecurity(scope="CustomRealm")
	public String hello(){
		//log message to server log
        logger.info("Logging info message...");

		return "Hello from the Java REST adapter";
	}

	@POST
	@Path("/forgetMe")
	@OAuthSecurity(scope="CustomRealm")
	public Response forgetMe(){
		CustomAuthenticator.forgetClient(AuthenticationContext.getCurrentClientId());
		return Response.ok().build();
	}

}
