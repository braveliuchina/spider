package cn.cnki.spider.util;

import cn.cnki.spider.pipeline.TyrbJsonFilePageModelPipeline;
import cn.cnki.spider.spider.TyrbRepo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.*;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.model.OOSpider;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Data
@RequiredArgsConstructor
public class ChromeUtil {

	private WebDriver webDriver;

	private BrowserMobProxy browserMobProxy = new BrowserMobProxyServer();

	/**
	 * 格式化url进入该url设置页
	 *
	 * @param url
	 * @return
	 */
	private static String _base_url(String url) {
		if (url.isEmpty()) {
			return url;
		}

		try {
			URL urls = new URL(url);
			return String.format("%s://%s", urls.getProtocol(), urls.getHost());
		} catch (Exception e) {
			return url;
		}
	}

	/**
	 * 元素选择
	 *
	 * @param driver
	 * @param element
	 * @return
	 */
	private static WebElement _shadow_root(WebDriver driver, WebElement element) {
		return (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", element);
	}

	//chrome 77
	public static void allow_flash77(WebDriver driver, String url) {
		url = _base_url(url);
		driver.get(String.format("chrome://settings/content/siteDetails?site=%s", url));
		WebElement webele_settings = _shadow_root(driver,
				(((ChromeDriver) driver).findElementByTagName("settings-ui")));
		WebElement webele_container = webele_settings.findElement(By.id("container"));
		WebElement webele_main = _shadow_root(driver, webele_container.findElement(By.id("main")));
		WebElement showing_subpage = _shadow_root(driver, webele_main.findElement(By.className("showing-subpage")));
		WebElement advancedPage = showing_subpage.findElement(By.id("advancedPage"));
//        WebElement settings_section_page = _shadow_root(driver,advancedPage.findElement(By.tagName("settings-section")));
		WebElement settings_privacy_page = _shadow_root(driver,
				advancedPage.findElement(By.tagName("settings-privacy-page")));
		WebElement pages = settings_privacy_page.findElement(By.id("pages"));
		WebElement settings_subpage = pages.findElement(By.tagName("settings-subpage"));
		WebElement site_details = _shadow_root(driver, settings_subpage.findElement(By.tagName("site-details")));
		// flash
		WebElement plugins = _shadow_root(driver, site_details.findElement(By.id("plugins")));
		WebElement permission = plugins.findElement(By.id("permission"));
		Select sel = new Select(permission);
		sel.selectByValue("allow");
//		// 自动下载
//		WebElement plugins2 = _shadow_root(driver, site_details.findElement(By.id("automaticDownloads")));
//		WebElement permission2 = plugins2.findElement(By.id("permission"));
//		Select sel2 = new Select(permission2);
//		sel2.selectByValue("allow");
//
//		// 自动下载
//		WebElement plugins3 = _shadow_root(driver, site_details.findElement(By.id("mixed-script")));
//		WebElement permission3 = plugins3.findElement(By.id("permission"));
//		Select sel3 = new Select(permission3);
//		sel3.selectByValue("allow");
//
//		WebElement plugins4 = _shadow_root(driver, site_details.findElement(By.id("nativeFileSystemWrite")));
//		WebElement permission4 = plugins4.findElement(By.id("permission"));
//		Select sel4 = new Select(permission4);
//		sel4.selectByValue("allow");
	}

	/**
	 * chrome 84设置允许flash运行
	 *
	 * @param driver
	 * @param url
	 */
	public static void allow_flash84(WebDriver driver, String url) throws Exception{
		url = _base_url(url);
		driver.get(String.format("chrome://settings/content/siteDetails?site=%s", url));
		WebElement webele_settings = _shadow_root(driver,
				(((ChromeDriver) driver).findElementByTagName("settings-ui")));
		WebElement webele_container = webele_settings.findElement(By.id("container"));
		WebElement webele_main = _shadow_root(driver, webele_container.findElement(By.id("main")));
		WebElement showing_subpage = _shadow_root(driver, webele_main.findElement(By.className("showing-subpage")));
		WebElement advancedPage = showing_subpage.findElement(By.id("basicPage"));
//        WebElement settings_section_page = _shadow_root(driver,advancedPage.findElement(By.tagName("settings-section")));
		WebElement settings_privacy_page = _shadow_root(driver,
				advancedPage.findElement(By.tagName("settings-privacy-page")));
		WebElement pages = settings_privacy_page.findElement(By.id("pages"));
		WebElement settings_subpage = pages.findElement(By.tagName("settings-subpage"));
		WebElement site_details = _shadow_root(driver, settings_subpage.findElement(By.tagName("site-details")));
		// flash
		WebElement plugins = _shadow_root(driver, site_details.findElement(By.id("plugins")));
		WebElement permission = plugins.findElement(By.id("permission"));
		Select sel = new Select(permission);
		sel.selectByValue("allow");
		// 自动下载
		WebElement plugins2 = _shadow_root(driver, site_details.findElement(By.id("automaticDownloads")));
		WebElement permission2 = plugins2.findElement(By.id("permission"));
		Select sel2 = new Select(permission2);
		sel2.selectByValue("allow");

		// 自动下载
		WebElement plugins3 = _shadow_root(driver, site_details.findElement(By.id("mixed-script")));
		WebElement permission3 = plugins3.findElement(By.id("permission"));
		Select sel3 = new Select(permission3);
		sel3.selectByValue("allow");

		WebElement plugins4 = _shadow_root(driver, site_details.findElement(By.id("nativeFileSystemWrite")));
		WebElement permission4 = plugins4.findElement(By.id("permission"));
		Select sel4 = new Select(permission4);
		sel4.selectByValue("allow");
	}

	/**
	 * chrome 86设置允许flash运行
	 *
	 * @param driver
	 * @param url
	 */
	public static void allow_flash(WebDriver driver, String url) throws Exception {
		url = _base_url(url);
		driver.get(String.format("chrome://settings/content/siteDetails?site=%s", url));
		WebElement webele_settings = _shadow_root(driver,
				(((ChromeDriver) driver).findElementByTagName("settings-ui")));
		WebElement webele_container = webele_settings.findElement(By.id("container"));
		WebElement webele_main = _shadow_root(driver, webele_container.findElement(By.id("main")));
		WebElement showing_subpage = _shadow_root(driver, webele_main.findElement(By.className("showing-subpage")));
		WebElement advancedPage = showing_subpage.findElement(By.id("basicPage"));
//        WebElement settings_section_page = _shadow_root(driver,advancedPage.findElement(By.tagName("settings-section")));
		WebElement settings_privacy_page = _shadow_root(driver,
				advancedPage.findElement(By.tagName("settings-privacy-page")));
		WebElement pages = settings_privacy_page.findElement(By.id("pages"));
		WebElement settings_subpage = pages.findElement(By.tagName("settings-subpage"));
		WebElement site_details = _shadow_root(driver, settings_subpage.findElement(By.tagName("site-details")));
		// flash
		WebElement plugins = _shadow_root(driver, site_details.findElement(By.id("plugins")));
		WebElement permission = plugins.findElement(By.id("permission"));
		Select sel = new Select(permission);
		sel.selectByValue("allow");
//		// 自动下载
//		WebElement plugins2 = _shadow_root(driver, site_details.findElement(By.id("automaticDownloads")));
//		WebElement permission2 = plugins2.findElement(By.id("permission"));
//		Select sel2 = new Select(permission2);
//		sel2.selectByValue("allow");
//
//		// 自动下载
//		WebElement plugins3 = _shadow_root(driver, site_details.findElement(By.id("mixed-script")));
//		WebElement permission3 = plugins3.findElement(By.id("permission"));
//		Select sel3 = new Select(permission3);
//		sel3.selectByValue("allow");
//
//		WebElement plugins4 = _shadow_root(driver, site_details.findElement(By.id("nativeFileSystemWrite")));
//		WebElement permission4 = plugins4.findElement(By.id("permission"));
//		Select sel4 = new Select(permission4);
//		sel4.selectByValue("allow");
	}

	public static Proxy buildSeleniumProxy(BrowserMobProxy browserMobProxy) {

		browserMobProxy.start(0);
		browserMobProxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
		browserMobProxy.newHar("flashXML");
		Proxy seleniumProxy = ClientUtil.createSeleniumProxy(browserMobProxy);

		// configure it as a desired capability
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
		return seleniumProxy;
	}
	
	public Proxy buildSeleniumProxy(BrowserMobProxy browserMobProxy, String harName) {

		try {
			browserMobProxy.start(0);
		} catch (Exception e) {
			browserMobProxy = new BrowserMobProxyServer();
			setBrowserMobProxy(browserMobProxy);
			browserMobProxy.start(0);
		}
		browserMobProxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
		browserMobProxy.newHar(harName);
		Proxy seleniumProxy = ClientUtil.createSeleniumProxy(browserMobProxy);

		// configure it as a desired capability
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
		return seleniumProxy;
	}

	public static boolean hasQuit(WebDriver driver) {
		try {
			driver.getTitle();
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	public WebDriver getDriver(String url, String harName) throws Exception {
		if (null != this.getWebDriver() && !hasQuit(this.getWebDriver())) {
			allow_flash(webDriver, url);
			return this.getWebDriver();
		}
//		ClassPathResource classPathResource = new ClassPathResource("drivers/chromedriver.exe");
		//			String driverProperty = System.getProperty("webdriver.chrome.driver");
//			if (StringUtils.isBlank(driverProperty)) {
//				System.out.println("driver property new reader");
		File file = new File("C:/spider-app/spider-app/drivers/chromedriver.exe");
//				@Cleanup InputStream in = classPathResource.getInputStream();
//				@Cleanup OutputStream outputStream = new FileOutputStream(file);
//
//				FileUtils.copyToFile(in, file);
		System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
//			}

		WebDriver webDriver = null;
		// 初始化selenium chrome webDriver
		ChromeOptions options = new ChromeOptions();
		//options.setHeadless(true);
		String fileDownloadPath = "C:\\downLoad\\selenium";
		Map<String, Object> prefsMap = new HashMap<String, Object>();
		prefsMap.put("download.default_directory", fileDownloadPath);
		prefsMap.put("download.prompt_for_download", false);
		prefsMap.put("download.directory_upgrade", true);
		prefsMap.put("safebrowsing.enabled", false);
		options.setExperimentalOption("prefs", prefsMap);
		// 创建 对应的 selenium 代理
		Proxy seleniumProxy = buildSeleniumProxy(browserMobProxy, harName);
		options.setProxy(seleniumProxy);
		webDriver = new ChromeDriver(options);
		webDriver.manage().window().setSize(new Dimension(1300, 800));
		webDriver.get(url);
		allow_flash(webDriver, webDriver.getCurrentUrl());
		try {
			Thread.sleep(15 * 1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.webDriver = webDriver;
		return webDriver;
	}
	
	public WebDriver getDriver(String url) throws Exception {
		return getDriver(url, "flashXml");
	}

	public List<String> doflashPageXmlPageRequest(String url) throws Exception {

		return doflashPageXmlPageRequest(url, "flashXml");

	}
	
	public List<String> doflashPageXmlPageRequest(String url, String harName) throws Exception {

		WebDriver webDriver = getDriver(url, harName);
		Actions actionOpenLinkInNewTab = new Actions(webDriver);
		actionOpenLinkInNewTab.keyDown(Keys.CONTROL).sendKeys("t").keyUp(Keys.CONTROL).perform();
		List<String> tabs = new ArrayList<String>(webDriver.getWindowHandles());
		webDriver.switchTo().window(tabs.get(tabs.size() - 1));
		webDriver.get(url);
		// 等待已确保页面正常加载
		try {
			Thread.sleep(15 * 1000L);
//			// 网络代理抓取网络请求记录
			Har har = browserMobProxy.getHar();
			return fetchMultiSpiderUrl(har);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Lists.emptyList();

	}

	public List<String> doflashPageXmlPageRequestV3(String url) throws Exception {

		return doflashPageXmlPageRequestV3(url, "flashXml"); 

	}

	public void mockLogin(String url, String harName, String userName, String password) throws Exception {

		WebDriver webDriver = getDriver(url);
		webDriver.get(url);
		// 等待已确保页面正常加载
		try {
			Thread.sleep(15 * 1000L);
//			// 网络代理抓取网络请求记录
			WebElement e = webDriver.findElement(By.id("login_username"));
			e.clear();
			e.sendKeys(userName);

			WebElement p = webDriver.findElement(By.id("login_password"));
			p.clear();
			p.sendKeys(userName);

			WebElement checkbox = webDriver.findElement(By.name("cookietime"));
			checkbox.click();

			WebElement btn = webDriver.findElement(By.id("loginbtn"));
			btn.click();
			System.out.println("login ...");
			Thread.sleep(15 * 1000L);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<String> doflashPageXmlPageRequestV3(String url, String harName) throws Exception {

		WebDriver webDriver = getDriver(url, harName);
		webDriver.get(url);
		// 等待已确保页面正常加载
		try {
			Thread.sleep(35 * 1000L);
//			// 网络代理抓取网络请求记录
			Har har = browserMobProxy.getHar();
			return fetchMultiCfg(har);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Lists.emptyList();

	}

	public String doflashPageXmlPageRequestV2(String url) throws Exception {

		WebDriver webDriver = getDriver(url);
		Actions actionOpenLinkInNewTab = new Actions(webDriver);
		actionOpenLinkInNewTab.keyDown(Keys.CONTROL).sendKeys("t").keyUp(Keys.CONTROL).perform();
		List<String> tabs = new ArrayList<String>(webDriver.getWindowHandles());
		webDriver.switchTo().window(tabs.get(tabs.size() - 1));
		webDriver.get(url);
		// 等待已确保页面正常加载
		try {
			Thread.sleep(15 * 1000L);
//			// 网络代理抓取网络请求记录
			Har har = browserMobProxy.getHar();
			return fetchCfg(har);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";

	}

	public List<String> buildForflashPageXmlPageRequest() throws IOException {

		Har har = browserMobProxy.getHar();
		try {
			return fetchSpiderUrl(har);
		} catch (XPathExpressionException | SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (webDriver != null) {
				webDriver.quit();
			}
			browserMobProxy.abort();
		}
		return Lists.emptyList();
	}

	public void releaseResource() {
		if (webDriver != null) {
			webDriver.quit();
		}
		browserMobProxy.abort();
	}

	public List<String> buildForflashPageXmlPageSingleRequest(String url) throws Exception {

		WebDriver webDriver = getDriver(url);
		webDriver.get(url);
		// 等待已确保页面正常加载
		try {
			Thread.sleep(15 * 1000L);
//			// 网络代理抓取网络请求记录
			Har har = browserMobProxy.getHar();
			return fetchSpiderUrl(har);
		} catch (InterruptedException | XPathExpressionException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (webDriver != null) {
				webDriver.quit();
			}
			browserMobProxy.abort();
		}

		return Lists.emptyList();
	}

	public void spiderForflashPageXmlPageRequest(String url) throws IOException {

		ClassPathResource classPathResource = new ClassPathResource("drivers/chromedriver.exe");
		File file = classPathResource.getFile();
		System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
		// 创建lightbody bmp浏览器代理
		BrowserMobProxy browserMobProxy = new BrowserMobProxyServer();
		// 创建 对应的 selenium 代理
		Proxy seleniumProxy = buildSeleniumProxy(browserMobProxy);

		WebDriver webDriver = null;
		try {
			// 初始化selenium chrome webDriver
			ChromeOptions options = new ChromeOptions();
			// 设置网络代理
			options.setProxy(seleniumProxy);
			webDriver = new ChromeDriver(options);
			webDriver.manage().window().setSize(new Dimension(1300, 800));
			// 获取重定向后网址再打开Flash权限
			webDriver.get(url);
			allow_flash(webDriver, webDriver.getCurrentUrl());
			// 启用flash后访问页面
			webDriver.get(url);
			// 等待已确保页面正常加载
			Thread.sleep(10 * 1000L);
			// 网络代理抓取网络请求记录
			Har har = browserMobProxy.getHar();
			buildAndSpider(har);
			Thread.sleep(5 * 1000L);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (webDriver != null) {
				webDriver.quit();
			}
			browserMobProxy.abort();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.setProperty("webdriver.chrome.driver",
				"E:/IDE/workSpace/cnki/spider-app/src/main/java/cn/cnki/drivers/chromedriver.exe");
		// 创建lightbody bmp浏览器代理
		BrowserMobProxy browserMobProxy = new BrowserMobProxyServer();
		// 创建 对应的 selenium 代理
		Proxy seleniumProxy = buildSeleniumProxy(browserMobProxy);

		WebDriver webDriver = null;
		try {
			// 初始化selenium chrome webDriver
			ChromeOptions options = new ChromeOptions();
			// 设置网络代理
			options.setProxy(seleniumProxy);
			webDriver = new ChromeDriver(options);
			webDriver.manage().window().setSize(new Dimension(1300, 800));

			String url = "http://epaper.tywbw.com/tyrb/";

			// 获取重定向后网址再打开Flash权限
			webDriver.get(url);
			allow_flash(webDriver, webDriver.getCurrentUrl());
			// 启用flash后访问页面
			webDriver.get(url);
			// 等待已确保页面正常加载
			Thread.sleep(10 * 1000L);
			// 网络代理抓取网络请求记录
			Har har = browserMobProxy.getHar();
			buildAndSpider(har);
			Thread.sleep(5 * 1000L);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (webDriver != null) {
				webDriver.quit();
			}
			browserMobProxy.abort();
		}
	}

	private static List<String> fetchMultiCfg(Har har) {
		HarLog logs = har.getLog();
		List<HarEntry> entries = logs.getEntries();
		if (entries.isEmpty()) {
			return null;
		}
		List<HarEntry> entryList = entries.stream().filter(entry -> {
			HarRequest request = entry.getRequest();
			String method = request.getMethod();
			String url = request.getUrl();
			return "GET".equals(method) && url.contains(".cfg");
		}).collect(Collectors.toList());
		if (entryList.size() < 1) {
			return null;
		}
		return entryList.stream().map(entry -> entry.getRequest().getUrl()).collect(Collectors.toList());
	}

	private static String fetchCfg(Har har) {
		HarLog logs = har.getLog();
		List<HarEntry> entries = logs.getEntries();
		if (entries.isEmpty()) {
			return null;
		}
		List<HarEntry> entryList = entries.stream().filter(entry -> {
			HarRequest request = entry.getRequest();
			String method = request.getMethod();
			String url = request.getUrl();
			return "GET".equals(method) && url.contains(".cfg");
		}).collect(Collectors.toList());
		if (entryList.size() < 1) {
			return null;
		}
		return entryList.get(0).getRequest().getUrl();
	}

	public static List<String> fetchMultiSpiderUrl(String urlPrefix, String context) {

		if (StringUtils.isBlank(context)) {
			return Lists.emptyList();
		}
		List<String> result = Lists.newArrayList();
		// 解析xml字符串
		Document docNew;
		try {
			docNew = newDocumentBuilder().parse(new InputSource(new StringReader(context.trim())));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Lists.emptyList();
		}
		// 创建 XPathFactory
		XPathFactory factory = XPathFactory.newInstance();
		// 创建 XPath对象
		XPath xpath = factory.newXPath();
		// 编译 XPath表达式
		XPathExpression expr;
		try {
			expr = xpath.compile("//Page/@XML");
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Lists.emptyList();
		}
		// 拿到匹配规则的网络请求中的response 并将response中的xml规则解析到list中
		NodeList nodeList;
		try {
			nodeList = (NodeList) expr.evaluate(docNew, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Lists.emptyList();
		}
		int length = nodeList.getLength();
		for (int i = 0; i < length; i++) {
			Attr attr = (Attr) nodeList.item(i);
//			String name = attr.getName();
			String value = attr.getValue();
			result.add(urlPrefix + value);
		}

		return result;
	}

	private static List<String> fetchMultiSpiderUrl(Har har)
			throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		HarLog logs = har.getLog();
		List<HarEntry> entries = logs.getEntries();
		if (entries.isEmpty()) {
			return Lists.emptyList();
		}
		List<HarEntry> entryList = entries.stream().filter(entry -> {
			HarRequest request = entry.getRequest();
			String method = request.getMethod();
			String url = request.getUrl();
			return "GET".equals(method) && url.contains(".cfg");
		}).collect(Collectors.toList());
		if (entryList.size() < 1) {
			return Lists.emptyList();
		}
		List<String> result = Lists.newArrayList();
		entryList.forEach(harEntry -> {
			HarResponse response = harEntry.getResponse();
			HarContent content = response.getContent();
			String context = content.getText();
			if (StringUtils.isBlank(context)) {
				return;
			}
			// 得到要抓取的xml页面的请求前缀
			String requestUrl = harEntry.getRequest().getUrl();
			String urlPrefix = harEntry.getRequest().getUrl().substring(0, requestUrl.lastIndexOf("/") + 1);

			// 解析xml字符串
			Document docNew;
			try {
				docNew = newDocumentBuilder().parse(new InputSource(new StringReader(context)));
			} catch (SAXException | IOException | ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			// 创建 XPathFactory
			XPathFactory factory = XPathFactory.newInstance();
			// 创建 XPath对象
			XPath xpath = factory.newXPath();
			// 编译 XPath表达式
			XPathExpression expr;
			try {
				expr = xpath.compile("//Page/@XML");
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			// 拿到匹配规则的网络请求中的response 并将response中的xml规则解析到list中
			NodeList nodeList;
			try {
				nodeList = (NodeList) expr.evaluate(docNew, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			int length = nodeList.getLength();
			for (int i = 0; i < length; i++) {
				Attr attr = (Attr) nodeList.item(i);
//				String name = attr.getName();
				String value = attr.getValue();
				result.add(urlPrefix + value);
			}
		});

		return result;
	}

	private static List<String> fetchSpiderUrl(Har har)
			throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		HarLog logs = har.getLog();
		List<HarEntry> entries = logs.getEntries();
		if (entries.isEmpty()) {
			return Lists.emptyList();
		}
		List<HarEntry> entryList = entries.stream().filter(entry -> {
			HarRequest request = entry.getRequest();
			String method = request.getMethod();
			String url = request.getUrl();
			return "GET".equals(method) && url.contains(".cfg");
		}).collect(Collectors.toList());
		if (entryList.size() < 1) {
			return Lists.emptyList();
		}
		HarEntry harEntry = entryList.get(0);
		HarResponse response = harEntry.getResponse();
		HarContent content = response.getContent();
		String context = content.getText();
		if (StringUtils.isBlank(context)) {
			return Lists.emptyList();
		}

		// 得到要抓取的xml页面的请求前缀
		String requestUrl = harEntry.getRequest().getUrl();
		String urlPrefix = harEntry.getRequest().getUrl().substring(0, requestUrl.lastIndexOf("/") + 1);

		// 解析xml字符串
		Document docNew = newDocumentBuilder().parse(new InputSource(new StringReader(context)));
		// 创建 XPathFactory
		XPathFactory factory = XPathFactory.newInstance();
		// 创建 XPath对象
		XPath xpath = factory.newXPath();
		// 编译 XPath表达式
		XPathExpression expr = xpath.compile("//Page/@XML");
		// 拿到匹配规则的网络请求中的response 并将response中的xml规则解析到list中
		NodeList nodeList = (NodeList) expr.evaluate(docNew, XPathConstants.NODESET);
		int length = nodeList.getLength();
		List<String> result = Lists.newArrayList();
		for (int i = 0; i < length; i++) {
			Attr attr = (Attr) nodeList.item(i);
//			String name = attr.getName();
			String value = attr.getValue();
			result.add(urlPrefix + value);
		}
		return result;
	}

	private static void buildAndSpider(Har har)
			throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		HarLog logs = har.getLog();
		List<HarEntry> entries = logs.getEntries();
		if (entries.isEmpty()) {
			return;
		}
		List<HarEntry> entryList = entries.stream().filter(entry -> {
			HarRequest request = entry.getRequest();
			String method = request.getMethod();
			String url = request.getUrl();
			return "GET".equals(method) && url.contains(".cfg");
		}).collect(Collectors.toList());
		if (entryList.size() < 1) {
			return;
		}
		HarEntry harEntry = entryList.get(0);
		HarResponse response = harEntry.getResponse();
		HarContent content = response.getContent();
		String context = content.getText();
		if (StringUtils.isBlank(context)) {
			return;
		}
		// 解析xml字符串
		Document docNew = newDocumentBuilder().parse(new InputSource(new StringReader(context)));
		// 创建 XPathFactory
		XPathFactory factory = XPathFactory.newInstance();
		// 创建 XPath对象
		XPath xpath = factory.newXPath();
		// 编译 XPath表达式
		XPathExpression expr = xpath.compile("//Page/@XML");
		// 拿到匹配规则的网络请求中的response 并将response中的xml规则解析到list中
		NodeList nodeList = (NodeList) expr.evaluate(docNew, XPathConstants.NODESET);
		int length = nodeList.getLength();
		List<String> result = Lists.newArrayList();
		for (int i = 0; i < length; i++) {
			Attr attr = (Attr) nodeList.item(i);
//			String name = attr.getName();
			String value = attr.getValue();
			result.add(value);
		}
		// 得到要抓取的xml页面的请求前缀
		String requestUrl = harEntry.getRequest().getUrl();
		String urlPrefix = harEntry.getRequest().getUrl().substring(0, requestUrl.lastIndexOf("/") + 1);
		// 用webMagic进行爬虫
		spiderTyrbRepos(urlPrefix, result);
	}

	private static void spiderTyrbRepos(String urlPrefix, List<String> xmlFiles) {
		OOSpider ooSpider = OOSpider.create(Site.me().setSleepTime(1000),
				new TyrbJsonFilePageModelPipeline("C:/spider"), TyrbRepo.class);
		xmlFiles.stream().forEach(xml -> {
			ooSpider.addUrl(urlPrefix + xml);
		});
		ooSpider.thread(5).run();
	}

	public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		documentBuilderFactory.setXIncludeAware(false);
		documentBuilderFactory.setExpandEntityReferences(false);

		// 调用工厂对象的newDocumentBuilder方法得到 DOM 解析器对象。
		return documentBuilderFactory.newDocumentBuilder();
	}
}