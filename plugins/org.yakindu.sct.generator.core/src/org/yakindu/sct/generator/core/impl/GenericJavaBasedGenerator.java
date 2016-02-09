/**
 * Copyright (c) 2011 committers of YAKINDU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * 	committers of YAKINDU - initial API and implementation
 * 
 */
package org.yakindu.sct.generator.core.impl;

import static org.yakindu.sct.generator.core.features.impl.IGenericJavaFeatureConstants.CONFIGURATION_MODULE;
import static org.yakindu.sct.generator.core.features.impl.IGenericJavaFeatureConstants.GENERATOR_CLASS;
import static org.yakindu.sct.generator.core.features.impl.IGenericJavaFeatureConstants.GENERATOR_PROJECT;
import static org.yakindu.sct.generator.core.features.impl.IGenericJavaFeatureConstants.TEMPLATE_FEATURE;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.util.Strings;
import org.yakindu.sct.commons.WorkspaceClassLoaderFactory;
import org.yakindu.sct.generator.core.features.ICoreFeatureConstants;
import org.yakindu.sct.model.sexec.ExecutionFlow;
import org.yakindu.sct.model.sgen.FeatureConfiguration;
import org.yakindu.sct.model.sgen.FeatureParameterValue;
import org.yakindu.sct.model.sgen.GeneratorEntry;
import org.yakindu.sct.model.sgraph.Statechart;

import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * 
 * 
 * @author holger willebrandt - Initial contribution and API
 */
public class GenericJavaBasedGenerator extends AbstractSExecModelGenerator {

	@Override
	protected Module getOverridesModule(final GeneratorEntry entry) {
		Module defaultModule = super.getOverridesModule(entry);

		String overridingModuleClass = null;
		FeatureConfiguration featureConfiguration = entry.getFeatureConfiguration(TEMPLATE_FEATURE);
		if (featureConfiguration != null) {
			FeatureParameterValue parameterValue = featureConfiguration.getParameterValue(CONFIGURATION_MODULE);
			if (parameterValue != null) {
				overridingModuleClass = parameterValue.getStringValue();
			} 
		}
		if (!Strings.isEmpty(overridingModuleClass)) {
			try {
				Class<?> moduleClass = getClassLoader(entry).loadClass(overridingModuleClass);
				if (Module.class.isAssignableFrom(moduleClass)) {
					Module module = (Module) moduleClass.newInstance();
					defaultModule = Modules.override(defaultModule).with(module);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.writeToConsole("Overriding module not found: " + overridingModuleClass);
				log.writeToConsole(e);
			}
		}
		return defaultModule;
	}

	protected ClassLoader getClassLoader(GeneratorEntry entry) {
		IProject project = getLookupRoot(entry);
		final ClassLoader classLoader = new WorkspaceClassLoaderFactory().createClassLoader(project,
				getClass().getClassLoader());
		return classLoader;
	}

	@Override
	public void runGenerator(Statechart statechart, GeneratorEntry entry) {
		String templateClass = getTemplateClassName(entry);
		final ClassLoader classLoader = getClassLoader(entry);
		IFileSystemAccess fsa = getFileSystemAccess(entry);
		try {
			Class<?> delegateGeneratorClass = (Class<?>) classLoader.loadClass(templateClass);
			Object delegate = getInjector(entry).getInstance(delegateGeneratorClass);

			Class<?> iType_ = (Class<?>) getClass().getClassLoader()
					.loadClass("org.yakindu.sct.generator.core.impl.IExecutionFlowGenerator");
			Class<?> iType__ = IExecutionFlowGenerator.class;
			Class<?> iType = (Class<?>) classLoader
					.loadClass("org.yakindu.sct.generator.core.impl.IExecutionFlowGenerator");

			ExecutionFlow flow = createExecutionFlow(statechart, entry);

			if (debugFeatureHelper.isDumpSexec(entry)) {
				dumpSexec(entry, flow);
			}


			if (delegate instanceof IExecutionFlowGenerator) {
				IExecutionFlowGenerator flowGenerator = (IExecutionFlowGenerator) delegate;
				flowGenerator.generate(flow, entry, fsa);
			}
			if (iType.isInstance(delegate)) {
				IExecutionFlowGenerator flowGenerator = (IExecutionFlowGenerator) delegate;
				flowGenerator.generate(flow, entry, fsa);
			}
			if (delegate instanceof ISGraphGenerator) {
				ISGraphGenerator graphGenerator = (ISGraphGenerator) delegate;
				graphGenerator.generate(statechart, entry);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.writeToConsole(e);
		}
	}

	protected String getTemplateClassName(GeneratorEntry entry) {
		return entry.getFeatureConfiguration(TEMPLATE_FEATURE).getParameterValue(GENERATOR_CLASS).getStringValue();
	}

	/**
	 * resolve the project that defines the lookup path for the XpandFacade
	 * 
	 */
	protected IProject getLookupRoot(GeneratorEntry entry) {
		IProject project = null;
		FeatureConfiguration templateConfig = entry.getFeatureConfiguration(TEMPLATE_FEATURE);
		FeatureParameterValue projectName = templateConfig.getParameterValue(GENERATOR_PROJECT);
		if (projectName != null) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName.getStringValue());
		} else {
			URI uri = entry.getElementRef().eResource().getURI();
			project = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uri.toPlatformString(true)))
					.getProject();
		}
		return project;
	}

	/**
	 * Provides a pre configured IFileSystemAccess instance
	 */
	public IFileSystemAccess getFileSystemAccess(GeneratorEntry entry) {
		//FIXME !!! unify, this is duplicate see ...Java*Generic*Something
		// set target project value
		sctFileSystemAccess.setOutputPath(ICoreFeatureConstants.OUTLET_FEATURE_TARGET_PROJECT,
				outletFeatureHelper.getTargetProjectValue(entry).getStringValue());
		// set target folder
		sctFileSystemAccess.setOutputPath(IFileSystemAccess.DEFAULT_OUTPUT,
				outletFeatureHelper.getTargetFolderValue(entry).getExpression().toString());

		FeatureParameterValue libraryTargetFolderValue = outletFeatureHelper
				.getLibraryTargetFolderValue(entry);
		
		if (libraryTargetFolderValue != null) {
			sctFileSystemAccess.setOutputPath(IExecutionFlowGenerator.LIBRARY_TARGET_FOLDER_OUTPUT,
					libraryTargetFolderValue.getExpression().toString());
		}

		sctFileSystemAccess.getOutputConfigurations().get(IFileSystemAccess.DEFAULT_OUTPUT).setCreateOutputDirectory(true);
		OutputConfiguration librarytargetFolderOutputConfiguration = sctFileSystemAccess.getOutputConfigurations()
				.get(IExecutionFlowGenerator.LIBRARY_TARGET_FOLDER_OUTPUT);
		if (librarytargetFolderOutputConfiguration != null) {
			librarytargetFolderOutputConfiguration.setCreateOutputDirectory(true);
			// do not overwrite existing resources and ensure the folder is not
			// cleaned.
			librarytargetFolderOutputConfiguration.setCanClearOutputDirectory(false);
			librarytargetFolderOutputConfiguration.setOverrideExistingResources(false);
		}

		return sctFileSystemAccess.getIFileSystemAccess();
	}

}
