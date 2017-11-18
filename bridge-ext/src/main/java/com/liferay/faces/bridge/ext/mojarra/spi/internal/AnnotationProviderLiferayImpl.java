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

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.servlet.annotation.HandlesTypes;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import com.liferay.faces.osgi.util.FacesBundleUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.sun.faces.config.FacesInitializer;
import com.sun.faces.spi.AnnotationProvider;


/**
 * @author  Kyle Stiemann
 */
public class AnnotationProviderLiferayImpl extends AnnotationProvider {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(AnnotationProviderLiferayImpl.class);

	// Private Constants
	private static final Set<Class<?>> ANNOTATIONS_HANDLED_BY_MOJARRA;

	static {

		final Set<Class<?>> annotationsHandledByMojarra = new HashSet<Class<?>>();

		try {

			Class<?> annotationScanningServletContainerInitializerClass = Class.forName(FacesInitializer.class
					.getName());
			HandlesTypes handledTypes = annotationScanningServletContainerInitializerClass.getAnnotation(
					HandlesTypes.class);
			Class[] annotationsHandledByMojarraArray = handledTypes.value();
			annotationsHandledByMojarra.addAll(Arrays.<Class<?>>asList(annotationsHandledByMojarraArray));

			// This list of classes was obtained from the AnnotationProvider JavaDoc.
			annotationsHandledByMojarra.addAll(Arrays.<Class<?>>asList(FacesComponent.class, FacesConverter.class,
					FacesRenderer.class, FacesValidator.class, ManagedBean.class, NamedEvent.class, FacesBehavior.class,
					FacesBehaviorRenderer.class));
		}
		catch (ClassNotFoundException e) {
			logger.error(e);
		}
		catch (NoClassDefFoundError e) {
			logger.error(e);
		}

		if (!annotationsHandledByMojarra.isEmpty()) {
			ANNOTATIONS_HANDLED_BY_MOJARRA = Collections.unmodifiableSet(annotationsHandledByMojarra);
		}
		else {
			ANNOTATIONS_HANDLED_BY_MOJARRA = Collections.emptySet();
		}
	}

	public AnnotationProviderLiferayImpl() {
	}

	@Override
	public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(Set<URI> set) {

		Map<Class<? extends Annotation>, Set<Class<?>>> annotatedClasses;

		// Annotation scanning works correctly in thick wabs. TODO test
		if (FacesBundleUtil.isCurrentWarThinWab()) {

			Map<String, Bundle> facesBundles = FacesBundleUtil.getFacesBundles(sc);
			Collection<Bundle> bundles = facesBundles.values();
			annotatedClasses = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();

			for (Class<?> annotation : ANNOTATIONS_HANDLED_BY_MOJARRA) {
				annotatedClasses.put((Class<? extends Annotation>) annotation, new HashSet<Class<?>>());
			}

			for (Bundle bundle : bundles) {

				BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
				Collection<String> classFilePaths = bundleWiring.listResources("/", "*.class",
						BundleWiring.LISTRESOURCES_RECURSE);

				for (String classFilePath : classFilePaths) {

					try {

						URL classResource = bundle.getResource(classFilePath);

						if (classResource == null) {
							continue;
						}

						String className = classFilePath.replaceAll("\\.class$", "").replace("/", ".");
						ClassLoader bundleClassLoader = bundleWiring.getClassLoader();
						Class<?> clazz = bundleClassLoader.loadClass(className);
						Annotation[] classAnnotations = clazz.getAnnotations();

						for (Annotation annotation : classAnnotations) {

							Class<? extends Annotation> annotationType = annotation.annotationType();

							if (ANNOTATIONS_HANDLED_BY_MOJARRA.contains(annotationType)) {
								annotatedClasses.get(annotationType).add(clazz);
							}
						}
					}
					catch (ClassNotFoundException e) {
						// no-op
					}
					catch (NoClassDefFoundError e) {
						// no-op
					}
				}
			}

			annotatedClasses = Collections.unmodifiableMap(annotatedClasses);
		}
		else {
			annotatedClasses = Collections.emptyMap();
		}

		return annotatedClasses;
	}
}
