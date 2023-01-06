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
	public static String PULLREQUEST;
	public static String TOKEN;
	public static void main(String[] args) throws Exception{
		logger.error("Plugin started with {} on {} at: {}", Arrays.toString(args), operatingSystem(), Instant.now());

		if(args.length != 2) {
			throw new RuntimeException("Expected argument number is 2 but " + args.length + " supplied!");
		}

		PULLREQUEST = args[0];
		TOKEN = args[1];
		Application bootApplication = new Application();
		Process process = bootApplication.runTestInAProcess();
	}

	public Process runTestInAProcess() throws Exception {
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/pulls/2";
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/issues/2/comments";
		String url = PULLREQUEST.replace("/pulls/", "/issues/") + "/comments";
		String body = "test";
		String payload = String.format(" \" { \\\"body\\\" : \\\" %s \\\" } \" ", body);
		//String credentials = "-u " + TOKEN;
		String credentials = String.format("--header \"authorization: Bearer %s\"", TOKEN);
		payload = payload.isEmpty() ? "" : "-d " + payload;
		String curlCommand = String.format("curl %s %s %s", url, credentials, payload);
		List<String> shellCommand = isRunningOnWindows() ? asList("cmd", "/C", curlCommand) : asList("bash", "-c", curlCommand);
		logger.error("Requesting '{}'", shellCommand);
		ProcessBuilder builder = new ProcessBuilder(shellCommand);
		Process process = builder.start();
		String response = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(joining("\n"));
		logger.error("Requested '{}'", shellCommand);
		logger.error("Response '{}'", response);
		return process;
	}
	private static String operatingSystem() {
		return System.getProperty("os.name");
	}

	private boolean isRunningOnWindows() {
		return operatingSystem().toLowerCase().contains("windows");
	}
}
