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
		String data = "Connected to server."
		assertEquals("Invalid log line", parser.parse(data))
	}

	@Test
	void parseLineHittingMainWebsite() {
		String data = "Oct 14 12:41:02.734 sagan-blue CF[Router] STDOUT spring.io - [14/10/2013:16:41:02 +0000] \"GET /blog/2009/03/08/rest-in-spring-3-mvc/ HTTP/1.1\" 200 31164 \"http://www.bing.com/search?q=Spring+mvc+rest+with+nested+variables&qs=n&form=QBRE&pq=spring+mvc+rest+with+nested+variables&sc=0-16&sp=-1&sk=&cvid=a0c07c59ffcc4e7b8a6e2ac6a04fa989\" \"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)\" 10.10.2.159:49646 response_time:0.023646007 app_id:dba30dd4-e349-4aa4-a9fd-92649e8b3db5"
		def results = parser.parse(data)
		assertEquals("GET", results.method)
		assertEquals("/blog/2009/03/08/rest-in-spring-3-mvc/", results.path)
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
