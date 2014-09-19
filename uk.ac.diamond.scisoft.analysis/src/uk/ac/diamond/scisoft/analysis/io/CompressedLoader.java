/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IFileLoader;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.metadata.IMetaLoader;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.Metadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;

import uk.ac.diamond.scisoft.analysis.utils.FileUtils;

public class CompressedLoader extends AbstractFileLoader  implements IMetaLoader {

	private IFileLoader loader;
	
	public CompressedLoader() {
		
	}
	
	public CompressedLoader(final String file) throws Exception {
		setFile(file);
	}
	
	private static final Pattern ZIP_PATH = Pattern.compile("(.+)\\.(.+)\\."+LoaderFactory.getZipExpression());
	
	public void setFile(final String file) throws Exception {
		
		final Matcher m = ZIP_PATH.matcher((new File(file)).getName());
		if (m.matches()) {
			
			final String name     = m.group(1);
			final String ext      = m.group(2);
			final String zipType  = m.group(3);
			
			final Class<? extends InputStream>   clazz = LoaderFactory.getZipStream(zipType);
			final Constructor<? extends InputStream> c = clazz.getConstructor(InputStream.class);
			
			final InputStream in = c.newInstance(new FileInputStream(file));
			// Hack zip files
			if (in instanceof ZipInputStream) {
				((ZipInputStream)in).getNextEntry();
			}
			
			final File tmp = File.createTempFile(name, "."+ext);
			tmp.deleteOnExit();
			
			// This is slow and unecessary. Should refactor LoaderFactory to 
			// work either from a file path or in memory representation.
			FileUtils.write(new BufferedInputStream(in), tmp);
			
			final Class<? extends IFileLoader> lclass = LoaderFactory.getLoaderClass(ext);
			this.loader = LoaderFactory.getLoader(lclass, tmp.getAbsolutePath());
		}
        
	}
	
	@Override
	public IDataHolder loadFile() throws ScanFileHolderException {
		return loader.loadFile();
	}
	@Override
	public IDataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
		return loader.loadFile(mon);
	}
	
	@Override
	public void loadMetadata(IMonitor mon) throws Exception {
		loader.loadFile(mon);
    }
	
	@Override
	public IMetadata getMetadata() {
		if (loader instanceof IMetaLoader) return ((IMetaLoader)loader).getMetadata();
		return new Metadata();
	}
	
}
