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
package org.ms123.common.process.engineapi.process;

import java.util.ArrayList;
import java.util.List;
import org.ms123.common.process.engineapi.BaseResource;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.ms123.common.process.engineapi.AbstractPaginateList;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity;
import org.camunda.bpm.engine.task.IdentityLink;
import static com.jcabi.log.Logger.info;


/**
 */
@SuppressWarnings("unchecked")
public class ProcessDefinitionsPaginateList extends AbstractPaginateList {

	private ProcessEngine m_pe;
	private String m_starterGroup;

	public ProcessDefinitionsPaginateList(BaseResource br,String starterGroup) {
		m_pe = br.getPE();
		m_starterGroup = starterGroup;
	}

	protected List processList(List list) {
		info(this,"ProcessDefinitionsPaginateList("+m_starterGroup+"):"+list);
		List<ProcessDefinitionResponse> responseProcessDefinitions = new ArrayList<ProcessDefinitionResponse>();
		for (Object definition : list) {
			if( m_starterGroup != null){
				boolean ok = false;
      	List<IdentityLink> links = m_pe.getRepositoryService().getIdentityLinksForProcessDefinition(((ProcessDefinitionEntity) definition).getId());
				//List<IdentityLinkEntity> links = ((ProcessDefinitionEntity)definition).getIdentityLinks();
				for(IdentityLink il : links){
					info(this, "IdentityLink:"+il.getGroupId()+"/"+il.getUserId()+"/"+m_starterGroup);
					if(il.getGroupId() != null && il.getGroupId().equals(m_starterGroup)){
						ok=true;
					}
				}
				info(this, "ok:"+ok);
				if(!ok) continue;
			}
			ProcessDefinitionResponse processDefinition = new ProcessDefinitionResponse((ProcessDefinitionEntity) definition);
			StartFormData startFormData = m_pe.getFormService().getStartFormData(((ProcessDefinitionEntity) definition).getId());
			if (startFormData != null) {
				processDefinition.setStartFormResourceKey(startFormData.getFormKey());
			}
			processDefinition.setGraphicNotationDefined(isGraphicNotationDefined(((ProcessDefinitionEntity) definition).getId()));
			responseProcessDefinitions.add(processDefinition);
		}
		return responseProcessDefinitions;
	}

	private boolean isGraphicNotationDefined(String id) {
		try {
			return ((ProcessDefinitionEntity) ((RepositoryServiceImpl) m_pe.getRepositoryService()).getDeployedProcessDefinition(id)).isGraphicalNotationDefined();
		} catch (Exception e) {
		}
		return false;
	}
}
