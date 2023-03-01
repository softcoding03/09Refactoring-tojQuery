package com.model2.mvc.service.product;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.*;


//==> 상품관리에서 CRUD 추상화/캡슐화한 DAO Interface Definition
	public interface ProductDao {
		
		// INSERT
		public void addProduct(Product product) throws Exception ;
	
		// SELECT ONE
		public Product getProduct(int productNo) throws Exception ;
	
		// SELECT LIST
		public List<Product> getProductList(Search search) throws Exception ;
	
		// UPDATE
		public void updateProduct(Product product) throws Exception ;
		
		// 게시판 Page 처리를 위한 전체Row(totalCount)  return
		public int getTotalCount(Search search) throws Exception ;
		
	}




/*
public class ProductDao {
	
	public ProductDao(){
	}

	public void insertProduct(Product product) throws Exception {
		
		Connection con = DBUtil.getConnection();
		
		String sql = "insert into Product values (seq_product_prod_no.nextval,?,?,to_char(to_date(?,'yyyy-mm-dd'),'yyyymmdd'),?,?,sysdate)";
		
		PreparedStatement stmt = con.prepareStatement(sql);

		stmt.setString(1, product.getProdName());
		stmt.setString(2, product.getProdDetail());
		stmt.setString(3, product.getManuDate());
		stmt.setInt(4, product.getPrice());
		stmt.setString(5, product.getFileName());
		
		stmt.executeUpdate();
		
		con.close();
	}

	public Product getProduct(int productNo) throws Exception {
		
		Connection con = DBUtil.getConnection();

		String sql = "select * from PRODUCT where PROD_NO=?";
		
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setInt(1, productNo);

		ResultSet rs = stmt.executeQuery();

		Product produt = null;
		
		while (rs.next()) {
			
			produt = new Product();
			produt.setProdNo(productNo);
			produt.setProdName(rs.getString("prod_Name"));
			produt.setProdDetail(rs.getString("prod_Detail"));
			produt.setManuDate(rs.getString("manufacture_Day"));
			produt.setPrice(rs.getInt("price"));
			produt.setFileName(rs.getString("image_file")); 
			produt.setRegDate(rs.getDate("reg_Date"));

		}
		
		con.close();

		return produt; //WHERE = productNo 를 이용해 상품 정보 가져오기
	}

	public Map<String,Object> getProductList(Search search) throws Exception {
		
		Map<String , Object>  map = new HashMap<String, Object>();
		Connection con = DBUtil.getConnection();
		
		String sql = "select * from PRODUCT ";
	
		
		if (search.getSearchCondition() != null) {
			if ( search.getSearchCondition().equals("0") &&  !search.getSearchKeyword().equals("") ) {
				sql += " WHERE PROD_NO = '" + search.getSearchKeyword()+"'";
			} else if ( search.getSearchCondition().equals("1") && !search.getSearchKeyword().equals("")) {// 상품번호가 serchVO의 번호에 세팅되어있어야함
				sql += " WHERE PROD_NAME LIKE '%" + search.getSearchKeyword()+"%'";
			} else if ( search.getSearchCondition().equals("2") && !search.getSearchKeyword().equals("")) {// 상품명이 serchVO의 번호에 세팅되어있어야함
				sql += " WHERE PRICE ='" + search.getSearchKeyword()+"'";//가격 가져옴
			}
		}
		sql += " order by prod_no";     
		
		System.out.println("ProductDAO :: Original SQL :: " + sql);
		//==> TotalCount GET
		int totalCount = this.getTotalCount(sql); //ProductDao.getTotalCount 실행 -> 레코드 총 row 수 반환
		System.out.println("ProductDAO :: totalCount :: " + totalCount);
		
		//==> CurrentPage 게시물만 받도록 Query 다시구성(모든데이터 가져올 필요 없기 때문에)
		sql = makeCurrentPageSql(sql, search); //ProductDao.makeCurrentPageSql 실행
		
		PreparedStatement stmt = con.prepareStatement(sql);
		//TYPE_SCROLL_INSENSITIVE = rs.next()은 다음 row를 가져오고 이전의 데이터로 돌아가진 못한다. 커서가 지나간 자리에도 다시 돌아갈 수 있게 만들어줌.
		//CONCUR_UPDATABLE result로 가져온 값을 updateable 할 수 있다.
		ResultSet rs = stmt.executeQuery();

		System.out.println(search);
		
		List<Product> list = new ArrayList<Product>();
		
		while(rs.next()){
				Product vo = new Product();
				
				vo.setProdNo(rs.getInt("PROD_NO"));
				vo.setProdName(rs.getString("PROD_NAME"));
				vo.setProdDetail(rs.getString("PROD_DETAIL"));
				vo.setManuDate(rs.getString("MANUFACTURE_DAY"));
				vo.setPrice(rs.getInt("PRICE"));
				vo.setFileName(rs.getString("IMAGE_FILE"));
				vo.setRegDate(rs.getDate("REG_DATE"));
				list.add(vo);  //list에, select해온 정보들을 저장한 vo를 담는다.
		}
		
	
		//==> totalCount 정보 저장
		map.put("totalCount", new Integer(totalCount));
		//==> currentPage 의 게시물 정보 갖는 List 저장
		map.put("list", list);
		
		rs.close();
		stmt.close();
		con.close();
			
		return map; //totalCount와 'list'(select해온 vo 정보들을 담은 list) 
	}

	
	public void updateProduct(Product product) throws Exception {
		//browser에서 상품정보수정 값들 입력받은 것 세팅해놓은 productVO를 인자로 받음
		Connection con = DBUtil.getConnection();
		
		String sql = "update PRODUCT set PROD_NAME=?,PROD_DETAIL=?,MANUFACTURE_DAY=to_char(to_date(?,'yyyy-mm-dd'),'yyyymmdd'),price=?,IMAGE_FILE=? where PROD_NO=?";
		
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setString(1, product.getProdName());
		stmt.setString(2, product.getProdDetail());
		stmt.setString(3, product.getManuDate());
		stmt.setInt(4, product.getPrice());
		stmt.setString(5, product.getFileName());
		stmt.setInt(6, product.getProdNo());
		stmt.executeUpdate();
		
		System.out.println("update 쿼리 날리기 완료 !");
		
		con.close();
	}
	
	private int getTotalCount(String sql) throws Exception {
		
		sql = "SELECT COUNT(*) "+
		          "FROM ( " +sql+ ") countTable";
		
		Connection con = DBUtil.getConnection();
		PreparedStatement pStmt = con.prepareStatement(sql);
		ResultSet rs = pStmt.executeQuery();
		
		int totalCount = 0;
		if( rs.next() ){
			totalCount = rs.getInt(1);// 첫 열의 int ?
		}
		
		pStmt.close();
		con.close();
		rs.close();
		
		return totalCount; //레코드 row 수 반환
	}
	
	// 게시판 currentPage Row 만  return 
	private String makeCurrentPageSql(String sql , Search search){
		sql = 	"SELECT * "+ 
					"FROM (SELECT inner_table. * , ROWNUM AS row_seq" +
							" FROM ("+sql+") inner_table"+
							" WHERE ROWNUM <="+search.getCurrentPage()*search.getPageSize()+" ) " +
					"WHERE row_seq BETWEEN "+((search.getCurrentPage()-1)*search.getPageSize()+1) +" AND "+search.getCurrentPage()*search.getPageSize();
		
		System.out.println("UserDAO :: make SQL :: "+ sql);	
		
		return sql;
	}
	
	
	
}*/