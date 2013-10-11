package io.spring.replay

@Grab("org.springframework.boot:spring-boot-starter:0.5.0.M4")
@Grab("org.springframework:spring-web:3.2.2.RELEASE") // this version support CF library
@Grab("org.cloudfoundry:cloudfoundry-client-lib:0.8.7")
@Grab("org.codehaus.jackson:jackson-core-asl:1.6.2")
@Grab("org.codehaus.jackson:jackson-mapper-asl:1.6.2")

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResponseErrorHandler

import org.cloudfoundry.client.lib.CloudCredentials
import org.cloudfoundry.client.lib.CloudFoundryClient

/**
 * Run it with "spring run replay.groovy".
 *
 * To work, you must create an application.properties file with:
 * username=<your CF username>
 * password=<your CF password>
 *
 * NOTE: This class won't be part of the test suite
 */
@Log
class Application implements CommandLineRunner {

	@Autowired
	Replay replay

	@Value('${username}')
	private String username

	@Value('${password}')
	private String password

	void run(String... args) {
		log.info "Logging in as " + username
		def client = replay.cf(username, password)
		client.login()
		def logs = client.getLogs("sagan-blue")
		logs["logs/stdout.log"].eachLine {
			log.info it
		}
	}
}

@Component
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

	def method(Line arg, String space = null) {
		if (space) {
			"http://${space}.${arg.host}${arg.path}".toString()
		} else {
			call(arg, "http://${arg.host}${arg.path}")
		}
	}

	def call(Line arg, String query) {
		if (arg.method == "GET") {
			try {
				restTemplate.getForObject(query, String.class)
			} catch (HttpClientErrorException e) {
				// Absorb 404 errors
			}
			true
		} else {
			//println "We don't handle ${arg.method}"
			false
		}
	}

	def cf(String username, String password) {
		URL target = "https://api.run.pivotal.io".toURL()

		/**
		 * Plugin your own credentials
		 * P.S. "spring test" doesn't create an app context, and hence doesn't (yet) support
		 * properties
		 */
		//CloudCredentials credentials = new CloudCredentials("", "")
		CloudCredentials credentials = new CloudCredentials(username, password)
		CloudFoundryClient client = new CloudFoundryClient(credentials, target, "spring.io", "production")
		client
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

class LogParser {
	def parse(String line) {
		def preprocessed = line.split()
		if (preprocessed.length > 3 && preprocessed[3] == "spring.io") {
			new Line(host: preprocessed[3],
					path: preprocessed[8],
					method: preprocessed[7][1..-1]
			)
		} else {
			"Invalid log line"
		}
	}
}