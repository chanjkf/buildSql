import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuildSql {
    private static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {
        String path = "C:\\Users\\yi\\Documents\\MyFile\\balance.xlsx";
        try {
            List<Balance> result;
            if (path.endsWith(".xls")) {
                result = readXls(path);
            } else {
                result = readXlsx(path);
            }
            buildSql(result);
        } catch (FileNotFoundException e) {
            System.out.println("file not find:"+e);
        } catch (IOException e) {
            System.out.println("io :"+e);
        } catch (Exception e) {
            System.out.println("exception:"+e);
        }
    }
    public static void buildSql(List<Balance> balances) throws IOException {
        String baseColumn = "user_id,\ncurrency_id,\navailable,\nhold,\nwithdraw_limit,\ncreate_time,\nmodify_time";
        String balanceTable = "user_currency_balance ";
        Date nowdate=new Date();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = Timestamp.valueOf(simpleDate.format(nowdate));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("C:\\Users\\yi\\Documents\\MyFile\\active.sql"))));
        for (Balance balance:balances) {
            StringBuilder balanceStr = new StringBuilder();
            StringBuilder billStr = new StringBuilder();
            balanceStr.append("INSERT INTO ")
                    .append(balanceTable)
                    .append("( " + "\n")
                    .append(baseColumn)
                    .append(") ").append(" \n")
                    .append(" VALUE").append(" \n")
                    .append("(")
                    .append(balance.getUserId() +", ")
                    .append(balance.getCurrencyId()+", ")
                    .append("0.0000000000000000,0.0000000000000000,-1.0000000000000000,")
                    .append("\""+timestamp+"\"").append(",").append("\n")
                    .append("\""+timestamp+"\"")
                    .append(")").append("\n")
                    .append("ON DUPLICATE KEY ")
                    .append("UPDATE available = available+")
                    .append(balance.getGiftSize())
                    .append(",modify_time=" + "\""+timestamp+"\";");
            writer.write(balanceStr.toString());
            writer.newLine();
            writer.flush();
            billStr.append("INSERT INTO user_bill ").append("\n ")
                    .append("(user_id, ").append("\n ")
                    .append("currency_id, ").append("\n ")
                    .append("product_id, ").append("\n ")
                    .append("type, ").append("\n ")
                    .append("size, ").append("\n ")
                    .append("before_balance, ").append("\n ")
                    .append("after_balance, ").append("\n ")
                    .append("refer_id, ").append("\n ")
                    .append("create_time, ").append("\n ")
                    .append("modify_time, ").append("\n ")
                    .append("side)").append("\n ")
                    .append("VALUES").append("\n")
                    .append("(")
                    .append(balance.getUserId() +", ")
                    .append(balance.getCurrencyId() + ",")
                    .append(0).append(", ")
                    .append(balance.getType()).append(",")
                    .append(balance.getGiftSize()).append(", ").append("\n")
                    .append("(select available-")
                    .append(balance.getGiftSize())
                    .append(" from user_currency_balance ")
                    .append("where user_id = ").append(balance.getUserId())
                    .append(" and currency_id = ").append(balance.getCurrencyId())
                    .append("),").append("\n")
                    .append("(")
                    .append("select available from user_currency_balance where user_id=").append(balance.getUserId())
                    .append(" and currency_id=").append(balance.getCurrencyId())
                    .append("), ").append("\n")
                    .append("0, ")
                    .append("\""+timestamp+"\"" +", ")
                    .append("\""+timestamp+"\"" +", ")
                    .append("0")
                    .append(");");
            writer.write(billStr.toString());
            writer.newLine();
            writer.newLine();
            writer.flush();
        }
        writer.close();




    }

    /**
     *
     * @Title: readXls
     * @Description: 处理xls文件
     * @param @param path
     * @param @return
     * @param @throws Exception    设定文件
     * @return List<List<String>>    返回类型
     * @throws
     *
     *
     * 1.先用InputStream获取excel文件的io流
     * 2.然后穿件一个内存中的excel文件HSSFWorkbook类型对象，这个对象表示了整个excel文件。
     * 3.对这个excel文件的每页做循环处理
     * 4.对每页中每行做循环处理
     * 5.对每行中的每个单元格做处理，获取这个单元格的值
     * 6.把这行的结果添加到一个List数组中
     * 7.把每行的结果添加到最后的总结果中
     * 8.解析完以后就获取了一个List<List<String>>类型的对象了
     *
     */
    private static List<Balance> readXls(String path) throws Exception {
        InputStream is = new FileInputStream(path);
        // HSSFWorkbook 标识整个excel
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
        List<Balance> balances = new ArrayList<>();
        int size = hssfWorkbook.getNumberOfSheets();
        // 循环每一页，并处理当前循环页
        for (int numSheet = 0; numSheet < size; numSheet++) {
            // HSSFSheet 标识某一页
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
            if (hssfSheet == null) {
                continue;
            }
            // 处理当前页，循环读取每一行
            for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
                // HSSFRow表示行
                HSSFRow hssfRow = hssfSheet.getRow(rowNum);
                Balance balance = new Balance();
                HSSFCell userIdCell = hssfRow.getCell(0);
                balance.setUserId(userIdCell.toString());
                HSSFCell currencyIdCell = hssfRow.getCell(1);
                balance.setCurrencyId(currencyIdCell.toString());
                HSSFCell giftSizeCell = hssfRow.getCell(2);
                balance.setGiftSize(giftSizeCell.toString());
                HSSFCell typeCell = hssfRow.getCell(3);
                balance.setType(typeCell.toString());
                balances.add(balance);
            }
        }
        return balances;
    }

    /**
     *
     * @Title: readXlsx
     * @Description: 处理Xlsx文件
     * @param @param path
     * @param @return
     * @param @throws Exception    设定文件
     * @return List<List<String>>    返回类型
     * @throws
     */
    private static List<Balance> readXlsx(String path) throws Exception {
        InputStream is = new FileInputStream(path);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(is);
        List<Balance> balances = new ArrayList<>();
        // 循环每一页，并处理当前循环页
        for (XSSFSheet xssfSheet : xssfWorkbook) {
            if (xssfSheet == null) {
                continue;
            }
            // 处理当前页，循环读取每一行
            for (int rowNum = 1; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
                XSSFRow xssfRow = xssfSheet.getRow(rowNum);
                Balance balance = new Balance();
                XSSFCell userIdCell = xssfRow.getCell(0);
                balance.setUserId(userIdCell.toString());
                XSSFCell currencyIdCell = xssfRow.getCell(1);
                balance.setCurrencyId(currencyIdCell.toString());
                XSSFCell giftSizeCell = xssfRow.getCell(2);
                balance.setGiftSize(giftSizeCell.toString());
                XSSFCell typeCell = xssfRow.getCell(3);
                balance.setType(typeCell.toString());
                balances.add(balance);
            }
        }
        return balances;
    }


    /**
     * 改造poi默认的toString（）方法如下
     * @Title: getStringVal
     * @Description: 1.对于不熟悉的类型，或者为空则返回""控制串
     *               2.如果是数字，则修改单元格类型为String，然后返回String，这样就保证数字不被格式化了
     * @param @param cell
     * @param @return    设定文件
     * @return String    返回类型
     * @throws
     */
    public static String getStringVal(HSSFCell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            case Cell.CELL_TYPE_NUMERIC:
                cell.setCellType(Cell.CELL_TYPE_STRING);
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                return "";
        }
    }
}
