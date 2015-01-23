package com.adobe.cq.tutorials.assets.impl;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.AssetReferenceSearch;


/**
 * This servlet returns informations about all assets used on the page as JSON.
 * 
 * It binds with the selector "references" and the extension "json" and can be used only for all
 * pages (jcr:primaryType = cq:Page).
 * In case of problems or errors an empty JSON is returned.
 * 
 * you can use it like this: http://localhost:4502/content/geometrixx/en/products.references.json
 *
 */

@SlingServlet(resourceTypes="cq/Page", selectors="references", extensions="json", methods="GET")
public class ReferencedAssetsServlet extends SlingSafeMethodsServlet {
	

	private static final long serialVersionUID = 180713647505991578L;
	private static final String DAM_ROOT = "/content/dam";

	private static Logger LOG = LoggerFactory.getLogger(ReferencedAssetsServlet.class);
	
	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException  {
		response.setContentType("application/json");
		
		try {
			JSONObject jsonOut = new JSONObject();
			Node jcrNode = request.getResource().adaptTo(Node.class);
			
			if (jcrNode == null) {
				// every adaptTo() can return null, so let's handle that case here 
				// although it's very unlikely
				LOG.error("cannot adapt resource {} to a node", request.getResource().getPath());
				response.getOutputStream().print(new JSONObject().toString());
				return;
				
			}
			
			// let's use the specialized assetReferenceSearch, which does all the work for us
			
			AssetReferenceSearch search = new AssetReferenceSearch 
					(jcrNode, DAM_ROOT, request.getResourceResolver()); 
			Map<String,Asset> result = search.search();
		
		
			for (String key: result.keySet()) {
				Asset asset = result.get(key);
				JSONObject assetDetails = new JSONObject();
				assetDetails.put("path", asset.getPath());
				assetDetails.put("mimetype", asset.getMimeType());
				
				jsonOut.put(asset.getName(), assetDetails);
			}
			response.getOutputStream().print(jsonOut.toString(2));
		}  catch (JSONException e) {
			// print empty JSON
			LOG.error ("Cannot serialize JSON",e);
			response.getOutputStream().print(new JSONObject().toString());
		}
	} // doGet

}
