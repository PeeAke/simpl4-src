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
package org.ms123.common.camel.components.activiti;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.camel.Exchange;
import org.apache.camel.MessageHistory;
import org.apache.camel.impl.DefaultProducer;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.workflow.api.WorkflowService;
import org.ms123.common.system.thread.ThreadContext;
import org.ms123.common.camel.api.ExchangeUtils;
import org.ms123.common.camel.trace.ExchangeFormatter;
import org.ms123.common.camel.trace.MessageHelper;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import flexjson.*;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.runtime.InvokerHelper;
import static org.ms123.common.system.history.HistoryService.HISTORY_MSG;
import static org.ms123.common.system.history.HistoryService.HISTORY_KEY;
import static org.ms123.common.system.history.HistoryService.HISTORY_TYPE;
import static org.ms123.common.system.history.HistoryService.HISTORY_HINT;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_START_PROCESS_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_JOB_EXCEPTION;
import static org.ms123.common.system.history.HistoryService.HISTORY_TOPIC;

import static org.ms123.common.system.history.HistoryService.ACTIVITI_CAMEL_CORRELATION_TYPE;
import static org.ms123.common.system.history.HistoryService.ACC_ACTIVITI_ID;
import static org.ms123.common.system.history.HistoryService.ACC_ROUTE_INSTANCE_ID;
import static org.ms123.common.system.history.HistoryService.HISTORY_ACTIVITI_ACTIVITY_KEY;
import static org.ms123.common.system.history.HistoryService.CAMEL_ROUTE_DEFINITION_KEY;

import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_EXECUTION_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_BUSINESS_KEY;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_PROCESS_INSTANCE_ID;
import static org.ms123.common.workflow.api.WorkflowService.WORKFLOW_ACTIVITY_NAME;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

@SuppressWarnings("unchecked")
public class ActivitiProducer extends org.activiti.camel.ActivitiProducer implements ActivitiConstants {

	protected JSONSerializer js = new JSONSerializer();
	private RuntimeService runtimeService;
	private RepositoryService repositoryService;
	private PermissionService permissionService;
	private WorkflowService workflowService;
	private CamelService camelService;

	private ActivitiOperation operation;
	private ActivitiEndpoint endpoint;

	private Map<String, String> processCriteria;
	private String activityId;
	private String namespace;
	private String headerFields;
	private String variableNames;
	private String businessKey;
	private String signalName;
	private String messageName;
	private boolean isSendSignal;
	private boolean isSendMessage;

	private Map options;
	private String activitiKey;

	public ActivitiProducer(ActivitiEndpoint endpoint, WorkflowService workflowService, PermissionService permissionService) {
		super(endpoint, -1, 100);
		this.endpoint = endpoint;
		this.permissionService = permissionService;
		this.workflowService = workflowService;
		this.runtimeService = workflowService.getProcessEngine().getRuntimeService();
		this.repositoryService = workflowService.getProcessEngine().getRepositoryService();
		this.camelService = (CamelService) endpoint.getCamelContext().getRegistry().lookupByName(CamelService.class.getName());
		info(this,"ActivitiProducer.camelService:" + this.camelService);
		setRuntimeService(this.runtimeService);
		String[] path = endpoint.getEndpointKey().split(":");
		this.operation = ActivitiOperation.valueOf(path[1].replace("//", ""));
		this.namespace = endpoint.getNamespace();
		this.signalName = endpoint.getSignalName();
		this.headerFields = endpoint.getHeaderFields();
		this.variableNames = endpoint.getVariableNames();
		this.businessKey = endpoint.getBusinessKey();
		this.messageName = endpoint.getMessageName();
		this.isSendSignal = endpoint.isSendSignal();
		this.isSendMessage = endpoint.isSendMessage();
		this.processCriteria = endpoint.getProcessCriteria();
		this.options = endpoint.getOptions();
	}

	public void process(Exchange exchange) throws Exception {
		this.activityId = getString(exchange, ACTIVITY_ID, this.activityId);
		this.namespace = getString(exchange, NAMESPACE, this.namespace);
		if (isEmpty(this.namespace)) {
			this.namespace = (String) exchange.getContext().getRegistry().lookupByName("namespace");
		}
		if (isEmpty(this.namespace)) {
			this.namespace = (String) exchange.getProperty("_namespace");
		}
		info(this,"ActivitiProducer.operation:" + this.operation+"/namespace:"+this.namespace);
		invokeOperation(this.operation, exchange);
		/* final CamelService camelService = (CamelService) exchange.getContext().getRegistry().lookupByName(CamelService.class.getName());
		 exchange.setProperty(WORKFLOW_ACTIVITY_NAME, this.activityId);
		 camelService.saveHistory(exchange);
		 saveActivitiCamelCorrelationHistory(exchange);
		 printHistory(exchange);*/
	}

	/**
	 * Entry method that selects the appropriate operation and executes it
	 * @param operation
	 * @param exchange
	 * @throws Exception
	 */
	private void invokeOperation(ActivitiOperation operation, Exchange exchange) throws Exception {
		switch (operation) {
		case sendMessageEvent:
			doSendMessageEvent(exchange);
			break;
		case sendSignalEvent:
			doSendSignalEvent(exchange);
			break;
		case sendSignalToReceiveTask:
			doSendSignalToReceiveTask(exchange);
			break;
		case startProcess:
			doStartProcess(exchange);
			break;
		case getProcessVariables:
			doGetProcessVariables(exchange);
			break;
		default:
			throw new RuntimeException("ActivitiProducer.Operation not supported. Value: " + operation);
		}
	}

	private void doGetProcessVariables(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange, false );
		if( processInstanceList.size() > 1 ){
			throw new RuntimeException("ActivitiProducer.doGetProcessVariables.more as one process queried: " + processInstanceList);
		}

		ProcessInstance pi = processInstanceList.get(0);	
		info(this,"doGetProcessVariables.processInstance1:" + pi);
		pi = 	runtimeService.createProcessInstanceQuery().includeProcessVariables().processInstanceId( pi.getId()).singleResult();
		info(this,"doGetProcessVariables.processInstance2:" + pi);
		info(this,"doGetProcessVariables.variables:" + pi.getProcessVariables());
		String variableNames = getString(exchange, "variableNames", this.variableNames);
		if( isEmpty(variableNames)){
			exchange.getIn().setBody(pi.getProcessVariables());
		}else{
			List<String> nameList = Arrays.asList(variableNames.split(","));
			info(this,"doGetProcessVariables:nameList:" + nameList);
			Map<String,Object> vars = pi.getProcessVariables();
			Map<String,Object> ret = new HashMap();
			for (Map.Entry<String, Object> entry : vars.entrySet()) {
				if( nameList.indexOf( entry.getKey()) > -1){
					info(this,"doGetProcessVariables:var:" + entry.getKey()+"="+entry.getValue());
					ret.put( entry.getKey(), entry.getValue());
				}
			}
			exchange.getIn().setBody(ret);
		}
	}

	private void doSendMessageEvent(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange);
		doSendMessageEvent(exchange, processInstanceList);
	}
	private void doSendMessageEvent(Exchange exchange,List<ProcessInstance> processInstanceList) {
		String messageName = getString(exchange, "messageName", this.messageName);
		info(this,"processInstanceList:" + processInstanceList);
		info(this,"messageName:" + messageName);
		Map<String,Object> processVariables = getProcessVariables(exchange);
		if( processInstanceList != null){
			for( ProcessInstance pi : processInstanceList){
				info(this,"PI:"+pi.getId());
				Execution execution = runtimeService.createExecutionQuery() .executionId(pi.getId()).messageEventSubscriptionName(messageName).singleResult(); 
				info(this,"\tExecution:"+execution);
				if( execution != null){
					info(this,"doSendMessageEvent:"+messageName+"/"+execution.getId());
					if( processVariables == null){
						this.runtimeService.messageEventReceived( messageName, execution.getId());
					}else{
						this.runtimeService.messageEventReceived( messageName, execution.getId(), processVariables);
					}
				}else{
					info(this,"doSendMessageEvent.message("+messageName+") not found in process:"+pi.getId());
					List<Execution> execs = runtimeService.createExecutionQuery() .messageEventSubscriptionName(messageName).list(); 
				}
			}
		}
	}

	private void doSendSignalEvent(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange);
		doSendSignalEvent(exchange, processInstanceList);
	}
	private void doSendSignalEvent(Exchange exchange,List<ProcessInstance> processInstanceList) {
		String signalName = getString(exchange, "signalName", this.signalName);
		info(this,"processInstanceList:" + processInstanceList);
		Map<String,Object> processVariables = getProcessVariables(exchange);
		if( processInstanceList == null){
			info(this,"doSendSignalEvent:"+signalName);
			if( processVariables == null){
				this.runtimeService.signalEventReceived( signalName);
			}else{
				this.runtimeService.signalEventReceived( signalName, processVariables);
			}
		}else{
			for( ProcessInstance pi : processInstanceList){
				Execution execution = runtimeService.createExecutionQuery() .executionId(pi.getId()).signalEventSubscriptionName(signalName).singleResult(); 
				info(this,"\tExecution:"+execution);
				if( execution != null){
					info(this,"doSendSignalEvent:"+signalName+"/"+execution.getId());
					if( processVariables == null){
						this.runtimeService.signalEventReceived( signalName, execution.getId());
					}else{
						this.runtimeService.signalEventReceived( signalName, execution.getId(), processVariables);
					}
				}else{
					info(this,"doSendSignalEvent.signal("+signalName+") not found in process:"+pi.getId());
					List<Execution> execs = runtimeService.createExecutionQuery() .signalEventSubscriptionName(signalName).list(); 
					info(this,"doSendSignalEvent.allExecutions("+signalName+"):"+execs);
				}
			}
		}
	}

	private void doSendSignalToReceiveTask(Exchange exchange) {
		List<ProcessInstance> processInstanceList = getProcessInstances(exchange);
		for( ProcessInstance pi : processInstanceList){
			signal(exchange, pi);
		}
	}

	private void doStartProcess(Exchange exchange) {
		boolean isSendSignal = getBoolean(exchange, "sendSignal", this.isSendSignal);
		boolean isSendMessage = getBoolean(exchange, "sendMessage", this.isSendMessage);
		List<ProcessInstance> processInstanceList=null;
		if( isSendMessage || isSendSignal ){
			processInstanceList = getProcessInstances(exchange, true, false);
		}
		if( !isEmpty(processInstanceList)){
			if( isSendMessage){
				doSendMessageEvent(exchange, processInstanceList);
			}else if( isSendSignal){
				doSendSignalEvent(exchange, processInstanceList);
			}
		}else{
			ProcessInstance pi = executeProcess(exchange);
			info(this,"ProcessInstance:" + pi);
			if (pi != null) {
				this.activitiKey += "/" + pi.getId();
				info(this,"m_activitiKey:" + this.activitiKey);
				exchange.getOut().setBody(pi.getId());
			}
		}
	}

	private void signal(Exchange exchange, ProcessInstance execution) {
		String ns = (String) exchange.getContext().getRegistry().lookupByName("namespace");
		this.activitiKey += "/" + execution.getId() + "/" + this.activityId;
		if (execution == null) {
			throw new RuntimeException("Couldn't find activityId " + this.activityId + " for processId " + execution.getId());
		}
		Map vars = execution.getProcessVariables();
		Map exVars = ExchangeUtils.prepareVariables(exchange, true, true, true);
		Map props = exchange.getProperties();
		Map headers = exchange.getIn().getHeaders();
		this.runtimeService.setVariables(execution.getId(), getCamelVariablenMap(exchange));
		new SignalThread(ns, getProcessDefinition(execution), execution, exchange).start();
	}

	private class SignalThread extends Thread {
		Execution execution;
		ProcessDefinition processDefinition;
		Exchange exchange;
		Map options;

		String ns;

		public SignalThread(String ns, ProcessDefinition pd, ProcessInstance exec, Exchange exchange) {
			this.execution = exec;
			this.exchange = exchange;
			this.options = options;
			this.ns = ns;
			this.processDefinition = pd;
		}

		public void run() {
			ThreadContext.loadThreadContext(ns, "admin");
			permissionService.loginInternal(ns);
			while (true) {
				try {
					info(this,"SignalThread.sending signal to:" + execution.getId());
					runtimeService.signal(execution.getId(), this.options);
				} catch (org.activiti.engine.ActivitiOptimisticLockingException e) {
					info(this,"SignalThread:" + e);
					try {
						Thread.sleep(100L);
					} catch (Exception x) {
					}
					continue;
				} catch (Exception e) {
					createLogEntry(exchange, this.processDefinition, e);
					if (e instanceof RuntimeException) {
						throw (RuntimeException) e;
					} else {
						throw new RuntimeException(e);
					}
				} finally {
					ThreadContext.getThreadContext().finalize(null);
				}
				break;
			}
		}
	}

	private ProcessInstance executeProcess(Exchange exchange) {
		Map<String, Object> vars = getProcessVariables(exchange);//getCamelVariablenMap(exchange);
		info(this,"ExecuteProcess:criteria:" + this.processCriteria);
		info(this,"ExecuteProcess:vars:" + vars);
		ThreadContext.loadThreadContext(this.namespace, "admin");
		this.permissionService.loginInternal(this.namespace);
		try {
			ProcessInstanceBuilder pib = this.runtimeService.createProcessInstanceBuilder();
			String processDefinitionId = getString(exchange, PROCESS_DEFINITION_ID, this.processCriteria.get(PROCESS_DEFINITION_ID));
			if (!isEmpty(processDefinitionId)) {
				info(this,"ExecuteProcess:processDefinitionId:" + processDefinitionId);
				pib.processDefinitionId(processDefinitionId);
			}
			String processDefinitionKey = getString(exchange, PROCESS_DEFINITION_KEY, this.processCriteria.get(PROCESS_DEFINITION_KEY));
			if (!isEmpty(processDefinitionKey)) {
				info(this,"ExecuteProcess:processDefinitionKey:" + processDefinitionKey);
				pib.processDefinitionKey(processDefinitionKey);
			}
			String businessKey = getString(exchange, BUSINESS_KEY, this.businessKey);
			if (!isEmpty(businessKey)) {
				info(this,"ExecuteProcess:businessKey:" + businessKey);
				pib.businessKey(businessKey);
			}

			for (Map.Entry<String, Object> entry : vars.entrySet()) {
				info(this,"ExecuteProcess:var:" + entry.getKey()+"="+entry.getValue());
				pib.addVariable(entry.getKey(), entry.getValue());
			}
			info(this,"ExecuteProcess:tenant:" + this.namespace);
			pib.tenantId(this.namespace);
			return pib.start();
		} finally {
			ThreadContext.getThreadContext().finalize(null);
			info(this,"EndProcess:" + this.processCriteria + "/" + this.namespace);
		}
	}

	private GroovyClassLoader groovyClassLoader = null;
	private Map<String,Script> scriptCache = new HashMap();
	private Script parse(String expr) {
		if( groovyClassLoader == null){
			ClassLoader parentLoader = this.getClass().getClassLoader();
			groovyClassLoader =   new GroovyClassLoader(parentLoader,new CompilerConfiguration());
		}
		try{
			GroovyCodeSource gcs = new GroovyCodeSource( expr, "script", "groovy/shell");
			return InvokerHelper.createScript(groovyClassLoader.parseClass(gcs,false), new Binding());
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("ActivitiProducer.parse:"+e.getMessage()+" -> "+ expr);
		}
	}

	private String eval2(String expr, Map<String,Object> vars) {
		info(this,"--> eval_in:" + expr+",vars:"+vars);
		Object result = expr;
		Script script = scriptCache.get(expr);
		if( script == null){
			script = parse(expr);
			scriptCache.put(expr, script);
		}
		script.setBinding(new Binding(vars));
		try{
			result = script.run();
		}catch(Exception e){
			String error = org.ms123.common.utils.Utils.formatGroovyException(e, expr);
			info(this,"ActivitiProducer.eval:"+error);
		}
		info(this,"<-- eval_out:" + result);
		return String.valueOf(result);
	}

	private String eval(String str, Map<String,Object> scope) {
		int countRepl = 0;
		int countPlainStr = 0;
		Object replacement = null;
		String newString = "";
		int openBrackets = 0;
		int first = 0;
		for (int i = 0; i < str.length(); i++) {
			if (i < str.length() - 2 && str.substring(i, i + 2).compareTo("${") == 0) {
				if (openBrackets == 0) {
					first = i + 2;
				}
				openBrackets++;
			} else if (str.charAt(i) == '}' && openBrackets > 0) {
				openBrackets -= 1;
				if (openBrackets == 0) {
					countRepl++;
					replacement = eval2(str.substring(first, i), scope);
					newString += replacement;
				}
			} else if (openBrackets == 0) {
				newString += str.charAt(i);
				countPlainStr++;
			}
		}
		if (countRepl == 1 && countPlainStr == 0) {
			return String.valueOf(replacement);
		} else {
			return newString;
		}
	}

	private Map<String,Object> getProcessVariables(Exchange exchange){
		List<String> headerList=null;
		String headerFields = getString(exchange, "headerFields", this.headerFields);
		if( !isEmpty(headerFields)){
			headerList = Arrays.asList(headerFields.split(","));
		}else{
			headerList = new ArrayList();
		}
		Map<String,Object> processVariables = new HashMap();
		for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
			if( headerList.size()==0 || headerList.contains( header.getKey())){
				if( header.getValue() instanceof Map){
					processVariables.putAll((Map)header.getValue());
				}else{
					processVariables.put(header.getKey(), header.getValue());
				}
			}
		}
		return processVariables;
	}

	private List<ProcessInstance> getProcessInstances(Exchange exchange) {
		return getProcessInstances(exchange, true, true );
	}
	private List<ProcessInstance> getProcessInstances(Exchange exchange, boolean childProcesses) {
		return getProcessInstances(exchange, childProcesses, true );
	}
	private List<ProcessInstance> getProcessInstances(Exchange exchange, boolean childProcesses, boolean exception) {
		Map<String, Object> vars = getCamelVariablenMap(exchange);

		boolean hasCriteria = false;
		info(this,"findExecution:processCriteria:" + this.processCriteria + "/activityId:" + this.activityId);
		info(this,"findExecution:vars:" + vars);
		ExecutionQuery eq = this.runtimeService.createExecutionQuery();
		String processDefinitionId = getString(exchange, PROCESS_DEFINITION_ID, this.processCriteria.get(PROCESS_DEFINITION_ID));
		if (!isEmpty(processDefinitionId)) {
			eq.processDefinitionId(trimToEmpty(eval(processDefinitionId, vars)));
			info(this,"getProcessInstances.processDefinitionId:"+trimToEmpty(eval(processDefinitionId,vars)));
			hasCriteria=true;
		}
		String processDefinitionKey = getString(exchange, PROCESS_DEFINITION_KEY, this.processCriteria.get(PROCESS_DEFINITION_KEY));
		if (!isEmpty(processDefinitionKey)) {
			eq.processDefinitionKey(trimToEmpty(eval(processDefinitionKey,vars)));
			info(this,"getProcessInstances.processDefinitionKey:"+trimToEmpty(eval(processDefinitionKey,vars)));
			hasCriteria=true;
		}
		String processInstanceId = getString(exchange, PROCESS_INSTANCE_ID, this.processCriteria.get(PROCESS_INSTANCE_ID));
		if (!isEmpty(processInstanceId)) {
			eq.processInstanceId(trimToEmpty(eval(processInstanceId,vars)));
			info(this,"getProcessInstances.processInstanceId:"+trimToEmpty(eval(processInstanceId,vars)));
			hasCriteria=true;
		}
		String businessKey = getString(exchange, BUSINESS_KEY, this.businessKey);
		if (isEmpty(businessKey)) {
			businessKey = getString(exchange, BUSINESS_KEY, this.processCriteria.get(BUSINESS_KEY));
		}
		if (!isEmpty(businessKey)) {
			eq.processInstanceBusinessKey(trimToEmpty(eval(businessKey,vars)),childProcesses);
			info(this,"getProcessInstances.businessKey:"+trimToEmpty(eval(businessKey,vars)));
			hasCriteria=true;
		}

		String activityId = getString(exchange, ACTIVITY_ID, this.processCriteria.get(ACTIVITY_ID));
		if (!isEmpty(activityId)) {
			eq.activityId(trimToEmpty(eval(activityId,vars)));
			hasCriteria=true;
		}
		String executionId = getString(exchange, EXECUTION_ID, this.processCriteria.get(EXECUTION_ID));
		if (!isEmpty(executionId)) {
			eq.executionId(trimToEmpty(eval(executionId,vars)));
			hasCriteria=true;
		}
		String processVariable = getString(exchange, PROCESSVARIABLE, this.processCriteria.get(PROCESSVARIABLE));
		if (!isEmpty(processVariable)) {
			processVariable = trimToEmpty(processVariable);
			int delim = processVariable.indexOf(",");
			if( delim == -1){
				eq.processVariableValueEquals(trimToEmpty(eval(processVariable,vars)));
			}else{
				String p [] = processVariable.split(",");
				eq.processVariableValueEquals(
					trimToEmpty(eval(p[0],vars)),
					trimToEmpty(eval(p[1],vars))
				);
			}
			hasCriteria=true;
		}

		if( hasCriteria ){
			info(this,"getProcessInstances.namespace:"+this.namespace);
			eq.executionTenantId(trimToEmpty(this.namespace));
			List<ProcessInstance> executionList = (List) eq.list();
			info(this,"getProcessInstances:" + executionList);
			if (exception && (executionList == null || executionList.size() == 0)) {
				throw new RuntimeException("ActivitiProducer.findProcessInstance:Could not find processInstance with criteria " + processCriteria);
			}
			return executionList;
		}
		return null;
	}

	private ProcessDefinition getProcessDefinition(ProcessInstance processInstance) {
		info(this,"getProcessDefinition:" + processInstance.getProcessDefinitionId() + "/ns:" + this.namespace);

		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
		if (processDefinition == null) {
			throw new RuntimeException("ActivitiProducer:getProcessDefinition:processDefinition not found:" + processInstance);
		}
		info(this,"getProcessDefinition:" + processDefinition + "/" + processDefinition.getTenantId());
		return processDefinition;
	}

	private void saveActivitiCamelCorrelationHistory(Exchange exchange, ProcessInstance processInstance) {
		EventAdmin eventAdmin = (EventAdmin) exchange.getContext().getRegistry().lookupByName(EventAdmin.class.getName());

		String aci = this.namespace + "/" + processInstance.getProcessDefinitionKey() + "/" + processInstance.getId() + "/" + this.activityId;
		exchange.setProperty(HISTORY_ACTIVITI_ACTIVITY_KEY, aci);

		String bc = (String) exchange.getIn().getHeader(Exchange.BREADCRUMB_ID);
		String routeDef = (String) exchange.getProperty(CAMEL_ROUTE_DEFINITION_KEY);
		Map props = new HashMap();
		props.put(HISTORY_TYPE, ACTIVITI_CAMEL_CORRELATION_TYPE);
		props.put(ACC_ACTIVITI_ID, aci);
		props.put(ACC_ROUTE_INSTANCE_ID, routeDef + "|" + bc);
		eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	private void printHistory(Exchange exchange) {
		List<MessageHistory> list = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);
		ExchangeFormatter formatter = new ExchangeFormatter();
		formatter.setShowExchangeId(true);
		formatter.setMultiline(true);
		formatter.setShowHeaders(true);
		formatter.setStyle(ExchangeFormatter.OutputStyle.Fixed);
		String routeStackTrace = MessageHelper.dumpMessageHistoryStacktrace(exchange, formatter, true);
		info(this,routeStackTrace);
	}

	private void createLogEntry(Exchange exchange, ProcessDefinition processDefinition, Exception e) {
		EventAdmin eventAdmin = (EventAdmin) exchange.getContext().getRegistry().lookupByName(EventAdmin.class.getName());
		Map props = new HashMap();
		if (this.operation == ActivitiOperation.startProcess) {
			String key = processDefinition.getTenantId() + "/" + processDefinition.getId();
			props.put(HISTORY_KEY, key);
			props.put(HISTORY_TYPE, HISTORY_ACTIVITI_START_PROCESS_EXCEPTION);
			Map hint = new HashMap();
			hint.put("processDefinitionId", processDefinition.getId());
			hint.put("processDefinitionKey", processDefinition.getKey());
			hint.put("processDefinitionName", processDefinition.getName());
			hint.put("processDeploymentId", processDefinition.getDeploymentId());
			props.put(HISTORY_HINT, this.js.deepSerialize(hint));
		} else {
			props.put(HISTORY_KEY, this.activitiKey);
			props.put(HISTORY_TYPE, HISTORY_ACTIVITI_JOB_EXCEPTION);
		}
		Throwable r = getRootCause(e);
		props.put(HISTORY_MSG, r != null ? getStackTrace(r) : getStackTrace(e));
		eventAdmin.postEvent(new Event(HISTORY_TOPIC, props));
	}

	protected boolean isProcessStart() {
		return this.activityId == null;
	}

	private boolean isEmpty(String s) {
		return (s == null || "".equals(s.trim()));
	}
	private boolean isEmpty(List s) {
		return (s == null || s.size()==0);
	}

	private Map getCamelVariablenMap(Exchange exchange) {
		Map camelMap = new HashMap();
		Map exVars = ExchangeUtils.prepareVariables(exchange, true, true, true);
		camelMap.putAll(exVars);
		return camelMap;
	}

	private String getString(Exchange e, String key, String def) {
		String value = ExchangeUtils.getParameter(def,e, String.class);
		info(this,"getString("+key+","+def+"):"+value);
		return value;
	}

	private boolean getBoolean(Exchange e, String key, boolean def) {
		Boolean value = e.getIn().getHeader(key, Boolean.class);
		debug(this,"getBoolean:" + key + "=" + value + "/def:" + def);
		if (value == null) {
			value = e.getProperty(key, Boolean.class);
		}
		debug(this,"getBoolean2:" + key + "=" + value + "/def:" + def);
		return value != null ? value : def;
	}

	protected ActivitiEndpoint getActivitiEndpoint() {
		return (ActivitiEndpoint) getEndpoint();
	}
}

