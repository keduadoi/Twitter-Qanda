package com.qanda.file;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.imgscalr.Scalr;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClient;

import com.google.gson.JsonObject;

public class UploadServlet extends HttpServlet {

//    private static final long serialVersionUID = 1L;
    private File fileUploadPath;
    private final String[] fileTypes = {"txt","dat","json"/*,"zip","gzip","tar"*/};

    @Override
    public void init(ServletConfig config) {
        //fileUploadPath = new File(config.getInitParameter("upload_path"));
    	
    }
        
    /**
        * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
        * 
        */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	fileUploadPath = new File(request.getServletContext().getRealPath("/")+"uploads");
        if (request.getParameter("getfile") != null 
                && !request.getParameter("getfile").isEmpty()) {
            File file = new File(fileUploadPath,
                    request.getParameter("getfile"));
            if (file.exists()) {
                int bytes = 0;
                ServletOutputStream op = response.getOutputStream();

                response.setContentType(getMimeType(file));
                response.setContentLength((int) file.length());
                response.setHeader( "Content-Disposition", "inline; filename=\"" + file.getName() + "\"" );

                byte[] bbuf = new byte[1024];
                DataInputStream in = new DataInputStream(new FileInputStream(file));

                while ((in != null) && ((bytes = in.read(bbuf)) != -1)) {
                    op.write(bbuf, 0, bytes);
                }

                in.close();
                op.flush();
                op.close();
            }
        } else if (request.getParameter("delfile") != null && !request.getParameter("delfile").isEmpty()) {
            File file = new File(fileUploadPath, request.getParameter("delfile"));
            if (file.exists()) {
                file.delete(); // TODO:check and report success
            } 
        } else if (request.getParameter("getthumb") != null && !request.getParameter("getthumb").isEmpty()) {
            File file = new File(fileUploadPath, request.getParameter("getthumb"));
                if (file.exists()) {
                    String mimetype = getMimeType(file);
                    if (mimetype.endsWith("png") || mimetype.endsWith("jpeg") || mimetype.endsWith("gif")) {
                        BufferedImage im = ImageIO.read(file);
                        if (im != null) {
                            BufferedImage thumb = Scalr.resize(im, 75); 
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            if (mimetype.endsWith("png")) {
                                ImageIO.write(thumb, "PNG" , os);
                                response.setContentType("image/png");
                            } else if (mimetype.endsWith("jpeg")) {
                                ImageIO.write(thumb, "jpg" , os);
                                response.setContentType("image/jpeg");
                            } else {
                                ImageIO.write(thumb, "GIF" , os);
                                response.setContentType("image/gif");
                            }
                            ServletOutputStream srvos = response.getOutputStream();
                            response.setContentLength(os.size());
                            response.setHeader( "Content-Disposition", "inline; filename=\"" + file.getName() + "\"" );
                            os.writeTo(srvos);
                            srvos.flush();
                            srvos.close();
                        }
                    }
            } // TODO: check and report success
        } else {
            PrintWriter writer = response.getWriter();
            writer.write("call POST with multipart form data");
        }
    }
    
    /**
        * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
        * 
        */
    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {   	
    	fileUploadPath = new File(request.getServletContext().getRealPath("/")+"uploads");
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new IllegalArgumentException("Request is not multipart, please 'multipart/form-data' enctype for your form.");
        }

        ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        JSONArray json = new JSONArray();
        try {
            List<FileItem> items = uploadHandler.parseRequest(request);
            for (FileItem item : items) {
                if (!item.isFormField() && Arrays.asList(fileTypes).contains(getSuffix(item.getName()))) {
                        File file = new File(fileUploadPath, item.getName());
                        item.write(file);
                        JSONObject jsono = new JSONObject();
                        jsono.put("name", item.getName());
                        jsono.put("size", item.getSize());
                        jsono.put("url", "upload?getfile=" + item.getName());
                        //jsono.put("thumbnail_url", "upload?getthumb=" + item.getName());
                        jsono.put("delete_url", "upload?delfile=" + item.getName());
                        jsono.put("delete_type", "GET");
                        json.put(jsono);
                        
                        //run python script
                        String pythonFile = request.getServletContext().getRealPath("/")+"store_tweet.py";
                        System.out.println(fileUploadPath.getAbsolutePath()+"/"+item.getName());
                        String[] arguments = {"python",pythonFile,"-C","temporary_tweets",fileUploadPath.getAbsolutePath()+"/"+item.getName()};
                        Thread storeThread = new Thread(new RunPythonScript(arguments));
                        storeThread.start();
                        storeThread.join();
//                        
//                        pythonFile = request.getServletContext().getRealPath("/")+"fetchtweet.py";
//                        arguments = new String[]{"python",pythonFile,"-C","temporary_tweets"};
//                        Thread fetchThread = new Thread(new RunPythonScript(arguments));
//                        fetchThread.start();
//                        fetchThread.join();
//                        
                        Cookie[] cookies = request.getCookies();
                        String dbName = "";
                        for(Cookie cookie : cookies){
                        	if(cookie.getName().equals("uploadDb")){
                        		dbName = cookie.getValue();
                        		break;
                        	}
                        }
                        Thread importThread = new Thread(new ImportDbThread("temporary_tweets", dbName));
                        importThread.start();
                        importThread.join();
                        
                }
            }
        } catch (FileUploadException e) {
                throw new RuntimeException(e);
        } catch (Exception e) {
                throw new RuntimeException(e);
        } finally {
            writer.write(json.toString());
            writer.close();
        }

    }

    private String getMimeType(File file) {
        String mimetype = "";
        if (file.exists()) {
//            URLConnection uc = new URL("file://" + file.getAbsolutePath()).openConnection();
//            String mimetype = uc.getContentType();
//            MimetypesFIleTypeMap gives PNG as application/octet-stream, but it seems so does URLConnection
//            have to make dirty workaround
            if (getSuffix(file.getName()).equalsIgnoreCase("png")) {
                mimetype = "image/png";
            } else {
                javax.activation.MimetypesFileTypeMap mtMap = new javax.activation.MimetypesFileTypeMap();
                mimetype  = mtMap.getContentType(file);
            }
        }
        System.out.println("mimetype: " + mimetype);
        return mimetype;
    }



    private String getSuffix(String filename) {
        String suffix = "";
        int pos = filename.lastIndexOf('.');
        if (pos > 0 && pos < filename.length() - 1) {
            suffix = filename.substring(pos + 1);
        }
        System.out.println("suffix: " + suffix);
        return suffix;
    }
    
    private class RunPythonScript implements Runnable{
    	private String[] arguments;
    	
    	public RunPythonScript(String[] arguments){
    		this.arguments = arguments;
    	}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String graphs = "";
			try {
				ProcessBuilder pb = new ProcessBuilder(arguments);
				Process p = pb.start();
				BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = buffer.readLine();
				while(line!=null){
					graphs = graphs+line+"\n";
					line = buffer.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(graphs+"\nEnd python script");
		}
    	
    }
    
    private class ImportDbThread implements Runnable{
    	
    	//private CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
    	private String tempDb;
    	private String dbName;
    	
    	public ImportDbThread(String tempDb, String dbName) {
			// TODO Auto-generated constructor stub
    		this.tempDb = tempDb;
    		this.dbName = dbName;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			CouchDbClient dbClient = new CouchDbClient(dbName, true, "http", "localhost", 5984, null, null);
			CouchDbClient tempDbClient = new CouchDbClient(tempDb, true, "http", "localhost", 5984, null, null);
			List<JsonObject>allTempTweets = tempDbClient.view("_all_docs").query(JsonObject.class);
			
			for(JsonObject tweet : allTempTweets){
				JsonObject tweetJson = tempDbClient.find(JsonObject.class, tweet.get("id").toString().replaceAll("\"", ""));
				System.out.println(tweetJson.toString());
				tweetJson.remove("_rev");
				try{
					dbClient.save(tweetJson);
				}catch(Exception e){
					continue;
				}
			}
			
			tempDbClient.context().deleteDB(tempDb,"delete database");
			
			System.out.println("New tweets added");
		}
    	
    }

}