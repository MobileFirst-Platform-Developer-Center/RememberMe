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

function wlCommonInit(){

}

var busyIndicator = new WL.BusyIndicator( null, {text : 'Loading...'});

function getSecretData(){
	busyIndicator.show();
	var resourceRequest = new WLResourceRequest("/adapters/Content/hello", WLResourceRequest.GET, 30000);
	resourceRequest.send().then(
		getSecretData_CallbackOK,
		getSecretData_CallbackFail
	);
}

function logout(){
	busyIndicator.show();
	var resourceRequest = new WLResourceRequest("/adapters/Content/forgetMe", WLResourceRequest.POST, 30000);
	resourceRequest.send().then(
			function(){
				WL.Client.logout('CustomRealm', {onSuccess:WL.Client.reloadApp});
			}
	);
}

function getSecretData_CallbackOK(response){
	$("#ResponseDiv").html(response.responseText);
	busyIndicator.hide();
}

function getSecretData_CallbackFail(response){
	$("#ResponseDiv").html(response.errorMsg);
	busyIndicator.hide();
}
