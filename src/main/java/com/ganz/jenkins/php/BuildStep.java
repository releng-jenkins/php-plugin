package com.ganz.jenkins.php;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

public class BuildStep extends Builder {

	private final String php;

	@DataBoundConstructor
	public BuildStep(String php) {
		this.php = php;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

		Installation installation = getInstallation();
		// use the php installed on the system
		if (installation == null) {
			// TODO l18N
			// TODO Populate the url of the jenkins configuration
			throw new AbortException(
					"Cannot find a " + php + " installation. Please check 'PHP installations' settings in Jenkins configuration.");
			// listener.getLogger().println("FAILED TO FIND " + php + "
			// INSTALLATION!");
			// return false;
		}
		Node node = Computer.currentComputer().getNode();
		if (node == null) {
			throw new AbortException("Cannot get installation for node, since it is not online.");
		}

		installation = installation.forNode(node, listener);
		build.getEnvironment(listener).put("PHP_HOME", installation.getHome());

		return true;

	}

	public Installation getInstallation() {
		for (Installation i : getDescriptor().getInstallations()) {
			if (php != null && php.equals(i.getName())) {
				return i;
			}
		}
		return null;
	}

	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.getDescriptor();
	}

	///////////////////////////////////////////////////////
	// BuildStep Descriptor implementation

	@Extension
	public static class Descriptor extends BuildStepDescriptor<hudson.tasks.Builder> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> project) {
			return true;
		}

		@Override
		public String getDisplayName() {
			// TODO I18N
			return "Use PHP";
		}

		public Installation[] getInstallations() {
			return Jenkins.getInstance().getDescriptorByType(Installation.Descriptor.class).getInstallations();
		}

	}
}