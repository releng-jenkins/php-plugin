package com.ganz.jenkins.php;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolInstallerDescriptor;

public class Installer extends ToolInstaller {

	@DataBoundConstructor
	public Installer(String id) {
		super(null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	/////////////////////////////////////////////
	// Descriptor implementation

	@Extension
	public static final class Descriptor extends ToolInstallerDescriptor<Installer> {
		public Descriptor() {

		}

		@Override
		public String getDisplayName() {
			// TODO : convert to messages
			return "Install PHP";
		}

		/**
		 * Checks if the installer described by this descriptor can be applied
		 * for the given toolType.
		 * 
		 * 
		 */
		@Override
		public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
			// FIX ME:
			return true;
		}
	}

}
