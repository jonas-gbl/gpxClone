
/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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
 * software developed by CollabNet <http://www.collab.net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of CollabNet.
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

/**
 * ResponseBlock is a class which represents one block of data as sent
 * from the client.  When the data can be handled as characters, the encoding
 * should be specified as well.
 *
 * @author <a href="mailto:edk@collab.net">Ed Korthof</a>
 */
package org.tigris.noodle;

public class ResponseBlock
{
    /**
     * The data in this block
     */
    protected byte[] block;

    /**
     * The offset from 0 where the response starts -- inclusive.
     */
    protected int offset;

    /**
     * The last of data in this block which should be streamed -- exclusive.
     */
    protected int endOffset;

    /**
     * The encoding of the bytes in this block
     */
    protected String encoding;

    /**
     * Constructor.  
     */
    public ResponseBlock(byte [] block, int offset, int endOffset,
                         String encoding)
        throws NoodleException
    {
        this.block = block;
        this.offset = offset;
        this.endOffset = endOffset;
        this.encoding = encoding;
        verifyBounds();
    }

    /**
     * Verify that the bounds are reasonable.
     */
    public void verifyBounds() throws NoodleException
    {
        // offset can't be larger than the block length (offset is inclusive
        // as the start location; offset == block.length means no more data
        // should be streamed)
        //
        // endOffset can't be larger than the block length
        //
        // offset can't be larger than endOffset
        //
        if (offset > block.length || endOffset > block.length ||
            endOffset < offset)
        {
            throw new NoodleException("invalid offset / endOffset / "+
                                      "block.length: " + offset + " / " +
                                      endOffset + " / " + block.length);
        }
    }

    public void setOffset(int newOffset)
        throws NoodleException
    {
        changeData(block, newOffset, endOffset, encoding);
    }

    public void setBlock(byte[] newBlock)
        throws NoodleException
    {
        changeData(newBlock, offset, endOffset, encoding);
    }

    public void setEndOffset(int newEndOffset)
        throws NoodleException
    {
        changeData(block, offset, newEndOffset, encoding);
    }

    public void setOffset(String newEncoding)
        throws NoodleException
    {
        changeData(block, offset, endOffset, newEncoding);
    }

    public void changeData(byte[] block, int offset, int endOffset,
                           String encoding)
        throws NoodleException
    {
        if (block != null)
            this.block = block;
        if (offset != -1)
            this.offset = offset;
        if (endOffset != -1)
            this.endOffset = endOffset;
        if (encoding != null)
            this.encoding = encoding;
        verifyBounds();
    }

    /**
     * retrieve the block of data
     */
    public byte[] getBlock()
    {
        return block;
    }

    /**
     * retrieve the offset where the current data starts
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * retrieve the endOffset of the current block of data
     */
    public int getEndOffset()
    {
        return endOffset;
    }
    
    /**
     * return the encoding of the data, if the bytes represent a set of
     * characters.  null will be returned when the bytes should not be
     * handled as characters.
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Return the amount of data left to stream from this block
     */
    public int getRemainingData()
    {
        return endOffset - offset;
    }
}
