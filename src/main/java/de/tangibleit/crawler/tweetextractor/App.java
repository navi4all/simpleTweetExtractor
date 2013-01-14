package de.tangibleit.crawler.tweetextractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

import com.jolbox.bonecp.BoneCPDataSource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Hello world!
 * 
 */
public class App extends Application {
	public static ActorSystem SYSTEM;
	public static ActorRef MANAGER;
	public static BoneCPDataSource DATASOURCE;

	public App() {
		super();

		setupDB();
		
		SYSTEM = ActorSystem.create();
		MANAGER = SYSTEM.actorOf(new Props(WorkerManager.class), "manager");

		
	}

	private void setupDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		DATASOURCE = new BoneCPDataSource(); // create a new datasource object
		DATASOURCE.setJdbcUrl(getConnectionUrl()); // set the
													// JDBC
		// url
		DATASOURCE.setUsername("root"); // set the username
		DATASOURCE.setPassword(""); // set the password
	}

	private String getConnectionUrl() {
		Properties properties = new Properties();
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream(
					"settings.properties"));
			properties.load(stream);
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		return properties.getProperty("jdbc");
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());

		// Defines only one route
		router.attach("/crawl", CrawlResource.class);

		return router;
	}

	public static void main(String[] args) {
		try {
			// Create a new Component.
			Component component = new Component();

			// Add a new HTTP server listening on port 8182.
			component.getServers().add(Protocol.HTTP, 8182);

			// Attach the sample application.
			component.getDefaultHost().attach(new App());

			// Start the component.
			component.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
