package com.tcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class Application {
	private static final Logger logger = LogManager.getLogger(Application.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	public static String PULLREQUEST;
	public static String TOKEN;

	public static void main(String[] args) throws Exception{
		logger.error("Plugin started with {} at: {}", Arrays.toString(args), Instant.now());

		if(args.length != 2) {
			throw new RuntimeException("Expected argument number is 2 but " + args.length + " supplied");
		}

		PULLREQUEST = args[0];
		TOKEN = args[1];
		logger.error("Test runner process started with: PULLREQUEST: {}, TOKEN: {}", PULLREQUEST, TOKEN);
		Application bootApplication = new Application();
		Process process = bootApplication.runTestInAProcess();
	}

	public Process runTestInAProcess() throws Exception {
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/pulls/2";
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/issues/2/comments";
		String url = PULLREQUEST.replace("/pulls/", "/issues/") + "/comments";
		String body = "test";
		String payload = String.format(" \" { \\\"body\\\" : \\\" %s \\\" } \" ", body);
		String credentials = "-u " + TOKEN;
		payload = payload.isEmpty() ? "" : "-d " + payload;
		String curlCommand = String.format("curl %s %s %s", url, credentials, payload);
		boolean isWindows = true;
		List<String> shellCommand = isWindows ? asList("cmd", "/C", curlCommand) : asList("bash", "-c", curlCommand);
		logger.error("Adding comment '{}' to PR '{}'", payload, url);
		ProcessBuilder builder = new ProcessBuilder(shellCommand);

		Process process = builder.start();
		String output = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(joining("\n"));
		logger.error(output);

		return process;
	}
}
