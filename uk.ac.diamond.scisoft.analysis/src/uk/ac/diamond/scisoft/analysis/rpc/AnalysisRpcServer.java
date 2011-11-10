/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rpc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.AnalysisRpcServerProvider;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.IRootFlattener;
import uk.ac.diamond.scisoft.analysis.rpc.internal.AnalysisRpcServerHandler;
import uk.ac.diamond.scisoft.analysis.rpc.internal.AnalysisRpcServerHandlerImpl;
import uk.ac.diamond.scisoft.analysis.rpc.internal.AnalysisRpcWebServer;
import uk.ac.diamond.scisoft.analysis.rpc.internal.HandlerRequestProcessorFactoryFactory;

/**
 * Server class for AnalysisRpc.
 * <p>
 * Within SDA, consider registering new handlers with {@link AnalysisRpcServerProvider} rather than creating a new
 * server on an additional port
 * 
 * @see AnalysisRpcBasicTest See the Ananlysis Rpc Basic Test for an example of use
 */
public class AnalysisRpcServer {
	private static final Logger logger = LoggerFactory.getLogger(AnalysisRpcServer.class);

	private XmlRpcServer xmlRpcServer;
	private WebServer webServer;

	private Map<String, IAnalysisRpcHandler> handlers = Collections.synchronizedMap(new HashMap<String, IAnalysisRpcHandler>());
	private IRootFlattener flattener = FlatteningService.getFlattener();

	/**
	 * Create a new AnalysisRpc server that listens on the given port
	 * 
	 * @param port
	 *            to listen on
	 */
	public AnalysisRpcServer(int port) {
		try {
			webServer = new AnalysisRpcWebServer(port, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
		} catch (UnknownHostException e1) {
			logger.error("Failed to get InetAddress of localhost", e1);
		}

		xmlRpcServer = webServer.getXmlRpcServer();

		PropertyHandlerMapping phm = new PropertyHandlerMapping();
		AnalysisRpcServerHandler handler = new AnalysisRpcServerHandlerImpl(this);
		phm.setRequestProcessorFactoryFactory(new HandlerRequestProcessorFactoryFactory(handler));

		try {
			phm.addHandler("Analysis", AnalysisRpcServerHandler.class);
		} catch (XmlRpcException e) {
			logger.error("Failed to addHandler for 'Analysis' to XML-RPC Server", e);
		}
		xmlRpcServer.setHandlerMapping(phm);

		XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
		serverConfig.setEnabledForExtensions(false);
		serverConfig.setContentLengthOptional(false);
	}

	/**
	 * Register a new handler for AnalysisRpc to delegate incoming calls to.
	 * 
	 * @param name
	 *            the registered name
	 * @param handler
	 *            the handler to delegate calls to
	 * @see IAnalysisRpcHandler
	 */
	public void addHandler(String name, IAnalysisRpcHandler handler) {
		handlers.put(name, handler);
	}

	/**
	 * Remove an existing handler from the server. This is an unusual operation to do and exists mostly to enable
	 * testing.
	 * 
	 * @param serviceName
	 *            to unregister
	 */
	public void removeHandler(String serviceName) {
		handlers.remove(serviceName);
	}

	/**
	 * Get the handler registered with the given name. This is an unusual operation to do and exists mostly to enable
	 * testing.
	 * 
	 * @param serviceName
	 *            to query
	 * @return handler associated with this service or <code>null</code> if none registered.
	 */
	public IAnalysisRpcHandler getHandler(String serviceName) {
		return handlers.get(serviceName);
	}

	/**
	 * Start the server. The server can be started before all handlers are added.
	 * 
	 * @throws AnalysisRpcException
	 *             if there is an exception starting the underlying XML-RPC server. The underlying {@link IOException}
	 *             is wrapped.
	 */
	public void start() throws AnalysisRpcException {
		try {
			webServer.start();
		} catch (IOException e) {
			logger.error("Failed to start AnalysisRPC underlying webServer", e);
			throw new AnalysisRpcException(e);
		}
	}

	/**
	 * Shutdown the server and stop servicing requests.
	 */
	public void shutdown() {
		webServer.shutdown();
	}

	public IAnalysisRpcHandler getDestination(String destination) {
		return handlers.get(destination);
	}

	public IRootFlattener getFlattener() {
		return flattener;
	}


}
