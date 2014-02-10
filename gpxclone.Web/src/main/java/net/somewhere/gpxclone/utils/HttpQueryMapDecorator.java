/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.utils;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;


public class HttpQueryMapDecorator extends GenericMapDecorator<String,String>
{
    public HttpQueryMapDecorator(Map<String,String> strMap2decorate)
    {
        super(strMap2decorate);
        if(!decoratedMap.isEmpty())
        {
            this.normalize();
        }
    }
    
    public void fill(List<NameValuePair> queryList)
    {
        for(NameValuePair nvPair: queryList)
        {
            this.put(nvPair.getName(), nvPair.getValue());
        }        
    }
        
    public void fill(String HttpQueryString)
    {
        List<NameValuePair> queryList=URLEncodedUtils.parse(HttpQueryString, Charset.defaultCharset());
        this.fill(queryList);
    }
    
    @Override
    public String put(String key, String value)
    {
        return super.put(key.toLowerCase(), value); 
    }
    
    @Override
    public void putAll(Map<? extends String, ? extends String> m)
    {               
        //Iterator<Map.Entry<? extends String,? extends String>> iterator = m.entrySet().iterator();
        //Iterator<? extends Map.Entry<? extends String, ? extends String>> iterator2;
        for (Map.Entry<? extends String, ? extends String> entry : m.entrySet())
        {
            this.put(entry.getKey(), entry.getValue()); 
        }                
    }
    
    @Override
    public String toString()
    {
        
        List<NameValuePair> nv_list = new ArrayList<NameValuePair>();
        
        for(Map.Entry<String,String> entry : this.entrySet())
        {
            nv_list.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
        }                
        
        return URLEncodedUtils.format(nv_list, Charset.defaultCharset());
    }
        
    private void normalize()
    {      
        for (String key : decoratedMap.keySet())
        {
            key=key.toLowerCase();
        }        
    }
}
