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
package org.ms123.common.camel.components.activiti;

import java.util.Map;
import java.util.HashMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.ms123.common.camel.api.CamelService;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.CamelContext;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

//@@@MS kaputt
@SuppressWarnings({"unchecked","deprecation"})
public class CamelBehaviorDefaultImpl extends org.activiti.camel.impl.CamelBehaviorDefaultImpl {

	String m_tenant, m_processDefinitionKey;

	protected void setAppropriateCamelContext(ActivityExecution execution) {
		info(this,"getProcessVariables:" + execution.getVariables());
		Map vars = execution.getVariables();
		String ns = (String) vars.get("__namespace");
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		setTenantAndName(execution);
		CamelService cs = (CamelService) lookupServiceByName(CamelService.class.getName());
		info(this,"m_tenant:" + m_tenant + "/" + ns + "/" + cs);
		//camelContextObj = cs.getCamelContext(ns);
		//info(this,"camelContextObj:" + camelContextObj);
	}

	protected ActivitiEndpoint getEndpoint(String key) {
		info(this,"getEndpoint.key:" + key);
		for (Endpoint e : camelContextObj.getEndpoints()) {
			info(this,"\tgetEndpoint.e:" + e + "/" + e.getEndpointKey());
			if (e.getEndpointKey().equals(key) && (e instanceof ActivitiEndpoint)) {
				return (ActivitiEndpoint) e;
			}
		}
		throw new RuntimeException("Activiti endpoint not defined for " + key);
	}

	protected void setTenantAndName(ActivityExecution execution) {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		ProcessEngine pe = (ProcessEngine) beans.get("processEngine");
		String processDefinitionId = ((ExecutionEntity) execution).getProcessDefinitionId();
		RepositoryService repositoryService = pe.getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
		m_tenant = processDefinition.getTenantId();
		m_processDefinitionKey = processDefinition.getKey();
		info(this,"ID:" + processDefinition.getId());
		info(this,"Name:" + processDefinition.getName());
		info(this,"Key:" + processDefinition.getKey());
	}

	public Object lookupServiceByName(String name) {
		Map beans = Context.getProcessEngineConfiguration().getBeans();
		BundleContext bc = (BundleContext) beans.get("bundleContext");
		Object service = null;
		ServiceReference sr = bc.getServiceReference(name);
		if (sr != null) {
			service = bc.getService(sr);
		}
		if (service == null) {
			throw new RuntimeException("CamelBehaviorDefaultImpl.Cannot resolve service:" + name);
		}
		return service;
	}
}
