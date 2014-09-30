package Chapter4.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class TweetFileProcessor implements Iterator<JSONObject>{
	
	protected BufferedReader fileBuffer;
	protected boolean endOfFile;
	protected String nextLine;
	
	public TweetFileProcessor(File f){
		
		endOfFile = false;
		
		InputStreamReader isr;
		BufferedReader br = null;
		try {
			isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
			br = new BufferedReader(isr);
			nextLine = br.readLine();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			endOfFile = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			endOfFile = true;
		} catch (IOException e) {
			e.printStackTrace();
			endOfFile = true;
		}
		finally{
			fileBuffer = br;
		}
	}

	@Override
	public boolean hasNext() {
		return !endOfFile;
	}

	@Override
	public JSONObject next() {
		JSONObject obj = null;
                try {
                    obj = new JSONObject(nextLine);
                } catch (JSONException ex) {
                    Logger.getLogger(TweetFileProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
		try {
			nextLine = fileBuffer.readLine();
			if(nextLine == null){
				endOfFile = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return obj;
	}

	@Override
	public void remove() throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}
}
