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
package org.ms123.common.process.engineapi.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.ms123.common.process.engineapi.BaseResource;
import org.ms123.common.process.api.ProcessService;
import org.ms123.common.process.engineapi.Util;
import org.camunda.bpm.engine.task.IdentityLink;
import static com.jcabi.log.Logger.info;

/**
 */
public class ProcessDefinitionCandidateResource extends BaseResource {
	private List<String> m_userList;
	private List<String> m_groupList;
	private String m_processDefinitionId;

	public ProcessDefinitionCandidateResource(ProcessService ps,String processDefinitionId,List<String> userList,List<String> groupList) {
		super(ps, null);
		m_processDefinitionId = processDefinitionId;
		m_userList = userList;
		m_groupList = groupList;
	}
	public void execute() {
		List<IdentityLink> links = getPE().getRepositoryService().getIdentityLinksForProcessDefinition(m_processDefinitionId);
		for( IdentityLink il : links){
			if( il.getGroupId() != null){
				getPE().getRepositoryService().deleteCandidateStarterGroup(m_processDefinitionId, il.getGroupId());
			}
			if( il.getUserId() != null){
				getPE().getRepositoryService().deleteCandidateStarterUser(m_processDefinitionId, il.getUserId());
			}
		}
		if( m_userList != null && m_userList.size()>0){
			for( String user : m_userList){
				getPE().getRepositoryService().addCandidateStarterUser(m_processDefinitionId, user);
			}
		}
		if( m_groupList != null && m_groupList.size()>0){
			for( String group : m_groupList){
				getPE().getRepositoryService().addCandidateStarterGroup(m_processDefinitionId, group);
			}
		}
		links = getPE().getRepositoryService().getIdentityLinksForProcessDefinition(m_processDefinitionId);
		for( IdentityLink il : links){
			info(this, "ProcessDefinitionCandidateResource(group:"+il.getGroupId()+",user:"+il.getUserId()+")");
		}
	}
}
