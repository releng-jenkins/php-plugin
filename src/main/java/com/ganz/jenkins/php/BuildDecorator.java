package com.ganz.jenkins.php;

import java.io.IOException;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class BuildDecorator extends BuildWrapper {

	private final String php;

	@DataBoundConstructor
	public BuildDecorator(String php) {
		this.php = php;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

		Installation installation = getInstallation();
		// use the php installed on the system
		if (installation == null) {
			// TODO l18N
			// TODO Populate the url of the jenkins configuration
			throw new AbortException(
					"Cannot find a " + php + " installation. Please check 'PHP installations' settings in Jenkins configuration.");
		}
		Node node = Computer.currentComputer().getNode();
		if (node == null) {
			throw new AbortException("Cannot get installation for node, since it is not online.");
		}

		installation = installation.forNode(node, listener);

		build.getEnvironment(listener).put("PHP_HOME", installation.getHome());
		return new Environment() {
			@Override
			public void buildEnvVars(Map<String, String> env) {

			}
		};

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
	// BuildDecorator implementation

	@Extension
	public static class Descriptor extends BuildWrapperDescriptor {

		public Descriptor() {
			super(BuildDecorator.class);
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
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

		@Override
		public boolean configure(StaplerRequest req, JSONObject object) throws hudson.model.Descriptor.FormException {
			return super.configure(req, object);
		}

	}

}
