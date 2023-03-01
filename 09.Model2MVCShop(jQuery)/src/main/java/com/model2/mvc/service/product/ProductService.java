package com.model2.mvc.service.product;


import java.util.HashMap;
import java.util.Map;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.User;

//==> 상품관리에서 서비스할 내용 추상화/캡슐화한 Service Interface Definition  
public interface ProductService {
	
	// 상품등록
	public void addProduct(Product product) throws Exception ;
	
	// 상품정보확인
	public Product getProduct(int productNo) throws Exception ;
	
	// 상품정보리스트 
	public Map<String , Object> getProductList(Search search) throws Exception;
	
	// 상품정보 수정
	public void updateProduct(Product product) throws Exception ;
	

	
}
/*
public interface ProductService {
	
	
	public void addProduct(Product product) throws Exception;
	
	public Product getProduct(int productNo) throws Exception;
	
	public Map<String, Object> getProductList(Search search) throws Exception;
	
	public void updateProduct(Product product) throws Exception;	

	
}
*/