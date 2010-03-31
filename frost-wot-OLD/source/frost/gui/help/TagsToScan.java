/* RCS-ID:      $Id$ */
/*
 * TagsToScan.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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

package frost.gui.help;

import java.util.*;

import javax.swing.text.html.*;

  
  /**
   * 
   *
   *
  
   *
   *
   */
  


/**
 * * List of known Tags (Standard Ed. 6 beta2-b76)
 *  A 
 *  ADDRESS 
 *  APPLET 
 *  AREA 
 *  B 
 *  BASE 
 *  BASEFONT 
 *  BIG 
 *  BLOCKQUOTE 
 *  BODY 
 *  BR 
 *  CAPTION 
 *  CENTER 
 *  CITE 
 *  CODE 
 *  COMMENT //All comments are labeled with this tag.
 *  CONTENT // All text content is labeled with this tag.
 *  DD 
 *  DFN 
 *  DIR 
 *  DIV 
 *  DL 
 *  static HTML.Tag  DT 
 *  static HTML.Tag  EM 
 *  static HTML.Tag  FONT 
 *  static HTML.Tag  FORM 
 *  static HTML.Tag  FRAME 
 *  static HTML.Tag  FRAMESET 
 *  static HTML.Tag  H1 
 *  static HTML.Tag  H2 
 *  static HTML.Tag  H3 
 *  static HTML.Tag  H4 
 *  static HTML.Tag  H5 
 *  static HTML.Tag  H6 
 *  static HTML.Tag  HEAD 
 *  static HTML.Tag  HR 
 *  static HTML.Tag  HTML 
 *  static HTML.Tag  I 
 *  static HTML.Tag  IMG 
 *  static HTML.Tag  IMPLIED  //All text content must be in a paragraph element.
 *  static HTML.Tag  INPUT 
 *  static HTML.Tag  ISINDEX 
 *  static HTML.Tag  KBD 
 *  static HTML.Tag  LI 
 *  static HTML.Tag  LINK 
 *  static HTML.Tag  MAP 
 *  static HTML.Tag  MENU 
 *  static HTML.Tag  META 
 *  static HTML.Tag  NOFRAMES 
 *  static HTML.Tag  OBJECT 
 *  static HTML.Tag  OL 
 *  static HTML.Tag  OPTION 
 *  static HTML.Tag  P 
 *  static HTML.Tag  PARAM 
 *  static HTML.Tag  PRE 
 *  static HTML.Tag  S 
 *  static HTML.Tag  SAMP 
 *  static HTML.Tag  SCRIPT 
 *  static HTML.Tag  SELECT 
 *  static HTML.Tag  SMALL 
 *  static HTML.Tag  SPAN 
 *  static HTML.Tag  STRIKE 
 *  static HTML.Tag  STRONG 
 *  static HTML.Tag  STYLE 
 *  static HTML.Tag  SUB 
 *  static HTML.Tag  SUP 
 *  static HTML.Tag  TABLE 
 *  static HTML.Tag  TD 
 *  static HTML.Tag  TEXTAREA 
 *  static HTML.Tag  TH 
 *  static HTML.Tag  TITLE 
 *  static HTML.Tag  TR 
 *  static HTML.Tag  TT 
 *  static HTML.Tag  U 
 *  static HTML.Tag  UL 
 *  static HTML.Tag  VAR 
 *            
 *   *
 *   *
 *   *
 *  list of known atrributes (Standard Ed. 6 beta2-b76)
 *   
 * static HTML.Attribute  ACTION 
 *  static HTML.Attribute  ALIGN 
 *  static HTML.Attribute  ALINK 
 *            
 *  static HTML.Attribute  ALT 
 *            
 *  static HTML.Attribute  ARCHIVE 
 *            
 *  static HTML.Attribute  BACKGROUND 
 *            
 *  static HTML.Attribute  BGCOLOR 
 *            
 *  static HTML.Attribute  BORDER 
 *            
 *  static HTML.Attribute  CELLPADDING 
 *            
 *  static HTML.Attribute  CELLSPACING 
 *            
 *  static HTML.Attribute  CHECKED 
 *            
 *  static HTML.Attribute  CLASS 
 *            
 *  static HTML.Attribute  CLASSID 
 *            
 *  static HTML.Attribute  CLEAR 
 *            
 *  static HTML.Attribute  CODE 
 *            
 *  static HTML.Attribute  CODEBASE 
 *            
 *  static HTML.Attribute  CODETYPE 
 *            
 *  static HTML.Attribute  COLOR 
 *            
 *  static HTML.Attribute  COLS 
 *            
 *  static HTML.Attribute  COLSPAN 
 *            
 *  static HTML.Attribute  COMMENT 
 *            
 *  static HTML.Attribute  COMPACT 
 *            
 *  static HTML.Attribute  CONTENT 
 *            
 *  static HTML.Attribute  COORDS 
 *            
 *  static HTML.Attribute  DATA 
 *            
 *  static HTML.Attribute  DECLARE 
 *            
 *  static HTML.Attribute  DIR 
 *            
 *  static HTML.Attribute  DUMMY 
 *            
 *  static HTML.Attribute  ENCTYPE 
 *            
 *  static HTML.Attribute  ENDTAG 
 *            
 *  static HTML.Attribute  FACE 
 *            
 *  static HTML.Attribute  FRAMEBORDER 
 *            
 *  static HTML.Attribute  HALIGN 
 *            
 *  static HTML.Attribute  HEIGHT 
 *            
 *  static HTML.Attribute  HREF 
 *            
 *  static HTML.Attribute  HSPACE 
 *            
 *  static HTML.Attribute  HTTPEQUIV 
 *            
 *  static HTML.Attribute  ID 
 *            
 *  static HTML.Attribute  ISMAP 
 *            
 *  static HTML.Attribute  LANG 
 *            
 *  static HTML.Attribute  LANGUAGE 
 *            
 *  static HTML.Attribute  LINK 
 *            
 *  static HTML.Attribute  LOWSRC 
 *            
 *  static HTML.Attribute  MARGINHEIGHT 
 *            
 *  static HTML.Attribute  MARGINWIDTH 
 *            
 *  static HTML.Attribute  MAXLENGTH 
 *            
 *  static HTML.Attribute  METHOD 
 *            
 *  static HTML.Attribute  MULTIPLE 
 *            
 *  static HTML.Attribute  N 
 *            
 *  static HTML.Attribute  NAME 
 *            
 *  static HTML.Attribute  NOHREF 
 *            
 *  static HTML.Attribute  NORESIZE 
 *            
 *  static HTML.Attribute  NOSHADE 
 *            
 *  static HTML.Attribute  NOWRAP 
 *            
 *  static HTML.Attribute  PROMPT 
 *            
 *  static HTML.Attribute  REL 
 *            
 *  static HTML.Attribute  REV 
 *            
 *  static HTML.Attribute  ROWS 
 *            
 *  static HTML.Attribute  ROWSPAN 
 *            
 *  static HTML.Attribute  SCROLLING 
 *            
 *  static HTML.Attribute  SELECTED 
 *            
 *  static HTML.Attribute  SHAPE 
 *            
 *  static HTML.Attribute  SHAPES 
 *            
 *  static HTML.Attribute  SIZE 
 *            
 *  static HTML.Attribute  SRC 
 *            
 *  static HTML.Attribute  STANDBY 
 *            
 *  static HTML.Attribute  START 
 *            
 *  static HTML.Attribute  STYLE 
 *            
 *  static HTML.Attribute  TARGET 
 *            
 *  static HTML.Attribute  TEXT 
 *            
 *  static HTML.Attribute  TITLE 
 *            
 *  static HTML.Attribute  TYPE 
 *            
 *  static HTML.Attribute  USEMAP 
 *            
 *  static HTML.Attribute  VALIGN 
 *            
 *  static HTML.Attribute  VALUE 
 *            
 *  static HTML.Attribute  VALUETYPE 
 *            
 *  static HTML.Attribute  VERSION 
 *            
 *  static HTML.Attribute  VLINK 
 *            
 *  static HTML.Attribute  VSPACE 
 *            
 *  static HTML.Attribute  WIDTH 
 *            
 *   *
 *   *
 *   the list of attributes 4.01: http://www.w3.org/TR/html401/index/attributes.html
 *   *
 *   *
 *   *% URI stuff
 * 
 * Name        Related Elements	Type	Default         Depr.	DTD	Comment
 * 
 * action      FORM                %URI;       #REQUIRED	 	 	server-side form handler
 * archive     APPLET              CDATA       #IMPLIED	D	L	comma-separated archive list
 * archive     OBJECT              CDATA	    #IMPLIED	 	 	space-separated list of URIs
 * background  BODY                %URI;       #IMPLIED	D	L	texture tile for document background
 * cite        BLOCKQUOTE, Q	%URI;	    #IMPLIED	 	 	URI for source document or msg
 * cite        DEL, INS            %URI;	    #IMPLIED	 	 	info on reason for change
 * classid     OBJECT              %URI;	    #IMPLIED	 	 	identifies an implementation
 * code        APPLET              CDATA	    #IMPLIED	D	L	applet class file
 * codebase    OBJECT              %URI;	    #IMPLIED	 	 	base URI for classid, data, archive
 * codebase    APPLET              %URI;	    #IMPLIED	D	L	optional base URI for applet
 * content     META                CDATA	    #REQUIRED	 	 	associated information
 * data        OBJECT              %URI;	    #IMPLIED	 	 	reference to object's data
 * defer       SCRIPT              (defer)	    #IMPLIED	 	 	UA may defer execution of script
 * href        A, AREA, LINK	%URI;	    #IMPLIED	 	 	URI for linked resource
 * href        BASE                %URI;	    #IMPLIED	 	 	URI that acts as base URI
 * longdesc    IMG         	%URI;	    #IMPLIED	 	 	link to long description (complements alt)
 * profile     HEAD                %URI;	    #IMPLIED	 	 	named dictionary of meta info
 * rel         A, LINK             %LinkTypes; #IMPLIED	 	 	forward link types
 * rev         A, LINK             %LinkTypes; #IMPLIED	 	 	reverse link types
 * src         SCRIPT              %URI;       #IMPLIED	 	 	URI for an external script
 * src         INPUT               %URI;       #IMPLIED	 	 	for fields with images
 * src         IMG                 %URI;       #REQUIRED	 	 	URI of image to embed
 * usemap      IMG, INPUT, OBJECT	%URI;       #IMPLIED	 	 	use client-side image map
 * 
 * FRAMES stuff  
 *    
 * target      A, AREA, BASE, FORM, LINK
 *                                %FrameTarget;	#IMPLIED 	L	render in this frame
 * longdesc    FRAME, IFRAME	%URI;	#IMPLIED	 	F	link to long description (complements title)
 * src         FRAME, IFRAME	%URI;	#IMPLIED	 	F	source of frame content
 * 
 * SCRIPT stuff   
 * 
 * onblur      A, AREA, BUTTON, INPUT, LABEL, SELECT, TEXTAREA
 *                                %Script;	#IMPLIED	 	 	the element lost the focus
 * onchange    INPUT, SELECT, TEXTAREA
 *                                %Script;	#IMPLIED	 	 	the element value was changed
 * onclick     All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                               %Script;         #IMPLIED	 	 	a pointer button was clicked
 * ondblclick  All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE
 *                                %Script;	#IMPLIED	 	 	a pointer button was double clicked
 * onfocus     A, AREA, BUTTON, INPUT, LABEL, SELECT, TEXTAREA
 *                                %Script;	#IMPLIED	 	 	the element got the focus
 * onkeydown   All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                                %Script;	#IMPLIED	 	 	a key was pressed down
 * onkeypress  All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                                %Script;	#IMPLIED	 	 	a key was pressed and released
 * onkeyup     All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                                %Script;	#IMPLIED	 	 	a key was released
 * onload      FRAMESET            %Script;	#IMPLIED	 	F	all the frames have been loaded
 * onload      BODY                %Script;	#IMPLIED	 	 	the document has been loaded
 * onmousedown All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                                %Script;	#IMPLIED	 	 	a pointer button was pressed down
 * onmousemove All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                                %Script;	#IMPLIED	 	 	a pointer was moved within
 * onmouseout  All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                                %Script;	#IMPLIED	 	 	a pointer was moved away
 * onmouseover All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                                %Script;	#IMPLIED	 	 	a pointer was moved onto
 * onmouseup   All elements but APPLET, BASE, BASEFONT, BDO, BR, FONT, FRAME, FRAMESET, HEAD, HTML, IFRAME, ISINDEX, META, PARAM, SCRIPT, STYLE, TITLE	
 *                                %Script;	#IMPLIED	 	 	a pointer button was released
 * onreset     FORM                %Script;	#IMPLIED	 	 	the form was reset
 * onselect    INPUT, TEXTAREA	%Script;	#IMPLIED	 	 	some text was selected
 * onsubmit    FORM                %Script;	#IMPLIED	 	 	the form was submitted
 * onunload    FRAMESET            %Script;	#IMPLIED	 	F	all the frames have been removed
 * onunload    BODY                %Script;	#IMPLIED	 	 	the document has been removed
 * @author notitaccu
 */
public class TagsToScan {
  
  private static Vector tagList = null;
  // private static vector(HTML.TAG, object) = ( ( HTML.Tag.IMG, (Object)HTML.Attribute.SRC),
  //                                                 HTML.Attribute.
  
  /** Creates a new instance of TagsToScan */
  public TagsToScan() {
    LoadTagList();
    
  }




  private void LoadTagList() {
    if (tagList == null) {
      tagList = new Vector();
      
      
      
      
      
      
      
    }
  }
  
  /**
   * HasTagUri
   * @param t the HTML.Tag to check
   * @return null if none 
   * HTML.Attribute or HTML.Attribute[] if uri-containing atribbute(s) present
   */
  public  Object HasTagUri(HTML.Tag t) {
    LoadTagList();
    // return vector(
    return null;
  }
  
}
