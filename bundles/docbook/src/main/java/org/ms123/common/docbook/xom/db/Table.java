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
package org.ms123.common.docbook.xom.db;

import nu.xom.*;

public class Table extends Informaltable {
	private Title m_title;

	public Table() {
		super("table");
	}

	public void setTitle(Title title) {
		m_title = title;
	}

	public Title getTitle() {
		return m_title;
	}
	public Element toXom() {
		Element e = super.toXom();
		if( m_title != null){
			e.appendChild(m_title.toXom());
		}
		return e;
	}
}
