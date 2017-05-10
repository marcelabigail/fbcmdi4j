package org.appfacy;

import java.awt.Desktop;
import java.net.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import facebook4j.auth.*;
import facebook4j.api.*;
import facebook4j.*;

public class Utils {
	private static final Logger logger = LogManager.getLogger(org.appfacy.Utils.class);
	private static final String APP_ID = "427564304284179";
	private static final String APP_SECRET ="8cc54bb74b012d88ddb011603bf77594";
	public static final String ACCESS_TOKEN ="EAACEdEose0cBAALZAfFd5O89ERPEiMJS9JRj0DkRKZAh1SsHxqCs8namggrdLDJDP8ewz2ZACBz44x6uDYmA749zEVEy7eZCamp54PC9YZCHPvQz3e1cN70hntmOdHaq4OyYRSbqDhVpwXmgK29e83lA3ZAh83iVq7uqJk9GNKx0BS4fGc2n06ZBGaCGQfzoBUZD";
	static void getAccessToken(String cfgDir, String cfgFile, Properties properties,
			Scanner scanner) throws FacebookException {
		Facebook facebook = new FacebookFactory().getInstance();
		facebook.setOAuthAppId(APP_ID, APP_SECRET);
		facebook.setOAuthPermissions(properties.getProperty("out.appPermissions="));
		AccessToken accessToken = facebook.getOAuthAccessToken(ACCESS_TOKEN);
		facebook.setOAuthAccessToken(accessToken);
		
		if(accessToken.toString() == null) {
			try {
				while(accessToken.toString() == null) {
					URL url = new URL("http://graph.facebook.com/oauth/access_token?client_id="+APP_ID+
							"&client_secret"+APP_SECRET+"&grant_type=client_credentials");
					System.out.println("\tFavor de ir al URL mostrado para autorizar la cuenta, si no se abre el navegsdor automáticamente: ");
					logger.info("Obteniendo acces token");
					System.out.println("\t"+url);
					try {
						Desktop.getDesktop().browse(new URI(url.toURI().toString()));
					} catch (UnsupportedOperationException|URISyntaxException|IOException ignore) {
					}
					
					System.out.println("Ingrese el codigo de seguridad: ");
					String cs = scanner.nextLine();
					AccessToken requestToken = facebook.getOAuthAccessToken();
					
					try {
						if (cs.length() > 0) {
							accessToken = facebook.getOAuthAccessToken(requestToken.toString(), cs);
						} else {
							accessToken = facebook.getOAuthAccessToken(requestToken.toString());
						}
					} catch (FacebookException fe) {
						logger.error(fe);
					}
					
					logger.info("Access token obtenido.");
					System.out.println("\tAccess token: " + accessToken.getToken());
					
					properties.setProperty("oauth.appAccessToken=", accessToken.getToken());
					
					saveProperties(cfgDir, cfgFile, properties);
					logger.info("Configuraión guardada");
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	public static Facebook configFB(Properties properties) throws FacebookException {
		logger.info("Iniciando Facebook");
		Facebook facebook = new FacebookFactory().getInstance();
		facebook.setOAuthAppId(APP_ID, APP_SECRET);
		facebook.setOAuthPermissions(properties.getProperty("outh.appPermissions"));
		facebook.setOAuthAccessToken(new AccessToken(ACCESS_TOKEN));
		return facebook;
	}
	
	public static Properties loadPropertiesFromFile(String cfgDir, String cfgFile) throws IOException {
		Properties properties = new Properties();
		Path propFile = Paths.get(cfgDir, cfgFile);
		if(Files.exists(propFile)) {
			properties.load(Files.newInputStream(propFile));
			BiConsumer<Object, Object> c = (x, y) -> x.toString().equals(y);
			c.accept(properties, null);
			properties.forEach(c);
			if(c.equals(false)) {
				logger.info("Archivo appfacy.properties correcto.");
			}
			else {
				logger.error("Archivo appfacy.properties vacío.");
			}
		}
		else {
			logger.info("Creando archivo de configuración");
			Files.copy(Paths.get("config", "appfacy.properties"), propFile);
		}
		return properties;
	}
	
	public static void saveProperties(String cfgDir, String cfgFile, Properties properties) throws IOException {
		Path propFile = Paths.get(cfgDir, cfgFile);
		properties.store(Files.newOutputStream(propFile), "Guardado por getAccessToken");
	}
	
	public static void getNewsFeed (Object list, int num) {
		logger.info("Obteniendo muro de noticias del usuario...");
		PostMethods feed = (PostMethods) list;
		try {
			System.out.println("Publicaciones del muro: ");
			HashMap<Integer, List<Object>> feedMap = new HashMap<Integer, List<Object>>();
			Object[] feedA = feed.getHome().toArray();
			for(int i = 0; i <= num; i++) {
				feedMap.put(i, new ArrayList<Object>());
				for(int j = 0; j <= num; j++) {
					feedMap.get(j).add(feedA[j]);
				}
			}
			feedMap.forEach((k, v) -> System.out.println("Post: "+ k + "\n" + v));
			}
		catch (FacebookException ex) {
			System.out.println("Error en carga de noticias, favor de revisar el log");
			logger.error(ex);
		}
	}
	
	public static void getWall (Object list, int num) {
		logger.info("Obteniendo muro del usuario activo");
		PostMethods wall = (PostMethods) list;
			try {
				if(wall.getPosts() != null) {
					System.out.println("Publicaciones del muro del usuario activo: ");
					HashMap<Integer, List<Object>> wallMap = new HashMap<Integer, List<Object>>();
					Object[] wallA = wall.getPosts().toArray();
					for(int i = 0; i <= num; i++) {
						wallMap.put(i, new ArrayList<Object>());
						for(int j = 0; j <= num; j++) {
							wallMap.get(j).add(wallA[j]);
						}
					}
					wallMap.forEach((k, v) -> System.out.println("Post: "+ k + "\n" + v));
				}
			}
			catch (FacebookException ex) {
			System.out.println("Error al obtener el muro. Consulta el log ");
			logger.error(ex);
			}
		}
	
	public static void printNewsFeed (Object list, int num, Path dir) {
		logger.info("Exportando el muro de noticias");
		PostMethods feed = (PostMethods) list;
		try {
			if (Files.exists(dir) && Files.isDirectory(dir)){ 
				if(feed.getHome() != null) {
					System.out.println("Publicaciones del muro: ");
					Path guardarDirf = Paths.get(dir.getFileName() + ".txt");
					BufferedWriter writer = Files.newBufferedWriter(guardarDirf);
					HashMap<Integer, List<Object>> feedMap = new HashMap<Integer, List<Object>>();
					Object[] feedA = feed.getHome().toArray();
					for(int i = 0; i <= num; i++) {
						feedMap.put(i, new ArrayList<Object>());
						for(int j = 0; j <= num; j++) {
							feedMap.get(j).add(feedA[j]);
						}
					}
					feedMap.forEach((k, v) -> {
						try {
							writer.write("Post: "+ k + "\n" + v);
						} catch (IOException e) {	
							System.out.println("Error al exportar datos al archivo");	
							logger.error(e);
						}
						});
					writer.close();
				}
			}
		}
		catch (FacebookException|IOException ex) {
			System.out.println("Ocurrió un error al obtener el newsfeed. Consulta el "
					+ "log para más información");
			logger.error(ex);
		}
	}
	
	public static void printWall (Object list, int num, Path dir) {
		logger.info("Exportando muro del usuario");
		PostMethods wall = (PostMethods) list;
		try {
			if(wall.getPosts() != null) {
				System.out.println("Publicaciones del muro: ");
				Path guardarDirw = Paths.get(dir.getFileName() + ".txt");
				BufferedWriter writer = Files.newBufferedWriter(guardarDirw);
				HashMap<Integer, List<Object>> wallMap = new HashMap<Integer, List<Object>>();
				Object[] wallA = wall.getPosts().toArray();
				for(int i = 0; i <= num; i++) {
					wallMap.put(i, new ArrayList<Object>());
					for(int j = 0; j <= num; j++) {
						wallMap.get(j).add(wallA[j]);
					}
				}
				wallMap.forEach((k, v) -> {
					try {
						writer.write("Post: "+ k + "\n" + v);
					} catch (IOException e) {	
						System.out.println("Ocurrió un error al exportar datos al archivo.");	
						logger.error(e);
					}
					});
				writer.close();
			}
		}
		catch (FacebookException|IOException ex) {
			System.out.println("Error al exportar el muro. Consulta el log");
			logger.error(ex);
		}
	}
}