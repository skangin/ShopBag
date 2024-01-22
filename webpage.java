package admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class webpage {

	@Inject
	private SqlSessionFactory sqlsessionfactory;
	
	@Resource
	private SqlSessionTemplate sqlsession; 
	
	PrintWriter pw = null;
	SqlSession se = null;
	HttpSession hs = null;
	
	@RequestMapping("shop/idcheck.do")
	public String idcheck(HttpServletRequest req, Model m) {
		String id = req.getParameter("idcheck");
		
		this.se = sqlsessionfactory.openSession();
		List<memberdto> check = se.selectList("adminDB.m_selectidsearch",id);  
		int result = check.size();
		if(result == 0) {
			m.addAttribute("result","true");
			m.addAttribute("id",id);
			
		}else {
			m.addAttribute("result","false");
			m.addAttribute("id",id);
		}
		
		this.se.close();
		
		
		return "/shop/id_checkok";
		
	}
	
	@RequestMapping("shop/mailcheck.do")
	public String mailcheck(HttpServletRequest req, Model m) {
		String mname = req.getParameter("mname");
		String mail = req.getParameter("mail"); 
		
		String mtitle = req.getParameter("mtitle");
		String mcontent = req.getParameter("mcontent");
		
		String host = "smtp.mail.nate.com";
		String user = "skanginsk@nate.com";
		String password = "sk85712564";
		  
		String to_email = mail;

		  Properties props = new Properties();
		  props.put("mail.smtp.host",host); // 네이버 SMTP
		  props.put("mail.smtp.port", 587);
		  props.put("mail.smtp.auth", "true");
		  props.put("mail.smtp.debug", "true");
		  props.put("mail.smtp.socketFactory.port", 587);
		  props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		
		  Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
		  protected PasswordAuthentication getPasswordAuthentication() {
		  return new PasswordAuthentication(user, password);
		  }
		  });

		  try {
		  MimeMessage msg = new MimeMessage(session);
		  msg.setFrom(new InternetAddress(user, mname));
		  msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to_email));

		  msg.setSubject(mtitle);
		  // 메일 내용
		  msg.setText(mcontent);

		  Transport.send(msg);
		  
		  m.addAttribute("msg","입력하신 이메일로 전송된 코드를 확인해주세요.");
		  }
		  catch (Exception e) {
		  e.printStackTrace();
		  m.addAttribute("msg","이메일 전송 중 오류가 발생했습니다. 메일 주소를 확인해주세요");
		  }
		
		
		return "shop/mail_checkok";
	}
	
	@RequestMapping("admin/admin_main.do")
	public void maindo(
			HttpServletRequest req,
			HttpServletResponse res,
			@RequestParam(required = false) String aid,
			@RequestParam(required = false) String apw
			) throws Exception {
		res.setContentType("text/html; charset=utf-8;");
		this.pw = res.getWriter();
		
		if(aid == null || apw == null) {
			this.pw.write("<script>"
					+ "alert('정상적인 접근이 아닙니다.');"
					+ "location.href='./index.jsp';"
					+ "</script>");
		}
		else if(aid.intern()=="master" && apw.intern()=="shop_master123") {
			this.hs = req.getSession(true);
			this.hs.setAttribute("name", "최고 관리자");
			
			this.pw.write("<script>"
					+ "alert('정상적으로 로그인되었습니다.');"
					+ "location.href='./admin_mainok.do';"
					+ "</script>");
		}
		else {
			this.pw.write("<script>"
					+ "alert('계정을 확인해주세요.');"
					+ "history.go(-1);"
					+ "</script>");
		}
		this.pw.close();
	}
	
	@RequestMapping("/admin/admin_mainok.do")
	public String member_list(Model m )throws Exception {
		List<memberdto> data = null;
		
		this.se = sqlsessionfactory.openSession();
		data = this.se.selectList("adminDB.m_selectall");
		m.addAttribute("data", data);		
		
		this.se.close();		
		
		return "/admin/admin_main";
	}
	
	@RequestMapping("shop/logoutok.do")
	public String logoutPage(HttpServletRequest req) {
		this.hs = req.getSession();
		this.hs.invalidate();
		
		return "redirect:shopmain.do";
	}
	
	
	
	@RequestMapping("/admin/member_stop.do")
	public String member_stop(@RequestParam String midx, Model m) {
		
		try {
			this.se = sqlsessionfactory.openSession();
			int result = se.update("adminDB.m_member_stop",midx);
			if(result >0) {
				m.addAttribute("msg","수정되었습니다.");
				
			}
			else {
				m.addAttribute("msg","과정 중 오류가 발생했습니다.");
				
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		finally {
			this.se.close();
		}
		return "/admin/member_updateok";
	}
	
	@RequestMapping("/admin/member_restore.do")
	public String member_restore(@RequestParam String midx,Model m) {
		try {
			this.se = sqlsessionfactory.openSession();
			int result = this.se.update("adminDB.m_member_restore",midx);
			if(result >0) {
				m.addAttribute("msg","수정되었습니다.");
			}
			else {
				m.addAttribute("msg","과정 중 오류가 발생했습니다.");
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		finally {
			this.se.close();
		}
		return "/admin/member_updateok";
	}
	
	@RequestMapping("shop/joinok.do")
	public String join_ok(@ModelAttribute("member") memberdto memberdto,Model m) throws Exception{
		this.se = sqlsessionfactory.openSession();
		
		if(memberdto.getMcsms() == null)
			memberdto.setMcsms("N");
		if(memberdto.getMcmail() == null)
			memberdto.setMcmail("N");
		
		int a = this.se.insert("adminDB.m_member_insert",memberdto);
		if(a>0) {
			m.addAttribute("msg","정상적으로 회원가입되었습니다.");
		}else {
			m.addAttribute("msg","회원가입중 오류가 발생하였습니다.");
		}
		this.se.close();
		return "/shop/joinok";
	}
	
	@RequestMapping("shop/login.do")
	public String login(Model m) {
		
		return "/shop/login";
	}
	
	@RequestMapping("shop/agree.do")
	public String agree(Model m) {
		
		return "/shop/agree";
	}
	
	@RequestMapping("shop/join.do")
	public String join(Model m) {
		
		return "/shop/join";
	}
	
	@PostMapping("shop/loginok.do")
	public void login_ok(HttpServletRequest req, HttpServletResponse res, Model m) throws Exception {
		String mid = req.getParameter("mid");
		String mpass = req.getParameter("mpass");
		memberdto login = new memberdto();
		login.setMid(mid);
		login.setMpass(mpass);
		
		this.se = sqlsessionfactory.openSession();
		List<memberdto> check= this.se.selectList("adminDB.m_selectlogin", login);
		
		int result = check.size();		
		res.setContentType("text/html; charset=utf-8;");
		this.pw = res.getWriter();
		String name = null;
		if(result == 0) {					
			this.pw.write("<script>"
					+ "alert('아이디와 비밀번호를 확인해주십시오');"
					+ "history.go(-1);"
					+ "</script>");
						
		}else {
			if(check.get(0).getMuse().intern()=="정상") {
				this.pw.write("<script>"
						+ "alert('정상적으로 로그인되었습니다.');"
						+"location.href='./shopmain.do';"
						+ "</script>");
			
				name = check.get(0).getMname();
				this.hs = req.getSession();
				this.hs.setAttribute("userName", name);
			}
			else { 
				this.pw.write("<script>"
						+ "alert('해당 계정은 관리자에 의해 정지되었습니다. 관리자에게 문의하십시오.');"
						+ "history.go(-1);"
						+ "</script>");
			}
		}
		
		
	}
	
	
	//Config
	
	@RequestMapping("/admin/admin_configok.do")	
	public void admin_config(@ModelAttribute config_dto config_dto, Model m, HttpServletResponse res) throws Exception {
		res.setContentType("text/html;charset=utf-8");
		List<config_dto> data = null;
		//Module
		config_module ci = new config_module(sqlsessionfactory);
		int result = ci.config_insert(config_dto);
		try {
			if(result == 1) {
			this.pw = res.getWriter();
			this.pw.write("<script>"
					+ "alert('정상적으로 반영 되었습니다.');"
					+ "location.href='./admin_config.do';"
					+ "</script>");
			}
		}
		catch(Exception e) {
			System.out.println("Database 오류발생!!");
		}
	}
	
	@RequestMapping("/admin/admin_config.do")	
	public String admin_config_view(Model m) {
		this.se = sqlsessionfactory.openSession();
		config_dto dto = this.se.selectOne("adminDB.config_select");  
	
		//System.out.println(dto.getSite_title());
		m.addAttribute("data",dto);
		this.se.close();	
		return "/admin/admin_config";
	}
	
	// Product
	
	/*쇼핑몰 메인page*/
	@RequestMapping("shop/shopmain.do")
	public String shop_main(Model m) {
		
		 
		
		try {
			this.se = sqlsessionfactory.openSession();
			List<p_dto> p_lists = this.se.selectList("adminDB.p_mainPlists");
			m.addAttribute("p_lists",p_lists);
			
			
			
		}
		catch(Exception e) {
			System.out.println("main 상품 출력 쿼리 PART 에러 발생");
			e.printStackTrace();
		}
		
		return "shop/index";
	}
	
	/* 쇼핑몰 하단 config 출력*/
	/*쇼핑몰 메인page*/
	@RequestMapping("shop/s_footer.do")
	public String s_footer(Model m) {		 
		
		try {
			this.se = sqlsessionfactory.openSession();			
			
			config_dto dto = this.se.selectOne("adminDB.config_select"); 
			m.addAttribute("siteInfo",dto);
			
		}
		catch(Exception e) {
			System.out.println("하단 config 출력 에러");
			e.printStackTrace();
		}
		
		return "shop/s_footer";
	}
	
	
	
	/* 상품 삭제 PART */
	@RequestMapping("admin/p_delete.do")
	public void p_delete(
			HttpServletResponse res,
			@RequestParam(defaultValue = "1") int pidx
			) {
		res.setContentType("text/html; charset=utf-8;");
		
		//System.out.println(pidx);
		PrintWriter pw = null;
		
		
		
		try {
			this.pw = res.getWriter();
			
			this.se = sqlsessionfactory.openSession();
			int result = this.se.delete("adminDB.p_delete",pidx);
			
			if(result ==1) {
				this.pw.write("<script>"
						+ "alert('해당 상품이 삭제 되었습니다.');"
						+ "location.href='./plists.do';"
						+ "</script>");
			}
			else {
				this.pw.write("<script>"
						+ "alert('삭제에 실패하였습니다.');"
						+ "history.go(-1);;"
						+ "</script>");
			}
			
			this.se.close();
			this.pw.close();
		}
		catch(Exception e) {
			System.out.println("상품 삭제 query 에러");
			e.printStackTrace();
		}
	}
	
	
	
	/*관리자 상품 list page PART*/
	@RequestMapping("admin/plists.do")
	public String p_regok(
			Model m,
			@ModelAttribute SearchDTO sdto,
			//@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "1") int pidx
			){
		
		
		try {
			this.se = sqlsessionfactory.openSession();
			List<p_dto> lists = null;
			int ct = 0;
			
			if(pidx < 2) {
				pidx =0;
			}
			else {
				pidx = (pidx-1)*5;
			}
			
			Map<String, Object> searchMap = new HashMap<String, Object>();
			
			if(sdto==null || sdto.getSearchVal() == null || sdto.getSearchType() == null) {
				lists = this.se.selectList("adminDB.p_selectAllProducts",pidx);
			}
			else {
				if(sdto.getSearchType().equals("상품명")){
					searchMap.put("part", "pname");
				}
				else if(sdto.getSearchType().equals("상품코드")){
					searchMap.put("part", "pcode");					
				}
				searchMap.put("searchVal", sdto.getSearchVal());
				lists = this.se.selectList("adminDB.p_selectBySearch",searchMap);
				
				m.addAttribute("sdto",sdto);
			}
			
			m.addAttribute("plists",lists);
			
			ct = se.selectOne("adminDB.p_countSelect",searchMap);
			m.addAttribute("ct",ct);
			
			this.se.close();
		}
		catch(Exception e) {
			System.out.println("상품 출력 select 쿼리부분 에러");
			e.printStackTrace();
		}
		
		return "admin/admin_product";
	}
	
	
	@Resource(name = "product")
	private product_module pm;
	
	/* 상품코드 중복체크 PART */
	@RequestMapping("admin/pcodeck.do")
	public String pcode_ck(Model m, @RequestParam(required = false) String pcode){

		p_dto pdto =this.pm.pcode_check(pcode);
		if(pdto == null) {
			m.addAttribute("pcodeCK","no");
		}
		else {
			m.addAttribute("pcodeCK","yes");
		}
		
		
		return "admin/pcodeck";
	}
	
	
	
	/*상품 등록 버튼 PART*/
	@RequestMapping("admin/product_register.do")
	public String p_register(
			MultipartFile[] mfile,HttpServletRequest req,
			@ModelAttribute("product2") p_dto pdto,
			HttpServletResponse res
			) throws IOException {
		res.setContentType("text/html; charset=utf-8;");
		
		this.pw = res.getWriter();

		int result = this.pm.fileUpload(mfile,pdto);
		if(result >= 1) {	
			this.pw.write("<script>"
					+ "alert('상품이 정상적으로 등록되었습니다.');"
					+ "location.href='./plists.do'"
					+ "</script>");
		}
		else {
			this.pw.write("<script>"
					+ "alert('상품 등록에 실패하셨습니다.');"
					+ "history.go(-1);"
					+ "</script>");
		}
		this.pw.close();
		
		return null;
	}

	
	
	
	
	/* 관리자 로그아웃 PART */
	@RequestMapping("admin/admin_logout.do")
	public String admin_logout(HttpServletRequest req) {
		this.hs = req.getSession(true);
		this.hs.invalidate();
		return "admin/index";
	}
}
