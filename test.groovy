package io.spring.replay

import org.junit.Before

class LogParserTests {

	LogParser parser

	@Before
	void setup() {
		parser = new LogParser()
	}

	@Test
	void parseLineNotGoingToMainWebsite() {
		String data = "sagan-blue CF[Router]  STDOUT sagan-production.cfapps.io - [10/10/2013:15:52:31 +0000] \"GET /blog/2011/11/24/spring-roo-1-2-0-rc1-released HTTP/1.1\" 200 15002 \"-\" \"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)\" 10.10.66.79:14835 response_time:0.022856244 app_id:88f6663b-53b5-4d64-afab-837e4b5a6828"
		assertEquals("Invalid log line", parser.parse(data))
	}

	@Test
	void parseLineHittingMainWebsite() {
		String data = "sagan-blue CF[Router]  STDOUT spring.io - [10/10/2013:15:42:57 +0000] \"GET /blog/2010/01/25/ajax-simplifications-in-spring-3-0/null?s=20&d=mm HTTP/1.1\" 404 9301 \"http://spring.io/blog/2010/01/25/ajax-simplifications-in-spring-3-0/\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:24.0) Gecko/20100101 Firefox/24.0\" 10.10.2.31:61475 response_time:0.017503378 app_id:88f6663b-53b5-4d64-afab-837e4b5a6828"
		def results = parser.parse(data)
		assertEquals("GET", results.method)
		assertEquals("/blog/2010/01/25/ajax-simplifications-in-spring-3-0/null?s=20&d=mm", results.path)
	}

}

class ReplayTests {

	Replay replay

	@Before
	void setup() {
		replay = new Replay()
	}

	@Test
	void testQueryLogin() {
		def parm = new Line(method: 'GET', host: 'www.google.com', path: '/')
		assertTrue(replay.method(parm))
	}

	@Test
	void testAlternateSpace() {
		def parm = new Line(method: 'GET', host: 'www.google.com', path: '/')
		assertEquals("http://staging.www.google.com/", replay.method(parm, "staging"))
	}

	@Test
	@Ignore
	void testHeadMethod() {
		fail("You need to code a HEAD test")
	}

}

class IntegrationTests {

	LogParser parser
	Replay replay

	@Before
	void setup() {
		parser = new LogParser()
		replay = new Replay()
	}

	@Test
	@Ignore
	void testTheBacklogWithoutAnyErrors() {
		new File("access-prod.log").eachLine { line ->
			def results = parser.parse(line)
			if (results != "Invalid log line") {
				replay.method(results)
			}
		}
	}

}

class CloudFoundryTests {

	Replay replay

	@Before
	void setup() {
		replay = new Replay()
	}

	@Test
	void cf() {
		def client = replay.cf("replace with your CF username", "replace with your CF password")
		println client
		client.login()
		def logs = client.getLogs("sagan-blue")
		logs.each { println it.key }
//		println logs["logs/stdout.log"]
	}

}