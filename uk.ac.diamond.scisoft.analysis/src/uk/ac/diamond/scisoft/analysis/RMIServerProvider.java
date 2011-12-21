/*
 * Copyright 2011 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple interface to Java RMI so that objects can be exported using the defaults encoded in this class.
 */
public class RMIServerProvider {
	private static final Logger logger = LoggerFactory.getLogger(RMIServerProvider.class);
	public final static int DEFAULT_REGISTRYSERVERPORT = 8405;

	private static RMIServerProvider instance = new RMIServerProvider();
	private int port = DEFAULT_REGISTRYSERVERPORT;
	private Registry serverRegistry = null;

	/**
	 * We need to ensure the Remote objects are not garbage collected. Therefore we need to keep a strong reference to
	 * them. If we don't, we can, sometime down the line of a run get:
	 * "java.rmi.NoSuchObjectException: no such object in table" Numerous references on the web about this, for example:
	 * http://stackoverflow.com/questions/645208/java-rmi-nosuchobjectexception-no-such-object-in-table
	 */
	private List<Remote> remotes = new ArrayList<Remote>();

	/**
	 * Get Instance of provider
	 * 
	 * @return instance
	 */
	public static RMIServerProvider getInstance() {
		return instance;
	}

	private RMIServerProvider() {
	}

	/**
	 * Export the remote object given under the named service.
	 * <p>
	 * This is the method you call on the "server" to make an object available.
	 * 
	 * @param serviceName
	 *            name of the service
	 * @param object
	 *            remote to export
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 */
	public synchronized void exportAndRegisterObject(String serviceName, Remote object) throws RemoteException,
			AlreadyBoundException {
		Remote stub = UnicastRemoteObject.exportObject(object, port);

		if (serverRegistry == null) {
			serverRegistry = LocateRegistry.createRegistry(port);
			logger.info("Starting RMI Server on port " + port);
		}
		logger.info("Adding " + serviceName);
		serverRegistry.bind(serviceName, stub);
		remotes.add(object);
	}

	/**
	 * Return a proxy to the object on the server.
	 * <p>
	 * This is the method you call from the "client" code (e.g. the code run from Jython) to access the server.
	 * 
	 * @param host
	 *            host name to connect to, or <code>null</code> for localhost
	 * @param serviceName
	 *            name of a registered service
	 * @return remote proxy remote object
	 * @throws RemoteException
	 * @throws NotBoundException
	 * @throws AccessException
	 */
	public Remote lookup(String host, String serviceName) throws RemoteException, NotBoundException, AccessException {
		Registry registry = LocateRegistry.getRegistry(host, port);
		return registry.lookup(serviceName);
	}

	/**
	 * Provide a port number to use. Allows overriding the default, particularly useful if multiple instances of SDA are
	 * run on the same machine.
	 * 
	 * @param rmiPortNumber
	 *            new port number, or 0 to use default port
	 * @throws IllegalStateException
	 *             if already serving on the previous port
	 * @throws IllegalArgumentException
	 *             if the port number is < 0
	 */
	public void setPort(int rmiPortNumber) throws IllegalStateException, IllegalArgumentException {
		if (rmiPortNumber < 0)
			throw new IllegalArgumentException("Port number must be >= 0");
		if (serverRegistry != null)
			throw new IllegalStateException("RMI Server Provider has already used the existing port, "
					+ "setPort must be called before any handlers are added.");

		if (rmiPortNumber == 0) {
			rmiPortNumber = DEFAULT_REGISTRYSERVERPORT;
		} else {
			port = rmiPortNumber;
		}
	}

	/**
	 * Return Port number in use
	 * 
	 * @return port number
	 */
	public int getPort() {
		return port;
	}

}
