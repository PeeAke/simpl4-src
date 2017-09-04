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
/**
	* @ignore(Hash)
	* @ignore(Clazz)
*/
qx.Class.define("ms123.processexplorer.FormWindow", {
  extend: qx.core.Object,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (context) {
		this.base(arguments);
		this._init(context);
	},
	statics: {
		__formCache: {}
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		_init:function(context){
			this._destroyWindow = true;
			this._window = this._createFormWindow(context.name);
			if( context.appRoot ){
				context.appRoot.add( this._window);
			}
		},
		close: function () {
			this._destroyWindow = false;
			this._window.close();
			this._destroyWindow = true;
		},
		destroy: function () {
			this._window.destroy();
		},
		open: function (params) {
			if( params.taskName ){
				this._window.setCaption(params.processName+"/"+params.taskName);
			}else{
				this._window.setCaption(params.processName);
			}
			var form = this.createForm(params);
			if (this._window.hasChildren()) {
				this._window.removeAll();
			}
			this._window.add(form, {
				edge: "center"
			});
			this._form = form;
			this._window.open();
		},
		getLabel: function (path) {
			if (this._form.formData[path] !== undefined && this._form.formData[path].label !== undefined) return this._form.formData[path].label;
			return path;
		},
		showErrors: function(cv){
			this._form.setErrors(cv);
		},
		createForm: function (params) {
			var formPath = params.formPath;
			var mappedFormValues = params.mappedFormValues;
			var processVariables = params.processVariables;
			var processTenantId = params.processTenantId;
			var buttons = params.buttons;
			console.log("formPath:"+formPath);

			var formDesc=params.formDesc || null;
			if( formDesc==null){
				try{
					//formDesc = ms123.processexplorer.FormWindow.__formCache[params.processName+formVar];
					if( !formDesc ){
						formDesc = ms123.util.Remote.rpcSync( "git:searchContent",{
										reponame:processTenantId,
										name:formPath,
										type:"sw.form"
								});	
						//console.log("parse:"+formDesc);
						formDesc = formDesc.evalJSON();
						//ms123.processexplorer.FormWindow.__formCache[params.processName+formVar]=formDesc;
					}
				}catch(e){
					ms123.form.Dialog.alert("FormWindow.open:"+e);
					return null;
				}
			}


			
			//var postdata = 'filters={"field":"fid", "op":"eq", "data":"' + formKey + '"}&rows=10000&page=1';
			//var ret = ms123.util.Remote.sendSync("data/form?query=true", "POST", null, postdata, null);

			if (!formDesc ) {
				formDesc = "{properties:{xf_name:\"dummyForm\"},stencil:{id:\"XForm\"},childShapes:[]}";//@@@MS braucht man das, Task ohne Form?
			}
			var formVar=formDesc.properties.xf_name;
			console.log("FormWindow.formVar:",formVar);
			var context = {};
			context.buttons = buttons;
			context.actionCallback = params.actionCallback;
			context.formDesc = formDesc;
			context.formVariables = processVariables;
			context.storeDesc = ms123.StoreDesc.getNamespaceDataStoreDescForNS(processTenantId);
			if(!context.storeDesc){
				context.storeDesc = new ms123.StoreDesc({
					namespace: processTenantId 
				});
			}
			var form = new ms123.widgets.Form(context);
			form.setName( formVar );
			var fd = (processVariables && processVariables[formVar]) ? processVariables[formVar] : {};
			if (mappedFormValues) {
				fd = ms123.util.Clone.merge({}, fd, mappedFormValues);
				var x = qx.util.Serializer.toJson(fd);
				console.log("fd:" + x);
			}
			form.fillForm(fd);
			return form;
		},
		_createFormWindow: function (name) {
			var win = new qx.ui.window.Window(name, "").set({
				resizable: true,
				useMoveFrame: true,
				useResizeFrame: true
			});
			win.setLayout(new qx.ui.layout.Dock);
			win.setWidth(600);
			win.setHeight(450);
			win.setAllowMaximize(false);
			win.setAllowMinimize(false);
			win.setModal(true);
			win.setActive(false);
			win.minimize();
			win.center();
			win.addListener("close", function (e) {
				if( this._destroyWindow ){
					win.destroy();
				}
			}, this);
			return win;
		}
	}
});
