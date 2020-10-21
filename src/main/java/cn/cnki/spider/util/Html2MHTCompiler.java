package cn.cnki.spider.util;

import lombok.Cleanup;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 *
 * MHT文件解析类
 *
 */
@SuppressWarnings("unchecked")
public class Html2MHTCompiler {
    private URL strWeb = null;
    /** 网页地址 */
    private String strText = null;
    /** 网页文本内容 */
    private String strFileName = null;
    /** 本地文件名 */
    private String strEncoding = null;
    /** 网页编码 */

    // MHT格式附加信息
    private String from = "braveliuchina@gmail.com";
    private String to;
    private String subject;
    private String cc;
    private String bcc;
    private String smtp = "localhost";

//    public static void main(String[] args) throws Exception {
//        String content = "<div style=\"text-align: center;\">\r\n" + 
//        		" <img border=\"0\" src=\"http://www.gszx.gov.cn/HdAtt/img/2020/08/20200831171329719001.jpg\" align=\"center\" style=\"font-size: 9pt;\">\r\n" + 
//        		"</div> \r\n" + 
//        		"<div align=\"\" style=\"margin-top: 0px; margin-bottom: 0px; text-indent: 2em; text-align: justify;\"> \r\n" + 
//        		" <p align=\"\" style=\"margin-top: 0px; margin-bottom: 0px; text-indent: 2em; text-align: justify;\"></p> \r\n" + 
//        		" <p align=\"\" style=\"text-align: center; margin-top: 0px; margin-bottom: 0px; text-indent: 2em;\"><img border=\"0\" src=\"http://www.gszx.gov.cn/HdAtt/img/2020/08/20200831171329438002.jpg\" align=\"center\"></p> \r\n" + 
//        		" <p align=\"\" style=\"text-align: center; margin-top: 0px; margin-bottom: 0px; text-indent: 2em;\"><br> </p> \r\n" + 
//        		"</div> \r\n" + 
//        		"<div align=\"\" style=\"margin-top: 0px; margin-bottom: 0px; text-indent: 2em; text-align: justify;\"> \r\n" + 
//        		" <p align=\"\" style=\"margin-top: 0px; margin-bottom: 0px; text-indent: 2em; text-align: justify;\"></p> \r\n" + 
//        		" <p align=\"\" style=\"margin-top: 0px; margin-bottom: 0px; text-indent: 2em; text-align: justify;\"></p> \r\n" + 
//        		"</div> \r\n" + 
//        		"<div align=\"\" style=\"margin-top: 0px; margin-bottom: 0px; text-indent: 2em; text-align: justify;\">\r\n" + 
//        		" &nbsp;&nbsp;&nbsp; 进入八月以来，西和县连续遭遇强降雨天气，截至8月21日，全县受灾1.3万户6.2万人。农作物受灾面大、群众房屋和水利、交通等基础设施受损严重，发生多处地质灾害，造成直接经济损失8.3亿元。降雨结束后，西和县抢抓有利天气，组织党员干部持续开展道路和河流清障，人饮工程和电力设施抢修，房前屋后泥土和落石清理工作，全力保障交通通畅。目前群众生活稳定有序。&nbsp; 本报记者 王&nbsp; 钊 摄影报道 \r\n" + 
//        		" <p align=\"\" style=\"margin-top: 0px; margin-bottom: 0px; text-indent: 2em; text-align: justify;\"></p> \r\n" + 
//        		" <p align=\"\" style=\"margin-top: 0px; margin-bottom: 0px; text-indent: 2em; text-align: justify;\"></p> \r\n" + 
//        		"</div>";
//        List<String> imgList = Lists.newArrayList("http://www.gszx.gov.cn/HdAtt/img/2020/08/20200831171329719001.jpg","http://www.gszx.gov.cn/HdAtt/img/2020/08/20200831171329438002.jpg");
//        createMhtArchive(content, Lists.newArrayList(), "UTF-8", imgList, "E:/spider/test.mht");
//        //Html2MHTCompiler.mht2html("c:\\test.mht", "c:\\test.htm");
//    }
    
    
    public static void main(String[] args) {
    	String strUrl = "http://www.qxnrb.com/epaper/qxnrb/res/2020-09/04/01/qxnrbp33_b@600_378.jpg"; 
    	String url2 = "http://www.gszx.gov.cn/HdAtt/img/2020/08/20200831165649496001.jpg";
    	byte [] arrs = cn.cnki.spider.util.JQuery.downBinaryFile(strUrl);
    	
    }

    /**
     *
     *
     * 方法说明：初始化
     *
     *
     * 输入参数：strText 网页文本内容; strUrl 网页地址; strEncoding 网页编码; strFileName 本地文件名
     *
     *
     * 返回类型：
     *
     */
    public Html2MHTCompiler(String strText, String strUrl, String strEncoding,
            String strFileName) {
        try {
            strWeb = new URL(strUrl);
        } catch (MalformedURLException e) {

            e.printStackTrace();
            return;
        }
        this.strText = strText;
        this.strEncoding = strEncoding;
        this.strFileName = strFileName;
    }

    /**
     *
     *
     * 方法说明：执行下载操作
     *
     *
     * 输入参数：
     *
     *
     * 返回类型：
     *
     */
    public boolean compile() {
        if (strWeb == null || strText == null || strFileName == null
                || strEncoding == null)
            return false;
        HashMap urlMap = new HashMap();
        NodeList nodes = new NodeList();
        try {
            Parser parser = createParser(strText);
            parser.setEncoding(strEncoding);
            nodes = parser.parse(null);
        } catch (ParserException e) {
            e.printStackTrace();
        }
        extractAllScriptNodes(nodes);
        ArrayList urlScriptList = extractAllScriptNodes(nodes, urlMap);
        ArrayList urlImageList = extractAllImageNodes(nodes, urlMap);
        for (Iterator iter = urlMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            strText = cn.cnki.spider.util.JHtmlClear.replace(strText, val, key);
        }
        try {
            createMhtArchive(strText, urlScriptList, urlImageList);
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *
     *
     * 方法说明：建立HTML parser
     *
     *
     * 输入参数：inputHTML 网页文本内容
     *
     *
     * 返回类型：HTML parser
     *
     */
    private Parser createParser(String inputHTML) {

        Lexer mLexer = new Lexer(new Page(inputHTML));
        return new Parser(mLexer, new DefaultParserFeedback(
                DefaultParserFeedback.QUIET));
    }

    /**
     *
     *
     * 方法说明：抽取基础URL地址
     *
     *
     * 输入参数：nodes 网页标签集合
     *
     *
     * 返回类型：
     *
     */
    private void extractAllScriptNodes(NodeList nodes) {
        NodeList filtered = nodes.extractAllNodesThatMatch(new TagNameFilter(
                "BASE"), true);
        if (filtered != null && filtered.size() > 0) {
            Tag tag = (Tag) filtered.elementAt(0);
            String href = tag.getAttribute("href");
            if (href != null && href.length() > 0) {
                try {
                    strWeb = new URL(href);
                } catch (MalformedURLException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *
     *
     * 方法说明：抽取网页包含的css,js链接
     *
     *
     * 输入参数：nodes 网页标签集合; urlMap 已存在的url集合
     *
     *
     * 返回类型：css,js链接的集合
     *
     */
    private ArrayList extractAllScriptNodes(NodeList nodes, HashMap urlMap) {
        ArrayList urlList = new ArrayList();
        NodeList filtered = nodes.extractAllNodesThatMatch(new TagNameFilter(
                "script"), true);
        for (int i = 0; i < filtered.size(); i++) {
            Tag tag = (Tag) filtered.elementAt(i);
            String src = tag.getAttribute("src");
            // Handle external css file's url
            if (src != null && src.length() > 0) {
                String innerURL = src;
                String absoluteURL = makeAbsoluteURL(strWeb, innerURL);
                if (absoluteURL != null && !urlMap.containsKey(absoluteURL)) {
                    urlMap.put(absoluteURL, innerURL);
                    ArrayList urlInfo = new ArrayList();
                    urlInfo.add(innerURL);
                    urlInfo.add(absoluteURL);
                    urlList.add(urlInfo);
                }
                tag.setAttribute("src", absoluteURL);
            }
        }

        filtered = nodes.extractAllNodesThatMatch(new TagNameFilter("link"),
                true);
        for (int i = 0; i < filtered.size(); i++) {
            Tag tag = (Tag) filtered.elementAt(i);
            String type = (tag.getAttribute("type"));
            String rel = (tag.getAttribute("rel"));
            String href = tag.getAttribute("href");

            boolean isCssFile = false;
            if (rel != null) {
                isCssFile = rel.indexOf("stylesheet") != -1;
            } else if (type != null) {
                isCssFile |= type.indexOf("text/css") != -1;
            }
            // Handle external css file's url
            if (isCssFile && href != null && href.length() > 0) {
                String innerURL = href;
                String absoluteURL = makeAbsoluteURL(strWeb, innerURL);
                if (absoluteURL != null && !urlMap.containsKey(absoluteURL)) {
                    urlMap.put(absoluteURL, innerURL);
                    ArrayList urlInfo = new ArrayList();
                    urlInfo.add(innerURL);
                    urlInfo.add(absoluteURL);
                    urlList.add(urlInfo);
                }
                tag.setAttribute("href", absoluteURL);
            }
        }

        return urlList;
    }

    /**
     *
     *
     * 方法说明：抽取网页包含的图像链接
     *
     *
     * 输入参数：nodes 网页标签集合; urlMap 已存在的url集合
     *
     *
     * 返回类型：图像链接集合
     *
     */
    private ArrayList extractAllImageNodes(NodeList nodes, HashMap urlMap) {
        ArrayList urlList = new ArrayList();
        NodeList filtered = nodes.extractAllNodesThatMatch(new TagNameFilter(
                "IMG"), true);
        for (int i = 0; i < filtered.size(); i++) {
            Tag tag = (Tag) filtered.elementAt(i);
            String src = tag.getAttribute("src");
            // Handle external css file's url
            if (src != null && src.length() > 0) {
                String innerURL = src;
                String absoluteURL = makeAbsoluteURL(strWeb, innerURL);
                if (absoluteURL != null && !urlMap.containsKey(absoluteURL)) {
                    urlMap.put(absoluteURL, innerURL);
                    ArrayList urlInfo = new ArrayList();
                    urlInfo.add(innerURL);
                    urlInfo.add(absoluteURL);
                    urlList.add(urlInfo);
                }
                tag.setAttribute("src", absoluteURL);
            }
        }

        return urlList;
    }

    /**
     *
     *
     * 方法说明：相对路径转绝对路径
     *
     *
     * 输入参数：strWeb 网页地址; innerURL 相对路径链接
     *
     *
     * 返回类型：绝对路径链接
     *
     */
    public static String makeAbsoluteURL(URL strWeb, String innerURL) {

        // 去除后缀
        int pos = innerURL.indexOf("?");
        if (pos != -1) {
            innerURL = innerURL.substring(0, pos);
        }
        if (innerURL != null && innerURL.toLowerCase().indexOf("http") == 0) {
            System.out.println(innerURL);
            return innerURL;
        }

        URL linkUri = null;
        try {
            linkUri = new URL(strWeb, innerURL);
        } catch (MalformedURLException e) {

            e.printStackTrace();
            return null;
        }

        String absURL = linkUri.toString();
        absURL = cn.cnki.spider.util.JHtmlClear.replace(absURL, "../", "");
        absURL = cn.cnki.spider.util.JHtmlClear.replace(absURL, "./", "");
        System.out.println(absURL);
        return absURL;
    }

    /**
     *
     *
     * 方法说明：创建mht文件
     *
     *
     * 输入参数：content 网页文本内容; urlScriptList 脚本链接集合; urlImageList 图片链接集合
     *
     *
     * 返回类型：
     *
     */
    private void createMhtArchive(String content, ArrayList urlScriptList,
            ArrayList urlImageList) throws Exception {
        MimeMultipart mp = new MimeMultipart("related");
        Properties props = new Properties();
        props.put("mail.smtp.host", smtp);
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage msg = new MimeMessage(session);
        msg.setHeader("X-Mailer", "Code Manager .SWT");
        if (from != null) {
            msg.setFrom(new InternetAddress(from));
        }
        if (subject != null) {
            msg.setSubject(subject);
        }
        if (to != null) {
            InternetAddress[] toAddresses = getInetAddresses(to);
            msg.setRecipients(Message.RecipientType.TO, toAddresses);
        }
        if (cc != null) {
            InternetAddress[] ccAddresses = getInetAddresses(cc);
            msg.setRecipients(Message.RecipientType.CC, ccAddresses);
        }
        if (bcc != null) {
            InternetAddress[] bccAddresses = getInetAddresses(bcc);
            msg.setRecipients(Message.RecipientType.BCC, bccAddresses);
        }
        // 设置网页正文
        MimeBodyPart bp = new MimeBodyPart();
        bp.setText(content, strEncoding);
        bp.addHeader("Content-Type", "text/html;charset=" + strEncoding);
        bp.addHeader("Content-Location", strWeb.toString());
        mp.addBodyPart(bp);
        int urlCount = urlScriptList.size();
        for (int i = 0; i < urlCount; i++) {
            bp = new MimeBodyPart();
            ArrayList urlInfo = (ArrayList) urlScriptList.get(i);
            // String url = urlInfo.get(0).toString();
            String absoluteURL = urlInfo.get(1).toString();
            bp.addHeader("Content-Location", javax.mail.internet.MimeUtility
                    .encodeWord(java.net.URLDecoder.decode(absoluteURL,
                            strEncoding)));
            DataSource source = new AttachmentDataSource(absoluteURL, "text");
            bp.setDataHandler(new DataHandler(source));
            mp.addBodyPart(bp);
        }

        urlCount = urlImageList.size();
        for (int i = 0; i < urlCount; i++) {
            bp = new MimeBodyPart();
            ArrayList urlInfo = (ArrayList) urlImageList.get(i);
            // String url = urlInfo.get(0).toString();
            String absoluteURL = urlInfo.get(0).toString();
            System.out.println(urlInfo.get(0).toString() + " +++ " + urlInfo.get(1));
            bp.addHeader("Content-Location", javax.mail.internet.MimeUtility
                    .encodeWord(java.net.URLDecoder.decode(absoluteURL,
                            strEncoding)));
            DataSource source = new AttachmentDataSource(absoluteURL, "image");
            bp.setDataHandler(new DataHandler(source));
            mp.addBodyPart(bp);
        }
        msg.setContent(mp);
        // write the mime multi part message to a file
        msg.writeTo(new FileOutputStream(strFileName));
    }

    public static void createMhtArchive(String content, ArrayList urlScriptList, String encoding,
            List<String> urlImageList, String mhtFile) throws Exception {
        MimeMultipart mp = new MimeMultipart("related");
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage msg = new MimeMessage(session);
        msg.setHeader("X-Mailer", "Code Manager .SWT");
        // 设置网页正文
        MimeBodyPart bp = new MimeBodyPart();
        bp.setText(content, encoding);
        bp.addHeader("Content-Type", "text/html;charset=" + "UTF-8");
        bp.addHeader("Content-Location", "");
        mp.addBodyPart(bp);
        int urlCount = urlScriptList.size();
        for (int i = 0; i < urlCount; i++) {
            bp = new MimeBodyPart();
            ArrayList urlInfo = (ArrayList) urlScriptList.get(i);
            // String url = urlInfo.get(0).toString();
            String absoluteURL = urlInfo.get(1).toString();
            bp.addHeader("Content-Location", javax.mail.internet.MimeUtility
                    .encodeWord(java.net.URLDecoder.decode(absoluteURL,
                            encoding)));
            DataSource source = new cn.cnki.spider.util.StaticAttachmentDataSource(URLEncoder.encode(absoluteURL, encoding), "text");
            bp.setDataHandler(new DataHandler(source));
            mp.addBodyPart(bp);
        }
        urlCount = urlImageList.size();
        for (int i = 0; i < urlCount; i++) {
            bp = new MimeBodyPart();
            String urlInfo = urlImageList.get(i);
            // String url = urlInfo.get(0).toString();
            bp.addHeader("Content-Location", javax.mail.internet.MimeUtility
                    .encodeWord(java.net.URLDecoder.decode(urlInfo,
                    		encoding)));
            DataSource source = new cn.cnki.spider.util.StaticAttachmentDataSource(urlInfo, "image");
            bp.setDataHandler(new DataHandler(source));
            mp.addBodyPart(bp);
        }
        msg.setContent(mp);
        // write the mime multi part message to a file
        File file = new File(mhtFile);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        @Cleanup OutputStream os = new FileOutputStream(file);
        msg.writeTo(os);
    }

    /**
     *
     *
     * 方法说明：mht转html
     *
     *
     * 输入参数：strMht mht文件路径; strHtml html文件路径
     *
     *
     * 返回类型：
     *
     */
    public static void mht2html(String strMht, String strHtml) {
        try {

            InputStream fis = new FileInputStream(strMht);
            Session mailSession = Session.getDefaultInstance(System
                    .getProperties(), null);
            MimeMessage msg = new MimeMessage(mailSession, fis);
            Object content = msg.getContent();
            if (content instanceof Multipart) {
                MimeMultipart mp = (MimeMultipart) content;
                MimeBodyPart bp1 = (MimeBodyPart) mp.getBodyPart(0);
                String strEncodng = getEncoding(bp1);
                String strText = getHtmlText(bp1, strEncodng);
                if (strText == null)
                    return;
                File parent = null;
                if (mp.getCount() > 1) {
                    parent = new File(new File(strHtml).getAbsolutePath() + ".files");
                    parent.mkdirs();
                    if (!parent.exists())
                        return;
                }
                for (int i = 1; i < mp.getCount(); ++i) {
                    MimeBodyPart bp = (MimeBodyPart) mp.getBodyPart(i);
                    String strUrl = getResourcesUrl(bp);
                    if (strUrl == null)
                        continue;
                    DataHandler dataHandler = bp.getDataHandler();
                    MimePartDataSource source = (MimePartDataSource) dataHandler
                            .getDataSource();
                    File resources = new File(parent.getAbsolutePath()
                            + File.separator + getName(strUrl, i));
                    if (saveResourcesFile(resources, bp.getInputStream()))
                        strText = cn.cnki.spider.util.JHtmlClear.replace(strText, strUrl, resources
                                .getAbsolutePath());
                }
                saveHtml(strText, strHtml);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     *
     *
     * 方法说明：得到资源文件的name
     *
     *
     * 输入参数：strName 资源文件链接, ID 资源文件的序号
     *
     *
     * 返回类型：资源文件的本地临时文件名
     *
     */
    public static String getName(String strName, int ID) {
        char separator = '/';
        System.out.println(strName);
        System.out.println(separator);
        if (strName.lastIndexOf(separator) >= 0)
            return format(strName.substring(strName.lastIndexOf(separator) + 1));
        return "temp" + ID;
    }

    /**
     *
     *
     * 方法说明：得到网页编码
     *
     *
     * 输入参数：bp MimeBodyPart类型的网页内容
     *
     *
     * 返回类型：MimeBodyPart里的网页内容的编码
     *
     */
    private static String getEncoding(MimeBodyPart bp) {
        if (bp != null) {
            try {
                Enumeration list = bp.getAllHeaders();
                while (list.hasMoreElements()) {
                    javax.mail.Header head = (javax.mail.Header) list
                            .nextElement();
                    if (head.getName().compareTo("Content-Type") == 0) {
                        String strType = head.getValue();
                        int pos = strType.indexOf("charset=");
                        if (pos != -1) {
                            String strEncoding = strType.substring(pos + 8, strType.length());
                            if (strEncoding.toLowerCase().compareTo("gb2312") == 0) {
                                strEncoding = "gbk";
                            }
                            return strEncoding;
                        }
                    }
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     *
     * 方法说明：得到资源文件url
     *
     *
     * 输入参数：bp MimeBodyPart类型的网页内容
     *
     *
     * 返回类型：资源文件url
     *
     */
    private static String getResourcesUrl(MimeBodyPart bp) {
        if (bp != null) {
            try {
                Enumeration list = bp.getAllHeaders();
                while (list.hasMoreElements()) {
                    javax.mail.Header head = (javax.mail.Header) list
                            .nextElement();
                    if (head.getName().compareTo("Content-Location") == 0) {
                        return head.getValue();
                    }
                }
            } catch (MessagingException e) {

                e.printStackTrace();
            }

        }
        return null;
    }

    /**
     *
     *
     * 方法说明：格式化文件名
     *
     *
     * 输入参数：strName 文件名
     *
     *
     * 返回类型：经过处理的符合命名规则的文件名
     *
     */
    private static String format(String strName) {
        if (strName == null)
            return null;
        strName = strName.replaceAll("     ", " ");
        String strText = "\\/:*?\"<>|^___FCKpd___0quot;";
        for (int i = 0; i < strName.length(); ++i) {
            String ch = String.valueOf(strName.charAt(i));
            if (strText.indexOf(ch) != -1) {
                strName = strName.replace(strName.charAt(i), '-');
            }
        }
        return strName;
    }

    /**
     *
     *
     * 方法说明：保存资源文件
     *
     *
     * 输入参数：resources 要创建的资源文件; inputStream 要输入文件中的流
     *
     *
     * 返回类型：boolean
     *
     */
    private static boolean saveResourcesFile(File resources,
            InputStream inputStream) {
        if (resources == null || inputStream == null) {
            return false;
        }
        BufferedInputStream in = null;
        FileOutputStream fio = null;
        BufferedOutputStream osw = null;
        try {
            in = new BufferedInputStream(inputStream);
            fio = new FileOutputStream(resources);
            osw = new BufferedOutputStream(new DataOutputStream(fio));
            int b;
            byte[] a = new byte[1024];
            boolean isEmpty = true;
            while ((b = in.read(a)) != -1) {
                isEmpty = false;
                osw.write(a, 0, b);
                osw.flush();
            }
            osw.close();
            fio.close();
            in.close();
            inputStream.close();
            if (isEmpty)
                resources.delete();
            return true;
        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("解析mht失败");
            return false;
        } finally {
            try {
                if (osw != null)
                    osw.close();
                if (fio != null)
                    fio.close();
                if (in != null)
                    in.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("解析mht失败");
                return false;
            }
        }
    }

    /**
     *
     *
     * 方法说明：得到mht文件的标题
     *
     *
     * 输入参数：mhtFilename mht文件名
     *
     *
     * 返回类型：mht文件的标题
     *
     */
    public static String getTitle(String mhtFilename) {
        try {

            InputStream fis = new FileInputStream(mhtFilename);
            Session mailSession = Session.getDefaultInstance(System
                    .getProperties(), null);
            MimeMessage msg = new MimeMessage(mailSession, fis);
            Object content = msg.getContent();
            if (content instanceof Multipart) {
                MimeMultipart mp = (MimeMultipart) content;
                MimeBodyPart bp1 = (MimeBodyPart) mp.getBodyPart(0);
                String strEncodng = getEncoding(bp1);
                String strText = getHtmlText(bp1, strEncodng);
                if (strText == null)
                    return null;
                strText = strText.toLowerCase();
                int pos1 = strText.indexOf("<title>");
                int pos2 = strText.indexOf("</title>");
                if (pos1 != -1 && pos2 != -1 && pos2 > pos1) {
                    return strText.substring(pos1 + 7, pos2).trim();
                }
            }
            return null;
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     *
     * 方法说明：得到html文本
     *
     *
     * 输入参数：bp MimeBodyPart类型的网页内容; strEncoding 内容编码
     *
     *
     * 返回类型：html文本
     *
     */
    private static String getHtmlText(MimeBodyPart bp, String strEncoding) {
        InputStream textStream = null;
        BufferedInputStream buff = null;
        BufferedReader br = null;
        Reader r = null;
        try {
            textStream = bp.getInputStream();
            buff = new BufferedInputStream(textStream);
            r = new InputStreamReader(buff, strEncoding);
            br = new BufferedReader(r);
            StringBuffer strHtml = new StringBuffer("");
            String strLine = null;
            while ((strLine = br.readLine()) != null) {
                strHtml.append(strLine + "\r\n");
            }
            br.close();
            r.close();
            textStream.close();
            return strHtml.toString();
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if (buff != null)
                    buff.close();
                if (textStream != null)
                    textStream.close();
            } catch (Exception e) {
                System.out.println("解析mht失败");
            }
        }
        return null;
    }

    /**
     *
     *
     * 方法说明：保存html文件
     *
     *
     * 输入参数：strText html内容; strHtml html文件名
     *
     *
     * 返回类型：
     *
     */
    private static void saveHtml(String strText, String strHtml) {
        try {
            FileWriter fw = new FileWriter(strHtml);
            fw.write(strText);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("解析mht失败");
        }
    }

    private InternetAddress[] getInetAddresses(String emails) throws Exception {
        ArrayList list = new ArrayList();
        StringTokenizer tok = new StringTokenizer(emails, ",");
        while (tok.hasMoreTokens()) {
            list.add(tok.nextToken());
        }
        int count = list.size();
        InternetAddress[] addresses = new InternetAddress[count];
        for (int i = 0; i < count; i++) {
            addresses[i] = new InternetAddress(list.get(i).toString());
        }
        return addresses;
    }

    class AttachmentDataSource implements DataSource {
        private MimetypesFileTypeMap map = new MimetypesFileTypeMap();
        private String strUrl;
        private String strType;
        private byte[] dataSize = null;

        /**
         *
         * This is some content type maps.
         *
         */
        private Map normalMap = new HashMap();
        {
            // Initiate normal mime type map
            // Images
            normalMap.put("image", "image/jpeg");
            normalMap.put("text", "text/plain");
        }

        public AttachmentDataSource(String strUrl, String strType) {
            this.strType = strType;
            this.strUrl = strUrl;

            strUrl = strUrl.trim();
            strUrl = strUrl.replaceAll(" ", "%20");
            dataSize = cn.cnki.spider.util.JQuery.downBinaryFile(strUrl);
        }

        /**
         *
         * Returns the content type.
         *
         */
        public String getContentType() {
            return getMimeType(getName());
        }

        public String getName() {
            char separator = File.separatorChar;
            if (strUrl.lastIndexOf(separator) >= 0)
                return strUrl.substring(strUrl.lastIndexOf(separator) + 1);
            return strUrl;
        }

        private String getMimeType(String fileName) {
            String type = (String) normalMap.get(strType);
            if (type == null) {
                try {
                    type = map.getContentType(fileName);
                } catch (Exception e) {

                }
                System.out.println(type);
                // Fix the null exception
                if (type == null) {
                    type = "application/octet-stream";
                }
            }

            return type;
        }

        public InputStream getInputStream() throws IOException {

            if (dataSize == null)
                dataSize = new byte[0];
            return new ByteArrayInputStream(dataSize);
        }

        public OutputStream getOutputStream() throws IOException {

            return new ByteArrayOutputStream();
        }

    }
}

class StaticAttachmentDataSource implements DataSource {
    private MimetypesFileTypeMap map = new MimetypesFileTypeMap();
    private String strUrl;
    private String strType;
    private byte[] dataSize = null;

    /**
     *
     * This is some content type maps.
     *
     */
    private Map normalMap = new HashMap();
    {
        // Initiate normal mime type map
        // Images
        normalMap.put("image", "image/jpeg");
        normalMap.put("text", "text/plain");
    }

    public StaticAttachmentDataSource(String strUrl, String strType) {
        this.strType = strType;
        this.strUrl = strUrl;

        strUrl = strUrl.trim();
        strUrl = strUrl.replaceAll(" ", "%20");
        dataSize = cn.cnki.spider.util.JQuery.downBinaryFile(strUrl);
    }
    
    /**
     *
     * Returns the content type.
     *
     */
    public String getContentType() {
        return getMimeType(getName());
    }

    public String getName() {
        char separator = File.separatorChar;
        if (strUrl.lastIndexOf(separator) >= 0)
            return strUrl.substring(strUrl.lastIndexOf(separator) + 1);
        return strUrl;
    }

    private String getMimeType(String fileName) {
        String type = (String) normalMap.get(strType);
        if (type == null) {
            try {
                type = map.getContentType(fileName);
            } catch (Exception e) {

            }
            System.out.println(type);
            // Fix the null exception
            if (type == null) {
                type = "application/octet-stream";
            }
        }

        return type;
    }

    public InputStream getInputStream() throws IOException {

        if (dataSize == null)
            dataSize = new byte[0];
        return new ByteArrayInputStream(dataSize);
    }

    public OutputStream getOutputStream() throws IOException {

        return new ByteArrayOutputStream();
    }

}

class JHtmlClear {
    public static String replace(String s, String s1, String s2) {
        return s.replace(s1, s2);
    }
}

class JQuery {
    public static String getHtmlText(String strUrl, String strEncoding) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            return new String(cn.cnki.spider.util.JQuery.getBytes(in), strEncoding);
        } catch (Exception e) {
            return "";
        }
    }

    public static byte[] downBinaryFile(String s) {
        try {
            URL url = new URL(s);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36)");
            DataInputStream in = new DataInputStream(connection
                    .getInputStream());
            return cn.cnki.spider.util.JQuery.getBytes(in);
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }

    public static byte[] getBytes(InputStream is) throws Exception {
        byte[] data = null;
        Collection chunks = new ArrayList();
        byte[] buffer = new byte[1024 * 1000];
        int read = -1;
        int size = 0;
        while ((read = is.read(buffer)) != -1) {
            if (read > 0) {
                byte[] chunk = new byte[read];
                System.arraycopy(buffer, 0, chunk, 0, read);
                chunks.add(chunk);
                size += chunk.length;
            }
        }
        if (size > 0) {
            ByteArrayOutputStream bos = null;
            try {
                bos = new ByteArrayOutputStream(size);
                for (Iterator itr = chunks.iterator(); itr.hasNext();) {
                    byte[] chunk = (byte[]) itr.next();
                    bos.write(chunk);
                }
                data = bos.toByteArray();
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        }
        return data;
    }
}