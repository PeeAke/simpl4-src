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
/*
*/
qx.Class.define("ms123.settings.CommonPropertyEdit", {
	extend: ms123.settings.PropertyEdit,

	/**
	 *****************************************************************************
	 CONSTRUCTOR
	 *****************************************************************************
	 */

	construct: function (facade) {
		this.base(arguments, facade);
	},

	/**
	 *****************************************************************************
	 PROPERTIES
	 *****************************************************************************
	 */
	/**
	 *****************************************************************************
	 MEMBERS
	 *****************************************************************************
	 */

	members: {
		_createEditForm: function () {
			var formData = {
				"multiple_tabs":{
					'type': "CheckBox",
					'label': this.tr("data.entity.multiple_tabs"),
					'value': false
				},
				"sidebar":{
					'type': "CheckBox",
					'label': this.tr("data.entity.sidebar"),
					'value': false
				},
				"exclusion_list":{
					'type': "CheckBox",
					'label': this.tr("data.entity.exclusion_list"),
					'value': false
				},
				"multi_add":{
					'type': "CheckBox",
					'label': this.tr("data.entity.multi_add"),
					'value': false
				},
				"add_self_to_subpanel":{
					'type': "CheckBox",
					'label': this.tr("data.entity.add_self_to_subpanel"),
					'value': false
				},
				"not_in_menu":{
					'type': "CheckBox",
					'label': this.tr("data.entity.not_in_menu"),
					'value': false
				},
				"teams_in_subpanel":{
					'type': "CheckBox",
					'label': this.tr("data.entity.teams_in_subpanel"),
					'value': false
				},
				"no_add_del_in_master":{
					'type': "CheckBox",
					'label': this.tr("data.entity.no_add_del_in_master"),
					'value': false
				},
				"state_select":{
					'type': "CheckBox",
					'label': this.tr("data.entity.stateselect"),
					'value': false
				},
				"select_distinct":{
					'type': "CheckBox",
					'label': this.tr("data.entity.selectdistinct"),
					'value': true
				},
				"no_resultset_count":{
					'type': "CheckBox",
					'label': this.tr("data.entity.no_resultset_count"),
					'value': false
				},
				"title_expression":{
					'type': "Textfield",
					'label': this.tr("data.entity.titel_expression"),
					'value': ""
				},
				"record_validation":{
					'type': "resourceselector",
					'config':{
						'type':'sw.camel'
					},
					'label': this.tr("data.entity.record_validation"),
					'value': null
				},
				"customServiceRead": {
					'type': "resourceselector",
					'label': this.tr("settings.views.propertyedit.customServiceRead"),
					'config':{
						'type':'sw.camel'
					},
					'value': null
				},
				"customServiceUpdate": {
					'type': "resourceselector",
					'label': this.tr("settings.views.propertyedit.customServiceUpdate"),
					'config':{
						'type':'sw.camel'
					},
					'value': null
				},
				"customServiceInsert": {
					'type': "resourceselector",
					'label': this.tr("settings.views.propertyedit.customServiceInsert"),
					'config':{
						'type':'sw.camel'
					},
					'value': null
				},
				"customServiceDelete": {
					'type': "resourceselector",
					'label': this.tr("settings.views.propertyedit.customServiceDelete"),
					'config':{
						'type':'sw.camel'
					},
					'value': null
				},
				"workflow_list":{
					'type': "GridInput",
					'label': this.tr("data.entity.workflow_list"),
					"config": {
          		"totalCount": 2,
          		"items": [
								{
									"colname": "workflow",
									"display": "Workflow",
									"type": "resourceselector",
									"config":{
										"type":"sw.process"
									}
								},
								{
									"colname": "filtername",
									"display": "Filter",
									"type": "resourceselector",
									"config":{
										"type":"sw.filter"
									}
								},
								{
									"colname": "menuname",
									"display": "Menuname",
									"type": "text",
									"constraints": "{\"Pattern\":[true,\".*\"],\"Size\":[false,null,null],\"NotBlank\":[false],\"CreditCardNumber\":[false],\"URL\":[false],\"Email\":[false]}"
								}
							]
					},
					'value': ""
				}
			}
			this._form = new ms123.form.Form({
				"tabs": [{
					id: "tab1",
					layout: "single",
					lineheight: 20
				}],
				"formData": formData,
				"allowCancel": true,
				"inWindow": false,
				"buttons": [],
				"callback": function (m, v) {},
				"context": null
			});
			return this._form;
		}
	}
});
