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
package org.ms123.common.wamp.camel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadRuntimeException;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.Message;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.ms123.common.wamp.camel.WampClientConstants.*;
import org.ms123.common.wamp.WampClientSession;

@SuppressWarnings("unchecked")
public class WampClientProducer extends DefaultAsyncProducer {

	private static final Logger LOG = LoggerFactory.getLogger(WampClientProducer.class);
	private WampClientSession clientSession;
	private ObjectMapper objectMapper = new ObjectMapper();

	public WampClientProducer(WampClientEndpoint endpoint) {
		super(endpoint);
	}

	@Override
	public WampClientEndpoint getEndpoint() {
		return (WampClientEndpoint) super.getEndpoint();
	}

	@Override
	public boolean process(Exchange exchange, AsyncCallback callback) {
		String namespace = getEndpoint().getCamelContext().getName().split("/")[0];
		this.clientSession.publish(namespace + "." + getEndpoint().getTopic(),null,buildResponse(getPublishData(exchange)));
		return true;
	}

	protected void doStart() throws Exception {
		String namespace = getEndpoint().getCamelContext().getName().split("/")[0];
		debug("######WampProducer.doSart:" + namespace + "." + getEndpoint().getTopic());
		super.doStart();
		this.clientSession = getEndpoint().createWampClientSession("realm1");
	}

	protected void doStop() throws Exception {
		String namespace = getEndpoint().getCamelContext().getName().split("/")[0];
		debug("######WampProducer.doStop:" + namespace + "." + getEndpoint().getTopic());
		this.clientSession.close();
		super.doStop();
	}
	private ObjectNode buildResponse(final Object methodResult) {
		ObjectNode node = null;
		if( methodResult instanceof Map ){
			node = this.objectMapper.valueToTree(methodResult);
		}else{
			node = this.objectMapper.createObjectNode();
			node.putPOJO("result", methodResult);
		}
		return node;
	}

	private Object getPublishData(Exchange exchange) {
		String publishSpec = getEndpoint().getPublish();
		List<String> publishHeaderList = getEndpoint().getPublishHeaderList();
		Object camelBody = ExchangeHelper.extractResultBody(exchange, null);
		if ("body".equals(publishSpec)) {
			return ExchangeHelper.extractResultBody(exchange, null);
		} else if ("headers".equals(publishSpec)) {
			Map<String, Object> camelVarMap = new HashMap();
			for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
				if (publishHeaderList.size() == 0 || publishHeaderList.contains(header.getKey())) {
					camelVarMap.put(header.getKey(), header.getValue());
				}
			}
			return camelVarMap;
		} else if ("bodyAndHeaders".equals(publishSpec)) {
			Map<String, Object> camelVarMap = new HashMap();
			if (camelBody instanceof Map<?, ?>) {
				Map<?, ?> camelBodyMap = (Map<?, ?>) camelBody;
				for (@SuppressWarnings("rawtypes")
				Map.Entry e : camelBodyMap.entrySet()) {
					if (e.getKey() instanceof String) {
						camelVarMap.put((String) e.getKey(), e.getValue());
					}
				}
			} else {
				camelVarMap.put("body", camelBody);
			}
			for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
				if (publishHeaderList.size() == 0 || publishHeaderList.contains(header.getKey())) {
					camelVarMap.put(header.getKey(), header.getValue());
				}
			}
			return camelVarMap;
		}
		return null;
	}

	protected void debug(String msg, Object... args) {
		System.out.println(MessageFormatter.arrayFormat(msg, varargsToArray(args)).getMessage());
		LOG.debug(msg, args);
	}

	protected void info(String msg, Object... args) {
		System.out.println(MessageFormatter.arrayFormat(msg, varargsToArray(args)).getMessage());
		LOG.info(msg, args);
	}

	private Object[] varargsToArray(Object... args) {
		Object[] ret = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			ret[i] = args[i];
		}
		return ret;
	}
}
