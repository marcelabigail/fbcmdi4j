package org.appfacy;																				

import java.io.IOException;																			
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InputMismatchException;															
import java.util.Scanner;																			
import java.util.Properties;																		

import org.apache.logging.log4j.Logger;																

import org.apache.logging.log4j.LogManager;															

import facebook4j.*;

public class Main {																					
	private static final Logger logger = LogManager.getLogger(org.appfacy.Main.class);				
	
	private static final String CFG_DIR = "config";													
	private static final String CFG_FILE = "appfacy.properties";									
	private static final String APP_VERSION = "v1.0"; 												
		
	public static void main(String[]args) throws FacebookException {
		logger.info("Iniciando la app");																
		System.out.format("Cliente de Facebook FBcmd4J %s\n\n", APP_VERSION);						
				
		Scanner scanner = new Scanner(System.in);
		
		Facebook facebook = null;
		Properties properties = null;																
		
		try {
			properties = org.appfacy.Utils.loadPropertiesFromFile(CFG_DIR, CFG_FILE);				
		}
		catch (IOException ex) {																	
			ex.printStackTrace();																	
			logger.error(ex.toString());															
		}
		
		if(properties == null) {																	
			System.out.println("Accediendo a tu cuenta de Facebook");
			logger.info("Realizando configuración de cliente de Facebook...");						
			org.appfacy.Utils.configFB(properties);													
		}
		if(org.appfacy.Utils.ACCESS_TOKEN == null) {
			System.out.print("Ausencia de token de acceso");
			logger.info("Obteniendo token de acceso");
			org.appfacy.Utils.getAccessToken(CFG_DIR, CFG_FILE, properties, scanner);
		}
		System.out.println("Validación de configuración correcta.");
		logger.info("Inicio correcto");
		
		System.out.println("Bienvenido al cliente para Facebook FBcmd4J "+APP_VERSION);
		
		int seleccion;
		
		try {
			while(true) {
				facebook = Utils.configFB(properties);
				
				System.out.println("Opciones: ");
				System.out.println("(0) Salir");
				System.out.println("(1) Newsfeed");
				System.out.println("(2) Wall");
				System.out.println("(3) Publicar");
				System.out.println("(4) Guardar Newsfeed");
				System.out.println("(5) Guardar Wall");
				
				try {
				seleccion = scanner.nextInt();
				scanner.nextLine();
				
				switch(seleccion) {
				case 0:
					System.out.println("Saliendo, cerrando cuenta");
					logger.info("Saliendo, cerrando cuenta");
					System.exit(0);
				case 1: 
					@SuppressWarnings("rawtypes") 
					ResponseList listf = facebook.getHome();
					System.out.println("Ingresa número de prublicaciones a mostrar");
					int numf = scanner.nextInt();
					scanner.nextLine();
					org.appfacy.Utils.getNewsFeed(listf, numf);
					break;
				case 2: 
					@SuppressWarnings("rawtypes") 
					ResponseList listw = facebook.getPosts();
					System.out.println("¿Cuántos recuerdos deseas que se muestren?");
					int numw = scanner.nextInt();
					scanner.nextLine();
					org.appfacy.Utils.getWall(listw, numw);
					break;
				case 3: 
					System.out.println("¿En que estas pensando?");
					facebook.postStatusMessage(scanner.nextLine());
					break;
				case 4: 
					@SuppressWarnings("rawtypes") 
					ResponseList listff = facebook.getHome();
					System.out.println("Ingresa número de publicaciones  a guardar");
					int numff = scanner.nextInt();
					scanner.nextLine();
					System.out.println("Ingresa el directorio donde deseas guardar el archivo:");
					Path dirf = Paths.get(scanner.nextLine());
					org.appfacy.Utils.printNewsFeed(listff, numff, dirf);
					break;
				case 5: 
					@SuppressWarnings("rawtypes") 
					ResponseList listwf = facebook.getPosts();
					System.out.println("¿Cuántas publicaciones deseas guardar?  ");
					int numwf = scanner.nextInt();
					scanner.nextLine();
					System.out.println("Ingresa el directorio donde deseas guardar el archivo:");
					Path dirw = Paths.get(scanner.nextLine());
					org.appfacy.Utils.printWall(listwf, numwf, dirw);
					break;
				default: 
					System.out.println("Error, intenta de nuevo");
					logger.error("Selección inválida por el usuario.");
					break;
				}
				}
				catch (InputMismatchException ex) {
					System.out.println("Ocurrió un error. Consulta el log de la aplicación para más"
							+ " información.");
					logger.error(ex);
				}
			}
		}
		catch (Exception ex) {
			System.out.println("Error, consulta el log de aplicación");
			logger.error(ex);
		}
		}
}