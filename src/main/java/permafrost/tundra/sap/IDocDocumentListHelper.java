/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lachlan Dowding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package permafrost.tundra.sap;

import com.sap.conn.idoc.IDocConversionException;
import com.sap.conn.idoc.IDocDocumentIterator;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocIllegalTypeException;
import com.sap.conn.idoc.IDocSyntaxException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience methods for working with IDocDocumentList objects.
 */
public class IDocDocumentListHelper {
    /**
     * Disallow instantiation of this class.
     */
    private IDocDocumentListHelper() {}

    /**
     * For each IDoc in the give IDocDocumentList, assigns the list index as the respective DOCNUM.
     *
     * @param list                      The list whose items are to be processed.
     * @return                          The given list with each item assigned a DOCNUM equal to its list index.
     * @throws IDocConversionException  If an error occurs.
     * @throws IDocSyntaxException      If an error occurs.
     */
    public static IDocDocumentList identify(IDocDocumentList list) throws IDocConversionException, IDocSyntaxException {
        if (list != null) {
            int index = 0;
            IDocDocumentIterator iterator = list.iterator();
            while (iterator.hasNext()) {
                iterator.next().setIDocNumber(Integer.toString(index++));
            }
        }

        return list;
    }

    /**
     * Partitions the given IDocDocumentList into an IDocDocumentList[] whose lengths are at most the given limit, and
     * whose items are the respective items from the given IDocDocumentList.
     *
     * @param list                      The list to be partitioned.
     * @param limit                     The maximum length of each IDocDocumentList in the returned IDocDocumentList[].
     * @return                          An IDocDocumentList[] containing the items from the given IDocDocumentList,
     *                                  partitioned by the given limit.
     * @throws IDocConversionException  If an error occurs.
     * @throws IDocSyntaxException      If an error occurs.
     * @throws IDocIllegalTypeException If an error occurs.
     */
    public static IDocDocumentList[] partition(IDocDocumentList list, int limit) throws IDocConversionException, IDocSyntaxException, IDocIllegalTypeException {
        if (list == null) return null;
        if (list.size() == 0) return new IDocDocumentList[0];
        if (limit <= 0) return new IDocDocumentList[] { list };

        List<IDocDocumentList> output = new ArrayList<IDocDocumentList>((list.size() / limit) + 1);

        IDocDocumentList documentList = construct(list);
        documentList.ensureCapacity(limit);
        int index = 0, remaining = list.size();

        IDocDocumentIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            documentList.add(iterator.next());
            remaining--;
            index++;

            if (index >= limit) {
                output.add(documentList);
                documentList = construct(list);
                documentList.ensureCapacity(remaining < limit ? remaining : limit);
                index = 0;
            }
        }

        if (documentList.size() > 0) {
            output.add(documentList);
        }

        return output.toArray(new IDocDocumentList[0]);
    }

    /**
     * Creates a new IDocDocumentList using the given IDocDocumentList's class's no argument constructor.
     *
     * @param other An IDocDocumentList whose class's no argument constructor is to be used to create a new object.
     * @return      A new IDocDocumentList created from calling the given IDocDocumentList's class's no argument
     *              constructor.
     */
    private static IDocDocumentList construct(IDocDocumentList other) {
        try {
            return construct(other.getClass().getConstructor());
        } catch(NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a new IDocDocumentList using the given no argument constructor.
     *
     * @param constructor   The constructor to used.
     * @return              A new IDocDocumentList created from invoking the given constructor.
     */
    private static IDocDocumentList construct(Constructor<? extends IDocDocumentList> constructor) {
        try {
            return constructor.newInstance();
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch(InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch(InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }
}


