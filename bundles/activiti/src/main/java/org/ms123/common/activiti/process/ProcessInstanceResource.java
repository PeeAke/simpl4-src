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
package org.ms123.common.activiti.process;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.runtime.ProcessInstance;
import org.ms123.common.activiti.ActivitiService;
import org.ms123.common.activiti.BaseResource;
import org.ms123.common.activiti.Util;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 */
@SuppressWarnings({"unchecked","deprecation"})
public class ProcessInstanceResource extends BaseResource {

	private String m_processInstanceId;
	private String m_reason;
	private Map m_activityNameIdMap = new HashMap();

	public ProcessInstanceResource(ActivitiService as, String processInstanceId) {
		super(as, null);
		m_processInstanceId = processInstanceId;
	}
	public ProcessInstanceResource(ActivitiService as, String processInstanceId, String reason) {
		super(as, null);
		m_processInstanceId = processInstanceId;
		m_reason = reason;
	}

	public Map getProcessInstance() {
		String processInstanceId = m_processInstanceId;
		HistoricProcessInstance instance = getPE().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		if (instance == null) {
			throw new RuntimeException("Process instance not found for id " + processInstanceId);
		}
		Map<String, Object> responseJSON = new HashMap();
		responseJSON.put("processInstanceId", instance.getId());
		if (instance.getBusinessKey() != null) {
			responseJSON.put("businessKey", instance.getBusinessKey());
		} else {
			responseJSON.put("businessKey", null);
		}
		responseJSON.put("processDefinitionId", instance.getProcessDefinitionId());
		responseJSON.put("startTime", instance.getStartTime().getTime());
		responseJSON.put("startActivityId", instance.getStartActivityId());
		if (instance.getStartUserId() != null) {
			responseJSON.put("startUserId", instance.getStartUserId());
		} else {
			responseJSON.put("startUserId", null);
		}
		if (instance.getEndTime() == null) {
			responseJSON.put("completed", false);
		} else {
			responseJSON.put("completed", true);
			responseJSON.put("endTime", instance.getEndTime().getTime());
			responseJSON.put("endActivityId", instance.getEndActivityId());
			responseJSON.put("duration", instance.getDurationInMillis());
		}
		addTaskList(processInstanceId, responseJSON);
		addActivityList(processInstanceId, responseJSON);
		addVariableList(processInstanceId, responseJSON);
		return responseJSON;
	}

	public Map deleteProcessInstance() {
		String processInstanceId = m_processInstanceId;
		getPE().getRuntimeService().deleteProcessInstance(processInstanceId, m_reason != null ? m_reason : "REST API");
		getPE().getHistoryService().deleteHistoricProcessInstance(processInstanceId);
		Map successNode = new HashMap();
		successNode.put("success", true);
		return successNode;
	}

	private void addTaskList(String processInstanceId, Map<String, Object> responseJSON) {
		//@@@MS List<HistoricTaskInstance> taskList = getPE().getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).orderByHistoricTaskInstanceStartTime().asc().list();
		List<HistoricTaskInstance> taskList = getPE().getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
		if (taskList != null && taskList.size() > 0) {
			ArrayList tasksJSON = new ArrayList();
			responseJSON.put("tasks", tasksJSON);
			for (HistoricTaskInstance historicTaskInstance : taskList) {
				Map<String, Object> taskJSON = new HashMap();
				taskJSON.put("taskId", historicTaskInstance.getId());
				taskJSON.put("taskDefinitionKey", historicTaskInstance.getTaskDefinitionKey());
				if (historicTaskInstance.getName() != null) {
					taskJSON.put("taskName", historicTaskInstance.getName());
				} else {
					taskJSON.put("taskName", null);
				}
				if (historicTaskInstance.getDescription() != null) {
					taskJSON.put("description", historicTaskInstance.getDescription());
				} else {
					taskJSON.put("description", null);
				}
				if (historicTaskInstance.getOwner() != null) {
					taskJSON.put("owner", historicTaskInstance.getOwner());
				} else {
					taskJSON.put("owner", null);
				}
				if (historicTaskInstance.getAssignee() != null) {
					taskJSON.put("assignee", historicTaskInstance.getAssignee());
				} else {
					taskJSON.put("assignee", null);
				}
				taskJSON.put("startTime", historicTaskInstance.getStartTime().getTime());
				if (historicTaskInstance.getDueDate() != null) {
					taskJSON.put("dueDate", historicTaskInstance.getDueDate().getTime());
				} else {
					taskJSON.put("dueDate", null);
				}
				if (historicTaskInstance.getEndTime() == null) {
					taskJSON.put("completed", false);
				} else {
					taskJSON.put("completed", true);
					taskJSON.put("endTime", historicTaskInstance.getEndTime().getTime());
					taskJSON.put("duration", historicTaskInstance.getDurationInMillis());
				}
				tasksJSON.add(taskJSON);
			}
		}
	}

	private void addActivityList(String processInstanceId, Map<String, Object> responseJSON) {
		List<HistoricActivityInstance> activityList = getPE().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
		if (activityList != null && activityList.size() > 0) {
			ArrayList activitiesJSON = new ArrayList();
			responseJSON.put("activities", activitiesJSON);
			for (HistoricActivityInstance historicActivityInstance : activityList) {
				Map<String, Object> activityJSON = new HashMap();
				activityJSON.put("activityId", historicActivityInstance.getActivityId());
				activityJSON.put("executionId", historicActivityInstance.getExecutionId());
				if (historicActivityInstance.getActivityName() != null) {
					activityJSON.put("activityName", historicActivityInstance.getActivityName());
				} else {
					activityJSON.put("activityName", null);
				}
				activityJSON.put("activityType", historicActivityInstance.getActivityType());
				activityJSON.put("taskId", historicActivityInstance.getTaskId());
				activityJSON.put("assignee", historicActivityInstance.getAssignee());
				activityJSON.put("id", historicActivityInstance.getId());
				activityJSON.put("startTime", historicActivityInstance.getStartTime().getTime());
				if (historicActivityInstance.getEndTime() == null) {
					activityJSON.put("completed", false);
				} else {
					activityJSON.put("completed", true);
					activityJSON.put("endTime", historicActivityInstance.getEndTime().getTime());
					activityJSON.put("duration", historicActivityInstance.getDurationInMillis());
				}
				activitiesJSON.add(activityJSON);
				m_activityNameIdMap.put(historicActivityInstance.getId(), historicActivityInstance.getActivityName());
			}
		}
	}

	private void addVariableList(String processInstanceId, Map<String, Object> responseJSON) {
		ProcessInstance processInstance = getPE().getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		if( processInstance != null){
			Map<String, Object> variableMap = getPE().getRuntimeService().getVariables(processInstanceId);
			if (variableMap != null && variableMap.size() > 0) {
				ArrayList variablesJSON = new ArrayList();
				responseJSON.put("variables", variablesJSON);
				for (String key : variableMap.keySet()) {
					Object variableValue = variableMap.get(key);
					Map<String, Object> variableJSON = new HashMap();
					variableJSON.put("variableName", key);
					if (variableValue != null) {
						if (variableValue instanceof Boolean) {
							variableJSON.put("variableValue", (Boolean) variableValue);
						} else if (variableValue instanceof Long) {
							variableJSON.put("variableValue", (Long) variableValue);
						} else if (variableValue instanceof Double) {
							variableJSON.put("variableValue", (Double) variableValue);
						} else if (variableValue instanceof Float) {
							variableJSON.put("variableValue", (Float) variableValue);
						} else if (variableValue instanceof Integer) {
							variableJSON.put("variableValue", (Integer) variableValue);
						} else {
							variableJSON.put("variableValue", variableValue);
						}
					} else {
						variableJSON.put("variableValue", null);
					}
					variablesJSON.add(variableJSON);
				}
			}
		}
		List<HistoricDetail> historyVariableList = getPE().getHistoryService().createHistoricDetailQuery().processInstanceId(processInstanceId).variableUpdates().orderByTime().asc().list();
		if (historyVariableList != null && historyVariableList.size() > 0) {
			ArrayList variablesJSON = new ArrayList();
			responseJSON.put("historyVariables", variablesJSON);
			for (HistoricDetail historicDetail : historyVariableList) {
				HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) historicDetail;
				Map<String, Object> variableJSON = new HashMap();
				variableJSON.put("variableName", variableUpdate.getVariableName());
				if (variableUpdate.getValue() != null) {
					if (variableUpdate.getValue() instanceof Boolean) {
						variableJSON.put("variableValue", (Boolean) variableUpdate.getValue());
					} else if (variableUpdate.getValue() instanceof Long) {
						variableJSON.put("variableValue", (Long) variableUpdate.getValue());
					} else if (variableUpdate.getValue() instanceof Double) {
						variableJSON.put("variableValue", (Double) variableUpdate.getValue());
					} else if (variableUpdate.getValue() instanceof Float) {
						variableJSON.put("variableValue", (Float) variableUpdate.getValue());
					} else if (variableUpdate.getValue() instanceof Integer) {
						variableJSON.put("variableValue", (Integer) variableUpdate.getValue());
					} else {
						variableJSON.put("variableValue", variableUpdate.getValue());
					}
				} else {
					variableJSON.put("variableValue", null);
				}
				variableJSON.put("variableType", variableUpdate.getVariableTypeName());
				variableJSON.put("revision", variableUpdate.getRevision());
				variableJSON.put("taskId", variableUpdate.getTaskId());
				variableJSON.put("activityInstanceId", variableUpdate.getActivityInstanceId());
				variableJSON.put("time", variableUpdate.getTime().getTime());
				if( m_activityNameIdMap.get(variableUpdate.getActivityInstanceId()) != null){
					variableJSON.put("activityName", m_activityNameIdMap.get(variableUpdate.getActivityInstanceId())+"("+variableUpdate.getActivityInstanceId()+")");
				}else if ( variableUpdate.getActivityInstanceId() != null){
					variableJSON.put("activityName", variableUpdate.getActivityInstanceId());
				}else if ( variableUpdate.getTaskId() != null){
					variableJSON.put("activityName", variableUpdate.getTaskId());
				}else{
					variableJSON.put("activityName", variableUpdate.getActivityInstanceId());
				}
				variablesJSON.add(variableJSON);
			}
		}
	}
}
