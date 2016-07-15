package com.ganz.jenkins.php;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.InstallSourceProperty;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;

public final class Installation extends ToolInstallation implements NodeSpecific<Installation> {

	private static final long serialVersionUID = 3623560351086920286L;

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
		return new Installation(getName(), translateFor(node, log), getProperties().toList());
	}

	@Override
	public String getHome() {
		String version = "";
		DescribableList<ToolProperty<?>, ToolPropertyDescriptor> properties = this.getProperties();
		for (ToolProperty<?> property : properties) {
			if (property instanceof InstallSourceProperty) {
				Installer installer = (Installer) ((InstallSourceProperty) property).installers.get(0);
				version = installer.id;
			}
		}
		return "php" + File.separatorChar + version;
	}

	/////////////////////////////////////////////
	// Descriptor implementation

	@Extension
	public static class Descriptor extends ToolDescriptor<Installation> {
		public Descriptor() {
			load();
		}

		@Override
		public String getDisplayName() {
			// TODO I18N
			return "PHP";
		}

		@Override
		public Installation[] getInstallations() {
			return super.getInstallations();
		}

		@Override
		public void setInstallations(Installation... installations) {
			super.setInstallations(installations);
			save();

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
