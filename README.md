# This repository is no longer actively maintained by VMware, Inc.

# Replay

This app is used to pull log data continuously, and then "replay" it against a test site. This is meant to load test the web site using live traffic and hopefully discover issues before rolling out to production.

## Setup

To run this app, either download it or clone it using git.

Next, install [Spring Boot's CLI](https://github.com/spring-projects/spring-boot#spring-boot-cli).

- One option is to [install via GVM](https://github.com/spring-projects/spring-boot#installation-with-gvm).
- Another is to install with Mac homebrew by typing:

```sh
$ brew tap pivotal/tap && brew install springboot
```

Then, you need both `cf` and the log plugin.

```sh
$ gem install cf
$ gem install logs-cf-plugin --pre -v '0.0.44.pre'
```

Finally, you must create an `application.properties` files in the same folder where `replay.groovy` is found with your CF credentials.

```properties
username=<your CF username>
password=<your CF password>
```

Don't include the angled brackets when you plug in your own credentials

> **Note:** `.gitignore` has been configured so `application.properties` will NOT be added to source control, and cause you to accidentally give away your CF credentials.

## Running the app

With everything setup, let's fire it up!

```sh
$ spring run replay.groovy
```

This should start things up. It may take a few seconds to get started if this is the first time you have run a Spring Boot application. After it launches, it may take a few more seconds to login to CF and start pulling log files. But once it gets underway, it should be continuous.

```sh

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::             (v0.5.0.M4)

2013-10-14 16:59:16.481  INFO 92375 --- [       runner-0] o.s.boot.SpringApplication               : Starting application on retina with PID 92375 (/Users/gturnquist/.groovy/grapes/org.springframework.boot/spring-boot/jars/spring-boot-0.5.0.M4.jar started by gturnquist)
2013-10-14 16:59:16.507  INFO 92375 --- [       runner-0] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@4d38e3ae: startup date [Mon Oct 14 16:59:16 EDT 2013]; root of context hierarchy
2013-10-14 16:59:17.769  INFO 92375 --- [       runner-0] o.s.boot.SpringApplication               : Started application in 1.502 seconds
2013-10-14 16:59:27.960  INFO 92375 --- [       runner-0] io.spring.replay.Application             : Fetching logs
2013-10-14 16:59:39.572  INFO 92375 --- [       Thread-2] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/css/search.css
2013-10-14 16:59:42.111  INFO 92375 --- [       runner-0] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/css/search.css
2013-10-14 16:59:43.049  INFO 92375 --- [       Thread-2] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/project_metadata/spring-security-oauth?callback=jQuery110106259836407843977_1381784389876&_=1381784389877
2013-10-14 16:59:43.772  INFO 92375 --- [       runner-0] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/project_metadata/spring-security-oauth?callback=jQuery110106259836407843977_1381784389876&_=1381784389877
2013-10-14 16:59:44.387  INFO 92375 --- [       Thread-2] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/js/search.js
2013-10-14 16:59:44.718  INFO 92375 --- [       runner-0] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/js/search.js
2013-10-14 16:59:45.053  INFO 92375 --- [       Thread-2] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/
2013-10-14 16:59:45.899  INFO 92375 --- [       runner-0] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/
2013-10-14 16:59:46.672  INFO 92375 --- [       Thread-2] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/blog/
2013-10-14 16:59:47.749  INFO 92375 --- [       runner-0] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/blog/
2013-10-14 16:59:49.859  INFO 92375 --- [       Thread-2] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/blog/feed/
2013-10-14 16:59:50.384  INFO 92375 --- [       runner-0] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/blog/feed/
2013-10-14 16:59:51.251  INFO 92375 --- [       Thread-2] io.spring.replay.Replay                  : Replaying sagan-blue GET http://staging.spring.io/
```

Hit CTRL-C to stop the app. Each line indicates where it got the data (sagan-blue from production) and the exact location where it is replaying that web request.

