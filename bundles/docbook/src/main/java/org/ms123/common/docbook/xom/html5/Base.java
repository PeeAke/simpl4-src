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
package org.ms123.common.docbook.xom.html5;

import nu.xom.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class Base {

	private String m_name;
	private String m_id;
	private String m_tabindex;
	private String m_class;
	private Base m_parent;

	private List<Object> m_content = new ArrayList();

	protected List<Attribute> m_attributes = new ArrayList();

	public Base(String name) {
		m_name = name;
	}
	public Base(String name,String id,String clazz){
		m_name = name;
		m_id = id;
		m_class = clazz;
	}

	public Base getParent(){
		return m_parent;
	}
	public void setParent(Base parent){
		m_parent=parent;
	}

	public List<Object> getContent() {
		return m_content;
	}

	public void add(Base e) {
		m_content.add(e);
		e.setParent(this);
	}

	public void add(String e) {
		m_content.add(e);
	}
	public void add(Element e){
		m_content.add(e);
	}
	public void add(Node e){
		m_content.add(e);
	}
	public void setId(String id){
		m_id = id;
	}
	public void setClass(String c){
		m_class = c;
	}
	public void setTabindex(String t){
		m_tabindex = t;
	}
	public void addAttribute( String name, String value){
		m_attributes.add( new Attribute(name, value));
	}

	public Element toXom() {
		Element e = new Element(m_name, "http://www.w3.org/1999/xhtml");
		for (Attribute a : m_attributes) {
			e.addAttribute(a);
		}
		if( m_id != null){
			e.addAttribute(new Attribute("id",m_id));
		}
		if( m_class != null){
			e.addAttribute(new Attribute("class",m_class));
		}
		if( m_tabindex != null){
			e.addAttribute(new Attribute("tabindex",m_tabindex));
		}
		for (Object o : m_content) {
			if (o instanceof String) {
				e.appendChild(new Text((String) o));
			}else if (o instanceof Node) {
				e.appendChild((Node)o);
			}else if (o instanceof ProcessingInstruction) {
				e.appendChild((ProcessingInstruction)o);
			} else {
				Base b = (Base) o;
				e.appendChild(b.toXom());
			}
		}
		return e;
	}
	public Map<String,Object> toMap() {
		Map<String,Object> m = new HashMap();
		m.put("name", m_name);
		for (Attribute a : m_attributes) {
			m.put( a.getLocalName(), a.getValue());
		}
		if( m_id != null){
			m.put( "id", m_id);
		}
		if( m_class != null){
			m.put( "class", m_class);
		}
		if( m_tabindex != null){
			m.put( "tabindex", m_tabindex);
		}
		List children = new ArrayList();
		m.put("children", children);
		for (Object o : m_content) {
			Base b = (Base) o;
			children.add( b.toMap());
		}
		return m;
	}
	public String toString(){
		return "Name:"+m_name;
	}
}
