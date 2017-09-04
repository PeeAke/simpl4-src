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
package org.ms123.common.data.query;

import java.util.*;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.setting.api.SettingService;
import org.ms123.common.utils.TypeUtils;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.data.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.ms123.common.entity.api.Constants.STATE_OK;
import static org.ms123.common.entity.api.Constants.STATE_NEW;
import static org.ms123.common.entity.api.Constants.DISABLE_STATESELECT;
import static org.ms123.common.setting.api.Constants.STATESELECT;
import static org.ms123.common.setting.api.Constants.GLOBAL_SETTINGS;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;

@SuppressWarnings("unchecked")
public class QueryBuilder {

	private static final Logger m_logger = LoggerFactory.getLogger(QueryBuilder.class);

	protected Inflector m_inflector = Inflector.getInstance();

	protected NucleusService m_nucleusService;

	protected StoreDesc m_sdesc;

	protected SettingService m_settingService;

	protected String m_entityName;

	protected String m_configName;

	protected Map<String, Object> m_filterParams;

	protected int m_paramCount = 1;

	protected boolean m_teamSecurity;
	protected Map m_queryParams = new HashMap();

	protected SessionContext m_sessionContext;

	protected SelectBuilder m_mainSelectBuilder;

	protected String m_type;
	protected Map m_params;
	protected List<String> m_variables=new ArrayList();

	public QueryBuilder(String type, StoreDesc sdesc, String entityName, boolean teamSecurity,String configName, SessionContext sessionContext, List<String> joinFields, Map filters, Map params, Map fieldSets) {
		m_sessionContext = sessionContext;
		m_sdesc = sdesc;
		m_entityName = entityName;
		m_configName = configName;
		m_teamSecurity = teamSecurity;
		if (!type.equals("orientdb")) {
			m_nucleusService = sessionContext.getNucleusService();
		}
		m_settingService = sessionContext.getSettingService();
		m_filterParams = params != null ? (Map)params.get("filterParams") :null;
		m_type = type;
		m_params = params;
		insertFilterParams(filters);
		if (sdesc.getStore().equals("cassandra") || "jdbc".equals(sdesc.getStore())) {
			m_mainSelectBuilder = new JPASelectBuilder(this, sdesc, entityName, joinFields, filters, fieldSets);
		}else if (sdesc.getVendor().equals(StoreDesc.VENDOR_H2)) {
			m_mainSelectBuilder = new JPASelectBuilderH2(this, sdesc, entityName, joinFields, filters, fieldSets);
		}else{
			if (type.equals("pg")) {
				m_mainSelectBuilder = new JPASelectBuilderPostgresql(this, sdesc, entityName, joinFields, filters, fieldSets);
			}
			if (type.equals("mvel")) {
				m_mainSelectBuilder = new MVELSelectBuilder(this, sdesc, entityName, joinFields, filters, fieldSets);
			}
			if (type.equals("orientdb")) {
				m_mainSelectBuilder = new OrientDBSelectBuilder(this, sdesc, entityName, filters, fieldSets);
			}
		}
		if (m_mainSelectBuilder == null) {
			throw new RuntimeException("QueryBuilder.no_builder_for:" + m_type);
		}
	}

	public String getWhere() {
		return m_mainSelectBuilder.getWhere();
	}

	public String getTeamUserWhere() {
		return m_mainSelectBuilder.getTeamUserWhere();
	}
	public String getTeamSecurityWhere() {
		return m_mainSelectBuilder.getTeamSecurityWhere();
	}

	public String getFrom(String jointype) {
		return m_mainSelectBuilder.getFrom(jointype);
	}

	public void addSelector(String sel) {
		m_mainSelectBuilder.addSelector(sel);
	}

	public void addSelectors(List<String> sel) {
		m_mainSelectBuilder.addSelectors(sel);
	}

	public List<String> getInvolvedEntity() {
		return m_mainSelectBuilder.getInvolvedEntity();
	}

	public List<String> getProjectionListEntity(String entity, String alias) {
		return m_mainSelectBuilder.getProjectionListEntity(entity, alias);
	}

	public List<String> getProjectionListAll(String entity) {
		return m_mainSelectBuilder.getProjectionListAll(entity);
	}

	public Map getFieldSets(String entityName) {
		try{
			return m_settingService.getFieldSets(m_configName, m_sdesc.getNamespace(), entityName);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public boolean stateSelectDisabled(){
		if( m_params != null){
			if( m_params.get(DISABLE_STATESELECT) != null){
				return Utils.getBoolean(m_params,DISABLE_STATESELECT,false);
			}
		}
		return false;
	}

	public String getRequestedState(){
		if( m_params != null){
			if( m_params.get("state") != null){
				return (String)m_params.get("state");
			}
		}
		return STATE_OK;
	}
	public String getEntityForPath(String entityName) {
		return TypeUtils.getEntityForPath(m_nucleusService, m_sdesc, entityName);
	}

	public Map getPermittedFields(String entityName) {
		return m_sessionContext.getPermittedFields(entityName);
	}

	public Class getClass(String className) {
		return m_nucleusService.getClass(m_sdesc, m_inflector.getClassName(className));
	}

	public SessionContext getSessionContext() {
		return m_sessionContext;
	}

	public boolean hasTeamSecurity() {
		return m_teamSecurity;
	}

	public boolean hasStateSelect(String entityName) {
	  Map m = m_settingService.getPropertiesForEntityView( m_sdesc.getNamespace(), GLOBAL_SETTINGS, entityName, null);
		return m.get(STATESELECT) != null ? (Boolean)m.get(STATESELECT) : false;
	}

	public SelectBuilder getSelectBuilder(String entityName, Map filterMap) {
		if (m_type.equals("pg")) {
			return new JPASelectBuilderPostgresql(this, m_sdesc, entityName, null, filterMap, null);
		}
		if (m_type.equals("mvel")) {
			return new MVELSelectBuilder(this, m_sdesc, entityName, null, filterMap, null);
		}
		throw new RuntimeException("QueryBuilder.no_builder_for:" + m_type);
	}

	public String getVariables() {
		if( m_variables.size() == 0) return null;
		String vars="";
		String colon="";
		for(String v : m_variables){
			vars += colon+v;
			colon=";";
		}
		return vars;
	}
	public void addVariable(String v){
		m_variables.add(v);
	}
	public Map getQueryParams() {
		return m_queryParams;
	}

	public int getParamCount() {
		return m_paramCount;
	}

	public void incParamCount() {
		m_paramCount++;
	}

	public boolean insertFilterParams(Map<String, Object> filter) {
		if (filter == null) {
			return false;
		}
		boolean ok = true;
		String label = (String) filter.get("label");
		if (filter.get("connector") == null && label != null) {
			if (label.matches("^[a-zA-Z].*")) {
				info(this,"insertFilterParams:"+m_filterParams+"/"+label);
				if( m_filterParams != null && (m_filterParams.keySet().contains( label )|| m_filterParams.keySet().contains( label.toLowerCase() ))){
					Object data = m_filterParams.get(label);
					if( data == null){
						label = label.toLowerCase();
						data = m_filterParams.get(label);
					}
					if (data != null) {
						filter.put("data", data);
					}
					ok = true;
				}else{
					ok = false;
				}
			}
		}
		List children = (List) filter.get("children");
		List newChildren = new ArrayList();
		for (int i = 0; i < children.size(); i++) {
			Map c = (Map) children.get(i);
			if( insertFilterParams(c) ){
				newChildren.add( c );
			}
		}
		filter.put("children", newChildren);
		return ok;
	}
}
