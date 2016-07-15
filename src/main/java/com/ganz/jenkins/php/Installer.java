package com.ganz.jenkins.php;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.model.DownloadService.Downloadable;
import hudson.tools.DownloadFromUrlInstaller;
import hudson.tools.InstallSourceProperty;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;
import jenkins.security.MasterToSlaveCallable;

//<a href=\".*\">php-([0-9\.]*)/?</a>
public class Installer extends DownloadFromUrlInstaller {
	private static String INSTALLABLES_URL = "http://10.0.0.6/tool/jenkins/tool/php/";

	@DataBoundConstructor
	public Installer(String id) {
		super(id);
	}

	/**
	 * Called by the InstallerTranslator::getToolHome when the builder attempts
	 * to retrieve the installation for current node.
	 */
	@Override
	public FilePath performInstallation(ToolInstallation installation, Node node, TaskListener listener)
			throws IOException, InterruptedException {
		listener.getLogger().print(
				"Checking  " + installation.getDescriptor().getDisplayName() + id + " installation on " + node.getDisplayName() + "...");
		FilePath home = preferredLocation(installation, node);
		FilePath marker = home.child(".installedByJenkins");
		if (marker.exists() && marker.readToString().equals(id)) {
			// TODO L18N
			listener.getLogger().println("okay.");
			return home;
		}
		// TODO L18N
		listener.getLogger().println("fail.");

		// TODO L18N
		listener.getLogger().println("Installing php " + id + " on " + node.getDisplayName() + "...");
		home.deleteRecursive();
		home.mkdirs();
		try {
			// TODO L18N
			listener.getLogger().print("Detecting node's platform and architecture...");
			Platform platform = Platform.of(node);
			Architecture arch = Architecture.of(node);
			listener.getLogger().println(platform + "-" + arch);

			URL archive = resolveArchive(installation, platform, arch, listener);
			// TODO L18N
			String message = "Unpacking " + archive + " to " + home + " on " + node.getDisplayName();
			if (!home.installIfNecessaryFrom(archive, listener, message)) {
				throw new Exception();
			}

			marker.touch(1);
		} catch (Exception e) {
			if (e.getCause() instanceof FileNotFoundException) {
				listener.getLogger().println("Failed to locate " + e.getCause().getMessage());
			}
			listener.getLogger().println(e.getMessage());
			throw new AbortException("Failed to install php " + id + ". Please see log for details.");
		}
		return home;
	}

	private String getArchiveName(Platform platform, Architecture architecture) {
		String extension = ".tar.gz";
		switch (platform) {
		case win:
			extension = ".zip";
			break;
		case osx:
			extension = ".dmg";
			break;
		default:
		}
		String result = "php-" + id + "-" + platform.getName() + "-" + architecture.getName() + extension;
		return result;
	}

	private URL resolveArchive(ToolInstallation installation, Platform platform, Architecture arch, TaskListener log) throws IOException {
		URL url = null;
		DescribableList<ToolProperty<?>, ToolPropertyDescriptor> properties = installation.getProperties();
		for (ToolProperty<?> property : properties) {
			if (property instanceof InstallSourceProperty) {
				Installer installer = (Installer) ((InstallSourceProperty) property).installers.get(0);
				url = new URL(installer.getInstallable().url + getArchiveName(platform, arch));
				return url;
			}
		}
		return null;
	}

	public enum Platform {
		nix("nix"), win("win"), osx("osx");

		private final String name;

		Platform(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static Platform of(Node n) throws IOException, InterruptedException, DetectionException {
			return n.getChannel().call(new DetectPlatformOperation());
		}

		static public Platform detect() throws DetectionException {
			String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
			if (os.contains("linux")) {
				return nix;
			}
			if (os.contains("windows")) {
				return win;
			}
			throw new DetectionException("Unknown platform : " + os);

		}

		static class DetectPlatformOperation extends MasterToSlaveCallable<Platform, DetectionException> {
			private static final long serialVersionUID = 1L;

			@Override
			public Platform call() throws DetectionException {
				return detect();
			}
		}
	}

	public enum Architecture {
		i386("x86"), x86_64("x64");// , sparc, itanium;
		private final String name;

		Architecture(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static Architecture of(Node n) throws IOException, InterruptedException, DetectionException {
			return n.getChannel().call(new DetectArchitectureOperation());
		}

		static public Architecture detect() throws DetectionException {
			String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
			if (arch.contains("amd64")) {
				return x86_64;
			}
			if (arch.contains("86")) {
				return i386;
			}
			throw new DetectionException("Unknown architecture : " + arch);
		}

		static class DetectArchitectureOperation extends MasterToSlaveCallable<Architecture, DetectionException> {
			private static final long serialVersionUID = 1L;

			@Override
			public Architecture call() throws DetectionException {
				return detect();
			}
		}
	}

	///////////////////////////////////////////////////////
	// DetectionException implementation

	public static class DetectionException extends Exception {
		private static final long serialVersionUID = 1L;

		public DetectionException(String message) {
			super(message);
		}

	}

	///////////////////////////////////////////////////////
	// Descriptor implementation

	@Extension
	public static final class Descriptor extends DownloadFromUrlInstaller.DescriptorImpl<Installer> {
		@Override
		public String getDisplayName() {
			return "Install PHP";
		}

		@Override
		public boolean isApplicable(Class<? extends ToolInstallation> installation) {
			return installation == Installation.class;
		}

		@Override
		public List<Installer.Installable> getInstallables() throws IOException {

			Downloadable d = Downloadable.get(getId());
			retrieveInstallableVersions(new URL("http://10.0.0.6/tool/jenkins/tool/php/"), d.getDataFile().file);

			String data = d.getDataFile().read();
			data = data.toLowerCase();
			Pattern pattern = Pattern.compile("<a href=\\\"(.*)\\\">php-([0-9\\.]*)/?</a>");
			Matcher matcher = pattern.matcher(data);
			ArrayList<Installable> list = new ArrayList<Installable>();
			while (matcher.find()) {
				String dir = matcher.group(1);
				String ver = matcher.group(2);
				Installer.Installable installable = new Installer.Installable(ver, dir);
				list.add(installable);
			}
			// Installable[] list = new Installable[3];
			// list[0] = new Installable("7.0.0");
			// list[1] = new Installable("5.6.0");
			// list[2] = new Installable("5.5.21");

			// return Arrays.asList(list);
			return list;
		}

		private void retrieveInstallableVersions(URL source, File target) throws IOException {
			URLConnection con = null;
			try {
				con = ProxyConfiguration.open(source);
				if (target.exists()) {
					con.setIfModifiedSince(target.lastModified());
				}
				con.connect();
				if (con instanceof HttpURLConnection
						&& ((HttpURLConnection) con).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
					return;
				}
			} catch (IOException e) {

			}

			InputStream in = ProxyConfiguration.getInputStream(source);
			OutputStream out = new FileOutputStream(target);
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.close();
			in.close();

		}

	}

	public static class Installable extends DownloadFromUrlInstaller.Installable {
		public Installable(String id, String path) {
			this.id = id;
			this.name = "PHP " + id;
			this.url = INSTALLABLES_URL + path;
		}
	}

}
