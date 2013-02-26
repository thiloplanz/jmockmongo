# jmockmongo

jmockmongo is a terribly incomplete tool to help with unit testing Java-based MongoDB applications. It works (to the degree that it works) by starting an in-process Netty server that speaks the MongoDB wire protocol, listens a local port for connections, and maintains "fake" databases and collections in JVM memory. 

This has the advantage that you can run your code (with the standard MongoDB Java client library) unmodified.

Only very few commands and queries are implemented. This is very much a work in progress, and progress only happens on demand (which at the moment is just me when I need to improve test coverage of my projects).

## Database server port

Early versions of jmockmongo listened on the same default port used
by MongoDB (port 27017). That was convenient, because you did not have to configure the Mongo client being tested at all. 

However, it was also quite risky, because it made it easy for automated tests
to accidentally connect to (and possibly damage) real databases,
especially since MongoDB allows connections without passwords,
usually listens (via mongos) on each application server's localhost,
and will serve queries against non-existing databases and collections
quite happily.

So instead, jmockmongo now selects any available server port and binds to that.
You have to query it for which port that is after it has been started:

    MockMongo mock = new MockMongo();
    MongoURI uri = mock.getMongoURI();
    // or 
    int port = mock.getPort();
    
As an alternative, you can also explicitly specify any port you want:

    MockMongo mock = new MockMongo(2307);

    

## Example

     // create and start a mock MongoDB
     // it will be completely empty
     MockMongo mock = new MockMongo();
     mock.start();

     // use the normal MongoDB client to talk to it
     Mongo mongo = new Mongo(mock.getMongoURI()); 
   
     mongo.getDatabase("test").getCollection("test").insert(
       new BasicDBObject("x", 1));
     
     // shutdown (necessary to unbind the server port)
     mock.stop();
     
     
   
     
    

### with JUnit

If you are using JUnit 3, there is a helper TestCase base class that starts and stops the mock MongoDB in the setup and teardown methods, and has helpers to bootstrap test data and assert results.


    public class UpdateTest extends MockMongoTestCaseSupport {
      
      public void testSimpleUpdate() throws Exception {

         // some test data
         prepareMockData("x.x", new BasicBSONObject("_id", "x"));
		
         Mongo m = getMongo();
         WriteResult result = m.getDB("x").getCollection("x").update(
           new BasicDBObject("_id", "x"),
           new BasicDBObject("$set", 
             new BasicDBObject("field", "test").append("another", "foo")),
           false, false, WriteConcern.SAFE);

         // WriteConcern.SAFE is necessary if you want to
         // assert the existence of the data immediately,
         // because otherwise the client won't wait for
         // completion

         // some assertions

         assertMockMongoFieldEquals("test", "x.x", "x", "field");
         assertMockMongoFieldEquals("foo", "x.x", "x", "another");
         assertEquals(1, result.getN());
      }
    }

## Download 

![Built on CloudBees](http://www.cloudbees.com/sites/default/files/Button-Built-on-CB-1.png) 


You can download jmockmongo release builds from a Maven repository [powered by CloudBees](https://thiloplanz.ci.cloudbees.com/job/jmockmongo/):

    <repositories>
      <repository>
		<id>thiloplanz</id>
		<url>http://repository-thiloplanz.forge.cloudbees.com/release/</url>
	  </repository>
    </repositories>

    <dependency>
		<groupId>jmockmongo</groupId>
		<artifactId>jmockmongo</artifactId>
		<version>0.0.2</version>
		<scope>test</scope>
	</dependency>





## Prior Art

If you want to mock-test your MongoDB code, you may also want to take a look around for other projects that might do a better job of it.


[BlueEyes](https://github.com/jdegoes/blueeyes) is a Scala Web framework that includes a mocking library for MongoDB. However, it does seem to be tied against their own MongoDB wrapper and can probably not be used unless you build on top of that wrapper.

[Flapdoodle Embedded MongoDB](https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de) provides for a platform neutral way
to run a real mongodb instance exclusively for unittests.