package cn.cnki.spider.util;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Font;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.TableStyle;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.IndexedColors;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class ExcelUtil {


    /**
     * 导出 Excel ：一个 sheet，带表头
     *  @param response  HttpServletResponse
     * @param list      数据 list，每个元素为一个 BaseRowModel
     * @param fileName  导出的文件名
     * @param sheetName 导入文件的 sheet 名
     * @param object    映射实体类，Excel 模型
     */
    public static void writeExcel(HttpServletResponse response, List<JSONObject> list, String fileName,
                                  String sheetName, JSONObject object) throws Exception {
        ExcelWriter writer = new ExcelWriter(getOutputStream(fileName, response), ExcelTypeEnum.XLSX);
        Sheet sheet = new Sheet(1, 0, BaseRowModel.class);
        sheet.setSheetName(sheetName);

        TableStyle tableStyle = new TableStyle();
        tableStyle.setTableContentBackGroundColor(IndexedColors.WHITE);
        Font font = new Font();
        font.setFontHeightInPoints((short) 9);
        tableStyle.setTableHeadFont(font);
        tableStyle.setTableContentFont(font);
        sheet.setTableStyle(tableStyle);

        writer.write(list, sheet);
        writer.finish();
    }

    public static void download(HttpServletResponse response, Class t, List list) throws IOException, IllegalAccessException,InstantiationException {
        response.setContentType("application/vnd.ms-excel");// 设置文本内省
        response.setCharacterEncoding("utf-8");// 设置字符编码
        response.setHeader("Content-disposition", "attachment;filename="+ URLEncoder.encode("导出结果","UTF-8") + ".xlsx"); // 设置响应头
        EasyExcel.write(response.getOutputStream(), t).sheet("导出结果").doWrite(list); //用io流来写入数据
    }

    /**
     * 导出融资还款情况表
     *
     * @param response
     * @param list
     * @param fileName
     * @param sheetName
     * @param object
     */
    public static void writeFinanceRepayment(HttpServletResponse response, List<? extends BaseRowModel> list,
                                             String fileName, String sheetName, BaseRowModel object) throws Exception {
        ExcelWriter writer = new ExcelWriter(getOutputStream(fileName, response), ExcelTypeEnum.XLSX);
        Sheet sheet = new Sheet(1, 0, object.getClass());
        sheet.setSheetName(sheetName);
        sheet.setTableStyle(getTableStyle());
        writer.write(list, sheet);

        for (int i = 1; i <= list.size(); i += 4) {
            writer.merge(i, i + 3, 0, 0);
            writer.merge(i, i + 3, 1, 1);
        }

        writer.finish();
    }

    /**
     * 导出文件时为Writer生成OutputStream
     */
    private static OutputStream getOutputStream(String fileName, HttpServletResponse response) throws Exception {
        //创建本地文件
        fileName = fileName + ".xls";

        try {
            fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
            response.addHeader("Content-Disposition", "filename=" + fileName);

            return response.getOutputStream();
        } catch (Exception e) {

            throw new Exception("导出异常！");
        }
    }

    /**
     * 资金收支导出 Excel ：一个 sheet，带表头
     *
     * @param response  HttpServletResponse
     * @param list      数据 list，每个元素为一个 BaseRowModel
     * @param fileName  导出的文件名
     * @param sheetName 导入文件的 sheet 名
     * @param object    映射实体类，Excel 模型
     */
    public static void exportFundBudgetExcel(HttpServletResponse response, List<? extends BaseRowModel> list,
                                             String fileName, String sheetName, BaseRowModel object) throws Exception {
        ExcelWriter writer = new ExcelWriter(getOutputStream(fileName, response), ExcelTypeEnum.XLSX);
        Sheet sheet = new Sheet(1, 0, object.getClass());
        sheet.setSheetName(sheetName);
        sheet.setTableStyle(getTableStyle());

        writer.write(list, sheet);
        writer.merge(2, 3, 0, 0);
        writer.merge(4, 13, 0, 0);
        writer.merge(14, 14, 0, 1);
        writer.finish();
    }

    /**
     * 根据输入流，判断为xls还是xlsx，该方法原本存在于easyexcel 1.1.0 的ExcelTypeEnum中。
     */
    public static ExcelTypeEnum valueOf(InputStream inputStream) throws Exception {
        try {
            FileMagic fileMagic = FileMagic.valueOf(inputStream);

            if (FileMagic.OLE2.equals(fileMagic)) {
                return ExcelTypeEnum.XLS;
            }

            if (FileMagic.OOXML.equals(fileMagic)) {
                return ExcelTypeEnum.XLSX;
            }

            throw new Exception("excelTypeEnum can not null");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置全局样式
     *
     * @return
     */
    private static TableStyle getTableStyle() {
        TableStyle tableStyle = new TableStyle();

        tableStyle.setTableContentBackGroundColor(IndexedColors.WHITE);
        Font font = new Font();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        tableStyle.setTableHeadFont(font);
        Font fontContent = new Font();
        fontContent.setFontHeightInPoints((short) 9);
        tableStyle.setTableContentFont(fontContent);

        return tableStyle;
    }
}