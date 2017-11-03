/**
 * Copyright (c) 2000-2017 Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.liferay.faces.bridge.ext.mojarra.spi.internal;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 *
 * @author Kyle Stiemann
 */
public final class FacesBundleUtil {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(FacesBundleUtil.class);

	// Private Constants
	private static final boolean FRAMEWORK_UTIL_DETECTED;

	static {

		boolean frameworkUtilDetected = false;

		try {

			Class.forName("org.osgi.framework.FrameworkUtil");
			frameworkUtilDetected = true;
		}
		catch (Throwable t) {

			if (!((t instanceof NoClassDefFoundError) || (t instanceof ClassNotFoundException))) {

				logger.error("An unexpected error occurred when attempting to detect OSGi:");
				logger.error(t);
			}
		}

		FRAMEWORK_UTIL_DETECTED = frameworkUtilDetected;
	}

	private FacesBundleUtil() {
		throw new AssertionError();
	}

	public static Set<Bundle> getFacesBundles(Object context) {

		Set<Bundle> facesBundles;

		if (FRAMEWORK_UTIL_DETECTED) {

			facesBundles = (Set<Bundle>) getServletContextAttribute(context, FacesBundleUtil.class.getName());

			if (facesBundles == null) {

				facesBundles = new HashSet<Bundle>();
				BundleContext wabBundleContext =
						(BundleContext) getServletContextAttribute(context, "osgi-bundlecontext");
				Bundle wabBundle = wabBundleContext.getBundle();
				facesBundles.add(wabBundle);

				
				// If the WAB's dependencies are not contained in the WAB's WEB-INF/lib, find all the WAB's
				// dependencies and return them as well.
				if (isThinWab(wabBundle)) {

					addRequiredBundlesRecurse(facesBundles, wabBundle);
					addBridgeImplBundles(facesBundles);
				}

				facesBundles = Collections.unmodifiableSet(facesBundles);
				setServletContextAttribute(context, FacesBundleUtil.class.getName(), facesBundles);
			}
		}
		else {
			facesBundles = Collections.emptySet();
		}

		return facesBundles;
	}

	public static boolean isThinWab(Object context) {

		BundleContext wabBundleContext = (BundleContext) getServletContextAttribute(context, "osgi-bundlecontext");
		return isThinWab(wabBundleContext.getBundle());
	}

	private static boolean isThinWab(Bundle wabBundle) {

		Bundle bundle = FrameworkUtil.getBundle(FacesBundleUtil.class);
		return !wabBundle.equals(bundle);
	}

	private static Object getServletContextAttribute(Object context, String servletContextAttributeName) {

		Object servletContextAttributeValue;
		boolean isFacesContext = context instanceof FacesContext;

		if (isFacesContext || context instanceof ExternalContext) {

			ExternalContext externalContext;

			if (isFacesContext) {

				FacesContext facesContext = (FacesContext) context;
				externalContext = facesContext.getExternalContext();
			}
			else {
				externalContext = (ExternalContext) context;
			}

			Map<String, Object> applicationMap = externalContext.getApplicationMap();
			servletContextAttributeValue = applicationMap.get(servletContextAttributeName);
		}
		else if (context instanceof ServletContext) {

			ServletContext servletContext = (ServletContext) context;
			servletContextAttributeValue = servletContext.getAttribute(servletContextAttributeName);
		}
		else {
			throw new IllegalArgumentException("context [" + context.getClass().getName() + "] is not an instanceof "
					+ FacesContext.class.getName() + " or " + ExternalContext.class.getName() + " or "
					+ ServletContext.class.getName());
		}

		return servletContextAttributeValue;
	}

	private static void setServletContextAttribute(Object context, String servletContextAttributeName,
			Object servletContextAttributeValue) {

		boolean isFacesContext = context instanceof FacesContext;

		if (isFacesContext || context instanceof ExternalContext) {

			ExternalContext externalContext;

			if (isFacesContext) {

				FacesContext facesContext = (FacesContext) context;
				externalContext = facesContext.getExternalContext();
			}
			else {
				externalContext = (ExternalContext) context;
			}

			Map<String, Object> applicationMap = externalContext.getApplicationMap();
			applicationMap.put(servletContextAttributeName, servletContextAttributeValue);
		}
		else if (context instanceof ServletContext) {

			ServletContext servletContext = (ServletContext) context;
			servletContext.setAttribute(servletContextAttributeName, servletContextAttributeValue);
		}
		else {
			throw new IllegalArgumentException("context [" + context.getClass().getName() + "] is not an instanceof "
					+ FacesContext.class.getName() + " or " + ExternalContext.class.getName() + " or "
					+ ServletContext.class.getName());
		}
	}

	private static void addRequiredBundlesRecurse(Set<Bundle> facesBundles, Bundle bundle) {

		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		List<BundleWire> bundleWires = bundleWiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);

		for (BundleWire bundleWire : bundleWires) {

			bundle = bundleWire.getProvider().getBundle();

			if (bundle.getBundleId() != 0) {

				facesBundles.add(bundle);
				addRequiredBundlesRecurse(facesBundles, bundle);
			}
		}
	}

	private static boolean isBridgeBundle(Bundle bundle, String bundleSymbolicNameSuffix) {

		String bundleSymbolicName = "com.liferay.faces.bridge." + bundleSymbolicNameSuffix;
		return bundleSymbolicName.equals(bundle.getHeaders().get("Bundle-SymbolicName"));
	}

	private static void addBridgeImplBundles(Set<Bundle> facesBundles) {

		for (Bundle bundle : facesBundles) {

			if (isBridgeBundle(bundle, "api")) {

				BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
				List<BundleWire> bundleWires =
						bundleWiring.getProvidedWires(BundleRevision.PACKAGE_NAMESPACE);
				boolean addedBridgeImplBundle = false;
				boolean addedBridgeExtBundle = false;

				for (BundleWire bundleWire : bundleWires) {

					Bundle bundleDependingOnBridgeAPI = bundleWire.getRequirer().getBundle();

					if (isBridgeBundle(bundleDependingOnBridgeAPI, "impl")) {

						facesBundles.add(bundleDependingOnBridgeAPI);
						addRequiredBundlesRecurse(facesBundles, bundleDependingOnBridgeAPI);
						addedBridgeImplBundle = true;
					}
					else if (isBridgeBundle(bundleDependingOnBridgeAPI, "ext")) {

						facesBundles.add(bundleDependingOnBridgeAPI);
						addRequiredBundlesRecurse(facesBundles, bundleDependingOnBridgeAPI);
						addedBridgeExtBundle = true;
					}

					if (addedBridgeImplBundle && addedBridgeExtBundle) {
						break;
					}
				}

				break;
			}
		}
	}
}
