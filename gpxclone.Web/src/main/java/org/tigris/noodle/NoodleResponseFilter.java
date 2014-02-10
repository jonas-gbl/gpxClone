/* ================================================================
 * Copyright (c) 2002-2003 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet (http://www.collab.net/)."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" name
 * nor may "Tigris" appear in their names without prior written
 * permission of CollabNet.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLABNET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */
package org.tigris.noodle;

import org.tigris.noodle.NoodleFilter;
import org.tigris.noodle.NoodleData;

/**
 * Those who want to Noodle can Noodle with NoodleFilters.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:leonardr@collab.net">Leonard Richardson</a>
 */
public interface NoodleResponseFilter extends NoodleFilter
{
    /**
     * Filter status indicating that this filter should be called
     * on the next block.
     */
    static final int MAINTAIN_THIS_FILTER = 0;

    /**
     * Filter status indicating that this filter is done processing
     * and need no longer be called.
     */
    static final int KILL_THIS_FILTER = 1;

    /**
     * Filter status indicating that this filter has determined that
     * no more filtering needs to be done--there was a redirect or
     * something.
     */
    static final int KILL_ALL_FILTERS = 2;

    /**
     * Hook called for a filter on every block of proxied data until
     * there is no block of proxied data, or until this filter returns
     * a filter status code other than MANTAIN_THIS_FILTER.
     *
     * @return a status code; one of the four *FILTER* constants in
     * this class.  
     *
     * @param noodleData Contains miscellaneous storage and
     *                   information about the request; you can also use
     *                   this to mantain filter state.  
     *
     * @param responseBlock The current response block to be filtered.
     */
    int filter(NoodleData noodleData, ResponseBlock block) throws Exception;
}
