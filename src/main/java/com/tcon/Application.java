package com.tcon;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.joining;

public class Application {
	private static final Logger logger = LogManager.getLogger(Application.class);
	public static String PULL_REQUEST;
	public static String TOKEN;
	public static String RESULT_FILE_FULL_PATH = "/github/workspace/results.json";
	public static void main(String[] args) throws Exception{
		logger.error("Plugin started with {} on {} at: {}", Arrays.toString(args), operatingSystem(), Instant.now());

		if(args.length < 2) {
			throw new RuntimeException("Expected argument number is 2 but " + args.length + " supplied!");
		}

/*		listDirectory("/");
		listDirectory("");
		listDirectory("/home/runner/work/tf-visualizer-action-test/tf-visualizer-action-test");
		listDirectory("home/runner/work/tf-visualizer-action-test/tf-visualizer-action-test");
		listDirectory("/github/workspace");
		listDirectory("github/workspace");*/


		PULL_REQUEST = args[0];
		TOKEN = args[1];

		if (args.length == 3) {
			RESULT_FILE_FULL_PATH = args[2];
		}

		Application bootApplication = new Application();
		String visualize = bootApplication.visualize(RESULT_FILE_FULL_PATH);
		Process process = bootApplication.addCommentPR(visualize);
		process = bootApplication.addCommentPR2(visualize);
	}

	private static void listDirectory(String path) {
		Path directory = Paths.get(path);
		File file1 = directory.toFile();
		logger.error("Looking at: {}, {}", path, file1.exists());

		try {
			String all =
					Files.walk(directory)
							.map(Path::toFile)
							.map(file -> file.getName())
							.collect(Collectors.joining(", "));
			logger.error(path + ": " +all);

		} catch (Exception e) {
			logger.error(path, e);
		}
	}



	public String visualize(String path) throws Exception {
		File file1 = Paths.get(path).toFile();
		Gson gson = new Gson();


		String tableRowFormat = "^|`%s`^|\\n";

		StringBuilder stringBuilder = new StringBuilder();

		String tableRowFormat1 = "^|%s^|\\n";
		stringBuilder.append(format(tableRowFormat1, "Resource"));
		stringBuilder.append(format(tableRowFormat1, "-"));
		TFResults tfResults = gson.fromJson(new FileReader(path), TFResults.class);

		stringBuilder.append(format(tableRowFormat, "A"));
		stringBuilder.append(format(tableRowFormat, "B"));


		/*
		int i = 0;

		JsonObject resultObject = gson.fromJson(new FileReader(path), JsonObject.class);
		JsonArray results = resultObject.get("results").getAsJsonArray();
		for (JsonElement result: results) {
			JsonObject asJsonObject = result.getAsJsonObject();
			stringBuilder.append(String.format(tableRowFormat
					, asJsonObject.get("severity")
					, asJsonObject.get("rule_id")
					, asJsonObject.get("long_id")
					, asJsonObject.get("resolution")));

			i++;

			if(i ==1) break;
		}*/

		System.out.println(stringBuilder);


		return stringBuilder.toString();
	}

	/*public String visualize(String path) throws Exception {
		File file1 = Paths.get(path).toFile();
		Gson gson = new Gson();


		String tableRowFormat = "^|`%s`^|`%s`^|`%s`^|`%s`^|`%s`^|\\n";

		StringBuilder stringBuilder = new StringBuilder();

		String tableRowFormat1 = "^|%s^|%s^|%s^|%s^|%s^|\\n";
		stringBuilder.append(format(tableRowFormat1, "Resource", "Path", "severity", "rule_id", "Description"));
		stringBuilder.append(format(tableRowFormat1, "-", "-", "-", "-", "-"));
		TFResults tfResults = gson.fromJson(new FileReader(path), TFResults.class);

		tfResults.results.stream()
				.sorted(reverseOrder())
				.map(tfResult -> format(tableRowFormat, tfResult.resource, tfResult.location.filename, tfResult.severity, tfResult.rule_id, tfResult.rule_description))
				.forEach(stringBuilder::append);

		*//*
		int i = 0;

		JsonObject resultObject = gson.fromJson(new FileReader(path), JsonObject.class);
		JsonArray results = resultObject.get("results").getAsJsonArray();
		for (JsonElement result: results) {
			JsonObject asJsonObject = result.getAsJsonObject();
			stringBuilder.append(String.format(tableRowFormat
					, asJsonObject.get("severity")
					, asJsonObject.get("rule_id")
					, asJsonObject.get("long_id")
					, asJsonObject.get("resolution")));

			i++;

			if(i ==1) break;
		}*//*

		System.out.println(stringBuilder);


		return stringBuilder.toString();
	}*/


	//curl https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/issues/22/comments  --header "authorization: Bearer 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" -d  ' { \"body\" : \"|Resource|\n|-|\n|A|\" } '

	public Process addCommentPR(String body) throws Exception {
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/pulls/2";
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/issues/2/comments";
		String url = PULL_REQUEST.replace("/pulls/", "/issues/") + "/comments";
		//body = "test";
		String payload = format(" \" { \\\"body\\\" : \\\" %s \\\" } \" ", body);
		//String credentials = "-u " + TOKEN;
		String credentials = format("--header \"authorization: Bearer %s\"", TOKEN);
		payload = payload.isEmpty() ? "" : "-d " + payload;
		String curlCommand = format("curl %s %s %s", url, credentials, payload);
		List<String> shellCommand = isRunningOnWindows() ? asList("cmd", "/C", curlCommand) : asList("bash", "-c", curlCommand);
		logger.error("Requesting '{}'", shellCommand);
		ProcessBuilder builder = new ProcessBuilder(shellCommand);
		Process process = builder.start();
		String response = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(joining("\n"));
		logger.error("Requested '{}'", shellCommand);
		logger.error("Response '{}'", response);
		return process;
	}

	public Process addCommentPR2(String body) throws Exception {
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/pulls/2";
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/issues/2/comments";
		String url = PULL_REQUEST.replace("/pulls/", "/issues/") + "/comments";
		//body = "test";
		String payload = format(" ' { \\\"body\\\" : \\\" %s \\\" } ' ", body);
		//String credentials = "-u " + TOKEN;
		String credentials = format("--header \"authorization: Bearer %s\"", TOKEN);
		payload = payload.isEmpty() ? "" : "-d " + payload;
		String curlCommand = format("curl %s %s %s", url, credentials, payload);
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
