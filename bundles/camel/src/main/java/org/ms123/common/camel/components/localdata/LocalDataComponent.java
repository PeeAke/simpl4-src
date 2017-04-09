/**
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
package org.ms123.common.camel.components.localdata;

import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.ms123.common.data.api.DataLayer;
import org.apache.camel.CamelContext;

/**
 * Represents the component that manages {@link LocalDataEndpoint}.
 */
public class LocalDataComponent extends DefaultComponent {

	private DataLayer m_dataLayer;
	private DataLayer m_dataLayerOrientDb;

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		Endpoint endpoint = new LocalDataEndpoint(uri, this);
		setProperties(endpoint, parameters);
		return endpoint;
	}

	@Override
	public void setCamelContext(CamelContext context) {
		super.setCamelContext(context);
		m_dataLayer = getByType(context, DataLayer.class);
		m_dataLayerOrientDb = getByName( context, DataLayer.class, "dataLayerOrientdb");
	}

	private <T> T getByType(CamelContext ctx, Class<T> kls) {
		return kls.cast(ctx.getRegistry().lookupByName(kls.getName()));
	}
	private <T> T getByName(CamelContext ctx, Class<T> kls, String name) {
		return kls.cast(ctx.getRegistry().lookupByName( name ));
	}

	protected DataLayer getDataLayer() {
		return m_dataLayer;
	}
	protected DataLayer getDataLayerOrientDB() {
		return m_dataLayerOrientDb;
	}
}
