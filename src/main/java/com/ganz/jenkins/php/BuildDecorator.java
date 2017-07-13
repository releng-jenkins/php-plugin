package com.ganz.jenkins.php;

import java.io.IOException;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Node;
import hudson.model.Run.RunnerAbortedException;
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
		return new Environment() {
			@Override
			public void buildEnvVars(Map<String, String> env) {

			}
		};
	}

	@Override
	public Launcher decorateLauncher(AbstractBuild build, final Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException, RunnerAbortedException {

		//FIXME:remove
		//EnvVars buildEnv = build.getEnvironment(listener);
		final EnvVars homes = new EnvVars();

		Installation installation = getInstallation();
		if (installation == null) {
			throw new AbortException(
					"Cannot find a " + php + " installation. Please check 'PHP installations' settings in Jenkins configuration.");
		}

		final Node node = build.getBuiltOn();
		if (node == null) {
			// FIXME
		}

		installation = installation.forNode(node, listener);

		homes.put("PHP_HOME", installation.getHome());
		homes.put("LD_LIBRARY_PATH", installation.getHome());
		return new LauncherDecorator(launcher) {
			@Override
			public Proc launch(ProcStarter starter) throws IOException {
				EnvVars vars;
				try {
					vars = toEnvVars(starter.envs());
				} catch (NullPointerException npe) {
					vars = new EnvVars();
				} catch (InterruptedException x) {
					throw new IOException(x);
				}
				if (vars.containsKey("PATH")) {
					final String overallPaths = vars.get("PATH");
					vars.remove("PATH");
					vars.put("PATH+", overallPaths);
				}

				vars.putAll(homes);
				return getDecoratedLauncher().launch(starter.envs(vars));
			}

			private EnvVars toEnvVars(String[] envs) throws IOException, InterruptedException {
				EnvVars vars = node.toComputer().getEnvironment();
				for (String line : envs) {
					vars.addLine(line);
				}
				return vars;
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
