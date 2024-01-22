package admin;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.web.multipart.MultipartFile;

public class CdnUpload{
	
	PrintWriter pw = null;
	
	private String host;
    private String user;
    private String password;
    private int port;
    
    public CdnUpload(String host, String user, String password, int port) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.port = port;
    }
    
    
    public String uploadFile(MultipartFile[] mfile) {
    	String pFileName = null;
    	String fileName = null;
    	int w =0;
    	while(w < mfile.length) {
    		fileName = mfile[w].getOriginalFilename();    		
    		w++;
    	}
    	String type =null;
    	String url = "http://fluc5493.cdn1.cafe24.com/shopbag/";
    	String ftpPath = "/www/shopbag/"+pFileName;
    	
    	Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
    	
		int lastIndex = fileName.lastIndexOf(".");
		if(lastIndex != -1) {
			type = fileName.substring(fileName.lastIndexOf("."));			
		}
		
		pFileName = sf.format(date) + "_" + type;
		String purl = url+pFileName;
		
		FTPClient ftp = new FTPClient();
		ftp.setControlEncoding("utf-8");
		FTPClientConfig cf = new FTPClientConfig();
		
		ftp.configure(cf);
		try {
			ftp.connect(this.host,this.port); 
			
			if(ftp.login(this.user, this.password)) {
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				
				/* FTP 파일 업로드 PART */
				boolean result = false;
				
				int ww =0;
		    	while(ww < mfile.length) {
		    		result = ftp.storeFile("/www/shopbag/"+pFileName, mfile[ww].getInputStream());    		
		    		ww++;
		    	}
				
				if(result) {
					System.out.println("업로드 성공");
					return purl;
				}
				else {
					System.out.println("업로드 실패");
				}
			}
			else {
				System.out.println("ftp 정보가 올바르지 않습니다.");
			}
			ftp.logout();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } 
                catch (Exception e) {
                	System.out.println("최종 ftp 연결 해제도 실패");
                    e.printStackTrace();
                }
            }
        }
		
    	return null;
    }
}