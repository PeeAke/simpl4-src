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
 * @ignore($)
 */
qx.Class.define("ms123.graphicaleditor.plugins.shapemenu.Button", {
	extend: qx.ui.form.MenuButton,
	/******************************************************************************
	 CONSTRUCTOR
	 ******************************************************************************/
	construct: function (option) {

		if (option) {
			this.option = option;
			if (!this.option.arguments) this.option.arguments = [];
		}

		this.parentId = this.option.id ? this.option.id : null;

		// graft the button.
		var buttonClassName = this.option.caption ? "Oryx_button_with_caption" : "Oryx_button";
		this.node = ms123.oryx.Editor.graft("http://www.w3.org/1999/xhtml", $(this.parentId), ['div',
		{
			'class': buttonClassName
		}]);

		var imgOptions = {
			src: this.option.icon
		};
		if (this.option.msg) {
			imgOptions.title = this.option.msg;
		}

		if (this.option.icon) {
			//			this.imgNode = ms123.oryx.Editor.graft("http://www.w3.org/1999/xhtml", null, ['img', imgOptions]);
			this.imgNode = new qx.html.Element("img", {});
			this.imgNode.setAttribute("src", this.option.icon);
		}

		if (this.option.caption) {
			var captionNode = ms123.oryx.Editor.graft("http://www.w3.org/1999/xhtml", this.node, ['span']);
			ms123.oryx.Editor.graft("http://www.w3.org/1999/xhtml", captionNode, this.option.caption);
		}

		this.base(arguments, "");

		this.removeListener("pointerdown", this._onPointerDown);
		this.addListener("pointerdown", this.__onPointerDown);

		this.addListener("mouseover", this.hover.bind(this));
		this.addListener("mouseout", this.reset.bind(this));
		this.addListener("mouseup", this.hover.bind(this));
		this.addListener("click", this.trigger.bind(this));


		var onBubble = false;
		//this.node.addEventListener(ms123.oryx.Config.EVENT_MOUSEOVER, this.hover.bind(this), onBubble);
		//this.node.addEventListener(ms123.oryx.Config.EVENT_MOUSEOUT, this.reset.bind(this), onBubble);
		//this.node.addEventListener(ms123.oryx.Config.EVENT_MOUSEUP, this.hover.bind(this), onBubble);
		//this.node.addEventListener('click', this.trigger.bind(this), onBubble);
		this.align = this.option.align ? this.option.align : ms123.oryx.Config.SHAPEMENU_RIGHT;
		this.group = this.option.group ? this.option.group : 0;

		this.setDecorator(null);
		this.setPadding(0, 0, 0, 0);
		this.hide();

		this.isVisible = false;
		this.willShow = false;
		this.resetTimer;
		this.setAnonymous(false);
	},


	/******************************************************************************
	 PROPERTIES
	 ******************************************************************************/
	properties: {},

	/******************************************************************************
	 STATICS
	 ******************************************************************************/
	statics: {},
	/******************************************************************************
	 MEMBERS
	 ******************************************************************************/
	members: {
		__onPointerDown: function (e) {
			if (!e.isLeftPressed()) {
				return;
			}
			var menu = this.getMenu();
			if (menu) {
				if (!menu.isVisible()) {
					this.open();
				} else {
					menu.exclude();
				}
			}
			this.capture(true);
		},

		_createContentElement: function () {
			var root = new qx.html.Root(this.node);
			root.setAttribute("$$widget", this.toHashCode());
			var styles = {
				"border": '0px',
				"padding": '4px',
				"zIndex": 10,
				"box-sizing": "content-box"
			};
			root.setStyles(styles);
			root.setRoot(false);

			root.add(this.imgNode);
			root.setAttribute("qxDraggable", "on");
			root.__flush();
			return root;
		},

		setPosition: function (x, y) {
			this.doReset();
			var c = this.getContentElement().getDomElement();
			c.style.left = x + "px";
			c.style.top = y + "px";
		},

		hide: function () {
			this.node.style.display = "none";
			this.isVisible = false;
		},

		show: function () {
			this.node.style.display = "";
			this.node.style.opacity = this.opacity;
			this.isVisible = true;
		},

		showOpaque: function () {
			this.node.style.opacity = 1.0;
		},

		showTransparent: function () {
			this.node.style.opacity = this.opacity;
		},

		prepareToShow: function () {
			this.willShow = true;
		},

		prepareToHide: function () {
			this.willShow = false;
			this.hide();
		},

		setLevel: function (level) {
			if (level == 0) this.opacity = 0.3;
			else if (level == 1) this.opacity = 1.0;
			else this.opacity = 0.0;
			this.showTransparent();
		},

		reset: function (evt) {
			// Delete the timeout for hiding
			window.clearTimeout(this.resetTimer)
			this.resetTimer = window.setTimeout(this.doReset.bind(this), 100)

			if (this.option.resetcallback) {
				this.option.arguments.push(evt);
				var state = this.option.resetcallback.apply(this, this.option.arguments);
				this.option.arguments.pop();
			}
		},

		doReset: function () {
			this.node.style.border = '0px';
			this.node.style.padding = '4px';
			this.node.style.boxSizing = "content-box";
			this.node.style["MozBoxSizing"] = "content-box";
			var node = this.getContentElement().getDomElement();
			if (node.hasClassName('Oryx_down')) node.removeClassName('Oryx_down');
			if (node.hasClassName('Oryx_hover')) node.removeClassName('Oryx_hover');

		},

		isHover: function () {
			var node = this.getContentElement().getDomElement();
			return node.hasClassName('Oryx_hover') ? true : false;
		},

		hover: function (evt) {
			this.node.style.border = '2px dotted gray';
			this.node.style.borderRadius = '4px';
			this.node.style.padding = '2px';
			this.node.style.boxSizing = "content-box";
			this.node.style["MozBoxSizing"] = "content-box";
			var node = this.getContentElement().getDomElement();
			// Delete the timeout for hiding
			window.clearTimeout(this.resetTimer)
			this.resetTimer = null;

			node.addClassName('Oryx_hover');
			if (this.option.hovercallback) {
				this.option.arguments.push(evt);
				var state = this.option.hovercallback.apply(this, this.option.arguments);
				this.option.arguments.pop();
			}
		},


		trigger: function (evt) {
			if (this.option.callback) {
				this.option.arguments.push(evt);
				var state = this.option.callback.apply(this, this.option.arguments);
				this.option.arguments.pop();
			}
		},

		toString: function () {
			return "HTML-Button " + this.id + "/" + this.toHashCode();
		}
	},
	/******************************************************************************
	 DESTRUCTOR
	 ******************************************************************************/
	destruct: function () {}

});
