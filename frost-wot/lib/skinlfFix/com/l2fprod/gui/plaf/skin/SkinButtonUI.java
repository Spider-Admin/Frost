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
package com.l2fprod.gui.plaf.skin;

import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;
import javax.swing.*;
import javax.swing.text.View;

import java.awt.*;

/**
 * @author    $Author$
 * @created   27 avril 2002
 * @version   $Revision$, $Date$
 */
public class SkinButtonUI extends BasicButtonUI {

  /**
   * Description of the Field
   */
  protected int dashedRectGapX;
  /**
   * Description of the Field
   */
  protected int dashedRectGapY;
  /**
   * Description of the Field
   */
  protected int dashedRectGapWidth;
  /**
   * Description of the Field
   */
  protected int dashedRectGapHeight;

  /**
   * Description of the Field
   */
  protected Color focusColor;
  /**
   * Description of the Field
   */
  protected Skin skin = SkinLookAndFeel.getSkin();

  private boolean defaults_initialized = false;

  final static SkinButtonUI buttonUI = new SkinButtonUI();

  // ********************************
  //         Paint Methods
  // ********************************

  /*
   *  These rectangles/insets are allocated once for all
   *  ButtonUI.paint() calls.  Re-using rectangles rather than
   *  allocating them in each paint call substantially reduced the time
   *  it took paint to run.  Obviously, this method can't be re-entered.
   */
  private static Rectangle viewRect = new Rectangle();
  private static Rectangle textRect = new Rectangle();
  private static Rectangle iconRect = new Rectangle();

  // ********************************
  //          Paint Methods
  // ********************************

  /**
   * Description of the Method
   *
   * @param g  Description of Parameter
   * @param c  Description of Parameter
   */
  public void paint(Graphics g, JComponent c) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();

    FontMetrics fm = g.getFontMetrics();

    Insets i = c.getInsets();

    viewRect.x = i.left;
    viewRect.y = i.top;
    viewRect.width = b.getWidth() - (i.right + viewRect.x);
    viewRect.height = b.getHeight() - (i.bottom + viewRect.y);

    textRect.x = textRect.y = textRect.width = textRect.height = 0;
    iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

    Font f = c.getFont();
    g.setFont(f);

    // layout the text and icon
    String text = SwingUtilities.layoutCompoundLabel(
        c, fm, b.getText(), b.getIcon(),
        b.getVerticalAlignment(), b.getHorizontalAlignment(),
        b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
        viewRect, iconRect, textRect,
        b.getText() == null ? 0 : defaultTextIconGap
        );

    clearTextShiftOffset();

    // perform UI specific press action, e.g. Windows L&F shifts text
    if (model.isArmed() && model.isPressed()) {
      paintButtonPressed(g, b);
    }

    skin.getButton().paintButton(g, b);

    // Paint the Icon
    if (b.getIcon() != null) {
      paintIcon(g, c, iconRect);
    }

    if (text != null && !text.equals("")) {
      //PENDING(fred): BasicHTML is not used because package private in JDK1.2 (not in 1.3)
      View v = (View) c.getClientProperty("html");
      //BasicHTML.propertyKey
      if (v != null) {
        v.paint(g, textRect);
      }
      else {
        paintText(g, c, textRect, text);
      }
    }

    if (b.isFocusPainted() && b.hasFocus()) {
      // paint UI specific focus
      paintFocus(g, b, viewRect, textRect, iconRect);
    }
  }

  /**
   * Gets the FocusColor attribute of the SkinButtonUI object
   *
   * @return   The FocusColor value
   */
  protected Color getFocusColor() {
    return focusColor;
  }

  // ********************************
  //         Create Listeners
  // ********************************
  //    protected BasicButtonListener createButtonListener(AbstractButton b) {
  //	return new SkinButtonListener(b);
  //    }

  // ********************************
  //            Defaults
  // ********************************
  /**
   * Description of the Method
   *
   * @param b  Description of Parameter
   */
  protected void installDefaults(final AbstractButton b) {
    super.installDefaults(b);
    if (!defaults_initialized) {
      String pp = getPropertyPrefix();
      dashedRectGapX = UIManager.getInt(pp + "dashedRectGapX");
      dashedRectGapY = UIManager.getInt(pp + "dashedRectGapY");
      dashedRectGapWidth = UIManager.getInt(pp + "dashedRectGapWidth");
      dashedRectGapHeight = UIManager.getInt(pp + "dashedRectGapHeight");
      focusColor = UIManager.getColor(pp + "focus");
      defaults_initialized = true;
    }

    skin.getButton().installSkin(b);

  }

  /**
   * Description of the Method
   *
   * @param b  Description of Parameter
   */
  protected void uninstallDefaults(AbstractButton b) {
    super.uninstallDefaults(b);
    defaults_initialized = false;
  }

  /**
   * Description of the Method
   *
   * @param g         Description of Parameter
   * @param b         Description of Parameter
   * @param viewRect  Description of Parameter
   * @param textRect  Description of Parameter
   * @param iconRect  Description of Parameter
   */
  protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
    // focus painted same color as text on Basic??
    int width = b.getWidth();
    int height = b.getHeight();
    g.setColor(getFocusColor());
    BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
        width - dashedRectGapWidth, height - dashedRectGapHeight);
  }

  /**
   * Description of the Method
   *
   * @param g  Description of Parameter
   * @param b  Description of Parameter
   */
  protected void paintButtonPressed(Graphics g, AbstractButton b) {
    setTextShiftOffset();
  }

  // ********************************
  //          Create PLAF
  // ********************************
  /**
   * Description of the Method
   *
   * @param c  Description of Parameter
   * @return   Description of the Returned Value
   */
  public static ComponentUI createUI(JComponent c) {
    buttonUI.skin = SkinLookAndFeel.getSkin();
    buttonUI.defaults_initialized = false;
    return buttonUI;
  }

}

