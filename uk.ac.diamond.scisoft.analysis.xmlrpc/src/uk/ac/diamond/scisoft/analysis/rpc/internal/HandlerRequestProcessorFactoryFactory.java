/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rpc.internal;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;

/**
 * We have our own FactoryFactory so we can use an instantiated AnalysisRpcServerHandler with state. The alternative is
 * to do the default XML-RPC behaviour which creates a new instance of the given type to service each request.
 */
public class HandlerRequestProcessorFactoryFactory implements RequestProcessorFactoryFactory {
	private final RequestProcessorFactory factory = new HandlerRequestProcessorFactory();
	private final AnalysisRpcServerHandler handler;

	public HandlerRequestProcessorFactoryFactory(AnalysisRpcServerHandler handler) {
		this.handler = handler;
	}

	@Override
	public RequestProcessorFactory getRequestProcessorFactory(@SuppressWarnings("rawtypes") Class aClass)
			throws XmlRpcException {
		return factory;
	}

	private class HandlerRequestProcessorFactory implements RequestProcessorFactory {
		@Override
		public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest) throws XmlRpcException {
			return handler;
		}
	}
}
