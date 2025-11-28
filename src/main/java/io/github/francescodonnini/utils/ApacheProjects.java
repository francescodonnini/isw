package io.github.francescodonnini.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApacheProjects {
    private static final Logger logger = Logger.getLogger(ApacheProjects.class.getName());

    public static final List<String> PROJECTS = List.of(
            "Accumulo", "ActiveMQ", "Airavata", "Ambari", "Ant", "Any23",
            "Archiva", "Aries", "Avro", "Axis2", "Beam", "Bigtop", "BookKeeper",
            "Brooklyn", "BVal", "Calcite", "Camel", "Carbondata", "Cassandra",
            "Cayenne", "Celix", "Chemistry", "CloudStack", "Cocoon", "Commons Bcel",
            "Commons BeanUtils", "Commons Codec", "Commons Collections", "Commons Compress",
            "Commons Configuration", "Commons CSV", "Commons Daemon", "Commons DBCP",
            "Commons DbUtils", "Commons Digester", "Commons Email", "Commons Exec",
            "Commons FileUpload", "Commons IO", "Commons Lang", "Commons Logging",
            "Commons Math", "Commons Net", "Commons Pool", "Commons Validator",
            "Commons VFS", "Cordova", "CouchDB", "Creadur", "Crunch", "Curator",
            "CXF", "DataFu", "DB", "DeltaSpike", "Derby", "Directory", "Drill",
            "Druid", "Dubbo", "Empire-db", "Felix", "Flink", "Flume", "FOP",
            "FreeMarker", "Geode", "Geronimo", "Giraph", "Gobblin", "Gora",
            "Groovy", "Guacamole", "Guththila", "Hadoop", "Hama", "HBase",
            "Helix", "Hive", "HttpComponents", "Hudi", "Iceberg", "Ignite",
            "Image Commons", "Isis", "Jackrabbit", "James", "JClouds", "Jena",
            "JMeter", "Johnzon", "JSPWiki", "Kafka", "Karaf", "Knox", "Kudu",
            "Kylin", "Lens", "Log4j", "Lucene", "Mahout", "ManifoldCF", "Marmotta",
            "Maven", "Mesos", "MetaModel", "Mina", "MyFaces", "NetBeans", "NiFi",
            "Nutch", "Ode", "Olingo", "Oozie", "OpenJPA", "OpenMeetings",
            "OpenNLP", "OpenOffice", "OpenWebBeans", "Orc", "Parquet", "PDFBox",
            "Phoenix", "Pig", "Pivot", "PLC4X", "POI", "Polygene", "Portals",
            "Pulsar", "Qpid", "Ranger", "Reef", "RocketMQ", "Roller", "Royale",
            "Samza", "Santuario", "ServiceComb", "Servicemix", "Shiro", "sis",
            "SkyWalking", "Sling", "Solr", "Spark", "Sqoop", "Storm", "Struts",
            "Synapse", "Tapestry", "Tez", "Thrift", "Tika", "Tiles",
            "Tomcat", "TomEE", "Traffic Control", "Turbine", "UIMA", "Velocity",
            "Wicket", "Xalan", "Xerces", "XMLBeans", "Yarn", "Zeppelin", "ZooKeeper"
    );

    private static final Map<String, String> JIRA = Map.of(
            "ActiveMQ", "AMQ"
    );

    public static String remoteUrl(String project) {
        return "https://github.com/apache/%s".formatted(project.toLowerCase());
    }

    public static String jiraKey(String project) {
        return JIRA.getOrDefault(project, project.toUpperCase());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        for (String project : PROJECTS) {
            var name = project.toLowerCase()
                    .replace(" ", "-");
            var parent = Path.of("/home/francesco/Documents/Università/Ingegneria del Software 2/60/sources");
            var dest = parent.resolve(name);
            if (!dest.toFile().exists()) {
                var p = new ProcessBuilder()
                        .command("git", "clone", remoteUrl(name), dest.toString())
                        .start();
                logger.log(Level.INFO, "{}", "command %s terminated with error %d".formatted(p.info(), p.waitFor()));
            }
        }
    }
}
