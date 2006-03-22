/*
 SAXTest.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation; either version 2 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.identities;

public class SAXTest {

//    public static void main(String[] args) {
//
//        DefaultHandler handler = new MyHandler();
//
//        parseXmlFile("C:\\Projects\\fr-wot\\identities.xml", handler, false);
//    }
//    
//    static class MyHandler extends DefaultHandler {
//        
//        @Override
//        public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
//            super.characters(arg0, arg1, arg2);
//            String s = new String(arg0, arg1, arg2);
//            System.out.println("characters: "+s);
//        }
//
//        @Override
//        public void endDocument() throws SAXException {
//            super.endDocument();
//            System.out.println("endDocument");
//        }
//
//        @Override
//        public void endElement(String uri, String localName, String qName) throws SAXException {
//            super.endElement(uri, localName, qName);
//            System.out.println("endElement: "+qName);
//        }
//
//        @Override
//        public void error(SAXParseException arg0) throws SAXException {
//            super.error(arg0);
//            System.out.println("error:"+arg0.toString());
//        }
//
////        @Override
////        public void fatalError(SAXParseException arg0) throws SAXException {
////            super.fatalError(arg0);
////            System.out.println("fatalError");
////        }
//
//        @Override
//        public void startDocument() throws SAXException {
//            super.startDocument();
//            System.out.println("startDocument");
//        }
//
//        @Override
//        public void warning(SAXParseException arg0) throws SAXException {
//            super.warning(arg0);
//            System.out.println("warning:"+arg0.toString());
//        }
//
//        public void startElement(String namespaceURI, String localName,
//                                 String qName, Attributes atts)  {
//            
//            System.out.println("startElement: "+qName);
//
//            // Get the number of attribute
//            int length = atts.getLength();
//    
//            // Process each attribute
//            for (int i=0; i<length; i++) {
//                // Get names and values for each attribute
//                String name = atts.getQName(i);
//                String value = atts.getValue(i);
//                System.out.println("att:"+name+"="+value);
//            }
//        }
//    }
//    
//    // Parses an XML file using a SAX parser.
//    // If validating is true, the contents is validated against the DTD
//    // specified in the file.
//    public static void parseXmlFile(String filename, DefaultHandler handler, boolean validating) {
//        try {
//            // Create a builder factory
//            SAXParserFactory factory = SAXParserFactory.newInstance();
//            factory.setValidating(validating);
//
//            // Create the builder and parse the file
//            factory.newSAXParser().parse(new File(filename), handler);
//        } catch (SAXException e) {
//            // A parsing error occurred; the xml input is not valid
//            e.printStackTrace();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
