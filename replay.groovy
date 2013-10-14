package io.spring.replay

@Grab("org.springframework.boot:spring-boot-starter:0.5.0.M4")

// This version supports CF library
// Don't know yet whether we are using vcap-java-client, but will hold
// at this version until determined otherwise
@Grab("org.springframework:spring-web:3.2.2.RELEASE")

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResponseErrorHandler

/**
 * Run it with "spring run replay.groovy".
 *
 * To work, you must create an application.properties file with:
 * username=<your CF username>
 * password=<your CF password>
 *
 * This requires that you have cf CLI installed and that you also have logs-cf-plugin 0.0.44.pre installed.
 *
 * NOTE: This class won't be part of the test suite
 */
@Log
class Application implements CommandLineRunner {

	@Autowired
	Replay replay

	@Autowired
	LogParser parser

	@Value('${username}')
	private String username

	@Value('${password}')
	private String password

	void run(String... args) {
		"cf logout".execute().waitFor()

		"cf login --username ${username} --password ${password} --org spring.io --space production".execute().waitFor()

		log.info "Fetching logs"
		Thread th1 = monitor("sagan-blue")
		Thread th2 = monitor("sagan-green")
		th1.run()
		th2.run()
	}

	def monitor(String node) {
		Thread.start {
			def proc = "cf logs ${node}".execute()

			proc.in.newReader().eachLine {
				def results = parser.parse(it)
				if (results != "Invalid log line") {
					replay.method(results, "staging", node)
				}

			}
		}
	}
}

@Component
@Log
class Replay {

	RestTemplate restTemplate = new RestTemplate()

	public Replay() {
		restTemplate.errorHandler = new ResponseErrorHandler() {
			@Override
			boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
				return false
			}

			@Override
			void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
				// swallow errors
			}
		}
	}

	synchronized def method(Line arg, String space = null, String node = "") {
		if (space) {
			call(arg, "http://${space}.${arg.host}${arg.path}", node)
		} else {
			call(arg, "http://${arg.host}${arg.path}", node)
		}
	}

	private def call(Line arg, String query, String node) {
		if (arg.method == "GET") {
			try {
				log.info "Replaying ${node} ${arg.method} ${query}"
				restTemplate.getForObject(query, String.class)
			} catch (HttpClientErrorException e) {
				// Absorb 404 errors
			}
			true
		} else if (arg.method == "HEAD") {
			try {
				log.info "Replaying ${node} ${arg.method} ${query}"
				restTemplate.headForHeaders(query)
			} catch (HttpClientErrorException e) {
				// Absorb 404 errors
			}
		} else {
			log.info "We don't handle ${arg.method}"
			false
		}
	}

}

class Line {
	String host
	String path
	String method

	String toString() {
		"${method} ${host}${path}"
	}
}

@Service
@Log
class LogParser {
	def parse(String line) {
		def preprocessed = line.split()
		if (preprocessed.length >= 12) {
			new Line(host: preprocessed[6],
					path: preprocessed[11],
					method: preprocessed[10][1..-1]
			)
		} else {
			"Invalid log line"
		}
	}
}