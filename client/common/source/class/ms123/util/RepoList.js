/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
qx.Class.define("ms123.util.RepoList", {
 extend: qx.core.Object,
 include : [ qx.locale.MTranslation],


	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (facade,formElement,config) {
		this.base(arguments);
		var repoList = this._getRepos(formElement);
	},

	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_getRepos: function (formElement) {
			var completed = (function (ret) {
				var repoList = [];
				repoList.push("-");
				for( var i = 0; i< ret.length;i++){
					var r = ret[i];
					repoList.push( r.name );
				}
				console.log("repoList.rel:"+JSON.stringify(repoList,null,2));
				for(var i=0; i < repoList.length; i++){
					var repoName = repoList[i];
					var listItem = new qx.ui.form.ListItem(repoName,null,repoName);
					if( formElement.addItem){
						formElement.addItem(listItem);
					}else{
						formElement.add(listItem);
					}
				}
				return repoList;
			}).bind(this);

			var failed = (function (details) {
				ms123.form.Dialog.alert(this.tr("namespace.getNamespaces") + ":" + details.message);
			}).bind(this);

			var params = {
				service: "git",
				method: "getRepositories",
				parameter: {},
				context: this,
				async: true,
				completed: completed,
				failed: failed
			}
			ms123.util.Remote.rpcAsync(params);
		}
	}
});
