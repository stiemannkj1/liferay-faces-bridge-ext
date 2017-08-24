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
package com.liferay.faces.bridge.ext.application.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.liferay.faces.bridge.ext.config.internal.LiferayPortletConfigParam;
import com.liferay.faces.bridge.ext.mojarra.spi.internal.ConfigurationResourceProviderBase;
import com.liferay.faces.util.helper.BooleanHelper;
import com.liferay.faces.util.product.Product;
import com.liferay.faces.util.product.ProductFactory;


/**
 * @author  Kyle Stiemann
 */
public class ResourceHandlerLiferayImpl extends ResourceHandlerWrapper {

	// Private Constants
	private static final boolean BOOTSFACES_DETECTED = ProductFactory.getProduct(Product.Name.BOOTSFACES).isDetected();
	private static final Set<String> BOOTSFACES_JQUERY_PLUGIN_JS_RESOURCES;
	private static final boolean BUTTERFACES_DETECTED = ProductFactory.getProduct(Product.Name.BUTTERFACES)
		.isDetected();
	private static final Set<String> BUTTERFACES_DIST_BOWER_JQUERY_PLUGIN_JS_RESOURCES;
	private static final Set<String> BUTTERFACES_DIST_BUNDLE_JS_JQUERY_PLUGIN_JS_RESOURCES;
	private static final Set<String> BUTTERFACES_EXTERNAL_JQUERY_PLUGIN_JS_RESOURCES;
	private static final String GLOBAL_AMD_LOADER_EXPOSED_PROPERTY_NAME = "exposeGlobal";
	private static final String GLOBAL_AMD_LOADER_EXPOSED_KEY = ResourceHandlerLiferayImpl.class.getName() + "." +
		GLOBAL_AMD_LOADER_EXPOSED_PROPERTY_NAME;
	private static final String JS_LOADER_CONFIGURATION_PID =
		"com.liferay.frontend.js.loader.modules.extender.internal.Details";
	private static final boolean PRIMEFACES_DETECTED = ProductFactory.getProduct(Product.Name.PRIMEFACES).isDetected();
	private static final Set<String> PRIMEFACES_JQUERY_PLUGIN_JS_RESOURCES;
	private static final boolean RICHFACES_DETECTED = ProductFactory.getProduct(Product.Name.RICHFACES).isDetected();

	static {

		// This list of resources was obtained by building BootsFaces and searching the target/ directory for js files
		// containg "typeof\\s+define\\s*=(=+)\\s*[\"']function[\"']|[\"']function[\"']\\s*=(=+)\\s*typeof\\s+define".
		Set<String> bootsFacesJQueryPluginResources = new HashSet<String>();
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-pt.js");
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-es.js");
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-hu.js");
		bootsFacesJQueryPluginResources.add("jq/jquery.js");
		bootsFacesJQueryPluginResources.add("jq/ui/mouse.js");
		bootsFacesJQueryPluginResources.add("jq/ui/slider.js");
		bootsFacesJQueryPluginResources.add("jq/ui/widget.js");
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-nl.js");
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-it.js");
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-pl.js");
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-de.js");
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-ru.js");
		bootsFacesJQueryPluginResources.add("jq/mobile/shake.js");
		bootsFacesJQueryPluginResources.add("jq/ui/core.js");
		bootsFacesJQueryPluginResources.add("jq/ui/i18n/datepicker-fr.js");
		bootsFacesJQueryPluginResources.add("jq/ui/datepicker.js");
		bootsFacesJQueryPluginResources.add("js/bootstrap-notify.min.js");
		bootsFacesJQueryPluginResources.add("js/jquery.blockUI.js");
		bootsFacesJQueryPluginResources.add("js/moment.min.js");
		bootsFacesJQueryPluginResources.add("js/typeahead.js");
		bootsFacesJQueryPluginResources.add("js/bootstrap-slider.min.js");
		bootsFacesJQueryPluginResources.add("js/fullcalendar-lang-all.js");
		bootsFacesJQueryPluginResources.add("js/jquery.minicolors.min.js");
		bootsFacesJQueryPluginResources.add("js/bootstrap-datetimepicker.min.js");
		bootsFacesJQueryPluginResources.add("js/fullcalendar.min.js");
		bootsFacesJQueryPluginResources.add("js/moment-with-locales.min.js");
		bootsFacesJQueryPluginResources.add("js/datatables.min.js");
		BOOTSFACES_JQUERY_PLUGIN_JS_RESOURCES = Collections.unmodifiableSet(bootsFacesJQueryPluginResources);

		// This list of resources was obtained by building ButterFaces and searching the target/ directory for js files
		// containg "typeof\\s+define\\s*=(=+)\\s*[\"']function[\"']|[\"']function[\"']\\s*=(=+)\\s*typeof\\s+define".
		Set<String> butterFacesJQueryPluginResources = new HashSet<String>();
		butterFacesJQueryPluginResources.add("prettify.js");
		butterFacesJQueryPluginResources.add("jquery.min.js");
		butterFacesJQueryPluginResources.add("jquery.js");
		BUTTERFACES_DIST_BOWER_JQUERY_PLUGIN_JS_RESOURCES = Collections.unmodifiableSet(
				butterFacesJQueryPluginResources);
		butterFacesJQueryPluginResources = new HashSet<String>();
		butterFacesJQueryPluginResources.add("butterfaces-all-with-bootstrap-bundle.min.js");
		butterFacesJQueryPluginResources.add("butterfaces-all-with-jquery-bundle.min.js");
		butterFacesJQueryPluginResources.add("butterfaces-all-bundle.min.js");
		butterFacesJQueryPluginResources.add("butterfaces-all-with-jquery-and-bootstrap-bundle.min.js");
		BUTTERFACES_DIST_BUNDLE_JS_JQUERY_PLUGIN_JS_RESOURCES = Collections.unmodifiableSet(
				butterFacesJQueryPluginResources);
		butterFacesJQueryPluginResources = new HashSet<String>();
		butterFacesJQueryPluginResources.add("mustache.min.js");
		butterFacesJQueryPluginResources.add("jquery.position.min.js");
		butterFacesJQueryPluginResources.add("to-markdown.js");
		butterFacesJQueryPluginResources.add("bootstrap-datetimepicker.min.js");
		butterFacesJQueryPluginResources.add("01-moment-with-locales.min.js");
		butterFacesJQueryPluginResources.add("trivial-components.min.js");
		BUTTERFACES_EXTERNAL_JQUERY_PLUGIN_JS_RESOURCES = Collections.unmodifiableSet(butterFacesJQueryPluginResources);

		// This list of resources was obtained by building Primefaces and searching the target/ directory for js files
		// containg "typeof\\s+define\\s*=(=+)\\s*[\"']function[\"']|[\"']function[\"']\\s*=(=+)\\s*typeof\\s+define".
		Set<String> primefacesJQueryPluginResources = new HashSet<String>();
		primefacesJQueryPluginResources.add("diagram/diagram.js");
		primefacesJQueryPluginResources.add("fileupload/fileupload.js");
		primefacesJQueryPluginResources.add("inputnumber/0-autoNumeric.js");
		primefacesJQueryPluginResources.add("inputnumber/inputnumber.js");
		primefacesJQueryPluginResources.add("jquery/jquery-plugins.js");
		primefacesJQueryPluginResources.add("jquery/jquery.js");
		primefacesJQueryPluginResources.add("knob/1-jquery.knob.js");
		primefacesJQueryPluginResources.add("knob/knob.js");
		primefacesJQueryPluginResources.add("mobile/jquery-mobile.js");
		primefacesJQueryPluginResources.add("moment/moment.js");
		primefacesJQueryPluginResources.add("mousewheel/jquery.mousewheel.min.js");
		primefacesJQueryPluginResources.add("photocam/photocam.js");
		primefacesJQueryPluginResources.add("push/push.js");
		primefacesJQueryPluginResources.add("raphael/raphael.js");
		primefacesJQueryPluginResources.add("schedule/schedule.js");
		primefacesJQueryPluginResources.add("texteditor/texteditor.js");
		primefacesJQueryPluginResources.add("touch/touchswipe.js");
		PRIMEFACES_JQUERY_PLUGIN_JS_RESOURCES = Collections.unmodifiableSet(primefacesJQueryPluginResources);
	}

	// Private Data Members
	private ResourceHandler wrappedResourceHandler;

	// Final Data Members
	private final Set<String> disabledAMDLoaderResources;
	private final boolean projectStageDevelopment;

	public ResourceHandlerLiferayImpl(ResourceHandler wrappedResourceHandler) {

		this.wrappedResourceHandler = wrappedResourceHandler;

		Set<String> disabledAMDLoaderResources = Collections.emptySet();
		ProjectStage projectStage = ProjectStage.Production;
		FacesContext startupFacesContext = FacesContext.getCurrentInstance();

		if (startupFacesContext != null) {

			ExternalContext externalContext = startupFacesContext.getExternalContext();
			String configuredValue = LiferayPortletConfigParam.DisabledAMDLoaderResources.getStringValue(
					externalContext);

			if (configuredValue != null) {

				configuredValue = configuredValue.trim();

				String[] resourceIds = configuredValue.split(",");
				boolean first = true;

				for (String resourceId : resourceIds) {

					if ((resourceId != null) && (resourceId.length() > 0)) {

						if (first) {

							disabledAMDLoaderResources = new HashSet<String>();
							first = false;
						}

						resourceId = resourceId.trim();
						disabledAMDLoaderResources.add(resourceId);
					}
				}

				if (!disabledAMDLoaderResources.isEmpty()) {
					disabledAMDLoaderResources = Collections.unmodifiableSet(disabledAMDLoaderResources);
				}
			}

			Application application = startupFacesContext.getApplication();
			projectStage = application.getProjectStage();
		}

		this.disabledAMDLoaderResources = disabledAMDLoaderResources;
		this.projectStageDevelopment = ProjectStage.Development.equals(projectStage);
	}

	@Override
	public Resource createResource(String resourceName) {

		Resource resource = super.createResource(resourceName);

		if (isDisableAMDLoaderForResource(resource, null, resourceName)) {
			resource = new JSResourceWithDisabledAMDLoaderImpl(resource);
		}

		return resource;
	}

	@Override
	public Resource createResource(String resourceName, String libraryName) {

		Resource resource = super.createResource(resourceName, libraryName);

		if (isDisableAMDLoaderForResource(resource, libraryName, resourceName)) {
			resource = new JSResourceWithDisabledAMDLoaderImpl(resource);
		}

		return resource;
	}

	@Override
	public Resource createResource(String resourceName, String libraryName, String contentType) {

		Resource resource = super.createResource(resourceName, libraryName, contentType);

		if (isDisableAMDLoaderForResource(resource, libraryName, resourceName)) {
			resource = new JSResourceWithDisabledAMDLoaderImpl(resource);
		}

		return resource;
	}

	@Override
	public ResourceHandler getWrapped() {
		return wrappedResourceHandler;
	}

	private boolean isAMDLoaderEnabledForResource(String libraryName, String resourceName) {

		String resourceId = resourceName;

		if ((libraryName != null) && (libraryName.length() > 0)) {
			resourceId = libraryName + ":" + resourceName;
		}

		return !disabledAMDLoaderResources.contains(resourceId);
	}

	private boolean isDisableAMDLoaderForResource(Resource resource, String libraryName, String resourceName) {
		return isJavaScriptResource(resource, resourceName) &&
			!(resource instanceof JSResourceWithDisabledAMDLoaderImpl) &&
			(isJQueryPluginJSResource(libraryName, resourceName) ||
				!isAMDLoaderEnabledForResource(libraryName, resourceName)) && isGlobalAMDLoaderExposed();
	}

	private boolean isGlobalAMDLoaderExposed() {

		boolean amdLoaderExposed = true;
		Object amdLoaderExposedObject = null;
		Map<String, Object> applicationMap = null;

		if (!projectStageDevelopment) {

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			applicationMap = externalContext.getApplicationMap();
			amdLoaderExposedObject = applicationMap.get(GLOBAL_AMD_LOADER_EXPOSED_KEY);
		}

		if (amdLoaderExposedObject == null) {

			Bundle portletBundle = FrameworkUtil.getBundle(ConfigurationResourceProviderBase.class);
			BundleContext bundleContext = portletBundle.getBundleContext();
			ServiceReference configurationAdminReference = bundleContext.getServiceReference(ConfigurationAdmin.class
					.getName());

			if (configurationAdminReference != null) {

				ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(
						configurationAdminReference);

				if (configurationAdmin != null) {

					try {

						Configuration jsLoaderConfiguration = configurationAdmin.getConfiguration(
								JS_LOADER_CONFIGURATION_PID);
						Dictionary<String, Object> properties = jsLoaderConfiguration.getProperties();

						if (properties != null) {
							amdLoaderExposedObject = properties.get(GLOBAL_AMD_LOADER_EXPOSED_PROPERTY_NAME);
						}
						else {

							Configuration[] configurations = configurationAdmin.listConfigurations(null);

							for (Configuration configuration : configurations) {

								properties = configuration.getProperties();

								if (properties != null) {

									amdLoaderExposedObject = properties.get(GLOBAL_AMD_LOADER_EXPOSED_PROPERTY_NAME);

									if (amdLoaderExposedObject != null) {
										break;
									}
								}
							}
						}
					}
					catch (IOException e) {
						// Do nothing.
					}
					catch (InvalidSyntaxException e) {
						// Do nothing.
					}
				}
			}

			if (amdLoaderExposedObject != null) {

				if (amdLoaderExposedObject instanceof Boolean) {
					amdLoaderExposed = (Boolean) amdLoaderExposedObject;
				}
				else {

					String exposeGlobalString = amdLoaderExposedObject.toString();
					amdLoaderExposed = BooleanHelper.toBoolean(exposeGlobalString);
				}
			}

			if (!projectStageDevelopment) {
				applicationMap.put(GLOBAL_AMD_LOADER_EXPOSED_KEY, amdLoaderExposed);
			}
		}
		else {
			amdLoaderExposed = (Boolean) amdLoaderExposedObject;
		}

		return amdLoaderExposed;
	}

	private boolean isJavaScriptResource(Resource resource, String resourceName) {

		if (resource != null) {

			String contentType = resource.getContentType();

			return (resourceName.endsWith(".js") || "application/javascript".equals(contentType) ||
					"text/javascript".equals(contentType));
		}
		else {
			return false;
		}
	}

	private boolean isJQueryPluginJSResource(String resourceLibrary, String resourceName) {

		boolean bootsFacesJQueryPluginJSResource = BOOTSFACES_DETECTED && "bsf".equals(resourceLibrary) &&
			BOOTSFACES_JQUERY_PLUGIN_JS_RESOURCES.contains(resourceName);

		boolean butterFacesJQueryPluginJSResource = false;

		if (BUTTERFACES_DETECTED && (resourceLibrary != null)) {

			butterFacesJQueryPluginJSResource = (resourceLibrary.equals("butterfaces-dist-bower") &&
					BUTTERFACES_DIST_BOWER_JQUERY_PLUGIN_JS_RESOURCES.contains(resourceName)) ||
				(resourceLibrary.equals("butterfaces-dist-bundle-js") &&
					BUTTERFACES_DIST_BUNDLE_JS_JQUERY_PLUGIN_JS_RESOURCES.contains(resourceName)) ||
				(resourceLibrary.equals("butterfaces-external") &&
					BUTTERFACES_EXTERNAL_JQUERY_PLUGIN_JS_RESOURCES.contains(resourceName));

		}

		boolean primeFacesJQueryPluginJSResource = PRIMEFACES_DETECTED &&
			((resourceLibrary == null) || resourceLibrary.equals("primefaces")) &&
			PRIMEFACES_JQUERY_PLUGIN_JS_RESOURCES.contains(resourceName);

		boolean richFacesJQueryPluginJSResource = false;

		if (RICHFACES_DETECTED) {

			boolean richfacesResourceLibrary = ("org.richfaces.resource".equals(resourceLibrary) ||
					"org.richfaces.staticResource".equals(resourceLibrary) || "org.richfaces".equals(resourceLibrary));

			richFacesJQueryPluginJSResource = ((resourceLibrary == null) || richfacesResourceLibrary) &&
				(resourceName.endsWith("packed.js") || resourceName.endsWith("jquery.js"));
		}

		return (bootsFacesJQueryPluginJSResource || butterFacesJQueryPluginJSResource ||
				primeFacesJQueryPluginJSResource || richFacesJQueryPluginJSResource);
	}
}
