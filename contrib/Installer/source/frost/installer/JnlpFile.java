/*
 * Created on Jan 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.installer;

import java.io.*;
import java.io.IOException;
import java.net.*;
import java.net.URL;
import java.util.*;
import java.util.Properties;

import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.crimson.tree.*;
import org.apache.crimson.tree.ParseContext;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JnlpFile {

	/**
	 * @author Administrator
	 *
	 * To change the template for this generated type comment go to
	 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
	 */
	private class XMLErrorHandler extends DefaultHandler {

		private boolean valid = true;

		/**
		 * 
		 */
		private XMLErrorHandler() {
			super();
		}

		/**
		 * @return
		 */
		private boolean isValid() {
			return valid;
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
		 */
		public void error(SAXParseException e) throws SAXException {
			super.error(e);
			System.out.println(e.getMessage());
			valid = false;
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		public void fatalError(SAXParseException e) throws SAXException {
			super.fatalError(e);
			System.out.println(e.getMessage());
			valid = false;
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
		 */
		public void warning(SAXParseException e) throws SAXException {
			super.warning(e);
			System.out.println(e.getMessage());
			valid = false;
		}

	}

	private static final String DOCTYPE_MARKER = "<!-- INSERT DOCTYPE HERE -->";
	private static final String DOCTYPE_HEADER_PREFIX =
		"<!DOCTYPE jnlp PUBLIC \"-//Sun Microsystems,Inc//DTD JNLP Descriptor 1.0//EN\"";

	private URL remoteLocation = null;
	private URL dtdLocation = null;

	private StringBuffer content = null;
	private Document document = null;
	private XMLErrorHandler xmlErrorHandler = null;
	private boolean usingCrimson = false;

	private static final int STATE_INITIAL = 0;
	private static final int STATE_DOWNLOADED = 1;
	private static final int STATE_PARSED_VALID = 2;
	private static final int STATE_PARSED_INVALID = 3;
	private int state = STATE_INITIAL;

	/**
	 * 
	 */
	public JnlpFile() {
		super();
	}

	/**
	 * @param jnlpRemoteLocation
	 */
	public void setRemoteLocation(URL newRemoteLocation) {
		remoteLocation = newRemoteLocation;
	}

	/**
	 * 
	 */
	public void download() throws IOException {
		BufferedReader reader = null;
		state = STATE_INITIAL;
		try {
			URLConnection connection = remoteLocation.openConnection();
			connection.setDoInput(true);
			connection.setUseCaches(false);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer jnlpBuffer = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				jnlpBuffer.append(line);
			}
			content = jnlpBuffer;
			state = STATE_DOWNLOADED;
		} catch (IOException exception) {
			throw exception;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * @return
	 */
	public boolean isValid() throws JnlpException {
		if (state == STATE_PARSED_VALID) {
			return true;
		}
		if (state == STATE_PARSED_INVALID) {
			return false;
		}
		throw new JnlpException("The JNLP file has not been parsed yet.");
	}

	/**
	 * @return
	 */
	public void parseDocument() throws JnlpException {
		if (state != STATE_PARSED_VALID && state != STATE_PARSED_INVALID) {
			if (state != STATE_DOWNLOADED) {
				throw new JnlpException("The JNLP file has not been downloaded yet.");
			} else {
				try {
					if (insertDoctype()) {

						DocumentBuilderFactory factory = createDocumentBuilderFactory();
						factory.setValidating(true);

						DocumentBuilder builder = factory.newDocumentBuilder();
						xmlErrorHandler = new XMLErrorHandler();
						builder.setErrorHandler(xmlErrorHandler);

						StringReader contentReader = new StringReader(content.toString());
						document = builder.parse(new InputSource(contentReader));
						contentReader.close();

						if (xmlErrorHandler.isValid()) {
							state = STATE_PARSED_VALID;
						} else {
							state = STATE_PARSED_INVALID;
						}
					} else {
						state = STATE_PARSED_INVALID;
					}
				} catch (IOException exception) {
					throw new JnlpException(
						"Error while reading the content: \n" + exception.getMessage(),
						exception);
				} catch (SAXException exception) {
					throw new JnlpException(
						"Error while parsing the content: \n" + exception.getMessage(),
						exception);
				} catch (ParserConfigurationException exception) {
					throw new JnlpException(
						"Error while configuring the parser: \n" + exception.getMessage(),
						exception);
				}
			}
		}
	}
	

	/**
	 * @return
	 */
	private boolean insertDoctype() {
		int markerPosition = content.indexOf(DOCTYPE_MARKER);
		if (markerPosition == -1) {
			return false;
		} else {
			String doctype = DOCTYPE_HEADER_PREFIX + " \"" + dtdLocation.toString() + "\">";
			content.replace(markerPosition, markerPosition + DOCTYPE_MARKER.length(), doctype);
			return true;
		}
	}

	/**
	 * @param url
	 */
	public void setDtdLocation(URL url) {
		dtdLocation = url;
	}

	/**
	 * @param url
	 */
	public void replaceCodebase(URL url) throws JnlpException {
		if (state == STATE_PARSED_VALID) {
			Element jnlpElement = document.getDocumentElement();
			jnlpElement.setAttribute("codebase", url.toString());
		} else {
			throw new JnlpException("The JNLP file is not valid.");
		}
	}

	/**
	 * @param jnlpLocalDirectory
	 */
	public File writeToLocalDirectory(File jnlpLocalDirectory) throws JnlpException, IOException {
		if (state == STATE_PARSED_VALID) {
			if (jnlpLocalDirectory.isDirectory() || jnlpLocalDirectory.mkdirs()) {
				Element jnlpElement = document.getDocumentElement();
				File localFile = new File(jnlpLocalDirectory, jnlpElement.getAttribute("href"));
				if (usingCrimson) {
					writeToLocalFileCrimson(localFile);
				} else {
					writeToLocalFileDefault(localFile);	
				}
				return localFile;
			} else {
				throw new JnlpException("Could not create the directory where the JNLP was to be written.");
			}
		} else {
			throw new JnlpException("The JNLP file is not valid.");
		}

	}
	
	/**
		 * @param jnlpLocalDirectory
		 */
	private void writeToLocalFileDefault(File localFile) throws JnlpException, IOException {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(localFile), "UTF-8");
			StreamResult streamResult = new StreamResult(writer);

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			try {
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			} catch (IllegalArgumentException exception) {
				//It would have been nice if it supported those options, but we go ahead anyway	
			}
			DOMSource source = new DOMSource(document);
			transformer.transform(source, streamResult);
		} catch (IOException exception) {
			throw exception;
		} catch (TransformerConfigurationException exception) {
			throw new JnlpException(
				"Error while configuring the transformer: \n" + exception.getMessage(),
				exception);
		} catch (TransformerException exception) {
			throw new JnlpException(
				"Error while transforming the content: \n" + exception.getMessage(),
				exception);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
		
		/**
		 * @return
		 */
		private DocumentBuilderFactory createDocumentBuilderFactory() {
			String oldProperty = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
			System.setProperty(
				"javax.xml.parsers.DocumentBuilderFactory",
				"org.apache.crimson.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory factory = null;
			try {
				factory = DocumentBuilderFactory.newInstance();
				usingCrimson = true;
			} catch (FactoryConfigurationError exception) {
				//So Crimson is not available. The default implementation will have to do.
				usingCrimson = false;
			}
			if (oldProperty == null) {
				System.getProperties().remove("javax.xml.parsers.DocumentBuilderFactory");
			} else {
				System.setProperty("javax.xml.parsers.DocumentBuilderFactory", oldProperty);
			}
			if (factory == null) {
				factory = DocumentBuilderFactory.newInstance();
			}
			return factory;
		}
		
	/**
		 * @param jnlpLocalDirectory
		 */
	private void writeToLocalFileCrimson(File localFile) throws IOException {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(localFile), "UTF-8");
			StreamResult streamResult = new StreamResult(writer);

			XmlDocument xmlDocument = (XmlDocument) document;
			Node doctype = xmlDocument.getFirstChild();
			xmlDocument.removeChild(doctype);
			xmlDocument.write(writer);
		} catch (IOException exception) {
			throw exception;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

}
