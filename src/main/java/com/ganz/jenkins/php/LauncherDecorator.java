package com.ganz.jenkins.php;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.remoting.Channel;

public class LauncherDecorator extends Launcher {
	private Launcher inner = null;

	protected LauncherDecorator(Launcher launcher) {
		super(launcher);
		inner = launcher;
	}

	protected Launcher getDecoratedLauncher() {
		return inner;
	}

	@Override
	public Proc launch(ProcStarter starter) throws IOException {
		return inner.launch(starter);
	}

	@Override
	public Channel launchChannel(String[] cmd, OutputStream out, FilePath workDir, Map<String, String> envVars)
			throws IOException, InterruptedException {
		return inner.launchChannel(cmd, out, workDir, envVars);
	}

	@Override
	public void kill(Map<String, String> modelEnvVars) throws IOException, InterruptedException {
		inner.kill(modelEnvVars);

	}

}
