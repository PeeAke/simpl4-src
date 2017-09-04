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
package org.ms123.common.data.quality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.regex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.ms123.common.utils.*;
import org.ms123.common.data.*;
import org.apache.commons.beanutils.*;
import flexjson.*;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.setting.api.SettingService;
import aQute.bnd.annotation.metatype.*;
import aQute.bnd.annotation.component.*;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.PDefaultString;
import org.ms123.common.rpc.PDefaultInt;
import org.ms123.common.rpc.PDefaultBool;
import org.ms123.common.rpc.PDefaultFloat;
import org.ms123.common.rpc.RpcException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import static org.ms123.common.entity.api.Constants.STATE_NEW;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=quality" })
public class QualityServiceImpl extends BasicQualityService implements QualityService {

	private static final Logger m_logger = LoggerFactory.getLogger(QualityServiceImpl.class);

	private final String ENTITY = "entity";

	// OsgiActivate 
	public void activate() {
		System.out.println("QualityServiceImpl.activate");
	}

	public List<Map> dupCheck(
			@PName("namespace")        String namespace, 
			@PName("entityName")       String entityName, 
			@PName("candidateList")    @POptional List<Map> candidateList, 
			@PName("state")            @PDefaultString(STATE_NEW) @POptional String state, 
			@PName("id")               @POptional String id, 
			@PName("dry")              @PDefaultBool(false) @POptional boolean dry) throws RpcException {
		try {
			return _dupCheck(namespace, entityName, candidateList, state, id, dry);
		} catch (Throwable e) {
			throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "QualityServiceImpl.dupCheck:", e);
		}
	}

	/************************************ C O N F I G ********************************************************/
	@Reference(dynamic = true)
	public void setNucleusService(NucleusService paramNucleusService) {
		this.m_nucleusService = paramNucleusService;
		System.out.println("QualityServiceImpl.setNucleusService:" + paramNucleusService);
	}

	@Reference(dynamic = true, optional = true)
	public void setSettingService(SettingService paramSettingService) {
		this.m_settingService = paramSettingService;
		System.out.println("QualityServiceImpl.setSettingService:" + paramSettingService);
	}

	@Reference(target = "(kind=jdo)", dynamic = true)
	public void setDataLayer(DataLayer paramDataLayer) {
		this.m_dataLayer = paramDataLayer;
		System.out.println("QualityServiceImpl.setDataLayer:" + paramDataLayer);
	}
}
