/* 140
*   Copyright (c) 2001 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
*
*   The author makes no representations or warranties about the suitability of the
*   software, either express or implied, including but not limited to the
*   implied warranties of merchantability, fitness for a particular
*   purpose, or non-infringement. The author shall not be liable for any damages
*   suffered by licensee as a result of using, modifying or distributing
*   this software or its derivatives.
*
*   The author requests that he be notified of any application, applet, or other binary that
*   makes use of this code and that some acknowedgement is given. Comments, questions and
*   requests for change will be welcomed.
*/
package mseries.ui;
import javax.swing.event.*;

/**
*   The SpinnerModel interface represents a sequence of values. Each value can only be accessed
*   using the getValue(), getNextValue() or getPreviousValue() methods. Random access is not
*   allowed. The set of values in the sequence is unbounded though implementations are at liberty
*   to impose bounds. The step is the amount which the current value will change when the next or
*   previous values are requested.
*<P>
*   A Spinner model is usually rendered in the Spinner by a SpinnerEditor which is aware of the
*   actual data types encapsuated in any particular model. The model notifies the editor of changes
*   by firing ChangeEvents.
*   @see mseries.ui.SpinnerEditor
*/
public interface SpinnerModel
{
    /** Returns the current value */
    public Object getValue();

    /** Sets a value in the model which may or may not be in the sequence */
    public void setValue(Object v);
    /** Sets the step size to indicate how far getNextValue and getPreviousValue should
    *   move in the set.
    */
    public void setStep(int step);
    /**
    *   Advances to and returns the next value in the sequence depending on the step size
    */
    public Object getNextValue();
    /**
    *   Advances to and returns the previous value in the sequence depending on the step size
    */
    public Object getPreviousValue();

    /** ChangeEvents are fired to registered listeners when the current value is changed
    */
    public void addChangeListener(ChangeListener x);
    public void removeChangeListener(ChangeListener x);
}
