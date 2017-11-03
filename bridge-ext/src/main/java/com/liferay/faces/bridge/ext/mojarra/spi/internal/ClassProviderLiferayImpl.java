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

import com.sun.faces.spi.ClassProvider;
import java.util.Set;
import javax.faces.context.FacesContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 *
 * @author Kyle Stiemann
 */
public class ClassProviderLiferayImpl implements ClassProvider {

	@Override
	public Class<?> loadClass(String name, ClassLoader suggestedLoader) throws ClassNotFoundException {
		return getClass(name, null, suggestedLoader);
	}

	@Override
	public Class<?> classForName(String name, boolean initialize, ClassLoader suggestedClassLoader)
			throws ClassNotFoundException {
		return getClass(name, initialize, suggestedClassLoader);
	}

	@Override
	public Class<?> classForName(String name) throws ClassNotFoundException {
		return getClass(name, true, getClass().getClassLoader());
	}

	/* package-private */ static Class<?> getClass(String name, Boolean initialize, ClassLoader suggestedClassLoader)
			throws ClassNotFoundException {

		Class<?> clazz = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();

		if (facesContext != null && FacesBundleUtil.isThinWab(facesContext)) {

			Set<Bundle> facesBundles = FacesBundleUtil.getFacesBundles(facesContext);

			for (Bundle bundle : facesBundles) {

				BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
				ClassLoader classLoader = bundleWiring.getClassLoader();

				try {

					if (initialize != null) {
						clazz = Class.forName(name, initialize, classLoader);
					}
					else {
						clazz = classLoader.loadClass(name);
					}

					break;
				}
				catch (ClassNotFoundException e) {
					// no-op
				}
			}
		}

		if (clazz == null) {

			if (initialize != null) {
				clazz = Class.forName(name, initialize, suggestedClassLoader);
			}
			else {
				clazz = suggestedClassLoader.loadClass(name);
			}
		}

		return clazz;
	}	
}
