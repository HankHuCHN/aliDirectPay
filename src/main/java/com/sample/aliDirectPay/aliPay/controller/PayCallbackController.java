package com.sample.aliDirectPay.aliPay.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sample.aliDirectPay.aliPay.util.AlipayNotify;
import com.sample.aliDirectPay.aliPay.util.MyUtils;

@Controller
@RequestMapping("/callback/alipay")
public class PayCallbackController {
	
	private Logger log = LoggerFactory.getLogger(PayCallbackController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";
	
	/**
	 * <p>Description:支付宝同步通知</p>
	 * @author hank
	 * @date 2017年1月22日 下午2:39:31
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/return_url")
	public String tradeStatusSync(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		log.info("同步通知START---------");
		//获取支付宝GET过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		if(null == requestParams || requestParams.isEmpty()){
			log.info("同步通知参数为空,请求被忽略");
			log.info("请求IP：" + MyUtils.getIpAddr(request));
			return null;
		}
		try {
			for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]
							: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
//			valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				params.put(name, valueStr);
			}
			log.debug("回调参数>>>>>" + params.toString());
			//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
			//商户订单号
			String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
			//支付宝交易号
			//String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
			//交易状态
			String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
			//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
			
			//计算得出通知验证结果
			boolean verify_result = AlipayNotify.verify(params);
			if(verify_result){//验证成功
				log.info("订单:" + out_trade_no + "验证成功");
				//////////////////////////////////////////////////////////////////////////////////////////
				//请在这里加上商户的业务逻辑程序代码
				//——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					//判断该笔订单是否在商户网站中已经做过处理
						//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
						//如果有做过处理，不执行商户的业务程序
					log.info("订单:" + out_trade_no + "交易成功");
					return "/pay/paySuccess";
				} else {
					log.info("订单:" + out_trade_no + "交易失败？trade_status=" + trade_status);
					return "/pay/payFail";
				}
				
				//该页面可做页面美工编辑
//			out.println("验证成功<br />");
				//——请根据您的业务逻辑来编写程序（以上代码仅作参考）——

				//////////////////////////////////////////////////////////////////////////////////////////
			}else{
				//该页面可做页面美工编辑
				log.info("订单:" + out_trade_no + "验证失败");
				log.info(params.toString());
				return "/pay/payFail";
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		} finally{
			log.info("同步通知end---------");
		}
		return "/pay/payFail";
	}
	
	/**
	 * <p>Description:支付宝异步通知</p>
	 * @author hank
	 * @date 2017年1月22日 下午2:41:42
	 * @param request
	 * @param response
	 */
	@RequestMapping("/notify_url")
	public void tradeStatusAsyn(HttpServletRequest request,
			HttpServletResponse response) {
		//获取支付宝POST过来反馈信息
		String msg = FAIL;
		String out_trade_no = "";
		log.info(">>>>>支付宝异步通知 start");
		Map<String,String> params = new HashMap<String,String>();
		boolean falg = true;
		try {
			Map requestParams = request.getParameterMap();
			if(null == requestParams || requestParams.isEmpty()){
				log.info("异步通知参数为空，请求被忽略");
				log.info("请求IP：" + MyUtils.getIpAddr(request));
				falg =false;
				return;
			}
			for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]
							: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
//			valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
				params.put(name, valueStr);
			}
//			log.info("回调参数>>>>>" + params.toString());
			//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
			//商户订单号
			out_trade_no = MyUtils.toStr(request.getParameter("out_trade_no"));
			//支付宝交易号
//			String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
			//交易状态
			String trade_status = MyUtils.toStr(request.getParameter("trade_status"));
			//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//

			if(AlipayNotify.verify(params)){//验证成功
				//请在这里加上商户的业务逻辑程序代码

				//——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
				
				if(trade_status.equals("TRADE_FINISHED")){
					//判断该笔订单是否在商户网站中已经做过处理
						//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
						//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
						//如果有做过处理，不执行商户的业务程序
						
					//注意：
					//退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
					
				} else if (trade_status.equals("TRADE_SUCCESS")){
					//判断该笔订单是否在商户网站中已经做过处理
						//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
						//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
						//如果有做过处理，不执行商户的业务程序
						
					//注意：
					//付款完成后，支付宝系统发送该交易状态通知
					
					// 调用修改订单状态接口
					try {
						// …………
					} catch (Exception e) {
						log.error("异步通知调用修改订单状态接口失败。");
						log.error(e.getMessage(),e);
					}
				}
				//保存异步通知日志
				try {
					//......
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					log.error("异步通知保存失败参数：" + params.toString());
				}
				//——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
//				out.print("success");	//请不要修改或删除
				msg = SUCCESS;
			}else{//验证失败
				log.error(">>>>>>支付宝异步通知处理成功，验证失败！");
				log.error("支付宝异步通知处理失败：" + params.toString());
			}
		} catch (Exception e) {
			log.error(">>>>>>支付宝异步通知处理失败");
			log.error(e.getMessage(),e);
			log.error("支付宝异步通知处理失败：" + params.toString());
		} finally {
			if(falg){
				try {
					PrintWriter out = response.getWriter();
					response.setContentType("text/html;charset=UTF-8");
					log.info("订单:"+out_trade_no+">>>支付宝异步通知处理结果:" + msg);
					out.write(msg); // 无论成功失败，都会给支付宝返回消息
				} catch (IOException e) {
					log.error(">>>>>>通知支付宝处理结果时发生错误！");
					log.error(e.getMessage(),e);
				}
			}
			log.info(">>>>>支付宝异步通知 end \r\n");
		}
	}
}
