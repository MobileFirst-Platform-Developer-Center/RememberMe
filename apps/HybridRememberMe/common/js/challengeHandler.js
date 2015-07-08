/*
 *
    COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, modify, and distribute
    these sample programs in any form without payment to IBM® for the purposes of developing, using, marketing or distributing
    application programs conforming to the application programming interface for the operating platform for which the sample code is written.
    Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES,
    EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
    FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT,
    INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE.
    IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE SOURCE CODE.

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
