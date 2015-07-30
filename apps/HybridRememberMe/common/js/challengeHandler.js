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

var challengeHandler = WL.Client.createWLChallengeHandler("CustomRealm");
challengeHandler.logger = WL.Logger.create({pkg:"challengeHandler"});

challengeHandler.handleChallenge = function(challenge){
	busyIndicator.hide();
    var authStatus = challenge.authStatus;
	this.logger.info("handleChallenge :: authStatus :: " + authStatus)

    if (authStatus == "credentialsRequired"){
        $("#AppDiv").hide();
        $("#AuthDiv").show();
        $("#AuthUsername").val('');
        $("#AuthPassword").val('');

        if (challenge.errorMessage){
            $("#AuthInfo").html(challenge.errorMessage);
        }
    }
}

challengeHandler.processSuccess = function (data){
	this.logger.info("processSuccess ::", data);
    $("#AuthDiv").hide();
    $("#AppDiv").show();
}

challengeHandler.handleFailure = function (data){
	busyIndicator.hide();
	this.logger.info("handleFailure ::", data);
    $("#AuthDiv").hide();
    $("#AppDiv").show();
	$("#output").val(JSON.stringify(data));
}

$('#AuthSubmitButton').on('click',function(){
	busyIndicator.show();

    challengeHandler.submitChallengeAnswer({
    	username: $("#AuthUsername").val(),
    	password: $("#AuthPassword").val(),
    	rememberMe: $('#rememberMe').is(':checked')
    })
});

$("#AuthCancelButton").bind('click', function () {
	$("#AppDiv").show();
	$("#AuthDiv").hide();
	challengeHandler.submitFailure();
});
