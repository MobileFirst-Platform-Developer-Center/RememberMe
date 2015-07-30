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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lightcouch.NoDocumentException;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.ibm.json.java.JSONObject;
import com.worklight.core.auth.ext.WorklightProtocolAuthenticator;
import com.worklight.core.auth.impl.AuthenticationContext;
import com.worklight.server.auth.api.AuthenticationResult;
import com.worklight.server.auth.api.AuthenticationStatus;
import com.worklight.server.auth.api.MissingConfigurationOptionException;
import com.worklight.server.bundle.api.WorklightConfiguration;


public class CustomAuthenticator extends WorklightProtocolAuthenticator {


	private static final long serialVersionUID = -3375501788949271371L;

	private static Logger logger = Logger.getLogger(CustomAuthenticator.class.getName());
	protected static final String USERNAME_KEY = "user.name";
	protected static final String PASSWORD_KEY = "user.password";
	protected static final String PROPERTY_EXPIRATION_DAYS = "rememberMeExpirationInDays";

	protected static final String CLOUDANT_DB = "rememberme";

	private static CloudantClient cloudant;
	private static Database db;

	private String username;
	private String password;
	private Boolean rememberMe;
	private Integer rememberMeExpirationInDays;

	@Override
	public void init(Map<String, String> options) throws MissingConfigurationOptionException {
		logger.info("CustomAuthenticator :: Initializing. options :: " + options.toString());
		super.init(options);

		String tempExpiration = options.remove(PROPERTY_EXPIRATION_DAYS);
		if(tempExpiration == null){
			throw new MissingConfigurationOptionException(PROPERTY_EXPIRATION_DAYS);
		}
		rememberMeExpirationInDays = Integer.parseInt(tempExpiration);

		String cloudantDomain = WorklightConfiguration.getInstance().getStringProperty("cloudant.domain");
		String cloudantKey = WorklightConfiguration.getInstance().getStringProperty("cloudant.key");
		String cloudantPassword = WorklightConfiguration.getInstance().getStringProperty("cloudant.password");

		cloudant = new CloudantClient(cloudantDomain,cloudantKey,cloudantPassword);
		db = cloudant.database(CLOUDANT_DB, false);


	}

	@Override
	public AuthenticationResult processRequest(HttpServletRequest request,
			HttpServletResponse response, boolean isAccessToProtectedResource)
			throws IOException, ServletException {

		logger.info("CustomAuthenticator :: processRequest");

		if (!isAccessToProtectedResource){
			logger.info("CustomAuthenticator :: !isAccessToProtectedResource");
			return AuthenticationResult.createFrom(AuthenticationStatus.REQUEST_NOT_RECOGNIZED);
		}


		String clientID = AuthenticationContext.getCurrentClientId();
		logger.info("CustomAuthenticator :: clientID = " + clientID);
		RememberedClient client = getRememberedClient(clientID);

		if(client != null){
			logger.info("CustomAuthenticator :: found client");
			username = client.getUsername();
			password = null; //Using non-validating
			logger.info("CustomAuthenticator :: SUCCESS from ClientID");
			return AuthenticationResult.createFrom(AuthenticationStatus.SUCCESS);
		}

		JSONObject challengeResponse = (JSONObject) getChallengeResponse(request);

		if (null == challengeResponse){
			return generateChallenge("Please enter username and password");
		}

		username = (String) challengeResponse.get("username");
		password = (String) challengeResponse.get("password");
		rememberMe = (Boolean) challengeResponse.get("rememberMe");
		logger.info("CustomAuthenticator :: rememberMe = " + rememberMe);


		if (null == username || null == password || username.length() == 0 || password.length() == 0){
			return generateChallenge("Username and password cannot be blank");
		} else {
			//The login module is non-validating. Do the validation here
			//TODO: password validation against your database

			if(rememberMe){
				//If all is good, backup the user/clientID
				this.rememberCient(clientID, username);
			}
			logger.info("CustomAuthenticator :: SUCCESS");
			return AuthenticationResult.createFrom(AuthenticationStatus.SUCCESS);
		}

	}

	private AuthenticationResult generateChallenge(String errorMessage){
		logger.info("CustomAuthenticator :: generateChallenge");
		AuthenticationResult authenticationResult = AuthenticationResult.createFrom(AuthenticationStatus.CLIENT_INTERACTION_REQUIRED);
		JSONObject challengeObj = new JSONObject();
		challengeObj.put("authStatus", "credentialsRequired");
		challengeObj.put("errorMessage", errorMessage);
		authenticationResult.setJson(challengeObj);
		return authenticationResult;
	}

	@Override
	public Map<String, Object> getAuthenticationData() {
		logger.info("CustomAuthenticator :: getAuthenticationData");
		Map<String, Object> authData = new HashMap<String, Object>();
		authData.put(USERNAME_KEY, username);
		authData.put(PASSWORD_KEY, password);
		authData.put("clientID", AuthenticationContext.getCurrentClientId());
		return authData;
	}

	private void rememberCient(String clientID, String username){
		RememberedClient client = new RememberedClient();
		client.setClientID(clientID);
		client.setUsername(username);

		Calendar c = Calendar.getInstance();
		c.setTime(new Date()); // Now use today date.
		c.add(Calendar.DATE, rememberMeExpirationInDays);
		client.setExpiration(c.getTime());

		db.save(client);
	}

	private RememberedClient getRememberedClient(String clientID){
		try{
			RememberedClient client = db.find(RememberedClient.class, clientID);
			logger.info("CustomAuthenticator :: expiration date = " + client.getExpiration());
			if(client.isExpired()){
				logger.info("CustomAuthenticator :: client has expired");
				this.forgetClient(client);
				return null;
			}
			return client;
		}
		catch(NoDocumentException e){
			return null;
		}
	}

	private void forgetClient(RememberedClient user){
		db.remove(user);
	}

	public static void forgetClient(String clientID){
		try{
			RememberedClient client = db.find(RememberedClient.class, clientID);
			db.remove(client);
			logger.info("CustomAuthenticator :: deleted client after logout");
		}
		catch(NoDocumentException e){
			logger.info("CustomAuthenticator :: no client found after logout");
			return;
		}
	}

}
