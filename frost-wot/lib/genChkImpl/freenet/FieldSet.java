package freenet;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import freenet.crypt.Digest;
import freenet.support.Fields;
import freenet.support.io.*;
import freenet.support.sort.*;

/**
 * This is a wrapper for a Hashtable containing strings and other FieldsSets
 * that also includes a parser/printer in the format used for fields in normal
 *  Freenet messages. FieldSets are used for almost all data serializing
 * (DataProperties, DataStore to disk, client metadata, etc) in Freenet.
 *
 * @author oskar
 */
public class FieldSet {

	private static Logger logger = Logger.getLogger(FieldSet.class.getName());

    /**
     * These are non-UTF8-legal bytes used in hashing the FieldSet.
     */
    public static final byte HASH_SUBSET  = (byte) 0xFD,   // .
                             HASH_EQUALS  = (byte) 0xFE,   // =
                             HASH_NEWLINE = (byte) 0xFF;   // \n

    /**
     * The maximum size in chars that a fieldset read by the parseFields
     * method can have
     */
    public static final long MAX_PARSE_SIZE = 524288;

    protected final Hashtable fields;

    /**
     * Interface to filter field names and values when parsing.
     */
    public static interface Filter {
        public String filter(String s);
    }

    private static class VoidFilter implements Filter {
        public String filter(String s) {
            return s;
        }
    }

    private static final Filter voidF = new VoidFilter();


    /**
     *  Construct an empty FieldSet
     */
    public FieldSet() {
        this.fields = new Hashtable();
    }

    /**
     *  Construct a FieldSet from the given stream using the default separators
     */
    public FieldSet(ReadInputStream in) throws IOException {
        this();
        parseFields(in);
    }

    protected FieldSet(FieldSet fs) {
        this.fields = fs.fields;
    }

    /**
     * Get a string value
     * @param name the name of the field
     * @param def  the default string to return
     * @return The field interpreted as a string
     */
    public String get(String s) {
        Object o = fields.get(s);
        if (o == null || !(o instanceof String))
            return null;
        String value = (String) fields.get(s);
        return value;
    }

    /**
     * Get a field subset.
     * @param name the name of the field
     * @return The field interpreted as a subset of fields. Null if no
     *         such field is found.
     */
    public FieldSet getSet(String s) {
        Object o = fields.get(s);
        if (o == null || !(o instanceof FieldSet))
            return null;
        return (FieldSet) o;
    }

    public FieldSet makeSet(String s) {
	Object o = fields.get(s);
	if(o == null) {
	    o = newFieldSet();
	    fields.put(s, o);
	}
	if(!(o instanceof FieldSet))
	    return null;
	return (FieldSet)o;
    }

    /**
     * Remove a field
     * @param name The name of the field to remove
     */
    public final Object remove(String s) {
        return fields.remove(s);
    }

    /**
     * Clear all elements, _but not sub-FieldSet's_
     */
    public final void clearElements() {
	for(Enumeration k = fields.keys(); k.hasMoreElements();) {
	    Object x = k.nextElement();
	    Object y = fields.get(x);
	    if(!(y instanceof FieldSet))
		fields.remove(x);
	    else
		((FieldSet)y).clearElements();
	}
    }

    /**
     * Add a string value. This will overwrite any old values for this field.
     * @param name      The name of the field.
     * @param value     The value to set to the field.
     */
    public final void put(String name, String value) {
        fields.put(name, value);
    }

    /**
     * Add a subset. This will overwrite any old values for this field.
     * @param name   The name of the field.
     * @param value  The value to set to the field.
     */
    public final void put(String name, FieldSet fs) {
        fields.put(name, fs);
    }

    /**
     * Add a string value. This will overwrite any old values for this field.
     * @param name   The name of the field.
     * @param value  The value to set to the field.
     * @deprecated
     * @see put(name, value)
     */
    public final void add(String name, String value) {

        // uncommented by Oskar

        put(name, value);

       // Careful, I've changed it so that all field names with '.' in them will
       // be stored as sets instead of strings.

        // No you won't, could you please try to think for two seconds about
        // what a class is trying to do before you fuck with it like that.
        //
        //       readField(name, value, '.');
    }

    /**
     * Add a subset. This will overwrite any old values for this field.
     * @param name   The name of the field.
     * @param value  The value to set to the field.
     * @deprecated
     * @see put(name, value)
     */
    public final void add(String name, FieldSet fs) {
        put(name, fs);
    }

    /**
     * Whether a field can be read as a string.
     * @param   name The fields name.
     * @return  Whether the data in the field can be returned as a string.
     */
    public final boolean isString(String name) {
        return fields.get(name) != null && fields.get(name) instanceof String;
    }

    /**
     * Whether a field can be read as a set.
     * @param   name The fields name.
     * @return  Whether the data in the field can be returned as a FieldSet.
     */
    public final boolean isSet(String name) {
        return fields.get(name) != null && fields.get(name) instanceof FieldSet;
    }


    /**
     * @return  Whether this fieldset is empty.
     */
    public final boolean isEmpty() {
        return fields.isEmpty();
    }

    /**
     * @return An enumeration of the field names in this FieldSet.
     */
    public final Enumeration keys() {
        return fields.keys();
    }

    /**
     * @return  the String and FieldSet objects stored as values
     */
    public final Enumeration elements() {
        return fields.elements();
    }

    /**
     * @return  The number of Fields in this FieldSet (not recursive)
     */
    public final int size() {
        return fields.size();
    }

    /**
     * @return  true, if there is an entry with that name
     */
    public boolean containsKey(String key) {
        return fields.containsKey(key);
    }

    /**
     * Resets the FieldSet (removes all entries).
     */
    public final void clear() {
        fields.clear();
    }

    /**
     * Writes the fields to a stream with a standard syntax, and the given
     * semantics, using the standard separators.
     *
     * @param w         The stream to write to.
     **/
    public final void writeFields(WriteOutputStream w) throws IOException {
        writeFields(w, "End", '\n', '=', '.');
    }

    public final void writeFields(WriteOutputStream w, String end) throws IOException {
        writeFields(w, end, '\n', '=', '.');
    }

    /**
     * Writes the fields to a stream with a standard syntax, and the given
     * semantics.
     * @param w         The stream to write to.
     * @param sep    The character used to delimit the end of a field name value
     *             pair.
     * @param equal  The character that delimits the field name from the field
     *             value.
     * @param subset The character used to delimit subsets in the field name.
     * @param terminate The string to write at the end to terminate the fieldset
     *                this must not include the character "equal" used to
     *                delimit the name from the value in pairs.
     */
    public final void writeFields(WriteOutputStream w, String terminate,
                                  char sep, char equal, char subset)
                                                            throws IOException {
        inWriteFields(w,"", sep, (char) 0, equal, subset, false);
        w.writeUTF(terminate,sep);
    }

    public final void writeFields(WriteOutputStream w, String terminate,
                                  char sep1, char sep2, char equal, char subset)
                                                            throws IOException {
        inWriteFields(w, "", sep1, sep2, equal, subset, true);
    }


    // internal version, adds the preceding string necessary for recursion
    private void inWriteFields(WriteOutputStream w, String pre,
                               char sep1, char sep2, char equal,
                               char subset, boolean usesep2) throws IOException {

        for (Enumeration e = keys() ; e.hasMoreElements() ;) {
            String name = (String) e.nextElement();
            if (isSet(name)) {
                getSet(name).inWriteFields(w, pre + name + subset, sep1, sep2,
                                           equal, subset, usesep2);
            } else if (isString(name)) {
                w.writeUTF(pre);
                w.writeUTF(name, equal);
                if (usesep2) {
                    w.writeUTF(get(name), sep1, sep2);
                } else {
                    w.writeUTF(get(name), sep1);
                }
            } else {
/*                Core.logger.log(this,"Could not interpret field as string " +
                                "when trying to send.",Logger.MINOR);*/
            }
        }
    }

    /**
     * Parses fields from a stream using the standard separators.
     *
     * @param in     The stream to read.
     * @return       The string encountered that lacks a field name/value
     *               delimiter and that therefore is assumed to terminate
     *               the fieldset.  (<code>null</code> if terminated by EOF.)
     * @exception    IOException  if something goes wrong.
     **/
    public final String parseFields(ReadInputStream in) throws IOException {
        return parseFields(in, '\n', '\r', '=', '.');
    }

    /**
     * Parses fields from a stream in a standard syntax with given
     * semantics. The easiest way to see the syntax is probably to
     * look at the output of writeFields() or look at the Freenet
     * protocol specs.
     *
     * @param in     The stream to read.
     * @param sep    The character used to delimit the end of a field name value
     *             pair.
     * @param equal  The character that delimits the field name from the field
     *             value.
     * @param subset The character used to delimit subsets in the field name.
     * @return  The string encountered that lacks a field name/value delimiter
     *          and that therefore is assumed to terminate the fieldset.
     *          (<code>null</code> if terminated by EOF.)
     * @exception  IOException  if something goes wrong.
     */
    public final String parseFields(ReadInputStream in, char sep,
                                    char equal, char subset) throws IOException {
        return privParse(in, sep, (char) 0, equal, subset, false,
                         voidF, voidF);
    }

    /**
     * Parses fields from a stream in a standard syntax with given
     * semantics. The easiest way to see the syntax is probably to
     * look at the output of writeFields() or look at the Freenet
     * protocol specs.
     *
     * @param in     The stream to read.
     * @param sep    The character used to delimit the end of a field name value
     *             pair.
     * @param ignore The character that should be ignored if it directly precedes
     *             the preceding seperator (used for \r)
     * @param equal  The character that delimits the field name from the field
     *             value
     * @param subset The character used to delimit subsets in the field name.
     * @return  The string encountered that lacks a field name/value delimiter
     *          and that therefore is assumed to terminate the fieldset.
     *          (<code>null</code> if terminated by EOF.)
     * @exception  IOException  if something goes wrong.
     */
    public final String parseFields(ReadInputStream in, char sep, char ignore,
                              char equal, char subset) throws IOException {
        return privParse(in,sep,ignore,equal,subset,true,voidF, voidF);
    }

    /**
     * Parses fields from a stream in a standard syntax with given
     * semantics. The easiest way to see the syntax is probably to
     * look at the output of writeFields() or look at the Freenet
     * protocol specs.
     *
     * @param in     The stream to read.
     * @param sep    The character used to delimit the end of a field name value
     *             pair.
     * @param ignore The character that should be ignored if it directly precedes
     *             the preceding seperator (used for \r)
     * @param equal  The character that delimits the field name from the field
     *             value
     * @param subset The character used to delimit subsets in the field name.
     * @param nameFilter  A filter for field name strings
     * @param valueFilter  A filter for field value strings
     * @return  The string encountered that lacks a field name/value delimiter
     *          and that therefore is assumed to terminate the fieldset.
     *          (<code>null</code> if terminated by EOF.)
     * @exception  IOException  if something goes wrong.
     */
    public final String parseFields(ReadInputStream in, char sep, char ignore,
                              char equal, char subset, Filter nameFilter,
                              Filter valueFilter)
        throws IOException {

        return privParse(in,sep,ignore,equal,subset,true,
                         nameFilter, valueFilter);
    }

    public final String parseFields(ReadInputStream in, char sep,
                              char equal, char subset, Filter nameFilter,
                              Filter valueFilter)
        throws IOException {

        return privParse(in,sep,(char) 0,equal,subset,false,
                                         nameFilter, valueFilter);
    }

    private String privParse(ReadInputStream in, char sep, char ignore,
                             char equal, char subset, boolean useignore,
                             Filter nameFilter, Filter valueFilter)
        throws IOException {

        // Now read field/data pairs
        String s, name, data;
        int n;
        long read = 0;
        while(true) {
            try {
                if (useignore)
                    s = in.readToEOF(sep,ignore);
                else
                    s = in.readToEOF(sep);
            }
            catch (EOFException e) {
                return null;
            }
            read += s.length();
            n = s.indexOf(equal);
            if (n >= 0) { // field
                name = s.substring(0,n);
                data = valueFilter.filter(s.substring(n + 1));
                readField(name,data,subset, nameFilter);
                if (read > MAX_PARSE_SIZE)
                    throw new IOException("Message too large");
            } else { // trailing
                return s;
            }
        }
    }

    private final byte[] getStringBytes(String c) {
        try {
            return c.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            //Should never happen
			logger.log(Level.SEVERE, "Exception thrown in getStringBytes(String c)", e);
            return null;
        }
    }


    /**
     * Hashes the FieldSet without ignoring any strings.
     * @see hashUpdate(Digest, String[])
     */
    public final void hashUpdate(Digest ctx) {
        hashUpdate(ctx, new String[0]);
    }

    /**
     * Hashes the FieldSet into the digest algorithm
     * provided.  Hash is done with the UTF bytes of the key
     * and value pairs, with the fields sorted alphabetically.
     * Recurs on sub field-sets when encountered.
     * Key/value and end-of-line separators are hashed in.
     * @param ctx The Digest object
     * @param ignore A list of strings to ignore. These are ignored
     *               only in this fieldset, not in any subsets.
     */
    public final void hashUpdate(Digest ctx, String[] ignore) {
        hashUpdate(ctx, ignore, new byte[0]);
    }

    private void hashUpdate(Digest ctx, String[] ignore, byte[] prefix) {
        String[] fieldNames = new String[size()];
        int i = 0;
        Enumeration keys = keys();
        keys:
        while (keys.hasMoreElements()) {
            String s = (String) keys.nextElement();
            for (int j=0; j<ignore.length; ++j)
                if (ignore[j].equals(s)) continue keys;
            fieldNames[i++] = s;
        }
        QuickSorter.quickSort(
            new ArraySorter(fieldNames, new Fields.StringComparator(), 0, i)
        );

        for (int j=0; j<i; j++) {
            byte[] f = getStringBytes(fieldNames[j]);
            Object data = fields.get(fieldNames[j]);
            if (data instanceof FieldSet) {
                byte[] newPrefix = new byte[prefix.length + f.length + 1];
                System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
                System.arraycopy(f, 0, newPrefix, prefix.length, f.length);
                newPrefix[newPrefix.length-1] = HASH_SUBSET;
                ((FieldSet) data).hashUpdate(ctx, new String[0], newPrefix);
            }
            else {
                ctx.update(prefix);
                ctx.update(f);
                ctx.update(HASH_EQUALS);
                ctx.update(getStringBytes((String) data));
                ctx.update(HASH_NEWLINE);
            }
        }
    }

    /**
     * @return A string representation of the FieldSet.
     */
    public String toString() {
        try {
            ByteArrayOutputStream sw = new ByteArrayOutputStream();
            WriteOutputStream pr = new  WriteOutputStream(sw);
            inWriteFields(pr,"",',',(char) 0,'=','.', false);
            pr.flush();
            return "{" + sw.toString("UTF8") + "}";
        } catch (IOException e) { // shouldn't ever happen
            return null;
        }
    }


    /**
     * Reads a field off a name and value
     */
    protected void readField(String name, String value, char sep, Filter f) {
        int dot = name.indexOf(sep);
        if (dot < 0) { // value
            fields.put(f.filter(name), value);
        } else { // subset
            String fname = f.filter(name.substring(0,dot));
            Object o = fields.get(fname);
            FieldSet fs;
            if (o == null || !(o instanceof FieldSet)) {
                fs = newFieldSet();
                fields.put(fname,fs);
            } else {
                fs = (FieldSet) o;
            }
            fs.readField(name.substring(dot+1),value,sep, f);
        }
    }

    protected FieldSet newFieldSet() {
	return new FieldSet(); // override in descendants
    }

}






