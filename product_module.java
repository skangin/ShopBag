package admin;
import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

@Repository("product")
public class product_module {
	/* CDN 서버 정보 */
	String host= "iup.cdn1.cafe24.com";
	String user = "fluc5493";
	String password = "sk85712564";
	int port = 21;

	
	@Resource(name="sqlsession")
	private SqlSessionTemplate sessionTemplate;
		
	/* 상품코드 중복체크 */
	public p_dto pcode_check(String pcode) {
		p_dto pdto = this.sessionTemplate.selectOne("adminDB.pcode_ck", pcode);
				
		return pdto;
	}
	
	
	/*  파일업로드!! */
	public int fileUpload(MultipartFile[] mfile,p_dto pdto) {

		int result = 0;
		try {			
			CdnUpload cdnUpload = new CdnUpload(this.host, this.user, this.password, this.port);
			String finalUrl = cdnUpload.uploadFile(mfile);
		
			pdto.setPimg(finalUrl);
			// DB저장
			result = this.sessionTemplate.insert("adminDB.p_insert",pdto);
			
			
		}
		catch(Exception e){
			System.out.println("파일 업로드 에러");
			e.printStackTrace();
		}
		
		return result;
	}
}
