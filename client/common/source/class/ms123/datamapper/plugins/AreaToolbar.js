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
	* @ignore($A)
*/
qx.Class.define("ms123.datamapper.plugins.AreaToolbar", {
	extend: ms123.baseeditor.Toolbar,
	include: [qx.locale.MTranslation],

	/**
	 * Constructor
	 */
	construct: function (facade,context) {
		this.base(arguments,facade);
		context.tree.getTree().addListener("changeSelection", function(e){
			console.log("TreeSelectionChanged");
			this.onUpdate({});
		}, this);
		this.setMinHeight(24);
	},

	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {

	}
});
