/**
 * Copyright (c) 2000-2016 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.bridge.filter.liferay.internal;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;
import javax.portlet.filter.RenderResponseWrapper;

import com.liferay.faces.bridge.BridgeFactoryFinder;
import com.liferay.faces.bridge.filter.liferay.LiferayURLFactory;


/**
 * @author  Neil Griffin
 */
public class RenderResponseBridgeLiferayImpl extends RenderResponseWrapper {

	// Private Data Members
	private Boolean friendlyURLMapperEnabled;
	private LiferayURLFactory liferayURLFactory;
	private String namespace;
	private String responseNamespaceWSRP;

	public RenderResponseBridgeLiferayImpl(RenderResponse renderResponse) {
		super(renderResponse);
		this.liferayURLFactory = (LiferayURLFactory) BridgeFactoryFinder.getFactory(LiferayURLFactory.class);
	}

	@Override
	public PortletURL createActionURL() throws IllegalStateException {

		FacesContext facesContext = FacesContext.getCurrentInstance();

		return liferayURLFactory.getLiferayActionURL(facesContext, getResponse(), super.getNamespace());
	}

	@Override
	public PortletURL createRenderURL() throws IllegalStateException {

		FacesContext facesContext = FacesContext.getCurrentInstance();

		return liferayURLFactory.getLiferayRenderURL(facesContext, getResponse(), super.getNamespace(),
				isFriendlyURLMapperEnabled(facesContext));
	}

	@Override
	public ResourceURL createResourceURL() throws IllegalStateException {

		FacesContext facesContext = FacesContext.getCurrentInstance();

		return liferayURLFactory.getLiferayResourceURL(facesContext, getResponse(), super.getNamespace());
	}

	protected boolean isFriendlyURLMapperEnabled(FacesContext facesContext) {

		if (friendlyURLMapperEnabled == null) {

			ExternalContext externalContext = facesContext.getExternalContext();
			PortletRequest portletRequest = (PortletRequest) externalContext.getRequest();
			PortletResponse portletResponse = (PortletResponse) externalContext.getResponse();
			PortletConfig portletConfig = (PortletConfig) portletRequest.getAttribute(PortletConfig.class.getName());
			LiferayPortletRequest liferayPortletRequest = new LiferayPortletRequest(portletRequest,
					portletResponse.getNamespace(), portletConfig);
			friendlyURLMapperEnabled = (liferayPortletRequest.getPortlet().getFriendlyURLMapperInstance() != null);
		}

		return friendlyURLMapperEnabled;
	}

	@Override
	public String getNamespace() {

		if (namespace == null) {

			namespace = super.getNamespace();

			if (namespace.startsWith("wsrp_rewrite")) {

				if (responseNamespaceWSRP == null) {

					FacesContext facesContext = FacesContext.getCurrentInstance();
					responseNamespaceWSRP = LiferayPortalUtil.getPortletId(facesContext);
				}

				namespace = responseNamespaceWSRP;
			}
		}

		return namespace;
	}
}
