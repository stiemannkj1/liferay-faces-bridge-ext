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
package com.liferay.faces.bridge.ext.mojarra.spi.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.sun.faces.spi.ConfigurationResourceProvider;
import java.util.Set;


/**
 * @author  Kyle Stiemann
 */
public abstract class ConfigurationResourceProviderBase implements ConfigurationResourceProvider {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationResourceProviderBase.class);

	@Override
	public abstract Collection<URI> getResources(ServletContext servletContext);

	protected Collection<URI> getResourcesPattern(String resourceFilePattern, ServletContext servletContext) {

		List<URI> resourceURIs;
		Set<Bundle> facesBundles = FacesBundleUtil.getFacesBundles(servletContext);

		if (!facesBundles.isEmpty()) {

			resourceURIs = new ArrayList<URI>();

			for (Bundle bundle : facesBundles) {

				BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
				Collection<String> resourceFilePaths =
						new ArrayList<String>(
								bundleWiring.listResources("META-INF/", resourceFilePattern,
										BundleWiring.LISTRESOURCES_RECURSE));

				if (resourceFilePattern.endsWith("faces-config.xml") && FacesBundleUtil.isThinWab(servletContext)) {

					String string = (String) servletContext.getAttribute("com/sun/faces/jsf-ri-runtime.xml");

					if (string != null) {
						resourceFilePaths.add("com/sun/faces/jsf-ri-runtime.xml");
					}
				}

				for (String resourceFilePath : resourceFilePaths) {

					Enumeration<URL> resourceURLs = null;

					try {

						// FACES-2650 Because there may be multiple jars in our bundle, some resources may have exactly
						// the same reourceFilePath. We need to find all the resources with this resourceFilePath in all
						// jars.
						resourceURLs = bundle.getResources(resourceFilePath);
					}
					catch (IOException ioe) {
						logger.error(ioe);
					}

					if (resourceURLs != null) {

						while (resourceURLs.hasMoreElements()) {

							try {

								URL resourceURL = resourceURLs.nextElement();

								if (resourceURL != null) {

									URI resourceURI = resourceURL.toURI();
									resourceURIs.add(resourceURI);
								}
								else {
									logger.warn("URL for resourceFilePath=[{0}] is null.", resourceFilePath);
								}
							}
							catch (URISyntaxException e) {
								logger.error(e);
							}
						}
					}
				}
			}

			resourceURIs = Collections.unmodifiableList(resourceURIs);
		}
		else {

			// FACES-3233 Bridge Ext not working outside OSGI context
			resourceURIs = Collections.<URI>emptyList();
		}

		return resourceURIs;
	}
}
