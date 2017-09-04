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
qx.Class.define("ms123.SelectableItems", {
	extend: qx.core.Object,
	include: [qx.locale.MTranslation],


	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (context) {
		this._url = context.url;
		this._varMap = context.varMap;
		if (!this._varMap) this._varMap = {};
		this._items = null;
		this._storeDesc = context.storeDesc || ms123.StoreDesc.getNamespaceDataStoreDesc();
	},

	statics: {
		NAMESPACE: "namespace"
	},

	properties: {},


	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		setVarMap: function (varMap) {
			qx.lang.Object.mergeWith(this._varMap, varMap);
console.log("setVarMap:"+varMap);
			this._items = null;
		},
		setVariable: function (name, value) {
			this._varMap[name] = value;
		},
		getItems: function () {
			if (this._items == null) {
				this._evalUrl();
			}
			return this._convert(this._items);
		},
		getItemsAsMap: function () {
			return this._list2map(this.getItems());
		},
		getMissingParamList: function () {
			return this._missingParamList;
		},
		_evalUrl: function () {

			if (this._url instanceof Array) {
				this._items = this._url;
				this._items = this._translate(this._items);
				return;
			}
			if (this._url == null) {
				this._items = [];
				return;
			}
			this._setDefaultVars();
			if (this._url.match("^delayed")) {
				this._items = this._url;
				return;
			} else if (this._url.match("^rpc:")) {
				try {
					var url = this._url.substring(4);
					url = this._supplant(url, this._varMap);
					var comma = url.indexOf(",");
					if (comma != -1) {
						var sm = url.substring(0, comma);
						sm = sm.replace("\.", ":");
						var params = url.substring(comma + 1);
						if (!params.match("^{")) {
							params = "{" + params + "}";
						}
						console.log("params:" + params);
						params = this._evalJson(params);
						this._items = ms123.util.Remote.rpcSync(sm, params);
					} else {
						this._items = ms123.util.Remote.rpcSync(url);
					}
				} catch (e) {
					//ms123.form.Dialog.alert("SelectableItems._getItems:" + url + "/" + e);
					console.error("SelectableItems._getItems:" + url + "/" + e);
					console.error(e.stack);
					this._items = [];
					return;
				}
			} else if (this._url.match("^enumeration:")) {
				try {
					var comma = this._url.indexOf(",");
					var mapping = null;
					var enumname = null;
					if (comma == -1) {
						enumname = this._url.substring("enumeration".length + 1);
					} else {
						enumname = this._url.substring("enumeration".length + 1, comma);
						var mappingstr = this._url.substring(comma + 1);
						mapping = this._evalJson(mappingstr);
					}
					this._items = ms123.util.Remote.rpcSync("enumeration:get", {
						name: enumname,
						namespace: this._varMap.namespace,
						mapping: mapping
					});
				} catch (e) {
					//ms123.form.Dialog.alert("SelectableItems._getItems:" + url + "/" + e);
					console.error("SelectableItems._getItems:" + url + "/" + e);
					console.error(e.stack);
					this._items = [];
					return;
				}
			} else if (this._url.match("^{") || this._url.match("^\\[")) {
				try {
					var url = qx.lang.Json.parse(this._url);
					if (url.enumDescription || url.enumDescription) {
						if (url.totalCount != null && url.totalCount == 0) {
							this._items = [];
							return;
						}
						this._items = this._handleEnumDescricption(url);
						if( this._items == null){
							this._items = [];
							return;
						}
					}else{
						this._items = url;
					}
				} catch (e) {
					console.error("SelectableItems.Could not parse:" + this._url + ":" + e);
				}
			} else {
				var values = this._url.split(";");
				if (values.length < 2) {
					values = this._url.split(",");
				}
				this._items = [];
				for (var j = 0; j < values.length; j++) {
					var value = values[j];
					var v = value.split(":");
					var o = {};
					if (v.length < 2) {
						o.label = v[0];
						o.value = v[0];
					} else {
						o.label = v[1];
						o.value = v[0];
					}
					this._items.push(o);
				}
			}
			this._items = this._translate(this._items);
		},
		_convert: function () {
			if (this._items == null) return [];
			if (this._items.length !== undefined) {
				return this._items;
			} else if (this._items.value == "root") { //Teamtree, muss anders geloest werden
				return this._items;
			} else {
				var newList = [];
				for (var key in this._items) {
					var val = this._items[key];
					if (typeof val === 'string') {
						if (val.match(/^[@%]/)) {
							val = this.tr(val.substring(1));
						}
						var o = {};
						o.label = val;
						o.tooltip = val;
						o.value = key;
						val = o;
					}
					newList.push(val);
				}
				return newList;
			}
		},
		_translate: function (o) {
			if (typeof o == "string") {
				if (o.match(/^[@%]/)) {
					var tr = this.tr(o.substring(1));
					if (tr) {
						o = tr;
					}
				}
				return o.toString();
			}
			for (var i in o) {
				if (typeof o[i] == "function") continue;
				o[i] = this._translate(o[i]);
			}
			return o;
		},
		_setDefaultVars: function () {
			if (!this._varMap.NAMESPACE) {
				this.setVariable("NAMESPACE", this._storeDesc.getNamespace());
			}
			if (!this._varMap.namespace) {
				this._varMap.namespace = this._varMap.NAMESPACE;
			}
			if (this._storeDesc.getNamespace() == "global") {
				this.setVariable("STORE_ID", ms123.StoreDesc.getGlobalMetaStoreDesc().getStoreId());
			} else {
				this.setVariable("STORE_ID", this._storeDesc.getStoreId());
			}
		},
		_handleEnumDescricption:function(url){
			var itemsRet=null;
			try {
				var x = url.enumDescription.split(":");
				var type = x[0];
				var name = x[1];
				var ms = "";
				var items = url.items;
				var mapping = {};
				for (var i = 0; i < items.length; i++) {
					if (items[i].mapping) {
						mapping[items[i].mapping] = items[i].colname;
					}else{
						mapping[items[i].colname] = items[i].colname;
					}
				}
				if (Object.keys(mapping).length == 0) mapping = null;
				if (type == "sw.enum") {
					itemsRet = ms123.util.Remote.rpcSync("enumeration:get", {
						name: name,
						namespace: this._varMap.namespace,
						mapping: mapping
					});
				}
				if ( type == "camelparam_route" ) {
					var ns = this._varMap.namespace;
					var lang = ms123.config.ConfigManager.getLanguage() 
					itemsRet = ms123.util.Remote.rpcSync( "camelRoute:"+ns+"."+name, {
						lang:lang
					} );
				}
				if (type == "sw.filter") {
					console.log("Namespace:"+JSON.stringify(this._varMap,null,2));
					console.log("Namespace:"+this._storeDesc);
					var storeId = this._varMap.STORE_ID || this._storeDesc.getStoreId();
					var ret = ms123.util.Remote.rpcSync("data:executeFilterByName", {
						name: name,
						params:this._varMap,
						checkParams:true,
						storeId: storeId,
						mapping: mapping
					});
					this._missingParamList=null;
					if( ret.missingParamList){
						this._missingParamList = ret.missingParamList;
						return this._url;
					}
					itemsRet = ret.rows;
				}
			} catch (e) {
				console.error("SelectableItems._handleEnumDescricption:" + url + "/" + e);
				console.error(e.stack);
				return null;
			}
			return itemsRet;
		},
		_supplant: function (s, o) {
			if (!o) return s;
			return s.replace(/[\\$@]{([^{}]*)}/g, function (a, b) {
				var r = o[b];
				return typeof r === 'string' || typeof r === 'number' ? r : a;
			});
		},
		_list2map: function (list) {
			var ret = {};
			if (list.length === undefined) {
				return ret;
			}
			for (var i = 0; i < list.length; i++) {
				var o = list[i];
				ret[o["value"]] = o;
			}
			return ret;
		},

		_getFilterParams:function(filter,paramList){
			var label = filter["label"];
			if (filter["connector"] == null && label != null) {
				label = label.toLowerCase();
				if (label.match(/^[a-z].*/)) {
					paramList.push(label);
				}
			}
			var children = filter["children"];
			for (var i = 0; i < children.length; i++) {
				var c = children[i];
				this._getFilterParams(c,paramList);
			}
		},
		_evalJson: function (text) {
			var trim = /^(\s|\u00A0)+|(\s|\u00A0)+$/g;
			text = text.replace(trim, "");
			if (/^[\],:{}\s]*$/.test(text.
								replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, "@").
								replace(/['"][^"\\\n\r]*['"]|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, "]").
								replace(/(?:^|:|,)(?:\s*\[)+/g, ":").
								replace(/\w*\s*\:/g, ":"))) {
				return (new Function("return " + text))();
			} else {
				throw ("SelectableItems.invalid JSON: " + text);
			}
		},


		toString: function () {
			return "SelectableItems:" + this._url;
		}

	}
});
