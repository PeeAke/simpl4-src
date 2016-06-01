/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
	@ignore($)
	@ignore(URL)
	@ignore(jQuery.trim)
	@asset(qx/icon/${qx.icontheme}/16/actions/*)
	@asset(qx/icon/${qx.icontheme}/16/places/*)
*/

qx.Class.define("ms123.namespaces.Manager", {
	extend: ms123.util.TableEdit,

	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (facade) {
		this.__configManager = new ms123.config.ConfigManager();
		this._currentForm=null;
		this.base(arguments, facade);
		//this.add(new qx.ui.basic.Label("Header"), {
		//	edge: "north"
		//});
		facade.window.add(this, {
			edge: 0
		});
		facade.window.setCaption(this.tr("namespacesmanager.title"));
	},

	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		_createColumnModel: function () {
			var columnmodel = [{
				name: "name",
				width: 300,
				type: "TextField",
				readonly:true,
				header: "%namespacesmanager.name"
			},
			{
				name: "isInstalled",
				width: 60,
				type: "TextField",
				readonly:true,
				header: "%namespacesmanager.is_installed"
			},
			{
				name: "isModified",
				width: 60,
				type: "TextField",
				readonly:true,
				header: "%namespacesmanager.is_modified"
			},
			{
				name: "updateAvailable",
				width: 60,
				type: "TextField",
				readonly:true,
				header: "%namespacesmanager.update_available"
			}
			];
			return this._translate(columnmodel);
		},
		_openForm: function () {
			if( this._currentForm ){
				this._propertyEditWindow.remove(this._currentForm);
			}
			this._currentForm = this._createAddForm();
			this._currentForm.fillForm({});
			this._propertyEditWindow.add(this._currentForm);
			this._propertyEditWindow.setActive(true);
			this._propertyEditWindow.open();
		},
		_createToolbar: function () {
			var isRuntime = ms123.config.ConfigManager.isRuntime();
			var toolbar = new qx.ui.toolbar.ToolBar();

			this._buttonInstall = new qx.ui.toolbar.Button(this.tr("namespacesmanager.install"), "icon/16/actions/list-add.png");
			this._buttonInstall.addListener("execute", function () {
				this._table.stopEditing();
				this._openForm();
			}, this);
			if( !isRuntime ){
				this._buttonInstall.setEnabled(true);
				toolbar._add(this._buttonInstall);
			}
			this._buttonUninst = new qx.ui.toolbar.Button(this.tr("namespacesmanager.uninstall"), "icon/16/actions/list-remove.png");
			this._buttonUninst.addListener("execute", function () {
				this._uninstNamespace( false );
			}, this);
			toolbar._add(this._buttonUninst);
			this._buttonUninst.setEnabled(false);

			toolbar.addSeparator();
			this._buttonPull = new qx.ui.toolbar.Button(this.tr("namespacesmanager.pull"), "icon/16/actions/go-bottom.png");
			this._buttonPull.addListener("execute", function () {
				this._pull( );
			}, this);
			toolbar._add(this._buttonPull);
			this._buttonPull.setEnabled(false);

			this._buttonPush = new qx.ui.toolbar.Button(this.tr("namespacesmanager.push"), "icon/16/actions/go-top.png");
			this._buttonPush.addListener("execute", function () {
				this._commitAndPush( );
			}, this);
			if( !isRuntime){
				toolbar._add(this._buttonPush);
				this._buttonPush.setEnabled(false);
			}

			toolbar.addSpacer();
			toolbar.addSeparator();
			this._buttonReload = new qx.ui.toolbar.Button(this.tr("namespacesmanager.reload"), "icon/16/actions/object-rotate-right.png");
			this._buttonReload.addListener("execute", function () {
				this._reload();
			}, this);
			toolbar._add(this._buttonReload);
			this._buttonReload.setEnabled(true);


			this._buttonArchive = new qx.ui.toolbar.Button("", "icon/16/actions/document-save.png");
			this._buttonArchive.addListener("execute", function () {
				this._deleteNamespace( true );
			}, this);
			this._buttonArchive.setEnabled(false);
			//toolbar._add(this._buttonArchive);



			return toolbar;
		},
		_commitAndPush:function(  ){
			var r = this._getRecordAtPos(this._currentTableIndex);
			ms123.form.Dialog.confirm(this.tr("namespacesmanager.confirm_push"),function(e){
				if( e ){
					try{
						ms123.util.Remote.rpcSync( "namespace:commitAndPushRepository",{ name:r.name,message:"message" });
						this._reload();
						ms123.form.Dialog.alert(this.tr("namespacesmanager.changes_pushed"));
					}catch(e){
						ms123.form.Dialog.alert("NamespacesManager.commitAndPush:"+e.message);
						return;
					}
				}
			},this);
		},
		_pull:function(  ){
			var r = this._getRecordAtPos(this._currentTableIndex);
			ms123.form.Dialog.confirm(this.tr("namespacesmanager.confirm_pull"),function(e){
				if( e ){
					try{
						//ms123.util.Remote.rpcSync( "git:pull",{ name:r.name });
						ms123.util.Remote.rpcSync( "namespace:updateRepository",{ name:r.name });
						this._reload();
						ms123.form.Dialog.alert(this.tr("namespacesmanager.app_updated"));
					}catch(e){
						ms123.form.Dialog.alert("NamespacesManager.pull:"+e.message);
						return;
					}
				}
			},this);
		},
		_install:function(  ){
			var r = this._getRecordAtPos(this._currentTableIndex);
			ms123.form.Dialog.confirm(this.tr("namespacesmanager.confirm_install"),function(e){
				if( e ){
					try{
						ms123.util.Remote.rpcSync( "namespace:installNamespace",{ name:r.name });
						this._reload();
						ms123.form.Dialog.alert(this.tr("namespacesmanager.app_installed"));
					}catch(e){
						ms123.form.Dialog.alert("NamespacesManager.install:"+e.message);
						return;
					}
				}
			},this);
		},
		_uninstNamespace:function( archive ){
			var r = this._getRecordAtPos(this._currentTableIndex);
			ms123.form.Dialog.confirm(this.tr("namespacesmanager.confirm_uninstall")+": "+r.name,function(e){
				if( e ){
					try{
						ms123.util.Remote.rpcSync( "namespace:deleteNamespace",{ name:r.name,withRepository:false });
						//ms123.util.Remote.rpcSync( "git:deleteRepository",{ name:"."+r.name +"_data"});
						this._reload();
						ms123.form.Dialog.alert(this.tr("namespacesmanager.uninstalled"));
					}catch(e){
						ms123.form.Dialog.alert("NamespacesManager._uninstNamespace:"+e.message);
						return;
					}
					this._reload();
				}
			},this);
		},
		_deleteNamespace:function( archive ){
			var r = this._getRecordAtPos(this._currentTableIndex);
			var func = archive==false ? "delete" : "archive";
			ms123.form.Dialog.confirm(this.tr("namespacesmanager.confirm_"+func)+": "+r.name,function(e){
				if( e ){

					try{
						var t = ms123.util.Remote.rpcSync( "namespace:deleteNamespace",{ name:r.name,withRepository:true });
						this._reload();
						ms123.form.Dialog.alert(this.tr("namespacesmanager."+func+"d"));
					}catch(e){
						ms123.form.Dialog.alert("NamespacesManager.deleteNamespace:"+e.message);
						return;
					}
					this._reload();
				}
			},this);
		},
		_createPropertyEdit:function(tableColumns){
			this._propertyEditWindow = this._createPropertyEditWindow();
		},
		_createTableListener:function(table){
			this._tableModel = table.getTableModel();
			//table.addListener("dblclick", this._onDblClick, this);
			var selModel = table.getSelectionModel();
			selModel.setSelectionMode(qx.ui.table.selection.Model.SINGLE_SELECTION);
			selModel.addListener("changeSelection", function (e) {
				var index = selModel.getLeadSelectionIndex();
				var map = this._tableModel.getRowDataAsMap(index);
				var count = selModel.getSelectedCount();
				if (count == 0) {
					if( this._buttonUp ) this._buttonUp.setEnabled(false);
					if( this._buttonDown ) this._buttonDown.setEnabled(false);
					if( this._buttonEdit ) this._buttonEdit.setEnabled(false);
					if( this._buttonArchive) this._buttonArchive.setEnabled(false);
					if( this._buttonPush) this._buttonPush.setEnabled(false);
					if( this._buttonPull) this._buttonPull.setEnabled(false);
					if( this._buttonDel ) this._buttonDel.setEnabled(false);
					if( this._buttonUninst ) this._buttonUninst.setEnabled(false);
					return;
				}
				this._currentTableIndex = index;
				if( this._buttonUp ) this._buttonUp.setEnabled(true);
				if( this._buttonDown ) this._buttonDown.setEnabled(true);
				if( this._buttonEdit) this._buttonEdit.setEnabled(true);
				if( this._buttonArchive) this._buttonArchive.setEnabled(true);
				if( this._buttonUninst) this._buttonUninst.setEnabled(map._isInstalled ? true : false);
				if( this._buttonPush) this._buttonPush.setEnabled(map._isModified ? true : false);
				if( this._buttonPull) this._buttonPull.setEnabled(map._updateAvailable ? true : false);
				if( this._buttonDel ) this._buttonDel.setEnabled(true);
				if(ms123.config.ConfigManager.isRuntime()){
					if( this._buttonPush) this._buttonPush.setEnabled(false);
				}
				if(map.name == "global"){
					if( this._buttonUninst) this._buttonUninst.setEnabled(false);
				}
			}, this);
		},
		_onDblClick: function (e) {
			var selModel = this._table.getSelectionModel();
			var index = selModel.getLeadSelectionIndex();
			if( index < 0 ) return;
			var map = this._tableModel.getRowDataAsMap(index);
			 console.log("_onDblClick:"+map.name);
			var context = {
				config:"projectshell",
				name: map.name,
				rootNode : "/"
			};
			new ms123.DesktopWindow(context, ms123.shell.ProjectShell);
		},
		_createAddForm: function () {
			var formData = {
				"url_meta": {
					'type': "TextField",
					'label': this.tr("namespacesmanager.url_meta"),
					'exclude': 'namespace!=null && namespace.length>0',
					'validation': {
						required: false,
						validator: "/^[A-Za-z]([0-9A-Za-z_/:.]){6,128}$/"
					},
					'value': ""
				},
				"namespace": {
					'type': "TextField",
					'label': this.tr("namespacesmanager.namespace_name"),
					'exclude': 'url_meta!=null && url_meta.length>0',
					'validation': {
						filter: "[A-Za-z0-9_]",
						required: false,
						validator: "/^[A-Za-z]([0-9A-Za-z_]){2,32}$/"
					},
					'value': ""
				},
				"username": {
					'type': "TextField",
					'label': this.tr("namespacesmanager.username"),
					'validation': {
						filter: "[A-Za-z0-9_]",
						required: false,
						validator: "/^[A-Za-z]([0-9A-Za-z_]){2,32}$/"
					},
					'value': ""
				},
				"password": {
					'type': "TextField",
					'label': this.tr("namespacesmanager.password"),
					'value': ""
				},
				"withDataRepos": {
					'type': "Checkbox",
					'label': this.tr("namespacesmanager.with_data_repo"),
					'value': false
				}
			};
			var self = this;
			var buttons = [{
				'label': this.tr("namespacesmanager.create_namespace"),
				'icon': "icon/22/actions/dialog-ok.png",
				'callback': function (m) {
					var f = qx.util.Serializer.toJson(m);
					console.log("formData:" + f);
					var p = self._parseRepoUrl(m);
					console.log("p:",p);
					if( p == null){
						return;
					}
					try{
						ms123.util.Remote.rpcSync( "namespace:installNamespace",{name:p.namespace, url_data:p.url_data, url_meta:p.url_meta });
					}catch(e){
						ms123.form.Dialog.alert("NamespacesManager.createNamespace:"+e.message);
						return;
					}
					self._reload();
					self._propertyEditWindow.close();
				},
				'value': "save"
			}];


			var context = {};
			context.formData = formData;
			context.buttons = buttons;
			context.formLayout = [{
				id: "tab1"
			}];
			return new ms123.widgets.Form(context);
		},
		_parseRepoUrl: function (m) {
			var ret = {};
			var namespace = m.namespace;
			if( this._isEmpty( m.url_meta )){
				if( this._isEmpty( m.namespace )){
					return null;
				}
			}
			if( !this._isEmpty( m.namespace )){
				ret.namespace = m.namespace;
				return ret;
			}else{
				var u = new URL(m.url_meta);
				console.log("u:",u);
				var startName = u.pathname.lastIndexOf( "/");
				var len = u.pathname.length;
				var ext='';
				if( u.pathname.endsWith(".git")){
					ret.namespace = u.pathname.substring( startName+1, len-4);
					ext = ".git";
				}else{
					ret.namespace = u.pathname.substring( startName+1);
				}
				if( !this._isEmpty(m.password)){
					u.password = m.password;
				}
				if( !this._isEmpty(m.username)){
					u.username = m.username;
				}
				var ud = null;
				if( m.withDataRepos){
					ud = new URL(u.toString());
					console.log("startName:",startName);
					console.log("u.pathname:",u.pathname);
					ud.pathname = u.pathname.substring(0,startName+1) + ret.namespace + "_data" + ext;
					ret.url_data = ud.toString();
				}
				ret.url_meta = u.toString();
			}
			return ret;
		},
		_isEmpty: function (s) {
			if (!s || jQuery.trim(s) == '') return true;
			return false;
		},
		_load: function () {
			var nsList = null;
			try{
				nsList = ms123.util.Remote.rpcSync( "namespace:getNamespaces",{});
				console.log("namespaces:"+qx.util.Serializer.toJson(nsList));
			}catch(e){
				ms123.form.Dialog.alert("Manager._load:"+e.message);
				return;
			}
			for( var i=0; i < nsList.length; i++){
				var m = nsList[i];
				m._isModified = (m.isModified == "yes");
				m._updateAvailable = (m.updateAvailable == "yes");
				m._isInstalled = (m.isInstalled == "yes");
				m.isModified = this.tr(m.isModified);
				m.updateAvailable = this.tr(m.updateAvailable);
				m.isInstalled = this.tr(m.isInstalled);
			}
			return nsList;
		}
	}
});
