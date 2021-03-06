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
*/
qx.Class.define("ms123.graphicaleditor.GraphicalEditor", {
	extend: qx.ui.container.Stack,

	/**
	 * Constructor
	 */
	construct: function (context) {
		this.base(arguments);

		this._mainContainer= new qx.ui.container.Composite();
		this.add(this._mainContainer);
		this.__storeDesc = context.storeDesc;
		this._mainContainer.setLayout(new qx.ui.layout.Dock());
	},

	events: {
		"save": "qx.event.type.Data",
		"deploy": "qx.event.type.Data",
		"savedeploy": "qx.event.type.Data"
	},
	/**
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */
	members: {
		init:function ( editorType, diagramName, json, projectRoot ){
			this.context = {};
			this.zoomLevel=null;
			if (typeof json == "string" && json) {
				var 	jsonObject = qx.lang.Json.parse(json);
				this.context.resourceId = jsonObject.resourceId;
				this.zoomLevel=jsonObject.zoomLevel;
			}
			this.context.editorType = editorType;
			this.context.diagramName = diagramName;
			this._diagramName = diagramName;
			this._projectRoot = projectRoot;
			this._initOryxEditor();

			window.setTimeout(qx.lang.Function.bind(function () {
				if( this.zoomLevel){
					this._facade.view.setAFixZoomLevel(this.zoomLevel);
				}
				this._facade.importJSON(json, true,false);
			}, this), 300)
		},

		_initOryxEditor: function () {
			var editor = new ms123.oryx.Editor(this.context);
			this._oryxEditor = editor;
			this._facade = editor._getPluginFacade();
			this._facade.mainStack = this;
			this._facade.storeDesc = this.__storeDesc;
			this._facade.projectRoot = this._projectRoot;
			var shapeRepository = new ms123.graphicaleditor.plugins.ShapeRepository(this._facade,this.context.editorType);
			var shapemenu = new ms123.graphicaleditor.plugins.shapemenu.Plugin(this._facade,this.context.editorType);

			new ms123.graphicaleditor.plugins.DragDropResize(this._facade);
			this._facade.editorType = this.context.editorType;
			this._facade.diagramName = this.context.diagramName;
			if( 
					this.context.editorType == "sw.form" || 
					this.context.editorType == "sw.website" || 
					this.context.editorType == "sw.document" ){
				new ms123.graphicaleditor.plugins.Forms(this._facade);
				new ms123.graphicaleditor.plugins.Website(this._facade);
				new ms123.graphicaleditor.plugins.RowLayouting(this._facade);
				new ms123.graphicaleditor.plugins.Arrangement(this._facade, this.context.editorType);
				new ms123.graphicaleditor.plugins.SelectionFrame(this._facade);
			}
			if( this.context.editorType == "sw.process" ){
				new ms123.graphicaleditor.plugins.DragDocker(this._facade);
				new ms123.graphicaleditor.plugins.AddDocker(this._facade);
				new ms123.graphicaleditor.plugins.SelectionFrame(this._facade);
				new ms123.graphicaleditor.plugins.Arrangement(this._facade, this.context.editorType);
				new ms123.graphicaleditor.plugins.Loading(this._facade);
				new ms123.graphicaleditor.plugins.Overlay(this._facade);
				new ms123.graphicaleditor.plugins.SyntaxChecker(this._facade);
				new ms123.graphicaleditor.plugins.bpmn.Plugin(this._facade);
			}

			if( this.context.editorType == ms123.shell.Config.CAMEL_FT ){
				new ms123.graphicaleditor.plugins.DragDocker(this._facade);
				new ms123.graphicaleditor.plugins.AddDocker(this._facade);
				new ms123.graphicaleditor.plugins.SelectionFrame(this._facade);
				new ms123.graphicaleditor.plugins.Arrangement(this._facade, this.context.editorType);
				new ms123.graphicaleditor.plugins.Loading(this._facade);
				new ms123.graphicaleditor.plugins.Overlay(this._facade);
				new ms123.graphicaleditor.plugins.camel.Plugin(this._facade);
				new ms123.graphicaleditor.plugins.SyntaxChecker(this._facade);
				new ms123.graphicaleditor.plugins.ServiceTest(this._facade);
			}

			var save = new ms123.graphicaleditor.plugins.Save(this._facade, this);
			new ms123.graphicaleditor.plugins.Undo(this._facade);
			new ms123.graphicaleditor.plugins.Split(this._facade);
			this._facade.edit = new ms123.graphicaleditor.plugins.Edit(this._facade);
			this._facade.view = new ms123.graphicaleditor.plugins.View(this._facade);
			new ms123.graphicaleditor.plugins.HighlightingSelectedShapes(this._facade);
			new ms123.graphicaleditor.plugins.ShapeHighlighting(this._facade);
			new ms123.graphicaleditor.plugins.EdgeLayouter(this._facade);
			if( this.context.editorType == "sw.form" ){
				new ms123.graphicaleditor.plugins.FormEntityImport(this._facade);
			}

			var propertyPanel = new qx.ui.container.Scroll();

			this._facade.save=save;
			//var place = "right_bottom";
			//var place = "right_top";
			//var place = "left_bottom";
			var place = "right";
			//var place = "center_bottom";
			var	direction="horizontal";
			if( ["center_bottom"].indexOf(place) != -1){
				direction="vertical";
			}
			new ms123.graphicaleditor.plugins.propertyedit.Plugin(this._facade, propertyPanel, this._diagramName, direction);
			shapeRepository.setWidth( 150 );
			propertyPanel.setWidth( 230 );
			this._facade.container = { shapeRepository:shapeRepository, propertyPanel:propertyPanel,editor:editor};
			var split3 = new ms123.graphicaleditor.Split3(shapeRepository, editor, propertyPanel, this._facade.container,direction,place);

			var toolbar = new ms123.graphicaleditor.plugins.Toolbar(this._facade);
			toolbar.registryChanged(editor.getPluginsData());
			shapemenu.registryChanged(editor.getPluginsData());
			this._mainContainer.add(toolbar, {
				edge: "north"
			});
			this._mainContainer.add(split3, {
				edge: "center"
			});
			this._mainContainer.add(this._createHiddenForm(), {
				edge: "south"
			});

			this._facade.registerPluginsOnKeyEvents();
		},
		_destroy:function(){
			this._oryxEditor._removeEventListener();
			this._disposeObjects("_oryxEditor");
		},
		_createHiddenForm: function () {
			var htmlForm =
				'<form id="graphicaleditor-downloadForm" action="" method="post" accept-charset="UTF-8" target="_blank">' +
				'<input type="hidden" name="__rpc__" value="">' +
				'</form>';
			var hiddenForm = new qx.ui.embed.Html(htmlForm); 
			hiddenForm.setHeight(0);
			return hiddenForm;
		},
		fireAction: function (cmd, data) {
			this.fireDataEvent(cmd, data, null);
		}
	},
	destruct: function () {
	}
});
