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
package org.ms123.common.docbook;

import flexjson.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.*;
import nu.xom.*;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.groovydsl.GroovyExtensionRegistry;
import org.dbdoclet.trafo.html.docbook.*;
import org.jruby.embed.osgi.OSGiScriptingContainer;
import org.ms123.common.data.api.DataLayer;
import org.ms123.common.data.api.SessionContext;
import org.ms123.common.docbook.rendering.*;
import org.ms123.common.docbook.xom.db.*;
import org.ms123.common.git.GitService;
import org.ms123.common.libhelper.Inflector;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.IOUtils;
import org.ms123.common.utils.UtilsService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;

/**
 *
 */
@SuppressWarnings("unchecked")
class BaseDocbookServiceImpl {
	private List<String> m_assetList = new ArrayList();
	
	BaseDocbookServiceImpl(){
		m_assetList.add( "image/png");
		m_assetList.add( "image/jpg");
		m_assetList.add( "image/svg+xml");
		m_assetList.add( "image/svg");
		m_assetList.add( "image/swf");
		m_assetList.add( "image/pdf");
	}

	protected Asciidoctor m_asciidoctor = null;

	protected Inflector m_inflector = Inflector.getInstance();

	protected DataLayer m_dataLayer;

	protected PermissionService m_permissionService;

	protected UtilsService m_utilsService;

	protected GitService m_gitService;

	protected BundleContext m_bc;

	protected JSONDeserializer m_ds = new JSONDeserializer();

	protected JSONSerializer m_js = new JSONSerializer();

	public void docbookToPdf(String namespace, InputStream is, Map<String, String> params, OutputStream os) throws Exception {
		PDFRenderer pdfRenderer = PDFRenderer.create(m_bc, m_gitService, namespace, is);
		pdfRenderer.parameters(params);
		pdfRenderer.render(os);
	}

	public void jsonToDocbook(String namespace, String jsonName, Map<String, Object> paramsIn, Map<String, String> paramsOut, OutputStream out)  throws Exception{
		String json = null;
		if( jsonName.startsWith("{")){
			json = jsonName;
		}else{
			json = m_gitService.searchContent(namespace, jsonName, "sw.document");
		}
		DocbookBuilder db = new DocbookBuilder(m_dataLayer, m_bc, getAsciidoctor());
		InputStream is = toInputStream(json, "UTF-8");
		db.toDocbook(namespace, is, out, paramsIn, paramsOut);
	}
	public void jsonToDocbook(String namespace, InputStream is, Map<String, Object> paramsIn, Map<String, String> paramsOut, OutputStream out)  throws Exception{
		DocbookBuilder db = new DocbookBuilder(m_dataLayer, m_bc, getAsciidoctor());
		db.toDocbook(namespace, is, out, paramsIn, paramsOut);
	}

	public void jsonToPdf(String namespace, String jsonName, Map<String, Object> paramsIn, OutputStream os) throws Exception {
		String json = null;
		if( jsonName.startsWith("{")){
			json = jsonName;
		}else{
			json = m_gitService.searchContent(namespace, jsonName, "sw.document");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Map paramsOut = new HashMap();
		jsonToDocbook(namespace, json, paramsIn, paramsOut, out);
		out.close();
		InputStream is = new ByteArrayInputStream(out.toByteArray());
		docbookToPdf(namespace,is, paramsOut,os);
	}
	public void jsonToPdf(String namespace, InputStream is, Map<String, Object> paramsIn, OutputStream os) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Map paramsOut = new HashMap();
		jsonToDocbook(namespace, is, paramsIn, paramsOut, out);
		out.close();
		docbookToPdf(namespace,new ByteArrayInputStream(out.toByteArray()), paramsOut,os);
	}


	protected void markdownToDocbook(String markdown, OutputStream out) throws Exception{
		Context ctx = new Context(null, null, false, new HashMap(), new HashMap());
		String html = BaseBuilder.xwikiToHtml(ctx,markdown);
		DocBookTransformer tf = new DocBookTransformer();
		debug(this,"html:" + html);
		String db = tf.transformFragment(html);
		debug(this,"db:" + db);
		DataOutputStream dout = new DataOutputStream(out);
		dout.writeUTF(db);
		dout.close();
	}

	protected void markdownToHtml(String markdown, Writer out) {
		WikiParser mp = new WikiParser(m_bc.getBundle());
		mp.parseToHtml(markdown, out);
	}


	protected void websiteStart(String serverName, String namespace, String name, OutputStream out,String uri) throws Exception {
		String json = m_gitService.searchContent(namespace, name, "sw.website");
		WebsiteBuilder hb = new WebsiteBuilder(m_dataLayer, m_bc);
		
		String userAgent = org.ms123.common.system.thread.ThreadContext.getThreadContext().getUserAgent().toString().toLowerCase();
		String sua = org.ms123.common.system.thread.ThreadContext.getThreadContext().getStringUserAgent().toLowerCase();
		info(this,"userAgent:"+userAgent+"/sua:"+sua+"/uri:"+uri);
		if( userAgent.indexOf("bot") != -1 
				|| userAgent.indexOf("crawler") != -1
				|| sua.indexOf("google-site-verification") != -1
				|| sua.indexOf("fischernetzdesign seo checker") != -1
				|| sua.indexOf("googlebot") != -1
				|| sua.indexOf("w3c_validator") != -1
			){
			String indexFileName = "index.html";
			if( uri.length() > 1 && uri.startsWith("/sw/website/mainpage-en")){ //@@@Bullshit
				indexFileName = "index_en.html";
			}
			info(this,"DeliverGooglePage:"+sua+"/page:"+indexFileName);
			File indexFile = new File(System.getProperty("workspace"),indexFileName); 
			if( indexFile.exists()){
				copy(new FileInputStream(indexFile), out);
				out.close();
			}
		}else{
			hb.getWebsiteStart(serverName, namespace, name, json, out, new HashMap(), new HashMap());
		}
	}
	protected Map _websiteMain(String namespace, String name) throws Exception {
		String json = m_gitService.searchContent(namespace, name, "sw.website");
		WebsiteBuilder hb = new WebsiteBuilder(m_dataLayer, m_bc);
		return hb.getWebsiteMain(namespace, name, json, new HashMap(), new HashMap());
	}
	protected Map _websiteFragment(String namespace, String name, String shapeId, String resourceId) throws Exception {
		String json = m_gitService.searchContent(namespace, name, "sw.website");
		WebsiteBuilder hb = new WebsiteBuilder(m_dataLayer, m_bc);
		return hb.getWebsiteFragment(namespace, name, json, shapeId, resourceId, new HashMap(), new HashMap());
	}
	protected Map _websitePage(String namespace, String name) throws Exception {
		String json = m_gitService.searchContent(namespace, name, "sw.webpage");
		WebsiteBuilder hb = new WebsiteBuilder(m_dataLayer, m_bc);
		return hb.getWebsitePage(namespace, name, json);
	}
	protected List _shapePropertiesList(String namespace, Map criteria,List propertyNames) throws Exception {
		String fileType = (String)criteria.get("fileType");
		List<String> stencilList = getStencilsForRoles( fileType.substring(3), (List)criteria.get("roles"));
		List<String> fileList = m_gitService.assetList(namespace, null, fileType, false);
		List<Map> shapeList = new ArrayList();
		for( String fileName : fileList){
			String json = m_gitService.getFileContent(namespace, fileName);
			if( json == null || "".equals(json)){
				continue;
			}
			Map rootShape = (Map) m_ds.deserialize(json);
			getShapeList( shapeList, getBasename(fileName), rootShape, null, criteria, propertyNames,stencilList );	
		}
		return shapeList;
	}
	private void getShapeList(List<Map> shapeList, String name, Map shape, Map parentShape, Map criteria, List<String> propertyNames, List<String> stencilList) throws Exception {
		List<Map> childShapes = (List) shape.get("childShapes");
		Map properties = (Map) shape.get("properties");
		if( isCriteriaOk( shape, parentShape, criteria,stencilList)){
			Map map = new HashMap();
			for(String pname : propertyNames){
				if( pname.equals("fileName")){
					map.put("fileName", name );
				}else{
					map.put(pname, properties.get(pname));
				}
			}
			shapeList.add(map);
		}
		for (Map child : childShapes) {
			properties = (Map) child.get("properties");
			getShapeList(shapeList, name, child, shape,criteria, propertyNames, stencilList);
		}
	}
	private boolean isCriteriaOk(Map shape, Map parentShape, Map criteria, List<String> stencilList) throws Exception {
		String parentStencil = (String)criteria.get("parentStencil");
		if( parentStencil != null && !getStencilId( parentShape).equals( parentStencil.toLowerCase())){
			return false;
		}
		if( stencilList.contains( getStencilId(shape)) ){
			return true;
		}
		return false;
	}
	protected String getStencilId(Map element) {
		if(element==null) return "__no_id";
		Map stencil = (Map) element.get("stencil");
		String id = ((String) stencil.get("id")).toLowerCase();
		return id;
	}
	public List<String> getStencilsForRoles(String name,List<String> roles) throws Exception {
		JSONDeserializer ds = new JSONDeserializer();
		String gitSpace = System.getProperty("git.repos");
		File file = new File(gitSpace + "/global/stencilsets", name + ".json");
		InputStream is = new FileInputStream(file);
		Reader in = new InputStreamReader(is, "UTF-8");
		Map<String,List> ssMap = (Map)ds.deserialize(in);
		List<Map> stencilList = ssMap.get("stencils");
		List<String> retList = new ArrayList();
		for( Map stencil : stencilList){
			List<String> sRoles = (List)stencil.get("roles");
			for( String role : roles){
				if( sRoles.contains(role)){
					retList.add( ((String)stencil.get("id")).toLowerCase() );
					break;
				}
			}
		}
		debug(this,"retList:"+retList);
		return retList;
	}
	public void adocToHtml( File adocFile, Writer w) throws Exception {
		Reader in = new InputStreamReader(new FileInputStream(adocFile), "UTF-8");
		Map<String, Object> options = new HashMap();
		Map<String, Object> attributes = new HashMap();
		attributes.put("icons", org.asciidoctor.Attributes.FONT_ICONS);
		options.put("attributes", attributes);
		options.put("safe", 0);
		getAsciidoctor().convert( in, w, options);
	}

	public String adocToHtml( String adoc) throws Exception {
		Map<String, Object> options = new HashMap();
		Map<String, Object> attributes = new HashMap();
		attributes.put("icons", org.asciidoctor.Attributes.FONT_ICONS);
		options.put("attributes", attributes);
		options.put("safe", 0);
		return getAsciidoctor().convert( adoc, options);
	}

	public void adocToDocbook( File adocFile, Writer w) throws Exception {
		Reader in = new InputStreamReader(new FileInputStream(adocFile), "UTF-8");
		Map<String, Object> options = new HashMap();
		Map<String, Object> attributes = new HashMap();
		attributes.put("icons", org.asciidoctor.Attributes.FONT_ICONS);
		options.put("attributes", attributes);
		options.put("safe", 0);
		options.put("backend", "docbook");
		getAsciidoctor().convert( in, w, options);
	}

	public String adocToDocbook( String adoc) throws Exception {
		Map<String, Object> options = new HashMap();
		Map<String, Object> attributes = new HashMap();
		attributes.put("icons", org.asciidoctor.Attributes.FONT_ICONS);
		options.put("attributes", attributes);
		options.put("safe", 0);
		options.put("backend", "docbook");
		return getAsciidoctor().convert( adoc, options);
	}

	protected synchronized Asciidoctor getAsciidoctor(){
		if( m_asciidoctor!=null){
			 return m_asciidoctor;
		}
		Bundle adBundle = null;
		for( Bundle b : m_bc.getBundles()){
			if( b.toString().startsWith("asciidoctorj")){
				adBundle=b;
				break;
			}
		}
		if( adBundle == null) throw new RuntimeException("Asciidoctor not found");
		OSGiScriptingContainer sc = new OSGiScriptingContainer( m_bc.getBundle());
		List loadPaths = new ArrayList(sc.getLoadPaths());
		Enumeration gemPaths = adBundle.findEntries("gems", "lib", true);
		while (gemPaths != null && gemPaths.hasMoreElements()) {
			String gemPath = ((java.net.URL)gemPaths.nextElement()).getFile();
			loadPaths.add(gemPath.substring(1, gemPath.length()-1));
		}
		debug(this,"loadPaths:"+loadPaths);
		sc.setLoadPaths(loadPaths);
		m_asciidoctor = create(sc.getProvider().getRuntime());
		new AsciidoctorX(m_asciidoctor).register();
		new GroovyExtensionRegistry().register(m_asciidoctor);
		return m_asciidoctor;
	}

	protected void _getAsset(String namespace, String name, String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if( !m_assetList.contains( type )){
			response.setStatus(403);
			return;
		}
		File asset=null;
		String contentType = type;
		try{
			if( "image/svg".equals(type)){
				type = "image/svg+xml";
				contentType = "image/svg+xml";
			}
			if( "image/swf".equals(type)){
				contentType = "application/x-shockwave-flash";
			}
			if( "image/pdf".equals(type)){
				contentType = "application/pdf";
			}
			asset = m_gitService.searchFile(namespace, name, type);
		}catch(Exception e){
			e.printStackTrace();
			response.setStatus(404);
			return;
		}
		Date sinceDate = new Date(request.getDateHeader("If-Modified-Since")+1000);
		long modTime = asset.lastModified( ); 
		if( modTime < sinceDate.getTime() ){
			response.setStatus(304);
			return;
		}else{
			response.setContentType( contentType );
			response.setContentLength( (int)asset.length() );
			response.setDateHeader("Last-Modified", modTime + 10000 );
			response.setStatus(HttpServletResponse.SC_OK);
			OutputStream os = response.getOutputStream();
			IOUtils.copy( new FileInputStream(asset), os );
			os.close();
		}
	}
	private String getBasename(String path) {
		String e[] = path.split("/");
		return e[e.length - 1];
	}
}
