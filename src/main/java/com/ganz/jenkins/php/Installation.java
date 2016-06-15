package com.ganz.jenkins.php;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;

public final class Installation extends ToolInstallation implements NodeSpecific<Installation>, EnvironmentSpecific<Installation> {

	public Installation(String name, String home) {
		super(name, home, Collections.<ToolProperty<?>> emptyList());
	}

	@DataBoundConstructor
	public Installation(String name, String home, List<? extends ToolProperty<?>> properties) {
		super(name, home, properties);
	}

	/////////////////////////////////////////////
	// NodeSpecific interface implementation
	@Override
	public Installation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	/////////////////////////////////////////////
	// EnvironmentSpecific interface implementation
	@Override
	public Installation forEnvironment(EnvVars environment) {
		// TODO Auto-generated method stub
		return null;
	}

	/////////////////////////////////////////////
	// Descriptor implementation

	@Extension
	public static class Descriptor extends ToolDescriptor<Installation> {
		@Override
		public String getDisplayName() {
			// TODO I18N
			return "PHP";
		}

		@Override
		public Installation[] getInstallations() {
			// TODO
			return new Installation[0];
		}

		@Override
		public void setInstallations(Installation... installations) {

		}

		/**
		 * Returns an optional list of installers to be configured by default
		 * for new tools of this type.
		 * 
		 */
		@Override
		public List<Installer> getDefaultInstallers() {
			return Collections.singletonList(new Installer(null));
		}

	}

}
