package cn.cnki.spider.util;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelExport {
    private static final Logger logger = LoggerFactory.getLogger(ExcelExport.class);
    HttpServletResponse response;
    // 文件名  
    private String fileName;
    //文件保存路径  
    private String fileDir;
    //sheet名  
    private String sheetName;
    //表头字体  
    private String titleFontType = "Arial Unicode MS";
    //表头背景色  
    private String titleBackColor = "C1FBEE";
    //表头字号  
    private short titleFontSize = 12;
    //添加自动筛选的列 如 A:M  
    private String address = "";
    //正文字体  
    private String contentFontType = "Arial Unicode MS";
    //正文字号  
    private short contentFontSize = 12;
    //Float类型数据小数位  
    private String floatDecimal = "0.00";
    //Double类型数据小数位  
    private String doubleDecimal = "0.00";
    //设置列的公式  
    private String colFormula[] = null;

    DecimalFormat floatDecimalFormat = new DecimalFormat(floatDecimal);
    DecimalFormat doubleDecimalFormat = new DecimalFormat(doubleDecimal);

    private HSSFWorkbook workbook = null;

    public ExcelExport() {

    }

    public ExcelExport(String fileDir, String sheetName) {
        this.fileDir = fileDir;
        this.sheetName = sheetName;
        workbook = new HSSFWorkbook();
    }

    public ExcelExport(HttpServletResponse response, String fileName, String sheetName) {
        this.fileName = fileName;
        this.response = response;
        this.sheetName = sheetName;
        workbook = new HSSFWorkbook();
    }

    /**
     * 设置表头字体.
     *
     * @param titleFontType
     */
    public void setTitleFontType(String titleFontType) {
        this.titleFontType = titleFontType;
    }

    /**
     * 设置表头背景色.
     *
     * @param titleBackColor 十六进制
     */
    public void setTitleBackColor(String titleBackColor) {
        this.titleBackColor = titleBackColor;
    }

    /**
     * 设置表头字体大小.
     *
     * @param titleFontSize
     */
    public void setTitleFontSize(short titleFontSize) {
        this.titleFontSize = titleFontSize;
    }

    /**
     * 设置表头自动筛选栏位,如A:AC.
     *
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 设置正文字体.
     *
     * @param contentFontType
     */
    public void setContentFontType(String contentFontType) {
        this.contentFontType = contentFontType;
    }

    /**
     * 设置正文字号.
     *
     * @param contentFontSize
     */
    public void setContentFontSize(short contentFontSize) {
        this.contentFontSize = contentFontSize;
    }

    /**
     * 设置float类型数据小数位 默认.00
     *
     * @param doubleDecimal 如 ".00"
     */
    public void setDoubleDecimal(String doubleDecimal) {
        this.doubleDecimal = doubleDecimal;
    }

    /**
     * 设置doubel类型数据小数位 默认.00
     *
     * @param floatDecimalFormat 如 ".00
     */
    public void setFloatDecimalFormat(DecimalFormat floatDecimalFormat) {
        this.floatDecimalFormat = floatDecimalFormat;
    }

    /**
     * 设置列的公式
     *
     * @param colFormula 存储i-1列的公式 涉及到的行号使用@替换 如A@+B@
     */
    public void setColFormula(String[] colFormula) {
        this.colFormula = colFormula;
    }

    /**
     * 写excel.
     * xls方式
     *
     * @param titleColumn 对应bean的属性名
     * @param titleName   excel要导出的列名
     * @param titleSize   列宽
     * @param dataList    数据
     */
    public void writeExcel(String titleColumn[], String titleName[], List<?> dataList) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        //添加Worksheet（不添加sheet时生成的xls文件打开时会报错)  
        Sheet sheet = workbook.createSheet(this.sheetName);
        //新建文件  
        OutputStream out = null;
        try {
            if (fileDir != null) {
                //有文件路径  
                out = new FileOutputStream(fileDir);
            } else {
                //否则，直接写到输出流中
                out = response.getOutputStream();
                fileName = fileName + ".xls";
                response.setContentType("application/msexcel;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename="
                        + URLEncoder.encode(fileName, "UTF-8"));
            }

            //写入excel的表头  
            Row titleNameRow = workbook.getSheet(sheetName).createRow(0);
            //设置样式  
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle = (HSSFCellStyle) setFontAndBorder(titleStyle, titleFontType, (short) titleFontSize);
            titleStyle = (HSSFCellStyle) setColor(titleStyle, titleBackColor, (short) 10);

            for (int i = 0; i < titleName.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);    //设置宽度
                Cell cell = titleNameRow.createCell(i);
                cell.setCellStyle(titleStyle);
                cell.setCellValue(titleName[i].toString());
            }

            //为表头添加自动筛选  
            if (!"".equals(address)) {
                CellRangeAddress c = (CellRangeAddress) CellRangeAddress.valueOf(address);
                sheet.setAutoFilter(c);
            }

            //通过反射获取数据并写入到excel中  
            if (dataList != null && dataList.size() > 0) {
                //设置样式  
                HSSFCellStyle dataStyle = workbook.createCellStyle();
                titleStyle = (HSSFCellStyle) setFontAndBorder(titleStyle, contentFontType, (short) contentFontSize);

                if (titleColumn.length > 0) {
                    for (int rowIndex = 1; rowIndex <= dataList.size(); rowIndex++) {
                        Object obj = dataList.get(rowIndex - 1);     //获得该对象
                        Class clsss = obj.getClass();     //获得该对对象的class实例  
                        Row dataRow = workbook.getSheet(sheetName).createRow(rowIndex);
                        for (int columnIndex = 0; columnIndex < titleColumn.length; columnIndex++) {
                            String title = titleColumn[columnIndex].toString().trim();
                            if (!"".equals(title)) {  //字段不为空
                                //使首字母大写  
                                String UTitle = Character.toUpperCase(title.charAt(0)) + title.substring(1, title.length()); // 使其首字母大写;
                                String methodName = "get" + UTitle;

                                // 设置要执行的方法  
//                                Method method = clsss.getDeclaredMethod(methodName);
                                JSONObject json = (JSONObject) obj;
                                String data = json.getString(title);
                                String returnType = "String";
                                //获取返回类型  
//                                String returnType = method.getReturnType().getName();
//                                Object object = method.invoke(obj);
//                                String data = method.invoke(obj) == null ? "" : object.toString();
                                Cell cell = dataRow.createCell(columnIndex);
                                if (data != null && !"".equals(data)) {
                                    if ("int".equals(returnType)) {
                                        cell.setCellValue(Integer.parseInt(data));
                                    } else if ("long".equals(returnType)) {
                                        cell.setCellValue(Long.parseLong(data));
                                    } else if ("float".equals(returnType)) {
                                        cell.setCellValue(floatDecimalFormat.format(Float.parseFloat(data)));
                                    } else if ("double".equals(returnType)) {
                                        cell.setCellValue(doubleDecimalFormat.format(Double.parseDouble(data)));
                                    } else if (Date.class.getName().equals(returnType)) {
                                        cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(data));
                                    } else {
                                        cell.setCellValue(data);
                                    }
                                }
                            } else {   //字段为空 检查该列是否是公式
                                if (colFormula != null) {
                                    String sixBuf = colFormula[columnIndex].replace("@", (rowIndex + 1) + "");
                                    Cell cell = dataRow.createCell(columnIndex);
                                    cell.setCellFormula(sixBuf);
                                }
                            }
                        }
                    }
                }
            }
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.debug("导出写Excel异常");
                }
            }
        }
    }


    /**
     * xlsx方式
     *
     * @param response
     * @param fileName
     * @param titleColumn
     * @param titleName
     * @param titleSize
     * @param dataList
     */
    public void writeBigExcel(HttpServletResponse response, String fileName, String titleColumn[],
                              String titleName[], int titleSize[], List<?> dataList) {
        try {
            SXSSFWorkbook workbook = new SXSSFWorkbook(100);
            OutputStream out = response.getOutputStream();
            String lastFileName = fileName + ".xlsx";
            response.setContentType("application/msexcel;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + URLEncoder.encode(lastFileName, "UTF-8"));
            int k = 0;
            int rowIndex;
            Sheet sheet = workbook.createSheet(fileName + (k + 1));
            //写入excel的表头
            Row titleNameRow = workbook.getSheet(fileName + (k + 1)).createRow(0);
            for (int i = 0; i < titleName.length; i++) {
                sheet.setColumnWidth(i, titleSize[i] * 256);    //设置宽度
                Cell cell = titleNameRow.createCell(i);
                cell.setCellValue(titleName[i]);
            }
            //写入到excel中
            if (dataList != null && dataList.size() > 0) {
                if (titleColumn.length > 0) {
                    for (int index = 0; index < dataList.size(); index++) {
                        //每个sheet3W条数据
                        if (index != 0 && (index) % 30000 == 0) {
                            k = k + 1;
                            sheet = workbook.createSheet(fileName + (k + 1));
                            //写入excel的表头
                            titleNameRow = workbook.getSheet(fileName + (k + 1)).createRow(0);
                            for (int i = 0; i < titleName.length; i++) {
                                sheet.setColumnWidth(i, titleSize[i] * 256);    //设置宽度
                                Cell cell = titleNameRow.createCell(i);
                                cell.setCellValue(titleName[i]);
                            }
                        }
                        if (index < 30000) {
                            rowIndex = index + 1;
                        } else {
                            rowIndex = index - 30000 * ((index) / 30000) + 1;
                        }
                        Object obj = dataList.get(index);
                        Class clazz = obj.getClass();
                        Row dataRow = workbook.getSheet(fileName + (k + 1)).createRow(rowIndex);
                        for (int columnIndex = 0; columnIndex < titleColumn.length; columnIndex++) {
                            String title = titleColumn[columnIndex].trim();
                            if (!"".equals(title)) {
                                // 获取返回类型
                                String UTitle = Character.toUpperCase(title.charAt(0)) + title.substring(1, title.length()); // 使其首字母大写;
                                String methodName = "get" + UTitle;
                                Method method = clazz.getDeclaredMethod(methodName);
                                String returnType = method.getReturnType().getName();
                                Object object = method.invoke(obj);
                                String data = method.invoke(obj) == null ? "" : object.toString();
                                Cell cell = dataRow.createCell(columnIndex);
                                if (data != null && !"".equals(data)) {
                                    if ("int".equals(returnType)) {
                                        cell.setCellValue(Integer.parseInt(data));
                                    } else if ("long".equals(returnType)) {
                                        cell.setCellValue(Long.parseLong(data));
                                    } else if ("float".equals(returnType)) {
                                        cell.setCellValue(new DecimalFormat("0.00").format(Float.parseFloat(data)));
                                    } else if ("double".equals(returnType)) {
                                        cell.setCellValue(new DecimalFormat("0.00").format(Double.parseDouble(data)));
                                    } else if (Date.class.getName().equals(returnType)) {
                                        cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(object));
                                    } else {
                                        cell.setCellValue(data);
                                    }
                                } else {   //字段为空 检查该列是否是公式
                                    if (colFormula != null) {
                                        String sixBuf = colFormula[columnIndex].replace("@", (rowIndex + 1) + "");
                                        cell = dataRow.createCell(columnIndex);
                                        cell.setCellFormula(sixBuf);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将16进制的颜色代码写入样式中来设置颜色
     *
     * @param style 保证style统一
     * @param color 颜色：66FFDD
     * @param index 索引 8-64 使用时不可重复
     * @return
     */
    public CellStyle setColor(CellStyle style, String color, short index) {
        if ("".equals(color)) {
            //转为RGB码  
            int r = Integer.parseInt((color.substring(0, 2)), 16);   //转为16进制
            int g = Integer.parseInt((color.substring(2, 4)), 16);
            int b = Integer.parseInt((color.substring(4, 6)), 16);
            //自定义cell颜色  
            HSSFPalette palette = workbook.getCustomPalette();
            palette.setColorAtIndex((short) index, (byte) r, (byte) g, (byte) b);

            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(index);
        }
        return style;
    }

    /**
     * 设置字体并加外边框
     *
     * @param style 样式
     * @param style 字体名
     * @param style 大小
     * @return
     */
    public CellStyle setFontAndBorder(CellStyle style, String fontName, short size) {
        HSSFFont font = workbook.createFont();
        font.setFontHeightInPoints(size);
        font.setFontName(fontName);
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN); //下边框
        style.setBorderLeft(BorderStyle.THIN);//左边框
        style.setBorderTop(BorderStyle.THIN);//上边框
        style.setBorderRight(BorderStyle.THIN);//右边框
        return style;
    }
}
