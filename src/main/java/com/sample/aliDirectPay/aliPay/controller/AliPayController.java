package com.sample.aliDirectPay.aliPay.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sample.aliDirectPay.aliPay.config.AlipayConfig;
import com.sample.aliDirectPay.aliPay.util.AlipaySubmit;
import com.sample.aliDirectPay.aliPay.util.MyUtils;

@Controller
@RequestMapping("/pay/alipay")
public class AliPayController {
	
	private Logger log = LoggerFactory.getLogger(AliPayController.class);
	
	@RequestMapping("/confirmOrder")
	public String aliPayHome(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	
		return  "/pay/index";
	}
	
	/**
	 * 支付宝即时到帐
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/directPay")
	public String aliDirectPay(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			// 构建即时到帐表单
			String html = buildAliPayDerectForm(request);
			log.debug("支付宝及时到帐表单=" + html);
			request.setAttribute("html", html);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return "/pay/invalidOrder";
		}
		return "/pay/route";
	}
	
	/**
	 * 创建阿里即时到帐表单
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private String buildAliPayDerectForm(HttpServletRequest request)throws Exception{
		// 商户订单号，商户网站订单系统中唯一订单号，必填
		String out_trade_no = request.getParameter("WIDout_trade_no");
		if(StringUtils.isBlank(out_trade_no)){
			throw new Exception("订单编号不能为空");
		}
		// 订单名称，必填	
		String subject = request.getParameter("WIDsubject");
		if(StringUtils.isBlank(subject)){
			throw new Exception("订单/商品名称不能为空");
		}
		// 付款金额，必填
		String total_fee = MyUtils.toStr(request.getParameter("WIDtotal_fee"));
		try {
			if(new Double(total_fee) < 0.01d){
				throw new Exception("付款金额必须大于等于0.01元");
			}
		} catch (Exception e) {
			throw new Exception("非法的付款金额:"+total_fee);
		}
		// 商品描述，可空
		String body = MyUtils.toStr(request.getParameter("WIDbody"));
		
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", AlipayConfig.service);
		sParaTemp.put("partner", AlipayConfig.getPartner());
		sParaTemp.put("seller_id", AlipayConfig.getSeller_id());
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", AlipayConfig.payment_type);
		sParaTemp.put("notify_url", AlipayConfig.getNotify_url());
		sParaTemp.put("return_url", AlipayConfig.getReturn_url());
		sParaTemp.put("anti_phishing_key", AlipayConfig.anti_phishing_key);
		sParaTemp.put("exter_invoke_ip", AlipayConfig.exter_invoke_ip);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("body", body);
		// 其他业务参数根据在线开发文档，添加参数.文档地址:https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.O9yorI&treeId=62&articleId=103740&docType=1
		// 如sParaTemp.put("参数名","参数值");
		// 建立请求
		String sHtmlText = AlipaySubmit.buildRequest(sParaTemp, "get", "确认");
		return sHtmlText;
	}
	

}
