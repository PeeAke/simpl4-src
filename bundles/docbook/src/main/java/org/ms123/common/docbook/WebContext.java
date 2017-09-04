/**
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
package org.ms123.common.docbook;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import nu.xom.*;
import org.zkoss.zuss.Zuss;
import org.zkoss.zuss.Resolver;
import org.zkoss.zuss.Locator;
import org.zkoss.zuss.metainfo.ZussDefinition;
import org.zkoss.zuss.impl.in.Parser;
import org.zkoss.zuss.impl.out.Translator;
import org.ms123.common.docbook.xom.html5.*;
import org.ms123.common.data.api.DataLayer;
import org.osgi.framework.BundleContext;
import eu.bitwalker.useragentutils.*;


@SuppressWarnings("unchecked")
public	class WebContext extends Context{
	List<Style> styleList = new ArrayList();

	List<Script> scriptList = new ArrayList();
	Stack<Map> propertyStack = new Stack();

	WebContext(String namespace, String pageName, boolean isStartpage, Map paramsIn, Map paramsOut) {
		super(namespace,pageName,isStartpage,paramsIn, paramsOut);
	}

	void addCss(Map properties) throws Exception {
		String css = getString(properties, "ws_css", null);
		if (css == null || css.trim().length() == 0){
			return;
		}
		int size = propertyStack.size();
		System.out.println("propertyStack:");
		for (int i = (size - 1); i >= 0; i--) {
			System.out.println(propertyStack.elementAt(i).get("ws_id"));
		}
		ZussResolver zr = new ZussResolver(propertyStack);
		ZussDefinition zdef = Zuss.parse(new StringReader(css), zr, null);
		StringWriter sw = new StringWriter();
		List<String> sel = null;
		String id = getString(properties, "ws_id", null);
		if( id != null){
			sel = new ArrayList();
			sel.add(  "#"+id);
		}
		Zuss.translate(zdef, sw, zr,sel);
		Style style = new Style();
		style.setStyle(sw.toString());
		styleList.add(style);
	}

	void addJs(Map properties) throws Exception {
		String js = (String) properties.get("ws_js");
		if (js == null || js.trim().length() == 0) {
			return;
		}
		Script script = new Script();
		script.setScript(js);
		scriptList.add(script);
	}

	class ZussResolver implements Locator, Resolver {

		private Stack<Map> m_propertyStack;

		ZussResolver(Stack<Map> properties) {
			m_propertyStack = properties;
		}

		@Override
		public Reader getResource(String name) throws IOException {
			File file = new File(name);
			System.out.println("WebsiteBuilder.getResource:" + name);
			//String _dir = ".";
			//final InputStream is = new java.io.FileInputStream( file.isAbsolute() ? file: new File(_dir, name));
			//return new InputStreamReader(is, "UTF-8");
			return null;
		}

		public Object getVariable(String name) {
			name = name.toLowerCase();
			int size = m_propertyStack.size();
			System.out.println("propertyStack:");
			for (int i = (size - 1); i >= 0; i--) {
				Object x = m_propertyStack.elementAt(i).get("css_" + name);
				if (x == null) {
					x = m_propertyStack.elementAt(i).get(name);
				}
				int ul = name.indexOf("_");
				if (x == null && ul != -1) {
					x = m_propertyStack.elementAt(i).get(name.substring(ul + 1));
				}
				if (x != null) {
					System.out.println("WebsiteBuilder.getVariable:" + name + "->" + x);
					return x;
				}
			}
			System.out.println("WebsiteBuilder.getVariable:" + name + "-> null");
			return null;
		}

		public Method getMethod(String name) {
			System.out.println("WebsiteBuilder.getMethod:" + name);
			return null;
		}
	}
}
