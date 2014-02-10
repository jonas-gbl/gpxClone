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
package org.tigris.noodle;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.*;
import java.net.*;

// Java Servlet Classes
import javax.servlet.*;
import javax.servlet.http.*;

import HTTPClient.*;

/**
 * ByteNoodleReader implements the NoodleReader interface, but reads data
 * only as bytes.  
 *
 * @author <a href="mailto:edk@collab.net">Ed Korthof</a>
 */

public class ByteNoodleReader implements NoodleReader
{
   /**
    * Buffer size.
    */
   private static final int BUFFER_SIZE = 4096;

   /**
    * The input stream
    */
   private InputStream input = null;

    /**
     * The next block of data to be filtered. A pointer to one of
     * the two buffers. Not accessible from outside ByteReader.
     */
    private byte[] lookaheadBlock = null;

    /**
     * The number of characters in the lookahead block.
     */
    private int lookaheadBlockSize = 0;

    /**
     * Whether the read from the proxy stream is complete.
     */
    private boolean readComplete = false;

    /**
     * Private buffer used to hold data which has been read.
     */
    private byte[] buffer1 = new byte[BUFFER_SIZE];

    /**
     * Private buffer used to hold data which has been read.
     */
    private byte[] buffer2 = new byte[BUFFER_SIZE];

    /**
     * The NoodleData with which we are associated
     */
    protected NoodleData data = null;

    /**
     * Constructor.
     */
    public ByteNoodleReader(NoodleData data)
        throws NoodleException
    {
        this.data = data;
        try
        {
            this.input = data.getProxyResponse().getInputStream();
        }
        catch (ModuleException me)
        {
            throw new NoodleException("unable to get response stream", me);
        }
        catch (IOException io)
        {
            throw new NoodleException("i/o error getting response stream", io);
        }
    }

    /**
     * Reads from the proxy resposne until EOF or until the given buffer
     * is full.  Impl moved from NoodleData.
     */
    public int fillBlock(byte[] byteBlock)
        throws NoodleException
    {
        int totalBytesRead = 0;
        int bytesReadThisTime = 0;
        try
        {
            while (bytesReadThisTime > -1 && totalBytesRead < byteBlock.length)
            {
                int start = totalBytesRead;
                int toRead = byteBlock.length - start;
                try
                {
                    bytesReadThisTime = input.read(byteBlock, start, toRead);
                }
                catch (EOFException eof)
                {
                    // we finished reading
                    bytesReadThisTime = -1;
                }
                if (bytesReadThisTime > -1)
                {
                    totalBytesRead += bytesReadThisTime;
                }
            }
            if (bytesReadThisTime == -1)
            {
                readComplete = true;
            }
        }
        catch (IOException io)
        {
            throw new NoodleException("error proxying data", io);
        }
        return totalBytesRead;
    }

    /**
     * Ask if there is another block to be read.
     */
    public boolean readComplete()
    {
        return readComplete;
    }

    /**
     * Ask for the next block.
     *
     * Swaps the old lookahead block for the new current block and
     * reads new data into the lookahead block until it's is full or
     * we reach EOF. Also handles block initialization.
     */
    public ResponseBlock doRead()
        throws NoodleException
    {
        byte [] byteBlock;
        int blockSize;
        if (lookaheadBlock == null)
        {   
            //No data has been read yet.  Read the first block and (if
            //there's more than 4K) the next block, for lookahead
            //purposes.
            byteBlock = buffer1;
            blockSize = fillBlock(byteBlock);
            if (!readComplete)
            {   
                lookaheadBlock = buffer2;
                lookaheadBlockSize = fillBlock(lookaheadBlock);
            }
        }
        else
        {   
            //We are reading the third or subsequent block. Swap the
            //'current' block and the 'lookahead' block, and read into the
            //next 'lookahead' block.
            byte[] newBlock = (lookaheadBlock == buffer1 ? buffer2 : buffer1);
            byteBlock = lookaheadBlock;
            blockSize = lookaheadBlockSize;
            lookaheadBlock = newBlock;
            lookaheadBlockSize = fillBlock(lookaheadBlock);
        }

        if (readComplete)
        {
            //We are done reading. If there is a lookahead buffer,
            //merge it in with the block to form a larger block.
            if (lookaheadBlock != null)
            {
                byte[] finalBlock = new byte[blockSize+lookaheadBlockSize];
                System.arraycopy(byteBlock, 0, finalBlock, 0, blockSize);
                System.arraycopy(lookaheadBlock, 0, finalBlock, blockSize,
                                 lookaheadBlockSize);
                byteBlock = finalBlock;
                lookaheadBlock = null;
                blockSize = byteBlock.length;
            }
        }
        return new ResponseBlock(byteBlock, 0, blockSize, null);
    }
}
