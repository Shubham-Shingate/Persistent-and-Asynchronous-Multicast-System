package com.uga.coordinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Coordinator {

	public static Map<Date, String> messageBuffer;

	public static Map<String, Participant> participants;

	public static Properties appProperties;

	public static long thresholdTd;

	public static int coOrdinatorPort;

	static {
		messageBuffer = Collections.synchronizedMap(new HashMap<Date, String>());
		participants = Collections.synchronizedMap(new HashMap<String, Participant>());
	}

	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.err.println("Pass the config file name for co-ordinator execution as the command line argument only");
			return;
		}

		/** --------Load the properties file into the application-------- */
		appProperties = new Properties();
		File propertyFile = new File(args[0]);
		if (!propertyFile.exists()) {
			System.err.println("The property file with the given name does not exists");
			return;
		}
		FileInputStream fis = new FileInputStream(propertyFile);
		try {
			appProperties.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/** -----Get the properties from Properties Object------ */
		coOrdinatorPort = Integer.parseInt(appProperties.getProperty("coordinator.port"));
		thresholdTd = Long.parseLong(appProperties.getProperty("coordinator.threshold"));

		/** -------Instantiate a messenger thread------- */
		MessengerThread messengerRunnable = new MessengerThread();
		Thread messengerThread = new Thread(messengerRunnable);
		messengerThread.start();

		/** -------Instantiate a handler threads------- */
		try (ServerSocket listener = new ServerSocket(coOrdinatorPort)) {
			System.out.println("The Co-Ordinator has started!!!");
			ExecutorService pool = Executors.newFixedThreadPool(10);
			while (true) {
				pool.execute(new ParticipantHandlerThread(listener.accept()));
			}
		}

	}

}
