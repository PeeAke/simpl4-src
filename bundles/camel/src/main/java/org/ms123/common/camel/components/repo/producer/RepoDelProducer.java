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
package org.ms123.common.camel.components.repo.producer;

import org.apache.camel.Exchange;
import org.ms123.common.camel.components.repo.RepoConfiguration;
import org.ms123.common.camel.components.repo.RepoEndpoint;

public class RepoDelProducer extends RepoProducer {

	public RepoDelProducer(RepoEndpoint endpoint, RepoConfiguration configuration) {
		super(endpoint, configuration);
	}

	@Override
	public void process(Exchange exchange) throws Exception {
	}
}
