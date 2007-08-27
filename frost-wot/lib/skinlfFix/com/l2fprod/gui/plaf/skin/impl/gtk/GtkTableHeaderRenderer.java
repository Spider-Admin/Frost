/* ====================================================================
 *
 * Skin Look And Feel 1.2.10 License.
 *
 * Copyright (c) 2000-2004 L2FProd.com.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by L2FProd.com
 *        (http://www.L2FProd.com/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Skin Look And Feel", "SkinLF" and "L2FProd.com" must not
 *    be used to endorse or promote products derived from this software
 *    without prior written permission. For written permission, please
 *    contact info@L2FProd.com.
 *
 * 5. Products derived from this software may not be called "SkinLF"
 *    nor may "SkinLF" appear in their names without prior written
 *    permission of L2FProd.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL L2FPROD.COM OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package com.l2fprod.gui.plaf.skin.impl.gtk;

import com.l2fprod.gui.plaf.skin.DefaultButton;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Description of the Class
 *
 * @author    fred
 * @created   27 avril 2002
 */
public class GtkTableHeaderRenderer
  extends DefaultTableCellRenderer
  implements javax.swing.plaf.UIResource {

  boolean isSelected;
  boolean hasFocus;

  transient DefaultButton itemSelected, itemUnselected;

  /**
   * Constructor for the GtkTableHeaderRenderer object
   */
  public GtkTableHeaderRenderer(
    DefaultButton itemSelected,
    DefaultButton itemUnselected) {
    setOpaque(false);
    this.itemSelected = itemSelected;
    this.itemUnselected = itemUnselected;
    
	setHorizontalTextPosition(LEFT);
	setHorizontalAlignment(CENTER);
  }

  /**
   * Gets the TableCellRendererComponent attribute of the
   * GtkTableHeaderRenderer object
   *
   * @param table       Description of Parameter
   * @param value       Description of Parameter
   * @param isSelected  Description of Parameter
   * @param hasFocus    Description of Parameter
   * @param row         Description of Parameter
   * @param column      Description of Parameter
   * @return            The TableCellRendererComponent value
   */
  public Component getTableCellRendererComponent(
    JTable table,
    Object value,
    boolean isSelected,
    boolean hasFocus,
    int row,
    int column) {
    if (table != null) {
      JTableHeader header = table.getTableHeader();
      if (header != null) {
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        setFont(header.getFont());
      }
    }

    this.isSelected = isSelected;
    this.hasFocus = hasFocus;
    setText((value == null) ? "" : value.toString());
    return this;
  }

  /**
   * Description of the Method
   *
   * @param g  Description of Parameter
   */
  protected void paintComponent(Graphics g) {
    if (isSelected || hasFocus) {
      itemSelected.paint(g, this);
    } else {
      itemUnselected.paint(g, this);
    }
    super.paintComponent(g);
  }
}
