# EX 2 - PPA-Chatserver

This project implements a Chatserver that starts on port 10000 as requested

### Developing
Code has been developed with Java SE 11. Feel free to choose the JRE you prefer. I developed it with [AdoptJDK](https://adoptopenjdk.net/)  using HotSpot as JVM. Be sure to have [Maven](https://maven.apache.org/) installed in your path too to build.

### Build and run

Be sure that your pwd is the project root to execute all the commands below

```bash
$ mvn clean package
```
*Please note that to pass tests you need to have port 10000 available. An integration test will run and check that a client can connect and send some
string to the server. The server will echo the string to the test client.*

To start the project

```bash
$ java -jar target/echoserver-0.0.1-SNAPSHOT.jar
```
To connect to server using telnet (assuming server is started on the same machine as clients and that you have telnet installed)

```bash
$ telnet 10000 localhost
```

Feel free to open more than one telnet connection and have a look at each message broadcasted to every client!
Write a string and press enter to send to the server.
Just press enter without entering string to close client connection.
*After 30 seconds without any interaction, the server will shutdwon the connection with the client*

### Using Docker Image

Don't want to start the jar? Love containers? Just build the Dockerfile provided (assuming you have docker installer)

```bash
$ docker image build -t echoserver:test .
```

Then start with:

```bash
$ docker run -d -p 10000:10000 --net=host echoserver:test
```

Open some clients connectio with:

```bash
$ telnet 10000 localhost
```

Play with the server. And rember to shutdown the container when done using

```bash
$ docker stop CONTAINER_ID
```

To obtain COINTAINER_ID for the above command just try

```bash
$ docker ps | grep echoserver
```
the leftmost string like *ac113b4a2154* should be your CONTAINER_ID
