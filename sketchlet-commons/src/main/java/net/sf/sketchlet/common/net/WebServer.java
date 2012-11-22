/*
 * WebServer.java
 *
 * Created on March 17, 2006, 3:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.StringTokenizer;

//file: server.java
public class WebServer extends Thread {
    private CommunicatorTcpInterface communicator;
    private static final String SKETCHLET_COMMAND_PREFIX = "amico?command=";
    private static final String SKETCHLET_WRITE_COMMAND_PREFIX = "amico_write";
    
    MimeTypes mimeTypes = new MimeTypes();
    
    public WebServer(int listen_port, String rootDirectory) {
        message_to = null;
        port = listen_port;
        
        this.rootDirectory = rootDirectory;
        if (!this.rootDirectory.endsWith("/")) {
            this.rootDirectory = this.rootDirectory + "/";
        }
        
        this.start();
    }
    
    public WebServer(int listen_port, WebServerStarter to_send_message_to, String rootDirectory) {
        message_to = to_send_message_to;
        port = listen_port;
        this.rootDirectory = rootDirectory;
        
        if (!this.rootDirectory.endsWith("/")) {
            this.rootDirectory = this.rootDirectory + "/";
        }
        
        this.start();
    }
    
    public void connectToCommunicator( String host, int port ) {
        this.communicator = new CommunicatorTcpInterface( host, port );
    }
    
    private void s(String s2) { //an alias to avoid typing so much!
        if (message_to != null)
            message_to.send_message_to_window(s2);
        else
            System.out.print(s2);
    }
    
    private WebServerStarter message_to; //the starter class, needed for gui
    private int port; //port we are going to listen to
    private String rootDirectory; //port we are going to listen to
    
    // this is a overridden method from the Thread class we extended from
    public void run() {
        ServerSocket serversocket = null;
        
        try {
            s("Trying to bind to localhost on port " + Integer.toString(port) + "...");
            serversocket = new ServerSocket(port);
        } catch (Exception e) { //catch any errors and print errors to gui
            s("\nFatal Error:" + e.getMessage());
            return;
        }
        s("OK!\n");
        s("Root directory is \"" + this.rootDirectory + "\"\n");
        //go in a infinite loop, wait for connections, process request, send response
        s("\nReady, Waiting for requests...\n");
        while (true) {
            try {
                Socket connectionsocket = serversocket.accept();
                InetAddress client = connectionsocket.getInetAddress();
                // s(client.getHostName() + " connected to server.\n");
                
                BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
                
                DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
                
                // as the name suggest this method handles the http request, see further down.
                // abstraction rules
                http_handler(input, output);
            } catch (Exception e) { //catch any errors, and print them
                s("\nError:" + e.getMessage() + "\n");
            }
            
        } //go back in loop, wait for next request
    }
    
    //our implementation of the hypertext transfer protocol
    //its very basic and stripped down
    private void http_handler(BufferedReader input, DataOutputStream output) {
        int method = 0; //1 get, 2 head, 0 not supported
        String http = new String(); //a bunch of strings to hold
        String path = new String(); //the various things, what http v, what path,
        String file = new String(); //what file
        String user_agent = new String(); //what user_agent
        
        try {
            //This is the two types of request we can handle
            //GET /index.html HTTP/1.0
            //HEAD /index.html HTTP/1.0
            String tmp = input.readLine(); //read from the stream
            String tmp2 = new String(tmp);
            tmp.toUpperCase(); //convert it to uppercase
            if (tmp.startsWith("GET")) { //compare it is it GET
                method = 1;
            } //if we set it to method 1
            if (tmp.startsWith("HEAD")) { //same here is it HEAD
                method = 2;
            } //set method to 2
            
            if (method == 0) { // not supported
                try {
                    output.writeBytes(construct_http_header(501));
                    output.close();
                    return;
                } catch (Exception e3) { //if some error happened catch it
                    s("error:" + e3.getMessage() + "\n");
                } //and display error
            }
            //}
            
            //tmp contains "GET /index.html HTTP/1.0 ......."
            //find first space
            //find next space
            //copy whats between minus slash, then you get "index.html"
            //it's a bit of dirty code, but bear with me...
            int start = 0;
            int end = 0;
            for (int a = 0; a < tmp2.length(); a++) {
                if (tmp2.charAt(a) == ' ' && start != 0) {
                    end = a;
                    break;
                }
                if (tmp2.charAt(a) == ' ' && start == 0) {
                    start = a;
                }
            }
            path = tmp2.substring(start + 2, end); //fill in the path
            
            System.out.println( "PATH: " + path );
            
            if (path.startsWith(WebServer.SKETCHLET_COMMAND_PREFIX)) {
                this.processAmicoCommand( path.substring( WebServer.SKETCHLET_COMMAND_PREFIX.length()), method, output );
            } else if (path.startsWith(WebServer.SKETCHLET_WRITE_COMMAND_PREFIX)) {
                this.processAmicoUpdateCommand( path.substring( WebServer.SKETCHLET_WRITE_COMMAND_PREFIX.length()) );
            } else {
                path = rootDirectory + path;
                this.processFileRequest( path, method, output );
            }
            
            //clean up the files, close open handles
            output.close();
        } catch (Exception e) {
            s("errorr" + e.getMessage() + "\n");
        } //catch any exception
    }
    
    private void processFileRequest( String path, int method, DataOutputStream output ) {
        //path do now have the filename to what to the file it wants to open
        s("\nClient requested:" + new File(path).getAbsolutePath() + "\n");
        FileInputStream requestedfile = null;
        
        try {
            //NOTE that there are several security consideration when passing
            //the untrusted string "path" to FileInputStream.
            //You can access all files the current user has read access to!!!
            //current user is the user running the javaprogram.
            //you can do this by passing "../" in the url or specify absoulute path
            //or change drive (win)
            
            //try to open the file,
            requestedfile = new FileInputStream(path);
        } catch (Exception e) {
            try {
                //if you could not open the file send a 404
                output.writeBytes(construct_http_header(404));
                //close the stream
                output.close();
            } catch (Exception e2) {}
            s("error" + e.getMessage() + "\n");
        } //print error to gui
        
        //happy day scenario
        try {
            int type_is = 0;
            //find out what the filename ends with,
            //so you can construct a the right content type
            
            String extension = "html";
            int n = path.lastIndexOf( "." );
            
            if (n > 0 && n < path.length() - 1) {
                extension = path.substring( n + 1 );
            }
            
            output.writeBytes(construct_http_header(200, this.mimeTypes.getMimeType( extension )));
            
            //if it was a HEAD request, we don't print any BODY
            if (method == 1) { //1 is GET 2 is head and skips the body
                while (true) {
                    //read the file from filestream, and print out through the
                    //client-outputstream on a byte per byte base.
                    int b = requestedfile.read();
                    if (b == -1) {
                        break; //end of file
                    }
                    output.write(b);
                }
                
            }
            //clean up the files, close open handles
            requestedfile.close();
        }
        
        catch (Exception e) {}
        
    }
    
    //this method makes the HTTP header for the response
    //the headers job is to tell the browser the result of the request
    //among if it was successful or not.
    private String construct_http_header( int return_code ) {
        return this.construct_http_header( return_code, "text/html" );
    }
    private String construct_http_header(int return_code, String file_type) {
        String s = "HTTP/1.0 ";
        //you probably have seen these if you have been surfing the web a while
        switch (return_code) {
            case 200:
                s = s + "200 OK";
                break;
            case 400:
                s = s + "400 Bad Request";
                break;
            case 403:
                s = s + "403 Forbidden";
                break;
            case 404:
                s = s + "404 Not Found";
                break;
            case 500:
                s = s + "500 Internal Server Error";
                break;
            case 501:
                s = s + "501 Not Implemented";
                break;
        }
        
        s = s + "\r\n"; //other header fields,
        s = s + "Connection: close\r\n"; //we can't handle persistent connections
        s = s + "Server: SimpleHTTPtutorial v0\r\n"; //server name
        
        //Construct the right Content-Type for the header.
        //This is so the browser knows what to do with the
        //file, you may know the browser dosen't look on the file
        //extension, it is the servers job to let the browser know
        //what kind of file is being transmitted. You may have experienced
        //if the server is miss configured it may result in
        //pictures displayed as text!
        s = s + "Content-Type: " + file_type + "\r\n";
        
        ////so on and so on......
        s = s + "\r\n"; //this marks the end of the httpheader
        //and the start of the body
        //ok return our newly created header!
        return s;
    }
    
    private void processAmicoUpdateCommand( String command ) {
        command = command.replace("?", "");
        System.out.println( command );
        int n = command.indexOf("=");
        
        try {
            String variable = command.substring( 0, n );
            String value = command.substring( n + 1 );
            
            this.communicator.updateVariable( variable, value );
        } catch (Exception e) {    
        }
    }
    
    private void processAmicoCommand( String command, int method, DataOutputStream output ) {
        if (this.communicator != null) {
            try {
                command = URLDecoder.decode( command, "UTF-8" );
                // System.out.println( command );
                String response = "";
                
                if (command.startsWith("UPDATE ")) {
                    int n = command.indexOf(" ",7);
                    if (n > 0 && n+1 < command.length()) {
                        command = command.substring( 0, n + 1 ) + " " + URLEncoder.encode( command.substring( n + 1 ), "UTF-8" );
                    }
                }
                
                if (command.startsWith("GET") || command.startsWith("POPULATE")) {
                    response = this.communicator.sendAndReceive( command ).trim();
                } else {
                    this.communicator.send( command );
                }
                
                output.writeBytes(construct_http_header(200));
                
                //if it was a HEAD request, we don't print any BODY
                if (method == 1) { //1 is GET 2 is head and skips the body
                    output.writeBytes( response );
                    System.out.println( "Response: " + response );
                }
            } catch (Exception e) {
                e.printStackTrace( System.out );
            }
        }
    }
    
} //class phhew caffeine yes please!

class MimeTypes {
    private Hashtable mimeHashtable = new Hashtable();
    
    public MimeTypes() {
        this.loadHashtable();
    }
    
    public String getMimeType( String extension ) {
        String mimeType = (String) this.mimeHashtable.get( extension.toLowerCase() );
        
        return (mimeType != null) ? mimeType : "text/html";
    }
    
    private void loadHashtable() {
        for (int i = 0; i < this.mimeTypes.length; i++) {
            String mimeType = this.mimeTypes[i][0];
            String extensions = this.mimeTypes[i][1].trim();
            
            if (!extensions.equals("")) {
                StringTokenizer tokenizer = new StringTokenizer( extensions );
                
                while (tokenizer.hasMoreTokens()) {
                    String extension = tokenizer.nextToken();
                    this.mimeHashtable.put( extension.toLowerCase(), mimeType.toLowerCase() );
                }
            }
        }
    }
    
    String[][] mimeTypes = {
        {"application/andrew-inset", " ez " },
        {"application/applefile", "" },
        {"application/atom+xml", " atom " },
        {"application/atomicmail", "" },
        {"application/batch-smtp", "" },
        {"application/beep+xml", "" },
        {"application/cals-1840", "" },
        {"application/cnrp+xml", "" },
        {"application/commonground", "" },
        {"application/cpl+xml", "" },
        {"application/cybercash", "" },
        {"application/dca-rft", "" },
        {"application/dec-dx", "" },
        {"application/dvcs", "" },
        {"application/edi-consent", "" },
        {"application/edifact", "" },
        {"application/edi-x12", "" },
        {"application/eshop", "" },
        {"application/font-tdpfr", "" },
        {"application/http", "" },
        {"application/hyperstudio", "" },
        {"application/iges", "" },
        {"application/index", "" },
        {"application/index.cmd", "" },
        {"application/index.obj", "" },
        {"application/index.response", "" },
        {"application/index.vnd", "" },
        {"application/iotp", "" },
        {"application/ipp", "" },
        {"application/isup", "" },
        {"application/mac-binhex40", " hqx " },
        {"application/mac-compactpro", " cpt " },
        {"application/macwriteii", "" },
        {"application/marc", "" },
        {"application/mathematica", "" },
        {"application/mathml+xml", " mathml " },
        {"application/msword", " doc " },
        {"application/news-message-id", "" },
        {"application/news-transmission", "" },
        {"application/ocsp-request", "" },
        {"application/ocsp-response", "" },
        {"application/octet-stream", " bin dms lha lzh exe class so dll dmg " },
        {"application/oda", " oda " },
        {"application/ogg", " ogg " },
        {"application/parityfec", "" },
        {"application/pdf", " pdf " },
        {"application/pgp-encrypted", "" },
        {"application/pgp-keys", "" },
        {"application/pgp-signature", "" },
        {"application/pkcs10", "" },
        {"application/pkcs7-mime", "" },
        {"application/pkcs7-signature", "" },
        {"application/pkix-cert", "" },
        {"application/pkix-crl", "" },
        {"application/pkixcmp", "" },
        {"application/postscript", " ai eps ps " },
        {"application/prs.alvestrand.titrax-sheet", "" },
        {"application/prs.cww", "" },
        {"application/prs.nprend", "" },
        {"application/prs.plucker", "" },
        {"application/qsig", "" },
        {"application/rdf+xml", " rdf " },
        {"application/reginfo+xml", "" },
        {"application/remote-printing", "" },
        {"application/riscos", "" },
        {"application/rtf", "" },
        {"application/sdp", "" },
        {"application/set-payment", "" },
        {"application/set-payment-initiation", "" },
        {"application/set-registration", "" },
        {"application/set-registration-initiation", "" },
        {"application/sgml", "" },
        {"application/sgml-open-catalog", "" },
        {"application/sieve", "" },
        {"application/slate", "" },
        {"application/smil", " smi smil " },
        {"application/srgs", " gram " },
        {"application/srgs+xml", " grxml " },
        {"application/timestamp-query", "" },
        {"application/timestamp-reply", "" },
        {"application/tve-trigger", "" },
        {"application/vemmi", "" },
        {"application/vnd.3gpp.pic-bw-large", "" },
        {"application/vnd.3gpp.pic-bw-small", "" },
        {"application/vnd.3gpp.pic-bw-var", "" },
        {"application/vnd.3gpp.sms", "" },
        {"application/vnd.3m.post-it-notes", "" },
        {"application/vnd.accpac.simply.aso", "" },
        {"application/vnd.accpac.simply.imp", "" },
        {"application/vnd.acucobol", "" },
        {"application/vnd.acucorp", "" },
        {"application/vnd.adobe.xfdf", "" },
        {"application/vnd.aether.imp", "" },
        {"application/vnd.amiga.ami", "" },
        {"application/vnd.anser-web-certificate-issue-initiation", "" },
        {"application/vnd.anser-web-funds-transfer-initiation", "" },
        {"application/vnd.audiograph", "" },
        {"application/vnd.blueice.multipass", "" },
        {"application/vnd.bmi", "" },
        {"application/vnd.businessobjects", "" },
        {"application/vnd.canon-cpdl", "" },
        {"application/vnd.canon-lips", "" },
        {"application/vnd.cinderella", "" },
        {"application/vnd.claymore", "" },
        {"application/vnd.commerce-battelle", "" },
        {"application/vnd.commonspace", "" },
        {"application/vnd.contact.cmsg", "" },
        {"application/vnd.cosmocaller", "" },
        {"application/vnd.criticaltools.wbs+xml", "" },
        {"application/vnd.ctc-posml", "" },
        {"application/vnd.cups-postscript", "" },
        {"application/vnd.cups-raster", "" },
        {"application/vnd.cups-raw", "" },
        {"application/vnd.curl", "" },
        {"application/vnd.cybank", "" },
        {"application/vnd.data-vision.rdz", "" },
        {"application/vnd.dna", "" },
        {"application/vnd.dpgraph", "" },
        {"application/vnd.dreamfactory", "" },
        {"application/vnd.dxr", "" },
        {"application/vnd.ecdis-update", "" },
        {"application/vnd.ecowin.chart", "" },
        {"application/vnd.ecowin.filerequest", "" },
        {"application/vnd.ecowin.fileupdate", "" },
        {"application/vnd.ecowin.series", "" },
        {"application/vnd.ecowin.seriesrequest", "" },
        {"application/vnd.ecowin.seriesupdate", "" },
        {"application/vnd.enliven", "" },
        {"application/vnd.epson.esf", "" },
        {"application/vnd.epson.msf", "" },
        {"application/vnd.epson.quickanime", "" },
        {"application/vnd.epson.salt", "" },
        {"application/vnd.epson.ssf", "" },
        {"application/vnd.ericsson.quickcall", "" },
        {"application/vnd.eudora.data", "" },
        {"application/vnd.fdf", "" },
        {"application/vnd.ffsns", "" },
        {"application/vnd.fints", "" },
        {"application/vnd.flographit", "" },
        {"application/vnd.framemaker", "" },
        {"application/vnd.fsc.weblaunch", "" },
        {"application/vnd.fujitsu.oasys", "" },
        {"application/vnd.fujitsu.oasys2", "" },
        {"application/vnd.fujitsu.oasys3", "" },
        {"application/vnd.fujitsu.oasysgp", "" },
        {"application/vnd.fujitsu.oasysprs", "" },
        {"application/vnd.fujixerox.ddd", "" },
        {"application/vnd.fujixerox.docuworks", "" },
        {"application/vnd.fujixerox.docuworks.binder", "" },
        {"application/vnd.fut-misnet", "" },
        {"application/vnd.grafeq", "" },
        {"application/vnd.groove-account", "" },
        {"application/vnd.groove-help", "" },
        {"application/vnd.groove-identity-message", "" },
        {"application/vnd.groove-injector", "" },
        {"application/vnd.groove-tool-message", "" },
        {"application/vnd.groove-tool-template", "" },
        {"application/vnd.groove-vcard", "" },
        {"application/vnd.hbci", "" },
        {"application/vnd.hhe.lesson-player", "" },
        {"application/vnd.hp-hpgl", "" },
        {"application/vnd.hp-hpid", "" },
        {"application/vnd.hp-hps", "" },
        {"application/vnd.hp-pcl", "" },
        {"application/vnd.hp-pclxl", "" },
        {"application/vnd.httphone", "" },
        {"application/vnd.hzn-3d-crossword", "" },
        {"application/vnd.ibm.afplinedata", "" },
        {"application/vnd.ibm.electronic-media", "" },
        {"application/vnd.ibm.minipay", "" },
        {"application/vnd.ibm.modcap", "" },
        {"application/vnd.ibm.rights-management", "" },
        {"application/vnd.ibm.secure-container", "" },
        {"application/vnd.informix-visionary", "" },
        {"application/vnd.intercon.formnet", "" },
        {"application/vnd.intertrust.digibox", "" },
        {"application/vnd.intertrust.nncp", "" },
        {"application/vnd.intu.qbo", "" },
        {"application/vnd.intu.qfx", "" },
        {"application/vnd.irepository.package+xml", "" },
        {"application/vnd.is-xpr", "" },
        {"application/vnd.japannet-directory-service", "" },
        {"application/vnd.japannet-jpnstore-wakeup", "" },
        {"application/vnd.japannet-payment-wakeup", "" },
        {"application/vnd.japannet-registration", "" },
        {"application/vnd.japannet-registration-wakeup", "" },
        {"application/vnd.japannet-setstore-wakeup", "" },
        {"application/vnd.japannet-verification", "" },
        {"application/vnd.japannet-verification-wakeup", "" },
        {"application/vnd.jisp", "" },
        {"application/vnd.kde.karbon", "" },
        {"application/vnd.kde.kchart", "" },
        {"application/vnd.kde.kformula", "" },
        {"application/vnd.kde.kivio", "" },
        {"application/vnd.kde.kontour", "" },
        {"application/vnd.kde.kpresenter", "" },
        {"application/vnd.kde.kspread", "" },
        {"application/vnd.kde.kword", "" },
        {"application/vnd.kenameaapp", "" },
        {"application/vnd.koan", "" },
        {"application/vnd.liberty-request+xml", "" },
        {"application/vnd.llamagraphics.life-balance.desktop", "" },
        {"application/vnd.llamagraphics.life-balance.exchange+xml", "" },
        {"application/vnd.lotus-1-2-3", "" },
        {"application/vnd.lotus-approach", "" },
        {"application/vnd.lotus-freelance", "" },
        {"application/vnd.lotus-notes", "" },
        {"application/vnd.lotus-organizer", "" },
        {"application/vnd.lotus-screencam", "" },
        {"application/vnd.lotus-wordpro", "" },
        {"application/vnd.mcd", "" },
        {"application/vnd.mediastation.cdkey", "" },
        {"application/vnd.meridian-slingshot", "" },
        {"application/vnd.micrografx.flo", "" },
        {"application/vnd.micrografx.igx", "" },
        {"application/vnd.mif", " mif " },
        {"application/vnd.minisoft-hp3000-save", "" },
        {"application/vnd.mitsubishi.misty-guard.trustweb", "" },
        {"application/vnd.mobius.daf", "" },
        {"application/vnd.mobius.dis", "" },
        {"application/vnd.mobius.mbk", "" },
        {"application/vnd.mobius.mqy", "" },
        {"application/vnd.mobius.msl", "" },
        {"application/vnd.mobius.plc", "" },
        {"application/vnd.mobius.txf", "" },
        {"application/vnd.mophun.application", "" },
        {"application/vnd.mophun.certificate", "" },
        {"application/vnd.motorola.flexsuite", "" },
        {"application/vnd.motorola.flexsuite.adsi", "" },
        {"application/vnd.motorola.flexsuite.fis", "" },
        {"application/vnd.motorola.flexsuite.gotap", "" },
        {"application/vnd.motorola.flexsuite.kmr", "" },
        {"application/vnd.motorola.flexsuite.ttc", "" },
        {"application/vnd.motorola.flexsuite.wem", "" },
        {"application/vnd.mozilla.xul+xml", " xul " },
        {"application/vnd.ms-artgalry", "" },
        {"application/vnd.ms-asf", "" },
        {"application/vnd.ms-excel", " xls " },
        {"application/vnd.ms-lrm", "" },
        {"application/vnd.ms-powerpoint", " ppt " },
        {"application/vnd.ms-project", "" },
        {"application/vnd.ms-tnef", "" },
        {"application/vnd.ms-works", "" },
        {"application/vnd.ms-wpl", "" },
        {"application/vnd.mseq", "" },
        {"application/vnd.msign", "" },
        {"application/vnd.music-niff", "" },
        {"application/vnd.musician", "" },
        {"application/vnd.netfpx", "" },
        {"application/vnd.noblenet-directory", "" },
        {"application/vnd.noblenet-sealer", "" },
        {"application/vnd.noblenet-web", "" },
        {"application/vnd.novadigm.edm", "" },
        {"application/vnd.novadigm.edx", "" },
        {"application/vnd.novadigm.ext", "" },
        {"application/vnd.obn", "" },
        {"application/vnd.osa.netdeploy", "" },
        {"application/vnd.palm", "" },
        {"application/vnd.pg.format", "" },
        {"application/vnd.pg.osasli", "" },
        {"application/vnd.powerbuilder6", "" },
        {"application/vnd.powerbuilder6-s", "" },
        {"application/vnd.powerbuilder7", "" },
        {"application/vnd.powerbuilder7-s", "" },
        {"application/vnd.powerbuilder75", "" },
        {"application/vnd.powerbuilder75-s", "" },
        {"application/vnd.previewsystems.box", "" },
        {"application/vnd.publishare-delta-tree", "" },
        {"application/vnd.pvi.ptid1", "" },
        {"application/vnd.pwg-multiplexed", "" },
        {"application/vnd.pwg-xhtml-print+xml", "" },
        {"application/vnd.quark.quarkxpress", "" },
        {"application/vnd.rapid", "" },
        {"application/vnd.s3sms", "" },
        {"application/vnd.sealed.net", "" },
        {"application/vnd.seemail", "" },
        {"application/vnd.shana.informed.formdata", "" },
        {"application/vnd.shana.informed.formtemplate", "" },
        {"application/vnd.shana.informed.interchange", "" },
        {"application/vnd.shana.informed.package", "" },
        {"application/vnd.smaf", "" },
        {"application/vnd.sss-cod", "" },
        {"application/vnd.sss-dtf", "" },
        {"application/vnd.sss-ntf", "" },
        {"application/vnd.street-stream", "" },
        {"application/vnd.svd", "" },
        {"application/vnd.swiftview-ics", "" },
        {"application/vnd.triscape.mxs", "" },
        {"application/vnd.trueapp", "" },
        {"application/vnd.truedoc", "" },
        {"application/vnd.ufdl", "" },
        {"application/vnd.uplanet.alert", "" },
        {"application/vnd.uplanet.alert-wbxml", "" },
        {"application/vnd.uplanet.bearer-choice", "" },
        {"application/vnd.uplanet.bearer-choice-wbxml", "" },
        {"application/vnd.uplanet.cacheop", "" },
        {"application/vnd.uplanet.cacheop-wbxml", "" },
        {"application/vnd.uplanet.channel", "" },
        {"application/vnd.uplanet.channel-wbxml", "" },
        {"application/vnd.uplanet.list", "" },
        {"application/vnd.uplanet.list-wbxml", "" },
        {"application/vnd.uplanet.listcmd", "" },
        {"application/vnd.uplanet.listcmd-wbxml", "" },
        {"application/vnd.uplanet.signal", "" },
        {"application/vnd.vcx", "" },
        {"application/vnd.vectorworks", "" },
        {"application/vnd.vidsoft.vidconference", "" },
        {"application/vnd.visio", "" },
        {"application/vnd.visionary", "" },
        {"application/vnd.vividence.scriptfile", "" },
        {"application/vnd.vsf", "" },
        {"application/vnd.wap.sic", "" },
        {"application/vnd.wap.slc", "" },
        {"application/vnd.wap.wbxml", " wbxml " },
        {"application/vnd.wap.wmlc", " wmlc " },
        {"application/vnd.wap.wmlscriptc", " wmlsc " },
        {"application/vnd.webturbo", "" },
        {"application/vnd.wrq-hp3000-labelled", "" },
        {"application/vnd.wt.stf", "" },
        {"application/vnd.wv.csp+wbxml", "" },
        {"application/vnd.xara", "" },
        {"application/vnd.xfdl", "" },
        {"application/vnd.yamaha.hv-dic", "" },
        {"application/vnd.yamaha.hv-script", "" },
        {"application/vnd.yamaha.hv-voice", "" },
        {"application/vnd.yellowriver-custom-menu", "" },
        {"application/voicexml+xml", " vxml " },
        {"application/watcherinfo+xml", "" },
        {"application/whoispp-query", "" },
        {"application/whoispp-response", "" },
        {"application/wita", "" },
        {"application/wordperfect5.1", "" },
        {"application/x-bcpio", " bcpio " },
        {"application/x-cdlink", " vcd " },
        {"application/x-chess-pgn", " pgn " },
        {"application/x-compress", "" },
        {"application/x-cpio", " cpio " },
        {"application/x-csh", " csh " },
        {"application/x-director", " dcr dir dxr " },
        {"application/x-dvi", " dvi " },
        {"application/x-futuresplash", " spl " },
        {"application/x-gtar", " gtar " },
        {"application/x-gzip", "" },
        {"application/x-hdf", " hdf " },
        {"application/x-javascript", " js " },
        {"application/x-koan", " skp skd skt skm " },
        {"application/x-latex", " latex " },
        {"application/x-netcdf", " nc cdf " },
        {"application/x-sh", " sh " },
        {"application/x-shar", " shar " },
        {"application/x-shockwave-flash", " swf " },
        {"application/x-stuffit", " sit " },
        {"application/x-sv4cpio", " sv4cpio " },
        {"application/x-sv4crc", " sv4crc " },
        {"application/x-tar", " tar " },
        {"application/x-tcl", " tcl " },
        {"application/x-tex", " tex " },
        {"application/x-texinfo", " texinfo texi " },
        {"application/x-troff", " t tr roff " },
        {"application/x-troff-man", " man " },
        {"application/x-troff-me", " me " },
        {"application/x-troff-ms", " ms " },
        {"application/x-ustar", " ustar " },
        {"application/x-wais-source", " src " },
        {"application/x400-bp", "" },
        {"application/xhtml+xml", " xhtml xht " },
        {"application/xslt+xml", " xslt " },
        {"application/xml", " xml xsl " },
        {"application/xml-dtd", " dtd " },
        {"application/xml-external-parsed-entity", "" },
        {"application/zip", " zip " },
        {"audio/32kadpcm", "" },
        {"audio/amr", "" },
        {"audio/amr-wb", "" },
        {"audio/basic", " au snd " },
        {"audio/cn", "" },
        {"audio/dat12", "" },
        {"audio/dsr-es201108", "" },
        {"audio/dvi4", "" },
        {"audio/evrc", "" },
        {"audio/evrc0", "" },
        {"audio/g722", "" },
        {"audio/g.722.1", "" },
        {"audio/g723", "" },
        {"audio/g726-16", "" },
        {"audio/g726-24", "" },
        {"audio/g726-32", "" },
        {"audio/g726-40", "" },
        {"audio/g728", "" },
        {"audio/g729", "" },
        {"audio/g729D", "" },
        {"audio/g729E", "" },
        {"audio/gsm", "" },
        {"audio/gsm-efr", "" },
        {"audio/l8", "" },
        {"audio/l16", "" },
        {"audio/l20", "" },
        {"audio/l24", "" },
        {"audio/lpc", "" },
        {"audio/midi", " mid midi kar " },
        {"audio/mpa", "" },
        {"audio/mpa-robust", "" },
        {"audio/mp4a-latm", "" },
        {"audio/mpeg", " mpga mp2 mp3 " },
        {"audio/parityfec", "" },
        {"audio/pcma", "" },
        {"audio/pcmu", "" },
        {"audio/prs.sid", "" },
        {"audio/qcelp", "" },
        {"audio/red", "" },
        {"audio/smv", "" },
        {"audio/smv0", "" },
        {"audio/telephone-event", "" },
        {"audio/tone", "" },
        {"audio/vdvi", "" },
        {"audio/vnd.3gpp.iufp", "" },
        {"audio/vnd.cisco.nse", "" },
        {"audio/vnd.cns.anp1", "" },
        {"audio/vnd.cns.inf1", "" },
        {"audio/vnd.digital-winds", "" },
        {"audio/vnd.everad.plj", "" },
        {"audio/vnd.lucent.voice", "" },
        {"audio/vnd.nortel.vbk", "" },
        {"audio/vnd.nuera.ecelp4800", "" },
        {"audio/vnd.nuera.ecelp7470", "" },
        {"audio/vnd.nuera.ecelp9600", "" },
        {"audio/vnd.octel.sbc", "" },
        {"audio/vnd.qcelp", "" },
        {"audio/vnd.rhetorex.32kadpcm", "" },
        {"audio/vnd.vmx.cvsd", "" },
        {"audio/x-aiff", " aif aiff aifc " },
        {"audio/x-alaw-basic", "" },
        {"audio/x-mpegurl", " m3u " },
        {"audio/x-pn-realaudio", " ram ra " },
        {"audio/x-pn-realaudio-plugin", "" },
        {"application/vnd.rn-realmedia", " rm " },
        {"audio/x-wav", " wav " },
        {"chemical/x-pdb", " pdb " },
        {"chemical/x-xyz", " xyz " },
        {"image/bmp", " bmp " },
        {"image/cgm", " cgm " },
        {"image/g3fax", "" },
        {"image/gif", " gif " },
        {"image/ief", " ief " },
        {"image/jpeg", " jpeg jpg jpe " },
        {"image/naplps", "" },
        {"image/png", " png " },
        {"image/prs.btif", "" },
        {"image/prs.pti", "" },
        {"image/svg+xml", " svg " },
        {"image/t38", "" },
        {"image/tiff", " tiff tif " },
        {"image/tiff-fx", "" },
        {"image/vnd.cns.inf2", "" },
        {"image/vnd.djvu", " djvu djv " },
        {"image/vnd.dwg", "" },
        {"image/vnd.dxf", "" },
        {"image/vnd.fastbidsheet", "" },
        {"image/vnd.fpx", "" },
        {"image/vnd.fst", "" },
        {"image/vnd.fujixerox.edmics-mmr", "" },
        {"image/vnd.fujixerox.edmics-rlc", "" },
        {"image/vnd.globalgraphics.pgb", "" },
        {"image/vnd.mix", "" },
        {"image/vnd.ms-modi", "" },
        {"image/vnd.net-fpx", "" },
        {"image/vnd.svf", "" },
        {"image/vnd.wap.wbmp", " wbmp " },
        {"image/vnd.xiff", "" },
        {"image/x-cmu-raster", " ras " },
        {"image/x-icon", " ico " },
        {"image/x-portable-anymap", " pnm " },
        {"image/x-portable-bitmap", " pbm " },
        {"image/x-portable-graymap", " pgm " },
        {"image/x-portable-pixmap", " ppm " },
        {"image/x-rgb", " rgb " },
        {"image/x-xbitmap", " xbm " },
        {"image/x-xpixmap", " xpm " },
        {"image/x-xwindowdump", " xwd " },
        {"message/delivery-status", "" },
        {"message/disposition-notification", "" },
        {"message/external-body", "" },
        {"message/http", "" },
        {"message/news", "" },
        {"message/partial", "" },
        {"message/rfc822", "" },
        {"message/s-http", "" },
        {"message/sip", "" },
        {"message/sipfrag", "" },
        {"model/iges", " igs iges " },
        {"model/mesh", " msh mesh silo " },
        {"model/vnd.dwf", "" },
        {"model/vnd.flatland.3dml", "" },
        {"model/vnd.gdl", "" },
        {"model/vnd.gs-gdl", "" },
        {"model/vnd.gtw", "" },
        {"model/vnd.mts", "" },
        {"model/vnd.parasolid.transmit.binary", "" },
        {"model/vnd.parasolid.transmit.text", "" },
        {"model/vnd.vtu", "" },
        {"model/vrml", " wrl vrml " },
        {"multipart/alternative", "" },
        {"multipart/appledouble", "" },
        {"multipart/byteranges", "" },
        {"multipart/digest", "" },
        {"multipart/encrypted", "" },
        {"multipart/form-data", "" },
        {"multipart/header-set", "" },
        {"multipart/mixed", "" },
        {"multipart/parallel", "" },
        {"multipart/related", "" },
        {"multipart/report", "" },
        {"multipart/signed", "" },
        {"multipart/voice-message", "" },
        {"text/calendar", " ics ifb " },
        {"text/css", " css " },
        {"text/directory", "" },
        {"text/enriched", "" },
        {"text/html", " html htm " },
        {"text/parityfec", "" },
        {"text/plain", " asc txt " },
        {"text/prs.lines.tag", "" },
        {"text/rfc822-headers", "" },
        {"text/richtext", " rtx " },
        {"text/rtf", " rtf " },
        {"text/sgml", " sgml sgm " },
        {"text/t140", "" },
        {"text/tab-separated-values", " tsv " },
        {"text/uri-list", "" },
        {"text/vnd.abc", "" },
        {"text/vnd.curl", "" },
        {"text/vnd.dmclientscript", "" },
        {"text/vnd.fly", "" },
        {"text/vnd.fmi.flexstor", "" },
        {"text/vnd.in3d.3dml", "" },
        {"text/vnd.in3d.spot", "" },
        {"text/vnd.iptc.nitf", "" },
        {"text/vnd.iptc.newsml", "" },
        {"text/vnd.latex-z", "" },
        {"text/vnd.motorola.reflex", "" },
        {"text/vnd.ms-mediapackage", "" },
        {"text/vnd.net2phone.commcenter.command", "" },
        {"text/vnd.sun.j2me.app-descriptor", "" },
        {"text/vnd.wap.si", "" },
        {"text/vnd.wap.sl", "" },
        {"text/vnd.wap.wml", " wml " },
        {"text/vnd.wap.wmlscript", " wmls " },
        {"text/x-setext", " etx " },
        {"text/xml", "" },
        {"text/xml-external-parsed-entity", "" },
        {"video/bmpeg", "" },
        {"video/bt656", "" },
        {"video/celb", "" },
        {"video/dv", "" },
        {"video/h261", "" },
        {"video/h263", "" },
        {"video/h263-1998", "" },
        {"video/h263-2000", "" },
        {"video/jpeg", "" },
        {"video/mp1s", "" },
        {"video/mp2p", "" },
        {"video/mp2t", "" },
        {"video/mp4v-es", "" },
        {"video/mpv", "" },
        {"video/mpeg", " mpeg mpg mpe " },
        {"video/nv", "" },
        {"video/parityfec", "" },
        {"video/pointer", "" },
        {"video/quicktime", " qt mov " },
        {"video/smpte292m", "" },
        {"video/vnd.fvt", "" },
        {"video/vnd.motorola.video", "" },
        {"video/vnd.motorola.videop", "" },
        {"video/vnd.mpegurl", " mxu m4u " },
        {"video/vnd.nokia.interleaved-multimedia", "" },
        {"video/vnd.objectvideo", "" },
        {"video/vnd.vivo", "" },
        {"video/x-msvideo", " avi " },
        {"video/x-sgi-movie", " movie " },
        {"x-conference/x-cooltalk", " ice " },
    };
};