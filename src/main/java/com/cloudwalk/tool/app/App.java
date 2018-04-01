package com.cloudwalk.tool.app;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.cloudwalk.tool.bankocrAccu.BankOcrAccuTool;
import com.cloudwalk.tool.bankorc.BankOcrTool;
import com.cloudwalk.tool.face.FaceBaceTool;
import com.cloudwalk.tool.face.FaceNagTool;
import com.cloudwalk.tool.face.FaceRecogTool;
import com.cloudwalk.tool.grid.GridTool;
import com.cloudwalk.tool.idcard.IDCardBackTool;
import com.cloudwalk.tool.idcard.IDCardFrontTool;
import com.cloudwalk.tool.idcardAccu.IDCardBackToolAccu;
import com.cloudwalk.tool.idcardAccu.IDCardFrontToolAccu;
import com.cloudwalk.tool.image.ImageTool;
import com.cloudwalk.tool.orc.OrcTool;
import com.cloudwalk.tool.silence.SilenceTool;
import com.cloudwalk.tool.util.FileUtil;

public class App {

	private static String url;
	private static String driver;
	private static String user;
	private static String pwd;
	private static String root;

	public static void main(String[] args) {
		try {
			SAXReader saxReader = new SAXReader();
			File file = new File(FileUtil.getAppRoot() + File.separator + "configure.xml");
			FileUtil.createDirAndFileIfNotExits(file);
			Document document = saxReader.read(file);
			Element rootElement = document.getRootElement();

			url = rootElement.element("url").getData().toString();
			driver = rootElement.element("driver").getData().toString();
			user = rootElement.element("user").getData().toString();
			pwd = rootElement.element("pwd").getData().toString();
			root = rootElement.element("root").getData().toString();

			if (rootElement.element("IDCardFront").getData().toString().equals("true")) {
				new IDCardFrontTool(url, driver, user, pwd, "E:/test-collection/idcard/front").uploadAll();
			}
			if (rootElement.element("IDCardBack").getData().toString().equals("true")) {
				new IDCardBackTool(url, driver, user, pwd, root + "/idcard/back").uploadAll();
			}
			if (rootElement.element("Image").getData().toString().equals("true")) {
				new ImageTool(url, driver, user, pwd, root + "/video").uploadAll();
			}
			if (rootElement.element("Silence").getData().toString().equals("true")) {
				new SilenceTool(url, driver, user, pwd, root + "/silence").uploadAll();
			}
			if (rootElement.element("FaceBase").getData().toString().equals("true")) {
				new FaceBaceTool(url, driver, user, pwd, root + "/face/idcard_for_base").uploadAll();
			}
			if (rootElement.element("FaceRecog").getData().toString().equals("true")) {
				new FaceRecogTool(url, driver, user, pwd, root + "/face/field_for_recog").uploadAll();
			}
			if (rootElement.element("BankOcr").getData().toString().equals("true")) {
				new BankOcrTool(url, driver, user, pwd, root + "/mixbankCard").uploadAll();
			}
			if (rootElement.element("Gird").getData().toString().equals("true")) {
				new GridTool(url, driver, user, pwd, root + "/grid").uploadAll();
			}
			if (rootElement.element("TicketOcr").getData().toString().equals("true")) {
				new OrcTool(url, driver, user, pwd, root + "/ticketocr").uploadAll();
			}
			if (rootElement.element("FaceNag").getData().toString().equals("true")) {
				new FaceNagTool(url, driver, user, pwd, root + "/nag1000_yuanshi").uploadAll();
			}
			if (rootElement.element("BankOcrAccu").getData().toString().equals("true")) {
				new BankOcrAccuTool(url, driver, user, pwd, "E:/ocrBankCard/image_corr").uploadAll();
			}
			if (rootElement.element("IDcardFrontAccu").getData().toString().equals("true")) {
				new IDCardFrontToolAccu(url, driver, user, pwd, "E:/idpic/front500").uploadAll();
			}
			if (rootElement.element("IDcardBackAccu").getData().toString().equals("true")) {
				new IDCardBackToolAccu(url, driver, user, pwd, "E:/idpic/back500").uploadAll();
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}

	}
}
