/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author Jonas
 */
public class HttpQueryMap extends HashMap<String,String>
{    
    
    
    public HttpQueryMap(List<NameValuePair> queryList)
    {
        
        update(queryList);
    }
    
    public HttpQueryMap(String HttpQueryString)
    {
        List<NameValuePair> queryList=URLEncodedUtils.parse(HttpQueryString, Charset.defaultCharset());
        this.update(queryList);
    }
    
    public void updateHttpQueryMap(String HttpQueryString)
    {
        List<NameValuePair> queryList=URLEncodedUtils.parse(HttpQueryString, Charset.defaultCharset());
        this.update(queryList);
    }
    
    public void updateHttpQueryMap(List<NameValuePair> queryList)
    {
        this.update(queryList);
    }

    @Override
    public String get(Object key) {
        return super.get(key); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(((String)key).toLowerCase()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String put(String key, String value) {
        return super.put(key.toLowerCase(), value.toLowerCase()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m)
    {               
        //Iterator<Map.Entry<? extends String,? extends String>> iterator = m.entrySet().iterator();
        //Iterator<? extends Map.Entry<? extends String, ? extends String>> iterator2;
        for (Map.Entry<? extends String, ? extends String> entry : m.entrySet())
        {
            put(entry.getKey(), entry.getValue()); 
        }                
    }

    

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(((String)value).toLowerCase()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString()
    {
        
        List<NameValuePair> nv_list = new ArrayList<NameValuePair>();
        
        Iterator<Map.Entry<String,String>> iterator = this.entrySet().iterator();
        Map.Entry<String,String> currentEntry;
        while(iterator.hasNext())
        {
            currentEntry  =  iterator.next();
            nv_list.add(new BasicNameValuePair(currentEntry.getKey(),currentEntry.getValue()));
            
        }
        
        return URLEncodedUtils.format(nv_list, Charset.defaultCharset());
    }
    
    private void update(List<NameValuePair> queryList)
    {
        Iterator<NameValuePair> queryListIterator = queryList.iterator();
            NameValuePair currentPair;
            while(queryListIterator.hasNext())
            {
                currentPair=queryListIterator.next();
                this.put(currentPair.getName().toLowerCase(), currentPair.getValue().toLowerCase());
            }
    }
    
    
    
    
}
