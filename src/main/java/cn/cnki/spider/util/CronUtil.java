package cn.cnki.spider.util;

public class CronUtil {

    public static String translateToChinese(String cronExp) {
        if (cronExp == null || cronExp.length() < 1) {
            return "cron表达式为空";
        }

        String[] tmpCorns = cronExp.split(" ");
        StringBuffer sBuffer = new StringBuffer();
        if (tmpCorns.length == 6) {
            //解析月
            if (!tmpCorns[4].equals("*") && !tmpCorns[4].equals("?")) {
                if (tmpCorns[4].contains("/")) {
                    sBuffer.append("从").append(tmpCorns[4].split("/")[0]).append("号开始").append(",每").append
                            (tmpCorns[4].split("/")[1]).append("月");
                } else {
                    sBuffer.append("每年").append(tmpCorns[4]).append("月");
                }
            }

            //解析周
            if (!tmpCorns[5].equals("*") && !tmpCorns[5].equals("?")) {
                if (tmpCorns[5].contains(",")) {
                    sBuffer.append("每周的第").append(tmpCorns[5]).append("天");
                } else {
                    sBuffer.append("每周");
                    char[] tmpArray = tmpCorns[5].toCharArray();
                    for (char tmp : tmpArray) {
                        switch (tmp) {
                            case '1':
                                sBuffer.append("日");
                                break;
                            case '2':
                                sBuffer.append("一");
                                break;
                            case '3':
                                sBuffer.append("二");
                                break;
                            case '4':
                                sBuffer.append("三");
                                break;
                            case '5':
                                sBuffer.append("四");
                                break;
                            case '6':
                                sBuffer.append("五");
                                break;
                            case '7':
                                sBuffer.append("六");
                                break;
                            default:
                                sBuffer.append(tmp);
                                break;
                        }
                    }
                }
            }

            //解析日
            if (!tmpCorns[3].equals("?")) {
                if (sBuffer.toString().contains("一") && sBuffer.toString().contains("二") && sBuffer.toString()
                        .contains("三")
                        && sBuffer.toString().contains("四") && sBuffer.toString().contains("五") && sBuffer.toString()
                        .contains("六")
                        && sBuffer.toString().contains("日")) {
                }

                if (!tmpCorns[3].equals("*")) {
                    if (tmpCorns[3].contains("/")) {
                        sBuffer.append("每周从第").append(tmpCorns[3].split("/")[0]).append("天开始").append(",每").append
                                (tmpCorns[3].split("/")[1]).append("天");
                    } else {
                        sBuffer.append("每月第").append(tmpCorns[3]).append("天");
                    }
                }
            }

            //解析时
            if (!tmpCorns[2].equals("*")) {
                if (tmpCorns[2].contains("/")) {
                    sBuffer.append("从").append(tmpCorns[2].split("/")[0]).append("点开始").append(",每").append
                            (tmpCorns[2].split("/")[1]).append("小时");
                } else {
                    if (!(sBuffer.toString().length() > 0)) {
                        sBuffer.append("每天").append(tmpCorns[2]).append("点");
                    }
                }
            }

            //解析分
            if (!tmpCorns[1].equals("*")) {
                if (tmpCorns[1].contains("/")) {
                    sBuffer.append("从第").append(tmpCorns[1].split("/")[0]).append("分开始").append(",每").append
                            (tmpCorns[1].split("/")[1]).append("分");
                } else if (tmpCorns[1].equals("0")) {

                } else {
                    sBuffer.append(tmpCorns[1]).append("分");
                }
            }
            if (sBuffer.toString().length() > 0) {
                sBuffer.append("执行一次");
            } else {
                sBuffer.append("表达式中文转换异常");
            }
        }
        return sBuffer.toString();

    }
}
