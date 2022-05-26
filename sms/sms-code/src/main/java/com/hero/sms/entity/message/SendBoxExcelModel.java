package com.hero.sms.entity.message;

import java.io.Serializable;

import com.alibaba.excel.annotation.ExcelProperty;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(15)
public class SendBoxExcelModel implements Serializable{
	@ExcelProperty(value = "手机号码",index = 0)
    private String column0;
	
	@ExcelProperty(value ="参数B",index = 1)
    private String column1;
	
	@ExcelProperty(value ="参数C",index = 2)
    private String column2;
	
	@ExcelProperty(value ="参数D",index = 3)
    private String column3;
	
	@ExcelProperty(value ="参数E",index = 4)
    private String column4;
	
	@ExcelProperty(value ="参数F",index = 5)
	private String column5;
	
	@ExcelProperty(value ="参数G",index = 6)
	private String column6;
	
	@ExcelProperty(value ="参数H",index = 7)
	private String column7;
	
	@ExcelProperty(value ="参数I",index = 8)
	private String column8;
}
