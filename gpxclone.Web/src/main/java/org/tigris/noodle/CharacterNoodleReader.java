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
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;

import java.util.*;
import java.net.*;

// Java Servlet Classes
import javax.servlet.*;
import javax.servlet.http.*;

import HTTPClient.*;

/**
 * CharacterNoodleReader implements the NoodleReader interface; it makes
 * an effort to read in the response using characters rather than bytes.
 * In determining the charset to use, it will give preference to the
 * HTTP Content-Type header; but it will also look for a META tag
 * if the Content-Type header doesn't specify the charset.
 *
 * Normally this is only called for text/html files -- for other kinds
 * of files we don't want to do this kind of operation, as we'd rather
 * just pass the bytes along unchanged: character encoding transformations
 * always have the potential to lose data.
 *
 * @author <a href="mailto:edk@collab.net">Ed Korthof</a>
 */

public class CharacterNoodleReader implements NoodleReader
{
   /**
    * Buffer size.
    */
   private static final int BUFFER_SIZE = 4096;

   /**
    * This is the charset which we are using to read data.
    */
   private String inputCharset = null;

   /**
    * The string used to search through the HTTP content type for the
    * charset
    */
   private static final String CONTENT_TYPE_CHARSET_MARKER = "charset=";

   /**
    * The input stream
    */
   private Reader input = null;

    /**
     * This indicates that the read from the proxy stream is complete
     * and <code>doRead</code> will not return any more blocks of
     * data.  It may be false even after reading is completed if there
     * is a stored block of data ready to be returned.
     */
    private boolean readComplete = false;

    /**
     * The next block of data to be filtered.
     */
    private char[] currentBlock = null;

    /**
     * The number of characters currently in the data block.
     */
    private int currentBlockSize = 0;

    /**
     * The block of data to be filtered after currentBlock is
     * filtered.
     */
    private char[] lookaheadBlock = null;

    /**
     * The number of characters currently in the lookahead block.
     */
    private int lookaheadBlockSize = 0;

    /**
     * Sometimes we will need to search for character boundaries when we
     * don't know the encoding of the byte stream.  As a simplification, we
     * assume that a sequence of a subset of ASCII is not so likely to include
     * the last byte of a multibyte character -- at least when we test for
     * isWhiteSpace + isLetterOrDigit + a few other chararacters.  Of course,
     * this isn't perfect, but in many cases it will work.  The following
     * characters are the additional characters that we test for ... the
     * intent of this is stop reading the first time we hit an HTML tag.
     */
    protected String ASCII_OK_CHARACTERS = "<>/\\='\"";

    /**
     * This is the number of consecutive simple ASCII bytes which are required
     * for the assumption that the current byte isn't in the middle of a 
     * character.  (Clearly one isn't enough, as many encodings allow for the
     * second byte of a multibyte character to be any printable ASCII value.)
     */
    protected int requiredConsecutiveASCIIBytes = 3;

    /**
     * The NoodleData with which we are associated
     */
    protected NoodleData data = null;

    /**
     * This overrides the charset which we'll use for output.  We use it
     * when we need to override because we weren't able to guess the original
     * encoding.
     */
    protected String outputCharsetOverride = null;

    /**
     * Constructor.
     */
    public CharacterNoodleReader(NoodleData data)
        throws NoodleException
    {
        this.data = data;
        this.input = getReader();
    }

    public String getCharset()
    {
        return inputCharset;
    }

    /**
     * Obtains an InputStreamReader. The ISR needs a charset, so this
     * method also reads a little data as bytes and tries to figure
     * out from a META tag or consecutive ASCII bytes which charset
     * should be used. Once a charset is decided on, the data read to
     * determine the charset is stored as chars in currentBlock, so that
     * it can be appended to by a call to doRead().  
     */
    private Reader getReader()
        throws NoodleException
    {
        inputCharset = null;
        InputStreamReader reader = null;
        byte[] initialData = null;
        try
        {
            InputStream rawInput = data.getProxyResponse().getInputStream();
            // first, guess based on the content-type header

            String type = data.getProxyResponse().
                getHeader(NoodleConstants.CONTENT_TYPE_HEADER_NAME);
            // TODO: more sophisticated parsing of the header?
            int offset = type.indexOf(CONTENT_TYPE_CHARSET_MARKER);
            if (offset >= 0)
            {
                inputCharset = type.substring(offset +
                                              CONTENT_TYPE_CHARSET_MARKER.
                                              length());
            }

            // if we still need to look, go through the painful part
            if (inputCharset == null || inputCharset.equals(""))
            {
                // we'd use a BufferedInputStream + mark & reset, but that
                // doesn't seem to be working.  so instead, we have some 
                // logic where we try to avoid splitting characters; in the
                // worst case, this may mean we don't stream data.
                initialData = getHeadData(rawInput);
                inputCharset =
                    findCharsetViaMeta(initialData, initialData.length);
            }
            //System.err.println("finished looking for inputCharset, have: " +
            //                   inputCharset);
            // now if we have a charset, try to create a character stream
            if (inputCharset != null && !inputCharset.equals(""))
            {
                try
                {
                    reader = new InputStreamReader(rawInput, inputCharset);
                }
                catch (IOException io)
                {
                    System.err.println("io exception while trying to get an " +
                                       "input stream w/ charset '" +
                                       inputCharset + "': " + io.getMessage());
                    io.printStackTrace();
                }
            }
            if (reader == null)
            {
                // we unable to determine the correct charset.  try the
                // default charset.
                try
                {
                    inputCharset = data.getDefaultCharset();
                    System.err.println("using the default charset: " +
                                       inputCharset);
                    reader = new InputStreamReader(rawInput, inputCharset);
                }
                catch (IOException io)
                {
                    System.err.println("unable to use the default charset '" +
                                       inputCharset +
                                       "' to get an input stream: " +
                                       io.getMessage());
                }
            }
            if (reader == null)
            {
                // this is our last fallback.
                reader = new InputStreamReader(rawInput);
                inputCharset = reader.getEncoding();
                // avoid using the standard output charset
                outputCharsetOverride = inputCharset;
            }
        }
        catch (ModuleException me)
        {
            throw new NoodleException("unable to get response stream", me);
        }
        catch (IOException io)
        {
            throw new NoodleException("i/o error getting response stream", io);
        }
        // if we read some initial data, try to turn it into characters using
        // the encoding which we have
        if (initialData != null)
        {
            try
            {
                //TODO: It would be good to make this translation without
                //so many intermediate copies.
                String initialDataStr = new String(initialData, inputCharset);
                char[] initialDataChr = initialDataStr.toCharArray();
                currentBlockSize = initialDataChr.length;
                if (currentBlockSize < BUFFER_SIZE)
                {
                    currentBlock = new char[BUFFER_SIZE];
                    System.arraycopy(initialDataChr, 0, currentBlock, 0, 
                                     currentBlockSize);
                }
                else
                {
                    currentBlock = initialDataChr;
                }
            }
            catch (java.io.UnsupportedEncodingException uee)
            {
                throw new NoodleException("invalid charset: " + inputCharset,
                                          uee);
            }
        }
        return reader;
    }

    /**
     * This is kind of a hack, but it seems necessary due to the fact that
     * we can't reset back to a mark with the input stream which we have.
     */
    private byte[] getHeadData(InputStream input)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
        // read looking for either </head, <body before BUFFER_SIZE --
        // or for any sequence of two consecutive ASCII bytes after
        // BUFFER_SIZE.  We look for one or the other of these in the hopes
        // of avoiding a situation where we split a character.
        byte endHeadArray[] = "</head".getBytes();
        byte bodyArray[] = "<body".getBytes();

        // we're looking for them in a byte stream, 
        int endHeadState = 0;
        int bodyState = 0;

        // if we can't find </head or <body before BUFFER_SIZE, we fall back
        // to looking for consecutive printable ASCII bytes
        int count = 0;
        int consecutiveASCIIBytes = 0;

        // boolean to mark the end of the loop
        boolean finished = false;
        while (!finished)
        {
            byte next = (byte) input.read();
            if (next == -1)
            {
                finished = true;
                break;
            }
            // state checks for </head>, and <body -- but ignore case
            byte test = (byte)(Character.toLowerCase((char)next));
            if (test == endHeadArray[endHeadState])
            {
                ++endHeadState;
                if (endHeadState >= endHeadArray.length)
                {
                    finished = true;
                }
            }
            else
            {
                endHeadState = 0;
            }

            if (test == bodyArray[bodyState])
            {
                ++bodyState;
                if (bodyState >= bodyArray.length)
                {
                    finished = true;
                }
            }
            else
            {
                bodyState = 0;
            }

            // TODO : make this more efficient
            if (count > BUFFER_SIZE &&
                     (Character.isWhitespace((char) next) ||
                      Character.isLetterOrDigit((char)next) ||
                      ASCII_OK_CHARACTERS.indexOf(Byte.toString(next)) >= 0))
            {
                ++consecutiveASCIIBytes;
                if (consecutiveASCIIBytes > requiredConsecutiveASCIIBytes)
                {
                    finished = true;
                }
            }
            else
            {
                consecutiveASCIIBytes = 0;
            }

            // write the original byte to our byte array
            baos.write(next);
            ++count;
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    /**
     * This and the following method would be better done through a library
     * which properly parses for HTML tags, and then parses the tags into
     * data structures.  But we don't have a complete HTML file -- this is
     * generally just a fragment, and many tags won't be well-formed.
     */
    private String findCharsetViaMeta(byte[] buffer, int totalRead)
    {
        String charset = null;

        for (int i = 0 ; i < (totalRead - 5); ++i)
        {
            if ((buffer[i] == '<') &&
                (buffer[i + 1] == 'm' || buffer[i + 1] == 'M') &&
                (buffer[i + 2] == 'e' || buffer[i + 2] == 'E') &&
                (buffer[i + 3] == 't' || buffer[i + 3] == 'T') &&
                (buffer[i + 4] == 'a' || buffer[i + 4] == 'A') &&
                (buffer[i + 5] == ' '))
            {
                int j = 6;
                for (; (i + j + 1) < totalRead && buffer[i + j] != '>';)
                {
                    ++j;
                }
                String meta = new String(buffer, i+1, j).toLowerCase();
                charset = getCharsetFromMeta(meta);
                if (charset != null)
                {
                    break;
                }
            }
        }
        System.err.println("finished looking for meta tags: " + charset);
        return charset;
    }

    private String getCharsetFromMeta(String meta)
    {
        System.err.println("got meta: " + meta);
        String charset = null;
        if (meta.indexOf(NoodleConstants.CONTENT_TYPE_HEADER_NAME.
                         toLowerCase()) > 0)
        {
            System.err.println("got content type header: "+ meta);
            int offset =
                meta.indexOf(NoodleConstants.CONTENT_TYPE_CHARSET.
                             toLowerCase());
            if (offset > 0)
            {
                System.err.println("got charset in it");
                offset += NoodleConstants.CONTENT_TYPE_CHARSET.length();
                int endOffset = offset + 1;
                char c = meta.charAt(endOffset);
                while (c != '\'' && c != ' ' && c != '\\' && c != '/' &&
                       c != '>')
                {
                    c = meta.charAt(++endOffset);
                }
                charset = meta.substring(offset, endOffset - 1);
            }
        }
        System.err.println("returning charset: " + charset);
        return charset;
    }

    /**
     * Reads from the proxy resposne into either currentBlock or
     * lookaheadBlock until EOF or until the given buffer is full.
     * Impl moved from NoodleData.  
     */
    private int fillBlock(char[] inputBlock)
        throws NoodleException
    {  
        int totalCharsRead = inputBlock.length;
        if (inputBlock == currentBlock)
        {
            totalCharsRead = currentBlockSize;
        }
        else if (inputBlock == lookaheadBlock)
        {
            totalCharsRead = lookaheadBlockSize;
        }

        int charsReadThisTime = 0;
        try
        {
            while (charsReadThisTime > -1 
                   && totalCharsRead < BUFFER_SIZE)
            {
                int start = totalCharsRead;
                int toRead = BUFFER_SIZE - start;
                try
                {
                    charsReadThisTime = input.read(inputBlock, start, toRead);
                }
                catch (EOFException eof)
                {
                    // we finished reading
                    charsReadThisTime = -1;
                }
                if (charsReadThisTime > -1)
                {
                    totalCharsRead += charsReadThisTime;
                }
            }
            if (charsReadThisTime == -1)
            {
                readComplete = true;
            }
        }
        catch (EOFException eof)
        {
            // we're done -- noop
        }
        catch (IOException io)
        {
            io.printStackTrace();
            throw new NoodleException("error proxying data: " +
                                      io.getMessage() , io);
        }
        return totalCharsRead;
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
        if (lookaheadBlock == null)
        {
            //No data (or less than a full block of data) has been
            //read yet.  Read the first block and (if there's more
            //than 4K) the next block, for lookahead purposes.
            if (currentBlock == null)
            {
                //This shouldn't happen, because currentBlock is
                //expected to be initialized by getReader().
                currentBlock = new char[BUFFER_SIZE];
            }
            currentBlockSize = fillBlock(currentBlock);
            if (!readComplete)
            {   
                lookaheadBlock = new char[BUFFER_SIZE];
                lookaheadBlockSize = fillBlock(lookaheadBlock);
            }
        }
        else
        {   
            //We are reading the third or subsequent block. Swap the
            //'current' block and the 'lookahead' block, and read into the
            //next 'lookahead' block.
            char[] newBlock = currentBlock;
            currentBlock = lookaheadBlock;
            currentBlockSize = lookaheadBlockSize;
            lookaheadBlock = newBlock;
            lookaheadBlockSize = 0;
            lookaheadBlockSize = fillBlock(lookaheadBlock);
        }

        if (readComplete)
        {
            //We are done reading. If there is a lookahead buffer,
            //merge it in with the block to form a larger block.
            if (lookaheadBlock != null)
            {
                char[] finalBlock = new char[currentBlockSize + 
                                            lookaheadBlockSize];
                System.arraycopy(currentBlock, 0, finalBlock, 0, 
                                 currentBlockSize);
                System.arraycopy(lookaheadBlock, 0, finalBlock, 
                                 currentBlockSize, lookaheadBlockSize);
                currentBlock = finalBlock;
                currentBlockSize = currentBlock.length;
                lookaheadBlock = null;
                lookaheadBlockSize = 0;
            }
        }
        String outputCharset = outputCharsetOverride;
        if (outputCharset == null)
        {
            outputCharset = data.getOutputCharset();
        }
        byte[] response;
        try
        {
            response = new String(currentBlock, 0, currentBlockSize)
                .getBytes(outputCharset);
        }
        catch (IOException io)
        {
            throw new NoodleException("invalid outputCharset: " +
                                      outputCharset, io);
        }
        return new ResponseBlock(response, 0, response.length, outputCharset);
    }
}
