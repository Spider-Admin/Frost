package org.shiftone.cache.util;



/**
 * Class LinkedListNode
 *
 *
 * @author <a href="mailto:jeff@shiftone.org">Jeff Drost</a>
 * @version $Revision$
 */
public class LinkedListNode
{

    LinkedListNode next;
    LinkedListNode prev;
    Object         value;

    public LinkedListNode(Object value)
    {
        this.value = value;
    }


    /**
     * Method getValue
     */
    public Object getValue()
    {
        return value;
    }


    /**
     * Method getNext
     */
    public LinkedListNode getNext()
    {

        return (next.isHeaderNode()
                ? null
                : next);
    }


    /**
     * Method getPrevious
     */
    public LinkedListNode getPrevious()
    {

        return (prev.isHeaderNode()
                ? null
                : prev);
    }


    /**
     * is this node the header node in a linked list?
     */
    boolean isHeaderNode()
    {
        return (value == this);
    }
}
