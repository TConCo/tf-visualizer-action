package com.tcon;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
ó
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		PULL_REQUEST = args[0];
		TOKEN = args[1];

		if (args.length == 3) {
			RESULT_FILE_FULL_PATH = args[2];
		}

		Application bootApplication = new Application();
		String visualize = bootApplication.visualize(RESULT_FILE_FULL_PATH);
		Process process = bootApplication.addCommentPR(visualize);
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
		Gson gson = new Gson();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<table>");
		stringBuilder.append("<thead>          ");
		stringBuilder.append("<tr>             ");
		stringBuilder.append("<th>Resource</th>     ");
		stringBuilder.append("<th>Path</th>     ");
		stringBuilder.append("<th>Severity</th>     ");
		stringBuilder.append("<th>RuleId</th>     ");
		stringBuilder.append("<th>Status</th>     ");
		stringBuilder.append("<th>Description</th>     ");
		stringBuilder.append("</tr>            ");
		stringBuilder.append("</thead>         ");
		stringBuilder.append("<tbody>          ");

		String tableRowFormat = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";

		TFResults tfResults = gson.fromJson(new FileReader(path), TFResults.class);
		Comparator<TFResult> tfResultComparator = Comparator.comparingInt(TFResult::getStatus).thenComparing(TFResult::compareTo);

		tfResults.results.stream()
				.sorted(tfResultComparator)
				.map(tfResult -> format(tableRowFormat, tfResult.resource, tfResult.location.filename, tfResult.severity, tfResult.rule_id, tfResult.status()	, tfResult.rule_description))
				.forEach(stringBuilder::append);


		stringBuilder.append("</tbody>         ");
		stringBuilder.append("</table>         ");

		System.out.println();
		logger.debug("Visualized table: {}", stringBuilder);


		return stringBuilder.toString();
	}

	public Process addCommentPR(String body) throws Exception {
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/pulls/2";
		// "https://api.github.com/repos/fatihtokus/tf-visualizer-action-test/issues/2/comments";
		String url = PULL_REQUEST.replace("/pulls/", "/issues/") + "/comments";
		String payload = format(" \" { \\\"body\\\" : \\\" %s \\\" } \" ", body);
		String credentials = format("--header \"authorization: Bearer %s\"", TOKEN);
		payload = payload.isEmpty() ? "" : "-d " + payload;
		String curlCommand = format("curl %s %s %s", url, credentials, payload);
		List<String> shellCommand = isRunningOnWindows() ? asList("cmd", "/C", curlCommand) : asList("bash", "-c", curlCommand);
		logger.error("Requesting '{}'", shellCommand);
		ProcessBuilder builder = new ProcessBuilder(shellCommand);
		Process process = builder.start();
		String response = Stream.concat(new BufferedReader(new InputStreamReader(process.getInputStream())).lines()
				, new BufferedReader(new InputStreamReader(process.getErrorStream())).lines())
				.collect(joining("\n"));
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
