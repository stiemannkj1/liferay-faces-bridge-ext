/**
 * Copyright (c) 2000-2017 Liferay, Inc. All rights reserved.
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
package com.liferay.faces.bridge.ext.osgi.mojarra.servlet;

import java.util.EventListener;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.liferay.faces.util.osgi.OSGiClassProviderUtil;

/**
 * @author  Kyle Stiemann
 */
public final class FacesInitializer extends com.sun.faces.config.FacesInitializer {

	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {

		super.onStartup(classes, servletContext);

		try {

			Class<?> thisClass = this.getClass();
			Class<? extends EventListener> bridgeSessionListenerClass = (Class<? extends EventListener>)
				OSGiClassProviderUtil.classForName("com.liferay.faces.bridge.servlet.BridgeSessionListener",
					servletContext, thisClass);
			servletContext.addListener(bridgeSessionListenerClass);
		}
		catch (ClassNotFoundException e) {
			throw new ServletException(e);
		}
	}
}
